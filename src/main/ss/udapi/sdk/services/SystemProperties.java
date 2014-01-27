package ss.udapi.sdk.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Set;


import org.apache.log4j.Logger;

public final class SystemProperties
{
  private static boolean propertiesLoaded = false;
  private static Properties systemProperties;
  private static Logger logger = Logger.getLogger(SystemProperties.class);

  private SystemProperties()
  {
  }


  public static String get(String key)
  {
    if (propertiesLoaded == false) {
      getSystemProperties();
      propertiesLoaded = true;
    }
    return systemProperties.getProperty(key); 
  }


  private static void getSystemProperties()
  {
    systemProperties = new Properties();

    systemProperties.put("ss.http_login_timeout", "20");
    systemProperties.put("ss.http_request_timeout", "60");
    systemProperties.put("ss.conn_heartbeat", "5");
    systemProperties.put("ss.echo_sender_interval", "20");
    systemProperties.put("ss.workerThreads", "20");
    systemProperties.put("ss.echo_max_missed_echos", "3");  
    
    try {
      systemProperties.load(new FileInputStream("sdk.properties"));
      systemProperties.load(new FileInputStream("example.properties"));
    } catch (IOException ex) {
      logger.error("Can't load the properties file.",ex);
    }
    
    logger.debug("properties file loaded: " + systemProperties.getProperty("ss.http_login_timeout"));
  }


  public static void setProperty(String key, String value)
  {
    systemProperties.put(key, value);
  }
  
  
}
