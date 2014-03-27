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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.rabbitmq.client.*;

/* Initiates connectivity to RabbitMQ and maintains corresponding queue listeners as new fixtures/resources are
 * created(added)/destroyed(deleted).
 */
public class MQListener implements Runnable {
	
	private static Logger logger = Logger.getLogger(MQListener.class);

	// Maps a resource to a specific cTag (which is in effect a queue listener).
	private static ConcurrentHashMap<String, String> resourceChannMap = new ConcurrentHashMap<String, String>();
	private static MQListener instance = null;
	private static Channel channel;
	private static Connection connection;
	private static RabbitMqConsumer consumer;
	private static ConcurrentLinkedQueue<ResourceSession> resourceSessionList = new ConcurrentLinkedQueue<ResourceSession>();
	private static ReentrantLock mqListenerLock = new ReentrantLock();

	private URI resourceQURI = null;

	private static final String THREAD_NAME = "MQ_Listener_Thread";
	private static final int CONNECT_RETRIES = 5;

	private static boolean terminate = false;

	private MQListener(URI destination) {
		
		if(destination == null)
			throw new IllegalArgumentException("destination URI cannot be null");
		
		resourceQURI = destination;
	}

	public synchronized static MQListener getMQListener(String amqpDest) {
		/*
		 * This lock ensures there cannot be multiple instantiations which can
		 * lead to a corrupt object without synchronisation, which in turn
		 * cannot be done on a here as the access is static.
		 */
		try {
			mqListenerLock.lock();
			logger.debug("Retrieving MQListener or create it if it doesn't exist");
			if (instance == null) {
				instance = new MQListener(new URI(amqpDest));
			}
		} catch (Exception ex) {
			logger.error("Could not initialiaze MQ Listener.");
			throw new MissingResourceException(
					"Service threadpool has become corrupted",
					"ss.udapi.sdk.services.ActionThreadExecutor", "MQListener");
		} finally {
			mqListenerLock.unlock();
		}
		return instance;
	}
	
	private synchronized boolean isConnectionOpen() {
		return (connection != null && connection.isOpen() && channel != null && channel.isOpen());
	}

	public synchronized void assureConnectionIsOpen() throws Exception {

		if (isConnectionOpen())
			return;

		if (channel == null) {
			logger.debug("MQ Channel not created");
		} else {
			logger.debug("MQ Channel is open: " + channel.isOpen());
		}

		
		try {
			
			logger.debug("Opening MQ channel");

			// try {
			String path = resourceQURI.getRawPath();
			String host = resourceQURI.getHost();
			String userInfo = resourceQURI.getRawUserInfo();
			String virtualHost = uriDecode(path.substring(1, path.indexOf('/', 1)));
			int port = resourceQURI.getPort();

			// Set up the connection.
			ConnectionFactory connectionFactory = new ConnectionFactory();
			connectionFactory.setRequestedHeartbeat(Integer.parseInt(SystemProperties.get("ss.conn_heartbeat")));
			connectionFactory.setHost(host);
			connectionFactory.setVirtualHost("/" + virtualHost);

			userInfo = URLDecoder.decode(userInfo, "UTF-8");
			if (userInfo != null) {
				String userPass[] = userInfo.split(":");

				if (userPass.length > 2) {
					throw new Exception(
							"Invalid user details format in AMQP URI: "
									+ userInfo);
				}

				connectionFactory.setUsername(uriDecode(userPass[0]));
				if (userPass.length == 2) {
					connectionFactory.setPassword(uriDecode(userPass[1]));
				}
			}

			if (port != -1) {
				connectionFactory.setPort(port);
			}

			// Start up the connection
			connection = connectionFactory.newConnection();
			
			
			/*
			 * And create a consumer using the first queue. This consumer allows
			 * subsequent queue listeners to be added and removed as resources
			 * are created / deleted.
			 */
			boolean connectSuccess = false;
			for (int retries = 1; retries <= CONNECT_RETRIES; retries++) {

				logger.info("Attempting new connection to MQ...");

				try {

					channel = connection.createChannel();
					channel.basicQos(0, 10, false);
					consumer = new RabbitMqConsumer(channel);
					
					// Create a queue listener for the first fixture.
					connectSuccess = true;
					break;

				} catch (IOException ex) {
				}
			}

			if (connectSuccess == false) {
				throw new IOException("Failure creating channel after "
						+ CONNECT_RETRIES + " attempts");
			}
			
			logger.info("Connection made to MQ");

		} catch (UnsupportedEncodingException e) {
			logger.error("Unsupported encoding while opening MQ connection: " + e);
			throw e;
		} catch (IOException e) {
			logger.error("IO error while opening MQ connection: " + e);
			throw e;
		} catch (Exception e) {
			logger.error("Generic error while opening MQ connection: " + e);
			throw e;
		}

	}
	
