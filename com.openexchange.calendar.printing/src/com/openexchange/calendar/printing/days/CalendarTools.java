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

import java.util.Calendar;
import java.util.Date;
import com.openexchange.calendar.printing.CPAppointment;
import com.openexchange.calendar.printing.CPCalendar;

/**
 * {@link CalendarTools}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class CalendarTools {

    private CalendarTools() {
        super();
    }

    public static void toDayStart(CPCalendar cal) {
        for (int field : new int[] { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND }) {
            cal.set(field, cal.getActualMinimum(field));
        }
    }

    public static void toDayEnd(CPCalendar cal) {
        for (int field : new int[] { Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND }) {
            cal.set(field, cal.getActualMaximum(field));
        }
    }

    public static Date getDayStart(CPCalendar cal, Date date) {
        Date orig = cal.getTime();
        cal.setTime(date);
        toDayStart(cal);
        Date retval = cal.getTime();
        cal.setTime(orig);
        return retval;
    }

    public static Date getDayEnd(CPCalendar cal, Date date) {
        Date orig = cal.getTime();
        cal.setTime(date);
        toDayEnd(cal);
        Date retval = cal.getTime();
        cal.setTime(orig);
        return retval;
    }

    public static void moveBackToMonday(CPCalendar cal) {
        // Week starts 1-based on Sunday
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        if (weekDay > Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_WEEK, Calendar.MONDAY - weekDay);
        } else if (Calendar.SUNDAY == weekDay) {
            cal.add(Calendar.DAY_OF_WEEK, -6);
        }
    }

    public static void moveForwardToSunday(CPCalendar cal) {
        // Week starts 1-based on Sunday
        int weekDay = cal.get(Calendar.DAY_OF_WEEK);
        if (weekDay > Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_WEEK, 8 - weekDay);
        }
    }

    public static boolean overlaps(CPAppointment appointment1, CPAppointment appointment2) {
        return overlaps(appointment2, appointment1.getStartDate(), appointment1.getEndDate());
    }

    public static boolean overlaps(CPAppointment appointment, Date startDate, Date endDate) {
        return appointment.getStartDate().before(endDate) && appointment.getEndDate().after(startDate);
    }
}
