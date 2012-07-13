package ss.udapi.sdk.streaming;

import java.util.List;

import org.apache.log4j.Logger;

public class SynchronizationAction extends Action{
	
	private static Logger logger = Logger.getLogger(SynchronizationAction.class.getName());
	
	public SynchronizationAction(List<Event> events) {
		super(events, DisconnectedEvent.class);
	}
	
	@Override
	public void run() {
		try
		{
			execute("Synchronization Action");
		}
		catch(Exception ex)
		{
			logger.warn("Error", ex);
		}
	}
}
