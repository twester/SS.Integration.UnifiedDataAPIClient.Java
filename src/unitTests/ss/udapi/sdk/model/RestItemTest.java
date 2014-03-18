package ss.udapi.sdk.model;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.RestLink;
import ss.udapi.sdk.model.Summary;
import static org.junit.Assert.*;

//These are to make sure beans still work once they become immutable

public class RestItemTest {
	private RestItem restItem;
	private ModelTestHelper testHelper;

	@Before
	public void setUp() {
		testHelper = new ModelTestHelper();
		testHelper.buildRestItems();
		restItem = testHelper.getRestItem();
	}

	@Test
	public void testSetGetName() {
		assertEquals("Tennis", restItem.getName());
	}

	@Test
	public void testSetGetContent() {
		Summary summary = testHelper.getSummary();

		assertEquals(summary.getDate(), restItem.getContent().getDate());
	}

	@Test
	public void testSetGetLinks() {
		List<RestLink> links = testHelper.getLinks();
		RestLink link = links.get(0);

		assertEquals(link.getHref(), restItem.getLinks().get(0).getHref());
	}

}