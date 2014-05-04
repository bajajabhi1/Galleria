package edu.columbia.cvml.galleria.VO;

public class FeatureValueObject {
	String feature = null;
	String imageName = null;
	String featureValue = null;
	
	public FeatureValueObject(String imageName, String featureName, String featureValue)
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
	public String getFeatureValue() {
		return featureValue;
	}
	public void setFeatureValue(String featureValue) {
		this.featureValue = featureValue;
	}
}
