package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ResourceWorkQueueTest
{
  private ResourceWorkQueue myQueueRef = null;
  
  @Before
  public void setUp() {
    ResourceWorkQueue.reset();
    myQueueRef = ResourceWorkQueue.getResourceWorkQueue();
  }
  
  
  @Test
  public void testAddQueue() {
    ResourceWorkQueue.addQueue("testResourceId1");
    assertTrue(ResourceWorkQueue.exists("testResourceId1"));
  }
  
  
  @Test
  public void testRemoveQueue() {
    ResourceWorkQueue.addQueue("testResourceId2");
    assertTrue(ResourceWorkQueue.exists("testResourceId2"));
    ResourceWorkQueue.removeQueue("testResourceId2");
    assertFalse(ResourceWorkQueue.exists("testResourceId2"));
  }


  
  @Test
  public void testManipulateResourceQueue() {
    ResourceWorkQueue.addQueue("testResourceId3");
    myQueueRef.addUOW("testResourceId3", "This is the task to add for processing");
    myQueueRef.addUOW("testResourceId3", "And a second one");
    
    ResourceWorkQueue.addQueue("testResourceId4");
    myQueueRef.addUOW("testResourceId4", "This one only gets one task");

    assertEquals("This is the task to add for processing", myQueueRef.removeUOW("testResourceId3"));

    //we'll do them out of sequence to test they are in fact two separate queues
    assertEquals("This one only gets one task", myQueueRef.removeUOW("testResourceId4"));
    
    assertEquals("And a second one", myQueueRef.removeUOW("testResourceId3"));
  }

  
  @Test
  public void testExists()  {
    ResourceWorkQueue.addQueue("testResourceId10");
    assertTrue(ResourceWorkQueue.exists("testResourceId10"));
    assertFalse(ResourceWorkQueue.exists("testResourceId11"));
  }

}
