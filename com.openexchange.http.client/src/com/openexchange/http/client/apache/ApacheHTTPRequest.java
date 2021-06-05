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

package com.openexchange.http.client.apache;

import java.io.IOException;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.builder.HTTPResponse;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class ApacheHTTPRequest implements HTTPRequest {

	private final Map<String, String> headers;
	private final Map<String, String> parameters;

	private final HttpRequestBase method;
	private final HttpClient client;
	private final ApacheClientRequestBuilder coreBuilder;
    private final CommonApacheHTTPRequest<?> reqBuilder;
    private final CookieStore cookieStore;



	public ApacheHTTPRequest(Map<String, String> headers, Map<String, String> parameters,
	    HttpRequestBase method, HttpClient client, ApacheClientRequestBuilder coreBuilder, CommonApacheHTTPRequest<?> builder, CookieStore cookieStore) {
		super();
		this.headers = headers;
		this.parameters = parameters;
		this.method = method;
		this.client = client;
		this.coreBuilder = coreBuilder;
		this.reqBuilder = builder;
		this.cookieStore = cookieStore;
	}

	@Override
    public HTTPResponse execute() throws OXException {
		try {
			HttpResponse resp = client.execute(method);
			return new ApacheHTTPResponse(resp, coreBuilder, cookieStore);
		} catch (IOException e) {
            throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e, e.getMessage());
		} finally {
			reqBuilder.done();
		}
	}

	@Override
    public Map<String, String> getHeaders() {
		return headers;
	}


	@Override
    public Map<String, String> getParameters() {
		return parameters;
	}

}
