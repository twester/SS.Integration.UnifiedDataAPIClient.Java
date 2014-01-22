package ss.udapi.sdk.services;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.log4j.Logger;

public class WorkQueue
{
  private static WorkQueue workQueue = null;
  private static LinkedBlockingQueue<String> linkedQueue = new LinkedBlockingQueue<String>();
  
  private static Logger logger = Logger.getLogger(WorkQueue.class);
  
  private WorkQueue()
  {
    linkedQueue = new LinkedBlockingQueue<String>();
  }
  
  public static WorkQueue getWorkQueue()
  {
    if (workQueue == null)
    {
      workQueue = new WorkQueue();
    }
    return workQueue;
  }
  
  public static void addTask(String task)
  {
    try {
      linkedQueue.put(task);
    } catch (Exception ex) {
      logger.error("WorkQueue management interrupted", ex);
    }
  }
  
  public static String getTask()
  {
    String task=null;
    try {
      task = linkedQueue.take();
    } catch (Exception ex) {
      logger.error("WorkQueue management interrupted", ex);
    }
    return task;
  }
  
  
  
}
