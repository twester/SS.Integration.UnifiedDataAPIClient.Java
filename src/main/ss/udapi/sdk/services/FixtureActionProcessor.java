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

import org.apache.log4j.Logger;

import ss.udapi.sdk.ResourceImpl;

public class FixtureActionProcessor implements Runnable
{
  private static Logger logger = Logger.getLogger(FixtureActionProcessor.class);
  private String task;
  
  public FixtureActionProcessor(String task) {
    this.task = task;
  }
  
  
  @Override
  public void run()
  {
    /*TODO the jsonObject produced here gets corrupted, it cannot be processed if the string is too big as
     *is the case with feature suspension, this is gson it works when fromGson() is called to map an Object
     *    JsonObject jsonObject = new JsonParser().parse(task).getAsJsonObject();
     *even this fails
     *    jsonObject.get("Content").getAsString(){
     *with:
     *    java.lang.UnsupportedOperationException: JsonObject
     *    at com.google.gson.JsonElement.getAsString(JsonElement.java:185)
     *    at ss.udapi.sdk.services.FixtureActionProcessor.run(Unknown Source)
     *    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
     *    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
     *    at java.lang.Thread.run(Thread.java:744)
     *so instead I have to get the string header and parse it manually, that's faster anyway  */
    
    String msgHead = task.substring(0, 200);
    int idStart = msgHead.indexOf("Id\":")+5;
    String fixtureId = msgHead.substring(idStart,idStart+27);
    logger.debug("Processing started for fixture/resource: " + fixtureId);
  
    System.out.println("----------------->For Echo testing: " + msgHead);
    
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(fixtureId);

    ResourceWorkQueue.addUOW(fixtureId, task);
      
//    resource.addTask(task);
    resource.streamData();
    
  }

}
