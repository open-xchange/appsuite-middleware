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

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link UpdateAlarmsPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdateAlarmsPerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link UpdateAlarmsPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public UpdateAlarmsPerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Initializes a new {@link UpdateAlarmsPerformer}, taking over the settings from another update performer.
     *
     * @param updatePerformer The update performer to take over the settings from
     */
    protected UpdateAlarmsPerformer(AbstractUpdatePerformer updatePerformer) {
        super(updatePerformer);
    }

    /**
     * Performs the alarm update in an event.
     *
     * @param objectId The identifier of the event to update the alarms for
     * @param recurrenceId The recurrence identifier of the occurrence to update, or <code>null</code> if no specific occurrence is targeted
     * @param alarms The updated list of alarms to apply, or <code>null</code> to remove any previously stored alarms
     * @param clientTimestamp The last timestamp / sequence number known by the client to catch concurrent updates
     * @return The result
     */
    public InternalCalendarResult perform(String objectId, RecurrenceId recurrenceId, List<Alarm> alarms, Long clientTimestamp) throws OXException {
        /*
         * load original event data & alarms
         */
        Event originalEvent = loadEventData(objectId);
        List<Event> exceptions = null;
        if (null != clientTimestamp) {
            requireUpToDateTimestamp(originalEvent, clientTimestamp.longValue());
        }

        if (null != recurrenceId) {
            if (isSeriesMaster(originalEvent)) {
				if (contains(originalEvent.getChangeExceptionDates(), recurrenceId)) {
					recurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalEvent, recurrenceId);
					Event originalExceptionEvent = loadExceptionData(originalEvent, recurrenceId);
					originalEvent = originalExceptionEvent;
				} else {
					resultTracker.rememberOriginalEvent(originalEvent);
					/*
		             * update for new change exception; prepare & insert a plain exception first, based on the original data from the master
		             */
		            Map<Integer, List<Alarm>> seriesMasterAlarms = storage.getAlarmStorage().loadAlarms(originalEvent);
		            Event newExceptionEvent = prepareException(originalEvent, recurrenceId);
		            Map<Integer, List<Alarm>> newExceptionAlarms = prepareExceptionAlarms(seriesMasterAlarms);
		            Check.quotaNotExceeded(storage, session);
		            storage.getEventStorage().insertEvent(newExceptionEvent);
		            storage.getAttendeeStorage().insertAttendees(newExceptionEvent.getId(), originalEvent.getAttendees());
		            storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getId(), newExceptionEvent.getId(), originalEvent.getAttachments());
                    storage.getConferenceStorage().insertConferences(newExceptionEvent.getId(), prepareConferences(originalEvent.getConferences()));
		            insertAlarms(newExceptionEvent, newExceptionAlarms, true);
		            newExceptionEvent = loadEventData(newExceptionEvent.getId());
		            resultTracker.trackCreation(newExceptionEvent, originalEvent);
		            /*
		             * perform alarm update & track results
		             */
                    resultTracker.rememberOriginalEvent(newExceptionEvent);
                    List<Alarm> originalAlarms = storage.getAlarmStorage().loadAlarms(originalEvent, calendarUserId);
                    updateAlarms(newExceptionEvent, calendarUserId, originalAlarms, alarms);
                    Event updatedExceptionEvent = loadEventData(newExceptionEvent.getId());
                    resultTracker.trackUpdate(newExceptionEvent, loadEventData(newExceptionEvent.getId()));
                    /*
                     * add change exception date to series master & track results
                     */
                    addChangeExceptionDate(originalEvent, recurrenceId, false);
	                Event updatedMasterEvent = loadEventData(originalEvent.getId());
	                resultTracker.trackUpdate(originalEvent, updatedMasterEvent);
                    /*
                     * reset alarm triggers for series master event and new change exception
                     */
	                storage.getAlarmTriggerStorage().deleteTriggers(updatedMasterEvent.getId());
	                storage.getAlarmTriggerStorage().insertTriggers(updatedMasterEvent, seriesMasterAlarms);
                    storage.getAlarmTriggerStorage().deleteTriggers(updatedExceptionEvent.getId());
                    storage.getAlarmTriggerStorage().insertTriggers(updatedExceptionEvent, storage.getAlarmStorage().loadAlarms(updatedExceptionEvent));
		            return resultTracker.getResult();
				}
            } else if (false == isSeriesException(originalEvent)) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(objectId, recurrenceId);
            }
        } else {
            if (isSeriesMaster(originalEvent)) {
                List<Event> exceptionData = loadExceptionData(originalEvent);
                exceptions = new ArrayList<>(exceptionData.size());
                for(Event eve: exceptionData) {
                    if (Utils.isInFolder(eve, folder)) {
                        exceptions.add(storage.getUtilities().loadAdditionalEventData(calendarUserId, EventMapper.getInstance().copy(eve, null, (EventField[]) null), new EventField[] { EventField.ALARMS }));
                    }
                }
            }
        }

        resultTracker.rememberOriginalEvent(originalEvent);
        List<Alarm> originalAlarms = storage.getAlarmStorage().loadAlarms(originalEvent, calendarUserId);
        /*
         * perform alarm update & track results
         */
        if (updateAlarms(originalEvent, calendarUserId, originalAlarms, alarms)) {
            if (exceptions != null) {
                // Propagate alarm changes to exceptions
                Map<Event, List<Alarm>> alarmToUpdate = AlarmUpdateProcessor.getUpdatedExceptions(originalAlarms, alarms == null ? Collections.emptyList() : alarms, exceptions);
                for(Entry<Event, List<Alarm>> entry: alarmToUpdate.entrySet()) {
                    updateAlarms(entry.getKey(), calendarUserId, entry.getKey().getAlarms(), entry.getValue());
                }
            }

            touch(originalEvent.getId());
            resultTracker.trackUpdate(originalEvent, loadEventData(originalEvent.getId()));
        }
        return resultTracker.getResult();
    }

}
