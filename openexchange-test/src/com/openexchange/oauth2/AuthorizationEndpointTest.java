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
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.java.util.UUIDs;
import com.openexchange.oauth2.requests.GetMailsRequest;
import com.openexchange.oauth2.requests.GetMailsResponse.Message;
import com.openexchange.oauth2.requests.StartSMTPRequest;
import com.openexchange.oauth2.requests.StopSMTPRequest;
import com.openexchange.oauth.provider.OAuthProviderConstants;
import com.openexchange.oauth2.utils.OAuthTestUtils;

/**
 * {@link AuthorizationEndpointTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AuthorizationEndpointTest extends EndpointTest {

    private String csrfState;
    private AJAXClient ajaxClient;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        csrfState = UUIDs.getUnformattedStringFromRandom();
        /*
         * start dummy smtp to catch password-reset mail
         */
        ajaxClient = new AJAXClient(User.User1);
        try {
            StartSMTPRequest startSMTPReqeuest = new StartSMTPRequest(false);
            startSMTPReqeuest.setUpdateNoReplyForContext(ajaxClient.getValues().getContextId());
            ajaxClient.execute(startSMTPReqeuest);
        } catch (Exception e) {
            after();
            throw e;
        }
    }

    @Override
    @After
    public void after() throws Exception {
        ajaxClient.execute(new StopSMTPRequest());
    }

    @Test
    public void testGETWithWrongProtocol() throws Exception {
        HttpGet getLoginForm = new HttpGet(new URIBuilder()
            .setScheme("http")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .setParameter("response_type", "code")
            .setParameter("client_id", getClientId())
            .setParameter("redirect_uri", getRedirectURI())
            .setParameter("scope", getScopes())
            .setParameter("state", csrfState)
            .build());
        HttpResponse loginFormResponse = executeAndConsume(getLoginForm);
        expectSecureRedirect(getLoginForm, loginFormResponse);
    }

    @Test
    public void testPOSTWithWrongProtocol() throws Exception {
        LinkedList<NameValuePair> authFormParams = new LinkedList<>();
        authFormParams.add(new BasicNameValuePair("param", "value"));

        HttpPost submitLoginForm = new HttpPost(new URIBuilder()
            .setScheme("http")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .build());
        submitLoginForm.setEntity(new UrlEncodedFormEntity(authFormParams));

        HttpResponse authCodeResponse = executeAndConsume(submitLoginForm);
        expectSecureRedirect(submitLoginForm, authCodeResponse);
    }

    @Test
    public void testGETWithWrongResponseType() throws Exception {
        testGETWithMissingAndInvalidParameter("response_type");
    }

    @Test
    public void testGETWithWrongClientId() throws Exception {
        testGETWithMissingAndInvalidParameter("client_id");
    }

    @Test
    public void testGETWithWrongRedirectURI() throws Exception {
        testGETWithMissingAndInvalidParameter("redirect_uri");
    }

    @Test
    public void testGETWithWrongScope() throws Exception {
        testGETWithInvalidParameter("scope");
    }

    @Test
    public void testGETWithWrongState() throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", getClientId());
        params.put("redirect_uri", getRedirectURI());
        params.put("scope", getScopes());
        params.put("state", csrfState);

        URI baseUri = new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .build();

        // 1. missing param
        URIBuilder uriBuilder = new URIBuilder(baseUri);
        for (Entry<String, String> p : params.entrySet()) {
            if (!p.getKey().equals("state")) {
                uriBuilder.setParameter(p.getKey(), p.getValue());
            }
        }
        HttpGet getLoginForm = new HttpGet(uriBuilder.build());
        HttpResponse loginFormResponse = executeAndConsume(getLoginForm);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, loginFormResponse.getStatusLine().getStatusCode());
    }

    @Test
    public void testPOSTWithMissingReferer() throws Exception {
        String csrfState = UUIDs.getUnformattedStringFromRandom();
        URI authorizationRequest = prepareAuthorizationRequest(csrfState);
        HttpGet authorizationGetRequest = new HttpGet(authorizationRequest);

        HttpResponse authorizationResponse = client.execute(authorizationGetRequest);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, authorizationResponse.getStatusLine().getStatusCode());
        assertTrue(authorizationResponse.containsHeader(HttpHeaders.LOCATION));

        String redirectLocation = authorizationResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        URIBuilder authenticationRequestURI = prepareAuthenticationRequest(redirectLocation, false, null);
        HttpPost authenticationRequest = new HttpPost(authenticationRequestURI.build());

        HttpResponse authCodeResponse = client.execute(authenticationRequest);

        String authCodeResponseBody = EntityUtils.toString(authCodeResponse.getEntity());
        assertEquals(authCodeResponseBody, HttpStatus.SC_MOVED_TEMPORARILY, authCodeResponse.getStatusLine().getStatusCode());
        assertTrue("Location header missing in redirect response", authCodeResponse.containsHeader(HttpHeaders.LOCATION));
        String redirectLocationAuth = authCodeResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        assertTrue("Unexpected redirect location: " + redirectLocationAuth, redirectLocationAuth.startsWith(getRedirectURI()));
        String contentType = authCodeResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("text/html"));
    }

    @Test
    public void testPOSTWithInvalidReferer() throws Exception {
        String csrfState = UUIDs.getUnformattedStringFromRandom();
        URI authorizationRequest = prepareAuthorizationRequest(csrfState);
        HttpGet authorizationGetRequest = new HttpGet(authorizationRequest);

        HttpResponse authorizationResponse = client.execute(authorizationGetRequest);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, authorizationResponse.getStatusLine().getStatusCode());
        assertTrue(authorizationResponse.containsHeader(HttpHeaders.LOCATION));

        String redirectLocation = authorizationResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        URIBuilder authenticationRequestURI = prepareAuthenticationRequest(redirectLocation, false, null);
        HttpPost authenticationRequest = new HttpPost(authenticationRequestURI.build());
        authenticationRequest.setHeader(HttpHeaders.REFERER, "https://potential-csrf.attacker.com");

        HttpResponse authCodeResponse = client.execute(authenticationRequest);

        String authCodeResponseBody = EntityUtils.toString(authCodeResponse.getEntity());
        assertEquals(authCodeResponseBody, HttpStatus.SC_MOVED_TEMPORARILY, authCodeResponse.getStatusLine().getStatusCode());
        assertTrue("Location header missing in redirect response", authCodeResponse.containsHeader(HttpHeaders.LOCATION));
        String redirectLocationAuth = authCodeResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        assertTrue("Unexpected redirect location: " + redirectLocationAuth, redirectLocationAuth.startsWith(getRedirectURI()));
        String contentType = authCodeResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("text/html"));
    }

    @Test
    public void testPOSTWithInvalidCSRFToken() throws Exception {
        testPOSTWithMissingAndInvalidParameter("csrf_token", ResponseType.REDIRECT);
    }

    @Test
    public void testPOSTWithInvalidClientId() throws Exception {
        testPOSTWithMissingAndInvalidParameter("client_id", ResponseType.REDIRECT);
    }

    @Test
    public void testPOSTWithInvalidScope() throws Exception {
        testPOSTWithInvalidParameter(OAuthProviderConstants.PARAM_SCOPE, ResponseType.REDIRECT, false, "invalid_scope");
    }

    @Test
    public void testPOSTWithInvalidState() throws Exception {
        testPOSTWithInvalidParameter("state", ResponseType.REDIRECT, true);
    }

    @Test
    public void testPOSTWithInvalidResponseType() throws Exception {
        testPOSTWithMissingAndInvalidParameter("response_type", ResponseType.REDIRECT);
    }

    @Test
    public void testPOSTWithInvalidRedirectURI() throws Exception {
        testPOSTWithMissingAndInvalidParameter("redirect_uri", ResponseType.REDIRECT);
    }

    @Test
    public void testPOSTWithInvalidUserLogin() throws Exception {
        testPOSTWithInvalidParameter("user_login", ResponseType.REDIRECT, true);
        testPOSTWithInvalidParameter("user_login", ResponseType.REDIRECT, false, "invalid_grant");
    }

    @Test
    public void testPOSTWithInvalidUserPassword() throws Exception {
        testPOSTWithInvalidParameter("user_password", ResponseType.REDIRECT, true);
        testPOSTWithInvalidParameter("user_password", ResponseType.REDIRECT, false, "invalid_grant");
    }

    private void testPOSTWithMissingAndInvalidParameter(String param, ResponseType responseType) throws Exception {
        testPOSTWithInvalidParameter(param, responseType, true);
        testPOSTWithInvalidParameter(param, responseType, false);
    }

    private static enum ResponseType {
        JSON,
        ERROR_PAGE,
        REDIRECT
    }

    private void testPOSTWithInvalidParameter(String param, ResponseType responseType, boolean omitParam) throws Exception {
        testPOSTWithInvalidParameter(param, responseType, omitParam, "invalid_request");
    }

    /**
     * Performs the POST request and uses an invalid value for the given param or omits it at all.
     *
     * @param param The param
     * @param responseType The expected response type
     * @param omitParam <code>true</code> if the param shall be omitted at all
     * @param errorCode The expected error code
     * @throws Exception
     */
    private void testPOSTWithInvalidParameter(String param, ResponseType responseType, boolean omitParam, String errorCode) throws Exception {
        String csrfState = UUIDs.getUnformattedStringFromRandom();
        URI authorizationRequest = prepareAuthorizationRequest(csrfState);
        HttpGet authorizationGetRequest = new HttpGet(authorizationRequest);

        HttpResponse authorizationResponse = client.execute(authorizationGetRequest);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, authorizationResponse.getStatusLine().getStatusCode());
        assertTrue(authorizationResponse.containsHeader(HttpHeaders.LOCATION));

        String redirectLocation = authorizationResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        URIBuilder authenticationRequestURI = prepareAuthenticationRequest(redirectLocation, omitParam, param);
        HttpPost authenticationRequest = new HttpPost(authenticationRequestURI.build());
        authenticationRequest.setHeader(HttpHeaders.REFERER, authorizationGetRequest.getURI().toString());

        HttpResponse response = client.execute(authenticationRequest);
        if (responseType == ResponseType.REDIRECT) {
            EntityUtils.consumeQuietly(response.getEntity());
            assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusLine().getStatusCode());
            assertTrue("Location header missing in redirect response", response.containsHeader(HttpHeaders.LOCATION));

            if (OAuthProviderConstants.PARAM_REDIRECT_URI.equalsIgnoreCase(param)) {
                return;
            }
            String authRedirectLocation = response.getFirstHeader(HttpHeaders.LOCATION).getValue();
            assertTrue("Unexpected redirect location: " + authRedirectLocation, authRedirectLocation.startsWith(getRedirectURI()));

            Map<String, String> redirectParams = OAuthTestUtils.extractRedirectParamsFromFragment(authRedirectLocation);
            if (errorCode != null) {
                assertEquals(errorCode, redirectParams.get("error"));
            }
            assertFalse(redirectParams.containsKey("code"));
            if (param.equalsIgnoreCase(OAuthProviderConstants.PARAM_STATE) && !omitParam) {
                String state = redirectParams.get(OAuthProviderConstants.PARAM_STATE);
                assertEquals(csrfState, state);
            }
        } else {
            String responseBody = EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
            assertTrue(response.containsHeader(HttpHeaders.CONTENT_TYPE));
            String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
            assertNotNull(contentType);
            assertTrue(contentType.startsWith("application/json"));
            JSONObject jError = new JSONObject(responseBody);
            if (errorCode != null) {
                assertEquals(errorCode, jError.getString("error"));
            }
        }
    }

    @Test
    public void testPOSTReturnsCodeAndState() throws Exception {
        String csrfState = UUIDs.getUnformattedStringFromRandom();

        URI authorizationRequest = prepareAuthorizationRequest(csrfState);
        HttpGet authorizationGetRequest = new HttpGet(authorizationRequest);

        HttpResponse authorizationResponse = client.execute(authorizationGetRequest);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, authorizationResponse.getStatusLine().getStatusCode());
        assertTrue(authorizationResponse.containsHeader(HttpHeaders.LOCATION));

        String redirectLocation = authorizationResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        URIBuilder authenticationRequestURI = prepareAuthenticationRequest(redirectLocation, false, null);
        HttpPost authenticationRequest = new HttpPost(authenticationRequestURI.build());
        authenticationRequest.setHeader(HttpHeaders.REFERER, authorizationGetRequest.getURI().toString());

        HttpResponse authCodeResponse = client.execute(authenticationRequest);

        String authCodeResponseBody = EntityUtils.toString(authCodeResponse.getEntity());
        assertEquals(authCodeResponseBody, HttpStatus.SC_MOVED_TEMPORARILY, authCodeResponse.getStatusLine().getStatusCode());
        assertTrue("Location header missing in redirect response", authCodeResponse.containsHeader(HttpHeaders.LOCATION));
        String redirectLocationAuth = authCodeResponse.getFirstHeader(HttpHeaders.LOCATION).getValue();
        assertTrue("Unexpected redirect location: " + redirectLocationAuth, redirectLocationAuth.startsWith(getRedirectURI()));

        Map<String, String> redirectParamsAuth = OAuthTestUtils.extractRedirectParamsFromFragment(redirectLocationAuth);

        assertFalse(redirectParamsAuth.get("error_description"), redirectParamsAuth.containsKey("error"));
        assertEquals(csrfState, redirectParamsAuth.get("state"));
        String code = redirectParamsAuth.get("code");
        assertNotNull(code);

        // Test notification mail
        List<Message> messages = ajaxClient.execute(new GetMailsRequest()).getMessages();
        assertEquals(1, messages.size());
        Message message = messages.get(0);
        Map<String, String> headers = message.getHeaders();
        String from = headers.get("From");
        assertEquals("Notification not send from no-reply address.", "no-reply@ox.io", from);
        String autogenerated = headers.get("Auto-Submitted");
        assertEquals("Mail not marked as auto-generated.", "auto-generated", autogenerated);
        String subject = headers.get("Subject");
        assertNotNull("Mail subject is null.", subject);
        assertTrue("External application expected in subject.", subject.contains("external"));
        assertTrue("External application expected in subject.", subject.contains("application"));
    }

    private void testGETWithMissingAndInvalidParameter(String param) throws Exception {
        testGETWithMissingParameter(param);
        testGETWithInvalidParameter(param);
    }

    private void testGETWithMissingParameter(String param) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", getClientId());
        params.put("redirect_uri", getRedirectURI());
        params.put("scope", getScopes());
        params.put("state", csrfState);

        URI baseUri = new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .build();

        URIBuilder uriBuilder = new URIBuilder(baseUri);
        for (Entry<String, String> p : params.entrySet()) {
            if (!p.getKey().equals(param)) {
                uriBuilder.setParameter(p.getKey(), p.getValue());
            }
        }
        HttpGet getLoginForm = new HttpGet(uriBuilder.build());
        HttpResponse loginFormResponse = executeAndConsume(getLoginForm);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, loginFormResponse.getStatusLine().getStatusCode());
    }

    private void testGETWithInvalidParameter(String param) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", getClientId());
        params.put("redirect_uri", getRedirectURI());
        params.put("scope", getScopes());
        params.put("state", csrfState);

        URI baseUri = new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .build();

        URIBuilder uriBuilder = new URIBuilder(baseUri);
        for (Entry<String, String> p : params.entrySet()) {
            if (p.getKey().equals(param)) {
                uriBuilder.setParameter(p.getKey(), "invalid");
            } else {
                uriBuilder.setParameter(p.getKey(), p.getValue());
            }
        }
        HttpGet getLoginForm = new HttpGet(uriBuilder.build());
        HttpResponse loginFormResponse = executeAndConsume(getLoginForm);
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, loginFormResponse.getStatusLine().getStatusCode());
    }

}
