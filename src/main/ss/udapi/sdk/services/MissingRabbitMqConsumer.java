package ss.udapi.sdk.services;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class MissingRabbitMqConsumer extends DefaultConsumer
{

  /*
   *  A consumer for rabbitMq message queueing system, because the rabbitMq message queueing system does not come 
   *  with a single method to enable dequeuing of any messages put in the message queueing system.
   *  A bit of an oversight for a message queueing system not to provide the means to do anything with the messages 
   *  once they go into the queue if I'm honest!
   *  
   *  Perhaps I'm the first person ever who has had the need to read off a queue in the qabbitMq message queueing system,
   *  in that case I can understand how they missed this glaring basic functionality off, but otherwise I must admit I'm a bit confused.
   *  
   *  Fern
   * 
   */
  
  private String cTag;
  private String body;
  
  private static Logger logger = Logger.getLogger(MissingRabbitMqConsumer.class);
  private EchoResourceMap echoMap = EchoResourceMap.getEchoMap();
  
  public MissingRabbitMqConsumer(Channel channel)
  {
    super(channel);
    
  }

  
  //TODO: need to test it with 1000s of features, not sure how well the AMQP code scales up.  Judging by the fact that provision of a simple consumer is not within scope
  //i would rather see the performance myself rather than rely on what they say.  All this starting up a new thread per set of deliveries might be bad, or it might work if the
  //object has a timeout before it kills it self
  @Override public void handleDelivery(String cTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bodyByteArray) throws IOException
  {
    this.cTag = cTag;
    body = new String(bodyByteArray);
    
    logger.debug("---------------->In the consumer" + body.substring(0,100) + " ----- " + cTag);
    
    String msgHead = body.substring(0, 64);
//if things slow down parsing the header change to this which doesn't look as tidy
//    if ((message.substring(0, 64).equals("{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",")))
    if (msgHead.equals("{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\","))
    {
      echoMap.decrEchoCount(CtagResourceMap.getResource(cTag));

    } else {
      logger.debug("-------------------------->NAY:  " + msgHead);
      WorkQueue myQueue = WorkQueue.getWorkQueue();
      myQueue.addTask(body);
    }

    
  }
  
  
}
