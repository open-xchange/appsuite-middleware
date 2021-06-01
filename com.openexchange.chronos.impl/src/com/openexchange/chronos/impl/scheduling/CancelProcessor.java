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

import static com.openexchange.java.Autoboxing.I;
import static java.util.Collections.singletonList;
import java.util.Collections;
import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.performer.AbstractUpdatePerformer;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link CancelProcessor} - Handles incoming <code>CANCEL</code> message by external organizer and tries to apply
 * the delete the events transmitted from the message.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.5">RFC5546 Section 3.2.5</a>
 */
public class CancelProcessor extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link CancelProcessor}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @throws OXException If initialization fails
     */
    public CancelProcessor(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Deletes all events transmitted in the message.
     * <p>
     * There are some cases in which an scheduling message with a CANCEL is for multiple attendees
     * but does not delete each of those attendee in each transmitted event. Those cases will be
     * skipped implicit.
     *
     * @param message The message containing the events to delete
     * @return An {@link InternalCalendarResult} containing the updates
     * @throws OXException In case data is invalid, outdated or permissions are missing, especially
     *             if the organizer of the transmitted event doesn't match the remembered one
     */
    public InternalCalendarResult process(IncomingSchedulingMessage message) throws OXException {
        CalendarUser originator = message.getSchedulingObject().getOriginator();
        Event seriesMaster = message.getResource().getSeriesMaster();
        if (null != seriesMaster) {
            /*
             * Delete complete series, ignore transmitted exception as they get deleted in one transaction
             */
            delete(seriesMaster, originator, message.getTargetUser());
        } else {
            /*
             * Delete transmitted occurrences one by one
             */
            for (Event deletee : message.getResource().getEvents()) {
                try {
                    delete(deletee, originator, message.getTargetUser());
                } catch (OXException e) {
                    session.addWarning(e);
                }
            }
        }
        return resultTracker.getResult();
    }

    /**
     * Deletes the given event
     *
     * @param deletee The event to delete
     * @param originator The originator of the message
     * @param calendarUserId The acting calendar user
     * @return A list containing either the deleted event(s) or the updated series master
     * @throws OXException In case event can't be found or preconditions aren't met
     * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.5">RFC5546 Section 3.2.5</a>
     * @see <a href="https://tools.ietf.org/html/rfc6047#section-3">RFC6047 Section 3</a>
     */
    private List<Event> delete(Event deletee, CalendarUser originator, int calendarUserId) throws OXException {
        RecurrenceId recurrenceId = deletee.getRecurrenceId();
        EventID eventID = Utils.resolveEventId(session, storage, deletee.getUid(), recurrenceId, calendarUserId);
        Event originalEvent = loadEventData(eventID.getObjectID());
        /*
         * Check if CANCEL is relevant for current user
         */
        if (null == CalendarUtils.find(deletee.getAttendees(), calendarUser)) {
            /*
             * Neither for all attendees nor for the target user, skip as recommended by the RFC
             */
            throw CalendarExceptionCodes.WRONG_CANCELLATION.create(eventID.getObjectID(), I(calendarUserId));
        }
        /*
         * Check if originator is allowed to cancel, either by perfect match comparing to the organizer or by comparing to the sent-by field of the organizer
         */
        if (false == SchedulingUtils.originatorMatches(originalEvent, originator)) {
            throw CalendarExceptionCodes.NOT_ORGANIZER.create(folder.getId(), originalEvent.getId(), originator.getUri(), originator.getCn());
        }
        Check.organizerMatches(originalEvent, Collections.singletonList(deletee));

        /*
         * Check internal constrains
         */
        Check.requireInSequence(originalEvent, deletee);
        Check.eventIsInFolder(originalEvent, folder);
        requireDeletePermissions(originalEvent);

        if (CalendarUtils.isSeriesEvent(originalEvent)) {
            if (CalendarUtils.isSeriesException(originalEvent)) {
                /*
                 * Delete single existing change exception
                 */
                Event originalSeriesMaster = optEventData(originalEvent.getSeriesId());
                return deleteException(originalSeriesMaster, originalEvent);
            }
            if (null == recurrenceId) {
                /*
                 * Delete series
                 */
                return delete(originalEvent);
            }
            Event originalSeriesMaster = loadEventData(originalEvent.getSeriesId());
            RecurrenceId passedRecurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalSeriesMaster, recurrenceId);
            if (null != recurrenceId.getRange()) {
                /*
                 * Delete "this and future" recurrences
                 */
                return singletonList(deleteFutureRecurrences(originalSeriesMaster, passedRecurrenceId, false));
            }
            /*
             * Delete specific occurrence, create a new delete exception
             */
            return singletonList(addDeleteExceptionDate(originalSeriesMaster, passedRecurrenceId));
        }
        /*
         * Delete event
         */
        return delete(originalEvent);
    }
}
