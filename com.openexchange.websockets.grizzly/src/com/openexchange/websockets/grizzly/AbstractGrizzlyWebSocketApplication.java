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

import static com.openexchange.java.Autoboxing.I;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.Protocol;
import org.glassfish.grizzly.http.util.Parameters;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.HandshakeException;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocket;
import org.glassfish.grizzly.websockets.WebSocketApplication;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.slf4j.Logger;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableSet;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.cascade.ComposedConfigProperty;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.configuration.CookieHashSource;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.session.UserAndContext;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.WebSockets;
import com.openexchange.websockets.grizzly.auth.GrizzlyWebSocketAuthenticator;

/**
 * {@link AbstractGrizzlyWebSocketApplication} - A Web Socket application accepting only session-bound Web Sockets.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public abstract class AbstractGrizzlyWebSocketApplication<S extends SessionBoundWebSocket> extends WebSocketApplication {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractGrizzlyWebSocketApplication.class);

    private static final String LOCAL_HOST;
    static {
        String fbHost;
        try {
            fbHost = InetAddress.getLocalHost().getHostAddress();
        } catch (final UnknownHostException e) {
            fbHost = "localhost";
        }
        LOCAL_HOST = fbHost;
    }

    /**
     * Gets the address of the local host.
     *
     * @return The local host's address
     */
    public static String getLocalHost() {
        return LOCAL_HOST;
    }

    private static final AtomicReference<GrizzlyWebSocketAuthenticator> AUTHENTICATOR_REFERENCE = new AtomicReference<GrizzlyWebSocketAuthenticator>();

    /**
     * Sets the authenticator to use
     *
     * @param authenticator The authenticator to use
     * @return The previously tracked authenticator or <code>null</code>
     */
    public static GrizzlyWebSocketAuthenticator setGrizzlyWebSocketAuthenticator(GrizzlyWebSocketAuthenticator authenticator) {
        if (null == authenticator) {
            throw new IllegalArgumentException("Authenticator is null.");
        }

        GrizzlyWebSocketAuthenticator s;
        do {
            s = AUTHENTICATOR_REFERENCE.get();
        } while (!AUTHENTICATOR_REFERENCE.compareAndSet(s, authenticator));
        return s;
    }

    /**
     * Unsets the application
     */
    public static void unsetGrizzlyWebSocketAuthenticator() {
        AUTHENTICATOR_REFERENCE.set(null);
    }

    // ----------------------------------------------------------------------------------------------------------------------------------------------

    protected final ConcurrentMap<UserAndContext, ConcurrentMap<ConnectionId, S>> openSockets;
    protected final ServiceLookup services;
    protected final CookieHashSource hashSource;
    protected final DefaultGrizzlyWebSocketAuthenticator defaultAuthenticator;
    private final Class<S> socketClass;

    /**
     * Initializes a new {@link AbstractGrizzlyWebSocketApplication}.
     */
    protected AbstractGrizzlyWebSocketApplication(ServiceLookup services, Class<S> socketClass) {
        super();
        this.services = services;
        this.socketClass = socketClass;
        openSockets = new ConcurrentHashMap<>(265, 0.9F, 1);
        ConfigurationService configService = services.getService(ConfigurationService.class);
        hashSource = CookieHashSource.parse(configService.getProperty(Property.COOKIE_HASH.getPropertyName()));
        defaultAuthenticator = new DefaultGrizzlyWebSocketAuthenticator(hashSource, services, LOGGER);
    }

    /**
     * Shuts-down this application.
     */
    public void shutDown() {
        for (Iterator<ConcurrentMap<ConnectionId, S>> i = openSockets.values().iterator(); i.hasNext();) {
            for (Iterator<S> iter = i.next().values().iterator(); iter.hasNext();) {
                S sessionBoundSocket = iter.next();
                sessionBoundSocket.send("session:invalid");
                closeSocketSafe(sessionBoundSocket);
                iter.remove();
            }
            i.remove();
        }
        for (WebSocket socket : ImmutableSet.<WebSocket> copyOf(getWebSockets())) {
            remove(socket);
            socket.close();
        }
    }

    /**
     * Safely closes specified socket.
     *
     * @param webSocket The Web Socket to close
     */
    protected void closeSocketSafe(S webSocket) {
        try {
            webSocket.close();
        } catch (Exception e) {
            LOGGER.error("Failed closing Web Socket ({}) with path \"{}\" for user {} in context {}", webSocket.getConnectionId(), webSocket.getPath(), I(webSocket.getUserId()), I(webSocket.getContextId()), e);
        }
    }

    /**
     * Gets the number of open Web Sockets for this application
     *
     * @return The number of open Web Sockets
     */
    public long getNumberOfWebSockets() {
        long count = 0L;
        for (ConcurrentMap<ConnectionId, S> userSockets : openSockets.values()) {
            count += userSockets.size();
        }
        return count;
    }

    /**
     * Closes the Web Socket matching given identifier.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param connectionId The connection identifier
     * @return <code>true</code> if such a Web Socket was closed; otherwise <code>false</code>
     */
    public boolean closeWebSockets(int userId, int contextId, ConnectionId connectionId) {
        if (null == connectionId) {
            return false;
        }

        ConcurrentMap<ConnectionId, S> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets) {
            return false;
        }

        S sessionBoundSocket = userSockets.remove(connectionId);
        if (null == sessionBoundSocket) {
            return false;
        }

        sessionBoundSocket.send("session:invalid");
        closeSocketSafe(sessionBoundSocket);
        LOGGER.debug("Closed Web Socket ({}) with path \"{}\" for user {} in context {}.", sessionBoundSocket.getConnectionId(), sessionBoundSocket.getPath(), I(sessionBoundSocket.getUserId()), I(sessionBoundSocket.getContextId()));
        return true;
    }

    /**
     * Closes all locally available Web Sockets matching specified path filter expression (if any).
     * <p>
     * In case no path filter expression is given (<code>pathFilter == null</code>), all user-associated Web Sockets are closed.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param pathFilter The optional path filter expression or <code>null</code>
     */
    public void closeWebSockets(int userId, int contextId, String pathFilter) {
        ConcurrentMap<ConnectionId, S> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets) {
            return;
        }

        for (Iterator<S> it = userSockets.values().iterator(); it.hasNext();) {
            S sessionBoundSocket = it.next();
            if (WebSockets.matches(pathFilter, sessionBoundSocket.getPath())) {
                sessionBoundSocket.send("session:invalid");
                closeSocketSafe(sessionBoundSocket);
                it.remove();
                LOGGER.debug("Closed Web Socket ({}) with path \"{}\" for user {} in context {}.", sessionBoundSocket.getConnectionId(), sessionBoundSocket.getPath(), I(sessionBoundSocket.getUserId()), I(sessionBoundSocket.getContextId()));
            }
        }
    }

    /**
     * Closes all locally available Web Sockets associated with specified session.
     *
     * @param sessionId The session identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void closeWebSocketForSession(String sessionId, int userId, int contextId) {
        if (null == sessionId) {
            return;
        }

        ConcurrentMap<ConnectionId, S> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets) {
            return;
        }

        for (Iterator<S> it = userSockets.values().iterator(); it.hasNext();) {
            S sessionBoundSocket = it.next();
            if (sessionId.equals(sessionBoundSocket.getSessionId())) {
                sessionBoundSocket.send("session:invalid");
                closeSocketSafe(sessionBoundSocket);
                it.remove();
                LOGGER.debug("Closed Web Socket ({}) with path \"{}\" bound to dropped/removed session {} for user {} in context {}.", sessionBoundSocket.getConnectionId(), sessionBoundSocket.getPath(), sessionId, I(sessionBoundSocket.getUserId()), I(sessionBoundSocket.getContextId()));
            }
        }
    }

    /**
     * Checks if the Web Socket associated with specified connection identifier exists.
     *
     * @param connectionId The connection identifier
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if such a Web Socket exists; otherwise <code>false</code>
     */
    public boolean exists(ConnectionId connectionId, int userId, int contextId) {
        ConcurrentMap<ConnectionId, S> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        return null == userSockets ? false : userSockets.containsKey(connectionId);
    }

    /**
     * Gets a listing of identifiers for sessions currently bound to an active Web Socket connection.
     *
     * @return The session identifier listing
     */
    public Map<String, List<S>> getActiveSessions() {
        Map<String, List<S>> sessions = new HashMap<>(32, 0.9F);
        for (ConcurrentMap<ConnectionId, S> userSockets : openSockets.values()) {
            for (S socket : userSockets.values()) {
                String sessionId = socket.getSessionId();
                if (null != sessionId) {
                    List<S> sockets = sessions.get(sessionId);
                    if (null == sockets) {
                        sockets = new LinkedList<>();
                        sessions.put(sessionId, sockets);
                    }
                    sockets.add(socket);
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
        if (socketClass.isInstance(socket)) {
            S sessionBoundSocket = socketClass.cast(socket);
            boolean found = false;
            if (null != optSession) {
                ConcurrentMap<ConnectionId, S> userSockets = openSockets.get(UserAndContext.newInstance(optSession));
                if (null != userSockets) {
                    for (Iterator<S> iter = userSockets.values().iterator(); !found && iter.hasNext();) {
                        if (sessionBoundSocket.equals(iter.next())) {
                            iter.remove();
                            found = true;
                        }
                    }
                }
            } else {
                for (Iterator<ConcurrentMap<ConnectionId, S>> i = openSockets.values().iterator(); !found && i.hasNext();) {
                    for (Iterator<S> iter = i.next().values().iterator(); !found && iter.hasNext();) {
                        if (sessionBoundSocket.equals(iter.next())) {
                            iter.remove();
                            found = true;
                        }
                    }
                }
            }
            closeSocketSafe(sessionBoundSocket);
        } else {
            remove(socket);
            socket.close();
        }
    }

    /**
     * Handles the specified <code>HandshakeException</code>.
     *
     * @param e The exception to handle
     * @param handler The associated protocol handler
     * @param requestPacket The request package
     */
    protected void handleHandshakeException(HandshakeException e, ProtocolHandler handler, HttpRequestPacket requestPacket) {
        FilterChainContext ctx = handler.getFilterChainContext();
        HttpResponsePacket response = requestPacket.getResponse();
        response.setProtocol(Protocol.HTTP_1_1);
        response.setStatus(401);
        response.setReasonPhrase("Authorization Required");
        ctx.write(HttpContent.builder(response).build());
    }

    /**
     * Gets the authenticator to use
     *
     * @return The authenticator
     */
    protected GrizzlyWebSocketAuthenticator getAuthenticator() {
        GrizzlyWebSocketAuthenticator authenticator = AUTHENTICATOR_REFERENCE.get();
        return null == authenticator ? defaultAuthenticator : authenticator;
    }

    /**
     * Checks validity of specified session
     *
     * @param sessionId The session identifier
     * @param requestPacket The request package
     * @param parameters The request parameters
     * @return The validated session
     */
    protected Session checkSession(String sessionId, HttpRequestPacket requestPacket, Parameters parameters) {
        return getAuthenticator().checkSession(sessionId, requestPacket, parameters);
    }

    /**
     * Checks if a Web Socket is allowed to be established for given session by this application
     *
     * @param session The session
     * @return <code>true</code> if allowed; otherwise <code>false</code>
     */
    protected boolean doCheckEnabled(Session session) {
        try {
            ConfigViewFactory configViewFactory = services.getOptionalService(ConfigViewFactory.class);
            if (null == configViewFactory) {
                throw ServiceExceptionCode.absentService(ConfigViewFactory.class);
            }

            ConfigView view = configViewFactory.getView(session.getUserId(), session.getContextId());
            ComposedConfigProperty<Boolean> property = view.property("com.openexchange.websockets.enabled", boolean.class);
            if (null == property || !property.isDefined()) {
                // Default is true
                return true;
            }

            return property.get().booleanValue();
        } catch (OXException e) {
            throw new HandshakeException(e.getPlainLogMessage());
        }
    }

    // ------------------------------------------------------ Methods from org.glassfish.grizzly.websockets.WebSocketApplication ------------------------------------------------------

    @Override
    public WebSocket createSocket(ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener... listeners) {
        try {
            // Parse request packet's URL parameters
            Parameters parameters = new Parameters();
            parameters.setQueryStringEncoding(Charset.forName("UTF-8"));
            parameters.setQuery(requestPacket.getQueryStringDC());
            parameters.handleQueryParameters();

            // Check for "session" parameter
            String sessionId = parameters.getParameter("session");
            if (sessionId == null) {
                throw new HandshakeException("Missing parameter 'session'");
            }

            // Look-up optional connection identifier; generate a new, unique one if absent
            boolean validateAbsence = true;
            ConnectionId connectionId;
            {
                String sConId = parameters.getParameter("connection");
                if (sConId == null) {
                    sConId = com.openexchange.java.util.UUIDs.getUnformattedStringFromRandom();
                    validateAbsence = false;
                }
                connectionId = ConnectionId.newInstance(sConId);
            }

            // Get and verify session
            Session session = checkSession(sessionId, requestPacket, parameters);

            // Check if enabled for given session
            if (false == isEnabledFor(session)) {
                throw new HandshakeException("Web Sockets not allowed for user " + session.getUserId() + " in context " + session.getContextId());
            }

            // Check if such a Web Socket already exists
            if (validateAbsence && exists(connectionId, session.getUserId(), session.getContextId())) {
                throw new HandshakeException("Such a Web Socket connection already exists: " + connectionId);
            }

            return doCreateSocket(session, connectionId, parameters, handler, requestPacket, listeners);
        } catch (HandshakeException e) {
            // Handle Handshake error
            handleHandshakeException(e, handler, requestPacket);
            throw e;
        }
    }

    /**
     * Creates the session-bound socket using given arguments.
     *
     * @param session The associated session
     * @param connectionId The socket's connection identifier
     * @param parameters The parsed request parameters
     * @param handler The protocol handler
     * @param requestPacket The request package
     * @param listeners The initial listeners to apply
     * @throws HandshakeException If Web Socket hand-shake is supposed to fail
     */
    protected abstract S doCreateSocket(Session session, ConnectionId connectionId, Parameters parameters, ProtocolHandler handler, HttpRequestPacket requestPacket, WebSocketListener[] listeners) throws HandshakeException;

    @Override
    public void onConnect(WebSocket socket) {
        // Override this method to take control over socket collection
        boolean error = true; // Pessimistic...
        try {
            if (socketClass.isInstance(socket)) {
                S sessionBoundSocket = socketClass.cast(socket);

                int MAX_SIZE = getMaxSize();

                UserAndContext userAndContext = UserAndContext.newInstance(sessionBoundSocket.getUserId(), sessionBoundSocket.getContextId());
                ConcurrentMap<ConnectionId, S> userSockets = openSockets.get(userAndContext);
                if (userSockets == null) {
                    userSockets = new ConcurrentHashMap<>(MAX_SIZE, 0.9F, 1);
                    ConcurrentMap<ConnectionId, S> existing = openSockets.putIfAbsent(userAndContext, userSockets);
                    if (existing != null) {
                        userSockets = existing;
                    }
                }

                synchronized (userSockets) {
                    if (userSockets.size() == MAX_SIZE) {
                        // Max. number of sockets per user exceeded
                        throw new HandshakeException("Max. number of Web Sockets (" + MAX_SIZE + ") exceeded for user " + userAndContext.getUserId() + " in context " + userAndContext.getContextId());
                    }

                    if (null != userSockets.putIfAbsent(sessionBoundSocket.getConnectionId(), sessionBoundSocket)) {
                        throw new HandshakeException("Such a Web Socket connection already exists: " + sessionBoundSocket.getConnectionId());
                    }
                }

                onConnectedSocket(sessionBoundSocket);

                LOGGER.debug("Accepted Web Socket ({}) with path \"{}\" for user {} in context {}.", sessionBoundSocket.getConnectionId(), sessionBoundSocket.getPath(), I(sessionBoundSocket.getUserId()), I(sessionBoundSocket.getContextId()));
            } else {
                super.onConnect(socket);
            }
            error = false;
        } finally {
            if (error) {
                socket.close();
            }
        }
    }

    /**
     * Call-back for connected session-bound Web Sockets.
     *
     * @param sessionBoundSocket The session-bound Web Socket that has been connected
     */
    protected abstract void onConnectedSocket(S sessionBoundSocket);

    /**
     * Gets the max. number of sockets allowed per user.
     *
     * @return The max. number of sockets
     */
    protected abstract int getMaxSize();

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        // Override this method to take control over socket collection
        if (socketClass.isInstance(socket)) {
            S sessionBoundSocket = socketClass.cast(socket);

            UserAndContext key = UserAndContext.newInstance(sessionBoundSocket.getUserId(), sessionBoundSocket.getContextId());
            ConcurrentMap<ConnectionId, S> userSockets = openSockets.get(key);
            if (userSockets != null) {
                userSockets.remove(sessionBoundSocket.getConnectionId());
            }

            closeSocketSafe(sessionBoundSocket);

            onClosedSocket(sessionBoundSocket);

            LOGGER.debug("Closed Web Socket ({}) with path \"{}\" due to connection closure for user {} in context {}.", sessionBoundSocket.getConnectionId(), sessionBoundSocket.getPath(), I(sessionBoundSocket.getUserId()), I(sessionBoundSocket.getContextId()));
        } else {
            super.onClose(socket, frame);
        }
    }

    /**
     * Call-back for closed session-bound Web Sockets.
     *
     * @param sessionBoundSocket The session-bound Web Socket that has been connected
     */
    protected abstract void onClosedSocket(S sessionBoundSocket);

    // --------------------------------------------- Cache for enablement ----------------------------------------------------------

    private static final Cache<UserAndContext, Boolean> CACHE_AVAILABILITY = CacheBuilder.newBuilder().maximumSize(65536).expireAfterWrite(30, TimeUnit.MINUTES).build();

    /**
     * Invalidates the <i>enabled cache</i>.
     */
    public static void invalidateEnabledCache() {
        CACHE_AVAILABILITY.invalidateAll();
    }

    private boolean isEnabledFor(Session session) {
        UserAndContext key = UserAndContext.newInstance(session);
        Boolean result = CACHE_AVAILABILITY.getIfPresent(key);
        if (null == result) {
            result = Boolean.valueOf(doCheckEnabled(session));
            CACHE_AVAILABILITY.put(key, result);
        }
        return result.booleanValue();
    }

}
