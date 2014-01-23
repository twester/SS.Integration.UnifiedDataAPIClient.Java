package ss.udapi.sdk;

import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.services.ResourceWorkerMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class FixtureActionProcessor implements Runnable
{
  private String task;
  
  public FixtureActionProcessor(String task)
  {
    this.task = task; 
  }
  
  
  @Override
  public void run()
  {
    JsonObject jsonObject = new JsonParser().parse(task).getAsJsonObject();
    String resourceId = jsonObject.get("Id").getAsString();

    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(resourceId);
    resource.addTask(jsonObject);
    
    resource.streamData();
    
    
    // TODO Auto-generated method stub

  }

}
