package ss.udapi.sdk;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.EchoSender;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.JsonHelper;
import ss.udapi.sdk.services.MQListener;
import ss.udapi.sdk.services.ResourceWorkerMap;
import ss.udapi.sdk.services.ServiceThreadExecutor;
import ss.udapi.sdk.services.SystemProperties;
import ss.udapi.sdk.services.WorkQueueMonitor;
import ss.udapi.sdk.streaming.ConnectedEvent;
import ss.udapi.sdk.streaming.DisconnectedEvent;
import ss.udapi.sdk.streaming.Event;
import ss.udapi.sdk.streaming.StreamEvent;
import ss.udapi.sdk.streaming.SynchronizationEvent;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

/* Yes, this is an integration test being called by JUNIT, I could create a separate script to run exactly the same tests.
 * JUNIT is only being used in lieueu of a separate script.
 * 
 * And yes the tests do have more than one assertion, they can be broken up into individual tests but there's so much to prep up
 * before each test it's quite expensive. 
 */

public class IntegrationTest {
	private RestItem restItem;
	private List<RestItem> requestItems;
	private List<RestItem> responseItems;
	private ServiceRequest requestSR;
	private ServiceRequest responseSR = new ServiceRequest();

	private HttpServices httpSvcs = mock(HttpServices.class);

	private boolean connectedEventCalled = false;
	private boolean disconnectedEventCalled = false;
	private boolean streamEventCalled = false;

	private Channel channel = null;
	private Connection connection = null;
	private ConnectionFactory connFactory;
	private String basicTestQueue = "testQueue";
	private String intTestQueue = "integrationTestQueue";

	private String resourceId = "46NtalSfupT7w2MxuudoYUd9CKw";
	private String resources = "[{\"Name\":\"Fern v NotFern\",\"Content\":{\"Id\":\"46NtalSfupT7w2MxuudoYUd9CKw\",\"StartTime\":\"2014-01-21T14:54:54Z\",\"Sequence\":449,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"Fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"NotFern\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"An important match\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/snapshot/Skittles/46NtalSfupT7w2MxuudoYUd9CKw/bYQ4NJ0ckn-oAMwylfJwzMbAREQ3\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/stream/Skittles/46NtalSfupT7w2MxuudoYUd9CKw/sdSfNkO9XsaI9CpGMxOLnTYhh1Y1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/sequence\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/sequence/Skittles/46NtalSfupT7w2MxuudoYUd9CKw/Ly7yvGLELxxThyA-88QT4riXoFU1\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/stream/echo/I546imiMYzaxVfuUQVUSWK4Nf1pF\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/stream/batchecho/n21SkC75lqm5y716xvV6LSSUtVs5\",\"Verbs\":[\"Post\"]}]},{\"Name\":\"fern v johnny alien\",\"Content\":{\"Id\":\"5IyktEE--jyYCP4IMNgFjoXegiw\",\"StartTime\":\"2014-02-05T08:57:15Z\",\"Sequence\":57,\"Tags\":[{\"Id\":1,\"Key\":\"Participant\",\"Value\":\"fern\"},{\"Id\":2,\"Key\":\"Participant\",\"Value\":\"johnny alien\"},{\"Id\":3,\"Key\":\"Competition\",\"Value\":\"another match\"}],\"MatchStatus\":40},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/snapshot\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/snapshot/Skittles/5IyktEE--jyYCP4IMNgFjoXegiw/txi1aKDfyRlQWZc3-w4vwsVGP_Qx\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/amqp\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/stream/Skittles/5IyktEE--jyYCP4IMNgFjoXegiw/T29o1GI23kOIZJGJHVegOfV0_HMx\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://127.0.0.1:8080/rels/sequence\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/sequence/Skittles/5IyktEE--jyYCP4IMNgFjoXegiw/J9S14jW_GlKoz9RQygrX5Q7xX_hE\",\"Verbs\":[\"Get\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/echo\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/stream/echo/wjIyvcuD67AChr32xwpJoFDQ0pw0\",\"Verbs\":[\"Post\"]},{\"Relation\":\"http://api.sportingsolutions.com/rels/stream/batchecho\",\"Href\":\"http://127.0.0.1:8080/UnifiedDataAPI/stream/batchecho/nyounZMjUxEGPcQ_o44qlXQazhQx\",\"Verbs\":[\"Post\"]}]}]";
	private String mqMessage = "{\"Relation\":\"http://c2e.sportingingsolutions.com/rels/v006/FootballOdds\",\"Content\":{\"FixtureName\":\"Fern v NotFern\",\"Id\":\"46NtalSfupT7w2MxuudoYUd9CKw\",\"Sequence\":88,\"MatchStatus\":40,\"Markets\":[],\"GameState\":{\"matchsummary\":\"0-0 00:00 1st\",\"concisematchsummary\":\"0-0 00:00 1st\",\"homepenalties\":0,\"awaypenalties\":0,\"homecorners\":0,\"awaycorners\":0,\"homeredcards\":0,\"awayredcards\":0,\"homeyellowcards\":0,\"awayyellowcards\":0,\"homewoodwork\":0,\"awaywoodwork\":0,\"homesubstitutions\":0,\"awaysubstitutions\":0,\"goals\":null},\"Epoch\":3,\"LastEpochChangeReason\":[40],\"Timestamp\":\"2014-02-11T16:39:57Z\"}}";
	private String recvdFromMq = "";
	private boolean svcThreadRunning = false;

