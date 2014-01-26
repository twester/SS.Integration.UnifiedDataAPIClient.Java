package ss.udapi.sdk.services;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

public class CtagResourceMap
{
  private static CtagResourceMap cTagMap= null;
  private static ConcurrentHashMap<String,String> map = new ConcurrentHashMap<String,String>();
  
  private static Logger logger = Logger.getLogger(CtagResourceMap.class);
  
  private CtagResourceMap()
  {
  }
  
  public static CtagResourceMap getEchoMap(){
    if (cTagMap == null) {
      cTagMap = new CtagResourceMap();
    }
    return cTagMap;
  }
  
  public static void addCtag(String cTag, String resource)
  {
    map.put(cTag, resource);
  }
  
  public static String getResource(String cTag)
  {
    return map.get(cTag);
  }
  
  public static void removeCtag(String cTag)
  {
    map.remove(cTag);
  }
  
  
}
