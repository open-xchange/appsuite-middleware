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

package com.openexchange.session.oauth.impl;

import static com.openexchange.java.Autoboxing.L;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.oauth.OAuthTokens;
import com.openexchange.session.oauth.RefreshResult;
import com.openexchange.session.oauth.RefreshResult.FailReason;
import com.openexchange.session.oauth.RefreshResult.SuccessReason;
import com.openexchange.session.oauth.TokenRefreshConfig;
import com.openexchange.session.oauth.TokenRefreshResponse;
import com.openexchange.session.oauth.TokenRefreshResponse.Error;
import com.openexchange.session.oauth.TokenRefresher;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessionstorage.SessionStorageExceptionCodes;
import com.openexchange.sessionstorage.SessionStorageService;

/**
 * {@link OAuthTokenUpdaterImpl}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class OAuthTokenUpdaterImpl {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthTokenUpdaterImpl.class);

    private static final long REMOTE_SESSION_LOOKUP_TIMEOUT_MILLIS = 5000L;

    private final Session session;
    private final TokenRefresher refresher;
    private final TokenRefreshConfig refreshConfig;
    private final ServiceLookup services;
    private final OAuthTokensGetterSetter tokenGetterSetter;

    /**
     * Initializes a new {@link OAuthTokenUpdaterImpl}.
     *
     * @param session The session for which to check OAuth tokens
     * @param refresher The refresher to use
     * @param refreshConfig The refresh configuration
     * @param tokenGetterSetter The getter/setter for tokens
     * @param services The tracked OSGi services
     */
    public OAuthTokenUpdaterImpl(Session session, TokenRefresher refresher, TokenRefreshConfig refreshConfig, OAuthTokensGetterSetter tokenGetterSetter, ServiceLookup services) {
        super();
        this.session = session;
        this.refresher = refresher;
        this.refreshConfig = refreshConfig;
        this.tokenGetterSetter = tokenGetterSetter;
        this.services = services;

    }

    /**
     * Checks (and respectively refreshes) tokens associated with this updater's session.
     *
     * @return The refresh result
     * @throws InterruptedException If operation gets interrupted
     * @throws OXException If an error occurs while trying to refresh access token
     */
    public RefreshResult checkOrRefreshTokens() throws InterruptedException, OXException {
        long lockTimeoutMillis = refreshConfig.getLockTimeoutMillis();
        long start = System.currentTimeMillis();
        OAuthTokens tokens;
        try {
            Optional<OAuthTokens> optTokens = tokenGetterSetter.getFromSessionAtomic(session, lockTimeoutMillis, TimeUnit.MILLISECONDS);
            if (!optTokens.isPresent()) {
                LOG.debug("Could not get OAuth tokens from session '{}'", session.getSessionID());
                return RefreshResult.fail(FailReason.PERMANENT_ERROR, "Could not get tokens from session");
            }

            tokens = optTokens.get();
        } catch (TimeoutException e) {
            return RefreshResult.fail(FailReason.LOCK_TIMEOUT, "Lock timeout for expired session OAuth token exceeded", e);
        }

        if (tokens.accessExpiresWithin(refreshConfig.getRefreshThresholdMillis(), TimeUnit.MILLISECONDS)) {
            LOG.debug("Need to refresh OAuth tokens from session '{}'", session.getSessionID());
            long lockMillisLeft = lockTimeoutMillis - (System.currentTimeMillis() - start);
            if (lockMillisLeft <= 0) {
                LOG.debug("Could not refresh OAuth tokens from session '{}' since lock timeout is exceeded", session.getSessionID());
                return RefreshResult.fail(FailReason.LOCK_TIMEOUT, "Lock timeout for expired session OAuth token exceeded");
            }

            LOG.debug("Trying to refresh OAuth tokens from session '{}'...", session.getSessionID());
            return refreshTokens(tokens, lockMillisLeft, TimeUnit.MILLISECONDS);
        }

        LOG.debug("No need to refresh OAuth tokens from session '{}' since not expired", session.getSessionID());
        return RefreshResult.success(SuccessReason.NON_EXPIRED);
    }

    private RefreshResult refreshTokens(OAuthTokens oldTokens, long lockTimeout, TimeUnit lockUnit) throws InterruptedException, OXException {
        try {
            return tokenGetterSetter.doThrowableAtomic(session, lockTimeout, lockUnit, () -> {
                Optional<OAuthTokens> optTokens = tokenGetterSetter.getFromSession(session);
                if (!optTokens.isPresent()) {
                    LOG.debug("Could not get OAuth tokens from session '{}'", session.getSessionID());
                    return RefreshResult.fail(FailReason.PERMANENT_ERROR, "Could not get tokens from session");
                }

                OAuthTokens tokens = optTokens.get();
                if (!oldTokens.getAccessToken().equals(tokens.getAccessToken())) {
                    LOG.debug("OAuth tokens from session '{}' were already refreshed by another thread", session.getSessionID());
                    return RefreshResult.success(SuccessReason.CONCURRENT_REFRESH);
                }

                LOG.debug("Refreshing OAuth tokens from session '{}'...", session.getSessionID());
                TokenRefreshResponse response = refresher.execute(tokens);
                if (response.isSuccess()) {
                    LOG.debug("Succeeded refreshing OAuth tokens from session '{}'...", session.getSessionID());
                    return handleSuccess(response.getTokens());
                }

                LOG.debug("Failed refreshing OAuth tokens from session '{}'...", session.getSessionID());
                return handleError(oldTokens, response.getError());
            });
        } catch (TimeoutException e) {
            return RefreshResult.fail(FailReason.LOCK_TIMEOUT, "Lock timeout for expired session oauth token exceeded", e);
        }
    }

    private RefreshResult handleSuccess(OAuthTokens tokens) {
        if (tokens.accessExpiresWithin(refreshConfig.getRefreshThreshold(), refreshConfig.getRefreshThresholdUnit())) {
            // Some IDMs assign a max. lifetime to refresh tokens token, too. Often aligned with
            // SSO session duration. As a result, fresh access tokens might have a shorter validity
            // period than the previous ones (expiry == max. refresh token lifetime).
            // In case the expiration time becomes lower than the configured refresh threshold,
            // it doesn't make sense to use the new token pair at all. Any subsequent request
            // would immediately try to refresh it again.
            LOG.info("Discarding refreshed OAuth tokens for session '{}'. Expiration is lower than configured refresh threshold: {}sec / {}sec",
                session.getSessionID(),
                L(TimeUnit.MILLISECONDS.toSeconds(tokens.getExpiresInMillis())),
                L(refreshConfig.getRefreshThresholdUnit().toSeconds(refreshConfig.getRefreshThreshold())));
            return RefreshResult.fail(FailReason.PERMANENT_ERROR, "Expiration date of new tokens is lower than refresh threshold");
        }

        tokenGetterSetter.setInSession(session, tokens);
        SessiondService sessiondService = services.getOptionalService(SessiondService.class);
        if (sessiondService == null) {
            LOG.warn("Storing OAuth tokens in stored session '{}' failed. SessionD service unavailable.", session.getSessionID());
        } else {
            try {
                LOG.info("Storing updated OAuth tokens in stored session '{}'", session.getSessionID());
                sessiondService.storeSession(session.getSessionID(), false);
            } catch (Exception e) {
                LOG.warn("Storing OAuth tokens in stored session '{}' failed", session.getSessionID(), e);
            }
        }

        return RefreshResult.success(SuccessReason.REFRESHED);
    }

    private RefreshResult handleError(OAuthTokens oldTokens, Error error) {
        switch (error.getType()) {
            case INVALID_REFRESH_TOKEN:
                LOG.info("OAuth token refresh failed due to invalid refresh token for session '{}'", session.getSessionID());
                return handleInvalidRefreshToken(oldTokens);
            case TEMPORARY:
                if (error.hasException()) {
                    OXException exception = error.getException();
                    LOG.warn("A temporary error occurred while trying to refresh tokens for session '{}'", session.getSessionID(), exception);
                    return RefreshResult.fail(FailReason.TEMPORARY_ERROR, error.getDescription(), exception);
                }
                LOG.warn("A temporary error occurred while trying to refresh tokens for session '{}'", session.getSessionID());
                return RefreshResult.fail(FailReason.TEMPORARY_ERROR, error.getDescription());
            case PERMANENT:
                if (error.hasException()) {
                    OXException exception = error.getException();
                    LOG.warn("A permanent error occurred while trying to refresh tokens for session '{}'", session.getSessionID(), exception);
                    return RefreshResult.fail(FailReason.PERMANENT_ERROR, error.getDescription(), exception);
                }
                LOG.warn("A permanent error occurred while trying to refresh tokens for session '{}'", session.getSessionID());
                return RefreshResult.fail(FailReason.PERMANENT_ERROR, error.getDescription());
            default:
                throw new IllegalStateException("Unknown error type: " + error.getType().name());
        }
    }

    private RefreshResult handleInvalidRefreshToken(OAuthTokens oldTokens) {
        if (!refreshConfig.isTryRecoverStoredTokens()) {
            tokenGetterSetter.removeFromSession(session);
            return RefreshResult.fail(FailReason.INVALID_REFRESH_TOKEN, "Invalid refresh token");
        }

        // try to recover tokens from stored session in case a distributed race condition occurred
        SessionStorageService sessionStorageService = services.getService(SessionStorageService.class);
        if (sessionStorageService != null) {
            try {
                LOG.debug("Trying to find newer tokens in stored session '{}'", session.getSessionID());
                Session remoteSession = sessionStorageService.lookupSession(session.getSessionID(), REMOTE_SESSION_LOOKUP_TIMEOUT_MILLIS);
                Optional<OAuthTokens> optRemoteTokens = tokenGetterSetter.getFromSession(remoteSession);
                if (!optRemoteTokens.isPresent()) {
                    LOG.warn("Stored session '{}' contains no tokens", session.getSessionID());
                    tokenGetterSetter.removeFromSession(session);
                    return RefreshResult.fail(FailReason.INVALID_REFRESH_TOKEN, "Invalid refresh token");
                }

                OAuthTokens remoteTokens = optRemoteTokens.get();
                boolean tokensDiffer = !oldTokens.getAccessToken().equals(remoteTokens.getAccessToken());
                if (tokensDiffer && !remoteTokens.isAccessExpired()) {
                    // success; seems like tokens have been refreshed on another node meanwhile
                    LOG.info("Taking over tokens from stored session '{}'", session.getSessionID());
                    tokenGetterSetter.setInSession(session, remoteTokens);
                    return RefreshResult.success(SuccessReason.CONCURRENT_REFRESH);
                }
                LOG.debug("Stored session '{}' contains no other valid tokens", session.getSessionID());
            } catch (OXException e) {
                if (SessionStorageExceptionCodes.NO_SESSION_FOUND.equals(e)) {
                    LOG.warn("No stored session found for ID '{}'", session.getSessionID());
                } else {
                    LOG.error("Error while looking up remote session '{}'", session.getSessionID(), e);
                }
            }
        }

        tokenGetterSetter.removeFromSession(session);
        return RefreshResult.fail(FailReason.INVALID_REFRESH_TOKEN, "Invalid refresh token");
    }

}
