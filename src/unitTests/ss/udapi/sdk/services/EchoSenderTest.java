package ss.udapi.sdk.services;

import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;

import java.util.List;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;


public class EchoSenderTest
{
  private EchoSender echoSender = null;
  private SystemProperties sysProps = mock(SystemProperties.class);
  
  @Before
  public void setUp() throws Exception
  {
    String resourceBody = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"4x0lAft_P7JnfqLK0J4o1y_Rgtg\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":160,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/snapshot/Football/4x0lAft_P7JnfqLK0J4o1y_Rgtg/eW-m1htDbDHblJ3hBGH8G-PJYvsy\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/Football/4x0lAft_P7JnfqLK0J4o1y_Rgtg/I_jl9FutdrjWPmMFe5NXHZbxbvlE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/sequence/Football/4x0lAft_P7JnfqLK0J4o1y_Rgtg/2BrWDeuhdeBEvoxHkJRFsF5mVEs4\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY0\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"46NtalSfupT7w2MxuudoYUd9CKw\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":104,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/snapshot/Football/46NtalSfupT7w2MxuudoYUd9CKw/bYQ4NJ0ckn-oAMwylfJwzMbAREQ2\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/Football/46NtalSfupT7w2MxuudoYUd9CKw/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/sequence/Football/46NtalSfupT7w2MxuudoYUd9CKw/DlUzE_85HvmneGPFDLws4eKb9_Iz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/batchecho/1eJp1LYuuRROXMjlPMxkhuvDJrBD\",\"Verbs\":[\"Post\"]}]}]";

    List<RestItem> restItems = JsonHelper.toRestItems(resourceBody);

    ServiceRequest resources = new ServiceRequest();
    resources.setAuthToken("AUTH_TOKEN_01");
    resources.setServiceRestItems(restItems);
    
    echoSender = EchoSender.getEchoSender("http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox", resources);

    
    
    
  }

  @Test
  public void test()
  {
    fail("Not yet implemented"); // TODO
  }

}
