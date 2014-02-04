package ss.udapi.sdk;
/*
import java.net.URL;
import java.util.List;

import ss.udapi.sdk.interfaces.Credentials;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.JsonHelper;
import ss.udapi.sdk.services.SystemProperties;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.Test;
import org.mockito.internal.matchers.Any;
import org.mockito.internal.matchers.AnyVararg;

public class SessionImplTest
{


  private ServiceRequest request = null;
  private List<RestItem> restItems = null;
  private Credentials creds = null;
  private URL url = null;
  private SessionImpl session = null;
  private Service service = null;
  
  private HttpServices httpSvcs = mock(HttpServices.class);
  private SystemProperties sysProps = mock(SystemProperties.class);
  
  
  
  @Before
  public void setUp() throws Exception
  {
    creds = new CredentialsImpl("user99", "password99");
    url = new URL("http://testurl.sportingsolutions.com");
    
    String responseBody = "[{\"Relation\":\"http://api.sportingsolutions.com/rels/login\",\"Href\":\"http://apicui.sportingsolutions.com/login/LcHAClpgi_av3MnG_gs-7Jcqia41\",\"Verbs\":[\"POST\"]}]},{\"Name\":\"Reset\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/usermanagementservice/user/reset\",\"Href\":\"http://connectappcui.sportingsolutions.com/SecurityService/UserManagementService/user/reset/5tzPt3YhcVoKPH-p6iA8DVebdjFG\",\"Verbs\":[\"PUT\"]}]}]";
    restItems = JsonHelper.toRestItems(responseBody);
    request.setAuthToken("AUTH_TOKEN");
    request.setServiceRestItems(restItems);
  }

  @Test
  public void testGetService()
  {
    
    when(httpSvcs.getSession("http://testurl.sportingsolutions.com", false)).thenReturn(request);
    
    
//    SessionImpl session = (SessionImpl) SessionFactory.createSession(url, creds);
//    service = session.getService("UnifiedDataAPI");

    System.out.println("----------------->" + service.getName());
  }

  @Test
  public void testGetServices()
  {
    fail("Not yet implemented"); // TODO
  }

}
*/