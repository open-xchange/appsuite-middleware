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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventStatus;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.mapping.EventMapper;
import com.openexchange.chronos.common.mapping.EventUpdateImpl;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.DiffAwareExternalCalendarResult;
import com.openexchange.chronos.provider.caching.ExternalCalendarResult;
import com.openexchange.chronos.provider.google.GoogleCalendarConfigField;
import com.openexchange.chronos.service.EventUpdate;
import com.openexchange.chronos.service.EventUpdates;
import com.openexchange.exception.OXException;

/**
 * {@link GoogleCalendarResult}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class GoogleCalendarResult extends ExternalCalendarResult implements DiffAwareExternalCalendarResult{


    private final GoogleCalendarAccess access;
    private final GoogleEventsPage currentResult;
    private final String folderId;

    /**
     * Initializes a new {@link GoogleCalendarResult}.
     * @param googleCalendarAccess
     * @param folderId
     * @throws OXException
     * @throws JSONException
     */
    public GoogleCalendarResult(GoogleCalendarAccess googleCalendarAccess, String folderId) throws OXException {
        super();
        access = googleCalendarAccess;
        this.folderId = folderId;
        JSONObject internalConfiguration = access.getAccount().getInternalConfiguration();
        String token = null;
        if(internalConfiguration.has(GoogleCalendarConfigField.SYNC_TOKEN)){
            try {
                token = internalConfiguration.getString(GoogleCalendarConfigField.SYNC_TOKEN);
            } catch (JSONException e) {
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        currentResult = access.getEventsInFolder(folderId, token, true);
    }

    @Override
    public EventUpdates calculateDiff(List<Event> existingEvents) throws OXException {

        List<Event> updates = currentResult.getEvents();

        final List<Event> removed = new ArrayList<>();
        final List<Event> added = new ArrayList<>();
        final List<EventUpdate> updated = new ArrayList<>();

        final List<Event> deleteExceptions = new ArrayList<>();

        for(Event update: updates){
            if (update.getUid() == null) {
                /*
                 * This is probably a delete exception. Remember the event to batch load the master events later.
                 */
                deleteExceptions.add(update);
                continue;

            }
            Event foundEvent = getEvent(existingEvents, update);
            if(update.getStatus().equals(EventStatus.CANCELLED)){
                if(foundEvent != null){
                    // add to removed
                    removed.add(foundEvent);
                    continue;
                }
                // Ignored, because it is already deleted
                continue;
            }

            if(foundEvent != null){
                // add to updated
                update.setId(foundEvent.getId());
                updated.add(new EventUpdateImpl(foundEvent, update, true));
                continue;
            }

            // add to new
            if (update.containsRecurrenceId() && update.getRecurrenceId() != null) {
                String masterId = findMasterId(update.getUid(), existingEvents);
                if (masterId != null) {
                    update.setSeriesId(masterId);
                } else {
                    // Ignore exception without master
                    continue;
                }
            }
            added.add(update);
        }

        // Handle delete exceptions
        Set<String> masterIds = getMasterIds(deleteExceptions);
        Map<String, Event> masterEvents = new HashMap<>(masterIds.size());
        for (String masterId : masterIds) {
            Event event = access.getEvent(folderId, masterId);
            masterEvents.put(event.getFilename(), event);
        }

        for (Event delEvent : deleteExceptions) {
            String uid = masterEvents.get(delEvent.getSeriesId()).getUid();
            findAndUpdateMaster(existingEvents, uid, delEvent.getRecurrenceId(), added, updated);
        }


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
     * @param uid
     * @return
     */
    private String findMasterId(String uid, List<Event> existingEvents) {
        for (Event existing : existingEvents) {
            if (existing.getUid().equals(uid) && !existing.containsRecurrenceId()) {
                return existing.getId();
            }
        }
        return null;
    }

    private Set<String> getMasterIds(List<Event> events) {
        Set<String> masterIds = new HashSet<>();
        Iterator<Event> iter = events.iterator();
        while (iter.hasNext()) {
            Event eve = iter.next();
            if (!isDeleteException(eve)) {
                // Ignore events, which are no delete exceptions
                iter.remove();
                continue;
            }
            masterIds.add(eve.getSeriesId());
        }

        return masterIds;
    }

    /**
     * @param existingEvents
     * @param recurrenceId
     * @throws OXException
     */
    private void findAndUpdateMaster(List<Event> existingEvents, String uid, RecurrenceId recurrenceId, List<Event> added, List<EventUpdate> updated) throws OXException {
        // Find the master in added, updated or deleted
        for(Event created: added){
            if (isMaster(created, uid)) {
                SortedSet<RecurrenceId> deleteExceptionDates = created.getDeleteExceptionDates();
                if(deleteExceptionDates == null){
                    deleteExceptionDates = new TreeSet<>();
                    created.setDeleteExceptionDates(deleteExceptionDates);
                }
                deleteExceptionDates.add(recurrenceId);
                return;
            }
        }

        for(EventUpdate eventUpdate: updated){
            if (isMaster(eventUpdate.getOriginal(), uid)) {
                SortedSet<RecurrenceId> deleteExceptionDates = eventUpdate.getUpdate().getDeleteExceptionDates();
                if(deleteExceptionDates == null){
                    deleteExceptionDates = new TreeSet<>();
                    eventUpdate.getUpdate().setDeleteExceptionDates(deleteExceptionDates);
                }
                deleteExceptionDates.add(recurrenceId);
                return;
            }
        }

        for(Event existing: existingEvents){
            if (isMaster(existing, uid)) {
                Event newUpdate = new Event();
                EventMapper.getInstance().copyIfNotSet(existing, newUpdate, EventField.values());
                SortedSet<RecurrenceId> deleteExceptionDates = newUpdate.getDeleteExceptionDates();
                if(deleteExceptionDates == null){
                    deleteExceptionDates = new TreeSet<>();
                    newUpdate.setDeleteExceptionDates(deleteExceptionDates);
                }
                deleteExceptionDates.add(recurrenceId);
                updated.add(new EventUpdateImpl(existing, newUpdate, true));
                return;
            }
        }
    }

    private boolean isMaster(Event event, String uid) {
        if (event.getUid() == null) {
            return false;
        }
        return event.getUid().equals(uid);
    }

    private Event getEvent(List<Event> existingEvents, Event updated) {
        for (Event existing : existingEvents) {
            if (existing.getUid().equals(updated.getUid()) && existing.containsRecurrenceId() == updated.containsRecurrenceId() && (!existing.containsRecurrenceId() || existing.getRecurrenceId().equals(updated.getRecurrenceId()))) {
                return existing;
            }
        }
        return null;
    }

    private boolean isDeleteException(Event updated){
        return updated.getUid() == null && updated.getRecurrenceId() != null && updated.getStatus().equals(EventStatus.CANCELLED);
    }

    @Override
    public void addEvents(List<Event> events) {
        // nothing to do
    }

    @Override
    public List<Event> getEvents() {
        return currentResult.getEvents();
    }

    @Override
    public boolean isUpToDate() {
        return currentResult.getEvents().isEmpty();
    }

}
