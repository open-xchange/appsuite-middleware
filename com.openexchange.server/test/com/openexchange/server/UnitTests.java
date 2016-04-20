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

package com.openexchange.server;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import com.openexchange.ajax.requesthandler.oauth.OAuthDispatcherServletTest;

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
    com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTableTest.class,
    com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRendererTest.class,
    com.openexchange.groupware.userconfiguration.AllowAllUserConfigurationTest.class,
    com.openexchange.groupware.userconfiguration.UserConfigurationTest.class,
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
    com.openexchange.mail.json.actions.GetAttachmentActionTest.class,
    com.openexchange.ajax.requesthandler.converters.preview.cache.FileStoreResourceCacheImplTest.class,
    com.openexchange.server.services.SharedInfostoreJSlobTest.class,
    com.openexchange.groupware.upload.quotachecker.MailUploadQuotaCheckerTest.class,
    com.openexchange.mail.text.TextProcessingTest.class,
    com.openexchange.login.internal.format.CompositeLoginFormatterTest.class,
    com.openexchange.user.UserServiceInterceptorRegistryTest.class,
    com.openexchange.groupware.infostore.search.impl.ToMySqlQueryVisitorTest.class,
    com.openexchange.mail.text.HtmlProcessingTest.class,
    com.openexchange.mailaccount.json.actions.ValidateActionTest.class,
    com.openexchange.ajax.requesthandler.converters.preview.PreviewThumbResultConverterTest.class,
    com.openexchange.mail.json.actions.GetMultipleAttachmentActionTest.class,
    com.openexchange.mail.json.parser.MessageParserTest.class,
    OAuthDispatcherServletTest.class,
    com.openexchange.passwordchange.DefaultBasicPasswordChangeServiceTest.class,
    com.openexchange.mailaccount.UnifiedInboxUIDTest.class,
    com.openexchange.mailaccount.utils.HostListTest.class
})
public class UnitTests {

    public UnitTests() {
        super();
    }

}
