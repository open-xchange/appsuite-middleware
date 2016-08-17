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

import static org.junit.Assert.assertEquals;
import java.io.InputStreamReader;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.json.JSONObject;
import org.junit.Test;


/**
 * {@link RevokeTokensTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class RevokeTokensTest extends EndpointTest {

    @Test
    public void testRevokeAccessToken() throws Exception {
        OAuthClient oauthClient = new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScope());
        oauthClient.assertAccess();

        HttpGet revoke = new HttpGet(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(REVOKE_ENDPOINT)
            .setParameter("access_token", ((OAuthSession) oauthClient.getSession()).getAccessToken())
            .build());
        HttpResponse revokeResponse = client.execute(revoke);
        revoke.reset();
        assertEquals(HttpStatus.SC_OK, revokeResponse.getStatusLine().getStatusCode());
        assertNoAccess(oauthClient);
    }

    @Test
    public void testRevokeRefreshToken() throws Exception {
        OAuthClient oauthClient = new OAuthClient(getClientId(), getClientSecret(), getRedirectURI(), getScope());
        oauthClient.assertAccess();

        HttpGet revoke = new HttpGet(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(REVOKE_ENDPOINT)
            .setParameter("refresh_token", ((OAuthSession) oauthClient.getSession()).getRefreshToken())
            .build());
        HttpResponse revokeResponse = client.execute(revoke);
        assertEquals(HttpStatus.SC_OK, revokeResponse.getStatusLine().getStatusCode());
        assertNoAccess(oauthClient);
    }

    @Test
    public void testFailWithInvalidTokens() throws Exception {
        // missing token parameter
        HttpGet revoke = new HttpGet(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(REVOKE_ENDPOINT)
            .build());
        HttpResponse revokeResponse = client.execute(revoke);
        assertEquals(HttpStatus.SC_BAD_REQUEST, revokeResponse.getStatusLine().getStatusCode());
        JSONObject errorObject = JSONObject.parse(new InputStreamReader(revokeResponse.getEntity().getContent(), revokeResponse.getEntity().getContentEncoding() == null ? "UTF-8" : revokeResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", errorObject.getString("error"));

        // illegal access token
        revoke = new HttpGet(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(REVOKE_ENDPOINT)
            .setParameter("access_token", "invalid")
            .build());
        revokeResponse = client.execute(revoke);
        assertEquals(HttpStatus.SC_BAD_REQUEST, revokeResponse.getStatusLine().getStatusCode());
        errorObject = JSONObject.parse(new InputStreamReader(revokeResponse.getEntity().getContent(), revokeResponse.getEntity().getContentEncoding() == null ? "UTF-8" : revokeResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", errorObject.getString("error"));

        // illegal refresh token
        revoke = new HttpGet(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(REVOKE_ENDPOINT)
            .setParameter("refresh_token", "invalid")
            .build());
        revokeResponse = client.execute(revoke);
        assertEquals(HttpStatus.SC_BAD_REQUEST, revokeResponse.getStatusLine().getStatusCode());
        errorObject = JSONObject.parse(new InputStreamReader(revokeResponse.getEntity().getContent(), revokeResponse.getEntity().getContentEncoding() == null ? "UTF-8" : revokeResponse.getEntity().getContentEncoding().getValue())).toObject();
        assertEquals("invalid_request", errorObject.getString("error"));
    }

}
