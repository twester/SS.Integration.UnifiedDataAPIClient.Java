package ss.udapi.sdk.services;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import ss.udapi.sdk.model.ServiceRequest;

public class ServiceThreadExecutor
{
  private static Executor exec;
  
  public static void createExecutor() {
    //move to a separate static threadpool so we don't create three threads per resource :-)
    exec = Executors.newFixedThreadPool(3);
  }
  
  public static void executeTask(Runnable task)
  {
    exec.execute(task);
  }
  
  
}
