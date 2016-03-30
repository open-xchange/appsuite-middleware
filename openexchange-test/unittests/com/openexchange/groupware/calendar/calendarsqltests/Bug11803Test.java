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

import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import java.util.Date;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Participant;
import com.openexchange.tools.iterator.SearchIterator;


public class Bug11803Test extends CalendarSqlTest {
    // Bug 11803

    public void testFreeBusyResultShouldOnlyContainRecurrenceInSpecifiedInterval() throws Exception {
        final Date start = D("07/02/2008 10:00");
        final Date end = D("07/02/2008 12:00");
        // Create Weekly recurrence
        final CalendarDataObject appointment = appointments.buildBasicAppointment(start, end);
        appointment.setRecurrenceType(CalendarDataObject.WEEKLY);
        appointment.setDays(CalendarDataObject.WEDNESDAY);
        appointment.setTitle("Everything can happen on a Wednesday");
        appointment.setInterval(1);
        appointments.save(appointment);
        clean.add(appointment);

        // Ask for freebusy information in one week containing one ocurrence

        final SearchIterator<Appointment> iterator = appointments.getCurrentAppointmentSQLInterface().getFreeBusyInformation(
            userId,
            Participant.USER,
            D("18/02/2008 00:00"),
            D("25/02/2008 00:00"));
        // Verify only one ocurrence was returned
        try {
            assertTrue("Should find exactly one ocurrence. Found none.", iterator.hasNext());
            final Appointment occurrence = iterator.next();
            assertFalse("Should find exactly one ocurrence. Found more than one", iterator.hasNext());

            assertEquals(D("20/02/2008 10:00"), occurrence.getStartDate());
            assertEquals(D("20/02/2008 12:00"), occurrence.getEndDate());
        } finally {
            iterator.close();
        }
    }
}
