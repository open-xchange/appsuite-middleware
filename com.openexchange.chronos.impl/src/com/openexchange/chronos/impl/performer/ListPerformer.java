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

import static com.openexchange.chronos.common.CalendarUtils.find;
import static com.openexchange.chronos.common.CalendarUtils.getOccurrence;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.anonymizeIfNeeded;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.Utils;
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
        Set<String> objectIDs = new HashSet<String>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            if (folder.getId().equals(eventID.getFolderID())) {
                objectIDs.add(eventID.getObjectID());
            }
        }
        List<Event> events = readEventsInFolder(folder, objectIDs.toArray(new String[objectIDs.size()]), false, null);
        List<Event> orderedEvents = new ArrayList<Event>(eventIDs.size());
        for (EventID eventID : eventIDs) {
            Event event = find(events, eventID.getObjectID());
            if (null == event) {
                continue;
            }
            Check.eventIsVisible(folder, event);
            RecurrenceId recurrenceId = eventID.getRecurrenceID();
            event.setFolderId(folder.getId());
            event = anonymizeIfNeeded(session, event);
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
                } else if (recurrenceId.equals(event.getRecurrenceId())) {
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
