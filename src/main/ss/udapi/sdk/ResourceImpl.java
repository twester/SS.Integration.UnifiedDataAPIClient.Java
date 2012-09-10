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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import ss.udapi.sdk.clients.RestHelper;
import ss.udapi.sdk.extensions.JsonHelper;
import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.RestLink;
import ss.udapi.sdk.model.StreamEcho;
import ss.udapi.sdk.model.Summary;
import ss.udapi.sdk.streaming.ConnectedAction;
import ss.udapi.sdk.streaming.DisconnectedAction;
import ss.udapi.sdk.streaming.Event;
import ss.udapi.sdk.streaming.StreamAction;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

public class ResourceImpl extends Endpoint implements Resource {

	private static Logger logger = Logger.getLogger(ResourceImpl.class.getName());

	private Boolean isStreaming;
	private List<Event> streamingEvents;
	
	
	private ConnectionFactory connectionFactory;
	private Integer maxRetries;
	private Integer disconnections;
	private QueueingConsumer consumer;
	private Channel channel;
	private Connection connection;
	
	private String queueName;
	private String virtualHost;
	private int echoSenderInterval;
	private int echoMaxDelay;
	private String lastRecievedEchoGuid;
	private boolean isProcessingStreamEvent;
	private final Object echoTimerMonitor = new Object();
	private final Object echoResetMonitor = new Object();
	private volatile boolean echoReset = false;
	private Thread echoThread;
	
	private Boolean isReconnecting;
	
	private ExecutorService actionExecuter = Executors.newSingleThreadExecutor();
	
	private final Object monitor = new Object();
	private volatile boolean pause = false;
	
