package ss.udapi.sdk.services;

import ss.udapi.sdk.model.ServiceRequest;

public class ResourceSession
{
  private String amqpDest = null;
  private ServiceRequest availableResources = null;
  private String resourceId = null;

  
  public ResourceSession(String amqpDest, ServiceRequest availableResources, String resourceId)
  {
    super();
    this.amqpDest = amqpDest;
    this.availableResources = availableResources;
    this.resourceId = resourceId;
  }


  public String getAmqpDest()
  {
    return amqpDest;
  }


  public ServiceRequest getAvailableResources()
  {
    return availableResources;
  }


  public String getResourceId()
  {
    return resourceId;
  }
  
  
  
  
  
  
}
