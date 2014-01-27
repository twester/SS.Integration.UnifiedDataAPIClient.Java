package ss.udapi.sdk.services;

import ss.udapi.sdk.ResourceImpl;
import ss.udapi.sdk.interfaces.Resource;

import com.google.gson.JsonElement;
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
    System.out.println("------------------------->Processing started");

    //TODO the jsonObject produced here gets corrupted it cannot be processed if the string is too big as
    //is the case with feature suspension, this is gson it works when fromGson() is called to map an Object
    //    JsonObject jsonObject = new JsonParser().parse(task).getAsJsonObject();
    //even this fails
    //    jsonObject.get("Content").getAsString(){
    //with:
    //    java.lang.UnsupportedOperationException: JsonObject
    //    at com.google.gson.JsonElement.getAsString(JsonElement.java:185)
    //    at ss.udapi.sdk.services.FixtureActionProcessor.run(Unknown Source)
    //    at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1145)
    //    at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:615)
    //    at java.lang.Thread.run(Thread.java:744)
    //so instead I have to get the string header and parse it manually
    
    System.out.println("------------------------->Processing started");
    
    String msgHead = task.substring(0, 200);
    int idStart = msgHead.indexOf("Id\":")+5;
    String fixtureId = msgHead.substring(idStart,idStart+27);
    
    System.out.println("-----------------------for fixture>" + fixtureId);
    
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(fixtureId);
    
    resource.addTask(task);
    
    resource.streamData();
    
    
  }

}
