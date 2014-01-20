package ss.udapi.sdk.services;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;

import org.apache.log4j.Logger;
import org.apache.log4j.NDC;









import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.QueueingConsumer.Delivery;



public class MQServices
{
  private static Logger logger = Logger.getLogger(MQServices.class);
  
  private ConnectionFactory connectionFactory;
  private Connection connection;
  private Channel channel;
  private QueueingConsumer consumer;
  
  private String queueName;
  private String virtualHost;

  private Boolean success = false;

  private Thread echoThread;
  
  public ConnectionFactory connectToStream(String streamURI)
  {

    try {
      
      
      connectionFactory = new ConnectionFactory();

      
      System.out.println("--------------->" + SystemProperties.get("ss.connheartbeat"));
      
      URI amqpUri = new URI(streamURI);

      connectionFactory.setRequestedHeartbeat(Integer.parseInt(SystemProperties.get("ss.connheartbeat")));

      String host = amqpUri.getHost();
      connectionFactory.setHost(host);

      int port = amqpUri.getPort();
      connectionFactory.setPort(port);

      
      
      
      
      String userInfo = amqpUri.getRawUserInfo();
      
      
      userInfo = URLDecoder.decode(userInfo,"UTF-8");

      
      if (userInfo != null) {
          String userPass[] = userInfo.split(":");
          if (userPass.length > 2) {
              throw new IllegalArgumentException("malformed credentials in URI: " + userInfo);
          }
          connectionFactory.setUsername(uriDecode(userPass[0]));

          if (userPass.length == 2) {
            connectionFactory.setPassword(uriDecode(userPass[1]));
          }
      }
      
      String path = amqpUri.getRawPath();
      if (path != null && path.length() > 0) {
        queueName = path.substring(path.indexOf('/',1)+1);
        virtualHost = uriDecode(amqpUri.getPath().substring(1,path.indexOf('/',1)));
          connectionFactory.setVirtualHost("/" + virtualHost);
      }
      
      
      
      if(channel != null){
        channel.close();
        channel = null;
      }
      
      if(connection != null){
        connection.close();
        connection = null;
      }
      connection = connectionFactory.newConnection();
      logger.info(String.format("Successfully connected to Streaming Server for "));
      
      
      Delivery output = null; 
      
      consumer = new QueueingConsumer(channel){
        @Override
        public void handleCancelOk(String consumerTag){
          super.handleCancelOk(consumerTag);
          dispose();
        }
      };

      
      
      while (output == null) {
        consumer.nextDelivery();
      }
      
      
      
      
//      byte[] message = output.getBody();
      
//      String messageString = new String(message);
      
//      System.out.println("----------->" + messageString);
      
//      StartEcho();
      
//      actionExecuter.execute(new ConnectedAction(streamingEvents));
      
/*      channel = connection.createChannel();
      consumer = new QueueingConsumer(channel){
        @Override
        public void handleCancelOk(String consumerTag){
          super.handleCancelOk(consumerTag);
          dispose();
        }
      };
      
      channel.basicConsume(queueName,true,consumer);
      channel.basicQos(0, 10, false);
      success = true;
  */    

      

    
  
        
    } catch (URISyntaxException ex) {
      logger.error("Malformed or missing MQ URI: [" + streamURI + "]");
    } catch (UnsupportedEncodingException ex) {
      logger.error("Malformed user credentials");
    } catch (IllegalArgumentException ex) {
      logger.error(ex.getMessage());
    } catch (IOException ex) {

    } catch (InterruptedException ex) {
      
    }

    return connectionFactory;
    
  }

  
  public void streamData(ConnectionFactory connFactory)
  {
    
  }
  
  
  
  
  
  
  
  
  
  
  
  
  private void dispose(){
    logger.info(String.format("Streaming stopped for %1$s"));
    try{
      if(channel != null){
        channel.close();
        channel = null;
      }
      
      if(connection != null){
        connection.close();
        connection = null;
      }
      
      if(echoThread != null){
        echoThread = null;
      }
    }catch(IOException ex){
      logger.error(String.format("Problem while trying to shutdown stream for "));
    }
    //actionExecuter.execute(new DisconnectedAction(streamingEvents));
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
