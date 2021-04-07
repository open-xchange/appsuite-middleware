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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.chronos.impl.scheduling;

import static com.openexchange.chronos.impl.Check.requireCalendarPermission;
import static com.openexchange.folderstorage.Permission.CREATE_OBJECTS_IN_FOLDER;
import static com.openexchange.folderstorage.Permission.NO_PERMISSIONS;
import static com.openexchange.folderstorage.Permission.WRITE_OWN_OBJECTS;
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
 * @since v8.0.0
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
     * @param storage The underlying calendar storage
     * @param session The calendar session
     * @param folder The calendar folder representing the current view on the events
     * @param source The source from which the scheduling has been triggered
     * @param addUserAttendee <code>true</code> to indicate that the calendar user shall be added to the list of attendees if she is missing.
     *            Set to <code>false</code> to throw an appropriated exception, which is default for the normal process of the {@link SchedulingMethod#ADD}.
     */
    protected AddProcessor(AbstractUpdatePerformer performer) {
        super(performer);
    }

    /**
     * Creates new change exceptions
     *
     * @param message The {@link IncomingSchedulingMessage}
     * @return An {@link InternalCalendarResult} containing the changes that has been performed
     * @throws OXException In case data is invalid, outdated or permissions are missing
     */
    public InternalCalendarResult process(IncomingSchedulingMessage message) throws OXException {
        return process(message, message.getResource().getEvents());
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
        if (null == changeExceptions || changeExceptions.isEmpty()) {
            return resultTracker.getResult();
        }

        Event firstEvent = changeExceptions.get(0);
        EventID eventID = Utils.resolveEventId(session, storage, firstEvent.getUid(), null, message.getTargetUser());
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
        if (null == events) {
            return null;
        }
        for (Event event : events) {
            if (null != recurrenceID && recurrenceID.matches(event.getRecurrenceId())) {
                return event;
            }
        }
        return null;
    }

}
