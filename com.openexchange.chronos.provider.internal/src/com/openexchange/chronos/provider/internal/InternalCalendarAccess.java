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

package com.openexchange.chronos.provider.internal;

import static com.openexchange.chronos.compat.Event2Appointment.asInt;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR;
import static com.openexchange.chronos.provider.CalendarFolderProperty.COLOR_LITERAL;
import static com.openexchange.chronos.provider.CalendarFolderProperty.SCHEDULE_TRANSP;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC;
import static com.openexchange.chronos.provider.CalendarFolderProperty.USED_FOR_SYNC_LITERAL;
import static com.openexchange.chronos.provider.internal.Constants.ACCOUNT_ID;
import static com.openexchange.chronos.provider.internal.Constants.CONTENT_TYPE;
import static com.openexchange.chronos.provider.internal.Constants.PRIVATE_FOLDER_ID;
import static com.openexchange.chronos.provider.internal.Constants.PUBLIC_FOLDER_ID;
import static com.openexchange.chronos.provider.internal.Constants.SHARED_FOLDER_ID;
import static com.openexchange.chronos.provider.internal.Constants.TREE_ID;
import static com.openexchange.chronos.provider.internal.Constants.USER_PROPERTY_PREFIX;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_CONNECTION;
import static com.openexchange.folderstorage.CalendarFolderConverter.getStorageFolder;
import static com.openexchange.osgi.Tools.requireService;
import java.sql.Connection;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.dmfs.rfc5545.DateTime;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.CalendarFolderProperty;
import com.openexchange.chronos.provider.extensions.FolderSearchAware;
import com.openexchange.chronos.provider.extensions.FolderSyncAware;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.QuotaAware;
import com.openexchange.chronos.provider.extensions.SubscribeAware;
import com.openexchange.chronos.provider.extensions.WarningsAware;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.chronos.provider.groupware.DefaultGroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarAccess;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarFolder;
import com.openexchange.chronos.provider.groupware.GroupwareFolderType;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.ImportResult;
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
import com.openexchange.java.Collators;
import com.openexchange.java.util.TimeZones;
import com.openexchange.quota.Quota;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.oxfolder.property.FolderUserPropertyStorage;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link InternalCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class InternalCalendarAccess implements FolderCalendarAccess, SubscribeAware, GroupwareCalendarAccess, FolderSyncAware, PersonalAlarmAware, FolderSearchAware, QuotaAware, WarningsAware {

    private final CalendarSession session;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link InternalCalendarAccess}.
     *
     * @param session The calendar session
     * @param account The calendar account
     * @param services A service lookup reference
     */
    public InternalCalendarAccess(CalendarSession session, CalendarAccount account, ServiceLookup services) {
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
        //        return getCalendarFolders(getFolderService().getVisibleFolders(TREE_ID, CONTENT_TYPE, getStorageType(type), true, session.getSession(), initDecorator()));
        switch (type) {
            case PRIVATE:
                return getCalendarFolders(getSubfoldersRecursively(getFolderService(), initDecorator(), PRIVATE_FOLDER_ID));
            case SHARED:
                return getCalendarFolders(getSubfoldersRecursively(getFolderService(), initDecorator(), SHARED_FOLDER_ID));
            case PUBLIC:
                return getCalendarFolders(getSubfoldersRecursively(getFolderService(), initDecorator(), PUBLIC_FOLDER_ID));
            default:
                throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(Constants.PROVIDER_ID);
        }
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
        /*
         * update extended properties & subscribed flag as needed; 'hide' the change in folder update afterwards
         */
        GroupwareCalendarFolder originalFolder = getFolder(folderId);
        if (null != folder.getExtendedProperties()) {
            updateProperties(originalFolder, folder.getExtendedProperties());
            DefaultGroupwareCalendarFolder folderUpdate = new DefaultGroupwareCalendarFolder(folder);
            folderUpdate.setExtendedProperties(null);
            folder = folderUpdate;
        }
        if (originalFolder.isSubscribed() != folder.isSubscribed()) {
            if (originalFolder.isDefaultFolder() && GroupwareFolderType.PRIVATE.equals(originalFolder.getType())) {
                throw OXException.noPermissionForFolder();
            }
            Map<String, String> properties = Collections.singletonMap(USER_PROPERTY_PREFIX + "subscribed", String.valueOf(folder.isSubscribed()));
            storeUserProperties(session.getContextId(), folderId, session.getUserId(), properties);
            DefaultGroupwareCalendarFolder folderUpdate = new DefaultGroupwareCalendarFolder(folder);
            folderUpdate.setSubscribed(true);
            folder = folderUpdate;
        }
        /*
         * perform common folder update
         */
        ParameterizedFolder storageFolder = getStorageFolder(TREE_ID, CONTENT_TYPE, folder, null, ACCOUNT_ID, null);
        getFolderService().updateFolder(storageFolder, new Date(clientTimestamp), session.getSession(), initDecorator());
        return storageFolder.getID();
    }

    @Override
    public String createFolder(CalendarFolder folder) throws OXException {
        /*
         * perform common folder create (excluding extended properties & subscribe flag)
         */
        String folderId;
        {
            DefaultGroupwareCalendarFolder plainFolder = new DefaultGroupwareCalendarFolder(folder);
            plainFolder.setExtendedProperties(null);
            plainFolder.setSubscribed(true);
            ParameterizedFolder folderToCreate = getStorageFolder(TREE_ID, CONTENT_TYPE, plainFolder, null, ACCOUNT_ID, null);
            FolderResponse<String> response = getFolderService().createFolder(folderToCreate, session.getSession(), initDecorator());
            folderId = response.getResponse();
        }
        /*
         * insert extended properties & subscribed flag if needed
         */
        if (null != folder.getExtendedProperties()) {
            updateProperties(getFolder(folderId), folder.getExtendedProperties());
        }
        if (false == folder.isSubscribed()) {
            Map<String, String> properties = Collections.singletonMap(USER_PROPERTY_PREFIX + "subscribed", String.valueOf(folder.isSubscribed()));
            storeUserProperties(session.getContextId(), folderId, session.getUserId(), properties);
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
    public Map<String, EventsResult> getEventsInFolders(List<String> folderIds) throws OXException {
        return getCalendarService().getEventsInFolders(session, folderIds);
    }

    @Override
    public List<Event> getEventsOfUser() throws OXException {
        return getCalendarService().getEventsOfUser(session);
    }

    @Override
    public List<Event> getEventsOfUser(Boolean rsvp, ParticipationStatus[] partStats) throws OXException {
        return getCalendarService().getEventsOfUser(session, rsvp, partStats);
    }

    @Override
    public Event resolveEvent(String eventId) throws OXException {
        return getCalendarService().getUtilities().resolveByID(session, eventId);
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
        return getCalendarService().getUtilities().resolveResource(session, folderId, resourceName);
    }

    @Override
    public Map<String, EventsResult> resolveResources(String folderId, List<String> resourceNames) throws OXException {
        return getCalendarService().getUtilities().resolveResources(session, folderId, resourceNames);
    }

    @Override
    public Map<String, EventsResult> searchEvents(List<String> folderIds, List<SearchFilter> filters, List<String> queries) throws OXException {
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
    public CalendarResult updateAttendee(EventID eventID, Attendee attendee, List<Alarm> alarms, long clientTimestamp) throws OXException {
        return getCalendarService().updateAttendee(session, eventID, attendee, alarms, clientTimestamp);
    }

    @Override
    public CalendarResult updateAlarms(EventID eventID, List<Alarm> alarms, long clientTimestamp) throws OXException {
        return getCalendarService().updateAlarms(session, eventID, alarms, clientTimestamp);
    }
    
    @Override
    public CalendarResult changeOrganizer(EventID eventID, Organizer organizer, long clientTimestamp) throws OXException {
        return getCalendarService().changeOrganizer(session, eventID, organizer, clientTimestamp);
    }

    @Override
    public CalendarResult deleteEvent(EventID eventID, long clientTimestamp) throws OXException {
        return getCalendarService().deleteEvent(session, eventID, clientTimestamp);
    }

    @Override
    public CalendarResult splitSeries(EventID eventID, DateTime splitPoint, String uid, long clientTimestamp) throws OXException {
        return getCalendarService().splitSeries(session, eventID, splitPoint, uid, clientTimestamp);
    }

    @Override
    public List<ImportResult> importEvents(String folderId, List<Event> events) throws OXException {
        return getCalendarService().importEvents(session, folderId, events);
    }

    @Override
    public Quota[] getQuotas() throws OXException {
        return getCalendarService().getUtilities().getQuotas(session);
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(Set<String> actions) throws OXException {
        return getCalendarService().getAlarmTriggers(session, actions);
    }

    @Override
    public IFileHolder getAttachment(EventID eventID, int managedId) throws OXException {
        return getCalendarService().getAttachment(session, eventID, managedId);
    }

    @Override
    public List<OXException> getWarnings() {
        return session.getWarnings();
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
    private CalendarService getCalendarService() {
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
        return session.get(PARAMETER_CONNECTION(), Connection.class);
    }

    /**
     * Gets a list of groupware calendar folders representing the userized folders in the supplied folder response.
     *
     * @param folderResponse The response from the folder service
     * @return The groupware calendar folders
     */
    private DefaultGroupwareCalendarFolder getCalendarFolder(UserizedFolder userizedFolder) throws OXException {
        /*
         * convert to calendar folder
         */
        DefaultGroupwareCalendarFolder calendarFolder = CalendarFolderConverter.getCalendarFolder(userizedFolder);
        /*
         * apply further extended properties & capabilities
         */
        Map<String, String> userProperties = loadUserProperties(session.getContextId(), userizedFolder.getID(), session.getUserId());
        calendarFolder.setExtendedProperties(getExtendedProperties(userProperties, userizedFolder));
        calendarFolder.setSupportedCapabilites(CalendarCapability.getCapabilities(getClass()));
        String subscribed = userProperties.get(USER_PROPERTY_PREFIX + "subscribed");
        calendarFolder.setSubscribed(null == subscribed || Boolean.parseBoolean(subscribed));
        return calendarFolder;
    }

    /**
     * Gets a list of groupware calendar folders representing the folders in the supplied userized folders.
     *
     * @param folders The folders from the folder service
     * @return The groupware calendar folders
     */
    private List<GroupwareCalendarFolder> getCalendarFolders(List<UserizedFolder> folders) throws OXException {
        if (null == folders || 0 == folders.size()) {
            return Collections.emptyList();
        }
        List<GroupwareCalendarFolder> calendarFolders = new ArrayList<GroupwareCalendarFolder>(folders.size());
        for (UserizedFolder userizedFolder : folders) {
            calendarFolders.add(getCalendarFolder(userizedFolder));
        }
        return sort(calendarFolders, session.getEntityResolver().getLocale(session.getUserId()));
    }

    private List<GroupwareCalendarFolder> sort(List<GroupwareCalendarFolder> calendarFolders, Locale locale) {
        if (null == calendarFolders || 2 > calendarFolders.size()) {
            return calendarFolders;
        }
        Collator collator = Collators.getSecondaryInstance(locale);
        calendarFolders.sort(new Comparator<GroupwareCalendarFolder>() {

            @Override
            public int compare(GroupwareCalendarFolder folder1, GroupwareCalendarFolder folder2) {
                if (folder1.isDefaultFolder() != folder2.isDefaultFolder()) {
                    /*
                     * default folders first
                     */
                    return folder1.isDefaultFolder() ? -1 : 1;
                }
                /*
                 * compare folder names, otherwise
                 */
                return collator.compare(folder1.getName(), folder2.getName());
            }
        });
        return calendarFolders;
    }

    /**
     * Gets the extended calendar properties for a storage folder.
     *
     * @param folder The folder to get the extended calendar properties for
     * @return The extended properties
     */
    private static ExtendedProperties getExtendedProperties(Map<String, String> userProperties, UserizedFolder folder) {
        ExtendedProperties properties = new ExtendedProperties();
        /*
         * used for sync
         */
        if (folder.isDefault() && PrivateType.getInstance().equals(folder.getType())) {
            properties.add(USED_FOR_SYNC(Boolean.TRUE, true));
        } else if (userProperties.containsKey(USER_PROPERTY_PREFIX + USED_FOR_SYNC_LITERAL)) {
            properties.add(USED_FOR_SYNC(userProperties.get(USER_PROPERTY_PREFIX + USED_FOR_SYNC_LITERAL), false));
        } else {
            properties.add(USED_FOR_SYNC(Boolean.TRUE, false));
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
                    if (false == String.class.isInstance(property.getValue())) {
                        throw OXException.noPermissionForFolder();
                    }
                    updatedProperties.put(name, (String) property.getValue());
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
        }
        return propertyStorage.getFolderProperties(contextId, asInt(folderId), userId, connection);
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

    /**
     * Collects all calendar subfolders from a parent folder recursively.
     *
     * @param folderService A reference to the folder service
     * @param decorator The optional folder service decorator to use
     * @param parentId The parent folder identifier to get the subfolders from
     * @return The collected subfolders, or an empty list if there are none
     */
    private List<UserizedFolder> getSubfoldersRecursively(FolderService folderService, FolderServiceDecorator decorator, String parentId) throws OXException {
        UserizedFolder[] subfolders = folderService.getSubfolders(TREE_ID, parentId, false, session.getSession(), decorator).getResponse();
        if (null == subfolders || 0 == subfolders.length) {
            return Collections.emptyList();
        }
        List<UserizedFolder> allFolders = new ArrayList<UserizedFolder>();
        for (UserizedFolder subfolder : subfolders) {
            if (CONTENT_TYPE.equals(subfolder.getContentType())) {
                allFolders.add(subfolder);
            }
            if (subfolder.hasSubscribedSubfolders()) {
                allFolders.addAll(getSubfoldersRecursively(folderService, decorator, subfolder.getID()));
            }
        }
        return allFolders;
    }

}
