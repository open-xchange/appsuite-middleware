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
import com.openexchange.ajax.drive.DriveAJAXSuite;
import com.openexchange.ajax.find.FindTestSuite;
import com.openexchange.ajax.jslob.JSlobTestSuite;
import com.openexchange.ajax.oauth.provider.OAuthProviderTests;
import com.openexchange.test.concurrent.ParallelSuite;

/**
 * Test suite for all AJAX interface tests.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
@RunWith(ParallelSuite.class)
@Suite.SuiteClasses({
    com.openexchange.SmokeTestSuite.class,
    com.openexchange.ajax.FolderTest.class,
    com.openexchange.ajax.UserTest.class,
    com.openexchange.ajax.mailaccount.MailAccountSuite.class,
    com.openexchange.ajax.appointment.AppointmentAJAXSuite.class,
    com.openexchange.ajax.attach.AttachmentTests.class,
    com.openexchange.ajax.config.ConfigTestSuite.class,
    com.openexchange.ajax.contact.ContactAJAXSuite.class,
    com.openexchange.ajax.folder.FolderTestSuite.class,
    com.openexchange.ajax.group.GroupTestSuite.class,
    com.openexchange.ajax.importexport.ImportExportServerSuite.class,
    com.openexchange.ajax.infostore.InfostoreAJAXSuite.class,
    com.openexchange.ajax.mail.MailTestSuite.class,
    com.openexchange.ajax.mail.filter.MailFilterTestSuite.class,
    com.openexchange.ajax.redirect.RedirectTests.class,
    com.openexchange.ajax.reminder.ReminderAJAXSuite.class,
    com.openexchange.ajax.session.SessionTestSuite.class,
    com.openexchange.ajax.task.TaskTestSuite.class,
    com.openexchange.ajax.publish.PublishTestSuite.class,
    com.openexchange.ajax.subscribe.SubscribeTestSuite.class,
    com.openexchange.ajax.user.UserAJAXSuite.class,
    // TODO: enable when MSLiveOAuthClient is implemented
    // com.openexchange.subscribe.mslive.MSLiveTestSuite.class,
    
    com.openexchange.dav.caldav.tests.CalDAVTestSuite.class,
    com.openexchange.dav.caldav.bugs.CalDAVBugSuite.class,
    com.openexchange.dav.carddav.tests.CardDAVTestSuite.class,
    com.openexchange.dav.carddav.bugs.CardDAVBugSuite.class,
    
    com.openexchange.grizzly.GrizzlyTestSuite.class,

    com.openexchange.ajax.resource.ResourceSuite.class,
    com.openexchange.ajax.roundtrip.pubsub.PubSubSuite.class,
    /*
     * TODO Enable the following test again. But this requires fixing the server. Currently the request fails.
     * com.openexchange.webdav.client.NaughtyClientTest.class,
     */
    com.openexchange.ajax.appointment.recurrence.AppointmentParticipantsShouldBecomeUsersIfPossible.class,
    com.openexchange.ajax.task.TaskExternalUsersBecomeInternalUsers.class,
//    com.openexchange.ajax.contact.AggregatingContactTest.class,
    com.openexchange.ajax.framework.ParamsTest.class,
    com.openexchange.ajax.contact.AdvancedSearchTest.class,
    com.openexchange.ajax.tokenloginV2.TokenLoginV2Test.class,
    com.openexchange.ajax.oauth.OAuthTests.class,
    com.openexchange.test.resourcecache.ResourceCacheTest.class,
    FindTestSuite.class,
    com.openexchange.ajax.quota.QuotaTestSuite.class,
    JSlobTestSuite.class,
    // Needs to be disabled as associated test suite requires a frontend package, which is currently not available
    // ManifestsTestSuite.class,
    // TODO: enable
    DriveAJAXSuite.class,
    com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRendererTest.class,
    OAuthProviderTests.class,
    com.openexchange.ajax.userfeedback.StoreTest.class,

})
public final class MainInterfaceTests {

}
