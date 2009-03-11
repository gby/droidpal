package codefidence.droidpal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.util.Log;

public class RestClient {

	private static String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	private static UrlEncodedFormEntity getFormEntity() throws UnsupportedEncodingException {

	    ArrayList<NameValuePair> values = new ArrayList<NameValuePair>(1);
	    values.add(new BasicNameValuePair("method", "android.NewComments"));
	    UrlEncodedFormEntity entity = new UrlEncodedFormEntity(values, HTTP.UTF_8);
	    return entity;

	}
	
	public static int get_comment_count(String url) {

		DefaultHttpClient http_client = new DefaultHttpClient();
		HttpPost post_method = new HttpPost(url);
		
		try {		
		
			HttpParams params = new BasicHttpParams();

			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);
			HttpProtocolParams.setUseExpectContinue(params, false); 
			
			params.setParameter("method", "android.NewComments");
			
            http_client.setParams(params);

            UrlEncodedFormEntity requestEntity = getFormEntity();
            post_method.setEntity(requestEntity);
		
			HttpResponse response;

			response = http_client.execute(post_method);

			Log.i("REST:Response Status line", response.getStatusLine().toString());

			HttpEntity entity = response.getEntity();

			if (entity != null) {

				InputStream instream = entity.getContent();
				String result = convertStreamToString(instream);
				Log.i("REST: result", result);
				
				JSONObject json = new JSONObject(result);
				Log.i("REST",  json.toString());

				// Parsing
				boolean server_error = json.getBoolean("#error");
				
				if(server_error) {
					throw (new ClientProtocolException(json.getString("#data")));
				}
				
				JSONObject data = json.optJSONObject("#data");
			
				Log.i("REST", "Number of comments: " + data.length());
				
/*				JSONArray nameArray = json.names();
				JSONArray valArray = json.toJSONArray(nameArray);
				for (int i = 0; i < valArray.length(); i++) {
					Log
							.i("REST", "<jsonname" + i + ">\n"
									+ nameArray.getString(i) + "\n</jsonname"
									+ i + ">\n" + "<jsonvalue" + i + ">\n"
									+ valArray.getString(i) + "\n</jsonvalue"
									+ i + ">");
				}*/

				instream.close();
				
				return data.length();
			}

		} catch (Exception e) {
			
		}
		
		return -1;
	}
}
