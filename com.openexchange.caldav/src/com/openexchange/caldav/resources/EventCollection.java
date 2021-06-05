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

package com.openexchange.caldav.resources;

import static com.openexchange.caldav.Tools.getSignificantEvent;
import static com.openexchange.caldav.Tools.getSignificantEvents;
import static com.openexchange.dav.DAVProtocol.protocolException;
import static com.openexchange.folderstorage.CalendarFolderConverter.optCalendarProvider;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.caldav.CaldavProtocol;
import com.openexchange.caldav.GroupwareCaldavFactory;
import com.openexchange.caldav.PhantomMaster;
import com.openexchange.caldav.mixins.AllowedSharingModes;
import com.openexchange.caldav.mixins.CalendarOrder;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDate;
import com.openexchange.caldav.mixins.DefaultAlarmVeventDatetime;
import com.openexchange.caldav.mixins.Invite;
import com.openexchange.caldav.mixins.RefreshRate;
import com.openexchange.caldav.mixins.Source;
import com.openexchange.caldav.mixins.SubscribedStripAlarms;
import com.openexchange.caldav.mixins.SubscribedStripAttachments;
import com.openexchange.caldav.mixins.SubscribedStripTodos;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSet;
import com.openexchange.caldav.mixins.SupportedCalendarComponentSets;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.SelfProtectionFactory;
import com.openexchange.chronos.provider.CalendarProviders;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.mixins.CalendarDescription;
import com.openexchange.dav.mixins.ScheduleCalendarTransp;
import com.openexchange.dav.reports.SyncStatus;
import com.openexchange.dav.resources.SyncToken;
import com.openexchange.dav.resources.SyncToken.Flag;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.java.Strings;
import com.openexchange.user.User;
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
public class EventCollection extends CalDAVFolderCollection<Event> {

    /** Fields that are always retrieved when requesting event lists from the service */
    protected static final EventField[] BASIC_FIELDS = new EventField[] {
        EventField.UID, EventField.FILENAME, EventField.FOLDER_ID, EventField.ID, EventField.SERIES_ID, EventField.SEQUENCE,
        EventField.CREATED, EventField.CREATED_BY, EventField.TIMESTAMP, EventField.LAST_MODIFIED, EventField.MODIFIED_BY,
        EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE
    };

    protected final String folderID;

    private String syncToken;

