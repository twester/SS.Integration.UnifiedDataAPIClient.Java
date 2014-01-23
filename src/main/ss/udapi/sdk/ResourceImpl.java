package ss.udapi.sdk;

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
import ss.udapi.sdk.services.ResourceWorkerMap;
import ss.udapi.sdk.services.ServiceThreadExecutor;
import ss.udapi.sdk.services.SystemProperties;
import ss.udapi.sdk.streaming.Event;

public class ResourceImpl implements Resource
{
  private Logger logger = Logger.getLogger(ResourceImpl.class.getName());
  
  private boolean isStreamingStopped;
  private boolean isStreamingSuspended;
  private boolean connected;
  
  private ServiceRequest availableResources;
  private RestItem restItem = new RestItem();
  private static HttpServices httpSvcs = new HttpServices();
  
  //TODO: this is where the work ends up
  private LinkedBlockingQueue<JsonObject> myTasks = new LinkedBlockingQueue<JsonObject>();
  
  private int echoSenderInterval;
  private int maxMissedEchos;
  private List<Event> streamingEvents;
  
  
  
  public void addTask(JsonObject task)
  {
    myTasks.add(task);
  }
  
  
  
  
  
  
  protected ResourceImpl(RestItem restItem, ServiceRequest availableResources)
  {
    this.restItem = restItem;
    this.availableResources = availableResources;
    logger.debug("Instantiated Resource: " + restItem.getName());

    ResourceWorkerMap.addUOW(getId(), this);
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
  
    isStreamingStopped = false;
    connect();
    streamData();
    
  }
  
  
  public void streamData()
  {
    while (! myTasks.isEmpty()) {
      JsonObject task = myTasks.poll();
    }
    
  }

  
  private void connect()
  {
    if (connected == false) {
      ServiceRequest amqpRequest = new ServiceRequest();
      amqpRequest = httpSvcs.processRequest(availableResources,"http://api.sportingsolutions.com/rels/stream/amqp", restItem.getName());
      
      String amqpDest = amqpRequest.getServiceRestItems().get(0).getLinks().get(0).getHref();
      logger.debug("------------>Starting new streaming services: name " + restItem.getName() + " with queue " + amqpDest);
      
      
      //TODO: this looks nasty - it's needed but it should be tidied up, same parameters
      MQListener mqListener = MQListener.getMQListener(amqpDest, availableResources);
      mqListener.setResources(amqpDest, availableResources);
      
      if (mqListener.isRunning() == true)
      {
        mqListener.addQueue(amqpDest, availableResources);
      } else { 
        ServiceThreadExecutor.executeTask(mqListener);
        
        EchoSender echoSender = EchoSender.getEchoSender(amqpDest, availableResources);
        ServiceThreadExecutor.executeTask(echoSender);
  
        try {
          //TODO see if we need this anymore the singleton executor service should habve take care of this
          Thread.sleep(100);
        } catch (Exception ex) {
          logger.fatal("MQListener instantiation interrupted");
        }
      }    
      connected = true;
    }
  }

  
  
  

  @Override
  public void stopStreaming()
  {
    isStreamingStopped = true;
  }

  @Override
  public void pauseStreaming()
  {
    isStreamingSuspended = true;

  }

  @Override
  public void unpauseStreaming()
  {
    isStreamingSuspended = false;
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











