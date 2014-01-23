package ss.udapi.sdk.services;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import ss.udapi.sdk.interfaces.Resource;

public class ResourceEchoMap
{
  private static ResourceEchoMap echoMap= null;
  private static ConcurrentHashMap<String,Integer> map = new ConcurrentHashMap<String,Integer>();
  
  private static Logger logger = Logger.getLogger(ResourceEchoMap.class);
  
  private ResourceEchoMap()
  {
  }
  
  public static ResourceEchoMap getEchoMap(){
    if (echoMap == null) {
      echoMap = new ResourceEchoMap();
    }
    return echoMap;
  }
  
  public void addResource(String resourceId)
  {
    synchronized(this) {
      map.put(resourceId, 0);
    }
  }
  
  public void incrEchoCount(String resourceId)
  {
    synchronized(this)
    {
      map.replace(resourceId, map.get(resourceId)+1);
    }
  }
  
  public void decrEchoCount(String resourceId)
  {
    synchronized(this)
    {
      map.replace(resourceId, map.get(resourceId)-1);
    }
  }
  
  public Set<String> incrAll(int retries)
  {
    Set<String> keys;
    Set<String> defaulters = new HashSet();
    
    synchronized(this)
    {
      keys = map.keySet();
      Iterator<String> keyIter = keys.iterator();

      while(keyIter.hasNext()) {
        String key = keyIter.next();
        Integer count = map.get(key);

        if (count == 3){
          defaulters.add(key);
        }
          
        map.replace(key, count+1);
      }
    }
    
    return defaulters;
    
  }
  
  
}
