package ss.udapi.sdk.model;

import java.util.List;

public class ServiceRequest
{
  private String authToken = null;
  private List<RestItem> serviceRestItems = null;

  public String getAuthToken()
  {
    return authToken;
  }
  
  public void setAuthToken(String authToken)
  {
    this.authToken = authToken;
  }

  public List<RestItem> getServiceRestItems()
  {
    return serviceRestItems;
  }

  public void setServiceRestItems(List<RestItem> serviceRestItems)
  {
    this.serviceRestItems = serviceRestItems;
  }
  
}
