package ss.udapi.sdk;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ss.udapi.sdk.ResourceImpl;
import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.JsonHelper;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FeatureImplTest
{
  private HttpServices httpSvcs = mock(HttpServices.class);
  private RestItem restItem;
  private ServiceRequest resRequest;
  private ServiceRequest resResponse = new ServiceRequest();
  private FeatureImpl feature;
  private ResourceImpl responseResource;

  
  @Before
  public void setUp() throws Exception
  {
    String resourceBody = "[{\"Name\":\"AmericanFootball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/AmericanFootball/NFUV1zO3kgXJuyqY3NQoJcmQiZM4\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"AustralianRules\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/AustralianRules/KkwI3jVOYnxV8M0d0dmHY9mzle0y\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Badminton\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Badminton/7aRyxGklZzTyNL5cZYyLRxAX6CdG\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Baseball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Baseball/caeDVIuJiBUcQUqs4JYfYo_feZ5B\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Basketball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Basketball/dECYjFmyPH0c9-VG3RJ6VMEko5I1\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"BeachVolleyball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/BeachVolleyball/XaMVGwGTInMMgYiBr9igjk2Nu-ky\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Boxing\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Boxing/gRmzsmrJbzkhk57kSBES50BELjg1\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Cricket\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Cricket/9hAUfrQ_-w59TqdpTUrzm1qdx5k2\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Darts\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Darts/hor57BNMNb6Xw1Sg8qAkLpqyjUY0\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Football\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Football/YFUBu4lobCJeX1v-yFZMyab5REU3\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"GaelicFootball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/GaelicFootball/VS4MKcGS6ZyDFaPJqD3daKlE958x\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"GaelicHurling\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/GaelicHurling/UWwoppGz7X-LxTOu_OBXeAm_zAIz\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Handball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Handball/1VKss1xr8m7iIJIRR2pil2YhppxB\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"HorseRacing\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/HorseRacing/T5wfx_s8XXR6Sn4sOiUKKUqE2dY2\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"IceHockey\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/IceHockey/fSiJMgblNTzziD2_U2vIN-tyI38x\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"RugbyLeague\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/RugbyLeague/Z-Dw1jCxSLryHAsUJuK8m0kNUBs2\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"RugbyUnion\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/RugbyUnion/9RVxBEsvoxxo-I_QEihAAcMsmLxC\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Snooker\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Snooker/hi1T4j4gaKmlsDWhCveOHP0_MOpC\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Squash\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Squash/p9cDqK_fbCkXS7ASVy0c2jeV4wgx\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Tennis\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Tennis/3orxELMR1XdTpCl-6TmcsFbEjFY3\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"TestCricket\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/TestCricket/3qCi0JRCQ5Spolejkzw0I0oq20Ax\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Volleyball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Volleyball/aXWbUhZ2AcZbLyPi7uPerP_5DjQ5\",\"Verbs\":[\"Get\"]}]}]";
    List<RestItem> restItems = JsonHelper.toRestItems(resourceBody);

    resRequest = new ServiceRequest();
    resRequest.setAuthToken("AUTH_TOKEN_01");
    resRequest.setServiceRestItems(restItems);
    restItem = getRestItems(resRequest, "Football");    
    System.out.println("------------->" + restItem);
    feature = new FeatureImpl(restItem, resRequest);

    String responseBody = "[{\"Name\":\"Fernando v Jim\",\"Content\":{\"Id\":\"4x0lAft_P7JnfqLK0J4o1y_Rgtg\",\"StartTime\":\"2014-01-14T11:14:16Z\",\"Sequence\":207,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fernando\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"Jim\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"test\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/4x0lAft_P7JnfqLK0J4o1y_Rgtg/eW-m1htDbDHblJ3hBGH8G-PJYvsz\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/4x0lAft_P7JnfqLK0J4o1y_Rgtg/I_jl9FutdrjWPmMFe5NXHZbxbvlD\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/4x0lAft_P7JnfqLK0J4o1y_Rgtg/s0e9ZKei1vo7bI6xqBY6rrgiS6xF\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw2\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/IWrPmnacWoSOoz_kOqQNjI7SBSY3\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"46NtalSfupT7w2MxuudoYUd9CKw\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":449,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"AGame\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/46NtalSfupT7w2MxuudoYUd9CKw/bYQ4NJ0ckn-oAMwylfJwzMbAREQ3\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/46NtalSfupT7w2MxuudoYUd9CKw/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/46NtalSfupT7w2MxuudoYUd9CKw/Ly7yvGLELxxThyA-88QT4riXoFU1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/I546imiMYzaxVfuUQVUSWK4Nf1pF\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/n21SkC75lqm5y716xvV6LSSUtVs5\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"fern v johnny alien\",\"Content\":{\"Id\":\"5IyktEE--jyYCP4IMNgFjoXegiw\",\"StartTime\":\"2014-02-05T08:57:15Z\",\"Sequence\":57,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"johnny alien\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"another match\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/snapshot/Football/5IyktEE--jyYCP4IMNgFjoXegiw/txi1aKDfyRlQWZc3-w4vwsVGP_Qx\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/Football/5IyktEE--jyYCP4IMNgFjoXegiw/T29o1GI23kOIZJGJHVegOfV0_HMx\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Football/5IyktEE--jyYCP4IMNgFjoXegiw/J9S14jW_GlKoz9RQygrX5Q7xX_hE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/stream/batchecho/nyounZMjUxEGPcQ_o44qlXQazhQx\",\"Verbs\":[\"Post\"]}]}]";
    List<RestItem> responseItems = JsonHelper.toRestItems(responseBody);
    resResponse.setServiceRestItems(responseItems);
    System.out.println("------------->");
    resResponse.setAuthToken("AUTH_TOKEN_01");
  }

  
  @Test
  public void testGetResource()
  {
    /* Here we mock httpsevices (sending a request for all resources for this feature).  The unit test is asserting
     * that FeatureImpl filters the response and returns the a ResourceImpl with a set of resources for this feature.
     */
    doAnswer(new Answer<ServiceRequest>() { 
      public ServiceRequest answer(InvocationOnMock invocation) throws Throwable {
        return resResponse; 
      } 
    }).when(httpSvcs).processRequest(resRequest, "http://api.sportingsolutions.com/rels/resources/list", "Football");
    
    feature.setHttpSvcs(httpSvcs);
    responseResource = (ResourceImpl) feature.getResource("fern v johnny alien");

    //So, does the ID we should get for this resource match the name we've given it? 
    assertEquals("5IyktEE--jyYCP4IMNgFjoXegiw", responseResource.getId());
  }


  
  @Test
  public void testGetResourcenotFound()
  {
    /* Here we mock httpsevices (sending a request for all resources for this feature).  The unit test is asserting
     * that FeatureImpl filters the response and returns the a ResourceImpl with a set of resources for this feature.
     */
    doAnswer(new Answer<ServiceRequest>() { 
      public ServiceRequest answer(InvocationOnMock invocation) throws Throwable {
        return resResponse; 
      } 
    }).when(httpSvcs).processRequest(resRequest, "http://api.sportingsolutions.com/rels/resources/list", "Football");
    
    feature.setHttpSvcs(httpSvcs);
    responseResource = (ResourceImpl) feature.getResource("name does not exist");

    //We should get nothing back at all 
    assertNull(responseResource);
  }

  

  @Test
  public void testGetResources()
  {
    /* Here we mock httpsevices (sending a request for all resources for this feature).  The unit test is asserting
     * that FeatureImpl filters the response and returns the a ResourceImpl with a set of resources for this feature.
     */
    doAnswer(new Answer<ServiceRequest>() { 
      public ServiceRequest answer(InvocationOnMock invocation) throws Throwable {
        return resResponse; 
      } 
    }).when(httpSvcs).processRequest(resRequest, "http://api.sportingsolutions.com/rels/resources/list", "Football");
    
    feature.setHttpSvcs(httpSvcs);
    List<Resource> responseSet = feature.getResources();

    //So, does the ID we should get for this resource match the name we've given it? 
    assertEquals(3, responseSet.size());
  }

  
  
  @Test
  public void testGetName()
  {
    assertEquals("Football", feature.getName());
  }


  
  //find the request we need
  private RestItem getRestItems(ServiceRequest request, String name) {
    RestItem matchingRest = null;
    Iterator<RestItem> itemRestIterator = request.getServiceRestItems().iterator();
    do {
      matchingRest = itemRestIterator.next();
      if (matchingRest.getName().compareTo(name) != 0) {
        matchingRest = null;
      }
    } while ( itemRestIterator.hasNext() && (matchingRest == null) ) ;
    return matchingRest;
  }  
  
}
