package com.openexchange.oauth;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;

public interface OAuthHTTPClientFactory {
	HTTPClient create(OAuthAccount account, API api, String apiKey,
			String secret) throws OXException;
}
