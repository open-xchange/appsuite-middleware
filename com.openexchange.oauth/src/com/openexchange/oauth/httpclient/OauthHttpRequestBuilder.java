package com.openexchange.oauth.httpclient;

import com.openexchange.http.client.builder.HTTPDeleteRequestBuilder;
import com.openexchange.http.client.builder.HTTPGetRequestBuilder;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPostRequestBuilder;
import com.openexchange.http.client.builder.HTTPPutRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.httpclient.impl.scribe.OauthScribeHttpGetRequestBuilder;

public class OauthHttpRequestBuilder implements HTTPRequestBuilder {

	private OAuthAccount account;

	public OauthHttpRequestBuilder(OAuthAccount account) {
		this.setAccount(account);
	}

	@Override
	public HTTPPutRequestBuilder put() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTPPostRequestBuilder post() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTPMultipartPostRequestBuilder multipartPost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HTTPGetRequestBuilder get() {
		return new OauthScribeHttpGetRequestBuilder(this);
	}

	@Override
	public HTTPDeleteRequestBuilder delete() {
		// TODO Auto-generated method stub
		return null;
	}

	public OAuthAccount getAccount() {
		return account;
	}

	public void setAccount(OAuthAccount account) {
		this.account = account;
	}
}
