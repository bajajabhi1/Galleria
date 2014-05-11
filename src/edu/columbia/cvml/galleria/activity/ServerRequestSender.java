package edu.columbia.cvml.galleria.activity;

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

public class ServerRequestSender extends AsyncTask<InputStream, Void, String> {

	public ServerRequestResponse delegate=null;
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
	private static final String TAG_FEATURES = "features";
	private static final String TAG_VALUE = "value";
	private static final String TAG_KEY = "key";
	private static final int TOP_K = 10;

	public ServerRequestSender(ServerRequestResponse resp)
	{
		this.delegate = resp;
	}

	protected String doInBackground(InputStream... fileInputStream) {

		String response = null;
		try {
			//ArrayList nameValuePairs = new ArrayList();

			//nameValuePairs.add(new BasicNameValuePair("myfile",image_str[0]));
			//HttpClient httpclient = new DefaultHttpClient();
			//final String urlServer = "http://192.168.209.1:8080/upload";
			final String urlServer = "http://10.0.2.2:8080/upload";
			/*HttpPost httppost = new HttpPost(URL);
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			String the_string_response = convertResponseToString(response);
			//Toast.makeText(ServerRequestSender.this, "Response " + the_string_response, Toast.LENGTH_LONG).show();
			return the_string_response;*/
			//FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
			//FileInputStream fileInputStream = new FileInputStream(new File(pathToOurFile) );
			URL url = new URL(urlServer);
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
			outputStream.writeBytes("Content-Disposition: form-data; name=\"myfile\";filename=\"testImage.jpg\"" + lineEnd);
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
			// Responses from the server (code and message)
			//int serverResponseCode = connection.getResponseCode();
			//String serverResponseMessage = connection.getResponseMessage();
			connection.getInputStream();
			response = convertResponseToString(connection.getInputStream());
			//Log.i(LOG_TAG, "http code "+serverResponseCode);
			Log.i(LOG_TAG, "http response "+response);
			fileInputStream[0].close();
			outputStream.flush();
			outputStream.close();
		} catch(Exception e){
			Log.e(LOG_TAG, "Error in http connection "+e.toString());
			e.printStackTrace();
		}
		return response;
	}

	protected void onPostExecute(String result) {
		
		Log.i(LOG_TAG, "Result- "+result);
		String annotations = "";
		try
		{
			JSONObject jsonObj = new JSONObject(result);
			// Getting JSON Array node
			JSONArray featuresJson = jsonObj.getJSONArray(TAG_FEATURES);

			// looping through All Contacts
			for (int i = 0; i < TOP_K; i++) {
				JSONObject c = featuresJson.getJSONObject(i);
				String name = c.getString(TAG_KEY);
				//String id = c.getString(TAG_VALUE);
				annotations = annotations + ", " + name;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		delegate.processFinish(annotations);
	}

	public String convertResponseToString(InputStream inputStream ) throws IllegalStateException, IOException
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
