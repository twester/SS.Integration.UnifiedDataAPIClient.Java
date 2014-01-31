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


package ss.udapi.sdk.services;

import ss.udapi.sdk.model.ServiceRequest;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.*;

import javax.management.BadAttributeValueExpException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.*;

public class MQListener implements Runnable
{
  private static Logger logger = Logger.getLogger(MQListener.class);
  private static HashMap<String,String> resourceChannMap = new HashMap<String,String>();
  private static MQListener instance = null;
  private static Channel channel;
  private static RabbitMqConsumer consumer;
  private static boolean MQListenerRunning = false;
  private static Lock creationLock = new ReentrantLock();
  private static ConcurrentLinkedQueue<ResourceSession> resourceSessionList = new ConcurrentLinkedQueue<ResourceSession>();

  
  private MQListener ()
  {
  }
  
  
  
  public static MQListener getMQListener(String amqpDest) {
    while(!creationLock.tryLock())
    {}

    try {
      creationLock.lock();
      logger.debug("Retrieving MQListener or create it if it doesn't exist");
      if (instance == null) {
        instance = new MQListener(); 
      } 
      return instance;
    } finally {
      creationLock.unlock();
    }
  }
  
  
  
  @Override
  public void run() {
    ResourceSession session = null;
    MQListenerRunning  = true;
    
    /* 
     * This section happens only once when the thread is kicked off
     */
    try {
      session = resourceSessionList.remove();
      URI resourceQURI = new URI(session.getAmqpDest());
      String path = resourceQURI.getRawPath();
      String queue = path.substring(path.indexOf('/',1)+1);
      String userInfo = resourceQURI.getRawUserInfo();
      
      ConnectionFactory connectionFactory = new ConnectionFactory();
      connectionFactory.setRequestedHeartbeat(Integer.parseInt(SystemProperties.get("ss.conn_heartbeat")));
      String host = resourceQURI.getHost();
      connectionFactory.setHost(host);
      String virtualHost = uriDecode(path.substring(1,path.indexOf('/',1)));
      connectionFactory.setVirtualHost("/" + virtualHost);

      userInfo = URLDecoder.decode(userInfo,"UTF-8");
      if (userInfo != null) {
          String userPass[] = userInfo.split(":");
          if (userPass.length > 2) {
              throw new BadAttributeValueExpException("Invalid user details format in AMQP URI: " + userInfo);
          }
          connectionFactory.setUsername(uriDecode(userPass[0]));
          if (userPass.length == 2) {
            connectionFactory.setPassword(uriDecode(userPass[1]));
          }
      }
      
      int port = resourceQURI.getPort();
      if (port != -1) {
        connectionFactory.setPort(port);
      }
      
      Connection connection;
      try {
        connection = connectionFactory.newConnection();
      } catch (IOException ex) {
        throw new IOException("Failure creating connection factory");
      }

      String ctag;
      try {
        channel = connection.createChannel();
        channel.basicQos(0, 10, false);
        consumer = new RabbitMqConsumer(channel);  
        ctag=channel.basicConsume(queue, true, consumer);
      } catch (IOException ex) {
        throw new IOException("Failure creating channel");
      }
      resourceChannMap.put(session.getResourceId(), ctag);
      CtagResourceMap.addCtag(ctag, session.getResourceId());
      logger.info("Initial basic consumer " + ctag + " added for queue " + queue + "for resource " + session.getResourceId());

      /*
       * This section is the loop which uses the connection opened above and adds additional consumers as they are requested
       */
      while (true) {
        while (resourceSessionList.isEmpty() == false) {
          session = resourceSessionList.remove();
          try {
            resourceQURI = new URI(session.getAmqpDest());
            path = resourceQURI.getRawPath();
            queue = path.substring(path.indexOf('/',1)+1);
            
            if (resourceChannMap.containsKey(session.getResourceId()) == false) {
              ctag=channel.basicConsume(queue, true, consumer);
              resourceChannMap.put(session.getResourceId(), ctag);
              CtagResourceMap.addCtag(ctag, session.getResourceId());
              logger.info("Additional basic consumer " + ctag + " added for queue " + queue + "for resource " + session.getResourceId());
            }
          } catch (IOException ex) {
            logger.debug(ex);
          } catch (URISyntaxException ex) {
            logger.error("Queue name corrupted. It would have been checked by now so something bad happened: " + session.getAmqpDest());
          }
        }
        Thread.sleep(1000);
      } 

 /* for java 1.7 this syntax is preferable
  *  } catch(URISyntaxException | UnsupportedEncodingException ex) {
  *    logger.error ("URI: " + session.getAmqpDest() + " for session: " + session + " is not valid.");
  *    ex.printStackTrace();
  * 
  */
      
    } catch(URISyntaxException ex) {
      logger.error ("URI: " + session.getAmqpDest() + " for session: " + session + " is not valid.");
      ex.printStackTrace();
    } catch( UnsupportedEncodingException ex) {
      logger.error ("URI: " + session.getAmqpDest() + " for session: " + session + " is not valid.");
      ex.printStackTrace();
    } catch(BadAttributeValueExpException ex) {
      logger.error (ex.getMessage());
    } catch(IOException ex) {
      logger.error ("Connection creation failure:" + ex.getMessage());
    } catch(InterruptedException ex) {
      logger.warn("MQListener was awoken from it's sleep.  No further action required, but what caused it?");
    }
  }

  
  
  public static void disconnect (String resourceId) {
    try {
      channel.basicCancel(resourceChannMap.get(resourceId));
      logger.info("Disconnecting basic consumer " + resourceChannMap.get(resourceId) + " for resource " + resourceId);
    } catch (IOException ex) {
      logger.error("Could not disconnect basic consumer " + resourceChannMap.get(resourceId) + " for resource " + resourceId);
    }
    
  }
  
  
  protected static void removeMapping(String cTag)
  {
    resourceChannMap.remove(CtagResourceMap.getResource(cTag));
    CtagResourceMap.removeCtag(cTag);
  }
  
  
  private static String uriDecode(String s) {
    try {
      // URLDecode decodes '+' to a space, as for form encoding.  So protect plus signs.
      return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
    }
    catch (java.io.UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
  }     

  
  
  public static boolean isRunning() {
    return MQListenerRunning;
  }

  
  
  public static void setResources(ResourceSession resourceSession) {
    resourceSessionList.add(resourceSession);
    logger.debug("Adding new resource queue listener request for: " + resourceSession.getAmqpDest());
  }
  
}
