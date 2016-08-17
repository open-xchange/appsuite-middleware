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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.mail.BodyPart;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.nodes.Document;
import org.junit.Test;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.AJAXClient.User;
import com.openexchange.ajax.oauth.provider.actions.StartSMTPRequest;
import com.openexchange.ajax.oauth.provider.actions.StopSMTPRequest;
import com.openexchange.ajax.oauth.provider.protocol.GETRequest;
import com.openexchange.ajax.oauth.provider.protocol.GETResponse;
import com.openexchange.ajax.oauth.provider.protocol.HttpTools;
import com.openexchange.ajax.oauth.provider.protocol.POSTRequest;
import com.openexchange.ajax.oauth.provider.protocol.POSTResponse;
import com.openexchange.ajax.smtptest.actions.GetMailsRequest;
import com.openexchange.ajax.smtptest.actions.GetMailsResponse.Message;

/**
 * {@link AuthorizationEndpointTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class AuthorizationEndpointTest extends EndpointTest {

    private AJAXClient ajaxClient;

    @Test
    public void testGETWithWrongProtocol() throws Exception {
        HttpGet getLoginForm = new HttpGet(new URIBuilder()
            .setScheme("http")
            .setHost(hostname)
            .setPath(AUTHORIZATION_ENDPOINT)
            .setParameter("response_type", "code")
            .setParameter("client_id", getClientId())
            .setParameter("redirect_uri", getRedirectURI())
            .setParameter("scope", getScope().toString())
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
        testPOSTWithMissingOrInvalidReferer(true);
    }

    @Test
    public void testPOSTWithInvalidReferer() throws Exception {
        testPOSTWithMissingOrInvalidReferer(false);
    }

    /**
     * The form response must not be embedded via iframe within a foreign site to avoid clickjacking attacks.
     * Therefore the form response must always contain the header <code>X-Frame-Options: SAMEORIGIN</code>.
     */
    @Test
    public void testXFrameOptions() throws Exception {
        GETRequest getLoginForm = new GETRequest()
            .setHostname(hostname)
            .setClientId(getClientId())
            .setRedirectURI(getRedirectURI())
            .setState(csrfState)
            .setScope(getScope().toString());
        GETResponse loginFormResponse = getLoginForm.execute(client);
        String frameOptions = loginFormResponse.getHeader("X-Frame-Options");
        assertEquals("SAMEORIGIN", frameOptions);
    }

    private void testPOSTWithMissingOrInvalidReferer(boolean omit) throws Exception {
        GETRequest getLoginForm = new GETRequest()
            .setHostname(hostname)
            .setClientId(getClientId())
            .setRedirectURI(getRedirectURI())
            .setState(csrfState)
            .setScope(getScope().toString());
        GETResponse loginFormResponse = getLoginForm.execute(client);
        POSTRequest loginRequest = loginFormResponse.preparePOSTRequest()
            .setLogin(login)
            .setPassword(password)
            .setHeader(HttpHeaders.REFERER, null);
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
        GETRequest getLoginForm = new GETRequest()
            .setHostname(hostname)
            .setClientId(getClientId())
            .setRedirectURI(getRedirectURI())
            .setState(csrfState);
        GETResponse loginFormResponse = getLoginForm.execute(client);
        POSTRequest loginRequest = loginFormResponse.preparePOSTRequest()
            .setLogin(login)
            .setPassword(password);

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

    @Test
    public void testPOSTTriggersMail() throws Exception {
        ajaxClient = new AJAXClient(User.User1);
        try {
            // start dummy smtp to catch password-reset mail
            StartSMTPRequest startSMTPReqeuest = new StartSMTPRequest(false);
            startSMTPReqeuest.setUpdateNoReplyForContext(ajaxClient.getValues().getContextId());
            ajaxClient.execute(startSMTPReqeuest);

            // Obtain access
            new OAuthSession(User.User1, getClientId(), getClientSecret(), getRedirectURI(), getScope());

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
            assertTrue("External application name expected in subject.", subject.contains(oauthClient.getName()));
            assertSignatureText(message.requireHtml(), "");
            assertSignatureImage(message);
        } finally {
            ajaxClient.execute(new StopSMTPRequest());
        }
    }

    private void testGETWithMissingParameter(String param, String error) throws Exception {
        Map<String, String> params = new HashMap<>();
        params.put("response_type", "code");
        params.put("client_id", getClientId());
        params.put("redirect_uri", getRedirectURI());
        params.put("scope", getScope().toString());
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
            Map<String, String> queryParams = HttpTools.extractQueryParams(HttpTools.getRedirectLocation(loginFormResponse));
            assertEquals(error, queryParams.get("error"));
        }
    }

    private void assertSignatureText(Document document, String expected) {
        assertEquals(expected, document.getElementById("signature_text").ownText());
    }

    private void assertSignatureImage(Message message) throws Exception {
        String src = message.requireHtml().getElementById("signature_image").attr("src");
        BodyPart image = message.getBodyPartByContentID("<" + src.substring(4) + ">");
        assertNotNull(image);
        byte[] expectedBytes = BaseEncoding.base64().decode(SIGNATURE_IMAGE);
        byte[] imageBytes = ByteStreams.toByteArray(image.getInputStream());
        assertArrayEquals(expectedBytes, imageBytes);
    }

    private final String SIGNATURE_IMAGE = "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";

}
