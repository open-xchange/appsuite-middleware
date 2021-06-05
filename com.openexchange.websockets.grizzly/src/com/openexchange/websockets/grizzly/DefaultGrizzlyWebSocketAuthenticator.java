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

package com.openexchange.websockets.grizzly;

import java.lang.reflect.UndeclaredThrowableException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.websockets.HandshakeException;
import org.glassfish.grizzly.websockets.WebSocketException;
import org.slf4j.Logger;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.ExpirationReason;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.user.User;
import com.openexchange.user.UserExceptionCode;
import com.openexchange.user.UserService;
import com.openexchange.websockets.grizzly.auth.GrizzlyWebSocketAuthenticator;

/**
 * {@link DefaultGrizzlyWebSocketAuthenticator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultGrizzlyWebSocketAuthenticator implements GrizzlyWebSocketAuthenticator {

    private final CookieHashSource hashSource;
    private final ServiceLookup services;
    private final Logger logger;

    /**
     * Initializes a new {@link DefaultGrizzlyWebSocketAuthenticator}.
     */
    public DefaultGrizzlyWebSocketAuthenticator(CookieHashSource hashSource, ServiceLookup services, Logger logger) {
        super();
        this.hashSource = hashSource;
        this.services = services;
        this.logger = logger;
    }

    @Override
    public Session checkSession(String sessionId, HttpServletRequest request) throws HandshakeException {
        // Acquire needed service
        SessiondService sessiond = SessiondService.SERVICE_REFERENCE.get();
        if (null == sessiond) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketSessionToucher.class);
            logger.warn("", ServiceExceptionCode.absentService(SessiondServiceExtended.class));
            throw new SessionValidationHandshakeException("Missing Sessiond service.");
        }

        // Look-up appropriate session
        Session session = sessiond.getSession(sessionId, false);
        if (null == session) {
            throw new NoSuchSessionHandshakeException(sessionId);
        }
        if (!sessionId.equals(session.getSessionID())) {
            logger.info("Request's session identifier \"{}\" differs from the one indicated by SessionD service \"{}\".", sessionId, session.getSessionID());
            throw new SessionValidationHandshakeException("Wrong session: " + sessionId);
        }

        // Check context
        Context context = getContextFrom(session);
        if (!context.isEnabled()) {
            sessiond.removeSession(sessionId);
            logger.info("The context {} associated with session is locked.", Integer.toString(session.getContextId()));
            throw new HandshakeException(HttpStatus.FORBIDDEN_403.getStatusCode(), "Context locked: " + session.getContextId());
        }

        // Check user
        User user = getUserFrom(session, context, sessiond);
        if (!user.isMailEnabled()) {
            logger.info("User {} in context {} is not activated.", Integer.toString(user.getId()), Integer.toString(session.getContextId()));
            throw new SessionValidationHandshakeException("Session expired: " + sessionId);
        }

        // Check cookies/secret
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                // No cookies available. Hence, no need to check secret.
                OXException oxe = SessionExceptionCodes.SESSION_EXPIRED.create(sessionId);
                oxe.setProperty(SessionExceptionCodes.OXEXCEPTION_PROPERTY_SESSION_EXPIRATION_REASON, ExpirationReason.NO_EXPECTED_SECRET_COOKIE.getIdentifier());
                throw oxe;
            }

            // Check secret...
            CookieHashSource hashSource = this.hashSource;
            if (null == request.getHeader(Header.UserAgent.toString())) {
                hashSource = CookieHashSource.REMEMBER;
            }
            SessionUtility.checkSecret(hashSource, request, session);
        } catch (OXException e) {
            throw new SessionValidationHandshakeException(e.getPlainLogMessage());
        }

        // Check IP address
        try {
            SessionUtility.checkIP(session, request.getRemoteAddr());
        } catch (OXException e) {
            throw new SessionValidationHandshakeException(e.getPlainLogMessage());
        }

        // All fine...
        return session;
    }

    private Context getContextFrom(Session session) {
        try {
            return services.getService(ContextService.class).getContext(session.getContextId());
        } catch (OXException e) {
            if (e.equalsCode(2, "CTX")) {
                throw new HandshakeException(HttpStatus.FORBIDDEN_403.getStatusCode(), "No such context: " + session.getContextId());
            }
            throw new HandshakeException(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode(), e.getSoleMessage());
        }
    }

    private User getUserFrom(Session session, Context context, SessiondService sessiondService) {
        String sessionId = session.getSessionID();
        try {
            return services.getService(UserService.class).getUser(session.getUserId(), context);
        } catch (OXException e) {
            if (ContextExceptionCodes.NOT_FOUND.equals(e)) {
                // An outdated session; context absent
                sessiondService.removeSession(sessionId);
                logger.info("The context associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                throw new SessionValidationHandshakeException("Session expired: " + sessionId);
            }
            if (UserExceptionCode.USER_NOT_FOUND.getPrefix().equals(e.getPrefix())) {
                int code = e.getCode();
                if (UserExceptionCode.USER_NOT_FOUND.getNumber() == code || LdapExceptionCode.USER_NOT_FOUND.getNumber() == code) {
                    // An outdated session; user absent
                    sessiondService.removeSession(sessionId);
                    logger.info("The user associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                    throw new SessionValidationHandshakeException("Session expired: " + sessionId);
                }
            }
            throw new HandshakeException(HttpStatus.INTERNAL_SERVER_ERROR_500.getStatusCode(), e.getSoleMessage());
        } catch (UndeclaredThrowableException e) {
            throw new WebSocketException(e);
        }
    }

}
