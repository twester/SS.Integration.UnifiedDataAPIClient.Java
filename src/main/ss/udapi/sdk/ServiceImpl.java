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

package ss.udapi.sdk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ss.udapi.sdk.interfaces.Feature;
import ss.udapi.sdk.interfaces.Service;
import ss.udapi.sdk.model.RestItem;

public class ServiceImpl extends Endpoint implements Service {

	Logger logger = Logger.getLogger(ServiceImpl.class.getName());
	
	ServiceImpl(Map<String,String> headers, RestItem restItem){
		super(headers,restItem);
		logger.debug(String.format("Instantiated Service %1$s", restItem.getName()));
	}
	
	public String getName() {
		return state.getName();
	}

	public List<Feature> getFeatures() {
		logger.info(String.format("Get all available services from %1$s", getName()));
		List<Feature> result = new ArrayList<Feature>();
		List<RestItem> restItems = FindRelationAndFollow("http://api.sportingsolutions.com/rels/features/list");
		for(RestItem restItem:restItems){
			result.add(new FeatureImpl(headers, restItem));
		}
		return result;
	}

	public Feature getFeature(String featureName) {
		logger.info(String.format("Get feature %1$s from %2$s", featureName, getName()));
		List<RestItem> restItems = FindRelationAndFollow("http://api.sportingsolutions.com/rels/features/list");
		for(RestItem restItem:restItems){
			if(restItem.getName().equals(featureName)){
				return new FeatureImpl(headers, restItem);
			}
		}
		return null;
	}

}
