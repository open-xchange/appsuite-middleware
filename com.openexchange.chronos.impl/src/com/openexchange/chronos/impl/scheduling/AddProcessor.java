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

package com.openexchange.chronos.impl.scheduling;

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
import static com.openexchange.tools.arrays.Collections.isNullOrEmpty;
import java.util.Collection;
import java.util.List;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.impl.InternalCalendarResult;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.impl.performer.AbstractUpdatePerformer;
import com.openexchange.chronos.impl.performer.CreatePerformer;
import com.openexchange.chronos.scheduling.IncomingSchedulingMessage;
import com.openexchange.chronos.scheduling.SchedulingMethod;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link AddProcessor} - Processes the method {@link SchedulingMethod#ADD}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.6
 */
public class AddProcessor extends CreatePerformer {

    /**
     * Initializes a new {@link RequestProcessor}.
     * 
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @throws OXException If initialization fails
     */
    public AddProcessor(CalendarStorage storage, CalendarSession session, CalendarFolder folder) throws OXException {
        super(storage, session, folder);
    }

    /**
     * Initializes a new {@link RequestProcessor}.
     * 
     * @param performer The update performer to take over the settings from
     */
    protected AddProcessor(AbstractUpdatePerformer performer) {
        super(performer);
    }

    /**
     * Creates new change exception(s)
     *
     * @param message The {@link IncomingSchedulingMessage}
     * @return An {@link InternalCalendarResult} containing the changes that has been performed
     * @throws OXException In case data is invalid, outdated or permissions are missing
     */
    public InternalCalendarResult process(IncomingSchedulingMessage message) throws OXException {
        return process(message, message.getResource().getChangeExceptions());
    }

    /**
     * Creates new change exceptions
     *
     * @param message The {@link IncomingSchedulingMessage}
     * @param changeExceptions The change exceptions to process
     * @return An {@link InternalCalendarResult} containing the changes that has been performed
     * @throws OXException In case data is invalid, outdated or permissions are missing
     */
    public InternalCalendarResult process(IncomingSchedulingMessage message, List<Event> changeExceptions) throws OXException {
        if (isNullOrEmpty(changeExceptions)) {
            return resultTracker.getResult();
        }

        EventID eventID = Utils.resolveEventId(session, storage, changeExceptions.get(0).getUid(), null, message.getTargetUser());
        Event originalMasterEvent = loadEventData(eventID.getObjectID());
        if (false == CalendarUtils.isSeriesMaster(originalMasterEvent)) {
            /*
             * It is recommended to send a REFRESH, see https://tools.ietf.org/html/rfc5546#section-3.2.4
             * However we don't support this, so throw error
             */
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Can't add ocurrences to a non series event");
        }
        /*
         * Check internal constrains
         */
        CalendarUser originator = message.getSchedulingObject().getOriginator();
        if (false == SchedulingUtils.originatorMatches(originalMasterEvent, originator)) {
            throw CalendarExceptionCodes.NOT_ORGANIZER.create(folder.getId(), originalMasterEvent.getId(), originator.getUri(), originator.getCn());
        }
        requireCalendarPermission(folder, CREATE_OBJECTS_IN_FOLDER, NO_PERMISSIONS, WRITE_OWN_OBJECTS, NO_PERMISSIONS);
        Check.eventIsVisible(folder, originalMasterEvent);
        Check.eventIsInFolder(originalMasterEvent, folder);
        Check.uidMatches(changeExceptions);
        requireWritePermissions(originalMasterEvent, true);

        List<Event> originalChangeExceptions = loadExceptionData(originalMasterEvent);
        for (Event exception : changeExceptions) {
            if (null != find(originalChangeExceptions, exception.getRecurrenceId())) {
                LOG.warn("Unable to add existing recurrence with ID {}", exception.getRecurrenceId());
                session.addWarning(CalendarExceptionCodes.IGNORED_INVALID_DATA.create(exception.getRecurrenceId(), EventField.CHANGE_EXCEPTION_DATES, "normal", "Change exception already exists."));
                continue;
            }
            /*
             * Create new exception for existing series
             */
            createEvent(exception, originalChangeExceptions);
        }

        return resultTracker.getResult();
    }

    /*
     * ============================== HELPERS ==============================
     */

    /**
     * Finds a specific event identified by its object-identifier and an optional recurrence identifier in a collection. The lookup is
     * performed based on {@link RecurrenceId#matches(RecurrenceId)}.
     *
     * @param events The events to search in
     * @param recurrenceID The recurrence identifier of the event to search
     * @return The event, or <code>null</code> if not found
     * @see RecurrenceId#matches(RecurrenceId)
     */
    public static Event find(Collection<Event> events, RecurrenceId recurrenceID) {
        if (isNullOrEmpty(events) || null == recurrenceID) {
            return null;
        }
        return events.stream().filter(e -> recurrenceID.matches(e.getRecurrenceId())).findAny().orElse(null);
    }

}
