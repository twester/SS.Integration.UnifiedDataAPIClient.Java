package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class ResourceSessionTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testResourceSessionAndGetters() {
		ResourceSession session = new ResourceSession("amqp1", "resource2");

		assertEquals("amqp1", session.getAmqpDest());
		assertEquals("resource2", session.getResourceId());

	}

}
