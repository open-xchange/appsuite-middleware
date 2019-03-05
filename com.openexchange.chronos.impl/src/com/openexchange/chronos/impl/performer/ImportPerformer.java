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

import static com.openexchange.chronos.common.CalendarUtils.getEventID;
import static com.openexchange.chronos.common.CalendarUtils.getEventsByUID;
import static com.openexchange.chronos.common.CalendarUtils.sortSeriesMasterFirst;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map.Entry;
import java.util.UUID;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.mapping.DefaultEventUpdate;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.ical.ImportedComponent;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.InternalImportResult;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.CreateResult;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.UIDConflictStrategy;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link ImportPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public class ImportPerformer extends AbstractUpdatePerformer {

    /**
     * Initializes a new {@link ImportPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     */
    public ImportPerformer(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Performs the import of one or more events.
     *
     * @param events The events to import
     * @return The import result
     * @throws OXException In case of error
     */
    public List<InternalImportResult> perform(List<Event> events) throws OXException {
        /*
         * set UIDConflict strategy. Use THROW as default
         */
        UIDConflictStrategy strategy = session.get(CalendarParameters.UID_CONFLICT_STRATEGY, UIDConflictStrategy.class);
        if (null == strategy) {
            strategy = UIDConflictStrategy.THROW;
        }
        /*
         * import events (and possible associated overridden instances) grouped by UID event groups
         */
        List<InternalImportResult> results = new ArrayList<InternalImportResult>();
        for (Entry<String, List<Event>> entry : getEventsByUID(events, true).entrySet()) {
            /*
             * (re-) assign new UID to imported event if required
             */
            List<Event> eventGroup = sortSeriesMasterFirst(entry.getValue());
            if (UIDConflictStrategy.REASSIGN.equals(strategy)) {
                results.addAll(handleUIDConflict(strategy, eventGroup));
                continue;
            }

            /*
             * create first event (master or non-recurring)
             *
             * It is NOT possible to add event with another internal organizer
             */
            InternalImportResult result;
            result = createEvent(eventGroup.get(0));

            /*
             * Check if UID Conflict needs to be handled
             */
            OXException e = result.getImportResult().getError();
            if (null != e && CalendarExceptionCodes.UID_CONFLICT.equals(e)) {
                if (false == UIDConflictStrategy.THROW.equals(strategy) && false == UIDConflictStrategy.REASSIGN.equals(strategy)) {
                    results.addAll(handleUIDConflict(strategy, eventGroup));
                    continue;
                }
                throw e;
            }

            results.add(result);
            /*
             * create further events as change exceptions
             */
            addEventExceptions(eventGroup, results, result);
        }
        return results;

    }

    /**
     * Resolves UID conflicts based on the strategy
     *
     * @param strategy The strategy
     * @param eventGroup The events with UID conflicts sorted by master first
     * @return The imported results
     * @throws OXException Various
     */
    private List<InternalImportResult> handleUIDConflict(UIDConflictStrategy strategy, List<Event> eventGroup) throws OXException {
        List<InternalImportResult> results = new LinkedList<InternalImportResult>();
        Event masterEvent = eventGroup.get(0);
        switch (strategy) {
            case REASSIGN: {
                String uid = UUID.randomUUID().toString();
                eventGroup.forEach(e -> e.setUid(uid));
                InternalImportResult result = createEvent(masterEvent);
                results.add(result);
                if (isSuccess(result) && 1 < eventGroup.size()) {
                    // Event was created, add exceptions
                    addEventExceptions(eventGroup, results, result);
                } // else; Failed to create event, return failure
                return results;
            }

            case UPDATE: {
                String eventId = session.getCalendarService().getUtilities().resolveByUID(session, masterEvent.getUid());
                Event loadEvent = storage.getEventStorage().loadEvent(eventId, null);
                loadEvent = storage.getUtilities().loadAdditionalEventData(session.getUserId(), loadEvent, null);
                if (new DefaultEventUpdate(loadEvent, masterEvent, false, EventField.LAST_MODIFIED, EventField.TIMESTAMP).getUpdatedFields().isEmpty()) {
                    // Nothing to update
                    return null;
                }
                // In case an event is NOT in the default calendar, update call will fail (if organizer is internal ...)
                UpdatePerformer u = new UpdatePerformer(storage, session, folder);

                long clientTimestamp = System.currentTimeMillis();
                InternalCalendarResult calendarResult = u.perform(eventId, loadEvent.getRecurrenceId(), masterEvent, clientTimestamp);
                InternalImportResult result = new InternalImportResult(calendarResult, getEventID(masterEvent), extractIndex(masterEvent), extractWarnings(masterEvent));
                results.add(result);

                if (isSuccess(result) && 1 < eventGroup.size()) {
                    ListIterator<Event> iterator = eventGroup.listIterator(1);
                    do {
                        Event event = iterator.next();
                        // Update event. UpdatePerformer will create event exceptions on its own, if necessary
                        calendarResult = u.perform(loadEvent.getId(), event.getRecurrenceId(), event, clientTimestamp);
                        InternalImportResult internalImportResult = new InternalImportResult(calendarResult, getEventID(event), extractIndex(event), extractWarnings(event));
                        results.add(internalImportResult);
                    } while (iterator.hasNext());
                }
                return results;
            }

            case UPDATE_OR_REASSIGN: {
                List<InternalImportResult> list;
                try {
                    list = handleUIDConflict(UIDConflictStrategy.UPDATE, eventGroup);
                } catch (OXException e) {
                    LOG.warn("Could not update all events. Try to reassign event UID.", e);
                    list = Collections.emptyList();
                }
                if (list.isEmpty() || !isSuccess(list.get(0))) {
                    // Updated failed, try reassign
                    return handleUIDConflict(UIDConflictStrategy.REASSIGN, eventGroup);
                }
                return list;
            }
            default:
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Unkown UIDConflictStrategy.");
        }
    }

    private boolean isSuccess(InternalImportResult result) {
        return null == result.getImportResult().getError();
    }

    /**
     * Add {@link InternalImportResult} of series exceptions to the results
     *
     * @param events The events with the same UID sorted by master first
     * @param results The results to return by the import action
     * @param result The {@link InternalImportResult} of the master event
     */
    private void addEventExceptions(List<Event> events, List<InternalImportResult> results, InternalImportResult result) {
        if (1 < events.size()) {
            EventID masterEventID = result.getImportResult().getId();
            long clientTimestamp = result.getImportResult().getTimestamp();
            for (int i = 1; i < events.size(); i++) {
                result = createEventException(masterEventID, events.get(i), clientTimestamp);
                results.add(result);
                clientTimestamp = result.getImportResult().getTimestamp();
            }
        }
    }

    private InternalImportResult createEvent(Event importedEvent) {
        final int MAX_RETRIES = 5;
        List<OXException> warnings = new ArrayList<OXException>();
        warnings.addAll(extractWarnings(importedEvent));
        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                InternalCalendarResult calendarResult = new CreatePerformer(storage, session, folder).perform(importedEvent);
                Event createdEvent = getFirstCreatedEvent(calendarResult);
                if (null == createdEvent) {
                    OXException error = CalendarExceptionCodes.UNEXPECTED_ERROR.create("No event created for \"" + importedEvent + "\"");
                    return new InternalImportResult(calendarResult, extractIndex(importedEvent), warnings, error);
                }
                return new InternalImportResult(calendarResult, getEventID(createdEvent), extractIndex(importedEvent), warnings);
            } catch (OXException e) {
                if (retryCount < MAX_RETRIES && handle(e, importedEvent)) {
                    // try again
                    LOG.debug("{} - trying again ({}/{})", e.getMessage(), Integer.valueOf(retryCount), Integer.valueOf(MAX_RETRIES), e);
                    warnings.add(e);
                    continue;
                }
                // "re-throw"
                return new InternalImportResult(new InternalCalendarResult(session, calendarUserId, folder), extractIndex(importedEvent), warnings, e);
            }
        }
        throw new AssertionError(); // should not get here
    }

    private InternalImportResult createEventException(EventID masterEventID, Event importedException, long clientTimestamp) {
        if (null == masterEventID) {
            OXException error = CalendarExceptionCodes.UNEXPECTED_ERROR.create("Cannot create exception for  \"" + importedException + "\" due to missing master event.");
            return new InternalImportResult(new InternalCalendarResult(session, calendarUserId, folder), extractIndex(importedException), Collections.emptyList(), error);
        }
        final int MAX_RETRIES = 5;
        List<OXException> warnings = new ArrayList<OXException>();
        warnings.addAll(extractWarnings(importedException));
        for (int retryCount = 1; retryCount <= MAX_RETRIES; retryCount++) {
            try {
                InternalCalendarResult calendarResult = new UpdatePerformer(storage, session, folder).perform(masterEventID.getObjectID(), null, importedException, clientTimestamp);
                Event createdEvent = getFirstCreatedEvent(calendarResult);
                if (null == createdEvent) {
                    OXException error = CalendarExceptionCodes.UNEXPECTED_ERROR.create("No event created for \"" + importedException + "\"");
                    return new InternalImportResult(calendarResult, extractIndex(importedException), warnings, error);
                }
                return new InternalImportResult(calendarResult, getEventID(createdEvent), extractIndex(importedException), warnings);
            } catch (OXException e) {
                if (retryCount < MAX_RETRIES && handle(e, importedException)) {
                    // try again
                    LOG.debug("{} - trying again ({}/{})", e.getMessage(), Integer.valueOf(retryCount), Integer.valueOf(MAX_RETRIES), e);
                    warnings.add(e);
                    continue;
                }
                // "re-throw"
                return new InternalImportResult(new InternalCalendarResult(session, calendarUserId, folder), extractIndex(importedException), warnings, e);
            }
        }
        throw new AssertionError(); // should not get here
    }

    private static List<OXException> extractWarnings(Event importedEvent) {
        if (ImportedComponent.class.isInstance(importedEvent)) {
            List<OXException> warnings = ((ImportedComponent) importedEvent).getWarnings();
            if (null != warnings) {
                return warnings;
            }
        }
        return Collections.emptyList();
    }

    private static int extractIndex(Event importedEvent) {
        if (ImportedComponent.class.isInstance(importedEvent)) {
            return ((ImportedComponent) importedEvent).getIndex();
        }
        return 0;
    }

    private static Event getFirstCreatedEvent(InternalCalendarResult createResult) {
        if (null != createResult) {
            List<CreateResult> creations = createResult.getUserizedResult().getCreations();
            if (null != creations && 0 < creations.size()) {
                return creations.get(0).getCreatedEvent();
            }
        }
        return null;
    }

    /**
     * Tries to handle data truncation and incorrect string errors automatically.
     *
     * @param e The exception to handle
     * @param event The event being saved
     * @return <code>true</code> if the exception could be handled and the operation should be tried again, <code>false</code>, otherwise
     */
    private boolean handle(OXException e, Event event) {
        try {
            switch (e.getErrorCode()) {
                case "CAL-4227": // Incorrect string [string %1$s, field %2$s, column %3$s]
                    return session.getUtilities().handleIncorrectString(e, event);
                case "CAL-5070": // Data truncation [field %1$s, limit %2$d, current %3$d]
                    return session.getUtilities().handleDataTruncation(e, event);
                default:
                    // Fall through
                    break;
            }
        } catch (Exception x) {
            LOG.warn("Error during automatic handling of {}", e.getErrorCode(), x);
        }
        return false;
    }

}
