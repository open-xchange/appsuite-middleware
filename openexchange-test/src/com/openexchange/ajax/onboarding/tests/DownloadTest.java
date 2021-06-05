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

package com.openexchange.ajax.onboarding.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import com.openexchange.testing.httpclient.modules.ClientonboardingApi;

/**
 * {@link DownloadTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class DownloadTest extends AbstractOnboardingTest {

    @Test
    public void testDownloadCalDAVProfile() throws Exception {
        ClientonboardingApi api = getApi();
        byte[] response = api.downloadClientOnboardingProfile("caldav");
        assertNotNull(response);
        String profile = new String(response, "UTF-8");
        assertTrue(profile.contains("PayloadIdentifier"));
        assertTrue(profile.contains("PayloadType"));
        assertTrue(profile.contains("PayloadVersion"));
        assertTrue(profile.contains("PayloadDisplayName"));
        assertTrue(profile.contains("com.open-xchange.caldav"));
        assertTrue(profile.contains("Configuration"));
    }

    @Test
    public void testDownloadCardDAVProfile() throws Exception {
        ClientonboardingApi api = getApi();
        byte[] response = api.downloadClientOnboardingProfile("carddav");
        assertNotNull(response);
        String profile = new String(response, "UTF-8");
        assertTrue(profile.contains("PayloadIdentifier"));
        assertTrue(profile.contains("PayloadType"));
        assertTrue(profile.contains("PayloadVersion"));
        assertTrue(profile.contains("PayloadDisplayName"));
        assertTrue(profile.contains("com.open-xchange.carddav"));
        assertTrue(profile.contains("Configuration"));
    }

    @Test
    public void testDownloadDAVProfile() throws Exception {
        ClientonboardingApi api = getApi();
        byte[] response = api.downloadClientOnboardingProfile("dav");
        assertNotNull(response);
        String profile = new String(response, "UTF-8");
        assertTrue(profile.contains("PayloadIdentifier"));
        assertTrue(profile.contains("PayloadType"));
        assertTrue(profile.contains("PayloadVersion"));
        assertTrue(profile.contains("PayloadDisplayName"));
        assertTrue(profile.contains("com.open-xchange.carddav"));
        assertTrue(profile.contains("com.open-xchange.caldav"));
        assertTrue(profile.contains("Configuration"));
    }

    @Test
    public void testInvalidProfileName() throws Exception {
        ClientonboardingApi api = getApi();
        byte[] response = api.downloadClientOnboardingProfile("invalid");
        assertNotNull(response);
        String resp = new String(response, "UTF-8");
        assertTrue(resp.contains("error"));
    }

}
