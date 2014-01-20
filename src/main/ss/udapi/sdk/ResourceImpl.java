package ss.udapi.sdk;

import java.util.List;

import org.apache.log4j.Logger;

import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.model.Summary;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.streaming.Event;

public class ResourceImpl implements Resource
{
  private Logger logger = Logger.getLogger(ResourceImpl.class.getName());
  
  private ServiceRequest availableResources;
  private RestItem restItem = new RestItem();
  private static HttpServices httpSvcs = new HttpServices();
  
  
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
  public void startStreaming(List<Event> streamingEvents)
  {
    // TODO Auto-generated method stub

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
