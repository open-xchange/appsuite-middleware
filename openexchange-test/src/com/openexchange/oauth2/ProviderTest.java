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

import static org.junit.Assert.*;
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
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.UUIDs;


/**
 * {@link ProviderTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class ProviderTest {

    /**
     *
     */
    private static final String REDIRECT_URI = "http://localhost:8080";
    private static String protocol;
    private static String hostname;
    private static String login;
    private static String password;

    private DefaultHttpClient client;

    @BeforeClass
    public static void beforeClass() throws OXException {
        AJAXConfig.init();
        protocol = AJAXConfig.getProperty(AJAXConfig.Property.PROTOCOL);
        hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        login = AJAXConfig.getProperty(User.User1.getLogin()) + "@" + AJAXConfig.getProperty(AJAXConfig.Property.CONTEXTNAME);
        password = AJAXConfig.getProperty(User.User1.getPassword());
    }

    @Before
    public void before() throws KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException {
        client = new DefaultHttpClient(new PoolingClientConnectionManager());
        HttpParams params = client.getParams();
        int minute = 1 * 60 * 1000;
        HttpConnectionParams.setConnectionTimeout(params, minute);
        HttpConnectionParams.setSoTimeout(params, minute);

        SSLSocketFactory ssf = new SSLSocketFactory(new TrustSelfSignedStrategy(), new AllowAllHostnameVerifier());
        client.getConnectionManager().getSchemeRegistry().register(new Scheme("https", 443, ssf));
    }

    @Test
    public void testAuthFlow() throws Exception {
        String csrfState = UUIDs.getUnformattedStringFromRandom();
        HttpGet getLoginForm = new HttpGet(new URIBuilder()
            .setScheme(protocol)
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
        System.out.println(EntityUtils.toString(loginFormResponse.getEntity()));

        LinkedList<NameValuePair> authFormParams = new LinkedList<>();
        authFormParams.add(new BasicNameValuePair("user_login", login));
        authFormParams.add(new BasicNameValuePair("user_password", password));
        authFormParams.add(new BasicNameValuePair("client_id", "983e78b3e76d423988ed09c345364f05"));
        authFormParams.add(new BasicNameValuePair("redirect_uri", REDIRECT_URI));
        authFormParams.add(new BasicNameValuePair("scope", "r_contacts"));
        authFormParams.add(new BasicNameValuePair("state", csrfState));
        authFormParams.add(new BasicNameValuePair("response_type", "code"));
        authFormParams.add(new BasicNameValuePair("access_denied", "false"));

        HttpPost submitLoginForm = new HttpPost(new URIBuilder()
            .setScheme(protocol)
            .setHost(hostname)
            .setPath("/ajax/o/oauth2/authorization")
            .build());
        submitLoginForm.setEntity(new UrlEncodedFormEntity(authFormParams));

        HttpResponse authCodeResponse = client.execute(submitLoginForm);
        EntityUtils.consumeQuietly(authCodeResponse.getEntity());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, authCodeResponse.getStatusLine().getStatusCode());
        assertTrue(authCodeResponse.containsHeader(HttpHeaders.LOCATION));
        String redirectLocation = authCodeResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        assertTrue(redirectLocation.startsWith(REDIRECT_URI));

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
            .setScheme(protocol)
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
        System.out.println(jAccessTokenResponse);
    }

}
