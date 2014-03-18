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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import org.apache.log4j.Logger;

/* Threadpool for the main services used by the SDK */
public class ServiceThreadExecutor {
	private static Executor exec;
	private static Logger logger = Logger
			.getLogger(ServiceThreadExecutor.class);
	private static ConcurrentHashMap<String, FutureTask<String>> map = new ConcurrentHashMap<String, FutureTask<String>>();

	/*
	 * Yes 3 is a magic number (as the song says). But there cannot be more than
	 * three threads running, we do not start any more: 1) MQListener 2)
	 * EchoSender 3) WorkQueueMonitor
	 * 
	 * Any additional threads are not ours and shouldn't be using our pool. As
	 * these objects are all singletons there cannot be more than three in
	 * total. So if the thread pool throws
	 * java.util.concurrent.RejectedExecutionException something bad *has*
	 * happened and needs to be investigated not covered up as would be the case
	 * if we had more threads.
	 */
	private static final int MAX_SERVICE_THREADS = 3;

	public static void createExecutor() {
		logger.debug("Instantiated Service Thread Executor");
		exec = Executors.newFixedThreadPool(MAX_SERVICE_THREADS);
	}

	public static void executeTask(Runnable task) {
		String taskName = task.toString().substring(0,
				task.toString().indexOf('@'));
		synchronized (ServiceThreadExecutor.class) {
			if (map.containsKey(taskName) == false) {
				FutureTask<String> futureTask = new FutureTask<String>(task,
						taskName);
				map.put(taskName, futureTask);
				exec.execute(futureTask);
				logger.debug("Instantiating initial ServiceThreadExecutor thread for: "
						+ taskName + ".");
			} else if (map.get(taskName).isDone() == true) {
				FutureTask<String> futureTask = new FutureTask<String>(task,
						taskName);
				map.put(taskName, futureTask);
				exec.execute(futureTask);
				logger.debug("Instantiating new ServiceThreadExecutor thread for: "
						+ taskName + ".");
			}
		}
	}
}
