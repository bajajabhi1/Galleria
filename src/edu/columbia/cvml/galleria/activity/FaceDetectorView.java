package edu.columbia.cvml.galleria.activity;

import java.io.IOException;
import java.io.InputStream;

import edu.columbia.cvml.galleria.util.ScalingUtility;
import edu.columbia.cvml.galleria.util.ScalingUtility.ScalingLogic;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class FaceDetectorView extends View
{

	private int imageWidth, imageHeight;
	private int MAX_NO_OF_FACES = 5;
	private FaceDetector myFaceDetect; 
	private FaceDetector.Face[] myFace;
	float myEyesDistance;
	int numberOfFaceDetected;
	private int DESIREDWIDTH = 480;
	private int DESIREDHEIGHT = 640;
	private String LOG_NAME = "FaceDetectionView";

	Bitmap bitmap;

	public FaceDetectorView(Context context, AttributeSet attrs) {
		super(context,attrs);
		// TODO Auto-generated constructor stub	
	}

	public void doFaceDetection(String img_name)
	{
		AssetManager assetManager = getContext().getAssets();
		try 
		{
			//inpStream = assetManager.open(img_name);
			//bitmap = BitmapFactory.decodeStream(inpStream);
			//ImageView first = (ImageView) findViewById(R.id.imagebox);
			//first.setImageBitmap(bitmap);
			BitmapFactory.Options bitmap_options = new BitmapFactory.Options();
			bitmap_options.inPreferredConfig = Bitmap.Config.RGB_565;
			//Bitmap background_image = BitmapFactory.decodeFile(image_fn, bitmap_options);
			//FaceDetector face_detector = new FaceDetector(background_image.getWidth(), background_image.getHeight(),MAX_FACES);


			InputStream inpStream = assetManager.open(img_name);
			//TextView text = (TextView) findViewById(R.id.annotations);
			//text.setText("Facial Recognition in progress... Please wait...");
			Toast.makeText(getContext(), "Facial Recognition in progress... Please wait...", Toast.LENGTH_LONG).show();
			Bitmap origbitmap = BitmapFactory.decodeStream(inpStream,null,bitmap_options);
			bitmap = ScalingUtility.createScaledBitmap(origbitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingLogic.FIT);
			myFace = new FaceDetector.Face[MAX_NO_OF_FACES];
			Toast.makeText(getContext(), "bitmap.getWidth(): "+ bitmap.getWidth() + ", bitmap.getHeight()" + bitmap.getHeight(), Toast.LENGTH_LONG).show();
			myFaceDetect = new FaceDetector( origbitmap.getWidth(), origbitmap.getHeight(), MAX_NO_OF_FACES);
			numberOfFaceDetected = myFaceDetect.findFaces(origbitmap, myFace);

			invalidate();
			if(numberOfFaceDetected == 0)
			{
				Toast.makeText(getContext(), "No face detected", Toast.LENGTH_LONG).show();
			}
			else if(numberOfFaceDetected == 1)
			{
				Toast.makeText(getContext(), "single face detected", Toast.LENGTH_LONG).show();
			}
			else if(numberOfFaceDetected < 7)
			{
				Toast.makeText(getContext(), "Group image detected", Toast.LENGTH_LONG).show();
			}
			else 
			{
				Toast.makeText(getContext(), "Seems like everyone is here :-) ", Toast.LENGTH_LONG).show();
			}
			Toast.makeText(getContext(), numberOfFaceDetected + " face detected", Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void decodeFile(String path) {
		String strMyImagePath = null;
		Bitmap scaledBitmap = null;

		try 
		{
			// Part 1: Decode image
			Bitmap unscaledBitmap = ScalingUtility.decodeFile(path, DESIREDWIDTH, DESIREDHEIGHT, ScalingLogic.FIT);

			if (!(unscaledBitmap.getWidth() <= 800 && unscaledBitmap.getHeight() <= 800)) {
				// Part 2: Scale image
				scaledBitmap = ScalingUtility.createScaledBitmap(unscaledBitmap, DESIREDWIDTH, DESIREDHEIGHT, ScalingLogic.FIT);
			} else {
				unscaledBitmap.recycle();
				//return path;
			}

			// Store to tmp file

			/*String extr = Environment.getExternalStorageDirectory().toString();
            File mFolder = new File(extr + "/myTmpDir");
            if (!mFolder.exists()) {
                mFolder.mkdir();
            }

            String s = "tmp.png";

            File f = new File(mFolder.getAbsolutePath(), s);

            strMyImagePath = f.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(f);
                scaledBitmap.compress(Bitmap.CompressFormat.PNG, 70, fos);
                fos.flush();
                fos.close();
            } catch (FileNotFoundException e) {

                e.printStackTrace();
            } catch (Exception e) {

                e.printStackTrace();
            }

            scaledBitmap.recycle();*/
		}
		catch (Throwable e)
		{
		}

		/*if (strMyImagePath == null) {
            return path;
        }
        return strMyImagePath;*/

	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		Log.i("FD", "on Draw call");
		//Toast.makeText(getContext(), "In OnDraw().", Toast.LENGTH_LONG).show();
		if(bitmap!=null)
		{
			//Toast.makeText(getContext(), "In if().", Toast.LENGTH_LONG).show();
			canvas.drawBitmap(bitmap, 0, 0, null);

			Paint myPaint = new Paint();
			myPaint.setColor(Color.GREEN);
			myPaint.setStyle(Paint.Style.STROKE); 
			myPaint.setStrokeWidth(3);

			for(int i=0; i < numberOfFaceDetected; i++)
			{
				//Toast.makeText(getContext(), "In loop " + i, Toast.LENGTH_LONG).show();
				Face face = myFace[i];
				PointF myMidPoint = new PointF();
				face.getMidPoint(myMidPoint);
				myEyesDistance = face.eyesDistance();
				canvas.drawRect(
						(int)(myMidPoint.x - myEyesDistance),
						(int)(myMidPoint.y - myEyesDistance),
						(int)(myMidPoint.x + myEyesDistance),
						(int)(myMidPoint.y + myEyesDistance),
						myPaint);
				
				if(numberOfFaceDetected ==1 && myEyesDistance > DESIREDWIDTH/5)
				{
					Toast.makeText(getContext(), "Its a selfie.", Toast.LENGTH_LONG).show();
				}
			}
		}
	}
}
