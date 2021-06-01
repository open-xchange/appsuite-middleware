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

package com.openexchange.chronos.schedjoules.api.client;

import org.apache.http.HttpResponse;
import com.openexchange.chronos.schedjoules.api.auxiliary.SchedJoulesCalendarRESTResponseBodyParser;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.schedjoules.impl.SchedJoulesProperty;
import com.openexchange.exception.OXException;
import com.openexchange.rest.client.v2.RESTMimeType;
import com.openexchange.rest.client.v2.parser.AbstractRESTResponseParser;

/**
 * {@link SchedJoulesRESTResponseParser}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class SchedJoulesRESTResponseParser extends AbstractRESTResponseParser {

    private static final String REMOTE_SERVICE_NAME = "SchedJoules";

    /**
     * Initialises a new {@link SchedJoulesRESTResponseParser}.
     */
    public SchedJoulesRESTResponseParser() {
        super();
        responseBodyParsers.put(RESTMimeType.CALENDAR, new SchedJoulesCalendarRESTResponseBodyParser());
    }

    @Override
    protected String getRemoteServiceName() {
        return REMOTE_SERVICE_NAME;
    }

    @Override
    protected int assertStatusCode(HttpResponse httpResponse) throws OXException {
        int statusCode = httpResponse.getStatusLine().getStatusCode();
        // Assert the 4xx codes
        switch (statusCode) {
            case 401:
                throw SchedJoulesAPIExceptionCodes.NOT_AUTHORIZED.create(httpResponse.getStatusLine().getReasonPhrase(), SchedJoulesProperty.apiKey.getFQPropertyName());
            case 404:
                throw SchedJoulesAPIExceptionCodes.PAGE_NOT_FOUND.create();
        }
        if (statusCode >= 400 && statusCode <= 499) {
            throw SchedJoulesAPIExceptionCodes.UNEXPECTED_ERROR.create(httpResponse.getStatusLine());
        }

        // Assert the 5xx codes
        switch (statusCode) {
            case 503:
                throw SchedJoulesAPIExceptionCodes.REMOTE_SERVICE_UNAVAILABLE.create(httpResponse.getStatusLine().getReasonPhrase());
        }
        if (statusCode >= 500 && statusCode <= 599) {
            throw SchedJoulesAPIExceptionCodes.REMOTE_SERVER_ERROR.create(httpResponse.getStatusLine());
        }
        return statusCode;
    }
}
