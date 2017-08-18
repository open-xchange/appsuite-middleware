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

package com.openexchange.chronos.alarm.impl;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.alarm.AlarmChange;
import com.openexchange.chronos.alarm.AlarmTriggerService;
import com.openexchange.chronos.alarm.EventSeriesWrapper;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.AlarmTriggerStorage;
import com.openexchange.exception.OXException;

/**
 * {@link AlarmTriggerServiceImpl} handles all event changes and updates alarms accordingly
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmTriggerServiceImpl implements AlarmTriggerService {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlarmTriggerServiceImpl.class);
    private final RecurrenceService recurrenceService;

    /**
     * Initializes a new {@link AlarmTriggerServiceImpl}.
     */
    public AlarmTriggerServiceImpl(RecurrenceService recurrenceService) {
        super();
        this.recurrenceService = recurrenceService;
    }

    @Override
    public void handleChange(AlarmChange change, AlarmTriggerStorage storage) {
        try {

            switch (change.getType()) {
                case CREATE:
                    handleCreate(change, storage);
                    break;
                case DELETE:
                    handleDelete(change, storage);
                    break;
                case UPDATE:
                    handleUpdate(change, storage);
                    break;
                default:
                    break;
            }
            LOG.debug("Processed {} change for event with id {}.", change.getType(), change.getOldEvent() != null ? change.getOldEvent().getEvent().getId() : change.getNewEvent().getEvent().getId());
        } catch (OXException e) {
            LOG.error("Error while handling calendar result for alarm generation", e);
        }

    }

    /**
     * Handles newly created alarms
     *
     * @param create The {@link AlarmChange} of type {@link AlarmChange.Type#CREATE}
     * @param storage The {@link AlarmTriggerStorage} to use
     * @throws OXException
     */
    private void handleCreate(AlarmChange create, AlarmTriggerStorage storage) throws OXException {
        createAlarmTriggers(create.getAlarmsPerAttendee(), create.getNewEvent(), storage);
    }

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * Creates new alarm triggers
     *
     * @param alarmsPerAttendee A map of alarms per attendee
     * @param eventWrapper The newly created event
     * @param storage The {@link AlarmTriggerStorage} to use
     * @throws OXException
     */
    private void createAlarmTriggers(Map<Integer, List<Alarm>> alarmsPerAttendee, EventSeriesWrapper eventWrapper, AlarmTriggerStorage storage) throws OXException {
        Event event = eventWrapper.getEvent();
        for(Integer userId: alarmsPerAttendee.keySet()){

            List<Alarm> alarms = alarmsPerAttendee.get(userId);
            if(alarms==null || alarms.isEmpty()){
                continue;
            }
            for (Alarm alarm : alarms) {
                AlarmTrigger trigger = new AlarmTrigger();
                trigger.setUserId(userId);
                trigger.setAction(alarm.getAction().getValue());
                trigger.setProcessed(false);
                trigger.setAlarm(alarm.getId());
                trigger.setEventId(event.getId());

                if (event.containsRecurrenceRule() && event.getRecurrenceRule() != null && event.getRecurrenceId() == null && event.getId().equals(event.getSeriesId())) {

                    long[] exceptions = null;
                    if (eventWrapper.getExceptions() != null) {
                        exceptions = new long[eventWrapper.getExceptions().size()];
                        int x = 0;
                        for (RecurrenceId recurrenceId : eventWrapper.getExceptions()) {
                            exceptions[x++] = recurrenceId.getValue().getTimestamp();
                        }
                    }
                    RecurrenceData data = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), exceptions);
                    RecurrenceIterator<RecurrenceId> iterateRecurrenceIds = recurrenceService.iterateRecurrenceIds(data, new Date(), null);
                    trigger.setRecurrence(String.valueOf(iterateRecurrenceIds.next().getValue().getTimestamp()));
                    trigger.setTime(AlarmUtils.getNextTriggerTime(event, alarm, new Date(), UTC, recurrenceService, eventWrapper.getExceptions()).getTime());
                } else {
                    if (event.getRecurrenceId() != null) {
                        trigger.setRecurrence(String.valueOf(event.getRecurrenceId().getValue().getTimestamp()));
                    }
                    trigger.setTime(AlarmUtils.getTriggerTime(alarm.getTrigger(), event, UTC).getTime());
                }

                // Set proper folder id
                for (Attendee att : event.getAttendees()) {
                    if (att.getEntity() == userId) {
                        trigger.setFolder(att.getFolderID());
                        break;
                    }
                }

                if (!trigger.containsFolder()) {
                    // TODO throw proper exception
                }

                storage.insertAlarmTrigger(trigger);
            }

        }

    }

    /**
     * Handles deleted alarms
     *
     * @param deletion The {@link AlarmChange} of type {@link AlarmChange.Type#DELETE}
     * @param storage The {@link AlarmTriggerStorage} to use
     * @throws OXException
     */
    private void handleDelete(AlarmChange deletion, AlarmTriggerStorage storage) throws OXException {

        EventSeriesWrapper deletedEvent = deletion.getOldEvent();
        EventID eventID = null;
        if (deletedEvent.getEvent().getRecurrenceId() != null && deletedEvent.getEvent().getId() == deletedEvent.getEvent().getSeriesId()) {
            eventID = new EventID(deletedEvent.getEvent().getFolderId(), deletedEvent.getEvent().getId(), deletedEvent.getEvent().getRecurrenceId());
        } else {
            eventID = new EventID(deletedEvent.getEvent().getFolderId(), deletedEvent.getEvent().getId());
        }
        storage.deleteAlarmTriggers(Collections.singletonList(eventID));
    }

    /**
     * A set of fields which is relevant for the update operation
     */
    private static final Set<EventField> RELEVANT_FIELDS = new HashSet<>();
    static {
        RELEVANT_FIELDS.add(EventField.ALARMS);
        RELEVANT_FIELDS.add(EventField.START_DATE);
        RELEVANT_FIELDS.add(EventField.END_DATE);
        RELEVANT_FIELDS.add(EventField.ATTENDEES);
        RELEVANT_FIELDS.add(EventField.DELETE_EXCEPTION_DATES);
        RELEVANT_FIELDS.add(EventField.RECURRENCE_RULE);
    }

    /**
     * Handles updated alarms
     *
     * @param update The {@link AlarmChange} of type {@link AlarmChange.Type#UPDATE}
     * @param storage The {@link AlarmTriggerStorage} to use
     * @throws OXException
     */
    private void handleUpdate(AlarmChange update, AlarmTriggerStorage storage) throws OXException {

        Set<EventField> updatedFields = update.getChangedFields();
        if (Collections.disjoint(updatedFields, RELEVANT_FIELDS)) {
            // Ignore updates which doesn't influence alarms
            return;
        }

        // First delete all old trigger
        Event old = update.getOldEvent().getEvent();
        storage.deleteAlarmTriggers(Collections.singletonList(new EventID(old.getFolderId(), old.getId(), old.getRecurrenceId())));

        // Then create new alarms from scratch
        createAlarmTriggers(update.getAlarmsPerAttendee(), update.getNewEvent(), storage);

    }

}
