//Copyright 2012 Spin Services Limited

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


public class HttpServices
{
  private static Logger logger = Logger.getLogger(HttpServices.class);
  private static ConnectionKeepAliveStrategy requestTimeout = buildTimeout(Integer.parseInt(SystemProperties.get("ss.http_request_timeout")));
  private static ConnectionKeepAliveStrategy loginTimeout = buildTimeout(Integer.parseInt(SystemProperties.get("ss.http_login_timeout")));
  private static String serviceAuthToken = null;
  
  
  
  public ServiceRequest getSession(String url) {
    List<RestItem> loginRestItems = null;
    ServiceRequest loginResp = new ServiceRequest();

    try {
      logger.info("Retrieving connection actions from url: " + url);
      new URL(url);     //this is only to check whether the URL format is correct
      HttpGet httpGet = new HttpGet(url);
      CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(requestTimeout).build();
      ResponseHandler<String> responseHandler = getResponseHandler(401);
      String responseBody = httpClient.execute(httpGet, responseHandler);
      
      loginRestItems = JsonHelper.toRestItems(responseBody);
      ArrayList<String> names = new ArrayList<String>();
      for (RestItem item : loginRestItems) {
        names.add(item.getName());  
      }
      logger.debug("Retrieved connection details: " + names.toString());
    } catch (MalformedURLException urlEx) {
      logger.error("malformed URL: " + url);
    } catch (ClientProtocolException protEx) {
      logger.error("Invalid Client Protocol: " + protEx.getCause());
    } catch (IOException ioEx) {
      logger.error("Communication error: to URL [" + url + "]");
    }
    loginResp.setServiceRestItems(loginRestItems);
    return loginResp;
  }
  
  
  
  public ServiceRequest processLogin(ServiceRequest loginReq, String relation, String name) {
    logger.info("Retrieving services for: " + name);
    CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(loginTimeout).build();
    List<RestItem> serviceRestItems = null;
    ServiceRequest serviceRequest = new ServiceRequest();

    RestItem loginDetails = getRestItems(loginReq, name);
    if (loginDetails == null) {
      logger.error("Login details not found for " + name);
    }
      
    RestLink link = getLink(loginDetails, relation);
    if (link == null) {
      logger.error("Login relation not found for relation: " + relation +" for " + name);
    }
      
    CloseableHttpResponse response = null;
    try {
      HttpUriRequest httpAction = new HttpPost(link.getHref());
      httpAction.setHeader("X-Auth-User", SystemProperties.get("ss.username"));
      httpAction.setHeader("X-Auth-Key", SystemProperties.get("ss.password"));
      httpAction.setHeader("Content-Type", "application/json");

      response = httpClient.execute(httpAction);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new ClientProtocolException("Unexpected response status: " + response.getStatusLine().getStatusCode() +
                    " while retrieving services for: " + name);
      }

      serviceAuthToken = response.getFirstHeader("X-Auth-Token").getValue();
      serviceRequest.setAuthToken(serviceAuthToken);
      HttpEntity entity = response.getEntity();
      String jsonResponse = new String(EntityUtils.toByteArray(entity));
      serviceRestItems = JsonHelper.toRestItems(jsonResponse);
      serviceRequest.setServiceRestItems(serviceRestItems);
    } catch (ClientProtocolException protEx) {
      logger.error("Invalid Client Protocol: " + protEx.getMessage() + " while retrieving services for: " + name);
    } catch (IOException ioEx) {
      logger.error("Communication error" + ioEx.getCause() + " while retrieving services for: " + name);
    } finally {
      try {
        response.close();
      } catch (Exception ex){
        //We're not too concerned, it's a limited lifetime stream, if we can't close it the server will (it may have already).
      }
    }

