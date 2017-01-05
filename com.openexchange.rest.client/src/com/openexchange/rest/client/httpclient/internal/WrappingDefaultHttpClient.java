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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.rest.client.httpclient.internal;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.client.AuthenticationHandler;
import org.apache.http.client.AuthenticationStrategy;
import org.apache.http.client.BackoffManager;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ConnectionBackoffStrategy;
import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.UserTokenHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.cookie.CookieSpecRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import com.openexchange.rest.client.httpclient.HttpClients;

/**
 * {@link WrappingDefaultHttpClient} - A wrapper for a <code>DefaultHttpClient</code> instance, which can be exchanged.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class WrappingDefaultHttpClient extends DefaultHttpClient {

    private final AtomicReference<DefaultHttpClient> httpClientReference;

    /**
     * Initializes a new {@link WrappingDefaultHttpClient}.
     *
     * @param httpClient The HTTP client to wrap
     */
    public WrappingDefaultHttpClient(DefaultHttpClient httpClient) {
        super();
        if (null == httpClient) {
            throw new IllegalArgumentException("HttpClient must not be null.");
        }
        httpClientReference = new AtomicReference<DefaultHttpClient>(httpClient);
    }

    /**
     * Replaces the currently wrapped <code>DefaultHttpClient</code> instance with the given one.
     *
     * @param httpClient The new <code>DefaultHttpClient</code> instance to apply
     */
    public void replaceHttpClient(DefaultHttpClient httpClient) {
        DefaultHttpClient cur;
        do {
            cur = httpClientReference.get();
        } while (false == httpClientReference.compareAndSet(cur, httpClient));

        if (null != cur) {
            HttpClients.shutDown(cur);
        }
    }

    private DefaultHttpClient getHttpClient() {
        DefaultHttpClient httpClient = httpClientReference.get();
        if (null == httpClient) {
            throw new IllegalStateException("HttpClient is null.");
        }
        return httpClient;
    }

    @Override
    public CloseableHttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return getHttpClient().execute(target, request, context);
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return getHttpClient().execute(request, context);
    }

    @Override
    public CloseableHttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        return getHttpClient().execute(request);
    }

    @Override
    public CloseableHttpResponse execute(HttpHost target, HttpRequest request) throws IOException, ClientProtocolException {
        return getHttpClient().execute(target, request);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return getHttpClient().execute(request, responseHandler);
    }

    @Override
    public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return getHttpClient().execute(request, responseHandler, context);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException, ClientProtocolException {
        return getHttpClient().execute(target, request, responseHandler);
    }

    @Override
    public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException, ClientProtocolException {
        return getHttpClient().execute(target, request, responseHandler, context);
    }

    @Override
    public String toString() {
        return getHttpClient().toString();
    }

    @Override
    public void setParams(HttpParams params) {
        getHttpClient().setParams(params);
    }

    @Override
    public void setAuthSchemes(AuthSchemeRegistry registry) {
        getHttpClient().setAuthSchemes(registry);
    }

    @Override
    public void setConnectionBackoffStrategy(ConnectionBackoffStrategy strategy) {
        getHttpClient().setConnectionBackoffStrategy(strategy);
    }

    @Override
    public void setBackoffManager(BackoffManager manager) {
        getHttpClient().setBackoffManager(manager);
    }

    @Override
    public void setCookieSpecs(CookieSpecRegistry registry) {
        getHttpClient().setCookieSpecs(registry);
    }

    @Override
    public void setReuseStrategy(ConnectionReuseStrategy strategy) {
        getHttpClient().setReuseStrategy(strategy);
    }

    @Override
    public void setKeepAliveStrategy(ConnectionKeepAliveStrategy strategy) {
        getHttpClient().setKeepAliveStrategy(strategy);
    }

    @Override
    public void setHttpRequestRetryHandler(HttpRequestRetryHandler handler) {
        getHttpClient().setHttpRequestRetryHandler(handler);
    }

    @Override
    public void setRedirectHandler(RedirectHandler handler) {
        getHttpClient().setRedirectHandler(handler);
    }

    @Override
    public void setRedirectStrategy(RedirectStrategy strategy) {
        getHttpClient().setRedirectStrategy(strategy);
    }

    @Override
    public void setTargetAuthenticationHandler(AuthenticationHandler handler) {
        getHttpClient().setTargetAuthenticationHandler(handler);
    }

    @Override
    public void setTargetAuthenticationStrategy(AuthenticationStrategy strategy) {
        getHttpClient().setTargetAuthenticationStrategy(strategy);
    }

    @Override
    public void setProxyAuthenticationHandler(AuthenticationHandler handler) {
        getHttpClient().setProxyAuthenticationHandler(handler);
    }

    @Override
    public void setProxyAuthenticationStrategy(AuthenticationStrategy strategy) {
        getHttpClient().setProxyAuthenticationStrategy(strategy);
    }

    @Override
    public void setCookieStore(CookieStore cookieStore) {
        getHttpClient().setCookieStore(cookieStore);
    }

    @Override
    public void setCredentialsProvider(CredentialsProvider credsProvider) {
        getHttpClient().setCredentialsProvider(credsProvider);
    }

    @Override
    public void setRoutePlanner(HttpRoutePlanner routePlanner) {
        getHttpClient().setRoutePlanner(routePlanner);
    }

    @Override
    public void setUserTokenHandler(UserTokenHandler handler) {
        getHttpClient().setUserTokenHandler(handler);
    }

    @Override
    public int getResponseInterceptorCount() {
        return getHttpClient().getResponseInterceptorCount();
    }

    @Override
    public HttpResponseInterceptor getResponseInterceptor(int index) {
        return getHttpClient().getResponseInterceptor(index);
    }

    @Override
    public HttpRequestInterceptor getRequestInterceptor(int index) {
        return getHttpClient().getRequestInterceptor(index);
    }

    @Override
    public int getRequestInterceptorCount() {
        return getHttpClient().getRequestInterceptorCount();
    }

    @Override
    public void addResponseInterceptor(HttpResponseInterceptor itcp) {
        getHttpClient().addResponseInterceptor(itcp);
    }

    @Override
    public void addResponseInterceptor(HttpResponseInterceptor itcp, int index) {
        getHttpClient().addResponseInterceptor(itcp, index);
    }

    @Override
    public void clearResponseInterceptors() {
        getHttpClient().clearResponseInterceptors();
    }

    @Override
    public void removeResponseInterceptorByClass(Class<? extends HttpResponseInterceptor> clazz) {
        getHttpClient().removeResponseInterceptorByClass(clazz);
    }

    @Override
    public void addRequestInterceptor(HttpRequestInterceptor itcp) {
        getHttpClient().addRequestInterceptor(itcp);
    }

    @Override
    public void addRequestInterceptor(HttpRequestInterceptor itcp, int index) {
        getHttpClient().addRequestInterceptor(itcp, index);
    }

    @Override
    public void clearRequestInterceptors() {
        getHttpClient().clearRequestInterceptors();
    }

    @Override
    public void removeRequestInterceptorByClass(Class<? extends HttpRequestInterceptor> clazz) {
        getHttpClient().removeRequestInterceptorByClass(clazz);
    }

    @Override
    public void close() {
        getHttpClient().close();
    }

}
