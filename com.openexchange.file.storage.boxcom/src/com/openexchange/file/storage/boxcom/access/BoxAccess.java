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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.file.storage.boxcom.access;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.BoxApi;
import org.scribe.model.Token;
import org.slf4j.Logger;
import com.box.boxjavalibv2.BoxClient;
import com.box.boxjavalibv2.BoxConfigBuilder;
import com.box.boxjavalibv2.authorization.OAuthAuthorization;
import com.box.boxjavalibv2.dao.BoxOAuthToken;
import com.box.boxjavalibv2.jsonparsing.BoxJSONParser;
import com.box.boxjavalibv2.jsonparsing.BoxResourceHub;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.boxcom.BoxExceptionCodes;
import com.openexchange.file.storage.boxcom.Services;
import com.openexchange.java.Strings;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.session.Session;


/**
 * {@link BoxAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class BoxAccess {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BoxAccess.class);

    /** The re-check threshold in seconds (45 minutes) */
    private static final long RECHECK_THRESHOLD = 2700;

    /**
     * Gets the Box access for given Box account.
     *
     * @param fsAccount The Box account providing credentials and settings
     * @param session The user session
     * @return The Box access; either newly created or fetched from underlying registry
     * @throws OXException If a Box access could not be created
     */
    public static BoxAccess accessFor(final FileStorageAccount fsAccount, final Session session) throws OXException {
        final BoxAccessRegistry registry = BoxAccessRegistry.getInstance();
        final String accountId = fsAccount.getId();
        BoxAccess boxAccess = registry.getAccess(session.getContextId(), session.getUserId(), accountId);
        if (null == boxAccess) {
            final BoxAccess newInstance = new BoxAccess(fsAccount, session, session.getUserId(), session.getContextId());
            boxAccess = registry.addAccess(session.getContextId(), session.getUserId(), accountId, newInstance);
            if (null == boxAccess) {
                boxAccess = newInstance;
            }
        } else {
            boxAccess.ensureNotExpired(session);
        }
        return boxAccess;
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    /** The associated OAuth account */
    private volatile OAuthAccount boxOAuthAccount;

    /** The last-accessed time stamp */
    private volatile long lastAccessed;

    /** The client identifier */
    private volatile String clientId;

    /** The client secret */
    private volatile String clientSecret;

    /**
     * Initializes a new {@link BoxAccess}.
     */
    private BoxAccess(FileStorageAccount fsAccount, Session session, int userId, int contextId) throws OXException {
        super();

        // Get OAuth account identifier from messaging account's configuration
        int oauthAccountId;
        {
            Map<String, Object> configuration = fsAccount.getConfiguration();
            if (null == configuration) {
                throw BoxExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            Object accountId = configuration.get("account");
            if (null == accountId) {
                throw BoxExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            if (accountId instanceof Integer) {
                oauthAccountId = ((Integer) accountId).intValue();
            } else {
                try {
                    oauthAccountId = Integer.parseInt(accountId.toString());
                } catch (NumberFormatException e) {
                    throw BoxExceptionCodes.MISSING_CONFIG.create(e, fsAccount.getId());
                }
            }
        }

        // Grab Box.com OAuth account
        OAuthAccount boxOAuthAccount;
        {
            OAuthService oAuthService = Services.getService(OAuthService.class);
            boxOAuthAccount = oAuthService.getAccount(oauthAccountId, session, userId, contextId);
        }

        // Assign Box.com account
        this.boxOAuthAccount = boxOAuthAccount;

        // Initialize rest
        clientId = boxOAuthAccount.getMetaData().getAPIKey(session);
        clientSecret = boxOAuthAccount.getMetaData().getAPISecret(session);
        lastAccessed = System.nanoTime();
    }

    private BoxClient createBoxClient(OAuthAccount boxOAuthAccount) {
        BoxClient boxClient = new NonRefreshingBoxClient(clientId, clientSecret, new BoxResourceHub(), new BoxJSONParser(new BoxResourceHub()), (new BoxConfigBuilder()).build());

        // Apply access token and refresh token from OAuth account
        Map<String, Object> tokenSpec = new HashMap<String, Object>(6);
        tokenSpec.put(BoxOAuthToken.FIELD_ACCESS_TOKEN, boxOAuthAccount.getToken());
        tokenSpec.put(BoxOAuthToken.FIELD_REFRESH_TOKEN, boxOAuthAccount.getSecret());
        tokenSpec.put(BoxOAuthToken.FIELD_TOKEN_TYPE, "bearer");
        tokenSpec.put(BoxOAuthToken.FIELD_EXPIRES_IN, Integer.valueOf(3600));
        ((OAuthAuthorization) boxClient.getAuth()).setOAuthData(new BoxOAuthToken(tokenSpec));
        return boxClient;
    }

    private OAuthAccount recreateTokenIfExpired(boolean considerExpired, OAuthAccount boxOAuthAccount, Session session) throws OXException {
        // Create Scribe Box.com OAuth service
        final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(BoxApi.class);
        serviceBuilder.apiKey(boxOAuthAccount.getMetaData().getAPIKey(session)).apiSecret(boxOAuthAccount.getMetaData().getAPISecret(session));
        BoxApi.BoxApiService scribeOAuthService = (BoxApi.BoxApiService) serviceBuilder.build();

        // Check expiration
        if (considerExpired || scribeOAuthService.isExpired(boxOAuthAccount.getToken())) {
            // Expired...
            String refreshToken = boxOAuthAccount.getSecret();
            Token accessToken;
            try {
                accessToken = scribeOAuthService.getAccessToken(new Token(boxOAuthAccount.getToken(), boxOAuthAccount.getSecret()), null);
            } catch (org.scribe.exceptions.OAuthException e) {
                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e, boxOAuthAccount.getDisplayName(), boxOAuthAccount.getId());
            }
            if (Strings.isEmpty(accessToken.getSecret())) {
                LOGGER.warn("Received invalid request_token from Box.com: {}. Response:{}{}", null == accessToken.getSecret() ? "null" : accessToken.getSecret(), Strings.getLineSeparator(), accessToken.getRawResponse());
            } else {
                refreshToken = accessToken.getSecret();
            }
            // Update account
            OAuthService oAuthService = Services.getService(OAuthService.class);
            int accountId = boxOAuthAccount.getId();
            Map<String, Object> arguments = new HashMap<String, Object>(3);
            arguments.put(OAuthConstants.ARGUMENT_REQUEST_TOKEN, new DefaultOAuthToken(accessToken.getToken(), refreshToken));
            arguments.put(OAuthConstants.ARGUMENT_SESSION, session);
            oAuthService.updateAccount(accountId, arguments, session.getUserId(), session.getContextId());

            // Reload
            return oAuthService.getAccount(accountId, session, session.getUserId(), session.getContextId());
        }
        return null;
    }

    /**
     * Ensures this access is not expired
     *
     * @param session The associated session
     * @return The non-expired access
     * @throws OXException If check fails
     */
    private BoxAccess ensureNotExpired(Session session) throws OXException {
        long now = System.nanoTime();
        if (TimeUnit.NANOSECONDS.toSeconds(now - lastAccessed) > RECHECK_THRESHOLD) {
            synchronized (this) {
                OAuthAccount newAccount = recreateTokenIfExpired(false, boxOAuthAccount, session);
                if (newAccount != null) {
                    this.boxOAuthAccount = newAccount;
                    clientId = newAccount.getMetaData().getAPIKey(session);
                    clientSecret = newAccount.getMetaData().getAPISecret(session);
                    lastAccessed = System.nanoTime();
                }
            }
        }
        return this;
    }

    /**
     * Re-initializes this Box access
     *
     * @param session The session
     * @throws OXException If operation fails
     */
    public void reinit(Session session) throws OXException {
        synchronized (this) {
            OAuthAccount newAccount = recreateTokenIfExpired(true, boxOAuthAccount, session);
            if (newAccount != null) {
                this.boxOAuthAccount = newAccount;
                clientId = newAccount.getMetaData().getAPIKey(session);
                clientSecret = newAccount.getMetaData().getAPISecret(session);
                lastAccessed = System.nanoTime();
            }
        }
    }

    /**
     * Gets the current Box client instance
     *
     * @return The box client
     */
    public BoxClient getBoxClient() {
        return createBoxClient(boxOAuthAccount);
    }

    /**
     * Disposes this access instance.
     */
    public void dispose() {
        // Nothing to do
    }

}
