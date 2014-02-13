package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.StreamEcho;

public class JsonHelperTest
{

  
  @Before
  public void setUp() throws Exception
  {
  }

  
  @Test
  public void testToRestItems() {
    String resourceBody = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"testresource2\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":160,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource2/eW-m1htDbDHblJ3hBGH8G-PJYvsy\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource2/I_jl9FutdrjWPmMFe5NXHZbxbvlE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/testresource2/2BrWDeuhdeBEvoxHkJRFsF5mVEs4\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY0\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"testresource1\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":104,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource1/bYQ4NJ0ckn-oAMwylfJwzMbAREQ2\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/testresource1/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/testresource1/DlUzE_85HvmneGPFDLws4eKb9_Iz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/1eJp1LYuuRROXMjlPMxkhuvDJrBD\",\"Verbs\":[\"Post\"]}]}]";
    List<RestItem> restItems = JsonHelper.toRestItems(resourceBody);

    String href = restItems.get(0).getLinks().get(0).getHref();
    assertEquals("http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/testresource2/eW-m1htDbDHblJ3hBGH8G-PJYvsy", href);
  }

  
  @Test
  public void testToJson() {
    StreamEcho streamEcho = new StreamEcho(); 
    streamEcho.setHost("TEST_HOST");
    streamEcho.setQueue("QUEUE");
    String guid = "a78c6f2c-92f4-44bb-b7c9-69cd7b59ee1a";
    DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    streamEcho.setMessage(guid + ";" + df.format(new Date(6256132)));
    String stringStreamEcho = JsonHelper.ToJson(streamEcho);
    
    assertEquals("{\"host\":\"TEST_HOST\",\"queue\":\"QUEUE\",\"message\":\"a78c6f2c-92f4-44bb-b7c9-69cd7b59ee1a;" + df.format(new Date(6256132)) + "\"}",stringStreamEcho);
  }

  
}
