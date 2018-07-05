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
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import com.openexchange.rest.client.httpclient.HttpClients;

/**
 * {@link WrappingCloseableHttpClient} - A wrapper for a <code>CloseableHttpClient</code> instance, which can be exchanged.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class WrappingCloseableHttpClient extends CloseableHttpClient {

    private final AtomicReference<CloseableHttpClient> httpClientReference;

    /**
     * Initializes a new {@link WrappingCloseableHttpClient}.
     *
     * @param httpClient The HTTP client to wrap
     */
    public WrappingCloseableHttpClient(CloseableHttpClient httpClient) {
        super();
        if (null == httpClient) {
            throw new IllegalArgumentException("HttpClient must not be null.");
        }
        httpClientReference = new AtomicReference<CloseableHttpClient>(httpClient);
    }

    /**
     * Replaces the currently wrapped <code>CloseableHttpClient</code> instance with the given one.
     *
     * @param httpClient The new <code>CloseableHttpClient</code> instance to apply
     */
    public void replaceHttpClient(CloseableHttpClient httpClient) {
        CloseableHttpClient cur;
        do {
            cur = httpClientReference.get();
        } while (false == httpClientReference.compareAndSet(cur, httpClient));

        if (null != cur) {
            HttpClients.shutDown(cur);
        }
    }

    private CloseableHttpClient getHttpClient() {
        CloseableHttpClient httpClient = httpClientReference.get();
        if (null == httpClient) {
            throw new IllegalStateException("HttpClient is null.");
        }
        return httpClient;
    }

    @Override
    protected CloseableHttpResponse doExecute(HttpHost target, HttpRequest request, HttpContext context) throws IOException, ClientProtocolException {
        return null;
    }

    @Override
    public void close() throws IOException {
        getHttpClient().close();
    }

    @Override
    public HttpParams getParams() {
        return getHttpClient().getParams();
    }

    @Override
    public ClientConnectionManager getConnectionManager() {
        return getHttpClient().getConnectionManager();
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

}
