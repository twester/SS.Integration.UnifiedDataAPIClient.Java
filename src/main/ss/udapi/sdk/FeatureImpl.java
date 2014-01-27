//Copyright 2012 Spin Services Limited

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
import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;


public class FeatureImpl implements Feature 
{
  private static Logger logger = Logger.getLogger(FeatureImpl.class.getName());
  private static HttpServices httpSvcs = new HttpServices();
  private ServiceRequest availableFeatures;
  private RestItem restItem = new RestItem();

  
  protected FeatureImpl(RestItem restItem, ServiceRequest availableFeatures) {
    this.restItem = restItem;
    this.availableFeatures = availableFeatures;
    logger.info("Instantiated Feature: " + restItem.getName());
  }

  
  public Resource getResource(String resourceName) {
    logger.info("Retrieving resource" + resourceName);
    
    ServiceRequest availableResources = httpSvcs.processRequest(availableFeatures, "http://api.sportingsolutions.com/rels/resources/list", restItem.getName());
    List<RestItem> restItems = availableResources.getServiceRestItems();
    for(RestItem searchRestItem:restItems) {
      if (searchRestItem.getName().equals(resourceName)) {
        return new ResourceImpl(searchRestItem, availableResources);
      }
    }
    return null;
  }
  
  
  public List<Resource> getResources()
  {
    logger.info("Retrieving all resources");

    ServiceRequest availableResources = httpSvcs.processRequest(availableFeatures, "http://api.sportingsolutions.com/rels/resources/list", restItem.getName());
    List<RestItem> restItems = availableResources.getServiceRestItems();
    List<Resource> resourceSet = new ArrayList<Resource>();
    for(RestItem searchRestItem:restItems) {
      resourceSet.add(new ResourceImpl(searchRestItem, availableResources));
    }
    return resourceSet;
  }

  
  public String getName() {
    return restItem.getName();
  }
}
