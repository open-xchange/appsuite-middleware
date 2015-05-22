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

package com.openexchange.ajax.oauth.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import java.net.URI;
import java.util.Map;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.junit.Test;


/**
 * {@link AuthInfoEndpointTest}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class AuthInfoEndpointTest extends EndpointTest {

    @Test
    public void testGetAuthInfo() throws Exception {
        HttpResponse loginRedirectResponse = OAuthSession.requestAuthorization(client, hostname, getClientId(), getRedirectURI(), csrfState, getScope());
        assertEquals(HttpStatus.SC_MOVED_TEMPORARILY, loginRedirectResponse.getStatusLine().getStatusCode());
        URI location = new URI(loginRedirectResponse.getFirstHeader(HttpHeaders.LOCATION).getValue());
        Map<String, String> parameters = OAuthSession.extractFragmentParams(location.getFragment());

        HttpGet getAuthInfo = new HttpGet(new URIBuilder()
            .setScheme("https")
            .setHost(hostname)
            .setPath(AUTH_INFO_ENDPOINT)
            .setParameter("client_id", parameters.get("client_id"))
            .setParameter("scope", parameters.get("scope"))
            .setParameter("csrf_token", parameters.get("csrf_token"))
            .build());
        HttpResponse authInfoResponse = client.execute(getAuthInfo);
        assertEquals(HttpStatus.SC_OK, authInfoResponse.getStatusLine().getStatusCode());
        JSONObject jResponse = new JSONObject(EntityUtils.toString(authInfoResponse.getEntity()));
        assertFalse(jResponse.hasAndNotNull("error"));
        JSONObject jAuthInfo = jResponse.getJSONObject("data");
        JSONObject jClient = jAuthInfo.getJSONObject("client");
        assertEquals(oauthClient.getName(), jClient.get("name"));
        assertEquals(oauthClient.getDescription(), jClient.get("description"));
        assertEquals(oauthClient.getContactAddress(), jClient.get("contact_address"));
        assertEquals(oauthClient.getWebsite(), jClient.get("website"));
        String iconString = jClient.getString("icon");
        // data:image/jpg;charset=UTF-8;base64,<b64-string>
        assertEquals(Base64.encodeBase64String(oauthClient.getIcon().getData()), iconString.substring(iconString.indexOf(',') + 1));

        JSONObject jScopes = jAuthInfo.getJSONObject("scopes");
        Set<String> scopeTokens = getScope().get();
        assertEquals(jScopes.length(), scopeTokens.size());
        for (String s : scopeTokens) {
            assertTrue(jScopes.hasAndNotNull(s));
        }
    }

}
