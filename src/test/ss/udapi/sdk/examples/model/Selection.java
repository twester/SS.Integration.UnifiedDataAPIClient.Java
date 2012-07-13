package ss.udapi.sdk.examples.model;

import java.util.HashMap;
import java.util.Map;

public class Selection {
	
	public Selection(){
		this.Tags = new HashMap<String,Object>();
	}

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
	public Map<String, Object> getTags() {
		return Tags;
	}
	public void setTags(Map<String, Object> tags) {
		this.Tags = tags;
	}
	public Double getPrice() {
		return Price;
	}
	public void setPrice(Double price) {
		this.Price = price;
	}
	public String getStatus() {
		return Status;
	}
	public void setStatus(String status) {
		this.Status = status;
	}
	public Boolean getTradable() {
		return Tradable;
	}
	public void setTradable(Boolean tradable) {
		this.Tradable = tradable;
	}
	
	private String Id;
	private String Name;
	private Map<String,Object> Tags;
	private Double Price;
	private String Status;
	private Boolean Tradable;
}
