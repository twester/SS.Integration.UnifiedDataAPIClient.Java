package ss.udapi.sdk.services;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.streaming.StreamAction;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import java.io.IOException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;


public class ServicesIntegrationTest
{

  private static HttpServices httpSvcs = new HttpServices();
  MQServices mqSvcs = new MQServices();
  
  private QueueingConsumer consumer;
  
  private ServiceRequest loginReq;
  private ServiceRequest serviceReq;
  private ServiceRequest featureReq;
  private ServiceRequest resourceReq;
  private ServiceRequest amqpReq;
  private ServiceRequest echoReq;
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


  
    resourceReq = httpSvcs.processRequest(featureReq, "http://api.sportingsolutions.com/rels/resources/list", "Football");
    System.out.println("5-------------->" + resourceReq.getServiceRestItems().get(0).getName());
    System.out.println("5-------------->" + resourceReq.getServiceRestItems().get(0).getLinks().get(0).getHref());
    

    amqpReq = httpSvcs.processRequest(resourceReq, "http://api.sportingsolutions.com/rels/stream/amqp", "Fernando v Jim");
    System.out.println("6-------------->" + amqpReq.getServiceRestItems().get(0).getName());
    System.out.println("6-------------->" + amqpReq.getServiceRestItems().get(0).getLinks().get(0).getHref());

    
    
    
//    snapShotResponse = httpSvcs.getSnapshot(resourceReq, "http://api.sportingsolutions.com/rels/snapshot", "Fernando v Jim");
//    System.out.println("7----------------->" + snapShotResponse);
    
//    echoReq = httpSvcs.processRequest(resourceReq, "http://api.sportingsolutions.com/rels/stream/echo", "Fernando v Jim");
//    System.out.println("8--------------> echoRequest");
    
    
/*
    ConnectionFactory connectionFactory = new ConnectionFactory();
    URI amqpUri;

    try {
      amqpUri = new URI(amqpReq.getServiceRestItems().get(0).getLinks().get(0).getHref());

    
      connectionFactory.setRequestedHeartbeat(5);
  
      String host = amqpUri.getHost();
      connectionFactory.setHost(host);
      System.out.println("host: ------------------>" + host);
  
  
      
      
      String path = amqpUri.getRawPath();
      System.out.println("path: ------------------>" + path);

      String queue = path.substring(path.indexOf('/',1)+1);
      System.out.println("queue: ------------------>" + queue);

      
      String virtualHost = uriDecode(amqpUri.getPath().substring(1,path.indexOf('/',1)));
      System.out.println("virtualHost: ------------------>" + virtualHost);

      connectionFactory.setVirtualHost("/" + virtualHost);
      
      int port = amqpUri.getPort();
      System.out.println("virtualHost: ------------------>" + port);
      
      
      String userInfo = amqpUri.getRawUserInfo();
      userInfo = URLDecoder.decode(userInfo,"UTF-8");
      System.out.println("userInfo: ------------------>" + userInfo);
      if (userInfo != null) {
          String userPass[] = userInfo.split(":");
          System.out.println("userName: ------------------>" + userPass[0]);
          System.out.println("userPass: ------------------>" + userPass[1]);
          if (userPass.length > 2) {
              throw new IllegalArgumentException("Bad user info in AMQP " +
                                                 "URI: " + userInfo);
          }
          connectionFactory.setUsername(uriDecode(userPass[0]));

          if (userPass.length == 2) {
            connectionFactory.setPassword(uriDecode(userPass[1]));
          }
      }
      
      

      
      if (port != -1) {
        connectionFactory.setPort(port);
      }
      
      Connection connection = connectionFactory.newConnection();
  
      Channel channel = null;
      try {
        channel = connection.createChannel();
        System.out.println("----------------------> channel created");
      } catch (Exception ex) {
        System.out.println("----------------------> channel error" + ex);
      }
      
      
      
      
      QueueingConsumer consumer = new QueueingConsumer(channel);    
  
      
      //this is where the for loop going over all teh queues goes
      channel.basicConsume(queue, false, consumer);
      
      System.out.println("-------------open channel-->" + channel.isOpen());
  */    
  /*
   * Retrieving individual messages

To explicitly retrieve messages, use Channel.basicGet. The returned value is an instance of GetResponse, from which the header information (properties) and message body can be extracted:

boolean autoAck = false;
GetResponse response = channel.basicGet(queueName, autoAck);
if (response == null) {
    // No message retrieved.
} else {
    AMQP.BasicProperties props = response.getProps();
    byte[] body = response.getBody();
    long deliveryTag = response.getEnvelope().getDeliveryTag();
    ...
and since the autoAck = false above, you must also call Channel.basicAck to acknowledge that you have successfully received the message:

    ...
    channel.basicAck(method.deliveryTag, false); // acknowledge receipt of the message
}
 
   * 
   */
     /* 
      while (true) {
        echoReq = httpSvcs.processRequest(resourceReq, "http://api.sportingsolutions.com/rels/stream/echo", "Fernando v Jim");
        Thread.sleep(3000);
        QueueingConsumer.Delivery delivery = consumer.nextDelivery();
        String message = new String(delivery.getBody());
  
        System.out.println(" [x] Received '" + message + "'");   
//        doWork(message); 
        System.out.println(" [x] Done" );
  
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      }
  
    } catch (URISyntaxException ex) {
      System.out.println("Malformed AMQP URL" + ex);
    } catch (IOException ex) {
      System.out.println("Malformed AMQP URL" + ex);
    } catch (InterruptedException ex) {
      System.out.println("Malformed AMQP URL" + ex);
    }
  
  
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

        */

  }     

}
