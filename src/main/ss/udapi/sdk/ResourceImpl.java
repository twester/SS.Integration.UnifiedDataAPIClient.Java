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

import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.model.Summary;
import ss.udapi.sdk.services.EchoSender;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.MQListener;
import ss.udapi.sdk.services.EchoResourceMap;
import ss.udapi.sdk.services.ResourceEventsMap;
import ss.udapi.sdk.services.ResourceSession;
import ss.udapi.sdk.services.ResourceWorkerMap;
import ss.udapi.sdk.services.ServiceThreadExecutor;
import ss.udapi.sdk.services.SystemProperties;
import ss.udapi.sdk.streaming.ConnectedAction;
import ss.udapi.sdk.streaming.DisconnectedAction;
import ss.udapi.sdk.streaming.Event;
import ss.udapi.sdk.streaming.StreamAction;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;


public class ResourceImpl implements Resource
{
  private static Logger logger = Logger.getLogger(ResourceImpl.class.getName());
  private static HttpServices httpSvcs = new HttpServices();
  private static ExecutorService actionExecuter = Executors.newSingleThreadExecutor();
  private static ResourceEventsMap eventsMap = ResourceEventsMap.getEventMap();
  private boolean isStreaming;
  private boolean connected;
  private String amqpDest;
  private ServiceRequest availableResources;
  private RestItem restItem = new RestItem();
  private List<Event> streamingEvents;
  
  //this is where the work ends up
  private LinkedBlockingQueue<String> myTasks = new LinkedBlockingQueue<String>();
  
  
  public void addTask(String task) {
    myTasks.add(task);
  }

  
  protected ResourceImpl(RestItem restItem, ServiceRequest availableResources) {
    this.restItem = restItem;
    this.availableResources = availableResources;
    logger.debug("Instantiated Resource: " + restItem.getName());
    
    if(ResourceWorkerMap.exists(getId()) == true) {
      isStreaming = true;
//      streamingEvents = eventsMap.getEvents(getId());
    } else {
      ResourceWorkerMap.addUOW(getId(), this);
      EchoResourceMap.getEchoMap().addResource(getId());
    }
  }
  

  @Override
  public String getSnapshot() {
    return httpSvcs.getSnapshot(availableResources, "http://api.sportingsolutions.com/rels/snapshot", restItem.getName());
  }

  //TODO: add more parameters??
  @Override
  public void startStreaming(List<Event> events) {
    startStreaming(events,
              new Integer(SystemProperties.get("ss.echo_sender_interval")),
              new Integer(SystemProperties.get("ss.echo_max_missed_echos")));
  }
  

  private void startStreaming(List<Event> events, int echoSenderInterval, int maxMissedEchos) {
    /*looks slightly odd we're setting the same values we read above, but the client could set these directly so 
     * we have to allow for that */
    SystemProperties.setProperty("ss.echo_sender_interval", Integer.toString(echoSenderInterval));
    SystemProperties.setProperty("ss.echo_max_missed_echos", Integer.toString(maxMissedEchos));
    eventsMap.addEvents(getId(), events);
    
    logger.info(String.format("Starting stream for " + getName() + 
                " with Echo Interval of " + echoSenderInterval +  " and Max Missed Echos of " + maxMissedEchos));
    this.streamingEvents = events;
    isStreaming = true;
    connect();
    streamData();
  }

  
  private void connect() {
    if (connected == false) {
      ServiceRequest amqpRequest = new ServiceRequest();
      amqpRequest = httpSvcs.processRequest(availableResources,"http://api.sportingsolutions.com/rels/stream/amqp", restItem.getName());
      amqpDest = amqpRequest.getServiceRestItems().get(0).getLinks().get(0).getHref();
      logger.info("Starting new streaming services, name: " + getName() +" queue: " + amqpDest + " fixture ID: " + getId()) ;

      /* Because we have many client threads starting at the same time they can all get here before MQ Listener is fully
       * initialized so MQListener.isRunning can be false for quite a while.  MQListener.getSender is locked so only one thread
       * will ever be able to initialize it.  As it and echoSender are singletons they can only run once so the rest if the if
       * can be dropped through safely.  Eventually MQListener.isRunning will be true so this only happens for the first few
       * threads anyway.
       */
      if (MQListener.isRunning() == false)
      {
        MQListener.setResources(new ResourceSession(amqpDest, availableResources, getId()));
        ServiceThreadExecutor.executeTask(MQListener.getMQListener(amqpDest, availableResources));
        
        EchoSender echoSender = EchoSender.getEchoSender(amqpDest, availableResources);
        ServiceThreadExecutor.executeTask(echoSender);
      }   
      
      //MQListener.setResources does not allow duplicates so fall throughs from the above false will be ignored
      MQListener.setResources(new ResourceSession(amqpDest, availableResources, getId()));
      actionExecuter.execute(new ConnectedAction(streamingEvents));
      connected = true;
    }
  }
  
  
  public void streamData() {
    logger.debug("In resource " + getId() + " number of items received: " + myTasks.size() + isStreaming);
    StreamAction streamAction = new StreamAction(streamingEvents);
    while ((! myTasks.isEmpty()) && (isStreaming == true)) {
      String task = myTasks.poll();
      logger.debug("Streaming data: " + task.substring(0, 60));
      if(task.substring(13,24).equals("EchoFailure")) {
        logger.error("Echo Retry exceeded out for stream" + getName());
        try {
          isStreaming = false;
          MQListener.disconnect(getId());
          EchoResourceMap.getEchoMap().addResource(getId());
          actionExecuter.execute(new DisconnectedAction(streamingEvents));
        } catch (Exception ex) {
          logger.warn("Error on fixture disconnect, could have already been disconnected: ", ex);
        } 
      } else {
        try {
          streamAction.execute(task);
        } catch (Exception ex) {
          logger.warn("Error on message receive", ex);
        } 
      }
    }
      
  }

  
  public void mqDisconnectEvent()
  {
    logger.info("Disconnect event for ID:" + getId());
    isStreaming = false;
    EchoResourceMap.getEchoMap().removeResource(getId());
    System.out.println("----------------------------->disconnect events" + streamingEvents.size());
    
    actionExecuter.execute(new DisconnectedAction(streamingEvents));
  }
  

  @Override
  public void stopStreaming()
  {
    isStreaming = false;
  }

  
  @Override
  public void pauseStreaming()
  {
    isStreaming = false;
  }

  
  @Override
  public void unpauseStreaming()
  {
    isStreaming = true;
  }

  
  @Override
  public String getId()
  {
    return restItem.getContent().getId();
  }

  
  @Override
  public String getName()
  {
    return restItem.getName();
  }

  
  @Override
  public Summary getContent()
  {
    return restItem.getContent();
  }
}











