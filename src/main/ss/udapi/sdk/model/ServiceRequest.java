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

package ss.udapi.sdk.model;

import java.util.List;

/**
 * Model used for HTTP Communications - Direct use of this class will lead to
 * undefined behaviour.
 */
/*
 * Used to pass the HTTP session authentication token and RestItem collections
 * between HTTP calls during normal communications with the Sporting Solutions
 * system end-point.
 */
public class ServiceRequest {
	
	private String authToken = null;
	private List<RestItem> serviceRestItems = null;

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	public List<RestItem> getServiceRestItems() {
		return serviceRestItems;
	}

	public void setServiceRestItems(List<RestItem> serviceRestItems) {
		this.serviceRestItems = serviceRestItems;
	}
}
