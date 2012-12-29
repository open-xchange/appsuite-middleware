/*
 * Copyright (c) 2009-2011 Xing, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.openexchange.xing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Scanner;
import javax.net.ssl.SSLException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.java.Streams;
import com.openexchange.xing.exception.XingApiException;
import com.openexchange.xing.exception.XingException;
import com.openexchange.xing.exception.XingIOException;
import com.openexchange.xing.exception.XingParseException;
import com.openexchange.xing.exception.XingSSLException;
import com.openexchange.xing.exception.XingServerException;
import com.openexchange.xing.exception.XingUnlinkedException;
import com.openexchange.xing.session.Session;
import com.openexchange.xing.session.Session.ProxyInfo;

/**
 * This class is mostly used internally by {@link XingAPI} for creating and executing REST requests to the Xing API, and parsing responses.
 * You probably won't have a use for it other than {@link #parseDate(String)} for parsing modified times returned in metadata, or (in very
 * rare circumstances) writing your own API calls.
 */
public class RESTUtility {

    private RESTUtility() {
    }

    private static final DateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss ZZZZZ", Locale.US);

    /** The HTTP method enumeration */
    public static enum Method {
        GET, POST;
    }

    /**
     * Creates and sends a request to the Xing API, parses the response as JSON, and returns the result.
     * 
     * @param method GET or POST.
     * @param host the hostname to use. Should be either api server, content server, or web server.
     * @param path the URL path, starting with a '/'.
     * @param apiVersion the API version to use. This should almost always be set to {@code XingAPI.VERSION}.
     * @param params the URL params in an array, with the even numbered elements the parameter names and odd numbered elements the values,
     *            e.g. <code>new String[] {"path", "/Public", "locale",
     *         "en"}</code>.
     * @param session the {@link Session} to use for this request.
     * @return a parsed JSON object, typically a Map or a JSONArray.
     * @throws XingServerException if the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException if any network-related error occurs.
     * @throws XingUnlinkedException if the user has revoked access.
     * @throws XingParseException if a malformed or unknown response was received from the server.
     * @throws XingException for any other unknown errors. This is also a superclass of all other Xing exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    static public Object request(Method method, String host, String path, int apiVersion, String[] params, Session session) throws XingException {
        HttpResponse resp = streamRequest(method, host, path, apiVersion, params, session).response;
        return parseAsJSON(resp);
    }

    /**
     * Creates and sends a request to the Xing API, and returns a {@link RequestAndResponse} containing the {@link HttpUriRequest} and
     * {@link HttpResponse}.
     * 
     * @param method GET or POST.
     * @param host the hostname to use. Should be either api server, content server, or web server.
     * @param path the URL path, starting with a '/'.
     * @param apiVersion the API version to use. This should almost always be set to {@code XingAPI.VERSION}.
     * @param params the URL params in an array, with the even numbered elements the parameter names and odd numbered elements the values,
     *            e.g. <code>new String[] {"path", "/Public", "locale",
     *         "en"}</code>.
     * @param session the {@link Session} to use for this request.
     * @return a parsed JSON object, typically a Map or a JSONArray.
     * @throws XingServerException if the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException if any network-related error occurs.
     * @throws XingUnlinkedException if the user has revoked access.
     * @throws XingException for any other unknown errors. This is also a superclass of all other Xing exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public static RequestAndResponse streamRequest(Method method, String host, String path, int apiVersion, String params[], Session session) throws XingException {
        HttpRequestBase req = null;
        String target = null;

        if (method == Method.GET) {
            target = buildURL(host, apiVersion, path, params);
            req = new HttpGet(target);
        } else {
            target = buildURL(host, apiVersion, path, null);
            HttpPost post = new HttpPost(target);

            if (params != null && params.length >= 2) {
                if (params.length % 2 != 0) {
                    throw new IllegalArgumentException("Params must have an even number of elements.");
                }
                List<NameValuePair> nvps = new ArrayList<NameValuePair>();

                for (int i = 0; i < params.length; i += 2) {
                    if (params[i + 1] != null) {
                        nvps.add(new BasicNameValuePair(params[i], params[i + 1]));
                    }
                }

                try {
                    post.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
                } catch (UnsupportedEncodingException e) {
                    throw new XingException(e);
                }
            }

            req = post;
        }

        session.sign(req);
        HttpResponse resp = execute(session, req);
        return new RequestAndResponse(req, resp);
    }

    /**
     * Reads in content from an {@link HttpResponse} and parses it as JSON.
     * 
     * @param response the {@link HttpResponse}.
     * @return a parsed JSON object, typically a Map or a JSONArray.
     * @throws XingServerException if the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException if any network-related error occurs while reading in content from the {@link HttpResponse}.
     * @throws XingUnlinkedException if the user has revoked access.
     * @throws XingParseException if a malformed or unknown response was received from the server.
     * @throws XingException for any other unknown errors. This is also a superclass of all other Xing exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public static JSONValue parseAsJSON(HttpResponse response) throws XingException {
        JSONValue result = null;

        BufferedReader bin = null;
        try {
            HttpEntity ent = response.getEntity();
            if (ent != null) {
                InputStreamReader in = new InputStreamReader(ent.getContent());
                // Wrap this with a Buffer, so we can re-parse it if it's
                // not JSON
                // Has to be at least 16384, because this is defined as the buffer size in
                // org.json.simple.parser.Yylex.java
                // and otherwise the reset() call won't work
                bin = new BufferedReader(in, 16384);
                bin.mark(16384);

                result = JSONObject.parse(bin);
                if (result.isObject()) {
                    checkForError((JSONObject) result);
                }
            }
        } catch (IOException e) {
            throw new XingIOException(e);
        } catch (JSONException e) {
            if (XingServerException.isValidWithNullBody(response)) {
                // We have something from Xing, but it's an error with no reason
                throw new XingServerException(response);
            }
            // This is from Xing, and we shouldn't be getting it
            throw new XingParseException(bin);
        } catch (OutOfMemoryError e) {
            throw new XingException(e);
        } finally {
            Streams.close(bin);
        }

        final int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != XingServerException._200_OK) {
            if (statusCode == XingServerException._401_UNAUTHORIZED) {
                throw new XingUnlinkedException();
            }
            throw new XingServerException(response, result);
        }

        return result;
    }

    private static void checkForError(final JSONObject responseObject) throws XingApiException {
        if (responseObject.has("error_name")) {
            throw new XingApiException(responseObject);
        }
    }

    /**
     * Reads in content from an {@link HttpResponse} and parses it as a query string.
     * 
     * @param response the {@link HttpResponse}.
     * @return a map of parameter names to values from the query string.
     * @throws XingIOException if any network-related error occurs while reading in content from the {@link HttpResponse}.
     * @throws XingParseException if a malformed or unknown response was received from the server.
     * @throws XingException for any other unknown errors. This is also a superclass of all other Xing exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public static Map<String, String> parseAsQueryString(HttpResponse response) throws XingException {
        final HttpEntity entity = response.getEntity();
        if (entity == null) {
            throw new XingParseException("Bad response from Xing.");
        }

        final Scanner scanner;
        try {
            scanner = new Scanner(entity.getContent()).useDelimiter("&");
        } catch (IOException e) {
            throw new XingIOException(e);
        }

        Map<String, String> result = new HashMap<String, String>();
        while (scanner.hasNext()) {
            String nameValue = scanner.next();
            String[] parts = nameValue.split("=");
            if (parts.length != 2) {
                throw new XingParseException("Bad query string from Xing.");
            }
            result.put(parts[0], parts[1]);
        }
        return result;
    }

    /**
     * Executes an {@link HttpUriRequest} with the given {@link Session} and returns an {@link HttpResponse}.
     * 
     * @param session the session to use.
     * @param req the request to execute.
     * @return an {@link HttpResponse}.
     * @throws XingServerException if the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException if any network-related error occurs.
     * @throws XingUnlinkedException if the user has revoked access.
     * @throws XingException for any other unknown errors. This is also a superclass of all other Xing exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public static HttpResponse execute(Session session, HttpUriRequest req) throws XingException {
        return execute(session, req, -1);
    }

    /**
     * Executes an {@link HttpUriRequest} with the given {@link Session} and returns an {@link HttpResponse}.
     * 
     * @param session the session to use.
     * @param req the request to execute.
     * @param socketTimeoutOverrideMs if >= 0, the socket timeout to set on this request. Does nothing if set to a negative number.
     * @return an {@link HttpResponse}.
     * @throws XingServerException if the server responds with an error code. See the constants in {@link XingServerException} for the
     *             meaning of each error code.
     * @throws XingIOException if any network-related error occurs.
     * @throws XingUnlinkedException if the user has revoked access.
     * @throws XingException for any other unknown errors. This is also a superclass of all other Xing exceptions, so you may want to only
     *             catch this exception which signals that some kind of error occurred.
     */
    public static HttpResponse execute(Session session, HttpUriRequest req, int socketTimeoutOverrideMs) throws XingException {
        HttpClient client = updatedHttpClient(session);

        // Set request timeouts.
        session.setRequestTimeout(req);
        if (socketTimeoutOverrideMs >= 0) {
            HttpParams reqParams = req.getParams();
            HttpConnectionParams.setSoTimeout(reqParams, socketTimeoutOverrideMs);
        }

        boolean repeatable = isRequestRepeatable(req);

        try {
            HttpResponse response = null;
            for (int retries = 0; response == null && retries < 5; retries++) {
                /*
                 * The try/catch is a workaround for a bug in the HttpClient libraries. It should be returning null instead when an error
                 * occurs. Fixed in HttpClient 4.1, but we're stuck with this for now. See:
                 * http://code.google.com/p/android/issues/detail?id=5255
                 */
                try {
                    response = client.execute(req);
                } catch (NullPointerException e) {
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
                throw new XingIOException("Apache HTTPClient encountered an error. No response, try again.");
            }

            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != XingServerException._200_OK && statusCode != XingServerException._206_PARTIAL_CONTENT) {
                // This will throw the right thing: either a XingServerException or a XingProxyException
                parseAsJSON(response);
            }

            return response;
        } catch (SSLException e) {
            throw new XingSSLException(e);
        } catch (IOException e) {
            // Quite common for network going up & down or the request being
            // cancelled, so don't worry about logging this
            throw new XingIOException(e);
        } catch (OutOfMemoryError e) {
            throw new XingException(e);
        }
    }

