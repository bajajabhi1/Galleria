package edu.columbia.cvml.galleria.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import edu.columbia.cvml.galleria.util.BitMapDecoder;
import edu.columbia.cvml.galleria.views.ScaleImageView;

public class CustomStaggeredAdapter extends ArrayAdapter<String> {

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		super.clear();
	}

	private static final int ANNO_DESIREDWIDTH = 480;
	private static final int ANNO_DESIREDHEIGHT = 320;
	
	String LOG_TAG =  "CustomStaggeredAdapter";
	public CustomStaggeredAdapter(Context context, int textViewResourceId,
			String[] objects) {
		super(context, textViewResourceId, objects);
		//mLoader = new ImageLoader(context);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		ViewHolder holder;

		if (convertView == null) {
			LayoutInflater layoutInflator = LayoutInflater.from(getContext());
			convertView = layoutInflator.inflate(R.layout.row_staggered_demo,
					null);
			holder = new ViewHolder();
			holder.imageView = (ScaleImageView) convertView .findViewById(R.id.imageView1);
			convertView.setTag(holder);
		}

		holder = (ViewHolder) convertView.getTag();
		String resourceURL = getItem(position);
		Log.i(LOG_TAG, "found:"+ resourceURL);
		if(resourceURL!=null)
			DisplayImage(getItem(position), holder.imageView);
		return convertView;
	}

	public void DisplayImage(String url, ImageView imageView)
	{
		Bitmap bitmap = BitMapDecoder.decodeSampledBitmap(url, ANNO_DESIREDHEIGHT, ANNO_DESIREDWIDTH);
		/*Bitmap bitmap = BitmapFactory.decodeFile(url);	    	
		if(bitmap.getWidth() > bitmap.getHeight())
		{
			bitmap = ScalingUtility.createScaledBitmap(bitmap, ImageDetectorService.ANNO_DESIREDWIDTH, ImageDetectorService.ANNO_DESIREDHEIGHT, ScalingLogic.FIT);
		}
		else
		{
			bitmap = ScalingUtility.createScaledBitmap(bitmap, ImageDetectorService.ANNO_DESIREDHEIGHT, ImageDetectorService.ANNO_DESIREDWIDTH, ScalingLogic.FIT);
		}*/
		//Bitmap bitmap=memoryCache.get(url);
		if(bitmap!=null)
			imageView.setImageBitmap(bitmap);
		else
		{
			imageView.setImageDrawable(null);
		}
	}

	static class ViewHolder {
		ScaleImageView imageView;
	}
}


