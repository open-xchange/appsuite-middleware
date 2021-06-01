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
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * These tests document a strange behaviour of the HTTP API: If you ask
 * a series for the nth element, you always get a freshly calculated
 * one, even if it should not exist due to a change exception there.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForUsingRecurrencePositionToGetChangeExceptions extends ManagedAppointmentTest {

    private Appointment app;
    private Changes changes;
    private Appointment update;

    public TestsForUsingRecurrencePositionToGetChangeExceptions() {
        super();
    }

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        app = generateDailyAppointment();
        app.setOccurrence(3);

        catm.insert(app);

        changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(2));
        changes.put(Appointment.START_DATE, D("2/1/2008 1:00", utc));
        changes.put(Appointment.END_DATE, D("2/1/2008 2:00", utc));

        update = app.clone();
        changes.update(update);
        catm.update(update);

    }

    @Test
    public void testShouldFindUnchangedFirstOccurrence() throws OXException {
        Appointment actual = catm.get(folder.getObjectID(), app.getObjectID(), 1);

        Expectations expectations = new Expectations();
        expectations.put(Appointment.START_DATE, D("1/1/2008 1:00"));
        expectations.put(Appointment.END_DATE, D("1/1/2008 2:00"));

        expectations.verify(actual);
    }

    @Test
    public void testShouldFindSomethingElseAsSecondOccurrenceButDoesNot() throws OXException {
        Appointment actual = catm.get(folder.getObjectID(), app.getObjectID(), 2);

        Expectations expectations = new Expectations();
        expectations.put(Appointment.START_DATE, D("2/1/2008 1:00"));
        expectations.put(Appointment.END_DATE, D("2/1/2008 2:00"));

        expectations.verify(actual);
    }

    @Test
    public void testShouldFindUnchangedLastOccurrence() throws OXException {
        Appointment actual = catm.get(folder.getObjectID(), app.getObjectID(), 3);

        Expectations expectations = new Expectations();
        expectations.put(Appointment.START_DATE, D("3/1/2008 1:00"));
        expectations.put(Appointment.END_DATE, D("3/1/2008 2:00"));

        expectations.verify(actual);

    }

}
