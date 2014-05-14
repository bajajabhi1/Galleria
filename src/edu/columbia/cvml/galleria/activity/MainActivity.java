package edu.columbia.cvml.galleria.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.Toast;

import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;

import edu.columbia.cvml.galleria.VO.FeatureValueObject;
import edu.columbia.cvml.galleria.async.AnnotatorRequestSenderAsync;
import edu.columbia.cvml.galleria.async.FaceDetectorAsync;
import edu.columbia.cvml.galleria.services.ImageDetectorService;
import edu.columbia.cvml.galleria.util.FaceDetectionConstants;
import edu.columbia.cvml.galleria.util.InvertedIndexManager;

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
	public static String imageBasePath = "/storage/emulated/0/DCIM/100MEDIA/";
	String LOG_TAG =  "MainActivity";
	Map<String,String> imagePathMap = new HashMap<String,String>();
	AutoCompleteTextView searchView = null;

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
			Log.d(LOG_TAG, "Addding Cluster to imagePath - " + cluster);
			if(!clusterMap.get(cluster).isEmpty())
			{
				imagePath[index] = clusterMap.get(cluster).get(0);
				Log.d(LOG_TAG, "IMage added to imagePath - " + imagePath[index]);
			}
			posClusterMap.put(index, cluster);
			index +=1;
		}
		Log.d(LOG_TAG, "Cluster Size - " +index);
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
				startActivity(i);
				System.gc();				
			}
		}); 

		adapter.notifyDataSetChanged();
		gridView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
		
		// start the service
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
		imagePathMap = new HashMap<String,String>();
		int index = 0;
		while (cursor.moveToNext()) 
		{
			imagePath[index] = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
			imagePathList.add(imagePath[index]);
			String fileName = imagePath[index].substring(imagePath[index].lastIndexOf("/")+1);
			Log.d(LOG_TAG, "Adding to imagePathMap = " + fileName);
			Log.d(LOG_TAG, "imagePathMap = " + imagePath[index]);
			imagePathMap.put(fileName, imagePath[index]);			
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
		
		// Add the Face Detector Clusters
		InvertedIndexManager idxMapMgr = new InvertedIndexManager(getApplicationContext(), FaceDetectorAsync.INDEX_FILE);
		Map<String,List<FeatureValueObject>> facesMap = idxMapMgr.loadIndex();
		Set<String> keys = facesMap.keySet();
		ArrayList<String> clusterList = null;
		for(String key : keys)
		{
			List<FeatureValueObject> imageList = facesMap.get(key);
			clusterList = new ArrayList<String>();
			for(FeatureValueObject fvo : imageList)
			{
				clusterList.add(imagePathMap.get(fvo.getImageName()));
				Log.d(LOG_TAG, "FaceDetected Finding Image = " + fvo.getImageName());
				Log.d(LOG_TAG, "FaceDetected IMage = " + imagePathMap.get(fvo.getImageName()));
			}
			if(!key.equalsIgnoreCase(FaceDetectionConstants.FACE_DETECT_NONE)) // No need to show none cluster
			{
				Log.d(LOG_TAG, "Addding Cluster - " + key);				
				clusterMap.put(key, clusterList);
			}
		}
		return clusterMap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		searchView = (AutoCompleteTextView) menu.findItem(R.id.search).getActionView();
		String[] androidBooks =
			{
			"Hello, Android - Ed Burnette",
			"Professional Android 2 App Dev - Reto Meier",
			"Unlocking Android - Frank Ableson",
			"Android App Development - Blake Meike",
			"Pro Android 2 - Dave MacLean",
			"Beginning Android 2 - Mark Murphy",
			"Android Programming Tutorials - Mark Murphy",
			"Android Wireless App Development - Lauren Darcey",
			"Pro Android Games - Vladimir Silva",
			};
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,androidBooks);
		searchView.setAdapter(adapter);
		searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			 
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
                // TODO Auto-generated method stub
                String getContryName = searchView.getText().toString().trim();
                Toast.makeText(getApplicationContext(), "Selected - " + getContryName, Toast.LENGTH_SHORT).show();  
            }
        });
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
		case R.id.WekaDebugScreen:
			Toast.makeText(MainActivity.this, "Weka DebugScreen is selected", Toast.LENGTH_SHORT).show();
			intent = new Intent(MainActivity.this, WekaDebugActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}
}