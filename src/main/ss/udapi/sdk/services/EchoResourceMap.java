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

/*
 * Used by EchoSender to manage the count of echo failures for each resource/fixture.
 * 
 * Every time EchoSender sends an BatchEcho request the count is increased for all fixtures.
 * 
 * Every time RabbitMqConsumer receives an echo response for a queue it looks up the associated resource/fixture
 * using CtagResourceMap and resets the count to 0.  The count is also reset to 0 when a message is retrieved from the queue
 * as the message arrival would only happen if the queue was OK.
 * 
 */
public class EchoResourceMap
{
  private static Logger logger = Logger.getLogger(EchoResourceMap.class);
  private static EchoResourceMap echoMap;
  private static ConcurrentHashMap<String,Integer> map = new ConcurrentHashMap<String,Integer>();
  

  private EchoResourceMap()
  {
  }

  
  public synchronized static EchoResourceMap getEchoMap(){
    if (echoMap == null) {
      echoMap = new EchoResourceMap();
    }
    return echoMap;
  }
  
  
  /*
   * We are going to start keeping a count for this resource.
   */
  public void addResource(String resourceId) {
    if (resourceId != null) {
      logger.debug("Monitoring echos for: " + resourceId);
      map.put(resourceId, 0);
    }
  }
  
  
  /*
   * This resource is no longer required, so we won't keep the counter.
   */
  public void removeResource(String resourceId)
  {
    if (resourceId != null) {
      logger.debug("No longer monitoring echos for: " + resourceId);
      map.remove(resourceId);
    }
  }
  
  
  
  /*
   * Not currently used, but seemed sensible to include such a method
   */
  protected void incrEchoCount(String resourceId)
  {
    if (resourceId != null) {
      map.replace(resourceId, map.get(resourceId)+1);
    }
  }

  
  /*
   * We got some activity for this resource's queue so the queue must be OK.
   */
  protected void resetEchoCount(String resourceId)
  {
    if (resourceId != null) {
      logger.info("Echo or message received for fixture Id: " + resourceId + ". Current missed echos: " + map.get(resourceId));
      map.replace(resourceId, 0);
    }
  }



  /* Not overly tidy, but in order to avoid looping twice through this set (once here and once in EchoSender we prepare the
   * list of defaulters here.  Could be moved to EchoSender and it would still work, just means iterating through this set
   * twice.
   * 
   * Defaulters is a list of all resources that have reached the maximum number of echo response failures. 
   */
  protected Set<String> incrAll(int retries)
  {
    Set<String> keys;
    Set<String> defaulters = new HashSet<String>();
    
    keys = map.keySet();
    Iterator<String> keyIter = keys.iterator();

    while(keyIter.hasNext()) {
      String resourceId = keyIter.next();
      int count = (map.get(resourceId));
      if (count == (retries)){
        logger.info(resourceId + " added to defaulters with");
        defaulters.add(resourceId);
      }
      map.replace(resourceId, count+1);
      logger.debug("Echo count increased for: " + resourceId + ". Current count " + (map.get(resourceId)));
    }
    return defaulters;
  }
  
  
  //Only used for unit tests
  protected Integer getEchoCount(String resourceId)
  {
    return map.get(resourceId);
  }
  
  
  // For unit tests only
  public static void reset() {
    map = new ConcurrentHashMap<String,Integer>();
  }
  
}
