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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;


public class EchoResourceMap
{
  private static Logger logger = Logger.getLogger(EchoResourceMap.class);
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
    map.put(resourceId, 0);
  }
  
  
  public void removeResource(String resourceId)
  {
    map.remove(resourceId);
  }
  
  
  public void incrEchoCount(String resourceId)
  {
    map.replace(resourceId, map.get(resourceId)+1);
  }

  
  public void decrEchoCount(String resourceId)
  {
    map.replace(resourceId, map.get(resourceId)-1);
    logger.info("Echo received for fixture Id: " + resourceId + ". Current missed echos: " + map.get(resourceId));
  }


  /*Not overly tidy, but in order to avoid looping twice through this set (once here and once in EchoSender we prepare the
   * list of defaulters here.  Could be moved to EchoSender and it would still work, just means iterating through this set
   * twice
   */
  public Set<String> incrAll(int retries)
  {
    Set<String> keys;
    Set<String> defaulters = new HashSet<String>();
    
    keys = map.keySet();
    Iterator<String> keyIter = keys.iterator();

    while(keyIter.hasNext()) {
      String resourceId = keyIter.next();
      int count = (map.get(resourceId));
      if (count == (retries)){
        defaulters.add(resourceId);
      }
      map.replace(resourceId, count+1);

      System.out.println("--------------->echo count increased for: " + resourceId + ". Current count " + (map.get(resourceId)));
    }

    return defaulters;
  }
  
  
}
