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

package com.openexchange.ajax.appointment.recurrence;

import java.util.Calendar;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * Find out which parameters are needed to create an appointment and which are not enough.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsToCreateMinimalAppointmentSeries extends ManagedAppointmentTest {

    public TestsToCreateMinimalAppointmentSeries(String name) {
        super(name);
    }

    public void _testShouldFailWhenSendingUnneccessaryDayInformationForDailyAppointment() throws Exception {
        /*
         * TODO: Fix!
         * This test fails as long as the Server side JSON-Writer/Parser is used.
         *
         * Details:
         * This does not send an exception, because the current JSON-writer does not set the days-value as it is not necessary for daily recurrence.
         */
        fail("Fails until an independent parser/writer is used for creating JSON-Objects");
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.DAILY);
        changes.put(Appointment.RECURRENCE_COUNT, 7);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAYS, 127);

        negativeAssertionOnCreate.check(changes, new OXException(998));
        negativeAssertionOnUpdate.check(changes, new OXException(999));
    }

    public void testShouldCreateDailyIntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.DAILY);
        changes.put(Appointment.INTERVAL, 1);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(changes, new Expectations(changes));
    }

    public void testShouldCreateWeeklyIntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.WEEKLY);
        changes.put(Appointment.INTERVAL, 1);

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.DAYS, 127); // Should default to 127 as per HTTP API

        positiveAssertionOnCreate.check(changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(changes, new Expectations(changes));
    }

    public void testShouldCreateWeeklyIntervalWithDaysFieldDifferentThan127() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.WEEKLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY + Appointment.TUESDAY);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(changes, new Expectations(changes));
    }

    public void testShouldFailCreatingMonthlyIntervalWithoutDayInMonthInfo() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.INTERVAL, 1);

        negativeAssertionOnCreate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create());
        negativeAssertionOnUpdate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create());
    }

    public void testShouldFailCreatingMonthly2IntervalWithoutDayInMonthInfo() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY);

        negativeAssertionOnCreate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create());
        negativeAssertionOnUpdate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create());
    }

    // first day every month
    public void testShouldCreateMonthlyIntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(changes, new Expectations(changes));
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
        positiveAssertionOnCreateAndUpdate.check(changes, new Expectations(changes));
    }

    public void testShouldFailCreatingYearlyIntervalWithoutDayInMonthInfo() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);

        negativeAssertionOnCreate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create());
        negativeAssertionOnUpdate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create());
    }

    public void testShouldFailCreatingYearly2IntervalWithoutDayInMonthInfo() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY);

        negativeAssertionOnCreate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create());
        negativeAssertionOnUpdate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_MONTHDAY.create());
    }

    public void testShouldFailCreatingYearlyIntervalWithoutMonth() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);

        negativeAssertionOnCreate.check(changes, OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_MONTH.create(1));
        negativeAssertionOnUpdate.check(changes, OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_MONTH.create(1));
    }

    public void testShouldCreateYearlyIntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);
        changes.put(Appointment.MONTH, Calendar.JANUARY);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(changes, new Expectations(changes));
    }

    public void testShouldCreateYearly2IntervalWithMinimalData() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.YEARLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);
        changes.put(Appointment.DAYS, Appointment.MONDAY);
        changes.put(Appointment.MONTH, Calendar.JANUARY);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(changes, new Expectations(changes));
    }

    public void testShouldFailCreatingIntervalWithoutIntervalInformation() throws Exception {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.DAILY);

        negativeAssertionOnCreate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_INTERVAL.create());
        negativeAssertionOnUpdate.check(changes, OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_INTERVAL.create());
    }

}
