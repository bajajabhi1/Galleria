package edu.columbia.cvml.galleria.activity;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class DisplayImageActivity extends Activity implements ServerRequestResponse {

	private String filepath;
	private Handler mHandler;
	private ImageView mImage;
	private TextView mText;
	private LinearLayout mLayout;
	private String LOG_TAG = "DisplayImageActivity";
	private boolean clickedBefore = false;

	@Override
	public void onCreate(Bundle savedInstanceState) {         

		super.onCreate(savedInstanceState);    
		setContentView(R.layout.activity_main);
		Intent i = getIntent();
		
		filepath = i.getStringExtra("filepath");
		Bitmap bitmap = BitmapFactory.decodeFile(filepath);

		ImageView img = new ImageView(this);
		img.setImageBitmap(bitmap);
		img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

		img.setAdjustViewBounds(true);
		img.setScaleType(ScaleType.CENTER_INSIDE);
		img.setPaddingRelative(40, 20, 40, 20);

		this.mImage = img;
		/*img.setClickable(true);
		img.setOnClickListener(new View.OnClickListener() {
			@Override
			public	void onClick(View currentView)
			{ 	


				if(!clickedBefore)
				{
					try{
						//Toast.makeText(DisplayImageActivity.this, "Uploading Image " + filepath + " for annotation", Toast.LENGTH_SHORT ).show();			
						//uploadImage();
						clickedBefore = true;
					}
					catch(Exception e)
					{
						Toast.makeText(DisplayImageActivity.this, "Image upload failed", Toast.LENGTH_SHORT ).show();
					}
				}
			}
		} );*/

		LinearLayout linearLayout= new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setBackgroundColor(Color.BLACK);
		linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

		linearLayout.addView(img);
		ScrollView scrollView = new ScrollView(this);
		scrollView.addView(linearLayout);
		scrollView.setBackgroundColor(Color.BLACK);
		scrollView.setClickable(true);
		//setContentView(linearLayout);
		setContentView(scrollView);
		mLayout = linearLayout;   
	}


	private void uploadImage()
	{
		//AssetManager assetManager = getAssets();
		try {

			InputStream inpStream = new FileInputStream(filepath);
			Log.i(LOG_TAG, "image path - ");

			//TextView text = (TextView) findViewById(R.id.annotations);

			TextView text = new TextView(this);
			text.setTextColor(Color.WHITE);
			text.setText("Annotation in progress... \n Please wait...");

			mText = text;
			mLayout.addView(mText);

			new ServerRequestSender(this).execute(inpStream);
			//inpStream = assetManager.open("20130810_155042.jpg");
			//Bitmap bitmap = BitmapFactory.decodeStream(inpStream);
			//ImageView first = (ImageView) findViewById(R.id.imagebox);
			//first.setImageBitmap(bitmap);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

	}

	public String convertResponseToString(HttpResponse response) throws IllegalStateException, IOException
	{
		String res = "";
		StringBuffer buffer = new StringBuffer();
		InputStream inputStream = response.getEntity().getContent();
		int contentLength = (int) response.getEntity().getContentLength(); //getting content length…..
		Toast.makeText(DisplayImageActivity.this, "contentLength : " + contentLength, Toast.LENGTH_LONG).show();
		if (contentLength < 0)
		{

		}         
		else
		{                
			byte[] data = new byte[512];
			int len = 0;
			try
			{
				while (-1 != (len = inputStream.read(data)))
				{
					buffer.append(new String(data, 0, len)); //converting to string and appending  to stringbuffer…..
				}
			}
			catch (IOException e)                
			{                    
				e.printStackTrace();                
			}
			try
			{
				inputStream.close(); // closing the stream…..                
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}                
			res = buffer.toString();  // converting stringbuffer to string…..
			Toast.makeText(DisplayImageActivity.this, "Result : " + res, Toast.LENGTH_LONG).show();
			//System.out.println("Response => " +  EntityUtils.toString(response.getEntity()));
		}
		return res;
	}

	//		@Override
	//		public boolean onCreateOptionsMenu(Menu menu) {
	//			// Inflate the menu; this adds items to the action bar if it is present.
	//			getMenuInflater().inflate(R.menu.main, menu);
	//			return true;
	//		}

	@Override
	public void processFinish(String output) {
		// TODO Auto-generated method stub
		output=output.substring(1);
		output = output.replaceAll("_"," ");
		//TextView text = (TextView) findViewById(R.id.annotations);

		TextView text = mText;
		text.setText(output);
	}

}
