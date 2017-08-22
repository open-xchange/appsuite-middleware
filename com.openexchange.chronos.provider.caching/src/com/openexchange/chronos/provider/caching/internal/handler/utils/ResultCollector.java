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

package com.openexchange.chronos.provider.caching.internal.handler.utils;

import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
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
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.CalendarStorageFactory;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link ResultCollector}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class ResultCollector {

    private static final List<EventField> DEFAULT_FIELDS = Arrays.asList(EventField.ID, EventField.SERIES_ID, EventField.FOLDER_ID, EventField.RECURRENCE_ID, EventField.TIMESTAMP, EventField.CREATED_BY, EventField.CALENDAR_USER, EventField.CLASSIFICATION, EventField.START_DATE, EventField.END_DATE, EventField.RECURRENCE_RULE, EventField.DELETE_EXCEPTION_DATES, EventField.ORGANIZER, EventField.ALARMS, EventField.ATTENDEES);

    private static final List<EventField> IGNORED_FIELDS = Arrays.asList(EventField.ATTACHMENTS);

    protected CachingCalendarAccess cachedCalendarAccess;

    public ResultCollector(CachingCalendarAccess cachingCalendarAccess) {
        this.cachedCalendarAccess = cachingCalendarAccess;
    }

    protected CalendarStorage initStorage(DBProvider dbProvider) throws OXException {
        return Services.getService(CalendarStorageFactory.class).create(this.cachedCalendarAccess.getSession().getContext(), this.cachedCalendarAccess.getAccount().getAccountId(), null, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }

    public List<Event> get(final List<EventID> eventIDs) throws OXException {
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContext().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {

            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                final Map<String, List<EventID>> sortEventIDsPerFolderId = HandlerHelper.sortEventIDsPerFolderId(eventIDs);
                List<Event> events = new ArrayList<>();
                for (Entry<String, List<EventID>> eventID : sortEventIDsPerFolderId.entrySet()) {
                    List<Event> readEventsInFolder = getEvents(storage, eventID.getKey(), eventID.getValue());
                    events.addAll(readEventsInFolder);
                }
                EventField[] fields = getFields(cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));
                return storage.getUtilities().loadAdditionalEventData(cachedCalendarAccess.getAccount().getUserId(), events, fields);
            }

        }.executeQuery();
    }

    public Event get(final String folderId, final String eventId, final RecurrenceId recurrenceId) throws OXException {
        return new OSGiCalendarStorageOperation<Event>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContext().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {

            @Override
            protected Event call(CalendarStorage storage) throws OXException {
                EventField[] fields = getFields(cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));

                Event event = storage.getEventStorage().loadEvent(eventId, fields);
                if (event == null) {
                    throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(eventId);
                }
                return storage.getUtilities().loadAdditionalEventData(cachedCalendarAccess.getAccount().getUserId(), event, fields);
            }

        }.executeQuery();
    }

    public List<Event> get(final String folderId) throws OXException {
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContext().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {

            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                SearchTerm<?> searchTerm = getSearchTerm(EventField.FOLDER_ID, SingleOperation.EQUALS, folderId);
                EventField[] fields = getFields(cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class));
                SearchOptions searchOptions = new SearchOptions(cachedCalendarAccess.getParameters());
                List<Event> events = storage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);
                return storage.getUtilities().loadAdditionalEventData(cachedCalendarAccess.getAccount().getUserId(), events, fields);
            }

        }.executeQuery();
    }

    protected List<Event> getEvents(CalendarStorage calendarStorage, String folderId, List<EventID> eventIDs) throws OXException {
        Set<String> objectIDs = new HashSet<String>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            if (folderId.equals(eventID.getFolderID())) {
                objectIDs.add(eventID.getObjectID());
            }
        }
        List<Event> events = readEventsInFolder(calendarStorage, folderId, objectIDs.toArray(new String[objectIDs.size()]), null);
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

    protected List<Event> readEventsInFolder(CalendarStorage calendarStorage, String folderId, String[] objectIDs, Date updatedSince) throws OXException {
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
        return calendarStorage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);
    }

    EventField[] getFields(EventField[] requestedFields) {
        EventField[] requested = null == requestedFields ? EventField.values() : requestedFields;

        Set<EventField> fields = new HashSet<EventField>();
        fields.addAll(Arrays.asList(requested));
        fields.addAll(DEFAULT_FIELDS);
        fields.removeAll(IGNORED_FIELDS);
        return fields.toArray(new EventField[fields.size()]);
    }
}
