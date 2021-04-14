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

package com.openexchange.chronos.impl.scheduling;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.performer.AbstractUpdatePerformer;
import com.openexchange.chronos.impl.performer.ResultTracker;
import com.openexchange.chronos.impl.performer.UpdateAttendeePerformer;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link AttendeeUpdateProcessor} - Updates the status for an participant after incoming changes
 * to an calendar object resource were processed and applied in the calendar.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public class AttendeeUpdateProcessor extends AbstractUpdatePerformer {

    protected final SchedulingMethod method;

    /**
     * Initializes a new {@link AttendeeUpdateProcessor}.
     * 
     * @param storage The calendar storage
     * @param session The calendar session
     * @param folder The calendar folder to operate on
     * @param method The method being processed
     * @throws OXException In case of error
     */
    public AttendeeUpdateProcessor(CalendarStorage storage, CalendarSession session, CalendarFolder folder, SchedulingMethod method) throws OXException {
        super(storage, session, folder);
        this.method = method;
    }

    /**
     * Performs post processing of the scheduling action, i.e. update attendee participant status
     * as needed and trigger sub-sequence scheduling.
     * <p>
     * If processing fails, e.g. because of concurrent modifications, original result will be returned.
     *
     * @param result The result to adjust
     * @param action The iTIP action that is being performed and processed
     * @param comment optional comment for the attendee processing
     * @return The calendar result after processing
     */
    public InternalCalendarResult process(InternalCalendarResult result, String action, String comment) {
        /*
         * Check if applicable
         */
        if (false == SchedulingMethod.REQUEST.equals(method) && false == SchedulingMethod.ADD.equals(method)) {
            return result;
        }
        CalendarResult userizedResult = result.getUserizedResult();
        if (isNullOrEmpty(userizedResult.getCreations()) && isNullOrEmpty(userizedResult.getUpdates())) {
            return result;
        }
        /*
         * Get user transmitted participant status, if any
         */
        final ParticipationStatus partStat;
        switch (action.toLowerCase()) {
            case "accept_and_ignore_conflicts":
                session.set(CalendarParameters.PARAMETER_CHECK_CONFLICTS, Boolean.FALSE);
                //$FALL-THROUGH$
            case "accept":
                partStat = ParticipationStatus.ACCEPTED;
                break;
            case "tentative":
                partStat = ParticipationStatus.TENTATIVE;
                break;
            case "decline":
                partStat = ParticipationStatus.DECLINED;
                break;
            case "update":
                // Restore participant status after update
                partStat = null;
                break;
            default:
                return result;
        }
        try {
            return update(result, comment, userizedResult, partStat);
        } catch (OXException e) {
            LOG.debug("Unable to update attendee status", e);
            session.addWarning(e);
        }
        return result;
    }

    InternalCalendarResult update(InternalCalendarResult result, String comment, CalendarResult userizedResult, ParticipationStatus partStat) throws OXException {
        /*
         * Adjust all updated and rescheduled events, keep part-stat of non-rescheduled events as user doesn't intend to change status in those
         */
        List<UpdateResult> updates = userizedResult.getUpdates();
        if (false == Collections.isNullOrEmpty(updates)) {
            Attendee update = prepareAttendeeUpdate(userizedResult.getUpdates(), partStat, comment);
            if (null != update) {
                List<EventID> eventIds = updates.stream() //@formatter:off
                    .filter(u -> Utils.isReschedule(u))
                    .map(u -> u.getUpdate())
                    .map(u -> new EventID(u.getFolderId(), u.getId(), u.getRecurrenceId()))
                    .collect(Collectors.toList()); //@formatter:on
                if (false == Collections.isNullOrEmpty(eventIds)) {
                    new UpdateAttendeePerformer(this).perform(eventIds, update, L(timestamp.getTime()));
                }
            }
        }

        /*
         * Adjust all created events
         */
        List<CreateResult> creations = userizedResult.getCreations();
        if (null != partStat && false == Collections.isNullOrEmpty(creations)) {
            List<Event> createdEvents = creations.stream().map(c -> c.getCreatedEvent()).collect(Collectors.toList());
            Optional<Event> representativeEvent = createdEvents.stream().filter(e -> null != CalendarUtils.find(e.getAttendees(), calendarUserId)).findAny();
            if (representativeEvent.isPresent()) {
                /*
                 * Perform attendee status update
                 */
                Attendee attendee = CalendarUtils.find(representativeEvent.get().getAttendees(), calendarUserId);
                Attendee update = AttendeeMapper.getInstance().copy(attendee, null, (AttendeeField[]) null);
                update.setPartStat(partStat);
                update.setComment(comment);
                update.setTimestamp(timestamp.getTime());
                List<EventID> eventIds = createdEvents.stream().map(c -> new EventID(c.getFolderId(), c.getId(), c.getRecurrenceId())).collect(Collectors.toList());
                new UpdateAttendeePerformer(this).perform(eventIds, update, L(timestamp.getTime()));

            }
        } // Else; "Update" scenario, let user decide for each new event as we have no data to restore from

        /*
         * Merge and return result
         */
        return merge(result, resultTracker.getResult());
    }

    /**
     * Prepares an attendee for update and sets the desired participant status
     *
     * @param updateResult The update result of the event the attendee shall be updated on again
     * @param session The calendar session
     * @param partStat The participant status to set or <code>null</code> to restore from original event
     * @param comment The optional comment to set
     * @return The attendee for the update or <code>null</code> to indocate that the update can be skipped
     * @throws OXException
     */
    private Attendee prepareAttendeeUpdate(List<UpdateResult> updateResult, ParticipationStatus partStat, String comment) throws OXException {
        for (UpdateResult result : updateResult) {
            Attendee prepared = prepareAttendeeUpdate(result, partStat, comment);
            if (null != prepared) {
                return prepared;
            }
        }
        return null;
    }

    /**
     * Prepares an attendee for update and sets the desired participant status
     *
     * @param updateResult The update result of the event the attendee shall be updated on again
     * @param session The calendar session
     * @param partStat The participant status to set or <code>null</code> to restore from original event
     * @param comment The optional comment to set
     * @return The attendee for the update or <code>null</code> to indicate that the update can be skipped
     * @throws OXException In case copy of the attendee fails
     */
    private Attendee prepareAttendeeUpdate(UpdateResult updateResult, ParticipationStatus partStat, String comment) throws OXException {
        Attendee attendee = CalendarUtils.find(updateResult.getUpdate().getAttendees(), calendarUserId);
        if (null == attendee) {
            LOG.debug("Unable to find attendee with ID {} in updated event {} after update for method {} was executed", I(calendarUserId), updateResult.getUpdate(), method);
            return null;
        }
        Attendee update = AttendeeMapper.getInstance().copy(attendee, null, (AttendeeField[]) null);
        if (null == partStat) {
            /*
             * "Update" scenario, user wants to keep status of the original event
             */
            Attendee originalAttendee = CalendarUtils.find(updateResult.getOriginal().getAttendees(), calendarUserId);
            if (null == originalAttendee || null == originalAttendee.getPartStat() || originalAttendee.getPartStat().matches(attendee.getPartStat())) {
                // Nothing to update
                return null;
            }
            update.setPartStat(originalAttendee.getPartStat());
            update.setComment(Strings.isNotEmpty(comment) ? comment : originalAttendee.getComment());
        } else {
            update.setPartStat(partStat);
            update.setComment(comment);
        }
        update.setTimestamp(timestamp.getTime());
        return update;
    }

    /**
     * Merges the original calendar result with the new calendar result
     *
     * @param originalResult The original calendar result from processing performed beforehand
     * @param postResult The result containing the updates of the post processing
     * @return The merged result
     */
    private InternalCalendarResult merge(InternalCalendarResult originalResult, InternalCalendarResult postResult) throws OXException {
        /*
         * Build a new result based on all touched events
         */
        ResultTracker tracker = new ResultTracker(storage, session, folder, calendarUserId, getSelfProtection());
        CalendarResult plainResult = originalResult.getPlainResult();
        List<UpdateResult> updates = postResult.getPlainResult().getUpdates();
        /*
         * Use the latest version of the created event for the client
         */
        for (CreateResult createResult : plainResult.getCreations()) {
            Event createdEvent = createResult.getCreatedEvent();
            Optional<Event> updatedEvent = findUpdated(updates, createdEvent);
            tracker.trackCreation(updatedEvent.orElse(createdEvent));
        }
        /*
         * Use event before the first update as original, use latest event version as updated
         */
        for (UpdateResult updateResult : plainResult.getUpdates()) {
            Event originalEvent = updateResult.getOriginal();
            Optional<Event> updatedEvent = findUpdated(updates, originalEvent);
            tracker.trackUpdate(originalEvent, updatedEvent.orElse(updateResult.getUpdate()));
        }
        /*
         * Gather all deletions
         */
        for (DeleteResult deleteResult : plainResult.getDeletions()) {
            tracker.trackDeletion(deleteResult.getOriginal());
        }
        for (DeleteResult deleteResult : postResult.getPlainResult().getDeletions()) {
            Optional<DeleteResult> existing = findDeletion(plainResult.getDeletions(), deleteResult);
            if (false == existing.isPresent()) {
                tracker.trackDeletion(deleteResult.getOriginal());
            }
        }
        /*
         * Add messages
         */
        if (null != postResult.getChangeNotifications()) {
            postResult.getChangeNotifications().forEach(c -> tracker.trackChangeNotification(c));
        }
        if (null != postResult.getSchedulingMessages()) {
            postResult.getSchedulingMessages().forEach(m -> tracker.trackSchedulingMessage(m));
        }
        return tracker.getResult();
    }

    private Optional<Event> findUpdated(List<UpdateResult> updates, Event originalEvent) {
        if (isNullOrEmpty(updates)) {
            return Optional.empty();
        }
        String eventId = originalEvent.getId();
        return updates.stream() // Search for current event, ignore touched master event
            .filter(u -> eventId.equals(u.getOriginal().getId()) && !SchedulingUtils.isTouchedEvent(u))//
            .map(u -> u.getUpdate()).findAny();
    }

    private Optional<DeleteResult> findDeletion(List<DeleteResult> results, DeleteResult deleteResult) {
        if (isNullOrEmpty(results)) {
            return Optional.empty();
        }
        return results.stream().filter(d -> d.getEventID().equals(deleteResult.getEventID())).findAny();
    }

}
