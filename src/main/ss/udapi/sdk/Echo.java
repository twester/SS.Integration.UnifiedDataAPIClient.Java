package ss.udapi.sdk;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.log4j.Logger;

import ss.udapi.sdk.clients.RestHelper;
import ss.udapi.sdk.extensions.JsonHelper;
import ss.udapi.sdk.extensions.WaitObject;
import ss.udapi.sdk.interfaces.EchoHandler;
import ss.udapi.sdk.model.StreamEcho;

public class Echo
{
	private static Logger logger = Logger.getLogger(Echo.class.getName());

	public static final String FINISHED = "STOP_ECHO";
	String idName = "";
	EchoHandler handler;
	URL echoSite;
	String virtualHost = "";
	String queueName = "";
	Map<String,String> restHeaders;
	private WaitObject signaller = new WaitObject();
	int echoRespTimeout;
	
	String lastReceivedEchoGuid;
	
	public Echo(String idName, EchoHandler handler, URL echoSite, String virtualHost, String queueName, 
			Map<String, String> restHeaders, int echoRespTimeout)
	{
		super();
		this.idName = idName;
		this.handler = handler;
		this.echoSite = echoSite;
		this.virtualHost = virtualHost;
		this.queueName = queueName;
		this.restHeaders = restHeaders;
		this.echoRespTimeout = echoRespTimeout;
	}
	//-------------------------------------------------------------------------------------
	private String getName()
	{
		return idName;
	}
	//-------------------------------------------------------------------------------------
	public void sendEcho()
	{
		try
		{
			String guid = UUID.randomUUID().toString();
			StreamEcho streamEcho = new StreamEcho();
			streamEcho.setHost(virtualHost);
			streamEcho.setQueue(queueName);

			DateFormat df = new SimpleDateFormat("yyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
			streamEcho.setMessage(guid + ";" + df.format(new Date()));

			String stringStreamEcho = JsonHelper.ToJson(streamEcho);

			String errorResp = "bad";
			String resp = errorResp;
			signaller.reset();
			lastReceivedEchoGuid = "";
			boolean echoNotSent = true;
			
			for (int i = 0; i < 3 && echoNotSent; i++)
			{
				resp = RestHelper.getResponse(echoSite, stringStreamEcho, "POST", "application/json", 3000, restHeaders, false, errorResp);
				
				if (resp.equalsIgnoreCase(errorResp))
				{
					logger.info("Failed to send echo " + queueName + guid);
					if (checkReceivedGUIDAgainstSentGUID(guid))
					{
						logger.info("But echo reponse received");
						echoNotSent = false;
					}			
				}
				else
				{
					echoNotSent = false;
				}
			}
			
			if (echoNotSent)
			{
				logger.warn("Echo has NOT been sent for " + queueName + " " + getName());
			}
			else
			{
				logger.debug("Echo sent " + guid);
			}
			
			if (receiveEcho(guid))
			{
				handler.resetEcho();
			}
			else
			{
				handler.echoTimeout();
			}

		} catch (Exception ex)
		{
			logger.error("Echo failed for " + getName(), ex);
			handler.resetEcho();
		}
	}
	//-------------------------------------------------------------------------------------
	public synchronized void gotEcho(String receivedGuid)
	{
		lastReceivedEchoGuid = receivedGuid;
		signaller.signalAll();
	}
	private synchronized String getLastGuid()
	{
		return lastReceivedEchoGuid;
	}
	//-------------------------------------------------------------------------------------
	private boolean receiveEcho(String sentGUID)
	{
		try
		{
			boolean echoReceived = signaller.waitOne(echoRespTimeout);

			if (echoReceived)
			{
				if (checkReceivedGUIDAgainstSentGUID(sentGUID))
				{
					logger.debug(String.format("Echo received for " + getName()));
				} else
				{
					logger.error("Received wrong guid. Sent: " + sentGUID + " received: " + lastReceivedEchoGuid);
				}
			} else
			{
				throw new Exception("Echo receive timeout");
			}
		} catch (Exception ex)
		{
			logger.error("Echo failure for " + getName() + " Reason: " + ex.getMessage());
			return false;
		}
		return true;
	}
	//-----------------------------------------------------------------------------------
	private boolean checkReceivedGUIDAgainstSentGUID(String sentGUID)
	{
		String receivedGuid = getLastGuid();
		return sentGUID.equals(receivedGuid) || receivedGuid == FINISHED;
	}
}
