package ss.udapi.sdk.services;
import java.lang.reflect.Type;
import java.util.List;

import ss.udapi.sdk.model.RestItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class JsonHelper {
	public static List<RestItem> toRestItems(String json){
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		
		Type myListType = new TypeToken<List<RestItem>>(){}.getType();
		List<RestItem> links = gson.fromJson(json, myListType);
		return links;
	}
	
	public static String ToJson(Object objectToJson){
		String serializedObject = null;
		if(objectToJson != null){
			GsonBuilder gsonBuilder = new GsonBuilder();
			Gson gson = gsonBuilder.create();
			serializedObject = gson.toJson(objectToJson);
		}
		return serializedObject;
	}
}