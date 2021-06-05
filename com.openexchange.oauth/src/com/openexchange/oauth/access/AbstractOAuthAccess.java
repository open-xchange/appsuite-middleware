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

package com.openexchange.oauth.access;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.oauth.API;
import com.openexchange.oauth.DefaultOAuthAccount;
import com.openexchange.oauth.OAuthAccount;
import com.openexchange.oauth.OAuthConstants;
import com.openexchange.oauth.OAuthExceptionCodes;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthUtil;
import com.openexchange.oauth.scope.OXScope;
import com.openexchange.session.Session;

/**
 * {@link AbstractOAuthAccess}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public abstract class AbstractOAuthAccess implements OAuthAccess {

    /** The re-check threshold in milliseconds (45 minutes) */
    private static final long RECHECK_THRESHOLD_MILLIS = 2700000;

    /** The time-to-live before considering a token as expired (2 minutes before actual expiration time). */
    private static final long TTL_BEFORE_RENEWAL = 120000;

    /** The OAuth account */
    private volatile OAuthAccount oauthAccount;

    /** The associated OAuth client */
    private final AtomicReference<OAuthClient<?>> oauthClientRef;

    /** The last-accessed time stamp milliseconds */
    private volatile long lastAccessedMillis;

    /** The associated Open-Xchange session */
    private final Session session;

    /**
     * Initializes a new {@link AbstractOAuthAccess}.
     *
     * @param session The Open-Xchange session
     */
    protected AbstractOAuthAccess(Session session) {
        super();
        this.session = session;
        oauthClientRef = new AtomicReference<>();
    }

    @Override
    public void dispose() {
        // Empty by default
    }

    @Override
    public OAuthAccount getOAuthAccount() {
        return oauthAccount;
    }

    /**
     * Verifies the specified OAuth account over validity:
     * <ul>
     * <li>accessToken exists?</li>
     * <li>specified scopes are both available and enabled?</li>
     * <li>the user identity is set? (lazy update)</li>
     * </ul>
     *
     * @param account The OAuth account to check for validity
     * @param oauthService The OAuth service
     * @param scopes The scopes that are required to be available and enabled as well
     * @throws OXException if the account is not valid
     */
    protected void verifyAccount(OAuthAccount account, OAuthService oauthService, OXScope... scopes) throws OXException {
        // Verify that the account has an access token
        if (Strings.isEmpty(account.getToken())) {
            API api = account.getAPI();
            throw OAuthExceptionCodes.OAUTH_ACCESS_TOKEN_INVALID.create(api.getDisplayName(), I(account.getId()), I(session.getUserId()), I(session.getContextId()));
        }

        // Verify that scopes are available and enabled
        OAuthUtil.checkScopesAvailableAndEnabled(account, session, scopes);

        // Verify if the account has the user identity set, lazy update
        if (Strings.isEmpty(account.getUserIdentity())) {
            String userIdentity = account.getMetaData().getUserIdentity(session, account.getId(), account.getToken(), account.getSecret());
            DefaultOAuthAccount.class.cast(account).setUserIdentity(userIdentity);
            oauthService.updateAccount(session, account.getId(), Collections.singletonMap(OAuthConstants.ARGUMENT_IDENTITY, userIdentity));
        }

        // Other checks?
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> OAuthClient<T> getClient() throws OXException {
        OAuthClient<?> client = oauthClientRef.get();
        if (client == null) {
            // Exclusive initialization for OAuth client
            synchronized (this) {
                client = oauthClientRef.get();
                if (client == null) {
                    initialize();
                    client = oauthClientRef.get();
                }
            }
        }
        return (OAuthClient<T>) client;
    }

    /**
     * Verifies whether the OAuth token and the associated OAuth client are expired.
     *
     * @return <code>true</code> if expired; <code>false</code> otherwise
     */
    protected boolean isExpired() {
        long now = System.currentTimeMillis();
        OAuthAccount oauthAccount = this.oauthAccount;
        if (oauthAccount != null) {
            long expiration = oauthAccount.getExpiration();
            if (expiration > 0) {
                return (expiration - now) <= TTL_BEFORE_RENEWAL;
            }
        }
        return (now - lastAccessedMillis) > RECHECK_THRESHOLD_MILLIS;
    }

    /**
     * Sets the {@link OAuthClient}. Updates the last accessed time stamp
     *
     * @param client The {@link OAuthClient} to set
     */
    protected <T> void setOAuthClient(OAuthClient<T> client) {
        lastAccessedMillis = System.currentTimeMillis();
        oauthClientRef.set(client);
    }

    /**
     * Gets the OAuth client reference w/o any initializations before-hand.
     *
     * @return The OAuth client instance or <code>null</code>
     */
    @SuppressWarnings("unchecked")
    public <T> OAuthClient<T> getOAuthClient() {
        return (OAuthClient<T>) oauthClientRef.get();
    }

    /**
     * Sets the OAuth account.
     *
     * @param oauthAccount The OAuth account to set
     */
    protected void setOAuthAccount(OAuthAccount oauthAccount) {
        this.oauthAccount = oauthAccount;
    }

    /**
     * Returns the OAuth account identifier from associated account's configuration
     *
     * @param configuration The configuration
     * @return The account identifier
     * @throws IllegalArgumentException If the configuration is <code>null</code>, or if the account identifier is not present, or is present but cannot be parsed as an integer
     */
    protected int getAccountId(Map<String, Object> configuration) {
        return OAuthUtil.getAccountId(configuration);
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }
}
