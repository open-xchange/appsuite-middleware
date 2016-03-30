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

package com.openexchange.subscribe.mslive;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.MsLiveConnectApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.session.Session;
import com.openexchange.subscribe.mslive.osgi.Services;

/**
 * {@link MSLiveApiClient} Utility class for MS Live OAuth API
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class MSLiveApiClient {

    /**
     * Get the default MS Live OAuth account
     *
     * @return the default MS Live OAuth account
     * @throws OXException If the account cannot be returned
     */
    public static OAuthAccount getDefaultOAuthAccount(Session session) throws OXException {
        final OAuthService service = Services.getService(OAuthService.class);
        return service.getDefaultAccount(API.MS_LIVE_CONNECT, session);
    }

    /**
     * Get the access token
     *
     * @param oauthAccount the OAuth account
     * @param session The session
     * @return a new access token
     * @throws OXException if the access token cannot be retrieved
     */
    public static String getAccessToken(OAuthAccount account, Session session) throws OXException {
        final OAuthService oauthService = Services.getService(OAuthService.class);

        // Build service
        final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(MsLiveConnectApi.class);
        serviceBuilder.apiKey(account.getMetaData().getAPIKey(session)).apiSecret(account.getMetaData().getAPISecret(session));
        org.scribe.oauth.OAuthService service = serviceBuilder.build();

        // Fetch access token
        String token = account.getToken();
        if (isExpired(token)) {
            String refreshToken = account.getSecret();
            final Token accessToken = service.getAccessToken(new Token(token, refreshToken), null);
            if (!Strings.isEmpty(accessToken.getSecret())) {
                refreshToken = accessToken.getSecret();
            }

            // Update account
            Map<String, Object> arguments = new HashMap<String, Object>(3);
            arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, new DefaultOAuthToken(accessToken.getToken(), refreshToken));
            arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
            oauthService.updateAccount(account.getId(), arguments, session.getUserId(), session.getContextId());
        }

        return token;
    }

    private static boolean isExpired(String accessToken) throws OXException {
        try {
            String encodedToken;
            try {
                encodedToken = URLEncoder.encode(accessToken, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e);
            }
            OAuthRequest request = new OAuthRequest(Verb.GET, "https://apis.live.net/v5.0/me?access_token=" + encodedToken);
            request.setConnectTimeout(5, TimeUnit.SECONDS);
            request.setReadTimeout(15, TimeUnit.SECONDS);
            Response response = request.send();
            if (response.getCode() == 401 || response.getCode() == 400) {
                // 401 unauthorized
                return true;
            }

            return false;
        } catch (org.scribe.exceptions.OAuthException e) {
            // Handle Scribe's org.scribe.exceptions.OAuthException (inherits from RuntimeException)
            Throwable cause = e.getCause();
            if (cause instanceof java.net.SocketTimeoutException) {
                // A socket timeout
                throw OAuthExceptionCodes.CONNECT_ERROR.create(cause, new Object[0]);
            }

            throw OAuthExceptionCodes.OAUTH_ERROR.create(cause, e.getMessage());
        }
    }

}
