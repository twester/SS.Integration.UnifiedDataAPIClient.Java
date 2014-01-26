package ss.udapi.sdk.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import ss.udapi.sdk.model.ServiceRequest;









import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class MQListener implements Runnable
{
  private static URI amqpURI;
  private ServiceRequest resources;
  private String queue;
  private int count;
  private static HashMap<String,String> resourceChannMap = new HashMap<String,String>();

  private static Logger logger = Logger.getLogger(MQListener.class);
  private static MQListener instance = null;
  private static Channel channel;
  private static MissingRabbitMqConsumer consumer;
  private static boolean MQListenerRunning = false;
  
  private static Lock creationLock = new ReentrantLock();
  private static Lock queueCreationLock = new ReentrantLock();
  private static Lock setterLock = new ReentrantLock();
  
  private static ConcurrentLinkedQueue<ResourceSession> resourceSessionList = new ConcurrentLinkedQueue<ResourceSession>();

  
  
  private MQListener ()
  {
  }
  
  public static MQListener getMQListener(String amqpDest, ServiceRequest resources)
  {
    while(!creationLock.tryLock())
    {
    }
    try {
      logger.debug("------------------------->Only called once right?");
      creationLock.lock();
      logger.debug("Retrieving listener or create it if it doesn't exist");
      if (instance == null)
      {
        instance = new MQListener(); 
        
      } 
      return instance;
    } finally {
      System.out.println("---------------> all done");
      creationLock.unlock();
    }
  }
 
  
  @Override
  public void run()
  {
    MQListenerRunning  = true;
      try {
            try {
              
              ResourceSession session = resourceSessionList.remove();


                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setRequestedHeartbeat(5);

                
                URI newAmqpURI = new URI(session.getAmqpDest());
                String path = newAmqpURI.getRawPath();
                String queue = path.substring(path.indexOf('/',1)+1);
                
                String host = newAmqpURI.getHost();
                connectionFactory.setHost(host);
            
                String virtualHost = uriDecode(newAmqpURI.getPath().substring(1,path.indexOf('/',1)));
                connectionFactory.setVirtualHost("/" + virtualHost);
                
                int port = newAmqpURI.getPort();
                
                
                String userInfo = newAmqpURI.getRawUserInfo();
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
                consumer = new MissingRabbitMqConsumer(channel);  
                

          
                //add the ctag to array to keep track of which queue is for which response
                String ctag=channel.basicConsume(queue, true, consumer);
                resourceChannMap.put(session.getResourceId(), ctag);
                CtagResourceMap.addCtag(ctag, session.getResourceId());
                logger.debug("--------------------->Initial basic consumer " + ctag + " added for queue " + queue + "for resource " + session.getResourceId());

              
              
                
              
            } catch (IOException ex) {
              System.out.println("Malformed AMQP URL" + ex);
            } catch (Exception ex) {
              logger.debug(ex);
            }
        

        
        
      
      


        while (true) {

          while (resourceSessionList.isEmpty() == false) {
          

            ResourceSession session = resourceSessionList.remove();
          
            logger.debug("------------------>" + session.getAmqpDest());
           
            try {

              URI newAmqpURI = new URI(session.getAmqpDest());
              String path = newAmqpURI.getRawPath();
              String queue = path.substring(path.indexOf('/',1)+1);
              
              if (resourceChannMap.containsKey(session.getResourceId()) == false)
              {
                String ctag=channel.basicConsume(queue, true, consumer);
                resourceChannMap.put(session.getResourceId(), ctag);
                CtagResourceMap.addCtag(ctag, session.getResourceId());
                logger.debug("--------------------->Additional basic consumer " + ctag + " added for queue " + queue + "for resource " + session.getResourceId());
              }
            } catch (IOException ex) {
              logger.debug(ex);
            } catch (URISyntaxException ex) {
              logger.error("Queue name corrupted. It would have been checked by now so something bad happened: " + session.getAmqpDest());
            }

          }

          Thread.sleep(1000);

          
        }
      
//      } catch (InterruptedException ex) {
//        System.out.println("Malformed AMQP URL" + ex);
      } catch (Exception ex) {
        ex.printStackTrace();
      }

    
//    }
    
  }

  
  public static void disconnect (String resourceId, String amqpDest)
  {
    try {
      URI newAmqpURI = new URI(amqpDest);
      String path = amqpURI.getRawPath();
      String queue = path.substring(path.indexOf('/',1)+1);

      channel.basicCancel(resourceChannMap.get(resourceId));
      //TODO: raise disconnect event
      //TODO: change the names around, the grammar is ugly

      CtagResourceMap.removeCtag(resourceChannMap.get(resourceId));
      resourceChannMap.remove(resourceId);
      logger.debug("--------------------->Basic consumer " + resourceChannMap.get(resourceId) + " for resource " + resourceId + " disconnected");

      
      
/*      String ctag=channel.basicConsume(queue, true, consumer);
      resourceChannMap.put(resourceId, ctag);
      logger.debug("--------------------->Basic consumer " + ctag + " reconnected for resource " + resourceId);
*/
      
    } catch (IOException ex) {
      logger.debug(ex);
    } catch (URISyntaxException ex) {
      logger.error("Queue name corrupted. It would have been checked by now so something bad happened: " + amqpDest);
    }
    
  }
  
  
  
  private static String uriDecode(String s) {
    try {
        // URLDecode decodes '+' to a space, as for
        // form encoding.  So protect plus signs.
        return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
    }
    catch (java.io.UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
  }     

  
  public static boolean isRunning()
  {
    return MQListenerRunning;
  }

  
  public static void setResources(ResourceSession resourceSession)
  {
    try {
      resourceSessionList.add(resourceSession);
      logger.debug("----------------->adding new entry" + resourceSession.getAmqpDest() + resourceSessionList.size());
    } finally {

    }
    
  }

  
}
