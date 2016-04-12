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
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarDataObject;
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
     * Imports appointments the iCal stream and stores them in a specific folder.
     *
     * @param inputStream The input stream carrying the iCal data
     * @param session The session
     * @param folderID The target folder identifier
     * @return The imported appointments
     */
    public static List<CalendarDataObject> importAppointments(InputStream inputStream, ServerSession session, int folderID) throws OXException {
        List<CalendarDataObject> appointments = parseAppointments(inputStream, session);
        if (null != appointments && 0 < appointments.size()) {
            AppointmentSQLInterface appointmentSql = ServerServiceRegistry.getInstance().getService(AppointmentSqlFactoryService.class).createAppointmentSql(session);
            for (CalendarDataObject appointment : appointments) {
                appointment.setContext(session.getContext());
                appointment.setParentFolderID(folderID);
                appointmentSql.insertAppointmentObject(appointment);
            }
        }
        return appointments;
    }

    /**
     * Imports tasks the iCal stream and stores them in a specific folder.
     *
     * @param inputStream The input stream carrying the iCal data
     * @param session The session
     * @param folderID The target folder identifier
     * @return The imported tasks
     */
    public static List<Task> importTasks(InputStream inputStream, ServerSession session, int folderID) throws OXException {
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
            calendarItems.addAll(importAppointments(fileHolder.getStream(), session, calendarFolder.getObjectID()));
            calendarItems.addAll(importTasks(fileHolder.getStream(), session, taskFolder.getObjectID()));
        } finally {
            Streams.close(inputStream, fileHolder);
        }
        return calendarItems;
    }

    private static List<CalendarDataObject> parseAppointments(InputStream inputStream, ServerSession session) throws OXException {
        try {
            ICalParser iCalParser = ServerServiceRegistry.getServize(ICalParser.class, true);
            return iCalParser.parseAppointments(inputStream, TimeZone.getTimeZone(session.getUser().getTimeZone()),
                session.getContext(), new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());
        } finally {
            Streams.close(inputStream);
        }
    }

    private static List<Task> parseTasks(InputStream inputStream, ServerSession session) throws OXException {
        try {
            ICalParser iCalParser = ServerServiceRegistry.getServize(ICalParser.class, true);
            return iCalParser.parseTasks(inputStream, TimeZone.getTimeZone(session.getUser().getTimeZone()),
                session.getContext(), new ArrayList<ConversionError>(), new ArrayList<ConversionWarning>());
        } finally {
            Streams.close(inputStream);
        }
    }

    private ICalUtil() {
        // prevent instantiation
    }

}
