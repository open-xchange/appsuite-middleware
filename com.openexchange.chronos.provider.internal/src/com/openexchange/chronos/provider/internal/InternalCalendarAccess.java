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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.provider.internal;

import static com.openexchange.chronos.compat.Event2Appointment.asInt;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC_LITERAL;
import static com.openexchange.chronos.provider.internal.Constants.CONTENT_TYPE;
import static com.openexchange.chronos.provider.internal.Constants.QUALIFIED_ACCOUNT_ID;
import static com.openexchange.chronos.provider.internal.Constants.TREE_ID;
import static com.openexchange.chronos.provider.internal.Constants.USER_PROPERTY_PREFIX;
import static com.openexchange.folderstorage.CalendarFolderConverter.getStorageFolder;
import static com.openexchange.folderstorage.CalendarFolderConverter.getStorageType;
import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.QuotaAware;
import com.openexchange.chronos.provider.extensions.SearchAware;
import com.openexchange.chronos.provider.extensions.SyncAware;
import com.openexchange.chronos.provider.groupware.DefaultGroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarAccess;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.CalendarFolderConverter;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.java.util.TimeZones;
import com.openexchange.quota.Quota;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InternalCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.0
 */
public class InternalCalendarAccess implements GroupwareCalendarAccess, SyncAware, PersonalAlarmAware, SearchAware, QuotaAware {

