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

package ss.udapi.sdk;

import ss.udapi.sdk.interfaces.Credentials;

/**
 * Simple bean object for the credentials used during logging in.
 * Although this class is not thread safe it will only ever be called once before any activity,
 * even if multiple instances exist only one thread in the JVM will gain access to the resources the others will
 * simply abend.   
 * 
 * 
 * @author FGonzalez149
 * 
 */
public class CredentialsImpl implements Credentials
{
	private String userName;
	private String password;
	
	
/** 
 * @param userName  User name associated with a valid account grating access to the Sporting Solutions Service.
 * @param password  Password associated with a valid account grating access to the Sporting Solutions Service.
 */
	public CredentialsImpl(String userName, String password) {
		this.userName = userName;
		this.password = password;
	}


	
	/**
	 * @return   The password value currently held in this instance of credentials.
	 */
	public String getUserName()	{
		return userName;
	}

	
	
	/**
	 * @return   The password value currently held in this instance of credentials. 
	 */
  public String getPassword() {
	  return password;
	}



}
