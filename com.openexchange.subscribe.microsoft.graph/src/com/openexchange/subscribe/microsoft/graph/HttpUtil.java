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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.subscribe.microsoft.graph;

import java.io.IOException;
import java.io.InputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.subscribe.SubscriptionErrorMessage;

/**
 * {@link HttpUtil}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class HttpUtil {

    private static final Logger LOG = LoggerFactory.getLogger(HttpUtil.class);
    private static final String CHARSET = "UTF-8";

    /**
     * @param content
     * @return
     * @throws OXException
     */
    public static JSONValue parseStream(InputStream content) throws OXException {
        try (InputStream inputStream = Streams.bufferedInputStreamFor(content)) {
            String string = Streams.stream2string(inputStream, CHARSET);
            char c = string.charAt(0);
            switch (c) {
                case '{':
                    return new JSONObject(string);
                case '[':
                    return new JSONArray(string);
                default:
                    throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create("Unexpected start token detected '" + c + "'");
            }
        } catch (IOException e) {
            throw SubscriptionErrorMessage.IO_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw SubscriptionErrorMessage.ParseException.create(e, e.getMessage());
        }
    }

    /**
     * Asserts the status code for any errors
     *
     * @param httpResponse The {@link HttpResponse}'s status code to assert
     * @return The status code
     * @throws OXException if an HTTP error is occurred (4xx or 5xx)
     * @throws IOException
     * @throws UnsupportedOperationException
     */
    public static int assertStatusCode(HttpResponse httpResponse) throws OXException, UnsupportedOperationException, IOException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        if (statusCode == 200) {
            return statusCode;
        }
        // Assert the 4xx codes
        switch (statusCode) {
            case 401:
                throw SubscriptionErrorMessage.PERMISSION_DENIED.create(httpResponse.getStatusLine().getReasonPhrase());
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw SubscriptionErrorMessage.UNEXPECTED_ERROR.create(httpResponse.getStatusLine());
        }

        // Assert the 5xx codes
        if (statusCode >= 500 && statusCode <= 599) {
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(httpResponse.getStatusLine());
        }
        return statusCode;
    }

    /**
     * Resets the specified {@link HttpRequestBase}
     *
     * @param httpRequest The {@link HttpRequestBase} to reset
     */
    public static void reset(HttpRequestBase httpRequest) {
        if (httpRequest == null) {
            return;
        }
        try {
            httpRequest.reset();
        } catch (final Throwable e) {
            LOG.debug("Error while resetting the HTTP request {}", e.getMessage(), e);
        }
    }

    /**
     * Consumes the specified {@link HttpResponse}
     *
     * @param response the {@link HttpResponse} to consume
     */
    public static void consume(HttpResponse response) {
        if (null == response) {
            return;
        }
        HttpEntity entity = response.getEntity();
        if (null == entity) {
            return;
        }
        try {
            EntityUtils.consume(entity);
        } catch (Throwable e) {
            LOG.debug("Error while consuming the entity of the HTTP response {}", e.getMessage(), e);
        }
    }
}
