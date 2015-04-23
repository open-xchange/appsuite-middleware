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
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.DefaultScopes;
import com.openexchange.oauth2.utils.OAuthTestUtils;

/**
 * {@link OAuthSession}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OAuthSession extends AJAXSession {

    private final String clientId;

    private final String clientSecret;

    private final String redirectURI;

    private final String[] scopes;

    private String accessToken;

    private String refreshToken;

    /**
     * Initializes a new {@link OAuthSession}.
     */
    public OAuthSession(User user, String clientId, String clientSecret, String redirectURI, String... scopes) {
        super(newWebConversation(), newOAuthHttpClient(), null);
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectURI = redirectURI;
        this.scopes = scopes;
        try {
            AJAXConfig.init();
            obtainAccess(user, getHttpClient());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }

    private static DefaultHttpClient newOAuthHttpClient() {
        DefaultHttpClient client = newHttpClient();
        try {
            SSLSocketFactory ssf = new SSLSocketFactory(new TrustSelfSignedStrategy(), new AllowAllHostnameVerifier());
            client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, ssf));
        } catch (KeyManagementException | UnrecoverableKeyException | NoSuchAlgorithmException | KeyStoreException e) {
            Assert.fail(e.getMessage());
        }

        HttpParams params = client.getParams();
        params.setBooleanParameter(ClientPNames.HANDLE_AUTHENTICATION, false);
        params.setBooleanParameter(ClientPNames.HANDLE_REDIRECTS, false);
        return client;
    }

    private void obtainAccess(User user, HttpClient client) throws Exception {
        String hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        String login = AJAXConfig.getProperty(user.getLogin()) + "@" + AJAXConfig.getProperty(AJAXConfig.Property.CONTEXTNAME);
        String password = AJAXConfig.getProperty(user.getPassword());

        String csrfState = UUIDs.getUnformattedStringFromRandom();

        URI authorizationRequest = prepareAuthorizationRequest(csrfState, hostname);
        HttpGet authorizationGetRequest = new HttpGet(authorizationRequest);

        HttpResponse authorizationResponse = client.execute(authorizationGetRequest);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, authorizationResponse.getStatusLine().getStatusCode());
        assertTrue(authorizationResponse.containsHeader(HttpHeaders.LOCATION));

        String redirectLocation = authorizationResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        URI authenticationRequestURI = prepareAuthenticationRequest(redirectLocation, hostname, login, password);
        HttpPost authenticationRequest = new HttpPost(authenticationRequestURI);
        authenticationRequest.setHeader(HttpHeaders.REFERER, authorizationGetRequest.getURI().toString());

        HttpResponse authCodeResponse = client.execute(authenticationRequest);

        String authCodeResponseBody = EntityUtils.toString(authCodeResponse.getEntity());
        assertEquals(authCodeResponseBody, HttpStatus.SC_MOVED_TEMPORARILY, authCodeResponse.getStatusLine().getStatusCode());
        assertTrue("Location header missing in redirect response", authCodeResponse.containsHeader(HttpHeaders.LOCATION));
        String redirectLocationAuth = authCodeResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        assertTrue("Unexpected redirect location: " + redirectLocationAuth, redirectLocationAuth.startsWith(redirectURI));

        Map<String, String> redirectParamsAuth = OAuthTestUtils.extractRedirectParamsFromQuery(redirectLocationAuth);

        assertFalse(redirectParamsAuth.get("error_description"), redirectParamsAuth.containsKey("error"));
        assertEquals(csrfState, redirectParamsAuth.get("state"));
        String code = redirectParamsAuth.get("code");
        assertNotNull(code);

        LinkedList<NameValuePair> redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", clientId));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", clientSecret));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", redirectURI));
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

        accessToken = jAccessTokenResponse.getString("access_token");
        refreshToken = jAccessTokenResponse.getString("refresh_token");
    }

    public static URI prepareAuthenticationRequest(String redirectLocation, String hostname, String login, String password) throws URISyntaxException {
        Map<String, String> redirectParams = OAuthTestUtils.extractRedirectParamsFromFragment(redirectLocation);

        URIBuilder uriBuilder = new URIBuilder().setScheme("https").setHost(hostname).setPath("/ajax/o/oauth2/authorization");
        uriBuilder.addParameter("user_login", login).addParameter("user_password", password).addParameter("access_denied", "false");
        for (Entry<String, String> s : redirectParams.entrySet()) {
            uriBuilder.addParameter(s.getKey(), s.getValue());
        }
        return uriBuilder.build();
    }

    private URI prepareAuthorizationRequest(String csrfState, String hostname) throws URISyntaxException {
        URIBuilder getLoginFormBuilder = new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath("/ajax/o/oauth2/authorization")
            .setParameter("response_type", "code")
            .setParameter("client_id", clientId)
            .setParameter("redirect_uri", redirectURI)
            .setParameter("state", csrfState);
        if (scopes != null && scopes.length > 0) {
            getLoginFormBuilder.setParameter("scope", new DefaultScopes(scopes).scopeString());
        }
        return getLoginFormBuilder.build();
    }

    /**
     * Gets the accessToken
     *
     * @return The accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Gets the refreshToken
     *
     * @return The refreshToken
     */
    public String getRefreshToken() {
        return refreshToken;
    }
}

