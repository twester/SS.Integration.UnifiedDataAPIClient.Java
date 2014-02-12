package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class CtagResourceMapTest
{

  @Before
  public void setUp() throws Exception { 
    CtagResourceMap.reset();
    CtagResourceMap.initCtagMap();
  }


  @Test
  public void testAddGetCtagResource() {
    CtagResourceMap.addCtag("tagId1", "resourceId1_1");
    assertTrue("resourceId1_1".equalsIgnoreCase(CtagResourceMap.getResource("tagId1")));
  }


  @Test
  public void testMissingCtagResourceMap() {
    assertTrue(CtagResourceMap.getResource("missingTag") == null);
  }

  
  @Test
  public void testRemoveCtag() {
    CtagResourceMap.addCtag("tagId2", "resourceId2_1");
    assertTrue("resourceId2_1".equalsIgnoreCase(CtagResourceMap.getResource("tagId2")));
    
    CtagResourceMap.removeCtag("tagId2");
    assertTrue(CtagResourceMap.getResource("tagId2") == null);
  }

  
}
