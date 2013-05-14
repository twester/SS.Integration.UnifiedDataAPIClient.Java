package ss.udapi.sdk.streaming;

import java.util.List;

import org.apache.log4j.Logger;

public class FeedStatusAction extends Action
{
	private static Logger logger = Logger.getLogger(FeedStatusAction.class.getName());
	public static enum FeedStatus { Up, Down};
	private FeedStatus currentStatus = FeedStatus.Down;
	
	public FeedStatusAction(List<Event> events)
	{
		super(events, FeedStatusEvent.class);
	}
	public void setStatus(FeedStatus fs)
	{
		currentStatus = fs;
	}
	public FeedStatus getStatus()
	{
		return currentStatus;
	}
	@Override
	public void run()
	{
		try
		{
			execute(currentStatus.toString());
		}
		catch(Exception ex)
		{
			logger.warn("Error", ex);
		}
	}

}
