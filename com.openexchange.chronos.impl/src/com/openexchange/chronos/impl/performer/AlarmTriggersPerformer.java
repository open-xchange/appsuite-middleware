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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.AlarmUtils.isInRange;
import static com.openexchange.chronos.common.AlarmUtils.shiftIntoRange;
import static com.openexchange.chronos.common.CalendarUtils.asDate;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.SearchUtils.getSearchTerm;
import static com.openexchange.chronos.impl.Utils.copy;
import static com.openexchange.chronos.impl.Utils.extract;
import static com.openexchange.chronos.impl.Utils.getFolder;
import static com.openexchange.chronos.impl.Utils.getFrom;
import static com.openexchange.chronos.impl.Utils.getUntil;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_EXPAND_OCCURRENCES;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_END;
import static com.openexchange.chronos.service.CalendarParameters.PARAMETER_RANGE_START;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link AlarmTriggersPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class AlarmTriggersPerformer extends AbstractQueryPerformer {

    private static final Logger LOG = LoggerFactory.getLogger(AlarmTriggersPerformer.class.getName());

    /**
     * Initializes a new {@link AlarmTriggersPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public AlarmTriggersPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     * 
     * @param actions The alarm action to consider
     * @return The loaded alarm triggers, or an empty list if there are none
     */
    public List<AlarmTrigger> perform(Set<String> actions) throws OXException {
        /*
         * get pending alarm triggers from storage & filter by action type if required
         */
        Date rangeFrom = getFrom(session);
        Date rangeUntil = getUntil(session);
        List<AlarmTrigger> triggers = storage.getAlarmTriggerStorage().loadTriggers(session.getUserId(), rangeFrom, rangeUntil);
        if (null != actions) {
            triggers = AlarmUtils.filter(triggers, actions.toArray(new String[actions.size()]));
        }
        if (triggers.isEmpty()) {
            return triggers;
        }
        /*
         * load targeted events from storage for further checks
         */
        EventField[] requestedFields = getFields(new EventField[0], (EventField[]) null);
        EventField[] fields = getFieldsForStorage(requestedFields);
        List<Event> events = loadEvents(triggers, fields);
        /*
         * remove triggers where the targeted events are not or no longer accessible for the requesting user
         */
        CalendarParameters oldParameters = extract(session, true, PARAMETER_EXPAND_OCCURRENCES, PARAMETER_RANGE_START, PARAMETER_RANGE_END);
        try {
            EventPostProcessor postProcessor = postProcessor(getObjectIDs(events), session.getUserId(), requestedFields, fields);
            return filter(triggers, events, rangeFrom, rangeUntil, postProcessor);
        } finally {
            copy(oldParameters, session);
        }
    }

    /**
     * Removes alarm triggers that are referencing an event that cannot be accessed for any reason.
     *
     * @param triggers The list of triggers to filter the inaccessible ones from
     * @param events The referenced events as loaded from the storage
     * @param rangeFrom The lower boundary of the range for the trigger as requested by the client
     * @param rangeUntil The upper boundary of the range for the trigger as requested by the client
     * @param postProcessor The event post processor to use for userizing the loaded events
     * @return The filtered list of alarm triggers
     */
    private List<AlarmTrigger> filter(List<AlarmTrigger> triggers, List<Event> events, Date rangeFrom, Date rangeUntil, EventPostProcessor postProcessor) {
        for (Iterator<AlarmTrigger> iterator = triggers.iterator(); iterator.hasNext();) {
            AlarmTrigger trigger = iterator.next();
            try {
                CalendarFolder folder = getFolder(session, trigger.getFolder(), false);
                Event event = find(events, trigger.getEventId());
                if (null != event) {
                    event = postProcessor.process(event, folder).getFirstEvent();
                    postProcessor.reset();
                }
                if (null == event) {
                    throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(trigger.getEventId());
                }
                Check.eventIsVisible(folder, event);
                Check.eventIsInFolder(event, folder);
                if (null != trigger.getRecurrenceId()) {
                    if (isSeriesMaster(event)) {
                        /*
                         * iterate recurrence set to get event occurrence targeted by trigger
                         */
                        RecurrenceIterator<Event> recurrenceIterator = session.getRecurrenceService().iterateEventOccurrences(
                            event, asDate(trigger.getRecurrenceId().getValue()), null);
                        event = getTargetedOccurrence(recurrenceIterator, trigger);
                        if (null == event || false == trigger.getRecurrenceId().matches(event.getRecurrenceId())) {
                            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(trigger.getEventId(), trigger.getRecurrenceId());
                        }
                        /*
                         * shift the trigger to a more recent occurrence if trigger time is before requested range as needed
                         */
                        if (iterator.hasNext() && null != rangeFrom && rangeFrom.after(new Date(l(trigger.getTime())))) {
                            Alarm alarm = storage.getAlarmStorage().loadAlarm(i(trigger.getAlarm()));
                            if (null == alarm) {
                                throw CalendarExceptionCodes.ALARM_NOT_FOUND.create(trigger.getAlarm(), trigger.getEventId());
                            }
                            shiftIntoRange(trigger, alarm, recurrenceIterator, rangeFrom, rangeUntil);
                        }
                    }
                }
                /*
                 * re-check if trigger falls into requested range
                 */
                if (false == isInRange(trigger, rangeFrom, rangeUntil)) {
                    iterator.remove();
                }
            } catch (OXException e) {
                LOG.debug("Skipping trigger for alarm {} of event {} ({}).", trigger.getAlarm(), trigger.getEventId(), e.getMessage(), e);
                iterator.remove();
            }
        }
        return triggers;
    }

    /**
     * Loads the events referenced by the supplied alarm triggers from the storage.
     * 
     * @param alarmTriggers The alarm triggers to load the events for
     * @param fields The event fields to retrieve
     * @return The events, or an empty list if none were found
     */
    private List<Event> loadEvents(List<AlarmTrigger> alarmTriggers, EventField[] fields) throws OXException {
        if (null == alarmTriggers || 0 == alarmTriggers.size()) {
            return Collections.emptyList();
        }
        List<Event> events;
        if (1 == alarmTriggers.size()) {
            Event event = storage.getEventStorage().loadEvent(alarmTriggers.get(0).getEventId(), fields);
            if (null == event) {
                return Collections.emptyList();
            }
            events = Collections.singletonList(event);
        } else {
            CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
            for (AlarmTrigger alarmTrigger : alarmTriggers) {
                orTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, alarmTrigger.getEventId()));
            }
            events = storage.getEventStorage().searchEvents(orTerm, null, fields);
        }
        return storage.getUtilities().loadAdditionalEventData(session.getUserId(), events, fields);
    }

    private static Event getTargetedOccurrence(RecurrenceIterator<Event> iterator, AlarmTrigger trigger) throws OXException {
        Event targetedOccurrence = null;
        long recurrenceTimestamp = trigger.getRecurrenceId().getValue().getTimestamp();
        while (iterator.hasNext()) {
            Event occurrence = iterator.next();
            if (trigger.getRecurrenceId().matches(occurrence.getRecurrenceId())) {
                targetedOccurrence = occurrence;
                break;
            }
            if (occurrence.getRecurrenceId().getValue().getTimestamp() > recurrenceTimestamp) {
                break;
            }
        }
        if (null == targetedOccurrence || false == trigger.getRecurrenceId().matches(targetedOccurrence.getRecurrenceId())) {
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(trigger.getEventId(), trigger.getRecurrenceId());
        }
        return targetedOccurrence;
    }

}
