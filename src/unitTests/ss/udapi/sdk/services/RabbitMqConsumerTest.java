package ss.udapi.sdk.services;

import com.rabbitmq.client.Channel;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RabbitMqConsumerTest
{
  private RabbitMqConsumer mqConsumer;
  private Channel channel = mock(Channel.class);
  
  
  @Before
  public void setUp() throws Exception
  {
    mqConsumer = new RabbitMqConsumer(null);
  }

  @Test
  public void testRabbitMqConsumer()
  {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testHandleDeliveryStringEnvelopeBasicPropertiesByteArray()
  {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testHandleCancelOkString()
  {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testHandleCancelString()
  {
    fail("Not yet implemented"); // TODO
  }

}
