package ss.udapi.sdk.services;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.apache.http.protocol.HttpContext;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.*;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;

import ss.udapi.sdk.services.JsonHelper;
import ss.udapi.sdk.model.RestItem;
import ss.udapi.sdk.model.RestLink;
import ss.udapi.sdk.model.ServiceRequest;
import ss.udapi.sdk.model.StreamEcho;


//TODO: abstract the common http stuff into a superclass and add header info to logger
//TODO: change to static methods and return new instances (immutability), not thread safe so synchronize
//TODO: rename httpServices to something more descriptive

public class HttpServices
{
  private static Logger logger = Logger.getLogger(HttpServices.class);
  
  private static ConnectionKeepAliveStrategy requestTimeout = buildTimeout(Integer.parseInt(SystemProperties.get("ss.http_request_timeout")));
  private static ConnectionKeepAliveStrategy loginTimeout = buildTimeout(Integer.parseInt(SystemProperties.get("ss.http_login_timeout")));
  private static String serviceAuthToken = null;
  
  public ServiceRequest getSession(String url)
  {
    List<RestItem> loginRestItems = null;
    ServiceRequest loginResp = new ServiceRequest();

    try {
      URL discardOnlyToCheckURLFormat = new URL(url);
      
      logger.debug("Retrieving Connection actions from url: " + url);

      HttpGet httpGet = new HttpGet(url);
      CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(requestTimeout).build();
      
      ResponseHandler<String> responseHandler = getResponseHandler(401);
      String responseBody = httpClient.execute(httpGet, responseHandler);
      
      loginRestItems = JsonHelper.toRestItems(responseBody);

      ArrayList<String> names = new ArrayList<String>();
      for (RestItem item : loginRestItems)
      {
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
  
  
  public ServiceRequest processLogin(ServiceRequest loginReq, String relation, String name)
  {
    CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(loginTimeout).build();
    
    List<RestItem> serviceRestItems = null;
    ServiceRequest serviceRequest = new ServiceRequest();
    
    
    RestItem loginDetails = null;
    Iterator<RestItem> loginRestIterator = loginReq.getServiceRestItems().iterator();
    do {
      loginDetails = loginRestIterator.next();
      if (loginDetails.getName().compareTo(name) != 0) {
        loginDetails = null;
      }
    } while ( loginRestIterator.hasNext() && (loginDetails == null) ) ;

    if (loginDetails == null)
      logger.error("No login details found.");

    
    RestLink link = null;
    Iterator<RestLink> linkIterator = loginDetails.getLinks().iterator();
    do {
      link = linkIterator.next();
      if (link.getRelation().compareTo(relation) != 0) {
        link = null;
      }
    } while ( linkIterator.hasNext() && (link == null) );

    if (link == null)
      logger.error("No login relation found for: [" + relation +"]");

    
    CloseableHttpResponse response = null;
    HttpUriRequest httpAction = null;
    
    try {
      if (link.getVerbs()[0].contains("POST")) {
        httpAction = new HttpPost(link.getHref());
      } 
      
      httpAction.setHeader("X-Auth-User", SystemProperties.get("ss.username"));
      httpAction.setHeader("X-Auth-Key", SystemProperties.get("ss.password"));
      httpAction.setHeader("Content-Type", "application/json");
      
      response = httpClient.execute(httpAction);
      if (response.getStatusLine().getStatusCode() != 200) {
        throw new ClientProtocolException("Unexpected response status: " + response.getStatusLine().getStatusCode());
      }

      serviceAuthToken = response.getFirstHeader("X-Auth-Token").getValue();
      serviceRequest.setAuthToken(serviceAuthToken);

      HttpEntity entity = response.getEntity();
      String jsonResponse = new String(EntityUtils.toByteArray(entity));

      serviceRestItems = JsonHelper.toRestItems(jsonResponse);

      serviceRequest.setServiceRestItems(serviceRestItems);
    } catch (ClientProtocolException protEx) {
      logger.error("Invalid Client Protocol: " + protEx.getMessage());
    } catch (IOException ioEx) {
      logger.error("Communication error" + ioEx.getCause());
    } finally {
      try {
        response.close();
      } catch (Exception ex){
        //We're not too concerned, it's a limited lifetime stream, if we can't close it the server will (it may have already).
      }
    }

    return serviceRequest;
  }



  public ServiceRequest processRequest(ServiceRequest request, String relation, String name)
  {
    List<RestItem> serviceRestItems = null;
    ServiceRequest response = new ServiceRequest();
    CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(requestTimeout).build();
    
    RestItem serviceDetails = null;
    if (name != null)
    {
      Iterator<RestItem> serviceRestIterator = request.getServiceRestItems().iterator();
      do {
        serviceDetails = serviceRestIterator.next();
        if (serviceDetails.getName().compareTo(name) != 0) {
          serviceDetails = null;
        }
      } while ( serviceRestIterator.hasNext() && (serviceDetails == null) ) ;
      
      if (serviceDetails == null){
        logger.error("No relation found for: [" + relation +"]");
      }
    }
    
    RestLink link = null;
    Iterator<RestLink> linkIterator = serviceDetails.getLinks().iterator();
    do {
      link = linkIterator.next();
      if (link.getRelation().compareTo(relation) != 0) {
        link = null;
      }
    } while ( linkIterator.hasNext() && (link == null) );

    if (link == null)
      logger.error("No link found for relation: [" + relation +"]");
    
    
    try {
      String responseBody = null;
      if (link.getVerbs()[0].compareToIgnoreCase("Get") == 0) {
        HttpGet httpGet = new HttpGet(link.getHref());
        httpGet.setHeader("X-Auth-Token", request.getAuthToken());
        logger.debug("Sending request for relation:["+ relation + "] name:[" + name + "] to href:[" + link.getHref() +"]");
        
        ResponseHandler<String> responseHandler = getResponseHandler(200);
        responseBody = httpClient.execute(httpGet, responseHandler);
        serviceRestItems = JsonHelper.toRestItems(responseBody);
      } else if (link.getVerbs()[0].compareToIgnoreCase("Post") == 0) {
        HttpPost httpPost = new HttpPost(link.getHref());

        httpPost.setHeader("X-Auth-Token", request.getAuthToken());
        httpPost.setHeader("Content-Type", "application/json");


        
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        System.out.println("Response --------------->" +httpResponse);
        if (httpResponse.getStatusLine().getStatusCode() != 202) {
          throw new ClientProtocolException("Unexpected response status: " + httpResponse.getStatusLine().getStatusCode());
        }

        HttpEntity responseEntity = httpResponse.getEntity();
        if (responseEntity != null)
        {
          ServiceRequest serviceRequest = new ServiceRequest();
          responseBody = new String(EntityUtils.toByteArray(responseEntity));
          serviceRestItems = JsonHelper.toRestItems(responseBody);
        }
      }

      
      response.setServiceRestItems(serviceRestItems);
      response.setAuthToken(request.getAuthToken());
    } catch (ClientProtocolException protEx) {
      logger.error("Invalid Client Protocol: " + protEx.getMessage());
    } catch (IOException ioEx) {
      logger.error("Communication error: " + ioEx.getMessage());
    } 
    return response;
  }

  
  
  
  
  
  
  
  
  
  
  
  public ServiceRequest processEcho(ServiceRequest request, String relation, String name, String entity)
  {
    List<RestItem> serviceRestItems = null;
    ServiceRequest response = new ServiceRequest();
    CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(requestTimeout).build();
    
    RestItem serviceDetails = null;
    if (name != null)
    {
      Iterator<RestItem> serviceRestIterator = request.getServiceRestItems().iterator();
      do {
        serviceDetails = serviceRestIterator.next();
        if (serviceDetails.getName().compareTo(name) != 0) {
          serviceDetails = null;
        }
      } while ( serviceRestIterator.hasNext() && (serviceDetails == null) ) ;
      
      if (serviceDetails == null){
        logger.error("No relation found for: [" + relation +"]");
      }
    }
    
    RestLink link = null;
    Iterator<RestLink> linkIterator = serviceDetails.getLinks().iterator();
    do {
      link = linkIterator.next();
      if (link.getRelation().compareTo(relation) != 0) {
        link = null;
      }
    } while ( linkIterator.hasNext() && (link == null) );

    if (link == null)
      logger.error("No link found for relation: [" + relation +"]");
    
    
    try {
      String responseBody = null;
      if (link.getVerbs()[0].compareToIgnoreCase("Get") == 0) {
        HttpGet httpGet = new HttpGet(link.getHref());
        httpGet.setHeader("X-Auth-Token", request.getAuthToken());
        logger.debug("Sending request for relation:["+ relation + "] name:[" + name + "] to href:[" + link.getHref() +"]");
        
        ResponseHandler<String> responseHandler = getResponseHandler(200);
        responseBody = httpClient.execute(httpGet, responseHandler);
        serviceRestItems = JsonHelper.toRestItems(responseBody);
      } else if (link.getVerbs()[0].compareToIgnoreCase("Post") == 0) {
        HttpPost httpPost = new HttpPost(link.getHref());

        httpPost.setHeader("X-Auth-Token", request.getAuthToken());
        httpPost.setHeader("Content-Type", "application/json");
        
        
        HttpEntity myEntity = new StringEntity(entity);
        httpPost.setEntity(myEntity);
        
        CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
        System.out.println("Response --------------->" +httpResponse);
        if (httpResponse.getStatusLine().getStatusCode() != 202) {
          throw new ClientProtocolException("Unexpected response status: " + httpResponse.getStatusLine().getStatusCode());
        }

        HttpEntity responseEntity = httpResponse.getEntity();
        if (responseEntity != null)
        {
          ServiceRequest serviceRequest = new ServiceRequest();
          responseBody = new String(EntityUtils.toByteArray(responseEntity));
          serviceRestItems = JsonHelper.toRestItems(responseBody);
        }
      }

      
      response.setServiceRestItems(serviceRestItems);
      response.setAuthToken(request.getAuthToken());
    } catch (ClientProtocolException protEx) {
      logger.error("Invalid Client Protocol: " + protEx.getMessage());
    } catch (IOException ioEx) {
      logger.error("Communication error: " + ioEx.getMessage());
    } 
    return response;
  }

  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  
  

  public String getSnapshot(ServiceRequest snapShot, String relation, String fixture)
  {
    String snapShotResponse = null;
    CloseableHttpClient httpClient = HttpClients.custom().setKeepAliveStrategy(requestTimeout).build();

    
    RestItem serviceDetails = null;
    Iterator<RestItem> serviceRestIterator = snapShot.getServiceRestItems().iterator();
    do {
      serviceDetails = serviceRestIterator.next();
      if (serviceDetails.getName().compareTo(fixture) != 0) {
        serviceDetails = null;
      }
    } while ( serviceRestIterator.hasNext() && (serviceDetails == null) ) ;
    
    if (serviceDetails == null)
      logger.error("No relation found for: [" + relation +"]");
    

    RestLink link = null;
    Iterator<RestLink> linkIterator = serviceDetails.getLinks().iterator();
    do {
      link = linkIterator.next();
      if (link.getRelation().compareTo(relation) != 0) {
        link = null;
      }
    } while ( linkIterator.hasNext() && (link == null) );

    if (link == null)
      logger.error("No link found for relation: [" + relation +"]");
    
    
    try {
      HttpGet httpGet = new HttpGet(link.getHref());

      httpGet.setHeader("X-Auth-Token", snapShot.getAuthToken());
      
      logger.debug("Sending request for snapshot for relation :[" + relation + "] fixture:[" + fixture + "] to href:[" + link.getHref() +"]");
      
      ResponseHandler<String> responseHandler = getResponseHandler(200);
      snapShotResponse = httpClient.execute(httpGet, responseHandler);
    } catch (ClientProtocolException protEx) {
      logger.error("Invalid Client Protocol" + protEx.getCause());
    } catch (IOException ioEx) {
      logger.error("Communication error" + ioEx.getCause());
    } 
    return snapShotResponse;
  }



  private ResponseHandler<String> getResponseHandler(final int validStatus)
  {
    return new ResponseHandler<String>() {
      public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
        int responseStatus = response.getStatusLine().getStatusCode();
        if (responseStatus == validStatus) {
          logger.debug("Connetion status" + responseStatus);
          HttpEntity entity = response.getEntity();
          return entity != null ? EntityUtils.toString(entity) : null;
        } else {
          throw new ClientProtocolException("Unexpected response status: " + responseStatus);
        }
      }
    };
  }


  
  private static ConnectionKeepAliveStrategy buildTimeout(final int timeout)
  {
    ConnectionKeepAliveStrategy myStrategy = new ConnectionKeepAliveStrategy() {

      @Override
      public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        return timeout * 1000;
      }

    };
    
    return myStrategy;
  }
    
  
  
  


    

    
}



  