	ResourceImpl(Map<String,String> headers, RestItem restItem){
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

	public void startStreaming(List<Event> events){
		startStreaming(events,10000,3000);
	}
	
	public void startStreaming(List<Event> events, Integer echoInterval, Integer echoMaxDelay){
		logger.info(String.format("Starting stream for %1$s with Echo Interval of %2$s and Echo Max Delay of %3$s",getName(),echoInterval,echoMaxDelay));
		this.streamingEvents = events;
		this.echoSenderInterval = 10000;
		this.echoMaxDelay = 3000;
		
		Runnable runnable = new Runnable(){
			@Override
			public void run(){
				try {
					streamData();
				} catch (Exception ex) {
					logger.error(String.format("There has been a serious error streaming %1$s . The stream cannot continue.", getName()),ex);
				}
			}
		};
		
		Thread theThread = new Thread(runnable);
		theThread.start();
	}
	
	private void sendEcho() {
		String guid = UUID.randomUUID().toString();
		
		while(isStreaming){
			try{
				synchronized(echoTimerMonitor){
						try{
							echoTimerMonitor.wait(echoSenderInterval);
						}catch(InterruptedException ex){
							Thread.currentThread().interrupt();
							break;
						}
					}
				
				if(!isProcessingStreamEvent){
					if(state != null){
						for(RestLink restLink:state.getLinks()){
							if(restLink.getRelation().equals("http://api.sportingsolutions.com/rels/stream/echo")){
								URL theURL = null;
								try{
									theURL = new URL(restLink.getHref());
								}catch(MalformedURLException ex){
									logger.warn("Malformed Login URL", ex);
								}
								
								StreamEcho streamEcho = new StreamEcho(); 
								streamEcho.setHost(virtualHost);
								streamEcho.setQueue(queueName);
								
								DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS'Z'");
								df.setTimeZone(TimeZone.getTimeZone("UTC"));
								streamEcho.setMessage(guid + ";" + df.format(new Date()));
								
								String stringStreamEcho = JsonHelper.ToJson(streamEcho);
								
								RestHelper.getResponse(theURL, stringStreamEcho, "POST", "application/json", 3000, headers, false);
							}
						}
					}
				}
			}catch(Exception ex){
				logger.error("Unable to post echo", ex);
			}
			
			try{
				synchronized(echoResetMonitor){
					try{
						while(!echoReset){
						
							echoResetMonitor.wait(echoMaxDelay);
						
							//if not timeout
							if(echoReset){
								if(guid.equals(lastRecievedEchoGuid)){
									logger.debug("OK");
								}else{
									logger.error("Recieved Echo Messages from differerent client");
								}
							}else{
								if(!isProcessingStreamEvent){
									logger.debug("BAD");
									isReconnecting = true;
									reconnect();
									synchronized(echoTimerMonitor){
										echoTimerMonitor.notify();
									}
									isReconnecting = false;
								}
								echoReset = true;
							}
						}
						echoReset = false;
					}catch(InterruptedException ex){
						Thread.currentThread().interrupt();
						break;
					}
				}
			}catch(Exception ex){
				logger.error("Unable to find echo", ex);
			}
		}
	}
	
	private void streamData() throws IOException, InterruptedException{
		connectionFactory = new ConnectionFactory();
		maxRetries = 10;
		disconnections = 0;
		isStreaming = true;
		
		reconnect();
		logger.info(String.format("Initialised connection to Streaming Queue for %1$s", getName()));
		
		StreamAction streamAction = new StreamAction(streamingEvents);
		
		while(isStreaming){
			
			synchronized(monitor){
				while(pause==true){
					monitor.wait();
				}
			}
			
			try{
				Delivery output = consumer.nextDelivery();
				if(output != null){
					byte[] message = output.getBody();
				
					String messageString = new String(message);
					JsonObject jsonObject = new JsonParser().parse(messageString).getAsJsonObject();
					if(jsonObject.get("Relation").getAsString().equals("http://api.sportingsolutions.com/rels/stream/echo")){
						String[] split = jsonObject.get("Content").getAsString().split(";");
						lastRecievedEchoGuid = split[0];
						
						synchronized(echoResetMonitor){
							echoReset = true;
							echoResetMonitor.notify();
						}
					}else{
						isProcessingStreamEvent = true;
						try {
							streamAction.execute(messageString);
						} catch (Exception e) {
							logger.warn("Error on message receive", e);
						}	
						isProcessingStreamEvent = false;
					}
				}
				disconnections = 0;
			}catch(Exception ex){
				logger.warn(String.format("Lost connection to stream %1$s", getName()));
				if(!isReconnecting){
					StopEcho();
					reconnect();
				}else{
					Thread.sleep(1000);
				}
			}
		}
	}
	
	private void reconnect() throws IOException, InterruptedException{
		Boolean success = false;
		while(!success && isStreaming){
			try{
				List<RestItem> restItems = FindRelationAndFollow("http://api.sportingsolutions.com/rels/stream/amqp");
				if(restItems != null){
					for(RestItem restItem:restItems){
						for(RestLink link:restItem.getLinks()){
							if(link.getRelation().equals("amqp")){
								URI amqpUri = null;
								try {
									amqpUri = new URI(link.getHref());
								} catch (URISyntaxException ex) {
									logger.warn("Malformed AMQP URL", ex);
								}
								
								if(amqpUri != null){
									connectionFactory.setRequestedHeartbeat(5);
									
									String host = amqpUri.getHost();
									
									if (host != null) {
							        	connectionFactory.setHost(host);
							        }

							        int port = amqpUri.getPort();
							        if (port != -1) {
							        	connectionFactory.setPort(port);
							        }
							        
							        String userInfo = amqpUri.getRawUserInfo();
							        userInfo = URLDecoder.decode(userInfo,"UTF-8");
							        if (userInfo != null) {
							            String userPass[] = userInfo.split(":");
							            if (userPass.length > 2) {
							                throw new IllegalArgumentException("Bad user info in AMQP " +
							                                                   "URI: " + userInfo);
							            }
							            connectionFactory.setUsername(uriDecode(userPass[0]));

							            if (userPass.length == 2) {
							            	connectionFactory.setPassword(uriDecode(userPass[1]));
							            }
							        }

							        String path = amqpUri.getRawPath();
							        if (path != null && path.length() > 0) {
							        	queueName = path.substring(path.indexOf('/',1)+1);
							        	virtualHost = uriDecode(amqpUri.getPath().substring(1,path.indexOf('/',1)));
							            connectionFactory.setVirtualHost("/" + virtualHost);
							        }
							        
							        if(channel != null){
							        	channel.close();
							        	channel = null;
							        }
							        
							        if(connection != null){
							        	connection.close();
							        	connection = null;
							        }
							        
							        connection = connectionFactory.newConnection();
							        logger.info(String.format("Successfully connected to Streaming Server for %1$s", getName()));

							        StartEcho();
							        
							        actionExecuter.execute(new ConnectedAction(streamingEvents));
							        
							        channel = connection.createChannel();
							        consumer = new QueueingConsumer(channel){
							        	@Override
							        	public void handleCancelOk(String consumerTag){
							        		super.handleCancelOk(consumerTag);
							        		dispose();
							        	}
							        };
							        
							        channel.basicConsume(queueName,true,consumer);
							        channel.basicQos(0, 10, false);
							        success = true;
								}
							}
						}
					}
				}
			}catch(Exception ex){
				if(disconnections > maxRetries){
					logger.error(String.format("Failed to reconnect Stream for %1$s",getName()));
					stopStreaming();
				}
				
				Thread.sleep(500);
				disconnections++;
				logger.warn(String.format("Failed to reconnect stream %1$s, Attempt %2$s", getName(),disconnections));
			}
		}
	}
	
	private void StartEcho(){
		if(echoThread == null){
			echoReset = false;
			echoThread = new Thread(new Runnable(){
									@Override
									public void run(){
										try {
											sendEcho();
										} catch (Exception ex) {
											logger.error(String.format("There has been a serious error streaming %1$s . The stream cannot continue.", getName()),ex);
										}
									}});
			echoThread.start();
		}
	}
	
	private void StopEcho(){
		if(echoThread != null){
			echoThread.interrupt();
			echoThread = null;
		}
	}
	
	
	
	private void dispose(){
		logger.info(String.format("Streaming stopped for %1$s",getName()));
		try{
			if(channel != null){
				channel.close();
				channel = null;
			}
			
			if(connection != null){
				connection.close();
				connection = null;
			}
			
			if(echoThread != null){
				echoThread = null;
			}
		}catch(IOException ex){
			logger.error(String.format("Problem while trying to shutdown stream for %1$s",getName()),ex);
		}
		actionExecuter.execute(new DisconnectedAction(streamingEvents));
	}

	public void pauseStreaming(){
		logger.info(String.format("Streaming paused for %1$s", getName()));
		pause = true;
		StopEcho();
	}
	
	public void unpauseStreaming(){
		logger.info(String.format("Streaming un-paused for %1$s",getName()));
		synchronized(monitor){
			pause = false;
			monitor.notify();
		}
		StartEcho();
	}
	
	public void stopStreaming(){
		isStreaming = false;
		if(consumer != null){
			
			StopEcho();
			
			try {
				channel.basicCancel(consumer.getConsumerTag());
			} catch (IOException ex) {
				logger.error(String.format("There has been an error while trying to stop streaming for %1$s", getName()), ex);
			}
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

}
