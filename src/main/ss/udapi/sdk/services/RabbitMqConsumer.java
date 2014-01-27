package ss.udapi.sdk.services;

import java.io.IOException;

import org.apache.log4j.Logger;

import ss.udapi.sdk.ResourceImpl;
import ss.udapi.sdk.interfaces.Resource;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class RabbitMqConsumer extends DefaultConsumer
{

  private String cTag;
  private String body;
  
  private static Logger logger = Logger.getLogger(RabbitMqConsumer.class);
  private EchoResourceMap echoMap = EchoResourceMap.getEchoMap();
  
  public RabbitMqConsumer(Channel channel)
  {
    super(channel);
    
  }

  
  
//THROW DISCONNECT EVENT ON: handleCancel, handleCancelOk 
  
  @Override public void handleDelivery(String cTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bodyByteArray) throws IOException
  {
    this.cTag = cTag;
    body = new String(bodyByteArray);
    
    logger.debug("---------------->In the consumer" + body.substring(0,100) + " ----- " + cTag);
    
    String msgHead = body.substring(0, 64);
    if (msgHead.equals("{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\","))
    {
      echoMap.decrEchoCount(CtagResourceMap.getResource(cTag));

    } else {
      logger.debug("-------------------------->NAY:  " + msgHead);
      WorkQueue myQueue = WorkQueue.getWorkQueue();
      myQueue.addTask(body);
    }
  }

  
  @Override
  public void handleCancelOk(String cTag)
  {
    this.cTag = cTag;
    String resourceId = CtagResourceMap.getResource(cTag);
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(resourceId);
    resource.mqDisconnectEvent();
  }

  @Override
  public void handleCancel(String cTag)
  {
    this.cTag = cTag;
    String resourceId = CtagResourceMap.getResource(cTag);
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(resourceId);
    resource.mqDisconnectEvent();
  }
  
}
