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


public class ResourceWorkQueue
{
  private static ResourceWorkQueue workQueue = null;
  private static ConcurrentHashMap<String,LinkedBlockingQueue<String>> map = new ConcurrentHashMap<String,LinkedBlockingQueue<String>>();
  
  
  private ResourceWorkQueue()
  {
  }
  
  
  public static LinkedBlockingQueue<String> addQueue(String resourceId) {
    map.put(resourceId, new LinkedBlockingQueue<String>());
    return map.get(resourceId);
  }
  
  
  public static ResourceWorkQueue getWorkQueue() {
    if (workQueue == null) {
      workQueue = new ResourceWorkQueue();
    }
    return workQueue;
  }
  
  
  public static void addUOW(String resourceId, String task) {
    LinkedBlockingQueue<String> queue = map.get(resourceId);
    queue.add(task);
  }
  
  
  public static LinkedBlockingQueue<String> getQueue(String resourceId) {
    return map.get(resourceId);
  }


  
  public static String removeUOW(String resourceId) {
    LinkedBlockingQueue<String> queue = map.get(resourceId);
    return queue.poll();
  }

  
  
  public static boolean exists(String resourceId) {
    return map.containsKey(resourceId);
  }
  
  
}
