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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    private List<CPAppointment> wholeDayAppointments = new ArrayList<CPAppointment>();
    private SortedSet<CPAppointment> appointments = new TreeSet<CPAppointment>(new AppointmentStartComparator());
    private int sideBySide = 1;

    public Day(Date time, CPCalendar cal, boolean outOfRange) {
        super();
        this.time = time;
        this.cal = cal;
        this.outOfRange = outOfRange;
    }

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

    public String getName() {
        DateFormat df = new SimpleDateFormat("EEEE", cal.getLocale());
        df.setTimeZone(cal.getTimeZone());
        return df.format(time);
    }

    public String getMonthName() {
        DateFormat df = new SimpleDateFormat("MMMM", cal.getLocale());
        df.setTimeZone(cal.getTimeZone());
        return df.format(time);
    }

    public void addWholeDay(CPAppointment appointment) {
        wholeDayAppointments.add(appointment);
    }

    public List<CPAppointment> getWholeDayAppointments() {
        return wholeDayAppointments;
    }

    public void add(CPAppointment appointment) {
        appointments.add(appointment);
    }

    public List<CPAppointment> getAppointments() {
        List<CPAppointment> retval = new ArrayList<CPAppointment>(appointments.size());
        for (CPAppointment appointment : appointments) {
            retval.add(appointment);
        }
        return retval;
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
}
