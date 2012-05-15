package com.openexchange.oauth.httpclient.impl.scribe;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import org.scribe.oauth.OAuthService;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.httpclient.HttpOauthRequest;
import com.openexchange.oauth.httpclient.OauthHttpRequestBuilder;

public class OauthScribeHttpGetRequestBuilder implements HTTPGetRequestBuilder {

	protected Map<String, String> parameters = new TreeMap<String, String>();
	protected Map<String, String> headers = new TreeMap<String, String>();
	private OauthHttpRequestBuilder coreBuilder;
	private boolean verbatimUrl;
	private String url, baseUrl;
	protected OAuthService service;
	protected Class<? extends org.scribe.builder.api.Api> provider;
	private Token token;

	public OauthScribeHttpGetRequestBuilder(OauthHttpRequestBuilder coreBuilder) {
		this.coreBuilder = coreBuilder;
		OAuthAccount account = coreBuilder.getAccount();
		provider = FacebookApi.class; //TODO: settable or inherited or whatever
		
		service = new ServiceBuilder()
        .provider(FacebookApi.class)
        .apiKey("your_api_key") //TODO: Whut? Where from?
        .apiSecret(account.getSecret())
        .build();
	}
	
	protected Verb getVerb(){
		return Verb.GET;
	}

	@Override
	public HTTPGetRequestBuilder url(String url) {
		verbatimUrl = false;
		this.baseUrl = url;
		return this;
	}

	@Override
	public HTTPGetRequestBuilder verbatimURL(String url) {
		verbatimUrl = true;
		this.url = url;
		return this;
	}

	@Override
	public HTTPGetRequestBuilder parameter(String parameter, String value) {
		parameters.put(parameter, value);
		return this;
	}

	@Override
	public HTTPGetRequestBuilder parameters(Map<String, String> parameters) {
		this.parameters.putAll(parameters);
		return this;
	}

	@Override
	public HTTPGetRequestBuilder header(String header, String value) {
		headers.put(header, value);
		return this;
	}

	@Override
	public HTTPGetRequestBuilder headers(Map<String, String> cookies) {
		headers.putAll(cookies);
		return this;
	}

	@Override
	public HTTPRequest build() throws OXException {
		String finalUrl = verbatimUrl ? url : buildUrl();
		OAuthRequest request = new OAuthRequest(getVerb(), finalUrl);
		service.signRequest(getToken(), request);
		return new HttpOauthRequest(request);
	}

	private Token getToken() {
		if(token == null){
			
		}
		return token;
	}

	private String buildUrl() {		
		StringBuilder bob = new StringBuilder(baseUrl).append("?");
		Set<Entry<String, String>> entrySet = parameters.entrySet();
		for(Entry<String,String> entry: entrySet){
			bob.append("&")
			.append(entry.getKey())
			.append("=")
			.append(entry.getValue());
		}
		return bob.toString();
	}
}