	@Before
	public void setUp() throws Exception {
		EchoSender.terminate();
		MQListener.terminate();
		WorkQueueMonitor.terminate();

		if (svcThreadRunning == false) {
			ServiceThreadExecutor.createExecutor();
			svcThreadRunning = true;
		}

		/*
		 * CtagResourceMap.reset(); EchoResourceMap.reset();
		 * ResourceWorkerMap.reset(); ResourceWorkQueue.reset();
		 * WorkQueue.reset();
		 */

		ResourceWorkerMap.initWorkerMap();

		SystemProperties.setProperty("ss.echo_sender_interval", "20");
		SystemProperties.setProperty("ss.echo_max_missed_echos", "3");
		connFactory = new ConnectionFactory();
		connFactory.setHost("127.0.0.1");
		connFactory.setVirtualHost("/fern");
		connFactory.setUsername("sportingsolutions@fern");
		connFactory.setPassword("sporting");
		connection = connFactory.newConnection();

		channel = connection.createChannel();

		// Prepare test data to create the Resource
		requestItems = JsonHelper.toRestItems(resources);
		requestSR = new ServiceRequest();
		requestSR.setAuthToken("AUTH_TOKEN_01");
		requestSR.setServiceRestItems(requestItems);
		restItem = getRestItems(requestSR, "Fern v NotFern");

		String responseAMQEndPoint = "[{\"Name\":\"stream\",\"Links\":[{\"Relation\":\"amqp\",\"Href\":\"amqp://sportingsolutions%40fern:sporting@127.0.0.1:5672/fern/"
				+ intTestQueue + "\"}]}]";
		responseItems = JsonHelper.toRestItems(responseAMQEndPoint);
		responseSR.setAuthToken("AUTH_TOKEN_01");
		responseSR.setServiceRestItems(responseItems);

		// Here we mock httpsevices. It still's try to connect but we have a
		// response ready for the call
		doAnswer(new Answer<ServiceRequest>() {
			public ServiceRequest answer(InvocationOnMock invocation)
					throws Throwable {
				return responseSR;
			}
		}).when(httpSvcs).processRequest(requestSR,
				"http://api.sportingsolutions.com/rels/stream/amqp",
				"Fern v NotFern");

		connectedEventCalled = false;
		disconnectedEventCalled = false;
		streamEventCalled = false;
	}

	@Test
	public void testBasicConnectivity() {
		String sentMsg = "sent";
		String recvdMsg = "received";
		try {
			channel.queueDeclare(basicTestQueue, false, false, false, null);
			sentMsg = "testBasicConnectivity";
			channel.basicPublish("", basicTestQueue, null, sentMsg.getBytes());

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(basicTestQueue, true, consumer);
			QueueingConsumer.Delivery delivery = consumer.nextDelivery(1500);
			if (delivery != null) {
				recvdMsg = new String(delivery.getBody());
			}

		} catch (Exception ex) {
			fail("Connectivity error to MQ while running Test" + ex);
		} finally {
			try {
				channel.close();
				connection.close();
			} catch (Exception ex) {
				fail("Connectivity error to MQ while running Test" + ex);
			}
		}
		assertEquals(sentMsg, recvdMsg);
	}

	@Test
	public void testConnectedDisconnectedEvents() {
		// Instantiate the resource
		ResourceImpl resourceImpl = new ResourceImpl(restItem, requestSR);
		resourceImpl.setHttpSvcs(httpSvcs);

		try {
			channel.queueDeclare(intTestQueue, false, false, false, null);
		} catch (Exception ex) {
			fail("Connectivity error to MQ while running Test" + ex);
			try {
				channel.close();
				connection.close();
			} catch (Exception otherEx) {
				fail("Error while closing MQ channle, may have been closed alreadyConnectivity error to MQ while running Test"
						+ ex);
			}
		}

		// 1st test: We should, at this stage not received a ConnectedEvent yet
		assertFalse(connectedEventCalled);

		// Here we build the queue ResourceImpl is going to connect to and we
		// will later publish to
		resourceImpl.startStreaming(createListeners());

		// Have a rest to let all the services start up properly
		try {
			Thread.sleep(5000);
		} catch (Exception ex) {
			// Cleaning a queue gave us problems? yikes!
		}

		// 2nd test: did we get a callback on ConnectedEvent (Connected to MQ) ?
		assertTrue(connectedEventCalled);

		// 3rd test: We should, at this stage not received a DisconnectedEvent
		// yet
		assertFalse(disconnectedEventCalled);

		resourceImpl.mqDisconnectEvent();
		// Have a rest to let all the services shut down up properly
		try {
			Thread.sleep(2000);
			channel.queuePurge(intTestQueue);
		} catch (Exception ex) {
			// Bit of housekeeping
		}

		// 4th test: did we get a callback on DisconnectedEvent (Disconnected
		// from MQ) ?
		assertTrue(disconnectedEventCalled);
	}

