package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class EchoresourceMapTest
{
  private EchoResourceMap resourceMap = null;

  
  @Before
  public void setUp() throws Exception {
    EchoResourceMap.reset();
    resourceMap = EchoResourceMap.getEchoMap();
    resourceMap.addResource("resource_1");
    resourceMap.addResource("resource_2");
    resourceMap.addResource("resource_3");
    resourceMap.addResource("resource_4");
    resourceMap.addResource("resource_10");
    resourceMap.addResource("resource_11");
    resourceMap.addResource("resource_12");

  }

  
  @Test
  public void testAddResource() {
    assertTrue(resourceMap.getEchoCount("resource_1").compareTo(0) == 0);
  }


  @Test
  public void testRemoveResource() {
    resourceMap.removeResource("resource_2");
    assertNull(resourceMap.getEchoCount("resource_2"));
  }

  
  @Test
  public void testIncrEchoCount() {
    assertTrue(resourceMap.getEchoCount("resource_3").compareTo(0) == 0);
    resourceMap.incrEchoCount("resource_3");
    assertTrue(resourceMap.getEchoCount("resource_3").compareTo(1) == 0);
  }

  
  @Test
  public void testResetEchoCount() {
    assertTrue(resourceMap.getEchoCount("resource_4").compareTo(0) == 0);
    resourceMap.incrEchoCount("resource_4");
    resourceMap.incrEchoCount("resource_4");
    assertTrue(resourceMap.getEchoCount("resource_4").compareTo(2) == 0);
    resourceMap.resetEchoCount("resource_4");
    assertTrue(resourceMap.getEchoCount("resource_4").compareTo(0) == 0);
  }

  
  @Test
  public void testIncrAll() {
    Set<String> defaulters = resourceMap.incrAll(2);
    resourceMap.resetEchoCount("resource_1");
    resourceMap.resetEchoCount("resource_2");
    resourceMap.resetEchoCount("resource_3");
    resourceMap.resetEchoCount("resource_4");
    assertTrue(resourceMap.getEchoCount("resource_10").compareTo(1) == 0);
    assertTrue(resourceMap.getEchoCount("resource_11").compareTo(1) == 0);
    assertTrue(resourceMap.getEchoCount("resource_12").compareTo(1) == 0);
    assertEquals(0, defaulters.size());
    resourceMap.resetEchoCount("resource_10");
    defaulters = resourceMap.incrAll(2);
    resourceMap.resetEchoCount("resource_1");
    resourceMap.resetEchoCount("resource_2");
    resourceMap.resetEchoCount("resource_3");
    resourceMap.resetEchoCount("resource_4");
    defaulters = resourceMap.incrAll(2);
    resourceMap.resetEchoCount("resource_1");
    resourceMap.resetEchoCount("resource_2");
    resourceMap.resetEchoCount("resource_3");
    resourceMap.resetEchoCount("resource_4");

    assertTrue(resourceMap.getEchoCount("resource_10").compareTo(2) == 0);
    assertTrue(resourceMap.getEchoCount("resource_11").compareTo(3) == 0);
    assertTrue(resourceMap.getEchoCount("resource_12").compareTo(3) == 0);

    assertTrue(defaulters.contains("resource_11"));
    assertTrue(defaulters.contains("resource_12"));
    assertTrue(defaulters.size() == 2);
  }

}
