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
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.caching.basic.BasicCachingCalendarAccess;
import com.openexchange.chronos.provider.caching.internal.Services;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.exception.OXException;

/**
 * {@link SingleEventResponseGenerator}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.0
 */
public class SingleEventResponseGenerator extends ResponseGenerator {

    final String eventId;
    final RecurrenceId recurrenceId;

    public SingleEventResponseGenerator(BasicCachingCalendarAccess cachedCalendarAccess, String eventId, RecurrenceId recurrenceId) {
        super(cachedCalendarAccess);
        this.eventId = eventId;
        this.recurrenceId = recurrenceId;
    }

    /**
     * Loads a specific event from the storage.
     *
     * @param storage The initialized calendar storage to use
     * @param userId The identifier of the calendar user to load additional data for, or <code>-1</code> to not load user-sensitive data
     * @param eventId The identifier of the event to load
     * @param recurrenceId The recurrence identifier of the targeted event occurrence, or <code>null</code> if not applicable
     * @param fields The event fields to retrieve from the storage, or <code>null</code> to load all data
     * @return The loaded event
     */
    public static Event loadEvent(CalendarStorage storage, int userId, String eventId, RecurrenceId recurrenceId, EventField[] fields) throws OXException {
        Event event = storage.getEventStorage().loadEvent(eventId, getFields(fields, EventField.FOLDER_ID));
        if (null == event) {
            throw CalendarExceptionCodes.EVENT_NOT_FOUND.create(eventId);
        }
        event = storage.getUtilities().loadAdditionalEventData(userId, event, fields);
        event.setFlags(getFlags(event, userId));
        if (null != recurrenceId) {
            if (isSeriesMaster(event)) {
                Event exceptionEvent = storage.getEventStorage().loadException(eventId, recurrenceId, fields);
                if (null != exceptionEvent) {
                    exceptionEvent = storage.getUtilities().loadAdditionalEventData(userId, exceptionEvent, fields);
                    exceptionEvent.setFlags(getFlags(exceptionEvent, userId));
                    event = exceptionEvent;
                } else {
                    event = CalendarUtils.getOccurrence(Services.getService(RecurrenceService.class), event, recurrenceId);
                }
            }
            if (null == event || false == recurrenceId.matches(event.getRecurrenceId())) {
                throw CalendarExceptionCodes.EVENT_RECURRENCE_NOT_FOUND.create(eventId, recurrenceId);
            }
        }
        return event;
    }

    public Event generate() throws OXException {
        EventField[] fields = cachedCalendarAccess.getParameters().get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        int userId = cachedCalendarAccess.getAccount().getUserId();
        return new OSGiCalendarStorageOperation<Event>(Services.getServiceLookup(), this.cachedCalendarAccess.getSession().getContextId(), this.cachedCalendarAccess.getAccount().getAccountId()) {

            @Override
            protected Event call(CalendarStorage storage) throws OXException {
                return loadEvent(storage, userId, eventId, recurrenceId, fields);
            }
        }.executeQuery();
    }
}
