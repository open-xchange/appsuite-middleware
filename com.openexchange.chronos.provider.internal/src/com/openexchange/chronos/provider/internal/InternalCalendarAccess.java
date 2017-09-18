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
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.FreeBusyTime;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.provider.CalendarFolder;
import com.openexchange.chronos.provider.FreeBusyAwareCalendarAccess;
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
import com.openexchange.chronos.service.EventConflict;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.chronos.service.FreeBusyService;
import com.openexchange.chronos.service.SearchFilter;
import com.openexchange.chronos.service.UpdatesResult;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderServiceDecorator;
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
 * @since v7.10.0
 */
public class InternalCalendarAccess implements GroupwareCalendarAccess, FreeBusyAwareCalendarAccess, SyncAware, PersonalAlarmAware, SearchAware, QuotaAware {

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
        Folder folder = getFolderService().getDefaultFolder(ServerSessionAdapter.valueOf(session.getSession()).getUser(), TREE_ID, CONTENT_TYPE, PrivateType.getInstance(), session.getSession(), initDecorator());
        return getCalendarFolder(folder, getFolderProperties(session.getContextId(), folder, session.getUserId(), false));
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
        Folder folder = getFolderService().getFolder(TREE_ID, folderId, session.getSession(), initDecorator());
        return getCalendarFolder(folder, getFolderProperties(session.getContextId(), folder, session.getUserId(), true));
    }

    @Override
    public void deleteFolder(String folderId, long clientTimestamp) throws OXException {
        getFolderService().deleteFolder(TREE_ID, folderId, new Date(clientTimestamp), session.getSession(), initDecorator());
    }

    @Override
    public String updateFolder(String folderId, CalendarFolder folder, long clientTimestamp) throws OXException {
        Folder storageFolder = getStorageFolder(TREE_ID, QUALIFIED_ACCOUNT_ID, CONTENT_TYPE, folder);
        getFolderService().updateFolder(storageFolder, new Date(clientTimestamp), session.getSession(), initDecorator());
        return storageFolder.getID();
    }

    @Override
    public String createFolder(String parentFolderId, CalendarFolder folder) throws OXException {
        Folder folderToCreate = getStorageFolder(TREE_ID, QUALIFIED_ACCOUNT_ID, CONTENT_TYPE, folder);
        folderToCreate.setParentID(parentFolderId);
        FolderResponse<String> response = getFolderService().createFolder(folderToCreate, session.getSession(), initDecorator());
        return response.getResponse();
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
     * Gets the free busy service.
     *
     * @return The {@link FreeBusyService}
     */
    private FreeBusyService getFreeBusyService() throws OXException {
        return session.getFreeBusyService();
    }

    /**
     * Creates and initializes a folder service decorator ready to use with calls to the underlying folder service.
     *
     * @return A new folder service decorator
     */
    private FolderServiceDecorator initDecorator() throws OXException {
        FolderServiceDecorator decorator = new FolderServiceDecorator();
        Connection connection = session.get(Connection.class.getName(), Connection.class);
        //        Object connection = session.getSession().getParameter(Connection.class.getName());
        if (null != connection) {
            decorator.put(Connection.class.getName(), connection);
        }
        decorator.setLocale(session.getEntityResolver().getLocale(session.getUserId()));
        decorator.put("altNames", Boolean.TRUE.toString());
        decorator.setTimeZone(TimeZones.UTC);
        decorator.setAllowedContentTypes(Collections.<ContentType> singletonList(CONTENT_TYPE));
        return decorator;
    }

    /**
     * Gets a list of groupware calendar folders representing the userized folders in the supplied folder response.
     *
     * @param folderResponse The response from the folder service
     * @return The groupware calendar folders
     */
    public static List<GroupwareCalendarFolder> getCalendarFolders(FolderResponse<UserizedFolder[]> folderResponse) {
        UserizedFolder[] folders = folderResponse.getResponse();
        if (null == folders || 0 == folders.length) {
            return Collections.emptyList();
        }
        List<GroupwareCalendarFolder> calendarFolders = new ArrayList<GroupwareCalendarFolder>(folders.length);
        for (UserizedFolder userizedFolder : folders) {
            calendarFolders.add(getCalendarFolder(userizedFolder, getFolderProperties(userizedFolder.getContext().getContextId(), userizedFolder, userizedFolder.getUser().getId(), true)));
        }
        return calendarFolders;
    }

    @Override
    public boolean[] hasEventsBetween(Date from, Date until) throws OXException {
        return getFreeBusyService().hasEventsBetween(session, from, until);
    }

    @Override
    public Map<Attendee, List<Event>> getFreeBusy(List<Attendee> attendees, Date from, Date until) throws OXException {
        return getFreeBusyService().getFreeBusy(session, attendees, from, until);
    }

    @Override
    public Map<Attendee, List<FreeBusyTime>> getMergedFreeBusy(List<Attendee> attendees, Date from, Date until) throws OXException {
        return getFreeBusyService().getMergedFreeBusy(session, attendees, from, until);
    }

    @Override
    public Map<Attendee, FreeBusyResult> calculateFreeBusyTime(List<Attendee> attendees, Date from, Date until) throws OXException {
        return getFreeBusyService().calculateFreeBusyTime(session, attendees, from, until);
    }

    @Override
    public List<EventConflict> checkForConflicts(Event event, List<Attendee> attendees) throws OXException {
        return getFreeBusyService().checkForConflicts(session, event, attendees);
    }

    @Override
    public List<AlarmTrigger> getAlarmTrigger(Set<String> actions) throws OXException {
        return getCalendarService().getAlarmTrigger(session, actions);
    }

    /**
     * Get user specific properties for the folder
     *
     * @param folder The {@link Folder}
     * @param contextId The identifier of the context
     * @param userId The identifier of the user the folder belongs to
     * @param loadOwner If set to <code>true</code> the folder owners properties will be loaded
     * @return {@link Collections#emptyMap()} or a {@link Map} with user-specific properties
     */
    private static Map<String, String> getFolderProperties(int contextId, Folder folder, int userId, boolean loadOwner) {
        Map<String, String> properties;
        FolderUserPropertyStorage fps = Services.optService(FolderUserPropertyStorage.class);
        if (null != fps) {
            try {
                properties = fps.getFolderProperties(contextId, Integer.valueOf(folder.getID()).intValue(), userId);
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
