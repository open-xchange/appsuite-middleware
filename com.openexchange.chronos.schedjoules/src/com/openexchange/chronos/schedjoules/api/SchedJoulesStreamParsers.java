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

package com.openexchange.chronos.schedjoules.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.schedjoules.api.client.SchedJoulesResponse;
import com.openexchange.chronos.schedjoules.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;

/**
 * {@link SchedJoulesStreamParsers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
final class SchedJoulesStreamParsers {

    private static final String CHARSET = "UTF-8";

    /**
     * {@link StreamParser} - Defines the types of the available stream parsers
     */
    static enum StreamParser {
        JSON,
        CALENDAR;
    }

    /**
     * A {@link Map} that holds references to all available stream parsers
     */
    private static final Map<StreamParser, SchedJoulesStreamParser<?>> streamParsers;
    static {
        streamParsers = new HashMap<>(2);

        // The JSON stream parser
        streamParsers.put(StreamParser.JSON, (response) -> {
            try (InputStream inputStream = Streams.bufferedInputStreamFor(response.getStream())) {
                return new JSONObject(Streams.stream2string(inputStream, CHARSET));
            } catch (IOException | JSONException e) {
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
            }
        });

        // The Calendar stream parser
        streamParsers.put(StreamParser.CALENDAR, (response) -> {
            ICalService iCalService = Services.getService(ICalService.class);
            ICalParameters parameters = iCalService.initParameters();
            parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.TRUE);

            try (InputStream inputStream = Streams.bufferedInputStreamFor(response.getStream())) {
                return iCalService.importICal(inputStream, parameters);
            } catch (IOException e) {
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e.getMessage());
            }
        });
    }

    /**
     * Parses the {@link InputStream} of the specified {@link SchedJoulesResponse}
     * with the specified {@link StreamParser} type.
     * 
     * @param response The {@link SchedJoulesResponse}
     * @param streamParser
     * @return
     * @throws OXException
     */
    @SuppressWarnings("unchecked")
    static <R> R parse(SchedJoulesResponse response, StreamParser streamParser) throws OXException {
        SchedJoulesStreamParser<?> parser = streamParsers.get(streamParser);
        if (parser == null) {
            throw new OXException(1138, "Uknown stream parser '" + streamParser + "'");
        }
        return (R) parser.parse(response);
    }

    /**
     * 
     * {@link SchedJoulesStreamParser}
     * 
     * @param <R> The returned type
     */
    private interface SchedJoulesStreamParser<R> {

        /**
         * Parses the {@link InputStream} from the specified {@link SchedJoulesResponse}
         * 
         * @param response The {@link SchedJoulesResponse}
         * @return The parsed {@link R} object
         * @throws OXException if a parsing error occurs
         */
        R parse(SchedJoulesResponse response) throws OXException;
    }
}
