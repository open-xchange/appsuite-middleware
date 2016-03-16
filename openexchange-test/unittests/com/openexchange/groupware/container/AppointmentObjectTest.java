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

package com.openexchange.groupware.container;

import static com.openexchange.groupware.calendar.TimeTools.D;
import static com.openexchange.groupware.container.Appointment.LOCATION;
import static com.openexchange.groupware.container.Appointment.RECURRENCE_START;
import static com.openexchange.groupware.container.Appointment.SHOWN_AS;
import static com.openexchange.groupware.container.Appointment.TIMEZONE;
import static com.openexchange.groupware.container.CalendarObject.ALARM;
import static com.openexchange.groupware.container.CalendarObject.FULL_TIME;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class AppointmentObjectTest extends CalendarObjectTest {

    public void testCloneShouldNotChangeContainsStatus() {
        Appointment a = new Appointment();
        assertFalse(a.containsShownAs());
        Appointment b = a.clone();
        assertFalse(b.containsShownAs());
    }

    @Override
    public void testAttrAccessors() {
        Appointment object = new Appointment();

        // SHOWN_AS
        assertFalse(object.contains(SHOWN_AS));
        assertFalse(object.containsShownAs());

        object.setShownAs(-12);
        assertTrue(object.contains(SHOWN_AS));
        assertTrue(object.containsShownAs());
        assertEquals(-12, object.get(SHOWN_AS));

        object.set(SHOWN_AS,12);
        assertEquals(12, object.getShownAs());

        object.remove(SHOWN_AS);
        assertFalse(object.contains(SHOWN_AS));
        assertFalse(object.containsShownAs());



        // ALARM
        assertFalse(object.contains(ALARM));
        assertFalse(object.containsAlarm());

        object.setAlarm(-12);
        assertTrue(object.contains(ALARM));
        assertTrue(object.containsAlarm());
        assertEquals(-12, object.get(ALARM));

        object.set(ALARM,12);
        assertEquals(12, object.getAlarm());

        object.remove(ALARM);
        assertFalse(object.contains(ALARM));
        assertFalse(object.containsAlarm());



        // FULL_TIME
        assertFalse(object.contains(FULL_TIME));
        assertFalse(object.containsFullTime());

        object.setFullTime(false);
        assertTrue(object.contains(FULL_TIME));
        assertTrue(object.containsFullTime());
        assertEquals(false, object.get(FULL_TIME));

        object.set(FULL_TIME,true);
        assertEquals(true, object.getFullTime());

        object.remove(FULL_TIME);
        assertFalse(object.contains(FULL_TIME));
        assertFalse(object.containsFullTime());



        // TIMEZONE
        assertFalse(object.contains(TIMEZONE));
        assertFalse(object.containsTimezone());

        object.setTimezone("Bla");
        assertTrue(object.contains(TIMEZONE));
        assertTrue(object.containsTimezone());
        assertEquals("Bla", object.get(TIMEZONE));

        object.set(TIMEZONE,"Blupp");
        assertEquals("Blupp", object.getTimezone());

        object.remove(TIMEZONE);
        assertFalse(object.contains(TIMEZONE));
        assertFalse(object.containsTimezone());



        // RECURRENCE_START
        assertFalse(object.contains(RECURRENCE_START));
        assertFalse(object.containsRecurringStart());

        long start = D("24/02/2008 00:00").getTime();
        long otherStart = D("24/03/2008 00:00").getTime();

        object.setRecurringStart(start);
        assertTrue(object.contains(RECURRENCE_START));
        assertTrue(object.containsRecurringStart());
        assertEquals(start, object.get(RECURRENCE_START));

        object.set(RECURRENCE_START,otherStart);
        assertEquals(otherStart, object.getRecurringStart());

        object.remove(RECURRENCE_START);
        assertFalse(object.contains(RECURRENCE_START));
        assertFalse(object.containsRecurringStart());



        // LOCATION
        assertFalse(object.contains(LOCATION));
        assertFalse(object.containsLocation());

        object.setLocation("Bla");
        assertTrue(object.contains(LOCATION));
        assertTrue(object.containsLocation());
        assertEquals("Bla", object.get(LOCATION));

        object.set(LOCATION,"Blupp");
        assertEquals("Blupp", object.getLocation());

        object.remove(LOCATION);
        assertFalse(object.contains(LOCATION));
        assertFalse(object.containsLocation());

    }


    public Appointment getAppointmentObject() {
        Appointment object = new Appointment();

        fillAppointmentObject(object);

        return object;
    }

    public void fillAppointmentObject(Appointment object) {
        super.fillCalendarObject(object);

        object.setAlarm(-12);

        object.setFullTime(false);

        object.setIgnoreConflicts(false);

        object.setLocation("Bla");

        object.setRecurringStart(D("24/02/2007 10:00").getTime());

        object.setShownAs(-12);

        object.setTimezone("Bla");

    }
}
