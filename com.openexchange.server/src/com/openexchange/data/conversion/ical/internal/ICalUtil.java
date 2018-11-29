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

package com.openexchange.data.conversion.ical.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.chronos.Event;
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
            calendarSession.set(CalendarParameters.PARAMETER_SUPPRESS_ITIP, Boolean.TRUE);
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
