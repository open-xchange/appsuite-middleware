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

import static com.openexchange.chronos.common.CalendarUtils.contains;
import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.getOccurrence;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.impl.Utils.getCalendarUserId;
import static com.openexchange.chronos.impl.Utils.getFolder;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.impl.CalendarFolder;
import com.openexchange.chronos.impl.Check;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link GetPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class GetPerformer extends AbstractQueryPerformer {

    /**
     * Initializes a new {@link GetPerformer}.
     *
     * @param session The calendar session
     * @param storage The underlying calendar storage
     */
    public GetPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the operation.
     *
     * @param folderId The identifier of the parent folder to read the event in
     * @param eventId The identifier of the event to get
     * @param recurrenceId The recurrence identifier of the occurrence to get, or <code>null</code> if no specific occurrence is targeted
     * @return The loaded event
     */
    public Event perform(String folderId, String eventId, RecurrenceId recurrenceId) throws OXException {
        /*
         * load event data
         */
        CalendarFolder folder = getFolder(session, folderId, false);
        EventField[] fields = getFields(session, EventField.ORGANIZER, EventField.ATTENDEES);
        Event event = storage.getEventStorage().loadEvent(eventId, fields);
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(eventId);
        }
        if (null != recurrenceId && isSeriesMaster(event) && contains(event.getChangeExceptionDates(), recurrenceId)) {
            Event exceptionEvent = storage.getEventStorage().loadException(eventId, recurrenceId, new EventField[] { EventField.ID });
            if (null == exceptionEvent) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventId, recurrenceId);
            }
            return perform(folderId, exceptionEvent.getId(), recurrenceId);
        }
        event = storage.getUtilities().loadAdditionalEventData(getCalendarUserId(folder), event, fields);
        /*
         * check permissions & userize event
         */
        Check.eventIsVisible(folder, event);
        Check.eventIsInFolder(event, folder);
        event = postProcessor().process(event, folder).getFirstEvent();
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(eventId);
        }
        /*
         * retrieve targeted event occurrence if specified
         */
        if (null != recurrenceId) {
            if (isSeriesMaster(event)) {
                event = getOccurrence(session.getRecurrenceService(), event, recurrenceId);
            }
            if (null == event || false == recurrenceId.matches(event.getRecurrenceId())) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventId, recurrenceId);
            }
        }
        return event;
    }

}
