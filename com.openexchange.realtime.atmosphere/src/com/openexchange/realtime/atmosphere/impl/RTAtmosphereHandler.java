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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.realtime.atmosphere.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.RealtimeExceptionCodes;
import com.openexchange.realtime.atmosphere.impl.stanza.builder.StanzaBuilderSelector;
import com.openexchange.realtime.atmosphere.impl.stanza.writer.StanzaWriter;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.realtime.atmosphere.stanza.StanzaBuilder;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.StanzaSender;
import com.openexchange.realtime.handle.StanzaQueueService;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.sessiond.SessionExceptionCodes;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link RTAtmosphereHandler} - Handler that gets associated with the {@link RTAtmosphereChannel} and does the main work of handling
 * incoming and outgoing Stanzas. Transformation of Stanzas is handed over to the proper OXRTHandler
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTAtmosphereHandler implements AtmosphereHandler, StanzaSender {

    private static final org.apache.commons.logging.Log LOG = Log.valueOf(LogFactory.getLog(RTAtmosphereHandler.class));

    private final ServiceLookup services;

    // Keep track of sessionID -> RTAtmosphereState to uniquely identify connected clients
    private final ConcurrentHashMap<String, RTAtmosphereState> sessionIdToState;

    // Keep track of user@context -> {context/user/resource1, context/user/resource2, ...} for canHandle/isConnected
    private final Map<String, Set<String>> userToBroadcasterIDs;

    /**
     * Initializes a new {@link RTAtmosphereHandler}.
     * 
     * @param library The library to use for OXRTHandler lookups needed for transformations of incoming and outgoing stanzas
     * @param services The service-lookup providing needed services
     */
    public RTAtmosphereHandler() {
        super();
        sessionIdToState = new ConcurrentHashMap<String, RTAtmosphereState>();
        userToBroadcasterIDs = new ConcurrentHashMap<String, Set<String>>();
        this.services = AtmosphereServiceRegistry.getInstance();
    }

    @Override
    public void destroy() {
        // Ignore for now
    }

    /*
     * The AtmosphereHandler.onRequest is invoked every time a new connection is made to an application. An application must take action and
     * decide what to do with the AtmosphereResource, e.g. suspend, resume or broadcast events. You can also write String or bytes back to
     * the client from that method.
     */
    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {
        // Log all events on the console, including WebSocket events for debugging
        resource.addEventListener(new WebSocketEventListenerAdapter());

        AtmosphereRequest request = resource.getRequest();
        AtmosphereResponse response = resource.getResponse();
        String method = request.getMethod();
        /*
         * GET requests can be handled via Continuations. Suspend the request and use it for bidirectional communication. "negotiating"
         * header is used to list all supported transports
         */
        if (method.equalsIgnoreCase("GET")) {
            if (request.getHeader("negotiating") == null) {
                try {
                    printBroadcasters("Broadcasters before GET Request");
                    printSessions("Sessions before GET Request");
                    // create state {id, session, atmoResource} and track ServerSession -> AtmosphereState
                    ServerSession serverSession = getServerSession(getSessionFromHeader(request), getSessionFromParameters(request));
                    RTAtmosphereState atmosphereState = trackState(resource, serverSession);

                    // Create new broadcaster for AtmosphereResource (if none already exists) Id will be formed like /context/user/resource
                    String broadcasterId = generateBroadcasterId(atmosphereState.id);
                    Broadcaster broadcaster = BroadcasterFactory.getDefault().lookup(broadcasterId, true);
                    atmosphereState.atmosphereResource.addEventListener(new AtmosphereResourceCleanupListener(
                        this,
                        atmosphereState,
                        broadcaster));
                    broadcaster.addAtmosphereResource(atmosphereState.atmosphereResource);

                    // keep track of connected users
                    trackConnectedUsers(atmosphereState.id.toGeneralForm().toString(), broadcasterId);

                    /*
                     * Register the atmosphere resource/connected user but don't overwrite an existing PresenceState. A GET is performed
                     * every X seconds idle time or after every POST when a client is connected via long-polling. So a POST to set the
                     * PresenceState would cause a GET that mustn't overwrite the PresenceState with a DefaultResource.
                     */
                    ResourceDirectory resourceDirectory = AtmosphereServiceRegistry.getInstance().getService(ResourceDirectory.class);
                    resourceDirectory.set(atmosphereState.id, new DefaultResource());

                    // finally suspend the resource
                    resource.suspend();
                    printBroadcasters("Broadcasters after GET Request");
                    printSessions("Sessions after GET Request");
                } catch (OXException e) {
                    // TODO:ExceptionHandling to connected clients
                    writeExceptionToResource(e, resource);
                    LOG.error(e.getMessage(), e);
                }
            } else {
                response.getWriter().write("OK");
            }
            /*
             * Use POST request to synchronously send data over the server.No need to track state, as we answer over the previously
             * suspended get request
             */
        } else if (method.equalsIgnoreCase("POST")) {
            printBroadcasters("Broadcasters before POST Request");
            printSessions("Sessions before POST Request");
            String postData = request.getReader().readLine();
            if (postData != null) {
                try {
                    // check for validity, don't allow unauthed clients to post
                    ServerSession serverSession = getServerSession(
                        getSessionFromHeader(request),
                        getSessionFromParameters(request),
                        getSessionFromPostData(postData));

                    RTAtmosphereState atmosphereState = sessionIdToState.get(serverSession.getSessionID());

                    if (atmosphereState == null) {
                        OXException ex = OXException.general("Couldn't find state for session:" + serverSession.getSessionID());
                        LOG.info("tracked session:" + sessionIdToState.keys());
                        throw ex;
                    }
                    JSONObject json = new JSONObject(postData);
                    StanzaBuilder<? extends Stanza> stanzaBuilder = StanzaBuilderSelector.getBuilder(atmosphereState.id, atmosphereState.session, json);
                    handleIncoming(stanzaBuilder.build(), atmosphereState);
                    printBroadcasters("Broadcasters after POST Request");
                    printSessions("Sessions after POST Request");
                } catch (JSONException jsonEx) {
                    LOG.error(jsonEx.getMessage(), jsonEx);
                    writeExceptionToResource(jsonEx, resource);
                } catch (IllegalArgumentException illEx) {
                    LOG.error(illEx.getMessage(), illEx);
                    writeExceptionToResource(illEx, resource);
                } catch (OXException e) {
                    LOG.error(e.getMessage(), e);
                    writeExceptionToResource(e, resource);
                }
            }
        }
    }

    private void printBroadcasters(String preString) {
        if (LOG.isDebugEnabled()) {
            List<Broadcaster> broadcasters = new ArrayList<Broadcaster>(BroadcasterFactory.getDefault().lookupAll());
            Collections.sort(broadcasters, new Comparator<Broadcaster>() {

                @Override
                public int compare(Broadcaster b1, Broadcaster b2) {
                    return b1.getID().compareTo(b2.getID());
                }
            });
            StringBuilder sb = new StringBuilder();
            sb.append("\n--------------------------------------------------------------------------------\n");
            sb.append(preString);
            sb.append(":\n");
            for (Broadcaster broadcaster : broadcasters) {
                sb.append(broadcaster.getID()).append(":");
                Collection<AtmosphereResource> atmosphereResources = broadcaster.getAtmosphereResources();
                sb.append("\tRessources: " + atmosphereResources.size()).append('\n');
                for (AtmosphereResource atmosphereResource : atmosphereResources) {
                    sb.append("\t\t").append(atmosphereResource.uuid()).append('\n');
                }
            }
            sb.append("--------------------------------------------------------------------------------\n");
            LOG.debug(sb.toString());
        }
    }

    private void printSessions(String preString) {
        if (LOG.isDebugEnabled()) {
            StringBuilder sb = new StringBuilder();
            sb.append("\n--------------------------------------------------------------------------------\n");
            sb.append(preString);
            sb.append(":\n");
            Set<Entry<String, RTAtmosphereState>> sessionSet = sessionIdToState.entrySet();
            sb.append("\tSessions: " + sessionSet.size()).append('\n');
            for (Entry<String, RTAtmosphereState> entry : sessionSet) {
                String session = entry.getKey();
                RTAtmosphereState resource = entry.getValue();
                sb.append("\t\t");
                sb.append("Session: ").append(session);
                sb.append(" <-> ");
                sb.append("Resource: ").append(resource.atmosphereResource.uuid());
                sb.append('\n');
            }
            sb.append("--------------------------------------------------------------------------------\n");
            LOG.debug(sb.toString());
        }
    }

    /**
     * Keep track of connected users by mapping user@context to /context/user/resource
     * 
     * @param generalId id in form of user@context
     * @param broadcasterId one of the several possible broadcasterIds in the form of /context/user/resource
     */
    private void trackConnectedUsers(final String generalId, final String broadcasterId) {
        if (userToBroadcasterIDs.containsKey(generalId)) {
            userToBroadcasterIDs.get(generalId).add(broadcasterId);
        } else {
            Set<String> broadcasterIds = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
            broadcasterIds.add(broadcasterId);
            userToBroadcasterIDs.put(generalId, broadcasterIds);
        }
    }

    /**
     * CleanUp callback used by AtmosphereResourceCleanupListener instances.
     */
    public void onDisconnect(RTAtmosphereState atmosphereState) {
        ResourceDirectory resourceDirectory = AtmosphereServiceRegistry.getInstance().getService(ResourceDirectory.class);
        try {
            resourceDirectory.remove(atmosphereState.id);
        } catch (OXException e) {
            LOG.error("Could not unregister resource " + atmosphereState.id.toString() + ".", e);
        }

        String generalId = atmosphereState.id.toGeneralForm().toString();
        String broadcasterId = generateBroadcasterId(atmosphereState.id);
        Set<String> broadcasterIds = userToBroadcasterIDs.get(generalId);
        if (broadcasterIds != null) {
            broadcasterIds.remove(broadcasterId);
        }
        LOG.debug("\nRemoving sessionId: "+atmosphereState.session.getSessionID()+"\n");
        sessionIdToState.remove(atmosphereState.session.getSessionID());
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
//        AtmosphereResource resource = event.getResource();
//        AtmosphereResponse response = resource.getResponse();
//        if (event.isSuspended()) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("\n\nonStateChange: Writing message: " + event.getMessage().toString() + " to resource: " + resource.uuid() + "\n\n");
//            }
//            response.getWriter().write(event.getMessage().toString());
//            switch (resource.transport()) {
//            case JSONP:
//            case AJAX:
//            case LONG_POLLING:
//                event.getResource().resume();
//                break;
//            default:
//                response.getWriter().flush();
//                break;
//            }
//        } else if (!event.isResuming()) {
//            if (LOG.isDebugEnabled()) {
//                LOG.debug("Event wasn't resuming remote connection got closed by proxy or browser: \n" + "BroadcasterId: " + event.broadcaster().getID() + "\n" + "Resource: " + event.getResource().uuid() + "\n");
//            }
//        }
    }

    /**
     * Write a simple JSON error to the client.
     * 
     * @param exception the exception to send.
     * @param resource the resource representing the client
     * @throws IOException
     */
    private void writeExceptionToResource(Exception exception, AtmosphereResource resource) throws IOException {
        try {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("error", exception.toString());
            jsonObject.write(resource.getResponse().getWriter());
        } catch (JSONException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /**
     * Convert the session info into a ServerSession Object.
     * 
     * @param sessionInfo The sessionInfo to convert
     * @return The ServerSession objectmatching the session infos
     * @throws IllegalArgumentException if the sessionInfo is null or empty
     * @throws OXException if an error happens while trying to build the ServerSession from the session infos
     */
    private ServerSession getServerSessionFromInfo(String sessionInfo) throws OXException {
        if (sessionInfo == null || sessionInfo.isEmpty()) {
            throw new IllegalArgumentException("Invalid parameter: sessionInfo");
        }

        SessiondService sessiondService = services.getService(SessiondService.class);
        if (sessiondService == null) {
            throw OXExceptionFactory.getInstance().create(ServiceExceptionCode.SERVICE_UNAVAILABLE, SessiondService.class);
        }
        Session session = sessiondService.getSession(sessionInfo);
        if (session == null) {
            throw OXExceptionFactory.getInstance().create(SessionExceptionCodes.SESSION_EXPIRED, sessionInfo);
        }
        return ServerSessionAdapter.valueOf(session);
    }

    /**
     * Inspect the request headers for session information and return it.
     * 
     * @param request the request to inspect
     * @return null if no session info can be found, the session info otherwise
     */
    private String getSessionFromHeader(AtmosphereRequest request) {
        return request.getHeader("session");
    }

    /**
     * Inspect the request parameters for session information and return it.
     * 
     * @param request the request to inspect
     * @return null if no session info can be found, the session info otherwise
     */
    private String getSessionFromParameters(AtmosphereRequest request) {
        return request.getParameter("session");
    }

    /**
     * Inspect the request's post data for session information and return it.
     * 
     * @param postData the JSON String to inspect
     * @return null if no session info can be found, the session info otherwise
     * @throws OXException if the postData isn't valid JSON
     */
    private String getSessionFromPostData(String postData) throws OXException {
        JSONObject requestData;
        String sessionInfo = null;
        try {
            requestData = new JSONObject(postData);
            sessionInfo = requestData.optString("session");
        } catch (JSONException e) {
            throw OXExceptionFactory.getInstance().create(SessionExceptionCodes.SESSION_PARAMETER_MISSING);
        }
        return sessionInfo;
    }

    /**
     * Get a ServerSession object from a list of session infos submitted in the request. Fails on the first non-null sessionInfo parameter
     * that is invalid.
     * 
     * @param sessionInfo a list of session infos, nulls are allowed
     * @return The Serversession that matches the first given session info
     * @throws OXException if no matching ServerSession can be found
     */
    private ServerSession getServerSession(String... sessionInfo) throws OXException {
        ServerSession serverSession = null;
        for (int i = 0; serverSession == null && i < sessionInfo.length; i++) {
            String currentSessionInfo = sessionInfo[i];
            if (currentSessionInfo != null && !currentSessionInfo.isEmpty()) {
                serverSession = getServerSessionFromInfo(sessionInfo[i]);
            }
        }
        return serverSession;
    }

    /**
     * Check if an entity is connected to the associated Channel.
     * 
     * @param id the ID of the entity you are looking for.
     * @return true if the entity is connected via one or more resources, false otherwise
     */
    public boolean isConnected(ID id) {
        String generalForm = id.toGeneralForm().toString();
        return userToBroadcasterIDs.containsKey(generalForm);
    }

    @Override
    public void send(Stanza stanza, ID recipient) throws OXException {
        StanzaWriter stanzaWriter = new StanzaWriter();
        String stanzaAsJSON = stanzaWriter.write(stanza).toString();
        String broadcasterId = generateBroadcasterId(recipient);
        
        /*
         * Broadcast stanza to all entities matching the user@context by using a wildcard for the resource: /user@context/*
         */
        Broadcaster broadcaster = BroadcasterFactory.getDefault().lookup(broadcasterId);
        if (broadcaster == null) {
            handleResourceNotAvailable();
        }
        
        Collection<AtmosphereResource> resources = broadcaster.getAtmosphereResources();
        if (resources == null || resources.isEmpty()) {
            handleResourceNotAvailable();
        }
        
        for (AtmosphereResource resource : resources) {
            if (resource.isCancelled() || resource.getResponse().isCommitted()) {
                handleResourceNotAvailable();
            }

            PrintWriter writer;
            try {
                writer = resource.getResponse().getWriter();
                writer.print(stanzaAsJSON);
                if (writer.checkError()) {
                    handleResourceNotAvailable();
                }
                switch (resource.transport()) {
                case JSONP:
                case AJAX:
                case LONG_POLLING:
                    resource.resume();
                    break;
                default:
                    break;
                }
            } catch (IOException e) {
                handleResourceNotAvailable();
            }
        }
    }
    
    private void handleResourceNotAvailable() throws OXException {
        throw RealtimeExceptionCodes.RESOURCE_NOT_AVAILABLE.create();
    }

    /**
     * Keep track of the sessionID -> RTAtmosphereState association. Checks the AtmosphereResource for the session header/parameter and adds
     * it to the sessionIdToState map that tracks Serversession <-> RTAtmosphereState
     * 
     * @param atmosphereResource the AtmosphereResource
     * @return An RTAtmosphereState that assembles the AtmosphereResource, Serversession and ID
     * @throws OXException if the server session is missing from the AtmosphereResource
     */
    private RTAtmosphereState trackState(AtmosphereResource atmosphereResource, ServerSession serverSession) throws OXException {
        RTAtmosphereState state = new RTAtmosphereState();
        state.session = serverSession;
        state.atmosphereResource = atmosphereResource;
        state.id = constructId(atmosphereResource, serverSession);
        LOG.debug("\nTracking sessionId: "+serverSession.getSessionID()+"\n");
        sessionIdToState.put(serverSession.getSessionID(), state);
        return state;
    }

    /**
     * Build an unique {@link ID} <code>{"ox", userLogin, context, resource}</code> from the infos given by the AtmosphereResource and
     * ServerSession.
     * 
     * @param atmosphereResource the current AtmosphereResource
     * @param serverSession the associated serverSession
     * @return the constructed unique ID
     */
    private ID constructId(AtmosphereResource atmosphereResource, ServerSession serverSession) {
        String userLogin = serverSession.getUserlogin();
        String contextName = getContextName(serverSession.getLogin());

        AtmosphereRequest request = atmosphereResource.getRequest();
        String resource = request.getHeader("resource");
        if (resource == null) {
            resource = request.getParameter("resource");
        }
        /*
         * TODO: think about proper unique resources later. Maybe add sessionID to ID for now we use the resource+sessionID or only
         * sessionID if no resource is given
         */
        if (resource == null) {
            resource = serverSession.getSessionID();
        } else {
            resource = resource + serverSession.getSessionID();
        }
        return new ID(RTAtmosphereChannel.PROTOCOL, null, userLogin, contextName, resource);
    }

    /**
     * Get context string from login string
     * 
     * @param login the login string
     * @return an empty string if no context can be found, the context oterwise
     */
    private String getContextName(String login) {
        int index = login.indexOf('@');
        if (index < 0) {
            return "";
        }
        return login.substring(index + 1);
    }

    /**
     * Handle incoming Stanza and decide if they have an internal namespace and need to be handled by the Channel/Handler or if they should
     * be dispatched iow. transformed to POJOs and handed over to the MessageDispatcher.
     * 
     * @param stanza The Stanza to handle
     * @param atmosphereState The associated state
     * @throws OXException
     */
    protected <T extends Stanza> void handleIncoming(T stanza, RTAtmosphereState atmosphereState) throws OXException {
        //Transform payloads
        //Initialize default fields after tranforming
        stanza.transformPayloadsToInternal();
        stanza.initializeDefaults();

        StanzaQueueService stanzaQueueService = AtmosphereServiceRegistry.getInstance().getService(StanzaQueueService.class);
        if (!stanzaQueueService.enqueueStanza(stanza)) {
            // TODO: exception?
        }
    }

    /**
     * Create a broadcaster id from a given {@link ID}. Creates broadcaster ids like : /context/user or /context/user/resource depending if
     * the given ID contains a resource. This way we can address: - single clients of a user identified by resource: /context/user/resource
     * - all clients of a user: /context/user/* - all clients of all users in a context: /context/* - all clients in all contexts: /*
     * 
     * @param id the id to use for generating the broadcaster id
     * @return the generated broadcaster id
     */
    private String generateBroadcasterId(ID id) {
        StringBuilder sb = new StringBuilder();
        sb.append("/").append(id.getContext()).append("/").append(id.getUser());
        if (id.getResource() != null) {
            sb.append("/").append(id.getResource());
        }
        return sb.toString();
    }

}
