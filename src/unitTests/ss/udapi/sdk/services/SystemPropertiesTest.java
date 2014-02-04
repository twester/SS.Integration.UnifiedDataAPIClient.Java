package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.Test;


/* Can't test too much as this all loaded from the file system, but we do have defaults and overwrites */
public class SystemPropertiesTest
{
  @Test
  public void testGetOnDefaultData()
  {
    assertEquals("60", SystemProperties.get("ss.http_request_timeout"));
  }

  
  @Test
  public void testMissingDataItem()
  {
    assertNull(SystemProperties.get("ss.enterprise"));
  }


  @Test
  public void testOverWriteDefaultData()
  {
    assertEquals("20", SystemProperties.get("ss.echo_sender_interval"));
    SystemProperties.setProperty("ss.echo_sender_interval", "75");
    assertEquals("75", SystemProperties.get("ss.echo_sender_interval"));
  }
  
  
  @Test
  public void testAddNewData()
  {
    assertNull(SystemProperties.get("ss.invincible"));
    SystemProperties.setProperty("ss.invincible", "123");
    assertEquals("123", SystemProperties.get("ss.invincible"));
  }

}
