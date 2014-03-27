//Copyright 2014 Spin Services Limited

//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at

//    http://www.apache.org/licenses/LICENSE-2.0

//Unless required by applicable law or agreed to in writing, software
//distributed under the License is distributed on an "AS IS" BASIS,
//WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//See the License for the specific language governing permissions and
//limitations under the License.

package ss.udapi.sdk.services;

import ss.udapi.sdk.services.JsonHelper;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.RestLink;
import ss.udapi.sdk.model.ServiceRequest;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.*;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

//Meets HTTP connectivity requirements to Sporting Solutions systems.
public class HttpServices {
	
	private static Logger logger = Logger.getLogger(HttpServices.class);
	private static ConnectionKeepAliveStrategy requestTimeout = buildTimeout(Integer
			.parseInt(SystemProperties.get("ss.http_request_timeout")));
	private static ConnectionKeepAliveStrategy loginTimeout = buildTimeout(Integer
			.parseInt(SystemProperties.get("ss.http_login_timeout")));
	private static String serviceAuthToken = null;
	private boolean compressionEnabled;

	// Get a list of available end-points that Sporting Solutions provides.
	public ServiceRequest getSession(String url, boolean compressionEnabled) throws Exception {
		
		this.compressionEnabled = compressionEnabled;
		List<RestItem> loginRestItems = null;
		ServiceRequest loginResp = new ServiceRequest();

		CloseableHttpClient httpClient = null;
		try {
			
			logger.info("Retrieving connection actions from url: " + url);

			// this is only to check whether the URL format is correct
			new URL(url); 
							
			HttpGet httpGet = new HttpGet(url);
			if (compressionEnabled == true) {
				httpGet.setHeader("Accept-Encoding", "gzip");
			}
			
			httpClient = HttpClients.custom()
					.setKeepAliveStrategy(requestTimeout).build();
			ResponseHandler<String> responseHandler = getResponseHandler(401);

			// Call the end-point using connectivity details we've prepared above
			// and get the list of end-points we have access to.
			String responseBody = httpClient.execute(httpGet, responseHandler);

			loginRestItems = JsonHelper.toRestItems(responseBody);
			ArrayList<String> names = new ArrayList<String>();
			for (RestItem item : loginRestItems) {
				names.add(item.getName());
			}
			
			logger.debug("Retrieved connection actions: " + names.toString());
			
			loginResp.setServiceRestItems(loginRestItems);
			return loginResp;
			
		} catch (MalformedURLException urlEx) {
			logger.error("malformed URL: " + url);
			throw urlEx;
		} catch (ClientProtocolException protEx) {
			logger.error("Invalid Client Protocol: " + protEx.getCause());
			throw protEx;
		} catch (IOException ioEx) {
			logger.error("Communication error: to URL [" + url + "]");
			throw ioEx;
		} finally {
			
			try {
				if(httpClient != null)
					httpClient.close();
			} catch (IOException ex) {
				// Can safely be ignored, either the server closed the
				// connection or we didn't open it so there's nothing to do.
			}
		}
	}

