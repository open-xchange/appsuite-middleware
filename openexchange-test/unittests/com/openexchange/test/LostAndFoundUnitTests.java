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
 * A collection of unit tests that were found by find_tests_without_suites.rb There may be a more appropriate place for these, but at least
 * now they'll run when the server is built. Script: ruby find_tests_without_suites.rb ~/git/backend/openexchange-test/unittests
 * com.openexchange.test.UnitTests com.openexchange.test.I18nTests
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class LostAndFoundUnitTests {

    public static Test suite() {
        final TestSuite tests = new TestSuite();

        tests.addTest(com.openexchange.database.ConfigDBUniqueIDTest.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug7470Test.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug7732Test.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug8475.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug8527.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug8653.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug8654.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug8681forCSV.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug8681forICAL.suite());
        tests.addTest(com.openexchange.groupware.importexport.Bug8681forVCard.suite());
        tests.addTest(com.openexchange.groupware.importexport.CSVContactExportTest.suite());
        tests.addTest(com.openexchange.groupware.importexport.CSVContactImportTest.suite());
        tests.addTest(com.openexchange.groupware.importexport.CSVParserTest.suite());
        tests.addTest(com.openexchange.groupware.importexport.ICalImportTest.suite());
        tests.addTest(com.openexchange.groupware.importexport.OutlookCSVContactImportTest.suite());
        tests.addTest(com.openexchange.groupware.importexport.SizedInputStreamTest.suite());
        tests.addTestSuite(com.openexchange.folder.FolderTest.class);
        tests.addTestSuite(com.openexchange.groupware.CalendarPerformanceTests.class);
        tests.addTestSuite(com.openexchange.groupware.calendar.CalendarMoveTest.class);
        tests.addTestSuite(com.openexchange.groupware.contexts.LoginTest.class);
        tests.addTestSuite(com.openexchange.groupware.reminder.ReminderTest.class);
        tests.addTestSuite(com.openexchange.mail.MailAccessTest.class);
        tests.addTestSuite(com.openexchange.mail.MailFolderTest.class);
        tests.addTestSuite(com.openexchange.mail.MailJSONHandlerTest.class);
        tests.addTestSuite(com.openexchange.mail.MailLogicToolsTest.class);
        tests.addTestSuite(com.openexchange.mail.MailMessageTest.class);
        tests.addTestSuite(com.openexchange.mail.MailParserWriterTest.class);
        tests.addTestSuite(com.openexchange.mail.folderstorage.MailFolderSpecialCharsTest.class);
        tests.addTestSuite(com.openexchange.mail.messagestorage.MailRFC2231Test.class);
        tests.addTestSuite(com.openexchange.resource.ResourceCreateTest.class);
        tests.addTestSuite(com.openexchange.resource.ResourceDeleteTest.class);
        tests.addTestSuite(com.openexchange.resource.ResourceUpdateTest.class);
        tests.addTestSuite(com.openexchange.server.ComplexDBPoolTest.class);
        tests.addTestSuite(com.openexchange.server.SimpleDBPoolTest.class);
        tests.addTestSuite(com.openexchange.tools.oxfolder.OXFolderDeleteListenerTest.class);
        tests.addTestSuite(com.openexchange.tools.regex.ParseCookiesTest.class);
        return tests;
    }
}
