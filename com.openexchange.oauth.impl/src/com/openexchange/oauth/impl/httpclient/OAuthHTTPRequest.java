/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.oauth.impl.httpclient;

import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.scribe.model.OAuthRequest;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.impl.services.Services;

/**
 * {@link OAuthHTTPRequest} - The HTTP OAuth request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Error handling
 */
public class OAuthHTTPRequest implements HTTPRequest {

	private final OAuthRequest delegate;
	private final Map<String, String> parameters;

	/**
	 * Initializes a new {@link OAuthHTTPRequest}.
	 */
	public OAuthHTTPRequest(OAuthRequest req, Map<String, String> parameters) {
	    super();
		delegate = req;
		this.parameters = parameters;
	}

	@Override
	public HTTPResponse execute() throws OXException {
	    try {
            delegate.setConnectTimeout(5, TimeUnit.SECONDS);
            delegate.setReadTimeout(15, TimeUnit.SECONDS);

            SSLSocketFactoryProvider factoryProvider = Services.optService(SSLSocketFactoryProvider.class);
            if (null != factoryProvider) {
                delegate.setSSLSocketFactory(factoryProvider.getDefault());
            }

            // Wrap response & return
            return new HttpOauthResponse(delegate.send());
        } catch (org.scribe.exceptions.OAuthException e) {
            // Handle Scribe's org.scribe.exceptions.OAuthException (inherits from RuntimeException)
            Throwable cause = e.getCause();
            if (cause instanceof java.net.SocketTimeoutException) {
                // A socket timeout
                throw OAuthExceptionCodes.CONNECT_ERROR.create(cause, new Object[0]);
            }

            throw OAuthExceptionCodes.OAUTH_ERROR.create(cause, e.getMessage());
        }
	}

	@Override
	public Map<String, String> getParameters() {
		return parameters;
	}

	@Override
	public Map<String, String> getHeaders() {
		return delegate.getHeaders();
	}
}
