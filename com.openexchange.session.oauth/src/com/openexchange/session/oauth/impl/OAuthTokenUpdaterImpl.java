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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.session.oauth.impl;

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

    private static final long REMOTE_SESSION_LOOKUP_TIMEOUT_MILLIS = 5000l;

    private final Session session;
    private final TokenRefresher refresher;
    private final TokenRefreshConfig refreshConfig;
    private final ServiceLookup services;
    private final OAuthTokensGetterSetter tokenGetterSetter;

    public OAuthTokenUpdaterImpl(Session session, TokenRefresher refresher, TokenRefreshConfig refreshConfig, OAuthTokensGetterSetter tokenGetterSetter, ServiceLookup services) {
        super();
        this.session = session;
        this.refresher = refresher;
        this.refreshConfig = refreshConfig;
        this.tokenGetterSetter = tokenGetterSetter;
        this.services = services;

    }

    public RefreshResult checkOrRefreshTokens() throws InterruptedException, OXException {
        long lockTimeoutMillis = refreshConfig.getLockTimeoutMillis();
        long start = System.currentTimeMillis();
        OAuthTokens tokens;
        try {
            Optional<OAuthTokens> optTokens = tokenGetterSetter.getFromSessionAtomic(session, lockTimeoutMillis, TimeUnit.MILLISECONDS);
            if (!optTokens.isPresent()) {
                return RefreshResult.fail(FailReason.PERMANENT_ERROR, "Could not get tokens from session");
            }

            tokens = optTokens.get();
        } catch (TimeoutException e) {
            return RefreshResult.fail(FailReason.LOCK_TIMEOUT, "Lock timeout for expired session oauth token exceeded", e);
        }

        if (tokens.accessExpiresWithin(refreshConfig.getRefreshThresholdMillis(), TimeUnit.MILLISECONDS)) {
            long lockMillisLeft = lockTimeoutMillis - (System.currentTimeMillis() - start);
            if (lockMillisLeft <= 0) {
                return RefreshResult.fail(FailReason.LOCK_TIMEOUT, "Lock timeout for expired session oauth token exceeded");
            }

            LOG.debug("Refreshing oauth tokens...");
            return refreshTokens(tokens, lockMillisLeft, TimeUnit.MILLISECONDS);
        }

        return RefreshResult.success(SuccessReason.NON_EXPIRED);
    }

    private RefreshResult refreshTokens(OAuthTokens oldTokens, long lockTimeout, TimeUnit lockUnit) throws InterruptedException, OXException {
        try {
            return tokenGetterSetter.doThrowableAtomic(session, lockTimeout, lockUnit, () -> {
                Optional<OAuthTokens> optTokens = tokenGetterSetter.getFromSession(session);
                if (!optTokens.isPresent()) {
                    return RefreshResult.fail(FailReason.PERMANENT_ERROR, "Could not get tokens from session");
                }

                OAuthTokens tokens = optTokens.get();
                if (!oldTokens.getAccessToken().equals(tokens.getAccessToken())) {
                    LOG.debug("Tokens were already refreshed by another thread");
                    return RefreshResult.success(SuccessReason.CONCURRENT_REFRESH);
                }

                TokenRefreshResponse response = refresher.execute(tokens);
                if (response.isSuccess()) {
                    return handleSuccess(response.getTokens());

                }

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
            LOG.info("Discarding refreshed tokens for session '{}'. Expiration is lower than configured refresh threshold: {}sec / {}sec",
                session.getSessionID(),
                TimeUnit.MILLISECONDS.toSeconds(tokens.getExpiresInMillis()),
                refreshConfig.getRefreshThresholdUnit().toSeconds(refreshConfig.getRefreshThreshold()));
            return RefreshResult.fail(FailReason.PERMANENT_ERROR, "Expiration date of new tokens is lower than refresh threshold");
        }

        tokenGetterSetter.setInSession(session, tokens);
        SessiondService sessiondService = services.getService(SessiondService.class);
        if (sessiondService == null) {
            LOG.warn("Storing tokens in stored session failed. SessiondService unavailable.");
        } else {
            try {
                LOG.info("Storing updated tokens in stored session '{}'", session.getSessionID());
                sessiondService.storeSession(session.getSessionID(), false);
            } catch (OXException e) {
                LOG.warn("Storing tokens in stored session failed", e);
            }
        }

        return RefreshResult.success(SuccessReason.REFRESHED);
    }

    private RefreshResult handleError(OAuthTokens oldTokens, Error error) {
        switch (error.getType()) {
            case INVALID_REFRESH_TOKEN:
                LOG.info("Token refresh failed for due to invalid refresh token for session '{}'", session.getSessionID());
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
