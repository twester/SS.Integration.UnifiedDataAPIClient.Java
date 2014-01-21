package ss.udapi.sdk;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import ss.udapi.sdk.examples.model.Fixture;
import ss.udapi.sdk.interfaces.Credentials;
import ss.udapi.sdk.interfaces.Feature;
import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.interfaces.Session;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.SystemProperties;
import ss.udapi.sdk.streaming.ConnectedEvent;
import ss.udapi.sdk.streaming.DisconnectedEvent;
import ss.udapi.sdk.streaming.Event;
import ss.udapi.sdk.streaming.StreamEvent;
import ss.udapi.sdk.streaming.SynchronizationEvent;

public class SDKIntegrationTest
{
  private static org.apache.log4j.Logger logger = Logger.getLogger(SDKIntegrationTest.class);

  private HttpServices httpSvcs = new HttpServices();
  private ServiceRequest loginDetails;
  private List<String> sportsList;
  RestItem restItem = new RestItem();

  private Boolean fixtureEnded;
  
//  private ConcurrentHashMap<String,StreamListener> listeners;
  
  @Test
  public void test()
  {
    //TODO: in the original version the URL is passed to sessionImpl, but sessionImpl has it's own anyway!
    Credentials credentials = new CredentialsImpl(SystemProperties.get("ss.username"), SystemProperties.get("ss.password"));

    try {
      logger.debug("Starting GTPService");
      logger.info("Connecting to UDAPI....");
      Session session = SessionFactory.createSession(new URL(SystemProperties.get("ss.url")), credentials);
      logger.info("Successfully connected to UDAPI");

      //Downcasting is not necessary for operation, only being done to get details to verify in this/unit tests.  As is this long access path :-)
      logger.debug("UDAPI, Getting Service: " + ((SessionImpl)session).getAvailableServices().getServiceRestItems().get(0).getLinks().get(0).getHref());

      Service service = session.getService("UnifiedDataAPI");
      logger.debug("UDAPI, Retrieved " + service.getName() + " service");

      Feature feature = service.getFeature("Football");
      logger.debug("Retrieved Feature " + feature.getName());
  
      List<Feature> features = service.getFeatures();
      logger.debug("Retrieved " + features.size() + "features");
      
      Resource resource = feature.getResource("Fernando v Jim");
      logger.debug("Retrieved Resource " + resource.getName());

      List<Resource> resources = feature.getResources();
      logger.debug("Retrieved " + resources.size() + "resources");
      
      String snapShot = resource.getSnapshot();
      logger.debug("Snapshot retrieved" + snapShot.substring(0, 300));
      
    } catch (MalformedURLException ex) {
      logger.error(ex);
    }
    
    
    
    
    
    
    
    
  }

/*      int epoch = 0;
      if (resource.getContent().getMatchStatus() != 50)
      {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
   
        Fixture fixtureSnapshot = gson.fromJson(snapShot, Fixture.class);
        epoch = fixtureSnapshot.getEpoch();
   
        
//        StreamListener streamListener = new StreamListener(resource,epoch);
//        listeners.put(resource.getId(),streamListener);
      }
      System.out.println("Resource----------->" + resource.getName());
      listen(resource, epoch);
      
    } catch (MalformedURLException ex) {
      logger.error(ex);
    }
    
  }

  
  
  private void listen(final Resource resource, final int epoch){
    try{
      List<Event> streamingEvents = new ArrayList<Event>();

      streamingEvents.add(new ConnectedEvent() {
        public void onEvent(String message) {
          logger.info(String.format("Stream Connected for %1$s", resource.getName()));
        }
      });
 
      streamingEvents.add(new StreamEvent() {
        public void onEvent(String message) {
          logger.info(String.format("Streaming Message Arrived for %1$s", resource.getName()));
          handleStreamMessage(message, resource, epoch);
          }
      });
      
      streamingEvents.add(new DisconnectedEvent() {
        public void onEvent(String message){
          logger.info(String.format("Stream Disconnected for %1$s", resource.getName()));
        }
      });
      
      streamingEvents.add(new SynchronizationEvent(){
        public void onEvent(String message) {
          handleSyncEvent(resource, epoch);
        }
      });
      resource.startStreaming(streamingEvents);
      System.out.println("--------------->" + resource.getName());
      System.out.println("--------------->" + streamingEvents.size()); 
    }catch(Exception ex){
      logger.error(ex);
    }
  }

  
  private void handleStreamMessage(String streamString, final Resource resource, int epoch){
    this.fixtureEnded = true;
    try{
      logger.info(streamString);
      
      GsonBuilder gsonBuilder = new GsonBuilder();
      Gson gson = gsonBuilder.create();
      
      JsonObject jsonObject = new JsonParser().parse(streamString).getAsJsonObject();
      Fixture fixtureDelta = gson.fromJson(jsonObject.get("Content"), Fixture.class);
      
      logger.info(String.format("Attempting to process Markets and Selctions for %1$s", resource.getName()));
      
      if(fixtureDelta.getEpoch() > epoch){
        
        if(fixtureDelta.getLastEpochChangeReason() != null && Arrays.asList(fixtureDelta.getLastEpochChangeReason()).contains(10)){
          logger.info(String.format("Fixture %1$s has been deleted from the GTP Fixture Factory.", resource.getName()));
          resource.stopStreaming();
          this.fixtureEnded = true;
        }else{
          logger.info(String.format("Epoch changed for %1$s from %2$s to %3$s", resource.getName(), epoch, fixtureDelta.getEpoch()));
          resource.pauseStreaming();
          
          logger.info(String.format("Get UDAPI Snapshot for %1$s", resource.getName()));
          String snapshotString = resource.getSnapshot();
          logger.info(String.format("Successfully retrieved UDAPI Snapshot for %1$s", resource.getName()));
          
          Fixture fixtureSnapshot = gson.fromJson(snapshotString, Fixture.class);
          epoch = fixtureSnapshot.getEpoch();
          
          //process the snapshot here
          logger.info(snapshotString);
          
          if(!fixtureDelta.getMatchStatus().equalsIgnoreCase("50")){
            resource.unpauseStreaming();
          }else{
            logger.info(String.format("Stopping Streaming for %1$s with id %2$s, Match Status is Match Over", resource.getName(), resource.getId()));
            resource.stopStreaming();
            this.fixtureEnded = true;
          }
        }
        
      }else if(fixtureDelta.getEpoch() == epoch){
        //process the delta
        logger.info(fixtureDelta.getMarkets().size());
      }
    }catch(Exception ex){
      logger.error(ex);
    }
  }  
  
  
  
  private void handleSyncEvent(final Resource resource, int epoch){
    logger.warn(String.format("Stream out of sync for %1$s", resource.getName()));
    
    resource.pauseStreaming();
    
    GsonBuilder gsonBuilder = new GsonBuilder();
    Gson gson = gsonBuilder.create();
    
    logger.info(String.format("Get UDAPI Snapshot for %1$s", resource.getName()));
    String snapshotString = resource.getSnapshot();
    logger.info(String.format("Successfully retrieved UDAPI Snapshot for %1$s", resource.getName()));
    
    Fixture fixtureSnapshot = gson.fromJson(snapshotString, Fixture.class);
    epoch = fixtureSnapshot.getEpoch();
    
    //process the snapshot here
    logger.info(snapshotString);
    
    if(fixtureSnapshot.getMatchStatus() != "50"){
      resource.unpauseStreaming();
    }
  }

*/
}

      
      
 