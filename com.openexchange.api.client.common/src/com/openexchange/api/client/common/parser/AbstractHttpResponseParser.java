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

package com.openexchange.api.client.common.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.Checks;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.httpclient.util.HttpContextUtils;

/**
 * {@link AbstractHttpResponseParser} - {@link HttpResponseParser} for the response that contains {@link ResponseFields#DATA}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @param <T> The class of the response type
 * @since v7.10.5
 * @see <a href="https://documentation.open-xchange.com/components/middleware/http/latest/index.html">Documentation</a>
 */
public abstract class AbstractHttpResponseParser<T> implements HttpResponseParser<T> {

    private final boolean throwOnError;
    private final boolean handleStatusError;
    private final List<String> cookies;

    /**
     * Initializes a new {@link AbstractHttpResponseParser}.
     */
    public AbstractHttpResponseParser() {
        this(true, true);
    }

    /**
     * Initializes a new {@link AbstractHttpResponseParser}.
     * 
     * @param throwOnError <code>true</code> to throw a {@link OXException} if found in the response object, <code>false</code> to set in the response object
     * @param handleStatusError <code>true</code> to throw a {@link OXException} if the status code implies a client or server error, <code>false</code> to ignore the status code
     */
    public AbstractHttpResponseParser(boolean throwOnError, boolean handleStatusError) {
        super();
        this.throwOnError = throwOnError;
        this.handleStatusError = handleStatusError;
        this.cookies = Collections.emptyList();
    }

    /**
     * Initializes a new {@link AbstractHttpResponseParser}.
     * 
     * @param throwOnError <code>true</code> to throw a {@link OXException} if found in the response object, <code>false</code> to set in the response object
     * @param handleStatusError <code>true</code> to throw a {@link OXException} if the status code implies a client or server error, <code>false</code> to ignore the status code
     * @param cookiePrefix Prefixes of cookies that shall be checked if set
     */
    public AbstractHttpResponseParser(boolean throwOnError, boolean handleStatusError, String... cookiePrefix) {
        super();
        this.throwOnError = throwOnError;
        this.handleStatusError = handleStatusError;
        this.cookies = new ArrayList<>(cookiePrefix.length);
        for (String prefix : cookiePrefix) {
            cookies.add(prefix);
        }
    }

    @Override
    public T parse(HttpResponse response, HttpContext httpContext) throws OXException {
        CommonApiResponse commonResponse = CommonApiResponse.build(response);
        if (throwOnError && commonResponse.hasOXException()) {
            throw commonResponse.getOXException();
        }

        if (handleStatusError) {
            Checks.checkStatusError(response);
        }

        CookieStore cookieStore = HttpContextUtils.getCookieStore(httpContext);
        for (String cookiePrefix : cookies) {
            Checks.checkCookieSet(cookiePrefix, cookieStore);
        }

        try {
            return parse(commonResponse, httpContext);
        } catch (JSONException e) {
            throw ApiClientExceptions.JSON_ERROR.create(e.getMessage(), e);
        }
    }

    /**
     * Parses a HTTP response to the desired object
     *
     * @param commonResponse The HTTP response parsed to a {@link CommonApiResponse}
     * @param httpContext The HTTP context with additional information
     * @return The desired object
     * @throws OXException In case the object can't be parsed
     * @throws JSONException In case of JSON error
     */
    public abstract T parse(CommonApiResponse commonResponse, HttpContext httpContext) throws OXException, JSONException;

}
