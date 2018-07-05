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
 *     Copyright (C) 2017-2020 OX Software GmbH
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

package com.openexchange.chronos.schedjoules.api.auxiliary;

import java.io.IOException;
import java.io.InputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Calendar;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesResponse;
import com.openexchange.chronos.schedjoules.exception.SchedJoulesAPIExceptionCodes;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link SchedJoulesResponseParser} - Defines the types of the available stream parsers and their implementations
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public enum SchedJoulesResponseParser {
    /**
     * The JSON parser
     */
    JSON("application/json") {

        @Override
        public Object parseResponse(SchedJoulesResponse response) throws OXException {
            try (InputStream inputStream = Streams.bufferedInputStreamFor(response.getStream())) {
                String string = Streams.stream2string(inputStream, CHARSET);
                char c = string.charAt(0);
                switch (c) {
                    case '{':
                        return new JSONObject(string);
                    case '[':
                        return new JSONArray(string);
                    default:
                        throw SchedJoulesAPIExceptionCodes.JSON_ERROR.create("Unexpected start token detected '" + c + "'");
                }
            } catch (IOException e) {
                throw SchedJoulesAPIExceptionCodes.IO_ERROR.create(e, e.getMessage());
            } catch (JSONException e) {
                throw SchedJoulesAPIExceptionCodes.JSON_ERROR.create(e, e.getMessage());
            }
        }
    },
    /**
     * The iCal parser
     */
    CALENDAR("text/calendar") {

        @Override
        public Object parseResponse(SchedJoulesResponse response) throws OXException {
            ICalService iCalService = Services.getService(ICalService.class);
            ICalParameters parameters = iCalService.initParameters();
            parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.TRUE);

            try (InputStream inputStream = Streams.bufferedInputStreamFor(response.getStream())) {
                Calendar calendar = iCalService.importICal(inputStream, parameters);
                return new SchedJoulesCalendar(calendar.getName(), calendar.getEvents(), response.getETag(), response.getLastModified());
            } catch (IOException e) {
                throw SchedJoulesAPIExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }
    };

    private static final String CHARSET = "UTF-8";

    private final String contentType;

    /**
     * Initialises a new {@link SchedJoulesResponseParser}.
     *
     * @param contentType The content type of the stream parser
     */
    private SchedJoulesResponseParser(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Gets the contentType
     *
     * @return The contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Parses the {@link InputStream} from the specified {@link SchedJoulesResponse}
     *
     * @param response The {@link SchedJoulesResponse}
     * @return The parsed {@link R} object
     * @throws OXException if a parsing error occurs
     */
    public static Object parse(SchedJoulesResponse response) throws OXException {
        String contentType = response.getContentType();
        if (Strings.isEmpty(contentType)) {
            throw new IllegalArgumentException("The content type can be neither 'null' nor empty");
        }
        for (SchedJoulesResponseParser streamParser : SchedJoulesResponseParser.values()) {
            if (streamParser.getContentType().equals(contentType)) {
                return streamParser.parseResponse(response);
            }
        }
        throw SchedJoulesAPIExceptionCodes.NO_STREAM_PARSER.create(contentType);
    }

    /**
     * Parses the {@link InputStream} from the specified {@link SchedJoulesResponse}
     *
     * @param response The {@link SchedJoulesResponse}
     * @return The parsed {@link R} object
     * @throws OXException if a parsing error occurs
     */
    public abstract Object parseResponse(SchedJoulesResponse response) throws OXException;
}
