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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReentrantLock;

import javax.management.BadAttributeValueExpException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.*;

/* Initiates connectivity to RabbitMQ and maintains corresponding queue listeners as new fixtures/resources are
 * created(added)/destroyed(deleted).
 */
public class MQListener implements Runnable
{
  private static Logger logger = Logger.getLogger(MQListener.class);

  //Maps a resource to a specific cTag (which is in effect a queue listener). 
  private static HashMap<String,String> resourceChannMap = new HashMap<String,String>();
  private static MQListener instance = null;
  private static Channel channel;
  private static RabbitMqConsumer consumer;
  private static ConcurrentLinkedQueue<ResourceSession> resourceSessionList = new ConcurrentLinkedQueue<ResourceSession>();
  private static ReentrantLock mqListenerLock = new ReentrantLock();

  private ResourceSession session = null;
  private URI resourceQURI = null;
  private String path = null;
  private String queue = null;
  private String ctag = null;
  
  private static final String THREAD_NAME = "MQ_Listener_Thread";
  private static final int CONNECT_RETRIES = 5;
  
  private static boolean terminate = false;

  
  private MQListener ()
  {
  }
  
  
  
  public synchronized static MQListener getMQListener(String amqpDest) {
    /* This lock ensures there cannot be multiple instantiations which can lead to a corrupt object without synchronization,
     * which in turn cannot be done on a here as the access is static.
     */
    try {
      mqListenerLock.lock();
      logger.debug("Retrieving MQListener or create it if it doesn't exist");
      if (instance == null) {
        instance = new MQListener(); 
      } 
    } catch (Exception ex) {
      logger.error("Could not initialiaze MQ Listener.");
      throw new MissingResourceException("Service threadpool has become corrupted", "ss.udapi.sdk.services.ActionThreadExecutor", "MQListener");
    } finally {
      mqListenerLock.unlock();
    }
    return instance;
  }
  
  
  
  @Override
  public void run() {
    terminate = false;
    /*
     * This section happens only once when the thread is kicked off if the channel (read connection) doesn't exist
     * or if the channel has died.
     */
    try {
      if (channel == null) {
        logger.debug("MQ Channel not open");
      } else {
        logger.debug("MQ Channel status: " + channel.isOpen());
      }
      
      if ((channel == null)  || (channel != null && (channel.isOpen() == false))) {
        //Set the MQ URL.
        session = resourceSessionList.remove();
        resourceQURI = new URI(session.getAmqpDest());
        path = resourceQURI.getRawPath();
        queue = path.substring(path.indexOf('/',1)+1);
        String userInfo = resourceQURI.getRawUserInfo();
        
        //Set up the connection.
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
        
        //Start up the connection
        Connection connection;
        try {                                                   
          connection = connectionFactory.newConnection();
        } catch (IOException ex) {
          throw new IOException("Failure creating connection factory");
        }
    
        /* And create a consumer using the first queue.  This consumer allows subsequent queue listeners to be added and removed
         * as resources are created / deleted.
         */
        boolean connectSuccess = false;
        for (int retries=1; retries<=CONNECT_RETRIES; retries++) {
          if (connectSuccess == false) {
            logger.info("Attempting new connection to MQ...");

            try {
              channel = connection.createChannel();
              channel.basicQos(0, 10, false);
  
              consumer = new RabbitMqConsumer(channel);
              //Create a queue listener for the first fixure.
              ctag=channel.basicConsume(queue, true, consumer);
              connectSuccess = true;
            } catch (IOException ex) {
              connectSuccess = false;
              //we're catching this and ignoring it during the retries
            }

          }
        }
        if (connectSuccess == false) {
          disconnect(session.getResourceId());
          throw new IOException("Failure creating channel");
        }
        logger.info("Connection made to MQ");
        String resourceId = session.getResourceId();
        
        /* A map to used to keep a tally of which queue listeners (cTag) have been created and to disconnect later on 
         * when all we get is the resource Id.  Disconnection can only happen via a cTag.
         */
        resourceChannMap.put(resourceId, ctag);
    
        /* A map used by RabbitMqConsumer to tie a cTag (which is all it gets from RabbitMq) to identify which resource an echo
         * response came in for.
         */
        CtagResourceMap.addCtag(ctag, resourceId);
        EchoResourceMap.getEchoMap().addResource(resourceId);
        logger.info("Initial basic consumer " + ctag + " added for queue " + queue + "for resource " + resourceId);
      }

      /*
       * This section is the loop which uses the connection opened above and adds additional consumers as they are requested.
       * The two maps are also updated here.  This loop constantly monitors resourceSessionList for any new pending additions
       * to the number of active queue listeners.  Could have used an observer to add more listeners, but that happens very
       * infrequently compared to the lifetime of the running program, this is simpler for the return you get.
       */
      Thread.currentThread().setName(THREAD_NAME);
        while (channel.isOpen()) {
          while (resourceSessionList.isEmpty() == false) {
            session = resourceSessionList.remove();
            try {
              resourceQURI = new URI(session.getAmqpDest());
              path = resourceQURI.getRawPath();
              queue = path.substring(path.indexOf('/',1)+1);
              
              String resourceId = session.getResourceId();
              
              if (resourceChannMap.containsKey(resourceId) == false) {
                ctag=channel.basicConsume(queue, true, consumer);
                resourceChannMap.put(resourceId, ctag);
                CtagResourceMap.addCtag(ctag, resourceId);
                EchoResourceMap.getEchoMap().addResource(resourceId);
                logger.info("Additional basic consumer " + ctag + " added for queue " + queue + "for resource " + resourceId);
              }

              if (terminate == true) {
                return;
              }
            } catch (IOException ex) {
              logger.error("Failure creating additional basic consumer for : " + session.getResourceId());
            } catch (URISyntaxException ex) {
              logger.error("Queue name corrupted: " + session.getAmqpDest());
            }
          }
          Thread.sleep(1000);
        } 
      // for java 1.7 this syntax is preferable: catch(URISyntaxException | UnsupportedEncodingException ex)
    } catch(AlreadyClosedException ex){
      logger.error("The amqp channel is closed: " + ex.getMessage());	
    } catch(URISyntaxException ex) {
      logger.error ("URI: " + session.getAmqpDest() + " for session: " + session + " is not valid.");
      ex.printStackTrace();
    } catch( UnsupportedEncodingException ex) {
      logger.error ("URI: " + session.getAmqpDest() + " for session: " + session + " is not valid.");
      ex.printStackTrace();
    } catch(BadAttributeValueExpException ex) {
      logger.error (ex.getMessage());
    } catch(IOException ex) {
      logger.error ("Connection creation failure: " + ex.getMessage());
    } catch(InterruptedException ex) {
      logger.warn("MQListener was awoken from it's sleep.  No further action required, but what caused it?");
    }
  }

  
  
