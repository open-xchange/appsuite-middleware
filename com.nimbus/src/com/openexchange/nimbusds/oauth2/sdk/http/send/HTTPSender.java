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

package com.openexchange.nimbusds.oauth2.sdk.http.send;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import javax.mail.internet.ContentType;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import com.nimbusds.oauth2.sdk.http.HTTPRequest;
import com.nimbusds.oauth2.sdk.http.HTTPRequest.Method;
import com.openexchange.java.Strings;
import com.nimbusds.oauth2.sdk.http.HTTPResponse;

/**
 * {@link HTTPSender} - A utility class to send HTTP requests with respect to configured out-bound HTTP settings (e.g. read timeout).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class HTTPSender {

    /**
     * Initializes a new {@link HTTPSender}.
     */
    private HTTPSender() {
        super();
    }

    /**
     * Sends specified HTTP request to the request URL using provider's {@link HttpClient} and retrieves the resulting HTTP response.
     * <p>
     * Sending is performed with respect to following request configuration items:
     * <ul>
     * <li>{@link HTTPRequest#getConnectTimeout()}</li>
     * <li>{@link HTTPRequest#getReadTimeout()}</li>
     * <li>{@link HTTPRequest#getFollowRedirects()}</li>
     * </ul>
     * <p>
     * All other environment properties required/involved to send HTTP request (SSL socket factory, host name verification, etc.) is taken
     * over from <code>HttpClient</code> instance.
     *
     * @param httpRequest The HTTP request to send
     * @param httpClientProvider The provider for the <code>HttpClient</code> instance to use
     * @return The resulting HTTP response.
     * @throws IOException If the HTTP request couldn't be performed, due to a network or other error.
     */
    public static HTTPResponse send(HTTPRequest httpRequest, HttpClientProvider httpClientProvider) throws IOException {
        // Compile request URI
        URI uri = compileRequestUri(httpRequest);

        // Initialize/obtain HTTP client
        HttpClient httpClient = httpClientProvider.getHttpClient();

        HttpRequestBase request = null;
        HttpResponse resp = null;
        try {
            // Initialize HTTP request from given HTTPRequest instance
            request = initHttpRequest(httpRequest, uri);

            // Execute HTTP request and get response
            resp = httpClient.execute(request);

            // Convert HTTP response to HTTPResponse instance & return
            return convertToHTTPResponse(resp);
        } finally {
            Utils.close(request, resp);
        }
    }
    
    private static URI compileRequestUri(HTTPRequest httpRequest) throws IOException {        
        Method method = httpRequest.getMethod();
        String query = httpRequest.getQuery();
        URL finalURL = httpRequest.getURL();

        if (query != null && (method == HTTPRequest.Method.GET || method == HTTPRequest.Method.DELETE)) {
            StringBuilder sb = new StringBuilder(httpRequest.getURL().toString());
            sb.append('?');
            sb.append(query);

            try {
                finalURL = new URL(sb.toString());
            } catch (MalformedURLException e) {
                throw new IOException("Couldn't append query string: " + e.getMessage(), e);
            }
        }

        String fragment = httpRequest.getFragment();
        if (fragment != null) {
            // Append raw fragment
            StringBuilder sb = new StringBuilder(finalURL.toString());
            sb.append('#');
            sb.append(fragment);

            try {
                finalURL = new URL(sb.toString());
            } catch (MalformedURLException e) {
                throw new IOException("Couldn't append raw fragment: " + e.getMessage(), e);
            }
        }

        try {
            return finalURL.toURI();
        } catch (URISyntaxException e) {
            throw new IOException("Failed to obtain URI equivalent for URL: " + finalURL.toString(), e);
        }
    }

    private static HttpRequestBase initHttpRequest(HTTPRequest httpRequest, URI uri) throws IOException {
        HttpRequestBase request;
        switch (httpRequest.getMethod()) {
            case DELETE:
                request = new HttpDelete(uri);
                break;
            case GET:
                request = new HttpGet(uri);
                break;
            case POST:
                request = new HttpPost(uri);
                break;
            case PUT:
                request = new HttpPut(uri);
                break;
            default:
                throw new IOException("Unknown method: " + httpRequest.getMethod());
        }

        // Take over request headers
        for (Map.Entry<String,String> header: httpRequest.getHeaders().entrySet()) {
            request.addHeader(header.getKey(), header.getValue());
        }

        // Configure request (timeout, redirect behavior, etc.)
        configureHttpRequest(httpRequest, request);

        // Set request body
        setRequestBody(httpRequest, request);
        
        // Return
        return request;
    }
    
    private static void configureHttpRequest(HTTPRequest httpRequest, HttpRequestBase request) {
        RequestConfig.Builder requestConfigBuilder = request.getConfig() == null ? RequestConfig.custom() : RequestConfig.copy(request.getConfig());
        
        if (httpRequest.getConnectTimeout() != 0) {
            // Apply specified connect timeout
            requestConfigBuilder.setConnectTimeout(httpRequest.getConnectTimeout());
        }
        
        if (httpRequest.getReadTimeout() != 0) {
            // Apply specified read timeout
            requestConfigBuilder.setSocketTimeout(httpRequest.getReadTimeout());
        }
        
        // Apply specified redirect behavior
        requestConfigBuilder.setRedirectsEnabled(httpRequest.getFollowRedirects());
        
        // Set request config
        request.setConfig(requestConfigBuilder.build());
    }

    private static void setRequestBody(HTTPRequest httpRequest, HttpRequestBase request) throws UnsupportedEncodingException {
        String query = httpRequest.getQuery();
        if (query != null && (request instanceof HttpEntityEnclosingRequest)) {
            HttpEntityEnclosingRequest entityEnclosingRequest = (HttpEntityEnclosingRequest) request;
            
            ContentType contentType = httpRequest.getContentType();
            if (contentType == null) {
                entityEnclosingRequest.setEntity(new StringEntity(query));
            } else {
                entityEnclosingRequest.setEntity(new StringEntity(query, org.apache.http.entity.ContentType.parse(contentType.toString())));
            }
        }
    }

    private static HTTPResponse convertToHTTPResponse(HttpResponse resp) throws IOException {
        // Obtain HTTP code + message
        int statusCode = resp.getStatusLine().getStatusCode();
        String statusMessage = resp.getStatusLine().getReasonPhrase();

        // Consume response entity
        HttpEntity entity = resp.getEntity();
        try {
            String bodyContent = EntityUtils.toString(entity);

            HTTPResponse response = new HTTPResponse(statusCode);
            response.setStatusMessage(statusMessage);

            for (Header header : resp.getAllHeaders()) {
                String name = header.getName();
                if (name != null) {
                    String value = header.getValue();
                    if (Strings.isNotEmpty(value)) {
                        response.setHeader(name, value);
                    }
                }
            }

            if (Strings.isNotEmpty(bodyContent)) {
                response.setContent(bodyContent);
            }

            return response;
        } finally {
            EntityUtils.consumeQuietly(entity);
        }
    }

}
