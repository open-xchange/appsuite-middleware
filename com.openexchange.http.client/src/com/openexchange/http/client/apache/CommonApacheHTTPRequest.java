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

import java.net.MalformedURLException;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.util.URIUtil;
import com.openexchange.exception.OXException;
import com.openexchange.http.client.builder.HTTPGenericRequestBuilder;
import com.openexchange.http.client.builder.HTTPRequest;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

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
        try {
            final HttpClient client = new HttpClient();
            final int timeout = 20000;
            client.getParams().setSoTimeout(timeout);
            client.getParams().setIntParameter("http.connection.timeout", timeout);

            client.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));

            client.getParams().setParameter("http.protocol.single-cookie-header", Boolean.TRUE);
            client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);

            /*
             * Generate URL
             */

            String encodedSite = verbatimURL ? url : URIUtil.encodeQuery(url);

            final java.net.URL javaURL = new java.net.URL(encodedSite);

            if (javaURL.getProtocol().equalsIgnoreCase("https")) {
                int port = javaURL.getPort();
                if (port == -1) {
                    port = 443;
                }

                final Protocol https = new Protocol("https", new TrustAdapter(), 443);
                client.getHostConfiguration().setHost(javaURL.getHost(), port, https);

                final HttpMethodBase m = createMethod(javaURL.getFile());
                m.getParams().setSoTimeout(20000);
                m.setQueryString(javaURL.getQuery());
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    m.setRequestHeader(entry.getKey(), entry.getValue());
                }
                addParams(m, javaURL.getQuery());

                return new ApacheHTTPRequest(headers, parameters, m, client, coreBuilder, this);
            }
            /*
             * No https, but http
             */
            final HttpMethodBase m = createMethod(encodedSite);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                m.setRequestHeader(entry.getKey(), entry.getValue());
            }
            addParams(m, javaURL.getQuery());

            return new ApacheHTTPRequest(headers, parameters, m, client, coreBuilder, this);
        } catch (URIException x) {
            throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(x.getMessage(), x);
        } catch (MalformedURLException e) {
            throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
        }
    }

	protected void addParams(HttpMethodBase m, String q) {

		NameValuePair[] query = new NameValuePair[parameters.size()];

		int i = 0;
		for (Map.Entry<String, String> entry : parameters.entrySet()) {
			query[i++] = new NameValuePair(entry.getKey(), entry.getValue());
		}
		m.setQueryString(query);
		String queryString = m.getQueryString();
		if (q != null) {
			if (queryString != null && queryString.length() > 0) {
				queryString = queryString+"&"+q;
			} else {
				queryString = q;
			}
		}
		m.setQueryString(queryString);
	}

	protected abstract HttpMethodBase createMethod(String encodedSite);

	/**
	 * Marks as done.
	 */
	public void done() {
	    // Nothing to do
	}

}
