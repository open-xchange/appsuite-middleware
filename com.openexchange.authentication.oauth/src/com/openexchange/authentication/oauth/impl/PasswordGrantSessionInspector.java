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

package com.openexchange.authentication.oauth.impl;

import static com.openexchange.sessiond.ExpirationReason.OAUTH_TOKEN_REFRESH_FAILED;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Reply;
import com.openexchange.session.Session;
import com.openexchange.session.inspector.Reason;
import com.openexchange.session.inspector.SessionInspectorService;
import com.openexchange.session.oauth.RefreshResult;
import com.openexchange.session.oauth.RefreshResult.FailReason;
import com.openexchange.session.oauth.SessionOAuthTokenService;
import com.openexchange.session.oauth.TokenRefreshConfig;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link PasswordGrantSessionInspector}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.10.3
 */
public class PasswordGrantSessionInspector implements SessionInspectorService {

    private static final Logger LOG = LoggerFactory.getLogger(PasswordGrantSessionInspector.class);

    private final OAuthAuthenticationConfig config;
    private final ServiceLookup services;

    public PasswordGrantSessionInspector(OAuthAuthenticationConfig config, ServiceLookup services) {
        super();
        this.config = config;
        this.services = services;
    }

    @Override
    public Reply onSessionHit(Session session, HttpServletRequest request, HttpServletResponse response) throws OXException {
        if (Boolean.TRUE.equals(session.getParameter(SessionParameters.PASSWORD_GRANT_MARKER))) {
            try {
                TokenRefresherImpl refresher = new TokenRefresherImpl(session, config, services);
                TokenRefreshConfig refreshConfig = TokenRefreshConfig.newBuilder()
                    .setLockTimeout(config.getTokenLockTimeoutSeconds(), TimeUnit.SECONDS)
                    .setRefreshThreshold(config.getEarlyTokenRefreshSeconds(), TimeUnit.SECONDS)
                    .setTryRecoverStoredTokens(config.tryRecoverStoredTokens())
                    .build();
                RefreshResult result = services.getServiceSafe(SessionOAuthTokenService.class).checkOrRefreshTokens(session, refresher, refreshConfig);
                if (result.isSuccess()) {
                    LOG.debug("Returning neutral reply for session '{}' due to successful token refresh result: {}", session.getSessionID(), result.getSuccessReason().name());
                    return Reply.NEUTRAL;
                }

                return handleErrorResult(session, result);
            } catch (OXException e) {
                LOG.error("Error while checking oauth tokens for session '{}'", session.getSessionID(), e);
                // try to perform request anyway on best effort
                return Reply.NEUTRAL;
            } catch (InterruptedException e) {
                LOG.warn("Thread was interrupted while checking session oauth tokens", e);
                // keep interrupted state
                Thread.currentThread().interrupt();
                return Reply.STOP;
            }
        }
        return Reply.NEUTRAL;
    }

    private Reply handleErrorResult(Session session, RefreshResult result) throws OXException {
        RefreshResult.FailReason failReason = result.getFailReason();
        if (failReason == FailReason.INVALID_REFRESH_TOKEN || failReason == FailReason.PERMANENT_ERROR) {
            if (result.hasException()) {
                LOG.info("Terminating session '{}' due to OAuth token refresh error: {} ({})", session.getSessionID(), failReason.name(), result.getErrorDesc(), result.getException());
            } else {
                LOG.info("Terminating session '{}' due to OAuth token refresh error: {} ({})", session.getSessionID(), failReason.name(), result.getErrorDesc());
            }
            services.getServiceSafe(SessiondService.class).removeSession(session.getSessionID());
            OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
            oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, OAUTH_TOKEN_REFRESH_FAILED.getIdentifier());
            throw oxe;
        }

        // try to perform request anyway on best effort
        if (result.hasException()) {
            LOG.warn("Error while refreshing OAuth tokens for session '{}': {}", session.getSessionID(), result.getErrorDesc(), result.getException());
        } else {
            LOG.warn("Error while refreshing OAuth tokens for session '{}': {}", session.getSessionID(), result.getErrorDesc());
        }
        return Reply.NEUTRAL;
    }

    @Override
    public Reply onSessionMiss(String sessionId, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Reply.NEUTRAL;
    }

    @Override
    public Reply onAutoLoginFailed(Reason reason, HttpServletRequest request, HttpServletResponse response) throws OXException {
        return Reply.NEUTRAL;
    }

}
