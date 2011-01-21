package com.openexchange.ajax.framework;

import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;

public class OxHttpClient extends DefaultHttpClient {
	/**
	 * User Agent displayed to server - needs to be consistent during a test run for security purposes
	 */
	public static final String USER_AGENT = "OX6 HTTP API Testing Agent";
	
	public OxHttpClient(){
		super();
     	getParams().setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY); //OX cookies work with all browsers, meaning they are a mix of the Netscape draft and the RFC
     	getParams().setParameter("User-Agent", USER_AGENT); //needs to be consistent 
     	getParams().setParameter("http.useragent", USER_AGENT); //needs to be consistent
	}
}
