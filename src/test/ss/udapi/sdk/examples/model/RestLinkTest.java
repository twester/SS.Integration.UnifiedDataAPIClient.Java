package ss.udapi.sdk.examples.model;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.RestLink;
import static org.junit.Assert.*;


public class RestLinkTest
{
  private RestLink restLink;
  private ModelTestHelper testHelper; 
  
  
  @Before
  public void preRestLink()
  {
    testHelper = new ModelTestHelper();
    testHelper.buildLinks();
    restLink = testHelper.getLinks().get(0);
  }
  
  
  @Test
  public void testSetGetRelation()
  {
    assertEquals("amqp", restLink.getRelation());
  }

  
  @Test
  public void testSetGetHref()
  {
    assertEquals("http://endpoint1", restLink.getHref());
  }

  
  @Test
  public void testSetGetVerbs()
  {
    String verb1 = restLink.getVerbs()[0];
    String verb2 = restLink.getVerbs()[1];
    assertEquals("pause", verb1);
    assertEquals("unpause", verb2);
  }

}
