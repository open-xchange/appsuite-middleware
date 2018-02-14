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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.chronos.impl.Utils.isEnforceDefaultAttendee;
import static com.openexchange.chronos.impl.Utils.isIncludeClassifiedEvents;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventFlag;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultEventsResult;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventsResult;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link AllPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AllPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link AllPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public AllPerformer(CalendarSession session, CalendarStorage storage) throws OXException {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @return The loaded events
     */
    public List<Event> perform() throws OXException {
        return perform(null, null);
    }

    /**
     * Performs the operation.
     *
     * @param partStats The participation status to include, or <code>null</code> to include all events independently of the user
     *            attendee's participation status
     * @param rsvp The reply expectation to include, or <code>null</code> to include all events independently of the user attendee's
     *            rsvp status
     * @return The loaded events
     */
    public List<Event> perform(Boolean rsvp, ParticipationStatus[] partStats) throws OXException {
        /*
         * search for events the current session's user attends
         */
        SearchTerm<?> searchTerm = getSearchTerm(AttendeeField.ENTITY, SingleOperation.EQUALS, I(session.getUserId()));
        if (null != rsvp) {
            /*
             * only include events with matching rsvp
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                .addSearchTerm(searchTerm)
                .addSearchTerm(getSearchTerm(AttendeeField.RSVP, SingleOperation.EQUALS, rsvp))
            ;
        }
        if (null != partStats) {
            /*
             * only include events with matching participation status
             */
            if (0 == partStats.length) {
                return Collections.emptyList();
            }
            if (1 == partStats.length) {
                searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                    .addSearchTerm(searchTerm)
                    .addSearchTerm(getSearchTerm(AttendeeField.PARTSTAT, SingleOperation.EQUALS, partStats[0]))
                ;
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (ParticipationStatus partStat : partStats) {
                    orTerm.addSearchTerm(getSearchTerm(AttendeeField.PARTSTAT, SingleOperation.EQUALS, partStat));
                }
                searchTerm = new CompositeSearchTerm(CompositeOperation.AND)
                    .addSearchTerm(searchTerm)
                    .addSearchTerm(orTerm)
                ;
            }
        }
        if (false == isEnforceDefaultAttendee(session)) {
            /*
             * also include not group-scheduled events associated with the calendar user
             */
            searchTerm = new CompositeSearchTerm(CompositeOperation.OR)
                .addSearchTerm(getSearchTerm(EventField.CALENDAR_USER, SingleOperation.EQUALS, I(session.getUserId())))
                .addSearchTerm(searchTerm);
        }
        /*
         * perform search & userize the results for the current session's user
         */
        EventField[] fields = getFields(session, EventField.ORGANIZER, EventField.ATTENDEES);
        List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), fields);
        events = storage.getUtilities().loadAdditionalEventData(session.getUserId(), events, fields);
        return postProcess(events, session.getUserId(), true, fields);
    }

    /**
     * Performs the operation.
     *
     * @param folders The parent folders to get all events from
     * @return The loaded events
     */
    public List<Event> perform21(List<UserizedFolder> folders) throws OXException {
        /*
         * load event data per folder & additional event data per calendar user
         */
        EventField[] fields = getFields(session);
        SearchOptions searchOptions = new SearchOptions(session);
        Map<UserizedFolder, List<Event>> eventsPerFolder = new HashMap<UserizedFolder, List<Event>>();
        for (Map.Entry<Integer, List<UserizedFolder>> entry : getFoldersPerCalendarUserId(folders).entrySet()) {
            List<Event> eventsForCalendarUser = new ArrayList<Event>();
            for (UserizedFolder folder : entry.getValue()) {
                requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
                List<Event> eventsInFolder = storage.getEventStorage().searchEvents(getFolderIdTerm(session, folder), searchOptions, fields);
                eventsForCalendarUser.addAll(eventsInFolder);
                eventsPerFolder.put(folder, eventsInFolder);
            }
            eventsForCalendarUser = storage.getUtilities().loadAdditionalEventData(i(entry.getKey()), eventsForCalendarUser, fields);
        }
        /*
         * post process events, based on each requested folder's perspective
         */
        List<Event> allEvents = new ArrayList<Event>();
        boolean includeClassifiedEvents = isIncludeClassifiedEvents(session);
        for (Entry<UserizedFolder, List<Event>> entry : eventsPerFolder.entrySet()) {
            allEvents.addAll(postProcess(entry.getValue(), entry.getKey(), includeClassifiedEvents, fields));
            getSelfProtection().checkEventCollection(allEvents);
        }
        return allEvents;
    }

    /**
     * Performs the operation.
     *
     * @param folderIds The identifiers of the parent folders to get all events from
     * @return The loaded events
     */
    public Map<String, EventsResult> perform(List<String> folderIds) throws OXException {
        if (null == folderIds || folderIds.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, EventsResult> resultsPerFolderId = new HashMap<String, EventsResult>(folderIds.size());
        /*
         * get folders, storing a possible exception in the results
         */
        List<UserizedFolder> folders = new ArrayList<UserizedFolder>(folderIds.size());
        for (String folderId : folderIds) {
            try {
                folders.add(getFolder(session, folderId));
            } catch (OXException e) {
                resultsPerFolderId.put(folderId, new DefaultEventsResult(e));
            }
        }
        /*
         * load event data per folder & additional event data per calendar user
         */
        EventField[] fields = getFields(session);
        SearchOptions searchOptions = new SearchOptions(session);
        Map<UserizedFolder, List<Event>> eventsPerFolder = new HashMap<UserizedFolder, List<Event>>(folders.size());
        for (Entry<Integer, List<UserizedFolder>> entry : getFoldersPerCalendarUserId(folders).entrySet()) {
            List<Event> eventsForCalendarUser = new ArrayList<Event>();
            for (UserizedFolder folder : entry.getValue()) {
                try {
                    requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
                    List<Event> eventsInFolder = storage.getEventStorage().searchEvents(getFolderIdTerm(session, folder), searchOptions, fields);
                    eventsForCalendarUser.addAll(eventsInFolder);
                    eventsPerFolder.put(folder, eventsInFolder);
                } catch (OXException e) {
                    resultsPerFolderId.put(folder.getID(), new DefaultEventsResult(e));
                }
            }
            eventsForCalendarUser = storage.getUtilities().loadAdditionalEventData(i(entry.getKey()), eventsForCalendarUser, fields);
        }
        /*
         * post process events, based on each requested folder's perspective
         */
        boolean includeClassifiedEvents = isIncludeClassifiedEvents(session);
        for (Entry<UserizedFolder, List<Event>> entry : eventsPerFolder.entrySet()) {
            resultsPerFolderId.put(entry.getKey().getID(), new DefaultEventsResult(postProcess(entry.getValue(), entry.getKey(), includeClassifiedEvents, fields)));
            getSelfProtection().checkResultMap(resultsPerFolderId);
        }
        return resultsPerFolderId;
    }

    /**
     * Performs the operation.
     *
     * @param folder The parent folder to get all events from
     * @return The loaded events
     */
    public List<Event> perform(UserizedFolder folder) throws OXException {
        /*
         * perform search & userize the results based on the requested folder
         */
        requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, NO_PERMISSIONS, NO_PERMISSIONS);
        SearchTerm<?> searchTerm = getFolderIdTerm(session, folder);
        EventField[] fields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        boolean dynamicFlags = false; //TODO: doesn't recognize pseudo-group-scheduled, so disabled for now
        if (false == dynamicFlags || null == fields || false == contains(fields, EventField.FLAGS)) {
            /*
             * get events with default fields & load additional event data as needed
             */
            fields = getFields(fields);
            List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), fields);
            events = storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), events, fields);
            return postProcess(events, folder, isIncludeClassifiedEvents(session), fields);
        }
        /*
         * get events & load additional data for event flag generation dynamically
         */
        fields = getFields(fields, EventField.ID, EventField.STATUS, EventField.TRANSP, EventField.CLASSIFICATION, EventField.ORGANIZER);
        List<Event> events = storage.getEventStorage().searchEvents(searchTerm, new SearchOptions(session), fields);
        events = storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), events, fields);
        DynamicEventFlagsGenerator flagsGenerator = new DynamicEventFlagsGenerator(session, storage, getCalendarUserId(folder), getObjectIDs(events), fields);
        return postProcess(events, folder, isIncludeClassifiedEvents(session), fields, flagsGenerator);
    }

    private static Map<Integer, List<UserizedFolder>> getFoldersPerCalendarUserId(List<UserizedFolder> folders) {
        Map<Integer, List<UserizedFolder>> foldersPerCalendarUserId = new HashMap<Integer, List<UserizedFolder>>();
        for (UserizedFolder folder : folders) {
            com.openexchange.tools.arrays.Collections.put(foldersPerCalendarUserId, I(getCalendarUserId(folder)), folder);
        }
        return foldersPerCalendarUserId;
    }

    private static final class DynamicEventFlagsGenerator implements EventFlagsGenerator {

        private final Map<String, Boolean> attachmentsPerEventId;
        private final Map<String, Boolean> alarmTriggersPerEventId;
        private final Map<String, ParticipationStatus> partStatsPerEventId;

        /**
         * Initializes a new {@link DynamicEventFlagsGenerator}.
         *
         * @param session The calendar session
         * @param storage The calendar storage
         * @param calendarUserId The identifier of the calendar user
         * @param eventIds The identifiers of the events to prepare the event flags for
         * @param loadedFields The already loaded fields
         */
        public DynamicEventFlagsGenerator(CalendarSession session, CalendarStorage storage, int calendarUserId, String[] eventIds, EventField[] loadedFields) throws OXException {
            super();
            /*
             * load attachment info dynamically, unless already requested explicitly
             */
            attachmentsPerEventId = contains(loadedFields, EventField.ATTACHMENTS) ? null : storage.getAttachmentStorage().hasAttachments(eventIds);
            /*
             * load alarm info dynamically, unless already requested explicitly
             */
            alarmTriggersPerEventId = contains(loadedFields, EventField.ALARMS) ? null : storage.getAlarmTriggerStorage().hasTriggers(calendarUserId, eventIds);
            /*
             * load attendee-related info dynamically, unless already requested explicitly
             */
            partStatsPerEventId = contains(loadedFields, EventField.ATTENDEES) ? null : storage.getAttendeeStorage().loadPartStats(eventIds, session.getEntityResolver().prepareUserAttendee(calendarUserId));
        }

        @Override
        public EnumSet<EventFlag> getFlags(Event event, int calendarUser) {
            EnumSet<EventFlag> flags = CalendarUtils.getFlags(event, calendarUser);
            if (null != event.getOrganizer()) {
                //TODO: doesn't recognize pseudo-group-scheduled, so disabled for now
                flags.add(EventFlag.SCHEDULED);
            }
            if (null != partStatsPerEventId) {
                ParticipationStatus partStat = partStatsPerEventId.get(event.getId());
                if (null != partStat) {
                    flags.add(EventFlag.ATTENDEE);
                    if (event.getOrganizer().getEntity() == calendarUser) {
                        flags.add(EventFlag.ORGANIZER);
                    }
                    if (ParticipationStatus.ACCEPTED.equals(partStat)) {
                        flags.add(EventFlag.ACCEPTED);
                    } else if (ParticipationStatus.DECLINED.equals(partStat)) {
                        flags.add(EventFlag.DECLINED);
                    } else if (ParticipationStatus.DELEGATED.equals(partStat)) {
                        flags.add(EventFlag.DELEGATED);
                    } else if (ParticipationStatus.NEEDS_ACTION.equals(partStat)) {
                        flags.add(EventFlag.NEEDS_ACTION);
                    } else if (ParticipationStatus.TENTATIVE.equals(partStat)) {
                        flags.add(EventFlag.TENTATIVE);
                    }
                }
            }
            if (null != attachmentsPerEventId && Boolean.TRUE.equals(attachmentsPerEventId.get(event.getId()))) {
                flags.add(EventFlag.ATTACHMENTS);
            }
            if (null != alarmTriggersPerEventId && Boolean.TRUE.equals(alarmTriggersPerEventId.get(event.getId()))) {
                flags.add(EventFlag.ALARMS);
            }
            return flags;
        }

    }

}
