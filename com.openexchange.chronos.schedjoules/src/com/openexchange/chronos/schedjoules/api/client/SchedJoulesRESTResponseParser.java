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

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.AbstractRESTResponseParser#getRemoveServiceName()
     */
    @Override
    protected String getRemoveServiceName() {
        return REMOTE_SERVICE_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.openexchange.rest.client.v2.parser.AbstractRESTResponseParser#assertStatusCode(org.apache.http.HttpResponse)
     */
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
            case 500:
                throw SchedJoulesAPIExceptionCodes.REMOTE_INTERNAL_SERVER_ERROR.create(httpResponse.getStatusLine().getReasonPhrase());
            case 503:
                throw SchedJoulesAPIExceptionCodes.REMOTE_SERVICE_UNAVAILABLE.create(httpResponse.getStatusLine().getReasonPhrase());
        }
        if (statusCode >= 500 && statusCode <= 599) {
            throw SchedJoulesAPIExceptionCodes.REMOTE_SERVER_ERROR.create(httpResponse.getStatusLine());
        }
        return statusCode;
    }
}
