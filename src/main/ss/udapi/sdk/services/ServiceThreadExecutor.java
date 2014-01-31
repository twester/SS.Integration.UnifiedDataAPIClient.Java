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

/* Threadpool for the main services used by the SDK */
public class ServiceThreadExecutor
{
  private static Executor exec;

  /* Yes 3 is a magic number (as the song says).  But there cannot be more than three threads running, we do not start any more:
   *    1) MQListener
   *    2) EchoSender
   *    3) WorkQueueMonitor
   * 
   * Any additional threads are not ours and shouldn't be using our pool.  As this objects are all singletons there cannot be
   * more than three in total.  So if the thread pool throws java.util.concurrent.RejectedExecutionException
   * then something bad *has* happened and needs to be investigated not covered up as would be the case if we had more threads. 
   */

  public static void createExecutor() {
    exec = Executors.newFixedThreadPool(3);
  }
  
  public static void executeTask(Runnable task) {
    exec.execute(task);
  }
  
  
}
