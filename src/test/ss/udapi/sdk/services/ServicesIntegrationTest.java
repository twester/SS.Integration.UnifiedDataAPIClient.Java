package ss.udapi.sdk.services;

import ss.udapi.sdk.model.ServiceRequest;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


import com.rabbitmq.client.ConnectionFactory;


public class ServicesIntegrationTest
{

  private static HttpServices httpSvcs = new HttpServices();
  MQServices mqSvcs = new MQServices();
  
  
  
  private ServiceRequest loginReq;
  private ServiceRequest serviceReq;
  private ServiceRequest featureReq;
  private ServiceRequest resourceReq;
  private ServiceRequest amqpReq;
  String snapShotResponse;
  
  ConnectionFactory connFactory;
  
  @Test
  public void testGetSession()
  {
    loginReq = httpSvcs.getSession("http://apicui.sportingsolutions.com");
    assertTrue((loginReq.getServiceRestItems().size()) == 2);
    System.out.println("1-------------->" + loginReq.getServiceRestItems().get(0).getName());
    System.out.println("2-------------->" + loginReq.getServiceRestItems().get(1).getName());

    serviceReq = httpSvcs.processLogin(loginReq, "http://api.sportingsolutions.com/rels/login", "Login");
    System.out.println("3-------------->" + serviceReq.getServiceRestItems().get(0).getName());

    featureReq = httpSvcs.processRequest(serviceReq, "http://api.sportingsolutions.com/rels/features/list", "UnifiedDataAPI");
    System.out.println("4-------------->" + featureReq.getServiceRestItems().get(0).getName());

    
//    resourceReq = httpSvcs.processRequest(featureReq, "http://api.sportingsolutions.com/rels/resources/list", null);
//    System.out.println("5-------------->" + resourceReq.getServiceRestItems().get(0).getName());
    
  
    resourceReq = httpSvcs.processRequest(featureReq, "http://api.sportingsolutions.com/rels/resources/list", "Football");
    System.out.println("6-------------->" + resourceReq.getServiceRestItems().get(0).getName());

    amqpReq = httpSvcs.processRequest(resourceReq, "http://api.sportingsolutions.com/rels/stream/amqp", "Fernando v Jim");
    System.out.println("7-------------->" + amqpReq.getServiceRestItems().get(0).getName());
    
//    snapShotResponse = httpSvcs.getSnapshot(resourceReq, "http://api.sportingsolutions.com/rels/snapshot", "Fernando v Jim");
//    System.out.println("8----------------->" + snapShotResponse);
    
    
    
    
  
  }

}
