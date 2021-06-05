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

package com.openexchange.data.conversion.ical.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.SchedulingControl;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.java.Streams;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ICalUtil}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public class ICalUtil {

    /**
     * Imports appointments and/or tasks from the iCal stream and stores them in corresponding default folder of the user.
     *
     * @param inputStream The input stream carrying the iCal data
     * @param session The session
     * @return The imported calendar items
     */
    public static List<CalendarObject> importToDefaultFolder(InputStream inputStream, ServerSession session) throws OXException {
        OXFolderAccess access = new OXFolderAccess(session.getContext());
        FolderObject taskFolder = access.getDefaultFolder(session.getUserId(), FolderObject.TASK);
        FolderObject calendarFolder = access.getDefaultFolder(session.getUserId(), FolderObject.CALENDAR);
        List<CalendarObject> calendarItems = new ArrayList<CalendarObject>();
        ThresholdFileHolder fileHolder = null;
        try {
            fileHolder = new ThresholdFileHolder();
            fileHolder.write(inputStream);
            InputStream stream = fileHolder.getStream();
            try {
                calendarItems.addAll(asCalendarObjects(importEvents(stream, session, calendarFolder.getObjectID())));
            } finally {
                Streams.close(stream);
            }
            stream = fileHolder.getStream();
            try {
                calendarItems.addAll(importTasks(stream, session, taskFolder.getObjectID()));
            } finally {
                Streams.close(stream);
            }
        } finally {
            Streams.close(inputStream, fileHolder);
        }
        return calendarItems;
    }

    /**
     * Imports events from the iCal stream and stores them in a specific folder.
     *
     * @param inputStream The input stream carrying the iCal data
     * @param session The session
     * @param folderID The target folder identifier
     * @return Create results for the imported events
     */
    private static List<CreateResult> importEvents(InputStream inputStream, ServerSession session, int folderID) throws OXException {
        List<CreateResult> results = new ArrayList<CreateResult>();
        List<Event> events = parseEvents(inputStream, session);
        if (null != events && 0 < events.size()) {
            CalendarSession calendarSession = ServerServiceRegistry.getInstance().getService(CalendarService.class).init(session);
            calendarSession.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.FALSE);
            calendarSession.set(CalendarParameters.PARAMETER_SKIP_EXTERNAL_ATTENDEE_URI_CHECKS, Boolean.TRUE);
            calendarSession.set(CalendarParameters.PARAMETER_SCHEDULING, SchedulingControl.NONE);
            for (Event event : events) {
                CalendarResult result = calendarSession.getCalendarService().createEvent(calendarSession, String.valueOf(folderID), event);
                results.addAll(result.getCreations());
            }
        }
        return results;
    }

    /**
     * Imports tasks from the iCal stream and stores them in a specific folder.
     *
     * @param inputStream The input stream carrying the iCal data
     * @param session The session
     * @param folderID The target folder identifier
     * @return The imported tasks
     */
    private static List<Task> importTasks(InputStream inputStream, ServerSession session, int folderID) throws OXException {
        List<Task> tasks = parseTasks(inputStream, session);
        if (null != tasks && 0 < tasks.size()) {
            TasksSQLImpl tasksSql = new TasksSQLImpl(session);
            for (Task task : tasks) {
                task.setParentFolderID(folderID);
                tasksSql.insertTaskObject(task);
            }
        }
        return tasks;
    }

    private static List<Event> parseEvents(InputStream inputStream, ServerSession session) throws OXException {
        ICalService iCalService = ServerServiceRegistry.getServize(ICalService.class, true);
        ICalParameters parameters = iCalService.initParameters();
        parameters.set(ICalParameters.DEFAULT_TIMEZONE, TimeZone.getTimeZone(session.getUser().getTimeZone()));
        parameters.set(ICalParameters.IGNORE_UNSET_PROPERTIES, Boolean.TRUE);
        ImportedCalendar importedCalendar = iCalService.importICal(inputStream, parameters);
        return importedCalendar.getEvents();
    }

    private static List<Task> parseTasks(InputStream inputStream, ServerSession session) throws OXException {
        ICalParser iCalParser = ServerServiceRegistry.getServize(ICalParser.class, true);
        return iCalParser.parseTasks(inputStream, TimeZone.getTimeZone(session.getUser().getTimeZone()), session.getContext(), new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>()).getImportedObjects();
    }

    private static List<CalendarObject> asCalendarObjects(List<CreateResult> createResults) {
        List<CalendarObject> calendarObjects = new ArrayList<CalendarObject>(createResults.size());
        for (CreateResult result : createResults) {
            final Event event = result.getCreatedEvent();
            calendarObjects.add(new CalendarObject() {

                private static final long serialVersionUID = -3529229714201994306L;

                @Override
                public boolean containsObjectID() {
                    return true;
                }

                @Override
                public int getObjectID() {
                    return Event2Appointment.asInt(event.getId());
                }

                @Override
                public boolean containsParentFolderID() {
                    return true;
                }

                @Override
                public int getParentFolderID() {
                    return Event2Appointment.asInt(event.getFolderId());
                }
            });
        }
        return calendarObjects;
    }

    private ICalUtil() {
        // prevent instantiation
    }

}
