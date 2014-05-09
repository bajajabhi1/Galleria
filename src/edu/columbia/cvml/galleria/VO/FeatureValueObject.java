package edu.columbia.cvml.galleria.VO;

import java.io.Serializable;

public class FeatureValueObject implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	String feature = null;
	String imageName = null;
	Float featureValue = null;
	
	public FeatureValueObject(String imageName, String featureName, Float featureValue)
	{
		this.feature = featureName;
		this.featureValue = featureValue;
		this.imageName = imageName;
	}
	
	public String getFeature() {
		return feature;
	}
	public void setFeature(String feature) {
		this.feature = feature;
	}
	public String getImageName() {
		return imageName;
	}
	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public Float getFeatureValue() {
		return featureValue;
	}
	public void setFeatureValue(Float featureValue) {
		this.featureValue = featureValue;
	}
	
	public String toString()
	{
		return imageName+":"+featureValue;
	}
}
