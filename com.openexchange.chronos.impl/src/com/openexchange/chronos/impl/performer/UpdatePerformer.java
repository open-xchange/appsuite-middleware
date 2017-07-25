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
import static com.openexchange.chronos.common.CalendarUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.findAttachment;
import static com.openexchange.chronos.common.CalendarUtils.getAlarmIDs;
import static com.openexchange.chronos.common.CalendarUtils.getAlarmUpdates;
import static com.openexchange.chronos.common.CalendarUtils.getExceptionDateUpdates;
import static com.openexchange.chronos.common.CalendarUtils.getUserIDs;
import static com.openexchange.chronos.common.CalendarUtils.hasExternalOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isLastUserAttendee;
import static com.openexchange.chronos.common.CalendarUtils.isOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isPublicClassification;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Check.requireUpToDateTimestamp;
import static com.openexchange.chronos.impl.Utils.asList;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.READ_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.READ_FOLDER;
import static com.openexchange.folderstorage.Permission.READ_OWN_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_ALL_OBJECTS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.AttendeeField;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Transp;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.AlarmMapper;
import com.openexchange.chronos.common.mapping.AttendeeMapper;
import com.openexchange.chronos.common.mapping.DefaultItemUpdate;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.AttendeeHelper;
import com.openexchange.chronos.impl.CalendarResultImpl;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.CreateResultImpl;
import com.openexchange.chronos.impl.UpdateResultImpl;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.SimpleCollectionUpdate;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.EventStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.java.util.TimeZones;
import com.openexchange.tools.arrays.Arrays;

