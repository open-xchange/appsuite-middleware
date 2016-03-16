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

import com.openexchange.exception.OXException;
import static com.openexchange.groupware.calendar.tools.CommonAppointments.D;
import com.openexchange.groupware.calendar.CalendarCallbacks;
import com.openexchange.groupware.calendar.CalendarDataObject;


public class Bug12377Test extends CalendarSqlTest {
    // Bug 12377

    public void testShouldDoCallbackWhenHavingCreatedAnException() throws OXException {
        final TestCalendarListener calendarListener = new TestCalendarListener();
        CalendarCallbacks.getInstance().addListener(calendarListener);
        try {
            final CalendarDataObject master = appointments.buildBasicAppointment(D("10/02/2008 10:00"), D("10/02/2008 12:00"));
            master.setRecurrenceType(CalendarDataObject.DAILY);
            master.setInterval(1);
            master.setOccurrence(10);
            appointments.save(master);
            clean.add(master);

            final CalendarDataObject exception = appointments.createIdentifyingCopy(master);
            exception.setRecurrencePosition(3);
            exception.setStartDate(D("13/02/2008 13:00"));
            exception.setEndDate(D("13/02/2008 15:00"));
            calendarListener.clear();
            final int[] changeExceptionId = new int[1];
            calendarListener.setVerifyer(new Verifyer() {

                @Override
                public void verify(final TestCalendarListener calendarListener) {
                    assertEquals("createdChangeExceptionInRecurringAppointment", calendarListener.getCalledMethodName());
                    final CalendarDataObject masterFromEvent = (CalendarDataObject) calendarListener.getArg(0);
                    final CalendarDataObject changeExceptionFromEvent = (CalendarDataObject) calendarListener.getArg(1);

                    assertEquals(masterFromEvent.getObjectID(), master.getObjectID());
                    assertEquals(masterFromEvent.getObjectID(), changeExceptionFromEvent.getRecurrenceID());
                    changeExceptionId[0] = changeExceptionFromEvent.getObjectID();

                }
            });

            appointments.save(exception);

            assertEquals(exception.getObjectID(), changeExceptionId[0]);

            assertTrue("Callback was not triggered", calendarListener.wasCalled());

        } finally {
            CalendarCallbacks.getInstance().removeListener(calendarListener);
        }
    }
}
