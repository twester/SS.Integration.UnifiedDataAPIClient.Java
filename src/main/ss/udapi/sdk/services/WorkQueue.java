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
    System.out.println("worQueue1------------->" + linkedQueue.size());
  }
  
  public static WorkQueue getWorkQueue()
  {
    if (workQueue == null)
    {
      workQueue = new WorkQueue();
    }
    return workQueue;
  }
  
  public void addTask(String task)
  {
    System.out.println("worQueuein------------->" + linkedQueue.size());
    try {
      linkedQueue.put(task);
    } catch (Exception ex) {
      logger.error("WorkQueue management interrupted", ex);
    }
    System.out.println("worQueuein------------->" + linkedQueue.size());
  }
  
  public String getTask()
  {
    String task=null;
    System.out.println("worQueueout------------->" + linkedQueue.size());
    try {
      task = linkedQueue.take();
    } catch (Exception ex) {
      logger.error("WorkQueue management interrupted", ex);
    }
    return task;
  }
  
  
  
}
