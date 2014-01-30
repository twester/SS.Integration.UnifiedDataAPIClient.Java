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
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.ServiceThreadExecutor;
import ss.udapi.sdk.services.SystemProperties;
import ss.udapi.sdk.services.WorkQueueMonitor;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Object which logs into a Sporting Solutions server and provides access to subscribed services.  Sessions cannot
 * be instantiated directly, they can only be created through the SessionFactory class. 
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
  
  /*
   * Constructor used by the factory to create new instances.
   */
  protected SessionImpl(URL serverURL, Credentials credentials) {
    logger.info("Logging into system at url: [" + serverURL.toExternalForm() + "]");
    this.serverURL = serverURL;
    
    /*
     * This is not strictly part of the session initialization but is needed for the the information retrieved via the session
     * to be of use.  It's really part of the system's overall initilization
     */
    ServiceThreadExecutor.createExecutor();
    WorkQueueMonitor queueWorker = WorkQueueMonitor.getMonitor();
    ServiceThreadExecutor.executeTask(queueWorker);

    GetRoot(serverURL,credentials, true);
  }


  /*
   * Carries out the initial login into the system.
   */
  private void GetRoot(URL serverURL, Credentials credentials, Boolean authenticate){
    boolean compressionEnabled = false;
    if (serverURL.toString().length() > 0) {
      SystemProperties.setProperty("ss.url", serverURL.getPath());
    }
    if (authenticate = true) {
      if (credentials != null) {
        SystemProperties.setProperty("ss.username", credentials.getUserName());
        SystemProperties.setProperty("ss.password", credentials.getPassword());
      }
      sessionResponse = httpSvcs.getSession(serverURL.toExternalForm(), compressionEnabled);
      availableServices = httpSvcs.processLogin(sessionResponse, "http://api.sportingsolutions.com/rels/login", "Login");
    } else {
      availableServices = httpSvcs.processLogin(sessionResponse, "http://api.sportingsolutions.com/rels/login", "Login");
    }
  }

  
  /**
   * Retrieves a specific service from those available for this account.
   * 
   * @param svcName       Name of service which will be retrieved from all services available for this account.
   */
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
 
 
  /**
   * Retrieves all available services available for this account.
   */
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

}
