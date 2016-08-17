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

package com.openexchange.ajax.oauth.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.LockSupport;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.junit.Test;
import com.openexchange.admin.rmi.dataobjects.Credentials;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.oauth.provider.actions.RevokeRequest;
import com.openexchange.ajax.oauth.provider.protocol.GETRequest;
import com.openexchange.ajax.oauth.provider.protocol.GETResponse;
import com.openexchange.ajax.oauth.provider.protocol.HttpTools;
import com.openexchange.ajax.oauth.provider.protocol.OAuthParams;
import com.openexchange.ajax.oauth.provider.protocol.POSTRequest;
import com.openexchange.ajax.oauth.provider.protocol.POSTResponse;
import com.openexchange.ajax.oauth.provider.protocol.Protocol;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.oauth.provider.authorizationserver.client.ClientManagement;
import com.openexchange.oauth.provider.impl.grant.OAuthGrantStorage;
import com.openexchange.oauth.provider.rmi.client.ClientDto;
import com.openexchange.oauth.provider.rmi.client.RemoteClientManagement;

/**
 * {@link ProtocolFlowTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ProtocolFlowTest extends EndpointTest {

    @Test
    public void testFlow() throws Exception {
        GETRequest getLoginForm = new GETRequest()
            .setHostname(hostname)
            .setClientId(getClientId())
            .setRedirectURI(getRedirectURI())
            .setState(csrfState);
        GETResponse loginFormResponse = getLoginForm.execute(client);
        POSTRequest loginRequest = loginFormResponse.preparePOSTRequest()
            .setLogin(login)
            .setPassword(password);
        POSTResponse loginResponse = loginRequest.submit(client);
        GETResponse getAuthForm = loginResponse.followRedirect(client);
        POSTRequest authRequest = getAuthForm.preparePOSTRequest();
        POSTResponse authResponse = authRequest.submit(client);
        Map<String, String> redirectParams = HttpTools.extractQueryParams(authResponse.getRedirectLocation());
        assertNull(redirectParams.get("error"));
        assertNotNull(redirectParams.get("code"));
        assertEquals(csrfState, redirectParams.get("state"));
    }

    @Test
    public void testRedeemRefreshToken() throws Exception {
        OAuthClient oauthClient = new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScope());
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
            .setPath(EndpointTest.TOKEN_ENDPOINT)
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
        OAuthParams params = new OAuthParams()
            .setHostname(hostname)
            .setClientId(getClientId())
            .setClientSecret(getClientSecret())
            .setRedirectURI(getRedirectURI())
            .setScope(getScope().toString());
        String sessionId = Protocol.login(client, params, login, password);
        String authCode = Protocol.authorize(client, params, sessionId);

        LinkedList<NameValuePair> redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", getClientId()));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", getSecondRedirectURI())); // <- wrong redirect URI
        redeemAuthCodeParams.add(new BasicNameValuePair("code", authCode));

        HttpPost redeemAuthCode = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(params.getHostname())
            .setPath(EndpointTest.TOKEN_ENDPOINT)
            .build());
        redeemAuthCode.setEntity(new UrlEncodedFormEntity(redeemAuthCodeParams));
        HttpResponse accessTokenResponse = client.execute(redeemAuthCode);
        assertEquals(HttpStatus.SC_BAD_REQUEST, accessTokenResponse.getStatusLine().getStatusCode());
        JSONObject jAccessTokenResponse = JSONObject.parse(new InputStreamReader(accessTokenResponse.getEntity().getContent(), accessTokenResponse.getEntity().getContentEncoding() == null ? "UTF-8" : accessTokenResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", jAccessTokenResponse.get("error"));

        // Test replay with correct URI (auth code must have been invalidated anyway for security reasons)
        redeemAuthCodeParams.clear();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", getClientId()));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", getRedirectURI())); // <- correct redirect URI
        redeemAuthCodeParams.add(new BasicNameValuePair("code", authCode));

        redeemAuthCode = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(params.getHostname())
            .setPath(EndpointTest.TOKEN_ENDPOINT)
            .build());
        redeemAuthCode.setEntity(new UrlEncodedFormEntity(redeemAuthCodeParams));
        accessTokenResponse = client.execute(redeemAuthCode);
        assertEquals(HttpStatus.SC_BAD_REQUEST, accessTokenResponse.getStatusLine().getStatusCode());
        jAccessTokenResponse = JSONObject.parse(new InputStreamReader(accessTokenResponse.getEntity().getContent(), accessTokenResponse.getEntity().getContentEncoding() == null ? "UTF-8" : accessTokenResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", jAccessTokenResponse.get("error"));
    }

    @Test
    public void testAuthCodeReplay() throws Exception {
        OAuthParams params = new OAuthParams()
            .setHostname(hostname)
            .setClientId(getClientId())
            .setClientSecret(getClientSecret())
            .setRedirectURI(getRedirectURI())
            .setScope(getScope().toString());
        String sessionId = Protocol.login(client, params, login, password);
        String authCode = Protocol.authorize(client, params, sessionId);
        Protocol.redeemAuthCode(client, params, authCode);

        /*
         * Try to obtain another token with the same auth code
         */
        LinkedList<NameValuePair> redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", getClientId()));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", getRedirectURI()));
        redeemAuthCodeParams.add(new BasicNameValuePair("code", authCode));

        HttpPost redeemAuthCode = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(params.getHostname())
            .setPath(EndpointTest.TOKEN_ENDPOINT)
            .build());
        redeemAuthCode.setEntity(new UrlEncodedFormEntity(redeemAuthCodeParams));
        HttpResponse replayResponse = client.execute(redeemAuthCode);
        assertEquals(HttpStatus.SC_BAD_REQUEST, replayResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testMaxNumberOfDistinctGrants() throws Exception {
        // A user must have at max. OAuthProviderService.MAX_CLIENTS_PER_USER grants for different clients
        Credentials masterAdminCredentials = AbstractOAuthTest.getMasterAdminCredentials();
        RemoteClientManagement clientManagement = (RemoteClientManagement) Naming.lookup("rmi://" + AJAXConfig.getProperty(Property.RMI_HOST) + ":1099/" + RemoteClientManagement.RMI_NAME);
        List<ClientDto> clients = new ArrayList<>(ClientManagement.MAX_CLIENTS_PER_USER);
        for (int i = 0; i < ClientManagement.MAX_CLIENTS_PER_USER; i++) {
            clients.add(clientManagement.registerClient(ClientManagement.DEFAULT_GID, prepareClient("testMaxNumberOfDistinctGrants " + i + " " + System.currentTimeMillis()), masterAdminCredentials));
        }

        try {
            // acquire one token per client
            for (ClientDto client : clients) {
                OAuthClient c = new OAuthClient(User.User1, client.getId(), client.getSecret(), client.getRedirectURIs().get(0), getScope());
                c.assertAccess();
            }

            // now the max + 1 try with the default client
            boolean error = false;
            try {
                OAuthClient c = new OAuthClient(User.User1, getClientId(), getClientSecret(), getRedirectURI(), getScope());
                c.assertAccess();
            } catch (AssertionError e) {
                error = true;
            }
            assertTrue(error);

            // revoke access for one client and assure we can now grant access to another one
            Iterator<ClientDto> it = clients.iterator();
            ClientDto client2 = it.next();
            AJAXClient ajaxClient = new AJAXClient(User.User1);
            ajaxClient.execute(new RevokeRequest(client2.getId()));

            OAuthClient c = new OAuthClient(User.User1, getClientId(), getClientSecret(), getRedirectURI(), getScope());
            c.assertAccess();
        } finally {
            for (ClientDto client : clients) {
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
            clients.add(new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScope()));
            LockSupport.parkNanos(1000000);  // last modified is used to delete old entries and has milliseconds granularity
        }

        for (OAuthClient client : clients) {
            client.assertAccess();
        }

        // max + 1 should replace the oldest one
        clients.add(new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScope()));
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
