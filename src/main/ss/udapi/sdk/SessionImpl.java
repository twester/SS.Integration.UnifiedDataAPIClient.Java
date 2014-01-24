package ss.udapi.sdk;

import ss.udapi.sdk.interfaces.Credentials;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.interfaces.Session;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.ServiceImpl;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.MQListener;
import ss.udapi.sdk.services.ResourceEchoMap;
import ss.udapi.sdk.services.ResourceWorkerMap;
import ss.udapi.sdk.services.ServiceThreadExecutor;
import ss.udapi.sdk.services.StartupSyncObject;
import ss.udapi.sdk.services.WorkQueue;
import ss.udapi.sdk.services.WorkQueueMonitor;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;

import java.net.URI;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

public class SessionImpl implements Session
{
  private static Logger logger = Logger.getLogger(SessionImpl.class.getName());
  private static HttpServices httpSvcs = new HttpServices();
  
  private ServiceRequest sessionResponse;
  private ServiceRequest availableServices;
  private URL serverURL;
  private List<RestItem> serviceRestItems;
  
  
  
  
  
  
  
  private URI amqpURI;
  private ServiceRequest resources;
  private int count;
  private HashMap<String,String> resourceChannMap = new HashMap<String,String>();


  private static MQListener instance = null;
  private static Channel channel;
  private static QueueingConsumer consumer;
  private static boolean MQListenerRunning = false;
  private Integer syncLock;
  
  
  
  
  
  protected SessionImpl(URL serverURL, Credentials credentials){
    logger.debug("Logging into system at url: [" + serverURL.toExternalForm() + "]");
    this.serverURL = serverURL;
    
    
    //not strictly part of the session but necessary implementation initialization
    
    //TODO what about restarts?  we don't want to loose all this do we?
    //singletons so they only use up a reference to it and it's built by the time we need it
    ServiceThreadExecutor.createExecutor();
    //WorkQueue workQueue = WorkQueue.getWorkQueue(); 
    
    ResourceWorkerMap workMap = ResourceWorkerMap.getWorkerMap();
    ResourceEchoMap echoMap = ResourceEchoMap.getEchoMap(); 

    WorkQueueMonitor queueWorker = WorkQueueMonitor.getMonitor();
    ServiceThreadExecutor.executeTask(queueWorker);
    StartupSyncObject syncObject = StartupSyncObject.getSyncObject(); 
    
    GetRoot(serverURL,credentials, true);
    
    
    
  }

  
  private void GetRoot(URL serverURL, Credentials credentials, Boolean authenticate){
    if (authenticate = true)
    {
      sessionResponse = httpSvcs.getSession(serverURL.toExternalForm());
      availableServices = httpSvcs.processLogin(sessionResponse, "http://api.sportingsolutions.com/rels/login", "Login");
    } else {
      availableServices = httpSvcs.processLogin(sessionResponse, "http://api.sportingsolutions.com/rels/login", "Login");
    }
    
    
    ServiceRequest featureReq = httpSvcs.processRequest(availableServices, "http://api.sportingsolutions.com/rels/features/list", "UnifiedDataAPI");
    ServiceRequest resourceReq = httpSvcs.processRequest(featureReq, "http://api.sportingsolutions.com/rels/resources/list", "Football");
    
    String instance = resourceReq.getServiceRestItems().get(0).getName();
    
    ServiceRequest amqpRequest = httpSvcs.processRequest(resourceReq, "http://api.sportingsolutions.com/rels/stream/amqp", instance);

    
    String amqpDest = amqpRequest.getServiceRestItems().get(0).getLinks().get(0).getHref();
    
    
  
    MQListener mqListener = MQListener.getMQListener();
      
    mqListener.setResources(amqpDest);   
    
    ServiceThreadExecutor.executeTask(mqListener);
    
  
  
  }

  
  
  
  
  private String uriDecode(String s) {
    try {
        // URLDecode decodes '+' to a space, as for
        // form encoding.  So protect plus signs.
        return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
    }
    catch (java.io.UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
  }     
  
  
  
  
  
  
  public Service getService(String svcName) {
    NDC.push("getService: " + svcName);
    logger.info("Retrieving service");
    
    //TODO: serviceRestItems is always set to null after a successful service retrieve, apart from the very first time after GetRoot, is that correct??
    if(serviceRestItems == null){
      GetRoot(serverURL,null,false);
      serviceRestItems = availableServices.getServiceRestItems();
    }
    
    if(serviceRestItems != null){     //If we end up with no results at all return null 
      Service service = null;
      for(RestItem restItem:serviceRestItems){
        if(restItem.getName().equals(svcName)){
          service = new ServiceImpl(restItem, availableServices);
        }
      }
      serviceRestItems = null;
      NDC.pop();
      return service;
    }
    NDC.pop();
    return null;
  }
  
  
  public List<Service> getServices() {
    NDC.push("getServices: all services");
    logger.info("Rerieving all services...");
    
    //TODO: serviceRestItems is always set to null after a successful service retrieve, apart from the very first time after GetRoot, is that correct??
    if(serviceRestItems == null){
      GetRoot(serverURL,null,false);
      serviceRestItems = availableServices.getServiceRestItems();
    }
    
    List<Service> serviceSet = new ArrayList<Service>();     //If we end up with no results at all return null 
    if(serviceRestItems != null){
      for(RestItem restItem:serviceRestItems){
        serviceSet.add(new ServiceImpl(restItem, availableServices));
      }
    }
    serviceRestItems = null;
    NDC.pop();
    return serviceSet;
  }


  public ServiceRequest getAvailableServices()
  {
    return availableServices;
  }
 
}
