package ss.udapi.sdk.examples.model;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.Summary;
import static org.junit.Assert.*;


public class SummaryTest
{
  private Summary summary;
  ModelTestHelper modelHelper;

  
  @Before
  public void prepSummary()
  {
    summary = new Summary();
    modelHelper = new ModelTestHelper();
    modelHelper.buildSummary();
    summary = modelHelper.getSummary();
  }

  
  @Test
  public void testSetGetId()
  {
    assertEquals("ABC-123", summary.getId());
  }

  @Test
  public void testSetGetDate()
  {
    assertEquals("2014-01-14", summary.getDate());
  }

  @Test
  public void testSetGetStartTime()
  {
    assertEquals("14:00", summary.getStartTime());
  }

  @Test
  public void testSetSequence()
  {
    int sequence = 1234;
    assertTrue(sequence == summary.getSequence());
  }

  @Test
  public void testSetTags()
  {
    assertEquals("tag1Key", summary.getTags().get(0).getKey());
  }

  @Test
  public void testSetMatchStatus()
  {
    int matchStatus = 2345;
    assertTrue(matchStatus == summary.getMatchStatus());
  }

}
