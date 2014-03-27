package ss.udapi.sdk.services;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ss.udapi.sdk.ResourceImpl;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class WorkQueueMonitorTest {
	private WorkQueueMonitor monitor = WorkQueueMonitor.getMonitor();
	private WorkQueue workQueue = null;
	private ResourceWorkQueue resourceWorkQueue = null;
	private ResourceImpl resource = mock(ResourceImpl.class);
	private boolean resourceImplCalled;
	private String body = "{\"Relation\":\"http://c2e.sportingingsolutions.com/rels/v006/FootballOdds\",\"Content\":{\"FixtureName\":\"fern v johnny alien\",\"Id\":\"5IyktEE--jyYCP4IMNgFjoXegiw\",\"Sequence\":57,\"MatchStatus\":40,\"Markets\":[],\"GameState\":{\"matchsummary\":\"0-0 00:00 1st\",\"concisematchsummary\":\"0-0 00:00 1st\",\"homepenalties\":0,\"awaypenalties\":0,\"homecorners\":0,\"awaycorners\":0,\"homeredcards\":0,\"awayredcards\":0,\"homeyellowcards\":0,\"awayyellowcards\":0,\"homewoodwork\":0,\"awaywoodwork\":0,\"homesubstitutions\":0,\"awaysubstitutions\":0,\"goals\":null},\"Epoch\":3,\"LastEpochChangeReason\":[40],\"Timestamp\":\"2014-02-05T15:30:51Z\"}}";

	@Before
	public void setUp() throws Exception {
		resourceImplCalled = false;

		workQueue = WorkQueue.getWorkQueue();
		WorkQueue.reset();
		resourceWorkQueue = ResourceWorkQueue.getResourceWorkQueue();
		ResourceWorkQueue.reset();
		ResourceWorkerMap.initWorkerMap();
		ResourceWorkerMap.reset();
		ResourceWorkerMap.addResource("5IyktEE--jyYCP4IMNgFjoXegiw", resource);
		ResourceWorkQueue.addQueue("5IyktEE--jyYCP4IMNgFjoXegiw");
	}

	@Test
	public void MonitorDequeuesAssignsWorkToProcessor() {
		int currentSize = WorkQueue.getWorkQueue().size();
		workQueue.addTask(body);
		// So here we have the newly added task
		assertTrue(workQueue.size() == currentSize + 1);

		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) throws Throwable {
				resourceImplCalled = true;
				resourceWorkQueue.removeUOW("5IyktEE--jyYCP4IMNgFjoXegiw");
				return null;
			}
		}).when(resource).streamData();

		// Start a thread to pull off the data and call a FixtureActionProcessor
		// to pass the data onto a resourceImpl and run it.
		Thread testThread = new Thread(monitor);
		testThread.start();

		// I know! sleep methods in unit test, but we are a testing a method
		// which itself goes to sleep, so we have to do it.
		try {
			Thread.sleep(500);
		} catch (InterruptedException ex) {
			fail("The echo thread was interrupted before test completed.");
		}

		// And check the data was pulled off
		assertTrue(workQueue.size() == 0);

		/*
		 * Now check that the FixtureActionProcessor pulled off this resource's
		 * implementation to process the data, this is a slight bit of
		 * integration testing WorkQueueMonitor calls FixtureActionProcessor
		 * which in turn calls the resource. As we can't directly look in
		 * FixtureAction action processor without exposing it's innards and
		 * adding hooks for testing we simply look at the ResourceImpl it calls.
		 * et voila!
		 */
		assertTrue(resourceImplCalled);
		WorkQueueMonitor.terminate();
	}

}
