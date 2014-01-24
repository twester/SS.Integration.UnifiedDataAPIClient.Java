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
  private static Hashtable<String, String> propertiesHash = new Hashtable<String, String>();
  private static Logger logger = Logger.getLogger(SystemProperties.class);

  private SystemProperties()
  {
  }


  public static String get(String key)
  {
    if (propertiesLoaded == false) {
      getSystemProperties();
    }
    return propertiesHash.get(key); 
  }

//TODO: read from file
  public static void getSystemProperties()
  {
/*    systemProperties = new Properties();
    try {
      systemProperties.load(new FileInputStream("sdk.properties"));
    } catch (IOException ex) {
      logger.error("Can't load the properties file.",ex);
    }
    logger.debug("properties file loaded");

  */  
    
    propertiesHash.put("ss.url", "http://apicui.sportingsolutions.com");
    propertiesHash.put("ss.username", "sportingsolutions@jimco");
    propertiesHash.put("ss.password", "sporting");
    propertiesHash.put("ss.http_login_timeout", "20");
    propertiesHash.put("ss.http_request_timeout", "60");
    propertiesHash.put("ss.conn_heartbeat", "5");
    propertiesHash.put("ss.echo_sender_interval", "60");
    propertiesHash.put("ss.workerThreads", "20");
    propertiesHash.put("ss.echo_max_missed_echos", "3");  
  }


  
  
}
