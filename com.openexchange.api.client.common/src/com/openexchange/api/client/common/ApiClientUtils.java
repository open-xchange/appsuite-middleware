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

package com.openexchange.api.client.common;

import static com.openexchange.share.core.ShareConstants.SHARE_SERVLET;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAppender;
import com.openexchange.java.Strings;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link ApiClientUtils}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public final class ApiClientUtils {

    private static final String SESSION = "session";
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClientUtils.class);

    /**
     * Checks if the path looks like an OX share link
     *
     * @param path The path to check
     * @return <code>true</code> if an OX instance generated the string, <code>false</code> otherwise
     */
    public static boolean isShare(String path) {
        return null != getShareToken(path);
    }

    /**
     * Parses the given path for a base token.
     *
     * @param path The path to get the base token from
     * @return The token or <code>null</code> if not applicable
     */
    public static String getBaseToken(String path) {
        ShareToken shareToken = getShareToken(path);
        if (null == shareToken) {
            return null;
        }
        return shareToken.getToken();
    }

    /**
     * Get a share token from a path
     *
     * @param path The path the share token is in
     * @return The token or <code>null</code> if not applicable
     */
    private static ShareToken getShareToken(String path) {
        String token = extractBaseToken(path);
        if (null == token) {
            return null;
        }
        try {
            ShareToken shareToken = new ShareToken(token);
            if (shareToken.getContextID() < 0 || shareToken.getUserID() < 0) {
                /*
                 * No context ID mean no OX share
                 */
                return null;
            }
            return shareToken;
        } catch (OXException e) {
            LOGGER.debug("Error while parsing: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Extracts the share base token from a path.
     *
     * @param path The path to extract the token from
     * @return The token or <code>null</code> if no token is embedded in the path
     */
    private static String extractBaseToken(String path) {
        if (Strings.isEmpty(path)) {
            return null;
        }
        String prefix = SHARE_SERVLET + '/';
        int beginIndex = path.lastIndexOf(prefix);
        if (-1 == beginIndex) {
            return null;
        }
        beginIndex += prefix.length();
        int endIndex = path.indexOf('/', beginIndex);
        return -1 == endIndex ? path.substring(beginIndex) : path.substring(beginIndex, endIndex);
    }

    /**
     * Get the identifier that is used for shares
     *
     * @return The share identifier
     */
    public static String getSharePrefix() {
        return SHARE_SERVLET;
    }

    /**
     * Check that the request targets the same origin as the given URL
     *
     * @param request The HTTP request
     * @param target The desired target
     * @throws OXException In case the domain doesn't match
     */
    public static void checkSameOrigin(HttpRequestBase request, URL target) throws OXException {
        if (null == request.getURI() || false == request.getURI().getHost().equals(target.getHost())) {
            throw ApiClientExceptions.NOT_SAME_ORIGIN.create(request.getURI(), target.getHost());
        }
    }

    /**
     * Checks that the redirect target is still on the same server
     *
     * @param originHost The original targeted host
     * @param redirectTarget The redirect to follow
     * @throws OXException In case the new target is invalid or redirects to another host
     */
    public static void checkSameOrigin(URL originHost, String redirectTarget) throws OXException {
        try {
            //relative or absolute redirect?
            URL redirect = redirectTarget.startsWith("/") ? new URL(originHost, redirectTarget) : new URL(redirectTarget);
            if (null != redirect.getHost()) {
                if (false == originHost.getHost().equals(redirect.getHost())) {
                    throw ApiClientExceptions.NOT_SAME_ORIGIN.create(redirect.getHost(), originHost.getHost());
                }
            }
        } catch (MalformedURLException e) {
            throw ApiClientExceptions.INVALID_TARGET.create(e, redirectTarget);
        }
    }

    /**
     * Checks that the JSESSION cookie is set
     *
     * @param cookieStore The cookie store to get the cookies from
     * @param target The target URL for logging
     * @throws OXException In case the cookie is missing
     */
    public static void checkJSESSIONCookie(CookieStore cookieStore, URL target) throws OXException {
        checkCookieSet(Tools.JSESSIONID_COOKIE, cookieStore, target);
    }

    /**
     * Checks that the secret cookie is set
     *
     * @param cookieStore The cookie store to get the cookies from
     * @param target The target URL for logging
     * @throws OXException In case the cookie is missing
     */
    public static void checkSecretCookie(CookieStore cookieStore, URL target) throws OXException {
        checkCookieSet(LoginServlet.SECRET_PREFIX, cookieStore, target);
    }

    /**
     * Checks that the public session cookie is set
     *
     * @param cookieStore The cookie store to get the cookies from
     * @param target The target URL for logging
     * @throws OXException In case the cookie is missing
     */
    public static void checkPublicSessionCookie(CookieStore cookieStore, URL target) throws OXException {
        checkCookieSet(LoginServlet.PUBLIC_SESSION_PREFIX, cookieStore, target);
    }

    /**
     * Checks that a certain cookie with a dedicated name is set
     *
     * @param cookiePrefix The name prefix of the cookie, e.g. {@value LoginServlet#SECRET_PREFIX}
     * @param cookieStore The cookie store to get the cookies from
     * @param target The target URL for logging
     * @throws OXException In case the cookie is missing
     */
    public static void checkCookieSet(String cookiePrefix, CookieStore cookieStore, URL target) throws OXException {
        List<Cookie> cookies = cookieStore.getCookies();
        if (null == cookies || cookies.isEmpty()) {
            throw ApiClientExceptions.MISSING_COOKIE.create(target);
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().startsWith(cookiePrefix)) {
                return;
            }
        }
        throw ApiClientExceptions.MISSING_COOKIE.create(target);
    }

    /**
     * Gets the session cookie from the cookie store
     *
     * @param cookieStore The cookie store to search in
     * @return The optional session cookie
     */
    public static Optional<Cookie> getSessionCookie(CookieStore cookieStore) {
        return getCookie(LoginServlet.SESSION_PREFIX, cookieStore);
    }

    /**
     * Compares cookies by the given prefix an returns the matching one
     *
     * @param cookiePrefix The name prefix of the cookie, e.g. {@value LoginServlet#SECRET_PREFIX}
     * @param cookieStore The cookie store to get the cookies from
     * @return The cookie with the same prefix
     */
    public static Optional<Cookie> getCookie(String cookiePrefix, CookieStore cookieStore) {
        if (null == cookieStore || Strings.isEmpty(cookiePrefix)) {
            return Optional.empty();
        }
        return cookieStore.getCookies().stream().filter(c -> c.getName().startsWith(cookiePrefix)).findFirst();
    }

    /**
     * Gets the header value for the given header name
     *
     * @param response The response to get the header from
     * @param headerName The header name to get the value from
     * @return <code>null</code> if either none or more than one header for the name is found,
     *         or the value of the header
     */
    public static String getHeaderValue(HttpResponse response, String headerName) {
        Header[] headers = response.getHeaders(headerName);
        if (null == headers || headers.length != 1) {
            return null;
        }
        return headers[0].getValue();
    }

    /**
     * Parses the path of the login link for the dispatcher prefix the remote
     * OX server uses that is set before each request.
     * <p>
     * E.g. the login link path looks like
     * <code>/ajax/share/04b29644047397444b29645473974b7ba433980a88b9aacd/1/8/Mzk</code>
     * the prefix is <code>/ajax</code>
     * <p>
     * E.g. the login link path looks like
     * <code>/appsuite/api/share/04b29644047397444b29645473974b7ba433980a88b9aacd/1/8/Mzk</code>
     * the prefix is <code>/appsuite/api</code>
     *
     *
     * @param loginLinkPath The login link to access
     * @return The prefix
     * @throws OXException In case exceptions of the path aren't met
     * @see com.openexchange.share.core.tools.ShareLinks.serverPath(HostData, String) on how the link is build
     */
    public static String parseDispatcherPrefix(String loginLinkPath) throws OXException {
        String sharePrefix = ApiClientUtils.getSharePrefix();
        if (false == loginLinkPath.contains(sharePrefix)) {
            throw ApiClientExceptions.INVALID_TARGET.create(loginLinkPath);
        }

        String prefix = loginLinkPath; // "/ajax/share/0..."
        prefix = prefix.substring(0, loginLinkPath.indexOf(sharePrefix)); // "/ajax/"
        if (prefix.endsWith("/")) {
            prefix = prefix.substring(0, prefix.length() - 1); // "/ajax"
        }
        if (false == prefix.startsWith("/")) {
            prefix = "/" + prefix; // "/ajax"
        }
        return prefix;
    }

    /**
     * Parsers the URL and filters for the parameters
     *
     * @param url The URL to parse
     * @return A map containing the name value pairs
     */
    public static Map<String, String> parseParameters(String url) {
        List<NameValuePair> list = URLEncodedUtils.parse(url, Charset.forName("UTF-8"));
        Map<String, String> map = new HashMap<String, String>(list.size());
        for (NameValuePair pair : list) {
            map.put(pair.getName(), pair.getValue() != null ? pair.getValue() : "");
        }
        return map;
    }

    /**
     * Parses the given string for the <code>session</code> parameter
     *
     * @param path The path to parse, e.g. <code>/appsuite/ui#!&session=adb9ac4166ca4ebc82b434d257140cc1&user=&user_id=15&context_id=1&m=infostore&f=39</code>
     * @return The session ID or <code>null</code>
     */
    public static String parseForSession(String path) {
        if (Strings.isEmpty(path) || false == path.contains(SESSION)) {
            return null;
        }
        for (Entry<String, String> entry : parseParameters(path).entrySet()) {
            if (SESSION.equals(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Parses a object to an int
     *
     * @param object The object to parse
     * @return The value as int or <code>-1</code> if it is considered empty
     */
    public static int parseInt(Object object) {
        if (null == object) {
            return -1;
        }
        if (object instanceof String) {
            String value = (String) object;
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                LOGGER.trace("{} is not a number", value, e);
            }
        } else if (object instanceof Integer) {
            return ((Integer) object).intValue();
        }

        return -1;
    }

    /**
     * Parses a object as String.
     *
     * @param object The object to parse
     * @return The object as string if set or <code>null</code> if it is considered empty
     *         as per {@link Strings#isEmpty(String)}
     */
    public static String parseString(Object object) {
        if (null == object) {
            return null;
        }
        String value;
        if (object instanceof String) {
            value = (String) object;
        } else {
            value = String.valueOf(object);
        }

        if (Strings.isEmpty(value)) {
            return null;
        }
        return value;
    }

    /**
     * Parses the response to a {@link JSONObject}
     *
     * @param response The response
     * @return A {@link JSONObject}
     * @throws OXException In case the response is not parsable
     */
    public static JSONObject parseJSONObject(HttpResponse response) throws OXException {
        return toJSONObject(parse(response));
    }

    /**
     * Parses the response to a {@link JSONObject}
     *
     * @param body The response body
     * @return A {@link JSONObject}
     * @throws OXException In case the response is not parsable
     */
    public static JSONObject parseJSONObject(String body) throws OXException {
        return toJSONObject(parse(body));
    }

    private static JSONObject toJSONObject(JSONValue jsonValue) throws OXException {
        if (null != jsonValue && jsonValue instanceof JSONObject) {
            return (JSONObject) jsonValue;
        }
        throw ApiClientExceptions.JSON_ERROR.create("Response not parsable");
    }

    /**
     * Parses the "data" field as array from the given JSON response body
     *
     * @param response The response containing the JSON body
     * @return The data field parsed from the given response
     * @throws OXException
     */
    public static JSONArray parseDataArray(HttpResponse response) throws OXException {
        try {
            JSONObject json = parseJSONObject(response);
            return json.getJSONArray("data");
        } catch (JSONException e) {
            throw ApiClientExceptions.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses the "data" field as JSOObject from the given JSON response body
     *
     * @param response The response containing the JSON body
     * @return The data field parsed from the given response
     * @throws OXException
     */
    public static JSONObject parseDataObject(HttpResponse response) throws OXException {
        try {
            JSONObject json = parseJSONObject(response);
            return json.getJSONObject("data");
        } catch (JSONException e) {
            throw ApiClientExceptions.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses the response to a {@link JSONArray}
     *
     * @param response The response
     * @return A {@link JSONArray}
     * @throws OXException In case the response is not parsable
     */
    public static JSONArray parseJSONArray(HttpResponse response) throws OXException {
        return toJSONArray(parse(response));
    }

    /**
     * Parses the response to a {@link JSONArray}
     *
     * @param body The response body
     * @return A {@link JSONArray}
     * @throws OXException In case the response is not parsable
     */
    public static JSONArray parseJSONArray(String body) throws OXException {
        return toJSONArray(parse(body));
    }

    private static JSONArray toJSONArray(JSONValue jsonValue) throws OXException {
        if (null != jsonValue && jsonValue instanceof JSONArray) {
            return (JSONArray) jsonValue;
        }
        throw ApiClientExceptions.JSON_ERROR.create("Response not parsable");
    }

    /**
     * Internal method to create a comma separated string from the given list
     *
     * @param items The items to create a comma separated list from
     * @return A comma separated list built from the given items
     */
    public static String toCommaString(String... items) {
        StringAppender appender = new StringAppender(",");
        for (String s : items) {
            appender.append(s);
        }
        return appender.toString();
    }

    /**
     * Internal method to create a comma separated string from the given list
     *
     * @param items The items to create a comma separated list from
     * @return A comma separated list built from the given items
     */
    public static String toCommaString(int... items) {
        StringAppender appender = new StringAppender(",");
        for (int i : items) {
            appender.append(i);
        }
        return appender.toString();
    }

    /**
     * Parses the response to a {@link JSONValue}
     *
     * @param response The response
     * @return A {@link JSONValue}
     * @throws OXException In case the response is not parsable
     */
    private static JSONValue parse(HttpResponse response) throws OXException {
        if (null == response.getEntity()) {
            throw ApiClientExceptions.UNEXPECTED_ERROR.create("Response is not set!");
        }
        try {
            return parse(response.getEntity().getContent());
        } catch (IOException e) {
            throw ApiClientExceptions.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Parses the response to a {@link JSONValue}
     *
     * @param body The response body
     * @return A {@link JSONValue}
     * @throws OXException In case the response is not parsable
     */
    private static JSONValue parse(String body) throws OXException {
        if (Strings.isEmpty(body)) {
            return null;
        }
        InputStream in = null;
        try {
            in = new ByteArrayInputStream(body.getBytes());
            return parse(in);
        } finally {
            Streams.close(in);
        }
    }

    /**
     * Parses the response to a {@link JSONValue}
     *
     * @param inputStream The input Stream to parse
     * @return A {@link JSONValue}
     * @throws OXException In case the response is not parsable
     */
    private static JSONValue parse(InputStream inputStream) throws OXException {
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            return JSONObject.parse(reader);
        } catch (JSONException e) {
            throw ApiClientExceptions.JSON_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(reader);
        }
    }

    /**
     * Receives the response and returns it as {@link String}
     * 
     * @param response The response
     * @return The response body
     */
    public static String getBody(HttpResponse response) {
        InputStreamReader reader = null;
        BufferedReader bufferedReader = null;
        try {
            HttpEntity entity = response.getEntity();
            reader = new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(reader);
            return bufferedReader.lines().collect(Collectors.joining("\n"));
        } catch (UnsupportedOperationException | IOException e) {
            LOGGER.trace("Unable to parse content", e);
        } finally {
            Streams.close(reader, bufferedReader);
        }
        return null;
    }

    /**
     * Tries to extract JSON from a HTML callback. Body must begin with
     * <code><!DOCTYPE HTML</code>
     *
     * @param response The response to get the JSON from
     * @return The JSON as {@link String} or <code>null</code>
     */
    public static String getJSONFromBody(HttpResponse response) {
        String body = getBody(response);
        if (Strings.isNotEmpty(body) && body.length() > 15 && body.substring(0, 14).equalsIgnoreCase("<!DOCTYPE HTML")) {
            final int pos1 = body.indexOf('{');
            final int pos2 = body.indexOf("})</script>");
            body = body.substring(pos1, pos2 + 1);
            return body;
        }
        return null;
    }

}
