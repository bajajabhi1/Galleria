package edu.columbia.cvml.galleria.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;
import edu.columbia.cvml.galleria.VO.FeatureValueObject;
import edu.columbia.cvml.galleria.async.AnnotatorRequestSenderAsync;
import edu.columbia.cvml.galleria.async.AsyncTaskRequestResponse;
import edu.columbia.cvml.galleria.async.FaceDetectorAsync;
import edu.columbia.cvml.galleria.util.ClusterFeatureManager;
import edu.columbia.cvml.galleria.util.FeatureJSONParser;
import edu.columbia.cvml.galleria.util.InvertedIndexManager;
import edu.columbia.cvml.galleria.util.ScalingUtility;
import edu.columbia.cvml.galleria.util.ScalingUtility.ScalingLogic;

public class ImageDetectorService extends Service
{

	private static final String LOG_TAG = "ImageDetectorService";
	private static final String PREFS_FILE_NAME = "ImageDetectorServicePref";
	private static final String PREFS_NAME_LAST_IMAGE_TIME = "PREFS_NAME_LAST_IMAGE_TIME";
	public static final int ANNO_DESIREDWIDTH = 640;
	public static final int ANNO_DESIREDHEIGHT = 480;
	private Set<String> lastProcessed = new LinkedHashSet<String>();

	private InvertedIndexManager annotatorIdxMapMgr = null;
	private InvertedIndexManager faceDetectorIdxMapMgr = null;
	private ClusterFeatureManager featureMgr = null;
	private int lastImageTime = 0;

	@Override
	public void onCreate()
	{
		Log.i(LOG_TAG, "ImageDetectorService::onCreate");
		super.onCreate();
	}


