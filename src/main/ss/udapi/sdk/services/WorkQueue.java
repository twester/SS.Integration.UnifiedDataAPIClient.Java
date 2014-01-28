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

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

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
  
  
  public void addTask(String task) {
    try {
      linkedQueue.put(task);
    } catch (Exception ex) {
      logger.error("WorkQueue management interrupted", ex);
    }
  }
  
  
  
  public String getTask() {
    String task=null;
    try {
      task = linkedQueue.take();
    } catch (Exception ex) {
      logger.error("WorkQueue management interrupted", ex);
    }
    return task;
  }
  
}
