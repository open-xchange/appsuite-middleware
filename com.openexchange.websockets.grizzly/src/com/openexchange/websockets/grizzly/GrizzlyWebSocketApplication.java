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

import static com.openexchange.java.Autoboxing.I;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
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
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.websockets.ConnectionId;
import com.openexchange.websockets.WebSocketInfo;
import com.openexchange.websockets.WebSockets;
import com.openexchange.websockets.grizzly.auth.GrizzlyWebSocketAuthenticator;
import com.openexchange.websockets.grizzly.remote.RemoteWebSocketDistributor;

/**
 * {@link GrizzlyWebSocketApplication}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class GrizzlyWebSocketApplication extends WebSocketApplication {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(GrizzlyWebSocketApplication.class);
    private static final Logger WS_LOGGER = org.slf4j.LoggerFactory.getLogger("WEBSOCKET");

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

    private static final AtomicReference<GrizzlyWebSocketApplication> APPLICATION_REFERENCE = new AtomicReference<GrizzlyWebSocketApplication>();

    /**
     * Initializes a new {@link GrizzlyWebSocketApplication} instance (if not already performed).
     *
     * @param listenerRegistry The listern registry to use
     * @param remoteDistributor The remote distributor to manage remote Web Sockets
     * @param services The service look-up
     * @return The newly created (or existing) instance
     */
    public static GrizzlyWebSocketApplication initializeGrizzlyWebSocketApplication(WebSocketListenerRegistry listenerRegistry, RemoteWebSocketDistributor remoteDistributor, ServiceLookup services) {
        GrizzlyWebSocketApplication app;
        GrizzlyWebSocketApplication newApp;
        do {
            app = APPLICATION_REFERENCE.get();
            if (null != app) {
                return app;
            }
            newApp = new GrizzlyWebSocketApplication(listenerRegistry, remoteDistributor, services);
        } while (!APPLICATION_REFERENCE.compareAndSet(app, newApp));
        return newApp;
    }

    /**
     * Unsets the application
     */
    public static void unsetGrizzlyWebSocketApplication() {
        APPLICATION_REFERENCE.set(null);
    }

    /**
     * Gets the application
     *
     * @return The application or <code>null</code>
     */
    public static GrizzlyWebSocketApplication getGrizzlyWebSocketApplication() {
        return APPLICATION_REFERENCE.get();
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

    // ---------------------------------------------------------------------------------------------------------------

    private final ConcurrentMap<UserAndContext, ConcurrentMap<ConnectionId, SessionBoundWebSocket>> openSockets;
    private final ServiceLookup services;
    private final WebSocketListenerRegistry listenerRegistry;
    private final CookieHashSource hashSource;
    private final RemoteWebSocketDistributor remoteDistributor;
    private final DefaultGrizzlyWebSocketAuthenticator defaultAuthenticator;

    /**
     * Initializes a new {@link GrizzlyWebSocketApplication}.
     */
    private GrizzlyWebSocketApplication(WebSocketListenerRegistry listenerRegistry, RemoteWebSocketDistributor remoteDistributor, ServiceLookup services) {
        super();
        this.listenerRegistry = listenerRegistry;
        this.remoteDistributor = remoteDistributor;
        this.services = services;
        openSockets = new ConcurrentHashMap<>(265, 0.9F, 1);
        ConfigurationService configService = services.getService(ConfigurationService.class);
        hashSource = CookieHashSource.parse(configService.getProperty(Property.COOKIE_HASH.getPropertyName()));
        defaultAuthenticator = new DefaultGrizzlyWebSocketAuthenticator(hashSource, services, LOGGER);
    }

    /**
     * Shuts-down this application.
     */
    public void shutDown() {
        for (Iterator<ConcurrentMap<ConnectionId, SessionBoundWebSocket>> i = openSockets.values().iterator(); i.hasNext();) {
            for (Iterator<SessionBoundWebSocket> iter = i.next().values().iterator(); iter.hasNext();) {
                SessionBoundWebSocket sessionBoundSocket = iter.next();
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
     * Removes all existing Web Socket connections from given candidates and retains the non-existing ones.
     *
     * @param candidates The candidates to remove from
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void retainNonExisting(Set<ConnectionId> candidates, int userId, int contextId) {
        if (null == candidates) {
            return;
        }

        ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets || userSockets.isEmpty()) {
            // None exists
            return;
        }

        for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
            candidates.remove(sessionBoundSocket.getConnectionId());
        }
    }

    /**
     * Gets the number of open Web Sockets on this node
     *
     * @return The number of open Web Sockets
     */
    public long getNumberOfWebSockets() {
        long count = 0L;
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            count += userSockets.size();
        }
        return count;
    }

    /**
     * Lists all currently locally available Web Sockets.
     *
     * @return Locally available Web Sockets
     */
    public List<com.openexchange.websockets.WebSocket> listLocalWebSockets() {
        List<com.openexchange.websockets.WebSocket> websockets = new LinkedList<>();
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
                websockets.add(sessionBoundSocket);
            }
        }
        return websockets;
    }

    /**
     * Lists all locally available Web Socket information.
     *
     * @return All available Web Socket information
     */
    public List<WebSocketInfo> listWebSocketInfo() {
        // Only locally available...
        List<WebSocketInfo> infos = new LinkedList<>();
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
                WebSocketInfo info = WebSocketInfo.builder()
                    .connectionId(sessionBoundSocket.getConnectionId())
                    .contextId(sessionBoundSocket.getContextId())
                    .address(LOCAL_HOST)
                    .path(sessionBoundSocket.getPath())
                    .userId(sessionBoundSocket.getUserId())
                    .build();
                infos.add(info);
            }
        }
        return infos;
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

        ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets) {
            return false;
        }

        SessionBoundWebSocket sessionBoundSocket = userSockets.remove(connectionId);
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
        ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets) {
            return;
        }

        for (Iterator<SessionBoundWebSocket> it = userSockets.values().iterator(); it.hasNext();) {
            SessionBoundWebSocket sessionBoundSocket = it.next();
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

        ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets) {
            return;
        }

        for (Iterator<SessionBoundWebSocket> it = userSockets.values().iterator(); it.hasNext();) {
            SessionBoundWebSocket sessionBoundSocket = it.next();
            if (sessionId.equals(sessionBoundSocket.getSessionId())) {
                sessionBoundSocket.send("session:invalid");
                closeSocketSafe(sessionBoundSocket);
                it.remove();
                LOGGER.debug("Closed Web Socket ({}) with path \"{}\" bound to dropped/removed session {} for user {} in context {}.", sessionBoundSocket.getConnectionId(), sessionBoundSocket.getPath(), sessionId, I(sessionBoundSocket.getUserId()), I(sessionBoundSocket.getContextId()));
            }
        }
    }

    private void closeSocketSafe(SessionBoundWebSocket webSocket) {
        try {
            remoteDistributor.removeWebSocket(webSocket);
            webSocket.close();
        } catch (Exception e) {
            LOGGER.error("Failed closing Web Socket ({}) with path \"{}\" for user {} in context {}", webSocket.getConnectionId(), webSocket.getPath(), I(webSocket.getUserId()), I(webSocket.getContextId()), e);
        }
    }

    /**
     * Asynchronously sends specified text message to all locally managed Web Socket connections.
     *
     * @param message The text message to send
     * @param pathFilter The optional path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param remote Whether the text message was remotely received; otherwise <code>false</code> for local origin
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public Future<Void> sendToUserAsync(String message, String pathFilter, boolean remote, int userId, int contextId) {
        SendToUserTask task = new SendToUserTask(message, pathFilter, remote, userId, contextId, this);
        return ThreadPools.submitElseExecute(task);
    }

    /**
     * Sends specified text message to all locally managed Web Socket connections.
     *
     * @param message The text message to send
     * @param pathFilter The optional path to filter by (e.g. <code>"/websockets/push"</code>)
     * @param remote Whether the text message was remotely received; otherwise <code>false</code> for local origin
     * @param userId The user identifier
     * @param contextId The context identifier
     */
    public void sendToUser(String message, String pathFilter, boolean remote, int userId, int contextId) {
        String info = remote ? "remotely received" : "locally created";

        ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets || userSockets.isEmpty()) {
            WS_LOGGER.debug("Found no local Web Sockets to send {} message \"{}\" to user {} in context {}", info, GrizzlyWebSocketUtils.abbreviateMessageArg(message), I(userId), I(contextId));
            return;
        }

        boolean any = false;
        for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
            if (WebSockets.matches(pathFilter, sessionBoundSocket.getPath())) {
                any = true;
                try {
                    sessionBoundSocket.sendMessage(message);
                    WS_LOGGER.debug("Sent {} message \"{}\" via Web Socket ({}) using path filter \"{}\" to user {} in context {}", info, GrizzlyWebSocketUtils.abbreviateMessageArg(message), sessionBoundSocket.getConnectionId(), pathFilter, I(userId), I(contextId));
                } catch (OXException e) {
                    WS_LOGGER.debug("Failed to send {} message to Web Socket: {}", info, sessionBoundSocket, e);
                }
            }
        }

        if (!any) {
            WS_LOGGER.debug("Found no matching local Web Socket to send {} message \"{}\" using path filter \"{}\" to user {} in context {}", info, GrizzlyWebSocketUtils.abbreviateMessageArg(message), pathFilter, I(userId), I(contextId));
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
        ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        return null == userSockets ? false : userSockets.containsKey(connectionId);
    }

    /**
     * Checks if there is any open Web Socket associated with specified user.
     *
     * @param pathFilter The path filter expression or <code>null</code>
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return <code>true</code> if any open Web Socket exists for given user; otherwise <code>false</code>
     */
    public boolean existsAny(String pathFilter, int userId, int contextId) {
        ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(UserAndContext.newInstance(userId, contextId));
        if (null == userSockets || userSockets.isEmpty()) {
            // No socket at all
            WS_LOGGER.debug("Found no local Web Sockets for user {} in context {}", I(userId), I(contextId));
            return false;
        }

        if (null == pathFilter) {
            // No filter given
            WS_LOGGER.debug("Found local Web Socket for user {} in context {}", I(userId), I(contextId));
            return true;
        }

        // Check if any satisfies given filter
        List<SessionBoundWebSocket> sockets = new ArrayList<>(userSockets.values());
        for (SessionBoundWebSocket sessionBoundSocket : sockets) {
            if (WebSockets.matches(pathFilter, sessionBoundSocket)) {
                WS_LOGGER.debug("Found local Web Socket for user {} in context {} matching filter \"{}\"", I(userId), I(contextId), pathFilter);
                return true;
            }
        }

        WS_LOGGER.debug("Found no local Web Socket for user {} in context {} matching filter \"{}\". Available Web Sockets: {}", I(userId), I(contextId), pathFilter, sockets);
        return false;
    }

    /**
     * Gets a listing of identifiers for sessions currently bound to an active Web Socket connection.
     *
     * @return The session identifier listing
     */
    public Map<String, List<SessionBoundWebSocket>> getActiveSessions() {
        Map<String, List<SessionBoundWebSocket>> sessions = new HashMap<>(32, 0.9F);
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            for (SessionBoundWebSocket socket : userSockets.values()) {
                String sessionId = socket.getSessionId();
                if (null != sessionId) {
                    List<SessionBoundWebSocket> sockets = sessions.get(sessionId);
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
            closeSocketSafe((SessionBoundWebSocket) socket);
        } else {
            remove(socket);
            socket.close();
        }
    }

    // ------------------------------------------------------ Methods from WebSocketApplication ------------------------------------------------------

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

            // Apply initial listeners
            WebSocketListener[] effectiveListeners;
            {
                List<WebSocketListener> listenersToUse = new LinkedList<>();
                if (null != listeners) {
                    for (WebSocketListener listener : listeners) {
                        if (null != listener) {
                            listenersToUse.add(listener);
                        }
                    }
                }
                for (WebSocketListener grizzlyWebSocketListener : listenerRegistry.getListeners()) {
                    if (grizzlyWebSocketListener instanceof IndividualWebSocketListenerAdapter) {
                        // Pass individual instance
                        listenersToUse.add(((IndividualWebSocketListenerAdapter) grizzlyWebSocketListener).newAdapter());
                    } else {
                        listenersToUse.add(grizzlyWebSocketListener);
                    }
                }
                effectiveListeners = listenersToUse.toArray(new WebSocketListener[listenersToUse.size()]);
            }

            // Create & return new session-bound Web Socket
            String path = requestPacket.getRequestURI();
            return new SessionBoundWebSocket(SessionInfo.newInstance(session), connectionId, path, parameters, handler, requestPacket, effectiveListeners);
        } catch (HandshakeException e) {
            // Handle Handshake error
            handleHandshakeException(e, handler, requestPacket);
            throw e;
        }
    }

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

    private boolean doCheckEnabled(Session session) {
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

    private void handleHandshakeException(HandshakeException e, ProtocolHandler handler, HttpRequestPacket requestPacket) {
        FilterChainContext ctx = handler.getFilterChainContext();
        HttpResponsePacket response = requestPacket.getResponse();
        response.setProtocol(Protocol.HTTP_1_1);
        response.setStatus(401);
        response.setReasonPhrase("Authorization Required");
        ctx.write(HttpContent.builder(response).build());
    }

    private GrizzlyWebSocketAuthenticator getAuthenticator() {
        GrizzlyWebSocketAuthenticator authenticator = AUTHENTICATOR_REFERENCE.get();
        return null == authenticator ? defaultAuthenticator : authenticator;
    }

    private Session checkSession(String sessionId, HttpRequestPacket requestPacket, Parameters parameters) {
        return getAuthenticator().checkSession(sessionId, requestPacket, parameters);
    }

    private static final int MAX_SIZE = 8;

    @Override
    public void onConnect(WebSocket socket) {
        // Override this method to take control over socket collection
        boolean error = true; // Pessimistic...
        try {
            if (socket instanceof SessionBoundWebSocket) {
                SessionBoundWebSocket sessionBoundSocket = (SessionBoundWebSocket) socket;

                UserAndContext userAndContext = UserAndContext.newInstance(sessionBoundSocket.getUserId(), sessionBoundSocket.getContextId());
                ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(userAndContext);
                if (userSockets == null) {
                    userSockets = new ConcurrentHashMap<>(MAX_SIZE, 0.9F, 1);
                    ConcurrentMap<ConnectionId, SessionBoundWebSocket> existing = openSockets.putIfAbsent(userAndContext, userSockets);
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

                synchronized (sessionBoundSocket) {
                    Collection<WebSocketListener> listeners = sessionBoundSocket.getListeners();
                    for (WebSocketListener grizzlyWebSocketListener : listenerRegistry.getListeners()) {
                        if (!listeners.contains(grizzlyWebSocketListener)) {
                            if (grizzlyWebSocketListener instanceof IndividualWebSocketListenerAdapter) {
                                // Pass individual instance
                                listeners.add(((IndividualWebSocketListenerAdapter) grizzlyWebSocketListener).newAdapter());
                            } else {
                                listeners.add(grizzlyWebSocketListener);
                            }
                        }
                    }
                }

                remoteDistributor.addWebSocket(sessionBoundSocket);

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

    @Override
    public void onClose(WebSocket socket, DataFrame frame) {
        // Override this method to take control over socket collection
        if (socket instanceof SessionBoundWebSocket) {
            SessionBoundWebSocket sessionBoundSocket = (SessionBoundWebSocket) socket;

            UserAndContext key = UserAndContext.newInstance(sessionBoundSocket.getUserId(), sessionBoundSocket.getContextId());
            ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets = openSockets.get(key);
            if (userSockets != null) {
                userSockets.remove(sessionBoundSocket.getConnectionId());
            }

            closeSocketSafe(sessionBoundSocket);
            LOGGER.debug("Closed Web Socket ({}) with path \"{}\" due to connection closure for user {} in context {}.", sessionBoundSocket.getConnectionId(), sessionBoundSocket.getPath(), I(sessionBoundSocket.getUserId()), I(sessionBoundSocket.getContextId()));
        } else {
            super.onClose(socket, frame);
        }
    }

    // ----------------------------------------------- Listener management -------------------------------------------------

    /**
     * Adds specified listener to existing Web Sockets
     *
     * @param grizzlyWebSocketListener The listener to add
     */
    public void addWebSocketListener(WebSocketListener grizzlyWebSocketListener) {
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
                synchronized (sessionBoundSocket) {
                    Collection<WebSocketListener> listeners = sessionBoundSocket.getListeners();
                    if (!listeners.contains(grizzlyWebSocketListener)) {
                        if (grizzlyWebSocketListener instanceof IndividualWebSocketListenerAdapter) {
                            // Pass individual instance
                            listeners.add(((IndividualWebSocketListenerAdapter) grizzlyWebSocketListener).newAdapter());
                        } else {
                            listeners.add(grizzlyWebSocketListener);
                        }
                    }
                }
            }
        }
    }

    /**
     * Removes specified listener to existing Web Sockets
     *
     * @param grizzlyWebSocketListener The listener to remove
     */
    public void removeWebSocketListener(WebSocketListener grizzlyWebSocketListener) {
        for (ConcurrentMap<ConnectionId, SessionBoundWebSocket> userSockets : openSockets.values()) {
            for (SessionBoundWebSocket sessionBoundSocket : userSockets.values()) {
                sessionBoundSocket.remove(grizzlyWebSocketListener);
            }
        }
    }

}
