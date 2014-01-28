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

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


import org.apache.log4j.Logger;

public final class SystemProperties
{
  private static boolean propertiesLoaded = false;
  private static Properties systemProperties;
  private static Logger logger = Logger.getLogger(SystemProperties.class);

  private SystemProperties()
  {
  }


  public static String get(String key) {
    if (propertiesLoaded == false) {
      getSystemProperties();
      propertiesLoaded = true;
    }
    return systemProperties.getProperty(key); 
  }


  private static void getSystemProperties() {
    systemProperties = new Properties();
    logger.debug("Loading properties file");

    // These are default properties in case the file is inaccessible or property is missing from file
    systemProperties.put("ss.http_login_timeout", "20");
    systemProperties.put("ss.http_request_timeout", "60");
    systemProperties.put("ss.conn_heartbeat", "5");
    systemProperties.put("ss.echo_sender_interval", "20");
    systemProperties.put("ss.workerThreads", "20");
    systemProperties.put("ss.echo_max_missed_echos", "3");  
    
    try {
      systemProperties.load(new FileInputStream("sdk.properties"));
      systemProperties.load(new FileInputStream("endpoint.properties"));
    } catch (IOException ex) {
      logger.error("Can't load the properties file.",ex);
    }
  }


  public static void setProperty(String key, String value) {
    systemProperties.put(key, value);
  }
  
  
}
