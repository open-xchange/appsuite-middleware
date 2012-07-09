package com.openexchange.oauth.httpclient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.AbstractHTTPClient;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.http.client.builder.HTTPResponseProcessor;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;

public class OAuthHTTPClient extends AbstractHTTPClient implements HTTPClient {

	private OAuthAccount account;
	private API api;
	private String apiKey;
	private String secret;

	public OAuthHTTPClient(OAuthAccount account, API api, String apiKey, String secret) {
		this.account = account;
		this.api = api;
		this.apiKey = apiKey;
		this.secret = secret;
	}

	@Override
	public HTTPRequestBuilder getBuilder() {
		return new OAuthHTTPRequestBuilder(this);
	}
	
	public <R> R process(String payload, Class<R> targetFormat) throws OXException{
		if(targetFormat == String.class){
			return (R) payload;
		} else if(targetFormat == InputStream.class) {
			try {
				return (R) new ByteArrayInputStream(payload.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				// WON'T HAPPEN!
			}
		} else if(targetFormat == Reader.class) {
			return (R) new StringReader(payload);
		}
		
		for(Class inputType: Arrays.asList(String.class, Reader.class, InputStream.class)) {
			List<HTTPResponseProcessor> procList = processors.get(inputType);
			for (HTTPResponseProcessor processor : procList) {
				if (processor.getTypes()[1] == targetFormat) {
					return (R) processor.process(process(payload, inputType));
				}
			}
		}
		
		
		throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create();
	}

	public OAuthAccount getAccount() {
		return account;
	}

	public void setAccount(OAuthAccount account) {
		this.account = account;
	}

	public API getApi() {
		return api;
	}

	public void setApi(API api) {
		this.api = api;
	}

	public String getApiKey() {
		return apiKey;
	}

	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	public String getSecret() {
		return secret;
	}

	public void setSecret(String secret) {
		this.secret = secret;
	}
	
	
}
