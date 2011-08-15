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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import com.openexchange.config.internal.ConfigurationImplTest;
import com.openexchange.consistency.ConsistencyTest;
import com.openexchange.contactcollector.ContactCollectorTest;
import com.openexchange.contactcollector.OrderByTest;
import com.openexchange.groupware.AppointmentAttachmentTest;
import com.openexchange.groupware.AppointmentDeleteNoCommit;
import com.openexchange.groupware.CalendarDeleteTest;
import com.openexchange.groupware.calendar.CalendarCommonCollectionTest;
import com.openexchange.groupware.contact.helpers.ContactMergerTest;
import com.openexchange.groupware.contacts.ContactFieldMapperTest;
import com.openexchange.groupware.importexport.CSVParserTest;
import com.openexchange.groupware.infostore.InfostoreDowngradeTest;
import com.openexchange.groupware.infostore.SearchEngineTest;
import com.openexchange.groupware.infostore.database.DocumentMetadataImplTest;
import com.openexchange.groupware.infostore.database.JSONDocumentMetadataTest;
import com.openexchange.groupware.notify.AddICalAttachmentTest;
import com.openexchange.groupware.notify.OnlyResources;
import com.openexchange.groupware.tasks.TestTaskTest;
import com.openexchange.json.JSONWriterTest;
import com.openexchange.mail.MailConverterTest;
import com.openexchange.mail.structure.MailPlainTextStructureTest;
import com.openexchange.tools.service.SpecificServiceChooserTest;
import com.openexchange.tools.update.ForeignKeyTest;


/**
 * A collection of unit tests that were found by find_tests_without_suites.rb
 * There may be a more appropriate place for these, but at least now they'll
 * run when the server is built.
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class LostAndFoundUnitTests {
    public static Test suite() {
        final TestSuite tests = new TestSuite();

        tests.addTestSuite(ForeignKeyTest.class);
        tests.addTestSuite(SpecificServiceChooserTest.class);
        tests.addTestSuite(MailPlainTextStructureTest.class);
        tests.addTestSuite(MailConverterTest.class);
        tests.addTestSuite(JSONWriterTest.class);
        tests.addTestSuite(TestTaskTest.class);
        tests.addTestSuite(OnlyResources.class);
        tests.addTestSuite(AddICalAttachmentTest.class);
        tests.addTestSuite(JSONDocumentMetadataTest.class);
        tests.addTestSuite(DocumentMetadataImplTest.class);
        tests.addTestSuite(SearchEngineTest.class);
        tests.addTestSuite(InfostoreDowngradeTest.class);
        tests.addTest(CSVParserTest.suite());
        tests.addTestSuite(ContactFieldMapperTest.class);
        tests.addTestSuite(ContactMergerTest.class);
        tests.addTestSuite(CalendarCommonCollectionTest.class);
        tests.addTestSuite(CalendarDeleteTest.class);
        tests.addTestSuite(AppointmentDeleteNoCommit.class);
        tests.addTestSuite(AppointmentAttachmentTest.class);
        tests.addTestSuite(OrderByTest.class);
        tests.addTestSuite(ContactCollectorTest.class);
        tests.addTestSuite(ConsistencyTest.class);
        tests.addTestSuite(ConfigurationImplTest.class);

        return tests;
    }
}
