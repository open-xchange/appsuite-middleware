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

package com.openexchange.share.handler.ical;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.config.ConfigurationService;
import com.openexchange.data.conversion.ical.ConversionError;
import com.openexchange.data.conversion.ical.ConversionWarning;
import com.openexchange.data.conversion.ical.ICalEmitter;
import com.openexchange.data.conversion.ical.ICalSession;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.calendar.AppointmentSqlFactoryService;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.groupware.container.DataObject;
import com.openexchange.groupware.container.FolderChildObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.modules.Module;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Task;
import com.openexchange.groupware.tasks.TasksSQLImpl;
import com.openexchange.java.Strings;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.servlet.handler.AccessShareRequest;
import com.openexchange.share.servlet.handler.HttpAuthShareHandler;
import com.openexchange.share.servlet.handler.ResolvedShare;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.user.UserService;

/**
 * {@link ICalHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ICalHandler extends HttpAuthShareHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ICalHandler.class);

    /**
     * The appointment properties being exported
     */
    private final static int[] APPOINTMENT_FIELDS = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.MODIFIED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED,
        FolderChildObject.FOLDER_ID, CommonObject.CATEGORIES, CommonObject.PRIVATE_FLAG, CommonObject.COLOR_LABEL, CalendarObject.TITLE,
        CalendarObject.START_DATE, CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_ID,
        CalendarObject.RECURRENCE_TYPE, CalendarObject.PARTICIPANTS, CalendarObject.USERS, Appointment.LOCATION,
        Appointment.FULL_TIME, Appointment.SHOWN_AS, Appointment.TIMEZONE, Appointment.UID
    };

    /**
     * The task properties being exported
     */
    private final static int[] TASK_FIELDS = {
        DataObject.OBJECT_ID, DataObject.CREATED_BY, DataObject.CREATION_DATE, DataObject.LAST_MODIFIED, DataObject.MODIFIED_BY,
        FolderChildObject.FOLDER_ID, CommonObject.PRIVATE_FLAG, CommonObject.CATEGORIES, CalendarObject.TITLE, CalendarObject.START_DATE,
        CalendarObject.END_DATE, CalendarObject.NOTE, CalendarObject.RECURRENCE_TYPE, CalendarObject.RECURRENCE_COUNT,
        CalendarObject.UNTIL, CalendarObject.PARTICIPANTS, Task.ACTUAL_COSTS, Task.ACTUAL_DURATION, CalendarObject.ALARM,
        Task.BILLING_INFORMATION, CommonObject.CATEGORIES, Task.COMPANIES, Task.CURRENCY, Task.DATE_COMPLETED, Task.IN_PROGRESS,
        Task.PERCENT_COMPLETED, Task.PRIORITY, Task.STATUS, Task.TARGET_COSTS, Task.TARGET_DURATION, Task.TRIP_METER,
        CommonObject.COLOR_LABEL, Task.UID
    };

    /**
     * Initializes a new {@link ICalHandler}.
     */
    public ICalHandler() {
        super();
    }

    @Override
    public int getRanking() {
        return 100;
    }

    @Override
    public boolean keepSession() {
        return false;
    }

    @Override
    protected boolean handles(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return (Module.CALENDAR.getFolderConstant() == shareRequest.getTarget().getModule() || Module.TASK.getFolderConstant() == shareRequest.getTarget().getModule()) &&
            (acceptsICal(request) || indicatesICalClient(request) || indicatesForcedICal(request));
    }

    @Override
    protected void handleResolvedShare(ResolvedShare resolvedShare) throws OXException, IOException {
        ShareTarget target = resolvedShare.getShareRequest().getTarget();
        /*
         * prepare iCal export
         */
        ICalEmitter iCalEmitter = Services.getService(ICalEmitter.class);
        ICalSession iCalSession = iCalEmitter.createSession();
        String name = extractName(resolvedShare, target);
        if (false == Strings.isEmpty(name)) {
            iCalSession.setName(name);
        }
        int module = target.getModule();
        if (Module.CALENDAR.getFolderConstant() == module) {
            writeCalendar(iCalEmitter, iCalSession, resolvedShare, target);
        } else if (Module.TASK.getFolderConstant() == module) {
            writeTasks(iCalEmitter, iCalSession, resolvedShare, target);
        } else {
            throw new UnsupportedOperationException("Unsupported module: " + module);
        }
        /*
         * write response
         */
        HttpServletResponse response = resolvedShare.getResponse();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/calendar");
        iCalEmitter.writeSession(iCalSession, response.getOutputStream());
        iCalEmitter.flush(iCalSession, response.getOutputStream());
    }

    /**
     * Writes the appointments of a calendar share using the supplied iCal emitter and session.
     *
     * @param iCalEmitter The iCal emitter
     * @param iCalSession the iCal session
     * @param share The resolved share
     * @throws OXException
     */
    private static void writeCalendar(ICalEmitter iCalEmitter, ICalSession iCalSession, ResolvedShare share, ShareTarget target) throws OXException {
        AppointmentSqlFactoryService factory = Services.getService(AppointmentSqlFactoryService.class);
        CalendarCollectionService calendarCollection = Services.getService(CalendarCollectionService.class);
        ArrayList<ConversionError> conversionErrors = new ArrayList<ConversionError>();
        ArrayList<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>();
        SearchIterator<Appointment> searchIterator = null;
        try {
            searchIterator = factory.createAppointmentSql(share.getSession()).getAppointmentsBetweenInFolder(
                Integer.valueOf(target.getFolder()), APPOINTMENT_FIELDS, getIntervalStart(), getIntervalEnd(),
                CalendarObject.START_DATE, Order.ASCENDING);
            while (searchIterator.hasNext()) {
                Appointment appointment = searchIterator.next();
                if (CalendarObject.NO_RECURRENCE != appointment.getRecurrenceType()) {
                    if (false == appointment.containsTimezone()) {
                        appointment.setTimezone(share.getUser().getTimeZone());
                    }
                    calendarCollection.replaceDatesWithFirstOccurence(appointment);
                }
                iCalEmitter.writeAppointment(iCalSession, appointment, share.getContext(), conversionErrors, conversionWarnings);
            }
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    /**
     * Writes the tasks of a task folder share using the supplied iCal emitter and session.
     *
     * @param iCalEmitter The iCal emitter
     * @param iCalSession the iCal session
     * @param share The resolved share
     * @throws OXException
     */
    private static void writeTasks(ICalEmitter iCalEmitter, ICalSession iCalSession, ResolvedShare share, ShareTarget target) throws OXException {
        TasksSQLInterface taskInterface = new TasksSQLImpl(share.getSession());
        ArrayList<ConversionError> conversionErrors = new ArrayList<ConversionError>();
        ArrayList<ConversionWarning> conversionWarnings = new ArrayList<ConversionWarning>();
        TaskSearchObject tso = new TaskSearchObject();
        tso.setRange(new Date[] { getIntervalStart(), getIntervalEnd() });
        tso.addFolder(Integer.valueOf(target.getFolder()).intValue());
        SearchIterator<Task> searchIterator = null;
        try {
            searchIterator = taskInterface.getTasksByExtendedSearch(tso, CalendarObject.START_DATE, Order.ASCENDING, TASK_FIELDS);
            while (searchIterator.hasNext()) {
                Task task = searchIterator.next();
                iCalEmitter.writeTask(iCalSession, task, share.getContext(), conversionErrors, conversionWarnings);
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
    }

    /**
     * Gets the date-time representing the end of the configured interval for exported iCal events.
     *
     * @return The interval end date
     */
    private static Date getIntervalEnd() {
        String value = Services.getService(ConfigurationService.class).getProperty(
            "com.openexchange.share.handler.iCal.futureInterval", "one_year");
        Calendar calendar = Calendar.getInstance();
        if ("one_month".equalsIgnoreCase(value)) {
            calendar.add(Calendar.MONTH, 1);
        } else if ("six_months".equalsIgnoreCase(value)) {
            calendar.add(Calendar.MONTH, 6);
        } else if ("one_year".equalsIgnoreCase(value)) {
            calendar.add(Calendar.YEAR, 1);
        } else if ("two_years".equalsIgnoreCase(value)) {
            calendar.add(Calendar.YEAR, 2);
        } else {
            LOG.warn("Unrecognized value for \"com.openexchange.share.handler.iCal.futureInterval\", falling back to \"one_year\"");
            calendar.add(Calendar.YEAR, 1);
        }
        for (int field : new int[] { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND }) {
            calendar.set(field, 0);
        }
        return calendar.getTime();
    }

    /**
     * Gets the date-time representing the start of the configured interval for exported iCal events.
     *
     * @return The interval start date
     */
    private static Date getIntervalStart() {
        String value = Services.getService(ConfigurationService.class).getProperty(
            "com.openexchange.share.handler.iCal.pastInterval", "one_month");
        Calendar calendar = Calendar.getInstance();
        if ("one_week".equalsIgnoreCase(value)) {
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
        } else if ("two_weeks".equalsIgnoreCase(value)) {
            calendar.add(Calendar.WEEK_OF_YEAR, -1);
        } else if ("one_month".equalsIgnoreCase(value)) {
            calendar.add(Calendar.MONTH, -1);
        } else if ("six_months".equalsIgnoreCase(value)) {
            calendar.add(Calendar.MONTH, -6);
        } else if ("one_year".equalsIgnoreCase(value)) {
            calendar.add(Calendar.YEAR, -1);
        } else {
            LOG.warn("Unrecognized value for \"com.openexchange.share.handler.iCal.pastInterval\", falling back to \"two_weeks\"");
            calendar.add(Calendar.YEAR, -1);
        }
        for (int field : new int[] { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND }) {
            calendar.set(field, 0);
        }
        return calendar.getTime();
    }

    private static boolean acceptsICal(HttpServletRequest request) {
        String acceptHeader = request.getHeader("Accept");
        return "text/calendar".equals(acceptHeader) || "text/iCal".equals(acceptHeader);
    }

    private static boolean indicatesICalClient(HttpServletRequest request) {
        String userAgentHeader = request.getHeader("User-Agent");
        return null != userAgentHeader && (
            userAgentHeader.contains("Microsoft Outlook") ||
            userAgentHeader.contains("Lightning") && userAgentHeader.contains("Thunderbird") ||
            userAgentHeader.contains("OutlookComCalendar") ||
            userAgentHeader.contains("Google-Calendar-Importer") ||
            userAgentHeader.contains("org.dmfs.caldav.lib")
        );
    }

    protected static boolean indicatesForcedICal(HttpServletRequest request) {
        return "ics".equalsIgnoreCase(AJAXUtility.sanitizeParam(request.getParameter("delivery"))) ||
            isTrue(AJAXUtility.sanitizeParam(request.getParameter("ics"))) ||
            "ical".equalsIgnoreCase(AJAXUtility.sanitizeParam(request.getParameter("delivery"))) ||
            isTrue(AJAXUtility.sanitizeParam(request.getParameter("ical")));
    }

    /**
     * Extracts the display name for a share, i.e. the (localized) folder name and owner information.
     *
     * @param share The share to extract the name for
     * @return The display name, or <code>null</code> if name extraction fails
     */
    private static String extractName(ResolvedShare share, ShareTarget target) {
        try {
            UserizedFolder folder = Services.getService(FolderService.class).getFolder(
                FolderStorage.REAL_TREE_ID, target.getFolder(), share.getSession(), null);
            Locale locale = share.getUser().getLocale();
            String name = null != locale ? folder.getLocalizedName(locale) : folder.getName();
            if (SharedType.getInstance().equals(folder.getType())) {
                int ownerID = folder.getCreatedBy();
                User user = Services.getService(UserService.class).getUser(ownerID, share.getContext());
                return name + " (" + user.getDisplayName() + ')';
            }
            return name;
        } catch (OXException e) {
            LOG.warn("Error extracting name for share {}", share, e);
            return null;
        }
    }

}
