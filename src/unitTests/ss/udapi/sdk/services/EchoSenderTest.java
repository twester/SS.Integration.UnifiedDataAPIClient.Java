package ss.udapi.sdk.services;

import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;

import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EchoSenderTest
{
  private EchoSender echoSender = null;
  private ServiceRequest resRequest;
  private EchoResourceMap echoMap = null;
  
  
  @Before
  public void setUp() throws Exception {
    echoMap = EchoResourceMap.getEchoMap();
    CtagResourceMap.reset();
    
    String resourceBody = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"testresource2\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":160,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource2/eW-m1htDbDHblJ3hBGH8G-PJYvsy\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource2/I_jl9FutdrjWPmMFe5NXHZbxbvlE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/testresource2/2BrWDeuhdeBEvoxHkJRFsF5mVEs4\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY0\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"testresource1\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":104,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource1/bYQ4NJ0ckn-oAMwylfJwzMbAREQ2\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource1/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/testresource1/DlUzE_85HvmneGPFDLws4eKb9_Iz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/1eJp1LYuuRROXMjlPMxkhuvDJrBD\",\"Verbs\":[\"Post\"]}]}]";
    List<RestItem> restItems = JsonHelper.toRestItems(resourceBody);
    resRequest = new ServiceRequest();
    resRequest.setAuthToken("AUTH_TOKEN_01");
    resRequest.setServiceRestItems(restItems);
    
    echoSender = EchoSender.getEchoSender("http://xxx.test123url.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox", resRequest);
    echoMap.addResource("testresource2");
  }



  @Test 
  public void testEchoRetryNotExceeded() {
    SystemProperties.setProperty("ss.echo_sender_interval", "1");
    SystemProperties.setProperty("ss.echo_max_missed_echos", "2");
    
    Thread testThread = new Thread(echoSender);
    
    testThread.start();
    
    //I know! sleep methods in unit test, but we are a testing a method which itself goes to sleep, so we have to do it.
    try {
      Thread.sleep(1000);
      echoMap.resetEchoCount("testresource2");
      Thread.sleep(1000);
      echoMap.resetEchoCount("testresource2");
      Thread.sleep(1000);
      echoMap.resetEchoCount("testresource2");
      Thread.sleep(1000);
      echoMap.resetEchoCount("testresource2");
    } catch (InterruptedException ex) {
      fail("The echo thread was interrupted before test completed.");
    } 
    
    /*We are checking to see if the echo count achieved the maximum number the count appears to be a plus one but that's because
     *of where we carry out the check in echo sender (ie when we next call it).
     */
     assertTrue(EchoResourceMap.getEchoMap().getEchoCount("testresource2").equals(0));
     EchoSender.terminate();
  }
  
}
