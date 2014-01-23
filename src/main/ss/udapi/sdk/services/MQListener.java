package ss.udapi.sdk.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

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
  private ServiceRequest resources;
  private int count;
  private HashMap<String,String> resourceChannMap = new HashMap<String,String>();

  private static Logger logger = Logger.getLogger(MQListener.class);
  private static MQListener instance = null;
  private static Channel channel;
  private static QueueingConsumer consumer;
  private static boolean MQListenerRunning = false;

  
  
  //TODO: this is the echo resource array but obviously has to be moved out as it's own class
  private ConcurrentMap queueMap = new ConcurrentHashMap();
      
  private MQListener ()
  {
  }
  
  public static MQListener getMQListener()
  {
    logger.debug("Retrieving listener or create it if it doesn't exist");
    if (instance == null)
    {
      instance = new MQListener();  //not needed now
    } 
    return instance;
  }
  
 
  /*
  public MQListener getMQListener(String amqpDest, ServiceRequest resources)
  {
    logger.debug("Retrieving listener or create it if it doesn't exist");
    if (instance == null)
    {
      instance = new MQListener(amqpDest, resources);
    } 

    try {
      this.resources = resources;
      this.amqpURI = new URI(amqpDest);
    } catch (Exception ex) {
      logger.debug(ex);
    }
    
    return instance;
  }
  */
  
  @Override
  public void run()
  {
    if (MQListenerRunning == false){
  
      try {
        ConnectionFactory connectionFactory = new ConnectionFactory();
    
        connectionFactory.setRequestedHeartbeat(5);
        
        String host = amqpURI.getHost();
        connectionFactory.setHost(host);
        
        String path = amqpURI.getRawPath();
    
        String queue = path.substring(path.indexOf('/',1)+1);

    
        
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
        channel.basicQos(0, 10, false);
        consumer = new QueueingConsumer(channel);    

  
        //add the ctag to array to keep track of which queue is for which response
        String ctag=channel.basicConsume(queue, true, consumer);
        logger.debug("--------------------->Initial basic consumer " + ctag + " added for queue " + queue);
        
        MQListenerRunning = true;

        
        
        while (true) {
          Delivery delivery = consumer.nextDelivery();
          if(delivery != null){    
            String message = new String(delivery.getBody());
            logger.debug("----------------->Message Received> [" + message.substring(0, 40) + "]");   

            
            String msgHead = message.substring(0, 64);
//if things slow down parsing the header change to this which doesn't look as tidy
//            if ((message.substring(0, 64).equals("{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",")))
            if (msgHead.equals("{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\","))
            {
              //TODO: echo processing
              System.out.println("-------------------------->ADD ECHO PROCESSING:  " + msgHead);
            } else {
              System.out.println("-------------------------->NAY:  " + msgHead);
              WorkQueue myQueue = WorkQueue.getWorkQueue();
              myQueue.addTask(message);
            }
            
            
            

          }
        }
      
      } catch (IOException ex) {
        System.out.println("Malformed AMQP URL" + ex);
      } catch (InterruptedException ex) {
        System.out.println("Malformed AMQP URL" + ex);
      } catch (Exception ex) {
        ex.printStackTrace();
      }

    }

    
  }

  public void addQueue(String newAmqpDest, ServiceRequest ewResources, String resourceId)
  {

    try {
      URI newAmqpURI = new URI(newAmqpDest);
      String path = amqpURI.getRawPath();
      String queue = path.substring(path.indexOf('/',1)+1);
      
      String ctag=channel.basicConsume(queue, true, consumer);
      resourceChannMap.put(resourceId, ctag);
      
      logger.debug("--------------------->Additional basic consumer " + ctag + " added for resource " + resourceId);
    } catch (IOException ex) {
      logger.debug(ex);
    } catch (URISyntaxException ex) {
      logger.error("Queue name corrupted. It would have been checked by now so something bad happened: " + newAmqpDest);
    }
  }
  
  public void reconnect (String resourceId, String amqpDest)
  {
    try {
      URI newAmqpURI = new URI(amqpDest);
      String path = amqpURI.getRawPath();
      String queue = path.substring(path.indexOf('/',1)+1);

      channel.basicCancel(resourceChannMap.get(resourceId));
      //TODO: change the names around, the grammar is ugly
      logger.debug("--------------------->Basic consumer " + resourceChannMap.get(resourceId) + " for resource " + resourceId + " disconnected");

      String ctag=channel.basicConsume(queue, true, consumer);
      resourceChannMap.put(resourceId, ctag);
      logger.debug("--------------------->Basic consumer " + ctag + " reconnected for resource " + resourceId);

      
    } catch (IOException ex) {
      logger.debug(ex);
    } catch (URISyntaxException ex) {
      logger.error("Queue name corrupted. It would have been checked by now so something bad happened: " + amqpDest);
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

  
  public void setResources(String amqpDest, ServiceRequest resources)
  {
    try {
      this.amqpURI = new URI(amqpDest);
    } catch (Exception ex) {
      logger.debug(ex);
    }
    this.resources = resources;
  }

  
}
