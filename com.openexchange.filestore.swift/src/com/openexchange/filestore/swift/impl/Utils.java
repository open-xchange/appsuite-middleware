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

package com.openexchange.filestore.swift.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.filestore.swift.impl.token.Token;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    /** The delimiter character to separate the prefix from the keys */
    private static final char DELIMITER = SwiftClient.DELIMITER;

    /**
     * Initializes a new {@link Utils}.
     */
    private Utils() {
        super();
    }

    /**
     * Creates a new arbitrary key (an unformatted string representation of a new random UUID), optionally prepended with the configured
     * prefix and delimiter.
     *
     * @param prefix The prefix to use; e.g. <code>"57462ctxstore"</code>
     * @return A new UID string, optionally with prefix and delimiter, e.g. <code>[prefix]/067e61623b6f4ae2a1712470b63dff00</code>.
     */
    static String generateKey(String prefix) {
        String uuid = UUIDs.getUnformattedString(UUID.randomUUID());
        return new StringBuilder(prefix.length() + 33).append(prefix).append(DELIMITER).append(uuid).toString();
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied name.
     *
     * @param prefix The prefix to use; e.g. <code>"57462ctxstore"</code>
     * @param name The name to prepend the prefix
     * @return The name with prefix
     */
    static String addPrefix(String prefix, UUID name) {
        return new StringBuilder(prefix.length() + 33).append(prefix).append(DELIMITER).append(UUIDs.getUnformattedString(name)).toString();
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied name.
     *
     * @param prefix The prefix to use; e.g. <code>"57462ctxstore"</code>
     * @param name The name to prepend the prefix
     * @return The name with prefix
     */
    static String addPrefix(String prefix, String name) {
        return new StringBuilder(prefix.length() + name.length() + 1).append(prefix).append(DELIMITER).append(name).toString();
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied names.
     *
     * @param prefix The prefix to use; e.g. <code>"57462ctxstore"</code>
     * @param names The names to prepend the prefix
     * @return The names with prefix in an array
     */
    static String[] addPrefix(String prefix, Collection<? extends String> names) {
        String[] keys = new String[names.size()];
        int i = 0;
        for (String name : names) {
            keys[i++] = addPrefix(prefix, name);
        }
        return keys;
    }

    /**
     * Prepends the configured prefix and delimiter character sequence to the supplied names.
     *
     * @param prefix The prefix to use; e.g. <code>"57462ctxstore"</code>
     * @param names The names to prepend the prefix
     * @return The names with prefix in an array
     */
    static String[] addPrefix(String prefix, String[] names) {
        String[] keys = new String[names.length];
        for (int i = 0; i < names.length; i++) {
            keys[i] = addPrefix(prefix, names[i]);
        }
        return keys;
    }

    /**
     * Strips the prefix and delimiter character sequence to the supplied key.
     *
     * @param prefix The prefix to use; e.g. <code>"57462ctxstore"</code>
     * @param key The key to strip the prefix from
     * @return The key without prefix
     */
    static String removePrefix(String prefix, String key) {
        int idx = prefix.length() + 1;
        if (idx > key.length() || false == key.startsWith(new StringBuilder(prefix.length() + 1).append(prefix).append(DELIMITER).toString(), 0)) {
            throw new IllegalArgumentException(key);
        }
        return key.substring(idx);
    }

    /**
     * Turns specified JSON value into an appropriate HTTP entity.
     *
     * @param jValue The JSON value
     * @return The HTTP entity
     * @throws JSONException If a JSON error occurs
     * @throws IOException If an I/O error occurs
     */
    static InputStreamEntity asHttpEntity(JSONValue jValue) throws JSONException, IOException {
        if (null == jValue) {
            return null;
        }

        ByteArrayOutputStream bStream = Streams.newByteArrayOutputStream(1024);
        OutputStreamWriter osw = new OutputStreamWriter(bStream, Charsets.UTF_8);
        jValue.write(osw);
        osw.flush();
        return new InputStreamEntity(Streams.asInputStream(bStream), bStream.size(), ContentType.APPLICATION_JSON);
    }

    /**
     * Gets a (parameters) map for specified arguments.
     *
     * @param args The arguments
     * @return The resulting map
     */
    static Map<String, String> mapFor(String... args) {
        if (null == args) {
            return null;
        }

        int length = args.length;
        if (0 == length || (length % 2) != 0) {
            return null;
        }

        Map<String, String> map = new LinkedHashMap<String, String>(length >> 1);
        for (int i = 0; i < length; i+=2) {
            map.put(args[i], args[i+1]);
        }
        return map;
    }

    /**
     * Gets the appropriate query string for given parameters
     *
     * @param parameters The parameters
     * @return The query string
     */
    static List<NameValuePair> toQueryString(Map<String, String> parameters) {
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
     * Builds the URI from given arguments
     *
     * @param The URL w/o query string
     * @param queryString The query string parameters
     * @return The built URI string
     * @throws IllegalArgumentException If the given string violates RFC 2396
     */
    static URI buildUri(String url, List<NameValuePair> queryString) {
        try {
            URI uri = new URI(url);
            URIBuilder builder = new URIBuilder();
            builder.setScheme(uri.getScheme()).
                    setHost(uri.getHost()).
                    setPort(uri.getPort()).
                    setPath(uri.getPath()).
                    setQuery(null == queryString ? null : URLEncodedUtils.format(queryString, "UTF-8"));
            return builder.build();
        } catch (URISyntaxException x) {
            throw new IllegalArgumentException("Failed to build URI", x);
        }
    }

    /**
     * Closes the supplied HTTP request & response resources silently.
     *
     * @param request The HTTP request to reset
     * @param response The HTTP response to consume and close
     */
    static void close(HttpRequestBase request, HttpResponse response) {
        if (null != response) {
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                try {
                    EntityUtils.consume(entity);
                } catch (Exception e) {
                    LOG.debug("Error consuming HTTP response entity", e);
                }
            }
        }

        if (null != request) {
            try {
                request.reset();
            } catch (Exception e) {
                LOG.debug("Error resetting HTTP request", e);
            }
        }
    }

    /**
     * Checks if a Swift end-point is currently unavailable. Therefore the account details are requested.
     * <p>
     * If the account details can be retrieved (server responds with status <code>200</code> or <code>206</code>) the end-point is
     * considered available; otherwise it's not.
     *
     * @param endpoint The end-point to check; e.g. <code>"https://swift.store.invalid/v1/CloudFS_123456/MyContainer"</code>
     * @param token The authentication token
     * @param httpClient The HTTP client to use
     * @return <code>true</code>/<code>false</code> if the end-point is unavailable; or <code>null</code> to ignore this check
     */
    static Boolean endpointUnavailable(Endpoint endpoint, HttpClient httpClient) {
        Token token = endpoint.getToken();
        if (null == token) {
            return null;
        }

        HttpGet get = null;
        HttpResponse response = null;
        try {
            get = new HttpGet(buildUri(endpoint.getContainerUri(), toQueryString(mapFor("format", "json", "limit", "1"))));
            get.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            response = httpClient.execute(get);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_NO_CONTENT == status) {
                return Boolean.FALSE;
            }
            if (HttpServletResponse.SC_UNAUTHORIZED == status) {
                // Token expired intermittently
                return null;
            }
        } catch (IOException e) {
            // ignore
        } finally {
            Utils.close(get, response);
        }

        return Boolean.TRUE;
    }

    /**
     * Gets the MD5 sum for specified string.
     *
     * @param string The string
     * @return The MD5 sum
     */
    public static String getMD5Sum(String string) {
        if (null == string) {
            return string;
        }

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] digest = md5.digest(string.getBytes(Charsets.UTF_8));
            return asHex(digest);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Turns array of bytes into string representing each byte as unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    private static String asHex(final byte[] hash) {
        final int length = hash.length;
        final char[] buf = new char[length * 2];
        for (int i = 0, x = 0; i < length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

}
