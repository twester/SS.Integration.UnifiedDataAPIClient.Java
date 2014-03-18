package ss.udapi.sdk;

import ss.udapi.sdk.interfaces.Credentials;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class CredentialsImplTest {
	Credentials credentials;

	@Before
	public void testCredentialsImpl() {
		credentials = new CredentialsImpl("user99", "password99");
	}

	@Test
	public void testGetUserName() {
		assertEquals("user99", credentials.getUserName());
	}

	@Test
	public void testGetPassword() {
		assertEquals("password99", credentials.getPassword());
	}

}
