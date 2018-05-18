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

package com.openexchange.groupware.datahandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DefaultConverter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.ResultConverterRegistry;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.conversion.ConversionResult;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.DataProperties;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ICalJSONDataHandler} - The data handler return JSON appointments and JSON tasks parsed from an ICal input stream.
 * <p>
 * Supported arguments:
 * <ul>
 * <li>com.openexchange.groupware.calendar.timezone<br>
 * (optional; default is session user's time zone)</li>
 * <li>com.openexchange.groupware.calendar.recurrencePosition<br>
 * (optional; default is zero)</li>
 * </ul>
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ICalJSONDataHandler extends ICalDataHandler {

    private static final Class<?>[] TYPES = { InputStream.class };

    /**
     * Initializes a new {@link ICalJSONDataHandler}
     *
     * @param services The {@link ServiceLookup}
     */
    public ICalJSONDataHandler(ServiceLookup services) {
        super(services);
    }

    @Override
    public String[] getRequiredArguments() {
        return new String[0];
    }

    @Override
    public Class<?>[] getTypes() {
        return TYPES;
    }

    @Override
    public ConversionResult processData(final Data<? extends Object> data, final DataArguments dataArguments, final Session session) throws OXException {
        if (null == session) {
            throw DataExceptionCodes.MISSING_ARGUMENT.create("session");
        }

        final Context ctx = ContextStorage.getStorageContext(session);
        final ICalParser iCalParser = services.getServiceSafe(ICalParser.class);
        final List<Event> events;
        final List<Task> tasks;
        final InputStreamCopy inputStreamCopy;
        {
            long size;
            try {
                size = Long.parseLong(data.getDataProperties().get(DataProperties.PROPERTY_SIZE));
            } catch (final NumberFormatException e) {
                size = 0;
            }
            inputStreamCopy = copyStream((InputStream) data.getData(), size);
        }
        /*
         * Get time zone
         */
        final TimeZone timeZone;
        {
            final String timeZoneId = dataArguments.get("com.openexchange.groupware.calendar.timezone");
            timeZone = TimeZoneUtils.getTimeZone(null == timeZoneId ? UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone() : timeZoneId);
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        try {
            /*
             * Start parsing appointments
             */
            InputStream stream = null;
            try {
                ICalService iCalService = services.getServiceSafe(ICalService.class);
                stream = inputStreamCopy.getInputStream();
                ImportedCalendar calendar = iCalService.importICal(stream, null);
                events = calendar.getEvents();
            } finally {
                safeClose(stream);
            }
            /*
             * Errors and warnings
             */
            final List<ConversionError> conversionErrors = new ArrayList<ConversionError>(4);
            final List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>(4);
            /*
             * Start parsing tasks
             */
            tasks = parseTaskStream(ctx, iCalParser, inputStreamCopy, conversionErrors, conversionWarnings, timeZone);
            // TODO: Handle errors/warnings
        } catch (final IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            safeClose(inputStreamCopy);
        }
        /*
         * The JSON array to return
         */
        final JSONArray objects = new JSONArray();
        ResultConverterRegistry converterRegistry = services.getServiceSafe(ResultConverterRegistry.class);
        AJAXRequestData request = new AJAXRequestData();
        request.setSession(serverSession);
        /*
         * Fill JSON appointments
         */
        if (!events.isEmpty()) {
            /*
             * Insert parsed appointments into denoted calendar folder
             */
            ResultConverter resultConverter = converterRegistry.getResultConverter("eventDocument", "json");
            if (null != resultConverter) {
                for (Event event : events) {
                    AJAXRequestResult result = new AJAXRequestResult(event);
                    resultConverter.convert(request, result, serverSession, new DefaultConverter());
                    Object resultObject = result.getResultObject();
                    objects.put(resultObject);
                }
            }
        }
        /*
         * Fill JSON tasks
         */
        if (!tasks.isEmpty()) {
            /*
             * Insert parsed tasks into denoted task folder
             */
            ResultConverter resultConverter = converterRegistry.getResultConverter("task", "json");
            if (null != resultConverter) {
                for (final Task task : tasks) {
                    AJAXRequestResult result = new AJAXRequestResult(task);
                    resultConverter.convert(request, result, serverSession, new DefaultConverter());
                    Object resultObject = result.getResultObject();
                    objects.put(resultObject);
                }
            }
        }
        ConversionResult result = new ConversionResult();
        result.setData(objects);
        return result;
    }
}
