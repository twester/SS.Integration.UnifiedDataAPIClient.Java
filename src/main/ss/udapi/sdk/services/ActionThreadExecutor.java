package ss.udapi.sdk.services;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import ss.udapi.sdk.model.ServiceRequest;

public class ActionThreadExecutor
{
  private static  Executor exec;
  private static Semaphore semaphore;
  
  
  public static void createExecutor() {
    int workerThreads = Integer.parseInt(SystemProperties.get("ss.workerThreads"));
    exec = Executors.newFixedThreadPool(workerThreads);
    semaphore = new Semaphore(workerThreads);
  }
  
  public static void executeTask(Runnable task)
  {
    exec.execute(task);
  }
  
  
}
