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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
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
import com.openexchange.java.ConcurrentHashSet;
import com.openexchange.java.ConcurrentList;
import com.openexchange.java.ConcurrentSortedSet;
import com.openexchange.log.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.realtime.atmosphere.impl.stanza.builder.StanzaBuilderSelector;
import com.openexchange.realtime.atmosphere.impl.stanza.writer.StanzaWriter;
import com.openexchange.realtime.atmosphere.osgi.AtmosphereServiceRegistry;
import com.openexchange.realtime.atmosphere.protocol.RTProtocol;
import com.openexchange.realtime.atmosphere.stanza.StanzaBuilder;
import com.openexchange.realtime.directory.DefaultResource;
import com.openexchange.realtime.directory.ResourceDirectory;
import com.openexchange.realtime.dispatch.StanzaSender;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.handle.StanzaQueueService;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.IDMap;
import com.openexchange.realtime.util.StanzaSequenceGate;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadRenamer;
import com.openexchange.timer.TimerService;
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

    protected final StateManager stateManager = new StateManager();

    protected final RTProtocol protocol = RTProtocol.getInstance();

    protected final StanzaSequenceGate gate = new StanzaSequenceGate("RTAtmosphereHandler") {

        @Override
        public void handleInternal(Stanza stanza, ID recipient) throws OXException {
            handleIncoming(stanza);
        }
    };

    public RTAtmosphereHandler() {
        super();

        startCleanupTimer();
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
            ID constructedId = constructId(resource, serverSession);
            StateEntry entry = stateManager.retrieveState(constructedId);
            if (method.equalsIgnoreCase("GET")) {

                AtmosphereStanzaTransmitter transmitter = new AtmosphereStanzaTransmitter(resource);
                stateManager.rememberTransmitter(constructedId, transmitter);

                entry = stateManager.retrieveState(constructedId);
                
                ResourceDirectory resourceDirectory = AtmosphereServiceRegistry.getInstance().getService(ResourceDirectory.class);
                resourceDirectory.set(constructedId, new DefaultResource());
                
                protocol.getReceived(entry.state, entry.transmitter);

            } else if (method.equalsIgnoreCase("POST")) {
                String postData = request.getReader().readLine();
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Incoming: " + postData);
                }

                handlePost(postData, constructedId, serverSession, entry);
            }
        } catch (OXException e) {
            protocol.handleOXException(e);
        } catch (Exception e) {
            protocol.handleException(e);
        }
    }

    protected void handlePost(String postData, ID constructedId, ServerSession serverSession, StateEntry entry) throws Exception {

        if (postData != null) {
            List<JSONObject> stanzas = parseJSON(postData);

            for (JSONObject json : stanzas) {
                if (json.has("type")) {
                    String type = json.optString("type");
            
                    
                    if (type.equals("ping")) {
                        // PING received
                        protocol.ping(constructedId, json.optBoolean("commit"), entry.state, entry.transmitter);
                        return;
                    }

                    if (type.equals("ack")) {
                        // ACK received
                        /*
                         * TODO: optimize this (e.g. client sends only the highest sequence number of a fully received sequence).
                         */
                        long seq = json.optLong("seq");

                        protocol.acknowledgementReceived(seq, entry.state);
                    }
                    return;
                }
                // Handle regular message
                StanzaBuilder<? extends Stanza> stanzaBuilder = StanzaBuilderSelector.getBuilder(constructedId, serverSession, json);
                Stanza stanza = stanzaBuilder.build();
                if (stanza.traceEnabled()) {
                    stanza.trace("received in atmosphere handler");
                }

                protocol.receivedMessage(stanza, gate, entry.state, entry.created, entry.transmitter);

            }
        }
    }

    /**
     * Handle incoming Stanza. Called by the StanzaSequenceGate to dispatch stanzas.
     * 
     * @param stanza The Stanza to handle
     * @throws OXException
     */
    protected <T extends Stanza> void handleIncoming(T stanza) throws OXException {
        // Transform payloads
        // Initialize default fields after tranforming
        stanza.transformPayloadsToInternal();
        stanza.initializeDefaults();

        StanzaQueueService stanzaQueueService = AtmosphereServiceRegistry.getInstance().getService(StanzaQueueService.class);

        if (!stanzaQueueService.enqueueStanza(stanza)) {
            // TODO: exception?
            LOG.error("Couldn't enqueue Stanza: " + stanza);
        }
    }

    /**
     * Send a stanza to the client
     */
    @Override
    public void send(Stanza stanza, ID recipient) throws OXException {
        StateEntry entry = stateManager.retrieveState(recipient);
        protocol.send(stanza, entry.state, entry.transmitter);
    }

    /**
     * Build an unique {@link ID} <code>{"ox", userLogin, context, resource}</code> from the infos given by the AtmosphereResource and
     * ServerSession.
     * 
     * @param atmosphereResource the current AtmosphereResource
     * @param serverSession the associated serverSession
     * @return the constructed unique ID
     */
    private ID constructId(AtmosphereResource atmosphereResource, ServerSession serverSession) throws OXException {
        String userLogin = serverSession.getUserlogin();
        String contextName = serverSession.getContext().getName();

        AtmosphereRequest request = atmosphereResource.getRequest();
        String resource = request.getHeader("resource");
        if (resource == null) {
            resource = request.getParameter("resource");
        }
        if (resource == null) {
            throw RealtimeExceptionCodes.INVALID_ID.create();
        }
        return new ID(RTAtmosphereChannel.PROTOCOL, null, userLogin, contextName, resource);
    }

    @Override
    public void onStateChange(AtmosphereResourceEvent event) throws IOException {
    }

    @Override
    public void destroy() {
    }

    public boolean isConnected(ID id) {
        return stateManager.isConnected(id);
    }

    /**
     * Starts the timer that times out clients if they were silent for too long
     */
    private void startCleanupTimer() {
        AtmosphereServiceRegistry.getInstance().getService(TimerService.class).scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                stateManager.timeOutStaleStates(System.currentTimeMillis());
            }

        }, 60000, 1, TimeUnit.MINUTES);
    }

    /**
     * Parses either a single JSONObject or a JSONArray and returns the objects in a List
     */
    private List<JSONObject> parseJSON(String postData) throws JSONException {
        List<JSONObject> stanzas = new LinkedList<JSONObject>();
        if (postData.startsWith("[")) {
            JSONArray arr = new JSONArray(postData);
            for (int i = 0, size = arr.length(); i < size; i++) {
                stanzas.add(arr.getJSONObject(i));
            }
        } else {
            stanzas.add(new JSONObject(postData));
        }
        return stanzas;
    }

}
