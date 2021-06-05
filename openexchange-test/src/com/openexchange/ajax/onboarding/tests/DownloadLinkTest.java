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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.testing.httpclient.modules.ClientonboardingApi;

/**
 * {@link DownloadLinkTest}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.1
 */
public class DownloadLinkTest extends AbstractOnboardingTest {

    @Test
    public void testDownloadLinkCalDAV() throws Exception {
        ClientonboardingApi api = getApi();
        JSONObject json = new JSONObject(api.generateDownloadLinkForClientOnboardingProfile("caldav"));
        assertNotNull(json);
        String link = json.getString("data");
        assertNotNull(link);
        String profile = download(link);
        assertTrue(profile.contains("PayloadIdentifier"));
        assertTrue(profile.contains("PayloadType"));
        assertTrue(profile.contains("PayloadVersion"));
        assertTrue(profile.contains("PayloadDisplayName"));
        assertTrue(profile.contains("com.open-xchange.caldav"));
        assertTrue(profile.contains("Configuration"));
    }

    @Test
    public void testDownloadLinkCardDAV() throws Exception {
        ClientonboardingApi api = getApi();
        JSONObject json = new JSONObject(api.generateDownloadLinkForClientOnboardingProfile("carddav"));
        assertNotNull(json);
        String link = json.getString("data");
        assertNotNull(link);
        String profile = download(link);
        assertTrue(profile.contains("PayloadIdentifier"));
        assertTrue(profile.contains("PayloadType"));
        assertTrue(profile.contains("PayloadVersion"));
        assertTrue(profile.contains("PayloadDisplayName"));
        assertTrue(profile.contains("com.open-xchange.carddav"));
        assertTrue(profile.contains("Configuration"));
    }

    @Test
    public void testDownloadLinkDAV() throws Exception {
        ClientonboardingApi api = getApi();
        JSONObject json = new JSONObject(api.generateDownloadLinkForClientOnboardingProfile("dav"));
        assertNotNull(json);
        String link = json.getString("data");
        assertNotNull(link);
        String profile = download(link);
        assertTrue(profile.contains("PayloadIdentifier"));
        assertTrue(profile.contains("PayloadType"));
        assertTrue(profile.contains("PayloadVersion"));
        assertTrue(profile.contains("PayloadDisplayName"));
        assertTrue(profile.contains("com.open-xchange.caldav"));
        assertTrue(profile.contains("com.open-xchange.carddav"));
        assertTrue(profile.contains("Configuration"));
    }

    private String download(String link) throws Exception {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(link);
        HttpResponse response = client.execute(request);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        return result.toString();
    }

}
