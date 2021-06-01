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
    com.openexchange.ajax.infostore.thirdparty.webdav.WebDAVTestSuite.class,
    com.openexchange.ajax.infostore.thirdparty.federatedSharing.FederatedSharingTestSuite.class,
    com.openexchange.ajax.infostore.apiclient.InfostoreApiClientSuite.class,
    com.openexchange.ajax.mail.MailTestSuite.class,
    com.openexchange.ajax.mail.filter.MailFilterTestSuite.class,
    com.openexchange.ajax.redirect.RedirectTests.class,
    com.openexchange.ajax.reminder.ReminderAJAXSuite.class,
    com.openexchange.ajax.session.SessionTestSuite.class,
    com.openexchange.ajax.task.TaskTestSuite.class,
    com.openexchange.ajax.user.UserAJAXSuite.class,
    // TODO: enable when MSLiveOAuthClient is implemented
    // com.openexchange.subscribe.mslive.MSLiveTestSuite.class,

    com.openexchange.dav.caldav.tests.CalDAVTestSuite.class,
    com.openexchange.dav.caldav.bugs.CalDAVBugSuite.class,
    com.openexchange.dav.carddav.tests.CardDAVTestSuite.class,
    com.openexchange.dav.carddav.bugs.CardDAVBugSuite.class,

    com.openexchange.grizzly.GrizzlyTestSuite.class,

    com.openexchange.ajax.resource.ResourceSuite.class,
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
    com.openexchange.ajax.sessionmanagement.SessionManagementSuite.class,
    com.openexchange.ajax.chronos.ChronosTestSuite.class,
    com.openexchange.ajax.multifactor.MultifactorTestSuite.class,
    com.openexchange.ajax.mailcompose.MailComposeTestSuite.class,
    com.openexchange.test.LostAndFoundInterfaceTests.class,
})
public final class MainInterfaceTests {

}
