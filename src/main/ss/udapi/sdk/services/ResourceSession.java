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


package ss.udapi.sdk.services;

import ss.udapi.sdk.model.ServiceRequest;

public class ResourceSession
{
  private String amqpDest = null;
  private ServiceRequest availableResources = null;
  private String resourceId = null;

  
  public ResourceSession(String amqpDest, ServiceRequest availableResources, String resourceId) {
    synchronized(this) {
      this.amqpDest = amqpDest;
      this.availableResources = availableResources;
      this.resourceId = resourceId;
    }
  }

  public String getAmqpDest() {
    synchronized(this) {
      return amqpDest;
    }
  }


  public ServiceRequest getAvailableResources() {
    synchronized(this) {
      return availableResources;
    }
  }


  public String getResourceId() {
    synchronized(this) {
      return resourceId;
    }
  }
  
  
  
  
  
  
}
