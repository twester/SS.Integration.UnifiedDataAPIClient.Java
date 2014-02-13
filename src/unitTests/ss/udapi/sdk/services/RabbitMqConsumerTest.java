package ss.udapi.sdk.services;

import com.rabbitmq.client.Channel;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ss.udapi.sdk.ResourceImpl;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqConsumerTest {
  private Channel channel = mock(Channel.class);
  private ResourceImpl resource = mock(ResourceImpl.class);
  private WorkQueue myQueue = null;
  private boolean resourceImplCalled;
  private byte[] messageByteArray;
  private byte[] echoByteArray;
  private EchoResourceMap echoMap = EchoResourceMap.getEchoMap();
  private RabbitMqConsumer mqConsumer;
  private String cTag = "amq.ctag-ApAlmaYcURB0nqy-B8UsjQ";

  //It is used in this session, just not in this class but in RabbitMQConsumer
  @SuppressWarnings("unused")
  private MQListener mqListener = MQListener.getMQListener("http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource2/I_jl9FutdrjWPmMFe5NXHZbxbvlE");
  
  
  @Before
  public void setUp() throws Exception {
    myQueue = WorkQueue.getWorkQueue();
    WorkQueue.reset();
    mqConsumer = new RabbitMqConsumer(channel);
    ResourceWorkerMap.initWorkerMap();
    ResourceWorkerMap.reset();
    resourceImplCalled = false;
    ResourceWorkerMap.addResource("5IyktEE--jyYCP4IMNgFjoXegiw", resource);
    MQListener.getMQListener(cTag);
    CtagResourceMap.initCtagMap();
    CtagResourceMap.reset();
    CtagResourceMap.addCtag("amq.ctag-ApAlmaYcURB0nqy-B8UsjQ", "5IyktEE--jyYCP4IMNgFjoXegiw");
    messageByteArray = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"testresource2\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":160,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource2/eW-m1htDbDHblJ3hBGH8G-PJYvsy\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource2/I_jl9FutdrjWPmMFe5NXHZbxbvlE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/testresource2/2BrWDeuhdeBEvoxHkJRFsF5mVEs4\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY0\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"testresource1\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":104,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource1/bYQ4NJ0ckn-oAMwylfJwzMbAREQ2\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource1/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/testresource1/DlUzE_85HvmneGPFDLws4eKb9_Iz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/1eJp1LYuuRROXMjlPMxkhuvDJrBD\",\"Verbs\":[\"Post\"]}]}]".getBytes();
    echoByteArray = "{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Content\":\"fab07c50-89d1-4c02-a0ec-219a041741bb;2014-02-05T14:29:32.816Z\"}".getBytes(); 
    resourceImplCalled = false;
    echoMap.addResource("5IyktEE--jyYCP4IMNgFjoXegiw");
  }


  @Test
  public void testEchoHandleDeliveryData() {
    int queueSize = myQueue.size();
    mqConsumer.handleDelivery(cTag, null, null, messageByteArray);
    //test that RabbitMQListener successfully found work queue and dumped the payload in there for workQueueMonitor to do as it pleases.
    assertTrue(myQueue.size() == (queueSize + 1));
  }

  
  
  
  
  @Test
  public void testHandleDeliveryEchoResp() {
    //here we increase the number of missed echos to two.
    echoMap.incrAll(3);
    echoMap.incrAll(3);
    assertTrue(echoMap.getEchoCount("5IyktEE--jyYCP4IMNgFjoXegiw") == 2);

    //and when we get an echo response for this resource we call EchoResourceMap to reset the count back to 0.
    mqConsumer.handleDelivery(cTag, null, null, echoByteArray);

    //just checking it happened.
    assertTrue(echoMap.getEchoCount("5IyktEE--jyYCP4IMNgFjoXegiw") == 0);
  }

  

    
  @Test
  public void testHandleCancelOkString() {    
    assertTrue(CtagResourceMap.getResource(cTag) != null);
    
    /* All we need to do in this test is check that the resource and MQListeners are alerted to this event.  Checking the complete call stack is
     * integration testing.  Which needs a real end point.
     */
    doAnswer(new Answer<Void>() { 
      public Void answer(InvocationOnMock invocation) throws Throwable {
        resourceImplCalled = true;
        return null; 
      } 
    }).when(resource).mqDisconnectEvent();

    //test the method
    mqConsumer.handleCancelOk(cTag);

    //check resource was called
    assertTrue(resourceImplCalled);

    //and that MQListener was called to forget about this resource
    assertTrue(CtagResourceMap.getResource(cTag) == null);
  }

  
  
  
  @Test
  public void testHandleCancelString() {
    assertTrue(CtagResourceMap.getResource(cTag) != null);
    
    /* All we need to do in this test is check that the resource and MQListeners are alerted to this event.  Checking the complete call stack is
     * integration testing.  Which needs a real end point.
     */
    doAnswer(new Answer<Void>() { 
      public Void answer(InvocationOnMock invocation) throws Throwable {
        resourceImplCalled = true;
        return null; 
      } 
    }).when(resource).mqDisconnectEvent();

    //test the method
    mqConsumer.handleCancelOk(cTag);

    //check resource was called
    assertTrue(resourceImplCalled);

    //and that MQListener was called to forget about this resource
    assertTrue(CtagResourceMap.getResource(cTag) == null);
  }
  
}
