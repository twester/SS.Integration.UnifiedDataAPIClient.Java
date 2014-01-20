package ss.udapi.sdk.services;

import java.io.File;
import java.io.FileInputStream;
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
    
    propertiesHash.put("ss.url", "http://apicui.sportingsolutions.com");
    propertiesHash.put("ss.username", "sportingsolutions@jimco");
    propertiesHash.put("ss.password", "sporting");
    propertiesHash.put("ss.http_login_timeout", "20");
    propertiesHash.put("ss.http_request_timeout", "60");
    propertiesHash.put("ss.conn_heartbeat", "5");
  }


  
  
}