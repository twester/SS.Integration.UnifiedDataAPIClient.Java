package ss.udapi.sdk;

import static org.junit.Assert.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ss.udapi.sdk.examples.StreamListener;
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

public class SDKIntegrationTest
{
  private static org.apache.log4j.Logger logger = Logger.getLogger(SDKIntegrationTest.class);

  private HttpServices httpSvcs = new HttpServices();
  private ServiceRequest loginDetails;
  private List<String> sportsList;
  RestItem restItem = new RestItem();
  
  private ConcurrentHashMap<String,StreamListener> listeners;
  
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
      

      
      if (resource.getContent().getMatchStatus() != 50)
      {
        GsonBuilder gsonBuilder = new GsonBuilder();
        Gson gson = gsonBuilder.create();
        
        Fixture fixtureSnapshot = gson.fromJson(snapShot, Fixture.class);
        Integer epoch = fixtureSnapshot.getEpoch();

        System.out.println("epoch----------------->" + epoch);
        
        StreamListener streamListener = new StreamListener(resource,epoch);
        listeners.put(resource.getId(), streamListener);
      }


      
      
      
    } catch (MalformedURLException ex) {
      logger.error(ex);
    }
    
  }

  
  
}
