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

package com.openexchange.test;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * A collection of interface tests that have been found by find_tests_without_suites.rb
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class LostAndFoundInterfaceTests {

    public static Test suite() {
        final TestSuite tests = new TestSuite();

    	tests.addTestSuite(com.openexchange.ajax.AJAXFileUploadTest.class);
    	tests.addTestSuite(com.openexchange.ajax.ConfigJumpTest.class);
    	tests.addTestSuite(com.openexchange.ajax.MailTest.class);
    	tests.addTestSuite(com.openexchange.ajax.TestFolder.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.ResolveUidTest.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.bugtests.Bug19500Test_NewAppointmentRequestWeirdBehaviour.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.bugtests.Bug4497Test_SharedAppDeletedByParticipant.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.bugtests.Bug5144Test_UserGetsRemovedFromParticipantList.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.bugtests.Bug7883Test_ReminderIsSyncedAndCrashesOutlook.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.recurrence.Bug12212Test.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.recurrence.Bug12280Test.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.recurrence.Bug12495Test.class);
    	tests.addTestSuite(com.openexchange.ajax.appointment.recurrence.Bug12496Test.class);
    	tests.addTestSuite(com.openexchange.ajax.contact.Bug18862Test.class);
    	tests.addTestSuite(com.openexchange.ajax.contact.Bug19543Test_DeletingContactsInDistributionList.class);
    	tests.addTestSuite(com.openexchange.ajax.contact.Bug19984Test.class);
    	tests.addTestSuite(com.openexchange.ajax.contact.ChangePrimaryMailTest.class);
    	tests.addTestSuite(com.openexchange.ajax.contact.GetTest.class);
    	tests.addTestSuite(com.openexchange.ajax.contact.ManagedSearchTests.class);
    	tests.addTestSuite(com.openexchange.ajax.contact.TermSearchTest.class);
    	tests.addTestSuite(com.openexchange.ajax.conversion.Bug20758Test.class);
    	tests.addTestSuite(com.openexchange.ajax.conversion.ICalMailPartImportTest.class);
    	tests.addTestSuite(com.openexchange.ajax.conversion.IMipImportTest.class);
    	tests.addTestSuite(com.openexchange.ajax.conversion.VCardMailPartImportTest.class);
    	tests.addTestSuite(com.openexchange.ajax.folder.api2.Bug15672Test.class);
    	tests.addTestSuite(com.openexchange.ajax.folder.api2.Bug16284Test.class);
    	tests.addTestSuite(com.openexchange.ajax.folder.api2.SubscribeTest.class);
    	tests.addTestSuite(com.openexchange.ajax.importexport.Bug17392Test.class);
    	tests.addTestSuite(com.openexchange.ajax.importexport.Bug20360Test_UmlautBreaksImport.class);
    	tests.addTestSuite(com.openexchange.ajax.importexport.Bug20738Test.class);
    	tests.addTestSuite(com.openexchange.ajax.importexport.ICalExportTest.class);
    	tests.addTestSuite(com.openexchange.ajax.importexport.ICalImportExportServletTest.class);
    	tests.addTestSuite(com.openexchange.ajax.importexport.VCardImportExportServletTest.class);
    	tests.addTestSuite(com.openexchange.ajax.infostore.test.CreateAndDeleteInfostoreTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.AllRequestAndResponseTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.AttachmentTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.Bug12409Test.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.Bug14234Test.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.CopyMailWithManagerTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.CopyTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.NewMailTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.ThreadSortTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.addresscollector.ConfigurationTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.addresscollector.MailTest.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.netsol.NetsolTestEmptyTrash.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.netsol.NetsolTestMailInbox.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.netsol.NetsolTestMailInboxSort.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.netsol.NetsolTestMailMessageDelete.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.netsol.NetsolTestMailMessageOpen.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.netsol.NetsolTestMailMessageSend.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.netsol.NetsolTestMailMessageSendAttachment.class);
    	tests.addTestSuite(com.openexchange.ajax.mail.netsol.NetsolTestViewFolders.class);
    	tests.addTestSuite(com.openexchange.ajax.passwordchange.PasswordChangeUpdateAJAXTest.class);
    	tests.addTestSuite(com.openexchange.ajax.session.AutologinTest.class);
    	tests.addTestSuite(com.openexchange.ajax.spellcheck.CheckTest.class);
    	tests.addTestSuite(com.openexchange.ajax.spellcheck.ListTest.class);
    	tests.addTestSuite(com.openexchange.ajax.spellcheck.SuggestionsTest.class);
    	tests.addTestSuite(com.openexchange.ajax.spellcheck.UserWordTest.class);
    	tests.addTestSuite(com.openexchange.ajax.task.BasicManagedTaskTests.class);
    	tests.addTestSuite(com.openexchange.ajax.task.Bug10941Test.class);
    	tests.addTestSuite(com.openexchange.ajax.task.Bug14450Test.class);
    	tests.addTestSuite(com.openexchange.ajax.task.Bug21026Test.class);
    	tests.addTestSuite(com.openexchange.ajax.voipnow.CallReportTest.class);
    	tests.addTestSuite(com.openexchange.ajax.voipnow.ClientDetailsTest.class);
    	tests.addTestSuite(com.openexchange.ajax.voipnow.ExtensionDetailsTest.class);
    	tests.addTestSuite(com.openexchange.ajax.voipnow.NewCallTest.class);
    	tests.addTestSuite(com.openexchange.push.udp.PushResponseTest.class);
    	tests.addTestSuite(com.openexchange.push.udp.RegisterTest.class);
    	tests.addTestSuite(com.openexchange.test.osgi.BundleTestAuthentication.class);
    	tests.addTestSuite(com.openexchange.test.osgi.BundleTestCache.class);
    	tests.addTestSuite(com.openexchange.test.osgi.BundleTestConfigJump.class);
    	tests.addTestSuite(com.openexchange.test.osgi.BundleTestConfiguration.class);
    	tests.addTestSuite(com.openexchange.test.osgi.BundleTestEventAdmin.class);
    	tests.addTestSuite(com.openexchange.test.osgi.BundleTestIMAP.class);
    	tests.addTestSuite(com.openexchange.test.osgi.BundleTestSMTP.class);
    	tests.addTestSuite(com.openexchange.test.osgi.BundleTestSessionD.class);
    	tests.addTestSuite(com.openexchange.webdav.FreeBusyTest.class);
    	tests.addTestSuite(com.openexchange.webdav.ICalTest.class);
    	tests.addTestSuite(com.openexchange.webdav.TestUpload2.class);
    	tests.addTestSuite(com.openexchange.webdav.client.EmptyLockTest.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.appointment.Bug12338Test.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.appointment.Bug19014Test_HugeCalendarsProvokeOOM.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.appointment.FreeBusyTest.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.appointment.PermissionTest.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.appointment.recurrence.Bug10859Test2.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.appointment.recurrence.ChangeExceptionTest.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.contact.Bug15051Test.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.contact.PermissionTest.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.folder.PermissionTest.class);
    	tests.addTestSuite(com.openexchange.webdav.xml.task.PermissionTest.class);

        return tests;
    }
}
