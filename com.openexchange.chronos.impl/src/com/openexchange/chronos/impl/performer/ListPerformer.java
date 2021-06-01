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

package com.openexchange.chronos.impl.performer;

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getOccurrence;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link ListPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class ListPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link ListPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public ListPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param eventIDs The identifiers of the events to get
     * @return The loaded events
     */
    public List<Event> perform(List<EventID> eventIDs) throws OXException {
        Map<CalendarFolder, List<EventID>> idsPerFolder = getIdsPerFolder(eventIDs);
        Map<String, List<Event>> eventsPerFolderId = new HashMap<String, List<Event>>(idsPerFolder.size());
        for (Map.Entry<CalendarFolder, List<EventID>> entry : idsPerFolder.entrySet()) {
            eventsPerFolderId.put(entry.getKey().getId(), readEventsInFolder(entry.getKey(), entry.getValue()));
        }
        List<Event> orderedEvents = new ArrayList<Event>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            List<Event> eventsInFolder = eventsPerFolderId.get(eventID.getFolderID());
            Event event = find(eventsInFolder, eventID.getObjectID(), eventID.getRecurrenceID());
            if (null == event) {
                // log not found event, but include null in resulting list to preserve order
                org.slf4j.LoggerFactory.getLogger(ListPerformer.class).debug("Requested event \"{}\" not found, skipping.", eventID);
            }
            orderedEvents.add(event);
        }
        return orderedEvents;
    }

    private List<Event> readEventsInFolder(CalendarFolder folder, List<EventID> eventIDs) throws OXException {
        List<String> objectIds = new ArrayList<String>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            if (folder.getId().equals(eventID.getFolderID())) {
                objectIds.add(eventID.getObjectID());
            }
        }
        if (0 == objectIds.size()) {
            return Collections.emptyList();
        }
        /*
         * get events with reduced fields & load additional event data for post processor dynamically
         */
        EventField[] requestedFields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        EventField[] fields = getFieldsForStorage(requestedFields);
        List<Event> events = storage.getEventStorage().loadEvents(objectIds, fields);
        events = storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), events, fields);
        EventPostProcessor postProcessor = postProcessor(objectIds.toArray(new String[objectIds.size()]), folder.getCalendarUserId(), requestedFields, fields);
        /*
         * generate resulting events
         */
        List<Event> orderedEvents = new ArrayList<Event>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            /*
             * lookup loaded event data, post-process event & check permissions
             */
            Event event = find(events, eventID.getObjectID());
            if (null != event) {
                event = postProcessor.process(event, folder).getFirstEvent();
                postProcessor.reset();
            }
            if (null == event) {
                continue; // skip
            }
            Check.eventIsVisible(folder, event);
            Check.eventIsInFolder(event, folder);
            /*
             * retrieve targeted event occurrence if specified
             */
            RecurrenceId recurrenceId = eventID.getRecurrenceID();
            if (null != recurrenceId) {
                if (isSeriesMaster(event)) {
                    if (null != storage.getEventStorage().loadException(event.getId(), recurrenceId, new EventField[] { EventField.ID })) {
                        throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), recurrenceId);
                    }
                    Event occurrence = getOccurrence(session.getRecurrenceService(), event, recurrenceId);
                    if (null == occurrence) {
                        throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventID.getObjectID(), recurrenceId);
                    }
                    orderedEvents.add(occurrence);
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

    private Map<CalendarFolder, List<EventID>> getIdsPerFolder(List<EventID> eventIDs) throws OXException {
        Map<String, List<EventID>> idsPerFolderId = new HashMap<String, List<EventID>>();
        for (EventID eventID : eventIDs) {
            com.openexchange.tools.arrays.Collections.put(idsPerFolderId, eventID.getFolderID(), eventID);
        }
        Map<CalendarFolder, List<EventID>> idsPerFolder = new HashMap<CalendarFolder, List<EventID>>(idsPerFolderId.size());
        for (Map.Entry<String, List<EventID>> entry : idsPerFolderId.entrySet()) {
            idsPerFolder.put(Utils.getFolder(session, entry.getKey(), false), entry.getValue());
        }
        return idsPerFolder;
    }

}
