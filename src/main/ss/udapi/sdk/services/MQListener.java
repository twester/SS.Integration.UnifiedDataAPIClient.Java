package ss.udapi.sdk.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.apache.log4j.Logger;

import ss.udapi.sdk.model.ServiceRequest;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;


public class MQListener implements Runnable
{
  private URI amqpURI;
  private Logger logger = Logger.getLogger(MQListener.class);
  private Integer count;
  private ServiceRequest request;
      
  public MQListener (String amqpDest, ServiceRequest request)
  {
    try {
      this.request = request;
      this.amqpURI = new URI(amqpDest);
      this.count = count;
    } catch (Exception ex) {
      logger.debug(ex);
    }
  }
  
  @Override
  public void run()
  {
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
  
      Channel channel = connection.createChannel();
      QueueingConsumer consumer = new QueueingConsumer(channel);    
      
      
      //this is where the for loop going over all the queues goes
      channel.basicConsume(queue, false, consumer);
      try {
        channel.basicConsume(queue, false, consumer);
      } catch (Exception ex) {
        System.out.println("------------------>Connection5 ex " + ex);
      }

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

  
}