    /**
     * Initializes a new {@link EventCollection}.
     *
     * @param factory The factory
     * @param url The WebDAV path
     * @param folder The underlying folder
     * @param order The calendar order to use, or {@value CalendarOrder#NO_ORDER} for no specific order
     */
    public EventCollection(GroupwareCaldavFactory factory, WebdavPath url, UserizedFolder folder, int order) throws OXException {
        super(factory, url, folder, order);
        this.folderID = folder.getID();
        includeProperties(
            new AllowedSharingModes(this),
            new CalendarDescription(this),
            new ScheduleCalendarTransp(this),
            new SupportedCalendarComponentSet(SupportedCalendarComponentSet.VEVENT),
            new SupportedCalendarComponentSets(SupportedCalendarComponentSets.VEVENT),
            new DefaultAlarmVeventDate(),
            new DefaultAlarmVeventDatetime()
        );
        if (isSubscription(folder)) {
            includeProperties(
                new RefreshRate(this),
                new Source(this),
                new SubscribedStripAlarms(),
                new SubscribedStripAttachments(),
                new SubscribedStripTodos()
            );
        }
        if (supportsPermissions(folder)) {
            includeProperties(new Invite(factory, this));
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
    public String getSyncToken() throws WebdavProtocolException {
        if (null == syncToken) {
            try {
                syncToken = new CalendarAccessOperation<String>(factory) {

                    @Override
                    protected String perform(IDBasedCalendarAccess access) throws OXException {
                        return String.valueOf(access.getSequenceNumbers(Collections.singletonList(folderID)).get(folderID));
                    }
                }.execute(factory.getSession());
            } catch (OXException e) {
                throw protocolException(getUrl(), e);
            }
        }
        return syncToken;
    }

    private static boolean isSubscription(UserizedFolder folder) {
        return CalendarProviders.ID_ICAL.equals(optCalendarProvider(folder));
    }

    @Override
    public String getResourceType() throws WebdavProtocolException {
        StringBuilder stringBuilder = new StringBuilder(DAVProtocol.COLLECTION);
        if (null != folder) {
            if (isSubscription(folder)) {
                stringBuilder.append('<').append(DAVProtocol.CALENDARSERVER_NS.getPrefix()).append(":subscribed/>");
            } else {
                stringBuilder.append('<').append(CaldavProtocol.CAL_NS.getPrefix()).append(":calendar/>");
            }
            if (supportsPermissions(folder)) {
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
        }
        return stringBuilder.toString();
    }

    @Override
    protected Collection<Event> getObjects(Date rangeStart, Date rangeEnd) throws OXException {
        return getSignificantEvents(new CalendarAccessOperation<List<Event>>(factory) {

            @Override
            protected List<Event> perform(IDBasedCalendarAccess access) throws OXException {
                access.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
                access.set(CalendarParameters.PARAMETER_RANGE_START, rangeStart);
                access.set(CalendarParameters.PARAMETER_RANGE_END, rangeEnd);
                return access.getEventsInFolder(folderID);
            }
        }.execute(factory.getSession()));
    }

    @Override
    protected Collection<Event> getObjects() throws OXException {
        return getSignificantEvents(new CalendarAccessOperation<List<Event>>(factory) {

            @Override
            protected List<Event> perform(IDBasedCalendarAccess access) throws OXException {
                access.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
                access.set(CalendarParameters.PARAMETER_RANGE_START, minDateTime.getMinDateTime());
                access.set(CalendarParameters.PARAMETER_RANGE_END, maxDateTime.getMaxDateTime());
                return access.getEventsInFolder(folderID);
            }
        }.execute(factory.getSession()));
    }

    @Override
    protected Event getObject(String resourceName) throws OXException {
        if (Strings.isEmpty(resourceName)) {
            return null;
        }
        return getSignificantEvent(new CalendarAccessOperation<List<Event>>(factory) {

            @Override
            protected List<Event> perform(IDBasedCalendarAccess access) throws OXException {
                access.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
                return access.resolveResource(folderID, resourceName);
            }
        }.execute(factory.getSession()));
    }

    public Map<String, EventsResult> resolveEvents(List<String> resourceNames) throws OXException {
        return new CalendarAccessOperation<Map<String, EventsResult>>(factory) {

            @Override
            protected Map<String, EventsResult> perform(IDBasedCalendarAccess access) throws OXException {
                access.set(CalendarParameters.PARAMETER_FIELDS, BASIC_FIELDS);
                return access.resolveResources(folderID, resourceNames);
            }
        }.execute(factory.getSession());
    }

    @Override
    public AbstractResource createResource(Event object, WebdavPath url) throws OXException {
        return new EventResource(this, object, url);
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

    static final EventField[] SYNC_STATUS_FIELDS = {
        EventField.ID, EventField.UID, EventField.FILENAME, EventField.TIMESTAMP, EventField.CREATED, EventField.LAST_MODIFIED,
        EventField.SERIES_ID, EventField.RECURRENCE_ID
    };

    private EventResource getEventResource(List<Event> resolvedEvents) {
        Event event = resolvedEvents.get(0);
        if (CalendarUtils.isSeriesException(event)) {
            event = new PhantomMaster(resolvedEvents);
        }
        return new EventResource(this, event, constructPathForChildResource(event));
    }

    @Override
    protected SyncStatus<WebdavResource> getSyncStatus(SyncToken syncToken) throws OXException {
        return getSyncStatus(syncToken, -1);
    }

    @Override
    protected SyncStatus<WebdavResource> getSyncStatus(SyncToken syncToken, int limit) throws OXException {
        SyncStatus<WebdavResource> syncStatus = new SyncStatus<WebdavResource>();
        /*
         * get new, modified & deleted objects since client token within synchronized interval
         */
        UpdatesResult updates = new CalendarAccessOperation<UpdatesResult>(factory) {

            @Override
            protected UpdatesResult perform(IDBasedCalendarAccess access) throws OXException {
                int maxResults = getMaxResults();
                if (0 < limit) {
                    maxResults = 0 < maxResults ? Math.min(maxResults, limit) : limit;
                }
                if (syncToken.isInitial()) {
                    access.set(CalendarParameters.PARAMETER_IGNORE, new String[] { "deleted" }); // exclude deleted events for initial sync
                }
                access.set(CalendarParameters.PARAMETER_FIELDS, SYNC_STATUS_FIELDS);
                access.set(CalendarParameters.PARAMETER_RANGE_START, minDateTime.getMinDateTime());
                access.set(CalendarParameters.PARAMETER_RANGE_END, maxDateTime.getMaxDateTime());
                access.set(CalendarParameters.PARAMETER_RIGHT_HAND_LIMIT, I(maxResults));
                return access.getUpdatedEventsInFolder(folderID, syncToken.getTimestamp());
            }
        }.execute(factory.getSession());
        /*
         * add sync status for each new and modified event, grouped by UID as needed & determine maximum timestamp
         */
        Map<String, List<Event>> newAndModifiedEventsByUID = CalendarUtils.getEventsByUID(updates.getNewAndModifiedEvents(), false);
        for (List<Event> value : newAndModifiedEventsByUID.values()) {
            EventResource resource = getEventResource(CalendarUtils.sortSeriesMasterFirst(value));
            int status = syncToken.isInitial() || null != resource.getCreationDate() && resource.getCreationDate().getTime() > syncToken.getTimestamp() ? HttpServletResponse.SC_CREATED : HttpServletResponse.SC_OK;
            syncStatus.addStatus(new WebdavStatusImpl<WebdavResource>(status, resource.getUrl(), resource));
        }
        /*
         * add sync status for each deleted event, grouped by UID as needed & determine maximum timestamp
         */
        for (Entry<String, List<Event>> entry : CalendarUtils.getEventsByUID(updates.getDeletedEvents(), false).entrySet()) {
            if (newAndModifiedEventsByUID.keySet().contains(entry.getKey())) {
                continue; // skip previously moved events
            }
            EventResource resource = getEventResource(CalendarUtils.sortSeriesMasterFirst(entry.getValue()));
            syncStatus.addStatus(new WebdavStatusImpl<WebdavResource>(HttpServletResponse.SC_NOT_FOUND, resource.getUrl(), resource));
        }
        /*
         * additionally add HTTP 507 status and mark sync token accordingly to indicate truncated results if required
         */
        SyncToken nextSyncToken;
        if (updates.isTruncated()) {
            syncStatus.addStatus(new WebdavStatusImpl<WebdavResource>(DAVProtocol.SC_INSUFFICIENT_STORAGE, getUrl(), this));
            nextSyncToken = new SyncToken(updates.getTimestamp(), null, Flag.TRUNCATED);
        } else {
            nextSyncToken = new SyncToken(updates.getTimestamp());
        }
        /*
         * set next sync token (as maximum timestamp) & return result
         */
        syncStatus.setToken(nextSyncToken.toString());
        return syncStatus;
    }

    @Override
    protected void internalDelete() throws WebdavProtocolException {
        if (null != folder && null != folder.getOwnPermission() && false == folder.getOwnPermission().isAdmin() &&
            null != folder.getSupportedCapabilities() && folder.getSupportedCapabilities().contains("subscribe") && folder.isSubscribed()) {
            /*
             * treat as "unsubscribe" request of shared calendar
             */
            getFolderToUpdate().setSubscribed(false);
            save();
        } else {
            super.internalDelete();
        }
    }

    int getMaxResults() {
        try {
            return caldavFactory.requireService(LeanConfigurationService.class).getIntProperty(SelfProtectionFactory.PROPERTY_EVENT_LIMIT);
        } catch (OXException e) {
            LOG.warn("Missing LeanConfigurationService. Falling back to default value", e);
            return i((Integer) SelfProtectionFactory.PROPERTY_EVENT_LIMIT.getDefaultValue());
        }
    }

}
