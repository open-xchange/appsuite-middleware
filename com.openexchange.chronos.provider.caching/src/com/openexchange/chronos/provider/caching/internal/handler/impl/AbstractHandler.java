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

package com.openexchange.chronos.provider.caching.internal.handler.impl;

import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.tools.arrays.Arrays.contains;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.CachingHandler;
import com.openexchange.chronos.provider.caching.internal.handler.utils.CreateEventsCallable;
import com.openexchange.chronos.provider.caching.internal.handler.utils.HandlerHelper;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link AbstractHandler}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public abstract class AbstractHandler implements CachingHandler {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractHandler.class);

    /** A collection of fields that are always included when querying events from the storage */
    private static final List<EventField> DEFAULT_FIELDS = Arrays.asList(EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID, EventField.TIMESTAMP, EventField.CREATED_BY, EventField.CALENDAR_USER, EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE, EventField.DELETE_EXCEPTION_DATES, EventField.ORGANIZER, EventField.ALARMS, EventField.ATTENDEES);

    protected final CachingCalendarAccess cachedCalendarAccess;

    public AbstractHandler(CachingCalendarAccess cachedCalendarAccess) {
        this.cachedCalendarAccess = cachedCalendarAccess;
    }

    @Override
    public void handleExceptions(OXException e) throws OXException {
        LOG.error("An error occurred: {}", e.getMessage(), e);
        this.revertLastUpdated();
    }

    protected CalendarStorage getCalendarStorage() {
        return this.cachedCalendarAccess.getCalendarStorage();
    }

    /**
     * Update the lastModified configuration of the account which means the update of the cached calendar information. It is not meant the timestamp of the CalendarAccount database row.
     * 
     * This method has to be called <b>before</b> processing the feed so that the configuration will be invalidated based on the change. If this does not happen only the up-to-date account will be used.
     * 
     * @throws OXException
     */
    protected void updateLastUpdated() throws OXException {
        Map<String, Object> configuration = this.cachedCalendarAccess.getAccount().getConfiguration();
        Object lastUpdate = configuration.get(CachingCalendarAccess.LAST_UPDATE);
        Long now = L(System.currentTimeMillis());
        if (lastUpdate != null) {
            Long previousLastUpdate = L((long) lastUpdate);
            configuration.put(CachingCalendarAccess.PREVIOUS_LAST_UPDATE, previousLastUpdate);
        } else {
            configuration.put(CachingCalendarAccess.PREVIOUS_LAST_UPDATE, now);
        }
        configuration.put(CachingCalendarAccess.LAST_UPDATE, now);
        this.cachedCalendarAccess.saveConfig(configuration);
    }

    /**
     * In case of errors revert lastUpdate to previous one.
     *
     * @throws OXException
     */
    private void revertLastUpdated() throws OXException {
        Map<String, Object> configuration = this.cachedCalendarAccess.getAccount().getConfiguration();
        Long previousLastUpdate = L((long) configuration.get(CachingCalendarAccess.PREVIOUS_LAST_UPDATE));
        configuration.put(CachingCalendarAccess.LAST_UPDATE, previousLastUpdate);
        this.cachedCalendarAccess.saveConfig(configuration);
    }
    

    @Override
    public List<Event> search(List<EventID> eventIDs) throws OXException {
        Map<String, List<EventID>> sortEventIDsPerFolderId = HandlerHelper.sortEventIDsPerFolderId(eventIDs);
        EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), EventField.ALARMS, EventField.ATTENDEES);

        List<Event> events = new ArrayList<>();
        for (Entry<String, List<EventID>> eventID : sortEventIDsPerFolderId.entrySet()) {
            List<Event> readEventsInFolder = readEventsInFolder(eventID.getKey(), eventID.getValue());
            events.addAll(readEventsInFolder);
        }

        return loadAdditionalEventData(this.cachedCalendarAccess.getAccount().getUserId(), events, fields);
    }

    @Override
    public List<Event> search(String folderId) throws OXException {
        return searchEvents(folderId, false);
    }

    @Override
    public Event search(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        return searchEvent(eventId);
    }

    protected Event searchEvent(String eventId) throws OXException {
        EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), EventField.ALARMS, EventField.ATTENDEES);

        Event event = getCalendarStorage().getEventStorage().loadEvent(eventId, fields);
        return loadAdditionalEventData(this.cachedCalendarAccess.getAccount().getUserId(), Collections.singletonList(event), fields).get(0);
    }

    protected List<Event> searchEvents(String folderId, boolean all) throws OXException {
        SearchTerm<?> searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folderId);
        EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), EventField.ALARMS, EventField.ATTENDEES);
        SearchOptions searchOptions = new SearchOptions(this.cachedCalendarAccess.getParameters());
        if (all) {
            searchOptions = null;
        }
        List<Event> events = getCalendarStorage().getEventStorage().searchEvents(searchTerm, searchOptions, fields);

        return loadAdditionalEventData(this.cachedCalendarAccess.getAccount().getUserId(), events, fields);
    }

    private List<Event> readEventsInFolder(String folderId, List<EventID> eventIDs) throws OXException {
        Set<String> objectIDs = new HashSet<String>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            if (folderId.equals(eventID.getFolderID())) {
                objectIDs.add(eventID.getObjectID());
            }
        }
        List<Event> events = readEventsInFolder(folderId, objectIDs.toArray(new String[objectIDs.size()]), null, true);
        List<Event> orderedEvents = new ArrayList<Event>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            Event event = CalendarUtils.find(events, eventID.getObjectID());
            if (null == event) {
                continue;
            }
            event.setFolderId(folderId);
            if (null != eventID.getRecurrenceID()) {
                if (isSeriesMaster(event)) {
                    RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);
                    Iterator<Event> iterator = recurrenceService.iterateEventOccurrences(event, new Date(eventID.getRecurrenceID().getValue().getTimestamp()), null);
                    if (false == iterator.hasNext()) {
                        throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), eventID.getRecurrenceID());
                    }
                    orderedEvents.add(iterator.next());
                } else if (eventID.getRecurrenceID().equals(event.getRecurrenceId())) {
                    orderedEvents.add(event);
                } else {
                    throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), eventID.getRecurrenceID());
                }
            } else {
                orderedEvents.add(event);
            }
        }
        return orderedEvents;
    }

    protected List<Event> readEventsInFolder(String folderId, String[] objectIDs, Date updatedSince, boolean all) throws OXException {
        SearchTerm<?> folderSearchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folderId);
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND).addSearchTerm(folderSearchTerm);
        if (null != objectIDs) {
            if (0 == objectIDs.length) {
                return Collections.emptyList();
            } else if (1 == objectIDs.length) {
                searchTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, objectIDs[0]));
            } else {
                CompositeSearchTerm orTerm = new CompositeSearchTerm(CompositeOperation.OR);
                for (String objectID : objectIDs) {
                    orTerm.addSearchTerm(getSearchTerm(EventField.ID, SingleOperation.EQUALS, objectID));
                }
                searchTerm.addSearchTerm(orTerm);
            }
        }
        if (null != updatedSince) {
            searchTerm.addSearchTerm(getSearchTerm(EventField.LAST_MODIFIED, SingleOperation.GREATER_THAN, updatedSince));
        }
        EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), EventField.ALARMS, EventField.ATTENDEES);
        SearchOptions searchOptions = new SearchOptions(this.cachedCalendarAccess.getParameters());
        if (all) {
            searchOptions = null;
        }
        return getCalendarStorage().getEventStorage().searchEvents(searchTerm, searchOptions, fields);
    }

    protected List<Event> getAndPrepareExtEvents(String folderId) throws OXException {
        List<Event> extEventsInFolder = this.cachedCalendarAccess.getEvents(folderId);
        HandlerHelper.setFolderId(extEventsInFolder, folderId);

        return extEventsInFolder;
    }

    protected Date getFrom() {
        return this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
    }

    protected Date getUntil() {
        return this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
    }

    protected void createAsync(List<Event> externalEvents) {
        ExecutorService threadPool = Executors.newSingleThreadExecutor();
        Future<Void> submit = threadPool.submit(new CreateEventsCallable(cachedCalendarAccess, externalEvents));

        try {
            submit.get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error while persisting {} events.", externalEvents.size(), e);
        }
    }

    private EventField[] getFields(EventField[] requestedFields, EventField... requiredFields) {
        if (null == requestedFields) {
            return EventField.values();
        }
        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(DEFAULT_FIELDS);
        if (null != requiredFields && 0 < requestedFields.length) {
            fields.addAll(Arrays.asList(requiredFields));
        }
        fields.addAll(Arrays.asList(requestedFields));
        return fields.toArray(new EventField[fields.size()]);
    }

    /**
     * Loads additional event data from the storage, based on the requested fields. This currently includes
     * <ul>
     * <li>{@link EventField#ATTENDEES}</li>
     * <li>{@link EventField#ALARMS}</li> (of the calendar user; not for <i>tombstones</i>)
     * </ul>
     *
     * @param events The events to load additional data for
     * @param userID The identifier of the calendar user to load additional data for, or <code>-1</code> to not load user-sensitive data
     * @param fields The requested fields, or <code>null</code> to assume all fields are requested
     * @return The events, enriched by the additionally loaded data
     */
    private List<Event> loadAdditionalEventData(int userID, List<Event> events, EventField[] fields) throws OXException {
        if (null == events || 0 == events.size()) {
            return events;
        }

        if (null == fields || contains(fields, EventField.ATTENDEES) || contains(fields, EventField.ALARMS)) {
            String[] objectIDs = getObjectIDs(events);
            if (null == fields || contains(fields, EventField.ATTENDEES)) {
                Map<String, List<Attendee>> attendeesById = getCalendarStorage().getAttendeeStorage().loadAttendees(objectIDs);
                for (Event event : events) {
                    event.setAttendees(attendeesById.get(event.getId()));
                }
            }
            if (0 < userID && (null == fields || contains(fields, EventField.ALARMS))) {
                Map<String, List<Alarm>> alarmsById = getCalendarStorage().getAlarmStorage().loadAlarms(events, userID);
                for (Event event : events) {
                    event.setAlarms(alarmsById.get(event.getId()));
                }
            }
        }
        return events;
    }
}
