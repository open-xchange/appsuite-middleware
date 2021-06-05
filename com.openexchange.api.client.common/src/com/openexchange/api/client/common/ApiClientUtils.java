/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.api.client.common;

import static com.openexchange.share.core.ShareConstants.SHARE_SERVLET;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.ajax.LoginServlet;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAppender;
import com.openexchange.java.Strings;

/**
 * {@link ApiClientUtils} - Util class with functions for serialization and deserialization
 * of HTTP requests and responses
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public final class ApiClientUtils {

    private static final String SESSION = "session";
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiClientUtils.class);

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
     * See also <code>com.openexchange.share.core.tools.ShareLinks.serverPath(HostData, String)</code> on how the link is build
     *
     * @param loginLinkPath The login link to access
     * @return The prefix
     */
    public static String parseDispatcherPrefix(String loginLinkPath) {
        String sharePrefix = SHARE_SERVLET;
        if (false == loginLinkPath.contains(sharePrefix)) {
            /*
             * Return as-is
             */
            return loginLinkPath;
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
        return value;
    }

    /**
     * Internal method to create a comma separated string from the given list
     *
     * @param items The items to create a comma separated list from
     * @return A comma separated list built from the given items or <code>null</code> if items was empty
     */
    public static String toCommaString(String... items) {
        if (null == items || items.length == 0) {
            return null;
        }
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
     * @return A comma separated list built from the given items or <code>null</code> if items was empty
     */
    public static String toCommaString(int... items) {
        if (null == items || items.length == 0) {
            return null;
        }
        StringAppender appender = new StringAppender(",");
        for (int i : items) {
            appender.append(i);
        }
        return appender.toString();
    }

    /**
     * Internal method to create a comma separated string from the given list
     *
     * @param items The items to create a comma separated list from
     * @return A comma separated list built from the given items
     */
    public static String toCommaString(Object... items) {
        if (null == items || items.length == 0) {
            return null;
        }
        StringAppender appender = new StringAppender(",");
        for (Object i : items) {
            appender.append(i);
        }
        return appender.toString();
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
     * Creates a common multipart/form-data body
     *
     * @param json The JSON part of the body, or null to omit the JSON part
     * @param data The file/data part of the body, or null to omit the data part
     * @param filename The name of of the file
     * @param contentType The content type of the file
     * @return A multipart/form-data body as {@link HttpEntity}
     */
    public static HttpEntity createMultipartBody(JSONObject json, InputStream data, String filename, String contentType) {
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        if (json != null) {
            builder.addTextBody("json", json.toString(), ContentType.APPLICATION_JSON);
        }
        if (data != null) {
            builder.addBinaryBody("file", data, ContentType.create(contentType), filename);
        }
        return builder.build();
    }

    /**
     * Creates a JSON body
     *
     * @param json The JSON to use
     * @return The JSON body
     */
    public static HttpEntity createJsonBody(JSONValue json) {
        return new StringEntity(json.toString(), ContentType.APPLICATION_JSON);
    }
}
