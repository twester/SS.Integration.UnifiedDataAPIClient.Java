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

import ss.udapi.sdk.interfaces.Resource;

import java.util.concurrent.ConcurrentHashMap;

/* Map holding individual instances of ResourceImpl.  There is one instance for each resource/fixture.
 * 
 * The WorkQueueMonitor picks up a UOW from WorkQueue, passes it to an instance of FixtureActionProcessor which retrieves 
 * the instance of ResourceImpl associated with that MQ Queue (via a lookup on ResourceWorkerMap).  It then executes the 
 * UOW within that ResourceImpl using one of the threads from this executor service's thread pool.  The UOW from MQ is 
 * wrapped up in a FixtureActionProcessor.  When the task in this thread completes the thread is returned to the threadpool 
 * by the JVM.
 * 
 * Having a separate work queue for the resource instead of calling streamdata() directly means we can react to multiple
 * (almost) simultaneous updates without waiting for streamdata() to complete, thereby reducing any thread blocks.
 */
public class ResourceWorkerMap {
	private static ResourceWorkerMap workerMap = null;
	private static ConcurrentHashMap<String, Resource> map = new ConcurrentHashMap<String, Resource>();

	private ResourceWorkerMap() {
	}

	public static void initWorkerMap() {
		if (workerMap == null) {
			workerMap = new ResourceWorkerMap();
		}
	}

	public static void addResource(String resourceId, Resource resourceImpl) {
		map.put(resourceId, resourceImpl);
	}

	public static Resource getResourceImpl(String resourceId) {
		return map.get(resourceId);
	}

	public static Resource removeResource(String resourceId) {
		return map.remove(resourceId);
	}

	public static boolean exists(String resourceId) {
		return map.containsKey(resourceId);
	}

	// For unit tests only
	public static void reset() {
		map = new ConcurrentHashMap<String, Resource>();
	}

}
