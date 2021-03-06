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

package com.openexchange.ajax.oauth.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
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
import org.junit.Test;
import com.openexchange.ajax.oauth.provider.protocol.GETRequest;
import com.openexchange.ajax.oauth.provider.protocol.GETResponse;
import com.openexchange.ajax.oauth.provider.protocol.HttpTools;
import com.openexchange.ajax.oauth.provider.protocol.POSTRequest;
import com.openexchange.ajax.oauth.provider.protocol.POSTResponse;

/**
 * {@link AuthorizationEndpointTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AuthorizationEndpointTest extends EndpointTest {

    @Test
    public void testGETWithWrongProtocol() throws Exception {
        HttpGet getLoginForm = new HttpGet(new URIBuilder().setScheme("http").setHost(HOSTNAME).setPath(AUTHORIZATION_ENDPOINT).setParameter("response_type", "code").setParameter("client_id", getClientId()).setParameter("redirect_uri", getRedirectURI()).setParameter("scope", getScope().toString()).setParameter("state", csrfState).build());
        HttpResponse loginFormResponse = executeAndConsume(getLoginForm);
        expectSecureRedirect(getLoginForm, loginFormResponse);
    }

    @Test
    public void testPOSTWithWrongProtocol() throws Exception {
        LinkedList<NameValuePair> authFormParams = new LinkedList<>();
        authFormParams.add(new BasicNameValuePair("param", "value"));

        HttpPost submitLoginForm = new HttpPost(new URIBuilder().setScheme("http").setHost(HOSTNAME).setPath(AUTHORIZATION_ENDPOINT).build());
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
        testPOSTWithMissingOrInvalidReferer();
    }

    /**
     * The form response must not be embedded via iframe within a foreign site to avoid clickjacking attacks.
     * Therefore the form response must always contain the header <code>X-Frame-Options: SAMEORIGIN</code>.
     */
    @Test
    public void testXFrameOptions() throws Exception {
        GETRequest getLoginForm = new GETRequest().setScheme(SCHEME).setHostname(HOSTNAME).setPort(PORT).setClientId(getClientId()).setRedirectURI(getRedirectURI()).setState(csrfState).setScope(getScope().toString());
        GETResponse loginFormResponse = getLoginForm.execute(client);
        String frameOptions = loginFormResponse.getHeader("X-Frame-Options");
        assertEquals("SAMEORIGIN", frameOptions);
    }

    private void testPOSTWithMissingOrInvalidReferer() throws Exception {
        GETRequest getLoginForm = new GETRequest().setScheme(SCHEME).setHostname(HOSTNAME).setPort(PORT).setClientId(getClientId()).setRedirectURI(getRedirectURI()).setState(csrfState).setScope(getScope().toString());
        GETResponse loginFormResponse = getLoginForm.execute(client);
        POSTRequest loginRequest = loginFormResponse.preparePOSTRequest().setLogin(testUser.getLogin()).setPassword(testUser.getPassword()).setHeader(HttpHeaders.REFERER, null);
        POSTResponse loginResponse = loginRequest.submit(client);
        String content = loginResponse.getBodyAsString();
        assertEquals(HttpStatus.SC_BAD_REQUEST, loginResponse.getStatusCode());
        assertTrue(loginResponse.containsHeader(HttpHeaders.CONTENT_TYPE));
        String contentType = loginResponse.getHeader(HttpHeaders.CONTENT_TYPE);
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("text/html"));
        assertTrue(content.contains("Missing or invalid referer."));
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
        testPOSTWithInvalidParameter("scope", ResponseType.REDIRECT_TO_CLIENT, false, "invalid_scope");
    }

    @Test
    public void testPOSTWithoutState() throws Exception {
        testPOSTWithInvalidParameter("state", ResponseType.REDIRECT_TO_CLIENT, true);
    }

    @Test
    public void testPOSTWithInvalidResponseType() throws Exception {
        testPOSTWithInvalidParameter("response_type", ResponseType.REDIRECT_TO_CLIENT, true, "invalid_request");
        testPOSTWithInvalidParameter("response_type", ResponseType.REDIRECT_TO_CLIENT, false, "unsupported_response_type");
    }

    @Test
    public void testPOSTWithInvalidRedirectURI() throws Exception {
        testPOSTWithMissingAndInvalidParameter("redirect_uri", ResponseType.ERROR_PAGE);
    }

    @Test
    public void testPOSTWithInvalidUserLogin() throws Exception {
        testPOSTWithInvalidParameter("login", ResponseType.REDIRECT_TO_LOGIN_FORM, true, "1");
        testPOSTWithInvalidParameter("login", ResponseType.REDIRECT_TO_LOGIN_FORM, false, "1");
    }

    @Test
    public void testPOSTWithInvalidUserPassword() throws Exception {
        testPOSTWithInvalidParameter("password", ResponseType.REDIRECT_TO_LOGIN_FORM, true, "1");
        testPOSTWithInvalidParameter("password", ResponseType.REDIRECT_TO_LOGIN_FORM, false, "1");
    }

    private void testPOSTWithMissingAndInvalidParameter(String param, ResponseType responseType) throws Exception {
        testPOSTWithInvalidParameter(param, responseType, true);
        testPOSTWithInvalidParameter(param, responseType, false);
    }

    private static enum ResponseType {
        ERROR_PAGE,
        REDIRECT_TO_LOGIN_FORM,
        REDIRECT_TO_CLIENT
    }

    private void testPOSTWithInvalidParameter(String param, ResponseType responseType, boolean omitParam) throws Exception {
        testPOSTWithInvalidParameter(param, responseType, omitParam, "invalid_request");
    }

    /**
     * Performs the login POST request and uses an invalid value for the given param or omits it at all.
     *
     * @param param The param
     * @param responseType The expected response type
     * @param omitParam <code>true</code> if the param shall be omitted at all
     * @param errorCode The expected error code
     * @throws Exception
     */
    private void testPOSTWithInvalidParameter(String param, ResponseType responseType, boolean omitParam, String errorCode) throws Exception {
        GETRequest getLoginForm = new GETRequest().setScheme(SCHEME).setHostname(HOSTNAME).setPort(PORT).setClientId(getClientId()).setRedirectURI(getRedirectURI()).setState(csrfState);
        GETResponse loginFormResponse = getLoginForm.execute(client);
        POSTRequest loginRequest = loginFormResponse.preparePOSTRequest().setLogin(testUser.getLogin()).setPassword(testUser.getPassword());

        // Invalidate param
        if (omitParam) {
            loginRequest.setParameter(param, null);
        } else {
            loginRequest.setParameter(param, "invalid");
        }

        POSTResponse loginResponse = loginRequest.submit(client);
        if (responseType == ResponseType.REDIRECT_TO_CLIENT) {
            loginResponse.assertRedirect();
            URI redirectLocationURI = loginResponse.getRedirectLocation();
            String redirectLocation = redirectLocationURI.toString();
            assertTrue("Unexpected redirect location: " + redirectLocation, redirectLocation.startsWith(getRedirectURI()));
            Map<String, String> redirectParams = HttpTools.extractQueryParams(redirectLocationURI);
            if (errorCode != null) {
                assertEquals(errorCode, redirectParams.get("error"));
            }
            assertFalse(redirectParams.containsKey("code"));
            if (!"state".equals(param)) {
                String state = redirectParams.get("state");
                assertEquals(csrfState, state);
            }
        } else if (responseType == ResponseType.ERROR_PAGE) {
            assertEquals(HttpStatus.SC_BAD_REQUEST, loginResponse.getStatusCode());
            assertTrue(loginResponse.containsHeader(HttpHeaders.CONTENT_TYPE));
            String contentType = loginResponse.getHeader(HttpHeaders.CONTENT_TYPE);
            assertNotNull(contentType);
            assertTrue(contentType.startsWith("text/html"));
        } else if (responseType == ResponseType.REDIRECT_TO_LOGIN_FORM) {
            loginResponse.assertRedirect();
            Map<String, String> redirectParams = HttpTools.extractQueryParams(loginResponse.getRedirectLocation());
            if (errorCode != null) {
                assertEquals(errorCode, redirectParams.get("error"));
            }
        } else {
            fail("Unknown response type: " + responseType);
        }
    }

    private void testGETWithMissingParameter(String param, String error) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", getClientId());
        params.put("redirect_uri", getRedirectURI());
        params.put("scope", getScope().toString());
        params.put("state", csrfState);

        URI baseUri = new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPath(AUTHORIZATION_ENDPOINT).build();

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
            Map<String, String> queryParams = HttpTools.extractQueryParams(HttpTools.getRedirectLocation(loginFormResponse));
            assertEquals(error, queryParams.get("error"));
        }
    }

    private void testGETWithInvalidParameter(String param, String error) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", getClientId());
        params.put("redirect_uri", getRedirectURI());
        params.put("scope", getScope().toString());
        params.put("state", csrfState);

        URI baseUri = new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPath(AUTHORIZATION_ENDPOINT).build();

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
            Map<String, String> queryParams = HttpTools.extractQueryParams(HttpTools.getRedirectLocation(loginFormResponse));
            assertEquals(error, queryParams.get("error"));
        }
    }
}
