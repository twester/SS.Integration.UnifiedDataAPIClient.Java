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

import ss.udapi.sdk.streaming.Event;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class ResourceEventsMap
{
  private static ResourceEventsMap eventsMap= null;
  private static ConcurrentHashMap<String,List<Event>> map = new ConcurrentHashMap<String,List<Event>>();
  

  private ResourceEventsMap()
  {
  }

  
  
  public static ResourceEventsMap getEventMap() {
    if (eventsMap == null) {
      eventsMap = new ResourceEventsMap();
    }
    return eventsMap;
  }
  

  
  public void addEvents(String resourceId, List<Event> events) {
    synchronized(this) {
      map.put(resourceId, events);
    }
  }
  

  
  public List<Event> getEvents(String resourceId) {
    synchronized(this) {
      return map.get(resourceId);
    }
  }
  
  
  
  public void removeResource(String resourceId) {
    synchronized(this) {
      map.remove(resourceId);
    }
  }


}
