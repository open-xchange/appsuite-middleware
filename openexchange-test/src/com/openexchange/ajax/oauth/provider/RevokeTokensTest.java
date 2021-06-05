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

package com.openexchange.ajax.oauth.provider;

import static org.junit.Assert.assertEquals;
import java.io.InputStreamReader;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.junit.Test;

/**
 * {@link RevokeTokensTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class RevokeTokensTest extends EndpointTest {

    @Test
    public void testRevokeAccessToken() throws Exception {
        OAuthClient oauthClient = new OAuthClient(testUser, getClientId(), getClientSecret(), getRedirectURI(), getScope());
        oauthClient.assertAccess();

        HttpGet revoke = new HttpGet(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPort(PORT).setPath(REVOKE_ENDPOINT).setParameter("access_token", ((OAuthSession) oauthClient.getSession()).getAccessToken()).build());
        HttpResponse revokeResponse = client.execute(revoke);
        revoke.reset();
        assertEquals(HttpStatus.SC_OK, revokeResponse.getStatusLine().getStatusCode());
        assertNoAccess(oauthClient);
    }

    @Test
    public void testRevokeRefreshToken() throws Exception {
        OAuthClient oauthClient = new OAuthClient(testUser, getClientId(), getClientSecret(), getRedirectURI(), getScope());
        oauthClient.assertAccess();

        HttpGet revoke = new HttpGet(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPort(PORT).setPath(REVOKE_ENDPOINT).setParameter("refresh_token", ((OAuthSession) oauthClient.getSession()).getRefreshToken()).build());
        HttpResponse revokeResponse = client.execute(revoke);
        assertEquals(HttpStatus.SC_OK, revokeResponse.getStatusLine().getStatusCode());
        assertNoAccess(oauthClient);
    }

    @Test
    public void testFailWithInvalidTokens() throws Exception {
        // missing token parameter
        HttpGet revoke = new HttpGet(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPort(PORT).setPath(REVOKE_ENDPOINT).build());
        HttpResponse revokeResponse = client.execute(revoke);
        assertEquals(HttpStatus.SC_BAD_REQUEST, revokeResponse.getStatusLine().getStatusCode());
        JSONObject errorObject = JSONObject.parse(new InputStreamReader(revokeResponse.getEntity().getContent(), revokeResponse.getEntity().getContentEncoding() == null ? "UTF-8" : revokeResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", errorObject.getString("error"));

        // illegal access token
        revoke = new HttpGet(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPort(PORT).setPath(REVOKE_ENDPOINT).setParameter("access_token", "invalid").build());
        revokeResponse = client.execute(revoke);
        assertEquals(HttpStatus.SC_BAD_REQUEST, revokeResponse.getStatusLine().getStatusCode());
        errorObject = JSONObject.parse(new InputStreamReader(revokeResponse.getEntity().getContent(), revokeResponse.getEntity().getContentEncoding() == null ? "UTF-8" : revokeResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", errorObject.getString("error"));

        // illegal refresh token
        revoke = new HttpGet(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPort(PORT).setPath(REVOKE_ENDPOINT).setParameter("refresh_token", "invalid").build());
        revokeResponse = client.execute(revoke);
        assertEquals(HttpStatus.SC_BAD_REQUEST, revokeResponse.getStatusLine().getStatusCode());
        errorObject = JSONObject.parse(new InputStreamReader(revokeResponse.getEntity().getContent(), revokeResponse.getEntity().getContentEncoding() == null ? "UTF-8" : revokeResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", errorObject.getString("error"));
    }

}
