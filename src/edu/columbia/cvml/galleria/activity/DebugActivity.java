package edu.columbia.cvml.galleria.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import edu.columbia.cvml.galleria.VO.FeatureValueObject;
import edu.columbia.cvml.galleria.async.AnnotatorRequestSenderAsync;
import edu.columbia.cvml.galleria.async.AsyncTaskRequestResponse;
import edu.columbia.cvml.galleria.async.FaceDetectorAsync;
import edu.columbia.cvml.galleria.services.ImageDetectorService;
import edu.columbia.cvml.galleria.util.ClusterFeatureManager;
import edu.columbia.cvml.galleria.util.InvertedIndexManager;

public class DebugActivity extends Activity implements AsyncTaskRequestResponse {

	String LOG_TAG =  "DebugActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_debug);
		Button button = (Button) findViewById(R.id.upload);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//uploadImage("20130810_155042.jpg");
				TextView text = (TextView) findViewById(R.id.annotations);
				text.setMovementMethod(new ScrollingMovementMethod());
				text.setText(loadIndexFile(1));
			}
		});
		
		button = (Button) findViewById(R.id.detectFace);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//detectFaces();
				isMyServiceRunning();
				/*TextView text = (TextView) findViewById(R.id.annotations);
				text.setMovementMethod(new ScrollingMovementMethod());
				text.setText(loadIndexFile(2));*/
			}
		});
		
		button = (Button) findViewById(R.id.LoadFeatureFile);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				TextView text = (TextView) findViewById(R.id.annotations);
				text.setMovementMethod(new ScrollingMovementMethod());
				text.setText(loadClusteringFile());
			}
		});
		
		// use this to start and trigger a service
		Intent i= new Intent(getApplicationContext(), ImageDetectorService.class);
		// potentially add data to the intent
		i.putExtra("KEY1", "Value to be used by the service");
		getApplicationContext().startService(i);
		Log.i(LOG_TAG, "Service start called");
		
		TextView text = (TextView) findViewById(R.id.annotations);
		text.setText(loadIndexFile(1));
		
	}
	
	private String loadIndexFile(int whichOne)
	{
		InvertedIndexManager idxMapMgr = null;
		if (whichOne == 1)
		{
			idxMapMgr = new InvertedIndexManager(getApplicationContext(), AnnotatorRequestSenderAsync.INDEX_FILE);
		}
		else
		{
			idxMapMgr = new InvertedIndexManager(getApplicationContext(), FaceDetectorAsync.INDEX_FILE);
		}
		Map<String,List<FeatureValueObject>> map = idxMapMgr.loadIndex();
		String ann = "";
		Set<String> keys = map.keySet();
		for(String key : keys)
		{
			ann = ann + key + "::";
			List<FeatureValueObject> fvoList = map.get(key);
			for(FeatureValueObject fvo : fvoList)
			{
				ann = ann + fvo.toString() + ",";
			}
			ann = ann + "+++\n";
		}
		return ann;
	}
	
	private String loadClusteringFile()
	{
		ClusterFeatureManager featureMgr = new ClusterFeatureManager(getApplicationContext());
		
		String featureFile = featureMgr.loadClusterImageFile();
		if(null == featureFile)
			return "EMPTY!";
		
		return featureFile;
	}
	
	private void isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Log.i(LOG_TAG, "Service running - "  + service.service.getClassName());
	    	if (ImageDetectorService.class.getName().equals(service.service.getClassName())) {
	        	Log.i(LOG_TAG, "Service found to be running");
				Toast.makeText(DebugActivity.this,"Service is running", Toast.LENGTH_LONG).show();
	        }
	    }
	}

	private void detectFaces()
	{
		 //doFaceDetection("20130810_155042.jpg");
		 /*try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 /*doFaceDetection("20140412_163630.jpg");
		 try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 doFaceDetection("20140412_164945.jpg");
		 try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		 //doFaceDetection("WP_20131212_007.png");
		FaceDetectorView view = (FaceDetectorView) findViewById(R.id.faceDetbox);
		view.getLayoutParams().height = 640;
		view.getLayoutParams().width = 480;
		view.doFaceDetection("IMG_20140503_191136.jpg");
	}
	
	private void uploadImage(String img_name)
	{
		AssetManager assetManager = getAssets();
		try {
			InputStream inpStream = assetManager.open(img_name);
			Log.i(LOG_TAG, "image path - ");
			TextView text = (TextView) findViewById(R.id.annotations);
			Toast.makeText(DebugActivity.this,"Annotating in progress... Please wait...", Toast.LENGTH_LONG).show();
			text.setText("Annotating in progress... Please wait...");
			new AnnotatorRequestSenderAsync(this,img_name).execute(inpStream);
			inpStream = assetManager.open(img_name);
			Bitmap bitmap = BitmapFactory.decodeStream(inpStream);
			ImageView first = (ImageView) findViewById(R.id.imagebox);
			first.setImageBitmap(bitmap);

			//File file = new File(imageList[0],"imagefile.jpg");
			//if(file.exists())
			//{
			/*ByteArrayOutputStream stream = new ByteArrayOutputStream();

				// Enclose this in a scope so that when it's over we can call the garbage collector (the phone doesn't have a lot of memory!)
				{
					Bitmap image = BitmapFactory.decodeStream(inpStream);
					Bitmap scaled = Bitmap.createScaledBitmap(image, (int)(image.getWidth() * 0.3), (int)(image.getHeight() * 0.3), false);
					scaled.compress(Bitmap.CompressFormat.JPEG, 90, stream);
				}

				System.gc();

				byte [] byte_arr = stream.toByteArray();
				String image_str = Base64.encodeToString(byte_arr,Base64.DEFAULT);
				Log.i(LOG_TAG, "image string - " + image_str);*/
			/*ArrayList nameValuePairs = new ArrayList();

				nameValuePairs.add(new BasicNameValuePair("upfile",image_str));

				try {
					HttpClient httpclient = new DefaultHttpClient();
					final String URL = "http://127.0.0.1:8080";
					HttpPost httppost = new HttpPost(URL);
					httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
					HttpResponse response = httpclient.execute(httppost);
					String the_string_response = convertResponseToString(response);
					Toast.makeText(MainActivity.this, "Response " + the_string_response, Toast.LENGTH_LONG).show();
				} catch(Exception e){
					Toast.makeText(MainActivity
							.this, "ERROR " + e.getMessage(), Toast.LENGTH_LONG).show();
					Log.i(LOG_TAG, "Error in http connection "+e.toString());
					e.printStackTrace();
				}*/
			//}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}


	@Override
	public void processFinish(String asyncCode, String output) {
		// TODO Auto-generated method stub
		TextView text = (TextView) findViewById(R.id.annotations);
		text.setText(output);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.debug_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{         
		switch (item.getItemId())
		{
		case R.id.MainScreen:
			Toast.makeText(DebugActivity.this, "Main Screen is selected", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(DebugActivity.this, MainActivity.class);
            startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
