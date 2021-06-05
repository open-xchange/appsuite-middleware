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
import static com.openexchange.chronos.common.CalendarUtils.getFields;
import static com.openexchange.chronos.common.CalendarUtils.initCalendar;
import static com.openexchange.chronos.common.CalendarUtils.isGroupScheduled;
import static com.openexchange.chronos.common.CalendarUtils.isInRange;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.chronos.common.CalendarUtils.matches;
import static com.openexchange.chronos.common.CalendarUtils.truncateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ParticipationStatus;
import com.openexchange.chronos.Period;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.impl.Utils;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;

/**
 * {@link HasPerformer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class HasPerformer extends AbstractFreeBusyPerformer {

    /**
     * Initializes a new {@link HasPerformer}.
     *
     * @param storage The underlying calendar storage
     * @param session The calendar session
     */
    public HasPerformer(CalendarSession session, CalendarStorage storage) {
        super(session, storage);
    }

    /**
     * Performs the "has events between" operation.
     *
     * @param userID The identifier of the user to evaluate the "has" flags for
     * @param from The start date of the period to consider
     * @param until The end date of the period to consider
     * @return The "has" result, i.e. an array of <code>boolean</code> values representing the days where appointments are in
     */
    public boolean[] perform(int userID, Date from, Date until) throws OXException {
        /*
         * interpret range as "utc" dates
         */
        Calendar calendar = initCalendar(TimeZones.UTC, from);
        Date rangeStart = truncateTime(calendar).getTime();
        calendar.setTime(until);
        truncateTime(calendar);
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date rangeEnd = calendar.getTime();
        /*
         * search overlapping events
         */
        EventField[] fields = getFields(new EventField[0], EventField.ORGANIZER, EventField.ATTENDEES);
        List<Attendee> attendees = Collections.singletonList(session.getEntityResolver().applyEntityData(new Attendee(), userID));
        List<Event> events = storage.getEventStorage().searchOverlappingEvents(attendees, true, new SearchOptions().setRange(rangeStart, rangeEnd), fields);
        events = storage.getUtilities().loadAdditionalEventData(-1, events, fields);
        /*
         * step through events day-wise & check for present events
         */
        List<Boolean> hasEventsList = new ArrayList<Boolean>();
        TimeZone timeZone = TimeZones.UTC;//getTimeZone(session);
        calendar = initCalendar(timeZone, from);
        calendar.setTime(rangeStart);
        Date minimumEndTime = calendar.getTime();
        calendar.add(Calendar.DAY_OF_YEAR, 1);
        Date maximumStartTime = calendar.getTime();
        while (maximumStartTime.before(rangeEnd)) {
            boolean hasEvents = false;
            for (int i = 0; i < events.size() && false == hasEvents; i++) {
                Event event = events.get(i);
                if (isGroupScheduled(event)) {
                    Attendee attendee = find(event.getAttendees(), userID);
                    if (null == attendee || ParticipationStatus.DECLINED.equals(attendee.getPartStat())) {
                        continue; // skip if user does not attend
                    }
                } else if (false == matches(event.getCalendarUser(), userID)) {
                    continue; // skip if user doesn't match event owner
                }
                if (isSeriesMaster(event)) {
                    long duration = event.getEndDate().getTimestamp() - event.getStartDate().getTimestamp();
                    Iterator<RecurrenceId> iterator = Utils.getRecurrenceIterator(session, event, minimumEndTime, maximumStartTime);
                    while (iterator.hasNext() && false == hasEvents) {
                        RecurrenceId recurrenceId = iterator.next();
                        Period occurence = new Period(new Date(recurrenceId.getValue().getTimestamp()), new Date(recurrenceId.getValue().getTimestamp() + duration), event.getStartDate().isAllDay());
                        hasEvents |= isInRange(occurence, minimumEndTime, maximumStartTime, timeZone);
                    }
                } else {
                    hasEvents |= isInRange(event, minimumEndTime, maximumStartTime, timeZone);
                }
            }
            hasEventsList.add(Boolean.valueOf(hasEvents));
            minimumEndTime = maximumStartTime;
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            maximumStartTime = calendar.getTime();
        }
        boolean[] hasEventsArray = new boolean[hasEventsList.size()];
        for (int i = 0; i < hasEventsArray.length; i++) {
            hasEventsArray[i] = hasEventsList.get(i).booleanValue();
        }
        return hasEventsArray;

    }

}
