package ss.udapi.sdk.services;

import java.util.List;

import ss.udapi.sdk.ResourceImpl;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class FixtureActionProcessorTest {
  private String task;
  private ResourceWorkQueue workQueue = null;
  private ResourceImpl resource = mock(ResourceImpl.class);
  private boolean resourceImplCalled = false;
  
  @Before
  public void setUp() throws Exception
  {
    task = "{\"Relation\":\"http://xxxx.test123.com/rels/v006/FootballOdds\",\"Content\":{\"FixtureName\":\"Fern v NotFern\",\"Id\":\"5IyktEE--jyYCP4IMNgFjoXegiw\",\"Sequence\":424,\"MatchStatus\":40,\"Markets\":[{\"Id\":\"KMpQXPiBNP3xXBeo7A22a967vX8\",\"Selections\":[{\"Id\":\"BYLugtZDAnpFvwpGbHFiJXMr_S0\",\"Price\":1.92,\"Tradable\":true,\"Status\":1,\"UpdatedProperties\":\"p\"},{\"Id\":\"9dB57hwuzxx_f9hhLZG-ZhZrzPY\",\"Price\":3.3,\"Tradable\":true,\"Status\":1,\"UpdatedProperties\":\"p\"},{\"Id\":\"nyaFvQNsJlKRGG8kiZ5dgH8ehSI\",\"Price\":3.55,\"Tradable\":true,\"Status\":1,\"UpdatedProperties\":\"p\"}]}],\"GameState\":{\"matchsummary\":\"0-0 00:00 1st\",\"concisematchsummary\":\"0-0 00:00 1st\",\"homepenalties\":0,\"awaypenalties\":0,\"homecorners\":0,\"awaycorners\":0,\"homeredcards\":0,\"awayredcards\":0,\"homeyellowcards\":0,\"awayyellowcards\":0,\"homewoodwork\":0,\"awaywoodwork\":0,\"homesubstitutions\":0,\"awaysubstitutions\":0,\"goals\":null},\"Epoch\":3,\"LastEpochChangeReason\":[40],\"Timestamp\":\"2014-02-04T11:52:30Z\"}},\"LastEpochChangeReason\":[40],\"Timestamp\":\"2014-02-04T11:52:30Z\"}}";
    String resourceBody = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"testresource2\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":160,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource2/eW-m1htDbDHblJ3hBGH8G-PJYvsy\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource2/I_jl9FutdrjWPmMFe5NXHZbxbvlE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/testresource2/2BrWDeuhdeBEvoxHkJRFsF5mVEs4\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY0\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"testresource1\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":104,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource1/bYQ4NJ0ckn-oAMwylfJwzMbAREQ2\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource1/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/testresource1/DlUzE_85HvmneGPFDLws4eKb9_Iz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/1eJp1LYuuRROXMjlPMxkhuvDJrBD\",\"Verbs\":[\"Post\"]}]}]";


    List<RestItem> restItems = JsonHelper.toRestItems(resourceBody);
    ServiceRequest availableResources = new ServiceRequest();
    availableResources.setAuthToken("TEST_AUTH");
    availableResources.setServiceRestItems(restItems);

    ResourceWorkerMap.initWorkerMap();
    ResourceWorkerMap.reset();
    ResourceWorkerMap.addResource("5IyktEE--jyYCP4IMNgFjoXegiw", resource);
    workQueue = ResourceWorkQueue.getResourceWorkQueue();
    WorkQueue.reset();
    ResourceWorkQueue.addQueue("5IyktEE--jyYCP4IMNgFjoXegiw");
    
    resourceImplCalled = false;
  }

  

  @Test
  public void testResourceImplCalled() {
    /* This mocks the side effect ResourceImpl has on the it's work queue (i.e. it removes it's work).
     * You can prove to yourself the work queue is populated by looking at the logs for "FixtureActionProcessor - Processing started for fixture/resource:"   
     */
    doAnswer(new Answer<Void>() { 
      public Void answer(InvocationOnMock invocation) throws Throwable {
        resourceImplCalled = true;
        workQueue.removeUOW("5IyktEE--jyYCP4IMNgFjoXegiw"); 
        return null; 
      } 
    }).when(resource).streamData();
    

    //Run actionProc in it's own thread
    FixtureActionProcessor actionProc = new FixtureActionProcessor(task);
    Thread testThread = new Thread(actionProc);
    testThread.start();

    //And wait for it tofinish
    try {
      Thread.sleep(500);
    } catch (InterruptedException ex) {
      fail("The echo thread was interrupted before test completed.");
    } 

    /* We're checking that fixtureActionProcessor has called ResourceImpl to dequeue the task (and process it), but we're not testing that here, we're
     * only testing that fixtureActionProcessor calls it 
     */
    assertTrue(ResourceWorkQueue.size("5IyktEE--jyYCP4IMNgFjoXegiw") == 0);
    assertTrue(resourceImplCalled);
    
    FixtureActionProcessor.terminate();
  }


}