	/*
	 * Login into the URL using the credentials provided by Sporting Solutions
	 * to retrieve an authentication token to be used for all subsequent HTTP
	 * calls. It also provides a list of services we are allowed to use.
	 * 
	 * It can return null.
	 */
	public ServiceRequest processLogin(ServiceRequest loginReq,
			String relation, String name) throws Exception {
		
		if (loginReq == null)
			throw new IllegalArgumentException("loginReq must be a valid request");
		
		logger.info("Preparing request for: " + name);
		CloseableHttpClient httpClient = HttpClients.custom()
				.setKeepAliveStrategy(loginTimeout).build();
		List<RestItem> serviceRestItems = null;
		ServiceRequest serviceRequest = new ServiceRequest();

		RestItem loginDetails = getRestItems(loginReq, name);
		if (loginDetails == null) {
			logger.error("Link not found for request " + name);
			return null;
		}

		RestLink link = getLink(loginDetails, relation);
		if (link == null) {
			logger.error("Relation not found for relation: " + relation + " for " + name);
			return null;
		}

		CloseableHttpResponse response = null;
		try {
			
			HttpUriRequest httpAction = new HttpPost(link.getHref());
			if (compressionEnabled == true) {
				httpAction.setHeader("Accept-Encoding", "gzip");
			}
			
			httpAction.setHeader("X-Auth-User", SystemProperties.get("ss.username"));
			httpAction.setHeader("X-Auth-Key", SystemProperties.get("ss.password"));
			httpAction.setHeader("Content-Type", "application/json");

			// Send the request of to retrieve the Authentication Token and the
			// list of available services.
			response = httpClient.execute(httpAction);
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new ClientProtocolException(
						"Unexpected response status: "
								+ response.getStatusLine().getStatusCode()
								+ " while retrieving services for: " + name);
			}

			if (response.getFirstHeader("X-Auth-Token") == null)
				throw new ClientProtocolException("Unexpected response: no auth token found");

			serviceAuthToken = response.getFirstHeader("X-Auth-Token").getValue();
			
			serviceRequest.setAuthToken(serviceAuthToken);
			HttpEntity entity = response.getEntity();
			String jsonResponse = new String(EntityUtils.toByteArray(entity));
			serviceRestItems = JsonHelper.toRestItems(jsonResponse);
			serviceRequest.setServiceRestItems(serviceRestItems);
			
			return serviceRequest;
			
		} catch (ClientProtocolException protEx) {
			logger.error("Invalid Client Protocol: " + protEx.getMessage()
					+ " while retrieving services for: " + name);
			throw protEx;
		} catch (IOException ioEx) {
			logger.error("Communication error" + ioEx.getCause()
					+ " while retrieving services for: " + name);
			throw ioEx;
		} finally {
			try {
				httpClient.close();
			} catch (IOException ex) {
				// Can safely be ignored, either the server closed the
				// connection or we didn't open it so there's nothing to do
			}
		}
	}

	/*
	 * 1) Sends a request to the Sporting Solutions end-point and retrieves
	 * information items which are used for further processing. Protected to
	 * allow unit testing.
	 */
	protected String retrieveBody(ServiceRequest request, String relation,
			String name, String entity) throws Exception {
		
		if (request == null)
			throw new IllegalArgumentException("request object cannot be null");

		if (name == null)
			throw new IllegalArgumentException("name cannot be null");
		
		CloseableHttpClient httpClient = HttpClients.custom()
				.setKeepAliveStrategy(requestTimeout).build();

		Exception lastraisedexception = null;
		
		// Double check we do have an actual usable service
		RestItem serviceDetails = getRestItems(request, name);
		if (serviceDetails == null) {
			logger.error("No details found for: " + relation + " on " + name);
			return null;
		}

		// The retrieve that service's end-point
		RestLink link = getLink(serviceDetails, relation);
		if (link == null) {
			logger.error("No links found for: " + relation + " on " + name);
			return null;
		}

		String responseBody = "";
		try {
			
			// Prepare the HTTP request depending on whether it's an echo
			// (POST), then send the request.
			if (relation.equals("http://api.sportingsolutions.com/rels/stream/batchecho")) {
				
				HttpPost httpPost = new HttpPost(link.getHref());
				httpPost.setHeader("X-Auth-Token", request.getAuthToken());
				httpPost.setHeader("Content-Type", "application/json");
				
				if (compressionEnabled == true) {
					httpPost.setHeader("Accept-Encoding", "gzip");
				}
				
				HttpEntity myEntity = new StringEntity(entity);
				httpPost.setEntity(myEntity);

				CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
				
				if (httpResponse.getStatusLine().getStatusCode() != 202) {
					throw new ClientProtocolException(
							"Unexpected response status for echo request: "
									+ httpResponse.getStatusLine()
											.getStatusCode());
				}

				HttpEntity responseEntity = httpResponse.getEntity();
				if (responseEntity != null) {
					responseBody = new String(
							EntityUtils.toByteArray(responseEntity));
				}
				
				// Or anything else (GET), then send the request.
				
			} else {
				
				HttpGet httpGet = new HttpGet(link.getHref());
				httpGet.setHeader("X-Auth-Token", request.getAuthToken());
				
				if (compressionEnabled == true) {
					httpGet.setHeader("Accept-Encoding", "gzip");
				}
				
				logger.debug("Sending request for relation:[" + relation
						+ "] name:[" + name + "] to href:[" + link.getHref()
						+ "]");
				
				ResponseHandler<String> responseHandler = getResponseHandler(200);
				responseBody = httpClient.execute(httpGet, responseHandler);
				
			}
		} catch (ClientProtocolException protEx) {
			logger.error("Invalid Client Protocol: " + protEx.getMessage()
					+ " while processing : " + name);
			lastraisedexception = protEx;
		} catch (IOException ioEx) {
			logger.error("Communication error: " + ioEx.getMessage()
					+ " while processing : " + name);
			lastraisedexception = ioEx;
		} finally {
			try {
				httpClient.close();
			} catch (IOException ex) {
				// Can safely be ignored, either the server closed the
				// connection or we didn't open it so there's nothing to do
			}
		}
		
		if (lastraisedexception != null)
			throw lastraisedexception;

		// Then return the response we got from Sporting Solutions.
		return responseBody;
	}

	/*
	 * 2) Called by most HTTP interactions with the exception of echo and
	 * snapshot requests as they have different behaviour. These interactions
	 * need a parsed set of RestItem(s)
	 */
	public ServiceRequest processRequest(ServiceRequest request,
			String relation, String name) throws Exception {
		
		return processRequest(request, relation, name, "n/a");
	}

	/*
	 * 3) Called directly by BatchEcho which provides the echo message body
	 * (entity). Then return a fully formed ServiceRequest which contains the
	 * http session Authentication token and the RestItems parsed from the
	 * response retrieved from retrieveBody() (1 above)
	 * 
	 * Also called by ServiceRequest (2 above) which needs a ServiceRequest item
	 * but does not provide/send a body to the end-points as all transactions are
	 * GETs.
	 */
	public ServiceRequest processRequest(ServiceRequest request,
			String relation, String name, String entity) throws Exception {
		
		ServiceRequest response = new ServiceRequest();
		String body = retrieveBody(request, relation, name, entity);
		List<RestItem> serviceRestItems = JsonHelper.toRestItems(body);

		response.setServiceRestItems(serviceRestItems);
		response.setAuthToken(request.getAuthToken());
		return response;
	}

	/*
	 * 4) Needs the raw data processRequest (1 above) returns, we don't do
	 * anything with this item. It's enormous we eventually just pass it to the
	 * client code for processing as they deem appropriate.
	 */
	public String getSnapshot(ServiceRequest snapShot, String relation, String fixture) throws Exception {
		return retrieveBody(snapShot, relation, fixture, "n/a");
	}

	private RestItem getRestItems(ServiceRequest request, String name) {
		
		if (request == null)
			return null;
		
		RestItem matchingRest = null;
		Iterator<RestItem> itemRestIterator = request.getServiceRestItems().iterator();
		
		do {
			matchingRest = itemRestIterator.next();
			if (matchingRest.getName().compareTo(name) != 0) {
				matchingRest = null;
			}
		} while (itemRestIterator.hasNext() && (matchingRest == null));
		
		return matchingRest;
	}

	private RestLink getLink(RestItem request, String relation) {
		
		if (request == null)
			return null;
		
		RestLink link = null;
		Iterator<RestLink> linkIterator = request.getLinks().iterator();
		
		do {
			link = linkIterator.next();
			if (link.getRelation().compareTo(relation) != 0) {
				link = null;
			}
		} while (linkIterator.hasNext() && (link == null));
		
		return link;
	}

	private ResponseHandler<String> getResponseHandler(final int validStatus) {
		return new ResponseHandler<String>() {
			public String handleResponse(final HttpResponse response)
					throws ClientProtocolException, IOException {
				int responseStatus = response.getStatusLine().getStatusCode();
				if (responseStatus == validStatus) {
					logger.debug("Http connection status " + responseStatus);
					HttpEntity entity = response.getEntity();
					return entity != null ? EntityUtils.toString(entity) : null;
				} else {
					throw new ClientProtocolException(
							"Unexpected http connection response status: "
									+ responseStatus);
				}
			}
		};
	}

	private static ConnectionKeepAliveStrategy buildTimeout(final int timeout) {
		return new ConnectionKeepAliveStrategy() {
			public long getKeepAliveDuration(HttpResponse response,
					HttpContext context) {
				return timeout * 1000;
			}
		};
	}

}
