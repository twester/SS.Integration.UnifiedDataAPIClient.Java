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

import com.rabbitmq.client.ConnectionFactory;

import ss.udapi.sdk.services.JsonHelper;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.model.StreamEcho;

public class EchoSender implements Runnable
{

  private URI amqpURI;
  private Logger logger = Logger.getLogger(EchoSender.class);
  private HttpServices httpSvcs = new HttpServices(); 
  private ServiceRequest resources = new ServiceRequest();
  private static EchoSender instance = null;
  private static boolean echoRunning = false;
  
  private EchoSender(String amqpDest, ServiceRequest resources)
  {
    this.resources = resources;
    try {
      this.amqpURI = new URI(amqpDest);
    } catch (Exception ex) {
      logger.debug(ex);
    }
  }

  
  
  public static EchoSender getEchoSender(String amqpDest, ServiceRequest resources)
  {
    if (instance == null) {
      instance = new EchoSender(amqpDest, resources);
    }
    return instance;
  }
  
  

  @Override
  public void run()
  {
    EchoResourceMap echoMap = EchoResourceMap.getEchoMap();
    WorkQueue myQueue = WorkQueue.getWorkQueue();
    if (echoRunning == false)    {
      synchronized(this) {
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
            
            httpSvcs.processEcho(resources, "http://api.sportingsolutions.com/rels/stream/batchecho", resources.getServiceRestItems().get(0).getName(), stringStreamEcho);
            Set<String> defaulters = echoMap.incrAll(Integer.parseInt(SystemProperties.get("ss.echo_max_missed_echos")));
            
            //TODO if the missing echos happen very frequently move this to another thread mind this thread is only dealing with echos so it should be ok
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
