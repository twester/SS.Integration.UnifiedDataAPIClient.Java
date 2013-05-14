package ss.udapi.sdk;

import java.util.TimerTask;

public class EchoTask extends TimerTask
{
	Echo tHandler;

	EchoTask(Echo handler)
	{
		tHandler = handler;
	}

	// -------------------------------------------------------------------------------------
	@Override
	public void run()
	{
		try
		{
			tHandler.sendEcho();
		} catch (Exception ex)
		{
			System.out.print("broken");
		}
	}
}
