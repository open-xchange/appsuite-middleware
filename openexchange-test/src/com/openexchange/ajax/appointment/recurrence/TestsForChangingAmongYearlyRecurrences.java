/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.appointment.recurrence;

import static com.openexchange.java.Autoboxing.I;
import org.junit.Test;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForChangingAmongYearlyRecurrences extends ManagedAppointmentTest {

    public TestsForChangingAmongYearlyRecurrences() {
        super();
    }

    @Test
    public void testShouldChangeFromYearly1ToYearly2() {
        Appointment app = generateYearlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, I(Appointment.YEARLY));
        changes.put(Appointment.INTERVAL, I(1));
        changes.put(Appointment.DAY_IN_MONTH, I(1));
        changes.put(Appointment.MONTH, I(1));
        changes.put(Appointment.DAYS, I(Appointment.MONDAY)); // this is the actual change

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    @Test
    public void testShouldFailChangingFromYearly1ToYearly2UsingOnlyAdditionalData() {
        Appointment app = generateYearlyAppointment();

        Changes changes = new Changes();
        /**
         * TODO: Fix test. It's necessary to set the recurrence type. Otherwise the Appointmen writer will treat this as a normal
         * appointment and ignore the days value.
         */
        changes.put(Appointment.RECURRENCE_TYPE, I(Appointment.YEARLY));
        changes.put(Appointment.DAYS, I(Appointment.MONDAY));

        negativeAssertionOnUpdate.check(app, changes, OXExceptionFactory.getInstance().create(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_INTERVAL));
    }

    @Test
    public void testShouldChangeFromYearly2ToYearly1With127() {
        Appointment app = generateYearlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, I(Appointment.YEARLY));
        changes.put(Appointment.DAYS, I(127)); // DUH!
        changes.put(Appointment.INTERVAL, I(1));
        changes.put(Appointment.DAY_IN_MONTH, I(1));
        changes.put(Appointment.MONTH, I(1));

        Expectations expectations = new Expectations(changes);

        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    @Test
    public void testShouldChangeFromYearly2ToYearly1WithNull() {
        Appointment app = generateYearlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, I(Appointment.YEARLY));
        changes.put(Appointment.DAYS, null);
        changes.put(Appointment.INTERVAL, I(1));
        changes.put(Appointment.DAY_IN_MONTH, I(1));
        changes.put(Appointment.MONTH, I(1));

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.DAYS, I(127));

        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    @Test
    public void testShouldFailChangingFromYearly2ToYearly1WhileMissingMonth() {
        Appointment app = generateYearlyAppointment();

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, I(Appointment.YEARLY));
        changes.put(Appointment.DAYS, I(1));
        changes.put(Appointment.INTERVAL, I(1));
        changes.put(Appointment.DAY_IN_MONTH, I(1));

        negativeAssertionOnUpdate.check(app, changes, OXCalendarExceptionCodes.RECURRING_MISSING_YEARLY_MONTH.create(I(1)));
    }

    @Test
    public void testShouldFailChangingFromYearly2ToYearly1UsingOnlyAdditionalData() {
        Appointment app = generateYearlyAppointment();

        Changes changes = new Changes();
        /**
         * TODO: Fix test. It's necessary to set the recurrence type. Otherwise the Appointmen writer will treat this as a normal
         * appointment and ignore the days value.
         */
        changes.put(Appointment.RECURRENCE_TYPE, I(Appointment.YEARLY));
        changes.put(Appointment.DAYS, I(127));

        negativeAssertionOnUpdate.check(app, changes, OXExceptionFactory.getInstance().create(OXCalendarExceptionCodes.INCOMPLETE_REC_INFOS_INTERVAL));
    }

}
