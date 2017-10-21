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

import static com.openexchange.chronos.provider.internal.Constants.CONTENT_TYPE;
import static com.openexchange.chronos.provider.internal.Constants.QUALIFIED_ACCOUNT_ID;
import static com.openexchange.chronos.provider.internal.Constants.TREE_ID;
import static com.openexchange.folderstorage.CalendarFolderConverter.getCalendarFolder;
import static com.openexchange.folderstorage.CalendarFolderConverter.getStorageFolder;
import static com.openexchange.folderstorage.CalendarFolderConverter.getStorageType;
import static com.openexchange.folderstorage.CalendarFolderField.COLOR;
import static com.openexchange.folderstorage.CalendarFolderField.SCHEDULE_TRANSP;
import static com.openexchange.folderstorage.CalendarFolderField.USED_FOR_SYNC;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.extensions.PersonalAlarmAware;
import com.openexchange.chronos.provider.extensions.QuotaAware;
import com.openexchange.chronos.provider.extensions.SearchAware;
import com.openexchange.chronos.provider.extensions.SyncAware;
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
import com.openexchange.folderstorage.CalendarFolderField;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderField;
import com.openexchange.folderstorage.FolderProperty;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.ParameterizedFolder;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.java.util.TimeZones;
import com.openexchange.quota.Quota;
import com.openexchange.server.ServiceExceptionCode;
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

    /** The logger */
    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(InternalCalendarAccess.class);

    private final CalendarSession session;

    /**
     * Initializes a new {@link InternalCalendarAccess}.
     *
     * @param session The calendar session
     */
    public InternalCalendarAccess(CalendarSession session) {
        super();
        this.session = session;
    }

    @Override
    public void close() {
        //
    }

    @Override
    public GroupwareCalendarFolder getDefaultFolder() throws OXException {
        UserizedFolder folder = getFolderService().getDefaultFolder(ServerSessionAdapter.valueOf(session.getSession()).getUser(), TREE_ID, CONTENT_TYPE, PrivateType.getInstance(), session.getSession(), initDecorator());
        return getCalendarFolder(folder, getFolderProperties(session.getContextId(), folder, session.getUserId(), false, optConnection()));
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
    public CalendarFolder getFolder(String folderId) throws OXException {
        UserizedFolder folder = getFolderService().getFolder(TREE_ID, folderId, session.getSession(), initDecorator());
        return getCalendarFolder(folder, getFolderProperties(session.getContextId(), folder, session.getUserId(), true, optConnection()));
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        getFolderService().deleteFolder(TREE_ID, folderId, new Date(clientTimestamp), session.getSession(), initDecorator());
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        ParameterizedFolder storageFolder = getStorageFolder(TREE_ID, QUALIFIED_ACCOUNT_ID, CONTENT_TYPE, folder);
        updateStoredFolderProperties(session.getContextId(), folderId, session.getUserId(), storageFolder.getProperties(), optConnection());
        getFolderService().updateFolder(storageFolder, new Date(clientTimestamp), session.getSession(), initDecorator());
        return storageFolder.getID();
    }

    @Override
    public String createFolder(String parentFolderId, CalendarFolder folder) throws OXException {
        ParameterizedFolder folderToCreate = getStorageFolder(TREE_ID, QUALIFIED_ACCOUNT_ID, CONTENT_TYPE, folder);
        folderToCreate.setParentID(parentFolderId);
        FolderResponse<String> response = getFolderService().createFolder(folderToCreate, session.getSession(), initDecorator());
        String folderId = response.getResponse();
        updateStoredFolderProperties(session.getContextId(), folderId, session.getUserId(), folderToCreate.getProperties(), optConnection());
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

    /**
     * Gets the folder service, throwing an appropriate exception in case the service is absent.
     *
     * @return The folder service
     */
    private FolderService getFolderService() throws OXException {
        FolderService folderService = Services.getService(FolderService.class);
        if (null == folderService) {
            throw ServiceExceptionCode.absentService(FolderService.class);
        }
        return folderService;
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
    private List<GroupwareCalendarFolder> getCalendarFolders(FolderResponse<UserizedFolder[]> folderResponse) {
        UserizedFolder[] folders = folderResponse.getResponse();
        if (null == folders || 0 == folders.length) {
            return Collections.emptyList();
        }
        List<GroupwareCalendarFolder> calendarFolders = new ArrayList<GroupwareCalendarFolder>(folders.length);
        for (UserizedFolder userizedFolder : folders) {
            calendarFolders.add(getCalendarFolder(userizedFolder,
                getFolderProperties(userizedFolder.getContext().getContextId(), userizedFolder, userizedFolder.getUser().getId(), true, optConnection())));
        }
        return calendarFolders;
    }

    @Override
    public List<AlarmTrigger> getAlarmTrigger(Set<String> actions) throws OXException {
        return getCalendarService().getAlarmTrigger(session, actions);
    }

    @Override
    public IFileHolder getAttachment(EventID eventID, int managedId) throws OXException {
        return getCalendarService().getAttachment(session, eventID, managedId);
    }

    /**
     * Get user specific properties for the folder
     *
     * @param folder The {@link Folder}
     * @param contextId The identifier of the context
     * @param userId The identifier of the user the folder belongs to
     * @param loadOwner If set to <code>true</code> the folder owners properties will be loaded
     * @param optConnection The optional database connection to use, or <code>null</code> if not available
     * @return The folder properties, or <code>null</code> if there are none
     */
    private static Map<FolderField, FolderProperty> getFolderProperties(int contextId, Folder folder, int userId, boolean loadOwner, Connection optConnection) {
        Map<FolderField, FolderProperty> folderProperties = new HashMap<FolderField, FolderProperty>();
        /*
         * add default folder properties (migrating legacy properties from "meta")
         */
        folderProperties.put(USED_FOR_SYNC, new FolderProperty(USED_FOR_SYNC.getName(), Boolean.TRUE));
        folderProperties.put(SCHEDULE_TRANSP, new FolderProperty(SCHEDULE_TRANSP.getName(), Transp.OPAQUE));
        Map<String, Object> meta = folder.getMeta();
        if (null != meta) {
            Object colorValue = meta.get("color");
            if (null != colorValue && String.class.isInstance(colorValue)) {
                folderProperties.put(COLOR, new FolderProperty(COLOR.getName(), colorValue));
            }
            Object colorLabelValue = meta.get("color_label");
            if (null != colorLabelValue && Integer.class.isInstance(colorLabelValue)) {
                folderProperties.put(COLOR, new FolderProperty(COLOR.getName(), Appointment2Event.getColor(((Integer) colorLabelValue).intValue())));
            }
        }
        /*
         * load and apply stored folder properties
         */
        Map<String, String> properties = getStoredFolderProperties(contextId, folder, userId, loadOwner, optConnection);
        if (null != properties && 0 < properties.size()) {
            for (Entry<String, String> entry : properties.entrySet()) {
                String name = entry.getKey();
                if (COLOR.getName().equals(name)) {
                    folderProperties.put(COLOR, new FolderProperty(name, entry.getValue()));
                } else if (USED_FOR_SYNC.getName().equals(name)) {
                    folderProperties.put(USED_FOR_SYNC, new FolderProperty(name, Boolean.valueOf(entry.getValue())));
                } else if (SCHEDULE_TRANSP.getName().equals(name)) {
                    folderProperties.put(SCHEDULE_TRANSP, new FolderProperty(name, entry.getValue()));
                }
            }
        }
        return folderProperties;
    }

    private static void updateStoredFolderProperties(int contextId, String folderId, int userId, Map<FolderField, FolderProperty> properties, Connection optConnection) throws OXException {

        // TODO batch operations, compare with existing props

        if (null == properties || properties.isEmpty()) {
            return;
        }
        FolderUserPropertyStorage propertyStorage = Services.getService(FolderUserPropertyStorage.class);
        if (null == propertyStorage) {
            throw ServiceExceptionCode.absentService(FolderUserPropertyStorage.class);
        }
        int folder;
        try {
            folder = Integer.parseInt(folderId);
        } catch (NumberFormatException e) {
            throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(e, folderId, "");
        }
        for (Entry<FolderField, FolderProperty> entry : properties.entrySet()) {
            if (false == CalendarFolderField.getValues().contains(entry.getKey())) {
                continue;
            }
            String key = entry.getKey().getName();
            if (null == entry.getValue() || null == entry.getValue().getValue()) {
                propertyStorage.deleteFolderProperty(contextId, folder, userId, key);
            } else {
                String value = String.valueOf(entry.getValue().getValue());
                propertyStorage.setFolderProperties(contextId, folder, userId, Collections.singletonMap(key, value), optConnection);
            }
        }
    }

    /**
     * Get user specific properties for the folder
     *
     * @param folder The {@link Folder}
     * @param contextId The identifier of the context
     * @param userId The identifier of the user the folder belongs to
     * @param loadOwner If set to <code>true</code> the folder owners properties will be loaded
     * @param optConnection The optional database connection to use, or <code>null</code> if not available
     * @return {@link Collections#emptyMap()} or a {@link Map} with user-specific properties
     */
    private static Map<String, String> getStoredFolderProperties(int contextId, Folder folder, int userId, boolean loadOwner, Connection optConnection) {
        Map<String, String> properties;
        FolderUserPropertyStorage fps = Services.optService(FolderUserPropertyStorage.class);
        if (null != fps) {
            try {
                if (null != optConnection) {
                    properties = fps.getFolderProperties(contextId, Integer.valueOf(folder.getID()).intValue(), userId, optConnection);
                } else {
                    properties = fps.getFolderProperties(contextId, Integer.valueOf(folder.getID()).intValue(), userId);
                }
                // Check if we can fall-back to owner properties
                if (loadOwner && folder.getCreatedBy() != userId) {
                    // Try to load owner properties
                    Map<String, String> ownerProperties = fps.getFolderProperties(contextId, Integer.valueOf(folder.getID()).intValue(), folder.getCreatedBy());
                    for (String key : ownerProperties.keySet()) {
                        if (false == properties.containsKey(key)) {
                            properties.put(key, ownerProperties.get(key));
                        }
                    }
                }
                return properties;
            } catch (OXException e) {
                LOGGER.error("Could not get user properties for folder {}", folder.getID(), e);
            }
        }
        properties = Collections.emptyMap();
        return properties;
    }

}
