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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.util.Iterator;
import java.util.LinkedList;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;

/**
 * {@link TokenEndpointTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class TokenEndpointTest extends EndpointTest {

    @Test
    public void testPOSTWithWrongProtocol() throws Exception {
        LinkedList<NameValuePair> tokenParams = new LinkedList<>();
        tokenParams.add(new BasicNameValuePair("param", "value"));

        HttpPost redeemToken = new HttpPost(new URIBuilder().setScheme("http").setHost(HOSTNAME).setPath(TOKEN_ENDPOINT).build());
        redeemToken.setEntity(new UrlEncodedFormEntity(tokenParams));

        HttpResponse tokenResponse = executeAndConsume(redeemToken);
        expectSecureRedirect(redeemToken, tokenResponse);
    }

    @Test
    public void testRedeemAuthCodeWithInvalidClientId() throws Exception {
        testRedeemAuthCodeWithMissingAndInvalidParam("client_id");
    }

    @Test
    public void testRedeemAuthCodeWithInvalidClientSecret() throws Exception {
        testRedeemAuthCodeWithMissingAndInvalidParam("client_secret");
    }

    @Test
    public void testRedeemAuthCodeWithInvalidGrantType() throws Exception {
        testRedeemAuthCodeWithMissingAndInvalidParam("grant_type");
    }

    @Test
    public void testRedeemAuthCodeWithInvalidCode() throws Exception {
        testRedeemAuthCodeWithMissingAndInvalidParam("code");
    }

    @Test
    public void testRedeemAuthCodeWithInvalidRedirectURI() throws Exception {
        testRedeemAuthCodeWithMissingAndInvalidParam("redirect_uri");
    }

    @Test
    public void testRedeemRefreshTokenWithInvalidClientId() throws Exception {
        testRedeemRefreshTokenWithMissingAndInvalidParam("client_id");
    }

    @Test
    public void testRedeemRefreshTokenWithInvalidClientSecret() throws Exception {
        testRedeemRefreshTokenWithMissingAndInvalidParam("client_secret");
    }

    @Test
    public void testRedeemRefreshTokenWithInvalidGrantType() throws Exception {
        testRedeemRefreshTokenWithMissingAndInvalidParam("grant_type");
    }

    @Test
    public void testRedeemRefreshTokenWithInvalidRefreshToken() throws Exception {
        testRedeemRefreshTokenWithMissingAndInvalidParam("refresh_token");
    }

    private void testRedeemAuthCodeWithMissingAndInvalidParam(String param) throws Exception {
        // 1. omit param
        LinkedList<NameValuePair> necessaryParams = new LinkedList<>();
        necessaryParams.add(new BasicNameValuePair("client_id", getClientId()));
        necessaryParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        necessaryParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        necessaryParams.add(new BasicNameValuePair("code", "invalid"));
        necessaryParams.add(new BasicNameValuePair("redirect_uri", getRedirectURI()));

        LinkedList<NameValuePair> requestParams = new LinkedList<>(necessaryParams);
        Iterator<NameValuePair> it = requestParams.iterator();
        while (it.hasNext()) {
            NameValuePair pair = it.next();
            if (pair.getName().equals(param)) {
                it.remove();
                break;
            }
        }

        HttpPost redeemToken = new HttpPost(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPath(TOKEN_ENDPOINT).build());
        redeemToken.setEntity(new UrlEncodedFormEntity(requestParams));
        HttpResponse tokenResponse = client.execute(redeemToken);
        String responseBody = EntityUtils.toString(tokenResponse.getEntity());
        assertEquals(HttpStatus.SC_BAD_REQUEST, tokenResponse.getStatusLine().getStatusCode());
        assertTrue(tokenResponse.containsHeader(HttpHeaders.CONTENT_TYPE));
        String contentType = tokenResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("application/json"));
        JSONObject jError = new JSONObject(responseBody);
        String errorCode = "invalid_request";
        assertEquals(errorCode, jError.getString("error"));

        // 2. set invalid value
        requestParams = new LinkedList<>(necessaryParams);
        it = requestParams.iterator();
        while (it.hasNext()) {
            NameValuePair pair = it.next();
            if (pair.getName().equals(param)) {
                it.remove();
                break;
            }
        }
        requestParams.add(new BasicNameValuePair(param, "invalid"));
        redeemToken = new HttpPost(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPath(TOKEN_ENDPOINT).build());
        tokenResponse = client.execute(redeemToken);
        responseBody = EntityUtils.toString(tokenResponse.getEntity());
        assertEquals(HttpStatus.SC_BAD_REQUEST, tokenResponse.getStatusLine().getStatusCode());
        assertTrue(tokenResponse.containsHeader(HttpHeaders.CONTENT_TYPE));
        contentType = tokenResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("application/json"));
        jError = new JSONObject(responseBody);
        assertEquals(errorCode, jError.getString("error"));
    }

    private void testRedeemRefreshTokenWithMissingAndInvalidParam(String param) throws Exception {
        // 1. omit param
        LinkedList<NameValuePair> necessaryParams = new LinkedList<>();
        necessaryParams.add(new BasicNameValuePair("client_id", getClientId()));
        necessaryParams.add(new BasicNameValuePair("client_secret", getClientSecret()));
        necessaryParams.add(new BasicNameValuePair("grant_type", "authorization_code"));
        necessaryParams.add(new BasicNameValuePair("refresh_token", "invalid"));

        LinkedList<NameValuePair> requestParams = new LinkedList<>(necessaryParams);
        Iterator<NameValuePair> it = requestParams.iterator();
        while (it.hasNext()) {
            NameValuePair pair = it.next();
            if (pair.getName().equals(param)) {
                it.remove();
                break;
            }
        }

        HttpPost redeemToken = new HttpPost(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPath(TOKEN_ENDPOINT).build());
        redeemToken.setEntity(new UrlEncodedFormEntity(requestParams));
        HttpResponse tokenResponse = client.execute(redeemToken);
        String responseBody = EntityUtils.toString(tokenResponse.getEntity());
        assertEquals(HttpStatus.SC_BAD_REQUEST, tokenResponse.getStatusLine().getStatusCode());
        assertTrue(tokenResponse.containsHeader(HttpHeaders.CONTENT_TYPE));
        String contentType = tokenResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("application/json"));
        JSONObject jError = new JSONObject(responseBody);
        String errorCode = "invalid_request";
        assertEquals(errorCode, jError.getString("error"));

        // 2. set invalid value
        requestParams = new LinkedList<>(necessaryParams);
        it = requestParams.iterator();
        while (it.hasNext()) {
            NameValuePair pair = it.next();
            if (pair.getName().equals(param)) {
                it.remove();
                break;
            }
        }
        requestParams.add(new BasicNameValuePair(param, "invalid"));
        redeemToken = new HttpPost(new URIBuilder().setScheme(SCHEME).setHost(HOSTNAME).setPath(TOKEN_ENDPOINT).build());
        tokenResponse = client.execute(redeemToken);
        responseBody = EntityUtils.toString(tokenResponse.getEntity());
        assertEquals(HttpStatus.SC_BAD_REQUEST, tokenResponse.getStatusLine().getStatusCode());
        assertTrue(tokenResponse.containsHeader(HttpHeaders.CONTENT_TYPE));
        contentType = tokenResponse.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue();
        assertNotNull(contentType);
        assertTrue(contentType.startsWith("application/json"));
        jError = new JSONObject(responseBody);
        assertEquals(errorCode, jError.getString("error"));
    }

}
