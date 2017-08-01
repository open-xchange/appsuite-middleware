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

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.ArrayUtils;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.handler.utils.HandlerHelper;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.AttendeeStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.exception.OXException;

/**
 * {@link UpdateHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class UpdateHandler extends AbstractHandler {

    private static final EventField[] FIELDS_TO_IGNORE = new EventField[] { EventField.CREATED_BY, EventField.FOLDER_ID, EventField.ID, EventField.CALENDAR_USER, EventField.CREATED, EventField.MODIFIED_BY, EventField.EXTENDED_PROPERTIES, EventField.TIMESTAMP };
    private static final EventField[] EQUALS_IDENTIFIER = new EventField[] { EventField.UID, EventField.RECURRENCE_ID };

    public UpdateHandler(CachingCalendarAccess cachedCalendarAccess) {
        super(cachedCalendarAccess);
    }

    @Override
    public List<Event> execute(String folderId) throws OXException {
        updateLastUpdated();

        EventUpdates diff = generateEventDiff(folderId);
        processDiff(diff);

        return searchEvents(folderId);
    }

    private EventUpdates generateEventDiff(String folderId) throws OXException {
        List<Event> extEventsInFolder = getAndPrepareExtEvents(folderId);

        List<Event> persistedEventsInFolder = searchEvents(folderId);
        EventUpdates eventUpdates = CalendarUtils.getEventUpdates(persistedEventsInFolder, extEventsInFolder, false, FIELDS_TO_IGNORE, EQUALS_IDENTIFIER);

        return eventUpdates;
    }

    private void processDiff(EventUpdates diff) throws OXException {
        create(diff);
        delete(diff);
        update(diff);
    }

    private void delete(EventUpdates diff) throws OXException {
        if (diff.isEmpty()) {
            return;
        }

        CalendarStorage calendarStorage = getCalendarStorage();
        for (Event event : diff.getRemovedItems()) {
            calendarStorage.getEventStorage().deleteEvent(event.getId());
            calendarStorage.getAttendeeStorage().deleteAttendees(event.getId());
            calendarStorage.getAlarmStorage().deleteAlarms(event.getId());
        }
    }

    private void create(EventUpdates diff) throws OXException {
        if (diff.isEmpty()) {
            return;
        }
        createAsync(diff.getAddedItems());
    }

    private void update(EventUpdates diff) throws OXException {
        if (diff.isEmpty()) {
            return;
        }

        boolean containsUID = containsUid(diff.getUpdatedItems());
        if (!containsUID) {
            //FIXME generate reproducible UID for upcoming refreshes
            delete(diff);
            create(diff);
            return;
        }
        EventStorage eventStorage = getCalendarStorage().getEventStorage();

        for (EventUpdate eventUpdate : diff.getUpdatedItems()) {

            Event persistedEvent = eventUpdate.getOriginal();
            Event updatedEvent = eventUpdate.getUpdate();

            updatedEvent.setId(persistedEvent.getId());
            eventStorage.updateEvent(updatedEvent);

            CollectionUpdate<Attendee, AttendeeField> attendeeUpdates = eventUpdate.getAttendeeUpdates();
            if (!attendeeUpdates.isEmpty()) {
                updateAttendees(updatedEvent.getId(), attendeeUpdates);
            }

            CollectionUpdate<Alarm, AlarmField> alarmUpdates = eventUpdate.getAlarmUpdates();
            if (!alarmUpdates.isEmpty()) {
                updateAlarms(updatedEvent, alarmUpdates);
            }
        }
    }

    private void updateAlarms(Event event, CollectionUpdate<Alarm, AlarmField> alarmUpdates) throws OXException {
        if (!alarmUpdates.isEmpty()) {
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
            List<? extends ItemUpdate<Alarm, AlarmField>> updatedItems = alarmUpdates.getUpdatedItems();
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

    private void updateAttendees(String eventId, CollectionUpdate<Attendee, AttendeeField> attendeeUpdates) throws OXException {
        if (!attendeeUpdates.isEmpty()) {
            AttendeeStorage attendeeStorage = getCalendarStorage().getAttendeeStorage();
            if (!attendeeUpdates.getAddedItems().isEmpty()) {
                attendeeStorage.insertAttendees(eventId, attendeeUpdates.getAddedItems());
            }
            if (!attendeeUpdates.getRemovedItems().isEmpty()) {
                attendeeStorage.deleteAttendees(eventId, attendeeUpdates.getRemovedItems());
            }
            if (!attendeeUpdates.getUpdatedItems().isEmpty()) {
                //FIXME
                //                attendeeStorage.updateAttendees(eventId, attendeeUpdates.getUpdatedItems().ge);
            }
        }
    }

    private boolean containsUid(List<EventUpdate> list) {
        for (ItemUpdate<Event, EventField> event : list) {
            if (!event.getUpdate().containsUid()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Event execute(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        execute(folderId);
        return searchEvent(eventId);
    }

    @Override
    public List<Event> execute(List<EventID> eventIds) throws OXException {
        updateLastUpdated();

        Map<String, List<EventID>> sortEventIDsPerFolderId = HandlerHelper.sortEventIDsPerFolderId(eventIds);
        for (String folderId : sortEventIDsPerFolderId.keySet()) {
            EventUpdates diff = generateEventDiff(folderId);
            processDiff(diff);
        }
        return searchEvents(eventIds);
    }
}
