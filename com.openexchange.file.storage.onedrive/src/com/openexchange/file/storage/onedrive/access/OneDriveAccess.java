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

package com.openexchange.file.storage.onedrive.access;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import org.apache.http.impl.client.DefaultHttpClient;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.MsLiveConnectApi;
import org.scribe.model.Token;
import org.slf4j.Logger;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.onedrive.OneDriveExceptionCodes;
import com.openexchange.file.storage.onedrive.osgi.Services;
import com.openexchange.java.Strings;
import com.openexchange.oauth.DefaultOAuthToken;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.session.Session;

/**
 * {@link OneDriveAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public class OneDriveAccess {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(OneDriveAccess.class);

    /** The re-check threshold in seconds (45 minutes) */
    private static final long RECHECK_THRESHOLD = 2700;

    /**
     * Gets the Microsoft OneDrive access for given Microsoft OneDrive account.
     *
     * @param fsAccount The Microsoft OneDrive account providing credentials and settings
     * @param session The user session
     * @return The Microsoft OneDrive access; either newly created or fetched from underlying registry
     * @throws OXException If a Microsoft OneDrive access could not be created
     */
    public static OneDriveAccess accessFor(final FileStorageAccount fsAccount, final Session session) throws OXException {
        final OneDriveAccessRegistry registry = OneDriveAccessRegistry.getInstance();
        final String accountId = fsAccount.getId();
        OneDriveAccess oneDriveAccess = registry.getAccess(session.getContextId(), session.getUserId(), accountId);
        if (null == oneDriveAccess) {
            final OneDriveAccess newInstance = new OneDriveAccess(fsAccount, session, session.getUserId(), session.getContextId());
            oneDriveAccess = registry.addAccess(session.getContextId(), session.getUserId(), accountId, newInstance);
            if (null == oneDriveAccess) {
                oneDriveAccess = newInstance;
            }
        } else {
            oneDriveAccess.ensureNotExpired(session);
        }
        return oneDriveAccess;
    }

    // ------------------------------------------------------------------------------------------------------------------------ //

    /** The associated OAuth account */
    private volatile OAuthAccount liveconnectOAuthAccount;

    /** The last-accessed time stamp */
    private volatile long lastAccessed;

    /** The access token */
    private volatile String accessToken;

    /** The HTTP client */
    private final DefaultHttpClient httpClient;

    /** The cache for known identifiers */
    private final ConcurrentMap<String, Object> knownIds;

    /**
     * Initializes a new {@link OneDriveAccess}.
     */
    private OneDriveAccess(FileStorageAccount fsAccount, Session session, int userId, int contextId) throws OXException {
        super();

        // Initialize map
        knownIds = new ConcurrentHashMap<String, Object>(256);

        // Get OAuth account identifier from messaging account's configuration
        int oauthAccountId;
        {
            Map<String, Object> configuration = fsAccount.getConfiguration();
            if (null == configuration) {
                throw OneDriveExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            Object accountId = configuration.get("account");
            if (null == accountId) {
                throw OneDriveExceptionCodes.MISSING_CONFIG.create(fsAccount.getId());
            }
            if (accountId instanceof Integer) {
                oauthAccountId = ((Integer) accountId).intValue();
            } else {
                try {
                    oauthAccountId = Integer.parseInt(accountId.toString());
                } catch (NumberFormatException e) {
                    throw OneDriveExceptionCodes.MISSING_CONFIG.create(e, fsAccount.getId());
                }
            }
        }

        // Grab Live Connect OAuth account
        OAuthAccount liveconnectOAuthAccount;
        {
            OAuthService oAuthService = Services.getService(OAuthService.class);
            liveconnectOAuthAccount = oAuthService.getAccount(oauthAccountId, session, userId, contextId);
        }

        // Assign Live Connect account
        this.liveconnectOAuthAccount = liveconnectOAuthAccount;

        // Initialize rest
        accessToken = liveconnectOAuthAccount.getToken();
        httpClient = createClient();
        lastAccessed = System.nanoTime();
    }

    private DefaultHttpClient createClient() {
        return HttpClients.getHttpClient("Open-Xchange OneDrive Client");
    }

    private OAuthAccount recreateTokenIfExpired(boolean considerExpired, OAuthAccount liveconnectOAuthAccount, Session session) throws OXException {
        // Create Scribe Microsoft OneDrive OAuth service
        final ServiceBuilder serviceBuilder = new ServiceBuilder().provider(MsLiveConnectApi.class);
        serviceBuilder.apiKey(liveconnectOAuthAccount.getMetaData().getAPIKey(session)).apiSecret(liveconnectOAuthAccount.getMetaData().getAPISecret(session));
        MsLiveConnectApi.MsLiveConnectService scribeOAuthService = (MsLiveConnectApi.MsLiveConnectService) serviceBuilder.build();

        // Check expiration
        if (considerExpired || scribeOAuthService.isExpired(liveconnectOAuthAccount.getToken())) {
            // Expired...
            String refreshToken = liveconnectOAuthAccount.getSecret();
            Token accessToken;
            try {
                accessToken = scribeOAuthService.getAccessToken(new Token(liveconnectOAuthAccount.getToken(), liveconnectOAuthAccount.getSecret()), null);
            } catch (org.scribe.exceptions.OAuthException e) {
                throw OAuthExceptionCodes.INVALID_ACCOUNT_EXTENDED.create(e, liveconnectOAuthAccount.getDisplayName(), liveconnectOAuthAccount.getId());
            }
            if (Strings.isEmpty(accessToken.getSecret())) {
                LOGGER.warn("Received invalid request_token from Live Connect: {}. Response:{}{}", null == accessToken.getSecret() ? "null" : accessToken.getSecret(), Strings.getLineSeparator(), accessToken.getRawResponse());
            } else {
                refreshToken = accessToken.getSecret();
            }
            // Update account
            OAuthService oAuthService = Services.getService(OAuthService.class);
            int accountId = liveconnectOAuthAccount.getId();
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
    private OneDriveAccess ensureNotExpired(Session session) throws OXException {
        long now = System.nanoTime();
        if (TimeUnit.NANOSECONDS.toSeconds(now - lastAccessed) > RECHECK_THRESHOLD) {
            synchronized (this) {
                now = System.nanoTime();
                if (TimeUnit.NANOSECONDS.toSeconds(now - lastAccessed) > RECHECK_THRESHOLD) {
                    OAuthAccount newAccount = recreateTokenIfExpired(false, liveconnectOAuthAccount, session);
                    if (newAccount != null) {
                        this.liveconnectOAuthAccount = newAccount;
                        accessToken = liveconnectOAuthAccount.getToken();
                        lastAccessed = System.nanoTime();
                    }
                }
            }
        }
        return this;
    }

    /**
     * Re-initializes this Microsoft OneDrive access
     *
     * @param session The session
     * @throws OXException If operation fails
     */
    public void reinit(Session session) throws OXException {
        synchronized (this) {
            OAuthAccount newAccount = recreateTokenIfExpired(true, liveconnectOAuthAccount, session);
            if (newAccount != null) {
                this.liveconnectOAuthAccount = newAccount;
                accessToken = liveconnectOAuthAccount.getToken();
                lastAccessed = System.nanoTime();
            }
        }
    }

    /**
     * Gets the cache for known identifiers.
     *
     * @return The cache for known identifiers
     */
    public ConcurrentMap<String, Object> getKnownIds() {
        return knownIds;
    }

    /**
     * Gets the current HTTP client instance
     *
     * @return The HTTP client
     */
    public DefaultHttpClient getHttpClient() {
        return httpClient;
    }

    /**
     * Gets the access token
     *
     * @return The access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Disposes this access instance.
     */
    public void dispose() {
        // Nothing to do
    }

}
