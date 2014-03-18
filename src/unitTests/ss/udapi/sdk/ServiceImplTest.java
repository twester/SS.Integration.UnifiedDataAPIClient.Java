package ss.udapi.sdk;

import ss.udapi.sdk.interfaces.Feature;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.services.HttpServices;
import ss.udapi.sdk.services.JsonHelper;

import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.mockito.Mockito.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class ServiceImplTest {
	private HttpServices httpSvcs = mock(HttpServices.class);
	private RestItem restItem;
	private ServiceRequest resRequest;
	private ServiceRequest resResponse = new ServiceRequest();
	private ServiceImpl service;
	private FeatureImpl responseFeature;

	@Before
	public void setUp() throws Exception {
		String requestBody = "[{\"Relation\":\"http://api.sportingsolutions.com/rels/udapi\",\"Name\":\"UnifiedDataAPI\",\"Tags\":{\"Version\":\"1.0.0.0\"},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/features/list\",\"Href\":\"http://xxx.test123url.com/UnifiedDataAPI/VqjDlq96GqIGyTLSdHsN0HsvwgJG\",\"Verbs\":[\"GET\"]}]},{\"Relation\":\"http://api.sportingsolutions.com/rels/customerrestservice\",\"Name\":\"CustomerRestService\",\"Tags\":{\"Version\":\"6.0.0.0\"},\"Links\":[{\"Relation\":\"http://c2e.sportingingsolutions.com/rels\",\"Href\":\"http://xxx.test123url.com//zvEQ9lq_aqJ5k9t5gDM34f5JYgtB\",\"Verbs\":[\"GET\"]}]},{\"Relation\":\"http://api.sportingsolutions.com/rels/usermanagementservice\",\"Name\":\"UserManagementService\",\"Tags\":{\"Version\":\"1.0.0.0\"},\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/usermanagementservice\",\"Href\":\"http://connectappcui.sportingsolutions.com/SecurityService/UserManagementService/MoplPuK-KJ4VlkWk-lZewpc_UHs0\",\"Verbs\":[\"GET\"]}]}]";
		List<RestItem> restItems = JsonHelper.toRestItems(requestBody);

		resRequest = new ServiceRequest();
		resRequest.setAuthToken("AUTH_TOKEN_01");
		resRequest.setServiceRestItems(restItems);
		restItem = getRestItems(resRequest, "UnifiedDataAPI");
		service = new ServiceImpl(restItem, resRequest);

		String responseBody = "[{\"Name\":\"AmericanFootball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/AmericanFootball/NFUV1zO3kgXJuyqY3NQoJcmQiZM4\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"AustralianRules\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/AustralianRules/KkwI3jVOYnxV8M0d0dmHY9mzle0y\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Badminton\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Badminton/7aRyxGklZzTyNL5cZYyLRxAX6CdG\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Baseball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Baseball/caeDVIuJiBUcQUqs4JYfYo_feZ5B\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Basketball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Basketball/dECYjFmyPH0c9-VG3RJ6VMEko5I1\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"BeachVolleyball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/BeachVolleyball/XaMVGwGTInMMgYiBr9igjk2Nu-ky\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Boxing\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Boxing/gRmzsmrJbzkhk57kSBES50BELjg1\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Cricket\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Cricket/9hAUfrQ_-w59TqdpTUrzm1qdx5k2\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Darts\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Darts/hor57BNMNb6Xw1Sg8qAkLpqyjUY0\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Football\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Football/YFUBu4lobCJeX1v-yFZMyab5REU3\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"GaelicFootball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/GaelicFootball/VS4MKcGS6ZyDFaPJqD3daKlE958x\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"GaelicHurling\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/GaelicHurling/UWwoppGz7X-LxTOu_OBXeAm_zAIz\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Handball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Handball/1VKss1xr8m7iIJIRR2pil2YhppxB\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"HorseRacing\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/HorseRacing/T5wfx_s8XXR6Sn4sOiUKKUqE2dY2\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"IceHockey\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/IceHockey/fSiJMgblNTzziD2_U2vIN-tyI38x\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"RugbyLeague\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/RugbyLeague/Z-Dw1jCxSLryHAsUJuK8m0kNUBs2\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"RugbyUnion\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/RugbyUnion/9RVxBEsvoxxo-I_QEihAAcMsmLxC\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Snooker\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Snooker/hi1T4j4gaKmlsDWhCveOHP0_MOpC\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Squash\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Squash/p9cDqK_fbCkXS7ASVy0c2jeV4wgx\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Tennis\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Tennis/3orxELMR1XdTpCl-6TmcsFbEjFY3\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"TestCricket\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/TestCricket/3qCi0JRCQ5Spolejkzw0I0oq20Ax\",\"Verbs\":[\"Get\"]}]},{\"Name\":\"Volleyball\",\"Links\":[{\"Relation\":\"http://api.sportingsolutions.com/rels/resources/list\",\"Href\":\"http://xxx.tes123turl.com/UnifiedDataAPI/Volleyball/aXWbUhZ2AcZbLyPi7uPerP_5DjQ5\",\"Verbs\":[\"Get\"]}]}]";
		List<RestItem> responseItems = JsonHelper.toRestItems(responseBody);
		resResponse.setServiceRestItems(responseItems);
		resResponse.setAuthToken("AUTH_TOKEN_01");
	}

	@Test
	public void testGetFeature() {
		/*
		 * Here we mock httpsevices (sending a request for all resources for
		 * this feature). The unit test is asserting that FeatureImpl filters
		 * the response and returns the a ResourceImpl with a set of resources
		 * for this feature.
		 */
		doAnswer(new Answer<ServiceRequest>() {
			public ServiceRequest answer(InvocationOnMock invocation)
					throws Throwable {
				return resResponse;
			}
		}).when(httpSvcs).processRequest(resRequest,
				"http://api.sportingsolutions.com/rels/features/list",
				"UnifiedDataAPI");

		service.setHttpSvcs(httpSvcs);
		responseFeature = (FeatureImpl) service.getFeature("AmericanFootball");

		// So, does the URL for the feature we want (amongst all the features
		// ServiceImpl gets) match what we expect?
		assertEquals(
				"http://xxx.tes123turl.com/UnifiedDataAPI/AmericanFootball/NFUV1zO3kgXJuyqY3NQoJcmQiZM4",
				responseFeature.getFeatureHref());
	}

	@Test
	public void testGetFeatureNotFound() {
		/*
		 * Here we mock httpsevices (sending a request for all resources for
		 * this feature). The unit test is asserting that FeatureImpl filters
		 * the response and returns the a ResourceImpl with a set of resources
		 * for this feature.
		 */
		doAnswer(new Answer<ServiceRequest>() {
			public ServiceRequest answer(InvocationOnMock invocation)
					throws Throwable {
				return resResponse;
			}
		}).when(httpSvcs).processRequest(resRequest,
				"http://api.sportingsolutions.com/rels/features/list",
				"UnifiedDataAPI");

		service.setHttpSvcs(httpSvcs);
		responseFeature = (FeatureImpl) service.getFeature("Skittles");

		// We should get nothing back at all
		assertNull(responseFeature);
	}

	@Test
	public void testGetResources() {
		/*
		 * Here we mock httpsevices (sending a request for all resources for
		 * this feature). The unit test is asserting that FeatureImpl filters
		 * the response and returns the a ResourceImpl with a set of resources
		 * for this feature.
		 */
		doAnswer(new Answer<ServiceRequest>() {
			public ServiceRequest answer(InvocationOnMock invocation)
					throws Throwable {
				return resResponse;
			}
		}).when(httpSvcs).processRequest(resRequest,
				"http://api.sportingsolutions.com/rels/features/list",
				"UnifiedDataAPI");

		service.setHttpSvcs(httpSvcs);
		List<Feature> responseSet = service.getFeatures();

		// So, does the ID we should get for this resource match the name we've
		// given it?
		assertEquals(22, responseSet.size());
	}

	@Test
	public void testGetName() {
		assertEquals("UnifiedDataAPI", service.getName());
	}

	// find the request we need
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