    private static boolean isRequestRepeatable(HttpRequest req) {
        // If the request contains an HttpEntity that can't be "reset" (like an InputStream),
        // then it isn't repeatable.
        if (req instanceof HttpEntityEnclosingRequest) {
            HttpEntityEnclosingRequest ereq = (HttpEntityEnclosingRequest) req;
            HttpEntity entity = ereq.getEntity();
            if (entity != null && !entity.isRepeatable()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a URL for a request to the Xing API.
     * 
     * @param host the Xing host (i.e., api server, content server, or web server).
     * @param apiVersion the API version to use. You should almost always use {@code XingAPI.VERSION} for this.
     * @param target the target path, staring with a '/'.
     * @param params any URL params in an array, with the even numbered elements the parameter names and odd numbered elements the values,
     *            e.g. <code>new String[] {"path", "/Public", "locale",
     *         "en"}</code>.
     * @return a full URL for making a request.
     */
    public static String buildURL(String host, int apiVersion, String target, String[] params) {
        if (!target.startsWith("/")) {
            target = "/" + target;
        }

        try {
            // We have to encode the whole line, then remove + and / encoding
            // to get a good OAuth URL.
            target = URLEncoder.encode("/" + apiVersion + target, "UTF-8");
            target = target.replace("%2F", "/");

            if (params != null && params.length > 0) {
                target += "?" + urlencode(params);
            }

            // These substitutions must be made to keep OAuth happy.
            target = target.replace("+", "%20").replace("*", "%2A");
        } catch (UnsupportedEncodingException uce) {
            return null;
        }

        return "https://" + host + ":443" + target;
    }

    /**
     * Parses a date/time returned by the Xing API. Returns null if it cannot be parsed.
     * 
     * @param date a date returned by the API.
     * @return a {@link Date}.
     */
    public static Date parseDate(String date) {
        try {
            return dateFormat.parse(date);
        } catch (java.text.ParseException e) {
            return null;
        }
    }

    /**
     * Gets the session's client and updates its proxy.
     */
    private static synchronized HttpClient updatedHttpClient(Session session) {
        HttpClient client = session.getHttpClient();
        updateClientProxy(client, session);
        return client;
    }

    /**
     * Updates the given client's proxy from the session.
     */
    private static void updateClientProxy(HttpClient client, Session session) {
        ProxyInfo proxyInfo = session.getProxyInfo();
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
     * URL encodes an array of parameters into a query string.
     */
    private static String urlencode(String[] params) {
        if (params.length % 2 != 0) {
            throw new IllegalArgumentException("Params must have an even number of elements.");
        }

        String result = "";
        try {
            boolean firstTime = true;
            for (int i = 0; i < params.length; i += 2) {
                if (params[i + 1] != null) {
                    if (firstTime) {
                        firstTime = false;
                    } else {
                        result += "&";
                    }
                    result += URLEncoder.encode(params[i], "UTF-8") + "=" + URLEncoder.encode(params[i + 1], "UTF-8");
                }
            }
            result.replace("*", "%2A");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
        return result;
    }
}
