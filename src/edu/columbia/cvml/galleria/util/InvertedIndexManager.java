package edu.columbia.cvml.galleria.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import edu.columbia.cvml.galleria.VO.FeatureValueObject;

public class InvertedIndexManager {

	String invIdxFile = null;
	Context ctx = null;
	Map<String,List<FeatureValueObject>> indexMap = new HashMap<String,List<FeatureValueObject>>();

	public InvertedIndexManager(Context context, String indexFile)
	{
		this.invIdxFile = indexFile;
		this.ctx = context;
	}

	/**
	 *  Input is image file name and the string of annotations seperated by seperator
	 */
	public void addImageEntry(List<FeatureValueObject> features)
	{
		// TODO update the indexMap
		// write it to file
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public Map<String,List<FeatureValueObject>> getIndexMap()
	{
		return indexMap;
	}
	
	public Map<String,List<FeatureValueObject>> loadIndex()
	{
		ObjectInputStream inputStream = null;
		Map<String,List<FeatureValueObject>> idxMap = null;
		try {
			inputStream = new ObjectInputStream(ctx.openFileInput(invIdxFile));
			idxMap = (HashMap<String,List<FeatureValueObject>>)inputStream.readObject();         
			System.out.println("Reading from the file");
			Set<String> keys = idxMap.keySet();
			for(String key : keys)
			{
				System.out.println(key + " => " + idxMap.get(key));
			}
		} catch (StreamCorruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		return idxMap;
	}

	public void writeIndex()
	{
		ObjectOutputStream outStream = null;
		try
		{
			outStream = new ObjectOutputStream(ctx.openFileOutput(invIdxFile, Context.MODE_APPEND));
			outStream.writeObject(indexMap);
			outStream.flush();
			outStream.close();
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
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
