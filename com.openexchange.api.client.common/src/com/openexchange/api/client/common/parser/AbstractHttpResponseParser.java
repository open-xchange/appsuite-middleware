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

package com.openexchange.api.client.common.parser;

import org.apache.http.HttpResponse;
import org.apache.http.protocol.HttpContext;
import org.json.JSONException;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.api.client.ApiClientExceptions;
import com.openexchange.api.client.HttpResponseParser;
import com.openexchange.api.client.common.Checks;
import com.openexchange.exception.OXException;

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
    }

    @Override
    public T parse(HttpResponse response, HttpContext httpContext) throws OXException {
        if (handleStatusError) {
            Checks.checkStatusError(response);
        }
        CommonApiResponse commonResponse = CommonApiResponse.build(response);
        if (throwOnError && commonResponse.hasOXException()) {
            throw commonResponse.getOXException();
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
