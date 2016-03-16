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
import com.openexchange.api2.AppointmentSQLInterface;
import com.openexchange.calendar.CalendarSql;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.search.Order;
import com.openexchange.tools.iterator.SearchIterator;


public class Bug12681Test extends CalendarSqlTest {
    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12681">bug #12681</a>
     */
    public void testUpdatingRecAppToEndsNever() {
        try {
            // Create daily appointment on 15. January 2009 08:00:00 UTC
            final CalendarDataObject appointment = appointments.buildBasicAppointment(new Date(1232006400000L), new Date(1232010000000L));
            appointment.setTitle("testUpdatingRecAppToEndsNever");
            appointment.setRecurrenceType(CalendarDataObject.DAILY);
            appointment.setInterval(1);
            appointment.setOccurrence(25);
            appointments.save(appointment);
            clean.add(appointment);

            // Update formerly created appointment to end never
            final CalendarDataObject update = appointments.createIdentifyingCopy(appointment);
            update.setRecurrenceType(CalendarDataObject.DAILY);
            update.setInterval(1);
            update.setOccurrence(0);
            update.removeUntil();

            // Request time-rage for February 2009
            {
                // Check LIST query for February 2009
                final AppointmentSQLInterface appointmentsql = new CalendarSql(session);
                final Date queryStart = new Date(1230508800000L);
                final Date queryEnd = new Date(1233532800000L);

                final SearchIterator<Appointment> listIterator = appointmentsql.getAppointmentsBetweenInFolder(
                    appointments.getPrivateFolder(),
                    ACTION_ALL_FIELDS,
                    queryStart,
                    queryEnd,
                    CalendarObject.START_DATE,
                    Order.ASCENDING);
                try {
                    boolean found = false;
                    while (listIterator.hasNext() && !found) {
                        listIterator.next();
                        found = true;
                    }
                    assertTrue("No occurrence found in February 2009!", found);
                } finally {
                    listIterator.close();
                }
            }
        } catch (final Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }
}
