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

package com.openexchange.saml.oauth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.rest.client.httpclient.HttpClients;

/**
 * {@link OAuthRefreshTokenRequest}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class OAuthRefreshTokenRequest {

    private static volatile OAuthRefreshTokenRequest instance;

    /**
     * Initializes the singleton instance with given HTTP client
     *
     * @param httpClient The HTTP client to use
     */
    public static void initInstance(CloseableHttpClient httpClient) {
        instance = new OAuthRefreshTokenRequest(httpClient);
    }

    /**
     * Releases the singleton instance
     */
    public static void releaseInstance() {
        instance = null;
    }

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static OAuthRefreshTokenRequest getInstance() {
        return instance;
    }

    // -----------------------------------------------------------------------------------------------------------------------------------------------------------

    private final CloseableHttpClient httpClient;

    private OAuthRefreshTokenRequest(CloseableHttpClient httpClient) {
        super();
        this.httpClient = httpClient;
    }

    private static final String GRANT_TYPE = "refresh_token";
    private static final String TOKEN_TYPE = "token_type";
    private static final String EXPIRE = "expires_in";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String ACCESS_TOKEN = "access_token";

    public OAuthAccessToken requestAccessToken(String refreshToken, int userId, int contextId) throws OXException {
        HttpPost requestAccessToken = null;
        HttpResponse validationResp = null;
        try {
            requestAccessToken = new HttpPost(SAMLOAuthConfig.getTokenEndpoint(userId, contextId));
            requestAccessToken.addHeader("Content-Type", "application/x-www-form-urlencoded");

            StringBuilder authBuilder = new StringBuilder(SAMLOAuthConfig.getClientID(userId, contextId));
            authBuilder.append(":").append(SAMLOAuthConfig.getClientSecret(userId, contextId));
            String auth = "Basic " + Base64.encodeBase64String(authBuilder.toString().getBytes());
            requestAccessToken.addHeader("Authorization", auth);

            List<NameValuePair> nvps = new ArrayList<NameValuePair>();

            nvps.add(new BasicNameValuePair("grant_type", GRANT_TYPE));
            nvps.add(new BasicNameValuePair("refresh_token", refreshToken));

            requestAccessToken.setEntity(new UrlEncodedFormEntity(nvps, Charsets.UTF_8));

            validationResp = httpClient.execute(requestAccessToken);
            String responseStr = EntityUtils.toString(validationResp.getEntity());
            if (responseStr != null) {
                JSONObject jsonResponse = new JSONObject(responseStr);
                String accessToken = jsonResponse.optString(ACCESS_TOKEN, null);
                if (null == accessToken) {
                    throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create("Token response doesn't contain the access token.");
                }

                OAuthAccessToken token = new OAuthAccessToken(accessToken, jsonResponse.getString(REFRESH_TOKEN), jsonResponse.getString(TOKEN_TYPE), jsonResponse.getInt(EXPIRE));
                return token;
            }

            throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create("Unable to parse token response.");
        } catch (ClientProtocolException e) {
            throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create(e, e.getMessage());
        } catch (IOException e) {
            throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create(e, e.getMessage());
        } catch (JSONException e) {
            throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create(e, e.getMessage());
        } finally {
            HttpClients.close(requestAccessToken, validationResp);
        }
    }
}
