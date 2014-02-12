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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

/* Each Fixture is assigned a ResourceImpl.  Each ResourceImpl has an associated ResourceWorkQueue onto which
 * a running instance of FixtureActionProcessor can drop a UOW for that resource.
 * ResourceImpl then reads from it's queue and executes the appropriate actions.
 * 
 * A separate map is used instead of directly inserting the task into ResourceImpl to minimise the amount of
 * methods exposed to the public API.
 */
public class ResourceWorkQueue
{
  private static ResourceWorkQueue workQueue = null;
  private static ConcurrentHashMap<String,LinkedBlockingQueue<String>> map;
  private static Logger logger = Logger.getLogger(ResourceWorkQueue.class);
  
  private ResourceWorkQueue()
  {
  }
  

  public synchronized static ResourceWorkQueue getResourceWorkQueue() {
    if (workQueue == null) {
      workQueue = new ResourceWorkQueue();
      map = new ConcurrentHashMap<String,LinkedBlockingQueue<String>>();
    }
    return workQueue;
  }
  
  
  //A new fixture has been registered which created a new instance of ResourceImpl which in turn creates a new queue here 
  public static LinkedBlockingQueue<String> addQueue(String resourceId) {
    map.put(resourceId, new LinkedBlockingQueue<String>());
    return map.get(resourceId);
  }
  

  //The fixture is no longer active, has been deleted and we're cleaning as the associated ResourceImpl gets removed.
  public static void removeQueue(String resourceId) {
    map.remove(resourceId);
  }
  

  //Add a new UOW for the associated resource/fixture.  Currently FixtureActionProcessor does this. 
  public void addUOW(String resourceId, String task) {
      logger.debug("Added echo alert" + task.substring(0,10) + " for [" + resourceId + "]");
      LinkedBlockingQueue<String> queue = map.get(resourceId);
      queue.add(task);
  }
  
  
  //ResourceImpl pulls the UOW to work on it.
  public String removeUOW(String resourceId) {
      LinkedBlockingQueue<String> queue = map.get(resourceId);
      return queue.poll();
  }

  
  
  public static LinkedBlockingQueue<String> getQueue(String resourceId) {
    return map.get(resourceId);
  }

  
  public static int size(String resourceId) {
    return map.get(resourceId).size();
  }

  
  public static boolean isEmpty(String resourceId) {
    return map.get(resourceId).isEmpty();
  }
  
  
  public static boolean exists(String resourceId) {
    return map.containsKey(resourceId);
  }


  // For unit tests only
  protected static void reset() {
    workQueue = null;
  }

}
