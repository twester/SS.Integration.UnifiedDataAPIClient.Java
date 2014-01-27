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

package ss.udapi.sdk.services;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class EchoResourceMap
{
  private static EchoResourceMap echoMap= null;
  private static ConcurrentHashMap<String,Integer> map = new ConcurrentHashMap<String,Integer>();
  

  private EchoResourceMap()
  {
  }

  
  public static EchoResourceMap getEchoMap(){
    if (echoMap == null) {
      echoMap = new EchoResourceMap();
    }
    return echoMap;
  }
  
  
  public void addResource(String resourceId) {
    synchronized(this) {
      map.put(resourceId, 0);
    }
  }
  
  
  public void removeResource(String resourceId)
  {
    synchronized(this) {
      map.remove(resourceId);
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
      Set<String> defaulters = new HashSet<String>();
      
      keys = map.keySet();
      Iterator<String> keyIter = keys.iterator();
      synchronized(this) {
        while(keyIter.hasNext()) {
          String key = keyIter.next();
          int count = (map.get(key));
          if (count == (retries)){
            defaulters.add(key);
          }
          map.put(key, count+1);
        }
      }
      return defaulters;
  }
  
  
}
