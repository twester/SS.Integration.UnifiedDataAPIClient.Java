package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.junit.Test;
import org.mockito.runners.MockitoJUnitRunner;

import ss.udapi.sdk.ResourceImpl;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceWorkerMapTest
{
  private ResourceWorkerMap workMap = ResourceWorkerMap.getWorkerMap();
  private ResourceImpl resource1 = mock(ResourceImpl.class);
  private static final String resId1 = "resource1";
  private ResourceImpl resource2 = mock(ResourceImpl.class);
  private static final String resId2 = "resource1";
  private ResourceImpl resource3 = mock(ResourceImpl.class);
  private static final String resId3 = "resource1";
  
  
  @Test
  public void testAddResource()
  {
    workMap.addResource(resId1, resource1);
    assertTrue(workMap.exists(resId1));
  }

  
  @Test
  public void testGetResourceImpl()
  {
    workMap.addResource(resId2, resource2);
    assertTrue(workMap.exists(resId2));
    ResourceImpl tempRes = (ResourceImpl) workMap.getResourceImpl(resId2);
    assertNotNull(tempRes);
  }

  
  @Test
  public void testGetMissingImpl()
  {
    ResourceImpl tempRes = (ResourceImpl) workMap.getResourceImpl("noResource");
    assertNull(tempRes);
  }

  
  
  @Test
  public void testRemoveResource()
  {
    workMap.addResource(resId3, resource3);
    assertTrue(workMap.exists(resId3));
    workMap.removeResource(resId3);
    assertFalse(workMap.exists(resId3));
  }
}
