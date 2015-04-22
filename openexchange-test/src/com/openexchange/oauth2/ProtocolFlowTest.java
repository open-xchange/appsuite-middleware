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
 *     Copyright (C) 2004-2015 Open-Xchange, Inc.
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

package com.openexchange.oauth2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStreamReader;
import java.net.URI;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.OAuthProviderService;
import com.openexchange.oauth.provider.client.Client;
import com.openexchange.oauth.provider.client.ClientManagement;
import com.openexchange.oauth.provider.internal.grant.OAuthGrantStorage;
import com.openexchange.oauth.provider.rmi.RemoteClientManagement;
import com.openexchange.oauth2.utils.OAuthTestUtils;

/**
 * {@link ProtocolFlowTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ProtocolFlowTest extends EndpointTest {

    @Test
    public void testRedeemRefreshToken() throws Exception {
        OAuthClient oauthClient = new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScopes());
        oauthClient.assertAccess();
        OAuthSession session = (OAuthSession) oauthClient.getSession();
        String accessToken = session.getAccessToken();
        String refreshToken = session.getRefreshToken();

        LinkedList<NameValuePair> redeemRefreshTokenParams = new LinkedList<>();
        redeemRefreshTokenParams.add(new BasicNameValuePair("client_id", getClientId()));
        redeemRefreshTokenParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        redeemRefreshTokenParams.add(new BasicNameValuePair("grant_type", "refresh_token"));
        redeemRefreshTokenParams.add(new BasicNameValuePair("redirect_uri", getRedirectURI()));
        redeemRefreshTokenParams.add(new BasicNameValuePair("refresh_token", refreshToken));

        HttpPost redeemRefreshToken = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath("/ajax/o/oauth2/accessToken")
            .build());
        redeemRefreshToken.setEntity(new UrlEncodedFormEntity(redeemRefreshTokenParams));

        HttpResponse accessTokenResponse = client.execute(redeemRefreshToken);
        assertEquals(HttpStatus.SC_OK, accessTokenResponse.getStatusLine().getStatusCode());
        JSONObject jAccessTokenResponse = JSONObject.parse(new InputStreamReader(accessTokenResponse.getEntity().getContent(), accessTokenResponse.getEntity().getContentEncoding() == null ? "UTF-8" : accessTokenResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertTrue("bearer".equalsIgnoreCase(jAccessTokenResponse.getString("token_type")));
        assertNotNull(jAccessTokenResponse.get("access_token"));
        assertNotNull(jAccessTokenResponse.get("refresh_token"));
        assertNotNull(jAccessTokenResponse.get("scope"));
        assertNotNull(jAccessTokenResponse.get("expires_in"));

        // Expect both tokens to be different from the old ones
        assertFalse(jAccessTokenResponse.getString("access_token").equals(accessToken));
        assertFalse(jAccessTokenResponse.getString("refresh_token").equals(refreshToken));

        // access with old token must not work anymore
        boolean error = false;
        try {
            oauthClient.assertAccess();
        } catch (AssertionError e) {
            error = true;
        }
        assertTrue(error);
    }

    @Test
    public void testRedeemIsDeniedWhenRedirectURIChanges() throws Exception {
        String csrfState = UUIDs.getUnformattedStringFromRandom();

        HttpGet authorizationRequest = new HttpGet(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .setParameter("response_type", "code")
            .setParameter("client_id", getClientId())
            .setParameter("redirect_uri", getRedirectURI())
            .setParameter("scope", getScopes())
            .setParameter("state", csrfState)
            .build());
        HttpResponse authorizationResponse = client.execute(authorizationRequest);
        String redirectLocation = authorizationResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        URIBuilder authenticationRequestURI = prepareAuthenticationRequest(redirectLocation);
        HttpPost authenticationRequest = new HttpPost(authenticationRequestURI.build());
        authenticationRequest.setHeader(HttpHeaders.REFERER, authorizationRequest.getURI().toString());

        HttpResponse authCodeResponse = client.execute(authenticationRequest);



        LinkedList<NameValuePair> redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", getClientId()));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", getSecondRedirectURI()));
//        redeemAuthCodeParams.add(new BasicNameValuePair("code", redirectParams.get("code")));

        HttpPost redeemAuthCode = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(TOKEN_ENDPOINT)
            .build());
        redeemAuthCode.setEntity(new UrlEncodedFormEntity(redeemAuthCodeParams));

        HttpResponse accessTokenResponse = client.execute(redeemAuthCode);
        assertEquals(HttpStatus.SC_BAD_REQUEST, accessTokenResponse.getStatusLine().getStatusCode());
        JSONObject jAccessTokenResponse = JSONObject.parse(new InputStreamReader(accessTokenResponse.getEntity().getContent(), accessTokenResponse.getEntity().getContentEncoding() == null ? "UTF-8" : accessTokenResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", jAccessTokenResponse.get("error"));

        // Test replay with correct URI (auth code must have been invalidated anyway for security reasons)
        redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", getClientId()));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", getRedirectURI()));
//        redeemAuthCodeParams.add(new BasicNameValuePair("code", redirectParams.get("code")));

        redeemAuthCode = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(TOKEN_ENDPOINT)
            .build());
        redeemAuthCode.setEntity(new UrlEncodedFormEntity(redeemAuthCodeParams));
        accessTokenResponse = client.execute(redeemAuthCode);
        assertEquals(HttpStatus.SC_BAD_REQUEST, accessTokenResponse.getStatusLine().getStatusCode());
        jAccessTokenResponse = JSONObject.parse(new InputStreamReader(accessTokenResponse.getEntity().getContent(), accessTokenResponse.getEntity().getContentEncoding() == null ? "UTF-8" : accessTokenResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", jAccessTokenResponse.get("error"));
    }

    @Test
    public void testAuthCodeReplay() throws Exception {
        String csrfState = UUIDs.getUnformattedStringFromRandom();

        URI authorizationRequest = prepareAuthorizationRequest(csrfState);
        HttpGet authorizationGetRequest = new HttpGet(authorizationRequest);

        HttpResponse authorizationResponse = client.execute(authorizationGetRequest);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, authorizationResponse.getStatusLine().getStatusCode());
        assertTrue(authorizationResponse.containsHeader(HttpHeaders.LOCATION));

        String redirectLocation = authorizationResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        URIBuilder authenticationRequestURI = prepareAuthenticationRequest(redirectLocation);
        HttpPost authenticationRequest = new HttpPost(authenticationRequestURI.build());
        authenticationRequest.setHeader(HttpHeaders.REFERER, authorizationGetRequest.getURI().toString());

        HttpResponse authCodeResponse = client.execute(authenticationRequest);

        String authCodeResponseBody = EntityUtils.toString(authCodeResponse.getEntity());
        assertEquals(authCodeResponseBody, HttpStatus.SC_MOVED_TEMPORARILY, authCodeResponse.getStatusLine().getStatusCode());
        assertTrue("Location header missing in redirect response", authCodeResponse.containsHeader(HttpHeaders.LOCATION));
        String redirectLocationAuth = authCodeResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        assertTrue("Unexpected redirect location: " + redirectLocationAuth, redirectLocationAuth.startsWith(getRedirectURI()));

        Map<String, String> redirectParamsAuth = OAuthTestUtils.extractRedirectParamsFromQuery(redirectLocationAuth);

        assertFalse(redirectParamsAuth.get("error_description"), redirectParamsAuth.containsKey("error"));
        assertEquals(csrfState, redirectParamsAuth.get("state"));
        String code = redirectParamsAuth.get("code");
        assertNotNull(code);

        LinkedList<NameValuePair> redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", getClientId()));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", getRedirectURI()));
        redeemAuthCodeParams.add(new BasicNameValuePair("code", code));

        HttpPost redeemAuthCode = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath("/ajax/o/oauth2/accessToken")
            .build());
        redeemAuthCode.setEntity(new UrlEncodedFormEntity(redeemAuthCodeParams));

        HttpResponse accessTokenResponse = client.execute(redeemAuthCode);
        assertEquals(HttpStatus.SC_OK, accessTokenResponse.getStatusLine().getStatusCode());
        JSONObject jAccessTokenResponse = JSONObject.parse(new InputStreamReader(accessTokenResponse.getEntity().getContent(), accessTokenResponse.getEntity().getContentEncoding() == null ? "UTF-8" : accessTokenResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertTrue("bearer".equalsIgnoreCase(jAccessTokenResponse.getString("token_type")));
        assertNotNull(jAccessTokenResponse.get("access_token"));
        assertNotNull(jAccessTokenResponse.get("refresh_token"));
        assertNotNull(jAccessTokenResponse.get("scope"));
        assertNotNull(jAccessTokenResponse.get("expires_in"));

        /*
         * Try to obtain another token with the same auth code
         */
        HttpResponse replayResponse = client.execute(redeemAuthCode);
        assertEquals(HttpStatus.SC_BAD_REQUEST, replayResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testMaxNumberOfDistinctGrants() throws Exception {
        // A user must have at max. OAuthProviderService.MAX_CLIENTS_PER_USER grants for different clients
        Credentials masterAdminCredentials = AbstractOAuthTest.getMasterAdminCredentials();
        RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        List<Client> clients = new ArrayList<>(OAuthProviderService.MAX_CLIENTS_PER_USER);
        for (int i = 0; i < OAuthProviderService.MAX_CLIENTS_PER_USER; i++) {
            clients.add(clientManagement.registerClient(ClientManagement.DEFAULT_GID, prepareClient("testMaxNumberOfDistinctGrants " + i + " " + System.currentTimeMillis()), masterAdminCredentials));
        }

        try {
            // acquire one token per client
            for (Client client : clients) {
                OAuthClient c = new OAuthClient(client.getId(), client.getSecret(), client.getRedirectURIs().get(0), getScopes());
                c.assertAccess();
            }

            // now the max + 1 try with the default client
            boolean error = false;
            try {
                OAuthClient c = new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScopes());
                c.assertAccess();
            } catch (AssertionError e) {
                error = true;
            }
            assertTrue(error);

            // FIXME: don't unregister client but revoke access for one of them as soon as the API call exists
            Iterator<Client> it = clients.iterator();
            Client client2 = it.next();
            clientManagement.unregisterClient(client2.getId(), masterAdminCredentials);
            it.remove();

            OAuthClient c = new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScopes());
            c.assertAccess();
        } finally {
            for (Client client : clients) {
                try {
                    clientManagement.unregisterClient(client.getId(), masterAdminCredentials);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    @Test
    public void testGrantStorageQuota() throws Exception {
        // there must be at max. OAuthGrantStorage.MAX_GRANTS_PER_CLIENT grants per user per client. On further auth requests old grants are deleted (LRU).
        List<OAuthClient> clients = new ArrayList<>();
        for (int i = 0; i < OAuthGrantStorage.MAX_GRANTS_PER_CLIENT; i++) {
            // obtains a fresh access token
            clients.add(new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScopes()));
            LockSupport.parkNanos(1000000);  // last modified is used to delete old entries and has milliseconds granularity
        }

        for (OAuthClient client : clients) {
            client.assertAccess();
        }

        // max + 1 should replace the oldest one
        clients.add(new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScopes()));
        boolean error = false;
        try {
            clients.get(0).assertAccess();
        } catch (AssertionError e) {
            error = true;
        }

        assertTrue(error);
        clients.remove(0);

        for (OAuthClient client : clients) {
            client.assertAccess();
        }
    }

}
