//Copyright 2014 Spin Services Limited

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package ss.udapi.sdk.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

/*
 * Loads properties from multiple property files if they exist.  Otherwise provides reasonable defaults and logs the fact that
 * a property was not found in the properties file.
 */
public class SystemProperties {
	
	private static boolean propertiesLoaded = false;
	private static Properties systemProperties;
	
	/*
	 * using ConcurrentHashMap instead of another instance of properties as it
	 * is, er... concurrency aware and as values can change during
	 * initialisation depending on supplied arguments.
	 */
	private static ConcurrentHashMap<String, String> ourMap = new ConcurrentHashMap<String, String>();
	private static Logger logger = Logger.getLogger(SystemProperties.class);

	private SystemProperties() {}

	public static String get(String key) {
		getSystemProperties();
		return ourMap.get(key);
	}

	private synchronized static void getSystemProperties() {

		if (propertiesLoaded == true)
			return;
		
		systemProperties = new Properties();
		logger.debug("Loading properties file");

		// These are default properties in case the file is inaccessible or
		// property is missing from file
		ourMap.put("ss.http_login_timeout", "20");
		ourMap.put("ss.http_request_timeout", "60");
		ourMap.put("ss.conn_heartbeat", "60");
		ourMap.put("ss.echo_sender_interval", "10");
		ourMap.put("ss.echo_max_missed_echos", "3");
		ourMap.put("ss.workerThreads", "20");

		FileInputStream stream = null;
		try {
			
			File f = new File("sdk.properties");
			if(f.exists()) {
				stream = new FileInputStream("sdk.properties");
				systemProperties.load(stream);
			}
			
			Set<String> propNames = ourMap.keySet();
			String readPropVal = null;

			// Here we alert about missing properties or load them in if they
			// are present
			for (String propName : propNames) {
				readPropVal = systemProperties.getProperty(propName);
				if (readPropVal == null) {
					logger.warn("Property [" + propName
							+ "] not found in sdk.properties file.  Using ["
							+ ourMap.get(propName) + "] as a default.");
				} else {
					ourMap.put(propName, readPropVal);
				}
			}
			
			propertiesLoaded = true;
			
		} catch (IOException ex) {
			logger.error("Can't load the properties file:  sdk.properties");
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (Exception ex1) {}
			}
		}
	}

	// Sets values, such as credentials which can be set by the client code via
	// the SDK public API.
	public static void setProperty(String key, String value) {
		ourMap.put(key, value);
	}

}
