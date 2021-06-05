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

package com.openexchange.ajax.oauth.provider.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Date;
import java.util.LinkedList;
import java.util.Map;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import com.openexchange.ajax.oauth.provider.EndpointTest;

/**
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class Protocol {

    public static String login(HttpClient client, OAuthParams params, String login, String password) throws IOException {
        GETRequest getLoginForm = new GETRequest().setScheme(params.getScheme()).setHostname(params.getHostname()).setPort(params.getPort()).setClientId(params.getClientId()).setRedirectURI(params.getRedirectURI()).setState(params.getState()).setScope(params.getScope());
        GETResponse loginFormResponse = getLoginForm.execute(client);
        POSTRequest loginRequest = loginFormResponse.preparePOSTRequest().setLogin(login).setPassword(password);
        POSTResponse loginResponse = loginRequest.submit(client);
        loginResponse.assertRedirect();
        URI redirectLocation = loginResponse.getRedirectLocation();
        String sessionId = HttpTools.extractQueryParams(redirectLocation).get("session");
        assertNotNull("Session ID is missing in response", sessionId);
        return sessionId;
    }

    public static String authorize(HttpClient client, OAuthParams params, String sessionId) throws IOException {
        GETRequest getAuthForm = new GETRequest().setScheme(params.getScheme()).setHostname(params.getHostname()).setPort(params.getPort()).setClientId(params.getClientId()).setRedirectURI(params.getRedirectURI()).setState(params.getState()).setScope(params.getScope()).setSessionId(sessionId);
        POSTRequest authRequest = getAuthForm.execute(client).preparePOSTRequest();
        POSTResponse authResponse = authRequest.submit(client);
        assertEquals(302, authResponse.getStatusCode());

        URI redirectLocation = authResponse.getRedirectLocation();
        assertTrue("Unexpected redirect location: " + redirectLocation, redirectLocation.toString().startsWith(params.getRedirectURI()));
        Map<String, String> redirectParams = HttpTools.extractQueryParams(redirectLocation);
        assertNull("Response contained an error: " + redirectParams.get("error") + " [" + redirectParams.get("error_description") + "]", redirectParams.get("error"));
        assertEquals("Unexpected state", params.getState(), redirectParams.get("state"));
        assertNotNull("Auth code missing in response", redirectParams.get("code"));
        return redirectParams.get("code");
    }

    public static Grant redeemAuthCode(HttpClient client, OAuthParams params, String authCode) throws Exception {
        LinkedList<NameValuePair> redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", params.getClientId()));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", params.getClientSecret()));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", params.getRedirectURI()));
        redeemAuthCodeParams.add(new BasicNameValuePair("code", authCode));

        HttpPost redeemAuthCode = new HttpPost(
            new URIBuilder()
                .setScheme(params.getScheme())
                .setHost(params.getHostname())
                .setPort(params.getPort())
                .setPath(EndpointTest.TOKEN_ENDPOINT).build());
        redeemAuthCode.setEntity(new UrlEncodedFormEntity(redeemAuthCodeParams));

        HttpResponse accessTokenResponse = client.execute(redeemAuthCode);
        assertEquals(HttpStatus.SC_OK, accessTokenResponse.getStatusLine().getStatusCode());
        JSONObject jAccessTokenResponse = JSONObject.parse(new InputStreamReader(accessTokenResponse.getEntity().getContent(), accessTokenResponse.getEntity().getContentEncoding() == null ? "UTF-8" : accessTokenResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertTrue("bearer".equalsIgnoreCase(jAccessTokenResponse.getString("token_type")));
        assertNotNull(jAccessTokenResponse.get("access_token"));
        assertNotNull(jAccessTokenResponse.get("refresh_token"));
        assertNotNull(jAccessTokenResponse.get("scope"));
        assertNotNull(jAccessTokenResponse.get("expires_in"));

        Grant grant = new Grant();
        grant.setAccessToken(jAccessTokenResponse.getString("access_token"));
        grant.setRefreshToken(jAccessTokenResponse.getString("refresh_token"));
        grant.setExpiry(new Date(System.currentTimeMillis() + (jAccessTokenResponse.getLong("expires_in") * 1000l)));
        grant.setScope(jAccessTokenResponse.getString("scope"));
        return grant;
    }

    public static Grant obtainAccess(HttpClient client, OAuthParams params, String login, String password) throws Exception {
        String sessionId = Protocol.login(client, params, login, password);
        String authCode = Protocol.authorize(client, params, sessionId);
        return Protocol.redeemAuthCode(client, params, authCode);
    }
}
