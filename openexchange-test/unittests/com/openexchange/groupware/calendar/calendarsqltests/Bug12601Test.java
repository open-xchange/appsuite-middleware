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

package com.openexchange.groupware.calendar.calendarsqltests;

import java.util.Date;
import java.util.TimeZone;
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.CalendarObject;


public class Bug12601Test extends CalendarSqlTest {
    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12601">bug #12601</a><br>
     * <i>Calendar: Mini-Calendar shows wrong dates for recurring full time appointments</i>
     */
    public void testNoShiftOfYearlyRecApp() {
        try {
            // Create yearly recurring appointment
            final CalendarDataObject appointment = appointments.buildBasicAppointment(new Date(-616723200000L), new Date(-616636800000L));
            appointment.setTitle("Test for bug #12601");
            appointment.setFullTime(true);
            appointment.setRecurrenceType(CalendarObject.YEARLY);
            appointment.setInterval(1);
            appointment.setDayInMonth(17);
            appointment.setMonth(5);
            appointments.save(appointment);
            clean.add(appointment);
            // Do Mini-Calendar's range check for June 1980 in time zone
            // Europe/Berlin
            final TimeZone timeZone = TimeZone.getTimeZone("Europe/Berlin");
            final AppointmentSQLInterface appointmentsql = new CalendarSql(session);
            final boolean[] bHas = appointmentsql.hasAppointmentsBetween(applyTimeZone2Date(328147200968L, timeZone), applyTimeZone2Date(
                331776000968L,
                timeZone));
            assertEquals("Unexpected array length", 42, bHas.length);
            assertEquals("Index 22 is not marked true", true, bHas[22]);
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
