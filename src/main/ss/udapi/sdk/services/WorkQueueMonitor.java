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

import org.apache.log4j.Logger;

public class WorkQueueMonitor implements Runnable
{
  private static Logger logger = Logger.getLogger(WorkQueueMonitor.class);
  private static WorkQueueMonitor monitor = null;
  private WorkQueue workQueue = WorkQueue.getWorkQueue();
  
  private WorkQueueMonitor()
  {
  }

  
  public static WorkQueueMonitor getMonitor() {
    if (monitor == null) {
      monitor = new WorkQueueMonitor();
      ActionThreadExecutor.createExecutor();
    }
    return monitor;
  }
  
  
  
  @Override
  public void run() {
    logger.info("Work queue Monitor initialized and waiting");
    while(true) {
      String task = workQueue.getTask();
      logger.debug("---------------->Queue Read: " + task.substring(0,40));
      try {
        FixtureActionProcessor processor = new FixtureActionProcessor(task);
        ActionThreadExecutor.executeTask(processor);
      } catch (Exception ex) {
        logger.error("Work queue monitor has been interrupted");
      }
    }
  }

  
}
