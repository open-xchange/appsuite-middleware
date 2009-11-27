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

import java.util.Calendar;
import com.openexchange.ajax.appointment.helper.Changes;
import com.openexchange.ajax.appointment.helper.Expectations;
import com.openexchange.ajax.appointment.helper.OXError;
import com.openexchange.groupware.container.Appointment;

/**
 * Find out which parameters are needed to create an appointment and which are not enough.
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsToCreateMinimalAppointmentSeries extends ManagedAppointmentTest {

    public TestsToCreateMinimalAppointmentSeries(String name) {
        super(name);
    }

    public void testShouldFailWhenSendingUnneccessaryDayInformationForDailyAppointment() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.DAILY);
        changes.put(Appointment.RECURRENCE_COUNT, 7);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAYS, 127);

        // TODO: Needs to throw exception
        negativeAssertionOnUpdate.check(changes, new OXError("APP", 999));
    }

    public void testShouldCreateDailyIntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.DAILY);
        changes.put(Appointment.INTERVAL, 1);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
    }

    public void testShouldCreateWeeklyIntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.WEEKLY);
        changes.put(Appointment.INTERVAL, 1);

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.DAYS, 127); // Should default to 127 as per HTTP API

        positiveAssertionOnCreate.check(changes, expectations);
    }

    public void testShouldCreateWeeklyIntervalWithDaysFieldDifferentThan127() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.WEEKLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY + Appointment.TUESDAY);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
    }

    public void testShouldFailCreatingMonthlyIntervalWithoutDayInMonthInfo() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.INTERVAL, 1);

        negativeAssertionOnUpdate.check(changes, new OXError("APP", 42));
    }

    public void testShouldFailCreatingMonthly2IntervalWithoutDayInMonthInfo() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY);

        negativeAssertionOnUpdate.check(changes, new OXError("APP", 45));
    }

    // first day every month
    public void testShouldCreateMonthlyIntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
    }

    // first monday every month
    public void testShouldCreateMonthly2IntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
    }

    public void testShouldFailCreatingYearlyIntervalWithoutDayInMonthInfo() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);

        negativeAssertionOnUpdate.check(changes, new OXError("APP", 46));
    }

    public void testShouldFailCreatingYearly2IntervalWithoutDayInMonthInfo() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY);

        negativeAssertionOnUpdate.check(changes, new OXError("APP", 48));
    }

    public void testShouldFailCreatingYearlyIntervalWithoutMonth() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);
        // currently, this is app-0080, but this is not actually too complex, it is just missing a the "month" field
        negativeAssertionOnUpdate.check(changes, new OXError("APP", 999));
    }

    public void testShouldCreateYearlyIntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);
        changes.put(Appointment.MONTH, Calendar.JANUARY);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
    }

    public void testShouldCreateYearly2IntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
    }

    public void testShouldFailCreatingIntervalWithoutIntervalInformation() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.DAILY);

        negativeAssertionOnUpdate.check(changes, new OXError("APP", 999));
    }

}
