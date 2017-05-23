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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    com.openexchange.groupware.infostore.URLHelperTest.class,
    com.openexchange.ajax.infostore.InfostoreParserTest.class,
    com.openexchange.ajax.infostore.InfostoreWriterTest.class,
    com.openexchange.ajax.infostore.JSONSimpleRequestTest.class,
    com.openexchange.ajax.attach.AttachmentParserTest.class,
    com.openexchange.ajax.attach.AttachmentWriterTest.class,
    com.openexchange.webdav.protocol.WebdavPathTest.class,
    com.openexchange.webdav.xml.writer.WriterSuite.class,
    com.openexchange.webdav.action.IfHeaderParserTest.class,
    com.openexchange.webdav.protocol.util.UtilsTest.class,
    com.openexchange.groupware.results.AbstractTimedResultTest.class,

    com.openexchange.groupware.CalendarTest.class,
    com.openexchange.groupware.CalendarRecurringTests.class,
    com.openexchange.groupware.AppointmentBugTests.class,

    com.openexchange.groupware.calendar.calendarsqltests.CalendarSqlTestSuite.class,
    com.openexchange.groupware.calendar.ConflictHandlerTest.class,
    com.openexchange.groupware.calendar.CalendarDowngradeUserTest.class,
    com.openexchange.groupware.calendar.RecurringCalculationTest.class,

    com.openexchange.tools.file.SaveFileActionTest.class,
    com.openexchange.tools.update.IndexTest.class,

    com.openexchange.tools.io.SizeAwareInputStreamTest.class,

    com.openexchange.groupware.attach.actions.CreateAttachmentsActionTest.class,
    com.openexchange.groupware.attach.actions.UpdateAttachmentsActionTest.class,
    com.openexchange.groupware.attach.actions.RemoveAttachmentsActionTest.class,
    com.openexchange.groupware.attach.actions.FireAttachedEventActionTest.class,
    com.openexchange.groupware.attach.actions.FireDetachedEventActionTest.class,

    com.openexchange.groupware.infostore.URLHelperTest.class,
    com.openexchange.groupware.infostore.InfostoreDeleteTest.class,
    com.openexchange.groupware.infostore.PropertyStoreTest.class,
    com.openexchange.groupware.infostore.EntityLockManagerTest.class,
    com.openexchange.groupware.infostore.InfostoreFacadeTest.class,
    com.openexchange.groupware.folder.FolderTreeUtilTest.class,
    com.openexchange.groupware.folder.FolderLockManagerTest.class,

    com.openexchange.groupware.infostore.AbstractDocumentListActionTest.class,
    com.openexchange.groupware.infostore.CreateDocumentActionTest.class,
    com.openexchange.groupware.infostore.CreateVersionActionTest.class,
    com.openexchange.groupware.infostore.UpdateDocumentActionTest.class,
    com.openexchange.groupware.infostore.UpdateVersionActionTest.class,
    com.openexchange.groupware.infostore.DeleteDocumentActionTest.class,
    com.openexchange.groupware.infostore.DeleteVersionActionTest.class,

    com.openexchange.groupware.infostore.validation.ValidationChainTest.class,
    com.openexchange.groupware.infostore.validation.InfostoreInvalidCharactersCheckTest.class,
    com.openexchange.groupware.infostore.validation.FilenamesMayNotContainSlashesValidatorTest.class,
    com.openexchange.groupware.infostore.DelUserFolderDiscovererTest.class,
    com.openexchange.groupware.infostore.InfostoreDowngradeTest.class,
    com.openexchange.groupware.infostore.SearchEngineTest.class,

    com.openexchange.groupware.tasks.DowngradeTest.class,

    com.openexchange.groupware.infostore.WebdavFolderAliasesTest.class,

    com.openexchange.groupware.infostore.webdav.FolderCollectionPermissionHandlingTest.class,
    com.openexchange.groupware.infostore.webdav.PermissionTest.class,

    com.openexchange.i18n.CompiledLineParserTemplateTest.class,
    com.openexchange.i18n.GettextParserTest.class,
    com.openexchange.i18n.impl.FileDiscovererTest.class,

    com.openexchange.groupware.attach.AttachmentBaseTest.class,

    com.openexchange.data.conversion.ical.ICalParserBasicTests.class,
    com.openexchange.data.conversion.ical.ICalParserBugTests.class,
    com.openexchange.data.conversion.ical.bugs.UnitTests.class,
    com.openexchange.data.conversion.ical.ICalEmitterTest.class,
    com.openexchange.groupware.importexport.ImportExportStandaloneSuite.class,
    com.openexchange.groupware.importexport.importers.Bug12380RecoveryParserTest.class,

    com.openexchange.webdav.action.behaviour.RequestSpecificBehaviourRegistryTest.class,
    com.openexchange.webdav.action.behaviour.UserAgentBehaviourTest.class,

    com.openexchange.webdav.action.ApacheURLDecoderTest.class,

    com.openexchange.groupware.attach.CopyAttachmentsForChangeExceptionsTest.class,

    com.openexchange.groupware.container.DataObjectTest.class,
    com.openexchange.groupware.container.FolderChildObjectTest.class,
    com.openexchange.groupware.container.FolderObjectTest.class,
    com.openexchange.groupware.container.CommonObjectTest.class,
    com.openexchange.groupware.container.TaskTest.class,
    com.openexchange.groupware.container.CalendarObjectTest.class,
    com.openexchange.groupware.container.AppointmentObjectTest.class,
    com.openexchange.groupware.container.ContactObjectTest.class,

    com.openexchange.groupware.contact.ContactMergeratorTest.class,
    com.openexchange.groupware.contact.UseCountGlobalFirstComparatorTest.class,
    com.openexchange.groupware.contact.DefaultContactComparatorTest.class,

    com.openexchange.sessiond.SessiondTest.class,
    com.openexchange.groupware.infostore.PathResolverTest.class,
    com.openexchange.webdav.infostore.integration.DropBoxScenarioTest.class,
    com.openexchange.webdav.infostore.integration.LockExpiryTest.class,

    com.openexchange.l10n.SuperCollatorTest.class,
    com.openexchange.pubsub.TemplateTestSuite.class,

    // Slow Tests
    com.openexchange.webdav.protocol.ProtocolTestSuite.class,
    com.openexchange.webdav.action.ActionTestSuite.class,
    com.openexchange.groupware.calendar.SlowCalendarTests.class,
    com.openexchange.tools.file.FileStorageTest.class,
    // Pretty outdated since v6.18.0
    // com.openexchange.tools.file.FileStorageThreadTest.class,
    com.openexchange.tools.file.QuotaFileStorageTest.class,
    com.openexchange.groupware.IDGeneratorTest.class,

    // Mail
    com.openexchange.mail.MailAPITestSuite.class,

    //User tests
    com.openexchange.user.UserTestSuite.class,

    // SMS tests
    com.openexchange.sms.SMSTestSuite.class,


})
public class UnitTests {

}
