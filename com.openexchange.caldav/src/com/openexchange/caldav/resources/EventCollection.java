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

package com.openexchange.caldav.resources;

import static com.openexchange.dav.DAVProtocol.protocolException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.PhantomMaster;
import com.openexchange.caldav.Tools;
import com.openexchange.caldav.mixins.AllowedSharingModes;
import com.openexchange.caldav.mixins.CalendarOrder;
import com.openexchange.caldav.mixins.CalendarOwner;
import com.openexchange.caldav.mixins.CalendarTimezone;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDate;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDatetime;
import com.openexchange.caldav.mixins.Invite;
import com.openexchange.caldav.mixins.ManagedAttachmentsServerURL;
import com.openexchange.caldav.mixins.MaxDateTime;
import com.openexchange.caldav.mixins.MinDateTime;
import com.openexchange.caldav.mixins.Organizer;
import com.openexchange.caldav.mixins.ScheduleDefaultCalendarURL;
import com.openexchange.caldav.mixins.ScheduleDefaultTasksURL;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSets;
import com.openexchange.caldav.mixins.SupportedReportSet;
import com.openexchange.caldav.query.Filter;
import com.openexchange.caldav.query.FilterAnalyzer;
import com.openexchange.caldav.reports.FilteringResource;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.mixins.CalendarColor;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;
import com.openexchange.webdav.protocol.WebdavStatusImpl;
import com.openexchange.webdav.protocol.helpers.AbstractResource;

