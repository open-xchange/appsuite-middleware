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
 *    trademarks of the OX Software GmbH. group of companies.
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

import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
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
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link CancelPerformer} - Handles incoming <code>CANCEL</code> message by external organizer and tries to apply
 * the delete the events transmitted from the message.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 * @see <a href="https://tools.ietf.org/html/rfc5546#section-3.2.5">RFC5546 Section 3.2.5</a>
 */
public class CancelPerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link CancelPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param source The source from which the scheduling has been triggered
     * @throws OXException
     */
    public CancelPerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
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
    public InternalCalendarResult perform(IncomingSchedulingMessage message) throws OXException {
        CalendarUser originator = message.getSchedulingObject().getOriginator();
        /*
         * Delete transmitted occurrences one by one
         */
        for (Event deletee : message.getResource().getEvents()) {
            delete(deletee, originator, message.getTargetUser());
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
        if (originalEvent.getAttendees().size() != deletee.getAttendees().size() && null == CalendarUtils.find(deletee.getAttendees(), calendarUserId)) {
            /*
             * Neither for all attendees nor for the target user, skip as recommended by the RFC
             */
            return Collections.emptyList();
        }
        /*
         * Check if originator is allowed to cancel, either by perfect match comparing to the organizer or by comparing to the sent-by field of the organizer
         */
        if (false == CalendarUtils.matches(originator, deletee.getOrganizer()) && (null == deletee.getOrganizer().getSentBy() || false == CalendarUtils.matches(originator, deletee.getOrganizer().getSentBy()))) {
            throw CalendarExceptionCodes.NOT_ORGANIZER.create(folder.getId(), originalEvent.getId(), originator.getUri(), originator.getCn());
        }
        Check.organizerMatches(originalEvent, deletee);

        /*
         * Check internal constrains
         */
        Check.requireInSequence(originalEvent, deletee);
        Check.eventIsInFolder(originalEvent, folder);
        requireUpToDateTimestamp(originalEvent, timestamp.getTime());
        requireDeletePermissions(originalEvent);

        if (CalendarUtils.isSeriesEvent(originalEvent)) {
            if (CalendarUtils.isSeriesException(originalEvent)) {
                /*
                 * Delete single existing change exception
                 */
                Event originalSeriesMaster = loadEventData(originalEvent.getSeriesId());
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
