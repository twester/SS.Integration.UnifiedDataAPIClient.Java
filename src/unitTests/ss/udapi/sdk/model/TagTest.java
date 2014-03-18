package ss.udapi.sdk.model;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.Tag;
import static org.junit.Assert.*;

public class TagTest {
	private Tag tag;
	ModelTestHelper testHelper;

	@Before
	public void setUp() {
		testHelper = new ModelTestHelper();
		testHelper.buildTags();
		tag = testHelper.getTags().get(0);
	}

	@Test
	public void testSetGetValue() {
		assertEquals("tag1Value", tag.getValue());
	}

	@Test
	public void testSetGetKey() {
		assertEquals("tag1Key", tag.getKey());
	}

	@Test
	public void testSetGetId() {
		int id = 111;
		assertTrue(tag.getId() == id);
	}

}
