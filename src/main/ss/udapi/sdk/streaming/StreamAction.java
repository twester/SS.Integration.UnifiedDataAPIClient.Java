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

package ss.udapi.sdk.streaming;

import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;

public class StreamAction extends Action {
	
	private static Logger logger = Logger.getLogger(StreamAction.class.getName());
	ConcurrentLinkedQueue<String> msgList = new ConcurrentLinkedQueue<String>();
	
	public StreamAction(List<Event> events) {
		super(events, StreamEvent.class);
	}
	
	public void addMsg(String msg)
	{
		msgList.add(msg);
	}
	
	@Override
	public void run() 
	{
		while (msgList.peek() != null)
		{
			try
			{
				execute(msgList.poll());
			}
			catch(Exception ex)
			{
				logger.warn("Error", ex);
			}
		}
	}
}
