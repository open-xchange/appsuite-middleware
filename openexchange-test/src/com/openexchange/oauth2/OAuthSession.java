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
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.HashMap;
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
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.html.internal.parser.HtmlParser;
import com.openexchange.java.util.UUIDs;


/**
 * {@link OAuthSession}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.x.x
 */
public class OAuthSession extends AJAXSession {

    private static final String REDIRECT_URI = "http://localhost:8080";

    private String accessToken;

    /**
     * Initializes a new {@link OAuthSession}.
     */
    public OAuthSession(User user) {
        super(newWebConversation(), newOAuthHttpClient(), null);
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
        return client;
    }

    private void obtainAccess(User user, HttpClient client) throws Exception {
        String hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        String login = AJAXConfig.getProperty(user.getLogin()) + "@" + AJAXConfig.getProperty(AJAXConfig.Property.CONTEXTNAME);
        String password = AJAXConfig.getProperty(user.getPassword());

        String csrfState = UUIDs.getUnformattedStringFromRandom();
        HttpGet getLoginForm = new HttpGet(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath("/ajax/o/oauth2/authorization")
            .setParameter("response_type", "code")
            .setParameter("client_id", "983e78b3e76d423988ed09c345364f05")
            .setParameter("redirect_uri", REDIRECT_URI)
            .setParameter("scope", "r_contacts")
            .setParameter("state", csrfState)
            .build());
        HttpResponse loginFormResponse = client.execute(getLoginForm);
        assertEquals(HttpStatus.SC_OK, loginFormResponse.getStatusLine().getStatusCode());
        String loginForm = EntityUtils.toString(loginFormResponse.getEntity());
        Map<String, String> hiddenFormParams = getHiddenFormFields(loginForm);

        LinkedList<NameValuePair> authFormParams = new LinkedList<>();
        authFormParams.add(new BasicNameValuePair("user_login", login));
        authFormParams.add(new BasicNameValuePair("user_password", password));
        authFormParams.add(new BasicNameValuePair("access_denied", "false"));
        for (Entry<String, String> entry : hiddenFormParams.entrySet()) {
            authFormParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        HttpPost submitLoginForm = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath("/ajax/o/oauth2/authorization")
            .build());
        submitLoginForm.setHeader(HttpHeaders.REFERER, getLoginForm.getURI().toString());
        submitLoginForm.setEntity(new UrlEncodedFormEntity(authFormParams));

        HttpResponse authCodeResponse = client.execute(submitLoginForm);
        String authCodeResponseBody = EntityUtils.toString(authCodeResponse.getEntity());
        assertEquals(authCodeResponseBody, HttpStatus.SC_MOVED_TEMPORARILY, authCodeResponse.getStatusLine().getStatusCode());
        assertTrue("Location header missing in redirect response", authCodeResponse.containsHeader(HttpHeaders.LOCATION));
        String redirectLocation = authCodeResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        assertTrue("Unexpected redirect location: " + redirectLocation, redirectLocation.startsWith(REDIRECT_URI));

        Map<String, String> redirectParams = new HashMap<>();
        String[] redirectParamPairs = URLDecoder.decode(new URI(redirectLocation).getRawQuery(), "UTF-8").split("&");
        for (String pair : redirectParamPairs) {
            String[] split = pair.split("=");
            redirectParams.put(split[0], split[1]);
        }

        assertFalse(redirectParams.get("error_description"), redirectParams.containsKey("error"));

        String state = redirectParams.get("state");
        assertEquals(csrfState, state);
        String code = redirectParams.get("code");
        assertNotNull(code);

        LinkedList<NameValuePair> redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", "983e78b3e76d423988ed09c345364f05"));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", "a1dd1c62735f4e61b80b6aa2b29df37d"));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
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
        assertNotNull(jAccessTokenResponse.get("token_type"));
        assertNotNull(jAccessTokenResponse.get("access_token"));
        assertNotNull(jAccessTokenResponse.get("refresh_token"));
        assertNotNull(jAccessTokenResponse.get("scope"));
        assertNotNull(jAccessTokenResponse.get("expires_in"));

        accessToken = jAccessTokenResponse.getString("access_token");
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
     * Returns all hidden form fields from an HTML form as name-value pairs.
     *
     * @param form The HTML form as String
     * @return A map of fields
     */
    public static Map<String, String> getHiddenFormFields(String form) {
        final Map<String, String> params = new HashMap<>();
        HtmlParser.parse(form, new HtmlHandler() {

            @Override
            public void handleXMLDeclaration(String version, Boolean standalone, String encoding) {

            }

            @Override
            public void handleText(String text, boolean ignorable) {

            }

            @Override
            public void handleStartTag(String tag, Map<String, String> attributes) {
                handleTag(tag, attributes);
            }

            @Override
            public void handleSimpleTag(String tag, Map<String, String> attributes) {
                handleTag(tag, attributes);
            }

            private void handleTag(String tag, Map<String, String> attributes) {
                if ("input".equals(tag) && "hidden".equals(attributes.get("type"))) {
                    String name = attributes.get("name");
                    String value = attributes.get("value");
                    if (name != null && value != null) {
                        params.put(name, value);
                    }
                }
            }

            @Override
            public void handleError(String errorMsg) {

            }

            @Override
            public void handleEndTag(String tag) {

            }

            @Override
            public void handleDocDeclaration(String docDecl) {

            }

            @Override
            public void handleComment(String comment) {

            }

            @Override
            public void handleCDATA(String text) {

            }
        });

        return params;
    }
}
