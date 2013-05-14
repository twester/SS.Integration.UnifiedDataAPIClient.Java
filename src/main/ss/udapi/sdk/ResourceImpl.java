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

package ss.udapi.sdk;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import ss.udapi.sdk.interfaces.*;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.RestLink;
import ss.udapi.sdk.model.Summary;
import ss.udapi.sdk.streaming.*;
import ss.udapi.sdk.streaming.FeedStatusAction.FeedStatus;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public class ResourceImpl extends Endpoint implements EchoHandler, Resource {

	private static Logger logger = Logger.getLogger(ResourceImpl.class.getName());

	// Config values:
	private final int MAX_RETRIES = 1;

	private boolean isStreaming;
	private List<Event> streamingEvents;
	
	private ConnectionFactory connectionFactory = new ConnectionFactory();
	private QueueingConsumer consumer;
	private Channel channel;
	private Connection connection;
	
	private String queueName;
	private String virtualHost;
	private int echoSenderInterval;
	private int echoMaxDelay;
	private String lastRecievedEchoGuid;
	
	private AtomicBoolean isReconnecting = new AtomicBoolean();
	
	private ExecutorService actionExecuter = Executors.newSingleThreadExecutor();
	
	//private final Object monitor = new Object();
	//private volatile boolean pause = false;
	
	private Timer echoTimer;
	private Echo echoHandler = null;
	private Thread streamThread;
	private FeedStatusAction feedStatusAction;
	
	ResourceImpl(Map<String,String> headers, RestItem restItem)
	{
		super(headers,restItem);
		logger.debug(String.format("Instantiated Resource %1$s", restItem.getName()));
	}
	
	public String getId() {
		return state.getContent().getId();
	}

	public String getName() {
		return state.getName();
	}

	public Summary getContent() {
		return state.getContent();
	}

	public String getSnapshot() {
		logger.info(String.format("Get Snapshot for %1$s", getName()));
		return FindRelationAndFollowAsString("http://api.sportingsolutions.com/rels/snapshot");
	}
	//--------------------------------------------------------------------------------------------
	public void startStreaming(List<Event> events){
		startStreaming(events,10000,3000);
	}
	
	public void startStreaming(List<Event> events, Integer echoInterval, Integer echoMaxDelay){
		logger.info(String.format("Starting stream for %1$s with Echo Interval of %2$s and Echo Max Delay of %3$s",getName(),echoInterval,echoMaxDelay));
		this.streamingEvents = events;
		feedStatusAction = new FeedStatusAction(streamingEvents);

		this.echoSenderInterval = 10000;
		this.echoMaxDelay = 3000;
		
		Runnable runnable = new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					logger.info("Streaming for " + getName() + " has STARTED");
					streamData();
				} catch (Exception ex)
				{
					logger.error(String.format("There has been a serious error streaming %1$s . The stream cannot continue.", getName()), ex);
				}
				finally
				{
					logger.info("Streaming for " + getName() + " has STOPPED");
					feedDown();
				}
			}
		};
		
		streamThread = new Thread(runnable);
		streamThread.start();
	}
	//--------------------------------------------------------------------------------------------
	private void streamData() throws InterruptedException
	{
		StreamAction streamAction = new StreamAction(streamingEvents);
		reconnect();
				
		while(isStreaming)
		{		
			/*synchronized(monitor){
				while(pause==true){
					try{
						monitor.wait();
					}catch(InterruptedException iex){
						logger.debug("WAIT INTERRUPTED");
						return;
					}
				}
			}*/
			
			try{
				Delivery output = consumer.nextDelivery();
				if(output != null)
				{
					feedUp();
					byte[] message = output.getBody();
				
					String messageString = new String(message);
					
					JsonObject jsonObject = new JsonParser().parse(messageString).getAsJsonObject();
					if(jsonObject.get("Relation").getAsString().equals("http://api.sportingsolutions.com/rels/stream/echo"))
					{
						String[] split = jsonObject.get("Content").getAsString().split(";");
						lastRecievedEchoGuid = split[0];
						
						logger.debug("Received Echo guid: " + lastRecievedEchoGuid);
						
						if (echoHandler != null)
						{
							echoHandler.gotEcho(lastRecievedEchoGuid);
						}
						else
						{
							logger.error("Had no echo handler");
						}
						//resetEcho();
					}
					else
					{
						try 
						{
							//resetEcho();
							streamAction.addMsg(messageString);
							actionExecuter.execute(streamAction);
						} catch (Exception e) {
							logger.warn("Error on message receive", e);
						}	
					}
				}
			}catch (InterruptedException ie)
			{
				logger.debug("Streaming thread interrupted, stopping");
			}
			catch(Exception ex)
			{
				logger.warn(String.format("Error on stream " + getName()), ex);
				stopStreaming();
			}
		}
	}
	private void reconnect() throws InterruptedException
	{
		if (isReconnecting.get())
		{
			logger.error("Reconnect but already reconnecting ...");
		}

		isStreaming = true;
		isReconnecting.set(true);
		int failures = 0;

		while (isStreaming && isReconnecting.get())
		{
			try
			{
				List<RestItem> restItems = FindRelationAndFollow("http://api.sportingsolutions.com/rels/stream/amqp");

				if (restItems == null || restItems.size() == 0)
				{
					throw new Exception("Getting amqp info failed, not data returned");
				}
				for (RestItem restItem : restItems)
				{
					for (RestLink link : restItem.getLinks())
					{
						if (link.getRelation().equals("amqp"))
						{
							URI amqpUri = null;
							try
							{
								amqpUri = new URI(link.getHref());
							} catch (URISyntaxException ex)
							{
								logger.warn("Malformed AMQP URL", ex);
							}

							if (amqpUri != null)
							{
								connectionFactory.setRequestedHeartbeat(5);

								String host = amqpUri.getHost();

								if (host != null)
								{
									connectionFactory.setHost(host);
								}

								int port = amqpUri.getPort();
								if (port != -1)
								{
									connectionFactory.setPort(port);
								}

								String userInfo = amqpUri.getRawUserInfo();
								userInfo = URLDecoder.decode(userInfo, "UTF-8");
								if (userInfo != null)
								{
									String userPass[] = userInfo.split(":");
									if (userPass.length > 2)
									{
										throw new IllegalArgumentException("Bad user info in AMQP " + "URI: " + userInfo);
									}
									connectionFactory.setUsername(uriDecode(userPass[0]));

									if (userPass.length == 2)
									{
										connectionFactory.setPassword(uriDecode(userPass[1]));
									}
								}

								String path = amqpUri.getRawPath();
								if (path != null && path.length() > 0)
								{
									queueName = path.substring(path.indexOf('/', 1) + 1);
									virtualHost = uriDecode(amqpUri.getPath().substring(1, path.indexOf('/', 1)));
									connectionFactory.setVirtualHost("/" + virtualHost);
								}

								connection = connectionFactory.newConnection();
								logger.info(String.format("Connected to Streaming Server for %1$s", getName()));

								channel = connection.createChannel();
								consumer = new QueueingConsumer(channel)
								{
									@Override
									public void handleCancelOk(String consumerTag)
									{
										super.handleCancelOk(consumerTag);
									}
								};

								channel.basicConsume(queueName, true, consumer);
								logger.info(String.format("Queue name: " + queueName + " for " + getName()));
								channel.basicQos(0, 10, false);

								actionExecuter.execute(new ConnectedAction(streamingEvents));
								
								URL echoURL = findEchoURL();								
								if (echoURL != null)
								{				
									echoHandler = new Echo(getName(), this, echoURL, virtualHost, 
											queueName, headers, this.echoMaxDelay);
								}
								else
								{
									logger.error("Failed to get echo url. Echoing disabled");
								}
								
								startEcho();
								isReconnecting.set(false);
							}
						}
					}
				}
			} catch (Exception ex)
			{
				failures++;
				logger.warn(String.format("Failed to connect stream %1$s, Error: %2$s Attempt %3$s", getName(), ex.getMessage(), failures));

				if (failures < MAX_RETRIES)
				{
					dispose();
					Thread.sleep(500);
				} else
				{
					logger.error(String.format("Exceeded max retries. Stopping Stream for %1$s", getName()));
					stopStreaming();
					break;
				}
			}
		}
	}
	//-------------------------- Echo management --------------------------
	private void startEcho()
	{
		if (echoTimer != null)
		{
			return;
		}
		if (!isStreaming)
		{
			logger.info("Not restarting echo timer. Streaming has stopped for" + getName());	
			return;
		}
		try
		{
			if (echoHandler != null)
			{				
				String name = "EchoTimer " + this.getName(); 
				echoTimer = new Timer(name);
				echoTimer.schedule(new EchoTask(echoHandler), this.echoSenderInterval);
			}
			else
			{
				logger.error("Failed to find echo handler. Echoing disabled");
			}
		}
		catch (Exception ex)
		{
			logger.error("Failed to start Echo timer", ex);
		}
	}
	private void stopEcho()
	{
		if (echoTimer != null)
		{
			echoTimer.cancel();
			if (echoHandler != null)
			{
				echoHandler.gotEcho(Echo.FINISHED);
			}
			echoTimer = null;
		}
	}

	@Override
	public synchronized void resetEcho()
	{
		try
		{
			stopEcho();
			startEcho();
		}
		catch (Exception ex)
		{
			logger.error("Resetting echo timer failed for " + getName(), ex);
		}
	}
	@Override
	public synchronized void echoTimeout()
	{
		if (isStreaming)
		{
			logger.error("Echo timed out for " + getName() + " Resetting connection");
			stopStreaming();
			//startStreaming(streamingEvents);
		}
	}
	//--------------------------------------------------------------------------
	private void stopChannel()
	{
		try
		{
			if (channel != null && channel.isOpen())
			{
				actionExecuter.execute(new DisconnectedAction(streamingEvents));
				channel.basicCancel(consumer.getConsumerTag());
				channel.close();
			}
		} catch (Exception ex)
		{
			logger.error("Stopping the channel failed with " + ex.getMessage());
		} finally
		{
			channel = null;
		}
	}

	private void stopConnection()
	{
		try
		{
			if(connection != null && connection.isOpen())
			{
				connection.close();
			}
		}catch (Exception ex)
		{
			logger.error("Stopping the channel failed with " + ex.getMessage());
		}
		finally
		{
			connection = null;
		}
	}
	
	private void dispose()
	{
		logger.debug(String.format("Clean-up for %1$s",getName()));
		try
		{
			//actionExecuter.shutdownNow();
			stopEcho();
			stopChannel();
			stopConnection();
			
		}
		catch(Exception ex)
		{
			logger.error(String.format("Problem while trying to clean-up stream for %1$s",getName()),ex);
		}
	}

	public void pauseStreaming(){
		logger.info(String.format("Streaming paused for %1$s", getName()));
		//pause = true;
		//stopEcho();
	}
	
	public void unpauseStreaming(){
		logger.info(String.format("Streaming un-paused for %1$s",getName()));
	//	synchronized(monitor){
	//		pause = false;
	//		monitor.notify();
	//	}
	//	startEcho();
	}
	
	public void stopStreaming()
	{
		try{
			logger.info(String.format("Streaming stopped for %1$s",getName()));

			isStreaming = false;
			isReconnecting.set(false);
			dispose();
			//
			// Echo just in case the timer has gone off
			//
			if (echoHandler != null)
			{
				echoHandler.gotEcho(Echo.FINISHED);
			}

			if (streamThread != null)
			{
				streamThread.interrupt();
				streamThread = null;
			}
		}
		catch (Exception ex){
			logger.warn("Stopping streaming failed", ex);
		}
	}
	
	public boolean isStreaming(){
		return isStreaming;
	}
	
	public synchronized void feedUp(){
		if (feedStatusAction.getStatus() == FeedStatus.Down){
			try{
				feedStatusAction.setStatus(FeedStatus.Up);
				actionExecuter.execute(feedStatusAction);
			} catch (Exception e){
				logger.warn("send feed up failed for " + getName());
			} 
		}
	}
	
	public synchronized void feedDown(){
		try{
			feedStatusAction.setStatus(FeedStatus.Down);
			actionExecuter.execute(feedStatusAction);				
		} catch (Exception e){
			logger.warn("send feed down failed for " + getName());
		}
	}
	
	private String uriDecode(String s) {
        try {
            // URLDecode decodes '+' to a space, as for
            // form encoding.  So protect plus signs.
            return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
        }
        catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
	
	private URL findEchoURL(){
		URL theURL = null;
		for (RestLink restLink : state.getLinks()){
			if (restLink.getRelation().equals("http://api.sportingsolutions.com/rels/stream/echo")){
				try{
					theURL = new URL(restLink.getHref());
				} catch (MalformedURLException ex){
					logger.warn("Malformed Echo URL", ex);
				}
			}
		}
		return theURL;
	}
}