	@Override
	public void run() {

		terminate = false;
		
		/*
		 * This section is the loop which uses the connection opened above and
		 * adds additional consumers as they are requested. The two maps are
		 * also updated here. This loop constantly monitors resourceSessionList
		 * for any new pending additions to the number of active queue
		 * listeners. Could have used an observer to add more listeners, but
		 * that happens very infrequently compared to the lifetime of the
		 * running program, this is simpler for the return you get.
		 */
		Thread.currentThread().setName(THREAD_NAME);
		logger.info(THREAD_NAME + " executing");
		
		while (isConnectionOpen()) {
			
			while (resourceSessionList.isEmpty() == false && terminate == false) {
				
				ResourceSession session = resourceSessionList.peek();
				
				try {
				
					String resourceId = session.getResourceId();

					if (resourceChannMap.containsKey(resourceId) == false) {

						String path = new URI(session.getAmqpDest()).getRawPath();
						String queue = path.substring(path.indexOf('/', 1) + 1);
						
						String ctag = channel.basicConsume(queue, true, consumer);

						if (ctag != null) {
							
							resourceChannMap.put(resourceId, ctag);
							CtagResourceMap.addCtag(ctag, resourceId);
							EchoResourceMap.getEchoMap().addResource(resourceId);
							logger.info("Basic consumer " + ctag
									+ " added for queue " + queue
									+ " for resource " + resourceId);
							
							resourceSessionList.remove(session);
						}
						
					} else {
						resourceSessionList.remove(session);
					}
					
					if (terminate == true) {
						return;
					}
					
				} catch (AlreadyClosedException  e) {
					logger.error("The amqp channel is closed: " + e.getMessage());
				} catch (IOException e) {
					logger.error("Failure creating additional basic consumer for : "
							+ session.getResourceId() + ": " + e);
				} catch (URISyntaxException e) {
					logger.error("Queue name corrupted: " + session.getAmqpDest());
				} catch (Exception e) {
					logger.error("Generic error occured: " + e);					
				}
				
			}
			
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
		}
		
		logger.info(THREAD_NAME + " terminated");
	}

	/*
	 * When we get a disconnect event, either called by the client code or when
	 * the maximum of missing echo responses is reached we close this channel.
	 * This causes a handleCancel event which MQListener receives which in turn
	 * calls the associated ResourceImpl to notify the client code about the
	 * disconnect event.
	 */
	public static void disconnect(String resourceId) {
		String consumerTag = resourceChannMap.get(resourceId);
		if (consumerTag == null) {
			logger.debug("Basic consumer for resource " + resourceId
					+ " has already disconnected.");
		} else {
			try {
				channel.basicCancel(consumerTag);
				logger.info("Disconnected basic consumer " + consumerTag
						+ " for resource " + resourceId);
			} catch (Exception ex) {
				logger.debug("Could not disconnect basic consumer "
						+ consumerTag + " for resource " + resourceId);
			}
		}
	}

	/*
	 * After the disconnect event notification is sent to the client MQListener
	 * calls this to remove the resource/cTag mappings. Bit of housekeeping
	 * really.
	 */
	protected static void removeMapping(String cTag) {
		logger.debug("cTag " + cTag + " no longer valid.");
		EchoResourceMap.getEchoMap().removeResource(CtagResourceMap.getResource(cTag));
		resourceChannMap.remove(CtagResourceMap.getResource(cTag));
		CtagResourceMap.removeCtag(cTag);
	}

	/*
	 * Adds a new request to initiate a queue listener for a newly created
	 * fixture/resource. The loop in run() above will pick this up and create the
	 * listener there.
	 */
	public static void setResources(ResourceSession resourceSession) {
		logger.debug("Adding new resource queue listener request for: "
				+ resourceSession.getAmqpDest());
		resourceSessionList.add(resourceSession);
	}

	// Hook for unit testing
	protected int countPendingResources() {
		return resourceSessionList.size();
	}

	// Clean up the path.
	private static String uriDecode(String s) {
		try {
			// URLDecode decodes '+' to a space, as for form encoding. So
			// protect plus signs.
			return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
		} catch (java.io.UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	// for unit testing
	public static void terminate() {
		terminate = true;
	}

}
