package ss.udapi.sdk.model;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.model.Summary;
import static org.junit.Assert.*;


public class ServiceRequestTest
{
  private ServiceRequest request;
  private ModelTestHelper testHelper;
  
  @Before
  public void setUp()
  {
    testHelper = new ModelTestHelper();
    testHelper.buildRestItems();
    request = new ServiceRequest();
  }
  
  
  @Test
  public void testGetAuthToken()
  {
    request.setAuthToken("letMeIn");
    assertEquals("letMeIn", request.getAuthToken());
  }

  
  @Test
  public void testGetServiceRestItems()
  {
    Summary summary = testHelper.getSummary();

    request.setServiceRestItems(testHelper.getRestItems());
    assertEquals(summary.getDate(), request.getServiceRestItems().get(0).getContent().getDate());
  }

}
