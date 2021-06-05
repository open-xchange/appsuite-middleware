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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.chronos.impl.Utils.getFolderIdTerm;
import static com.openexchange.folderstorage.Permission.DELETE_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;

/**
 * {@link ClearPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ClearPerformer extends AbstractUpdatePerformer {

    private static final int BATCH_SIZE = 500;

    /**
     * Initializes a new {@link ClearPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public ClearPerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the deletion of all events in the folder.
     *
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The result
     */
    public InternalCalendarResult perform(long clientTimestamp) throws OXException {
        /*
         * check current session user's permissions; 'clear' requires access to all events in folder
         */
        requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, NO_PERMISSIONS, DELETE_OWN_OBJECTS);
        /*
         * delete all events in folder in batches
         */
        SearchTerm<?> searchTerm = getFolderIdTerm(session, folder);
        SearchOptions searchOptions = new SearchOptions().setLimits(0, BATCH_SIZE);
        int deleted;
        do {
            deleted = deleteEvents(searchTerm, searchOptions, clientTimestamp);
        } while (0 < deleted);
        /*
         * return calendar result
         */
        return resultTracker.getResult();
    }

    private int deleteEvents(SearchTerm<?> searchTerm, SearchOptions searchOptions, long clientTimestamp) throws OXException {
        /*
         * load original events, ensuring that all relevant metadata for downstream interceptors is available
         */
        Set<EventField> interceptorFields = interceptorRegistry.getRelevantFields();
        EventField[] fields = getFields(new EventField[0], interceptorFields.toArray(new EventField[interceptorFields.size()]));
        List<Event> originalEvents = storage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);
        if (null == originalEvents || 0 == originalEvents.size()) {
            return 0;
        }
        originalEvents = storage.getUtilities().loadAdditionalEventData(-1, originalEvents, null);
        /*
         * derive kind of deletion for each event and check permissions
         */
        List<Event> eventsToDelete = new ArrayList<Event>(originalEvents.size());
        List<Entry<Event, Attendee>> attendeesToDeleteByEvent = new ArrayList<Entry<Event, Attendee>>();
        for (Event originalEvent : originalEvents) {
            requireUpToDateTimestamp(originalEvent, clientTimestamp);
            if (deleteRemovesEvent(originalEvent)) {
                /*
                 * deletion of not group-scheduled event / by organizer / last user attendee
                 */
                requireDeletePermissions(originalEvent);
                eventsToDelete.add(originalEvent);
            } else {
                /*
                 * deletion as one of the attendees
                 */
                Attendee userAttendee = find(originalEvent.getAttendees(), calendarUserId);
                if (null == userAttendee) {
                    throw CalendarExceptionCodes.NO_DELETE_PERMISSION.create(folder.getId());
                }
                requireDeletePermissions(originalEvent, userAttendee);
                attendeesToDeleteByEvent.add(new AbstractMap.SimpleEntry<Event, Attendee>(originalEvent, userAttendee));
            }
        }
        /*
         * perform deletion & return number of processed events
         */
        if (0 < eventsToDelete.size()) {
            deleteEvents(eventsToDelete);
        }
        if (0 < attendeesToDeleteByEvent.size()) {
            deleteAttendees(attendeesToDeleteByEvent);
        }
        return originalEvents.size();
    }

    private void deleteEvents(List<Event> eventsToDelete) throws OXException {
        /*
         * collect data to delete, prepare tombstone data & notify interceptors
         */
        List<String> eventIds = new ArrayList<String>(eventsToDelete.size());
        Map<String, List<Attachment>> attachmentsByEventId = new HashMap<String, List<Attachment>>();
        List<Event> eventTombstones = new ArrayList<Event>(eventsToDelete.size());
        Map<String, List<Attendee>> attendeeTombstonesByEventId = new HashMap<String, List<Attendee>>(eventsToDelete.size());
        for (Event originalEvent : eventsToDelete) {
            eventIds.add(originalEvent.getId());
            eventTombstones.add(storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUser));
            if (null != originalEvent.getAttendees() && 0 < originalEvent.getAttendees().size()) {
                attendeeTombstonesByEventId.put(originalEvent.getId(), storage.getUtilities().getTombstones(originalEvent.getAttendees()));
            }
            if (null != originalEvent.getAttachments() && 0 < originalEvent.getAttachments().size()) {
                attachmentsByEventId.put(originalEvent.getId(), originalEvent.getAttachments());
            }
            interceptorRegistry.triggerInterceptorsOnBeforeDelete(originalEvent);
        }
        /*
         * insert tombstone data & perform deletion
         */
        storage.getEventStorage().insertEventTombstones(eventTombstones);
        storage.getAttendeeStorage().insertAttendeeTombstones(attendeeTombstonesByEventId);
        storage.getAlarmStorage().deleteAlarms(eventIds);
        storage.getAlarmTriggerStorage().deleteTriggers(eventIds);
        storage.getConferenceStorage().deleteConferences(eventIds);
        storage.getAttendeeStorage().deleteAttendees(eventIds);
        storage.getEventStorage().deleteEvents(eventIds);
        if (0 < attachmentsByEventId.size()) {
            storage.getAttachmentStorage().deleteAttachments(session.getSession(), Collections.singletonMap(folder.getId(), attachmentsByEventId));
        }
        /*
         * track deletions in result & group corresponding calendar object resources
         */
        Map<String, List<Event>> eventsByUID = new LinkedHashMap<String, List<Event>>();
        for (Event originalEvent : eventsToDelete) {
            resultTracker.trackDeletion(originalEvent);
            if (null != originalEvent.getUid()) {
                com.openexchange.tools.arrays.Collections.put(eventsByUID, originalEvent.getUid(), originalEvent);
            }
        }
        /*
         * track notifications and scheduling messages for deleted resources
         */
        for (List<Event> value : eventsByUID.values()) {
            schedulingHelper.trackDeletion(new DefaultCalendarObjectResource(value));
        }
    }

    private void deleteAttendees(List<Entry<Event, Attendee>> attendeesToDeleteByEvent) throws OXException {
        //TODO: further batch operations
        /*
         * collect data to delete & prepare tombstone data
         */
        Map<Integer, List<String>> eventIdsByUserId = new HashMap<Integer, List<String>>();
        List<Event> eventTombstones = new ArrayList<Event>(attendeesToDeleteByEvent.size());
        Map<String, List<Attendee>> attendeeTombstonesByEventId = new HashMap<String, List<Attendee>>(attendeesToDeleteByEvent.size());
        for (Entry<Event, Attendee> attendeeToDeleteByEvent : attendeesToDeleteByEvent) {
            Event event = attendeeToDeleteByEvent.getKey();
            Attendee attendee = attendeeToDeleteByEvent.getValue();
            com.openexchange.tools.arrays.Collections.put(eventIdsByUserId, I(attendee.getEntity()), event.getId());
            eventTombstones.add(storage.getUtilities().getTombstone(event, timestamp, calendarUser));
            attendeeTombstonesByEventId.put(event.getId(), Collections.singletonList(storage.getUtilities().getTombstone(attendee)));
        }
        /*
         * insert tombstone data & perform deletion and updates
         */
        for (Entry<Event, Attendee> attendeeToDeleteByEvent : attendeesToDeleteByEvent) {
            Event event = attendeeToDeleteByEvent.getKey();
            Attendee attendee = attendeeToDeleteByEvent.getValue();
            storage.getAttendeeStorage().deleteAttendees(event.getId(), Collections.singletonList(attendee));
            storage.getAlarmStorage().deleteAlarms(event.getId(), attendee.getEntity());
        }
        for (Entry<Integer, List<String>> entry : eventIdsByUserId.entrySet()) {
            storage.getAlarmTriggerStorage().deleteTriggers(entry.getValue(), i(entry.getKey()));
        }
        storage.getEventStorage().insertEventTombstones(eventTombstones);
        storage.getAttendeeStorage().insertAttendeeTombstones(attendeeTombstonesByEventId);
        /*
         * 'touch' modified events, track updates in result & group corresponding calendar object resources
         */
        Map<String, List<Event>> eventsByUID = new LinkedHashMap<String, List<Event>>();
        for (Entry<Event, Attendee> attendeeToDeleteByEvent : attendeesToDeleteByEvent) {
            Event originalEvent = attendeeToDeleteByEvent.getKey();
            touch(originalEvent.getId());
            Event updatedEvent = loadEventData(originalEvent.getId());
            resultTracker.trackUpdate(originalEvent, updatedEvent);
            if (null != originalEvent.getUid()) {
                com.openexchange.tools.arrays.Collections.put(eventsByUID, originalEvent.getUid(), originalEvent);
            }
        }
        /*
         * track notifications and scheduling messages for deleted resources
         */
        for (List<Event> value : eventsByUID.values()) {
            schedulingHelper.trackDeletion(new DefaultCalendarObjectResource(value));
        }
    }

}
