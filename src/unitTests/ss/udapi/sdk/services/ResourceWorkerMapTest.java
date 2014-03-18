package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import org.mockito.runners.MockitoJUnitRunner;

import ss.udapi.sdk.ResourceImpl;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceWorkerMapTest {
	private ResourceImpl resource1 = mock(ResourceImpl.class);
	private static final String resId1 = "resource1";
	private ResourceImpl resource2 = mock(ResourceImpl.class);
	private static final String resId2 = "resource1";
	private ResourceImpl resource3 = mock(ResourceImpl.class);
	private static final String resId3 = "resource1";

	@Before
	public void setUp() {
		ResourceWorkerMap.initWorkerMap();
		ResourceWorkerMap.reset();
	}

	@Test
	public void testAddResource() {
		ResourceWorkerMap.addResource(resId1, resource1);
		assertTrue(ResourceWorkerMap.exists(resId1));
	}

	@Test
	public void testGetResourceImpl() {
		ResourceWorkerMap.addResource(resId2, resource2);
		assertTrue(ResourceWorkerMap.exists(resId2));
		ResourceImpl tempRes = (ResourceImpl) ResourceWorkerMap
				.getResourceImpl(resId2);
		assertNotNull(tempRes);
	}

	@Test
	public void testGetMissingImpl() {
		ResourceImpl tempRes = (ResourceImpl) ResourceWorkerMap
				.getResourceImpl("noResource");
		assertNull(tempRes);
	}

	@Test
	public void testRemoveResource() {
		ResourceWorkerMap.addResource(resId3, resource3);
		assertTrue(ResourceWorkerMap.exists(resId3));
		ResourceWorkerMap.removeResource(resId3);
		assertFalse(ResourceWorkerMap.exists(resId3));
	}
}
