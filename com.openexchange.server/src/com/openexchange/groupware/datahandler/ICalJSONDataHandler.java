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
            } catch (NumberFormatException e) {
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
        } catch (IOException e) {
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
