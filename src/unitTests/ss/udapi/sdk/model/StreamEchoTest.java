package ss.udapi.sdk.model;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.StreamEcho;
import static org.junit.Assert.*;

public class StreamEchoTest {
	private StreamEcho streamEcho;

	@Before
	public void setUp() {
		streamEcho = new StreamEcho();
	}

	@Test
	public void testSetGetHost() {
		streamEcho.setHost("http://google.com");
		assertEquals("http://google.com", streamEcho.getHost());
	}

	@Test
	public void testSetGetQueue() {
		streamEcho.setQueue("jms://server/Queue");
		assertEquals("jms://server/Queue", streamEcho.getQueue());
	}

	@Test
	public void testSetGetMessage() {
		streamEcho.setMessage("jason-payload");
		assertEquals("jason-payload", streamEcho.getMessage());
	}

}
