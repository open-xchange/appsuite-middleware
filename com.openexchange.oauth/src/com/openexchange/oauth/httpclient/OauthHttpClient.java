package com.openexchange.oauth.httpclient;

import com.openexchange.http.client.HTTPClient;
import com.openexchange.http.client.builder.HTTPRequestBuilder;
import com.openexchange.oauth.OAuthAccount;

public class OauthHttpClient implements HTTPClient {

	private OAuthAccount account;

	public OauthHttpClient(OAuthAccount account) {
		this.account = account;
	}

	@Override
	public HTTPRequestBuilder getBuilder() {
		return new OauthHttpRequestBuilder(account);
	}

}
