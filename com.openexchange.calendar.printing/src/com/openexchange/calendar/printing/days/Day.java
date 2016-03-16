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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import com.openexchange.calendar.printing.CPAppointment;
import com.openexchange.calendar.printing.CPCalendar;

/**
 * {@link Day}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Day implements Comparable<Day> {

    private final Date time;
    private final CPCalendar cal;
    private final boolean outOfRange;
    private final List<CPAppointment> wholeDayAppointments = new ArrayList<CPAppointment>();
    private final SortedSet<CPAppointment> appointments = new TreeSet<CPAppointment>(new AppointmentStartComparator());
    private final List<SortedSet<CPAppointment>> columns = new ArrayList<SortedSet<CPAppointment>>();

    public Day(Date time, CPCalendar cal, boolean outOfRange) {
        super();
        this.time = time;
        this.cal = cal;
        this.outOfRange = outOfRange;
        columns.add(new TreeSet<CPAppointment>(new AppointmentStartComparator()));
    }

    @Override
    public int compareTo(Day o) {
        return time.compareTo(o.time);
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof Day) ? time.equals(((Day) obj).time) : false;
    }

    @Override
    public int hashCode() {
        return time.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(time.toString());
        sb.append(':');
        sb.append(appointments.toString());
        return sb.toString();
    }

    public Date getTime() {
        return time;
    }

    public boolean isOutOfRange() {
        return outOfRange;
    }

    public String format(String pattern, Date date) {
        return cal.format(pattern, date);
    }

    public void addWholeDay(CPAppointment appointment) {
        wholeDayAppointments.add(appointment);
    }

    public List<CPAppointment> getWholeDayAppointments() {
        return wholeDayAppointments;
    }

    public boolean hasWholeDayAppointments() {
        return wholeDayAppointments.size() != 0;
    }

    public void add(CPAppointment appointment) {
        appointments.add(appointment);
        for (SortedSet<CPAppointment> column : columns) {
            boolean fits = true;
            for (CPAppointment other : column) {
                if (CalendarTools.overlaps(appointment, other)) {
                    fits = false;
                    break;
                }
            }
            if (fits) {
                column.add(appointment);
                return;
            }
        }
        SortedSet<CPAppointment> newColumn = new TreeSet<CPAppointment>(new AppointmentStartComparator());
        columns.add(newColumn);
        newColumn.add(appointment);
    }

    public List<CPAppointment> getAppointments() {
        List<CPAppointment> retval = new ArrayList<CPAppointment>(appointments.size());
        for (CPAppointment appointment : appointments) {
            retval.add(appointment);
        }
        return retval;
    }

    public boolean hasAppointments() {
        return appointments.size() != 0;
    }

    public List<CPAppointment> getAppointmentsStartingBetween(int startMinutes, int endMinutes) {
        Date orig = cal.getTime();
        cal.setTime(time);
        cal.add(Calendar.MINUTE, startMinutes);
        Date startDate = cal.getTime();
        cal.setTime(time);
        cal.add(Calendar.MINUTE, endMinutes);
        Date endDate = cal.getTime();
        List<CPAppointment> retval = new ArrayList<CPAppointment>();
        for (CPAppointment appointment : appointments) {
            if (!appointment.getStartDate().before(startDate) && appointment.getStartDate().before(endDate)) {
                retval.add(appointment);
            }
        }
        cal.setTime(orig);
        return retval;
    }

    public List<SortedSet<CPAppointment>> getColumns() {
        return columns;
    }

    public int getColumn(CPAppointment appointment) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).contains(appointment)) {
                return i;
            }
        }
        return 0;
    }

    public boolean isToday() {
        Date orig = cal.getTime();
        cal.setTimeInMillis(System.currentTimeMillis());
        CalendarTools.toDayStart(cal);
        boolean retval = time.equals(cal.getTime());
        cal.setTime(orig);
        return retval;
    }

    public boolean isFirstDayOfWeek() {
        Date orig = cal.getTime();
        cal.setTime(time);
        // To be consistent with UI first day of week is always Monday.
        // int lastDayOfWeek = cal.getFirstDayOfWeek();
        int firstDayOfWeek = Calendar.MONDAY;
        boolean retval = firstDayOfWeek == cal.get(Calendar.DAY_OF_WEEK);
        cal.setTime(orig);
        return retval;
    }

    public boolean isLastDayOfWeek() {
        Date orig = cal.getTime();
        cal.setTime(time);
        // To be consistent with UI last day of week is always Sunday.
        // int lastDayOfWeek = cal.getLastDayOfWeek();
        int lastDayOfWeek = Calendar.SUNDAY;
        boolean retval = lastDayOfWeek == cal.get(Calendar.DAY_OF_WEEK);
        cal.setTime(orig);
        return retval;
    }

    public boolean isWeekEnd() {
        Date orig = cal.getTime();
        cal.setTime(time);
        final int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        boolean retval = (Calendar.SATURDAY == dayOfWeek) || (Calendar.SUNDAY == dayOfWeek);
        cal.setTime(orig);
        return retval;
    }
}
