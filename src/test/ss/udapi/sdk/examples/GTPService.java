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

package ss.udapi.sdk.examples;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;

import ss.udapi.sdk.CredentialsImpl;
import ss.udapi.sdk.SessionFactory;
import ss.udapi.sdk.examples.model.Fixture;
import ss.udapi.sdk.interfaces.Credentials;
import ss.udapi.sdk.interfaces.Feature;
import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.interfaces.Session;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GTPService {
	
	private static Logger logger = Logger.getLogger(GTPService.class.getName());
	
	private Timer theTimer;
	private List<String> sportsList;
	private ConcurrentHashMap<String,StreamListener> listeners;
	private ConcurrentHashMap<String,Boolean> activeFixtures;
	
	private Properties theProperties;
	
	public GTPService(String propertyFile){
		
		theProperties = new Properties();
		try {
			theProperties.load(new FileInputStream(propertyFile));
		} catch (IOException ex) {
			logger.error("Can't load the properties file.",ex);
		}
		
		sportsList = new ArrayList<String>();
		sportsList.add("Tennis");
//		sportsList.add("Football");
	//	sportsList.add("Basketball");
	//	sportsList.add("Baseball");
//		sportsList.add("IceHockey");
//		sportsList.add("TestCricket");
		listeners = new ConcurrentHashMap<String,StreamListener>();
		activeFixtures = new ConcurrentHashMap<String,Boolean>();
	}
	
	public void start(){
		try{
			logger.debug("Starting GTPService");
			logger.info("Connecting to UDAPI....");
			Credentials credentials = new CredentialsImpl(theProperties.getProperty("ss.username"),theProperties.getProperty("ss.password"));
			Session theSession = SessionFactory.createSession(new URL(theProperties.getProperty("ss.url")), credentials);
			logger.info("Successfully connected to UDAPI");
			logger.debug("UDAPI, Getting Service");
			final Service theService = theSession.getService("UnifiedDataAPI");
			logger.debug("UDAPI, Retrieved Service");
			
			logger.info("Starting Timer...");
			theTimer = new Timer(true);
			theTimer.scheduleAtFixedRate(new TimerTask(){public void run(){
				timerEvent(theService);
			}}, 0, 60000);
		}catch(Exception ex){
			logger.error(ex);
		}
	}
	
	private void timerEvent(Service theService){
		try{
			for(final String sport:sportsList){
				Feature theFeature = theService.getFeature(sport);
				if(theFeature != null){
					logger.info(String.format("Get the list of available fixtures for %1$s from GTP", sport));
					List<Resource> fixtures = theFeature.getResources();
					
					if(fixtures != null && !fixtures.isEmpty()){
						ExecutorService exec = Executors.newFixedThreadPool(10);
						try{
							for(final Resource resource:fixtures){
								exec.submit(new Runnable(){
									@Override
									public void run(){
										processFixture(resource,sport);
									}
								});
							}
						}finally{
							exec.shutdown();
						}
					}else{
						logger.info(String.format("There are currently no %1$s fixtures in UDAPI", sport));
					}
				}else{
					logger.info(String.format("Cannot find %1$s in UDAPI....", sport));
				}
			}
		}catch(Exception ex){
			logger.error(ex);
		}
	}
	
	private void processFixture(Resource fixture, String sport){
		if(!activeFixtures.containsKey(fixture.getId()) && !listeners.containsKey(fixture.getId())){
			activeFixtures.put(fixture.getId(),true);
			
			Integer matchStatus = 0;
			if(fixture.getContent() != null){
				matchStatus = fixture.getContent().getMatchStatus();
			}
			
			if(matchStatus != 50){
				
				GsonBuilder gsonBuilder = new GsonBuilder();
				Gson gson = gsonBuilder.create();
				
				logger.info(String.format("Get UDAPI Snapshot for %1$s id %2$s", fixture.getName(), fixture.getId()));
				String snapshotString = fixture.getSnapshot();
				logger.info(String.format("Successfully retrieved UDAPI Snapshot for %1$s id %2$s", fixture.getName(), fixture.getName()));
				
				Fixture fixtureSnapshot = gson.fromJson(snapshotString, Fixture.class);
				Integer epoch = fixtureSnapshot.getEpoch();
				
				//do something with the snapshot here
				
				StreamListener streamListener = new StreamListener(fixture,epoch);
				listeners.put(fixture.getId(), streamListener);
				
			}else{
				logger.info(String.format("Fixture %1$s id %2$s has finished. Will not process.", fixture.getName(), fixture.getId()));
			}
			activeFixtures.remove(fixture.getId());
		}else{
			logger.info(String.format("Fixture %1$s id %2$s is currently being processed", fixture.getName(), fixture.getId()));
			if(listeners.containsKey(fixture.getId())){
				if(listeners.get(fixture.getId()).getFixtureEnded()){
					listeners.remove(fixture.getId());
					logger.info(String.format("Fixture %1$s if %2$s is over", fixture.getId(), fixture.getName()));
				}
			}
		}
	}
}
