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
import org.apache.http.HttpEntity;
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
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.saml.oauth.service.OAuthAccessToken;
import com.openexchange.saml.oauth.service.SAMLOAuthExceptionCodes;

/**
 * {@link AbstractOAuthAccessTokenRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public abstract class AbstractOAuthAccessTokenRequest {

    /** The HTTP client */
    protected final CloseableHttpClient httpClient;

    /** The config view factory */
    protected final ConfigViewFactory configViewFactory;

    /**
     * Initializes a new {@link AbstractOAuthAccessTokenRequest}.
     *
     * @param httpClient The HTTP client to use
     * @param configViewFactory The config view factory
     */
    protected AbstractOAuthAccessTokenRequest(CloseableHttpClient httpClient, ConfigViewFactory configViewFactory) {
        super();
        this.httpClient = httpClient;
        this.configViewFactory = configViewFactory;
    }

    private static final String TOKEN_TYPE = "token_type";
    private static final String EXPIRE = "expires_in";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String ACCESS_TOKEN = "access_token";
    private static final String ERROR_DESCRIPTION = "error_description";
    private static final String ERROR = "error";
    private static final String SCOPE = "scope";

    private static final String INVALID_GRANT = "invalid_grant";


    /**
     * Requests an OAuth access token.
     *
     * @param accessInfo The access info; e.g. base64-encoded SAML response or refresh token
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param scope An optional scope
     * @return The OAuth access token
     * @throws OXException If OAuth access token cannot be returned
     */
    public OAuthAccessToken requestAccessToken(String accessInfo, int userId, int contextId, String scope) throws OXException {
        HttpPost requestAccessToken = null;
        HttpResponse validationResp = null;
        try {
            OAuthConfiguration oAuthConfiguration = SAMLOAuthConfig.getConfig(userId, contextId, configViewFactory);
            if (oAuthConfiguration == null) {
                throw SAMLOAuthExceptionCodes.OAUTH_NOT_CONFIGURED.create(userId, contextId);
            }

            // Initialize POST request
            requestAccessToken = new HttpPost(oAuthConfiguration.getTokenEndpoint());
            requestAccessToken.addHeader("Content-Type", "application/x-www-form-urlencoded");

            // Build base64(<client-id> + ":" + <client-secret>) "Authorization" header
            if(oAuthConfiguration.getClientId()!=null && oAuthConfiguration.getClientSecret()!=null){
                String authString = new StringBuilder(oAuthConfiguration.getClientId()).append(':').append(oAuthConfiguration.getClientSecret()).toString();
                String auth = "Basic " + Base64.encodeBase64String(authString.getBytes(Charsets.UTF_8));
                requestAccessToken.addHeader("Authorization", auth);
            }

            // Build the url-encoded pairs for the POST request
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("grant_type", getGrantType()));
            if(!Strings.isEmpty(scope)){
                nvps.add(new BasicNameValuePair(SCOPE, scope));
            }
            addAccessInfo(accessInfo, nvps);
            requestAccessToken.setEntity(new UrlEncodedFormEntity(nvps, Charsets.UTF_8));

            // Execute POST
            validationResp = httpClient.execute(requestAccessToken);

            // Get & parse response body
            HttpEntity entity = validationResp.getEntity();
            if (null != entity) {
                String responseStr = EntityUtils.toString(entity, Charsets.UTF_8);
                if (responseStr != null) {
                    JSONObject jsonResponse = new JSONObject(responseStr);
                    if(jsonResponse.has(ERROR)){
                        if(jsonResponse.getString(ERROR).equals(INVALID_GRANT)){
                            if(jsonResponse.has(ERROR_DESCRIPTION)) {
                                throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create("Invalid grant error: "+jsonResponse.getString(ERROR_DESCRIPTION));
                            } else {
                                throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create("Invalid grant error.");
                            }
                        } else {
                            if(jsonResponse.has(ERROR_DESCRIPTION)) {
                                OXException e = SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create(jsonResponse.getString(ERROR)+" error: "+jsonResponse.getString(ERROR_DESCRIPTION));
                                throw e;
                            } else {
                                throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create(jsonResponse.getString(ERROR)+" error for user {} in context {}.", userId, contextId);
                            }
                        }
                    }
                    String accessToken = jsonResponse.optString(ACCESS_TOKEN, null);
                    if (null == accessToken) {
                        throw SAMLOAuthExceptionCodes.NO_ACCESS_TOKEN.create("Token response doesn't contain the access token.");
                    }

                    String refreshToken = jsonResponse.has(REFRESH_TOKEN) ? jsonResponse.getString(REFRESH_TOKEN):null;
                    int expires = jsonResponse.has(EXPIRE) ? jsonResponse.getInt(EXPIRE) : -1;
                    String tokenType = jsonResponse.has(TOKEN_TYPE) ? jsonResponse.getString(TOKEN_TYPE) : null;
                    return new OAuthAccessToken(accessToken, refreshToken, tokenType, expires);
                }
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

    /**
     * Gets the grant type.
     *
     * @return The grant type
     */
    protected abstract String getGrantType();

    /**
     * Adds the access info.
     *
     * @param accessInfo The access info to add
     * @param nvps The list to add to
     */
    protected abstract void addAccessInfo(String accessInfo, List<NameValuePair> nvps);
}
