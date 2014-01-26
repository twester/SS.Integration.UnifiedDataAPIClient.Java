package ss.udapi.sdk;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.rabbitmq.client.ConnectionFactory;

import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.model.StreamEcho;
import ss.udapi.sdk.model.Summary;
import ss.udapi.sdk.services.EchoSender;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.JsonHelper;
import ss.udapi.sdk.services.MQListener;
import ss.udapi.sdk.services.EchoResourceMap;
import ss.udapi.sdk.services.ResourceSession;
import ss.udapi.sdk.services.ResourceWorkerMap;
import ss.udapi.sdk.services.ServiceThreadExecutor;
import ss.udapi.sdk.services.SystemProperties;
import ss.udapi.sdk.streaming.ConnectedAction;
import ss.udapi.sdk.streaming.Event;
import ss.udapi.sdk.streaming.StreamAction;

public class ResourceImpl implements Resource
{
  private Logger logger = Logger.getLogger(ResourceImpl.class.getName());
  
  private ExecutorService actionExecuter = Executors.newSingleThreadExecutor();
  
  private boolean isStreaming;
  private boolean connected;
  
  
  private StreamAction streamAction;
  
  private String amqpDest;
  private ServiceRequest availableResources;
  private RestItem restItem = new RestItem();
  private static HttpServices httpSvcs = new HttpServices();
  
  //this is where the work ends up
  private LinkedBlockingQueue<String> myTasks = new LinkedBlockingQueue<String>();
  
  private int echoSenderInterval;
  private int maxMissedEchos;
  private List<Event> streamingEvents;
  
  
  
  public void addTask(String task)
  {
    myTasks.add(task);
  }
  
  
  
  
  
  
  protected ResourceImpl(RestItem restItem, ServiceRequest availableResources)
  {
    this.restItem = restItem;
    this.availableResources = availableResources;
    logger.debug("Instantiated Resource: " + restItem.getName());

    ResourceWorkerMap.addUOW(getId(), this);
    EchoResourceMap.getEchoMap().addResource(getId());
  }
  

  @Override
  public String getSnapshot()
  {
    return httpSvcs.getSnapshot(availableResources, "http://api.sportingsolutions.com/rels/snapshot", restItem.getName());
  }

  @Override
  public void startStreaming(List<Event> events)
  {
    startStreaming(events,
              new Integer(SystemProperties.get("ss.echo_sender_interval")),
              new Integer(SystemProperties.get("ss.echo_max_missed_echos")));
  }
  

  private void startStreaming(List<Event> events, int echoSenderInterval, int maxMissedEchos)
  {
    logger.info(String.format("Starting stream for %1$s with Echo Interval of %2$s and Max Missed Echos of %3$s",getName(),echoSenderInterval,maxMissedEchos));
    this.streamingEvents = events;
    this.echoSenderInterval = echoSenderInterval;
    this.maxMissedEchos = maxMissedEchos;
    streamAction = new StreamAction(streamingEvents);
  
    isStreaming = true;
    connect();
    streamData();
    
  }
  
  
  public void streamData()
  {
    StreamAction streamAction = new StreamAction(streamingEvents);
    
    while (! myTasks.isEmpty() && (isStreaming == true)) {
      String task = myTasks.poll();
      logger.debug("---------------------------->Streaming data:" + task.substring(0, 40));
      if(task.substring(13,24).equals("EchoFailure")) {
        logger.error("----------------------->Echo Retry exceeded out for stream" + getId());
       MQListener.reconnect(getId(), amqpDest);
      }
      try {
        streamAction.execute(task);
      } catch (Exception e) {
        logger.warn("Error on message receive", e);
      } 
    }
    
  }

  
  private void connect()
  {
    if (connected == false) {
      ServiceRequest amqpRequest = new ServiceRequest();
      amqpRequest = httpSvcs.processRequest(availableResources,"http://api.sportingsolutions.com/rels/stream/amqp", restItem.getName());
      
      amqpDest = amqpRequest.getServiceRestItems().get(0).getLinks().get(0).getHref();
      logger.debug("------------>Starting new streaming services: name " + restItem.getName() + " with queue " + amqpDest + " : " + getId()) ;
      
      
      //TODO: this looks nasty - it's needed but it should be tidied up, same parameters

        
      logger.debug("---------------listener running " + MQListener.isRunning() );
      if (MQListener.isRunning() == false)
      {

        
        
        MQListener.setResources(new ResourceSession(amqpDest, availableResources, getId()));
        
        ServiceThreadExecutor.executeTask(MQListener.getMQListener(amqpDest, availableResources));

        
        System.out.println("--------------------------> Starting Echo");
        EchoSender echoSender = EchoSender.getEchoSender(amqpDest, availableResources);
        ServiceThreadExecutor.executeTask(echoSender);

        
        actionExecuter.execute(new ConnectedAction(streamingEvents));

        //TODO move down once listener is running
        
      } else { 
        System.out.println("--------------------------> Second bit");
      }    
      MQListener.setResources(new ResourceSession(amqpDest, availableResources, getId()));
      connected = true;
    }
  }

  
  
  

  @Override
  public void stopStreaming()
  {
    isStreaming = true;
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











