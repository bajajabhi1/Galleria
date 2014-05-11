package edu.columbia.cvml.galleria.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import edu.columbia.cvml.galleria.VO.FeatureValueObject;
import edu.columbia.cvml.galleria.async.AnnotatorRequestSenderAsync;

public class FeatureJSONParser
{
	private static final String LOG_TAG = "FeatureJSONParser";

	public static List<FeatureValueObject> getFeatureList(String json)
	{
		List<FeatureValueObject> featureList = new ArrayList<FeatureValueObject>();

		try
		{
			FeatureValueObject fvobj = null;
			JSONObject jsonObj = new JSONObject(json);
			String imageName = jsonObj.getString(AnnotatorRequestSenderAsync.TAG_FILENAME_KEY);
			// Getting JSON Array node
			JSONArray featuresJson = jsonObj.getJSONArray(AnnotatorRequestSenderAsync.TAG_FEATURES);

			// looping through top K features
			for (int i = 0; i < AnnotatorRequestSenderAsync.TOP_K; i++) {
				JSONObject c = featuresJson.getJSONObject(i);
				String featureName = c.getString(AnnotatorRequestSenderAsync.TAG_KEY);
				String featureValue = c.getString(AnnotatorRequestSenderAsync.TAG_VALUE);
				Float featureFloatValue = Float.valueOf(featureValue);
				fvobj = new FeatureValueObject(imageName, featureName, featureFloatValue);
				featureList.add(fvobj);
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}
		return featureList;
	}

	public static String getClusterFeatureString(String json)
	{
		String featureStr = "";

		try
		{
			JSONObject jsonObj = new JSONObject(json);
			// Getting JSON Array node
			JSONArray featuresJson = jsonObj.getJSONArray(AnnotatorRequestSenderAsync.TAG_FEATURES);

			// looping through all features
			for (int i = 0; i < featuresJson.length(); i++)
			{
				JSONObject c = featuresJson.getJSONObject(i);
				String featureValue = c.getString(AnnotatorRequestSenderAsync.TAG_VALUE);
				featureStr = featureStr + ClusterFeatureManager.FEATURE_SEPARATOR + featureValue;
			}
			if(featureStr.charAt(0) == ClusterFeatureManager.FEATURE_SEPARATOR)
			{
				featureStr = featureStr.substring(1);
			}
		} catch (JSONException e) {
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}
		return featureStr;
	}

	public static String getImageName(String json)
	{
		try
		{
			JSONObject jsonObj = new JSONObject(json);
			return jsonObj.getString(AnnotatorRequestSenderAsync.TAG_FILENAME_KEY);
		} 
		catch (JSONException e)
		{
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
}
