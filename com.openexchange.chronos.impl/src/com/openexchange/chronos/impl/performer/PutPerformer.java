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

import static com.openexchange.chronos.common.CalendarUtils.collectAttendees;
import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getFolderView;
import static com.openexchange.chronos.common.CalendarUtils.getSimpleAttendeeUpdates;
import static com.openexchange.chronos.common.CalendarUtils.hasExternalOrganizer;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesException;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import static com.openexchange.chronos.impl.Utils.isReply;
import static com.openexchange.chronos.impl.Utils.isReschedule;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.CalendarObjectResource;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.DefaultCalendarObjectResource;
import com.openexchange.chronos.common.DefaultEventUpdates;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.InternalEventUpdate;
import com.openexchange.chronos.service.CalendarResult;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.DeleteResult;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.common.AbstractSimpleCollectionUpdate;
import com.openexchange.java.Strings;

/**
 * {@link PutPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class PutPerformer extends AbstractUpdatePerformer {
    
    /**
     * Initializes a new {@link PutPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public PutPerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Puts a new or updated event from a calendar object resource to the calendar. In case the calendar resource already exists, the event
     * is updated or added to the resource implicitly if possible.
     *
     * @param eventData The event to store for the calendar object resource
     * @return The result
     */
    public InternalCalendarResult perform(Event eventData) throws OXException {
        return perform(Collections.singletonList(eventData), false);
    }

    /**
     * Puts a new or updated calendar object resource, i.e. an event and/or its change exceptions, to the calendar. In case the calendar
     * resource already exists under the perspective of the parent folder, it is updated implicitly: No longer indicated events are
     * removed, new ones are added, and existing ones are updated.
     *
     * @param resource The calendar object resource to store
     * @return The result
     */
    public InternalCalendarResult perform(CalendarObjectResource resource) throws OXException {
        return perform(resource.getEvents(), true);
    }

    /**
     * Puts new or updated events from a calendar object resource, i.e. an event and/or its change exceptions, to the calendar. In case
     * the calendar resource already exists under the perspective of the parent folder, it is updated (replaced) implicitly.
     *
     * @param eventData The list of events to store for the calendar object resource
     * @param replace <code>true</code> to remove events from the calendar object resource that are no longer present in the supplied collection, <code>false</code>, otherwise
     * @return The result
     */
    private InternalCalendarResult perform(List<Event> eventData, boolean replace) throws OXException {
        if (isNullOrEmpty(eventData)) {
            return resultTracker.getResult();
        }
        /*
         * extract & check designating properties from supplied events
         */
        List<Event> suppliedEvents = new ArrayList<Event>(eventData);
        Check.organizerMatches(null, suppliedEvents);
        String uid = Check.uidMatches(suppliedEvents);
        String filename = Check.filenameMatches(suppliedEvents);
        /*
         * lookup any existing events for this calendar object resource (with the same uid) & prepare to keep track of changes
         */
        List<Event> storedEvents = new ArrayList<Event>(sortSeriesMasterFirst(new ResolvePerformer(session, storage).lookupByUid(uid, calendarUserId, (EventField[]) null)));
        CalendarObjectResource originalResource = storedEvents.isEmpty() ? null : new DefaultCalendarObjectResource(storedEvents);
        List<EventUpdates> eventUpdates = new ArrayList<EventUpdates>();
        if (null != originalResource) {
            /*
             * ensure integrity of existing calendar object resource (same folder and optional filename), otherwise treat as conflict
             */
            for (Event storedEvent : storedEvents) {
                if (false == Objects.equals(getFolderView(storedEvent, calendarUserId), folder.getId()) ||
                    (Strings.isNotEmpty(filename) || storedEvent.containsFilename()) && false == Objects.equals(storedEvent.getFilename(), filename)) {
                    throw CalendarExceptionCodes.UID_CONFLICT.create(uid, storedEvent.getId());
                }
            }
            if (false == hasExternalOrganizer(originalResource)) {
                /*
                 * for internally organized events, check & hide incoming sequence numbers beforehand to avoid conflicts with
                 * intermediate updates of series master event
                 */
                for (ListIterator<Event> iterator = suppliedEvents.listIterator(); iterator.hasNext();) {
                    Event suppliedEvent = iterator.next();
                    Event originalEvent = findByRecurrenceId(storedEvents, suppliedEvent.getRecurrenceId());
                    if (null != originalEvent) {
                        iterator.set(maskSequence(Check.requireInSequence(originalEvent, suppliedEvent)));
                    }
                }
            }
            if (replace) {
                /*
                 * delete previously existing, but no longer indicated events
                 */
                eventUpdates.add(deleteMissing(suppliedEvents, storedEvents, originalResource));
            }
        }
        /*
         * create or update events from calendar object resource one after the other
         */
        eventUpdates.add(createOrUpdate(suppliedEvents, storedEvents));
        /*
         * handle scheduling based on aggregated results & return tracked result
         */
        handleScheduling(originalResource, new DefaultCalendarObjectResource(storedEvents), combine(eventUpdates));
        return resultTracker.getResult();
    }

    /**
     * Creates or updates events in the calendar object resource as indicated by the client. Performed changes are tracked, and the
     * supplied list of stored events is kept up to date implicitly.
     * 
     * @param indicatedEvents The events as supplied by the client
     * @param storedEvents The originally stored events
     * @return The updates performed for the underlying calendar object resource
     */
    private EventUpdates createOrUpdate(List<Event> indicatedEvents, List<Event> storedEvents) throws OXException {
        List<EventUpdate> updatedEvents = new ArrayList<EventUpdate>();
        List<Event> deletedEvents = new ArrayList<Event>();
        List<Event> createdEvents = new ArrayList<Event>();
        for (Event suppliedEvent : sortSeriesMasterFirst(indicatedEvents)) {
            Event originalEvent = findByRecurrenceId(storedEvents, suppliedEvent.getRecurrenceId());
            if (null != originalEvent) {
                /*
                 * update for existing event
                 */
                LOG.debug("Updating {} using incoming {} for calendar object resource [{}] in {}.", originalEvent, suppliedEvent, originalEvent.getUid(), folder);
                InternalEventUpdate eventUpdate = new UpdatePerformer(this).updateEvent(originalEvent, suppliedEvent).getEventUpdate();
                updatedEvents.add(eventUpdate);
                deletedEvents.addAll(eventUpdate.getDeletedExceptions());
            } else if (null != suppliedEvent.getRecurrenceId()) {
                /*
                 * new change exception event; try and create recurrence based on existing series master event
                 */
                if (false == isNullOrEmpty(storedEvents) && isSeriesMaster(storedEvents.get(0))) {
                    Event originalSeriesMaster = storedEvents.get(0);
                    LOG.debug("Adding new exception with series master {} using incoming {} for calendar object resource [{}] in {}.", 
                        originalSeriesMaster, suppliedEvent, originalSeriesMaster.getUid(), folder);
                    InternalEventUpdate eventUpdate = new UpdatePerformer(this).updateRecurrence(originalSeriesMaster, suppliedEvent.getRecurrenceId(), suppliedEvent).getEventUpdate();
                    updatedEvents.add(eventUpdate);
                    deletedEvents.addAll(eventUpdate.getDeletedExceptions());
                } else {
                    /*
                     * new detached occurrence w/o known series master event; insert new orphaned series exception event
                     */
                    LOG.debug("Adding new orphaned exception w/o series master event using incoming {} for calendar object resource [{}] in {}.", suppliedEvent, suppliedEvent.getUid(), folder);
                    createdEvents.add(new CreatePerformer(this).createEvent(suppliedEvent, storedEvents));
                }
            } else {
                /*
                 * new event (series master or non-recurring) for this calendar object resource, otherwise
                 */
                LOG.debug("Adding new event using incoming {} for calendar object resource [{}] in {}.", suppliedEvent, suppliedEvent.getUid(), folder);
                createdEvents.add(new CreatePerformer(this).createEvent(suppliedEvent, storedEvents));
            }
            /*
             * update list of stored events with the so-far changes
             */
            updateEvents(storedEvents, resultTracker.getResult().getPlainResult());
        }
        return new DefaultEventUpdates(createdEvents, deletedEvents, updatedEvents);
    }
    
    /**
     * Deletes all previously stored events that are no longer indicated by the client. Performed changes are tracked, and the supplied
     * list of stored events is kept up to date implicitly.
     * 
     * @param indicatedEvents The events as supplied by the client
     * @param storedEvents The originally stored events
     * @param originalResource The original calendar object resource
     * @return The updates performed for the underlying calendar object resource
     */
    private EventUpdates deleteMissing(List<Event> indicatedEvents, List<Event> storedEvents, CalendarObjectResource originalResource) throws OXException {
        List<EventUpdate> updatedEvents = new ArrayList<EventUpdate>();
        List<Event> deletedEvents = new ArrayList<Event>();
        for (Event originalEvent : originalResource.getEvents()) {
            if (null == findByRecurrenceId(indicatedEvents, originalEvent.getRecurrenceId())) {
                /*
                 * event from original calendar object resource no longer indicated by client; proceed with deletion if still in intermediate resource
                 */
                Event storedEvent = find(storedEvents, originalEvent.getId());
                if (null == storedEvent) {
                    continue; // already processed
                }
                Event originalSeriesMaster = isSeriesException(storedEvent) ? originalResource.getSeriesMaster() : null;
                if (deleteRemovesEvent(storedEvent)) {
                    /*
                     * deletion of not group-scheduled event / by organizer / last user attendee
                     */
                    LOG.debug("Deleting no longer indicated {} for calendar object resource [{}] in {}.", storedEvent, storedEvent.getUid(), folder);
                    deletedEvents.addAll(new DeletePerformer(this).deleteEvent(storedEvent, originalSeriesMaster));
                } else {
                    Attendee userAttendee = find(storedEvent.getAttendees(), calendarUserId);
                    if (null == userAttendee) {
                        continue; // not applicable
                    }
                    /*
                     * deletion as one of the attendees
                     */
                    LOG.debug("Deleting {} in no longer indicated {} for calendar object resource [{}] in {}.", userAttendee, storedEvent, storedEvent.getUid(), folder);
                    updatedEvents.addAll(new DeletePerformer(this).deleteEvent(storedEvent, originalSeriesMaster, userAttendee));
                }
                updateEvents(storedEvents, resultTracker.getResult().getPlainResult());
            }
        }
        return new DefaultEventUpdates(Collections.emptyList(), deletedEvents, updatedEvents);
    }
    
    /**
     * Handles any necessary scheduling after an update has been performed, i.e. tracks suitable scheduling messages and notifications.
     * 
     * @param originalResource The original calendar object resource
     * @param newResource The updated calendar object resource
     * @param eventUpdates The performed event updates to track scheduling messages for
     */
    private void handleScheduling(CalendarObjectResource originalResource, CalendarObjectResource newResource, EventUpdates eventUpdates) {
        if (null == originalResource) {
            /*
             * track scheduling operations for new calendar object resource, only
             */
            schedulingHelper.trackCreation(newResource);
            return;
        }
        if (null == eventUpdates || eventUpdates.isEmpty()) {
            /*
             * nothing to do
             */
            return;
        }
        /*
         * collect reschedules & replies from the performed updates
         */
        List<EventUpdate> reschedules = new ArrayList<EventUpdate>();
        List<EventUpdate> replies = new ArrayList<EventUpdate>();
        EventUpdate masterUpdate = null;
        EventUpdate masterReply = null;
        for (EventUpdate eventUpdate : eventUpdates.getUpdatedItems()) {
            if (isReschedule(eventUpdate)) {
                reschedules.add(eventUpdate);
                if (isSeriesMaster(eventUpdate.getOriginal())) {
                    masterUpdate = eventUpdate;
                }
            } else if (isReply(eventUpdate.getAttendeeUpdates(), calendarUser)) {
                replies.add(eventUpdate);
                if (isSeriesMaster(eventUpdate.getOriginal())) {
                    masterReply = eventUpdate;
                }
            }
        }
        /*
         * track scheduling messages & notifications for reschedules, replies and deletions
         */
        if ((0 < reschedules.size() || 0 < eventUpdates.getAddedItems().size()) && false == hasExternalOrganizer(originalResource)) {
            /*
             * determine scheduling operations based on superset of attendees in all instances of the calendar object resource
             */
            AbstractSimpleCollectionUpdate<Attendee> collectedAttendeeUpdates = getSimpleAttendeeUpdates(
                collectAttendees(originalResource, null, (CalendarUserType[]) null), collectAttendees(newResource, null, (CalendarUserType[]) null));
            if (false == collectedAttendeeUpdates.getRemovedItems().isEmpty()) {
                /*
                 * send as deletion for no longer listed attendees
                 */
                schedulingHelper.trackDeletion(originalResource, null, collectedAttendeeUpdates.getRemovedItems());
            }
            if (false == collectedAttendeeUpdates.getAddedItems().isEmpty()) {
                /*
                 * send as creation for newly appeared attendees
                 */
                schedulingHelper.trackCreation(newResource, collectedAttendeeUpdates.getAddedItems());
            }
            if (false == collectedAttendeeUpdates.getRetainedItems().isEmpty()) {
                /*
                 * send as series master update, or individual update(s) for retained attendees
                 */
                if (null != masterUpdate) {
                    schedulingHelper.trackUpdate(newResource, null, masterUpdate, collectedAttendeeUpdates.getRetainedItems());
                } else {
                    schedulingHelper.trackUpdate(newResource, newResource.getSeriesMaster(), reschedules, collectedAttendeeUpdates.getRetainedItems());
                }
            }
        } else if (0 < replies.size()) {
            /*
             * track reply message from calendar user to organizer
             */
            if (null != masterReply) {
                Attendee userAttendee = find(masterReply.getOriginal().getAttendees(), calendarUser);
                schedulingHelper.trackReply(userAttendee, newResource, replies);
            } else {
                Attendee userAttendee = find(replies.get(0).getOriginal().getAttendees(), calendarUser);
                schedulingHelper.trackReply(userAttendee, newResource, newResource.getSeriesMaster(), replies);
            }
        } else if (0 < eventUpdates.getRemovedItems().size()) {
            /*
             * track deletions for newly created delete exceptions
             */
            schedulingHelper.trackDeletion(new DefaultCalendarObjectResource(eventUpdates.getRemovedItems()), newResource.getSeriesMaster(), null);
        }
    }

    /**
     * Searches an event (occurrence) with a specific recurrence identifier from a list of possible events. Matching is performed on the
     * recurrence identifier solely, i.e. an event without recurrence identifier will match the first event that doesn't represent an
     * overridden instance from the list, i.e. a non-recurring or series master event.
     * 
     * @param events The events to search
     * @param recurrenceId The recurrence identifier to match, or <code>null</code> to lookup an event w/o recurrence identifier
     * @return The matching event, or <code>null</code> if not found
     */
    private static Event findByRecurrenceId(Collection<Event> events, RecurrenceId recurrenceId) {
        if (null != events) {
            for (Event event : events) {
                if (Objects.equals(event.getRecurrenceId(), recurrenceId)) {
                    return event;
                }
            }
        }
        return null;
    }

    /**
     * Updates the collection of "known" events by applying the changes of the passed calendar result.
     * 
     * @param events The events to update
     * @param result The calendar result to apply
     * @return <code>true</code> if the supplied list of events was modified, <code>false</code>, otherwise
     */
    private static boolean updateEvents(List<Event> events, CalendarResult result) {
        boolean modified = false;
        for (CreateResult creation : result.getCreations()) {
            modified = modified || addOrReplace(events, creation.getCreatedEvent());
        }
        for (UpdateResult update : result.getUpdates()) {
            modified = modified || addOrReplace(events, update.getUpdate());
        }
        for (DeleteResult deletion : result.getDeletions()) {
            modified = modified || remove(events, deletion.getEventID().getObjectID());
        }
        if (modified) {
            sortSeriesMasterFirst(events);
        }
        return modified;
    }

    private static boolean remove(List<Event> events, String objectId) {
        for (ListIterator<Event> iterator = events.listIterator(); iterator.hasNext();) {
            if (objectId.equals(iterator.next().getId())) {
                iterator.remove();
                return true;
            }
        }
        return false;
    }

    private static boolean addOrReplace(List<Event> events, Event eventToAdd) {
        for (ListIterator<Event> iterator = events.listIterator(); iterator.hasNext();) {
            Event event = iterator.next();
            if (eventToAdd.getId().equals(event.getId())) {
                if (eventToAdd.equals(event)) {
                    return false;
                }
                iterator.set(eventToAdd);
                return true;
            }
        }
        return events.add(eventToAdd);
    }

    private static EventUpdates combine(List<EventUpdates> eventUpdates) {
        List<Event> addedItems = new ArrayList<Event>();
        List<Event> removedItems = new ArrayList<Event>();
        List<EventUpdate> updatedItems = new ArrayList<EventUpdate>();
        if (null != eventUpdates) {
            for (EventUpdates updates : eventUpdates) {
                addedItems.addAll(updates.getAddedItems());
                removedItems.addAll(updates.getRemovedItems());
                updatedItems.addAll(updates.getUpdatedItems());
            }
        }
        return new DefaultEventUpdates(addedItems, removedItems, updatedItems);
    }

    private static Event maskSequence(Event delegate) {
        return new DelegatingEvent(delegate) {

            @Override
            public boolean containsSequence() {
                return false;
            }
        };
    }

}
