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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

/* All activity received from the MQ system is simply pulled of the MQ queues and put into this work queue.  By RabbitMQ
 * consumer to minimize the amount of processing that thread does.
 * 
 * The WorkQueueMonitor picks up a UOW from WorkQueue, passes it to an instance of FixtureActionProcessor which retrieves 
 * the instance of ResourceImpl associated with that MQ Queue (via a lookup on ResourceWorkerMap).  It then executes the 
 * UOW within that ResourceImpl using one of the threads from this executor service's thread pool.  The UOW from MQ is 
 * wrapped up in a FixtureActionProcessor.  When the task in this thread completes the thread is returned to the threadpool 
 * by the JVM.
 */
public class WorkQueue
{
  private static WorkQueue workQueue = null;
  private static LinkedBlockingQueue<String> linkedQueue = new LinkedBlockingQueue<String>();
  
  private static Logger logger = Logger.getLogger(WorkQueue.class);
  
  private WorkQueue()
  {
  }

  
  public static WorkQueue getWorkQueue() {
    if (workQueue == null)
    {
      workQueue = new WorkQueue();
    }
    return workQueue;
  }
  
  
  
  //RabbitMQ consumer drops messages retrieved from MQ into this WorkQueue
  public void addTask(String task) {
    try {
      linkedQueue.put(task);
    } catch (Exception ex) {
      logger.error("WorkQueue management interrupted", ex);
    }
  }
  
  
  //WorkQueueMonitorpulls messages retrieved this WorkQueue and initiates processing.
  public String getTask() {
    String task=null;
    try {
      //this is a blocking method so if there's nothing to pickup from the queue the calling thread sits in a blocked state not
      //usign any resources.
      task = linkedQueue.take();
    } catch (Exception ex) {
      logger.error("WorkQueue management interrupted", ex);
    }
    return task;
  }
  
}
