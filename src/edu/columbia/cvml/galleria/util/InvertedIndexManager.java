package edu.columbia.cvml.galleria.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.util.Log;
import edu.columbia.cvml.galleria.VO.FeatureValueObject;

public class InvertedIndexManager {

	String invIdxFile = null;
	private static final String LOG_TAG = "InvertedIndexManager";
	Context ctx = null;
	Map<String,List<FeatureValueObject>> indexMap = new HashMap<String,List<FeatureValueObject>>();

	public InvertedIndexManager(Context context, String indexFile)
	{
		this.invIdxFile = indexFile;
		this.ctx = context;
	}

	/**
	 *  Input is image file name and the string of annotations separated by separator
	 */
	public void addImageEntry(List<FeatureValueObject> features)
	{
		Log.d(LOG_TAG," in addImageEntry");
		if(indexMap.isEmpty())
		{
			Log.d(LOG_TAG," in addImageEntry : indexMap is empty ");
			List<FeatureValueObject> fvoList = null;
			for(FeatureValueObject fvo : features)
			{
				fvoList = new LinkedList<FeatureValueObject>();
				fvoList.add(fvo);
				indexMap.put(fvo.getFeature(), fvoList);
			}
		}
		else 
		{
			for(FeatureValueObject fvo : features)

			{
				List<FeatureValueObject> fvoList = indexMap.get(fvo.getFeature());
				if(fvoList == null)
				{
					Log.d(LOG_TAG," in addImageEntry : not found - " +fvo.getFeature());
					List<FeatureValueObject> fvoNewList = new LinkedList<FeatureValueObject>();
					fvoNewList.add(fvo);
					indexMap.put(fvo.getFeature(), fvoNewList);
				}
				else
				{
					Log.d(LOG_TAG," in addImageEntry : found - " +fvo.getFeature());
					int idx = 0;
					boolean found = false;
					Iterator<FeatureValueObject> itr = fvoList.iterator();
					while(itr.hasNext())
					{
						FeatureValueObject mapFvo = itr.next();
						if(fvo.getFeatureValue() > mapFvo.getFeatureValue())
						{
							found = true;
							Log.d(LOG_TAG,"In found true");
							break;							
						}
						idx++;
					}
					if (found)
						fvoList.add(idx, fvo);
				}
			}
		}

	}

	public void addSingleFeatureEntry(FeatureValueObject feature)
	{
		Log.d(LOG_TAG," in addImageEntry");
		if(indexMap.isEmpty())
		{
			Log.d(LOG_TAG," in addImageEntry : indexMap is empty ");
			List<FeatureValueObject> fvoList = new LinkedList<FeatureValueObject>();
			fvoList.add(feature);
			indexMap.put(feature.getFeature(), fvoList);
		}
		else 
		{
			List<FeatureValueObject> fvoList = indexMap.get(feature.getFeature());
			if(fvoList == null)
			{
				Log.d(LOG_TAG," in addImageEntry : not found - " +feature.getFeature());
				List<FeatureValueObject> fvoNewList = new LinkedList<FeatureValueObject>();
				fvoNewList.add(feature);
				indexMap.put(feature.getFeature(), fvoNewList);
			}
			else
			{
				fvoList.add(0,feature); // Add in front of list as latest
			}
		}

	}

	public Map<String,List<FeatureValueObject>> getIndexMap()
	{
		return indexMap;
	}

	public Map<String,List<FeatureValueObject>> loadIndex()
	{
		ObjectInputStream inputStream = null;
		try
		{
			inputStream = new ObjectInputStream(ctx.openFileInput(invIdxFile));
			indexMap = (HashMap<String,List<FeatureValueObject>>)inputStream.readObject();         
			Log.d(LOG_TAG,"Reading from the file");
			Set<String> keys = indexMap.keySet();
			for(String key : keys)
			{
				Log.d(LOG_TAG,key + " => " + indexMap.get(key));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG,e.getMessage());
			//e.printStackTrace();
		}
		finally
		{
			if(inputStream!=null)
			{
				try {
					inputStream.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}
		return indexMap;
	}

	public void writeIndex()
	{
		ObjectOutputStream outStream = null;
		try
		{
			outStream = new ObjectOutputStream(ctx.openFileOutput(invIdxFile,Context.MODE_PRIVATE));
			outStream.writeObject(indexMap);
			outStream.flush();
			outStream.close();
			Log.d(LOG_TAG,"Index written to file");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG,e.getMessage());
			//e.printStackTrace();
		}
		finally
		{
			if(outStream!=null)
			{
				try {
					outStream.close();
				} catch (IOException e) {
					//ignore
				}
			}
		}

	}

}
