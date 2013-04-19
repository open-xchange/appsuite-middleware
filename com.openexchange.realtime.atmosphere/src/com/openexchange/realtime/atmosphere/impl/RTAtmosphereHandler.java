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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.servlet.http.HttpServletResponse;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereRequest;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereResponse;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
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
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.realtime.util.StanzaSequenceGate;
import com.openexchange.tools.session.ServerSession;

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

    private final AtmosphereServiceRegistry atmosphereServiceRegistry;

    /*
     * Map general ids (user@context) to full ids (ox://user@context/resource.browserx.taby, this is used for lookups via isConnected
     */
    private final IDMap<Set<ID>> generalToConcreteIDMap;

    /*
     * Map full client IDs to the AtmosphereResource that represents their connection to the server, this is used for sending
     */
    private final IDMap<AtmosphereResource> concreteIDToResourceMap;

    /*
     * Map for holding outboxes
     */
    private final ConcurrentHashMap<ID, List<Stanza>> outboxes;

    /*
     * Give resources time to linger before finally cleaning up
     */
    AtmosphereResourceReaper atmosphereResourceReaper = new AtmosphereResourceReaper();

    /*
     * Maintain a mapping of all IDs that use a certain session
     */
    private ConcurrentHashMap<String, Set<ID>> idsPerSession = new ConcurrentHashMap<String, Set<ID>>();

    private StanzaSequenceGate gate = new StanzaSequenceGate("RTAtmosphereHandler") {

        @Override
        public void handleInternal(Stanza stanza, ID recipient) throws OXException {
            handleIncoming(stanza);
        }
    };

    /**
     * Initializes a new {@link RTAtmosphereHandler}.
     */
    public RTAtmosphereHandler() {
        super();
        generalToConcreteIDMap = new IDMap<Set<ID>>();
        concreteIDToResourceMap = new IDMap<AtmosphereResource>();
        outboxes = new ConcurrentHashMap<ID, List<Stanza>>();
        this.atmosphereServiceRegistry = AtmosphereServiceRegistry.getInstance();
    }

    @Override
    public void onRequest(AtmosphereResource resource) throws IOException {
        // Log all events on the console, including WebSocket events for debugging
        if (LOG.isDebugEnabled()) {
            resource.addEventListener(new WebSocketEventListenerAdapter());
        }

        AtmosphereRequest request = resource.getRequest();
        AtmosphereResponse response = resource.getResponse();
        response.setCharacterEncoding("UTF-8");
        String method = request.getMethod();
        SessionValidator sessionValidator = new SessionValidator(resource);
        try {
            ServerSession serverSession = sessionValidator.getServerSession();
            // TODO: respect unique id sent by client as param/header when constructing the ID
            ID constructedId = constructId(resource, serverSession);

            if (method.equalsIgnoreCase("GET")) {
                /*
                 * GET requests can be handled via Continuations. Suspend the request and use it for bidirectional communication.
                 * "negotiating" header is used to list all supported transports. Full client ID <-> AtmosphereResource are tracked by this
                 * Handler for this cluster node. Cluster wide tracking of connected users is done via the ResourceDirectory service. An
                 * EventListener takes care of cleaning up the user tracking when we recognize a disconnect.
                 */
                if (request.getHeader("negotiating") == null) {
                    // keep track of clients connected via get that are waiting for data
                    trackConnectedUser(constructedId, resource, serverSession);
                    // and add EventListener that takes care of cleaning up the tracking resources once the client disconnects again
                    resource.addEventListener(new AtmosphereResourceCleanupListener(
                        resource,
                        constructedId,
                        generalToConcreteIDMap,
                        concreteIDToResourceMap,
                        outboxes,
                        atmosphereResourceReaper));
                    // finally suspend the resource until data is available for the clients and resource gets resumed after send
                    drainOutbox(constructedId);
                } else {
                    response.getWriter().write("OK");
                }
            } else if (method.equalsIgnoreCase("POST")) {
                /*
                 * Use a POST request to synchronously send data over the server. No need to track state, as we answer over suspended get
                 * requests
                 */
                String postData = request.getReader().readLine();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Incoming: " + postData);
                }
                if (postData != null) {
                    List<JSONObject> stanzas = new LinkedList<JSONObject>();
                    if (postData.startsWith("[")) {
                        JSONArray arr = new JSONArray(postData);
                        for (int i = 0, size = arr.length(); i < size; i++) {
                            stanzas.add(arr.getJSONObject(i));
                        }
                    } else {
                        stanzas.add(new JSONObject(postData));
                    }
                    for (JSONObject json : stanzas) {
                        if (json.has("type") && "ping".equalsIgnoreCase(json.optString("type"))) {
                            // ignore
                            return;
                        }
                        StanzaBuilder<? extends Stanza> stanzaBuilder = StanzaBuilderSelector.getBuilder(
                            constructedId,
                            sessionValidator.getServerSession(),
                            json);
                        Stanza stanza = stanzaBuilder.build();
                        if (stanza.traceEnabled()) {
                            stanza.trace("received in atmosphere handler");
                        }
                        if (stanza.getSequenceNumber() != -1) {
                            // Return receipt
                            stanza.trace("Send return receipt for sequence number: " + stanza.getSequenceNumber());
                            Message msg = new Message();
                            msg.setTo(stanza.getFrom());
                            msg.setFrom(stanza.getFrom());
                            msg.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
                                stanza.getSequenceNumber(),
                                "json",
                                "atmosphere",
                                "received").build()));
                            send(msg, msg.getTo());
                        }
                        gate.handle(stanza, stanza.getTo());
                    }
                }
            }
        } catch (OXException e) {
            LOG.error(e.getMessage(), e);
            writeExceptionToResource(e, resource);
            try {
                if (e.getErrorCode().equals("SES-0203")) {
                    Set<ID> ids = idsPerSession.remove(sessionValidator.getSessionId());
                    if (ids != null) {
                        for (ID id : ids) {
                            AtmosphereResource atmosphereResource = concreteIDToResourceMap.get(id);
                            try {
                                writeExceptionToResource(e, atmosphereResource);
                                atmosphereResource.getResponse().getWriter().flush();
                            } catch (Throwable t) {
                                // Give up
                            }
                        }
                    }
                }
            } catch (OXException oxe) {
                // Giving Up
                LOG.error(oxe.getMessage(), oxe);
            }
        } catch (Exception e) {
            // TODO:ExceptionHandling to connected clients
            writeExceptionToResource(e, resource);
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Tracks a client via its concrete ID and the associated AtmosphereResource.
     * <ol>
     * <li>Adds the concreteID to the generalID -> concreteID map
     * <li>Adds an entry to the concreteID -> AtmosphereResource map
     * <li>Registers the concreteID to the ResourceDirectory service
     * </ol>
     * 
     * @param concreteID The concrete ID of the connected client
     * @param atmosphereResource The resource of the connected client
     * @param serverSession
     * @throws OXException
     */
    private void trackConnectedUser(ID concreteID, AtmosphereResource atmosphereResource, final ServerSession session) throws OXException {
        /* if the id was marked for removal via the reaper try to remove it from the reaper */
        atmosphereResourceReaper.remove(concreteID);

        // Adds the concreteID to the generalID -> concreteID map
        ID generalID = concreteID.toGeneralForm();
        if (generalToConcreteIDMap.containsKey(generalID)) {
            Set<ID> fullIDSet = generalToConcreteIDMap.get(generalID);
            if (fullIDSet == null) {
                fullIDSet = new HashSet<ID>();
                generalToConcreteIDMap.put(generalID, fullIDSet);
            }
            fullIDSet.add(concreteID);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Added to generalID -> concreteIDMap: " + generalID + " -> " + concreteID);
            }
        } else {
            Set<ID> concreteIDSet = new HashSet<ID>();
            concreteIDSet.add(concreteID);
            generalToConcreteIDMap.put(generalID, concreteIDSet);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Added to generalID -> concreteIDMap: " + generalID + " -> " + concreteID);
            }
        }

        // Adds an entry to the concreteID -> AtmosphereResource map
        concreteIDToResourceMap.put(concreteID, atmosphereResource);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Added to concreteIDMap -> atmosphereResourceMap: " + concreteID + " -> " + atmosphereResource.uuid());
        }

        final Set<ID> idSet = com.openexchange.tools.Collections.opt(
            idsPerSession,
            session.getSessionID(),
            Collections.synchronizedSet(new HashSet<ID>()));
        if (idSet.add(concreteID)) {
            concreteID.on("dispose", new IDEventHandler() {

                @Override
                public void handle(String event, ID id, Object source, Map<String, Object> properties) {
                    idSet.remove(id);
                    if (idSet.isEmpty()) {
                        idsPerSession.remove(session.getSessionID());
                    }
                }
            });
        }

        // Register the concreteID in the ResourceDirectory
        ResourceDirectory resourceDirectory = atmosphereServiceRegistry.getService(ResourceDirectory.class);
        if (resourceDirectory == null) {
            throw RealtimeExceptionCodes.NEEDED_SERVICE_MISSING.create(ResourceDirectory.class);
        }
        resourceDirectory.set(concreteID, new DefaultResource());
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
     * Check if an entity is connected to the associated Channel by checking if we are tracking the generalID of the entity and additionally
     * if we have at least one connected resource for this client.
     * 
     * @param id the ID of the entity you are looking for.
     * @return true if the entity is connected via one or more resources, false otherwise
     */
    public boolean isConnected(ID id) {
        if (id == null) {
            throw new IllegalArgumentException("Missing obligatory parameter: id");
        }

        // Check for general availability
        if (id.isGeneralForm()) {
            Set<ID> fullClientIDs = generalToConcreteIDMap.get(id);
            return fullClientIDs == null ? false : !fullClientIDs.isEmpty();
        }

        // Check if the specific client is connected
        Set<ID> fullClientIDs = generalToConcreteIDMap.get(id.toGeneralForm());
        if (fullClientIDs == null || fullClientIDs.isEmpty()) {
            return false;
        }
        if (!fullClientIDs.contains(id)) {
            return false;
        }
        if (concreteIDToResourceMap.containsKey(id)) {
            return true;
        }
        return false;
    }

    /**
     * Handle incoming Stanza and decide if they have an internal namespace and need to be handled by the Channel/Handler or if they should
     * be dispatched iow. transformed to POJOs and handed over to the MessageDispatcher.
     * 
     * @param stanza The Stanza to handle
     * @param atmosphereState The associated state
     * @throws OXException
     */
    protected <T extends Stanza> void handleIncoming(T stanza) throws OXException {
        // Transform payloads
        // Initialize default fields after tranforming
        stanza.transformPayloadsToInternal();
        stanza.initializeDefaults();

        StanzaQueueService stanzaQueueService = atmosphereServiceRegistry.getService(StanzaQueueService.class);
        
        if (!stanzaQueueService.enqueueStanza(stanza)) {
            // TODO: exception?
            LOG.error("Couldn't enqueue Stanza: " + stanza);
        }
    }

    @Override
    public void send(Stanza stanza, ID recipient) throws OXException {
        try {
            recipient.lock("rt-atmosphere-outbox");
            stanza.trace("Enqueing stanza in atmosphere outbox for ID: " + recipient );
            outboxFor(recipient).add(stanza);
            drainOutbox(recipient);
        } finally {
            recipient.unlock("rt-atmosphere-outbox");
        }

    }

    private List<Stanza> outboxFor(ID id) {
        List<Stanza> outbox = outboxes.get(id);
        if (outbox == null) {
            outbox = Collections.synchronizedList(new LinkedList<Stanza>());
            List<Stanza> activeOutbox = outboxes.putIfAbsent(id, outbox);
            return (activeOutbox != null) ? activeOutbox : outbox;
        }
        return outbox;
    }

    private void drainOutbox(ID id) throws OXException {
        drainOutbox(id, 0);
    }

    private void drainOutbox(ID id, int count) throws OXException {
        List<Stanza> outbox = null;
        try {
            id.lock("rt-atmosphere-outbox");
            AtmosphereResource atmosphereResource = concreteIDToResourceMap.get(id);
            boolean failed = false;
            boolean sent = false;

            outbox = outboxes.remove(id);
            if (outbox != null && !outbox.isEmpty()) {
                JSONArray array = new JSONArray();
                StanzaWriter stanzaWriter = new StanzaWriter();
                for (Stanza stanza : outbox) {
                    stanza.trace("Drained from outbox");
                    array.put(stanzaWriter.write(stanza));
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Trying to send: " + array.length() + " stanzas: " + array);
                }

                if (atmosphereResource == null || atmosphereResource.isCancelled() || atmosphereResource.getResponse().isCommitted()) {
                    // Enqueue again and try later
                    outboxFor(id).addAll(outbox);
                    outbox = null;
                    failed = true;
                }

                if (!failed) {
                    PrintWriter writer;
                    try {
                        writer = atmosphereResource.getResponse().getWriter();
                        writer.print(array);
                        if (LOG.isTraceEnabled()) {
                            LOG.trace("Outgoing: " + array);
                        }
                        if (writer.checkError()) {
                            outboxFor(id).addAll(outbox);
                            failed = true;
                        } else {
                            sent = true;
                        }

                    } catch (IOException e) {
                        // Enqueue again and try later
                        outboxFor(id).addAll(outbox);
                        failed = true;
                    }
                    outbox = null;
                }
            }

            if (sent) {
                switch (atmosphereResource.transport()) {
                case JSONP:
                case AJAX:
                case LONG_POLLING:
                    atmosphereResource.getResponse().getWriter().flush();
                    break;
                default:
                    break;
                }
            } else {
                switch (atmosphereResource.transport()) {
                case JSONP:
                case AJAX:
                case LONG_POLLING:
                    if (!atmosphereResource.getResponse().isCommitted()) {
                        atmosphereResource.suspend();
                    }
                    break;
                default:
                    break;
                }
            }
        } catch (OXException x) {
            throw x;
        } catch (Throwable t) {
            if (LOG.isTraceEnabled()) {
                LOG.trace(t.getMessage(), t);
            }
            if (outbox != null) {
                outboxFor(id).addAll(outbox);
            }
        } finally {
            id.unlock("rt-atmosphere-outbox");
        }

    }

    private void handleResourceNotAvailable() throws OXException {
        throw RealtimeExceptionCodes.RESOURCE_NOT_AVAILABLE.create();
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
            return "defaultcontext";
        }
        return login.substring(index + 1);
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        // Handled via send() or ResourceCleanupListener
    }

    @Override
    public void destroy() {
        // Ignore for now
    }

}