	@Override
	public void onDestroy()
	{
		Log.i(LOG_TAG, "ImageDetectorService::onDestroy");
		super.onDestroy();
	}


	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		Log.i(LOG_TAG, "ImageDetectorService::onStartCommand");
		// Restore preferences
		SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, MODE_PRIVATE);
		String default_time = System.currentTimeMillis() +"";
		Log.i(LOG_TAG, "Time Stamp current: - " + System.currentTimeMillis());
		
		default_time = default_time.substring(0, default_time.length()-3);
		int def_time = Integer.parseInt(default_time);
		lastImageTime = settings.getInt(PREFS_NAME_LAST_IMAGE_TIME, def_time);
		
		Log.d(LOG_TAG, "loaded preference file, lastImageTime - " + lastImageTime);

		// Initialize all the file managers and load the initial files into cache
		annotatorIdxMapMgr = new InvertedIndexManager(getApplicationContext(), AnnotatorRequestSenderAsync.INDEX_FILE);
		annotatorIdxMapMgr.loadIndex();
		faceDetectorIdxMapMgr = new InvertedIndexManager(getApplicationContext(), FaceDetectorAsync.INDEX_FILE);
		faceDetectorIdxMapMgr.loadIndex();
		featureMgr = new ClusterFeatureManager(getApplicationContext());
		featureMgr.loadFeatureLineImageMap();
		featureMgr.loadImageFeatureMap();

		ImageObserver observer = new ImageObserver(new Handler(),lastImageTime);

		Log.d(LOG_TAG, "beforer registering content observer");

		this.getApplicationContext().getContentResolver().registerContentObserver(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);

		Log.d(LOG_TAG, "registered content observer");
		return Service.START_STICKY;
	}


	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	class ImageObserver extends ContentObserver implements AsyncTaskRequestResponse
	{

		public static final String LOG_TAG = "ImageObserver";
		private int lastImageTime = 0;
		private boolean isProcessingSuccess = false;
		
		public ImageObserver(Handler handler, int lastImageTime)
		{
			super(handler);
			this.lastImageTime = lastImageTime;
		}

		@Override
		public void onChange(boolean selfChange)
		{
			super.onChange(selfChange);
			Log.d(LOG_TAG, "Found changes happening");

			// process the new image
			processNewImage();
			Log.d(LOG_TAG, "Finished processing new images");
			// update the lastImageTime
			if(isProcessingSuccess)
			{
				SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt(PREFS_NAME_LAST_IMAGE_TIME, lastImageTime);
				// Commit the edits!
				editor.commit();
				Log.d(LOG_TAG, "Last Image Time updated");
			}
		}

		private void processNewImage()
		{
			// Run query
			Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

			String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME,
					MediaStore.MediaColumns.DATE_ADDED, MediaStore.MediaColumns._ID };
			String eol = "\r\n";
			String listOfImage = "";
			Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), uri, projection, MediaStore.MediaColumns.DATE_ADDED + ">?",
					new String[] { String.valueOf(lastImageTime)}, MediaStore.MediaColumns.DATE_ADDED + " asc");
			
			Set<String> currImgSet = new LinkedHashSet<String>();
			while (cursor.moveToNext()) 
			{
				String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
				Log.d(LOG_TAG, "Processing -" + displayName+"-");
				if(currImgSet.contains(displayName)) // Skip this image if already processed
				{
					Log.d(LOG_TAG, "Found again" + displayName+"-");
					continue;
				}
				if(lastProcessed.contains(displayName)) // Skip this image if already processed last trigger
				{
					Log.d(LOG_TAG, "Found in last processed" + displayName+"-");
					continue;
				}
				
				currImgSet.add(displayName);
				int dateCreated = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.DATE_ADDED));
				int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
				Uri imageUri = Uri.withAppendedPath(uri, "" + id);
				try {
					Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
					Bitmap bitmap2 = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
					sendFaceDetectionAndAnnotatorRequest(bitmap, bitmap2, displayName);
				} catch (FileNotFoundException e) {
					Log.e(LOG_TAG, "Failed to load the image - " + imageUri);
					e.printStackTrace();
				} catch (IOException e) {
					Log.e(LOG_TAG, "Failed to load the image - " + imageUri);
					e.printStackTrace();
				}
				listOfImage = listOfImage + displayName + "," + dateCreated + eol;
				Log.d(LOG_TAG, "Processing - " + displayName + "," + dateCreated);
				lastImageTime = dateCreated;
			}
			lastProcessed.addAll(currImgSet);
			cursor.close();
			Log.d(LOG_TAG, listOfImage);
		}

		public void sendFaceDetectionAndAnnotatorRequest(Bitmap bitmap, Bitmap bitMap2, String imageFileName)
		{
			new FaceDetectorAsync(this,imageFileName).execute(bitMap2);
			
			if(bitmap.getWidth() > bitmap.getHeight())
			{
				bitmap = ScalingUtility.createScaledBitmap(bitmap, ANNO_DESIREDWIDTH, ANNO_DESIREDHEIGHT, ScalingLogic.FIT);
			}
			else
			{
				bitmap = ScalingUtility.createScaledBitmap(bitmap, ANNO_DESIREDHEIGHT, ANNO_DESIREDWIDTH, ScalingLogic.FIT);
			}			
			Log.d(LOG_TAG,bitmap.getWidth() + ". " + bitmap.getHeight());
			ByteArrayOutputStream bos = new ByteArrayOutputStream();			
			// CompressFormat set up to JPG, you can change to PNG or whatever you want;
			bitmap.compress(CompressFormat.JPEG, 100, bos);
			Log.d(LOG_TAG,bitmap.getWidth() + ". " + bitmap.getHeight());
			byte[] data = bos.toByteArray();
			InputStream bs = new ByteArrayInputStream(data);
			new AnnotatorRequestSenderAsync(this,imageFileName).execute(bs);
		}

		@Override
		public void processFinish(String asyncCode, String output)
		{		
			if (output!=null)
			{
				if(asyncCode == AnnotatorRequestSenderAsync.ASYNC_TASK_CODE)
				{
					Log.d(LOG_TAG, "Processing Annotator Finished");
					Toast.makeText(getApplicationContext(), "Got the annotations - " + output, Toast.LENGTH_LONG).show();
					List<FeatureValueObject> features = FeatureJSONParser.getFeatureList(output);
					annotatorIdxMapMgr.addImageEntry(features);
					annotatorIdxMapMgr.writeIndex();
					String imageFeatureStr = "";
					for(FeatureValueObject fvo : features)
					{
						imageFeatureStr = imageFeatureStr + ", " + fvo.getFeature();
					}
					featureMgr.addImageEntry(FeatureJSONParser.getImageName(output), 
							FeatureJSONParser.getClusterFeatureValueString(output), imageFeatureStr);
					featureMgr.writeFeatureLineImageMap();
					featureMgr.writeImageFeatureMap();
				}
				else if(asyncCode == FaceDetectorAsync.ASYNC_TASK_CODE)
				{
					Log.d(LOG_TAG, "Processing Face Detection Finished");
					Toast.makeText(getApplicationContext(), "Faces Detected - " + output, Toast.LENGTH_LONG).show();
					Log.d(LOG_TAG, "Faces Detected - " + output);
					String fileName = output.substring(0,output.indexOf(FaceDetectorAsync.FACEDETECTOR_FILENAME_SEPARATOR));
					Log.d(LOG_TAG, "Faces fileName - " + fileName);
					String faces = output.substring(output.indexOf(FaceDetectorAsync.FACEDETECTOR_FILENAME_SEPARATOR)+2);
					Log.d(LOG_TAG, "Faces - " + faces);
					FeatureValueObject fvo = new FeatureValueObject(fileName, faces, 1f);
					faceDetectorIdxMapMgr.addSingleFeatureEntry(fvo);
					faceDetectorIdxMapMgr.writeIndex();
				}
				isProcessingSuccess  = true;
			}
			else
			{
				Log.e(LOG_TAG,"Output received null from async call");
				isProcessingSuccess  = false;
			}
		}
	}
}
