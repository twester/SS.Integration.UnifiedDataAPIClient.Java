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

public class RabbitMqConsumer extends DefaultConsumer
{
  private static Logger logger = Logger.getLogger(RabbitMqConsumer.class);
  private EchoResourceMap echoMap = EchoResourceMap.getEchoMap();

  
  public RabbitMqConsumer(Channel channel){
    super(channel);
    
  }

  
  
  @Override
  public void handleDelivery(String cTag, Envelope envelope, AMQP.BasicProperties properties, byte[] bodyByteArray) {
    String body = new String(bodyByteArray);
    String msgHead = body.substring(0, 64);
    
    logger.debug("Consumer: " + cTag + " received message: " + msgHead);
    if (msgHead.equals("{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",")) {
      if (CtagResourceMap.getResource(cTag).equals("4x0lAft_P7JnfqLK0J4o1y_Rgtg")){
        System.out.println("--------------->Disregarding echo response for 4x0lAft_P7JnfqLK0J4o1y_Rgtg, Fernando v Jim");
      } else {
        echoMap.decrEchoCount(CtagResourceMap.getResource(cTag));
      }
    } else {
      WorkQueue myQueue = WorkQueue.getWorkQueue();
      myQueue.addTask(body);
    }
  }

  
  
  @Override
  public void handleCancelOk(String cTag) {
    String resourceId = CtagResourceMap.getResource(cTag);
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(resourceId);
    resource.mqDisconnectEvent();
  }

  
  
  @Override
  public void handleCancel(String cTag) {
    String resourceId = CtagResourceMap.getResource(cTag);
    ResourceImpl resource = (ResourceImpl)ResourceWorkerMap.getResourceImpl(resourceId);
    resource.mqDisconnectEvent();
  }
  
}
