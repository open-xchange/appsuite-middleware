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

package com.openexchange.chronos.trigger;

import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getDateInTimeZone;
import static com.openexchange.chronos.common.CalendarUtils.getTriggerDuration;
import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isFloating;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Collections.put;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AlarmTriggerHandler}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class AlarmTriggerHandler implements CalendarHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlarmTriggerHandler.class);

    private static final EventField[] TRIGGER_RELATED_EVENT_FIELDS = {
        EventField.PUBLIC_FOLDER_ID, EventField.ALL_DAY, EventField.START_DATE, EventField.END_DATE, EventField.START_TIMEZONE,
        EventField.END_TIMEZONE, EventField.RECURRENCE_ID
    };

    private static final AttendeeField[] TRIGGER_RELATED_ATTENDEE_FIELDS = { AttendeeField.PARTSTAT, AttendeeField.FOLDER_ID };

    private final ServiceLookup services;

    /**
     * Initializes a new {@link AlarmTriggerHandler}.
     *
     * @param services A service lookup reference
     */
    public AlarmTriggerHandler(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public void eventCreated(CreateResult result) {
        try {
            List<Attendee> userAttendees = filter(result.getCreatedEvent().getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL);
            List<ReminderTrigger> triggers = prepareTriggers(result.getSession(), result.getCreatedEvent(), userAttendees);
            if (0 < triggers.size()) {
                insertTriggers(result.getSession().getContext(), triggers);
            }
        } catch (OXException e) {
            LOG.error("Error handling alarm triggers for created event {}:{} ", result.getCreatedEvent(), e.getMessage(), e);
        }
    }

    @Override
    public void eventUpdated(UpdateResult result) {
        try {
            if (result.containsAnyChangeOf(TRIGGER_RELATED_EVENT_FIELDS)) {
                /*
                 * date/time-related property changed; re-create triggers of all actual attendees & remove triggers for deleted attendees
                 */
                List<Attendee> newAttendees = filter(result.getUpdate().getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL);
                Map<Integer, List<ReminderTrigger>> triggersPerUser = prepareTriggersPerUser(result.getSession(), result.getUpdate(), newAttendees);
                for (Attendee originalAttendee : filter(result.getOriginal().getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                    if (false == triggersPerUser.containsKey(I(originalAttendee.getEntity()))) {
                        triggersPerUser.put(I(originalAttendee.getEntity()), Collections.<ReminderTrigger> emptyList());
                    }
                }
                if (0 < triggersPerUser.size()) {
                    replaceTriggers(result.getSession().getContext(), result.getUpdate().getId(), triggersPerUser);
                }
            } else {
                if (false == result.getAttendeeUpdates().isEmpty()) {
                    /*
                     * attendees changed; adjust triggers based on attendee updates
                     */
                    adjustTriggers(result.getSession(), result.getUpdate(), result.getAttendeeUpdates());
                }
                if (false == result.getAlarmUpdates().isEmpty()) {
                    /*
                     * alarms of the calendar user changed, ensure to adjust corresponding triggers
                     */
                    //TODO: selective update
                    Map<Integer, List<ReminderTrigger>> triggersPerUser;
                    Attendee userAttendee = find(result.getUpdate().getAttendees(), result.getCalendarUser().getId());
                    if (null != userAttendee) {
                        triggersPerUser = prepareTriggersPerUser(result.getSession(), result.getUpdate(), Collections.singletonList(userAttendee));
                    } else {
                        triggersPerUser = new HashMap<Integer, List<ReminderTrigger>>();
                    }
                    if (false == triggersPerUser.containsKey(I(userAttendee.getEntity()))) {
                        triggersPerUser.put(I(userAttendee.getEntity()), Collections.<ReminderTrigger> emptyList());
                    }
                    replaceTriggers(result.getSession().getContext(), result.getUpdate().getId(), triggersPerUser);
                }
            }
        } catch (OXException e) {
            LOG.error("Error handling alarm triggers for updated event {}:{} ", result.getUpdate(), e.getMessage(), e);
        }
    }

    @Override
    public void eventDeleted(DeleteResult result) {
        try {
            deleteTriggers(result.getSession().getContext(), result.getDeletedEvent().getId());
        } catch (OXException e) {
            LOG.error("Error handling alarm triggers for deleted event {}:{} ", result.getDeletedEvent(), e.getMessage(), e);
        }
    }

    private void insertTriggers(Context context, List<ReminderTrigger> triggers) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbService.getWritable(context);
            connection.setAutoCommit(false);
            updated += new TriggerStorage(context, connection).insertTriggers(triggers);
            connection.commit();
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (null != connection) {
                Databases.autocommit(connection);
                if (0 < updated) {
                    dbService.backWritable(context, connection);
                } else {
                    dbService.backWritableAfterReading(context, connection);
                }
            }
        }
    }

    private void replaceTriggers(Context context, int objectID, Map<Integer, List<ReminderTrigger>> triggersPerUser) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbService.getWritable(context);
            connection.setAutoCommit(false);
            TriggerStorage triggerStorage = new TriggerStorage(context, connection);
            updated += triggerStorage.replaceTriggers(objectID, triggersPerUser);
            connection.commit();
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (null != connection) {
                Databases.autocommit(connection);
                if (0 < updated) {
                    dbService.backWritable(context, connection);
                } else {
                    dbService.backWritableAfterReading(context, connection);
                }
            }
        }
    }

    private void deleteTriggers(Context context, int objectID) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection connection = null;
        int updated = 0;
        try {
            connection = dbService.getWritable(context);
            connection.setAutoCommit(false);
            TriggerStorage triggerStorage = new TriggerStorage(context, connection);
            updated += triggerStorage.removeTriggers(objectID);
            connection.commit();
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (null != connection) {
                Databases.autocommit(connection);
                if (0 < updated) {
                    dbService.backWritable(context, connection);
                } else {
                    dbService.backWritableAfterReading(context, connection);
                }
            }
        }
    }

    private void adjustTriggers(CalendarSession session, Event updatedEvent, CollectionUpdate<Attendee, AttendeeField> attendeeUpdates) throws OXException {
        int updated = 0;
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection connection = null;
        try {
            connection = dbService.getWritable(session.getContext());
            connection.setAutoCommit(false);
            TriggerStorage triggerStorage = new TriggerStorage(session.getContext(), connection);
            for (Attendee removedAttendee : filter(attendeeUpdates.getRemovedItems(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                updated += triggerStorage.removeTriggers(updatedEvent.getId(), removedAttendee.getEntity());
            }
            List<ReminderTrigger> newTriggers = prepareTriggers(session, updatedEvent, filter(attendeeUpdates.getAddedItems(), Boolean.TRUE, CalendarUserType.INDIVIDUAL));
            if (0 < newTriggers.size()) {
                updated += triggerStorage.insertTriggers(newTriggers);
            }
            List<Attendee> updatedAttendees = getAttendeesRequiringNewTrigger(attendeeUpdates.getUpdatedItems());
            List<ReminderTrigger> updatedTriggers = prepareTriggers(session, updatedEvent, updatedAttendees);
            if (0 < newTriggers.size()) {
                updated += triggerStorage.replaceTriggers(updatedTriggers);
            }
            connection.commit();
        } catch (SQLException e) {
            throw new OXException(e);
        } finally {
            if (null != connection) {
                Databases.autocommit(connection);
                if (0 < updated) {
                    dbService.backWritable(session.getContext(), connection);
                } else {
                    dbService.backWritableAfterReading(session.getContext(), connection);
                }
            }
        }
    }

    private List<ReminderTrigger> prepareTriggers(CalendarSession session, Event event, List<Attendee> userAttendees) throws OXException {
        Map<Integer, List<ReminderTrigger>> triggersPerUser = prepareTriggersPerUser(session, event, userAttendees);
        if (0 == triggersPerUser.size()) {
            return Collections.emptyList();
        }
        List<ReminderTrigger> triggers = new ArrayList<ReminderTrigger>();
        for (List<ReminderTrigger> value : triggersPerUser.values()) {
            triggers.addAll(value);
        }
        return triggers;
    }

    /**
     * Determines the next reminder triggers for a specific event based on the alarm settings of each user attendee.
     *
     * @param context The context
     * @param event The event to calculate the next reminder trigger for
     * @param userAttendees The user attendees to consider
     * @return The reminder triggers of each user, mapped to the user identifier
     */
    private Map<Integer, List<ReminderTrigger>> prepareTriggersPerUser(CalendarSession session, Event event, List<Attendee> userAttendees) throws OXException {
        /*
         * load alarms per user
         */
        Map<Integer, List<ReminderTrigger>> triggersPerUser = new HashMap<Integer, List<ReminderTrigger>>(userAttendees.size());
        CalendarStorage storage = services.getService(CalendarStorageFactory.class).create(session.getContext(), session.getEntityResolver()); //dbprovider?
        Map<Integer, List<Alarm>> alarmsByUser = storage.getAlarmStorage().loadAlarms(event.getId());
        if (null == alarmsByUser || 0 == alarmsByUser.size()) {
            return triggersPerUser;
        }
        boolean forSeries = isSeriesMaster(event);
        if (false == isFloating(event)) {
            /*
             * collect triggers for next occurrence of event in common timezone
             */
            TimeZone timeZone = null != event.getStartTimezone() ? TimeZone.getTimeZone(event.getStartTimezone()) : session.getEntityResolver().getTimeZone(event.getCreatedBy());
            Event occurrence = getNextOccurrence(event, timeZone);
            if (null == occurrence) {
                return triggersPerUser;
            }
            for (Attendee attendee : userAttendees) {
                List<Alarm> alarms = alarmsByUser.get(I(attendee.getEntity()));
                if (null != alarms && 0 < alarms.size()) {
                    put(triggersPerUser, I(attendee.getEntity()), prepareTriggers(occurrence, attendee, timeZone, alarms, forSeries));
                }
            }
        } else {
            /*
             * collect triggers for next occurrence of event in individual attendee timezone
             */
            for (Attendee attendee : userAttendees) {
                List<Alarm> alarms = alarmsByUser.get(I(attendee.getEntity()));
                if (null != alarms && 0 < alarms.size()) {
                    TimeZone timeZone = session.getEntityResolver().getTimeZone(attendee.getEntity());
                    Event occurrence = getNextOccurrence(event, timeZone);
                    if (null != occurrence) {
                        put(triggersPerUser, I(attendee.getEntity()), prepareTriggers(occurrence, attendee, timeZone, alarms, forSeries));
                    }
                }
            }
        }
        return triggersPerUser;
    }

    /**
     * Calculates and prepares the attendee's next reminder triggers for a specific event occurrence.
     *
     * @param event The event to calculate the triggers for
     * @param attendee The attendee to calculate the triggers for
     * @param timeZone The timezone to consider
     * @param alarms The configured alarms for the event
     * @param forSeries <code>true</code> if the event is part of a recurring event series, <code>false</code>, otherwise
     * @return The reminder triggers, or an empty list if there are none
     */
    private List<ReminderTrigger> prepareTriggers(Event event, Attendee attendee, TimeZone timeZone, List<Alarm> alarms, boolean forSeries) throws OXException {
        if (null == alarms || 0 == alarms.size()) {
            return Collections.emptyList();
        }
        List<ReminderTrigger> triggers = new ArrayList<ReminderTrigger>(alarms.size());
        int folderID = 0 < event.getPublicFolderId() ? event.getPublicFolderId() : attendee.getFolderID();
        for (Alarm alarm : alarms) {
            Date triggerDate = getTriggerDate(event, alarm.getTrigger(), timeZone);
            if (null != triggerDate) {
                triggers.add(new ReminderTrigger(folderID, event.getId(), attendee.getEntity(), triggerDate, forSeries));
            }
        }
        return triggers;
    }

    private static Date getTriggerDate(Event occurrence, Trigger trigger, TimeZone timeZone) {
        if (null == trigger) {
            return null;
        }
        if (null != trigger.getDateTime()) {
            return trigger.getDateTime();
        }
        if (null == trigger.getDuration()) {
            return null;
        }
        long duration = getTriggerDuration(trigger.getDuration());
        Date relatedDate = Trigger.Related.END.equals(trigger.getRelated()) ? occurrence.getEndDate() : occurrence.getStartDate();
        if (occurrence.isAllDay()) {
            relatedDate = getDateInTimeZone(relatedDate, timeZone);
        }
        Date nextTrigger = new Date(relatedDate.getTime() + duration);
        return nextTrigger.after(new Date()) ? nextTrigger : null;
    }

    private Event getNextOccurrence(Event event, TimeZone timeZone) {
        Date now = new Date();
        if (false == isSeriesMaster(event)) {
            return now.before(event.getStartDate()) ? event : null;
        }
        Calendar calendar = initCalendar(timeZone, now);
        Iterator<Event> occurrenceIterator = services.getService(RecurrenceService.class).calculateInstances(event, calendar, null, 1);
        if (false == occurrenceIterator.hasNext()) {
            return null;
        }
        return occurrenceIterator.next();
    }

    private static List<Attendee> getAttendeesRequiringNewTrigger(List<ItemUpdate<Attendee, AttendeeField>> attendeeUpdates) {
        List<Attendee> attendees = new ArrayList<Attendee>();
        for (ItemUpdate<Attendee, AttendeeField> attendeeUpdate : attendeeUpdates) {
            if (0 < attendeeUpdate.getUpdate().getEntity() && CalendarUserType.INDIVIDUAL.equals(attendeeUpdate.getUpdate().getCuType()) &&
                attendeeUpdate.containsAnyChangeOf(TRIGGER_RELATED_ATTENDEE_FIELDS)) {
                attendees.add(attendeeUpdate.getUpdate());
            }
        }
        return attendees;
    }

}
