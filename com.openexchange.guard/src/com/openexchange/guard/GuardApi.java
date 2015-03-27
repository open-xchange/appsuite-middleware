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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.guard;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.auth.AuthScheme;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.version.Version;

/**
 * {@link GuardApi}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class GuardApi {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GuardApi.class);

    // -------------------------------------------------------------------------------------------------------------- //

    /** The status code policy to obey */
    public static interface StatusCodePolicy {

        /**
         * Examines given status line
         *
         * @param httpResponse The HTTP response
         * @throws OXException If an Open-Xchange error is yielded from status
         * @throws HttpResponseException If status is interpreted as an error
         */
        void handleStatusCode(HttpResponse httpResponse) throws OXException, HttpResponseException;
    }

    /** The default status code policy; accepting greater than/equal to <code>200</code> and lower than <code>300</code> */
    public static final StatusCodePolicy STATUS_CODE_POLICY_DEFAULT = new StatusCodePolicy() {

        @Override
        public void handleStatusCode(HttpResponse httpResponse) throws OXException, HttpResponseException {
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if (statusCode < 200 || statusCode >= 300) {
                if (404 == statusCode) {
                    throw GuardApiExceptionCodes.NOT_FOUND_SIMPLE.create();
                }
                String reason;
                try {
                    JSONObject jsonObject = new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = jsonObject.getJSONObject("error").getString("message");
                } catch (Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw new HttpResponseException(statusCode, reason);
            }
        }
    };

    /** The status code policy; accepting greater than/equal to <code>200</code> and lower than <code>300</code> while ignoring <code>404</code> */
    public static final StatusCodePolicy STATUS_CODE_POLICY_IGNORE_NOT_FOUND = new StatusCodePolicy() {

        @Override
        public void handleStatusCode(HttpResponse httpResponse) throws HttpResponseException {
            StatusLine statusLine = httpResponse.getStatusLine();
            int statusCode = statusLine.getStatusCode();
            if ((statusCode < 200 || statusCode >= 300) && statusCode != 404) {
                String reason;
                try {
                    JSONObject jsonObject = new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
                    reason = jsonObject.getJSONObject("error").getString("message");
                } catch (Exception e) {
                    reason = statusLine.getReasonPhrase();
                }
                throw new HttpResponseException(statusCode, reason);
            }
        }
    };

    // -------------------------------------------------------------------------------------------------------------- //

    private final String authLogin;
    private final String authPassword;
    private final URI uri;
    private volatile DefaultHttpClient httpClient;
    private volatile BasicHttpContext localcontext;
    private volatile HttpHost targetHost;

    /**
     * Initializes a new {@link GuardApi}.
     *
     * @param endPoint The end-point
     * @throws OXException If initialization fails
     */
    public GuardApi(String endPoint, ConfigurationService service) throws OXException {
        super();
        String authLogin = service.getProperty("com.openexchange.rest.services.basic-auth.login");
        String authPassword = service.getProperty("com.openexchange.rest.services.basic-auth.password");
        if (Strings.isEmpty(authLogin) || Strings.isEmpty(authPassword)) {
            LOGGER.error("Denied initialization due to unset Basic-Auth configuration. Please set properties 'com.openexchange.rest.services.basic-auth.login' and 'com.openexchange.rest.services.basic-auth.password' appropriately.");
            throw ServiceExceptionCode.absentService(ConfigurationService.class);
        }
        this.authLogin = authLogin.trim();
        this.authPassword = authPassword.trim();

        String sUrl = endPoint;
        try {
            uri = new URI(sUrl);
        } catch (URISyntaxException e) {
            throw GuardApiExceptionCodes.INVALID_GUARD_URL.create(null == sUrl ? "<empty>" : sUrl);
        }
    }

    private DefaultHttpClient getHttpClient() {
        DefaultHttpClient tmp = httpClient;
        if (null == tmp) {
            synchronized (this) {
                tmp = httpClient;
                if (null == tmp) {
                    tmp = HttpClients.getHttpClient("OX Guard Http Client v" + Version.getInstance().getVersionString());

                    Credentials credentials = new UsernamePasswordCredentials(authLogin, authPassword);
                    tmp.getCredentialsProvider().setCredentials(AuthScope.ANY, credentials);

                    // Generate BASIC scheme object and stick it to the local execution context
                    BasicHttpContext context = new BasicHttpContext();
                    BasicScheme basicAuth = new BasicScheme();
                    context.setAttribute("preemptive-auth", basicAuth);
                    this.localcontext = context;

                    // Add as the first request interceptor
                    tmp.addRequestInterceptor(new PreemptiveAuth(), 0);

                    HttpHost targetHost = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
                    this.targetHost = targetHost;

                    httpClient = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Performs the GET using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @throws OXException If GET fails
     */
    public <R> R doCallGet(Map<String, String> parameters, Class<? extends R> clazz) throws OXException {
        HttpGet request = null;
        try {
            request = new HttpGet(buildUri(toQueryString(parameters)));
            return handleHttpResponse(execute(request, getHttpClient()), clazz);
        } catch (HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed -- recreate token
                throw GuardApiExceptionCodes.AUTH_ERROR.create(e, e.getMessage());
            }
            throw handleHttpResponseError(null, e);
        } catch (IOException e) {
            throw handleIOError(e);
        } catch (RuntimeException e) {
            throw GuardApiExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            reset(request);
        }
    }

    /**
     * Performs the PUT using given parameters.
     *
     * @param parameters
     * @param clazz The return type
     * @throws OXException If PUT fails
     */
    public <R> R doCallPut(Map<String, String> parameters, JSONValue jsonBody, Class<? extends R> clazz) throws OXException {
        HttpPut request = null;
        try {
            request = new HttpPut(buildUri(toQueryString(parameters)));
            request.setEntity(asHttpEntity(jsonBody));
            return handleHttpResponse(execute(request, getHttpClient()), clazz);
        } catch (HttpResponseException e) {
            if (400 == e.getStatusCode() || 401 == e.getStatusCode()) {
                // Authentication failed -- recreate token
                throw GuardApiExceptionCodes.AUTH_ERROR.create(e, e.getMessage());
            }
            throw handleHttpResponseError(null, e);
        } catch (IOException e) {
            throw handleIOError(e);
        } catch (JSONException e) {
            throw GuardApiExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw GuardApiExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            reset(request);
        }
    }

    // ----------------------------------------------------------------------------------------------------------- //

    /**
     * Builds the URI from given arguments
     *
     * @param queryString The query string parameters
     * @return The built URI string
     * @throws IllegalArgumentException If the given string violates RFC 2396
     */
    protected URI buildUri(List<NameValuePair> queryString) {
        try {
            return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(), null == queryString ? null : URLEncodedUtils.format(queryString, "UTF-8"), null);
        } catch (URISyntaxException x) {
            IllegalArgumentException y = new IllegalArgumentException();
            y.initCause(x);
            throw y;
        }
    }

    /**
     * Turns specified JSON value into an appropriate HTTP entity.
     *
     * @param jValue The JSON value
     * @return The HTTP entity
     * @throws JSONException If a JSON error occurs
     * @throws IOException If an I/O error occurs
     */
    protected InputStreamEntity asHttpEntity(JSONValue jValue) throws JSONException, IOException {
        if (null == jValue) {
            return null;
        }

        ThresholdFileHolder sink = null;
        boolean error = true;
        try {
            sink = new ThresholdFileHolder();
            OutputStreamWriter osw = new OutputStreamWriter(sink.asOutputStream(), Charsets.UTF_8);
            jValue.write(osw);
            osw.flush();
            InputStreamEntity entity = new InputStreamEntity(sink.getStream(), sink.getLength(), ContentType.APPLICATION_JSON);
            error = false;
            return entity;
        } catch (OXException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                throw (IOException) cause;
            }
            throw new IOException(null == cause ? e : cause);
        } finally {
            if (error && null != sink) {
                Streams.close(sink);
            }
        }
    }

    /**
     * Gets the appropriate query string for given parameters
     *
     * @param parameters The parameters
     * @return The query string
     */
    protected List<NameValuePair> toQueryString(Map<String, String> parameters) {
        if (null == parameters || parameters.isEmpty()) {
            return null;
        }
        List<NameValuePair> l = new LinkedList<NameValuePair>();
        for (Map.Entry<String, String> e : parameters.entrySet()) {
            l.add(new BasicNameValuePair(e.getKey(), e.getValue()));
        }
        return l;
    }

    /**
     * Executes specified HTTP method/request using given HTTP client instance.
     *
     * @param method The method/request to execute
     * @param httpClient The HTTP client to use
     * @return The HTTP response
     * @throws ClientProtocolException If client protocol error occurs
     * @throws IOException If an I/O error occurs
     */
    protected HttpResponse execute(HttpRequestBase method, DefaultHttpClient httpClient) throws ClientProtocolException, IOException {
        return httpClient.execute(targetHost, method, localcontext);
    }

    /**
     * Resets given HTTP request
     *
     * @param request The HTTP request
     */
    protected void reset(HttpRequestBase request) {
        if (null != request) {
            try {
                request.reset();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Handles given HTTP response while expecting <code>200 (Ok)</code> status code.
     *
     * @param httpResponse The HTTP response
     * @param clazz The class of the result object
     * @return The result object
     * @throws OXException If an Open-Xchange error occurs
     * @throws ClientProtocolException If a client protocol error occurs
     * @throws IOException If an I/O error occurs
     */
    protected <R> R handleHttpResponse(HttpResponse httpResponse, Class<R> clazz) throws OXException, ClientProtocolException, IOException {
        return handleHttpResponse(httpResponse, STATUS_CODE_POLICY_DEFAULT, clazz);
    }

    /**
     * Handles given HTTP response while expecting given status code.
     *
     * @param httpResponse The HTTP response
     * @param policy The status code policy to obey
     * @param clazz The class of the result object
     * @return The result object
     * @throws OXException If an Open-Xchange error occurs
     * @throws ClientProtocolException If a client protocol error occurs
     * @throws IOException If an I/O error occurs
     * @throws IllegalStateException If content stream cannot be created
     */
    protected <R> R handleHttpResponse(HttpResponse httpResponse, StatusCodePolicy policy, Class<R> clazz) throws OXException, ClientProtocolException, IOException {
        policy.handleStatusCode(httpResponse);

        // OK, continue
        if (Void.class.equals(clazz)) {
            return null;
        }
        try {
            return (R) new JSONObject(new InputStreamReader(httpResponse.getEntity().getContent(), Charsets.UTF_8));
        } catch (JSONException e) {
            throw GuardApiExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Handles given I/O error.
     *
     * @param e The I/O error
     * @return The resulting exception
     */
    protected OXException handleIOError(IOException e) {
        Throwable cause = e.getCause();
        if (cause instanceof AuthenticationException) {
            return GuardApiExceptionCodes.AUTH_ERROR.create(cause, cause.getMessage());
        }
        return GuardApiExceptionCodes.IO_ERROR.create(e, e.getMessage());
    }

    /** Status code (401) indicating that the request requires HTTP authentication. */
    private static final int SC_UNAUTHORIZED = 401;

    /** Status code (404) indicating that the requested resource is not available. */
    private static final int SC_NOT_FOUND = 404;

    /**
     * Handles given HTTP response error.
     *
     * @param identifier The optional identifier for associated Microsoft OneDrive resource
     * @param e The HTTP error
     * @return The resulting exception
     */
    protected OXException handleHttpResponseError(String identifier, HttpResponseException e) {
        if (null != identifier && SC_NOT_FOUND == e.getStatusCode()) {
            return GuardApiExceptionCodes.NOT_FOUND.create(e, identifier);
        }
        if (SC_UNAUTHORIZED == e.getStatusCode()) {
            return GuardApiExceptionCodes.AUTH_ERROR.create();
        }
        return GuardApiExceptionCodes.GUARD_SERVER_ERROR.create(e, Integer.valueOf(e.getStatusCode()), e.getMessage());
    }

    static class PreemptiveAuth implements HttpRequestInterceptor {

        @Override
        public void process(HttpRequest request, HttpContext context) throws HttpException, IOException {

            AuthState authState = (AuthState) context.getAttribute(
                ClientContext.TARGET_AUTH_STATE);

            // If no auth scheme avaialble yet, try to initialize it preemptively
            if (authState.getAuthScheme() == null) {
                AuthScheme authScheme = (AuthScheme) context.getAttribute(
                    "preemptive-auth");
                CredentialsProvider credsProvider = (CredentialsProvider) context.getAttribute(
                    ClientContext.CREDS_PROVIDER);
                HttpHost targetHost = (HttpHost) context.getAttribute(
                    ExecutionContext.HTTP_TARGET_HOST);
                if (authScheme != null) {
                    Credentials creds = credsProvider.getCredentials(
                        new AuthScope(
                            targetHost.getHostName(),
                            targetHost.getPort()));
                    if (creds == null) {
                        throw new HttpException("No credentials for preemptive authentication");
                    }
                    authState.setAuthScheme(authScheme);
                    authState.setCredentials(creds);
                }
            }
        }
    }

}
