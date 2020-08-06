/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
