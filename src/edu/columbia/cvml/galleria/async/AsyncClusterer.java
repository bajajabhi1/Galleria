package edu.columbia.cvml.galleria.async;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import edu.columbia.cvml.galleria.util.ClusterFeatureManager;
import edu.columbia.cvml.galleria.weka.WekaWrapper;

public class AsyncClusterer extends AsyncTask <Context, Object, String>{
	/*
	 * (non-Javadoc)
	 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
	 * Function to run the K-means clustering of Weka in an asynchronous manner
	 * so that it doesn't block the main thread.
	 */
	public static final String ASYNC_TASK_CODE = "ASYNC_KM";
	private Activity currentActivity;
	String LOG_TAG = "Async_Clusterer";
	public AsyncTaskRequestResponse delegate = null;
	public static final String FILE_SEPARATOR = ","; 
	public static final String CLUSTER_SEPARATOR = "#";
	public static final String CLUSTER_FILENAME_SEPARATOR = "::";

	//Context context;
	//TextView outputText;
	public AsyncClusterer(AsyncTaskRequestResponse resp, Activity activity)
	{
		this.currentActivity = activity;
		this.delegate = resp;
	}


	@Override
	protected String doInBackground(Context... params) {

		String output = "Task has not executed";
		Context context = (Context)params[0] ;
		Log.i(LOG_TAG, "BEGIN ASYNC KM");
		if (context==null)
		{
			Log.e(LOG_TAG, "No context initialized - please attach context");
		}

		else {
			Log.i(LOG_TAG, "Context initialized, starting operations");
			try {
				output = performClustering(context);
			} catch (Exception e)  {
				Log.e(LOG_TAG,"Some error happened - message " + e.getLocalizedMessage());
			}    
		}
		return output;

	}

	protected String performClustering(Context context) throws Exception
	{
		String output = "";
		//ClusterFeatureManager CFM = new ClusterFeatureManager(context);
		ClusterFeatureManager CFM =  ClusterFeatureManager.getInstance (context);
		WekaWrapper wek = new WekaWrapper();
		Log.i(LOG_TAG,"wrapper created");   
		Map<String,ArrayList<String>> indexMap = null;
		InputStream image_csv;

		try {

			String csv_string = CFM.loadClusterImageFile();
			Log.i(LOG_TAG, "CSV String loaded"); 

			FileOutputStream fos = currentActivity.openFileOutput(wek.tempfile, Context.MODE_PRIVATE);
			fos.write(csv_string.getBytes());
			fos.close();

			InputStream fileinput = currentActivity.openFileInput(wek.tempfile);

			wek.loadData(fileinput);
			wek.setNumberOfClusters(10);
			Log.i(LOG_TAG,"Data loaded");
			wek.loadIndex(CFM.loadFeatureLineImageMap());           
			Log.i(LOG_TAG,"index loaded");
			output = wek.runKMeans();                        
			Log.i(LOG_TAG,output);
			//wek.describeClusters();


			indexMap = wek.getMappings();


			Log.i(LOG_TAG, "Map created of size" + indexMap.size());
			ClusterFeatureManager.writeClusterFeatureMap(context, indexMap);
			Log.i(LOG_TAG, "Finished storing ClusterFeatureMap");

		} 
		catch (FileNotFoundException e) 
		{
			Log.e(LOG_TAG, "Probably file not found exception: " + e.getLocalizedMessage() + " " + e.getMessage());
			e.printStackTrace();            
		}

		Log.i(LOG_TAG, "AsyncClusterer is shutting down");

		output = createClusterString(wek.getMappings());
		return output;
	}

	protected void onPostExecute(String result) {
		//Log.i(LOG_TAG, "Result of Image Detection - "+ imageFileName + FACEDETECTOR_FILENAME_SEPARATOR + result);
		Log.i(LOG_TAG, "onPost Execute executing");
		delegate.processFinish(ASYNC_TASK_CODE, result);
	}

	public String createClusterString(Map<String, ArrayList<String>> indexMap)
	{   
		Set<String> keys  = indexMap.keySet();
		String clusterStr = "";
		for(String key : keys)
		{
			String fileNameList = "";
			ArrayList<String> fileList = indexMap.get(key);
			for(String fileName:fileList)
			{
				if(fileNameList.equals(""))
				{
					fileNameList = fileName;
				}
				else
				{
					fileNameList = fileNameList + FILE_SEPARATOR + fileName;
				}
			}
			clusterStr = key + CLUSTER_FILENAME_SEPARATOR + fileNameList + CLUSTER_SEPARATOR;
		}
		clusterStr = clusterStr.substring(0, clusterStr.lastIndexOf(CLUSTER_SEPARATOR));
		Log.i(LOG_TAG,"Cluster Str = " + clusterStr);
		return clusterStr;
	}
}
