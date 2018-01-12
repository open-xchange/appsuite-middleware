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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getExceptionDateUpdates;
import static com.openexchange.chronos.common.CalendarUtils.getUserIDs;
import static com.openexchange.chronos.common.CalendarUtils.hasExternalOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isAllDay;
import static com.openexchange.chronos.common.CalendarUtils.isOpaqueTransparency;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RecurrenceRange;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link UpdatePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdatePerformer extends AbstractUpdatePerformer {

    /** <i>Meta</i>-fields of events that are always skipped when applying updated event data */
    private static final EventField[] SKIPPED_FIELDS = {
        EventField.CREATED, EventField.CREATED_BY, EventField.LAST_MODIFIED, EventField.TIMESTAMP, EventField.MODIFIED_BY, EventField.SEQUENCE
    };

    /**
     * Initializes a new {@link UpdatePerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public UpdatePerformer(CalendarStorage storage, CalendarSession session, UserizedFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the update operation.
     *
     * @param objectId The identifier of the event to update
     * @param recurrenceId The optional id of the recurrence.
     * @param updatedEventData The updated event data
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The update result
     */
    public InternalCalendarResult perform(String objectId, RecurrenceId recurrenceId, Event updatedEventData, long clientTimestamp) throws OXException {
        getSelfProtection().checkEvent(updatedEventData);
        /*
         * load original event data
         */
        Event originalEvent = requireUpToDateTimestamp(loadEventData(objectId), clientTimestamp);
        /*
         * update event or event occurrence
         */
        if (null == recurrenceId && updatedEventData.containsRecurrenceId()) {
            recurrenceId = updatedEventData.getRecurrenceId();
        }
        if (isSeriesMaster(originalEvent) && null != recurrenceId) {
            updateRecurrence(originalEvent, recurrenceId, updatedEventData);
        } else {
            updateEvent(originalEvent, updatedEventData);
        }
        return resultTracker.getResult();
    }

    /**
     * Updates data of an existing event recurrence and tracks the update in the underlying calendar result.
     *
     * @param originalSeriesMaster The original series master event
     * @param The recurrence identifier targeting the event occurrence to update
     * @param eventData The updated event data
     * @param ignoredFields Additional fields to ignore during the update; {@link #SKIPPED_FIELDS} are always skipped
     */
    private void updateRecurrence(Event originalSeriesMaster, RecurrenceId recurrenceId, Event updatedEventData, EventField... ignoredFields) throws OXException {
        recurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalSeriesMaster, recurrenceId);
        if (contains(originalSeriesMaster.getDeleteExceptionDates(), recurrenceId)) {
            /*
             * cannot update a delete exception
             */
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalSeriesMaster.getSeriesId(), recurrenceId);
        }
        if (contains(originalSeriesMaster.getChangeExceptionDates(), recurrenceId)) {
            /*
             * update for existing change exception, perform update, touch master & track results
             */
            Check.recurrenceRangeMatches(recurrenceId, null);
            Event originalExceptionEvent = loadExceptionData(originalSeriesMaster.getSeriesId(), recurrenceId);
            updateEvent(originalExceptionEvent, updatedEventData, ignoredFields);
            touch(originalSeriesMaster.getSeriesId());
            resultTracker.trackUpdate(originalSeriesMaster, loadEventData(originalSeriesMaster.getId()));
            return;
        } else if (null != recurrenceId.getRange()) {
            /*
             * update "this and future" recurrences; first split the series at this recurrence
             */
            Check.recurrenceRangeMatches(recurrenceId, RecurrenceRange.THISANDFUTURE);
            new SplitPerformer(this).perform(originalSeriesMaster.getSeriesId(), recurrenceId.getValue(), null, originalSeriesMaster.getTimestamp());
            /*
             * reload the (now splitted) series event & apply the update
             */
            Event updatedMasterEvent = loadEventData(originalSeriesMaster.getId());
            updateEvent(updatedMasterEvent, updatedEventData, ignoredFields);
        } else {
            /*
             * update for new change exception; prepare & insert a plain exception first, based on the original data from the master
             */
            Event newExceptionEvent = prepareException(originalSeriesMaster, recurrenceId);
            Check.quotaNotExceeded(storage, session);
            storage.getEventStorage().insertEvent(newExceptionEvent);
            storage.getAttendeeStorage().insertAttendees(newExceptionEvent.getId(), originalSeriesMaster.getAttendees());
            storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getID(), newExceptionEvent.getId(), originalSeriesMaster.getAttachments());
            for (Entry<Integer, List<Alarm>> entry : storage.getAlarmStorage().loadAlarms(originalSeriesMaster).entrySet()) {
                insertAlarms(newExceptionEvent, entry.getKey().intValue(), entry.getValue(), true);
            }
            newExceptionEvent = loadEventData(newExceptionEvent.getId());
            resultTracker.trackCreation(newExceptionEvent, originalSeriesMaster);
            /*
             * perform the update on the newly created change exception
             * - recurrence rule is forcibly ignored during update to satisfy UsmFailureDuringRecurrenceTest.testShouldFailWhenTryingToMakeAChangeExceptionASeriesButDoesNot()
             * - sequence number is also ignored (since possibly incremented implicitly before)
             */
            updateEvent(newExceptionEvent, updatedEventData, EventField.ID, EventField.RECURRENCE_RULE, EventField.SEQUENCE);
            /*
             * add change exception date to series master & track results
             */
            addChangeExceptionDate(originalSeriesMaster, recurrenceId);
            Event updatedMasterEvent = loadEventData(originalSeriesMaster.getId());
            resultTracker.trackUpdate(originalSeriesMaster, updatedMasterEvent);

            storage.getAlarmTriggerStorage().deleteTriggers(originalSeriesMaster.getId());
            storage.getAlarmTriggerStorage().insertTriggers(updatedMasterEvent);
        }
    }

    /**
     * Updates data of an existing event.
     *
     * @param originalEvent The original, plain event data
     * @param eventData The updated event data
     * @param ignoredFields Additional fields to ignore during the update; {@link #SKIPPED_FIELDS} are always skipped
     */
    private void updateEvent(Event originalEvent, Event eventData, EventField... ignoredFields) throws OXException {
        /*
         * check if folder view on event is allowed as needed
         */
        if (needsExistenceCheckInTargetFolder(originalEvent, eventData)) {
            Check.eventIsInFolder(originalEvent, folder);
        }
        /*
         * handle new delete exceptions from the calendar user's point of view beforehand
         */
        if (isSeriesMaster(originalEvent) && eventData.containsDeleteExceptionDates() && updateDeleteExceptions(originalEvent, eventData)) {
            originalEvent = loadEventData(originalEvent.getId());
        }
        /*
         * prepare event update & check conflicts as needed
         */
        List<Event> originalChangeExceptions = isSeriesMaster(originalEvent) ? loadExceptionData(originalEvent.getId()) : null;
        EventUpdateProcessor eventUpdate = new EventUpdateProcessor(
            session, folder, originalEvent, originalChangeExceptions, eventData, timestamp, Arrays.add(SKIPPED_FIELDS, ignoredFields));
        if (needsConflictCheck(eventUpdate)) {
            Check.noConflicts(storage, session, eventUpdate.getUpdate(), eventUpdate.getAttendeeUpdates().previewChanges());
        }
        /*
         * check permissions & update event data in storage, checking permissions as required
         */
        storeEventUpdate(originalEvent, eventUpdate.getDelta(), eventUpdate.getUpdatedFields());
        storeAttendeeUpdates(originalEvent, eventUpdate.getAttendeeUpdates());
        storeAttachmentUpdates(originalEvent, eventUpdate.getAttachmentUpdates());
        /*
         * update passed alarms for calendar user, apply default alarms for newly added internal user attendees
         */
        if (eventData.containsAlarms()) {
            updateAlarms(eventUpdate.getUpdate(), calendarUserId, storage.getAlarmStorage().loadAlarms(originalEvent, calendarUserId), eventData.getAlarms());
        }
        for (int userId : getUserIDs(eventUpdate.getAttendeeUpdates().getAddedItems())) {
            List<Alarm> defaultAlarm = isAllDay(eventUpdate.getUpdate()) ? session.getConfig().getDefaultAlarmDate(userId) : session.getConfig().getDefaultAlarmDateTime(userId);
            if (null != defaultAlarm) {
                insertAlarms(eventUpdate.getUpdate(), userId, defaultAlarm, true);
            }
        }
        /*
         * track update result & update any stored alarm triggers of all users if required
         */
        Event updatedEvent = loadEventData(originalEvent.getId());
        resultTracker.trackUpdate(originalEvent, updatedEvent);
        storage.getAlarmTriggerStorage().deleteTriggers(originalEvent.getId());
        storage.getAlarmTriggerStorage().insertTriggers(updatedEvent);
        /*
         * recursively perform pending updates of change exceptions, too
         */
        if (false == eventUpdate.getExceptionUpdates().isEmpty()) {
            for (Event removedException : eventUpdate.getExceptionUpdates().getRemovedItems()) {
                delete(removedException);
            }
            for (ItemUpdate<Event, EventField> updatedException : eventUpdate.getExceptionUpdates().getUpdatedItems()) {
                updateEvent(updatedException.getOriginal(), updatedException.getUpdate());
            }
        }
    }

    /**
     * Determines if it's allowed to skip the check if the updated event already exists in the targeted folder or not.
     * <p/>
     * The skip may be checked under certain circumstances, particularly:
     * <ul>
     * <li>the event has an <i>external</i> organizer</li>
     * <li>the organizer matches in the original and in the updated event</li>
     * <li>the unique identifier matches in the original and in the updated event</li>
     * <li>the updated event's sequence number is not smaller than the sequence number of the original event</li>
     * </ul>
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @return <code>true</code> if the check should be performed, <code>false</code>, otherwise
     * @see <a href="https://bugs.open-xchange.com/show_bug.cgi?id=29566#c12">Bug 29566</a>, <a href="https://bugs.open-xchange.com/show_bug.cgi?id=23181"/>Bug 23181</a>
     */
    public boolean needsExistenceCheckInTargetFolder(Event originalEvent, Event updatedEvent) {
        if (hasExternalOrganizer(originalEvent) && matches(originalEvent.getOrganizer(), updatedEvent.getOrganizer()) &&
            originalEvent.getUid().equals(updatedEvent.getUid()) && updatedEvent.getSequence() >= originalEvent.getSequence()) {
            return false;
        }
        return true;
    }

    /**
     * Gets a value indicating whether conflict checks should take place along with the update or not.
     *
     * @param eventUpdate The event update to evaluate
     * @param attendeeHelper The attendee helper for the update
     * @return <code>true</code> if conflict checks should take place, <code>false</code>, otherwise
     */
    private boolean needsConflictCheck(EventUpdate eventUpdate) throws OXException {
        if (eventUpdate.getUpdatedFields().contains(EventField.START_DATE) &&
            eventUpdate.getUpdate().getStartDate().before(eventUpdate.getOriginal().getStartDate())) {
            /*
             * (re-)check conflicts if updated start is before the original start
             */
            return true;
        }
        if (eventUpdate.getUpdatedFields().contains(EventField.END_DATE) &&
            eventUpdate.getUpdate().getEndDate().after(eventUpdate.getOriginal().getEndDate())) {
            /*
             * (re-)check conflicts if updated end is after the original end
             */
            return true;
        }
        if (eventUpdate.getUpdatedFields().contains(EventField.TRANSP) && false == isOpaqueTransparency(eventUpdate.getOriginal())) {
            /*
             * (re-)check conflicts if transparency is now opaque
             */
            return true;
        }
        if (0 < eventUpdate.getAttendeeUpdates().getAddedItems().size()) {
            /*
             * (re-)check conflicts if there are new attendees
             */
            return true;
        }
        if (eventUpdate.getUpdatedFields().contains(EventField.RECURRENCE_RULE)) {
            /*
             * (re-)check conflicts if recurrence rule changes
             */
            return true;
        }
        return false;
    }

    private boolean updateDeleteExceptions(Event originalEvent, Event updatedEvent) throws OXException {
        if (isSeriesMaster(originalEvent) && false == isNullOrEmpty(updatedEvent.getDeleteExceptionDates())) {
            if (deleteRemovesEvent(originalEvent)) {
                /*
                 * "real" delete exceptions for all attendees, take over as-is during normal update routine
                 */
                return false;
            }
            /*
             * check for newly indicated delete exceptions, from the calendar user's point of view
             */
            Attendee userAttendee = find(originalEvent.getAttendees(), calendarUserId);
            SortedSet<RecurrenceId> originalDeleteExceptionDates = Utils.applyExceptionDates(storage, originalEvent, calendarUserId).getDeleteExceptionDates();
            SimpleCollectionUpdate<RecurrenceId> exceptionDateUpdates = getExceptionDateUpdates(originalDeleteExceptionDates, updatedEvent.getDeleteExceptionDates());
            if (0 < exceptionDateUpdates.getRemovedItems().size() || null == userAttendee) {
                throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), EventField.DELETE_EXCEPTION_DATES);
            }
            if (0 < exceptionDateUpdates.getAddedItems().size()) {
                for (RecurrenceId newDeleteException : exceptionDateUpdates.getAddedItems()) {
                    RecurrenceId recurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalEvent, newDeleteException);
                    if (contains(originalEvent.getChangeExceptionDates(), recurrenceId)) {
                        /*
                         * remove attendee from existing change exception
                         */
                        Event originalExceptionEvent = loadExceptionData(originalEvent.getSeriesId(), recurrenceId);
                        delete(originalExceptionEvent, userAttendee);
                    } else {
                        /*
                         * creation of new delete exception for this attendee
                         */
                        deleteFromRecurrence(originalEvent, recurrenceId, userAttendee);
                    }
                }
                updatedEvent.removeDeleteExceptionDates();
                return true;
            }
        }
        return false;
    }

    /**
     * Persists event data updates in the underlying calendar storage, verifying that the current user has appropriate write permissions
     * in order to do so.
     *
     * @param originalEvent The event being updated
     * @param deltaEvent The delta event providing the updated event data
     * @param updatedFields The actually updated fields
     * @return <code>true</code> if there were changes, <code>false</code>, otherwise
     */
    private boolean storeEventUpdate(Event originalEvent, Event deltaEvent, Set<EventField> updatedFields) throws OXException {
        boolean realChange = false;
        for (EventField updatedField : updatedFields) {
            if (Arrays.contains(SKIPPED_FIELDS, updatedField)) {
                continue;
            }
            realChange = true;
            break;
        }
        if (realChange) {
            requireWritePermissions(originalEvent);
        } else {
            requireWritePermissions(originalEvent, Collections.singletonList(session.getEntityResolver().prepareUserAttendee(session.getUserId())));
        }
        storage.getEventStorage().updateEvent(deltaEvent);
        return true;
    }

    /**
     * Persists attendee updates in the underlying calendar storage, verifying that the current user has appropriate write permissions
     * in order to do so.
     *
     * @param originalEvent The event the attendees are updated for
     * @param attachmentUpdates The attendee updates to persist
     * @return <code>true</code> if there were changes, <code>false</code>, otherwise
     */
    private boolean storeAttendeeUpdates(Event originalEvent, CollectionUpdate<Attendee, AttendeeField> attendeeUpdates) throws OXException {
        if (attendeeUpdates.isEmpty()) {
            return false;
        }
        /*
         * perform attendee deletions
         */
        List<Attendee> removedItems = attendeeUpdates.getRemovedItems();
        if (0 < removedItems.size()) {
            requireWritePermissions(originalEvent, removedItems);
            storage.getEventStorage().insertEventTombstone(storage.getUtilities().getTombstone(originalEvent, timestamp, calendarUser));
            storage.getAttendeeStorage().insertAttendeeTombstones(originalEvent.getId(), storage.getUtilities().getTombstones(removedItems));
            storage.getAttendeeStorage().deleteAttendees(originalEvent.getId(), removedItems);
            storage.getAlarmStorage().deleteAlarms(originalEvent.getId(), getUserIDs(removedItems));
        }
        /*
         * perform attendee updates
         */
        List<? extends ItemUpdate<Attendee, AttendeeField>> updatedItems = attendeeUpdates.getUpdatedItems();
        if (0 < updatedItems.size()) {
            List<Attendee> attendeesToUpdate = new ArrayList<Attendee>(updatedItems.size());
            for (ItemUpdate<Attendee, AttendeeField> attendeeToUpdate : updatedItems) {
                Attendee originalAttendee = attendeeToUpdate.getOriginal();
                Attendee newAttendee = AttendeeMapper.getInstance().copy(originalAttendee, null, (AttendeeField[]) null);
                AttendeeMapper.getInstance().copy(attendeeToUpdate.getUpdate(), newAttendee, AttendeeField.RSVP, AttendeeField.COMMENT, AttendeeField.PARTSTAT, AttendeeField.ROLE);
                attendeesToUpdate.add(newAttendee);
            }
            requireWritePermissions(originalEvent, attendeesToUpdate);
            storage.getAttendeeStorage().updateAttendees(originalEvent.getId(), attendeesToUpdate);
        }
        /*
         * perform attendee inserts
         */
        if (0 < attendeeUpdates.getAddedItems().size()) {
            requireWritePermissions(originalEvent);
            storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), attendeeUpdates.getAddedItems());
        }
        return true;
    }

    /**
     * Persists attachment updates in the underlying calendar storage, verifying that the current user has appropriate write permissions
     * in order to do so.
     *
     * @param originalEvent The event the attachments are updated for
     * @param attachmentUpdates The attachment updates to persist
     * @return <code>true</code> if there were changes, <code>false</code>, otherwise
     */
    private boolean storeAttachmentUpdates(Event originalEvent, SimpleCollectionUpdate<Attachment> attachmentUpdates) throws OXException {
        if (attachmentUpdates.isEmpty()) {
            return false;
        }
        requireWritePermissions(originalEvent);
        if (0 < attachmentUpdates.getRemovedItems().size()) {
            storage.getAttachmentStorage().deleteAttachments(session.getSession(), folder.getID(), originalEvent.getId(), attachmentUpdates.getRemovedItems());
        }
        if (0 < attachmentUpdates.getAddedItems().size()) {
            storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getID(), originalEvent.getId(), attachmentUpdates.getAddedItems());
        }
        return true;
    }

}
