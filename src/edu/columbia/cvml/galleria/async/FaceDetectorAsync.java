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
	public static final String FACEDETECTOR_FILENAME_SEPARATOR = ":";
	public AsyncTaskRequestResponse delegate=null;
	private String imageFileName = null;
	private int MAX_NO_OF_FACES = 8;
	private FaceDetector myFaceDetect; 
	private FaceDetector.Face[] myFace;
	float myEyesDistance;
	int numberOfFaceDetected;
	private int DESIREDWIDTH = 480;
	private int DESIREDHEIGHT = 640;
	private String LOG_TAG = "FaceDetector";

	public FaceDetectorAsync(AsyncTaskRequestResponse resp, String fileName)
	{
		this.delegate = resp;
		this.imageFileName = fileName;
	}

	protected String doInBackground(Bitmap... origBitMap)
	{
		String response = "";
		Bitmap scaledBitmap = ScalingUtility.createScaledBitmap(origBitMap[0], DESIREDWIDTH, DESIREDHEIGHT, ScalingLogic.FIT);
		Bitmap maskBitmap = Bitmap.createBitmap( scaledBitmap.getWidth(), scaledBitmap.getHeight(), Bitmap.Config.RGB_565 );
		Log.i(LOG_TAG, "scaledBitmap.getWidth() - " + scaledBitmap.getWidth());
		Log.i(LOG_TAG, "scaledBitmap.getHeight() - " + scaledBitmap.getHeight());
		Canvas c = new Canvas();
		c.setBitmap(maskBitmap);
		Paint p = new Paint();
		p.setFilterBitmap(true); // possibly not nessecary as there is no scaling
		c.drawBitmap(scaledBitmap,0,0,p);
		scaledBitmap.recycle();
		myFace = new FaceDetector.Face[MAX_NO_OF_FACES];
		myFaceDetect = new FaceDetector( maskBitmap.getWidth(), maskBitmap.getHeight(), MAX_NO_OF_FACES);
		numberOfFaceDetected = myFaceDetect.findFaces(maskBitmap, myFace);
		if(numberOfFaceDetected == 0)
		{
			response = FaceDetectionConstants.FACE_DETECT_NONE;
		}
		else if(numberOfFaceDetected == 1)
		{
			
			PointF myMidPoint = new PointF();
			myFace[0].getMidPoint(myMidPoint);
			myEyesDistance = myFace[0].eyesDistance();
			Log.i(LOG_TAG, "myEyesDistance - " + myEyesDistance);
			if (myEyesDistance > DESIREDWIDTH/5)
			{
				response = FaceDetectionConstants.FACE_DETECT_SELFIE;
			}
			else
			{
				response = FaceDetectionConstants.FACE_DETECT_SINGLE;				
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
