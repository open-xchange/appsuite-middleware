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

package com.openexchange.ajax.appointment.recurrence;

import static com.openexchange.groupware.calendar.TimeTools.D;
import com.openexchange.ajax.appointment.helper.Changes;
import com.openexchange.ajax.appointment.helper.Expectations;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.container.Appointment;

/**
 * These tests use the recurrence_position field to access change exceptions.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForCreatingChangeExceptions extends ManagedAppointmentTest {

    public TestsForCreatingChangeExceptions(String name) {
        super(name);
    }

    public void testShouldAllowMovingAnExceptionBehindEndOfSeries() throws OXException {
        Appointment app = generateMonthlyAppointment(); // starts last year in January
        app.setOccurrence(3); // this should end last year in March

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2); // this should be the appointment in February
        changes.put(Appointment.START_DATE, D("1/5/2008 1:00"));
        changes.put(Appointment.END_DATE, D("1/5/2008 2:00"));

        positiveAssertionOnChangeException.check(app, changes, new Expectations(changes));
    }

    public void testShouldAllowMovingTheFirstAppointmentTo2359TheSameDay() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 1);
        changes.put(Appointment.START_DATE, D("1/1/2008 23:00", utc));
        changes.put(Appointment.END_DATE, D("1/1/2008 23:59", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }
    
    public void testShouldAllowMovingTheSecondAppointmentBeforeTheFirst() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.START_DATE, D("31/12/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("31/12/2008 2:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    public void testShouldAllowMovingTheSecondAppointmentTo2359TheDayBefore() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.START_DATE, D("1/1/2008 23:00", utc));
        changes.put(Appointment.END_DATE, D("1/1/2008 23:59", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    public void testShouldAllowMovingTheSecondAppointmentTo2359OnTheSameDay() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.START_DATE, D("2/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("2/1/2008 2:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }

    public void testShouldAllowMovingTheSecondAppointmentToTheSamePlaceAsTheThirdOne() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.START_DATE, D("3/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("3/1/2008 2:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
    }
    
    public void testShouldNotMixUpWholeDayChangeExceptionAndNormalSeries() throws OXException {
        Appointment app = generateDailyAppointment();
        app.setOccurrence(3);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, 2);
        changes.put(Appointment.FULL_TIME, true);
        changes.put(Appointment.START_DATE, D("3/1/2008 0:00", utc));
        changes.put(Appointment.END_DATE, D("3/1/2008 24:00", utc));

        Expectations expectations = new Expectations(changes);
        positiveAssertionOnChangeException.check(app, changes, expectations);
        
        Appointment series = positiveAssertionOnChangeException.getSeries();
        assertFalse("Series should not become full time if exception does" , series.getFullTime());
    }
}
