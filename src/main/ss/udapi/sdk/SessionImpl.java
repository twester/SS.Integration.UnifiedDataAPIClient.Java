//Copyright 2014 Spin Services Limited

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package ss.udapi.sdk;

import ss.udapi.sdk.interfaces.Credentials;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.interfaces.Session;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.ServiceImpl;
import ss.udapi.sdk.services.CtagResourceMap;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.ServiceThreadExecutor;
import ss.udapi.sdk.services.SystemProperties;
import ss.udapi.sdk.services.WorkQueueMonitor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
/**
 * Object to log into 
 * @author FGonzalez149
 *
 */
public class SessionImpl implements Session
{
  private static Logger logger = Logger.getLogger(SessionImpl.class.getName());
  private static HttpServices httpSvcs = new HttpServices();
  private ServiceRequest sessionResponse;
  private ServiceRequest availableServices;
  private URL serverURL;
  private List<RestItem> serviceRestItems;
  
  
  protected SessionImpl(URL serverURL, Credentials credentials) {
    logger.info("Logging into system at url: [" + serverURL.toExternalForm() + "]");
    this.serverURL = serverURL;
    
    //Not strictly part of the session initialization but needed for the session and it is part of the system's initilization
    ServiceThreadExecutor.createExecutor();
    WorkQueueMonitor queueWorker = WorkQueueMonitor.getMonitor();
    ServiceThreadExecutor.executeTask(queueWorker);
    CtagResourceMap.getCtagMap();

    GetRoot(serverURL,credentials, true);
  }

  
  private void GetRoot(URL serverURL, Credentials credentials, Boolean authenticate){
    if (authenticate = true) {
      if (serverURL.toString().length() > 0) {
        SystemProperties.setProperty("ss.url", serverURL.getPath());
      }
      if (credentials != null) {
        SystemProperties.setProperty("ss.username", credentials.getUserName());
        SystemProperties.setProperty("ss.password", credentials.getPassword());
      }
      sessionResponse = httpSvcs.getSession(serverURL.toExternalForm());
      availableServices = httpSvcs.processLogin(sessionResponse, "http://api.sportingsolutions.com/rels/login", "Login");
    } else {
      availableServices = httpSvcs.processLogin(sessionResponse, "http://api.sportingsolutions.com/rels/login", "Login");
    }
  }

  
  public Service getService(String svcName) {
    logger.info("Retrieving service: " + svcName);
    
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
      return service;
    }
    return null;
  }
  
  
  public List<Service> getServices() {
    logger.info("Rerieving all services...");
    
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
    return serviceSet;
  }


  public ServiceRequest getAvailableServices()
  {
    return availableServices;
  }
 
}
