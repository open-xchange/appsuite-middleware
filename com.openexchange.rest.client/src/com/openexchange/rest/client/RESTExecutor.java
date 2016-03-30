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

package com.openexchange.rest.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import javax.net.ssl.SSLException;
import org.apache.commons.codec.CharEncoding;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.API.RequestAndResponse;
import com.openexchange.rest.client.exception.HTTPResponseCodes;
import com.openexchange.rest.client.exception.RESTExceptionCodes;
import com.openexchange.rest.client.session.Session;
import com.openexchange.rest.client.session.Session.ProxyInfo;

/**
 * {@link RESTExecutor}. Used to create, execute and parse the responses of REST requests to any REST API.
 * 
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RESTExecutor {

    private static final URLCodec URL_CODEC = new URLCodec(CharEncoding.UTF_8);

    /**
     * Initializes a new {@link RESTExecutor}.
     */
    private RESTExecutor() {
        super();
    }
    
    /**
     * Creates and sends a request to the REST API, and returns a {@link RequestAndResponse} containing the {@link HttpUriRequest} and
     * {@link HttpResponse}.
     * 
     * @param restRequest The RESTRequest
     * @param expectResponseBody Flag to indicate whether or not to expect a response body
     * @return A {@link RESTResponse} encapsulating the status code, the {@link HttpResponse} and the optional response body as JSONObject
     * @throws OXException If the server responds with an error code, or if any network-related error occurs, or if the user has revoked
     *             access, or if any other unknown error occurs.
     */
    public static RESTResponse streamRequest(final RESTRequest restRequest, final boolean expectResponseBody) throws OXException {
        final HttpRequestBase req;
        final String requestURL = buildURL(restRequest.getHost(), restRequest.getAPIVersion(), restRequest.getPath(), restRequest.getParams());
        switch (restRequest.getMethod()) {
        case PUT: {
            final HttpPut put = new HttpPut(requestURL);
            final JSONObject requestInformation = restRequest.getRequestBody();
            if (null != requestInformation) {
                put.setEntity(new InputStreamEntity(new JSONInputStream(requestInformation, CharEncoding.UTF_8), -1L, ContentType.APPLICATION_JSON));
            }
            req = put;
        }
            break;
        case POST: {
            final HttpPost post = new HttpPost(requestURL);
            final JSONObject requestInformation = restRequest.getRequestBody();
            if (null != requestInformation) {
                try {
                    final int contentLength = requestInformation.toString().getBytes(CharEncoding.UTF_8).length;
                    post.setEntity(new InputStreamEntity(new JSONInputStream(requestInformation, CharEncoding.UTF_8), contentLength, ContentType.APPLICATION_JSON));
                } catch (UnsupportedEncodingException e) {
                    throw RESTExceptionCodes.UNSUPPORTED_ENCODING.create(CharEncoding.UTF_8);
                }
            }
            req = post;
        }
            break;
        case GET:
            req = new HttpGet(requestURL);
            break;
        case DELETE:
            req = new HttpDelete(requestURL);
            break;
        default:
            throw RESTExceptionCodes.UNSUPPORTED_METHOD.create(restRequest.getMethod());
        }
        // Sign request
        restRequest.getSession().sign(req);

        final List<Integer> expectedStatusCodes = restRequest.getExpectedStatusCodes();
        final HttpResponse resp = execute(restRequest.getSession(), req, -1, expectedStatusCodes);
        final int statusCode = resp.getStatusLine().getStatusCode();
        final RESTResponse restResponse = new RESTResponse(statusCode, resp);

        if (expectResponseBody) {
            final JSONValue json = parseAsJSON(restResponse.getResponse(), restRequest.getExpectedStatusCodes());
            restResponse.setResponseBody(json);
        }

        return restResponse;
    }

    /**
     * Creates a URL for a REST request
     * 
     * @param host The host on which resides the REST API
     * @param apiVersion The optional API version to use. Or <code>-1</code> to ignore
     * @param target The target path, starting with a '/'.
     * @param params Any URL parameters in a String array. with the even numbered elements the parameter names and odd numbered elements the
     *            values, e.g. <code>new String[] {"path", "/Public", "locale", "en"}</code>.
     * @return A full URL for making a request.
     */
    private static String buildURL(final String host, final int apiVersion, final String target, final String[] params) {
        String trgt = new String();
        if (Strings.isEmpty(target)) {
            if (params != null && params.length > 0) {
                final StringBuilder sb = new StringBuilder(params.length << 4);
                sb.append('?').append(urlencode(params));
                trgt = sb.toString();
            }
        } else {
            // Path is not empty
            trgt = target;
            if (!trgt.startsWith("/")) {
                trgt = new StringBuilder(trgt.length() + 1).append('/').append(trgt).toString();
            }

            try {
                // We have to encode the whole line, then remove + and / encoding to get a good OAuth URL.
                final StringBuilder versionBuilder = new StringBuilder(16);
                if (apiVersion > 0) {
                    versionBuilder.append("/v").append(apiVersion);
                }
                trgt = URLEncoder.encode(versionBuilder.append(trgt).toString(), CharEncoding.UTF_8);
                trgt = trgt.replace("%2F", "/");

                if (params != null && params.length > 0) {
                    final StringBuilder sb = new StringBuilder(trgt);
                    sb.append('?').append(urlencode(params));
                    trgt = sb.toString();
                }

                // These substitutions must be made to keep OAuth happy.
                trgt = trgt.replace("+", "%20").replace("*", "%2A");
            } catch (final UnsupportedEncodingException uce) {
                return null;
            }
        }
        return buildURL(host, trgt);
    }

    /**
     * Executes an {@link HttpUriRequest} with the given {@link Session} and returns an {@link HttpResponse}.
     * 
     * @param session The {@link Session} to use for this request.
     * @param req The request to execute.
     * @param socketTimeoutOverrideMs If >= 0, the socket timeout to set on this request. Does nothing if set to a negative number.
     * @param expectedStatusCodes The expected status code(s) on successful response
     * @return An {@link HttpResponse}.
     * @throws OXException If the server responds with an error code, or if any network-related error occurs, or if the user has revoked
     *             access, or if any other unknown error occurs.
     */
    private static HttpResponse execute(final Session session, final HttpUriRequest req, final int socketTimeoutOverrideMs, final List<Integer> expectedStatusCodes) throws OXException {
        final HttpClient client = updatedHttpClient(session);

        // Set request timeouts.
        session.setRequestTimeout(req);
        if (socketTimeoutOverrideMs >= 0) {
            final HttpParams reqParams = req.getParams();
            HttpConnectionParams.setSoTimeout(reqParams, socketTimeoutOverrideMs);
        }

        final boolean repeatable = isRequestRepeatable(req);

        try {
            HttpResponse response = null;
            for (int retries = 0; response == null && retries < 5; retries++) {
                // Maybe we don't need that workaround anymore, since we are operating with HttpClient 4.2.1 
                
                /*
                 * The try/catch is a workaround for a bug in the HttpClient libraries. It should be returning null instead when an error
                 * occurs. Fixed in HttpClient 4.1, but we're stuck with this for now. See:
                 * http://code.google.com/p/android/issues/detail?id=5255
                 */
                try {
                    response = client.execute(req);
                } catch (final NullPointerException e) {
                    // Leave 'response' as null. This is handled below.
                }

                /*
                 * We've potentially connected to a different network, but are still using the old proxy settings. Refresh proxy settings so
                 * that we can retry this request.
                 */
                if (response == null) {
                    updateClientProxy(client, session);
                }

                if (!repeatable) {
                    break;
                }
            }

            if (response == null) {
                // This is from that bug, and retrying hasn't fixed it.
                throw RESTExceptionCodes.IO_EXCEPTION.create("Apache HTTPClient encountered an error. No response, try again.");
            }

            return response;
        } catch (final SSLException e) {
            throw RESTExceptionCodes.SSL_EXCEPTION.create(e.getMessage());
        } catch (final IOException e) {
            throw RESTExceptionCodes.IO_EXCEPTION.create(e.getMessage());
        } catch (final OutOfMemoryError e) {
            throw RESTExceptionCodes.OOM_EXCEPTION.create(e.getMessage());
        }
    }

    /**
     * Reads in content from an {@link HttpResponse} and parses it as JSON.
     * 
     * @param response The {@link HttpResponse}.
     * @param expectedStatusCodes Contains the expected status code on successful response
     * @return a parsed JSON object, typically a Map or a JSONArray.
     * @throws OXException If the server responds with an error code, or if any network-related error occurs while reading in content from
     *             the {@link HttpResponse}, or if the user has revoked access, or if a malformed or an unknown response was received from
     *             the server, or if any other unknown error occurs.
     */
    private static JSONValue parseAsJSON(final HttpResponse response, final List<Integer> expectedStatusCodes) throws OXException {
        JSONValue result = null;

        BufferedReader bin = null;
        try {
            final HttpEntity ent = response.getEntity();
            if (ent != null) {
                final InputStreamReader in = new InputStreamReader(ent.getContent());
                // Wrap this with a Buffer, so we can re-parse it if it's
                // not JSON
                // Has to be at least 16384, because this is defined as the buffer size in
                // org.json.simple.parser.Yylex.java
                // and otherwise the reset() call won't work
                bin = new BufferedReader(in, 16384);
                bin.mark(16384);
                result = JSONObject.parse(bin);
            }
        } catch (final IOException e) {
            throw RESTExceptionCodes.IO_EXCEPTION.create(e);
        } catch (final JSONException e) {
            if (RESTExceptionCodes.isValidWithNullBody(response)) {
                // We have something from the server, but it's an error with no reason
                throw RESTExceptionCodes.ERROR.create(response);
            }
            // This is from the REST server, and we shouldn't be getting it
            final String body = RESTExceptionCodes.stringifyBody(bin);
            if (Strings.isEmpty(body)) {
                throw RESTExceptionCodes.ERROR.create(response, result);
            }
            throw RESTExceptionCodes.PARSE_ERROR.create(body);
        } catch (final OutOfMemoryError e) {
            throw RESTExceptionCodes.OOM_EXCEPTION.create(e);
        } finally {
            Streams.close(bin);
        }

        final int statusCode = response.getStatusLine().getStatusCode();
        if (false == expectedStatusCodes.contains(statusCode)) {
            if (statusCode == HTTPResponseCodes._401_UNAUTHORIZED) {
                throw RESTExceptionCodes.UNAUTHORIZED.create();
            }
            throw RESTExceptionCodes.ERROR.create(response, result);
        }

        return result;
    }

    /**
     * Verifies whether the specified {@link HttpRequest} is repeatable. If the request contains an {@link HttpEntity } that can't be "reset"
     * (like an {@link InputStream}), hence it isn't repeatable.
     * 
     * @param req The {@link HttpRequest}
     * @return true of the request is repeatable; false otherwise
     */
    private static boolean isRequestRepeatable(final HttpRequest req) {
        if (req instanceof HttpEntityEnclosingRequest) {
            final HttpEntityEnclosingRequest ereq = (HttpEntityEnclosingRequest) req;
            final HttpEntity entity = ereq.getEntity();
            if (entity != null && !entity.isRepeatable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the session's client and updates its proxy.
     */
    private static synchronized HttpClient updatedHttpClient(final Session session) {
        final HttpClient client = session.getHttpClient();
        updateClientProxy(client, session);
        return client;
    }

    /**
     * Updates the given client's proxy from the session.
     */
    private static void updateClientProxy(final HttpClient client, final Session session) {
        final ProxyInfo proxyInfo = session.getProxyInfo();
        if (proxyInfo != null && proxyInfo.host != null && !proxyInfo.host.equals("")) {
            HttpHost proxy;
            if (proxyInfo.port < 0) {
                proxy = new HttpHost(proxyInfo.host);
            } else {
                proxy = new HttpHost(proxyInfo.host, proxyInfo.port);
            }
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        } else {
            client.getParams().removeParameter(ConnRoutePNames.DEFAULT_PROXY);
        }
    }

    /**
     * Build an http(s) URL according to the prefix of the host.
     * 
     * @param host The host
     * @param target The target path, starting with a '/'.
     * @return The http(s) URL
     */
    private static String buildURL(final String host, final String target) {
        final StringBuilder sb = new StringBuilder(64);
        if (host.startsWith("http://") || host.startsWith("https://")) {
            sb.append(host);
        } else {
            sb.append("https://").append(host).append(":443");
        }
        return sb.append(target).toString();
    }

    /**
     * URL encodes an array of parameters into a query string.
     * 
     * @param params The parameters to encode
     * @return The encoded parameters as string
     */
    private static String urlencode(final String[] params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Params must have an even number of elements.");
        }
        try {
            final StringBuilder result = new StringBuilder(params.length << 4);
            boolean firstTime = true;
            for (int i = 0; i < params.length; i += 2) {
                final String value = params[i + 1];
                if (null != value) {
                    if (firstTime) {
                        firstTime = false;
                    } else {
                        result.append('&');
                    }
                    result.append(encodeUrl(params[i])).append('=').append(encodeUrl(value));
                }
            }
            return result.toString().replace("*", "%2A");
        } catch (final RuntimeException e) {
            return null;
        }
    }

    /**
     * URL encodes given string.
     * 
     * @param s A string to encode
     * @return The encoded string
     */
    private static final String encodeUrl(String s) {
        try {
            return Strings.isEmpty(s) ? s : URL_CODEC.encode(s);
        } catch (EncoderException e) {
            return s;
        }
    }
}
