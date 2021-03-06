package edu.columbia.cvml.galleria.activity;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import com.origamilabs.library.views.StaggeredGridView;
import com.origamilabs.library.views.StaggeredGridView.OnItemClickListener;

import edu.columbia.cvml.galleria.VO.FeatureValueObject;
import edu.columbia.cvml.galleria.async.AnnotatorRequestSenderAsync;
import edu.columbia.cvml.galleria.async.AsyncClusterer;
import edu.columbia.cvml.galleria.async.AsyncTaskRequestResponse;
import edu.columbia.cvml.galleria.async.FaceDetectorAsync;
import edu.columbia.cvml.galleria.services.ImageDetectorService;
import edu.columbia.cvml.galleria.util.ClusterFeatureManager;
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
public class MainActivity extends Activity implements AsyncTaskRequestResponse {
	public static String imageBasePath = "/storage/emulated/0/DCIM/100MEDIA/";
	String LOG_TAG =  "MainActivity";
	Map<String,String> imagePathMap = new HashMap<String,String>();
	AutoCompleteTextView searchView = null;
	InvertedIndexManager idxMapMgr = null;
	Map<String,List<FeatureValueObject>> annotMap = null;
	Map<String,List<FeatureValueObject>> faceDetMap = null;
	CustomStaggeredAdapter adapter = null;
	StaggeredGridView gridView = null;
	Map<String,ArrayList<String>> annoListMap = null;
	int imageCount = 0;
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
		//actionbar.setBackgroundDrawable(new ColorDrawable(R.color.white));
		gridView = (StaggeredGridView) this.findViewById(R.id.staggeredGridView1);

		int margin = getResources().getDimensionPixelSize(R.dimen.margin);


		gridView.setItemMargin(margin); // set the GridView margin
		gridView.setPadding(margin, 0, margin, 0); // have the margin on the sides as well 

		final Map<String,ArrayList<String>> clusterMap = loadClusters();
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

		adapter = new CustomStaggeredAdapter(MainActivity.this, R.id.imageView1, imagePath);
		gridView.setAdapter(adapter);
		adapter.notifyDataSetChanged();

		// start the service
		Intent i= new Intent(getApplicationContext(), ImageDetectorService.class);
		getApplicationContext().startService(i);
		Log.i(LOG_TAG, "Service start called");
		
		// Call the clustering 
		if(checkRunCluster())
		{
			AsyncClusterer async_KM = new AsyncClusterer(this, this);                                
			async_KM.execute(getApplicationContext());
		}

