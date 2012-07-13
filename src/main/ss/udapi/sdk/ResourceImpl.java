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
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.RestLink;
import ss.udapi.sdk.model.Summary;
import ss.udapi.sdk.streaming.ConnectedAction;
import ss.udapi.sdk.streaming.DisconnectedAction;
import ss.udapi.sdk.streaming.Event;
import ss.udapi.sdk.streaming.StreamAction;
import ss.udapi.sdk.streaming.SynchronizationAction;

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
	
	private Integer currentSequence;
	private Integer sequenceDiscrepancyThreshold;
	private Integer sequenceCheckerInterval;
	private Timer sequenceCheckerTimer;
	
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
		startStreaming(events,10000,2);
	}
	
	public void startStreaming(List<Event> events, Integer sequenceCheckerInterval, Integer sequenceDiscrepencyThreshold){
		logger.info(String.format("Starting stream for %1$s with Sequence Checker Interval of %2$s and Discrepency Threshold of %3$s",getName(),sequenceCheckerInterval,sequenceDiscrepencyThreshold));
		this.sequenceCheckerInterval = sequenceCheckerInterval;
		this.sequenceDiscrepancyThreshold = sequenceDiscrepencyThreshold;
		this.streamingEvents = events;
		
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
					currentSequence = jsonObject.get("Content").getAsJsonObject().get("Sequence").getAsInt();
					
					try {
						streamAction.execute(messageString);
					} catch (Exception e) {
						logger.warn("Error on message receive", e);
					}	
				}
				disconnections = 0;
			}catch(Exception ex){
				logger.warn(String.format("Lost connection to stream %1$s", getName()));
				if(!isReconnecting){
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
							        String queueName = "";
							        String path = amqpUri.getRawPath();
							        if (path != null && path.length() > 0) {
							        	queueName = path.substring(path.indexOf('/',1)+1);

							            connectionFactory.setVirtualHost("/" + uriDecode(amqpUri.getPath().substring(1,path.indexOf('/',1))));
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
							        
							        if(sequenceCheckerTimer != null){
							        	sequenceCheckerTimer.cancel();
							        	sequenceCheckerTimer = null;
							        }
							        
							        currentSequence = getSequenceAsInt();
							        
							        sequenceCheckerTimer = new Timer(true);
							        sequenceCheckerTimer.scheduleAtFixedRate(
				        								new TimerTask(){
				        										public void run(){
				        											
				        											checkSequence();
				        										}
				        								}, sequenceCheckerInterval, sequenceCheckerInterval);
							        
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
			}catch(IOException ex){
				if(disconnections > maxRetries){
					logger.error(String.format("Failed to reconnect Stream for %1$s",getName()));
					stopStreaming();
					throw ex;
				}
				
				Thread.sleep(500);
				disconnections++;
				logger.warn(String.format("Failed to reconnect stream %1$s, Attempt %2$s", getName(),disconnections));
			}
			catch(Exception ex){
				stopStreaming();
				break;
			}
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
			
			actionExecuter.execute(new DisconnectedAction(streamingEvents));
		}catch(IOException ex){
			logger.error(String.format("Problem while trying to shutdown stream for %1$s",getName()),ex);
		}
	}

	public void pauseStreaming(){
		logger.info(String.format("Streaming paused for %1$s", getName()));
		pause = true;
	}
	
	public void unpauseStreaming(){
		logger.info(String.format("Streaming un-paused for %1$s",getName()));
		synchronized(monitor){
			pause = false;
			monitor.notifyAll();
		}
	}
	
	public void stopStreaming(){
		isStreaming = false;
		if(consumer != null){
			try {
				channel.basicCancel(consumer.getConsumerTag());
				if(sequenceCheckerTimer != null){
		        	sequenceCheckerTimer.cancel();
		        	sequenceCheckerTimer = null;
		        }
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
	
	private void checkSequence(){
		Integer sequence = getSequenceAsInt();
		Integer sequenceGap = Math.abs(sequence - currentSequence);
		logger.debug(String.format("Sequence discrepency = %1$s for %2$s",sequenceGap,getName()));
		if(sequenceGap > sequenceDiscrepancyThreshold){
			if(sequenceCheckerTimer != null){
				sequenceCheckerTimer.cancel();
				sequenceCheckerTimer = null;
			}

			actionExecuter.execute(new SynchronizationAction(streamingEvents));
			
			isReconnecting = true;
			
			try {
				reconnect();
			} catch (Exception e) {
				logger.error(String.format("Problem reconnecting after synchronization failure for %1$s", getName()));
			}
			isReconnecting = false;
		}
	}
	
	private int getSequenceAsInt(){
		try{
			String stringSequence = FindRelationAndFollowAsString("http://api.sportingsolutions.com/rels/sequence");
			if(stringSequence != null && !stringSequence.isEmpty()){
				Integer sequence = Integer.parseInt(stringSequence);
				return sequence;
			}
		}catch(Exception ex){
			logger.error(String.format("Unable to retrieve sequence for %1$s", getName()),ex);
		}
		return 0;
	}
}
