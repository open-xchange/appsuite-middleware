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

package com.openexchange.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.ajax.requesthandler.oauth.OAuthDispatcherServletTest;
import com.openexchange.lock.impl.AccessControlImplTest;

/**
 * Suite for integrated unit tests of the com.openexchange.server bundle.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(Suite.class)
@SuiteClasses({
    com.openexchange.ajax.ProcessUploadStaticTest.class,
    com.openexchange.ajax.parser.TaskLastModifiedTest.class,
    com.openexchange.ajax.writer.DataWriterTest.class,
    com.openexchange.ajax.writer.TaskWriterTest.class,
    com.openexchange.ajax.LoginAddFragmentTest.class,
    com.openexchange.groupware.ldap.UserAttributeDiffTest.class,
    com.openexchange.groupware.ldap.Bug33891Test.class,
    com.openexchange.i18n.tools.replacement.TaskEndDateReplacementTest.class,
    com.openexchange.i18n.tools.replacement.FormatLocalizedStringReplacementTest.class,
    com.openexchange.login.internal.LoginPerformerTest.class,
    com.openexchange.tools.collections.OXCollectionsTest.class,
    com.openexchange.tools.iterator.SearchIteratorDelegatorTest.class,
    com.openexchange.tools.net.URIParserTest.class,
    com.openexchange.mail.utils.MsisdnUtilityTest.class,
    com.openexchange.groupware.userconfiguration.AllowAllUserConfigurationTest.class,
    com.openexchange.groupware.userconfiguration.UserConfigurationTest.class,
    com.openexchange.groupware.userconfiguration.PermissionConfigurationCheckerTest.class,
    com.openexchange.mail.api.MailConfigTest.class,
    com.openexchange.mail.mime.ContentDispositionTest.class,
    com.openexchange.mail.mime.ContentTypeTest.class,
    com.openexchange.mail.mime.MimeUtilityTest.class,
    com.openexchange.mail.mime.MimeStructureFixerTest.class,
    com.openexchange.mail.mime.MimeSmilFixerTest.class,
    com.openexchange.mail.mime.QuotedInternetAddressTest.class,
    com.openexchange.mail.mime.utils.MimeMessageUtilityTest.class,
    com.openexchange.mail.parser.handlers.JsonMessageHandlerTest.class,
    com.openexchange.groupware.notify.ParticipantNotifyTest.class,
    com.openexchange.groupware.tasks.mapping.ParticipantsTest.class,
    com.openexchange.mail.json.actions.GetAttachmentActionTest.class,
    com.openexchange.ajax.requesthandler.converters.preview.cache.FileStoreResourceCacheImplTest.class,
    com.openexchange.server.services.SharedInfostoreJSlobTest.class,
    com.openexchange.groupware.upload.quotachecker.MailUploadQuotaCheckerTest.class,
    com.openexchange.mail.text.TextProcessingTest.class,
    com.openexchange.login.internal.format.CompositeLoginFormatterTest.class,
    com.openexchange.user.interceptor.UserServiceInterceptorRegistryTest.class,
    com.openexchange.groupware.infostore.search.impl.ToMySqlQueryVisitorTest.class,
    com.openexchange.mail.text.HtmlProcessingTest.class,
    com.openexchange.mailaccount.json.actions.ValidateActionTest.class,
    com.openexchange.ajax.requesthandler.converters.preview.PreviewThumbResultConverterTest.class,
    com.openexchange.mail.json.actions.GetMultipleAttachmentActionTest.class,
    com.openexchange.mail.json.parser.MessageParserTest.class,
    OAuthDispatcherServletTest.class,
    com.openexchange.mail.config.Bug38266Test.class,
    com.openexchange.mailaccount.UnifiedInboxUIDTest.class,
    com.openexchange.sessiond.impl.IPRangeTest.class,
    com.openexchange.ajax.requesthandler.AJAXRequestDataTest.class,
    com.openexchange.mail.usersetting.CachingUserSettingMailStorageTest.class,
    com.openexchange.folderstorage.internal.ConfiguredDefaultPermissionsTest.class,
    com.openexchange.groupware.contact.Bug53690Test.class,
    com.openexchange.groupware.contact.Bug59522Test.class,
    com.openexchange.folderstorage.internal.performers.UserSharedFoldersPerformerTest.class,
    com.openexchange.folderstorage.database.DatabaseFolderTest.class,
    com.openexchange.config.admin.internal.HideAdminServiceImplTest.class,
    AccessControlImplTest.class,
    com.openexchange.i18n.tools.replacement.AuthorizationTest.class,
    com.openexchange.groupware.filestore.MWB1009Test.class,
})
public class UnitTests {

    public UnitTests() {
        super();
    }

}
