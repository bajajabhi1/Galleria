package edu.columbia.cvml.galleria.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import edu.columbia.cvml.galleria.util.BitMapDecoder;

@SuppressLint("NewApi")
public class DisplayImageActivity extends Activity {

	private String filepath;
	private ImageView mImage;
	private LinearLayout mLayout;
	private String LOG_TAG = "DisplayImageActivity";
	public static final int ANNO_DESIREDWIDTH = 640;
	public static final int ANNO_DESIREDHEIGHT = 480;

	@Override
	public void onCreate(Bundle savedInstanceState) {         

		super.onCreate(savedInstanceState);    
		setContentView(R.layout.activity_main);
		Intent i = getIntent();
		
		filepath = i.getStringExtra("filepath");
		Bitmap bitmap = BitMapDecoder.decodeSampledBitmap(filepath, ANNO_DESIREDHEIGHT, ANNO_DESIREDWIDTH);
		//Bitmap bitmap = BitmapFactory.decodeFile(filepath);

		ImageView img = new ImageView(this);
		img.setImageBitmap(bitmap);
		//img.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT));

		img.setAdjustViewBounds(true);
		img.setScaleType(ScaleType.FIT_XY);
		img.setPaddingRelative(80, 40, 80, 40);

		this.mImage = img;
		LinearLayout linearLayout= new LinearLayout(this);
		linearLayout.setOrientation(LinearLayout.VERTICAL);
		linearLayout.setBackgroundColor(Color.BLACK);
		linearLayout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT));

		linearLayout.addView(img);
		ScrollView scrollView = new ScrollView(this);
		scrollView.addView(linearLayout);
		scrollView.setBackgroundColor(Color.BLACK);
		//scrollView.setClickable(true);
		//setContentView(linearLayout);
		setContentView(scrollView);
		mLayout = linearLayout;   
	}
}
