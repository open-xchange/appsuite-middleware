package com.openexchange.oauth.httpclient;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.httpclient.impl.scribe.ScribeHTTPGetRequestBuilder;
import com.openexchange.oauth.httpclient.impl.scribe.ScribeHTTPPutRequestBuilder;

public class OAuthHTTPRequestBuilder implements HTTPRequestBuilder {


	private OAuthHTTPClient client;

	public OAuthHTTPRequestBuilder(OAuthHTTPClient client) {
		this.client = client;
	}

	@Override
	public HTTPPutRequestBuilder put() {
		return new ScribeHTTPPutRequestBuilder(this);
	}

	@Override
	public HTTPPostRequestBuilder post() {
		return new ScribeHTTPPostRequestBuilder(this);
	}

	@Override
	public HTTPMultipartPostRequestBuilder multipartPost() {
		throw new UnsupportedOperationException();
		//return new ScribeHTTPMultipartPostRequestBuilder(this);
	}

	@Override
	public HTTPGetRequestBuilder get() {
		return new ScribeHTTPGetRequestBuilder(this);
	}

	@Override
	public HTTPDeleteRequestBuilder delete() {
		return new ScribeHTTPDeleteRequestBuilder(this);
	}

	public HTTPRequestBuilder getBuilder() {
		return client.getBuilder();
	}

	public OAuthAccount getAccount() {
		return client.getAccount();
	}

	public API getApi() {
		return client.getApi();
	}

	public String getApiKey() {
		return client.getApiKey();
	}

	public String getSecret() {
		return client.getSecret();
	}

	
	
}
