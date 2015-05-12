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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.framework.AJAXSession;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.html.internal.parser.HtmlHandler;
import com.openexchange.html.internal.parser.HtmlParser;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth.provider.DefaultScopes;


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
        HttpClientParams.setRedirecting(params, false);
        HttpClientParams.setAuthenticating(params, false);
        return client;
    }

    public static HttpResponse requestAuthorization(HttpClient client, String hostname, String clientId, String redirectURI, String state, String scope, String... queryParams) throws Exception {
        URIBuilder getLoginRedirectBuilder = new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(EndpointTest.AUTHORIZATION_ENDPOINT)
            .setParameter("response_type", "code")
            .setParameter("client_id", clientId)
            .setParameter("redirect_uri", redirectURI)
            .setParameter("state", state);
        if (scope != null) {
            getLoginRedirectBuilder.setParameter("scope", scope);
        }

        if (queryParams != null && queryParams.length > 0) {
            for (int i = 0; i < queryParams.length;) {
                getLoginRedirectBuilder.setParameter(queryParams[i++], queryParams[i++]);
            }
        }

        HttpGet getLoginRedirect = new HttpGet(getLoginRedirectBuilder.build());
        HttpResponse loginRedirectResponse = client.execute(getLoginRedirect);
        releaseConnectionOnRedirect(loginRedirectResponse);
        return loginRedirectResponse;
    }

    public static HttpResponse performAuthorization(HttpClient client, String hostname, String clientId, String redirectURI, String state, String scope, String csrfToken, String login, String password, String... formParams) throws Exception {
        LinkedList<NameValuePair> authFormParams = new LinkedList<>();
        authFormParams.add(new BasicNameValuePair("user_login", login));
        authFormParams.add(new BasicNameValuePair("user_password", password));
        authFormParams.add(new BasicNameValuePair("access_denied", "false"));
        authFormParams.add(new BasicNameValuePair("client_id", clientId));
        authFormParams.add(new BasicNameValuePair("state", state));
        authFormParams.add(new BasicNameValuePair("redirect_uri", redirectURI));
        authFormParams.add(new BasicNameValuePair("response_type", "code"));
        authFormParams.add(new BasicNameValuePair("csrf_token", csrfToken));
        if (scope != null) {
            authFormParams.add(new BasicNameValuePair("scope", scope));
        }

        if (formParams != null && formParams.length > 0) {
            for (int i = 0; i < formParams.length;) {
                authFormParams.add(new BasicNameValuePair(formParams[i++], formParams[i++]));
            }
        }

        HttpPost submitLoginForm = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(EndpointTest.AUTHORIZATION_ENDPOINT)
            .build());
        submitLoginForm.setHeader(HttpHeaders.REFERER, "https://" + hostname + EndpointTest.AUTHORIZATION_ENDPOINT);
        submitLoginForm.setEntity(new UrlEncodedFormEntity(authFormParams));

        HttpResponse authCodeResponse = client.execute(submitLoginForm);
        releaseConnectionOnRedirect(authCodeResponse);
        return authCodeResponse;
    }

    public static HttpResponse redeemAuthCode(HttpClient client, String hostname, String clientId, String clientSecret, String code, String redirectURI, String... formParams) throws Exception {
        LinkedList<NameValuePair> redeemAuthCodeParams = new LinkedList<>();
        redeemAuthCodeParams.add(new BasicNameValuePair("client_id", clientId));
        redeemAuthCodeParams.add(new BasicNameValuePair("client_secret", clientSecret));
        redeemAuthCodeParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        redeemAuthCodeParams.add(new BasicNameValuePair("redirect_uri", redirectURI));
        redeemAuthCodeParams.add(new BasicNameValuePair("code", code));

        if (formParams != null && formParams.length > 0) {
            for (int i = 0; i < formParams.length;) {
                redeemAuthCodeParams.add(new BasicNameValuePair(formParams[i++], formParams[i++]));
            }
        }

        HttpPost redeemAuthCode = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(EndpointTest.TOKEN_ENDPOINT)
            .build());
        redeemAuthCode.setEntity(new UrlEncodedFormEntity(redeemAuthCodeParams));

        HttpResponse accessTokenResponse = client.execute(redeemAuthCode);
        releaseConnectionOnRedirect(accessTokenResponse);
        return accessTokenResponse;
    }

    public static JSONObject extractJSON(HttpResponse response) throws JSONException, ParseException, IOException {
        JSONObject jsonObject = new JSONObject(EntityUtils.toString(response.getEntity()));
        if (jsonObject.has("data")) {
            return jsonObject.getJSONObject("data");
        }

        return jsonObject;
    }

    public static Map<String, String> extractQueryParams(String uri) throws UnsupportedEncodingException, URISyntaxException {
        Map<String, String> params = new HashMap<>();
        String[] redirectParamPairs = URLDecoder.decode(new URI(uri).getRawQuery(), "UTF-8").split("&");
        for (String pair : redirectParamPairs) {
            String[] split = pair.split("=");
            params.put(split[0], split[1]);
        }

        return params;
    }

    private void obtainAccess(User user, HttpClient client) throws Exception {
        String hostname = AJAXConfig.getProperty(AJAXConfig.Property.HOSTNAME);
        String login = AJAXConfig.getProperty(user.getLogin()) + "@" + AJAXConfig.getProperty(AJAXConfig.Property.CONTEXTNAME);
        String password = AJAXConfig.getProperty(user.getPassword());
        String state = UUIDs.getUnformattedStringFromRandom();
        String scope = new DefaultScopes(scopes).scopeString();
        HttpResponse loginRedirectResponse = requestAuthorization(client, hostname, clientId, redirectURI, state, scope);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, loginRedirectResponse.getStatusLine().getStatusCode());
        URI location = new URI(loginRedirectResponse.getFirstHeader(HttpHeaders.LOCATION).getValue());
        Map<String, String> parameters = extractFragmentParams(location.getFragment());

        HttpResponse authCodeResponse = performAuthorization(client, hostname, clientId, redirectURI, state, scope, parameters.get("csrf_token"), login, password);
        assertEquals("Unexpected status code", HttpStatus.SC_OK, authCodeResponse.getStatusLine().getStatusCode());
        JSONObject authCodeJSON = extractJSON(authCodeResponse);
        assertFalse(authCodeJSON.toString(), authCodeJSON.has("error"));
        String redirectLocation = authCodeJSON.getString("redirect_uri");
        assertTrue("Unexpected redirect location: " + redirectLocation, redirectLocation.startsWith(redirectURI));

        Map<String, String> redirectParams = extractQueryParams(redirectLocation);
        assertFalse(redirectParams.get("error_description"), redirectParams.containsKey("error"));
        String returnedState = redirectParams.get("state");
        assertEquals(state, returnedState);
        String code = redirectParams.get("code");
        assertNotNull(code);

        HttpResponse accessTokenResponse = redeemAuthCode(client, hostname, clientId, clientSecret, code, redirectURI);
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

    /**
     * Parses parameters out of an URI fragment
     * @param fragment The decoded URI fragment
     * @return A map of parameters
     */
    public static Map<String, String> extractFragmentParams(String fragment) {
        Map<String, String> parameters = new HashMap<>();
        String[] kvPairs = fragment.split("&");
        for (String kvPair : kvPairs) {
            int idx = kvPair.indexOf('=');
            if (idx < 0) {
                continue;
            }

            String key = kvPair.substring(0, idx);
            String value = kvPair.substring(idx + 1);
            parameters.put(key, value);
        }

        return parameters;
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

    public static void releaseConnectionOnRedirect(HttpResponse response) {
        /*
         * A custom grizzly extension always produces HTML output on redirects.
         * We need to consume this entity or the connection cannot be re-used.
         */
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == HttpStatus.SC_MOVED_PERMANENTLY || statusCode == HttpStatus.SC_MOVED_TEMPORARILY) {
            EntityUtils.consumeQuietly(response.getEntity());
        }
    }
}
