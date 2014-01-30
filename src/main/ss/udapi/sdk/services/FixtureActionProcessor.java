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

/* Wraps up a UOW received from the Sporting Solutions systems via the MQ System (RabbitMQ) as runnable.
 * 
 * The WorkQueueMonitor picks up a UOW from MQ, assigns to the ResourceImpl associated with that MQ Queue (which corresponds
 * to a fixture and executes it using one of the threads from this executor service's thread pool.  The UOW from MQ is 
 * wrapped up in a FixtureActionProcessor.  When the task in this thread completes the thread is returned to the threadpool 
 * by the JVM.
 */
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
    /* We could parse the message to pick up the ID, but in some cases the messages can be fairly sizeable whcih would
     * slow things down and if large enough can lead to an internal GSON exception: java.lang.UnsupportedOperationException: JsonObject
     * All we care about is the Id number which can be found by looking at the message directly.
     */
    String msgHead = task.substring(0, 200);
    int idStart = msgHead.indexOf("Id\":")+5;
    String fixtureId = msgHead.substring(idStart,idStart+27);
    logger.debug("Processing started for fixture/resource: " + fixtureId);
  
    System.out.println("----------------->For Echo testing: " + msgHead);

    //Now that we know what fixture the work is for  put the UOW in that fixtrues work queue.
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(fixtureId);
    ResourceWorkQueue.addUOW(fixtureId, task);

    //And run it.
    resource.streamData();
    
  }

}