    private final CalendarSession session;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link InternalCalendarAccess}.
     *
     * @param session The calendar session
     * @param services A service lookup reference
     */
    public InternalCalendarAccess(CalendarSession session, ServiceLookup services) {
        super();
        this.session = session;
        this.services = services;
    }

    @Override
    public void close() {
        //
    }

    @Override
    public GroupwareCalendarFolder getDefaultFolder() throws OXException {
        UserizedFolder folder = getFolderService().getDefaultFolder(ServerSessionAdapter.valueOf(session.getSession()).getUser(), TREE_ID, CONTENT_TYPE, PrivateType.getInstance(), session.getSession(), initDecorator());
        return getCalendarFolder(folder);
    }

    @Override
    public List<GroupwareCalendarFolder> getVisibleFolders(GroupwareFolderType type) throws OXException {
        return getCalendarFolders(getFolderService().getVisibleFolders(TREE_ID, CONTENT_TYPE, getStorageType(type), true, session.getSession(), initDecorator()));
    }

    @Override
    public List<CalendarFolder> getVisibleFolders() throws OXException {
        List<CalendarFolder> folders = new ArrayList<CalendarFolder>();
        for (GroupwareFolderType type : GroupwareFolderType.values()) {
            folders.addAll(getVisibleFolders(type));
        }
        return folders;
    }

    @Override
    public GroupwareCalendarFolder getFolder(String folderId) throws OXException {
        UserizedFolder folder = getFolderService().getFolder(TREE_ID, folderId, session.getSession(), initDecorator());
        return getCalendarFolder(folder);
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        getFolderService().deleteFolder(TREE_ID, folderId, new Date(clientTimestamp), session.getSession(), initDecorator());
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        ParameterizedFolder storageFolder = getStorageFolder(TREE_ID, QUALIFIED_ACCOUNT_ID, CONTENT_TYPE, folder);
        /*
         * update extended properties
         */
        if (null != folder.getExtendedProperties()) {
            updateProperties(getFolder(folderId), folder.getExtendedProperties());
        }
        /*
         * perform common folder update
         */
        getFolderService().updateFolder(storageFolder, new Date(clientTimestamp), session.getSession(), initDecorator());
        return storageFolder.getID();
    }

    @Override
    public String createFolder(String parentFolderId, CalendarFolder folder) throws OXException {
        /*
         * perform common folder create
         */
        ParameterizedFolder folderToCreate = getStorageFolder(TREE_ID, QUALIFIED_ACCOUNT_ID, CONTENT_TYPE, folder);
        folderToCreate.setParentID(parentFolderId);
        FolderResponse<String> response = getFolderService().createFolder(folderToCreate, session.getSession(), initDecorator());
        String folderId = response.getResponse();
        /*
         * update extended properties if needed
         */
        if (null != folder.getExtendedProperties()) {
            updateProperties(getFolder(folderId), folder.getExtendedProperties());
        }
        return folderId;
    }

    @Override
    public long getSequenceNumber(String folderId) throws OXException {
        return getCalendarService().getSequenceNumber(session, folderId);
    }

    @Override
    public Event getEvent(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        return getCalendarService().getEvent(session, folderId, new EventID(folderId, eventId, recurrenceId));
    }

    @Override
    public List<Event> getChangeExceptions(String folderId, String seriesId) throws OXException {
        return getCalendarService().getChangeExceptions(session, folderId, seriesId);
    }

    @Override
    public List<Event> getEvents(List<EventID> eventIDs) throws OXException {
        return getCalendarService().getEvents(session, eventIDs);
    }

    @Override
    public List<Event> getEventsInFolder(String folderId) throws OXException {
        return getCalendarService().getEventsInFolder(session, folderId);
    }

    @Override
    public List<Event> getEventsOfUser() throws OXException {
        return getCalendarService().getEventsOfUser(session);
    }

    @Override
    public UpdatesResult getUpdatedEventsInFolder(String folderId, long updatedSince) throws OXException {
        return getCalendarService().getUpdatedEventsInFolder(session, folderId, updatedSince);
    }

    @Override
    public UpdatesResult getUpdatedEventsOfUser(long updatedSince) throws OXException {
        return getCalendarService().getUpdatedEventsOfUser(session, updatedSince);
    }

    @Override
    public List<Event> resolveResource(String folderId, String resourceName) throws OXException {
        CalendarService calendarService = getCalendarService();
        String id = calendarService.getUtilities().resolveByUID(session, resourceName);
        if (null == id) {
            id = calendarService.getUtilities().resolveByFilename(session, resourceName);
        }
        if (null == id) {
            return null;
        }
        try {
            Event event = getCalendarService().getEvent(session, folderId, new EventID(folderId, id));
            List<Event> events = new ArrayList<Event>();
            events.add(event);
            if (CalendarUtils.isSeriesMaster(event)) {
                events.addAll(calendarService.getChangeExceptions(session, folderId, id));
            }
            return events;
        } catch (OXException e) {
            if ("CAL-4041".equals(e.getErrorCode())) {
                /*
                 * "Event not found in folder..." -> try to load detached occurrences
                 */
                List<Event> detachedOccurrences = calendarService.getChangeExceptions(session, folderId, id);
                if (0 < detachedOccurrences.size()) {
                    return detachedOccurrences;
                }
            }
        }
        return null;
    }

    @Override
    public List<Event> searchEvents(String[] folderIds, List<SearchFilter> filters, List<String> queries) throws OXException {
        return getCalendarService().searchEvents(session, folderIds, filters, queries);
    }

    @Override
    public CalendarResult createEvent(String folderId, Event event) throws OXException {
        return getCalendarService().createEvent(session, folderId, event);
    }

    @Override
    public CalendarResult updateEvent(EventID eventID, Event event, long clientTimestamp) throws OXException {
        return getCalendarService().updateEvent(session, eventID, event, clientTimestamp);
    }

    @Override
    public CalendarResult moveEvent(EventID eventID, String folderId, long clientTimestamp) throws OXException {
        return getCalendarService().moveEvent(session, eventID, folderId, clientTimestamp);
    }

    @Override
    public CalendarResult updateAttendee(EventID eventID, Attendee attendee, long clientTimestamp) throws OXException {
        return getCalendarService().updateAttendee(session, eventID, attendee, clientTimestamp);
    }

    @Override
    public CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException {
        return getCalendarService().updateAlarms(session, eventID, alarms, clientTimestamp);
    }

    @Override
    public CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException {
        return getCalendarService().deleteEvent(session, eventID, clientTimestamp);
    }

    @Override
    public Quota[] getQuotas() throws OXException {
        return getCalendarService().getUtilities().getQuotas(session);
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(Set<String> actions) throws OXException {
        return getCalendarService().getAlarmTrigger(session, actions);
    }

    @Override
    public IFileHolder getAttachment(EventID eventID, int managedId) throws OXException {
        return getCalendarService().getAttachment(session, eventID, managedId);
    }

    /**
     * Gets the folder service, throwing an appropriate exception in case the service is absent.
     *
     * @return The folder service
     */
    private FolderService getFolderService() throws OXException {
        return requireService(FolderService.class, services);
    }

    /**
     * Gets the calendar service.
     *
     * @return The calendar service
     */
    private CalendarService getCalendarService() throws OXException {
        return session.getCalendarService();
    }

    /**
     * Creates and initializes a folder service decorator ready to use with calls to the underlying folder service.
     *
     * @return A new folder service decorator
     */
    private FolderServiceDecorator initDecorator() throws OXException {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Connection connection = optConnection();
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.setLocale(session.getEntityResolver().getLocale(session.getUserId()));
        decorator.put("altNames", Boolean.TRUE.toString());
        decorator.setTimeZone(TimeZones.UTC);
        decorator.setAllowedContentTypes(Collections.<ContentType> singletonList(CONTENT_TYPE));
        return decorator;
    }

    private Connection optConnection() {
        return session.get(Connection.class.getName(), Connection.class);
    }

    /**
     * Gets a list of groupware calendar folders representing the userized folders in the supplied folder response.
     *
     * @param folderResponse The response from the folder service
     * @return The groupware calendar folders
     */
    private DefaultGroupwareCalendarFolder getCalendarFolder(UserizedFolder userizedFolder) throws OXException {
        DefaultGroupwareCalendarFolder calendarFolder = CalendarFolderConverter.getCalendarFolder(userizedFolder);
        calendarFolder.setExtendedProperties(getProperties(userizedFolder));
        return calendarFolder;
    }

    /**
     * Gets a list of groupware calendar folders representing the userized folders in the supplied folder response.
     *
     * @param folderResponse The response from the folder service
     * @return The groupware calendar folders
     */
    private List<GroupwareCalendarFolder> getCalendarFolders(FolderResponse<UserizedFolder[]> folderResponse) throws OXException {
        UserizedFolder[] folders = folderResponse.getResponse();
        if (null == folders || 0 == folders.length) {
            return Collections.emptyList();
        }
        List<GroupwareCalendarFolder> calendarFolders = new ArrayList<GroupwareCalendarFolder>(folders.length);
        for (UserizedFolder userizedFolder : folders) {
            calendarFolders.add(getCalendarFolder(userizedFolder));
        }
        return calendarFolders;
    }

    /**
     * Gets the extended calendar properties for a storage folder.
     *
     * @param folder The folder to get the extended calendar properties for
     * @return The extended properties
     */
    private ExtendedProperties getProperties(UserizedFolder folder) throws OXException {
        Map<String, String> userProperties = loadUserProperties(session.getContextId(), folder.getID(), session.getUserId());
        ExtendedProperties properties = new ExtendedProperties();
        /*
         * used for sync
         */
        if (folder.isDefault() && PrivateType.getInstance().equals(folder.getType())) {
            properties.add(USED_FOR_SYNC(true, true));
        } else if (userProperties.containsKey(USER_PROPERTY_PREFIX + USED_FOR_SYNC_LITERAL)) {
            properties.add(USED_FOR_SYNC(userProperties.get(USER_PROPERTY_PREFIX + USED_FOR_SYNC_LITERAL), false));
        } else {
            properties.add(USED_FOR_SYNC(true, false));
        }
        /*
         * schedule transparency
         */
        properties.add(SCHEDULE_TRANSP(TimeTransparency.OPAQUE, true));
        /*
         * color
         */
        String color = userProperties.get(USER_PROPERTY_PREFIX + COLOR_LITERAL);
        if (null == color) {
            Map<String, Object> meta = folder.getMeta();
            if (null != meta) {
                Object colorValue = meta.get("color");
                if (null != colorValue && String.class.isInstance(colorValue)) {
                    color = (String) colorValue;
                } else {
                    Object colorLabelValue = meta.get("color_label");
                    if (null != colorLabelValue && Integer.class.isInstance(colorLabelValue)) {
                        color = Appointment2Event.getColor(((Integer) colorLabelValue).intValue());
                    }
                }
            }
        }
        properties.add(COLOR(color, false));
        return properties;
    }

    /**
     * Updates extended calendar properties of a groupware calendar folder.
     *
     * @param originalFolder The original folder being updated
     * @param properties The properties as passed by the client
     */
    private void updateProperties(GroupwareCalendarFolder originalFolder, ExtendedProperties properties) throws OXException {
        ExtendedProperties originalProperties = originalFolder.getExtendedProperties();
        List<ExtendedProperty> propertiesToStore = new ArrayList<ExtendedProperty>();
        for (ExtendedProperty property : properties) {
            ExtendedProperty originalProperty = originalProperties.get(property.getName());
            if (null == originalProperty) {
                throw OXException.noPermissionForFolder();
            }
            if (originalProperty.equals(property)) {
                continue;
            }
            if (CalendarFolderProperty.isProtected(originalProperty)) {
                throw OXException.noPermissionForFolder();
            }
            propertiesToStore.add(property);
        }
        if (0 < propertiesToStore.size()) {
            Map<String, String> updatedProperties = new HashMap<String, String>(propertiesToStore.size());
            Set<String> removedProperties = new HashSet<String>();
            for (ExtendedProperty property : propertiesToStore) {
                String name = USER_PROPERTY_PREFIX + property.getName();
                if (null != property.getValue()) {
                    updatedProperties.put(name, property.getValue());
                } else {
                    removedProperties.add(name);
                }
            }
            removeUserProperties(session.getContextId(), originalFolder.getId(), session.getUserId(), removedProperties);
            storeUserProperties(session.getContextId(), originalFolder.getId(), session.getUserId(), updatedProperties);
        }
    }

    private Map<String, String> loadUserProperties(int contextId, String folderId, int userId) throws OXException {
        FolderUserPropertyStorage propertyStorage = requireService(FolderUserPropertyStorage.class, services);
        Connection connection = optConnection();
        if (null == connection) {
            return propertyStorage.getFolderProperties(contextId, asInt(folderId), userId);
        } else {
            return propertyStorage.getFolderProperties(contextId, asInt(folderId), userId, connection);
        }
    }

    private void storeUserProperties(int contextId, String folderId, int userId, Map<String, String> properties) throws OXException {
        if (null == properties || properties.isEmpty()) {
            return;
        }
        FolderUserPropertyStorage propertyStorage = requireService(FolderUserPropertyStorage.class, services);
        Connection connection = optConnection();
        if (null == connection) {
            propertyStorage.setFolderProperties(contextId, asInt(folderId), userId, properties);
        } else {
            propertyStorage.setFolderProperties(contextId, asInt(folderId), userId, properties, connection);
        }
    }

    private void removeUserProperties(int contextId, String folderId, int userId, Set<String> propertyNames) throws OXException {
        if (null == propertyNames || propertyNames.isEmpty()) {
            return;
        }
        FolderUserPropertyStorage propertyStorage = requireService(FolderUserPropertyStorage.class, services);
        Connection connection = optConnection();
        if (null == connection) {
            propertyStorage.deleteFolderProperties(contextId, asInt(folderId), userId, propertyNames);
        } else {
            propertyStorage.deleteFolderProperties(contextId, asInt(folderId), userId, propertyNames, connection);
        }
    }

}
