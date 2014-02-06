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

import ss.udapi.sdk.interfaces.Feature;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Services provide access to available features for that service.
 *
 */
public class ServiceImpl implements Service
{
  private Logger logger = Logger.getLogger(ServiceImpl.class.getName());
  private ServiceRequest availableServices;
  private RestItem restItem = new RestItem();
  private static HttpServices httpSvcs = new HttpServices();

  
  protected ServiceImpl(RestItem restItem, ServiceRequest availableServices){
    this.restItem = restItem;
    this.availableServices = availableServices;
    logger.info("Instantiated Service: " + restItem.getName());
  }


  /**
   * Retrieves a specific feature from those available for this service.
   * 
   * @param featureName       Name of feature which will be retrieved from all features available for this service.
   */  
  public Feature getFeature(String featureName) {
    logger.info("Retrieving feature: " + featureName);
    
    ServiceRequest availableFeatures = httpSvcs.processRequest(availableServices, "http://api.sportingsolutions.com/rels/features/list", restItem.getName());
    List<RestItem> restItems = availableFeatures.getServiceRestItems();
    for(RestItem searchRestItem:restItems){
      if (searchRestItem.getName().equals(featureName)) {
        return new FeatureImpl(searchRestItem, availableFeatures);
      }
    }
    return null;
  }

  

  /**
   * Retrieves all available features available for this service.
   */
  public List<Feature> getFeatures() {
    logger.info("Retrieving all features");
    
    ServiceRequest availableFeatures = httpSvcs.processRequest(availableServices, "http://api.sportingsolutions.com/rels/features/list", restItem.getName());
    List<RestItem> restItems = availableFeatures.getServiceRestItems();
    List<Feature> featureSet = new ArrayList<Feature>();
    for(RestItem searchRestItem:restItems){
      featureSet.add(new FeatureImpl(searchRestItem, availableFeatures));
    }
    return featureSet;
  }


  
  /**
   * Retrieves the service name.
   */
  public String getName() {
    return restItem.getName();
  }
  
  
  
  //Setter for unit testing
  protected void setHttpSvcs(HttpServices httpSvcs)
  {
    ServiceImpl.httpSvcs = httpSvcs;
  }


  //Getter for unit testing
  protected String getServiceHref()
  {
    return availableServices.getServiceRestItems().get(0).getLinks().get(0).getHref();
  }


  
}
