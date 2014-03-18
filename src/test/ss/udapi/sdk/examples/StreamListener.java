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

package ss.udapi.sdk.examples;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import ss.udapi.sdk.examples.model.Fixture;
import ss.udapi.sdk.interfaces.Resource;
import ss.udapi.sdk.streaming.ConnectedEvent;
import ss.udapi.sdk.streaming.DisconnectedEvent;
import ss.udapi.sdk.streaming.Event;
import ss.udapi.sdk.streaming.StreamEvent;
import ss.udapi.sdk.streaming.SynchronizationEvent;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class StreamListener {

	private static Logger logger = Logger.getLogger(StreamListener.class
			.getName());
	private Integer currentEpoch;
	private Resource gtpFixture;
	private Boolean fixtureEnded;

	public Boolean getFixtureEnded() {
		return this.fixtureEnded;
	}

	public StreamListener(Resource gtpFixture, Integer currentEpoch) {
		this.fixtureEnded = false;
		this.gtpFixture = gtpFixture;
		this.currentEpoch = currentEpoch;
		listen();
	}

	private void listen() {
		try {
			List<Event> streamingEvents = new ArrayList<Event>();

			streamingEvents.add(new ConnectedEvent() {
				public void onEvent(String message) {
					logger.info(String.format("Stream Connected for %1$s",
							gtpFixture.getName()));
				}
			});

			streamingEvents.add(new StreamEvent() {
				public void onEvent(String message) {
					logger.info(String.format(
							"Streaming Message Arrived for %1$s",
							gtpFixture.getName()));
					handleStreamMessage(message);
				}
			});

			streamingEvents.add(new DisconnectedEvent() {
				public void onEvent(String message) {
					logger.info(String.format("Stream Disconnected for %1$s",
							gtpFixture.getName()));
				}
			});

			streamingEvents.add(new SynchronizationEvent() {
				public void onEvent(String message) {
					handleSyncEvent();
				}
			});

			gtpFixture.startStreaming(streamingEvents);
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	private void handleStreamMessage(String streamString) {
		try {
			logger.info(streamString);

			GsonBuilder gsonBuilder = new GsonBuilder();
			Gson gson = gsonBuilder.create();

			JsonObject jsonObject = new JsonParser().parse(streamString)
					.getAsJsonObject();
			Fixture fixtureDelta = gson.fromJson(jsonObject.get("Content"),
					Fixture.class);

			logger.info(String.format(
					"Attempting to process Markets and Selctions for %1$s",
					gtpFixture.getName()));

			if (fixtureDelta.getEpoch() > currentEpoch) {

				if (fixtureDelta.getLastEpochChangeReason() != null
						&& Arrays.asList(
								fixtureDelta.getLastEpochChangeReason())
								.contains(10)) {
					logger.info(String
							.format("Fixture %1$s has been deleted from the GTP Fixture Factory.",
									gtpFixture.getName()));
					gtpFixture.stopStreaming();
					this.fixtureEnded = true;
				} else {
					logger.info(String.format(
							"Epoch changed for %1$s from %2$s to %3$s",
							gtpFixture.getName(), currentEpoch,
							fixtureDelta.getEpoch()));
					gtpFixture.pauseStreaming();

					logger.info(String.format("Get UDAPI Snapshot for %1$s",
							gtpFixture.getName()));
					String snapshotString = gtpFixture.getSnapshot();
					logger.info(String.format(
							"Successfully retrieved UDAPI Snapshot for %1$s",
							gtpFixture.getName()));

					Fixture fixtureSnapshot = gson.fromJson(snapshotString,
							Fixture.class);
					currentEpoch = fixtureSnapshot.getEpoch();

					// process the snapshot here
					logger.info(snapshotString);

					if (!fixtureDelta.getMatchStatus().equalsIgnoreCase("50")) {
						gtpFixture.unpauseStreaming();
					} else {
						logger.info(String
								.format("Stopping Streaming for %1$s with id %2$s, Match Status is Match Over",
										gtpFixture.getName(),
										gtpFixture.getId()));
						gtpFixture.stopStreaming();
						this.fixtureEnded = true;
					}
				}

			} else if (fixtureDelta.getEpoch() == currentEpoch) {
				// process the delta
				logger.info(fixtureDelta.getMarkets().size());
			}
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	private void handleSyncEvent() {
		logger.warn(String.format("Stream out of sync for %1$s",
				gtpFixture.getName()));

		gtpFixture.pauseStreaming();

		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		logger.info(String.format("Get UDAPI Snapshot for %1$s",
				gtpFixture.getName()));
		String snapshotString = gtpFixture.getSnapshot();
		logger.info(String.format(
				"Successfully retrieved UDAPI Snapshot for %1$s",
				gtpFixture.getName()));

		Fixture fixtureSnapshot = gson.fromJson(snapshotString, Fixture.class);
		currentEpoch = fixtureSnapshot.getEpoch();

		// process the snapshot here
		logger.info(snapshotString);

		if (fixtureSnapshot.getMatchStatus() != "50") {
			gtpFixture.unpauseStreaming();
		}
	}
}
