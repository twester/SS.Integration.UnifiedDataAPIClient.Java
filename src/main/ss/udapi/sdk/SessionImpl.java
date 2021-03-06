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

package ss.udapi.sdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ss.udapi.sdk.clients.RestHelper;
import ss.udapi.sdk.extensions.JsonHelper;
import ss.udapi.sdk.interfaces.Credentials;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.interfaces.Session;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.RestLink;


public class SessionImpl extends Endpoint implements Session {

	private static Logger logger = Logger.getLogger(SessionImpl.class.getName());

	private Boolean isCompressed = false;
	private URL serverURL;
	
	private List<RestItem> serviceRestItems;
	
	public SessionImpl(URL serverURL, Credentials credentials){
		this.serverURL = serverURL;
		headers = new HashMap<String,String>();
		GetRoot(serverURL,credentials, true);
	}
	
	private void GetRoot(URL serverURL, Credentials credentials, Boolean authenticate){
		logger.debug(String.format("Connecting to %1$s", serverURL));
		
		HttpURLConnection theConnection = RestHelper.createConnection(serverURL, null, "GET", "application/json", 20000, headers, isCompressed);
		
		InputStream inputStream = null;
		try{
			if(authenticate){
				if(theConnection.getResponseCode() == 401){
					logger.debug("Not authenticated. Logging on");
					inputStream = theConnection.getErrorStream();
					
					String rawJson = RestHelper.getResponse(inputStream, isCompressed);
					List<RestItem> restItems = JsonHelper.toRestItems(rawJson);
					
					String url = "";
					for(RestItem restItem:restItems){
						for(RestLink restLink:restItem.getLinks()){
							if(restLink.getRelation().equals("http://api.sportingsolutions.com/rels/login")){
								url = restLink.getHref();
								break;
							}
						}
						if(!url.isEmpty()){
							break;
						}
					}
					URL theURL = null;
					try{
						theURL = new URL(url);
					}catch(MalformedURLException ex){
						logger.warn("Malformed Login URL", ex);
					}
					
					serviceRestItems = Login(theURL, credentials);
					logger.info("Logged in successfully");
				}
			}else{
				inputStream = theConnection.getInputStream();
				
				String rawJson = RestHelper.getResponse(inputStream, isCompressed);
				serviceRestItems = JsonHelper.toRestItems(rawJson);
			}
		}catch(Exception ex){
			logger.warn("Get Request Failed", ex);
		}
	}
	
	private List<RestItem> Login(URL loginURL, Credentials credentials){
		Map<String,String> headers = new HashMap<String,String>();
		headers.put("X-Auth-User", credentials.getUserName());
		headers.put("X-Auth-Key", credentials.getPassword());
		
		HttpURLConnection theConnection = RestHelper.createConnection(loginURL, null, "POST", "application/json", 60000, headers, isCompressed);
		Map<String, List<String>> responseMap = theConnection.getHeaderFields();
		
		this.headers.put("X-Auth-Token", responseMap.get("X-Auth-Token").get(0));
		String rawJson = "";
		try {
			rawJson = RestHelper.getResponse(theConnection.getInputStream(), isCompressed);
		} catch (IOException ex) {
			logger.warn("Unable to read response", ex);
		}
		return JsonHelper.toRestItems(rawJson);
	}
	
	public Service getService(String name) {
		logger.info(String.format("Get Service %1$s",name));
		
		if(serviceRestItems == null){
			GetRoot(serverURL,null,false);
		}
		
		if(serviceRestItems != null){
			Service result = null;
			for(RestItem restItem:serviceRestItems){
				if(restItem.getName().equals(name)){
					result = new ServiceImpl(headers,restItem);
				}
			}
			serviceRestItems = null;
			return result;
		}
		return null;
	}

	public List<Service> getServices() {
		logger.info("Get all available services..");
		
		if(serviceRestItems == null){
			GetRoot(serverURL,null,false);
		}
		
		List<Service> result = new ArrayList<Service>();
		if(serviceRestItems != null){
			for(RestItem restItem:serviceRestItems){
				result.add(new ServiceImpl(headers,restItem));
			}
		}
		serviceRestItems = null;
		return result;
	}

}
