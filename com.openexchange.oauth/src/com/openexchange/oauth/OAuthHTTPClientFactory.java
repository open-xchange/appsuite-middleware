package com.openexchange.oauth;

import com.openexchange.exception.OXException;
import com.openexchange.http.client.HTTPClient;

public interface OAuthHTTPClientFactory {
	public HTTPClient create(OAuthAccount account) throws OXException;
}
