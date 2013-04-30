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
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
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
import com.openexchange.realtime.util.SequenceNumberComparator;
import com.openexchange.realtime.util.StanzaSequenceGate;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
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
    private final ConcurrentHashMap<ID, List<EnqueuedStanza>> outboxes;

    /*
     * Give resources time to linger before finally cleaning up
     */
    AtmosphereResourceReaper atmosphereResourceReaper = null;

    /*
     * Maintain a mapping of all IDs that use a certain session
     */
    private ConcurrentHashMap<String, Set<ID>> idsPerSession = new ConcurrentHashMap<String, Set<ID>>();
    
    private ConcurrentHashMap<ID, Long> sequenceNumbers = new ConcurrentHashMap<ID, Long>();
    
    private ConcurrentHashMap<ID, SortedSet<EnqueuedStanza>> resendBuffers = new ConcurrentHashMap<ID, SortedSet<EnqueuedStanza>>();

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
        outboxes = new ConcurrentHashMap<ID, List<EnqueuedStanza>>();
        this.atmosphereServiceRegistry = AtmosphereServiceRegistry.getInstance();
        atmosphereResourceReaper = new AtmosphereResourceReaper();
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
            refreshReaper(constructedId);

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
                    Exception exception = null;
                    for (JSONObject json : stanzas) {
                        try {
                            if (json.has("type")) {
                                // ignore
                                String type = json.optString("type");
                                if (type.equals("ping")) {
                                    return;
                                }
                                
                                if (type.equals("ack")) {
                                    
                                    SortedSet<EnqueuedStanza> resendBuffer = resendBufferFor(constructedId);
                                    EnqueuedStanza found = null;
                                    long seq = json.optLong("seq");
                                    
                                    for (EnqueuedStanza enqueuedStanza : new LinkedList<EnqueuedStanza>(resendBuffer)) {
                                        if (enqueuedStanza.sequenceNumber == seq ) {
                                            found = enqueuedStanza;
                                            break;
                                        }
                                    }
                                    if (found != null) {
                                        resendBuffer.remove(found);
                                        found.stanza.trace("Got ack from client");
                                    }
                                }
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
                                final Message msg = new Message();
                                msg.setTo(stanza.getFrom());
                                msg.setFrom(stanza.getFrom());
                                msg.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
                                    stanza.getSequenceNumber(),
                                    "json",
                                    "atmosphere",
                                    "received").build()));
                                atmosphereServiceRegistry.getService(ThreadPoolService.class).submit(new Task<Object>() {
    
                                    @Override
                                    public void setThreadName(ThreadRenamer threadRenamer) {
                                        threadRenamer.rename("Acknowledgement Sender");
                                    }
    
                                    @Override
                                    public void beforeExecute(Thread t) {
                                        
                                    }
    
                                    @Override
                                    public void afterExecute(Throwable t) {
                                        
                                    }
    
                                    @Override
                                    public Object call() throws Exception {
                                        send(msg, msg.getTo());                                    
                                        return null;
                                    }
                                    
                                }); 
                            }
                            gate.handle(stanza, stanza.getTo());
                        } catch (Exception t) {
                            if (exception == null) {
                                exception = t;                                
                            }
                            LOG.error(t.getMessage(),t);
                        }
                        
                    }
                    if (exception != null) {
                        throw exception;
                    }
                }
            }
        } catch (OXException e) {
            writeExceptionToResource(e, resource);
            try {
                if (e.getErrorCode().equals("SES-0203")) {
                    Set<ID> ids = idsPerSession.remove(sessionValidator.getSessionId());
                    if (ids != null) {
                        for (ID id : ids) {
                            AtmosphereResource atmosphereResource = concreteIDToResourceMap.get(id);
                            try {
                                writeExceptionToResource(e, atmosphereResource);
                                atmosphereResource.resume();
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
            LOG.error(e.getMessage(), e);
            writeExceptionToResource(e, resource);
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
    private void trackConnectedUser(final ID concreteID, AtmosphereResource atmosphereResource, final ServerSession session) throws OXException {
        /* if the id was marked for removal via the reaper try to remove it from the reaper */
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


    private void refreshReaper(final ID concreteID) {
        atmosphereResourceReaper.remove(concreteID);
        atmosphereResourceReaper.add(new Moribund(concreteID) {

            @Override
            public void die() throws OXException {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Disconnecting: " + this);
                }

                // remove concreteID from general -> concreteID mapping
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing from generalID -> conreteID map: " + this);
                }
                ID generalID = concreteID.toGeneralForm();
                Set<ID> fullIDs = generalToConcreteIDMap.get(generalID);
                if (fullIDs != null) {
                    fullIDs.remove(concreteID);
                    if (fullIDs.isEmpty()) {
                        generalToConcreteIDMap.remove(generalID);
                    }
                }

                // remove concreteID -> Resource mapping
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Removing from concreteID -> resource map: " + this);
                }
                concreteIDToResourceMap.remove(concreteID);

                // clear outboxes
                outboxes.remove(concreteID);
                resendBuffers.remove(concreteID);
                sequenceNumbers.remove(concreteID);
                
                // remove concreteID from cluster wide resourceDirectory
                ResourceDirectory resourceDirectory = AtmosphereServiceRegistry.getInstance().getService(ResourceDirectory.class);
                try {
                    resourceDirectory.remove(concreteID);
                } catch (OXException e) {
                    LOG.error("Could not unregister resource with ID: " + concreteID, e);
                }                
            }
            
        });        
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
            if (stanza.getSequenceNumber() != -1) {
                stamp(stanza, recipient);
                resendBufferFor(recipient).add(new EnqueuedStanza(stanza));
            } else {
                outboxFor(recipient).add(new EnqueuedStanza(stanza));
            }
            drainOutbox(recipient);
        } finally {
            recipient.unlock("rt-atmosphere-outbox");
        }

    }

    private void stamp(Stanza stanza, ID recipient) {
        try {
            recipient.lock("rt-atmosphere-sequence");
            stanza.setSequenceNumber(com.openexchange.tools.Collections.opt(sequenceNumbers, recipient, Long.valueOf(0)));
            stanza.trace("Stamped outgoing stanza with sequence number " + stanza.getSequenceNumber());
            sequenceNumbers.put(recipient, stanza.getSequenceNumber()+1);
        } finally {
            recipient.unlock("rt-atmosphere-sequence");
        }
        
    }
    
    private SortedSet<EnqueuedStanza> resendBufferFor(ID id) {
        SortedSet<EnqueuedStanza> resendBuffer = resendBuffers.get(id);
        if (resendBuffer == null) {
            resendBuffer = new TreeSet<EnqueuedStanza>();
            SortedSet<EnqueuedStanza> activeBuffer = resendBuffers.putIfAbsent(id, resendBuffer);
            return (activeBuffer != null) ? activeBuffer : resendBuffer;
        }
        return resendBuffer;
    }

    private List<EnqueuedStanza> outboxFor(ID id) {
        List<EnqueuedStanza> outbox = outboxes.get(id);
        if (outbox == null) {
            outbox = Collections.synchronizedList(new LinkedList<EnqueuedStanza>());
            List<EnqueuedStanza> activeOutbox = outboxes.putIfAbsent(id, outbox);
            return (activeOutbox != null) ? activeOutbox : outbox;
        }
        return outbox;
    }


    private void drainOutbox(ID id) throws OXException {
        List<EnqueuedStanza> outbox = null;
        List<Stanza> stanzasToSend = new LinkedList<Stanza>();
        try {
            id.lock("rt-atmosphere-outbox");
            AtmosphereResource atmosphereResource = concreteIDToResourceMap.get(id);
            boolean failed = false;
            boolean sent = false;

            outbox = outboxes.remove(id);
            SortedSet<EnqueuedStanza> resendBuffer = resendBuffers.get(id);
            if ((outbox != null && !outbox.isEmpty()) || (resendBuffer != null && !resendBuffer.isEmpty())) {
                if (outbox == null) {
                    outbox = Collections.emptyList();
                }
                JSONArray array = new JSONArray();
                StanzaWriter stanzaWriter = new StanzaWriter();
                if (resendBuffer != null) {
                    List<EnqueuedStanza> toRemove = new LinkedList<EnqueuedStanza>();
                    for(EnqueuedStanza stanza: new LinkedList<EnqueuedStanza>(resendBuffer)) {
                        if (stanza.incCounter()) {
                            stanza.stanza.trace("Drained from resendBuffer");
                            array.put(stanzaWriter.write(stanza.stanza));
                            stanzasToSend.add(stanza.stanza);
                        } else {
                            toRemove.add(stanza);
                            stanza.stanza.trace("Counted to infinity. Stanza will be lost");
                        }
                    }
                    resendBuffer.removeAll(toRemove);
                }
                List<EnqueuedStanza> cleanedOutbox = new LinkedList<EnqueuedStanza>();
                for (EnqueuedStanza enqueued : outbox) {
                    Stanza stanza = enqueued.stanza;
                    if (enqueued.incCounter()) {
                        stanza.trace("Drained from outbox");
                        array.put(stanzaWriter.write(stanza));
                        stanzasToSend.add(stanza);
                        cleanedOutbox.add(enqueued);
                    }
                }
                outbox = cleanedOutbox;

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Trying to send: " + array.length() + " stanzas: " + array);
                }

                if (atmosphereResource == null || atmosphereResource.isCancelled() || atmosphereResource.getResponse().isCommitted()) {
                    // Enqueue again and try later
                    for (Stanza s: stanzasToSend) {
                        s.trace("Atmosphere Resource was committed. Enqueue again");
                    }
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
                        String stackTrace = null;
                        for (Stanza s: stanzasToSend) {
                            if (s.traceEnabled()) {
                                s.trace("Got IOException: Enqueue again");
                                if (stackTrace == null) {
                                    StringWriter w = new StringWriter();
                                    e.printStackTrace(new PrintWriter(w));
                                    stackTrace = w.toString();
                                }
                                s.trace(stackTrace);
                            }
                        }

                    }
                    outbox = null;
                }
            }

            if (sent) {
                switch (atmosphereResource.transport()) {
                case JSONP:
                case AJAX:
                case LONG_POLLING:
                    atmosphereResource.resume();
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
            String stackTrace = null;
            for (Stanza s: stanzasToSend) {
                if (s.traceEnabled()) {
                    s.trace("Got IOException: Enqueue again");
                    if (stackTrace == null) {
                        StringWriter w = new StringWriter();
                        t.printStackTrace(new PrintWriter(w));
                        stackTrace = w.toString();
                    }
                    s.trace(stackTrace);
                }
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
        String contextName = serverSession.getContext().getName();

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

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
        // Handled via send() or ResourceCleanupListener
    }

    @Override
    public void destroy() {
        // Ignore for now
    }

}
