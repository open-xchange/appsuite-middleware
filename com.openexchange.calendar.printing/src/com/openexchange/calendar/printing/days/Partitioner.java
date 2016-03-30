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

package com.openexchange.calendar.printing.days;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.printing.CPAppointment;
import com.openexchange.calendar.printing.CPCalendar;
import com.openexchange.calendar.printing.CPParameters;
import com.openexchange.calendar.printing.CPTool;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.CalendarCollectionService;
import com.openexchange.groupware.calendar.Constants;
import com.openexchange.groupware.calendar.RecurringResultInterface;
import com.openexchange.groupware.calendar.RecurringResultsInterface;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.contexts.Context;

/**
 * Takes the list of appointments and calculates on which days an appointment has to appear.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Partitioner {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Partitioner.class);
    private final CPParameters params;
    private final CPCalendar cal;
    private final AppointmentSQLInterface appointmentSql;
    private final CalendarCollectionService calendarTools;
    private final Context context;
    private Date firstDay;
    private Date lastDay;
    private Date displayStart;
    private Date displayEnd;

    public Partitioner(final CPParameters params, final CPCalendar cal, final Context context, final AppointmentSQLInterface appointmentSql, final CalendarCollectionService calendarTools) {
        super();
        this.params = params;
        this.cal = cal;
        this.context = context;
        this.appointmentSql = appointmentSql;
        this.calendarTools = calendarTools;
    }

    public List<Day> partition(final List<Appointment> idList, int userId) {
        final SortedMap<Date, Day> dayMap = new TreeMap<Date, Day>();
        preFill(dayMap);

        for (final Appointment appointment : idList) {
            calculateToDays(dayMap, appointment, userId);
        }

        final List<Day> retval = new ArrayList<Day>(dayMap.size());
        while (!dayMap.isEmpty()) {
            final Date tmp = dayMap.firstKey();
            retval.add(dayMap.get(tmp));
            dayMap.remove(tmp);
        }
        return retval;
    }

    private void preFill(final SortedMap<Date, Day> dayMap) {
        makeFullBlock();
        cal.setTime(displayStart);
        Date tmp = cal.getTime();
        while (!tmp.after(displayEnd)) {
            dayMap.put(tmp, new Day(tmp, cal, tmp.before(firstDay) || tmp.after(new Date(lastDay.getTime()))));
            cal.add(Calendar.DATE, 1);
            tmp = cal.getTime();
        }
    }

    private void makeFullBlock() {
        firstDay = CalendarTools.getDayStart(cal, params.getStart());
        // omit the last millisecond of the end, because this must be exclusive and we calculate always inclusive.
        lastDay = CalendarTools.getDayStart(cal, new Date(params.getEnd().getTime() - 1));
        final long days = (lastDay.getTime() - firstDay.getTime()) / Constants.MILLI_DAY;
        if (days >= 27 && days <= 31) {
            makeMonthBlock(firstDay, lastDay);
        } else {
            displayStart = firstDay;
            displayEnd = lastDay;
        }
    }

    private void makeMonthBlock(final Date start, final Date end) {
        cal.setTime(start);
        // To be consistent with UI only use Monday as week start day.
        // cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        CalendarTools.moveBackToMonday(cal);
        displayStart = cal.getTime();
        cal.setTime(end);
        // To be consistent with UI only use Sunday as last day of week.
        // cal.set(Calendar.DAY_OF_WEEK, cal.getLastDayOfWeek());
        CalendarTools.moveForwardToSunday(cal);
        displayEnd = cal.getTime();
    }

    private void calculateToDays(final SortedMap<Date, Day> dayMap, final Appointment appointmentId, int userId) {
        try {
            final Appointment appointment = appointmentSql.getObjectById(appointmentId.getObjectID(), appointmentId.getParentFolderID());
            if (CPTool.hasDeclined(appointment, userId)) {
                return;
            }
            if (appointment.isMaster()) {
                final RecurringResultsInterface results = calendarTools.calculateRecurring(appointment, displayStart.getTime(), CalendarTools.getDayEnd(cal, displayEnd).getTime(), 0);
                if (results == null) {
                    addToMap(dayMap, appointment);
                } else {
                    for (int i = 0; i < results.size(); i++) {
                        final RecurringResultInterface result = results.getRecurringResult(i);
                        final Appointment occurrence = appointment.clone();
                        occurrence.setStartDate(new Date(result.getStart()));
                        occurrence.setEndDate(new Date(result.getEnd()));
                        addToMap(dayMap, occurrence);
                    }
                }
            } else {
                addToMap(dayMap, appointment);
            }
        } catch (final OXException e) {
            LOG.error("", e);
        } catch (final SQLException e) {
            LOG.error("", e);
        }
    }

    private void addToMap(final SortedMap<Date, Day> dayMap, final Appointment appointment) {
        if (appointment.getFullTime()) {
            // always have time at 00:00 UTC -> move to 00:00 of user time zone to ease following calculation
            final long start = appointment.getStartDate().getTime();
            appointment.setStartDate(new Date(start - cal.getTimeZone().getOffset(start)));
            final long end = appointment.getEndDate().getTime();
            appointment.setEndDate(new Date(end - cal.getTimeZone().getOffset(end)));
        }
        Date dest = CalendarTools.getDayStart(cal, appointment.getStartDate());
        Day day = dayMap.get(dest);
        Date endOfDay = CalendarTools.getDayEnd(cal, dest);
        do {
            final CPAppointment cpAppointment = new CPAppointment(appointment, cal, context);
            if (dest.after(appointment.getStartDate())) {
                cpAppointment.setStartDate(dest);
            }
            if (!endOfDay.after(appointment.getEndDate())) {
                cpAppointment.setEndDate(endOfDay);
            }
            if (null != day) {
                if (appointment.getFullTime()) {
                    day.addWholeDay(cpAppointment);
                } else {
                    day.add(cpAppointment);
                }
            }
            dest = CalendarTools.getDayStart(cal, new Date(endOfDay.getTime() + 1));
            day = dayMap.get(dest);
            endOfDay = CalendarTools.getDayEnd(cal, dest);
        } while (appointment.getEndDate().after(dest));
    }
}
