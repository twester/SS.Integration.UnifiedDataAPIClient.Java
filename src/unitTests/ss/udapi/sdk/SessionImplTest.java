package ss.udapi.sdk;

import java.net.URL;
import java.util.List;

import ss.udapi.sdk.interfaces.Credentials;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.JsonHelper;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;


public class SessionImplTest
{
  private HttpServices httpSvcs = mock(HttpServices.class);
  private ServiceRequest sessionResponse = new ServiceRequest();
  private ServiceRequest availableServices = new ServiceRequest();
  
  private SessionImpl session;
  private Credentials credentials = new CredentialsImpl("userName", "passWord");

  @Before
  public void setUp() throws Exception
  {
    session = (SessionImpl) SessionFactory.createSession(new URL("http://xxx.test123url.com"), credentials);
    
    String availableServicesBody = "[{\"Name\":\"Login\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/login\",\"Href\":\"http://xxx.test123url.com/login/uKuk8urxlXv-93oQqlttTO_lxS5B\",\"Verbs\":[\"POST\"]}]},{\"Name\":\"Reset\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/usermanagementservice/user/reset\",\"Href\":\"http://connectappcui.sportingsolutions.com/SecurityService/UserManagementService/user/reset/qHhWTlqtYvfquzFUDb0_5xjh1Tsy\",\"Verbs\":[\"PUT\"]}]}]";
    List<RestItem> restItems = JsonHelper.toRestItems(availableServicesBody);
    availableServices.setServiceRestItems(restItems);
    
  }

  
  
  @Test
  public void testGetService()
  {
    /* Here we mock httpsevices (sending a request for all available services on this account).The unit test is 
     * asserting SessionImpl can find the service we request from those received from HttpSvcs.
     */
    boolean compressionEnabled = false;

    doAnswer(new Answer<ServiceRequest>() { 
      public ServiceRequest answer(InvocationOnMock invocation) throws Throwable {
        return sessionResponse; 
      } 
    }).when(httpSvcs).getSession("http://xxx.test123url.com", compressionEnabled);

    doAnswer(new Answer<ServiceRequest>() { 
      public ServiceRequest answer(InvocationOnMock invocation) throws Throwable {
        return availableServices; 
      } 
    }).when(httpSvcs).processLogin(sessionResponse, "http://api.sportingsolutions.com/rels/login", "Login");
    
    try {
      session.setHttpSvcs(httpSvcs, new URL("http://xxx.test123url.com"), credentials);
    } catch (Exception ex){
      //this URL cannot change so it will not be malformed, if it get's chewed up the test will fail
      
    }
    
    
    ServiceImpl responseService = (ServiceImpl) session.getService("Login");
    
    //So, does the URL for the feature we want (amongst all the features ServiceImpl gets) match what we expect? 
    assertEquals("http://xxx.tes123turl.com/UnifiedDataAPI/AmericanFootball/NFUV1zO3kgXJuyqY3NQoJcmQiZM4", responseService.getServiceHref());

  }

  @Test
  public void testGetServices()
  {
    fail("Not yet implemented"); // TODO
  }

}
