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

package com.openexchange.chronos.impl;

import java.util.Date;
import java.util.TimeZone;
import org.dmfs.rfc5545.DateTime;
import org.dmfs.rfc5545.Duration;
import org.dmfs.rfc5545.recur.RecurrenceRule;
import com.openexchange.chronos.CalendarUser;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.type.PublicType;

/**
 * {@link CalendarService}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class Consistency {

    /**
     * Checks and adjusts the timezones of the event's start- and end-time (in case they are <i>set</i>) to match well-known & valid
     * timezones, using different fallbacks if no exactly matching timezone is available.
     *
     * @param session The calendar session
     * @param calendarUserId The identifier of the user to get the fallback timezone from
     * @param event The event to set the timezones in
     * @param originalEvent The original event, or <code>null</code> if not applicable
     */
    public static void adjustTimeZones(CalendarSession session, int calendarUserId, Event event, Event originalEvent) throws OXException {
        if (event.containsStartDate()) {
            event.setStartDate(selectTimeZone(session, event.getStartDate(), calendarUserId, null == originalEvent ? null : originalEvent.getStartDate()));
        }
        if (event.containsEndDate()) {
            event.setEndDate(selectTimeZone(session, event.getEndDate(), calendarUserId, null == originalEvent ? null : originalEvent.getEndDate()));
        }
    }

    private static DateTime selectTimeZone(CalendarSession session, DateTime dateTime, int calendarUserId, DateTime originalDateTime) throws OXException {
        if (null == dateTime || dateTime.isFloating() || null == dateTime.getTimeZone()) {
            return dateTime;
        }
        TimeZone selectedTimeZone = Utils.selectTimeZone(session, calendarUserId, dateTime.getTimeZone(), null == originalDateTime ? null : originalDateTime.getTimeZone());
        if (false == dateTime.getTimeZone().equals(selectedTimeZone)) {
            return DateTime.parse(selectedTimeZone, dateTime.toString());
        }
        return dateTime;
    }

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

    public static void setModified(CalendarSession session, Date lastModified, Event event, int modifiedBy) throws OXException {
        setModified(lastModified, event, session.getEntityResolver().applyEntityData(new CalendarUser(), modifiedBy));
    }

    public static void setCreated(CalendarSession session, Date created, Event event, int createdBy) throws OXException {
        setCreated(created, event, session.getEntityResolver().applyEntityData(new CalendarUser(), createdBy));
    }

    public static void setModified(Date lastModified, Event event, CalendarUser modifiedBy) {
        event.setLastModified(lastModified);
        event.setModifiedBy(modifiedBy);
        event.setTimestamp(lastModified.getTime());
    }

    public static void setCreated(Date created, Event event, CalendarUser createdBy) {
        event.setCreated(created);
        event.setCreatedBy(createdBy);
    }

    public static void setCalenderUser(CalendarSession session, CalendarFolder folder, Event event) throws OXException {
        if (PublicType.getInstance().equals(folder.getType())) {
            event.setCalendarUser(null);
        } else {
            event.setCalendarUser(session.getEntityResolver().applyEntityData(new CalendarUser(), folder.getCreatedBy()));
        }
    }

    private Consistency() {
        super();
    }

}
