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

/*
 * Used by MQLIstener to maintain a relationship between MQ Channels and resources/fixtures every time a queue consumer is
 * created.
 * 
 * All messsages received by RabbitMQListener are tagged with the channel tag (cTag).  This allows RabbitMQListener to
 * communicate with a fixture in the event of a connection failure and helps it manage the echo processing logic.
 *  
 */
public class CtagResourceMap
{
  private static CtagResourceMap cTagMap = null;
  private static ConcurrentHashMap<String,String> map = new ConcurrentHashMap<String,String>();;

  
  private CtagResourceMap()
  {
  }

  
  public synchronized static void initCtagMap() {
    if (cTagMap == null) {
      cTagMap = new CtagResourceMap();
    }
  }
  
  
  protected static void addCtag(String cTag, String resource) {
    map.put(cTag, resource);
  }
  
  
  protected static String getResource(String cTag) {
    return map.get(cTag);
  }
  
  
  protected static void removeCtag(String cTag) {
    map.remove(cTag);
  }
  
  
  // For unit tests only
  public static void reset() {
    map = new ConcurrentHashMap<String,String>();;
  }
}
