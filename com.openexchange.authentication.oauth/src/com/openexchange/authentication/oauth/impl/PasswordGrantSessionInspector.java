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

package com.openexchange.authentication.oauth.impl;

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
                LOG.info("Terminating session '{}' due to oauth token refresh error: {} ({})", session.getSessionID(), failReason.name(), result.getErrorDesc(), result.getException());
            } else {
                LOG.info("Terminating session '{}' due to oauth token refresh error: {} ({})", session.getSessionID(), failReason.name(), result.getErrorDesc());
            }
            services.getServiceSafe(SessiondService.class).removeSession(session.getSessionID());
            throw SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
        }

        // try to perform request anyway on best effort
        if (result.hasException()) {
            LOG.warn("Error while refreshing oauth tokens for session '{}': {}", session.getSessionID(), result.getErrorDesc(), result.getException());
        } else {
            LOG.warn("Error while refreshing oauth tokens for session '{}': {}", session.getSessionID(), result.getErrorDesc());
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
