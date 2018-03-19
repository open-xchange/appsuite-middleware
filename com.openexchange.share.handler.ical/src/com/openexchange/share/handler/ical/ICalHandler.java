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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.TimeZone;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.common.hash.Hashing;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.api2.TasksSQLInterface;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ical.CalendarExport;
import com.openexchange.chronos.ical.ICalParameters;
import com.openexchange.chronos.ical.ICalService;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccessFactory;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SortOrder;
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
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareExceptionCodes;
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
     * The event fields being exported
     */
    private final static EventField[] EVENT_FIELDS;
    static {
        EnumSet<EventField> fields = EnumSet.allOf(EventField.class);
        fields.remove(EventField.ATTACHMENTS);
        fields.remove(EventField.ALARMS);
        fields.remove(EventField.FLAGS);
        EVENT_FIELDS = fields.toArray(new EventField[fields.size()]);
    }

    private final ServiceLookup services;

    /**
     * Initializes a new {@link ICalHandler}.
     *
     * @param services A service lookup reference
     */
    public ICalHandler(ServiceLookup services) {
        super();
        this.services = services;
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
    protected boolean handles(AccessShareRequest shareRequest, HttpServletRequest request, HttpServletResponse response) {
        if (acceptsICal(request) || indicatesICalClient(request) || indicatesForcedICal(request)) {
            ShareTarget target = shareRequest.getTarget();
            return null == target || Module.CALENDAR.getFolderConstant() == target.getModule() || Module.TASK.getFolderConstant() == target.getModule();
        }
        return false;
    }

    @Override
    protected void handleResolvedShare(ResolvedShare resolvedShare) throws OXException, IOException {
        try {
            /*
             * get & check target
             */
            ShareTarget target = resolvedShare.getShareRequest().getTarget();
            if (null == target || resolvedShare.getShareRequest().isInvalidTarget()) {
                throw ShareExceptionCodes.UNKNOWN_SHARE.create(resolvedShare.getShareRequest().getTargetPath());
            }
            /*
             * export tasks or events based on share target
             */
            if (Module.CALENDAR.getFolderConstant() == target.getModule()) {
                writeEvents(resolvedShare, target);
            } else if (Module.TASK.getFolderConstant() == target.getModule()) {
                writeTasks(resolvedShare, target);
            } else {
                throw ShareExceptionCodes.UNEXPECTED_ERROR.create("Unsupported module: " + target.getModule());
            }
        } catch (OXException e) {
            sendError(resolvedShare.getResponse(), e);
        }
    }

    /**
     * Writes the events of a calendar folder.
     *
     * @param share The resolved share
     * @param target The share target
     */
    private void writeEvents(ResolvedShare share, ShareTarget target) throws OXException, IOException {
        /*
         * prepare iCal export
         */
        ICalService iCalService = services.getService(ICalService.class);
        ICalParameters iCalParameters = iCalService.initParameters();
        iCalParameters.set(ICalParameters.DEFAULT_TIMEZONE, TimeZone.getTimeZone(share.getUser().getTimeZone()));
        CalendarExport calendarExport = iCalService.exportICal(iCalParameters);
        UserizedFolder folder = services.getService(FolderService.class).getFolder(
            FolderStorage.REAL_TREE_ID, target.getFolder(), share.getSession(), null);
        String name = extractName(share, folder);
        if (Strings.isEmpty(name)) {
            name = "Calendar";
        }
        calendarExport.setName(name);
        calendarExport.setMethod("PUBLISH");
        /*
         * get events in folder & add to export, considering the client supplied ETag in "If-None-Match"
         */
        String ifNoneMatch = share.getRequest().getHeader("If-None-Match");
        Entry<String, List<Event>> eventsAndETag = getEvents(share.getSession(), folder, ifNoneMatch);
        if (null == eventsAndETag.getValue()) {
            share.getResponse().sendError(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }
        for (Event event : eventsAndETag.getValue()) {
            calendarExport.add(event);
        }
        /*
         * write response
         */
        HttpServletResponse response = share.getResponse();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/calendar; charset=UTF-8");
        response.setHeader("ETag", eventsAndETag.getKey());
        com.openexchange.tools.servlet.http.Tools.setHeaderForFileDownload(
            share.getRequest().getHeader("User-Agent"), response, name + ".ics");
        calendarExport.writeVCalendar(response.getOutputStream());
    }

    private Entry<String, List<Event>> getEvents(Session session, UserizedFolder folder, String ifNoneMatch) throws OXException {
        if (null == folder.getAccountID()) {
            /*
             * assume default account; get events from calendar service
             */
            CalendarSession calendarSession = services.getService(CalendarService.class).init(session);
            applyParameters(calendarSession);
            long sequenceNumber = calendarSession.getCalendarService().getSequenceNumber(calendarSession, folder.getID());
            String etag = getETag(folder, sequenceNumber);
            if (etag.equals(ifNoneMatch)) {
                return new AbstractMap.SimpleEntry<String, List<Event>>(etag, null);
            }
            List<Event> events = calendarSession.getCalendarService().getEventsInFolder(calendarSession, folder.getID());
            return new AbstractMap.SimpleEntry<String, List<Event>>(etag, events);
        } else {
            /*
             * get events from calendar access
             */
            IDBasedCalendarAccess calendarAccess = services.getService(IDBasedCalendarAccessFactory.class).createAccess(session);
            applyParameters(calendarAccess);
            Long sequenceNumber = calendarAccess.getSequenceNumbers(Collections.singletonList(folder.getID())).get(folder.getID());
            String etag = getETag(folder, null != sequenceNumber ? sequenceNumber.longValue() : 0L);
            if (etag.equals(ifNoneMatch)) {
                return new AbstractMap.SimpleEntry<String, List<Event>>(etag, null);
            }
            List<Event> events = calendarAccess.getEventsInFolder(folder.getID());
            return new AbstractMap.SimpleEntry<String, List<Event>>(etag, events);
        }
    }

    private <T extends CalendarParameters> T applyParameters(T calendarParameters) {
        calendarParameters
            .set(CalendarParameters.PARAMETER_ORDER_BY, EventField.START_DATE)
            .set(CalendarParameters.PARAMETER_ORDER, SortOrder.Order.ASC)
            .set(CalendarParameters.PARAMETER_FIELDS, EVENT_FIELDS)
            .set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE)
            .set(CalendarParameters.PARAMETER_RANGE_START, getIntervalStart())
            .set(CalendarParameters.PARAMETER_RANGE_END, getIntervalEnd())
            .set(CalendarParameters.PARAMETER_DEFAULT_ATTENDEE, Boolean.FALSE)
        ;
        return calendarParameters;
    }

    /**
     * Writes the tasks of a task folder.
     *
     * @param share The resolved share
     * @param target The share target
     */
    private void writeTasks(ResolvedShare share, ShareTarget target) throws OXException, IOException {
        /*
         * prepare iCal export
         */
        ICalEmitter iCalEmitter = services.getService(ICalEmitter.class);
        ICalSession iCalSession = iCalEmitter.createSession();
        String name = extractName(share, target);
        if (false == Strings.isEmpty(name)) {
            iCalSession.setName(name);
        }
        /*
         * perform the export
         */
        writeTasks(iCalEmitter, iCalSession, share, target);
        /*
         * write response
         */
        HttpServletResponse response = share.getResponse();
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("text/calendar");
        iCalEmitter.writeSession(iCalSession, response.getOutputStream());
        iCalEmitter.flush(iCalSession, response.getOutputStream());
    }

    /**
     * Writes the tasks of a task folder share using the supplied iCal emitter and session.
     *
     * @param iCalEmitter The iCal emitter
     * @param iCalSession the iCal session
     * @param share The resolved share
     * @throws OXException
     */
    private void writeTasks(ICalEmitter iCalEmitter, ICalSession iCalSession, ResolvedShare share, ShareTarget target) throws OXException {
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
    private Date getIntervalEnd() {
        String value = services.getService(ConfigurationService.class).getProperty(
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
    private Date getIntervalStart() {
        String value = services.getService(ConfigurationService.class).getProperty(
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
     * @param share The resolved share
     * @param target The share target to extract the name for
     * @return The display name, or <code>null</code> if name extraction fails
     */
    private String extractName(ResolvedShare share, ShareTarget target) {
        try {
            UserizedFolder folder = services.getService(FolderService.class).getFolder(
                FolderStorage.REAL_TREE_ID, target.getFolder(), share.getSession(), null);
            return extractName(share, folder);
        } catch (OXException e) {
            LOG.warn("Error extracting name for share {}", share, e);
            return null;
        }
    }

    /**
     * Extracts the display name for a share, i.e. the (localized) folder name and owner information.
     *
     * @param share The resolved share
     * @param folder The share to extract the name for
     * @return The display name, or <code>null</code> if name extraction fails
     */
    private String extractName(ResolvedShare share, UserizedFolder folder) {
        try {
            Locale locale = share.getUser().getLocale();
            String name = null != locale ? folder.getLocalizedName(locale) : folder.getName();
            if (SharedType.getInstance().equals(folder.getType())) {
                int ownerID = folder.getCreatedBy();
                User user = services.getService(UserService.class).getUser(ownerID, share.getContext());
                return name + " (" + user.getDisplayName() + ')';
            }
            return name;
        } catch (OXException e) {
            LOG.warn("Error extracting name for share {}", share, e);
            return null;
        }
    }

    private static String getETag(UserizedFolder folder, long sequenceNumber) {
        long folderLastModified = null != folder.getLastModifiedUTC() ? folder.getLastModifiedUTC().getTime() : 0L;
        return Hashing.murmur3_128().newHasher().putLong(folderLastModified).putLong(sequenceNumber).hash().toString();
    }

}
