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

package com.openexchange.chronos.impl;

import static com.openexchange.chronos.impl.CalendarUtils.anonymizeIfNeeded;
import static com.openexchange.chronos.impl.CalendarUtils.appendCommonTerms;
import static com.openexchange.chronos.impl.CalendarUtils.find;
import static com.openexchange.chronos.impl.CalendarUtils.getFields;
import static com.openexchange.chronos.impl.CalendarUtils.getFolderIdTerm;
import static com.openexchange.chronos.impl.CalendarUtils.getFrom;
import static com.openexchange.chronos.impl.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.impl.CalendarUtils.getTimeZone;
import static com.openexchange.chronos.impl.CalendarUtils.getUntil;
import static com.openexchange.chronos.impl.CalendarUtils.initCalendar;
import static com.openexchange.chronos.impl.CalendarUtils.isAttendee;
import static com.openexchange.chronos.impl.CalendarUtils.isExcluded;
import static com.openexchange.chronos.impl.CalendarUtils.isInRange;
import static com.openexchange.chronos.impl.CalendarUtils.isResolveOccurrences;
import static com.openexchange.chronos.impl.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.CalendarUtils.sort;
import static com.openexchange.chronos.impl.Check.requireCalendarContentType;
import static com.openexchange.chronos.impl.Check.requireFolderPermission;
import static com.openexchange.chronos.impl.Check.requireReadPermission;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.I2i;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarService;
import com.openexchange.chronos.CalendarSession;
import com.openexchange.chronos.CalendarStorage;
import com.openexchange.chronos.CalendarStorageFactory;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventID;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceService;
import com.openexchange.chronos.SortOptions;
import com.openexchange.chronos.SortOrder;
import com.openexchange.chronos.UserizedEvent;
import com.openexchange.chronos.impl.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderResponse;
import com.openexchange.folderstorage.FolderService;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.Type;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.CalendarContentType;
import com.openexchange.folderstorage.type.PrivateType;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.folderstorage.type.SharedType;
import com.openexchange.group.Group;
import com.openexchange.group.GroupService;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.resource.Resource;
import com.openexchange.resource.ResourceService;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.search.internal.operands.ColumnFieldOperand;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.user.UserService;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarReader {

    protected final CalendarSession session;
    protected final CalendarStorage storage;

    /**
     * Initializes a new {@link CalendarReader}.
     *
     * @param session The session
     */
    public CalendarReader(CalendarSession session) throws OXException {
        this(session, Services.getService(CalendarStorageFactory.class).create(session.getContext()));
    }

    /**
     * Initializes a new {@link CalendarReader}.
     *
     * @param session The session
     * @param storage The storage
     */
    public CalendarReader(CalendarSession session, CalendarStorage storage) {
        super();
        this.session = session;
        this.storage = storage;
    }

    public long getSequenceNumber(int folderID) throws OXException {
        UserizedFolder folder = getFolder(folderID);
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        Date lastModified = folder.getLastModifiedUTC();
        SearchTerm<?> searchTerm = getFolderIdTerm(folder);
        SortOptions sortOptions = new SortOptions().addOrder(SortOrder.DESC(EventField.LAST_MODIFIED)).setLimits(0, 1);
        EventField[] fields = { EventField.LAST_MODIFIED };
        List<Event> events = storage.searchEvents(searchTerm, sortOptions, fields);
        if (0 < events.size() && events.get(0).getLastModified().after(lastModified)) {
            lastModified = events.get(0).getLastModified();
        }
        List<Event> deletedEvents = storage.searchDeletedEvents(searchTerm, sortOptions, fields);
        if (0 < deletedEvents.size() && deletedEvents.get(0).getLastModified().after(lastModified)) {
            lastModified = deletedEvents.get(0).getLastModified();
        }
        return lastModified.getTime();
    }

    public boolean[] hasEventsBetween(int userID, Date from, Date until) throws OXException {
        /*
         * ensure "full date" range, in user timezone
         */
        TimeZone timeZone = getTimeZone(session);
        Calendar calendar = initCalendar(timeZone, from);
        Date rangeStart = CalendarUtils.truncateTime(calendar).getTime();
        calendar.setTime(until);
        CalendarUtils.truncateTime(calendar);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date rangeEnd = calendar.getTime();
        /*
         * search events
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(EventField.END_DATE, SingleOperation.GREATER_THAN, rangeStart)).addSearchTerm(getSearchTerm(EventField.START_DATE, SingleOperation.LESS_THAN, rangeEnd))
            .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(userID)))
            //TODO .addSearchTerm(getSearchTerm(AttendeeField.PARTSTAT, SingleOperation.NOT_EQUALS, ParticipationStatus.DECLINED))
        ;
        List<Event> events = storage.searchEvents(searchTerm, null, null);
        /*
         * step through events day-wise & check for present events
         */
        List<Boolean> hasEventsList = new ArrayList<Boolean>();
        calendar.setTime(rangeStart);
        Date minimumEndTime = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date maximumStartTime = calendar.getTime();
        while (maximumStartTime.before(rangeEnd)) {
            boolean hasEvents = false;
            for (int i = 0; i < events.size() && false == hasEvents; i++) {
                Event event = events.get(i);
                Attendee attendee = find(event.getAttendees(), userID);
                if (null == attendee || ParticipationStatus.DECLINED.equals(attendee.getPartStat())) {
                    continue; // skip
                }
                if (isSeriesMaster(event)) {
                    Iterator<Event> occurrences = resolveOccurrences(event, minimumEndTime, maximumStartTime);
                    while (occurrences.hasNext() && hasEvents == false) {
                        hasEvents |= isInRange(occurrences.next(), minimumEndTime, maximumStartTime, timeZone);
                    }
                } else {
                    hasEvents |= isInRange(event, minimumEndTime, maximumStartTime, timeZone);
                }
            }
            hasEventsList.add(Boolean.valueOf(hasEvents));
            minimumEndTime = maximumStartTime;
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            maximumStartTime = calendar.getTime();
        }
        boolean[] hasEventsArray = new boolean[hasEventsList.size()];
        for (int i = 0; i < hasEventsArray.length; i++) {
            hasEventsArray[i] = hasEventsList.get(i).booleanValue();
        }
        return hasEventsArray;
    }

    public int resolveUid(String uid) throws OXException {
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(EventField.UID, SingleOperation.EQUALS, uid))
            .addSearchTerm(new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.ISNULL))
                .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
            )
        ;
        /*
         * search for an event matching the UID
         */
        List<Event> events = storage.searchEvents(searchTerm, null, new EventField[] { EventField.ID });
        return 0 < events.size() ? events.get(0).getId() : 0;
    }

    public List<UserizedEvent> getChangeExceptions(int folderID, int objectID) throws OXException {
        return getChangeExceptions(getFolder(folderID), objectID);
    }

    public List<UserizedEvent> searchEvents(int[] folderIDs, String pattern) throws OXException {
        List<UserizedFolder> folders;
        if (null == folderIDs || 0 == folderIDs.length) {
            folders = getVisibleFolders();
        } else {
            folders = new ArrayList<UserizedFolder>(folderIDs.length);
            for (int folderID : folderIDs) {
                folders.add(getFolder(folderID));
            }
        }
        List<UserizedEvent> userizedEvents = new ArrayList<UserizedEvent>();
        for (UserizedFolder folder : folders) {
            userizedEvents.addAll(searchEvents(folder, pattern));
        }
        return sort(userizedEvents, new SortOptions(session));
    }

    public UserizedEvent readEvent(EventID eventID) throws OXException {
        return readEvent(eventID.getFolderID(), eventID.getObjectID());
    }

    public UserizedEvent readEvent(int folderID, int objectID) throws OXException {
        return readEvent(getFolder(folderID), objectID);
    }

    public List<UserizedEvent> readEvents(List<EventID> eventIDs) throws OXException {
        List<UserizedEvent> events = new ArrayList<UserizedEvent>(eventIDs.size());
        Map<UserizedFolder, List<EventID>> idsPerFolder = getIdsPerFolder(eventIDs);
        for (Map.Entry<UserizedFolder, List<EventID>> entry : idsPerFolder.entrySet()) {
            events.addAll(readEventsInFolder(entry.getKey(), entry.getValue()));
        }
        List<UserizedEvent> orderedEvents = new ArrayList<UserizedEvent>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            UserizedEvent event = find(events, eventID.getFolderID(), eventID.getObjectID());
            if (null == event) {
                continue; //TODO check; see com.openexchange.ajax.appointment.NewListTest.testRemovedObjectHandling()
                //                throw OXException.notFound(eventID.toString()); //TODO
            }
            //TODO: specific occurrence?
            orderedEvents.add(event);
        }
        return orderedEvents;
    }

    private List<UserizedEvent> searchEvents(UserizedFolder folder, String pattern) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        Check.requireMinimumSearchPatternLength(pattern);
        String wildcardPattern = pattern.startsWith("*") ? pattern : '*' + pattern;
        wildcardPattern = wildcardPattern.endsWith("*") ? wildcardPattern : wildcardPattern + '*';
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getFolderIdTerm(folder))
            .addSearchTerm(new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(getSearchTerm(EventField.SUMMARY, SingleOperation.EQUALS, wildcardPattern))
                .addSearchTerm(getSearchTerm(EventField.DESCRIPTION, SingleOperation.EQUALS, wildcardPattern))
                .addSearchTerm(getSearchTerm(EventField.CATEGORIES, SingleOperation.EQUALS, wildcardPattern))
            )
        ;
        List<Event> events = storage.searchEvents(searchTerm, new SortOptions(session), getFields(session));
        return userize(events, folder);
    }

    private List<UserizedEvent> readEventsInFolder(UserizedFolder folder, List<EventID> eventIDs) throws OXException {
        Set<Integer> objectIDs = new HashSet<Integer>(eventIDs.size());
        int folderID = Integer.parseInt(folder.getID());
        for (EventID eventID : eventIDs) {
            if (folderID == eventID.getFolderID()) {
                objectIDs.add(I(eventID.getObjectID()));
            }
        }
        return readEventsInFolder(folder, I2i(objectIDs), false, null);
    }

    private Map<UserizedFolder, List<EventID>> getIdsPerFolder(List<EventID> eventIDs) throws OXException {
        Map<Integer, List<EventID>> idsPerFolderId = new HashMap<Integer, List<EventID>>();
        for (EventID eventID : eventIDs) {
            com.openexchange.tools.arrays.Collections.put(idsPerFolderId, I(eventID.getFolderID()), eventID);
        }
        Map<UserizedFolder, List<EventID>> idsPerFolder = new HashMap<UserizedFolder, List<EventID>>(idsPerFolderId.size());
        for (Map.Entry<Integer, List<EventID>> entry : idsPerFolderId.entrySet()) {
            idsPerFolder.put(getFolder(entry.getKey().intValue()), entry.getValue());
        }
        return idsPerFolder;
    }

    public List<UserizedEvent> readEventsInFolder(int folderID, Date updatedSince) throws OXException {
        return readEventsInFolder(getFolder(folderID), null, false, updatedSince);
    }

    public List<UserizedEvent> readDeletedEventsInFolder(int folderID, Date deletedSince) throws OXException {
        return readEventsInFolder(getFolder(folderID), null, true, deletedSince);
    }

    public List<UserizedEvent> readEventsOfUser(int userID, Date updatedSince) throws OXException {
        return readEventsOfUser(userID, false, updatedSince);
    }

    public List<UserizedEvent> readDeletedEventsOfUser(int userID, Date deletedSince) throws OXException {
        return readEventsOfUser(userID, true, deletedSince);
    }

    protected UserizedEvent readEvent(UserizedFolder folder, int objectID) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        Event event = storage.loadEvent(objectID, getFields(session));
        if (null == event) {
            throw OXException.notFound(String.valueOf(objectID));//TODO
        }
        Check.eventIsInFolder(event, folder);
        if (session.getUser().getId() != event.getCreatedBy()) {
            requireReadPermission(folder, Permission.READ_ALL_OBJECTS);
        }
        return userize(Collections.singletonList(event), folder).get(0);
    }

    protected List<UserizedEvent> getChangeExceptions(UserizedFolder folder, int objectID) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getFolderIdTerm(folder))
            .addSearchTerm(getSearchTerm(EventField.SERIES_ID, SingleOperation.EQUALS, I(objectID)))
            .addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.NOT_EQUALS, new ColumnFieldOperand<EventField>(EventField.SERIES_ID)))
        ;
        /*
         * perform search & userize the results
         */
        List<Event> events = storage.searchEvents(searchTerm, null, getFields(session));
        return userize(events, folder);
    }

    protected List<UserizedEvent> readEventsInFolder(UserizedFolder folder, int[] objectIDs, boolean deleted, Date updatedSince) throws OXException {
        requireCalendarContentType(folder);
        requireFolderPermission(folder, Permission.READ_FOLDER);
        requireReadPermission(folder, Permission.READ_OWN_OBJECTS);
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(getFolderIdTerm(folder));
        if (null != objectIDs) {
            if (0 == objectIDs.length) {
                return Collections.emptyList();
            } else if (1 == objectIDs.length) {
                searchTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, I(objectIDs[0])));
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (int objectID : objectIDs) {
                    orTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, I(objectID)));
                }
                searchTerm.addSearchTerm(orTerm);
            }
        }
        appendCommonTerms(searchTerm, getFrom(session), getUntil(session), updatedSince);
        /*
         * perform search & userize the results
         */
        List<Event> events;
        if (deleted) {
            events = storage.searchDeletedEvents(searchTerm, new SortOptions(session), getFields(session));
        } else {
            events = storage.searchEvents(searchTerm, new SortOptions(session), getFields(session));
        }
        return userize(events, folder);
    }

    protected List<UserizedEvent> readEventsOfUser(int userID, boolean deleted, Date updatedSince) throws OXException {
        /*
         * construct search term
         */
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
            .addSearchTerm(getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(userID)));
        appendCommonTerms(searchTerm, getFrom(session), getUntil(session), updatedSince);
        /*
         * perform search & userize the results for the current session's user
         */
        List<Event> events;
        if (deleted) {
            events = storage.searchDeletedEvents(searchTerm, new SortOptions(session), getFields(session, EventField.ATTENDEES));
        } else {
            events = storage.searchEvents(searchTerm, new SortOptions(session), getFields(session, EventField.ATTENDEES));
        }
        return userize(events, session.getUser().getId());
    }

    private List<UserizedEvent> userize(List<Event> events, int forUser) throws OXException {
        List<UserizedEvent> userizedEvents = new ArrayList<UserizedEvent>(events.size());
        Map<Integer, List<Alarm>> alarmsById = readAlarms(events, forUser);
        for (Event event : events) {
            if (isExcluded(event, session)) {
                continue;
            }
            Attendee userAttendee = find(event.getAttendees(), forUser);
            int folderID;
            if (null != userAttendee && 0 < userAttendee.getFolderID()) {
                folderID = userAttendee.getFolderID();
            } else if (0 < event.getPublicFolderId()) {
                folderID = event.getPublicFolderId();
            } else {
                throw OXException.general("No suitable parent folder for event " + event); //TODO shouldn't happen at all?
            }
            UserizedEvent userizedEvent = getUserizedEvent(event, folderID, alarmsById.get(I(event.getId())));
            if (isSeriesMaster(event) && isResolveOccurrences(session)) {
                userizedEvents.addAll(resolveOccurrences(userizedEvent));
            } else {
                userizedEvents.add(userizedEvent);
            }
        }
        return sort(userizedEvents, new SortOptions(session));
    }

    private Map<Integer, List<Alarm>> readAlarms(List<Event> events, int userID) throws OXException {
        List<Integer> objectIDs = new ArrayList<Integer>(events.size());
        for (Event event : events) {
            if (isAttendee(event, userID)) {
                objectIDs.add(I(event.getId()));
            }
        }
        return 0 < objectIDs.size() ? storage.loadAlarms(I2i(objectIDs), userID) : Collections.<Integer, List<Alarm>> emptyMap();
    }

    private List<UserizedEvent> userize(List<Event> events, UserizedFolder inFolder) throws OXException {
        User calendarUser = getCalendarUser(inFolder);
        int folderID = Integer.parseInt(inFolder.getID());
        List<UserizedEvent> userizedEvents = new ArrayList<UserizedEvent>(events.size());
        Map<Integer, List<Alarm>> alarmsById = readAlarms(events, calendarUser.getId());
        for (Event event : events) {
            if (isExcluded(event, session)) {
                continue;
            }
            UserizedEvent userizedEvent = getUserizedEvent(event, folderID, alarmsById.get(I(event.getId())));
            if (isSeriesMaster(event) && isResolveOccurrences(session)) {
                userizedEvents.addAll(resolveOccurrences(userizedEvent));
            } else {
                userizedEvents.add(userizedEvent);
            }
        }
        return sort(userizedEvents, new SortOptions(session));
    }

    private List<UserizedEvent> resolveOccurrences(UserizedEvent master) throws OXException {
        List<UserizedEvent> events = new ArrayList<UserizedEvent>();
        Iterator<Event> occurrences = resolveOccurrences(master.getEvent(), getFrom(session), getUntil(session));
        while (occurrences.hasNext()) {
            Event occurrence = occurrences.next();
            if (isExcluded(occurrence, session)) {
                continue;
            }
            events.add(getUserizedEvent(occurrence, master.getFolderId(), master.getAlarms()));
        }
        return events;
    }

    private UserizedEvent getUserizedEvent(Event event, int folderID, List<Alarm> alarms) throws OXException {
        UserizedEvent userizedEvent = new UserizedEvent(session.getSession(), event, folderID, alarms);
        return anonymizeIfNeeded(userizedEvent);
    }

    private Iterator<Event> resolveOccurrences(Event masterEvent, Date from, Date until) {
        TimeZone timeZone = getTimeZone(session);
        Calendar fromCalendar = null == from ? null : initCalendar(timeZone, from);
        Calendar untilCalendar = null == until ? null : initCalendar(timeZone, until);
        return Services.getService(RecurrenceService.class).calculateInstances(masterEvent, fromCalendar, untilCalendar, -1);
    }

    protected UserizedFolder getFolder(int folderID) throws OXException {
        return Services.getService(FolderService.class).getFolder(FolderStorage.REAL_TREE_ID, String.valueOf(folderID), session.getSession(), null);
    }

    protected List<UserizedFolder> getVisibleFolders() throws OXException {
        return getVisibleFolders(PrivateType.getInstance(), SharedType.getInstance(), PublicType.getInstance());
    }

    protected List<UserizedFolder> getVisibleFolders(Type... types) throws OXException {
        List<UserizedFolder> visibleFolders = new ArrayList<UserizedFolder>();
        FolderService folderService = Services.getService(FolderService.class);
        for (Type type : types) {
            FolderResponse<UserizedFolder[]> response = folderService.getVisibleFolders(FolderStorage.REAL_TREE_ID, CalendarContentType.getInstance(), type, false, session.getSession(), null);
            UserizedFolder[] folders = response.getResponse();
            if (null != folders && 0 < folders.length) {
                visibleFolders.addAll(Arrays.asList(folders));
            }
        }
        return visibleFolders;
    }

    /**
     * Gets the actual target calendar user for a specific folder. This is either the current session's user for "private" or "public"
     * folders, or the folder owner for "shared" calendar folders.
     *
     * @param folder The folder to get the calendar user for
     * @return The calendar user
     */
    protected User getCalendarUser(UserizedFolder folder) throws OXException {
        return SharedType.getInstance().equals(folder.getType()) ? getUser(folder.getCreatedBy()) : session.getUser();
    }

    /**
     * Gets the "acting" calendar user for a specific folder, i.e. the proxy user who is acting on behalf of the calendar owner, which is
     * the current session's user in case the folder is a "shared" calendar, otherwise <code>null</code> for "private" or "public" folders.
     *
     * @param folder The folder to determine the proxy user for
     * @return The proxy calendar user, or <code>null</code> if the current session's user is acting on behalf of it's own
     */
    protected User getProxyUser(UserizedFolder folder) throws OXException {
        return SharedType.getInstance().equals(folder.getType()) ? getUser(folder.getCreatedBy()) : session.getUser();
    }

    protected User getUser(int userID) throws OXException {
        UserService userService = Services.getService(UserService.class);
        return userService.getUser(userID, session.getContext());
    }

    protected Group getGroup(int groupID) throws OXException {
        return Services.getService(GroupService.class).getGroup(session.getContext(), groupID);
    }

    protected Resource getResource(int resourceID) throws OXException {
        return Services.getService(ResourceService.class).getResource(resourceID, session.getContext());
    }

    protected int getDefaultFolderID(User user) throws OXException {
        //TODO: via higher level service?
        return new OXFolderAccess(session.getContext()).getDefaultFolderID(user.getId(), FolderObject.CALENDAR);
    }

}
