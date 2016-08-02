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

package com.openexchange.websockets.grizzly;

import java.lang.reflect.UndeclaredThrowableException;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import javax.servlet.http.HttpServletRequest;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.util.Parameters;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.HandshakeException;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketException;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.slf4j.Logger;
import com.openexchange.ajax.SessionUtility;
import com.openexchange.config.ConfigurationService;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig.Property;
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
import com.openexchange.session.UserAndContext;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.sessiond.SessiondServiceExtended;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.user.UserService;
import com.openexchange.websockets.grizzly.http.GrizzlyWebSocketHttpServletRequest;

/**
 * {@link GrizzlyWebSocketApplication}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GrizzlyWebSocketApplication extends WebSocketApplication {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketApplication.class);

    private final ConcurrentMap<UserAndContext, ConcurrentMap<ConnectionId, SessionBoundWebSocket>> openSockets;
    private final ServiceLookup services;
    private final WebSocketListenerRegistry listenerRegistry;
    private final CookieHashSource hashSource;

    /**
     * Initializes a new {@link GrizzlyWebSocketApplication}.
     */
    public GrizzlyWebSocketApplication(WebSocketListenerRegistry listenerRegistry, ServiceLookup services) {
        super();
        this.listenerRegistry = listenerRegistry;
        this.services = services;
        openSockets = new ConcurrentHashMap<>(265, 0.9F, 1);
        ConfigurationService configService = services.getService(ConfigurationService.class);
        hashSource = CookieHashSource.parse(configService.getProperty(Property.COOKIE_HASH.getPropertyName()));
    }

    /**
     * Shuts-down this application.
     */
    public void shutDown() {
        for (Iterator<ConcurrentMap<ConnectionId, SessionBoundWebSocket>> i = openSockets.values().iterator(); i.hasNext();) {
            for (Iterator<SessionBoundWebSocket> iter = i.next().values().iterator(); iter.hasNext();) {
                SessionBoundWebSocket sessionBoundSocket = iter.next();
                sessionBoundSocket.close();
                iter.remove();
            }
            i.remove();
        }
    }

    /**
     * Asynchronously sends specified text message to all locally managed Web Socket connections.
     *
     * @param message The text message to send
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public Future<Void> sendToUserAsync(String message, int userId, int contextId) {
        SendToUserTask task = new SendToUserTask(message, userId, contextId, this);
        return ThreadPools.submitElseExecute(task);
    }

    /**
     * Sends specified text message to all locally managed Web Socket connections.
     *
     * @param message The text message to send
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void sendToUser(String message, int userId, int contextId) {
        ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null != userSockets) {
            for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
                sessionBoundSocket.send(message);
            }
        }
    }

    /**
     * Gets a listing of identifiers for sessions currently bound to an active Web Socket connection.
     *
     * @return The session identifier listing
     */
    public Map<String, SessionBoundWebSocket> getActiveSessions() {
        Map<String, SessionBoundWebSocket> sessions = new HashMap<>(32, 0.9F);
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            for (SessionBoundWebSocket socket : userSockets.values()) {
                String sessionId = socket.getSessionId();
                if (null != sessionId) {
                    sessions.put(sessionId, socket);
                }
            }
        }
        return sessions;
    }

    /**
     * Closes specified Web Socket connection.
     *
     * @param socket The Web Socket
     * @param optSession The optional session associated with the socket to close
     */
    public void close(WebSocket socket, Session optSession) {
        try {
            if (socket instanceof SessionBoundWebSocket) {
                boolean found = false;
                if (null != optSession) {
                    ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(optSession));
                    if (null != userSockets) {
                        for (Iterator<SessionBoundWebSocket> iter = userSockets.values().iterator(); !found && iter.hasNext();) {
                            if (socket.equals(iter.next())) {
                                iter.remove();
                                found = true;
                            }
                        }
                    }
                } else {
                    for (Iterator<ConcurrentMap<ConnectionId, SessionBoundWebSocket>> i = openSockets.values().iterator(); !found && i.hasNext();) {
                        for (Iterator<SessionBoundWebSocket> iter = i.next().values().iterator(); !found && iter.hasNext();) {
                            if (socket.equals(iter.next())) {
                                iter.remove();
                                found = true;
                            }
                        }
                    }
                }
            } else {
                remove(socket);
            }
        } finally {
            socket.close();
        }
    }

    // ------------------------------------------------------ Methods from WebSocketApplication ------------------------------------------------------

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        // Parse request packet's URL parameters
        Parameters parameters = new Parameters();
        parameters.setQueryStringEncoding(Charset.forName("UTF-8"));
        parameters.setQuery(requestPacket.getQueryStringDC());

        // Check for "session" parameter
        String sessionId = parameters.getParameter("session");
        if (sessionId == null) {
            throw new HandshakeException("Missing parameter 'session'");
        }

        // Look-up optional connection identifier; generate a new, unique one if absent
        String conId = parameters.getParameter("connection");
        if (conId == null) {
            conId = com.openexchange.java.util.UUIDs.getUnformattedStringFromRandom();
        }

        // Get and verify session
        Session session = checkSession(sessionId, requestPacket, parameters);

        // Apply initial listeners
        List<WebSocketListener> listenersToUse = new LinkedList<>();
        if (null != listeners) {
            for (WebSocketListener listener : listeners) {
                if (null != listener) {
                    listenersToUse.add(listener);
                }
            }
        }
        listenersToUse.addAll(listenerRegistry.getListeners());

        // Create & return new session-bound Web Socket
        return new SessionBoundWebSocket(SessionInfo.newInstance(session), ConnectionId.newInstance(conId), handler, requestPacket, listenersToUse.toArray(new WebSocketListener[listenersToUse.size()]));
    }

    private Session checkSession(String sessionId, HttpRequestPacket requestPacket, Parameters parameters) {
        // Acquire needed service
        SessiondService sessiondService = SessiondService.SERVICE_REFERENCE.get();
        if (null == sessiondService) {
            org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketSessionToucher.class);
            logger.warn("", ServiceExceptionCode.absentService(SessiondServiceExtended.class));
            throw new HandshakeException("Missing parameter Sessiond service.");
        }

        // Look-up appropriate session
        Session session = sessiondService.getSession(sessionId);
        if (null == session) {
            throw new HandshakeException("No such session: " + sessionId);
        }
        if (!sessionId.equals(session.getSessionID())) {
            LOG.info("Request's session identifier \"{}\" differs from the one indicated by SessionD service \"{}\".", sessionId, session.getSessionID());
            throw new HandshakeException("Wrong session: " + sessionId);
        }

        // Check context
        Context context = getContextFrom(session);
        if (!context.isEnabled()) {
            sessiondService.removeSession(sessionId);
            LOG.info("The context {} associated with session is locked.", Integer.toString(session.getContextId()));
            throw new HandshakeException("Context locked: " + session.getContextId());
        }

        // Check user
        User user = getUserFrom(session, context, sessiondService);
        if (!user.isMailEnabled()) {
            LOG.info("User {} in context {} is not activated.", Integer.toString(user.getId()), Integer.toString(session.getContextId()));
            throw new HandshakeException("Session expired: " + sessionId);
        }

        // Check cookies/secret
        try {
            HttpServletRequest servletRequest = new GrizzlyWebSocketHttpServletRequest(requestPacket, parameters);
            SessionUtility.checkSecret(hashSource, servletRequest, session);
        } catch (OXException e) {
            throw new HandshakeException(e.getPlainLogMessage());
        }

        // Check IP address
        try {
            SessionUtility.checkIP(session, requestPacket.getRemoteAddress());
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
                LOG.info("The context associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                throw new HandshakeException("Session expired: " + sessionId);
            }
            if (UserExceptionCode.USER_NOT_FOUND.getPrefix().equals(e.getPrefix())) {
                int code = e.getCode();
                if (UserExceptionCode.USER_NOT_FOUND.getNumber() == code || LdapExceptionCode.USER_NOT_FOUND.getNumber() == code) {
                    // An outdated session; user absent
                    sessiondService.removeSession(sessionId);
                    LOG.info("The user associated with session \"{}\" cannot be found. Obviously an outdated session which is invalidated now.", sessionId);
                    throw new HandshakeException("Session expired: " + sessionId);
                }
            }
            throw new WebSocketException(e);
        } catch (UndeclaredThrowableException e) {
            throw new WebSocketException(e);
        }
    }

    @Override
    public void onConnect(WebSocket socket) {
        // Override this method to take control over socket collection
        if (socket instanceof SessionBoundWebSocket) {
            SessionBoundWebSocket sessionBoundSocket = (SessionBoundWebSocket) socket;

            UserAndContext userAndContext = UserAndContext.newInstance(sessionBoundSocket.getUserId(), sessionBoundSocket.getContextId());
            ConcurrentMap<ConnectionId, SessionBoundWebSocket> sockets = openSockets.get(userAndContext);
            if (sockets == null) {
                sockets = new ConcurrentHashMap<>(8, 0.9F, 1);
                ConcurrentMap<ConnectionId, SessionBoundWebSocket> existing = openSockets.putIfAbsent(userAndContext, sockets);
                if (existing != null) {
                    sockets = existing;
                }
            }

            if (null != sockets.putIfAbsent(sessionBoundSocket.getConnectionId(), sessionBoundSocket)) {
                throw new HandshakeException("Such a Web Socket connection already exists: " + sessionBoundSocket.getConnectionId());
            }

            synchronized (sessionBoundSocket) {
                Collection<WebSocketListener> listeners = sessionBoundSocket.getListeners();
                for (WebSocketListener listener : listenerRegistry.getListeners()) {
                    if (!listeners.contains(listener)) {
                        listeners.add(listener);
                    }
                }
            }
        } else {
            super.onConnect(socket);
        }
    }

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        // Override this method to take control over socket collection
        if (socket instanceof SessionBoundWebSocket) {
            SessionBoundWebSocket sessionBoundSocket = (SessionBoundWebSocket) socket;

            UserAndContext userAndContext = UserAndContext.newInstance(sessionBoundSocket.getUserId(), sessionBoundSocket.getContextId());
            ConcurrentMap<ConnectionId, SessionBoundWebSocket> sockets = openSockets.get(userAndContext);
            if (sockets != null) {
                sockets.remove(sessionBoundSocket.getConnectionId());
            }

            socket.close();
        } else {
            super.onClose(socket, frame);
        }
    }

    // ----------------------------------------------- Listener management -------------------------------------------------

    /**
     * Adds specified listener to existing Web Sockets
     *
     * @param listener The listener to add
     */
    public void addWebSocketListener(WebSocketListener listener) {
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
                synchronized (sessionBoundSocket) {
                    Collection<WebSocketListener> listeners = sessionBoundSocket.getListeners();
                    if (!listeners.contains(listener)) {
                        listeners.add(listener);
                    }
                }
            }
        }
    }

    /**
     * Removes specified listener to existing Web Sockets
     *
     * @param listener The listener to remove
     */
    public void removeWebSocketListener(WebSocketListener listener) {
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
                sessionBoundSocket.remove(listener);
            }
        }
    }

}
