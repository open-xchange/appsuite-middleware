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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.LaxRedirectStrategy;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;
import com.openexchange.java.Streams;

public abstract class CommonApacheHTTPRequest<T extends HTTPGenericRequestBuilder<T>> {

	protected String url;

	protected Map<String, String> parameters = new TreeMap<String, String>();
	protected Map<String, String> headers = new TreeMap<String, String>();

	protected ApacheClientRequestBuilder coreBuilder;

	private boolean verbatimURL;

	public CommonApacheHTTPRequest(ApacheClientRequestBuilder coreBuilder) {
		this.coreBuilder = coreBuilder;
	}

	public T url(String url) {
		this.url = url;
		this.verbatimURL = false;
		return (T) this;
	}

	public T verbatimURL(String url) {
		this.url = url;
		this.verbatimURL = true;
		return (T) this;
	}


	public T parameter(String parameter, String value) {
		parameters.put(parameter, value);
		return (T) this;
	}

	public T parameters(Map<String, String> parameters) {
		this.parameters = parameters;
		return (T) this;
	}

	public T header(String header, String value) {
		headers.put(header, value);
		return (T) this;
	}

	public T headers(Map<String, String> headers) {
		this.headers = headers;
		return (T) this;
	}

    public HTTPRequest build() throws OXException {
        CloseableHttpClient client = null;
        try {
            CookieStore httpCookieStore = new BasicCookieStore();
            client = HttpClientBuilder.create().setDefaultCookieStore(httpCookieStore).setRetryHandler(new DefaultHttpRequestRetryHandler(0, false)).setRedirectStrategy(new LaxRedirectStrategy()).build();
            int timeout = 20000;
            RequestConfig config = RequestConfig.custom().setConnectTimeout(timeout).setConnectTimeout(timeout).setCookieSpec(CookieSpecs.DEFAULT).build();
            String encodedSite = verbatimURL ? url : new URI(url).toString();
            final HttpRequestBase m = createMethod(encodedSite);
            m.setConfig(config);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                m.setHeader(entry.getKey(), entry.getValue());
            }
            addParams(m);

            ApacheHTTPRequest httpRequest = new ApacheHTTPRequest(headers, parameters, m, client, coreBuilder, this, httpCookieStore);
            client = null; // Avoid premature closing
            return httpRequest;
        } catch (URISyntaxException x) {
            throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(x.getMessage(), x);
        } finally {
            Streams.close(client);
        }
    }

	protected void addParams(HttpRequestBase m) throws URISyntaxException {

	    URIBuilder uriBuilder = new URIBuilder(m.getURI());

	    for (Map.Entry<String, String> entry : parameters.entrySet()) {
	        uriBuilder.addParameter(entry.getKey(), entry.getValue());
	    }
	    m.setURI(uriBuilder.build());
	}

	protected abstract HttpRequestBase createMethod(String encodedSite);

	/**
	 * Marks as done.
	 */
	public void done() {
	    // Nothing to do
	}

}
