package ss.udapi.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;

import ss.udapi.sdk.interfaces.Feature;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;


public class ServiceImpl implements Service
{
  private Logger logger = Logger.getLogger(ServiceImpl.class.getName());
  
  private ServiceRequest availableServices;
  private RestItem restItem = new RestItem();
  private static HttpServices httpSvcs = new HttpServices();

  
  protected ServiceImpl(RestItem restItem, ServiceRequest availableServices){
    this.restItem = restItem;
    this.availableServices = availableServices;
    logger.debug("Instantiated Service: " + restItem.getName());
  }

  
  public String getName() {
    return restItem.getName();
  }

  public Feature getFeature(String featureName) {
    NDC.push("getFeature: " + featureName);

    ServiceRequest availableFeatures = httpSvcs.processRequest(availableServices, "http://api.sportingsolutions.com/rels/features/list", restItem.getName());
    List<RestItem> restItems = availableFeatures.getServiceRestItems();
    for(RestItem searchRestItem:restItems){
      if (searchRestItem.getName().equals(featureName)) {
        NDC.pop();
        return new FeatureImpl(searchRestItem, availableFeatures);
      }
    }
    NDC.pop();
    return null;
  }
  
  
  public List<Feature> getFeatures() {
    NDC.push("getFeatures: all features for service: " + restItem.getName());
    logger.info("Retrieving all features");
    
    ServiceRequest availableFeatures = httpSvcs.processRequest(availableServices, "http://api.sportingsolutions.com/rels/features/list", restItem.getName());
    List<RestItem> restItems = availableFeatures.getServiceRestItems();
    List<Feature> featureSet = new ArrayList<Feature>();
    for(RestItem searchRestItem:restItems){
      featureSet.add(new FeatureImpl(searchRestItem, availableFeatures));
    }
    NDC.pop();
    return featureSet;
  }

  
}
