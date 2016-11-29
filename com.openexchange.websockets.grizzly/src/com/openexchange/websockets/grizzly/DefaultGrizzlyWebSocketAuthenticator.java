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

package com.openexchange.websockets.grizzly;

import java.lang.reflect.UndeclaredThrowableException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
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
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
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
            throw new HandshakeException("Missing parameter Sessiond service.");
        }

        // Look-up appropriate session
        Session session = sessiond instanceof SessiondServiceExtended ? ((SessiondServiceExtended) sessiond).getSession(sessionId, false) : sessiond.getSession(sessionId);
        if (null == session) {
            throw new HandshakeException("No such session: " + sessionId);
        }
        if (!sessionId.equals(session.getSessionID())) {
            logger.info("Request's session identifier \"{}\" differs from the one indicated by SessionD service \"{}\".", sessionId, session.getSessionID());
            throw new HandshakeException("Wrong session: " + sessionId);
        }

        // Check context
        Context context = getContextFrom(session);
        if (!context.isEnabled()) {
            sessiond.removeSession(sessionId);
            logger.info("The context {} associated with session is locked.", Integer.toString(session.getContextId()));
            throw new HandshakeException("Context locked: " + session.getContextId());
        }

        // Check user
        User user = getUserFrom(session, context, sessiond);
        if (!user.isMailEnabled()) {
            logger.info("User {} in context {} is not activated.", Integer.toString(user.getId()), Integer.toString(session.getContextId()));
            throw new HandshakeException("Session expired: " + sessionId);
        }

        // Check cookies/secret
        try {
            Cookie[] cookies = request.getCookies();
            if (cookies == null) {
                // No cookies available. Hence, no need to check secret.
                throw SessionExceptionCodes.WRONG_SESSION_SECRET.create();
            }

            // Check secret...
            SessionUtility.checkSecret(hashSource, request, session);
        } catch (OXException e) {
            throw new HandshakeException(e.getPlainLogMessage());
        }

        // Check IP address
        try {
            SessionUtility.checkIP(session, request.getRemoteAddr());
        } catch (OXException e) {
            throw new HandshakeException(e.getPlainLogMessage());
        }

        // All fine...
        return session;
    }

    private Context getContextFrom(Session session) {
        try {
            return services.getService(ContextService.class).getContext(session.getContextId());
        } catch (OXException e) {
            throw new HandshakeException("No such context: " + session.getContextId());
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
                throw new HandshakeException("Session expired: " + sessionId);
            }
            if (UserExceptionCode.USER_NOT_FOUND.getPrefix().equals(e.getPrefix())) {
                int code = e.getCode();
                if (UserExceptionCode.USER_NOT_FOUND.getNumber() == code || LdapExceptionCode.USER_NOT_FOUND.getNumber() == code) {
                    // An outdated session; user absent
                    sessiondService.removeSession(sessionId);
                    logger.info("The user associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                    throw new HandshakeException("Session expired: " + sessionId);
                }
            }
            throw new WebSocketException(e);
        } catch (UndeclaredThrowableException e) {
            throw new WebSocketException(e);
        }
    }

}
