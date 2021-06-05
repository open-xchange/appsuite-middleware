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

package com.openexchange.ajax.appointment;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.ajax.appointment.recurrence.TestsForChangingAmongMonthlyRecurrences;
import com.openexchange.ajax.appointment.recurrence.TestsForChangingAmongYearlyRecurrences;
import com.openexchange.ajax.appointment.recurrence.TestsForCreatingChangeExceptions;
import com.openexchange.ajax.appointment.recurrence.TestsForDeleteExceptionsAndFixedEndsOfSeries;
import com.openexchange.ajax.appointment.recurrence.TestsForDifferentWaysOfEndingASeries;
import com.openexchange.ajax.appointment.recurrence.TestsForModifyingChangeExceptions;
import com.openexchange.ajax.appointment.recurrence.TestsForUsingRecurrencePositionToGetChangeExceptions;
import com.openexchange.ajax.appointment.recurrence.TestsToCreateMinimalAppointmentSeries;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Suite for systematic tests to check the expected behaviour
 * of the HTTP API for the calendar.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    TestsToCreateMinimalAppointmentSeries.class,
    TestsForChangingAmongMonthlyRecurrences.class,
    TestsForChangingAmongYearlyRecurrences.class,
    TestsForDeleteExceptionsAndFixedEndsOfSeries.class,
    TestsForCreatingChangeExceptions.class,
    TestsForUsingRecurrencePositionToGetChangeExceptions.class,
    TestsForDifferentWaysOfEndingASeries.class,
    TestsForModifyingChangeExceptions.class,

})
public class NewAppointmentHttpApiTestSuite  {

}