  /* When we get a disconnect event, either called by the client code or when the maximum of missing echo responses is reached
   * we close this channel.  This causes a handleCancel event which MQListener receives which in turn calls the associated
   * ResourceImpl to notify the client code about the disconnect event. 
   */
  public static void disconnect (String resourceId) {
	String consumerTag = resourceChannMap.get(resourceId);  
    if (consumerTag == null) {
      logger.debug("Basic consumer for resource " + resourceId + " has already disconnected.");
    } else {
      try {
        channel.basicCancel(consumerTag);
        logger.info("Disconnected basic consumer " + consumerTag + " for resource " + resourceId);
      } catch (Exception ex) {
        logger.debug("Could not disconnect basic consumer " + consumerTag + " for resource " + resourceId);
      }
    }
  }
  
  
  
  /* After the disconnect event notification is sent to the client MQListener calls this to remove the resource/cTag
   * mappings.  Bit of housekeeping really.
   */
  protected static void removeMapping(String cTag) {
    EchoResourceMap.getEchoMap().removeResource(CtagResourceMap.getResource(cTag));
    logger.debug("cTag" + cTag + "no longer valid.");
    resourceChannMap.remove(CtagResourceMap.getResource(cTag));
    CtagResourceMap.removeCtag(cTag);
  }



  /* Adds a new request to initiate a queue listener for a newly created fixture/resource.  The loop in run() above
   * will pick this up and crete the listner there.  
   */
  public static void setResources(ResourceSession resourceSession) {
    resourceSessionList.add(resourceSession);
    logger.debug("Adding new resource queue listener request for: " + resourceSession.getAmqpDest());
  }
  
  
  
  //Hook for unit testing
  protected int countPendingResources() {
    return resourceSessionList.size();
  }
  
  
  //Clean up the path.
  private static String uriDecode(String s) {
    try {
      // URLDecode decodes '+' to a space, as for form encoding.  So protect plus signs.
      return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
    }
    catch (java.io.UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
  }     

  //for unit testing
  public static void terminate() {
    terminate = true;
  }
  
}
