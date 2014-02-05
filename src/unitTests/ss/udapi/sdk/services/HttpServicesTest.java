package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;

import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;


public class HttpServicesTest
{
  private HttpServices httpSvcs = new HttpServices();
  private CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
  
  private HelperHttpResponse httpResponse;
  private String resourceBody;
  
  private List<RestItem> restItems;
  private ServiceRequest availableResources;

  private ServiceRequest request;
  
  private RestItem item = null;

  private ProtocolVersion protV = new ProtocolVersion("HTTP", 1, 1); 
  
  @Before
  public void setUp() throws Exception
  {
    httpResponse = new HelperHttpResponse();
    resourceBody = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"testresource2\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":160,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/snapshot/Football/testresource2/eW-m1htDbDHblJ3hBGH8G-PJYvsy\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsodddlutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/stream/Football/testresource2/I_jl9FutdrjWPmMFe5NXHZbxbvlE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/sequence/Football/testresource2/2BrWDeuhdeBEvoxHkJRFsF5mVEs4\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY0\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"testresource1\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":104,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/snapshot/Football/testresource1/bYQ4NJ0ckn-oAMwylfJwzMbAREQ2\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/stream/Football/testresource1/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/sequence/Football/testresource1/DlUzE_85HvmneGPFDLws4eKb9_Iz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.testurl.com/UnifiedDataAPI/stream/batchecho/1eJp1LYuuRROXMjlPMxkhuvDJrBD\",\"Verbs\":[\"Post\"]}]}]";

    restItems = JsonHelper.toRestItems(resourceBody);
    availableResources = new ServiceRequest();
    availableResources.setAuthToken("TEST_AUTH");
    availableResources.setServiceRestItems(restItems);
    

    for(RestItem searchRestItem:restItems) {
      if (searchRestItem.getName().equals("Fern v NotFern")) {
        item = searchRestItem;
      }
    }

    
    request = new ServiceRequest();
    request.setAuthToken("MyAuthToken");
    request.setServiceRestItems(restItems);
    
    
  }
  
  
  
  

  @Test
  public void testGetSession()
  {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testProcessLogin()
  {
    fail("Not yet implemented"); // TODO
  }

  

  
  
  @Test
  public void testRetrieveBodyPostAsUsedForEchos()
  {
    try {
      httpResponse.setStatusLine(protV, 202);
      when(httpClient.execute((HttpUriRequest)(anyObject()))).thenReturn(httpResponse);
      String response = httpSvcs.retrieveBody(request, "http://api.sportingsolutions.com/rels/stream/batchecho", "Fern v NotFern", "any old string");
      assertTrue(response.length() == 0);    } catch (Exception ex) {
      fail("Unknown Exception during test.");
    }

  }

  
  
  @Test
  public void testRetrieveBodyGetAsUsedForEverythingButEchos()
  {
    try {
      when(httpClient.execute((HttpUriRequest)(anyObject()),(ResponseHandler<String>)(anyObject()))).thenReturn(resourceBody);
      String response = httpSvcs.retrieveBody(request, "http://api.sportingsolutions.com/rels/stream/amqp", "Fern v NotFern", "n/a");
      assertTrue(response.length() != 0);
    } catch (Exception ex) {
      fail("Unknown Exception during test.");
    }


  }
  
  
  
  
  
//String resourceBody = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"testresource2\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":160,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/snapshot/Football/testresource2/eW-m1htDbDHblJ3hBGH8G-PJYvsy\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/Football/testresource2/I_jl9FutdrjWPmMFe5NXHZbxbvlE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/sequence/Football/testresource2/2BrWDeuhdeBEvoxHkJRFsF5mVEs4\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/echo/UJp-nkCrpBHv195n1Oi2rWm9TCox\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY0\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"testresource1\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":104,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/snapshot/Football/testresource1/bYQ4NJ0ckn-oAMwylfJwzMbAREQ2\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/Football/testresource1/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/sequence/Football/testresource1/DlUzE_85HvmneGPFDLws4eKb9_Iz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://apicui.sportingsolutions.com/UnifiedDataAPI/stream/batchecho/1eJp1LYuuRROXMjlPMxkhuvDJrBD\",\"Verbs\":[\"Post\"]}]}]";
//HttpEntity myEntity = new StringEntity(resourceBody);
//httpResponse.setEntity(myEntity);

  
  
  
  @Test
  public void testProcessRequestServiceRequestStringString()
  {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testProcessRequestServiceRequestStringStringString()
  {
    fail("Not yet implemented"); // TODO
  }

  
  
  @Test
  public void testGetSnapshot()
  {
    fail("Not yet implemented"); // TODO
    
  }

  
}



