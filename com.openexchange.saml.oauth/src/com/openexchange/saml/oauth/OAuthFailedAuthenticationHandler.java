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
 *    trademarks of the OX Software GmbH group of companies.
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
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link OAuthFailedAuthenticationHandler}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class OAuthFailedAuthenticationHandler implements AuthenticationFailedHandler {

    private final ServiceLookup services;
    private final OAuthAccessTokenService tokenService;
    private static final Logger LOG = LoggerFactory.getLogger(OAuthAccessTokenService.class);

    /**
     * Initializes a new {@link OAuthFailedAuthenticationHandler}.
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
        return AuthenticationFailureHandlerResult.createErrorResult(SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID()));
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
                return AuthenticationFailureHandlerResult.createErrorResult(SessionExceptionCodes.SESSION_EXPIRED.create(session.getSessionID()));
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
            return AuthenticationFailureHandlerResult.createErrorResult(SessionExceptionCodes.SESSION_EXPIRED.create(x, session.getSessionID()));
        }
    }

}
