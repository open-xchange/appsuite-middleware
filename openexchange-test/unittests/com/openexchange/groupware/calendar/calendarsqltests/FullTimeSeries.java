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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import com.openexchange.groupware.calendar.CalendarDataObject;

/**
 * @author <a href="mailto:martin.herfurth@open-xchange.org">Martin Herfurth</a>
 */
public class FullTimeSeries extends CalendarSqlTest {

    public void testFullTimeSeries() throws Throwable {
        CalendarDataObject appointment = appointments.buildAppointmentWithUserParticipants(user);
        appointment.setTitle("Test Full Time Series");
        long now = System.currentTimeMillis();
        appointment.setStartDate(new Date(now));
        appointment.setEndDate(new Date(now + 3600000));
        appointment.setFullTime(true);
        appointment.setRecurrenceType(CalendarDataObject.DAILY);
        appointment.setInterval(1);
        appointment.setOccurrence(2);
        appointments.save(appointment);
        int objectId = appointment.getObjectID();
        clean.add(appointment);

        Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        cal.setTimeInMillis(now);

        CalendarDataObject loaded = appointments.load(objectId, folders.getStandardFolder(userId, ctx));
        Calendar loadedUntil = new GregorianCalendar(TimeZone.getTimeZone("UTC"));
        loadedUntil.setTime(loaded.getUntil());

        cal.add(Calendar.DAY_OF_MONTH, 1);
        assertEquals("Wrong day in until", cal.get(Calendar.DAY_OF_MONTH), loadedUntil.get(Calendar.DAY_OF_MONTH));
        assertEquals("Wrong hour in until", 0, loadedUntil.get(Calendar.HOUR_OF_DAY));
    }
}
