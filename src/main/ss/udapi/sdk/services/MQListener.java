package ss.udapi.sdk.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.log4j.Logger;

import ss.udapi.sdk.model.ServiceRequest;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;


public class MQListener implements Runnable
{
  private static URI amqpURI;
  private static Logger logger = Logger.getLogger(MQListener.class);
  private Integer count;
  private static MQListener instance = null;
  private ServiceRequest resources;
  private static Channel channel;
  private static QueueingConsumer consumer;
  private static boolean MQListenerRunning = false;
  
  //TODO: this is the echo resource array but obviously has to be moved out as it's own class
  private ConcurrentMap queueMap = new ConcurrentHashMap();
      
  public MQListener (String amqpDest, ServiceRequest resources)
  {
    try {
      this.resources = resources;
      this.amqpURI = new URI(amqpDest);
      this.count = count;
    } catch (Exception ex) {
      logger.debug(ex);
    }
  }
  
  public static MQListener getMQListener(String amqpDest, ServiceRequest resources)
  {
    System.out.println("----------getting LIstener>" );
    if (instance == null)
    {
      System.out.println("----------getting LIstener>");
      instance = new MQListener(amqpDest, resources);
    } 

    String path = amqpURI.getRawPath();
    String queue = path.substring(path.indexOf('/',1)+1);
/*    try {
      System.out.println ("basicConsume---------------------------->" + channel.basicConsume(queue, true, consumer) );
    } catch (IOException ex){
      logger.debug(ex);
    }      
*/

    return instance;
  }
  
  
  @Override
  public void run()
  {
    MQListenerRunning = true;
    try {
      ConnectionFactory connectionFactory = new ConnectionFactory();
  
      connectionFactory.setRequestedHeartbeat(5);
      
      String host = amqpURI.getHost();
      connectionFactory.setHost(host);
      
      String path = amqpURI.getRawPath();
  
      String queue = path.substring(path.indexOf('/',1)+1);
      System.out.println("------------------>Queue>" + queue);
  
      
      String virtualHost = uriDecode(amqpURI.getPath().substring(1,path.indexOf('/',1)));
  
      connectionFactory.setVirtualHost("/" + virtualHost);
      
      int port = amqpURI.getPort();
      
      
      String userInfo = amqpURI.getRawUserInfo();
      userInfo = URLDecoder.decode(userInfo,"UTF-8");
      if (userInfo != null) {
          String userPass[] = userInfo.split(":");
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
  
      channel = connection.createChannel();
      consumer = new QueueingConsumer(channel);    

      System.out.println ("basicConsume---------------------------->" + channel.basicConsume(queue, true, consumer) );      

      while (true) {
        Delivery delivery = consumer.nextDelivery();
            
        
            
        String message = new String(delivery.getBody());
        System.out.println("-----------------Message Received> '" + message + "'");   
        
        count ++;
        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
      }

    
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
  }     

  
  
  public boolean isRunning()
  {
    return MQListenerRunning;
  }
  
}
