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

public class Market {

	public Market(){
		this.Tags = new HashMap<String,Object>();
		this.Selections = new ArrayList<Selection>();
	}
	
	private String Id;
	private String Name;
	private Boolean Tradable;
	private Map<String,Object> Tags;
	private List<Selection> Selections;
	
	public String getId() {
		return Id;
	}
	public void setId(String id) {
		this.Id = id;
	}
	public String getName() {
		return Name;
	}
	public void setName(String name) {
		this.Name = name;
	}
	public Boolean getTradable() {
		return Tradable;
	}
	public void setTradable(Boolean tradable) {
		this.Tradable = tradable;
	}
	public Map<String, Object> getTags() {
		return Tags;
	}
	public void setTags(Map<String, Object> tags) {
		this.Tags = tags;
	}
	public List<Selection> getSelections() {
		return Selections;
	}
	public void setSelections(List<Selection> selections) {
		this.Selections = selections;
	}
}
