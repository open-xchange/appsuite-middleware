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


public class Bug12413Test extends CalendarSqlTest {
    /**
     * Test for <a href= "http://bugs.open-xchange.com/cgi-bin/bugzilla/show_bug.cgi?id=12413">bug #12413</a><br>
     * <i>Calendar: Month list view hides appointments on 2008-10-31</i>
     */
    public void testProperAllRequest() throws Exception {
        // create appointment on 31.10.2008 from 23:00h until 24:00h
        final CalendarDataObject octoberApp = appointments.buildBasicAppointment(new Date(1225494000000L), new Date(1225497600000L));
        octoberApp.setTitle("October-Appointment");
        // Save
        appointments.save(octoberApp);
        clean.add(octoberApp);
        // create appointment on 01.11.2008 from 00:00h until 01:00h
        final CalendarDataObject novemberApp = appointments.buildBasicAppointment(new Date(1225497600000L), new Date(1225501200000L));
        novemberApp.setTitle("November-Appointment");
        // Save
        appointments.save(novemberApp);
        clean.add(novemberApp);
        {
            // Check LIST query for October
            final AppointmentSQLInterface appointmentsql = new CalendarSql(session);
            // 1. October 2008 00:00:00 UTC
            final Date octQueryStart = new Date(1222819200000L);
            // 1. November 2008 00:00:00 UTC
            final Date octQueryEnd = new Date(1225497600000L);
            final SearchIterator<Appointment> octListIterator = appointmentsql.getAppointmentsBetweenInFolder(
                appointments.getPrivateFolder(),
                ACTION_ALL_FIELDS,
                octQueryStart,
                octQueryEnd,
                CalendarObject.START_DATE,
                Order.ASCENDING);
            try {
                int count = 0;
                while (octListIterator.hasNext()) {
                    octListIterator.next();
                    count++;
                }
                assertEquals("Unexpected number of search iterator results: ", 1, count);
            } finally {
                octListIterator.close();
            }
        }
        {
            // Check LIST query for November
            final AppointmentSQLInterface appointmentsql = new CalendarSql(session);
            // 1. November 2008 00:00:00 UTC
            final Date novQueryStart = new Date(1225497600000L);
            // 1. December 2008 00:00:00 UTC
            final Date novQueryEnd = new Date(1228089600000L);
            final SearchIterator<Appointment> novListIterator = appointmentsql.getAppointmentsBetweenInFolder(
                appointments.getPrivateFolder(),
                ACTION_ALL_FIELDS,
                novQueryStart,
                novQueryEnd,
                CalendarObject.START_DATE,
                Order.ASCENDING);
            try {
                int count = 0;
                while (novListIterator.hasNext()) {
                    novListIterator.next();
                    count++;
                }
                assertEquals("Unexpected number of search iterator results: ", 1, count);
            } finally {
                novListIterator.close();
            }
        }
    }
}
