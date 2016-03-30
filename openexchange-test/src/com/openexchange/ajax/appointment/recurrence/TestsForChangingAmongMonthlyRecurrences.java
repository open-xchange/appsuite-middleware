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

import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForChangingAmongMonthlyRecurrences extends ManagedAppointmentTest {

    public TestsForChangingAmongMonthlyRecurrences(String name) {
        super(name);
    }

    private Changes generateMonthlyChanges() {
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);
        return changes;
    }

    public void testShouldChangeFromMonthly1ToMonthly2WhenCreating() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = generateMonthlyChanges();
        changes.put(Appointment.DAYS, Appointment.MONDAY); // this is the actual change

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    public void testShouldChangeFromMonthly1ToMonthly2WhenUpdating() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = generateMonthlyChanges();
        changes.put(Appointment.DAYS, Appointment.MONDAY); // this is the actual change

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    public void testShouldNotFailChangingFromMonthly1ToMonthly2() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.DAYS, Appointment.MONDAY);

        positiveAssertionOnCreate.check(app, changes, new Expectations(changes));
    }

    public void testShouldFailChangingFromMonthly1ToMonthly2UsingOnlyAdditionalData() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = new Changes();
        /**
         * TODO: Fix test. It's necessary to set the recurrence type. Otherwise the Appointmen writer will treat this as a normal
         * appointment and ignore the days value.
         */
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.DAYS, Appointment.MONDAY);

        negativeAssertionOnUpdate.check(
            app,
            changes,
            OXExceptionFactory.getInstance().create(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_INTERVAL));
    }

    public void testShouldChangeFromMonthly2ToMonthly1With127DuringCreation() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.DAYS, 127); // DUH!
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreate.check(app, changes, expectations);
    }

    public void testShouldChangeFromMonthly2ToMonthly1With127WhenUpdating() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.DAYS, 127); // DUH!
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    public void testShouldChangeFromMonthly2ToMonthly1WithNullDuringCreation() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.DAYS, null);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.DAYS, 127);

        positiveAssertionOnCreate.check(app, changes, expectations);
    }

    public void testShouldChangeFromMonthly2ToMonthly1WithNullWhenUpdating() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.DAYS, null);
        changes.put(Appointment.INTERVAL, 1);
        changes.put(Appointment.DAY_IN_MONTH, 1);

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.DAYS, 127);

        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    public void testShouldFailChangingFromMonthly2ToMonthly1UsingOnlyAdditionalData() throws Exception {
        Appointment app = generateMonthlyAppointment();

        Changes changes = new Changes();
        /**
         * TODO: Fix test. It's necessary to set the recurrence type. Otherwise the Appointment writer will treat this as a normal
         * appointment and ignore the days value.
         */
        changes.put(Appointment.RECURRENCE_TYPE, Appointment.MONTHLY);
        changes.put(Appointment.DAYS, 127);

        negativeAssertionOnUpdate.check(
            app,
            changes,
            OXExceptionFactory.getInstance().create(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_INTERVAL));
    }

}
