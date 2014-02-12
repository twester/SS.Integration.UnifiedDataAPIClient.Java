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

import java.util.MissingResourceException;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

/* Monitors for any new UOWs on WorkQueue and dispatches it to ResourceImpl instances for processing.
 * 
 * The WorkQueueMonitor picks up a UOW from WorkQueue, passes it to an instance of FixtureActionProcessor which retrieves 
 * the instance of ResourceImpl associated with that MQ Queue (via a lookup on ResourceWorkerMap).  It then executes the 
 * UOW within that ResourceImpl using one of the threads from this executor service's thread pool.  The UOW from MQ is 
 * wrapped up in a FixtureActionProcessor.  When the task in this thread completes the thread is returned to the threadpool 
 * by the JVM.
 *
 * An alternative design would be to have a theadpool of WorkQueue Listener objects (FixtureActionProcessor) called directly
 * by RabitMqConsumer, but I chose this design as a centralised control point and to minimise the amount of concerns leaked
 * between modules.
 * 
 */
public class WorkQueueMonitor implements Runnable
{
  private static Logger logger = Logger.getLogger(WorkQueueMonitor.class);
  private static WorkQueueMonitor monitor = null;
  private WorkQueue workQueue = WorkQueue.getWorkQueue();
  private static ReentrantLock workQueueLock = new ReentrantLock();
  private static final String THREAD_NAME = "Work_Queue_Thread";
  private static boolean terminate = false;
  
  private WorkQueueMonitor()
  {
  }

  
  
  public static WorkQueueMonitor getMonitor() {
    try {
      workQueueLock.lock();
      if (monitor == null) {
        monitor = new WorkQueueMonitor();
        ActionThreadExecutor.createActionThreadExecutor();
      }
    } catch (Exception ex) {
      logger.error("Could not initialiaze Work Queue Monitor.");
      throw new MissingResourceException("Service threadpool has become corrupted", "ss.udapi.sdk.services.ActionThreadExecutor", "WorkQueueMonitor");
    } finally {
      workQueueLock.unlock();
    }
    return monitor;
  }
  
  
  
  @Override
  public void run() {
    logger.info("Work queue Monitor initialized and waiting");
    Thread.currentThread().setName(THREAD_NAME);
    //Monitor the queue
    while(true) {
      //When a UOW comes along call FixtureActionProcessor to grab the associated ResourceImpl and process it.
      String task = workQueue.getTask();
      logger.debug("Queue Read: " + task.substring(0,40));
      try {
        FixtureActionProcessor processor = new FixtureActionProcessor(task);
        ActionThreadExecutor.executeTask(processor);
      } catch (Exception ex) {
        logger.error("Work queue monitor has been interrupted");
      }

      if (terminate == true) {
        return;
      }
    }
  }

  
  //for unit testing
  protected static void terminate() {
    terminate = true;
  }
  
}
