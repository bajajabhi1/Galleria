package edu.columbia.cvml.galleria.services;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
import edu.columbia.cvml.galleria.util.FileOperation;
import edu.columbia.cvml.galleria.util.InvertedIndexManager;
import edu.columbia.cvml.galleria.util.ScalingUtility;
import edu.columbia.cvml.galleria.util.ScalingUtility.ScalingLogic;

public class ImageDetectorService extends Service
{

	private static final String LOG_TAG = "ImageDetectorService";
	private static final String PREFS_FILE_NAME = "ImageDetectorServicePref";
	private static final String PREFS_NAME_LAST_IMAGE_TIME = "PREFS_NAME_LAST_IMAGE_TIME";
	private static final String LINE_END = "\r\n";
	private static final int ANNO_DESIREDWIDTH = 640;
	private static final int ANNO_DESIREDHEIGHT = 480;
	
	private InvertedIndexManager annotatorIdxMapMgr = null;
	private InvertedIndexManager faceDetectorIdxMapMgr = null;

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
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		Log.i(LOG_TAG, "Time Stamp current: - " + System.currentTimeMillis());
		Log.i(LOG_TAG, "Time Stamp current: - " + c.getTime().toString());
		
		default_time = default_time.substring(0, default_time.length()-3);
		int def_time = Integer.parseInt(default_time);
		lastImageTime = settings.getInt(PREFS_NAME_LAST_IMAGE_TIME, def_time);
		Long t = def_time * 1000l;
		c = Calendar.getInstance();
		c.setTimeInMillis(t);
		Log.i(LOG_TAG, "lastImageTime: - " + System.currentTimeMillis());
		Log.i(LOG_TAG, "lastImageTime: - " + c.getTime().toString());

		annotatorIdxMapMgr = new InvertedIndexManager(getApplicationContext(), AnnotatorRequestSenderAsync.INDEX_FILE);
		faceDetectorIdxMapMgr = new InvertedIndexManager(getApplicationContext(), FaceDetectorAsync.INDEX_FILE);
		Log.d(LOG_TAG, "loaded preference file, lastImageTime - " + lastImageTime);
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
			// update the lastImageTime
			SharedPreferences settings = getSharedPreferences(PREFS_FILE_NAME, 0);
			// TODO
			// Write the HASH MAP to inverted index file
			SharedPreferences.Editor editor = settings.edit();
			editor.putInt(PREFS_NAME_LAST_IMAGE_TIME, lastImageTime);
			// Commit the edits!
			editor.commit();
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

			while (cursor.moveToNext()) 
			{
				String displayName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME));
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
				Log.e(LOG_TAG, "Processing - " + displayName + "," + dateCreated);
				lastImageTime = dateCreated;
			}
			Log.d(LOG_TAG, listOfImage);
		}

		public void sendFaceDetectionAndAnnotatorRequest(Bitmap bitmap, Bitmap bitMap2, String imageFileName)
		{
			new FaceDetectorAsync(this,imageFileName).execute(bitMap2);
			bitmap = ScalingUtility.createScaledBitmap(bitmap, ANNO_DESIREDWIDTH, ANNO_DESIREDHEIGHT, ScalingLogic.FIT);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();			
			// CompressFormat set up to JPG, you can change to PNG or whatever you want;
			bitmap.compress(CompressFormat.JPEG, 100, bos);
			byte[] data = bos.toByteArray();
			InputStream bs = new ByteArrayInputStream(data);
			new AnnotatorRequestSenderAsync(this,imageFileName).execute(bs);
		}

		public String convertResponseToString(InputStream inputStream ) throws IllegalStateException, IOException
		{
			String res = "";
			StringBuffer buffer = new StringBuffer();
			{                
				byte[] data = new byte[512];
				int len = 0;
				try
				{
					while (-1 != (len = inputStream.read(data)))
					{
						buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer
					}
				}
				catch (IOException e)                
				{                    
					e.printStackTrace();                
				}
				try
				{
					inputStream.close(); // closing the stream                
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}                
				res = buffer.toString();  // converting stringbuffer to string
			}
			return res;
		}

		@Override
		public void processFinish(String asyncCode, String output)
		{			
			if(asyncCode == AnnotatorRequestSenderAsync.ASYNC_TASK_CODE)
			{
				Log.d(LOG_TAG, "Processing Annotator Finished");
				Toast.makeText(getApplicationContext(), "Got the annotations - " + output, Toast.LENGTH_LONG).show();
				List<FeatureValueObject> features = getFeatureList(output);
				annotatorIdxMapMgr.addImageEntry(features);
				annotatorIdxMapMgr.writeIndex();
				//FileOperation.writeFileToInternalStorage(getApplicationContext(), AnnotatorRequestSenderAsync.INDEX_FILE, output + LINE_END);
			}
			else if(asyncCode == FaceDetectorAsync.ASYNC_TASK_CODE)
			{
				Log.d(LOG_TAG, "Processing Face Detection Finished");
				Toast.makeText(getApplicationContext(), "Faces Detected - " + output, Toast.LENGTH_LONG).show();
				Log.d(LOG_TAG, "Faces Detected - " + output);
				String fileName = output.substring(0,output.indexOf(FaceDetectorAsync.FACEDETECTOR_FILENAME_SEPARATOR)+1);
				String faces = output.substring(output.indexOf(FaceDetectorAsync.FACEDETECTOR_FILENAME_SEPARATOR)+1);
				FeatureValueObject fvo = new FeatureValueObject(fileName, faces, 1f);
				faceDetectorIdxMapMgr.addSingleFeatureEntry(fvo);
				faceDetectorIdxMapMgr.writeIndex();
			}
		}
		
		public List<FeatureValueObject> getFeatureList(String json)
		{
			List<FeatureValueObject> featureList = new ArrayList<FeatureValueObject>();

			try
			{
				FeatureValueObject fvobj = null;
				JSONObject jsonObj = new JSONObject(json);
				String imageName = jsonObj.getString(AnnotatorRequestSenderAsync.TAG_FILENAME_KEY);
				// Getting JSON Array node
				JSONArray featuresJson = jsonObj.getJSONArray(AnnotatorRequestSenderAsync.TAG_FEATURES);

				// looping through All Contacts
				for (int i = 0; i < AnnotatorRequestSenderAsync.TOP_K; i++) {
					JSONObject c = featuresJson.getJSONObject(i);
					String featureName = c.getString(AnnotatorRequestSenderAsync.TAG_KEY);
					String featureValue = c.getString(AnnotatorRequestSenderAsync.TAG_VALUE);
					Float featureFloatValue = Float.valueOf(featureValue);
					fvobj = new FeatureValueObject(imageName, featureName, featureFloatValue);
					featureList.add(fvobj);
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				Log.e(LOG_TAG, e.getMessage());
				e.printStackTrace();
			}
			return featureList;
		}
	}
}
