package ss.udapi.sdk;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ss.udapi.sdk.ResourceImpl;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class FeatureImplTest
{
  private HttpServices httpSvcs = mock(HttpServices.class);
  private RestItem restItem = mock(RestItem.class);
  private ServiceRequest request = mock(ServiceRequest.class);
  private FeatureImpl feature;
  private ResourceImpl resource;

  
  @Before
  public void setUp() throws Exception
  {
    feature = new FeatureImpl(restItem, request);

    
  }

  @Test
  public void testGetResource()
  {

    
    resource = (ResourceImpl) feature.getResource("5IyktEE--jyYCP4IMNgFjoXegiw");
    fail("Not yet implemented");
  }

  @Test
  public void testGetResources()
  {
    fail("Not yet implemented");
  }

  @Test
  public void testGetName()
  {
    fail("Not yet implemented");
  }

}
