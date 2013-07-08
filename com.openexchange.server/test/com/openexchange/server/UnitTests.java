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

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * {@link UnitTests}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class UnitTests {

    private UnitTests() {
        super();
    }

    public static Test suite() {
        final TestSuite tests = new TestSuite();
        tests.addTestSuite(com.openexchange.ajax.parser.ContactSearchtermSqlConverterTest.class);
        tests.addTestSuite(com.openexchange.ajax.parser.TaskLastModifiedTest.class);
        tests.addTestSuite(com.openexchange.ajax.LoginAddFragmentTest.class);
        tests.addTest(new JUnit4TestAdapter(com.openexchange.groupware.ldap.UserAttributeDiffTest.class));
        tests.addTest(new JUnit4TestAdapter(com.openexchange.i18n.tools.replacement.TaskEndDateReplacementTest.class));
        tests.addTestSuite(com.openexchange.tools.collections.OXCollectionsTest.class);
        tests.addTestSuite(com.openexchange.tools.iterator.SearchIteratorDelegatorTest.class);
        tests.addTest(new JUnit4TestAdapter(com.openexchange.tools.net.URIParserTest.class));
        tests.addTest(new JUnit4TestAdapter(com.openexchange.mail.utils.MsisdnUtilityTest.class));
        tests.addTest(new JUnit4TestAdapter(com.openexchange.groupware.update.tasks.MakeFolderIdPrimaryForDelContactsTableTest.class));
        tests.addTest(new JUnit4TestAdapter(com.openexchange.ajax.MailAttachmentTest.class));
        tests.addTestSuite(com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRendererTest.class);
        tests.addTest(new JUnit4TestAdapter(com.openexchange.mail.utils.MsisdnUtilityTest.class));
        tests.addTest(new JUnit4TestAdapter(com.openexchange.groupware.userconfiguration.UserConfigurationTest.class));
        return tests;
    }
}
