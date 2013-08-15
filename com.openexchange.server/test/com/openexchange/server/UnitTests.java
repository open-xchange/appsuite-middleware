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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import com.openexchange.ajax.LoginAddFragmentTest;
import com.openexchange.ajax.MailAttachmentTest;
import com.openexchange.ajax.parser.ContactSearchtermSqlConverterTest;
import com.openexchange.ajax.parser.TaskLastModifiedTest;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRendererTest;
import com.openexchange.groupware.ldap.UserAttributeDiffTest;
import com.openexchange.groupware.notify.ParticipantNotifyTest;
import com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTableTest;
import com.openexchange.groupware.userconfiguration.UserConfigurationTest;
import com.openexchange.i18n.tools.replacement.TaskEndDateReplacementTest;
import com.openexchange.mail.mime.MimeSmilFixerTest;
import com.openexchange.mail.mime.MimeStructureFixerTest;
import com.openexchange.mail.utils.MsisdnUtilityTest;
import com.openexchange.tools.collections.OXCollectionsTest;
import com.openexchange.tools.iterator.SearchIteratorDelegatorTest;
import com.openexchange.tools.net.URIParserTest;

/**
 * {@link UnitTests}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(Suite.class)
@SuiteClasses({
    ContactSearchtermSqlConverterTest.class,
    TaskLastModifiedTest.class,
    LoginAddFragmentTest.class,
    UserAttributeDiffTest.class,
    TaskEndDateReplacementTest.class,
    OXCollectionsTest.class,
    SearchIteratorDelegatorTest.class,
    URIParserTest.class,
    MsisdnUtilityTest.class,
    MakeFolderIdPrimaryForDelContactsTableTest.class,
    MailAttachmentTest.class,
    FileResponseRendererTest.class,
    MsisdnUtilityTest.class,
    UserConfigurationTest.class,
    MimeStructureFixerTest.class,
    MimeSmilFixerTest.class,
    ParticipantNotifyTest.class
})
public class UnitTests {

    /**
     * Initializes a new {@link UnitTests}.
     */
    public UnitTests() {
        super();
    }

}
