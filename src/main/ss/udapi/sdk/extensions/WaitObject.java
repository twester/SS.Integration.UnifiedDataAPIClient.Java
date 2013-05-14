package ss.udapi.sdk.extensions;


public class  WaitObject
{
	//private static Logger logger = Logger.getLogger(WaitObject.class.getName());

	boolean signal = false;
	int signalCount = 1;
	
	public WaitObject(int counter)
	{
		signalCount = counter;
	}
	public WaitObject()
	{
	}
	public synchronized boolean waitOne(long timeoutInMills) throws InterruptedException
	{
		if (signal)
		{
			return true;
		}
		boolean finished = false;
		//
		// To guard against "spurious wakeup" as per api doc
		//
		while (!finished)
		{
			long before = System.currentTimeMillis();
			wait(timeoutInMills);
			if (System.currentTimeMillis() - before > timeoutInMills)
			{
				finished = true;
			}
			else
			{
				timeoutInMills = timeoutInMills - (System.currentTimeMillis() - before);
				finished = signal;
			}
		}
		return signal;
	}
	public synchronized void signal()
	{
		stopWait(false);
	}
	public synchronized void signalAll()
	{
		stopWait(true);
	}
	private synchronized void stopWait(boolean all)
	{		
		if (signalCount > 0)
		{
			signalCount--;
		}
		
		if (signalCount == 0)
		{
			signal = true;
			
			if (all)
			{
				notifyAll();
			}
			else
			{
				notify();
			}
		}
	}
	public synchronized void reset()
	{
		signalCount = 1;
		signal = false;
	}
	
}
