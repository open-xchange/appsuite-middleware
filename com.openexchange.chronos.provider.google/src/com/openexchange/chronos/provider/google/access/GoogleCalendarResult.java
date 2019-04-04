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

package com.openexchange.chronos.provider.google.access;

import static com.openexchange.chronos.common.CalendarUtils.getRecurrenceIds;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.Check;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.DiffAwareExternalCalendarResult;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;
import com.openexchange.chronos.provider.google.osgi.Services;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;

/**
 * {@link GoogleCalendarResult}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarResult extends ExternalCalendarResult implements DiffAwareExternalCalendarResult {

    private static final Logger LOG = LoggerFactory.getLogger(GoogleCalendarResult.class);

    private final GoogleCalendarAccess access;
    private GoogleEventsPage currentResult;
    private String folderId;

    /**
     * Initializes a new {@link GoogleCalendarResult}.
     *
     * @param googleCalendarAccess
     * @param folderId
     * @throws OXException
     * @throws JSONException
     */
    public GoogleCalendarResult(GoogleCalendarAccess googleCalendarAccess) throws OXException {
        super(true, Collections.emptyList()); // overwritten by implementation... does not contain correct update state
        access = googleCalendarAccess;
        JSONObject internalConfiguration = access.getAccount().getInternalConfiguration();
        String token = null;
        if (internalConfiguration.has(GoogleCalendarConfigField.SYNC_TOKEN)) {
            try {
                token = internalConfiguration.getString(GoogleCalendarConfigField.SYNC_TOKEN);
            } catch (JSONException e) {
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        try {
            folderId = internalConfiguration.getString(GoogleCalendarConfigField.FOLDER);
            if (folderId == null) {
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Google calendar account is invalid. Please delete and recreate it.");
            }
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        try {
            currentResult = access.getEventsInFolder(folderId, token, true);
        } catch (OXException e) {
            if(isSynTokenInvalidException(e)) {
                currentResult = access.getEventsInFolder(folderId, null, false);
            } else {
                throw e;
            }
        }
    }

    private String getFolder() throws OXException {
        if(folderId !=null) {
            return folderId;
        }
        JSONObject internalConfiguration = access.getAccount().getInternalConfiguration();
        try {
            folderId = internalConfiguration.getString(GoogleCalendarConfigField.FOLDER);
            if (folderId == null) {
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Google calendar account is invalid. Please delete and recreate it.");
            }
        } catch (JSONException e) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
        return folderId;
    }
    
    private static final EventField[] FIELDS_TO_IGNORE = new EventField[] { EventField.CREATED_BY, EventField.FOLDER_ID, EventField.ID, EventField.CALENDAR_USER, EventField.CREATED, EventField.MODIFIED_BY, EventField.EXTENDED_PROPERTIES, EventField.TIMESTAMP };
    private static final EventField[] EQUALS_IDENTIFIER = new EventField[] { EventField.UID, EventField.RECURRENCE_ID };

    @Override
    public EventUpdates calculateDiff(List<Event> existingEvents) throws OXException {

        List<Event> updates = currentResult.getEvents();
        GoogleEventsPage page = currentResult;
        try {
            while (page.getToken() != null) {
                page = access.getEventsInFolder(getFolder(), page.getToken(), false);
                updates.addAll(page.getEvents());
            }
        } catch (OXException e) {
            if (isSynTokenInvalidException(e)) {
                // Try a full synch in case of token errors
                access.getAccount().getInternalConfiguration().remove(GoogleCalendarConfigField.SYNC_TOKEN);
                Map<String, List<Event>> externalEvents = prepareExternalEvents(access.getEventsInFolder(getFolder(), null, false).getEvents());
                List<Event> updatedEvents = externalEvents.values().stream().flatMap(List::stream).collect(Collectors.toList());
                if (externalEvents.containsKey(null)) {
                    /*
                     * event source contains events without UID, use a replacing event update as fallback
                     */
                    return createUpdate(updatedEvents, Collections.emptyList(), existingEvents);
                }
                return CalendarUtils.getEventUpdates(existingEvents, updatedEvents, true, FIELDS_TO_IGNORE, EQUALS_IDENTIFIER);
            }
            throw e;
        }

        final List<Event> removed = new ArrayList<>();
        final List<Event> added = new ArrayList<>();
        final List<EventUpdate> updated = new ArrayList<>();

        final List<Event> deleteExceptions = new ArrayList<>();

        for (Event update : updates) {
            if (isDeleteException(update)) {
                /*
                 * This is probably a delete exception. Remember the event to find master events later.
                 */
                deleteExceptions.add(update);
                continue;
            }
            Event foundEvent = getEvent(existingEvents, update);
            if (update.containsStatus() && EventStatus.CANCELLED.equals(update.getStatus())) {
                if (foundEvent != null) {
                    // add to removed
                    removed.add(foundEvent);
                    continue;
                }
                // Ignored, because it is already deleted
                continue;
            }

            if (foundEvent != null) {
                // add to updated
                update.setId(foundEvent.getId());
                update.setSeriesId(foundEvent.getSeriesId());
                updated.add(new DefaultEventUpdate(foundEvent, update, true));
                continue;
            }

            // add to new
            if (update.containsRecurrenceId() && update.getRecurrenceId() != null) {
                String masterId = findMasterId(update.getUid(), existingEvents);
                if (masterId != null) {
                    update.setSeriesId(masterId);
                } else if (!containsMaster(updates, update.getUid())) {
                    // Ignore exception without master
                    continue;
                }
            }
            added.add(update);
        }

        // Handle delete exceptions
        handleDeleteExceptions(deleteExceptions, existingEvents, added, updated, removed);
        return createUpdate(added, updated, removed);
    }
    
    private static final int SYNC_TOKEN_INVALID_CODE = 410;
    
    /**
     * Checks whether the exception contains a google sync token is invalid exception
     * 
     * @param e The exception to check
     * @return <code>true</code> if the exceptions contains a google sync token invalid exceptions 
     */
    private boolean isSynTokenInvalidException(OXException e) {
        if (CalendarExceptionCodes.IO_ERROR.equals(e) && e.getCause() != null && e.getCause() instanceof GoogleJsonResponseException) {
            GoogleJsonResponseException googleException = (GoogleJsonResponseException) e.getCause();
            return googleException.getDetails().getCode() == SYNC_TOKEN_INVALID_CODE;
        }
        return false;
    }
    
    /**
     * Creates an {@link EventUpdates} object from the given parameters
     * 
     * @param added The added events
     * @param updated The updated events
     * @param removed The removed events
     * @return A new {@link EventUpdates} instance
     */
    private EventUpdates createUpdate(List<Event> added, List<EventUpdate> updated, List<Event> removed) {
        return new EventUpdates() {

            @Override
            public boolean isEmpty() {
                return removed.isEmpty() && added.isEmpty() && updated.isEmpty();
            }

            @Override
            public List<Event> getRemovedItems() {
                return removed;
            }

            @Override
            public List<Event> getAddedItems() {
                return added;
            }

            @Override
            public List<EventUpdate> getUpdatedItems() {
                return updated;
            }
        };
    }
    
    /**
     * Prepares the list of events from the external calendar source for further processing. This includes:
     * <ul>
     * <li>remove events that cannot be stored due to missing mandatory fields</li>
     * <li>map events by their UID property (events without UID are mapped to <code>null</code>)</li>
     * <li>event lists are sorted so that the series master event will be the first element</li>
     * <li>the change exception field of series master events will be set based on the actual overridden instances</li>
     * </ul>
     *
     * @param events The events to prepare
     * @return The prepared events, mapped by their unique identifier (events without UID are mapped to <code>null</code>)
     */
    private static Map<String, List<Event>> prepareExternalEvents(List<Event> events) {
        if (null == events) {
            return Collections.emptyMap();
        }
        Map<String, List<Event>> eventsByUID = new LinkedHashMap<String, List<Event>>();
        for (Event event : events) {
            /*
             * ignore events lacking mandatory fields
             */
            try {
                Check.mandatoryFields(event, EventField.START_DATE);
            } catch (OXException e) {
                LOG.debug("Removed event with uid {} from list to add because of the following corrupt data: {}", event.getUid(), e.getMessage());
                continue;
            }
            /*
             * map events by UID
             */
            com.openexchange.tools.arrays.Collections.put(eventsByUID, event.getUid(), event);
        }
        for (List<Event> eventGroup : eventsByUID.values()) {
            if (1 >= eventGroup.size()) {
                continue;
            }
            /*
             * sort series master first, then assign change exception dates
             */
            eventGroup = sortSeriesMasterFirst(eventGroup);
            if (null != eventGroup.get(0).getRecurrenceRule()) {
                eventGroup.get(0).setChangeExceptionDates(getRecurrenceIds(eventGroup.subList(1, eventGroup.size())));
            }
        }
        return eventsByUID;
    }

    /**
     * Checks if one of the given events is the master for the given uid.
     *
     * @param events The events to check
     * @param uid The uid of the event series
     * @return true if one event is the master, false otherwise
     */
    private boolean containsMaster(List<Event> events, String uid) {
        for (Event eve : events) {
            if (uid!=null && uid.equals(eve.getUid()) && !eve.containsRecurrenceId()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Finds the id of the master event
     *
     * @param uid The uid of the event series
     * @param existingEvents The existing events
     * @return The id of the master or null
     */
    private String findMasterId(String uid, List<Event> existingEvents) {
        for (Event existing : existingEvents) {
            if (existing.getUid().equals(uid) && !existing.containsRecurrenceId()) {
                return existing.getId();
            }
        }
        return null;
    }

    /**
     * Handles the possible delete exceptions
     *
     * @param deleteExceptions The delete exceptions
     * @param existingEvents A list of existing events
     * @param added A list of added events within this sync
     * @param updated A list of updated events within this sync
     * @param removed A list of removed events in this sync
     * @throws OXException
     */
    private void handleDeleteExceptions(List<Event> deleteExceptions, List<Event> existingEvents, List<Event> added, List<EventUpdate> updated, List<Event> removed) throws OXException {
        for (Event eve : deleteExceptions) {
            findAndDeleteChangeException(existingEvents, eve.getFilename(), removed);
            if (eve.getUid() != null) {
                findAndUpdateMaster(existingEvents, eve.getUid(), eve.getRecurrenceId(), added, updated);
            } else {
                addDeleteExceptionToNewOrUpdatedMaster(eve, added, updated);
            }
        }
    }

    /**
     * Searches the existing events for a event with the same google id (filename) and adds it to the removed events.
     *
     * @param existingEvents The existing events
     * @param googleId The google id
     * @param removed The list of removed events in this sync
     */
    private void findAndDeleteChangeException(List<Event> existingEvents, String googleId, List<Event> removed) {
        for (Event existing : existingEvents) {
            if (existing.getFilename().equals(googleId)) {
                // Delete exception deletes an already existing change exception
                removed.add(existing);
            }
        }
    }

    /**
     * Searches for the master event of this delete exception and adds the recurrence id to the list of delete exceptions.
     *
     * @param eve The delete exception
     * @param added The added events in this sync
     * @param updated The updated events in this sync
     */
    private void addDeleteExceptionToNewOrUpdatedMaster(Event eve, List<Event> added, List<EventUpdate> updated) {
        for (Event possibleMaster : added) {
            if (possibleMaster.getFilename().equals(eve.getSeriesId())) {
                addRecurrenceIdToDeleteException(possibleMaster, eve.getRecurrenceId(), true);
                return;
            }
        }

        for (EventUpdate update : updated) {
            Event possibleMaster = update.getUpdate();
            if (possibleMaster.getFilename().equals(eve.getSeriesId())) {
                addRecurrenceIdToDeleteException(possibleMaster, eve.getRecurrenceId(), true);
                return;
            }
        }
    }

    /**
     * Searches added, updated and existing events for the master event for a given uid and adds the recurrence id to the list of delete exceptions.
     *
     * @param existingEvents A list of existing events
     * @param uid The uid of the event series
     * @param recurrenceId The {@link RecurrenceId} to add
     * @param added A list of added events in this sync
     * @param updated A list of updated events in this sync
     * @throws OXException
     */
    private void findAndUpdateMaster(List<Event> existingEvents, String uid, RecurrenceId recurrenceId, List<Event> added, List<EventUpdate> updated) throws OXException {
        // Find the master in added, updated or deleted
        for (Event created : added) {
            if (isMaster(created, uid)) {
                addRecurrenceIdToDeleteException(created, recurrenceId, true);
                return;
            }
        }

        for (EventUpdate eventUpdate : updated) {
            if (isMaster(eventUpdate.getOriginal(), uid)) {
                addRecurrenceIdToDeleteException(eventUpdate.getUpdate(), recurrenceId, true);
                return;
            }
        }

        for (Event existing : existingEvents) {
            if (isMaster(existing, uid)) {
                Event newUpdate = new Event();
                EventMapper.getInstance().copyIfNotSet(existing, newUpdate, EventField.values());
                addRecurrenceIdToDeleteException(newUpdate, recurrenceId, false);
                updated.add(new DefaultEventUpdate(existing, newUpdate, true));
                return;
            }
        }
    }

    /**
     * Checks if the exception is a valid exception for the event master, by iterating its recurrence rule.
     *
     * If not this exception is probably a cancellation of a change exception for an master update.
     *
     * @param exception The {@link RecurrenceId} of the exception
     * @param master The master event
     * @return true if it is a valid Exception, false otherwise
     */
    private boolean isValidException(RecurrenceId exception, Event master) {
        RecurrenceService service = Services.getService(RecurrenceService.class);
        Calendar cal = Calendar.getInstance(exception.getValue().getTimeZone());
        cal.setTimeInMillis(exception.getValue().getTimestamp());
        try {
            return service.calculateRecurrencePosition(master, cal) > 0;
        } catch (OXException e) {
            LOG.debug("{}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Add a {@link RecurrenceId} to an {@link Event}
     *
     * @param master The master {@link Event}
     * @param recurrenceId The {@link RecurrenceId}
     */
    private void addRecurrenceIdToDeleteException(Event master, RecurrenceId recurrenceId, boolean needsCheck) {
        SortedSet<RecurrenceId> deleteExceptionDates = master.getDeleteExceptionDates();
        if (deleteExceptionDates == null) {
            deleteExceptionDates = new TreeSet<>();
            master.setDeleteExceptionDates(deleteExceptionDates);
        } else if (deleteExceptionDates.contains(recurrenceId)) {
            // Remove recurrence id in case its already exists (double cancel)
            deleteExceptionDates.remove(recurrenceId);
            return;
        }

        if (!needsCheck || isValidException(recurrenceId, master)) {
            deleteExceptionDates.add(recurrenceId);
        }
    }

    /**
     * Checks whether the given event is the master event for a event series with the given uid
     *
     * @param event The event to check
     * @param uid The uid of the event series
     * @return true if the event is the master, false otherwise
     */
    private boolean isMaster(Event event, String uid) {
        if (event.getUid() == null || event.getRecurrenceId() != null) {
            return false;
        }
        return event.getUid().equals(uid);
    }

    /**
     * Retrieves the existing event that corresponds to the updated event from the list of existing events.
     *
     * @param existingEvents A list of existing events
     * @param updated The updated event
     * @return The existing event or null if such an event doesn't exist yet
     */
    private Event getEvent(List<Event> existingEvents, Event updated) {
        for (Event existing : existingEvents) {
            if (existing.getUid().equals(updated.getUid()) && existing.containsRecurrenceId() == updated.containsRecurrenceId() && (!existing.containsRecurrenceId() || existing.getRecurrenceId().equals(updated.getRecurrenceId()))) {
                return existing;
            }
        }
        return null;
    }

    /**
     * Checks if an event update is a delete exception
     *
     * @param updated The event update
     * @return true if the event is a delete exception, false otherwise.
     */
    private boolean isDeleteException(Event updated) {
        return updated.getRecurrenceId() != null && updated.containsStatus() && EventStatus.CANCELLED.equals(updated.getStatus());
    }

    @Override
    public List<Event> getEvents() {
        return currentResult.getEvents();
    }

    @Override
    public boolean isUpdated() {
        return !currentResult.getEvents().isEmpty();
    }

}