/**
 * {@link EventCollection}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class EventCollection extends FolderCollection<Event> implements FilteringResource {

    /** Fields that are always retrieved when requesting event lists from the service */
    private static final EventField[] BASIC_FIELDS = new EventField[] {
        EventField.UID, EventField.FILENAME, EventField.FOLDER_ID, EventField.ID, EventField.SERIES_ID,
        EventField.CREATED, EventField.CREATED_BY, EventField.TIMESTAMP, EventField.LAST_MODIFIED, EventField.MODIFIED_BY, EventField.CLASSIFICATION,
        EventField.START_DATE, EventField.END_DATE,
        EventField.RECURRENCE_RULE, EventField.CHANGE_EXCEPTION_DATES, EventField.DELETE_EXCEPTION_DATES
    };

    protected final GroupwareCaldavFactory factory;
    protected final String folderID;

    private CalendarSession calendarSession;
    private final MinDateTime minDateTime;
    private final MaxDateTime maxDateTime;
    private Date lastModified;

    /**
     * Initializes a new {@link EventCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     */
    protected EventCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder) throws OXException {
        this(factory, url, folder, CalendarOrder.NO_ORDER);
    }

    /**
     * Initializes a new {@link CalDAVFolderCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder, or <code>null</code> if it not yet exists
     * @param order The calendar order to use, or {@value #NO_ORDER} for no specific order
     */
    public EventCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder);
        this.factory = factory;
        this.folderID = null != folder ? folder.getID() : null;
        this.minDateTime = new MinDateTime(factory);
        this.maxDateTime = new MaxDateTime(factory);
        includeProperties(new SupportedReportSet(), minDateTime, maxDateTime, new Invite(factory, this),
            new AllowedSharingModes(factory.getSession()), new CalendarOwner(this), new Organizer(this),
            new ScheduleDefaultCalendarURL(factory), new ScheduleDefaultTasksURL(factory), new CalendarColor(this),
            new ManagedAttachmentsServerURL(), new CalendarTimezone(factory, this),
            new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VEVENT),
            new SupportedCalendarComponentSets(SupportedCalendarComponentSets.VEVENT),
            new DefaultAlarmVeventDate(), new DefaultAlarmVeventDatetime()
        );
        if (CalendarOrder.NO_ORDER != order) {
            includeProperties(new CalendarOrder(order));
        }
    }

    public CalendarSession getCalendarSession() throws WebdavProtocolException {
        if (null == calendarSession) {
            try {
                calendarSession = factory.getService(CalendarService.class).init(factory.getSession());
                calendarSession.set(CalendarParameters.PARAMETER_INCLUDE_PRIVATE, Boolean.TRUE);
                calendarSession.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
                calendarSession.set(CalendarParameters.PARAMETER_IGNORE_CONFLICTS, Boolean.TRUE);
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
        }
        return calendarSession;
    }

    /**
     * Gets the actual target calendar user of the collection. This is either the current session's user for "private" or "public"
     * folders, or the folder owner for "shared" calendar folders.
     *
     * @return The calendar user
     */
    public User getCalendarUser() throws OXException {
        if (SharedType.getInstance().equals(folder.getType())) {
            return super.getFactory().getService(UserService.class).getUser(folder.getCreatedBy(), folder.getContext());
        }
        return folder.getUser();
    }

    @Override
    public GroupwareCaldavFactory getFactory() {
        return factory;
    }

    @Override
    public Date getLastModified() throws WebdavProtocolException {
        if (null == this.lastModified) {
            lastModified = Tools.getLatestModified(minDateTime.getMinDateTime(), folder);
            try {
                CalendarSession calendarSession = getCalendarSession();
                calendarSession.set(CalendarParameters.PARAMETER_FIELDS, new EventField[] { EventField.TIMESTAMP });
                UpdatesResult updates = calendarSession.getCalendarService().getUpdatedEventsInFolder(calendarSession, folderID, lastModified.getTime());
                /*
                 * new and modified objects
                 */
                for (Event event : updates.getNewAndModifiedEvents()) {
                    lastModified = Tools.getLatestModified(lastModified, event);
                }
                /*
                 * deleted objects
                 */
                for (Event event : updates.getDeletedEvents()) {
                    lastModified = Tools.getLatestModified(lastModified, event);
                }
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
        }
        return lastModified;
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        StringBuilder stringBuilder = new StringBuilder(super.getResourceType());
        stringBuilder.append('<').append(DAVProtocol.CAL_NS.getPrefix()).append(":calendar/>");
        if (null != folder) {
            if (SharedType.getInstance().equals(folder.getType())) {
                // used to indicate that the calendar is owned by another user and is being shared to the current user.
                stringBuilder.append('<').append(DAVProtocol.CALENDARSERVER_NS.getPrefix()).append(":shared/>");
            } else if (PrivateType.getInstance().equals(folder.getType())) {
                // used to indicate that the calendar is owned by the current user and is being shared by them.
                stringBuilder.append('<').append(DAVProtocol.CALENDARSERVER_NS.getPrefix()).append(":shared-owner/>");
            } else if (PublicType.getInstance().equals(folder.getType())) {
                // evaluate own permission if folder shares can be edited or not
                stringBuilder.append('<').append(DAVProtocol.CALENDARSERVER_NS.getPrefix()).append(folder.getOwnPermission().isAdmin() ? ":shared-owner/>" : ":shared/>");
            }
        }
        return stringBuilder.toString();
    }

    @Override
    public List<WebdavResource> filter(Filter filter) throws WebdavProtocolException {
        List<Object> arguments = new ArrayList<Object>(2);
        if (false == FilterAnalyzer.VEVENT_RANGE_QUERY_ANALYZER.match(filter, arguments)) {
            throw protocolException(getUrl(), HttpServletResponse.SC_NOT_IMPLEMENTED);
        }
        Date from = 0 < arguments.size() ? toDate(arguments.get(0)) : null;
        if (null == from || from.before(minDateTime.getMinDateTime())) {
            from = minDateTime.getMinDateTime();
        }
        Date until = 1 < arguments.size() ? toDate(arguments.get(1)) : null;
        if (null == until || until.after(maxDateTime.getMaxDateTime())) {
            until = maxDateTime.getMaxDateTime();
        }
        try {
            CalendarSession calendarSession = getCalendarSession();
            calendarSession.set(CalendarParameters.PARAMETER_RANGE_START, from);
            calendarSession.set(CalendarParameters.PARAMETER_RANGE_END, until);
            calendarSession.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
            List<Event> events = calendarSession.getCalendarService().getEventsInFolder(calendarSession, folderID);
            List<WebdavResource> resources = new ArrayList<WebdavResource>(events.size());
            for (Event event : events) {
                resources.add(createResource(event, constructPathForChildResource(event)));
            }
            return resources;
        } catch (OXException e) {
            throw protocolException(getUrl(), e);
        }
    }

    private static Date toDate(Object object) {
        long tstamp = (Long) object;
        if (tstamp == -1) {
            return null;
        }
        return new Date(tstamp);
    }

    @Override
    protected Collection<Event> getObjects() throws OXException {
        CalendarSession calendarSession = getCalendarSession();
        calendarSession.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
        calendarSession.set(CalendarParameters.PARAMETER_RANGE_START, minDateTime.getMinDateTime());
        calendarSession.set(CalendarParameters.PARAMETER_RANGE_END, maxDateTime.getMaxDateTime());
        return calendarSession.getCalendarService().getEventsInFolder(calendarSession, folderID);
    }

    @Override
    protected Event getObject(String resourceName) throws OXException {
        if (Strings.isEmpty(resourceName)) {
            return null;
        }
        CalendarSession calendarSession = getCalendarSession();
        calendarSession.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
        /*
         * by default, try to resolve object by UID and filename
         */
        String objectID = calendarSession.getCalendarService().resolveByUID(calendarSession, resourceName);
        if (null == objectID) {
            objectID = calendarSession.getCalendarService().resolveByFilename(calendarSession, resourceName);
        }
        if (null != objectID) {
            try {
                return calendarSession.getCalendarService().getEvent(calendarSession, folderID, new EventID(folderID, objectID));
            } catch (OXException e) {
                if ("CAL-4041".equals(e.getErrorCode())) {
                    /*
                     * "Event not found in folder..." -> also try to load detached occurrences
                     */
                    List<Event> detachedOccurrences = calendarSession.getCalendarService().getChangeExceptions(calendarSession, folderID, objectID);
                    if (0 < detachedOccurrences.size()) {
                        return new PhantomMaster(detachedOccurrences);
                    }
                } else {
                    throw e;
                }
            }
        }
        return null; // not found
    }

    @Override
    protected AbstractResource createResource(Event object, WebdavPath url) throws OXException {
        return new EventResource(this, object, url);
    }

    @Override
    protected String getFileExtension() {
        return CalDAVResource.EXTENSION_ICS;
    }

    @Override
    protected WebdavPath constructPathForChildResource(Event object) {
        String fileName = object.getFilename();
        if (Strings.isEmpty(fileName)) {
            fileName = object.getUid();
        }
        String fileExtension = getFileExtension().toLowerCase();
        if (false == fileExtension.startsWith(".")) {
            fileExtension = "." + fileExtension;
        }
        return constructPathForChildResource(fileName + fileExtension);
    }

    @Override
    protected SyncStatus<WebdavResource> getSyncStatus(Date since) throws OXException {
        SyncStatus<WebdavResource> multistatus = new SyncStatus<WebdavResource>();
        boolean initialSync = 0 == since.getTime();
        Date nextSyncToken = Tools.getLatestModified(since, folder);
        /*
         * get new, modified & deleted objects since client token
         */
        CalendarSession calendarSession = getCalendarSession();
        calendarSession.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
        UpdatesResult updates = calendarSession.getCalendarService().getUpdatedEventsInFolder(calendarSession, folderID, since.getTime());
        /*
         * determine sync-status & overall last-modified
         */
        Set<String> uids = new HashSet<String>();
        for (Event event : updates.getNewAndModifiedEvents()) {
            WebdavResource resource = createResource(event, constructPathForChildResource(event));
            int status = null != event.getCreated() && event.getCreated().after(since) ?
                HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
            multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
            uids.add(event.getUid());
            // remember aggregated last modified for next sync token
            nextSyncToken = Tools.getLatestModified(nextSyncToken, event);
        }
        for (Event event : updates.getDeletedEvents()) {
            // only include objects that are not also modified (due to move operations)
            if (null != event.getUid() && false == uids.contains(event.getUid())) {
                if (false == initialSync) {
                    // add resource to multistatus
                    WebdavResource resource = createResource(event, constructPathForChildResource(event));
                    multistatus.addStatus(new WebdavStatusImpl<WebdavResource>(HttpServletResponse.SC_NOT_FOUND, resource.getUrl(), resource));
                }
                // remember aggregated last modified for parent folder
                nextSyncToken = Tools.getLatestModified(nextSyncToken, event);
            }
        }
        /*
         * Return response with new next sync-token in response
         */
        multistatus.setToken(Long.toString(nextSyncToken.getTime()));
        return multistatus;
    }

}
