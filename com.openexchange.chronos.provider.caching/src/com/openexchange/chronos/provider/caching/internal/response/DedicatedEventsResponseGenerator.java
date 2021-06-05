/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.provider.caching.internal.response;

import static com.openexchange.chronos.common.CalendarUtils.getFlags;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;
import com.openexchange.search.CompositeSearchTerm;
import com.openexchange.search.CompositeSearchTerm.CompositeOperation;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link DedicatedEventsResponseGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class DedicatedEventsResponseGenerator extends ResponseGenerator {

    final List<EventID> eventIDs;

    public DedicatedEventsResponseGenerator(BasicCachingCalendarAccess cachedCalendarAccess, List<EventID> eventIDs) {
        super(cachedCalendarAccess);
        this.eventIDs = eventIDs;
    }

    public List<Event> generate() throws OXException {
        return new OSGiCalendarStorageOperation<List<Event>>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {
            @Override
            protected List<Event> call(CalendarStorage storage) throws OXException {
                return readEvents(storage);
            }
        }.executeQuery();
    }

    protected List<Event> readEvents(CalendarStorage calendarStorage) throws OXException {
        Set<String> objectIDs = new HashSet<String>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            objectIDs.add(eventID.getObjectID());
        }
        List<Event> events = readEventsById(calendarStorage, objectIDs.toArray(new String[objectIDs.size()]));
        List<Event> orderedEvents = new ArrayList<Event>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            Event event = CalendarUtils.find(events, eventID.getObjectID());
            if (null == event) {
                continue;
            }
            RecurrenceId recurrenceId = eventID.getRecurrenceID();
            event.setFlags(getFlags(event, cachedCalendarAccess.getAccount().getUserId()));
            if (null != recurrenceId) {
                if (isSeriesMaster(event)) {
                    if (null != calendarStorage.getEventStorage().loadException(event.getId(), recurrenceId, new EventField[] { EventField.ID })) {
                        throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), recurrenceId);
                    }
                    Iterator<Event> iterator = Services.getService(RecurrenceService.class).iterateEventOccurrences(event, new Date(recurrenceId.getValue().getTimestamp()), null);
                    if (false == iterator.hasNext()) {
                        throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), recurrenceId);
                    }
                    orderedEvents.add(iterator.next());
                } else if (recurrenceId.matches(event.getRecurrenceId())) {
                    orderedEvents.add(event);
                } else {
                    throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), recurrenceId);
                }
            } else {
                orderedEvents.add(event);
            }
        }
        return orderedEvents;
    }

    protected List<Event> readEventsById(CalendarStorage calendarStorage, String[] objectIDs) throws OXException {
        CompositeSearchTerm searchTerm = new CompositeSearchTerm(CompositeOperation.AND);
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
        EventField[] fields = getFields(this.cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class), EventField.FOLDER_ID);
        SearchOptions searchOptions = new SearchOptions(this.cachedCalendarAccess.getParameters());
        List<Event> events = calendarStorage.getEventStorage().searchEvents(searchTerm, searchOptions, fields);
        return calendarStorage.getUtilities().loadAdditionalEventData(cachedCalendarAccess.getAccount().getUserId(), events, fields);
    }
}