		idxMapMgr = new InvertedIndexManager(getApplicationContext(), AnnotatorRequestSenderAsync.INDEX_FILE);
		annotMap = idxMapMgr.loadIndex();
		idxMapMgr = new InvertedIndexManager(getApplicationContext(), FaceDetectorAsync.INDEX_FILE);
		faceDetMap = idxMapMgr.loadIndex();
		loadSearchVocabJson();
	}
	
	private boolean checkRunCluster()
	{
		Log.d(LOG_TAG, "LineCount = "+ ClusterFeatureManager.getClusteringFileLineCount(getApplicationContext()));
		Log.d(LOG_TAG, "ImageCount = "+ imageCount);
		if(imageCount < ClusterFeatureManager.getClusteringFileLineCount(getApplicationContext()) - 3)
		{
			return true;
		}
		return false;
	}

	private void loadSearchVocabJson()
	{
		AssetManager aMan = getAssets();                
		try {
			InputStream imagedata = aMan.open("searchVocab.json");
			String json  = convertResponseToString(imagedata);
			getClusterFeatureValueString(json);
		} catch (IOException e) {
			Log.d(LOG_TAG, "Json loading exception - " + e.getMessage());
		}
	}

	public void getClusterFeatureValueString(String json)
	{
		try
		{
			JSONObject jsonObj = new JSONObject(json);
			Iterator<String> itr = jsonObj.keys();
			ArrayList<String> annoList = null;
			annoListMap = new HashMap<String,ArrayList<String>>();
			while(itr.hasNext())
			{
				annoList = new ArrayList<String>();
				String key = itr.next();
				JSONArray featuresJson = jsonObj.getJSONArray(key);
				//Log.e(LOG_TAG, "json key - " + key);
				for (int i = 0; i < featuresJson.length(); i++)
				{
					String c = featuresJson.getString(i);
					annoList.add(c);
				}
				annoListMap.put(key, annoList);
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}
	}

	private Map<String,ArrayList<String>> loadClusters()
	{
		//Map<String,ArrayList<String>> clusterMap = new HashMap<String,ArrayList<String>>();
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
		/*ArrayList<String> cluster1List = new ArrayList<String>();
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
		clusterMap.put("Cluster2",cluster2List);*/
		cursor.close();

		Map<String,ArrayList<String>> clusterFilePathMap = 	new HashMap<String,ArrayList<String>>();
		
		Map<String,ArrayList<String>> clusterMap = ClusterFeatureManager.loadClusterImageMap(getApplicationContext());
		imageCount = 0;
		Set<String> keyset = clusterMap.keySet();
		ArrayList<String> clusterList = null;
		for(String key : keyset)
		{
			clusterList = new ArrayList<String>();
			ArrayList<String> images = clusterMap.get(key);
			for(String image: images)
			{
				clusterList.add(imagePathMap.get(image));
				Log.d(LOG_TAG, "adding Image = " + image);
				Log.d(LOG_TAG, "IMage path = " + imagePathMap.get(image));
			}			
			imageCount = imageCount + images.size();
			Log.d(LOG_TAG, "Addding Cluster - " + key);				
			clusterFilePathMap.put(key, clusterList);
		}
		
		
		// Add the Face Detector Clusters
		InvertedIndexManager idxMapMgr = new InvertedIndexManager(getApplicationContext(), FaceDetectorAsync.INDEX_FILE);
		Map<String,List<FeatureValueObject>> facesMap = idxMapMgr.loadIndex();
		Set<String> keys = facesMap.keySet();
		for(String key : keys)
		{
			List<FeatureValueObject> imageList = facesMap.get(key);
			clusterList = new ArrayList<String>();
			for(FeatureValueObject fvo : imageList)
			{
				clusterList.add(imagePathMap.get(fvo.getImageName()));
				//clusterList.add(imageBasePath+fvo.getImageName());
				Log.d(LOG_TAG, "FaceDetected Finding Image = " + fvo.getImageName());
				Log.d(LOG_TAG, "FaceDetected IMage = " + imagePathMap.get(fvo.getImageName()));
			}
			if(!key.equalsIgnoreCase(FaceDetectionConstants.FACE_DETECT_NONE)) // No need to show none cluster
			{
				Log.d(LOG_TAG, "Addding Cluster - " + key);				
				clusterFilePathMap.put(key, clusterList);
			}
		}
		return clusterFilePathMap;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		searchView = (AutoCompleteTextView) menu.findItem(R.id.search).getActionView();
		searchView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,annotations);
		searchView.setAdapter(adapter);
		searchView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				// TODO Auto-generated method stub
				String feature = searchView.getText().toString().trim();
				//Toast.makeText(getApplicationContext(), "Selected - " + feature, Toast.LENGTH_SHORT).show(); 
				setSearchedImageList(feature);
			}
		});
		return true;
	}

	public void setSearchedImageList(String feature)
	{
		if(feature.equals(FaceDetectionConstants.FACE_DETECT_SELFIE) || feature.equals(FaceDetectionConstants.FACE_DETECT_GROUP)
				|| feature.equals(FaceDetectionConstants.FACE_DETECT_ALL) || feature.equals(FaceDetectionConstants.FACE_DETECT_SINGLE_DOUBLE))
		{
			List<FeatureValueObject> fvoList = faceDetMap.get(feature);
			if(fvoList!=null)
			{
				ArrayList<String> imagePathList = new ArrayList<String>();
				//final String[] imagePath = new String[fvoList.size()];
				String fileName = "";
				int index = 0;
				for(FeatureValueObject fvo: fvoList)
				{
					//imagePath[index] = imageBasePath + fvo.getImageName();
					imagePathList.add(imageBasePath + fvo.getImageName());
					fileName = fileName + ", "+ fvo.getImageName();
					index = index + 1;
				}
				//Toast.makeText(getApplicationContext(), "Face Detec File List - " + fileName, Toast.LENGTH_SHORT).show();
				Intent i = new Intent(MainActivity.this, CarouselActivity.class);
				i.putStringArrayListExtra("filepath", imagePathList);
				startActivity(i);
				System.gc();
			}
		}
		else
		{
			ArrayList<String> annoList = annoListMap.get(feature);
			ArrayList<String> imagePathList = new ArrayList<String>();	
			if(annoList!=null)
			{
				String fileName = "";			
				for(String anno : annoList)
				{
					List<FeatureValueObject> fvoList = annotMap.get(anno);
					if(fvoList!=null)
					{
						//final String[] imagePath = new String[fvoList.size()];
						int index = 0;
						for(FeatureValueObject fvo: fvoList)
						{
							//imagePath[index] = imageBasePath + fvo.getImageName();
							imagePathList.add(imageBasePath + fvo.getImageName());
							fileName = fileName + ", "+ fvo.getImageName();
							index = index + 1;
						}
					}

				}
			}
			if(imagePathList.isEmpty())
			{
				Toast.makeText(getApplicationContext(), "Sorry!!! No images found.", Toast.LENGTH_SHORT).show();
				searchView.setText("");
			}
			else
			{
				//Toast.makeText(getApplicationContext(), "File List - " + fileName, Toast.LENGTH_SHORT).show();
				Intent i = new Intent(MainActivity.this, CarouselActivity.class);
				i.putStringArrayListExtra("filepath", imagePathList);
				startActivity(i);
				System.gc();
			}
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{         
		switch (item.getItemId())
		{
		case R.id.DebugScreen:
			//Toast.makeText(MainActivity.this, "DebugScreen is selected", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(MainActivity.this, DebugActivity.class);
			startActivity(intent);
			return true;
		case R.id.WekaDebugScreen:
			//Toast.makeText(MainActivity.this, "Weka DebugScreen is selected", Toast.LENGTH_SHORT).show();
			intent = new Intent(MainActivity.this, WekaDebugActivity.class);
			startActivity(intent);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public String convertResponseToString(InputStream inputStream) throws IllegalStateException, IOException
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

	String[] annotations =
		{	FaceDetectionConstants.FACE_DETECT_SELFIE, FaceDetectionConstants.FACE_DETECT_GROUP,FaceDetectionConstants.FACE_DETECT_ALL,FaceDetectionConstants.FACE_DETECT_SINGLE_DOUBLE,
			"shoes","office","student","dance","desk","snow","woods","sleep","fishing","skin","earth","chair","asylum","birds",
			"christmas","hills","cup","rose","father","lake","wings","attraction","window","wood","parents","smile","piano","hat",
			"dolls","dad","fly","evening","garden","food","kitty","school","trees","coast","band","flora","fan","snake","hands","animals","shadow","cage",
			"hall","loss","bat","puppy","clouds","meat","artist","painting","clown","bay","queen","sand","race","architecture","night","tower","river","view",
			"scenery","sketch","creek","crowd","doll","house","fish","cruise","spider","sign","hair","lips","street","design","sea","mirror","home","girl",
			"morning","sunrise","wonder","flower","cocktail","paintings","space","sun","factory","ice","moon","lights","fog","market","pets","letter","construction",
			"bird","celebration","body","dog","noise","cars","ipod","team","commercial","lighthouse","cockroach","water","santa","princess","card","cemetery","box",
			"zombie","wildlife","accident","teeth","petals","island","study","lego","punk","blossom","airport","places","party","castle","heart","road","apple",
			"bridge","wall","sky","child","scene","bath","mushrooms","angel","toys","blonde","mist","landscape","concert","city","horse","toy","fence","area",
			"festival","bathroom","girls","sunlight","sports","beer","cats","pool","storm","spring","armory","legs","war","teen","mountains","head","streets",
			"graveyard","kittens","fire","deer","coke","vehicle","park","training","chocolate","glass","flag","kids","bull","baby","wedding","flowers","bug",
			"present","church","king","lady","valley","tank","room","skull","comics","car","tree","bed","cat","monument","cake","graffiti","kitten","male",
			"sculpture","leaves","fortress","history","toilet","pony","stream","cinema","pie","mask","halloween","eyes","curves","pig","autumn","men","guy",
			"pond","heels","rally","ship","face","hospitals","dawn","butt","rainbow","terrorist","winter","dress","cross","field","chest","book","forest","animal",
			"poster","performance","glasses","beach","theatre","butterfly","plant","hotel","star","beauty","mountain","farm","eggs","drink","driver","rain","tattoo",
			"insect","waterfall","adventure","statue","waves","backyard","cupcake","friends","grave","darkness","building","feet","reflection","phone","windows",
			"industry","grass","children","bugs"
			,"ocean","boat","train","sunset","reflections","mother","landmark","raindrops","model","egg","drawing","reserve"		};
	@Override
	public void processFinish(String asyncCode, String output) {
		Log.i(LOG_TAG, "Clustering is done");
		Intent intent = getIntent();
		finish();
		startActivity(intent);
	}
}