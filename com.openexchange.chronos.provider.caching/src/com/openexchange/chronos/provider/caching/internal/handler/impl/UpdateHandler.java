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

package com.openexchange.chronos.provider.caching.internal.handler.impl;

import static com.openexchange.chronos.common.CalendarUtils.getEventsByUID;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.ProcessingType;
import com.openexchange.chronos.provider.caching.internal.handler.impl.update.EventsDiff;
import com.openexchange.chronos.provider.caching.internal.handler.impl.update.EventsDiffGenerator;
import com.openexchange.chronos.service.CalendarUtilities;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link UpdateHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class UpdateHandler extends AbstractHandler {

    private static EventField[] COMPARISON_IGNORE_FIELDS = new EventField[] { EventField.ID, EventField.ALARMS, EventField.CREATED, EventField.CREATED_BY, EventField.LAST_MODIFIED, EventField.MODIFIED_BY, EventField.SEQUENCE, EventField.UID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.CALENDAR_USER };

    public UpdateHandler(CachingCalendarAccess cachedCalendarAccess) {
        super(cachedCalendarAccess);
    }

    @Override
    public ProcessingType getAssociatedType() {
        return ProcessingType.UPDATE;
    }

    @Override
    public List<Event> execute(List<EventID> eventIds) throws OXException {
        EventsDiff diff = generateEventDiff(eventIds);
        processDiff(diff);
        updateLastModified();

        return searchEvents(eventIds);
    }

    private EventsDiff generateEventDiff(List<EventID> eventIDs) throws OXException {
        List<Event> externalEvents = getAndPrepareExtEvents(eventIDs);

        Map<String, List<Event>> extEventsByUID = getEventsByUID(externalEvents, false);

        List<Event> persistedEventsInFolder = searchEvents(eventIDs);
        Map<String, List<Event>> persistedEventsByUID = getEventsByUID(persistedEventsInFolder, false);

        return EventsDiffGenerator.extractDiff(extEventsByUID, persistedEventsByUID);
    }

    @Override
    public List<Event> execute(String folderId) throws OXException {
        EventsDiff diff = generateEventDiff(folderId);
        processDiff(diff);
        updateLastModified();

        return searchEvents(folderId);
    }

    private EventsDiff generateEventDiff(String folderId) throws OXException {
        List<Event> extEventsInFolder = getAndPrepareExtEvents(folderId);

        Map<String, List<Event>> extEventsInFolderByUID = getEventsByUID(extEventsInFolder, false);
        List<Event> persistedEventsInFolder = searchEvents(folderId);
        Map<String, List<Event>> persistedEventsInFolderByUID = getEventsByUID(persistedEventsInFolder, false);

        EventsDiff diff = EventsDiffGenerator.extractDiff(extEventsInFolderByUID, persistedEventsInFolderByUID);
        return diff;
    }

    private void processDiff(EventsDiff diff) throws OXException {
        create(diff.getEventsToCreate());
        delete(diff.getEventsToDelete());
        update(diff.getEventsToUpdate());
    }

    private void delete(List<Entry<String, List<Event>>> eventsToDelete) throws OXException {
        if (eventsToDelete.isEmpty()) {
            return;
        }

        CalendarStorage calendarStorage = getCalendarStorage();
        for (Map.Entry<String, List<Event>> entry : eventsToDelete) {
            for (Event event : entry.getValue()) {
                calendarStorage.getEventStorage().deleteEvent(event.getId());
                calendarStorage.getAttendeeStorage().deleteAttendees(event.getId());
                if (this.cachedCalendarAccess.supportsAlarms()) {
                    calendarStorage.getAlarmStorage().deleteAlarms(event.getId());
                }
            }
        }
    }

    private void create(List<Entry<String, List<Event>>> eventsToCreate) throws OXException {
        if (eventsToCreate.isEmpty()) {
            return;
        }

        for (Map.Entry<String, List<Event>> entry : eventsToCreate) {
            create(entry);
        }
    }

    private void update(List<Entry<String, List<Event>>> eventsToUpdate) throws OXException {
        if (eventsToUpdate.isEmpty()) {
            return;
        }

        boolean containsUID = containsUid(eventsToUpdate);
        CalendarUtilities calendarUtilities = Services.optService(CalendarUtilities.class);
        if (!containsUID || calendarUtilities == null) {
            //FIXME generate reproducible UID for upcoming refreshes 
            delete(eventsToUpdate);
            create(eventsToUpdate);
            return;
        }
        EventStorage eventStorage = getCalendarStorage().getEventStorage();

        for (Entry<String, List<Event>> event : eventsToUpdate) {
            SearchTerm<?> searchTerm = getSearchTerm(EventField.UID, SingleOperation.EQUALS, event.getKey());
            List<Event> storedEvents = eventStorage.searchEvents(searchTerm, null, FIELDS);
            Event persistedEvent = storedEvents.get(0);
            Event updatedEvent = event.getValue().get(0);
            EventUpdate compare = calendarUtilities.compare(persistedEvent, updatedEvent, true, COMPARISON_IGNORE_FIELDS);

            updatedEvent.setId(persistedEvent.getId());
            updateAttendees(updatedEvent, compare);
            updateAlarms(updatedEvent, compare);

            eventStorage.updateEvent(updatedEvent);
        }
    }

    private void updateAlarms(Event event, EventUpdate compare) throws OXException {
        if (!this.cachedCalendarAccess.supportsAlarms()) {
            return;
        }
        CollectionUpdate<Alarm, AlarmField> alarmUpdates = compare.getAlarmUpdates();
        if (this.cachedCalendarAccess.supportsAlarms() && !alarmUpdates.isEmpty()) {
            AlarmStorage alarmStorage = getCalendarStorage().getAlarmStorage();
            if (!alarmUpdates.getAddedItems().isEmpty()) {
                alarmStorage.insertAlarms(event, event.getCreatedBy(), alarmUpdates.getAddedItems());
            }
            if (!alarmUpdates.getRemovedItems().isEmpty()) {
                List<Integer> removedAlarms = new ArrayList<>(alarmUpdates.getRemovedItems().size());
                for (Alarm alarm : alarmUpdates.getRemovedItems()) {
                    removedAlarms.add(I(alarm.getId()));
                }
                alarmStorage.deleteAlarms(event.getId(), event.getCreatedBy(), ArrayUtils.toPrimitive(removedAlarms.toArray(new Integer[removedAlarms.size()])));
            }
            List<ItemUpdate<Alarm, AlarmField>> updatedItems = alarmUpdates.getUpdatedItems();
            if (!updatedItems.isEmpty()) {
                List<Alarm> alarms = new ArrayList<Alarm>(updatedItems.size());
                for (ItemUpdate<Alarm, AlarmField> itemUpdate : updatedItems) {
                    Alarm update = itemUpdate.getUpdate();
                    update.setId(itemUpdate.getOriginal().getId());
                    update.setUid(itemUpdate.getOriginal().getUid());
                    alarms.add(update);
                }
                alarmStorage.updateAlarms(event, event.getCreatedBy(), alarms);
            }
        }
    }

    private void updateAttendees(Event event, EventUpdate compare) throws OXException {
        CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = compare.getAttendeeUpdates();
        if (!attendeeUpdates.isEmpty()) {
            AttendeeStorage attendeeStorage = getCalendarStorage().getAttendeeStorage();
            if (!attendeeUpdates.getAddedItems().isEmpty()) {
                attendeeStorage.insertAttendees(event.getId(), attendeeUpdates.getAddedItems());
            }
            if (!attendeeUpdates.getRemovedItems().isEmpty()) {
                attendeeStorage.deleteAttendees(event.getId(), attendeeUpdates.getAddedItems());
            }
            if (!attendeeUpdates.getUpdatedItems().isEmpty()) {
                attendeeStorage.updateAttendees(event.getId(), attendeeUpdates.getAddedItems());
            }
        }
    }

    private boolean containsUid(List<Entry<String, List<Event>>> eventsToUpdate) {
        for (Map.Entry<String, List<Event>> entry : eventsToUpdate) {
            List<Event> value = entry.getValue();
            for (Event event : value) {
                if (!event.containsUid()) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public Event execute(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        Event externalEvent = this.cachedCalendarAccess.getEventExt(folderId, eventId, recurrenceId);
        filterByRanges(Collections.singletonList(externalEvent));
        Map<String, List<Event>> eventsExtByUID = getEventsByUID(Collections.singletonList(externalEvent), false);

        Event persistedEvent = searchEvent(folderId, eventId, recurrenceId);
        Map<String, List<Event>> eventsPersistedByUID = getEventsByUID(Collections.singletonList(persistedEvent), false);

        EventsDiff diff = EventsDiffGenerator.extractDiff(eventsExtByUID, eventsPersistedByUID);
        processDiff(diff);
        updateLastModified();

        return searchEvent(folderId, eventId, recurrenceId);
    }
}
