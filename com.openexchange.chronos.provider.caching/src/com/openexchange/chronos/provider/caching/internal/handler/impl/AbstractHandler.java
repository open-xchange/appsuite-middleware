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

import static com.openexchange.chronos.common.CalendarUtils.getEventsByUID;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
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
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.CachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.provider.caching.internal.handler.CachingHandler;
import com.openexchange.chronos.provider.caching.internal.handler.utils.HandlerHelper;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
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

    private static final List<EventField> IGNORED_FIELDS = Arrays.asList(EventField.ATTACHMENTS);

    protected final CachingCalendarAccess cachedCalendarAccess;

    public AbstractHandler(CachingCalendarAccess cachedCalendarAccess) {
        this.cachedCalendarAccess = cachedCalendarAccess;
    }

    protected CalendarStorage initStorage(DBProvider dbProvider) throws OXException {
        return Services.getService(CalendarStorageFactory.class).create(this.cachedCalendarAccess.getSession().getContext(), this.cachedCalendarAccess.getAccount().getAccountId(), null, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }

    @Override
    public void handleExceptions(OXException e) throws OXException {
        LOG.error("An error occurred: {}", e.getMessage(), e);
        this.revertLastUpdated();
    }

    private void revertLastUpdated() throws OXException {
        Map<String, Object> configuration = this.cachedCalendarAccess.getAccount().getConfiguration();
        Long previousLastUpdate = L((long) configuration.get(CachingCalendarAccess.PREVIOUS_LAST_UPDATE));
        configuration.put(CachingCalendarAccess.LAST_UPDATE, previousLastUpdate);
        this.cachedCalendarAccess.saveConfig(configuration);
    }

    @Override
    public void updateLastUpdated() throws OXException {
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

    @Override
    public List<Event> search(List<EventID> eventIDs) throws OXException {
        Map<String, List<EventID>> sortEventIDsPerFolderId = HandlerHelper.sortEventIDsPerFolderId(eventIDs);

        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection readConnection = null;

        Context context = this.cachedCalendarAccess.getSession().getContext();
        try {
            readConnection = dbService.getReadOnly(context);
            CalendarStorage calendarStorage = initStorage(new SimpleDBProvider(readConnection, null));
            List<Event> events = new ArrayList<>();
            for (Entry<String, List<EventID>> eventID : sortEventIDsPerFolderId.entrySet()) {
                List<Event> readEventsInFolder = readEventsInFolder(calendarStorage, eventID.getKey(), eventID.getValue());
                events.addAll(readEventsInFolder);
            }
            EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));
            return calendarStorage.getUtilities().loadAdditionalEventData(this.cachedCalendarAccess.getAccount().getUserId(), events, fields);
        } finally {
            if (null != readConnection) {
                dbService.backReadOnly(context, readConnection);
            }
        }
    }

    @Override
    public Event search(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection readConnection = null;

        Context context = this.cachedCalendarAccess.getSession().getContext();
        try {
            readConnection = dbService.getReadOnly(context);
            return searchInternal(initStorage(new SimpleDBProvider(readConnection, null)), eventId);
        } finally {
            if (null != readConnection) {
                dbService.backReadOnly(context, readConnection);
            }
        }
    }

    protected Event searchInternal(CalendarStorage calendarStorage, String eventId) throws OXException {
        EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));

        Event event = calendarStorage.getEventStorage().loadEvent(eventId, fields);
        if (event == null) {
            return null;
        }
        return calendarStorage.getUtilities().loadAdditionalEventData(this.cachedCalendarAccess.getAccount().getUserId(), event, fields);
    }

    @Override
    public List<Event> search(String folderId) throws OXException {
        return searchInternal(folderId, false);
    }

    public List<Event> searchInternal(String folderId, boolean all) throws OXException {
        DatabaseService dbService = Services.getService(DatabaseService.class);
        Connection readConnection = null;

        Context context = this.cachedCalendarAccess.getSession().getContext();
        try {
            readConnection = dbService.getReadOnly(context);
            return searchInternal(initStorage(new SimpleDBProvider(readConnection, null)), folderId, all);
        } finally {
            if (null != readConnection) {
                dbService.backReadOnly(context, readConnection);
            }
        }
    }

    protected List<Event> searchInternal(CalendarStorage calendarStorage, String folderId, boolean all) throws OXException {
        SearchTerm<?> searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folderId);
        EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));
        SearchOptions searchOptions = new SearchOptions(this.cachedCalendarAccess.getParameters());
        if (all) {
            searchOptions = null;
        }
        List<Event> events = calendarStorage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);

        return calendarStorage.getUtilities().loadAdditionalEventData(this.cachedCalendarAccess.getAccount().getUserId(), events, fields);
    }

    private List<Event> readEventsInFolder(CalendarStorage calendarStorage, String folderId, List<EventID> eventIDs) throws OXException {
        Set<String> objectIDs = new HashSet<String>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            if (folderId.equals(eventID.getFolderID())) {
                objectIDs.add(eventID.getObjectID());
            }
        }
        List<Event> events = readEventsInFolder(calendarStorage, folderId, objectIDs.toArray(new String[objectIDs.size()]), null, true);
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

    protected List<Event> readEventsInFolder(CalendarStorage calendarStorage, String folderId, String[] objectIDs, Date updatedSince, boolean all) throws OXException {
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
        EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));
        SearchOptions searchOptions = new SearchOptions(this.cachedCalendarAccess.getParameters());
        if (all) {
            searchOptions = null;
        }
        return calendarStorage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);
    }

    protected List<Event> getAndPrepareExtEvents(String folderId) throws OXException {
        List<Event> extEventsInFolder = this.cachedCalendarAccess.getEvents(folderId);
        HandlerHelper.setFolderId(extEventsInFolder, folderId);

        return extEventsInFolder;
    }

    protected void create(CalendarStorage calendarStorage, List<Event> externalEvents) throws OXException {
        if (!externalEvents.isEmpty()) {
            Map<String, List<Event>> extEventsByUID = getEventsByUID(externalEvents, false);
            for (Entry<String, List<Event>> event : extEventsByUID.entrySet()) {
                create(calendarStorage, event);
            }
        }
    }

    protected void create(CalendarStorage calendarStorage, Entry<String, List<Event>> entry) throws OXException {
        Date now = new Date();

        List<Event> events = sortSeriesMasterFirst(entry.getValue());
        insertEvents(calendarStorage, now, events.toArray(new Event[events.size()]));
    }

    protected void insertEvents(CalendarStorage calendarStorage, Date now, Event... lEvents) throws OXException {
        if (null == lEvents || 0 == lEvents.length) {
            return;
        }
        List<Event> events = Arrays.asList(lEvents);

        String id = calendarStorage.getEventStorage().nextId();
        Event importedEvent = applyDefaults(events.get(0), now);
        importedEvent.setId(id);
        importedEvent.setCalendarUser(this.cachedCalendarAccess.getAccount().getUserId());
        if (Strings.isNotEmpty(importedEvent.getRecurrenceRule())) {
            importedEvent.setSeriesId(id);
        }
        calendarStorage.getEventStorage().insertEvent(importedEvent);
        if (null != importedEvent.getAttendees() && !importedEvent.getAttendees().isEmpty()) {
            calendarStorage.getAttendeeStorage().insertAttendees(id, importedEvent.getAttendees());
        }

        if (null != importedEvent.getAlarms() && !importedEvent.getAlarms().isEmpty()) {
            calendarStorage.getAlarmStorage().insertAlarms(importedEvent, this.cachedCalendarAccess.getSession().getUserId(), importedEvent.getAlarms());
        }

        if (1 < events.size()) {
            for (int i = 1; i < events.size(); i++) {
                Event importedChangeException = applyDefaults(events.get(i), now);
                importedChangeException.setSeriesId(id);
                importedChangeException.setId(calendarStorage.getEventStorage().nextId());
                calendarStorage.getEventStorage().insertEvent(importedChangeException);
                if (null != importedChangeException.getAttendees() && !importedChangeException.getAttendees().isEmpty()) {
                    calendarStorage.getAttendeeStorage().insertAttendees(importedChangeException.getId(), importedChangeException.getAttendees());
                }
                if (null != importedChangeException.getAlarms() && !importedChangeException.getAlarms().isEmpty()) {
                    calendarStorage.getAlarmStorage().insertAlarms(importedChangeException, this.cachedCalendarAccess.getSession().getUserId(), importedChangeException.getAlarms());
                }
            }
        }
    }

    private Event applyDefaults(Event importedEvent, Date now) {
        importedEvent.setCreatedBy(this.cachedCalendarAccess.getSession().getUserId());
        importedEvent.setModifiedBy(this.cachedCalendarAccess.getSession().getUserId());
        importedEvent.setTimestamp(now.getTime());
        return importedEvent;
    }

    private EventField[] getFields(EventField[] requestedFields) {
        EventField[] requested = null == requestedFields ? EventField.values() : requestedFields;

        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(Arrays.asList(requested));
        fields.addAll(DEFAULT_FIELDS);
        fields.removeAll(IGNORED_FIELDS);
        return fields.toArray(new EventField[fields.size()]);
    }
}
