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



public class UnitTests {

    public static Test suite() {
        final TestSuite tests = new TestSuite();

        tests.addTestSuite(com.openexchange.groupware.infostore.URLHelperTest.class);
        tests.addTestSuite(com.openexchange.ajax.infostore.InfostoreParserTest.class);
        tests.addTestSuite(com.openexchange.ajax.infostore.InfostoreWriterTest.class);
        tests.addTestSuite(com.openexchange.ajax.infostore.JSONSimpleRequestTest.class);
        tests.addTestSuite(com.openexchange.ajax.attach.AttachmentParserTest.class);
        tests.addTestSuite(com.openexchange.ajax.attach.AttachmentWriterTest.class);
        tests.addTestSuite(com.openexchange.webdav.protocol.WebdavPathTest.class);
        tests.addTest(com.openexchange.webdav.xml.writer.WriterSuite.suite());
        tests.addTestSuite(com.openexchange.webdav.action.IfHeaderParserTest.class);
        tests.addTestSuite(com.openexchange.webdav.protocol.util.UtilsTest.class);
        tests.addTestSuite(com.openexchange.groupware.results.AbstractTimedResultTest.class);

        tests.addTestSuite(com.openexchange.groupware.CalendarTest.class);
        tests.addTestSuite(com.openexchange.groupware.CalendarRecurringTests.class);
        tests.addTestSuite(com.openexchange.groupware.AppointmentBugTests.class);

        tests.addTest(com.openexchange.groupware.calendar.calendarsqltests.CalendarSqlTestSuite.suite());
        tests.addTestSuite(com.openexchange.groupware.calendar.ConflictHandlerTest.class);
        tests.addTestSuite(com.openexchange.groupware.calendar.CalendarDowngradeUserTest.class);
        tests.addTestSuite(com.openexchange.groupware.calendar.RecurringCalculationTest.class);

        tests.addTestSuite(com.openexchange.tools.file.SaveFileActionTest.class);
        tests.addTestSuite(com.openexchange.tools.update.IndexTest.class);

        tests.addTestSuite(com.openexchange.tools.io.SizeAwareInputStreamTest.class);

        tests.addTestSuite(com.openexchange.groupware.attach.actions.CreateAttachmentsActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.attach.actions.UpdateAttachmentsActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.attach.actions.RemoveAttachmentsActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.attach.actions.FireAttachedEventActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.attach.actions.FireDetachedEventActionTest.class);

        tests.addTestSuite(com.openexchange.groupware.infostore.URLHelperTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.InfostoreDeleteTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.PropertyStoreTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.EntityLockManagerTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.InfostoreFacadeTest.class);
        tests.addTestSuite(com.openexchange.groupware.folder.FolderTreeUtilTest.class);
        tests.addTestSuite(com.openexchange.groupware.folder.FolderLockManagerTest.class);

        tests.addTestSuite(com.openexchange.groupware.infostore.AbstractDocumentListActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.CreateDocumentActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.CreateVersionActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.UpdateDocumentActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.UpdateVersionActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.DeleteDocumentActionTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.DeleteVersionActionTest.class);

        tests.addTestSuite(com.openexchange.groupware.infostore.validation.ValidationChainTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.validation.InfostoreInvalidCharactersCheckTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.validation.FilenamesMayNotContainSlashesValidatorTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.DelUserFolderDiscovererTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.InfostoreDowngradeTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.SearchEngineTest.class);

        tests.addTestSuite(com.openexchange.groupware.tasks.DowngradeTest.class);

        tests.addTestSuite(com.openexchange.groupware.infostore.WebdavFolderAliasesTest.class);

        tests.addTestSuite(com.openexchange.groupware.infostore.webdav.FolderCollectionPermissionHandlingTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.webdav.PermissionTest.class);

        tests.addTestSuite(com.openexchange.i18n.CompiledLineParserTemplateTest.class);
        tests.addTestSuite(com.openexchange.i18n.GettextParserTest.class);
        tests.addTestSuite(com.openexchange.i18n.impl.FileDiscovererTest.class);

        tests.addTestSuite(com.openexchange.groupware.attach.AttachmentBaseTest.class);

        tests.addTestSuite(com.openexchange.data.conversion.ical.ICalParserBasicTests.class);
        tests.addTestSuite(com.openexchange.data.conversion.ical.ICalParserBugTests.class);
        tests.addTest(com.openexchange.data.conversion.ical.bugs.UnitTests.suite());
        tests.addTestSuite(com.openexchange.data.conversion.ical.ICalEmitterTest.class);
        tests.addTest(com.openexchange.groupware.importexport.ImportExportStandaloneSuite.suite());
        tests.addTestSuite(com.openexchange.groupware.importexport.importers.Bug12380RecoveryParserTest.class);

        tests.addTestSuite(com.openexchange.webdav.action.behaviour.RequestSpecificBehaviourRegistryTest.class);
        tests.addTestSuite(com.openexchange.webdav.action.behaviour.UserAgentBehaviourTest.class);

        tests.addTestSuite(com.openexchange.webdav.action.ApacheURLDecoderTest.class);

        tests.addTestSuite(com.openexchange.groupware.attach.CopyAttachmentsForChangeExceptionsTest.class);

        tests.addTestSuite(com.openexchange.groupware.container.DataObjectTest.class);
        tests.addTestSuite(com.openexchange.groupware.container.FolderChildObjectTest.class);
        tests.addTestSuite(com.openexchange.groupware.container.FolderObjectTest.class);
        tests.addTestSuite(com.openexchange.groupware.container.CommonObjectTest.class);
        tests.addTestSuite(com.openexchange.groupware.container.TaskTest.class);
        tests.addTestSuite(com.openexchange.groupware.container.CalendarObjectTest.class);
        tests.addTestSuite(com.openexchange.groupware.container.AppointmentObjectTest.class);
        tests.addTestSuite(com.openexchange.groupware.container.ContactObjectTest.class);

        tests.addTestSuite(com.openexchange.groupware.contact.ContactMergeratorTest.class);
        tests.addTestSuite(com.openexchange.groupware.contact.UseCountGlobalFirstComparatorTest.class);
        tests.addTestSuite(com.openexchange.groupware.contact.DefaultContactComparatorTest.class);

        tests.addTestSuite(com.openexchange.sessiond.SessiondTest.class);
        tests.addTestSuite(com.openexchange.groupware.infostore.PathResolverTest.class);
        tests.addTestSuite(com.openexchange.webdav.infostore.integration.DropBoxScenarioTest.class);
        tests.addTestSuite(com.openexchange.webdav.infostore.integration.LockExpiryTest.class);

        tests.addTestSuite(com.openexchange.l10n.SuperCollatorTest.class);
        tests.addTest(com.openexchange.pubsub.TemplateTestSuite.suite());

        // Slow Tests
        tests.addTest(com.openexchange.webdav.protocol.ProtocolTestSuite.suite());
        tests.addTest(com.openexchange.webdav.action.ActionTestSuite.suite());
        tests.addTestSuite(com.openexchange.groupware.calendar.SlowCalendarTests.class);
        tests.addTestSuite(com.openexchange.tools.file.FileStorageTest.class);
        // Pretty outdated since v6.18.0
        // tests.addTestSuite(com.openexchange.tools.file.FileStorageThreadTest.class);
        tests.addTestSuite(com.openexchange.tools.file.QuotaFileStorageTest.class);
        tests.addTestSuite(com.openexchange.groupware.IDGeneratorTest.class);
//        tests.addTest(com.openexchange.test.LostAndFoundUnitTests.suite());

        // Mail
        tests.addTest(com.openexchange.mail.MailAPITestSuite.suite());

        //User tests
        tests.addTest(com.openexchange.user.UserTestSuite.suite());

        // SMS tests
        tests.addTest(com.openexchange.sms.SMSTestSuite.suite());

        return tests;
    }
}
