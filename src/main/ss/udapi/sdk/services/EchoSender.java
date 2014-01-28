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

import java.net.URI;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.log4j.Logger;

import ss.udapi.sdk.services.JsonHelper;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.model.StreamEcho;


public class EchoSender implements Runnable
{
  private static Logger logger = Logger.getLogger(EchoSender.class);
  private static EchoSender instance = null;
  private static boolean echoRunning = false;
  private static HttpServices httpSvcs = new HttpServices(); 
  private URI amqpURI;
  private ServiceRequest resources = new ServiceRequest();

  
  private EchoSender(String amqpDest, ServiceRequest resources) {
    this.resources = resources;
    try {
      this.amqpURI = new URI(amqpDest);
    } catch (Exception ex) {
      logger.debug(ex);
    }
  }
  
  
  public static EchoSender getEchoSender(String amqpDest, ServiceRequest resources) {
    if (instance == null) {
      instance = new EchoSender(amqpDest, resources);
    }
    return instance;
  }
  

  @Override
  public void run() {
    logger.info("Starting echoes.");
    EchoResourceMap echoMap = EchoResourceMap.getEchoMap();
    WorkQueue myQueue = WorkQueue.getWorkQueue();
    if (echoRunning == false)    {
      while (true) {
        try {
          String path = amqpURI.getRawPath();
          String queue = path.substring(path.indexOf('/',1)+1);
          String virtualHost = uriDecode(amqpURI.getPath().substring(1,path.indexOf('/',1)));
          
          StreamEcho streamEcho = new StreamEcho(); 
          streamEcho.setHost(virtualHost);
          streamEcho.setQueue(queue);
          
          String guid = UUID.randomUUID().toString();
          DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS'Z'");
          df.setTimeZone(TimeZone.getTimeZone("UTC"));
          streamEcho.setMessage(guid + ";" + df.format(new Date()));
          
          String stringStreamEcho = JsonHelper.ToJson(streamEcho);
          logger.info("Batch echo sent: " + stringStreamEcho);
          
          httpSvcs.processRequest(resources, "http://api.sportingsolutions.com/rels/stream/batchecho", resources.getServiceRestItems().get(0).getName(), stringStreamEcho);

          //TODO: if a simple json library is available move to that for this poison pill 
          Set<String> defaulters = echoMap.incrAll(Integer.parseInt(SystemProperties.get("ss.echo_max_missed_echos")));
          Iterator<String> keyIter = defaulters.iterator();
          while(keyIter.hasNext()) {
            String key = keyIter.next();
            String task = "{\"Relation\":\"EchoFailure\",\"Id\":\"" + key +"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
            myQueue.addTask(task);
          }

          echoRunning=true;
          Thread.sleep(Integer.parseInt(SystemProperties.get("ss.echo_sender_interval"))*1000);
        } catch (InterruptedException ex) {
          logger.error("Echo Thread disrupted" + ex);
        }
      }
    }
  }
  
  
  private String uriDecode(String s) {
    try {
        // URLDecode decodes '+' to a space, as for form encoding.  So protect plus signs.
        return URLDecoder.decode(s.replace("+", "%2B"), "US-ASCII");
    }
    catch (java.io.UnsupportedEncodingException e) {
        throw new RuntimeException(e);
    }
  }     
  
}
