package edu.columbia.cvml.galleria.activity;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.galleria.R;

import edu.columbia.cvml.galleria.async.AsyncTaskRequestResponse;
import edu.columbia.cvml.galleria.async.AnnotatorRequestSenderAsync;
import edu.columbia.cvml.galleria.services.ImageDetectorService;

public class MainActivity extends Activity implements AsyncTaskRequestResponse {

	String LOG_TAG =  "MainActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Button button = (Button) findViewById(R.id.upload);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				uploadImage("20130810_155042.jpg");
			}
		});
		
		button = (Button) findViewById(R.id.detectFace);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				detectFaces();
				//isMyServiceRunning();
			}
		});
		
		// use this to start and trigger a service
		Intent i= new Intent(getApplicationContext(), ImageDetectorService.class);
		// potentially add data to the intent
		i.putExtra("KEY1", "Value to be used by the service");
		getApplicationContext().startService(i);
		Log.i(LOG_TAG, "Service start called");
	}
	
	private void isMyServiceRunning() {
	    ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
	    for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
	    	Log.i(LOG_TAG, "Service running - "  + service.service.getClassName());
	    	if (ImageDetectorService.class.getName().equals(service.service.getClassName())) {
	        	Log.i(LOG_TAG, "Service found to be running");
				Toast.makeText(MainActivity.this,"Service is running", Toast.LENGTH_LONG).show();
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
			Toast.makeText(MainActivity.this,"Annotating in progress... Please wait...", Toast.LENGTH_LONG).show();
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void processFinish(String asyncCode, String output) {
		// TODO Auto-generated method stub
		TextView text = (TextView) findViewById(R.id.annotations);
		text.setText(output);
	}

}
