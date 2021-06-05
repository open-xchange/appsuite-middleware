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

package com.openexchange.chronos.impl.scheduling;

import static com.openexchange.chronos.common.CalendarUtils.getEventIDs;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.performer.AbstractUpdatePerformer;
import com.openexchange.chronos.impl.performer.UpdateAttendeePerformer;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.exception.OXException;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link AttendeeUpdateProcessor} - Updates the status for an participant on events that
 * were created or updated before.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class AttendeeUpdateProcessor extends AbstractUpdatePerformer {

    protected final SchedulingMethod method;

    /**
     * 
     * Initializes a new {@link AttendeeUpdateProcessor}.
     * 
     * @param perfomer The existing performer
     * @param method The method being processed
     */
    public AttendeeUpdateProcessor(AbstractUpdatePerformer perfomer, SchedulingMethod method) {
        super(perfomer);
        this.method = method;
    }

    /**
     * Updates the attendee status
     *
     * @param attendee The attendee to gain information for the update from
     * @return The calendar result
     * @throws OXException In case update isn't successful
     */
    public InternalCalendarResult process(Attendee attendee) throws OXException {
        return process(attendee.getPartStat(), attendee.getComment());
    }

    /**
     * Updates the attendee status
     *
     * @param partStat The status to set
     * @param comment optional comment for the attendee to set
     * @return The calendar result
     * @throws OXException In case update isn't successful
     */
    public InternalCalendarResult process(ParticipationStatus partStat, String comment) throws OXException {
        /*
         * Check if applicable
         */
        InternalCalendarResult result = resultTracker.getResult();
        if (false == SchedulingMethod.REQUEST.equals(method) && false == SchedulingMethod.ADD.equals(method)) {
            return result;
        }
        if (ParticipationStatus.NEEDS_ACTION.matches(partStat)) {
            return result;
        }
        CalendarResult userizedResult = result.getUserizedResult();
        if (isNullOrEmpty(userizedResult.getCreations()) && isNullOrEmpty(userizedResult.getUpdates())) {
            return result;
        }
        /*
         * Update
         */
        return update(partStat, comment, result.getUserizedResult());
    }

    /*
     * ============================== HELPERS ==============================
     */

    private InternalCalendarResult update(ParticipationStatus partStat, String comment, CalendarResult userizedResult) throws OXException {
        List<Event> events = new LinkedList<>();
        Attendee update = prepareAttendeeUpdate(partStat, comment);
        /*
         * Update status in all updated and rescheduled events, keep part-stat of non-rescheduled events as user doesn't intend to change status in those
         */
        List<UpdateResult> updates = userizedResult.getUpdates();
        if (false == Collections.isNullOrEmpty(updates)) {
            events.addAll(updates.stream() //@formatter:off
                .filter(u -> Utils.isReschedule(u))
                .map(u -> u.getUpdate())
                .collect(Collectors.toList())); //@formatter:on
        }
        /*
         * Get all created events to update
         */
        List<CreateResult> creations = userizedResult.getCreations();
        if (false == Collections.isNullOrEmpty(creations)) {
            events.addAll(creations.stream().map(c -> c.getCreatedEvent()).collect(Collectors.toList()));
        }

        /*
         * Update and return result
         */
        return new UpdateAttendeePerformer(this).perform(getEventIDs(events), update, null);
    }

    /**
     * Prepares an attendee update for the current calendar user
     *
     * @param partStat The participant status to set
     * @param comment The optional comment to set
     * @return The attendee for the update
     */
    private Attendee prepareAttendeeUpdate(ParticipationStatus partStat, String comment) {
        Attendee update = new Attendee();
        update.setEntity(calendarUserId);
        update.setPartStat(partStat);
        update.setComment(comment);
        update.setTimestamp(timestamp.getTime());
        return update;
    }

}
