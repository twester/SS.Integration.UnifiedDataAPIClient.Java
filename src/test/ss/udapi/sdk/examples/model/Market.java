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
