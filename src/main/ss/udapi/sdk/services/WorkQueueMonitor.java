package ss.udapi.sdk.services;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class WorkQueueMonitor implements Runnable
{
  private static Logger logger = Logger.getLogger(WorkQueueMonitor.class);
  
  private static WorkQueueMonitor monitor = null;
  private WorkQueue workQueue = WorkQueue.getWorkQueue();
  
  private WorkQueueMonitor()
  {
    
  }
  
  public static WorkQueueMonitor getMonitor()
  {
    if (monitor == null) 
    {
      monitor = new WorkQueueMonitor();
      ActionThreadExecutor.createExecutor();
    }
    return monitor;
  }
  
  @Override
  public void run()
  {
    
    while(true)
    {
      String task = workQueue.getTask();
      logger.debug("---------------->Queue Read: " + task.substring(0,40));

      try {
        FixtureActionProcessor processor = new FixtureActionProcessor(task);
        ActionThreadExecutor.executeTask(processor);
      } catch (Exception ex) {
        logger.debug("Work queue monitor has been interrupted");
      }
    }
    
    
  }

}
