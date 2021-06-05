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
import com.openexchange.ajax.appointment.bugtests.AppointmentBugTestSuite;
import com.openexchange.ajax.appointment.bugtests.FolderIdTestAjax;
import com.openexchange.ajax.appointment.recurrence.RecurrenceTestSuite;
import com.openexchange.test.concurrent.ParallelSuite;

@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    AllTest.class,
    ConfirmTest.class,
    ConfirmOccurrencesTest.class,
    ConfirmOthers.class,
    CopyTest.class,
    DeleteTest.class,
    GetTest.class,
    FreeBusyTest.class,
    HasTest.class,
    ListTest.class,
    MoveTest.class,
    NewTest.class,
    SearchTest.class,
    UpdateTest.class,
    UpdatesTest.class,
    UpdatesForModifiedAndDeletedTest.class,
    ConflictTest.class,
    MultipleTest.class,
    PortalSearchTest.class,
    FunambolTest.class,
    NewListTest.class,
    UserStory2173Test.class,
    CalendarTestManagerTest.class,
    UserStory1085Test.class,
    AppointmentAttachmentTests.class,
    ConfirmationsTest.class,
    SharedFoldersShowOwnersPrivateAppointmentsAsBlocks.class,
    CreatedByTest.class,
    AllAliasTest.class,
    ListAliasTest.class,
    DeleteMultipleAppointmentTest.class,
    GetChangeExceptionsTest.class,
    PrivateTests.class,
    FolderIdTestAjax.class,
    MoveTestNew.class,
    CreateExceptionWithBadDate.class,
    RecurrenceTestSuite.class,
    AppointmentBugTestSuite.class,
    NewAppointmentHttpApiTestSuite.class,
    RangeQueryTest.class

})
public class AppointmentAJAXSuite  {

}
