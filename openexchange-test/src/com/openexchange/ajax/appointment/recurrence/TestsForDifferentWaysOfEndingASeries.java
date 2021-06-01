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
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * OX uses different ways of limiting/ending a series: You can use an ending date or you can define a number of occurrences. This
 * information needs to be kept (you cannot just convert one into the other), because changing this series might have different
 * implications, eg.: If you move a daily series two days closer to the end date, you might lose two occurrences - if you move a series with
 * 7 occurrences, moving does not matter.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForDifferentWaysOfEndingASeries extends ManagedAppointmentTest {

    public TestsForDifferentWaysOfEndingASeries() {
        super();
    }

    @Test
    public void testShouldNotSetUntilIfOccurrencesIsUsed() throws Exception {
        Appointment app = generateDailyAppointment();
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_COUNT, I(7));
        changes.put(Appointment.RECURRENCE_TYPE, app.get(Appointment.RECURRENCE_TYPE));
        changes.put(Appointment.INTERVAL, app.get(Appointment.INTERVAL));

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.UNTIL, null);

        positiveAssertionOnCreate.check(app, changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    @Test
    public void testShouldNotSetOccurrencesIfUntilIsUsed() throws Exception {
        Appointment app = generateDailyAppointment();
        Changes changes = new Changes();
        changes.put(Appointment.UNTIL, D("7/1/2008 00:00"));
        changes.put(Appointment.RECURRENCE_TYPE, app.get(Appointment.RECURRENCE_TYPE));
        changes.put(Appointment.INTERVAL, app.get(Appointment.INTERVAL));

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.RECURRENCE_COUNT, null);

        positiveAssertionOnCreate.check(app, changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }

    @Test
    public void testShouldNotSetOccurrencesIfNothingIsSet() throws Exception {
        Appointment app = generateDailyAppointment();
        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_TYPE, app.get(Appointment.RECURRENCE_TYPE));
        changes.put(Appointment.INTERVAL, app.get(Appointment.INTERVAL));

        Expectations expectations = new Expectations(changes);
        expectations.put(Appointment.RECURRENCE_COUNT, null);
        expectations.put(Appointment.UNTIL, null);

        positiveAssertionOnCreate.check(app, changes, expectations);
        positiveAssertionOnCreateAndUpdate.check(app, changes, expectations);
    }
}
