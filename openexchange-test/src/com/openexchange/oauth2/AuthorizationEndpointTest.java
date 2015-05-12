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
import java.net.URLDecoder;
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
import com.openexchange.oauth2.requests.GetMailsRequest;
import com.openexchange.oauth2.requests.GetMailsResponse.Message;
import com.openexchange.oauth2.requests.StartSMTPRequest;
import com.openexchange.oauth2.requests.StopSMTPRequest;

/**
 * {@link AuthorizationEndpointTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AuthorizationEndpointTest extends EndpointTest {

    private AJAXClient ajaxClient;

    @Override
    @Before
    public void before() throws Exception {
        super.before();
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
        testGETWithMissingParameter("response_type", "invalid_request");
        testGETWithInvalidParameter("response_type", "unsupported_response_type");
    }

    @Test
    public void testGETWithWrongClientId() throws Exception {
        testGETWithMissingParameter("client_id", null);
        testGETWithInvalidParameter("client_id", null);
    }

    @Test
    public void testGETWithWrongRedirectURI() throws Exception {
        testGETWithMissingParameter("redirect_uri", null);
        testGETWithInvalidParameter("redirect_uri", null);
    }

    @Test
    public void testGETWithWrongScope() throws Exception {
        testGETWithInvalidParameter("scope", "invalid_scope");
    }

    @Test
    public void testGETWithWrongState() throws Exception {
        testGETWithMissingParameter("state", "invalid_request");
    }

    @Test
    public void testPOSTWithMissingReferer() throws Exception {
        HttpResponse redirectResponse = OAuthSession.requestAuthorization(client, hostname, getClientId(), getRedirectURI(), csrfState, getScopes());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, redirectResponse.getStatusLine().getStatusCode());

        // Send a valid POST request with missing referer header
        LinkedList<NameValuePair> authFormParams = new LinkedList<>();
        authFormParams.add(new BasicNameValuePair("user_login", login));
        authFormParams.add(new BasicNameValuePair("user_password", password));
        authFormParams.add(new BasicNameValuePair("access_denied", "false"));
        Map<String, String> additionalParams = OAuthSession.extractFragmentParams(new URI(getRedirectLocation(redirectResponse)).getFragment());
        for (Entry<String, String> entry : additionalParams.entrySet()) {
            authFormParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        HttpPost submitLoginForm = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .build());
        submitLoginForm.setEntity(new UrlEncodedFormEntity(authFormParams));
        HttpResponse response = client.execute(submitLoginForm);

        EntityUtils.consume(response.getEntity());
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertTrue(response.containsHeader(HttpHeaders.CONTENT_TYPE));
        String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("text/html"));
    }

    @Test
    public void testPOSTWithInvalidReferer() throws Exception {
        HttpResponse redirectResponse = OAuthSession.requestAuthorization(client, hostname, getClientId(), getRedirectURI(), csrfState, getScopes());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, redirectResponse.getStatusLine().getStatusCode());

        // Send a valid POST request with wrong referer header
        LinkedList<NameValuePair> authFormParams = new LinkedList<>();
        authFormParams.add(new BasicNameValuePair("user_login", login));
        authFormParams.add(new BasicNameValuePair("user_password", password));
        authFormParams.add(new BasicNameValuePair("access_denied", "false"));
        Map<String, String> additionalParams = OAuthSession.extractFragmentParams(new URI(getRedirectLocation(redirectResponse)).getFragment());
        for (Entry<String, String> entry : additionalParams.entrySet()) {
            authFormParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }

        HttpPost submitLoginForm = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .build());
        submitLoginForm.setHeader(HttpHeaders.REFERER, "https://potential-csrf.attacker.com");
        submitLoginForm.setEntity(new UrlEncodedFormEntity(authFormParams));
        HttpResponse response = client.execute(submitLoginForm);

        EntityUtils.consume(response.getEntity());
        assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
        assertTrue(response.containsHeader(HttpHeaders.CONTENT_TYPE));
        String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("text/html"));
    }

    @Test
    public void testPOSTWithInvalidCSRFToken() throws Exception {
        testPOSTWithMissingAndInvalidParameter("csrf_token", ResponseType.ERROR_PAGE);
    }

    @Test
    public void testPOSTWithInvalidClientId() throws Exception {
        testPOSTWithMissingAndInvalidParameter("client_id", ResponseType.ERROR_PAGE);
    }

    @Test
    public void testPOSTWithInvalidScope() throws Exception {
        testPOSTWithInvalidParameter("scope", ResponseType.REDIRECT, false, "invalid_scope");
    }

    @Test
    public void testPOSTWithoutState() throws Exception {
        testPOSTWithInvalidParameter("state", ResponseType.REDIRECT, true);
    }

    @Test
    public void testPOSTWithInvalidResponseType() throws Exception {
        testPOSTWithInvalidParameter("response_type", ResponseType.REDIRECT, true, "invalid_request");
        testPOSTWithInvalidParameter("response_type", ResponseType.REDIRECT, false, "unsupported_response_type");
    }

    @Test
    public void testPOSTWithInvalidRedirectURI() throws Exception {
        testPOSTWithMissingAndInvalidParameter("redirect_uri", ResponseType.ERROR_PAGE);
    }

    @Test
    public void testPOSTWithInvalidUserLogin() throws Exception {
        testPOSTWithInvalidParameter("user_login", ResponseType.ERROR_JSON, true, "LGI-0006");
        testPOSTWithInvalidParameter("user_login", ResponseType.ERROR_JSON, false, "LGI-0006");
    }

    @Test
    public void testPOSTWithInvalidUserPassword() throws Exception {
        testPOSTWithInvalidParameter("user_password", ResponseType.ERROR_JSON, true, "LGI-0006");
        testPOSTWithInvalidParameter("user_password", ResponseType.ERROR_JSON, false, "LGI-0006");
    }

    private void testPOSTWithMissingAndInvalidParameter(String param, ResponseType responseType) throws Exception {
        testPOSTWithInvalidParameter(param, responseType, true);
        testPOSTWithInvalidParameter(param, responseType, false);
    }

    private static enum ResponseType {
        ERROR_JSON,
        ERROR_PAGE,
        REDIRECT
    }

    private void testPOSTWithInvalidParameter(String param, ResponseType responseType, boolean omitParam) throws Exception {
        testPOSTWithInvalidParameter(param, responseType, omitParam, "invalid_request");
    }

    /**
     * Performs the POST request and uses an invalid value for the given param or omits it at all.
     * @param param The param
     * @param responseType The expected response type
     * @param omitParam <code>true</code> if the param shall be omitted at all
     * @param errorCode The expected error code
     * @throws Exception
     */
    private void testPOSTWithInvalidParameter(String param, ResponseType responseType, boolean omitParam, String errorCode) throws Exception {
        HttpResponse redirectResponse = OAuthSession.requestAuthorization(client, hostname, getClientId(), getRedirectURI(), csrfState, getScopes());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, redirectResponse.getStatusLine().getStatusCode());

        String loginRedirectLocation = getRedirectLocation(redirectResponse);
        Map<String, String> fragmentParams = OAuthSession.extractFragmentParams(new URI(loginRedirectLocation).getFragment());
        Map<String, String> postParams = new HashMap<>(fragmentParams);
        postParams.put("response_type", "code");
        postParams.put("client_id", getClientId());
        postParams.put("redirect_uri", getRedirectURI());
        postParams.put("scope", getScopes());
        postParams.put("state", csrfState);
        postParams.put("user_login", login);
        postParams.put("user_password", password);
        postParams.put("access_denied", "false");
        postParams.put("csrf_token", postParams.get("csrf_token"));

        LinkedList<NameValuePair> authFormParams = new LinkedList<>();
        for (Entry<String, String> entry : postParams.entrySet()) {
            if (entry.getKey().equals(param)) {
                if (!omitParam) {
                    authFormParams.add(new BasicNameValuePair(entry.getKey(), "invalid"));
                }
            } else {
                authFormParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
        }

        HttpPost submitLoginForm = new HttpPost(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .build());
        submitLoginForm.setHeader(HttpHeaders.REFERER, "https://" + hostname + AUTHORIZATION_ENDPOINT);
        submitLoginForm.setEntity(new UrlEncodedFormEntity(authFormParams));

        HttpResponse response = client.execute(submitLoginForm);
        if (responseType == ResponseType.REDIRECT) {
            OAuthSession.releaseConnectionOnRedirect(response);
            assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, response.getStatusLine().getStatusCode());
            assertTrue("Location header missing in redirect response", response.containsHeader(HttpHeaders.LOCATION));
            String redirectLocation = getRedirectLocation(response);
            assertTrue("Unexpected redirect location: " + redirectLocation, redirectLocation.startsWith(getRedirectURI()));

            Map<String, String> redirectParams = new HashMap<>();
            String[] redirectParamPairs = URLDecoder.decode(new URI(redirectLocation).getRawQuery(), "UTF-8").split("&");
            for (String pair : redirectParamPairs) {
                String[] split = pair.split("=");
                redirectParams.put(split[0], split[1]);
            }

            if (errorCode != null) {
                assertEquals(errorCode, redirectParams.get("error"));
            }
            assertFalse(redirectParams.containsKey("code"));
            if (!"state".equals(param)) {
                String state = redirectParams.get("state");
                assertEquals(csrfState, state);
            }
        } else if (responseType == ResponseType.ERROR_PAGE) {
            EntityUtils.consume(response.getEntity());
            assertEquals(HttpStatus.SC_BAD_REQUEST, response.getStatusLine().getStatusCode());
            assertTrue(response.containsHeader(HttpHeaders.CONTENT_TYPE));
            String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
            assertNotNull(contentType);
            assertTrue(contentType.startsWith("text/html"));
            // TODO: validate page content?
        } else {
            String responseBody = EntityUtils.toString(response.getEntity());
            assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
            assertTrue(response.containsHeader(HttpHeaders.CONTENT_TYPE));
            String contentType = response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
            assertNotNull(contentType);
            assertTrue(contentType.startsWith("application/json"));
            JSONObject jError = new JSONObject(responseBody);
            if (errorCode != null) {
                assertEquals(errorCode, jError.getString("code"));
            }
        }
    }

    @Test
    public void testPOSTTriggersMail() throws Exception {
        // Obtain access
        new OAuthSession(User.User1, getClientId(), getClientSecret(), getRedirectURI(), getScopes());

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

    private void testGETWithMissingParameter(String param, String error) throws Exception {
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

        if ("client_id".equals(param) || "redirect_uri".equals(param)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, loginFormResponse.getStatusLine().getStatusCode());
        } else {
            assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, loginFormResponse.getStatusLine().getStatusCode());
            Map<String, String> queryParams = OAuthSession.extractQueryParams(getRedirectLocation(loginFormResponse));
            assertEquals(error, queryParams.get("error"));
        }
    }

    private void testGETWithInvalidParameter(String param, String error) throws Exception {
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

        if ("client_id".equals(param) || "redirect_uri".equals(param)) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, loginFormResponse.getStatusLine().getStatusCode());
        } else {
            assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, loginFormResponse.getStatusLine().getStatusCode());
            Map<String, String> queryParams = OAuthSession.extractQueryParams(getRedirectLocation(loginFormResponse));
            assertEquals(error, queryParams.get("error"));
        }
    }

    private static String getRedirectLocation(HttpResponse response) {
        return response.getFirstHeader(HttpHeaders.LOCATION).getValue();
    }

}
