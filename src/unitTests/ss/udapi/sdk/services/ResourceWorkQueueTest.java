package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ResourceWorkQueueTest
{
  
  
  @Before
  public void setUp()
  {
    ResourceWorkQueue.initResourceWorkQueue();
  }
  
  
  @Test
  public void testAddQueue()
  {
    ResourceWorkQueue.addQueue("testResourceId1");
    assertTrue(ResourceWorkQueue.exists("testResourceId1"));
  }
  
  
  @Test
  public void testRemoveQueue()
  {
    ResourceWorkQueue.addQueue("testResourceId2");
    assertTrue(ResourceWorkQueue.exists("testResourceId2"));
    ResourceWorkQueue.removeQueue("testResourceId2");
    assertFalse(ResourceWorkQueue.exists("testResourceId2"));
  }


  
  @Test
  public void testManipulateResourceQueue()
  {
    ResourceWorkQueue.addQueue("testResourceId3");
    ResourceWorkQueue.addUOW("testResourceId3", "This is the task to add for processing");
    ResourceWorkQueue.addUOW("testResourceId3", "And a second one");
    
    ResourceWorkQueue.addQueue("testResourceId4");
    ResourceWorkQueue.addUOW("testResourceId4", "This one only gets one task");

    assertEquals("This is the task to add for processing", ResourceWorkQueue.removeUOW("testResourceId3"));

    //we'll do them out of sequence to test they are in fact two separate queues
    assertEquals("This one only gets one task", ResourceWorkQueue.removeUOW("testResourceId4"));
    
    assertEquals("And a second one", ResourceWorkQueue.removeUOW("testResourceId3"));
  }

  
  @Test
  public void testExists()
  {
    ResourceWorkQueue.addQueue("testResourceId10");
    assertTrue(ResourceWorkQueue.exists("testResourceId10"));
    assertFalse(ResourceWorkQueue.exists("testResourceId11"));
  }

}
