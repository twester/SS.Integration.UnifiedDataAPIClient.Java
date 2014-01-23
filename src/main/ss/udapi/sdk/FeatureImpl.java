package ss.udapi.sdk;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import ss.udapi.sdk.interfaces.Feature;
import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;

public class FeatureImpl implements Feature
{
  private Logger logger = Logger.getLogger(FeatureImpl.class.getName());
  
  private ServiceRequest availableFeatures;
  private RestItem restItem = new RestItem();
  private static HttpServices httpSvcs = new HttpServices();

  
  protected FeatureImpl(RestItem restItem, ServiceRequest availableFeatures){
    this.restItem = restItem;
    this.availableFeatures = availableFeatures;
    logger.debug("Instantiated Feature: " + restItem.getName());
  }

  
  public String getName() {
    return restItem.getName();
  }

  public Resource getResource(String resourceName) {
    NDC.push("getResource: " + resourceName);
    logger.info("Retrieving resource");
    
    ServiceRequest availableResources = httpSvcs.processRequest(availableFeatures, "http://api.sportingsolutions.com/rels/resources/list", restItem.getName());
    List<RestItem> restItems = availableResources.getServiceRestItems();
    for(RestItem searchRestItem:restItems){
      System.out.println("----------------->" + searchRestItem.getName());
      if (searchRestItem.getName().equals(resourceName)) {
        NDC.pop();
        return new ResourceImpl(searchRestItem, availableResources);
      }
    }
    NDC.pop();
    return null;
  }
  
  
  public List<Resource> getResources() {
    NDC.push("getResources: all resources for feature: " + restItem.getName());
    logger.info("Retrieving all resources");

    ServiceRequest availableResources = httpSvcs.processRequest(availableFeatures, "http://api.sportingsolutions.com/rels/resources/list", restItem.getName());
    List<RestItem> restItems = availableResources.getServiceRestItems();
    List<Resource> resourceSet = new ArrayList<Resource>();
    for(RestItem searchRestItem:restItems){
      resourceSet.add(new ResourceImpl(searchRestItem, availableResources));
    }
    NDC.pop();
    return resourceSet;
  }

}
