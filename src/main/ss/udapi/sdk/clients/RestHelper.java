//Copyright 2012 Spin Services Limited

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package ss.udapi.sdk.clients;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.log4j.Logger;

public class RestHelper {

	private static Logger logger = Logger.getLogger(RestHelper.class.getName());
	
	public static HttpURLConnection createConnection(URL url, String data, String httpMethod, String contentType, Integer timeout,
			Map<String, String> headers, Boolean gzip)
	{
		HttpURLConnection connection = null;
		try
		{
			connection = (HttpURLConnection) url.openConnection();// proxy);
			connection.setRequestMethod(httpMethod.toUpperCase());
			connection.setRequestProperty("Content-Type", contentType);
			connection.setReadTimeout(timeout);
			connection.setUseCaches(false);
			connection.setDoInput(true);
			connection.setDoOutput(true);

			if (headers != null)
			{
				for (String key : headers.keySet())
				{
					connection.setRequestProperty(key, headers.get(key));
				}
			}

			if (gzip)
			{
				connection.setRequestProperty("Accept-Encoding", "gzip");
			}

			if (data == null)
			{
				data = "";
			}

			if (httpMethod.toUpperCase().equals("POST"))
			{
				connection.setFixedLengthStreamingMode(data.getBytes().length);
			}

			if (!data.equals(""))
			{
				DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
				wr.writeBytes(data);
				wr.close();
			}

		} catch (Exception ex)
		{
			logger.warn("Connection Creation Failed", ex);
			connection = null;
		}
		return connection;
	}
	
	public static String getResponse(URL url, String data, String httpMethod, String contentType, 
			Integer timeout, Map<String,String> headers, Boolean gzip)
	{
		return getResponse(url, data, httpMethod, contentType, timeout, headers, gzip, "");
	}

	public static String getResponse(URL url, String data, String httpMethod, String contentType, Integer timeout, 
			Map<String, String> headers, Boolean gzip, String errorResp)
	{
		HttpURLConnection theConnection = createConnection(url, data, httpMethod, contentType, timeout, headers, gzip);
		
		if (theConnection != null)
		{
			try
			{
				InputStream inputStream = null;
				inputStream = theConnection.getInputStream();
				return getResponse(inputStream, gzip);
			} catch (IOException ex)
			{
				logger.warn("Unable to read response " + ex.getMessage());
			}
		}
		return errorResp;
	}
	
	public static String getResponse(InputStream inputStream, Boolean gzip){
		
		StringBuffer responseBuffer = new StringBuffer(); 
		
		try{
			if(gzip){
				inputStream = new GZIPInputStream(inputStream);  
			}
		
			BufferedReader rd = new BufferedReader( new InputStreamReader(inputStream) );
			String line;

	    	while((line = rd.readLine()) != null) {
		    	responseBuffer.append(line);
		        //responseBuffer.append('\n');
		    }
		    rd.close();
	    }catch(Exception ex){
	    	logger.warn("Get Response Failed", ex );	
	    } 
	    
	    return responseBuffer.toString();
	}

}
