package ss.udapi.sdk.services;
/*
import java.util.List;

import ss.udapi.sdk.ResourceImpl;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class FixtureActionProcessorTest
{
  private String task;
  private ResourceImpl resource;
  ResourceWorkerMap workerMap;
  ResourceWorkQueue workQueue;
  
  @Before
  public void setUp() throws Exception
  {
    task = "{\"Relation\":\"http://xxxx.testurl.com/rels/v006/FootballOdds\",\"Content\":{\"FixtureName\":\"Fern v NotFern\",\"Id\":\"testresource1\",\"Sequence\":424,\"MatchStatus\":40,\"Markets\":[{\"Id\":\"KMpQXPiBNP3xXBeo7A22a967vX8\",\"Selections\":[{\"Id\":\"BYLugtZDAnpFvwpGbHFiJXMr_S0\",\"Price\":1.92,\"Tradable\":true,\"Status\":1,\"UpdatedProperties\":\"p\"},{\"Id\":\"9dB57hwuzxx_f9hhLZG-ZhZrzPY\",\"Price\":3.3,\"Tradable\":true,\"Status\":1,\"UpdatedProperties\":\"p\"},{\"Id\":\"nyaFvQNsJlKRGG8kiZ5dgH8ehSI\",\"Price\":3.55,\"Tradable\":true,\"Status\":1,\"UpdatedProperties\":\"p\"}]}],\"GameState\":{\"matchsummary\":\"0-0 00:00 1st\",\"concisematchsummary\":\"0-0 00:00 1st\",\"homepenalties\":0,\"awaypenalties\":0,\"homecorners\":0,\"awaycorners\":0,\"homeredcards\":0,\"awayredcards\":0,\"homeyellowcards\":0,\"awayyellowcards\":0,\"homewoodwork\":0,\"awaywoodwork\":0,\"homesubstitutions\":0,\"awaysubstitutions\":0,\"goals\":null},\"Epoch\":3,\"LastEpochChangeReason\":[40],\"Timestamp\":\"2014-02-04T11:52:30Z\"}},\"LastEpochChangeReason\":[40],\"Timestamp\":\"2014-02-04T11:52:30Z\"}}";
    
    String resourceBody = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"testresource2\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":160,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/snapshot/Football/testresource2/eW-m1htDbDHblJ3hBGH8G-PJYvsy\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/Football/testresource2/I_jl9FutdrjWPmMFe5NXHZbxbvlE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/sequence/Football/testresource2/2BrWDeuhdeBEvoxHkJRFsF5mVEs4\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY0\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"testresource1\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":104,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/snapshot/Football/testresource1/bYQ4NJ0ckn-oAMwylfJwzMbAREQ2\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/Football/testresource1/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/sequence/Football/testresource1/DlUzE_85HvmneGPFDLws4eKb9_Iz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/batchecho/1eJp1LYuuRROXMjlPMxkhuvDJrBD\",\"Verbs\":[\"Post\"]}]}]";
    
    
    
    List<RestItem> restItems = JsonHelper.toRestItems(resourceBody);
    ServiceRequest availableResources = new ServiceRequest();
    availableResources.setAuthToken("TEST_AUTH");
    availableResources.setServiceRestItems(restItems);
    
    RestItem item = null;
    for(RestItem searchRestItem:restItems) {
      if (searchRestItem.getName().equals("Fern v NotFern")) {
        item = searchRestItem;
      }
    }
    
    workerMap = ResourceWorkerMap.getWorkerMap();
    workQueue = ResourceWorkQueue.getResourceWorkQueue();

    
    resource = new ResourceImpl(item, availableResources);

    workerMap.addResource("testresource1", resource);
    
        
    
    
  }

  

  
  @Test
  public void test()
  {
    FixtureActionProcessor actionProc = new FixtureActionProcessor(task);
    Thread testThread = new Thread(actionProc);
    
    
    
    testThread.start();

    try {
      Thread.sleep(5000);
    } catch (InterruptedException ex) {
      fail("The echo thread was interrupted before test completed.");
    } 

    assertEquals(task, ResourceWorkQueue.removeUOW("testresource1"));
    

  }

}

*/