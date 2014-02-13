package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class WorkQueueTest
{
  private WorkQueue queue = null;

  
  @Before
  public void setUp() {
    queue = WorkQueue.getWorkQueue();
    WorkQueue.reset();
  }
  
  
  @Test
  public void testAddGetTask() {
    String task1 = "In reality it would be a JSON message, but as it's a string this will do";
    queue.addTask(task1);
    String task2 = "As we are testing a queue, this should always be the second string/task";
    queue.addTask(task2);
    
    assertEquals(task1, queue.getTask());
    assertEquals(task2, queue.getTask());
  }

  @Test
  public void testProveItsFIFO() {
    String task3 = "More strings to test";
    queue.addTask(task3);
    String task4 = "And the last one i hope";
    queue.addTask(task4);

    assertNotEquals(task4, queue.getTask());
    assertNotEquals(task3, queue.getTask());
  }
  
}
