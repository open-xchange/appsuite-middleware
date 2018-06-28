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
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarParameters;
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
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;

/**
 * {@link ICalChronosDataHandler} - The data handler to insert calendar events and
 * tasks parsed from an ICal input stream.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> - original
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a> - chronos specified
 * @since v7.10.0
 */
public final class ICalChronosDataHandler extends ICalDataHandler {

    private static final String CALENDAR_PATH = "com.openexchange.groupware.calendar.folder";
    private static final String FOLDER_PATH   = "com.openexchange.groupware.task.folder";

    private static final Class<?>[] TYPES = { InputStream.class };

    /**
     * Initializes a new {@link ICalChronosDataHandler}
     *
     * @param services The {@link ServiceLookup}
     */
    public ICalChronosDataHandler(ServiceLookup services) {
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

        /*
         * Parse parameters
         */
        String calendarFolder = null;
        if (hasValue(dataArguments, CALENDAR_PATH)) {
            calendarFolder = dataArguments.get(CALENDAR_PATH);
        }
        int taskFolder = getArg(dataArguments, FOLDER_PATH);

        /*
         * The JSON array to return
         */
        final JSONArray folderAndIdArray = new JSONArray();
        ConversionResult result = new ConversionResult();

        InputStreamCopy inputStreamCopy = null;
        try {
            inputStreamCopy = copyStream((InputStream) data.getData(), getSize(data));
            handleEvents(session, calendarFolder, inputStreamCopy, folderAndIdArray, result);
            handleTasks(session, taskFolder, inputStreamCopy, folderAndIdArray, result);
        } catch (final IOException e) {
            throw DataExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            safeClose(inputStreamCopy);
        }

        result.setData(folderAndIdArray);
        return result;
    }

    private void handleEvents(final Session session, String calendarFolder, InputStreamCopy inputStreamCopy, final JSONArray folderAndIdArray, ConversionResult result) throws OXException, IOException {
        if (Strings.isEmpty(calendarFolder)) {
            result.addWarning(ConversionWarning.Code.NO_FOLDER_FOR_APPOINTMENTS.create());
            return;
        }
        /*
         * Parse events
         */
        List<Event> events;
        InputStream stream = null;

        try {
            ICalService iCalService = services.getServiceSafe(ICalService.class);
            stream = inputStreamCopy.getInputStream();
            ImportedCalendar calendar = iCalService.importICal(stream, null);
            events = calendar.getEvents();
            if (events.isEmpty()) {
                return;
            }
        } finally {
            safeClose(stream);
        }

        /*
         * Insert parsed events into denoted calendar folder
         */
        try {
            IDBasedCalendarAccess access = services.getServiceSafe(IDBasedCalendarAccessFactory.class).createAccess(session);
            access.set(CalendarParameters.UID_CONFLICT_STRATEGY, UIDConflictStrategy.UPDATE_OR_REASSIGN);
            access.set(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.TRUE);
            List<ImportResult> importEvents = access.importEvents(calendarFolder, events);
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

    private void handleTasks(final Session session, int taskFolder, InputStreamCopy inputStreamCopy, final JSONArray folderAndIdArray, ConversionResult result) throws OXException, IOException, ConversionError {
        if (taskFolder == 0) {
            result.addWarning(ConversionWarning.Code.NO_FOLDER_FOR_TASKS.create());
            return;
        }
        /*
         * Start parsing tasks
         */
        List<Task> tasks;
        List<ConversionError> conversionErrors = new ArrayList<ConversionError>(4);
        List<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>(4);

        Context ctx = ContextStorage.getStorageContext(session);
        TimeZone defaultZone = TimeZone.getTimeZone(UserStorage.getInstance().getUser(session.getUserId(), ctx).getTimeZone());
        ICalParser iCalParser = services.getServiceSafe(ICalParser.class);

        tasks = parseTaskStream(ctx, iCalParser, inputStreamCopy, conversionErrors, conversionWarnings, defaultZone);
        if (tasks.isEmpty()) {
            return;
        }

        /*
         * Insert parsed tasks into denoted task folder
         */
        try {
            insertTasks(session, taskFolder, tasks, folderAndIdArray);
        } catch (final JSONException e) {
            throw OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
        }
    }

    /*
     * ===============================================================
     * =========================== HELPERS ===========================
     * ===============================================================
     */

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

    private int getArg(DataArguments dataArguments, String key) throws OXException {
        if (hasValue(dataArguments, key)) {
            try {
                return Integer.parseInt(dataArguments.get(key));
            } catch (final NumberFormatException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(key, e, dataArguments.get(key));
            }
        }
        return 0;
    }

    private long getSize(final Data<? extends Object> data) {
        try {
            return Long.parseLong(data.getDataProperties().get(DataProperties.PROPERTY_SIZE));
        } catch (final NumberFormatException e) {
            return 0;
        }
    }
}
