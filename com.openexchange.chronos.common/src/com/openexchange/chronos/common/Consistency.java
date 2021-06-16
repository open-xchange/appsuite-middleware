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

package com.openexchange.chronos.common;

import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;

/**
 * {@link Consistency}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.6
 */
public class Consistency {

    /**
     * Adjusts the start- and end-date of the supplied event in case it is marked as "all-day". This includes the truncation of the
     * time-part in <code>UTC</code>-timezone for the start- and end-date, as well as ensuring that the end-date is at least 1 day after
     * the start-date.
     *
     * @param event The event to adjust
     */
    public static void adjustAllDayDates(Event event) {
        if (null != event.getStartDate() && event.getStartDate().isAllDay()) {
            if (event.containsEndDate() && null != event.getEndDate()) {
                if (event.getEndDate().equals(event.getStartDate())) {
                    event.setEndDate(event.getEndDate().addDuration(new Duration(1, 1, 0)));
                }
            }
        }
    }

    /**
     * Normalizes all recurrence identifiers within the supplied event so that their value matches the date type and timezone of a certain
     * <i>reference</i> date, which is usually the start date of the recurring component.
     * <p/>
     * This includes the recurrence identifier itself, and additionally the event's collection of recurrence dates, exceptions dates, and
     * change exception dates, if set.
     * <p/>
     * Recurrence identifiers whose value is an <i>all-day</i> date are not changed.
     *
     * @param referenceDate The reference date to which the recurrence identifiers will be normalized to
     * @param event The event to normalize the recurrence identifiers in
     */
    public static void normalizeRecurrenceIDs(DateTime referenceDate, Event event) {
        if (event.containsRecurrenceId()) {
            event.setRecurrenceId(CalendarUtils.normalizeRecurrenceID(referenceDate, event.getRecurrenceId()));
        }
        if (event.containsRecurrenceDates()) {
            event.setRecurrenceDates(CalendarUtils.normalizeRecurrenceIDs(referenceDate, event.getRecurrenceDates()));
        }
        if (event.containsDeleteExceptionDates()) {
            event.setDeleteExceptionDates(CalendarUtils.normalizeRecurrenceIDs(referenceDate, event.getDeleteExceptionDates()));
        }
        if (event.containsChangeExceptionDates()) {
            event.setChangeExceptionDates(CalendarUtils.normalizeRecurrenceIDs(referenceDate, event.getChangeExceptionDates()));
        }
    }

    /**
     * Adjusts the recurrence rule according to the <a href="https://tools.ietf.org/html/rfc5545#section-3.3.10">RFC-5545, Section 3.3.10</a>
     * specification, which ensures that the value type of the UNTIL part of the recurrence rule has the same value type as the DTSTART.
     *
     * @param event The event to adjust
     * @throws OXException if the event has an invalid recurrence rule
     */
    public static void adjustRecurrenceRule(Event event) throws OXException {
        if (null == event.getRecurrenceRule()) {
            return;
        }
        RecurrenceRule rule = CalendarUtils.initRecurrenceRule(event.getRecurrenceRule());
        if (null == rule.getUntil()) {
            return;
        }
        DateTime until = rule.getUntil();
        TimeZone timeZone = event.getStartDate().getTimeZone();
        boolean startAllDay = event.getStartDate().isAllDay();
        boolean untilAllDay = until.isAllDay();
        if (startAllDay && !untilAllDay) {
            rule.setUntil(until.toAllDay());
        } else if (!startAllDay && untilAllDay) {
            rule.setUntil(new DateTime(until.getCalendarMetrics(), timeZone, until.getTimestamp()));
        } else {
            return;
        }
        event.setRecurrenceRule(rule.toString());
    }

}
