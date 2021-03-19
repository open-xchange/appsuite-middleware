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

import static com.openexchange.chronos.common.CalendarUtils.isAllDay;
import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.chronos.impl.Utils.isSkipExternalAttendeeURIChecks;
import static com.openexchange.chronos.impl.scheduling.SchedulingUtils.prepareAttendees;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Classification;
import com.openexchange.chronos.DefaultAttendeePrivileges;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.RecurrenceRange;
import com.openexchange.chronos.TimeTransparency;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Consistency;
import com.openexchange.chronos.impl.InternalAttendeeUpdates;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.InternalEventUpdate;
import com.openexchange.chronos.impl.JSONPrintableEvent;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.performer.SplitPerformer;
import com.openexchange.chronos.impl.performer.UpdatePerformer;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;
import com.openexchange.groupware.tools.mappings.common.ItemUpdate;
import com.openexchange.groupware.tools.mappings.common.SimpleCollectionUpdate;
import com.openexchange.java.Strings;

/**
 * {@link RequestProcessor} - Processes the method {@link SchedulingMethod#REQUEST}
 * <p>
 * Note: This processor does <b>NOT</b> schedule any messages. This processors applies data from the message
 * only with modifying the data to our model.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v8.0.0
 */
public class RequestProcessor extends UpdatePerformer {

