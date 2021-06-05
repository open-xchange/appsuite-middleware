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
import static org.junit.Assert.assertFalse;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.calendar.OXCalendarExceptionCodes;
import com.openexchange.groupware.container.Appointment;
import com.openexchange.groupware.container.Changes;
import com.openexchange.groupware.container.Expectations;

/**
 * There are two ways to limit an appointment series: One is by date, one is by number of
 * occurrences. Currently, removal of an occurrence by creating a delete exception does
 * not reduce the number of occurrences, also called "recurrence_count".
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class TestsForDeleteExceptionsAndFixedEndsOfSeries extends ManagedAppointmentTest {

    public TestsForDeleteExceptionsAndFixedEndsOfSeries() {
        super();
    }

    @Test
    public void testShouldNotReduceNumberOfOccurrencesWhenDeletingOneInYearlySeries() {
        Appointment app = generateYearlyAppointment();
        app.setOccurrence(5);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(5));

        Expectations expectations = new Expectations();
        expectations.put(Appointment.RECURRENCE_COUNT, I(5));
        expectations.put(Appointment.UNTIL, null); //tricky decision whether this should be set or not

        positiveAssertionOnDeleteException.check(app, changes, expectations);
    }

    @Test
    public void testShouldFailWhenDeletingBeyondScopeOfSeriesInYearlySeries() {
        Appointment app = generateYearlyAppointment();
        app.setOccurrence(5);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(6));

        try {
            negativeAssertionOnDeleteException.check(app, changes, new OXException(11));
        } catch (AssertionError e) {
            negativeAssertionOnDeleteException.check(app, changes, OXCalendarExceptionCodes.UNKNOWN_RECURRENCE_POSITION.create(e));
        }
    }

    @Test
    public void testShouldNotReduceNumberOfOccurrencesWhenDeletingOneInMonthlySeries() {
        Appointment app = generateMonthlyAppointment();
        app.setOccurrence(6);

        Changes changes = new Changes();
        changes.put(Appointment.RECURRENCE_POSITION, I(6));

        Expectations expectations = new Expectations();
        expectations.put(Appointment.RECURRENCE_COUNT, I(6));

        positiveAssertionOnDeleteException.check(app, changes, expectations);
    }

    @Test
    public void testShouldRemoveWholeSeriesIfEverySingleOccurrenceIsDeleted() {
        Appointment app = generateMonthlyAppointment();
        int numberOfOccurences = 3;
        app.setOccurrence(numberOfOccurences);

        catm.insert(app);

        for (int i = 0; i < numberOfOccurences; i++) {
            catm.createDeleteException(app, i + 1);
            assertFalse("Should not fail while creating delete exception #" + i, catm.hasLastException());
        }
    }

}
