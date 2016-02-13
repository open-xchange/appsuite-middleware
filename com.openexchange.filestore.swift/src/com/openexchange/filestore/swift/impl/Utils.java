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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;

/**
 * {@link Utils}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
class Utils {

    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);

    /**
     * Initializes a new {@link Utils}.
     */
    private Utils() {
        super();
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
     * @param baseUrl The base URL of the end-point; e.g. <code>"https://my.clouddrive.invalid/v1/MyCloudFS_aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee"</code>
     * @param token The authentication token
     * @param httpClient The HTTP client to use
     * @return <code>true</code>/<code>false</code> if the end-point is unavailable; or <code>null</code> to ignore this check
     */
    static Boolean endpointUnavailable(String baseUrl, Token token, HttpClient httpClient) {
        if (null == token) {
            return null;
        }

        HttpGet get = null;
        HttpResponse response = null;
        try {
            get = new HttpGet(buildUri(baseUrl, toQueryString(mapFor("format", "json"))));
            get.setHeader(new BasicHeader("X-Auth-Token", token.getId()));
            response = httpClient.execute(get);
            int status = response.getStatusLine().getStatusCode();
            if (HttpServletResponse.SC_OK == status || HttpServletResponse.SC_PARTIAL_CONTENT == status) {
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
}
