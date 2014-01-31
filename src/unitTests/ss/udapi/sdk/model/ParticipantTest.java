package ss.udapi.sdk.examples.model;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.Participant;
import static org.junit.Assert.*;


public class ParticipantTest
{
  private Participant participant;
  
  
  @Before
  public void prepParticipants()  {
    participant = new Participant();
  }

  
  @Test
  public void testSetGetNameTest()  {
    participant.setName("Fern");
    assertEquals("Fern", participant.getName());
  }
  
  
  @Test
  public void testSetGetId()  {
    int id = 1234;
    participant.setId(id);
    assertTrue(participant.getId() == id);
  }

}