/**
 * {@link UpdatePerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class UpdatePerformer extends AbstractUpdatePerformer {

    /** Event fields that are always skipped when applying updated event data */
    private static final EventField[] SKIPPED_FIELDS = {
        EventField.CREATED, EventField.CREATED_BY, EventField.LAST_MODIFIED, EventField.MODIFIED_BY, EventField.SEQUENCE,
        EventField.ALARMS, EventField.ATTENDEES, EventField.ATTACHMENTS
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
     * @param objectID The identifier of the event to update
     * @param recurrenceId The optional id of the recurrence.
     * @param updatedEvent The updated event data
     * @param clientTimestamp The client timestamp to catch concurrent modifications
     * @return The update result
     */
    public CalendarResultImpl perform(String objectID, RecurrenceId recurrenceId, Event updatedEvent, long clientTimestamp) throws OXException {
        /*
         * load original event data
         */
        Event originalEvent = requireUpToDateTimestamp(loadEventData(objectID, false), clientTimestamp);
        /*
         * update event or event occurrence
         */
        if (null == recurrenceId && updatedEvent.containsRecurrenceId()) {
            recurrenceId = updatedEvent.getRecurrenceId();
        }
        if (isSeriesMaster(originalEvent) && null != recurrenceId) {
            updateEvent(originalEvent, updatedEvent, recurrenceId);
        } else {
            if (updateEvent(originalEvent, updatedEvent)) {
                result.addUpdate(new UpdateResultImpl(originalEvent, loadEventData(originalEvent.getId())));
            }
        }
        return result;
    }

    private void updateEvent(Event originalEvent, Event updatedEvent, RecurrenceId recurrenceID) throws OXException {
        if (isSeriesMaster(originalEvent)) {
            if (contains(originalEvent.getDeleteExceptionDates(), recurrenceID)) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalEvent.getSeriesId(), recurrenceID);
            }
            if (contains(originalEvent.getChangeExceptionDates(), recurrenceID)) {
                /*
                 * update for existing change exception
                 */
                Event originalExceptionEvent = loadExceptionData(originalEvent.getId(), recurrenceID);
                if (updateEvent(originalExceptionEvent, updatedEvent)) {
                    result.addUpdate(new UpdateResultImpl(originalEvent, loadEventData(originalEvent.getId())));
                    touch(originalEvent.getSeriesId());
                }
            } else {
                /*
                 * update for new change exception, prepare & insert a plain exception first
                 */
                Event newExceptionEvent = prepareException(originalEvent, Check.recurrenceIdExists(session.getRecurrenceService(), originalEvent, recurrenceID));
                storage.getEventStorage().insertEvent(newExceptionEvent);
                /*
                 * take over all original attendees, attachments & alarms
                 */
                storage.getAttendeeStorage().insertAttendees(newExceptionEvent.getId(), originalEvent.getAttendees());
                storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getID(), newExceptionEvent.getId(), originalEvent.getAttachments());
                for (Entry<Integer, List<Alarm>> entry : storage.getAlarmStorage().loadAlarms(originalEvent).entrySet()) {
                    insertAlarms(newExceptionEvent, entry.getKey().intValue(), entry.getValue(), true);
                }
                /*
                 * reload the newly created exception as 'original' & perform the update
                 * - recurrence rule is forcibly ignored during update to satisfy UsmFailureDuringRecurrenceTest.testShouldFailWhenTryingToMakeAChangeExceptionASeriesButDoesNot()
                 * - sequence number is also ignored (since possibly incremented implicitly before)
                 * - attachments are copied over from the master to detect possible differences correctly
                 */
                newExceptionEvent = loadEventData(newExceptionEvent.getId());
                newExceptionEvent.setAttachments(originalEvent.getAttachments());
                updateEvent(newExceptionEvent, updatedEvent, EventField.RECURRENCE_RULE, EventField.SEQUENCE);
                addChangeExceptionDate(originalEvent, recurrenceID);
                result.addCreation(new CreateResultImpl(loadEventData(newExceptionEvent.getId())));
            }
        } else if (isSeriesException(originalEvent)) {
            /*
             * update for existing change exception
             */
            if (updateEvent(originalEvent, updatedEvent)) {
                result.addUpdate(new UpdateResultImpl(originalEvent, loadEventData(originalEvent.getId())));
                touch(originalEvent.getSeriesId());
            }
        } else {
            /*
             * unsupported, otherwise
             */
            throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalEvent.getId(), String.valueOf(recurrenceID));
        }
    }

    /**
     * Updates an existing event.
     *
     * @param originalEvent The original event data
     * @param updatedEvent The updated event data
     * @param ignoredFields Additional fields to ignore during the update; {@link UpdatePerformer#SKIPPED_FIELDS} are always skipped
     * @return <code>true</code> if there were changes, <code>false</code>, otherwise
     */
    private boolean updateEvent(Event originalEvent, Event updatedEvent, EventField... ignoredFields) throws OXException {
        boolean wasUpdated = false;
        /*
         * check if folder view on event is allowed as needed
         */
        if (needsExistenceCheckInTargetFolder(originalEvent, updatedEvent)) {
            Check.eventIsInFolder(originalEvent, folder);
        }
        /*
         * handle new delete exceptions from the calendar user's point of view beforehand
         */
        if (isSeriesMaster(originalEvent) && updatedEvent.containsDeleteExceptionDates() && updateDeleteExceptions(originalEvent, updatedEvent)) {
            wasUpdated |= true;
            originalEvent = loadEventData(originalEvent.getId());
        }
        /*
         * update event data
         */
        AttendeeHelper attendeeHelper = AttendeeHelper.onUpdatedEvent(session, folder, originalEvent.getAttendees(), updatedEvent);
        List<Attendee> newAttendees = attendeeHelper.previewChanges();
        ItemUpdate<Event, EventField> eventUpdate = prepareEventUpdate(originalEvent, updatedEvent, newAttendees, ignoredFields);
        if (null != eventUpdate && 0 < eventUpdate.getUpdatedFields().size()) {
            /*
             * check permissions & conflicts
             */
            requireWritePermissions(originalEvent);
            if (needsConflictCheck(eventUpdate, attendeeHelper)) {
                Event changedEvent = EventMapper.getInstance().copy(originalEvent, new Event(), (EventField[]) null);
                changedEvent = EventMapper.getInstance().copy(eventUpdate.getUpdate(), changedEvent, (EventField[]) null);
                Check.noConflicts(storage, session, changedEvent, newAttendees);
            }
            if (needsSequenceNumberIncrement(eventUpdate, attendeeHelper)) {
                /*
                 * increment sequence number
                 */
                eventUpdate.getUpdate().setSequence(originalEvent.getSequence() + 1);
            }
            /*
             * adjust change & delete exceptions as needed
             */
            adjustExceptionsOnReschedule(eventUpdate);
            /*
             * perform update
             */
            Consistency.setModified(timestamp, eventUpdate.getUpdate(), session.getUserId());
            storage.getEventStorage().updateEvent(eventUpdate.getUpdate());
            wasUpdated |= true;
        }
        /*
         * process any attendee updates
         */
        if (updatedEvent.containsAttendees()) {
            wasUpdated |= updateAttendees(originalEvent, updatedEvent, attendeeHelper);
        } else if (null != eventUpdate && needsParticipationStatusReset(eventUpdate)) {
            wasUpdated |= resetParticipationStatus(originalEvent.getId(), originalEvent.getAttendees());
        }
        /*
         * process any attachment updates
         */
        if (updatedEvent.containsAttachments()) {
            wasUpdated |= updateAttachments(originalEvent, originalEvent.getAttachments(), updatedEvent.getAttachments());
        }
        /*
         * process any alarm updates for the calendar user
         */
        if (updatedEvent.containsAlarms()) {
            Event changedEvent = EventMapper.getInstance().copy(originalEvent, new Event(), (EventField[]) null);
            if (null != eventUpdate) {
                changedEvent = EventMapper.getInstance().copy(eventUpdate.getUpdate(), changedEvent, (EventField[]) null);
            }
            changedEvent.setFolderId(folder.getID());
            wasUpdated |= updateAlarms(changedEvent, calendarUserId, updatedEvent.getAlarms());
        }
        /*
         * update any stored alarm triggers of all users if required
         */
        if (null != eventUpdate && needsAlarmTriggerUpdate(eventUpdate)) {
            Map<Integer, List<Alarm>> alarmsByUserID = storage.getAlarmStorage().loadAlarms(originalEvent);
            if (null != alarmsByUserID && 0 < alarmsByUserID.size()) {
                Event changedEvent = EventMapper.getInstance().copy(originalEvent, new Event(), (EventField[]) null);
                changedEvent = EventMapper.getInstance().copy(eventUpdate.getUpdate(), changedEvent, (EventField[]) null);
                if (isGroupScheduled(originalEvent)) {
                    for (Attendee attendee : filter(originalEvent.getAttendees(), Boolean.TRUE, CalendarUserType.INDIVIDUAL)) {
                        List<Alarm> alarms = alarmsByUserID.get(I(attendee.getEntity()));
                        if (null != alarms && 0 < alarms.size()) {
                            changedEvent.setFolderId(AttendeeHelper.ATTENDEE_PUBLIC_FOLDER_ID == attendee.getFolderID() ? changedEvent.getFolderId() : attendee.getFolderID());
                            storage.getAlarmStorage().updateAlarms(changedEvent, attendee.getEntity(), alarms);
                        }
                    }
                } else {
                    List<Alarm> alarms = alarmsByUserID.get(I(calendarUserId));
                    if (null != alarms && 0 < alarms.size()) {
                        storage.getAlarmStorage().updateAlarms(changedEvent, calendarUserId, alarms);
                    }
                }
            }
        }
        /*
         * ensure to 'touch' original event in case not already done & track update result
         */
        if (wasUpdated) {
            if (null == eventUpdate) {
                touch(originalEvent.getId());
            }
        }
        return wasUpdated;
    }

    /**
     * Adjusts any previously existing change- and delete exceptions whenever a series event is rescheduled. In particluar:
     * <ul>
     * <li>If the series master event's period is changed, any series exceptions are removed</li>
     * <li>If an event series is turned into a single event, any series exceptions are removed</li>
     * <li>If the recurrence rule changes, any exceptions whose recurrence identifier no longer matches the recurrence are removed</li>
     * </ul>
     *
     * @param eventUpdate The event update
     * @return <code>true</code> if there were changes, <code>false</code>, otherwise
     */
    private boolean adjustExceptionsOnReschedule(ItemUpdate<Event, EventField> eventUpdate) throws OXException {
        /*
         * check if applicable for event update
         */
        Event originalEvent = eventUpdate.getOriginal();
        if (false == isSeriesMaster(originalEvent) ||
            (isNullOrEmpty(originalEvent.getChangeExceptionDates()) && isNullOrEmpty(originalEvent.getDeleteExceptionDates()))) {
            return false;
        }
        /*
         * reset all delete- and change exceptions if master period changes, or if recurrence is deleted
         */
        if (eventUpdate.containsAnyChangeOf(new EventField[] { EventField.START_DATE, EventField.END_DATE }) ||
            eventUpdate.getUpdatedFields().contains(EventField.RECURRENCE_RULE) && null == eventUpdate.getUpdate().getRecurrenceRule()) {
            eventUpdate.getUpdate().setDeleteExceptionDates(null);
            eventUpdate.getUpdate().setChangeExceptionDates(null);
            deleteExceptions(originalEvent.getSeriesId(), originalEvent.getChangeExceptionDates());
            return true;
        }
        if (eventUpdate.getUpdatedFields().contains(EventField.RECURRENCE_RULE)) {
            /*
             * recurrence rule changed, build list of possible exception dates matching the new rule
             */
            SortedSet<RecurrenceId> exceptionDates = new TreeSet<RecurrenceId>();
            if (null != originalEvent.getDeleteExceptionDates()) {
                exceptionDates.addAll(originalEvent.getDeleteExceptionDates());
            }
            if (null != originalEvent.getChangeExceptionDates()) {
                exceptionDates.addAll(originalEvent.getChangeExceptionDates());
            }
            Calendar untilCalendar = initCalendar(TimeZones.UTC, exceptionDates.last().getValue().getTimestamp());
            untilCalendar.add(Calendar.DATE, 1);
            List<RecurrenceId> possibleExceptionDates = asList(session.getRecurrenceService().iterateRecurrenceIds(
                eventUpdate.getUpdate(), new Date(exceptionDates.first().getValue().getTimestamp()), untilCalendar.getTime()));
            /*
             * reset no longer matching delete- and change exceptions if recurrence rule changes
             */
            boolean wasUpdated = false;
            if (false == isNullOrEmpty(originalEvent.getDeleteExceptionDates())) {
                SortedSet<RecurrenceId> newDeleteExceptionDates = new TreeSet<RecurrenceId>(originalEvent.getDeleteExceptionDates());
                if (newDeleteExceptionDates.retainAll(possibleExceptionDates)) {
                    eventUpdate.getUpdate().setDeleteExceptionDates(newDeleteExceptionDates);
                    wasUpdated |= true;
                }
            }
            if (false == isNullOrEmpty(originalEvent.getChangeExceptionDates())) {
                List<RecurrenceId> notMatchingChangeExceptionDates = new ArrayList<RecurrenceId>(originalEvent.getChangeExceptionDates());
                notMatchingChangeExceptionDates.removeAll(possibleExceptionDates);
                if (0 < notMatchingChangeExceptionDates.size()) {
                    deleteExceptions(originalEvent.getSeriesId(), notMatchingChangeExceptionDates);
                    SortedSet<RecurrenceId> newChangeExceptionDates = new TreeSet<RecurrenceId>(originalEvent.getChangeExceptionDates());
                    newChangeExceptionDates.removeAll(notMatchingChangeExceptionDates);
                    eventUpdate.getUpdate().setChangeExceptionDates(newChangeExceptionDates);
                    wasUpdated |= true;
                }
            }
            return wasUpdated;
        }
        return false;
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
     * Gets a value indicating whether the participation status of the event's attendees needs to be reset along with the update or not.
     *
     * @param eventUpdate The event update to evaluate
     * @return <code>true</code> if the attendee's participation status should be reseted, <code>false</code>, otherwise
     */
    private boolean needsParticipationStatusReset(ItemUpdate<Event, EventField> eventUpdate) throws OXException {
        return eventUpdate.containsAnyChangeOf(new EventField[] { EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.END_DATE });
    }

    /**
     * Gets a value indicating whether conflict checks should take place along with the update or not.
     *
     * @param eventUpdate The event update to evaluate
     * @param attendeeHelper The attendee helper for the update
     * @return <code>true</code> if conflict checks should take place, <code>false</code>, otherwise
     */
    private boolean needsConflictCheck(ItemUpdate<Event, EventField> eventUpdate, AttendeeHelper attendeeHelper) throws OXException {
        if (eventUpdate.containsAnyChangeOf(new EventField[] { EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.END_DATE })) {
            return true;
        }
        if (eventUpdate.getUpdatedFields().contains(EventField.TRANSP) && false == CalendarUtils.isOpaqueTransparency(eventUpdate.getOriginal())) {
            return true;
        }
        if (0 < attendeeHelper.getAttendeesToInsert().size()) {
            return true;
        }
        return false;
    }

    /**
     * Gets a value indicating whether the event's sequence number ought to be incremented along with the update or not.
     *
     * @param eventUpdate The event update to evaluate
     * @param attendeeHelper The attendee helper for the update
     * @return <code>true</code> if the event's sequence number should be updated, <code>false</code>, otherwise
     */
    private boolean needsSequenceNumberIncrement(ItemUpdate<Event, EventField> eventUpdate, AttendeeHelper attendeeHelper) throws OXException {
        if (eventUpdate.containsAnyChangeOf(new EventField[] { EventField.SUMMARY, EventField.LOCATION, EventField.RECURRENCE_RULE, EventField.START_DATE, EventField.END_DATE })) {
            return true;
        }
        if (0 < attendeeHelper.getAttendeesToDelete().size() || 0 < attendeeHelper.getAttendeesToInsert().size() || 0 < attendeeHelper.getAttendeesToUpdate().size()) {
            //TODO: more distinct evaluation of attendee updates
            return true;
        }
        return false;
    }

    /**
     * Gets a value indicating whether date-time-related properties of the event have changed so that an alarm trigger update is required.
     *
     * @param eventUpdate The event update to evaluate
     * @return <code>true</code> if alarm trigger updates should take place, <code>false</code>, otherwise
     */
    private boolean needsAlarmTriggerUpdate(ItemUpdate<Event, EventField> eventUpdate) throws OXException {
        return eventUpdate.containsAnyChangeOf(new EventField[] { EventField.START_DATE, EventField.END_DATE });
    }

    private boolean updateDeleteExceptions(Event originalEvent, Event updatedEvent) throws OXException {
        if (isSeriesMaster(originalEvent) && null != updatedEvent.getDeleteExceptionDates() && 0 < updatedEvent.getDeleteExceptionDates().size()) {
            if (false == isGroupScheduled(originalEvent) || isOrganizer(originalEvent, calendarUserId) || isLastUserAttendee(originalEvent.getAttendees(), calendarUserId)) {
                /*
                 * "real" delete exceptions for all attendees, take over as-is during normal update routine
                 */
                return false;
            }
            /*
             * check for newly indicated delete exceptions, from the calendar user's point of view
             */
            Attendee userAttendee = find(originalEvent.getAttendees(), calendarUserId);
            Event originalUserEvent = EventMapper.getInstance().copy(originalEvent, new Event(), (EventField[]) null);
            originalUserEvent = Utils.applyExceptionDates(storage, originalUserEvent, calendarUserId);
            SimpleCollectionUpdate<RecurrenceId> exceptionDateUpdates = getExceptionDateUpdates(originalUserEvent.getDeleteExceptionDates(), updatedEvent.getDeleteExceptionDates());
            if (0 < exceptionDateUpdates.getRemovedItems().size() || null == userAttendee) {
                throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), EventField.DELETE_EXCEPTION_DATES);
            }
            if (0 < exceptionDateUpdates.getAddedItems().size()) {
                for (RecurrenceId newDeleteException : exceptionDateUpdates.getAddedItems()) {
                    RecurrenceId recurrenceId = Check.recurrenceIdExists(session.getRecurrenceService(), originalEvent, newDeleteException);
                    if (contains(originalEvent.getChangeExceptionDates(), newDeleteException)) {
                        /*
                         * remove attendee from existing change exception
                         */
                        delete(loadExceptionData(originalEvent.getId(), recurrenceId), userAttendee);
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

    private boolean updateAttendees(Event originalEvent, Event updatedEvent, AttendeeHelper attendeeHelper) throws OXException {
        if (false == attendeeHelper.hasChanges()) {
            return false;
        }
        /*
         * perform attendee deletions
         */
        List<Attendee> attendeesToDelete = attendeeHelper.getAttendeesToDelete();
        if (0 < attendeesToDelete.size()) {
            requireWritePermissions(originalEvent, attendeesToDelete);
            storage.getEventStorage().insertEventTombstone(getTombstone(originalEvent, timestamp, calendarUserId));
            storage.getAttendeeStorage().insertAttendeeTombstones(originalEvent.getId(), getTombstones(attendeesToDelete));
            storage.getAttendeeStorage().deleteAttendees(originalEvent.getId(), attendeesToDelete);
            storage.getAlarmStorage().deleteAlarms(originalEvent.getId(), getUserIDs(attendeesToDelete));
        }
        /*
         * perform attendee updates
         */
        List<Attendee> attendeesToUpdate = attendeeHelper.getAttendeesToUpdate();
        if (0 < attendeesToUpdate.size()) {
            requireWritePermissions(originalEvent, attendeesToUpdate);
            storage.getAttendeeStorage().updateAttendees(originalEvent.getId(), attendeesToUpdate);
        }
        /*
         * perform attendee inserts
         */
        if (0 < attendeeHelper.getAttendeesToInsert().size()) {
            requireWritePermissions(originalEvent);
            storage.getAttendeeStorage().insertAttendees(originalEvent.getId(), attendeeHelper.getAttendeesToInsert());
        }
        return true;
    }

    private boolean updateAttachments(Event originalEvent, List<Attachment> originalAttachments, List<Attachment> newAttachments) throws OXException {
        List<Attachment> attachmentsToInsert = new ArrayList<Attachment>();
        List<Attachment> attachmentsToDelete = new ArrayList<Attachment>();
        if (null == originalAttachments) {
            if (null == newAttachments) {
                return false;
            }
            attachmentsToInsert.addAll(newAttachments);
        } else if (null == newAttachments) {
            attachmentsToDelete.addAll(originalAttachments);
        } else {
            for (Attachment newAttachment : newAttachments) {
                if (0 < newAttachment.getManagedId() && null != findAttachment(originalAttachments, newAttachment.getManagedId())) {
                    continue;
                }
                attachmentsToInsert.add(newAttachment);
            }
            for (Attachment originalAttachment : originalAttachments) {
                if (0 < originalAttachment.getManagedId() && null == findAttachment(newAttachments, originalAttachment.getManagedId())) {
                    attachmentsToDelete.add(originalAttachment);
                }
            }
        }
        if (attachmentsToDelete.isEmpty() && attachmentsToInsert.isEmpty()) {
            return false;
        }
        requireWritePermissions(originalEvent);
        if (0 < attachmentsToDelete.size()) {
            storage.getAttachmentStorage().deleteAttachments(session.getSession(), folder.getID(), originalEvent.getId(), attachmentsToDelete);
        }
        if (0 < attachmentsToInsert.size()) {
            storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getID(), originalEvent.getId(), attachmentsToInsert);
        }
        return true;
    }

    /**
     * Updates a calendar user's alarms for a specific event.
     *
     * @param event The event to update the alarms in
     * @param calendarUserId The identifier of the calendar user whose alarms are updated
     * @param updatedAlarms The updated alarms
     * @return <code>true</code> if there were any updates, <code>false</code>, otherwise
     */
    private boolean updateAlarms(Event event, int calendarUserId, List<Alarm> updatedAlarms) throws OXException {
        List<Alarm> originalAlarms = storage.getAlarmStorage().loadAlarms(event, calendarUserId);
        CollectionUpdate<Alarm, AlarmField> alarmUpdates = getAlarmUpdates(originalAlarms, updatedAlarms);
        if (alarmUpdates.isEmpty()) {
            return false;
        }
        requireWritePermissions(event);
        /*
         * delete removed alarms
         */
        List<Alarm> removedItems = alarmUpdates.getRemovedItems();
        if (0 < removedItems.size()) {
            storage.getAlarmStorage().deleteAlarms(event.getId(), calendarUserId, getAlarmIDs(removedItems));
        }
        /*
         * save updated alarms
         */
        List<ItemUpdate<Alarm, AlarmField>> updatedItems = alarmUpdates.getUpdatedItems();
        if (0 < updatedItems.size()) {
            List<Alarm> alarms = new ArrayList<Alarm>(updatedItems.size());
            for (ItemUpdate<Alarm, AlarmField> itemUpdate : updatedItems) {
                Alarm alarm = AlarmMapper.getInstance().copy(itemUpdate.getOriginal(), null, (AlarmField[]) null);
                AlarmMapper.getInstance().copy(itemUpdate.getUpdate(), alarm, AlarmField.values());
                alarm.setId(itemUpdate.getOriginal().getId());
                alarm.setUid(itemUpdate.getOriginal().getUid());
                alarms.add(Check.alarmIsValid(alarm));
            }
            storage.getAlarmStorage().updateAlarms(event, calendarUserId, alarms);
        }
        /*
         * insert new alarms
         */
        insertAlarms(event, calendarUserId, alarmUpdates.getAddedItems(), false);
        return true;
    }

    /**
     * Resets the participation status of all individual attendees - excluding the current calendar user - to
     * {@link ParticipationStatus#NEEDS_ACTION} for a specific event.
     *
     * @param objectID The identifier of the event to reset the participation status for
     * @param attendees The event's attendees
     * @return <code>true</code> if at least one attendee was updated, <code>false</code>, otherwise
     */
    private boolean resetParticipationStatus(String objectID, List<Attendee> attendees) throws OXException {
        List<Attendee> attendeesToUpdate = new ArrayList<Attendee>();
        for (Attendee attendee : CalendarUtils.filter(attendees, null, CalendarUserType.INDIVIDUAL)) {
            if (calendarUserId == attendee.getEntity() || ParticipationStatus.NEEDS_ACTION.equals(attendee.getPartStat())) {
                continue;
            }
            Attendee attendeeUpdate = new Attendee();
            AttendeeMapper.getInstance().copy(attendee, attendeeUpdate, AttendeeField.ENTITY, AttendeeField.MEMBER, AttendeeField.CU_TYPE, AttendeeField.URI);
            attendeeUpdate.setPartStat(ParticipationStatus.NEEDS_ACTION); //TODO: or reset to initial partstat based on folder type?
            attendeesToUpdate.add(attendeeUpdate);
        }
        if (0 < attendeesToUpdate.size()) {
            storage.getAttendeeStorage().updateAttendees(objectID, attendeesToUpdate);
            return true;
        }
        return false;
    }

    /**
     * Prepares the event update by generating a delta event object where all changed properties are <i>set</i>. The last modification
     * timestamp, the modified by field and the event identifier are assigned implicitly. Only changeable properties targeting the
     * {@link EventStorage} are considered, while other adjacent event data like alarms, attendees and attachments (as set in
     * {@link UpdatePerformer#SKIPPED_FIELDS} is left out.
     *
     * @param originalEvent The original event
     * @param updatedEvent The updated event
     * @param newAttendees The forecasted resulting attendees in the event after the update was performed
     * @param ignoredFields Additional fields to ignore when determining the differences; {@link UpdatePerformer#SKIPPED_FIELDS} are always skipped
     * @return The event update, or <code>null</code> if no changes of the event data were detected
     */
    private ItemUpdate<Event, EventField> prepareEventUpdate(Event originalEvent, Event updatedEvent, List<Attendee> newAttendees, EventField... ignoredFields) throws OXException {
        /*
         * determine & check modified fields
         */
        Event eventUpdate = EventMapper.getInstance().getDifferences(originalEvent, updatedEvent, true, Arrays.add(SKIPPED_FIELDS, ignoredFields));
        for (EventField field : EventMapper.getInstance().getAssignedFields(eventUpdate)) {
            switch (field) {
                case TRANSP:
                    /*
                     * ignore OPAQUE to TRANSPARENT transition when attendee's participation status is declined
                     */
                    if (null != updatedEvent.getTransp() && Transp.TRANSPARENT.equals(updatedEvent.getTransp().getValue()) &&
                        false == isOrganizer(originalEvent, calendarUserId)) {
                        Attendee originalAttendee = find(originalEvent.getAttendees(), calendarUserId);
                        if (null != originalAttendee && false == ParticipationStatus.DECLINED.equals(originalAttendee.getPartStat())) {
                            Attendee updatedAttendee = find(updatedEvent.getAttendees(), calendarUserId);
                            if (null == updatedAttendee || ParticipationStatus.DECLINED.equals(updatedAttendee.getPartStat())) {
                                eventUpdate.removeTransp();
                            }
                        }
                    }
                    break;
                case CLASSIFICATION:
                    /*
                     * check validity, treating PUBLIC as default value
                     */
                    Check.classificationIsValid(eventUpdate.getClassification(), folder);
                    if (isSeriesException(originalEvent) && (
                        isPublicClassification(originalEvent) && false == isPublicClassification(eventUpdate) ||
                        false == isPublicClassification(originalEvent) && isPublicClassification(eventUpdate))) {
                        throw CalendarExceptionCodes.UNSUPPORTED_CLASSIFICATION_FOR_OCCURRENCE.create(
                            String.valueOf(eventUpdate.getClassification()), originalEvent.getSeriesId(), String.valueOf(originalEvent.getRecurrenceId()));
                    }
                    break;
                case GEO:
                    /*
                     * check validity
                     */
                    Check.geoLocationIsValid(eventUpdate);
                    break;
                case RECURRENCE_RULE:
                    /*
                     * deny update for change exceptions (but ignore if set to 'null')
                     */
                    if (isSeriesException(originalEvent)) {
                        if (null == eventUpdate.getRecurrenceRule()) {
                            eventUpdate.removeRecurrenceRule();
                            break;
                        }
                        // TODO: better ignore? com.openexchange.ajax.appointment.recurrence.UsmFailureDuringRecurrenceTest.testShouldFailWhenTryingToMakeAChangeExceptionASeriesButDoesNot()
                        //       vs. com.openexchange.ajax.appointment.recurrence.TestsForModifyingChangeExceptions.testShouldNotAllowTurningAChangeExceptionIntoASeries()
                        throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), field);
                    }
                    if (isSeriesMaster(originalEvent) && null == eventUpdate.getRecurrenceRule()) {
                        /*
                         * series to single event, remove recurrence & ensure all necessary recurrence data is present in passed event update
                         */
                        EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE);
                        eventUpdate.setSeriesId(null);
                        eventUpdate.setChangeExceptionDates(null);
                        eventUpdate.setDeleteExceptionDates(null);
                        break;
                    }
                    /*
                     * ensure all necessary recurrence related data is present in passed event update & check rule validity
                     */
                    EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE);
                    Check.recurrenceRuleIsValid(session.getRecurrenceService(), eventUpdate);
                    /*
                     * single event to series, assign new recurrence id
                     */
                    if (null == originalEvent.getSeriesId()) {
                        eventUpdate.setSeriesId(originalEvent.getId());
                    }
                    break;
                case START_DATE:
                case END_DATE:
                    /*
                     * ensure all necessary recurrence related data is present in passed event update, adjust start/end & re-validate start- and end date
                     */
                    EventMapper.getInstance().copyIfNotSet(originalEvent, eventUpdate, EventField.RECURRENCE_RULE, EventField.SERIES_ID, EventField.START_DATE, EventField.END_DATE);
                    Consistency.adjustAllDayDates(eventUpdate);
                    Check.startAndEndDate(eventUpdate);
                    break;
                case RECURRENCE_ID:
                    if (false == isSeriesException(originalEvent) && null == eventUpdate.getRecurrenceId()) {
                        // ignore neutral value
                        break;
                    }
                    throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), field);
                case DELETE_EXCEPTION_DATES:
                    if (isNullOrEmpty(eventUpdate.getDeleteExceptionDates()) && isNullOrEmpty(originalEvent.getDeleteExceptionDates())) {
                        // ignore neutral value
                        break;
                    }
                    if (false == isSeriesMaster(originalEvent)) {
                        throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), field);
                    }
                    if (null != eventUpdate.getDeleteExceptionDates()) {
                        Check.recurrenceIdsExist(session.getRecurrenceService(), originalEvent, eventUpdate.getDeleteExceptionDates());
                        SimpleCollectionUpdate<RecurrenceId> exceptionDateUpdates = getExceptionDateUpdates(originalEvent.getDeleteExceptionDates(), eventUpdate.getDeleteExceptionDates());
                        if (0 < exceptionDateUpdates.getRemovedItems().size()) {
                            throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), field);
                        }
                    }
                    break;
                case CHANGE_EXCEPTION_DATES:
                    if (isNullOrEmpty(eventUpdate.getDeleteExceptionDates()) && isNullOrEmpty(originalEvent.getDeleteExceptionDates())) {
                        // ignore neutral value
                        break;
                    }
                    throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), field);
                case CREATED:
                    // ignore implicitly
                    eventUpdate.removeCreated();
                    break;
                case CREATED_BY:
                    // ignore implicitly
                    eventUpdate.removeCreatedBy();
                    break;
                case LAST_MODIFIED:
                    // ignore implicitly
                    eventUpdate.removeLastModified();
                    break;
                case MODIFIED_BY:
                    // ignore implicitly
                    eventUpdate.removeModifiedBy();
                    break;
                case SEQUENCE:
                    // ignore implicitly
                    eventUpdate.removeSequence();
                    break;
                case FOLDER_ID:
                    if (Utils.getFolderView(storage, originalEvent, calendarUserId).equals(eventUpdate.getFolderId())) {
                        // ignore implicitly
                        eventUpdate.removeFolderId();
                        break;
                    }
                    throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), field);
                case UID:
                case SERIES_ID:
                case CALENDAR_USER:
                    throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), field);
                default:
                    break;
            }
        }
        /*
         * adjust attendee-dependent fields
         */
        if (null == newAttendees || 0 == newAttendees.size()) {
            /*
             * no group-scheduled event (any longer), ensure to take over common calendar folder & remove organizer
             */
            if (null != originalEvent.getOrganizer() || eventUpdate.containsOrganizer()) {
                eventUpdate.setOrganizer(null);
            }
            if (null == originalEvent.getFolderId() || eventUpdate.containsFolderId()) {
                eventUpdate.setFolderId(folder.getID());
            }
        } else {
            /*
             * group-scheduled event, ensure to take over an appropriate organizer & reset common calendar folder (unless public)
             */
            if (null == originalEvent.getOrganizer()) {
                eventUpdate.setOrganizer(prepareOrganizer(eventUpdate.getOrganizer()));
            } else if (eventUpdate.containsOrganizer()) {
                Organizer organizer = session.getEntityResolver().prepare(eventUpdate.getOrganizer(), CalendarUserType.INDIVIDUAL);
                if (null != organizer && false == matches(originalEvent.getOrganizer(), organizer)) {
                    throw CalendarExceptionCodes.FORBIDDEN_CHANGE.create(originalEvent.getId(), EventField.ORGANIZER);
                }
                eventUpdate.removeOrganizer(); // ignore
            }
            if (null != originalEvent.getFolderId() || eventUpdate.containsFolderId()) {
                eventUpdate.setFolderId(PublicType.getInstance().equals(folder.getType()) ? folder.getID() : null);
            }
        }
        if (calendarUserId != originalEvent.getCalendarUser() || eventUpdate.containsCalendarUser()) {
            Consistency.setCalenderUser(folder, eventUpdate);
        }
        if (0 == EventMapper.getInstance().getAssignedFields(eventUpdate).length) {
            return null;
        }
        EventMapper.getInstance().copy(originalEvent, eventUpdate, EventField.ID);
        Consistency.setModified(timestamp, eventUpdate, session.getUserId());
        return new DefaultItemUpdate<Event, EventField>(EventMapper.getInstance(), originalEvent, eventUpdate);
    }

    private void requireWritePermissions(Event originalEvent) throws OXException {
        if (session.getUserId() == originalEvent.getCreatedBy()) {
            requireCalendarPermission(folder, READ_FOLDER, READ_OWN_OBJECTS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        } else {
            requireCalendarPermission(folder, READ_FOLDER, READ_ALL_OBJECTS, WRITE_ALL_OBJECTS, NO_PERMISSIONS);
        }
        Check.classificationAllowsUpdate(folder, originalEvent);
    }

    private void requireWritePermissions(Event originalEvent, List<Attendee> updatedAttendees) throws OXException {
        if (null != updatedAttendees && (1 < updatedAttendees.size() || session.getUserId() != updatedAttendees.get(0).getEntity())) {
            requireWritePermissions(originalEvent);
        }
    }

}
