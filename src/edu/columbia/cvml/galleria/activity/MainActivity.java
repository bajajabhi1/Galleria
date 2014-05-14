package edu.columbia.cvml.galleria.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;

import edu.columbia.cvml.galleria.services.ImageDetectorService;

/**
 * 
 * This will not work so great since the heights of the imageViews 
 * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get 
 * the (intrinsic) image width and height and set the views height manually. I will
 * look into a fix once I find extra time.
 * 
 * @author Maurycy Wojtowicz, Abhinav Bajaj
 *
 */
@SuppressLint("NewApi")
public class MainActivity extends Activity {
	String LOG_TAG =  "MainActivity";

	/**
	 * This will not work so great since the heights of the imageViews 
	 * are calculated on the iamgeLoader callback ruining the offsets. To fix this try to get 
	 * the (intrinsic) image width and height and set the views height manually. I will
	 * look into a fix once I find extra time.
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		ActionBar actionbar = getActionBar();
		actionbar.setTitle("Galleria");
		actionbar.setSubtitle("When you love clicking!!!");
		actionbar.setHomeButtonEnabled(true);
		StaggeredGridView gridView = (StaggeredGridView) this.findViewById(R.id.staggeredGridView1);

		int margin = getResources().getDimensionPixelSize(R.dimen.margin);


		gridView.setItemMargin(margin); // set the GridView margin
		gridView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well 

		final Map<String,ArrayList<String>> clusterMap = simulateClusterMethod();
		final Map<Integer,String> posClusterMap = new HashMap<Integer,String>();
		Set<String> clusterNameSet = clusterMap.keySet();
		final String[] imagePath = new String[clusterNameSet.size()];
		int index = 0;
		for(String cluster : clusterNameSet)
		{
			if(!clusterMap.get(cluster).isEmpty())
				imagePath[index] = clusterMap.get(cluster).get(0);
			posClusterMap.put(index, cluster);
			index +=1;
		}
		
		CustomStaggeredAdapter adapter = new CustomStaggeredAdapter(MainActivity.this, R.id.imageView1, imagePath);
		gridView.setAdapter(adapter);

		gridView.setOnItemClickListener(new OnItemClickListener()
		{            	  
			@Override
			public void onItemClick(StaggeredGridView parent, View view, int position, long id)
			{
				//Toast.makeText(getApplicationContext(), "You clicked it, yay", Toast.LENGTH_LONG).show();
				//Intent i = new Intent(MainActivity.this, DisplayImageActivity.class);
				Intent i = new Intent(MainActivity.this, CarouselActivity.class);
				//String filepath = (String) parent.getAdapter().getItem(position);
				String cluster = posClusterMap.get(position);
				i.putStringArrayListExtra("filepath", clusterMap.get(cluster));
				System.gc();
				startActivity(i);
			}
		}); 

		adapter.notifyDataSetChanged();
		gridView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		// use this to start and trigger a service
		Intent i= new Intent(getApplicationContext(), ImageDetectorService.class);
		getApplicationContext().startService(i);
		Log.i(LOG_TAG, "Service start called");
	}
	
	private Map<String,ArrayList<String>> simulateClusterMethod()
	{
		Map<String,ArrayList<String>> clusterMap = new HashMap<String,ArrayList<String>>();
		Uri extUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		String[] projection = new String[] { MediaStore.MediaColumns.DISPLAY_NAME, MediaStore.Images.Media.DATA,
				MediaStore.MediaColumns.DATE_ADDED, MediaStore.MediaColumns._ID };
		Cursor cursor = MediaStore.Images.Media.query(getContentResolver(), extUri, projection, null, MediaStore.MediaColumns.DATE_ADDED + " asc");
		final String[] imagePath = new String[cursor.getCount()];
		final ArrayList<String> imagePathList = new ArrayList<String>();
		int index = 0;
		while (cursor.moveToNext()) 
		{
			imagePath[index] = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			imagePathList.add(imagePath[index]);
			//Log.d(LOG_TAG, "Image = " + imagePath[index]);
			index +=1;
		}
		Log.d(LOG_TAG, "size = " + index);
		ArrayList<String> cluster1List = new ArrayList<String>();
		for(int i = 0;i<4 && i<index;i++)
		{
			cluster1List.add(imagePathList.get(i));			
		}
		clusterMap.put("Cluster1",cluster1List);
		ArrayList<String> cluster2List = new ArrayList<String>();
		for(int i = 4;i<8 && i<index;i++)
		{
			cluster2List.add(imagePathList.get(i));			
		}
		clusterMap.put("Cluster2",cluster2List);
		cursor.close();
		return clusterMap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{         
		switch (item.getItemId())
		{
		case R.id.DebugScreen:
			Toast.makeText(MainActivity.this, "DebugScreen is selected", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, DebugActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}    

	private void shuffleArray(Object[] array)
	{
		int index; 
		Object temp;
		Random random = new Random();
		for (int i = array.length - 1; i > 0; i--)
		{
			index = random.nextInt(i + 1);
			temp = array[index];
			array[index] = array[i];
			array[i] = temp;
		}
	}
}