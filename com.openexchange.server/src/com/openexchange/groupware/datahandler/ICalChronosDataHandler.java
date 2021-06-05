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
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.ical.ICalParameters;
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
    private static final String FOLDER_PATH = "com.openexchange.groupware.task.folder";

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
        } catch (IOException e) {
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
            ImportedCalendar calendar = iCalService.importICal(stream, initializeParameters(iCalService));
            events = calendar.getEvents();
            if (null == events || events.isEmpty()) {
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
            access.set(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.NONE);
            access.set(CalendarParameters.PARAMETER_SKIP_EXTERNAL_ATTENDEE_URI_CHECKS, Boolean.TRUE);

            List<ImportResult> importEvents = access.importEvents(calendarFolder, events);
            if (null == importEvents || importEvents.isEmpty()) {
                if (null != access.getWarnings()) {
                    access.getWarnings().forEach(w -> result.addWarning(w));
                } else {
                    result.addWarning(ConversionWarning.Code.UNEXPECTED_ERROR.create("No appointments to import"));
                }
                return;
            }

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

    private ICalParameters initializeParameters(ICalService iCalService) {
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.SANITIZE_INPUT, Boolean.TRUE);
        return iCalParameters;
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
        } catch (JSONException e) {
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
            } catch (NumberFormatException e) {
                throw DataExceptionCodes.INVALID_ARGUMENT.create(key, e, dataArguments.get(key));
            }
        }
        return 0;
    }

    private long getSize(final Data<? extends Object> data) {
        try {
            return Long.parseLong(data.getDataProperties().get(DataProperties.PROPERTY_SIZE));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
