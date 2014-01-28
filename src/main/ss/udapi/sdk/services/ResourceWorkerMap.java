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

import ss.udapi.sdk.interfaces.Resource;

import java.util.concurrent.ConcurrentHashMap;


public class ResourceWorkerMap
{
  private static ResourceWorkerMap workerMap= null;
  private static ConcurrentHashMap<String,Resource> map = new ConcurrentHashMap<String,Resource>();
  
  
  private ResourceWorkerMap()
  {
  }
  
  
  public static ResourceWorkerMap getWorkerMap() {
    if (workerMap == null) {
      workerMap = new ResourceWorkerMap();
    }
    return workerMap;
  }
  
  
  public static void addUOW(String resourceId, Resource resourceImpl) {
    map.put(resourceId, resourceImpl);
  }
  
  
  public static Resource getResourceImpl(String resourceId) {
    return map.get(resourceId);
  }
 
  
  public static boolean exists(String resourceId) {
    return map.containsKey(resourceId);
  }
  
}
