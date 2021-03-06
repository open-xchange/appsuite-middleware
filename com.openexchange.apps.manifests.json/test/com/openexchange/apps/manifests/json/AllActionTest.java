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

package com.openexchange.apps.manifests.json;

import static com.openexchange.java.Autoboxing.B;
import java.util.Collections;
import org.json.JSONArray;
import org.json.JSONException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.powermock.modules.junit4.PowerMockRunner;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.apps.manifests.DefaultManifestBuilder;
import com.openexchange.apps.manifests.ManifestBuilder;
import com.openexchange.apps.manifests.ManifestContributor;
import com.openexchange.capabilities.Capabilities;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.NearRegistryServiceTracker;
import com.openexchange.tools.session.ServerSession;

/**
 * Unit tests for {@link AllAction}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
@RunWith(PowerMockRunner.class)
public class AllActionTest {

    /**
     * Mock for the service lookup
     */
    @Mock
    private final AJAXModuleActivator serviceLookup = null;

    /**
     * Mock for the ServerSession
     */
    @Mock
    private final ServerSession serverSession = null;

    /**
     * Mock for the CapabilityService
     */
    @Mock
    private final CapabilityService capabilityService = null;

    @Mock
    private final NearRegistryServiceTracker<ManifestContributor> manifestContributorTracker = null;

    /**
     * {@inheritDoc}
     */
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        // BEHAVIOUR
        Mockito.when(B(this.serverSession.isAnonymous())).thenReturn(Boolean.FALSE);
        Mockito.when(serviceLookup.getService(CapabilityService.class)).thenReturn(this.capabilityService);
        Mockito.when(this.capabilityService.getCapabilities(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyBoolean(), ArgumentMatchers.anyBoolean())).thenReturn(
            Capabilities.emptyCapabilitySet());
        Mockito.when(this.manifestContributorTracker.getServiceList()).thenReturn(Collections.<ManifestContributor> emptyList());
    }

     @Test
     public void testGetManifests_IsAnonymousSessionAndManifestEmpty_ReturnEmptyArray() throws OXException {
        Mockito.when(B(this.serverSession.isAnonymous())).thenReturn(Boolean.TRUE);
        JSONArray manifests = new JSONArray();

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(0, array.length());
    }

     @Test
     public void testGetManifests_IsAnonymousSessionAndManifestNotEmptyButNoNamespace_ReturnEmptyArray() throws OXException, JSONException {
        Mockito.when(B(this.serverSession.isAnonymous())).thenReturn(Boolean.TRUE);
        JSONArray manifests = new JSONArray(
            "[{\"icon\":\"addressbook.png\",\"category\":\"Productivity\",\"title\":\"Plaxo Address Book\",\"description\":\"The only address book that works for you. Plaxo keeps your contact info updated & your communication devices in sync.\",\"company\":\"Plaxo Inc.\",\"path\":\"3rd.party/addr/main\",\"requires\":\"plaxo.addressbook\"},{\"icon\":\"app-store.png\",\"category\":\"Basic\",\"title\":\"Application Manager\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/applications/main\"},{\"path\":\"io.ox/backbone/tests/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"icon\":\"calendar.png\",\"category\":\"Productivity\",\"title\":\"Calendar\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/calendar/main\",\"requires\":\"calendar\"},{\"icon\":\"addressbook.png\",\"category\":\"Basic\",\"title\":\"Address Book\",\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/contacts/main\",\"requires\":\"contacts\"}]");

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(manifests.length(), array.length());
    }

     @Test
     public void testGetManifests_IsAnonymousSessionAndManifestNotEmptyNoSigninPlugin_ReturnEmptyArray() throws OXException, JSONException {
        Mockito.when(B(this.serverSession.isAnonymous())).thenReturn(Boolean.TRUE);
        JSONArray manifests = new JSONArray(
            "[{\"icon\":\"addressbook.png\",\"category\":\"Productivity\",\"title\":\"Plaxo Address Book\",\"description\":\"The only address book that works for you. Plaxo keeps your contact info updated & your communication devices in sync.\",\"company\":\"Plaxo Inc.\",\"path\":\"3rd.party/addr/main\",\"requires\":\"plaxo.addressbook\"},{\"icon\":\"app-store.png\",\"category\":\"Basic\",\"title\":\"Application Manager\",\"visible\":false,\"settings\":false,\"company\":\"OX Software GmbH\",\"path\":\"io.ox/lessons/main\",\"requires\":\"dev\"},{\"icon\":\"mail.png\",\"category\":\"Basic\",\"title\":\"Mail\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/mail/main\",\"requires\":\"webmail\"},{\"path\":\"io.ox/mail/accounts/keychain\",\"requires\":[\"webmail\",\"multiple_mail_accounts\"],\"namespace\":\"io.ox/keychain/api\"},{\"path\":\"io.ox/mail/accounts/settings\",\"requires\":[\"webmail\",\"multiple_mail_accounts\"],\"namespace\":\"io.ox/settings/accounts/settings/pane\"},{\"path\":\"io.ox/mail/settings/signatures/register\",\"namespace\":[\"io.ox/mail/settings/pane\",\"io.ox/core/updates/updater\"]},{\"icon\":\"mail.png\",\"category\":\"Basic\",\"title\":\"Compose email\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/mail/write/main\",\"requires\":\"webmail\"},{\"path\":\"io.ox/mail/write/test\",\"requires\":\"webmail\",\"namespace\":\"io.ox/dev/testing/main\"},{\"path\":\"io.ox/oauth/keychain\",\"namespace\":\"io.ox/keychain/api\"},{\"path\":\"io.ox/oauth/settings\",\"namespace\":\"io.ox/settings/accounts/settings/pane\"},{\"icon\":\"portal.png\",\"category\":\"Productivity\",\"title\":\"Portal\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/portal/main\",\"requires\":\"!deniedPortal\"},{\"icon\":\"files.png\",\"category\":\"Basic\",\"title\":\"Settings\",\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/settings/main\"},{\"path\":\"io.ox/settings/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"path\":\"io.ox/settings/accounts/email/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"icon\":\"default.png\",\"category\":\"Productivity\",\"title\":\"Tasks\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/tasks/main\",\"requires\":\"tasks\"},{\"icon\":\"default.png\",\"category\":\"Productivity\",\"title\":\"Edit task\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/tasks/edit/main\",\"requires\":\"tasks\"},{\"path\":\"plugins/halo/register\",\"namespace\":\"core\"},{\"path\":\"plugins/halo/test\",\"namespace\":\"test\"},{\"path\":\"plugins/halo/appointments/register\",\"requires\":\"calendar\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/contacts/register\",\"requires\":\"contacts\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/linkedIn/register\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/mail/register\",\"requires\":\"webmail\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/notifications/calendar/register\",\"requires\":\"calendar\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/notifications/mail/register\",\"requires\":\"webmail\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/notifications/tasks/register\",\"requires\":\"tasks\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/portal/birthdays/register\",\"requires\":\"contacts\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/calendar/register\",\"requires\":\"calendar\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/dummy/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/flickr/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/helloworld/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/linkedIn/register\",\"requires\":\"oauth linkedin\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/mail/register\",\"requires\":\"webmail\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/quota/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/reddit/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/rss/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/tasks/register\",\"requires\":\"tasks\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/tumblr/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/twitter/register\",\"requires\":\"oauth twitter\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/updater/register\",\"requires\":\"oxupdater\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/userSettings/register\",\"namespace\":\"portal\"}]");

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(manifests.length(), array.length());
    }

     @Test
     public void testGetManifests_IsAnonymousSessionAndManifestNotEmpty_ReturnEmptyArray() throws OXException, JSONException {
        Mockito.when(B(this.serverSession.isAnonymous())).thenReturn(Boolean.TRUE);
        JSONArray manifests = new JSONArray(
            "[{\"icon\":\"addressbook.png\",\"category\":\"Productivity\",\"title\":\"Plaxo Address Book\",\"description\":\"The only address book that works for you. Plaxo keeps your contact info updated & your communication devices in sync.\",\"company\":\"Plaxo Inc.\",\"path\":\"3rd.party/addr/main\",\"requires\":\"plaxo.addressbook\"},{\"icon\":\"app-store.png\",\"category\":\"Basic\",\"title\":\"Application Manager\",\"visible\":false,\"settings\":false,\"company\":\"OX Software GmbH\",\"path\":\"io.ox/lessons/main\",\"requires\":\"dev\"},{\"icon\":\"mail.png\",\"category\":\"Basic\",\"title\":\"Mail\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/mail/main\",\"requires\":\"webmail\"},{\"path\":\"io.ox/mail/accounts/keychain\",\"requires\":[\"webmail\",\"multiple_mail_accounts\"],\"namespace\":\"io.ox/keychain/api\"},{\"path\":\"io.ox/mail/accounts/settings\",\"requires\":[\"webmail\",\"multiple_mail_accounts\"],\"namespace\":\"io.ox/settings/accounts/settings/pane\"},{\"path\":\"io.ox/mail/settings/signatures/register\",\"namespace\":[\"io.ox/mail/settings/pane\",\"io.ox/core/updates/updater\"]},{\"icon\":\"mail.png\",\"category\":\"Basic\",\"title\":\"Compose email\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/mail/write/main\",\"requires\":\"webmail\"},{\"path\":\"io.ox/mail/write/test\",\"requires\":\"webmail\",\"namespace\":\"io.ox/dev/testing/main\"},{\"path\":\"io.ox/oauth/keychain\",\"namespace\":\"io.ox/keychain/api\"},{\"path\":\"io.ox/oauth/settings\",\"namespace\":\"io.ox/settings/accounts/settings/pane\"},{\"icon\":\"portal.png\",\"category\":\"Productivity\",\"title\":\"Portal\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/portal/main\",\"requires\":\"!deniedPortal\"},{\"icon\":\"files.png\",\"category\":\"Basic\",\"title\":\"Settings\",\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/settings/main\"},{\"path\":\"io.ox/settings/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"path\":\"io.ox/settings/accounts/email/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"icon\":\"default.png\",\"category\":\"Productivity\",\"title\":\"Tasks\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/tasks/main\",\"requires\":\"tasks\"},{\"icon\":\"default.png\",\"category\":\"Productivity\",\"title\":\"Edit task\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/tasks/edit/main\",\"requires\":\"tasks\"},{\"path\":\"plugins/halo/register\",\"namespace\":\"core\"},{\"path\":\"plugins/halo/test\",\"namespace\":\"test\"},{\"path\":\"plugins/halo/appointments/register\",\"requires\":\"calendar\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/contacts/register\",\"requires\":\"contacts\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/linkedIn/register\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/mail/register\",\"requires\":\"webmail\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/notifications/calendar/register\",\"requires\":\"calendar\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/notifications/mail/register\",\"requires\":\"webmail\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/notifications/tasks/register\",\"requires\":\"tasks\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/portal/birthdays/register\",\"requires\":\"contacts\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/calendar/register\",\"requires\":\"calendar\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/dummy/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/flickr/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/helloworld/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/linkedIn/register\",\"requires\":\"oauth linkedin\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/mail/register\",\"requires\":\"webmail\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/quota/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/reddit/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/rss/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/tasks/register\",\"requires\":\"tasks\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/tumblr/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/twitter/register\",\"requires\":\"oauth twitter\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/updater/register\",\"requires\":\"oxupdater\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/userSettings/register\",\"namespace\":\"signin\"}]");

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(manifests.length(), array.length());
    }

     @Test
     public void testGetManifests_NotAnonymousNoCapabilityNoManifest_ReturnEmptyArray() throws OXException {
        JSONArray manifests = new JSONArray();

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(0, array.length());
    }

     @Test
     public void testGetManifests_NotAnonymousNoCapabilityAvailable_ReturnEmptyArray() throws OXException {
        JSONArray manifests = new JSONArray();

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(0, array.length());
    }

     @Test
     public void testGetManifests_NotAnonymousOneCapabilityAvailableManifestEmpty_ReturnEmptyArray() throws OXException {
        JSONArray manifests = new JSONArray();

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(0, array.length());
    }

     @Test
     public void testGetManifests_NotAnonymousOneCapabilityAvailableManifestAvailable_ReturnArray() throws OXException, JSONException {
        JSONArray manifests = new JSONArray(
            "[{\"icon\":\"addressbook.png\",\"category\":\"Productivity\",\"title\":\"Plaxo Address Book\",\"description\":\"The only address book that works for you. Plaxo keeps your contact info updated & your communication devices in sync.\",\"company\":\"Plaxo Inc.\",\"path\":\"3rd.party/addr/main\",\"requires\":\"plaxo.addressbook\"},{\"icon\":\"app-store.png\",\"category\":\"Basic\",\"title\":\"Application Manager\",\"visible\":false,\"settings\":false,\"company\":\"OX Software GmbH\",\"path\":\"io.ox/lessons/main\",\"requires\":\"dev\"},{\"icon\":\"mail.png\",\"category\":\"Basic\",\"title\":\"Mail\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/mail/main\",\"requires\":\"webmail\"},{\"path\":\"io.ox/mail/accounts/keychain\",\"requires\":[\"webmail\",\"multiple_mail_accounts\"],\"namespace\":\"io.ox/keychain/api\"},{\"path\":\"io.ox/mail/accounts/settings\",\"requires\":[\"webmail\",\"multiple_mail_accounts\"],\"namespace\":\"io.ox/settings/accounts/settings/pane\"},{\"path\":\"io.ox/mail/settings/signatures/register\",\"namespace\":[\"io.ox/mail/settings/pane\",\"io.ox/core/updates/updater\"]},{\"icon\":\"mail.png\",\"category\":\"Basic\",\"title\":\"Compose email\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/mail/write/main\",\"requires\":\"webmail\"},{\"path\":\"io.ox/mail/write/test\",\"requires\":\"webmail\",\"namespace\":\"io.ox/dev/testing/main\"},{\"path\":\"io.ox/oauth/keychain\",\"namespace\":\"io.ox/keychain/api\"},{\"path\":\"io.ox/oauth/settings\",\"namespace\":\"io.ox/settings/accounts/settings/pane\"},{\"icon\":\"portal.png\",\"category\":\"Productivity\",\"title\":\"Portal\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/portal/main\",\"requires\":\"!deniedPortal\"},{\"icon\":\"files.png\",\"category\":\"Basic\",\"title\":\"Settings\",\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/settings/main\"},{\"path\":\"io.ox/settings/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"path\":\"io.ox/settings/accounts/email/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"icon\":\"default.png\",\"category\":\"Productivity\",\"title\":\"Tasks\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/tasks/main\",\"requires\":\"tasks\"},{\"icon\":\"default.png\",\"category\":\"Productivity\",\"title\":\"Edit task\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/tasks/edit/main\",\"requires\":\"tasks\"},{\"path\":\"plugins/halo/register\",\"namespace\":\"core\"},{\"path\":\"plugins/halo/test\",\"namespace\":\"test\"},{\"path\":\"plugins/halo/appointments/register\",\"requires\":\"calendar\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/contacts/register\",\"requires\":\"contacts\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/linkedIn/register\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/mail/register\",\"requires\":\"webmail\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/notifications/calendar/register\",\"requires\":\"calendar\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/notifications/mail/register\",\"requires\":\"webmail\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/notifications/tasks/register\",\"requires\":\"tasks\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/portal/birthdays/register\",\"requires\":\"contacts\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/calendar/register\",\"requires\":\"calendar\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/dummy/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/flickr/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/helloworld/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/linkedIn/register\",\"requires\":\"oauth linkedin\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/mail/register\",\"requires\":\"webmail\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/quota/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/reddit/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/rss/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/tasks/register\",\"requires\":\"tasks\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/tumblr/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/twitter/register\",\"requires\":\"oauth twitter\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/updater/register\",\"requires\":\"oxupdater\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/userSettings/register\",\"namespace\":\"signin\"}]");

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(manifests.length(), array.length());
    }

     @Test
     public void testGetManifests_NotAnonymousOneCapabilityAvailableManifestAvailableNoBackendNeeded_ReturnArray() throws OXException, JSONException {
        JSONArray manifests = new JSONArray(
            "[{\"icon\":\"addressbook.png\",\"category\":\"Productivity\",\"title\":\"Plaxo Address Book\",\"description\":\"The only address book that works for you. Plaxo keeps your contact info updated & your communication devices in sync.\",\"company\":\"Plaxo Inc.\",\"path\":\"3rd.party/addr/main\",\"requires\":\"plaxo.addressbook\"},{\"icon\":\"app-store.png\",\"category\":\"Basic\",\"title\":\"Application Manager\",\"visible\":false,\"settings\":false,\"company\":\"OX Software GmbH\",\"path\":\"io.ox/lessons/main\",\"requires\":\"dev\"},{\"icon\":\"mail.png\",\"category\":\"Basic\",\"title\":\"Mail\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/mail/main\",\"requires\":\"webmail\"},{\"path\":\"io.ox/mail/accounts/keychain\",\"requires\":[\"webmail\",\"multiple_mail_accounts\"],\"namespace\":\"io.ox/keychain/api\"},{\"path\":\"io.ox/mail/accounts/settings\",\"requires\":[\"webmail\",\"multiple_mail_accounts\"],\"namespace\":\"io.ox/settings/accounts/settings/pane\"},{\"path\":\"io.ox/mail/settings/signatures/register\",\"namespace\":[\"io.ox/mail/settings/pane\",\"io.ox/core/updates/updater\"]},{\"icon\":\"mail.png\",\"category\":\"Basic\",\"title\":\"Compose email\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/mail/write/main\",\"requires\":\"webmail\"},{\"path\":\"io.ox/mail/write/test\",\"requires\":\"webmail\",\"namespace\":\"io.ox/dev/testing/main\"},{\"path\":\"io.ox/oauth/keychain\",\"namespace\":\"io.ox/keychain/api\"},{\"path\":\"io.ox/oauth/settings\",\"namespace\":\"io.ox/settings/accounts/settings/pane\"},{\"icon\":\"portal.png\",\"category\":\"Productivity\",\"title\":\"Portal\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/portal/main\",\"requires\":\"!deniedPortal\"},{\"icon\":\"files.png\",\"category\":\"Basic\",\"title\":\"Settings\",\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/settings/main\"},{\"path\":\"io.ox/settings/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"path\":\"io.ox/settings/accounts/email/test\",\"namespace\":\"io.ox/dev/testing/main\"},{\"icon\":\"default.png\",\"category\":\"Productivity\",\"title\":\"Tasks\",\"settings\":true,\"company\":\"Open-Xchange\",\"path\":\"io.ox/tasks/main\",\"requires\":\"tasks\"},{\"icon\":\"default.png\",\"category\":\"Productivity\",\"title\":\"Edit task\",\"visible\":false,\"settings\":false,\"company\":\"Open-Xchange\",\"path\":\"io.ox/tasks/edit/main\",\"requires\":\"tasks\"},{\"path\":\"plugins/halo/register\",\"namespace\":\"core\"},{\"path\":\"plugins/halo/test\",\"namespace\":\"test\"},{\"path\":\"plugins/halo/appointments/register\",\"requires\":\"calendar\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/contacts/register\",\"requires\":\"contacts\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/linkedIn/register\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/halo/mail/register\",\"requires\":\"webmail\",\"namespace\":\"plugins/halo\"},{\"path\":\"plugins/notifications/calendar/register\",\"requires\":\"calendar\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/notifications/mail/register\",\"requires\":\"webmail\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/notifications/tasks/register\",\"requires\":\"tasks\",\"namespace\":\"io.ox/core/notifications\"},{\"path\":\"plugins/portal/birthdays/register\",\"requires\":\"contacts\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/calendar/register\",\"requires\":\"calendar\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/dummy/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/flickr/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/helloworld/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/linkedIn/register\",\"requires\":\"oauth linkedin\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/mail/register\",\"requires\":\"webmail\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/quota/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/reddit/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/rss/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/tasks/register\",\"requires\":\"tasks\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/tumblr/register\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/twitter/register\",\"requires\":\"oauth twitter\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/updater/register\",\"requires\":\"oxupdater\",\"namespace\":\"portal\"},{\"path\":\"plugins/portal/userSettings/register\",\"namespace\":\"signin\"}]");

        ManifestBuilder mb = new DefaultManifestBuilder(manifests, manifestContributorTracker);
        JSONArray array = mb.buildManifests(serverSession, null);

        Assert.assertEquals(manifests.length(), array.length());
    }
}
