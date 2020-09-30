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

package com.openexchange.oidc.http.outbound;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.mail.internet.ContentType;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.oidc.osgi.Services;
import net.minidev.json.JSONObject;

/**
 * {@link SSLInjectingHTTPRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class SSLInjectingHTTPRequest extends HTTPRequest {

    private static final URL DUMMY;
    static {
        URL dummy = null;
        try {
            dummy = new URL("http://www.open-xchange.com");
        } catch (MalformedURLException e) {
            /* Ignore */
        }
        DUMMY = dummy;
    }

    /**
     * Gets the wrapping instance for given HTTP request.
     *
     * @param httpRequest The HTTP request
     * @return The wrapped token request
     */
    public static SSLInjectingHTTPRequest valueOf(HTTPRequest httpRequest) {
        if (httpRequest instanceof SSLInjectingHTTPRequest) {
            return (SSLInjectingHTTPRequest) httpRequest;
        }

        return httpRequest == null ? null : new SSLInjectingHTTPRequest(httpRequest);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final HTTPRequest httpRequest;

    /**
     * Initializes a new {@link SSLInjectingHTTPRequest}.
     *
     * @param httpRequest The HTTP request to delegate to
     */
    private SSLInjectingHTTPRequest(HTTPRequest httpRequest) {
        super(HTTPRequest.Method.GET, DUMMY);
        this.httpRequest = httpRequest;
    }

    @Override
    public ContentType getContentType() {
        return httpRequest.getContentType();
    }

    @Override
    public void setContentType(ContentType ct) {
        httpRequest.setContentType(ct);
    }

    @Override
    public void setContentType(String ct) throws ParseException {
        httpRequest.setContentType(ct);
    }

    @Override
    public void ensureContentType() throws ParseException {
        httpRequest.ensureContentType();
    }

    @Override
    public void ensureContentType(ContentType contentType) throws ParseException {
        httpRequest.ensureContentType(contentType);
    }

    @Override
    public String getHeader(String name) {
        return httpRequest.getHeader(name);
    }

    @Override
    public Method getMethod() {
        return httpRequest.getMethod();
    }

    @Override
    public void setHeader(String name, String value) {
        httpRequest.setHeader(name, value);
    }

    @Override
    public URL getURL() {
        return httpRequest.getURL();
    }

    @Override
    public void ensureMethod(Method expectedMethod) throws ParseException {
        httpRequest.ensureMethod(expectedMethod);
    }

    @Override
    public Map<String, String> getHeaders() {
        return httpRequest.getHeaders();
    }

    @Override
    public String getAuthorization() {
        return httpRequest.getAuthorization();
    }

    @Override
    public void setAuthorization(String authz) {
        httpRequest.setAuthorization(authz);
    }

    @Override
    public String getAccept() {
        return httpRequest.getAccept();
    }

    @Override
    public void setAccept(String accept) {
        httpRequest.setAccept(accept);
    }

    @Override
    public String getQuery() {
        return httpRequest.getQuery();
    }

    @Override
    public void setQuery(String query) {
        httpRequest.setQuery(query);
    }

    @Override
    public Map<String, String> getQueryParameters() {
        return httpRequest.getQueryParameters();
    }

    @Override
    public JSONObject getQueryAsJSONObject() throws ParseException {
        return httpRequest.getQueryAsJSONObject();
    }

    @Override
    public String getFragment() {
        return httpRequest.getFragment();
    }

    @Override
    public void setFragment(String fragment) {
        httpRequest.setFragment(fragment);
    }

    @Override
    public int getConnectTimeout() {
        return httpRequest.getConnectTimeout();
    }

    @Override
    public void setConnectTimeout(int connectTimeout) {
        httpRequest.setConnectTimeout(connectTimeout);
    }

    @Override
    public int getReadTimeout() {
        return httpRequest.getReadTimeout();
    }

    @Override
    public String toString() {
        return httpRequest.toString();
    }

    @Override
    public void setReadTimeout(int readTimeout) {
        httpRequest.setReadTimeout(readTimeout);
    }

    @Override
    public boolean getFollowRedirects() {
        return httpRequest.getFollowRedirects();
    }

    @Override
    public void setFollowRedirects(boolean follow) {
        httpRequest.setFollowRedirects(follow);
    }

    @Override
    public HttpURLConnection toHttpURLConnection() throws IOException {
        return httpRequest.toHttpURLConnection();
    }

    @Override
    public HttpURLConnection toHttpURLConnection(HostnameVerifier hostnameVerifier, SSLSocketFactory sslSocketFactory) throws IOException {
        if (sslSocketFactory == null) {
            SSLSocketFactoryProvider factoryProvider = Services.getOptionalService(SSLSocketFactoryProvider.class);
            if (factoryProvider != null) {
                sslSocketFactory = factoryProvider.getDefault();
            }
        }
        return httpRequest.toHttpURLConnection(hostnameVerifier, sslSocketFactory);
    }

    @Override
    public HTTPResponse send() throws IOException {
        return httpRequest.send();
    }

    @Override
    public HTTPResponse send(HostnameVerifier hostnameVerifier, SSLSocketFactory sslSocketFactory) throws IOException {
        if (sslSocketFactory == null) {
            SSLSocketFactoryProvider factoryProvider = Services.getOptionalService(SSLSocketFactoryProvider.class);
            if (factoryProvider != null) {
                sslSocketFactory = factoryProvider.getDefault();
            }
        }
        return httpRequest.send(hostnameVerifier, sslSocketFactory);
    }


}
