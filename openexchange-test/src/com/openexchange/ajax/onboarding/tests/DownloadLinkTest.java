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

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    @Test
    public void testDownloadLinkCalDAV() throws Exception {
        ClientonboardingApi api = getApi();
        JSONObject json = new JSONObject(api.generateDownloadLinkForClientOnboardingProfile(getSessionId(), "caldav"));
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
        JSONObject json = new JSONObject(api.generateDownloadLinkForClientOnboardingProfile(getSessionId(), "carddav"));
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
        JSONObject json = new JSONObject(api.generateDownloadLinkForClientOnboardingProfile(getSessionId(), "dav"));
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
