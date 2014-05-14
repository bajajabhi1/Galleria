package edu.columbia.cvml.galleria.async;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.AsyncTask;
import android.util.Log;
import edu.columbia.cvml.galleria.util.FaceDetectionConstants;
import edu.columbia.cvml.galleria.util.ScalingUtility;
import edu.columbia.cvml.galleria.util.ScalingUtility.ScalingLogic;

public class FaceDetectorAsync extends AsyncTask<Bitmap, Void, String> {

	public static final String ASYNC_TASK_CODE = "FaceDetector";
	public static final String INDEX_FILE = "FaceDetector_INDEX";
	public static final String FACEDETECTOR_FILENAME_SEPARATOR = "::";
	private static final String LOG_TAG = "FaceDetector";

	public AsyncTaskRequestResponse delegate=null;
	private String imageFileName = null;
	private int MAX_NO_OF_FACES = 8;
	private FaceDetector myFaceDetect; 
	private FaceDetector.Face[] myFace;
	float myEyesDistance = 0;
	float myEyesDistance2 = 0;
	int numberOfFaceDetected;
	private int DESIREDWIDTH = 380;
	private int DESIREDHEIGHT = 672;
	
	public FaceDetectorAsync(AsyncTaskRequestResponse resp, String fileName)
	{
		this.delegate = resp;
		this.imageFileName = fileName;
	}

	protected String doInBackground(Bitmap... origBitMap)
	{
		String response = "";
		Bitmap scaledBitmap = null;
		int divisor = 8;
		if(origBitMap[0].getWidth() > origBitMap[0].getHeight())
		{
			scaledBitmap = ScalingUtility.createScaledBitmap(origBitMap[0], DESIREDWIDTH, DESIREDHEIGHT, ScalingLogic.FIT);
			divisor = 12;
		}
		else
		{
			scaledBitmap = ScalingUtility.createScaledBitmap(origBitMap[0], DESIREDHEIGHT, DESIREDWIDTH, ScalingLogic.FIT);
		}	
		Bitmap maskBitmap = Bitmap.createBitmap( scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.RGB_565 );
		Log.d(LOG_TAG, "scaledBitmap.getWidth() - " + scaledBitmap.getWidth());
		Log.d(LOG_TAG, "scaledBitmap.getHeight() - " + scaledBitmap.getHeight());
		Canvas c = new Canvas();
		c.setBitmap(maskBitmap);
		Paint p = new Paint();
		p.setFilterBitmap(true); // possibly not nessecary as there is no scaling
		c.drawBitmap(scaledBitmap,0,0,p);
		scaledBitmap.recycle();
		myFace = new FaceDetector.Face[MAX_NO_OF_FACES];
		myFaceDetect = new FaceDetector( maskBitmap.getWidth(), maskBitmap.getHeight(), MAX_NO_OF_FACES);
		numberOfFaceDetected = myFaceDetect.findFaces(maskBitmap, myFace);
		Log.i(LOG_TAG, "Faces Detected - " + numberOfFaceDetected);
		if(numberOfFaceDetected == 0)
		{
			response = FaceDetectionConstants.FACE_DETECT_NONE;
		}
		else if(numberOfFaceDetected == 1 || numberOfFaceDetected == 2)
		{
			
			PointF myMidPoint = new PointF();
			myFace[0].getMidPoint(myMidPoint);
			myEyesDistance = myFace[0].eyesDistance();
			if (numberOfFaceDetected == 2)
			{
				PointF myMidPoint2 = new PointF();
				myFace[1].getMidPoint(myMidPoint2);
				myEyesDistance2 = myFace[1].eyesDistance();
				Log.i(LOG_TAG, "myEyesDistance2 - " + myEyesDistance2);
			}
			Log.i(LOG_TAG, "myEyesDistance - " + myEyesDistance);			
			if (myEyesDistance > scaledBitmap.getWidth()/divisor || myEyesDistance2 > scaledBitmap.getWidth()/divisor)
			{
				response = FaceDetectionConstants.FACE_DETECT_SELFIE;
			}
			else
			{
				response = FaceDetectionConstants.FACE_DETECT_SINGLE_DOUBLE;				
			}
		}
		else if(numberOfFaceDetected < 7)
		{
			response = FaceDetectionConstants.FACE_DETECT_GROUP;
		}
		else 
		{
			response = FaceDetectionConstants.FACE_DETECT_ALL;
		}
		return response;
	}

	protected void onPostExecute(String result) {
		Log.i(LOG_TAG, "Result of Image Detection - "+ imageFileName + FACEDETECTOR_FILENAME_SEPARATOR + result);
		delegate.processFinish(ASYNC_TASK_CODE, imageFileName + FACEDETECTOR_FILENAME_SEPARATOR + result);
	}
}
