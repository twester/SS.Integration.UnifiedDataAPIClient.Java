package ss.udapi.sdk.services;

import java.net.URI;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
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
    echoRunning = true;
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
        System.out.println("myEcho---------------->" + stringStreamEcho);
        
        httpSvcs.processEcho(resources, "http://api.sportingsolutions.com/rels/stream/batchecho", "Fernando v Jim", stringStreamEcho);
   
        Thread.sleep(5000);
      } catch (InterruptedException ex) {
        logger.error("Echo Thread disrupted" + ex);
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

  public boolean getEchoRunning()
  {
    return echoRunning;
  }
  
  
  
}
