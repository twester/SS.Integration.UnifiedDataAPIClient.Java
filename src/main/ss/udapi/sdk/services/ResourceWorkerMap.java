package ss.udapi.sdk.services;

import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ss.udapi.sdk.interfaces.Resource;

public class ResourceWorkerMap
{
  private static ResourceWorkerMap workerMap= null;
  private static ConcurrentHashMap<String,Resource> map = new ConcurrentHashMap<String,Resource>();
  
  private static Logger logger = Logger.getLogger(ResourceWorkerMap.class);
  
  private ResourceWorkerMap()
  {
  }
  
  public static ResourceWorkerMap getWorkerMap(){
    if (workerMap == null) {
      workerMap = new ResourceWorkerMap();
    }
    return workerMap;
  }
  
  public static void addUOW(String resourceId, Resource resourceImpl)
  {
    map.put(resourceId, resourceImpl);
  }
  
  public static Resource getResourceImpl(String resourceId)
  {
    return map.get(resourceId);
  }
 
  public static boolean exists(String resourceId)
  {
    return map.containsKey(resourceId);
  }
  
}
