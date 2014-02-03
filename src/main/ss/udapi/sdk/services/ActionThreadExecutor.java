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

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/*
 * Activity tasks received from the Sporting Solutions systems via the MQ System (RabbitMQ) are placed on an instance of 
 * WorkQueue.
 * The WorkQueueMonitor picks up a taskfrom MQ, assigns to the ResourceImpl associated with that MQ Queue (which corresponds
 * to a fixture and executes it using one of the threads from this executor service's thread pool.  The task from MQ is 
 * wrapped up in a FixtureActionProcessor.  When the task completes the thread is returned to the threadpool by the JVM.

 * The number of threads allocated is configured in: conf/sdk.properties using "ss.workerThreads"
 */
public class ActionThreadExecutor
{
  private static  Executor exec;
  private static ActionThreadExecutor instance = null;
  
  ActionThreadExecutor()
  {
    synchronized(this)
    {
      int workerThreads = Integer.parseInt(SystemProperties.get("ss.workerThreads"));
      exec = Executors.newFixedThreadPool(workerThreads);
    }
  }

  /*
   * Create a instance.
   */
  protected static ActionThreadExecutor createActionThreadExecutor()
  {
    if (instance ==null) {
      new ActionThreadExecutor();
    }
    return instance;
  }
  

  /*
   * Assign a task to this threadpool
   */
  protected static void executeTask(Runnable task)
  {
    exec.execute(task);
  }
  
}
