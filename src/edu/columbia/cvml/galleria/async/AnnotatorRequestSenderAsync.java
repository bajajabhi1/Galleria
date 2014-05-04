package edu.columbia.cvml.galleria.async;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.util.Log;

public class AnnotatorRequestSenderAsync extends AsyncTask<InputStream, Void, String> {

	private AsyncTaskRequestResponse delegate=null;
	public static final String ASYNC_TASK_CODE = "ANNOTATOR";
	public static final String INDEX_FILE = "ANNOTATOR_INDEX";
	public static final String ANNOTATIONS_SEPARATOR = ","; 
	public static final String ANNOTATIONS_VALUE_SEPARATOR = ":";
	public static final String ANNOTATIONS_FILENAME_SEPARATOR = ":";
	private String imageFileName = null;
	InputStream is = null;
	String result = "";
	String error_text="";
	JSONObject j = null;
	String LOG_TAG =  "ServerRequestSender";
	String twoHyphens = "--";
	String boundary ="*****";
	String lineEnd = "\r\n";
	int bytesRead, bytesAvailable, bufferSize;
	byte[] buffer;
	int maxBufferSize = 1*1024*1024;
	public static final String TAG_FEATURES = "features";
	public static final String TAG_VALUE = "value";
	public static final String TAG_KEY = "key";
	public static final String TAG_FILENAME_KEY = "filename";
	public static final int TOP_K = 10;
	final String URL_SERVER = "http://192.168.209.1:8080/upload";
	//final String URL_SERVER = "http://10.0.2.2:8080/upload";
	
	public AnnotatorRequestSenderAsync(AsyncTaskRequestResponse resp, String fileName)
	{
		this.delegate = resp;
		this.imageFileName = fileName;
	}

	protected String doInBackground(InputStream... fileInputStream)
	{
		String response = null;
		try {
			//ArrayList nameValuePairs = new ArrayList();

			//nameValuePairs.add(new BasicNameValuePair("myfile",image_str[0]));
			//HttpClient httpclient = new DefaultHttpClient();
			/*HttpPost httppost = new HttpPost(URL);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			String the_string_response = convertResponseToString(response);
			//Toast.makeText(ServerRequestSender.this, "Response " + the_string_response, Toast.LENGTH_LONG).show();
			return the_string_response;*/
			//FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
			//FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
			URL url = new URL(URL_SERVER);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			// Allow Inputs &amp; Outputs.
			connection.setDoInput(true);
			connection.setDoOutput(true);
			connection.setUseCaches(false);
			// Set HTTP method to POST.
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Connection", "Keep-Alive");
			connection.setRequestProperty("Content-Type", "multipart/form-data;boundary="+boundary);
			DataOutputStream outputStream = new DataOutputStream( connection.getOutputStream() );
			outputStream.writeBytes(twoHyphens + boundary + lineEnd);
			outputStream.writeBytes("Content-Disposition: form-data; name=\"myfile\";filename=\""+imageFileName+"\"" + lineEnd);
			outputStream.writeBytes(lineEnd);
			bytesAvailable = fileInputStream[0].available();
			bufferSize = Math.min(bytesAvailable, maxBufferSize);
			buffer = new byte[bufferSize];
			// Read file
			bytesRead = fileInputStream[0].read(buffer, 0, bufferSize);
			while (bytesRead > 0)
			{
				outputStream.write(buffer, 0, bufferSize);
				bytesAvailable = fileInputStream[0].available();
				bufferSize = Math.min(bytesAvailable, maxBufferSize);
				bytesRead = fileInputStream[0].read(buffer, 0, bufferSize);
			}
			outputStream.writeBytes(lineEnd);
			outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);
			connection.getInputStream();
			response = convertResponseToString(connection.getInputStream());
			Log.d(LOG_TAG, "http response => " + response);
			fileInputStream[0].close();
			outputStream.flush();
			outputStream.close();
		} 
		catch(Exception e)
		{
			Log.e(LOG_TAG, "Error in http connection "+e.toString());
			e.printStackTrace();
		}
		return response;
	}

	protected void onPostExecute(String result)
	{
		Log.d(LOG_TAG, "Result - "+result);
		/*String annotations = "";
		String fileName = "";
		try
		{
			JSONObject jsonObj = new JSONObject(result);
			fileName = jsonObj.getString(TAG_FILENAME_KEY);
			// Getting JSON Array node
			JSONArray featuresJson = jsonObj.getJSONArray(TAG_FEATURES);

			// looping through All Contacts
			for (int i = 0; i < TOP_K; i++) {
				JSONObject c = featuresJson.getJSONObject(i);
				String name = c.getString(TAG_KEY);
				String value = c.getString(TAG_VALUE);
				annotations = annotations + ANNOTATIONS_SEPARATOR + name + ANNOTATIONS_VALUE_SEPARATOR + value;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.e(LOG_TAG, e.getMessage());
			e.printStackTrace();
		}*/
		delegate.processFinish(ASYNC_TASK_CODE, result);
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
}