    return serviceRequest;
  }
  
  
  
  public ServiceRequest processRequest(ServiceRequest request, String relation, String name) {
    return processRequest(request, relation, name, "n/a");
  }

  
  
  public ServiceRequest processRequest(ServiceRequest request, String relation, String name, String entity) {
    ServiceRequest response = new ServiceRequest();
    String body = retrieveBody(request, relation, name, entity);
    List<RestItem> serviceRestItems = JsonHelper.toRestItems(body);

    response.setServiceRestItems(serviceRestItems);
    response.setAuthToken(request.getAuthToken());
    return response;
  }
  
  
  
  public String getSnapshot(ServiceRequest snapShot, String relation, String fixture) {
    return retrieveBody(snapShot, relation, fixture, "n/a");
  }



  private String retrieveBody(ServiceRequest request, String relation, String name, String entity) {
    CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(requestTimeout).build();
    
    RestItem serviceDetails = null;
    if (name != null) {
      serviceDetails = getRestItems(request, name);
      if (serviceDetails == null) {
        logger.error("No details found for: " + relation + " on " + name);
      }
    }
    
    RestLink link = getLink(serviceDetails, relation);
    if (link == null) {
      logger.error("No links found for: " + relation + " on " + name);
    }
    
    String responseBody = null;    
    try {
      if (relation.equals("http://api.sportingsolutions.com/rels/stream/batchecho"))
      {
        HttpPost httpPost = new HttpPost(link.getHref());
        httpPost.setHeader("X-Auth-Token", request.getAuthToken());
        httpPost.setHeader("Content-Type", "application/json");
        HttpEntity myEntity = new StringEntity(entity);
        httpPost.setEntity(myEntity);
        
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        if (httpResponse.getStatusLine().getStatusCode() != 202) {
          throw new ClientProtocolException("Unexpected response status for echo request: " 
                      + httpResponse.getStatusLine().getStatusCode());
        }

        HttpEntity responseEntity = httpResponse.getEntity();
        if (responseEntity != null) {
          responseBody = new String(EntityUtils.toByteArray(responseEntity));
        }
      } else {
        HttpGet httpGet = new HttpGet(link.getHref());
        httpGet.setHeader("X-Auth-Token", request.getAuthToken());
        logger.debug("Sending request for relation:["+ relation + "] name:[" + name + "] to href:[" + link.getHref() +"]");
        ResponseHandler<String> responseHandler = getResponseHandler(200);

        responseBody = httpClient.execute(httpGet, responseHandler);
      }
    } catch (ClientProtocolException protEx) {
      logger.error("Invalid Client Protocol: " + protEx.getMessage());
    } catch (IOException ioEx) {
      logger.error("Communication error: " + ioEx.getMessage());
    } 
    return responseBody;
  }

  
  
  private RestItem getRestItems(ServiceRequest request, String name) {
    RestItem matchingRest = null;
    Iterator<RestItem> itemRestIterator = request.getServiceRestItems().iterator();
    do {
      matchingRest = itemRestIterator.next();
      if (matchingRest.getName().compareTo(name) != 0) {
        matchingRest = null;
      }
    } while ( itemRestIterator.hasNext() && (matchingRest == null) ) ;
    return matchingRest;
  }  
  
  
  
  private RestLink getLink(RestItem request, String relation) {
    RestLink link = null;
    Iterator<RestLink> linkIterator = request.getLinks().iterator();
    do {
      link = linkIterator.next();
      if (link.getRelation().compareTo(relation) != 0) {
        link = null;
      }
    } while ( linkIterator.hasNext() && (link == null) );
    return link;
  }
  
  
  
  private ResponseHandler<String> getResponseHandler(final int validStatus) {
    return new ResponseHandler<String>() {
      public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        int responseStatus = response.getStatusLine().getStatusCode();
        if (responseStatus == validStatus) {
          logger.debug("Http connetion status " + responseStatus);
          HttpEntity entity = response.getEntity();
          return entity != null ? EntityUtils.toString(entity) : null;
        } else {
          throw new ClientProtocolException("Unexpected http connection response status: " + responseStatus);
        }
      }
    };
  }


  
  private static ConnectionKeepAliveStrategy buildTimeout(final int timeout) {
    return new ConnectionKeepAliveStrategy() {
      public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        return timeout * 1000;
      }
    };
  }
    
}



  
