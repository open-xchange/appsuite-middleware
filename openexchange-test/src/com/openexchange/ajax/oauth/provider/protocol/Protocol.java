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

package com.openexchange.ajax.oauth.provider.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
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
        GETRequest getLoginForm = new GETRequest()
            .setHostname(params.getHostname())
            .setClientId(params.getClientId())
            .setRedirectURI(params.getRedirectURI())
            .setState(params.getState())
            .setScope(params.getScope());
        GETResponse loginFormResponse = getLoginForm.execute(client);
        POSTRequest loginRequest = loginFormResponse.preparePOSTRequest()
            .setLogin(login)
            .setPassword(password);
        POSTResponse loginResponse = loginRequest.submit(client);
        loginResponse.assertRedirect();
        URI redirectLocation = loginResponse.getRedirectLocation();
        String sessionId = HttpTools.extractQueryParams(redirectLocation).get("session");
        assertNotNull("Session ID is missing in response", sessionId);
        return sessionId;
    }

    public static String authorize(HttpClient client, OAuthParams params, String sessionId) throws IOException {
        GETRequest getAuthForm = new GETRequest()
            .setHostname(params.getHostname())
            .setClientId(params.getClientId())
            .setRedirectURI(params.getRedirectURI())
            .setState(params.getState())
            .setScope(params.getScope())
            .setSessionId(sessionId);
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

        HttpPost redeemAuthCode = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(params.getHostname())
            .setPath(EndpointTest.TOKEN_ENDPOINT)
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
