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
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletResponse;
import org.jdom2.Namespace;
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
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.dav.DAVProperty;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.PreconditionException;
import com.openexchange.dav.mixins.CalendarColor;
import com.openexchange.dav.mixins.CalendarDescription;
import com.openexchange.dav.mixins.CurrentUserPrivilegeSet;
import com.openexchange.dav.mixins.ScheduleCalendarTransp;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalendarFolderConverter;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.login.Interface;
import com.openexchange.user.UserService;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProperty;
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
        EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE, EventField.DELETE_EXCEPTION_DATES, EventField.CHANGE_EXCEPTION_DATES
    };

    protected final GroupwareCaldavFactory factory;
    protected final String folderID;

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
        includeProperties(
            new CurrentUserPrivilegeSet(folder.getOwnPermission(), true),
            new SupportedReportSet(),
            minDateTime,
            maxDateTime,
            new Invite(factory, this),
            new AllowedSharingModes(factory.getSession()),
            new CalendarOwner(this),
            new Organizer(this),
            new ScheduleDefaultCalendarURL(factory),
            new ScheduleDefaultTasksURL(factory),
            new CalendarColor(this),
            new CalendarDescription(this),
            new ScheduleCalendarTransp(this),
            new ManagedAttachmentsServerURL(),
            new CalendarTimezone(factory, this),
            new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VEVENT),
            new SupportedCalendarComponentSets(SupportedCalendarComponentSets.VEVENT),
            new DefaultAlarmVeventDate(),
            new DefaultAlarmVeventDatetime()
        );
        if (CalendarOrder.NO_ORDER != order) {
            includeProperties(new CalendarOrder(order));
        }
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
                UpdatesResult updates = new CalendarAccessOperation<UpdatesResult>(factory) {

                    @Override
                    protected UpdatesResult perform(IDBasedCalendarAccess access) throws OXException {
                        access.set(CalendarParameters.PARAMETER_FIELDS, new EventField[] { EventField.TIMESTAMP });
                        return access.getUpdatedEventsInFolder(folderID, lastModified.getTime());
                    }
                }.execute(factory.getSession());
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
    public String getPushTopic() {
        return null != folder ? "ox:" + Interface.CALDAV.toString().toLowerCase() + ":" + folder.getID() : null;
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
        Date minDateTime = this.minDateTime.getMinDateTime();
        Date rangeStart = null == from ? minDateTime : null == minDateTime ? from : from.before(minDateTime) ? minDateTime : from;
        Date until = 1 < arguments.size() ? toDate(arguments.get(1)) : null;
        Date maxDateTime = this.maxDateTime.getMaxDateTime();
        Date rangeEnd = null == until ? maxDateTime : null == maxDateTime ? until : until.after(maxDateTime) ? maxDateTime : until;
        try {
            List<Event> events = new CalendarAccessOperation<List<Event>>(factory) {

                @Override
                protected List<Event> perform(IDBasedCalendarAccess access) throws OXException {
                    access.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
                    access.set(CalendarParameters.PARAMETER_RANGE_START, rangeStart);
                    access.set(CalendarParameters.PARAMETER_RANGE_END, rangeEnd);
                    return access.getEventsInFolder(folderID);
                }
            }.execute(factory.getSession());
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
        return new CalendarAccessOperation<Collection<Event>>(factory) {

            @Override
            protected Collection<Event> perform(IDBasedCalendarAccess access) throws OXException {
                access.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
                access.set(CalendarParameters.PARAMETER_RANGE_START, minDateTime.getMinDateTime());
                access.set(CalendarParameters.PARAMETER_RANGE_END, maxDateTime.getMaxDateTime());
                return access.getEventsInFolder(folderID);
            }
        }.execute(factory.getSession());
    }

    @Override
    protected Event getObject(String resourceName) throws OXException {
        if (Strings.isEmpty(resourceName)) {
            return null;
        }
        return new CalendarAccessOperation<Event>(factory) {

            @Override
            protected Event perform(IDBasedCalendarAccess access) throws OXException {
                access.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
                List<Event> events = access.resolveResource(folderID, resourceName);
                if (null == events || events.isEmpty()) {
                    return null;
                }
                Event event = events.get(0);
                return CalendarUtils.isSeriesException(event) ? new PhantomMaster(events) : event;
            }
        }.execute(factory.getSession());
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
        UpdatesResult updates = new CalendarAccessOperation<UpdatesResult>(factory) {

            @Override
            protected UpdatesResult perform(IDBasedCalendarAccess access) throws OXException {
                access.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
                return access.getUpdatedEventsInFolder(folderID, since.getTime());
            }
        }.execute(factory.getSession());
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

    @Override
    protected void internalPutProperty(WebdavProperty property) throws WebdavProtocolException {
        if (DAVProperty.class.isInstance(property)) {
            putProperty((DAVProperty) property);
            return;
        }
        throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "cannot-modify-protected-property", getUrl(), HttpServletResponse.SC_FORBIDDEN);
    }

    private void putProperty(DAVProperty property) throws WebdavProtocolException {
        ParameterizedFolder folderToUpdate = getFolderToUpdate();
        if (matches(property, CalendarColor.NAMESPACE, CalendarColor.NAME)) {
            String value = CalendarColor.parse(property);
            /*
             * apply color folder property
             */
            CalendarFolderConverter.setExtendedProperty(folderToUpdate, CalendarFolderProperty.COLOR(value));
            /*
             * also apply color in meta field for private folders
             */
            if (PrivateType.getInstance().equals(folder.getType())) {
                Map<String, Object> meta = folderToUpdate.getMeta();
                if (Strings.isEmpty(value)) {
                    meta.remove("color");
                } else {
                    meta.put("color", value);
                }
                /*
                 * if possible, also try and match a specific color-label
                 */
                Map<String, String> attributes = property.getAttributes();
                if (null != attributes && attributes.containsKey(CalendarColor.SYMBOLIC_COLOR)) {
                    Integer uiValue = CalendarColor.mapColorLabel(attributes.get(CalendarColor.SYMBOLIC_COLOR));
                    if (uiValue != null) {
                        meta.put("color_label", uiValue);
                    }
                }
            }
        } else if (matches(property, CalendarDescription.NAMESPACE, CalendarDescription.NAME)) {
            /*
             * apply description folder property
             */
            String value = null != property.getElement() ? property.getElement().getText() : null;
            CalendarFolderConverter.setExtendedProperty(folderToUpdate, CalendarFolderProperty.DESCRIPTION(value));
        } else if (ScheduleCalendarTransp.NAMESPACE.getURI().equals(property.getNamespace()) && ScheduleCalendarTransp.NAME.equals(property.getName())) {
            /*
             * apply schedule transparency folder property
             */
            Transp value = TimeTransparency.OPAQUE;
            if (null != property.getElement() && null != property.getElement().getChild(Transp.TRANSPARENT.toLowerCase(), DAVProtocol.CAL_NS)) {
                value = TimeTransparency.TRANSPARENT;
            }
            CalendarFolderConverter.setExtendedProperty(folderToUpdate, CalendarFolderProperty.SCHEDULE_TRANSP(value));
        } else if (matches(property, CalendarTimezone.NAMESPACE, CalendarTimezone.NAME)) {
            throw new PreconditionException(DAVProtocol.DAV_NS.getURI(), "cannot-modify-protected-property", getUrl(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private static boolean matches(WebdavProperty property, Namespace namespace, String name) {
        return null != property && namespace.getURI().equals(property.getNamespace()) && name.equals(property.getName());
    }

}
