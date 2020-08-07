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

import static com.openexchange.api.client.common.ApiClientConstants.TEXT_JAVA_SCRIPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.apache.http.entity.ContentType.TEXT_HTML;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link JSONUtils} - Utils for deserialization of HTTP responses to JSON
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public final class JSONUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(JSONUtils.class);

    private JSONUtils() {}

    /**
     * Returns a {@link JSONObject} obtained from the HTTP response
     * <p>
     * JSON will be extracted if the content type is one of
     * <li>application/json</li>
     * <li>text/javascript</li>
     * <li>text/html</li>
     * <p>
     * JSON will not be extracted from types:
     * <li>application/octet-stream</li>
     * <li>application/zip</li>
     *
     * @param response The response to get the JSON from
     * @return A {@link OXException} In case a exception is found in the response or <code>null</code>
     */
    public static JSONValue getJSON(HttpResponse response) {
        String contentType = ApiClientUtils.getHeaderValue(response, "Content-Type");
        if (Strings.isEmpty(contentType)) {
            return null;
        }
        try {
            if (contentType.indexOf(TEXT_JAVA_SCRIPT.getMimeType()) > -1 || contentType.indexOf(APPLICATION_JSON.getMimeType()) > -1) {
                return parse(response);
            }
            if (contentType.indexOf(TEXT_HTML.getMimeType()) > -1) {
                String body = getJSONFromBody(response);
                if (Strings.isNotEmpty(body)) {
                    return parse(body);
                }
            }
        } catch (OXException e) {
            LOGGER.debug("Unable to parse content", e);
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
    private static String getJSONFromBody(HttpResponse response) {
        String body = ApiClientUtils.getBody(response);
        if (Strings.isNotEmpty(body) && body.length() > 15 && body.substring(0, 14).equalsIgnoreCase("<!DOCTYPE HTML")) {
            final int pos1 = body.indexOf('{');
            final int pos2 = body.indexOf("})</script>");
            if(pos1 > -1 && pos2 > -1) {
                body = body.substring(pos1, pos2 + 1);
                return body;
            }
        }
        return null;
    }

    /**
     * Parses the response to a {@link JSONValue}
     *
     * @param response The response
     * @return A {@link JSONValue}
     * @throws OXException In case the response is not parsable
     */
    public static JSONValue parse(HttpResponse response) throws OXException {
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
    public static JSONValue parse(String body) throws OXException {
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
     * @throws OXException in case of JSON error
     */
    public static JSONArray parseDataArray(HttpResponse response) throws OXException {
        try {
            JSONObject json = parseJSONObject(response);
            return json.getJSONArray("data");
        } catch (JSONException e) {
            throw ApiClientExceptions.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
