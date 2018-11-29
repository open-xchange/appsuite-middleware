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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.ImportResult;
import com.openexchange.chronos.service.UIDConflictStrategy;
import com.openexchange.chronos.service.UpdateResult;
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
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link ICalInsertDataHandler} - The data handler to insert appointments and
 * tasks parsed from an ICal input stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ICalInsertDataHandler extends ICalDataHandler {

    private static final String[] ARGS = { "com.openexchange.groupware.calendar.folder",
        "com.openexchange.groupware.task.folder" };

    private static final Class<?>[] TYPES = { InputStream.class };

    /**
     * Initializes a new {@link ICalInsertDataHandler}
     *
     * @param services The {@link ServiceLookup}
     */
    public ICalInsertDataHandler(ServiceLookup services) {
        super(services);
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

        int calendarFolder = 0;
        if (hasValue(dataArguments, ARGS[0])) {
            try {
                calendarFolder = Integer.parseInt(dataArguments.get(ARGS[0]));
            } catch (final NumberFormatException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(ARGS[0], e, dataArguments.get(ARGS[0]));
            }
        }

        int taskFolder = 0;
        if (hasValue(dataArguments, ARGS[1])) {
            try {
                taskFolder = Integer.parseInt(dataArguments.get(ARGS[1]));
            } catch (final NumberFormatException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(ARGS[1], e, dataArguments.get(ARGS[1]));
            }
        }

        final Context ctx = ContextStorage.getStorageContext(session);
        final ICalParser iCalParser = services.getServiceSafe(ICalParser.class);
        if (iCalParser == null) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(ICalParser.class.getName());
        }
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
        try {
            /*
             * Get user time zone
             */
            final TimeZone defaultZone = TimeZone.getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone());
            /*
             * Errors and warnings
             */
            final List<ConversionError> conversionErrors = new ArrayList<ConversionError>(4);
            final List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>(4);
            {
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
                 * Start parsing tasks
                 */
                tasks = parseTaskStream(ctx, iCalParser, inputStreamCopy, conversionErrors, conversionWarnings, defaultZone);
            }
        } catch (final IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            safeClose(inputStreamCopy);
        }
        /*
         * The JSON array to return
         */
        final JSONArray folderAndIdArray = new JSONArray();
        ConversionResult result = new ConversionResult();
        if (!events.isEmpty()) {
            if (calendarFolder == 0) {
                result.addWarning(ConversionWarning.Code.NO_FOLDER_FOR_APPOINTMENTS.create());
            } else {
                /*
                 * Insert parsed appointments into denoted calendar folder
                 */
                try {
                    CalendarService calendarService = services.getServiceSafe(CalendarService.class);
                    CalendarSession calendarSession = calendarService.init(session);
                    calendarSession.set(CalendarParameters.UID_CONFLICT_STRATEGY, UIDConflictStrategy.UPDATE_OR_REASSIGN);
                    calendarSession.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.FALSE);
                    calendarSession.set(CalendarParameters.PARAMETER_SKIP_EXTERNAL_ATTENDEE_URI_CHECKS, Boolean.TRUE);
                    calendarSession.set(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.TRUE);
                    List<ImportResult> importEvents = calendarService.importEvents(calendarSession, String.valueOf(calendarFolder), events);
                    for (ImportResult importEvent : importEvents) {
                        if (null == importEvent.getError()) {
                            for (CreateResult created : importEvent.getCreations()) {
                                folderAndIdArray.put(new JSONObject().put(FolderChildFields.FOLDER_ID, calendarFolder).put(DataFields.ID, created.getCreatedEvent().getId()));
                            }
                            for (UpdateResult updated : importEvent.getUpdates()) {
                                folderAndIdArray.put(new JSONObject().put(FolderChildFields.FOLDER_ID, calendarFolder).put(DataFields.ID, updated.getUpdate().getId()));
                            }
                        } else {
                            throw importEvent.getError();
                        }
                    }
                } catch (JSONException e) {
                    throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                }
            }
        }
        if (!tasks.isEmpty()) {
            if (taskFolder == 0) {
                result.addWarning(ConversionWarning.Code.NO_FOLDER_FOR_TASKS.create());
            } else {
                /*
                 * Insert parsed tasks into denoted task folder
                 */
                try {
                    insertTasks(session, taskFolder, tasks, folderAndIdArray);
                } catch (final JSONException e) {
                    throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e, new Object[0]);
                }
            }
        }
        result.setData(folderAndIdArray);
        return result;
    }

    private boolean hasValue(DataArguments dataArguments, String key) {
        if (!dataArguments.containsKey(key)) {
            return false;
        }
        if (dataArguments.get(key) == null) {
            return false;
        }
        if (dataArguments.get(key).equals("null")) {
            return false;
        }
        if (dataArguments.get(key).trim().isEmpty()) {
            return false;
        }
        return true;
    }
}
