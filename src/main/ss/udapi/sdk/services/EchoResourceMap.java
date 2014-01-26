package ss.udapi.sdk.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


public class EchoResourceMap
{
  private static EchoResourceMap echoMap= null;
  private static ConcurrentHashMap<String,Integer> map = new ConcurrentHashMap<String,Integer>();
  
  private static Logger logger = Logger.getLogger(EchoResourceMap.class);
  
  private EchoResourceMap()
  {
  }
  
  public static EchoResourceMap getEchoMap(){
    if (echoMap == null) {
      echoMap = new EchoResourceMap();
    }
    return echoMap;
  }
  
  public void addResource(String resourceId)
  {
    synchronized(this) {
      if (map.containsKey(resourceId) == false){
        map.put(resourceId, 0);
      }
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
      map.put(resourceId, map.get(resourceId)-1);
     }
  }
  
  public Set<String> incrAll(int retries)
  {
      Set<String> keys;
      Set<String> defaulters = new HashSet<String>();
      
      keys = map.keySet();
      Iterator<String> keyIter = keys.iterator();

      while(keyIter.hasNext()) {
        String key = keyIter.next();
        int count = (map.get(key));
        if (count == (retries)){
          defaulters.add(key);
        }
        map.put(key, count+1);
      }
      return defaulters;
    
    
  }
  
  
}
