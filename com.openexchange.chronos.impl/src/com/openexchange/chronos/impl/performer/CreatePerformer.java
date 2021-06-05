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

import static com.openexchange.chronos.common.CalendarUtils.getRecurrenceIds;
import static com.openexchange.chronos.common.CalendarUtils.getUserIDs;
import static com.openexchange.chronos.common.CalendarUtils.isAllDay;
import static com.openexchange.chronos.common.CalendarUtils.isInternal;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.normalizeRecurrenceID;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.getResolvableEntities;
import static com.openexchange.chronos.impl.Utils.prepareOrganizer;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.UUID;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.InternalAttendeeUpdates;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.java.Strings;

/**
 * {@link CreatePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CreatePerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link CreatePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public CreatePerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Initializes a new {@link CreatePerformer}, taking over the settings from another update performer.
     *
     * @param updatePerformer The update performer to take over the settings from
     */
    protected CreatePerformer(AbstractUpdatePerformer updatePerformer) {
        super(updatePerformer);
    }

    /**
     * Performs the creation of an event.
     *
     * @param eventData The event to create as supplied by the client
     * @return The result
     */
    public InternalCalendarResult perform(Event eventData) throws OXException {
        /*
         * create event, prepare scheduling messages & return result
         */
        Event createdEvent = createEvent(eventData, null);
        schedulingHelper.trackCreation(new DefaultCalendarObjectResource(createdEvent));
        return resultTracker.getResult();
    }

    /**
     * Prepares and stores a new event based on the client supplied event data and tracks the creation results accordingly. Permission-
     * and consistency-related checks are performed implicitly, however, no scheduling- or notification messages are tracked.
     * <p/>
     * A new unique object identifier is assigned automatically, and the event's series identifier is set appropriately depending on the
     * event being part of a series and other existing events with the same UID.
     * 
     * @param eventData The event data as passed from the client
     * @param existingEvents A list of already stored events from the same calendar object resource (with the same UID),
     *            or <code>null</code> if there are none
     * @return The created event
     */
    protected Event createEvent(Event eventData, List<Event> existingEvents) throws OXException {
        /*
         * check current session user's permissions
         */
        requireCalendarPermission(folder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        /*
         * prepare event & attendee data for insert, assign parent folder
         */
        Event newEvent = prepareNewEvent(eventData, existingEvents);
        /*
         * check for conflicts & quota restrictions
         */
        Check.quotaNotExceeded(storage, session);
        Check.noConflicts(storage, session, newEvent, newEvent.getAttendees());
        /*
         * trigger calendar interceptors
         */
        interceptorRegistry.triggerInterceptorsOnBeforeCreate(newEvent);
        /*
         * insert event, attendees, attachments & conferences
         */
        storage.getEventStorage().insertEvent(newEvent);
        if (false == isNullOrEmpty(newEvent.getAttendees())) {
            storage.getAttendeeStorage().insertAttendees(newEvent.getId(), newEvent.getAttendees());
        }
        if (false == isNullOrEmpty(newEvent.getAttachments())) {
            storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getId(), newEvent.getId(), prepareAttachments(newEvent.getAttachments()));
        }
        if (false == isNullOrEmpty(newEvent.getConferences())) {
            storage.getConferenceStorage().insertConferences(newEvent.getId(), newEvent.getConferences());
        }
        /*
         * reload created event for further processing
         */
        Event createdEvent = loadEventData(newEvent.getId());
        /*
         * insert passed alarms for calendar user, apply default alarms for other internal user attendees & setup corresponding alarm triggers
         */
        storeAlarmData(createdEvent, getUserIDs(createdEvent.getAttendees()), eventData);
        /*
         * track creation & return result
         */
        resultTracker.trackCreation(createdEvent);
        return createdEvent;
    }
    
    /**
     * Prepares a new event based on the client supplied event data prior inserting it into the storage. A new identifier for the event is
     * acquired from the storage and set in the event data implicitly.
     * <p/>
     * In case the event is part of an existing calendar object resource (i.e. it belongs to the passed existing events), the series
     * identifier is taken over accordingly. Otherwise, if the event denotes a series master event or overridden instance, a new series
     * identifier is generated and assigned.
     * 
     * @param eventData The event data as passed from the client
     * @param existingEvents A list of already stored events from the same calendar object resource (with the same UID),
     *            or <code>null</code> if there are none
     * @return The prepared event
     */
    protected Event prepareNewEvent(Event eventData, List<Event> existingEvents) throws OXException {
        Event event = new Event();
        /*
         * identifiers
         */
        event.setId(storage.getEventStorage().nextId());
        if (false == eventData.containsUid() || Strings.isEmpty(eventData.getUid())) {
            event.setUid(UUID.randomUUID().toString());
        } else if (isNullOrEmpty(existingEvents)) {
            event.setUid(Check.uidIsUnique(session, storage, eventData, calendarUserId));
        } else {
            event.setUid(existingEvents.get(0).getUid());
        }
        /*
         * creation/modification/calendaruser metadata
         */
        Consistency.setCreated(session, timestamp, event, session.getUserId());
        Consistency.setModified(session, timestamp, event, session.getUserId());
        Consistency.setCalenderUser(session, folder, event);
        /*
         * date/time related properties
         */
        Check.startAndEndDate(session, eventData);
        EventMapper.getInstance().copy(eventData, event, EventField.START_DATE, EventField.END_DATE);
        Consistency.adjustAllDayDates(event);
        Consistency.adjustTimeZones(session.getSession(), calendarUserId, event, isNullOrEmpty(existingEvents) ? null : existingEvents.get(0));
        /*
         * attendees, attachments, conferences
         */
        event.setAttendees(Check.maxAttendees(getSelfProtection(), InternalAttendeeUpdates.onNewEvent(session, folder, eventData, timestamp).getAddedItems()));
        event.setAttachments(Check.attachmentsAreVisible(session, storage, eventData.getAttachments()));
        event.setConferences(prepareConferences(Check.maxConferences(getSelfProtection(), eventData.getConferences())));
        /*
         * classification, transparency, color, geo
         */
        if (eventData.containsClassification() && null != eventData.getClassification()) {
            event.setClassification(Check.classificationIsValid(eventData.getClassification(), folder, event.getAttendees()));
        } else {
            event.setClassification(Classification.PUBLIC);
        }
        event.setTransp(eventData.containsTransp() && null != eventData.getTransp() ? eventData.getTransp() : TimeTransparency.OPAQUE);
        event.setColor(eventData.containsColor() ? eventData.getColor() : null);
        if (eventData.containsGeo()) {
            event.setGeo(Check.geoLocationIsValid(eventData));
        }
        /*
         * recurrence related fields
         */        
        if (eventData.containsRecurrenceRule() && null != eventData.getRecurrenceRule() || 
            eventData.containsRecurrenceDates() && false == isNullOrEmpty(eventData.getRecurrenceDates())) {
            /*
             * series master event, ensure to take over an existing series identifier as object identifier, as well as known exception dates
             */
            if (false == isNullOrEmpty(existingEvents)) {
                event.setId(existingEvents.get(0).getSeriesId());
                event.setChangeExceptionDates(getRecurrenceIds(existingEvents));
            }
            event.setSeriesId(event.getId());
            EventMapper.getInstance().copy(eventData, event, EventField.RECURRENCE_RULE, EventField.RECURRENCE_ID, EventField.RECURRENCE_DATES);
            Consistency.adjustRecurrenceRule(event);
            event.setRecurrenceRule(Check.recurrenceRuleIsValid(session.getRecurrenceService(), event));
            event.setSeriesId(event.getId());
            event.setRecurrenceDates(eventData.getRecurrenceDates());
            event.setDeleteExceptionDates(Check.recurrenceIdsExist(session.getRecurrenceService(), event, eventData.getDeleteExceptionDates()));
            Consistency.normalizeRecurrenceIDs(event.getStartDate(), event);
        } else if (eventData.containsRecurrenceId() && null != eventData.getRecurrenceId()) {
            /*
             * change exception event, ensure to assign an appropriate series identifier as well
             */
            event.setSeriesId(isNullOrEmpty(existingEvents) ? storage.getEventStorage().nextId() : existingEvents.get(0).getSeriesId());
            RecurrenceId normalizedRecurrenceId = normalizeRecurrenceID(event.getStartDate(), eventData.getRecurrenceId());
            event.setRecurrenceId(normalizedRecurrenceId);
            event.setChangeExceptionDates(new TreeSet<RecurrenceId>(Collections.singleton(normalizedRecurrenceId)));
        }
        /*
         * copy over further (unchecked) event fields
         */
        event = EventMapper.getInstance().copy(eventData, event,
            EventField.SUMMARY, EventField.LOCATION, EventField.DESCRIPTION, EventField.CATEGORIES, EventField.FILENAME, EventField.URL,
            EventField.RELATED_TO, EventField.STATUS, EventField.EXTENDED_PROPERTIES
        );
        /*
         * set scheduling related fields / parent folder information
         */
        if (isNullOrEmpty(event.getAttendees())) {
            /*
             * not group-scheduled event (only on a single user's calendar), apply parent folder identifier
             */
            event.setFolderId(folder.getId());
        } else {
            /*
             * group-scheduled event, assign & check organizer, sequence number and dynamic parent-folder identifier (for non-public folders)
             */
            event.setOrganizer(prepareOrganizer(session, folder, eventData.getOrganizer(), getResolvableEntities(session, folder, eventData)));
            Check.internalOrganizerIsAttendee(event, folder);
            if (isSeriesException(event) && isInternal(event.getOrganizer(), CalendarUserType.INDIVIDUAL)) {
                throw CalendarExceptionCodes.INVALID_DATA.create(EventField.ORGANIZER, "Cannot create detached occurrence for internally organized events");
            }
            event.setSequence(eventData.containsSequence() ? eventData.getSequence() : 0);
            event.setFolderId(PublicType.getInstance().equals(folder.getType()) ? folder.getId() : null);
            event.setAttendeePrivileges(eventData.containsAttendeePrivileges() ? Check.attendeePrivilegesAreValid(eventData.getAttendeePrivileges(), folder, event.getOrganizer()) : null);
        }
        return event;
    }

    /**
     * Stores alarm data and setups corresponding triggers for a newly created event. The inserted alarms are taken from the client-
     * supplied event data for the actual calendar user on the one hand, and from the configured default alarms of all other users on the
     * other hand.
     * 
     * @param createdEvent The newly stored event
     * @param userIDs The identifiers of the users to store the alarms and triggers for
     * @param eventData The client-supplied event data to consider for the alarms of the current calendar user
     */
    protected void storeAlarmData(Event createdEvent, int[] userIDs, Event eventData) throws OXException {
        Map<Integer, List<Alarm>> alarmsPerUserId = new HashMap<Integer, List<Alarm>>(userIDs.length);
        for (int userId : userIDs) {
            if (calendarUserId == userId && eventData.containsAlarms()) {
                List<Alarm> alarms = Check.maxAlarms(getSelfProtection(), Check.alarmsAreValid(eventData.getAlarms()));
                alarmsPerUserId.put(I(userId), insertAlarms(createdEvent, userId, alarms, false));
            } else {
                List<Alarm> defaultAlarm = isAllDay(createdEvent) ? session.getConfig().getDefaultAlarmDate(userId) : session.getConfig().getDefaultAlarmDateTime(userId);
                if (null != defaultAlarm) {
                    alarmsPerUserId.put(I(userId), insertAlarms(createdEvent, userId, defaultAlarm, true));
                }
            }
        }
        storage.getAlarmTriggerStorage().insertTriggers(createdEvent, alarmsPerUserId);
    }

}
