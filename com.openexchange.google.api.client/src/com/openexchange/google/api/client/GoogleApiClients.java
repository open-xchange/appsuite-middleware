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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.google.api.client;

import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.Google2v2Api;
import org.scribe.model.Token;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.openexchange.exception.OXException;
import com.openexchange.google.api.client.services.Services;
import com.openexchange.oauth.API;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;


/**
 * {@link GoogleApiClients} - Utility class for Google API client.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public class GoogleApiClients {

    /**
     * Initializes a new {@link GoogleApiClients}.
     */
    private GoogleApiClients() {
        super();
    }

    /** Global instance of the JSON factory. */
    public static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Gets the default Google OAuth account.
     * <p>
     * Validates expiry of current access token and requests a new one if less than 5 minutes to live
     *
     * @param session The session
     * @return The default Google OAuth account
     * @throws OXException If default Google OAuth account cannot be returned
     */
    public static OAuthAccount getDefaultGoogleAccount(final Session session) throws OXException {
        return getDefaultGoogleAccount(session, true);
    }

    /**
     * Gets the default Google OAuth account.
     * <p>
     * Optionally validates expiry of current access token and requests a new one if less than 5 minutes to live
     *
     * @param session The session
     * @param reacquireIfExpired <code>true</code> to re-acquire a new access token, if existing one is about to expire; otherwise <code>false</code>
     * @return The default Google OAuth account
     * @throws OXException If default Google OAuth account cannot be returned
     */
    public static OAuthAccount getDefaultGoogleAccount(final Session session, final boolean reacquireIfExpired) throws OXException {
        final OAuthService oAuthService = Services.optService(OAuthService.class);
        if (null == oAuthService) {
            throw ServiceExceptionCode.absentService(OAuthService.class);
        }

        // Get default Google account
        OAuthAccount defaultAccount = oAuthService.getDefaultAccount(API.GOOGLE, session);

        if (reacquireIfExpired) {
            // Create Scribe Google OAuth service
            final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(Google2v2Api.class);
            serviceBuilder.apiKey(defaultAccount.getMetaData().getAPIKey(session)).apiSecret(defaultAccount.getMetaData().getAPISecret(session));
            Google2v2Api.GoogleOAuth2Service scribeOAuthService = (Google2v2Api.GoogleOAuth2Service) serviceBuilder.build();

            // Check expiry
            int expiry = scribeOAuthService.getExpiry(defaultAccount.getToken());
            if (expiry < 300) {
                // Less than 5 minutes to live -> refresh token!
                Token accessToken = scribeOAuthService.getAccessToken(new Token(defaultAccount.getToken(), defaultAccount.getSecret()), null);

                // Update account
                int accountId = defaultAccount.getId();
                Map<String, Object> arguments = new HashMap<String, Object>(3);
                arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, new DefaultOAuthToken(accessToken.getToken(), accessToken.getSecret()));
                arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
                oAuthService.updateAccount(accountId, arguments, session.getUserId(), session.getContextId());

                // Reload
                defaultAccount = oAuthService.getAccount(accountId, session, session.getUserId(), session.getContextId());
            }
        }

        return defaultAccount;
    }

    /**
     * Gets the Google credentials from default OAuth account.
     *
     * @param session The associated session
     * @return The Google credentials from default OAuth account
     * @throws OXException If Google credentials cannot be returned
     */
    public static GoogleCredential getCredentials(final Session session) throws OXException {
        try {
            final OAuthAccount defaultAccount = getDefaultGoogleAccount(session);

            // Initialize transport
            NetHttpTransport transport = new NetHttpTransport.Builder().doNotValidateCertificate().build();

            // Build credentials
            return new GoogleCredential.Builder()
            .setClientSecrets(defaultAccount.getMetaData().getAPIKey(session), defaultAccount.getMetaData().getAPISecret(session))
            .setJsonFactory(JSON_FACTORY).setTransport(transport).build()
            .setRefreshToken(defaultAccount.getSecret()).setAccessToken(defaultAccount.getToken());
        } catch (GeneralSecurityException e) {
            throw OAuthExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
