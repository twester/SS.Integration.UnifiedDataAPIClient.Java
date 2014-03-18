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

package ss.udapi.sdk.services;

import ss.udapi.sdk.model.RestItem;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

/*
 * Converts a JSON object into either:
 * 
 * 1) A String ready for either passing into a resource for processing, or for sending to the Sporting Solutions systems. 
 * 2) A RestItem which is used by HTTPServices to find individual endpoints amongst all the available ones in a resource.
 *  
 */
public class JsonHelper {

	public static List<RestItem> toRestItems(String json) {
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		Type myListType = new TypeToken<List<RestItem>>() {
		}.getType();
		List<RestItem> links = gson.fromJson(json, myListType);
		return links;
	}

	public static String ToJson(Object objectToJson) {
		String serializedObject = null;
		if (objectToJson != null) {
			GsonBuilder gsonBuilder = new GsonBuilder();
			Gson gson = gsonBuilder.create();
			serializedObject = gson.toJson(objectToJson);
		}
		return serializedObject;
	}

}
