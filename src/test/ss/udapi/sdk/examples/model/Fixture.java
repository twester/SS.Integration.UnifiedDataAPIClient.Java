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

package ss.udapi.sdk.examples.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ss.udapi.sdk.model.Participant;

public class Fixture {

	public Fixture(){
		this.Tags = new HashMap<String,Object>();
		this.GameState = new HashMap<String,Object>();
		this.Markets = new ArrayList<Market>();
		this.Participants = new ArrayList<Participant>();
	}
	
	public Integer getEpoch() {
		return Epoch;
	}
	public void setEpoch(Integer epoch) {
		this.Epoch = epoch;
	}
	public Integer[] getLastEpochChangeReason() {
		return LastEpochChangeReason;
	}
	public void setLastEpochChangeReason(Integer[] lastEpochChangeReason) {
		this.LastEpochChangeReason = lastEpochChangeReason;
	}
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		this.Id = id;
	}
	public String getMatchStatus() {
		return MatchStatus;
	}
	public void setMatchStatus(String matchStatus) {
		this.MatchStatus = matchStatus;
	}
	public Integer getSequence() {
		return Sequence;
	}
	public void setSequence(Integer sequence) {
		this.Sequence = sequence;
	}
	public Map<String, Object> getTags() {
		return Tags;
	}
	public void setTags(Map<String, Object> tags) {
		this.Tags = tags;
	}
	public Map<String, Object> getGameState() {
		return GameState;
	}
	public void setGameState(Map<String, Object> gameState) {
		this.GameState = gameState;
	}
	public List<Market> getMarkets() {
		return Markets;
	}
	public void setMarkets(List<Market> markets) {
		this.Markets = markets;
	}
	public List<Participant> getParticipants() {
		return Participants;
	}
	public void setParticipants(List<Participant> participants) {
		this.Participants = participants;
	}
	
	private Integer Epoch;
	private Integer[] LastEpochChangeReason;
	private String Id;
	private String MatchStatus;
	private Integer Sequence;
	private Map<String,Object> Tags;
	private Map<String,Object> GameState;
	private List<Market> Markets;
	private List<Participant> Participants;
}
