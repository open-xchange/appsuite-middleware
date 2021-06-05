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

package com.openexchange.calendar.json.actions.chronos;

import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.RecurrenceIdComparator;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DefaultEventConverter}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DefaultEventConverter extends EventConverter {

    protected final CalendarSession session;

    /**
     * Initializes a new {@link DefaultEventConverter}.
     *
     * @param services A service lookup reference
     * @param session The underlying calendar session
     */
    public DefaultEventConverter(ServiceLookup services, CalendarSession calendarSession) {
        super(services, calendarSession.getSession());
        this.session = calendarSession;
    }

    @Override
    public TimeZone getDefaultTimeZone() throws OXException {
        TimeZone timeZone = session.get(CalendarParameters.PARAMETER_TIMEZONE, TimeZone.class);
        if (null == timeZone) {
            timeZone = session.getEntityResolver().getTimeZone(session.getUserId());
        }
        return timeZone;
    }

    @Override
    protected RecurrenceService getRecurrenceService() {
        return session.getRecurrenceService();
    }

    @Override
    public CalendarSession getCalendarSession() {
        return session;
    }

    /**
     * Gets a specific event.
     *
     * @param eventID The identifier of the event to get
     * @param fields The event fields to retrieve
     * @return The event
     */
    @Override
    protected Event getEvent(EventID eventID, EventField... fields) throws OXException {
        EventField[] oldFields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        Boolean oldExpandOccurrences = session.get(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.class);
        Date oldRangeStart = session.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date oldRangeEnd = session.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        try {
            session.set(CalendarParameters.PARAMETER_FIELDS, null);
            session.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, Boolean.FALSE);
            session.set(CalendarParameters.PARAMETER_RANGE_START, null);
            session.set(CalendarParameters.PARAMETER_RANGE_END, null);
            return session.getCalendarService().getEvent(session, eventID.getFolderID(), eventID);
        } finally {
            session.set(CalendarParameters.PARAMETER_FIELDS, oldFields);
            session.set(CalendarParameters.PARAMETER_EXPAND_OCCURRENCES, oldExpandOccurrences);
            session.set(CalendarParameters.PARAMETER_RANGE_START, oldRangeStart);
            session.set(CalendarParameters.PARAMETER_RANGE_END, oldRangeEnd);
        }
    }

    @Override
    protected SortedSet<RecurrenceId> loadChangeExceptionDates(Event event) throws OXException {
        TreeSet<RecurrenceId> recurrenceIds = new TreeSet<RecurrenceId>(RecurrenceIdComparator.DEFAULT_COMPARATOR);
        EventField[] oldFields = session.get(CalendarParameters.PARAMETER_FIELDS, EventField[].class);
        Date oldRangeStart = session.get(CalendarParameters.PARAMETER_RANGE_START, Date.class);
        Date oldRangeEnd = session.get(CalendarParameters.PARAMETER_RANGE_END, Date.class);
        try {
            session.set(CalendarParameters.PARAMETER_FIELDS, new EventField[] { EventField.RECURRENCE_ID });
            session.set(CalendarParameters.PARAMETER_RANGE_START, null);
            session.set(CalendarParameters.PARAMETER_RANGE_END, null);
            List<Event> changeExceptions = session.getCalendarService().getChangeExceptions(session, event.getFolderId(), event.getSeriesId());
            for (Event changeException : changeExceptions) {
                recurrenceIds.add(changeException.getRecurrenceId());
            }
            return recurrenceIds;
        } finally {
            session.set(CalendarParameters.PARAMETER_FIELDS, oldFields);
            session.set(CalendarParameters.PARAMETER_RANGE_START, oldRangeStart);
            session.set(CalendarParameters.PARAMETER_RANGE_END, oldRangeEnd);
        }
    }

    @Override
    protected RecurrenceData loadRecurrenceData(String seriesId) throws OXException {
        return session.getCalendarService().getUtilities().loadRecurrenceData(session, seriesId);
    }

}
