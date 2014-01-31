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

import ss.udapi.sdk.ResourceImpl;

import org.apache.log4j.Logger;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;


/* 
 * A listener providing call back methods which RabbitMQ uses to notify the SDK of an event.  The only ones we care about are:
 * 1) Message received.
 * 2) Connection lost.
 */
public class RabbitMqConsumer extends DefaultConsumer
{
  private static Logger logger = Logger.getLogger(RabbitMqConsumer.class);
  private EchoResourceMap echoMap = EchoResourceMap.getEchoMap();

  
  public RabbitMqConsumer(Channel channel){
    super(channel);
    
  }

  
  //Message received from MQ
  @Override
  public void handleDelivery(String cTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bodyByteArray) {

    //Get the message header
    String body = new String(bodyByteArray);
    String msgHead = body.substring(0, 64);
    logger.debug("Consumer: " + cTag + " received message: " + msgHead);
    
    /* If it's not an echo assign the work to a the queue's resource implementation (fixture handler).
     * The resource is retrieved from CtagResourceMap which is maintained by MQListener (as it creates/destroys queue listeners). */
    if (msgHead.equals("{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",") != true) {
      WorkQueue myQueue = WorkQueue.getWorkQueue();
      myQueue.addTask(body);
    } else {
      if (CtagResourceMap.getResource(cTag).equals("4x0lAft_P7JnfqLK0J4o1y_Rgtg")){
        System.out.println("--------------->Disregarding echo response for 4x0lAft_P7JnfqLK0J4o1y_Rgtg, Fernando v Jim");

        echoMap.resetEchoCount(CtagResourceMap.getResource(cTag));

      } else {
        //TODO: move this outside the if once we finish testing the echo failure
        echoMap.resetEchoCount(CtagResourceMap.getResource(cTag));

      }

    }

    //We sucessfully got an echo response or wome work from a queue, so the queue must be OK.
  }

  
  /*
   * The queue has been disconnected.   This was requested by us when the number of echo retries was exceeded as managed by
   * EchoSender or as a result of the client code requesting ResourceImpl.stopStreaming(), a likely action once a fixture has ended.
   * Here we tidy up some maps which are no longer needed (as the fixture/resource are no longer required).  The client code
   * can re-instantiate the resource and new mapping will be created. 
   */
  @Override
  public void handleCancelOk(String cTag) {
    logger.debug("Consumer: " + cTag + "disconnected" );
    String resourceId = CtagResourceMap.getResource(cTag);
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(resourceId);
    resource.mqDisconnectEvent();
    MQListener.removeMapping(cTag);
  }

  
  /*
   * The queue has been disconnected.   But this time it was a result of a failure on MQ (unlikely but we should allow for it).
   * Here we tidy up some maps which are no longer needed (as the fixture/resource are no longer required).  The client code
   * can re-instantiate the resource and new mapping will be created. 
   */
  @Override
  public void handleCancel(String cTag) {
    logger.debug("Consumer: " + cTag + "disconnected2" );
    String resourceId = CtagResourceMap.getResource(cTag);
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(resourceId);
    resource.mqDisconnectEvent();
    MQListener.removeMapping(cTag);
  }
  
}
