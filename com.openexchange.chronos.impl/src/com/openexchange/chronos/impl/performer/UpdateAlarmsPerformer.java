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
        if (null != recurrenceId) {
            if (isSeriesMaster(originalEvent)) {
                recurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalEvent, recurrenceId);
                Event originalExceptionEvent = loadExceptionData(originalEvent, recurrenceId);
                originalEvent = originalExceptionEvent;
            } else if (false == isSeriesException(originalEvent)) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(objectId, recurrenceId);
            }
        } else {
            if (isSeriesMaster(originalEvent)) {
                List<Event> exceptionData = loadExceptionData(originalEvent);
                exceptions = new ArrayList<>(exceptionData.size());
                for(Event eve: exceptionData) {
                    exceptions.add(storage.getUtilities().loadAdditionalEventData(calendarUserId, EventMapper.getInstance().copy(eve, null, (EventField[]) null), null));
                }
            }
        }
        if (null != clientTimestamp) {
            requireUpToDateTimestamp(originalEvent, clientTimestamp.longValue());
        }
        resultTracker.rememberOriginalEvent(originalEvent);
        List<Alarm> originalAlarms = storage.getAlarmStorage().loadAlarms(originalEvent, calendarUserId);
        /*
         * perform alarm update & track results
         */
        if (updateAlarms(originalEvent, calendarUserId, originalAlarms, alarms)) {
            if(exceptions != null) {
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
