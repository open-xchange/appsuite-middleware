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

package com.openexchange.chronos.alarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.alarm.storage.AlarmTriggerStorage;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.service.CalendarEvent;
import com.openexchange.chronos.service.CalendarHandler;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;

/**
 * {@link AlarmCalendarHandler} handles all event changes and updates alarms accordingly
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmCalendarHandler implements CalendarHandler {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlarmCalendarHandler.class);
    private final AlarmTriggerStorage storage;
    private final RecurrenceService recurrenceService;

    /**
     * Initializes a new {@link AlarmCalendarHandler}.
     */
    public AlarmCalendarHandler(AlarmTriggerStorage storage, RecurrenceService recurrenceService) {
        super();
        this.storage = storage;
        this.recurrenceService = recurrenceService;
    }

    @Override
    public void handle(CalendarEvent event) {
        try {
            handleCreate(event);
            handleDelete(event);
            handleUpdate(event);
        } catch (OXException e) {
            LOG.error("Error while handling calendar result for alarm generation", e);
        }
    }

    private void handleCreate(CalendarEvent event) throws OXException {
        int contextId = event.getContextId();
        List<CreateResult> creations = event.getCreations();
        if (creations != null && !creations.isEmpty()) {

            for (CreateResult createResult : creations) {
                //TODO: get internal user attendees & create triggers for their alarms
                //                createAlarms(contextId, event.getCalendarUser(), createResult.getCreatedEvent());
            }
        }

    }

    private void createAlarms(Integer contextId, Integer calUser, Event eve) throws OXException {
        if (eve.containsAlarms()) {
            for (Alarm alarm : eve.getAlarms()) {
                AlarmTrigger trigger = new AlarmTrigger();
                trigger.setAccount(0);
                trigger.setUserId(calUser);
                trigger.setContextId(contextId);
                trigger.setAction(alarm.getAction().getValue());
                trigger.setProcessed(false);
                trigger.setAlarm(alarm.getId());

                if (eve.containsRecurrenceRule()) {
                    long[] exceptions = null;
                    if (eve.containsDeleteExceptionDates()) {
                        SortedSet<RecurrenceId> deleteExceptionDates = eve.getDeleteExceptionDates();
                        exceptions = new long[deleteExceptionDates.size()];
                        int x = 0;
                        for (RecurrenceId id : deleteExceptionDates) {
                            exceptions[x++] = id.getValue().getTimestamp();
                        }
                    }
                    RecurrenceData data = new DefaultRecurrenceData(eve.getRecurrenceRule(), eve.getStartDate(), exceptions);
                    RecurrenceIterator<RecurrenceId> iterateRecurrenceIds = recurrenceService.iterateRecurrenceIds(data, new Date(), null);
                    trigger.setRecurrence(String.valueOf(iterateRecurrenceIds.next().getValue().getTimestamp()));
                }
                trigger.setTime(AlarmUtils.getTriggerTime(alarm.getTrigger(), eve, TimeZone.getTimeZone("UTC")).getTime());
                storage.insertAlarmTrigger(trigger);
            }
        }
    }

    private void handleDelete(CalendarEvent event) throws OXException {
        int contextId = event.getContextId();
        List<DeleteResult> deletions = event.getDeletions();
        if (deletions != null && !deletions.isEmpty()) {

            List<Integer> alarmsToDelete = new ArrayList<>(deletions.size());
            for (DeleteResult deleteResult : deletions) {
                EventID eve = deleteResult.getEventID();
                //TODO: delete all triggers by event id / recurrence id
            }
            if (alarmsToDelete.size() > 0) {
                storage.deleteAlarmTriggers(contextId, 0, alarmsToDelete);
            }
        }
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

    private void handleUpdate(CalendarEvent event) throws OXException {
        int contextId = event.getContextId();
        List<UpdateResult> updates = event.getUpdates();
        if (updates != null && !updates.isEmpty()) {

            for (UpdateResult updateResult : updates) {

                //TODO: don't expect individual alarms to be contained in results

                Event eve = updateResult.getUpdate();
                Set<EventField> updatedFields = updateResult.getUpdatedFields();
                if (Collections.disjoint(updatedFields, RELEVANT_FIELDS)) {
                    // Ignore updates which doesn't influence alarms
                    continue;
                }

                // First delete all old trigger
                Event old = updateResult.getOriginal();
                List<Integer> triggerToDelete = new ArrayList<>(old.getAlarms().size());
                for (Alarm oldAlarm : old.getAlarms()) {
                    triggerToDelete.add(oldAlarm.getId());
                }
                storage.deleteAlarmTriggers(contextId, 0, triggerToDelete);

                // Then create new alarms from scratch
                //                createAlarms(contextId, event.getCalendarUser(), eve);

            }
        }
    }

}