	@Test
	public void testStreamingEvent() {
		// Instantiate the resource
		ResourceImpl resourceImpl = new ResourceImpl(restItem, requestSR);
		resourceImpl.setHttpSvcs(httpSvcs);

		try {
			channel.queueDeclare(intTestQueue, false, false, false, null);
		} catch (Exception ex) {
			fail("Connectivity error to MQ while running Test" + ex);
			try {
				channel.close();
				connection.close();
			} catch (Exception otherEx) {
				// We couldn't close a connection that's already closed or
				// hasn't been open...
			}
		}

		// Here we build the queue ResourceImpl is going to connect to and we
		// will later publish to
		resourceImpl.startStreaming(createListeners());

		// Have a rest to let all the services start up properly
		try {
			Thread.sleep(5000);
			channel.basicPublish("", intTestQueue, null, mqMessage.getBytes());
		} catch (Exception ex) {
			fail("Cannot publish to MQ on test environment" + ex);
		}

		// A little rest to let the publication go through
		try {
			Thread.sleep(2000);
		} catch (Exception ex) {
		}

		// 1st test: did we get a callback on ConnectedEvent (Connected to MQ) ?
		assertTrue(connectedEventCalled);

		// 2nd test: The stream received event was triggered
		assertTrue(streamEventCalled);

		// 3rd test: And the message received was the same as posted on the
		// Queue
		assertEquals(mqMessage, recvdFromMq);

		resourceImpl.mqDisconnectEvent();
		// Have a rest to let all the services shut down up properly
		try {
			Thread.sleep(2000);
			channel.queuePurge(intTestQueue);
		} catch (Exception ex) {
			// Bit of housekeeping
		}
	}

	@Test
	public void testDisconnectViaExceeded() {
		// Make the test run a little faster
		SystemProperties.setProperty("ss.echo_sender_interval", "1");
		SystemProperties.setProperty("ss.echo_max_missed_echos", "2");
		// Instatiate the resource
		ResourceImpl resourceImpl = new ResourceImpl(restItem, requestSR);
		resourceImpl.setHttpSvcs(httpSvcs);

		try {
			channel.queueDeclare(intTestQueue, false, false, false, null);
		} catch (Exception ex) {
			fail("Connectivity error to MQ while running Test" + ex);
			try {
				channel.close();
				connection.close();
			} catch (Exception otherEx) {
				// We couldn't close a connection that's already closed or
				// hasn't been open...
			}
		}
		// Here we build the queue ResourceImpl is going to connect to and we
		// will later publish to
		resourceImpl.startStreaming(createListeners());
		// Have a rest to let the echo count expire
		try {
			Thread.sleep(30000);
		} catch (Exception ex) {
			fail("Interrupted sleep cycle" + ex);
		}
		// 1st test: did the echo count get increased by the batch echo sends?
		assertTrue(disconnectedEventCalled);

		try {
			channel.queuePurge(resourceId);
			channel.close();
			connection.close();
		} catch (Exception otherEx) {
			// We couldn't close a connection that's already closed or hasn't
			// been open...
		}
	}

	// Set up the listeners
	private List<Event> createListeners() {
		List<Event> streamingEvents = new ArrayList<Event>();

		streamingEvents.add(new ConnectedEvent() {
			public void onEvent(String message) {
				connectedEventCalled = true;
			}
		});

		streamingEvents.add(new StreamEvent() {
			public void onEvent(String message) {
				streamEventCalled = true;
				recvdFromMq = message;
			}
		});

		streamingEvents.add(new DisconnectedEvent() {
			public void onEvent(String message) {
				disconnectedEventCalled = true;
			}
		});

		streamingEvents.add(new SynchronizationEvent() {
			public void onEvent(String message) {
				// NOP
			}
		});

		return streamingEvents;
	}

	// Find the request we need
	private RestItem getRestItems(ServiceRequest request, String name) {
		RestItem matchingRest = null;
		Iterator<RestItem> itemRestIterator = request.getServiceRestItems()
				.iterator();
		do {
			matchingRest = itemRestIterator.next();
			if (matchingRest.getName().compareTo(name) != 0) {
				matchingRest = null;
			}
		} while (itemRestIterator.hasNext() && (matchingRest == null));
		return matchingRest;
	}

}
