package com.openexchange.oauth.impl;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthHTTPClientFactory;
import com.openexchange.oauth.httpclient.OauthHttpClient;

public class OAuthHTTPClientFactoryImpl implements OAuthHTTPClientFactory {

	@Override
	public HTTPClient create(OAuthAccount account) throws OXException {
		return new OauthHttpClient(account);
	}

}