    /**
     * Initializes a new {@link RequestProcessor}.
     * 
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @throws OXException If initialization fails
     */
    public RequestProcessor(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Creates or updates events as transmitted by the organizer
     *
     * @param message The {@link IncomingSchedulingMessage}
     * @return An {@link InternalCalendarResult} containing the changes that has been performed
     * @throws OXException In case data is invalid, outdated or permissions are missing
     */
    public InternalCalendarResult process(IncomingSchedulingMessage message) throws OXException {
        EventID eventID = resolveEvent(message);
        if (null == eventID) {
            createEvent(message);
        } else {
            updateEvent(message, eventID);
        }
        return resultTracker.getResult();
    }

    /**
     * Creates the transmitted event(s)
     *
     * @param message The message
     * @throws OXException In case user isn't allowed to create event(s)
     */
    private void createEvent(IncomingSchedulingMessage message) throws OXException {
        /*
         * Check internal constrains
         */
        requireCalendarPermission(folder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        Check.quotaNotExceeded(storage, session);

        /*
         * Prepare event
         */
        Event newEvent = message.getResource().getFirstEvent();
        if (null != newEvent.getRecurrenceId()) {
            // XXX We can't handle single exception invitations at the moment, see MW-1134
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Unable to create exception only");
        }
        List<Event> changeExceptions = null;
        boolean looksLikeSeriesMaster = SchedulingUtils.looksLikeSeriesMaster(newEvent);
        if (looksLikeSeriesMaster) {
            /*
             * Filter for existing change exceptions, remove change exceptions we don't have data for
             */
            changeExceptions = new LinkedList<>();
            if (false == isNullOrEmpty(newEvent.getChangeExceptionDates())) {
                for (RecurrenceId recurrenceId : newEvent.getChangeExceptionDates()) {
                    Event changeException = message.getResource().getChangeException(recurrenceId);
                    if (null == changeException) {
                        LOG.warn("Unable to find change exception with recurrence ID {} in master event. Removing exception from master event", recurrenceId);
                        session.addWarning(CalendarExceptionCodes.IGNORED_INVALID_DATA.create(recurrenceId, EventField.CHANGE_EXCEPTION_DATES, "normal", "No data for transmitted recurrence found"));
                    } else {
                        changeExceptions.add(changeException);
                    }
                }
            }
            TreeSet<RecurrenceId> exceptionDates = new TreeSet<RecurrenceId>();
            exceptionDates.addAll(changeExceptions.stream().map((e) -> e.getRecurrenceId()).collect(Collectors.toList()));
            /*
             * Check if we handled all transmitted exceptions
             */
            if (false == isNullOrEmpty(message.getResource().getChangeExceptions())) {
                for (Event exception : message.getResource().getChangeExceptions()) {
                    if (false == CalendarUtils.contains(exceptionDates, exception.getRecurrenceId())) {
                        if (newEvent.getUid().equals(exception.getUid())) {
                            LOG.trace("Adding event exception {}", new JSONPrintableEvent(session, exception));
                            changeExceptions.add(exception);
                            exceptionDates.add(exception.getRecurrenceId());
                        } else {
                            LOG.warn("Found event that does not belong to the series with the UID {}. Event: {}", newEvent.getUid(), new JSONPrintableEvent(session, exception));
                            session.addWarning(CalendarExceptionCodes.IGNORED_INVALID_DATA.create(exception.getRecurrenceId(), EventField.CHANGE_EXCEPTION_DATES, "normal", "Event belongs to another series"));
                        }
                    }
                }
            }
            newEvent.setChangeExceptionDates(exceptionDates);
        }

        newEvent = createEvent(message, newEvent);
        /*
         * Create change exception as needed
         */
        if (looksLikeSeriesMaster && false == isNullOrEmpty(changeExceptions)) {
            AddProcessor addProcessor = new AddProcessor(this, true);
            addProcessor.process(message, changeExceptions);
        }
    }

    /**
     * Updates the transmitted event(s)
     *
     * @param message The message
     * @param eventID The ID of the existing event to update
     */
    private void updateEvent(IncomingSchedulingMessage message, EventID eventID) throws OXException {
        Event originalEvent = loadEventData(eventID.getObjectID());
        CalendarUser originator = message.getSchedulingObject().getOriginator();
        /*
         * Check if originator is allowed to update
         */
        if (false == SchedulingUtils.originatorMatches(originalEvent, originator)) {
            throw CalendarExceptionCodes.NOT_ORGANIZER.create(folder.getId(), originalEvent.getId(), originator.getUri(), originator.getCn());
        }
        Event update = prepareUpdatedEvent(originalEvent, message.getResource().getFirstEvent());
        if (false == CalendarUtils.isSeriesEvent(originalEvent)) {
            /*
             * Update single event
             */
            InternalEventUpdate eventUpdate = new InternalEventUpdate(session, folder, originalEvent, null, null, update, timestamp, true, SKIPPED_FIELDS);
            if (eventUpdate.isEmpty()) {
                return;
            }
            update = updateEvent(message, eventUpdate);
            /*
             * Check if the event has been transformed to a series
             */
            if (SchedulingUtils.looksLikeSeriesEvent(update) && false == eventUpdate.getExceptionUpdates().isEmpty()) {
                /*
                 * Add new exceptions, nothing to delete or update so far
                 */
                new AddProcessor(this, true).process(message, eventUpdate.getExceptionUpdates().getAddedItems());
            }
            return;
        }
        Event originalMasterEvent = loadEventData(originalEvent.getSeriesId());
        if (SchedulingUtils.looksLikeSeriesMaster(update)) {
            /*
             * Handle "this and future"
             */
            if (null != update.getRecurrenceId() && null != update.getRecurrenceId().getRange()) {
                RecurrenceId recurrenceId = update.getRecurrenceId();
                Check.recurrenceRangeMatches(recurrenceId, RecurrenceRange.THISANDFUTURE);
                Entry<CalendarObjectResource, CalendarObjectResource> splitResult = new SplitPerformer(this).split(originalMasterEvent, recurrenceId.getValue(), null);
                /*
                 * Apply the update for the splitted series master event after rolling back the related-to field, taking over a new recurrence rule as needed
                 */
                Event updatedSeriesMaster = splitResult.getValue().getSeriesMaster();
                if (null == updatedSeriesMaster) {
                    throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Unable to track update. Reason: Nothing was changed.");
                }
                originalEvent = adjustUpdatedSeriesAfterSplit(originalMasterEvent, updatedSeriesMaster);
                Event eventUpdate = adjustClientUpdateAfterSplit(originalMasterEvent, updatedSeriesMaster, update);
                eventUpdate = prepareUpdatedEvent(originalEvent, eventUpdate);
                updateEvent(message, new InternalEventUpdate(session, folder, originalEvent, loadExceptionData(originalEvent), updatedSeriesMaster, eventUpdate, timestamp, true, EventField.ID, EventField.RECURRENCE_ID, EventField.DELETE_EXCEPTION_DATES, EventField.CHANGE_EXCEPTION_DATES));
                return;
            }
            /*
             * Update series
             */
            Event updatedSeriesMaster = update;
            List<Event> originalChangeExceptions = loadExceptionData(originalMasterEvent);
            updatedSeriesMaster = prepareUpdatedEvent(originalMasterEvent, updatedSeriesMaster);
            InternalEventUpdate eventUpdate = new InternalEventUpdate(session, folder, originalEvent, originalChangeExceptions, originalMasterEvent, updatedSeriesMaster, timestamp, true, SKIPPED_FIELDS);
            updateEvent(message, eventUpdate);
            /*
             * Update exceptions
             */
            List<Event> changeExceptions = message.getResource().getChangeExceptions();
            EventUpdates eventUpdates = CalendarUtils.getEventUpdates(originalChangeExceptions, changeExceptions, EventField.ID);
            if (false == eventUpdates.getUpdatedItems().isEmpty()) {
                for (ItemUpdate<Event, EventField> exceptionUpdate : eventUpdates.getUpdatedItems()) {
                    Event original = exceptionUpdate.getOriginal();
                    Event changeExceptionUpdate = prepareUpdatedEvent(original, exceptionUpdate.getUpdate());
                    updateEvent(message, new InternalEventUpdate(session, folder, original, null, originalMasterEvent, changeExceptionUpdate, timestamp, true, SKIPPED_FIELDS));
                }
            }
            /*
             * Handle deletions and inserts of exceptions
             */
            if (false == eventUpdates.getRemovedItems().isEmpty()) {
                requireWritePermissions(originalEvent, true);
                for (Event removedException : eventUpdates.getRemovedItems()) {
                    if (null != CalendarUtils.find(originalMasterEvent.getChangeExceptionDates(), removedException.getRecurrenceId())) {
                        deleteException(originalMasterEvent, loadExceptionData(originalMasterEvent, removedException.getRecurrenceId()));
                    } else {
                        delete(removedException);
                        addDeleteExceptionDate(originalMasterEvent, removedException.getRecurrenceId());
                    }
                }

            }
            if (false == eventUpdates.getAddedItems().isEmpty()) {
                new AddProcessor(this, true).process(message, eventUpdates.getAddedItems());
            }
            return;
        }
        /*
         * Update single recurrence(s), gather what is to add and what to update
         */
        List<Event> createableEvents = new LinkedList<>();
        List<Event> updateableEvents = new LinkedList<>();
        SortedSet<RecurrenceId> changeExceptionDates = originalMasterEvent.getChangeExceptionDates();
        SortedSet<RecurrenceId> deleteExceptionDates = originalMasterEvent.getDeleteExceptionDates();
        for (Event exceptionUpdate : message.getResource().getEvents()) {
            RecurrenceId recurrenceId = exceptionUpdate.getRecurrenceId();
            if (CalendarUtils.contains(changeExceptionDates, recurrenceId)) {
                updateableEvents.add(exceptionUpdate);
            } else {
                if (CalendarUtils.contains(deleteExceptionDates, recurrenceId)) {
                    /*
                     * cannot update a delete exception
                     */
                    throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(originalMasterEvent.getSeriesId(), recurrenceId);
                }
                createableEvents.add(exceptionUpdate);
            }
        }
        /*
         * Perform the inserts and the updates
         */
        if (false == createableEvents.isEmpty()) {
            new AddProcessor(this, true).process(message, createableEvents);
        }
        if (false == updateableEvents.isEmpty()) {
            for (Event exceptionUpdate : updateableEvents) {
                Event originalExceptionEvent = loadExceptionData(originalMasterEvent, exceptionUpdate.getRecurrenceId());
                Event updatedException = prepareUpdatedEvent(originalExceptionEvent, exceptionUpdate);
                InternalEventUpdate exceptionEventUpdate = new InternalEventUpdate(session, folder, originalExceptionEvent, null, originalMasterEvent, updatedException, timestamp, true, SKIPPED_FIELDS);
                updateEvent(message, exceptionEventUpdate);
            }
        }
    }

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Resolves the first transmitted event to the stored version, if any exists
     *
     * @param message The message to get the first event from
     * @return The {@link EventID} or <code>null</code> if no event can be found
     * @throws OXException In case of error
     */
    private EventID resolveEvent(IncomingSchedulingMessage message) throws OXException {
        Event firstEvent = message.getResource().getFirstEvent();
        try {
            return Utils.resolveEventId(session, storage, firstEvent.getUid(), firstEvent.getRecurrenceId(), message.getTargetUser());
        } catch (OXException e) {
            if (!e.equalsCode(CalendarExceptionCodes.EVENT_NOT_FOUND.getNumber(), CalendarExceptionCodes.PREFIX)) {
                throw e;
            }
            LOG.trace("Unable to resolve event", e);
        }
        return null;
    }

    /**
     * Creates the given event in the storages
     *
     * @param message The message to get e.g. attachments from
     * @param event The event to create
     * @return The created event
     * @throws OXException
     */
    private Event createEvent(IncomingSchedulingMessage message, Event event) throws OXException {
        Event newEvent = prepareEvent(event);
        /*
         * Trigger calendar interceptors
         */
        interceptorRegistry.triggerInterceptorsOnBeforeCreate(newEvent);
        /*
         * Insert event, attendees, attachments & conferences
         */
        storage.getEventStorage().insertEvent(newEvent);
        if (false == isNullOrEmpty(newEvent.getAttendees())) {
            storage.getAttendeeStorage().insertAttendees(newEvent.getId(), newEvent.getAttendees());
        }
        if (false == isNullOrEmpty(newEvent.getAttachments())) {
            storage.getAttachmentStorage().insertAttachments(session.getSession(), folder.getId(), newEvent.getId(), SchedulingUtils.getBinaryAttachments(event.getAttachments(), message));
        }
        if (false == isNullOrEmpty(newEvent.getConferences())) {
            storage.getConferenceStorage().insertConferences(newEvent.getId(), newEvent.getConferences());
        }
        Event loadEventData = loadEventData(newEvent.getId());
        /*
         * Load default alarms for new event and insert them
         */
        Map<Integer, List<Alarm>> alarmsPerUserId = new HashMap<Integer, List<Alarm>>();
        List<Alarm> defaultAlarm = isAllDay(newEvent) ? session.getConfig().getDefaultAlarmDate(calendarUserId) : session.getConfig().getDefaultAlarmDateTime(calendarUserId);
        if (null != defaultAlarm) {
            alarmsPerUserId.put(I(calendarUserId), insertAlarms(newEvent, calendarUserId, defaultAlarm, true));
        }
        storage.getAlarmTriggerStorage().insertTriggers(newEvent, alarmsPerUserId);
        if (CalendarUtils.isSeriesException(newEvent)) {
            resultTracker.trackCreation(loadEventData, loadEventData(newEvent.getSeriesId()));
        }
        resultTracker.trackCreation(loadEventData);
        return loadEventData;
    }

    /**
     * Prepares the event to be inserted in the storage by adjusting properties as needed
     *
     * @param eventData The event data to prepare from
     * @return The prepared event
     * @throws OXException In case event data contain unusable data
     */
    private Event prepareEvent(Event eventData) throws OXException {
        Event event = new Event();
        /*
         * identifiers
         */
        if (false == eventData.containsUid() || Strings.isEmpty(eventData.getUid())) {
            throw CalendarExceptionCodes.INVALID_DATA.create(EventField.UID, "UID of the event is missing. Without the UID the event can't be referenced and updated in the future");
        }
        event.setUid(eventData.getUid());
        event.setId(storage.getEventStorage().nextId());
        /*
         * Prepare as group-scheduled event, we are invited attendee
         */
        event.setOrganizer(prepareOrganizer(eventData.getOrganizer()));
        event.setSequence(eventData.containsSequence() ? eventData.getSequence() : 0);
        event.setFolderId(PublicType.getInstance().equals(folder.getType()) ? folder.getId() : null);
        event.setAttendeePrivileges(DefaultAttendeePrivileges.DEFAULT);
        /*
         * creation/modification/calendar user metadata
         */
        Consistency.setCreated(session, timestamp, event, session.getUserId());
        Consistency.setModified(session, timestamp, event, session.getUserId());
        Consistency.setCalenderUser(session, folder, event);
        /*
         * date/time related properties
         */
        Check.startAndEndDate(session, eventData);
        EventMapper.getInstance().copy(eventData, event, EventField.START_DATE, EventField.END_DATE);
        Consistency.adjustAllDayDates(event);
        Consistency.adjustTimeZones(session.getSession(), calendarUserId, event, null);
        /*
         * attendees, attachments, conferences
         */
        prepareAttendees(session, null, eventData, calendarUser);
        event.setAttendees(Check.maxAttendees(getSelfProtection(), InternalAttendeeUpdates.onNewEvent(session, folder, eventData, timestamp).getAddedItems()));
        event.setConferences(prepareConferences(Check.maxConferences(getSelfProtection(), eventData.getConferences())));
        /*
         * classification, transparency, color, geo
         */
        if (eventData.containsClassification() && null != eventData.getClassification()) {
            event.setClassification(Check.classificationIsValid(eventData.getClassification(), folder, event.getAttendees()));
        } else {
            event.setClassification(Classification.PUBLIC);
        }
        event.setTransp(eventData.containsTransp() && null != eventData.getTransp() ? eventData.getTransp() : TimeTransparency.OPAQUE);
        event.setColor(eventData.containsColor() ? eventData.getColor() : null);
        if (eventData.containsGeo()) {
            event.setGeo(Check.geoLocationIsValid(eventData));
        }
        /*
         * recurrence related fields
         */
        if (eventData.containsRecurrenceRule() && null != eventData.getRecurrenceRule()) {
            EventMapper.getInstance().copy(eventData, event, EventField.RECURRENCE_RULE, EventField.RECURRENCE_ID, EventField.RECURRENCE_DATES);
            Consistency.adjustRecurrenceRule(event);
            event.setRecurrenceRule(Check.recurrenceRuleIsValid(session.getRecurrenceService(), event));
            event.setSeriesId(event.getId());
            event.setRecurrenceDates(eventData.getRecurrenceDates());
            event.setDeleteExceptionDates(Check.recurrenceIdsExist(session.getRecurrenceService(), event, eventData.getDeleteExceptionDates()));
            Consistency.normalizeRecurrenceIDs(event.getStartDate(), event);
        }
        /*
         * copy over further (unchecked) event fields
         */
        return EventMapper.getInstance().copy(eventData, event, EventField.SUMMARY, EventField.LOCATION, EventField.DESCRIPTION, EventField.CATEGORIES, //
            EventField.FILENAME, EventField.URL, EventField.RELATED_TO, EventField.STATUS, EventField.EXTENDED_PROPERTIES, EventField.ATTACHMENTS);
    }

    /**
     * Prepares the external organizer
     *
     * @param organizer The transmitted organizer
     * @return The usable organizer
     * @throws OXException If preparation fails
     */
    private Organizer prepareOrganizer(Organizer organizerData) throws OXException {
        Organizer organizer = session.getEntityResolver().prepare(organizerData, CalendarUserType.INDIVIDUAL, null);
        if (0 < organizer.getEntity()) {
            /*
             * internal organizer ?!
             */
            throw CalendarExceptionCodes.INVALID_CALENDAR_USER.create(organizer.getUri(), I(organizer.getEntity()), CalendarUserType.INDIVIDUAL);
        }
        /*
         * take over external organizer as-is
         */
        return isSkipExternalAttendeeURIChecks(session) ? organizer : Check.requireValidEMail(organizer);
    }

    private Event prepareUpdatedEvent(Event originalEvent, Event updatedEvent) throws OXException {
        restoreInjectedAttendeeDate(originalEvent, updatedEvent);

        /*
         * Check internal constrains
         */
        Check.organizerMatches(originalEvent, updatedEvent);
        Check.requireInSequence(originalEvent, updatedEvent);
        Check.eventIsVisible(folder, originalEvent);
        Check.eventIsInFolder(originalEvent, folder);
        Check.startAndEndDate(session, updatedEvent);
        if (updatedEvent.containsGeo()) {
            Check.geoLocationIsValid(updatedEvent);
        }
        requireWritePermissions(originalEvent, true);
        getSelfProtection().checkEvent(updatedEvent);
        Event update = EventMapper.getInstance().copy(updatedEvent, null, (EventField[]) null);
        EventMapper.getInstance().copy(originalEvent, update, new EventField[] { EventField.SERIES_ID, EventField.ORGANIZER, EventField.CALENDAR_USER, EventField.ATTENDEE_PRIVILEGES });

        prepareAttendees(session, originalEvent, update, calendarUser);
        return update;
    }

    /**
     * Updates an specific event
     *
     * @param message The message to get optional attachments from
     * @param eventUpdate The event to update
     * @return The updated event as saved in the storage
     * @throws OXException In case of error
     */
    private Event updateEvent(IncomingSchedulingMessage message, InternalEventUpdate eventUpdate) throws OXException {
        if (eventUpdate.isEmpty()) {
            return eventUpdate.getOriginal();
        }
        /*
         * Check for conflicts if needed
         */
        if (SchedulingUtils.needsConflictCheck(session, eventUpdate)) {
            Check.noConflicts(storage, session, eventUpdate.getUpdate(), eventUpdate.getAttendeeUpdates().previewChanges());
        }

        /*
         * Trigger calendar interceptors
         */
        interceptorRegistry.triggerInterceptorsOnBeforeUpdate(eventUpdate.getOriginal(), eventUpdate.getUpdate());
        /*
         * Update event data in storage
         */
        Event originalEvent = eventUpdate.getOriginal();
        storeEventUpdate(originalEvent, eventUpdate.getDelta(), eventUpdate.getUpdatedFields(), true);
        storeAttendeeUpdates(originalEvent, eventUpdate.getAttendeeUpdates(), true);
        storeConferenceUpdates(originalEvent, eventUpdate.getConferenceUpdates(), true);
        if (false == eventUpdate.getAttachmentUpdates().isEmpty()) {
            /*
             * Prepare added items and insert them into the storage
             */
            SimpleCollectionUpdate<Attachment> attachmentUpdates = eventUpdate.getAttachmentUpdates();
            storeAttachmentUpdates(originalEvent, SchedulingUtils.getAttachmentUpdates(attachmentUpdates, message), true);
        }
        Event storedEvent = loadEventData(originalEvent.getId());
        resultTracker.trackUpdate(originalEvent, storedEvent);
        return storedEvent;
    }

}
