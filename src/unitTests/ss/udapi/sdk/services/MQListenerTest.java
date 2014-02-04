package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class MQListenerTest
{
  private Channel channel = mock(Channel.class);
  private Connection conn = mock(Connection.class);
  private ConnectionFactory connFact= mock(ConnectionFactory.class);
  
  private MQListener mqListener = MQListener.getMQListener("http://testurl");
  
  @Before
  public void setUp() throws Exception
  {
  }

  @Test
  public void testRun()
  {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testDisconnect()
  {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testRemoveMapping()
  {
    fail("Not yet implemented"); // TODO
  }

  @Test
  public void testSetResources()
  {
    ResourceSession session = new ResourceSession("amqp://val1%40uname:pw@1.1.1.1:5672/vhost/amq.gen-pz15SpXWf5mEKHgACG8D9g","testResource1");
    mqListener.setResources(session);
    assertTrue(mqListener.countPendingResources() == 1);

    mqListener.setResources(session);
    assertTrue(mqListener.countPendingResources() == 2);
  }

}
