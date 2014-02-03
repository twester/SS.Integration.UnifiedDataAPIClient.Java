package ss.udapi.sdk.model;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.RestLink;
import static org.junit.Assert.*;


public class RestLinkTest
{
  private RestLink restLink1;
  private RestLink restLink2;
  private ModelTestHelper testHelper; 
  
  
  @Before
  public void setUp()
  {
    testHelper = new ModelTestHelper();
    testHelper.buildLinks();
    restLink1 = testHelper.getLinks().get(0);
    restLink2 = testHelper.getLinks().get(1);
  }
  
  
  @Test
  public void testSetGetRelation()
  {
    assertEquals("amqp", restLink1.getRelation());
  }

  
  @Test
  public void testSetGetHref()
  {
    assertEquals("http://endpoint1", restLink1.getHref());
  }

  
  @Test
  public void testSetGetVerbs()
  {
    String verb1 = restLink1.getVerbs()[0];
    String verb2 = restLink2.getVerbs()[0];
    assertEquals("GET", verb1);
    assertEquals("POST", verb2);
  }

}
