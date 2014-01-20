package ss.udapi.sdk;

import java.util.List;

import org.apache.log4j.Logger;

import com.rabbitmq.client.ConnectionFactory;

import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.model.Summary;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.SystemProperties;
import ss.udapi.sdk.streaming.Event;

public class ResourceImpl implements Resource
{
  private Logger logger = Logger.getLogger(ResourceImpl.class.getName());
  
  private ServiceRequest availableResources;
  private RestItem restItem = new RestItem();
  private static HttpServices httpSvcs = new HttpServices();
  
  private int echoSenderInterval;
  private int maxMissedEchos;
  private List<Event> streamingEvents;
  
  
  protected ResourceImpl(RestItem restItem, ServiceRequest availableResources){
    this.restItem = restItem;
    this.availableResources = availableResources;
    logger.debug("Instantiated Resource: " + restItem.getName());
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

  @Override
  public String getSnapshot()
  {
    return httpSvcs.getSnapshot(availableResources, "http://api.sportingsolutions.com/rels/snapshot", restItem.getName());
  }

  @Override
  public void startStreaming(List<Event> events)
  {
    System.out.println("--------------->" + events.size() + SystemProperties.get("ss.echo_sender_interval")); 

    startStreaming(events,
              new Integer(SystemProperties.get("ss.echo_sender_interval")),
              new Integer(SystemProperties.get("ss.echo_max_missed_echos")));
  }
  

  private void startStreaming(List<Event> events, int echoSenderInterval, int maxMissedEchos)
  {
    System.out.println("--------------->" + events.size()); 

    logger.info(String.format("Starting stream for %1$s with Echo Interval of %2$s and Max Missed Echos of %3$s",getName(),echoSenderInterval,maxMissedEchos));
    this.streamingEvents = events;
    this.echoSenderInterval = echoSenderInterval;
    this.maxMissedEchos = maxMissedEchos;
  
    
//  AND THIS IS WHERE WE START CHANGING THINGS.  NOT ONE THREAD BUT GET NOTIFICATIONS FROM A MONITOR THREAD TO DO SOMETHING
  }

  
  
  
  private void streamData()
  {
    ServiceRequest amqpRequest = new ServiceRequest();
    amqpRequest = httpSvcs.processRequest(availableResources,"http://api.sportingsolutions.com/rels/stream/amqp", restItem.getName());
    System.out.println("---------->" + amqpRequest);
//    ConnectionFactory connectionFactory = new ConnectionFactory();
    
    
  }
  
  
  
  
  

  @Override
  public void stopStreaming()
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void pauseStreaming()
  {
    // TODO Auto-generated method stub

  }

  @Override
  public void unpauseStreaming()
  {
    // TODO Auto-generated method stub

  }

}
