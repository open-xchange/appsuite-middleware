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

package com.openexchange.ajax.importexport;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Test suite for iCal tests.
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({ // @formatter:off
    ICalTaskExportTest.class,
    ICalAppointmentExportTest.class,
    ICalSeriesTests.class,
    ICalSingleAndBatchExportTest.class,
    Bug9840Test.class,
    Bug11724Test.class,
    Bug11868Test.class,
    Bug11871Test.class,
    Bug11920Test.class,
    Bug11996Test.class,
    Bug12414Test.class,
    Bug12470Test.class,
    Bug17393Test.class,
    Bug17963Test_DateWithoutTime.class,
    Bug19046Test_SeriesWithExtraneousStartDate.class,
    Bug19089Test.class,
    Bug19681_TimezoneForUtcProperties.class,
    Bug20132Test_WrongRecurrenceDatePosition.class,
    Bug20405Test_TaskWithoutDueDate.class,
    Bug20413Test_CompletelyWrongDTStart.class,
    Bug20453Test_emptyDTEND.class,
    Bug20498Test_ReminderJumpsAnHour.class,
    Bug20715Test_UidIsNotcaseSensitive.class,
    Bug20718Test_JumpDuringDstCrossing.class,
    Bug20896Test_AlarmsChange.class,
    Bug20945Test_UnexpectedError26.class,
    Bug22059Test.class,
    Bug27474Test.class,
    Bug28071Test.class,
    Bug56435Test_TaskStateRoundtrip.class,
    Bug8475Test_TaskAttendeeHandling.class,
    Bug8654Test_TaskImport.class,
    Bug63867Test.class,
    MWB161Test.class,
    MWB464Test.class,
    MWB805Test.class,
}) // @formatter:on
public final class ICalTestSuite {
}
