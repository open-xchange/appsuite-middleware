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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.groupware.calendar.CalendarDataObject;
import com.openexchange.groupware.container.Appointment;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class Bug11210Test extends CalendarSqlTest {

    private CalendarDataObject single, single2, single3;

    private CalendarDataObject sequenceMonthly;

    private final List<CalendarDataObject> allAppointments = new ArrayList<CalendarDataObject>();

    private int THIS_YEAR, THIS_MONTH;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        THIS_YEAR = Calendar.getInstance().get(Calendar.YEAR);
        THIS_MONTH = Calendar.getInstance().get(Calendar.MONTH);

        sequenceMonthly = appointments.buildBasicAppointment(createDate(THIS_YEAR, THIS_MONTH + 1, 1, 12), createDate(THIS_YEAR, THIS_MONTH + 1, 1, 13));
        sequenceMonthly.setRecurrenceType(Appointment.MONTHLY);
        sequenceMonthly.setInterval(1);
        sequenceMonthly.setDayInMonth(1);
        single = appointments.buildBasicAppointment(createDate(THIS_YEAR, THIS_MONTH + 2, 1, 12), createDate(THIS_YEAR, THIS_MONTH + 2, 1, 13));
        single2 = appointments.buildBasicAppointment(createDate(THIS_YEAR, THIS_MONTH + 3, 1, 12), createDate(THIS_YEAR, THIS_MONTH + 3, 1, 13));
        single3 = appointments.buildBasicAppointment(createDate(THIS_YEAR + 2, THIS_MONTH, 1, 12), createDate(THIS_YEAR + 2, THIS_MONTH, 1, 13));

        allAppointments.add(single);
        allAppointments.add(single2);
        allAppointments.add(sequenceMonthly);

        clean.addAll(allAppointments);

        setIgnoreConflicts(false);
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testConflict() throws Exception {
        appointments.save(single);
        CalendarDataObject[] conflicts = appointments.save(sequenceMonthly);
        assertFalse("No conflict", conflicts == null);
        assertEquals("Number of conflicts not correct", 1, conflicts.length);
        assertEquals("Wrong conflict", single.getObjectID(), conflicts[0].getObjectID());
    }

    public void testNoConflictAfterOneYear() throws Exception {
        appointments.save(single3);
        CalendarDataObject[] conflicts = appointments.save(sequenceMonthly);
        assertTrue("Conflict occurred", conflicts == null);
    }

    public void testOnlyOneConflict() throws Exception {
        appointments.save(single);
        appointments.save(single2);
        CalendarDataObject[] conflicts = appointments.save(sequenceMonthly);
        assertFalse("No conflict", conflicts == null);
        assertEquals("Number of conflicts not correct", 1, conflicts.length);
        assertEquals("Wrong conflict", single.getObjectID(), conflicts[0].getObjectID());
    }

    protected Date createDate(int year, int month, int day, int hour) {
        Calendar calendar = GregorianCalendar.getInstance(TimeZone.getTimeZone("UTC"));

        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    private void setIgnoreConflicts(boolean ignoreConflicts) {
        for (CalendarDataObject app : allAppointments) {
            if (app != null) {
                app.setIgnoreConflicts(ignoreConflicts);
            }
        }
    }
}
