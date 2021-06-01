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

package com.openexchange.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * A collection of interface tests that have been found by find_tests_without_suites.rb
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    // TODO Fix or remove disabled tests
    com.openexchange.ajax.MailTest.class,
//    com.openexchange.ajax.appointment.bugtests.Bug19500Test_NewAppointmentRequestWeirdBehaviour.class,
    com.openexchange.ajax.appointment.bugtests.Bug4497Test_SharedAppDeletedByParticipant.class,
    com.openexchange.ajax.appointment.bugtests.Bug5144Test_UserGetsRemovedFromParticipantList.class,
    com.openexchange.ajax.appointment.bugtests.Bug7883Test_ReminderIsSyncedAndCrashesOutlook.class,
//    com.openexchange.ajax.appointment.recurrence.Bug12212Test.class,
    com.openexchange.ajax.appointment.recurrence.Bug12280Test.class,
    com.openexchange.ajax.appointment.recurrence.Bug12495Test.class,
    com.openexchange.ajax.appointment.recurrence.Bug12496Test.class,
//    com.openexchange.ajax.contact.Bug18862Test.class,
//    com.openexchange.ajax.contact.Bug19543Test_DeletingContactsInDistributionList.class,
    com.openexchange.ajax.contact.Bug19984Test.class,
    com.openexchange.ajax.contact.GetTest.class,
    com.openexchange.ajax.contact.ManagedSearchTests.class,
    com.openexchange.ajax.folder.api2.Bug15672Test.class,
    com.openexchange.ajax.folder.api2.Bug16284Test.class,
    com.openexchange.ajax.folder.api2.SubscribeTest.class,
//    com.openexchange.ajax.importexport.Bug17392Test.class,
    com.openexchange.ajax.importexport.Bug20360Test_UmlautBreaksImport.class,
//    com.openexchange.ajax.importexport.Bug20738Test.class,
//    com.openexchange.ajax.importexport.ICalImportExportServletTest.class,
    com.openexchange.ajax.importexport.VCardImportExportServletTest.class,
    com.openexchange.ajax.infostore.test.CreateAndDeleteInfostoreTest.class,
    com.openexchange.ajax.mail.AllRequestAndResponseTest.class,
    com.openexchange.ajax.mail.AttachmentTest.class,
    com.openexchange.ajax.mail.Bug12409Test.class,
    com.openexchange.ajax.mail.Bug14234Test.class,
    com.openexchange.ajax.mail.CopyMailWithManagerTest.class,
    com.openexchange.ajax.mail.NewMailTest.class,
//    com.openexchange.ajax.mail.addresscollector.ConfigurationTest.class,
//    com.openexchange.ajax.mail.addresscollector.MailTest.class,
    com.openexchange.ajax.passwordchange.PasswordChangeUpdateAJAXTest.class,
//    com.openexchange.ajax.session.AutologinTest.class,
    com.openexchange.ajax.task.BasicManagedTaskTests.class,
    com.openexchange.ajax.task.Bug10941Test.class,
    com.openexchange.ajax.task.Bug14450Test.class,
    com.openexchange.ajax.task.Bug21026Test.class,
})
public class LostAndFoundInterfaceTests {
}
