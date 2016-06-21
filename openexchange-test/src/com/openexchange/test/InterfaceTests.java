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

import com.openexchange.ajax.drive.DriveAJAXSuite;
import com.openexchange.ajax.find.FindTestSuite;
import com.openexchange.ajax.jslob.JSlobTestSuite;
import com.openexchange.ajax.oauth.provider.OAuthProviderTests;
import com.openexchange.ajax.share.ShareAJAXSuite;
import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test suite for all AJAX interface tests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public final class InterfaceTests {

    private InterfaceTests() {
        super();
    }

    public static final Test suite() {
        final TestSuite tests = new TestSuite();
        // First of all the smoke tests.
        tests.addTest(com.openexchange.SmokeTestSuite.suite());

        tests.addTestSuite(com.openexchange.ajax.FolderTest.class);
        tests.addTestSuite(com.openexchange.ajax.MultipleTest.class);
        tests.addTestSuite(com.openexchange.ajax.UserTest.class);

        tests.addTest(com.openexchange.ajax.mailaccount.MailAccountSuite.suite());
        tests.addTest(com.openexchange.ajax.appointment.AppointmentAJAXSuite.suite());
        tests.addTest(new JUnit4TestAdapter(com.openexchange.ajax.attach.AttachmentTests.class));
        tests.addTest(com.openexchange.ajax.config.ConfigTestSuite.suite());
        tests.addTest(com.openexchange.ajax.contact.ContactAJAXSuite.suite());
        tests.addTest(com.openexchange.ajax.folder.FolderTestSuite.suite());
        tests.addTest(com.openexchange.ajax.group.GroupTestSuite.suite());
        tests.addTest(com.openexchange.ajax.importexport.ImportExportServerSuite.suite());
        tests.addTest(com.openexchange.ajax.infostore.InfostoreAJAXSuite.suite());
        tests.addTest(com.openexchange.ajax.mail.MailTestSuite.suite());
        tests.addTest(com.openexchange.ajax.mail.filter.MailFilterTestSuite.suite());
        tests.addTest(new JUnit4TestAdapter(com.openexchange.ajax.redirect.RedirectTests.class));
        tests.addTest(com.openexchange.ajax.reminder.ReminderAJAXSuite.suite());
        tests.addTest(com.openexchange.ajax.session.SessionTestSuite.suite());
        tests.addTest(com.openexchange.ajax.task.TaskTestSuite.suite());
        tests.addTest(com.openexchange.ajax.publish.PublishTestSuite.suite());
        tests.addTest(com.openexchange.ajax.subscribe.SubscribeTestSuite.suite());
        tests.addTest(new JUnit4TestAdapter(com.openexchange.ajax.user.UserAJAXSuite.class));
        tests.addTest(com.openexchange.subscribe.google.GoogleTestSuite.suite());
        // TODO: enable when MSLiveOAuthClient is implemented
        // tests.addTest(com.openexchange.subscribe.mslive.MSLiveTestSuite.suite());

        tests.addTest(new JUnit4TestAdapter(com.openexchange.dav.caldav.tests.CalDAVTestSuite.class));
        tests.addTest(new JUnit4TestAdapter(com.openexchange.dav.caldav.bugs.CalDAVBugSuite.class));
        tests.addTest(new JUnit4TestAdapter(com.openexchange.dav.carddav.tests.CardDAVTestSuite.class));
        tests.addTest(new JUnit4TestAdapter(com.openexchange.dav.carddav.bugs.CardDAVBugSuite.class));

        tests.addTest(com.openexchange.grizzly.GrizzlyTestSuite.suite());

        tests.addTest(com.openexchange.webdav.xml.appointment.AppointmentWebdavSuite.suite());
        tests.addTest(com.openexchange.webdav.xml.contact.ContactWebdavSuite.suite());
        tests.addTest(com.openexchange.webdav.xml.folder.FolderWebdavSuite.suite());
        tests.addTest(com.openexchange.webdav.xml.task.TaskWebdavSuite.suite());
        tests.addTest(com.openexchange.webdav.xml.attachment.AttachmentWebdavSuite.suite());
        tests.addTest(com.openexchange.ajax.resource.ResourceSuite.suite());

        tests.addTest(com.openexchange.ajax.roundtrip.pubsub.PubSubSuite.suite());
        tests.addTestSuite(com.openexchange.webdav.xml.GroupUserTest.class);
        /*
         * TODO Enable the following test again. But this requires fixing the server. Currently the request fails.
         * tests.addTestSuite(com.openexchange.webdav.client.NaughtyClientTest.class);
         */
        tests.addTestSuite(com.openexchange.ajax.FunambolTests.class);
        tests.addTestSuite(com.openexchange.ajax.appointment.recurrence.AppointmentParticipantsShouldBecomeUsersIfPossible.class);
        tests.addTestSuite(com.openexchange.ajax.task.TaskExternalUsersBecomeInternalUsers.class);
        tests.addTestSuite(com.openexchange.ajax.contact.AggregatingContactTest.class);
        tests.addTestSuite(com.openexchange.ajax.framework.ParamsTest.class);
        tests.addTestSuite(com.openexchange.ajax.contact.AdvancedSearchTest.class);
        tests.addTestSuite(com.openexchange.ajax.tokenloginV2.TokenLoginV2Test.class);
        tests.addTest(new JUnit4TestAdapter(com.openexchange.ajax.oauth.OAuthTests.class));
        tests.addTestSuite(com.openexchange.test.resourcecache.ResourceCacheTest.class);
        tests.addTest(FindTestSuite.suite());
        tests.addTest(com.openexchange.ajax.quota.QuotaTestSuite.suite());
        tests.addTest(JSlobTestSuite.suite());
        tests.addTest(ShareAJAXSuite.suite());
        // Needs to be disabled as associated test suite requires a frontend package, which is currently not available
        // tests.addTest(ManifestsTestSuite.suite());
        tests.addTest(new JUnit4TestAdapter(OAuthProviderTests.class));
        // TODO: enable
        tests.addTest(DriveAJAXSuite.suite());
        return tests;
    }
}
