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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.messaging.facebook.session;

import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;
import org.scribe.model.Token;
import org.scribe.model.Verb;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.facebook.FacebookConfiguration;
import com.openexchange.messaging.facebook.FacebookConstants;
import com.openexchange.messaging.facebook.FacebookMessagingException;
import com.openexchange.messaging.facebook.FacebookMessagingExceptionCodes;
import com.openexchange.messaging.facebook.FacebookMessagingResource;
import com.openexchange.messaging.facebook.services.FacebookMessagingServiceRegistry;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthException;
import com.openexchange.oauth.OAuthService;
import com.openexchange.session.Session;

/**
 * {@link FacebookOAuthAccess} - Initializes and provides Facebook OAuth access.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FacebookOAuthAccess {

    /**
     * Gets the facebook OAuth access for given facebook messaging account.
     * 
     * @param messagingAccount The facebook messaging account providing credentials and settings
     * @param session The user session
     * @return The facebook OAuth access; either newly created or fetched from underlying registry
     * @throws FacebookMessagingException If a Facebook session could not be created
     */
    public static FacebookOAuthAccess accessFor(final MessagingAccount messagingAccount, final Session session) throws FacebookMessagingException {
        final FacebookOAuthAccessRegistry registry = FacebookOAuthAccessRegistry.getInstance();
        final int accountId = messagingAccount.getId();
        FacebookOAuthAccess facebookSession = registry.getSession(session.getContextId(), session.getUserId(), accountId);
        if (null == facebookSession) {
            final FacebookOAuthAccess newInstance = new FacebookOAuthAccess(messagingAccount, session.getUserId(), session.getContextId());
            facebookSession = registry.addSession(session.getContextId(), session.getUserId(), accountId, newInstance);
            if (null == facebookSession) {
                facebookSession = newInstance;
            }
        }
        return facebookSession;
    }

    /**
     * The facebook OAuth service.
     */
    private final org.scribe.oauth.OAuthService facebookOAuthService;

    /**
     * The OAuth account.
     */
    private final OAuthAccount oauthAccount;

    /**
     * The facebook user identifier.
     */
    private final String facebookUserId;

    /**
     * The facebook user's full name
     */
    private final String facebookUserName;

    /**
     * The OAuth access token for Facebook.
     */
    private final Token facebookAccessToken;

    /**
     * The last-accessed time stamp.
     */
    private volatile long lastAccessed;

    /**
     * Initializes a new {@link FacebookMessagingResource}.
     * 
     * @param messagingAccount The facebook messaging account providing credentials and settings
     * @throws MessagingException
     */
    private FacebookOAuthAccess(final MessagingAccount messagingAccount, final int user, final int contextId) throws FacebookMessagingException {
        super();
        /*
         * Get OAuth account identifier from messaging account's configuration
         */
        final int oauthAccountId = ((Integer) messagingAccount.getConfiguration().get(FacebookConstants.FACEBOOK_OAUTH_ACCOUNT)).intValue();
        final OAuthService oAuthService = FacebookMessagingServiceRegistry.getServiceRegistry().getService(OAuthService.class);
        try {
            oauthAccount = oAuthService.getAccount(oauthAccountId, user, contextId);
            facebookAccessToken = new Token(oauthAccount.getToken(), oauthAccount.getSecret());
            /*
             * Generate FB service
             */
            {
                final String apiKey = FacebookConfiguration.getInstance().getApiKey();
                final String secretKey = FacebookConfiguration.getInstance().getSecretKey();
                facebookOAuthService = new ServiceBuilder().provider(FacebookApi.class).apiKey(apiKey).apiSecret(secretKey).build();
            }
            /*
             * Get the FB user identifier and thus implicitly test OAuth access token
             */
            final OAuthRequest request = new OAuthRequest(Verb.GET, "https://graph.facebook.com/me");
            facebookOAuthService.signRequest(facebookAccessToken, request);
            final Response response = request.send();
            final JSONObject object = new JSONObject(response.getBody());
            facebookUserId = object.getString("id");
            facebookUserName = object.getString("name");
        } catch (final OAuthException e) {
            throw new FacebookMessagingException(e);
        } catch (final org.scribe.exceptions.OAuthException e) {
            throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw FacebookMessagingExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Gets the last-accessed time stamp.
     * 
     * @return The last-accessed time stamp
     */
    public long getLastAccessed() {
        return lastAccessed;
    }

    @Override
    public String toString() {
        return new StringBuilder(32).append("{ oauthAccount=").append(oauthAccount.getDisplayName()).append(", facebookUserId=").append(
            facebookUserId).append(", facebookToken=").append(facebookAccessToken).append('}').toString();
    }

    /**
     * Gets associated OAuth account.
     * 
     * @return The OAuth account
     */
    public OAuthAccount getOauthAccount() {
        return oauthAccount;
    }

    /**
     * Gets the facebook user identifier.
     * 
     * @return The facebook user identifier
     */
    public String getFacebookUserId() {
        return facebookUserId;
    }

    /**
     * Gets the facebook user's full name.
     * 
     * @return The facebook user's full name.
     */
    public String getFacebookUserName() {
        return facebookUserName;
    }

    /**
     * Gets the Facebook OAuth service needed to sign requests.
     * 
     * @return The Facebook OAuth service
     * @see org.scribe.oauth.OAuthService#signRequest(Token, OAuthRequest)
     */
    public org.scribe.oauth.OAuthService getFacebookOAuthService() {
        return facebookOAuthService;
    }

    /**
     * Gets the Facebook access token needed to sign requests.
     * 
     * @return The Facebook access token
     * @see org.scribe.oauth.OAuthService#signRequest(Token, OAuthRequest)
     */
    public Token getFacebookAccessToken() {
        return facebookAccessToken;
    }

    /**
     * Executes GET request for specified URL.
     * 
     * @param url The URL
     * @return The response
     * @throws FacebookMessagingException If request fails
     */
    public String executeGETRequest(final String url) throws FacebookMessagingException {
        try {
            final OAuthRequest request = new OAuthRequest(Verb.GET, url);
            facebookOAuthService.signRequest(facebookAccessToken, request);
            return request.send().getBody();
        } catch (final org.scribe.exceptions.OAuthException e) {
            throw FacebookMessagingExceptionCodes.OAUTH_ERROR.create(e, e.getMessage());
        } catch (final Exception e) {
            throw FacebookMessagingExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
