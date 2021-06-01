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

package com.openexchange.saml.oauth;

import static com.openexchange.java.Autoboxing.I;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.lock.AccessControl;
import com.openexchange.lock.LockService;
import com.openexchange.lock.ReentrantLockAccessControl;
import com.openexchange.mail.api.AuthType;
import com.openexchange.mail.api.AuthenticationFailedHandler;
import com.openexchange.mail.api.AuthenticationFailureHandlerResult;
import com.openexchange.mail.api.MailConfig;
import com.openexchange.saml.oauth.service.OAuthAccessToken;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService;
import com.openexchange.saml.oauth.service.OAuthAccessTokenService.OAuthGrantType;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.ExpirationReason;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link OAuthFailedAuthenticationHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class OAuthFailedAuthenticationHandler implements AuthenticationFailedHandler {

    private static final Logger LOG = LoggerFactory.getLogger(OAuthAccessTokenService.class);

    private final ServiceLookup services;
    private final OAuthAccessTokenService tokenService;

    /**
     * Initializes a new {@link OAuthFailedAuthenticationHandler}.
     *
     * @param tokenService
     * @param services
     */
    public OAuthFailedAuthenticationHandler(OAuthAccessTokenService tokenService, ServiceLookup services) {
        super();
        this.tokenService = tokenService;
        this.services = services;
    }

    private AccessControl getLockFor(Session session) {
        LockService lockService = services.getOptionalService(LockService.class);
        if (null == lockService) {
            return new ReentrantLockAccessControl((ReentrantLock) session.getParameter(Session.PARAM_LOCK));
        }

        int userId = session.getUserId();
        int contextId = session.getContextId();
        try {
            return lockService.getAccessControlFor(new StringBuilder(32).append("oauthfah-").append(contextId).append('-').append(userId).toString(), 1, userId, contextId);
        } catch (Exception e) {
            LOG.warn("Failed to acquire lock for user {} in context {}. Using global lock instead.", I(userId), I(contextId), e);
            return null;
        }
    }

    @Override
    public AuthenticationFailureHandlerResult handleAuthenticationFailed(OXException failedAuthentication, Service service, MailConfig mailConfig, Session session) throws OXException {
        if (!AuthType.isOAuthType(mailConfig.getAuthType())) {
            return AuthenticationFailureHandlerResult.createContinueResult();
        }

        if (!isSamlAuthenticated(session)) {
            return AuthenticationFailureHandlerResult.createContinueResult();
        }

        SessiondService sessiondService = services.getService(SessiondService.class);
        String oldRefreshToken = (String) session.getParameter(Session.PARAM_OAUTH_REFRESH_TOKEN);
        if (null != oldRefreshToken) {
            // Try to refresh the access token
            AccessControl lock = getLockFor(session);
            if (null == lock) {
                synchronized (session) {
                    return doHandleAuthFailed(session, oldRefreshToken, mailConfig, sessiondService);
                }
            }

            try {
                lock.acquireGrant();
                return doHandleAuthFailed(session, oldRefreshToken, mailConfig, sessiondService);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw OXException.general("Interrupted", e);
            } finally {
                Streams.close(lock);
            }
        }

        // Unable to refresh access token -> logout
        LOG.debug("Unable to refresh access token for user {} in context {}. Session contains no refresh token.", I(session.getUserId()), I(session.getContextId()));
        sessiondService.removeSession(session.getSessionID());
        OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
        oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.OAUTH_TOKEN_REFRESH_FAILED.getIdentifier());
        return AuthenticationFailureHandlerResult.createErrorResult(oxe);
    }

    /**
     * See <code>com.openexchange.saml.SAMLSessionParameters.AUTHENTICATED</code>
     */
    private static final String AUTHENTICATED = "com.openexchange.saml.Authenticated";

    private boolean isSamlAuthenticated(Session session) {
        return "true".equals(session.getParameter(AUTHENTICATED));
    }

    private AuthenticationFailureHandlerResult doHandleAuthFailed(Session session, String oldRefreshToken, MailConfig mailConfig, SessiondService sessiondService) {
        if (false == oldRefreshToken.equals(session.getParameter(Session.PARAM_OAUTH_REFRESH_TOKEN))) {
            // Changed in the meantime...
            mailConfig.setPassword((String) session.getParameter(Session.PARAM_OAUTH_ACCESS_TOKEN));
            return AuthenticationFailureHandlerResult.createRetryResult();
        }

        try {
            OAuthAccessToken accessToken = tokenService.getAccessToken(OAuthGrantType.REFRESH_TOKEN, (String) session.getParameter(Session.PARAM_OAUTH_REFRESH_TOKEN), session.getUserId(), session.getContextId(), null);
            if (accessToken == null) {
                LOG.debug("Unable to refresh access token for user {} in context {}. Session will be invalidated.", I(session.getUserId()), I(session.getContextId()));
                sessiondService.removeSession(session.getSessionID());
                OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID());
                oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.OAUTH_TOKEN_REFRESH_FAILED.getIdentifier());
                return AuthenticationFailureHandlerResult.createErrorResult(oxe);
            }
            session.setParameter(Session.PARAM_OAUTH_ACCESS_TOKEN, accessToken.getAccessToken());
            session.setParameter(Session.PARAM_OAUTH_REFRESH_TOKEN, accessToken.getRefreshToken());
            mailConfig.setPassword(accessToken.getAccessToken());
            sessiondService.storeSession(session.getSessionID());
            LOG.debug("Access token succesfully refreshed for user {} in context {}", I(session.getUserId()), I(session.getContextId()));
            return AuthenticationFailureHandlerResult.createRetryResult();
        } catch (OXException x) {
            // Unable to refresh access token -> logout
            LOG.debug("Unable to refresh access token for user {} in context {}. Session will be invalidated.", I(session.getUserId()), I(session.getContextId()));
            sessiondService.removeSession(session.getSessionID());
            OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(x, session.getSessionID());
            oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.OAUTH_TOKEN_REFRESH_FAILED.getIdentifier());
            return AuthenticationFailureHandlerResult.createErrorResult(oxe);
        }
    }

}
