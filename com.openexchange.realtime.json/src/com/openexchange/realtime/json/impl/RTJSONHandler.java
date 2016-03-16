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

package com.openexchange.realtime.json.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.osgi.ExceptionUtils;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.dispatch.StanzaSender;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.json.management.ManagementHouseKeeper;
import com.openexchange.realtime.json.osgi.JSONServiceRegistry;
import com.openexchange.realtime.json.osgi.RealtimeJanitors;
import com.openexchange.realtime.json.protocol.RTProtocol;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.StanzaSequenceGate;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RTJSONHandler} - Handler that gets associated with the {@link JSONChannel} and does the main work of handling
 * incoming and outgoing Stanzas. Transformation of Stanzas is handed over to the proper OXRTHandler
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> JavaDoc
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class RTJSONHandler implements StanzaSender {

    private static final Logger LOG = LoggerFactory.getLogger(RTJSONHandler.class);
    protected final StateManager stateManager;
    protected final RTProtocol protocol;
    protected final StanzaSequenceGate gate;
    JSONProtocolHandler protocolHandler;
    private volatile ScheduledTimerTask startCleanupTimer;

    public RTJSONHandler() {
        super();
        stateManager = new StateManager();
        RealtimeJanitors.getInstance().addJanitor(stateManager);
        protocol = RTProtocolImpl.getInstance();
        gate = new StanzaSequenceGate(RTJSONHandler.class.getSimpleName()) {

            @Override
            public void handleInternal(Stanza stanza, ID recipient) throws OXException {
                    handleIncoming(stanza);
            }
        };
        ManagementHouseKeeper.getInstance().addManagementObject(gate.getManagementObject());
        RealtimeJanitors.getInstance().addJanitor(gate);
        protocolHandler = new JSONProtocolHandler(protocol, gate);
        startCleanupTimer = startCleanupTimer();
    }

    /**
     * Shuts down associated cleanup task.
     */
    public void shutDownCleanupTimer() {
        ScheduledTimerTask startCleanupTimer = this.startCleanupTimer;
        if (null != startCleanupTimer) {
            stateManager.shutDown();
            startCleanupTimer.cancel(true);
            this.startCleanupTimer = null;
        }
    }

    protected void handlePost(String postData, ID constructedId, ServerSession serverSession, StateEntry entry) throws RealtimeException {
        if (postData != null) {
            List<JSONObject> stanzas;
            try {
                stanzas = parseJSON(postData);
            } catch (JSONException e) {
                throw RealtimeExceptionCodes.STANZA_BAD_REQUEST.create(e.getMessage());
            }
            protocolHandler.handleIncomingMessages(constructedId, serverSession, entry, stanzas, null);
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

        MessageDispatcher dispatcher = JSONServiceRegistry.getInstance().getService(MessageDispatcher.class);

        dispatcher.send(stanza);
    }

    /**
     * Send a stanza to the client
     */
    @Override
    public void send(Stanza stanza, ID recipient) throws OXException {
        StateEntry entry = stateManager.retrieveState(recipient);
        protocol.send(stanza, entry.state, entry.transmitter);
    }

    public boolean isConnected(ID id) {
        return stateManager.isConnected(id);
    }

    /**
     * Starts the timer that times out clients if they were silent for too long
     */
    private ScheduledTimerTask startCleanupTimer() {
        final Logger logger = LOG;
        return JSONServiceRegistry.getInstance().getService(TimerService.class).scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                stateManager.timeOutStaleStates(System.currentTimeMillis());
                } catch (Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    logger.error("Error during CleanupTimer run.", t);
                }
            }

        }, 1, 10, TimeUnit.SECONDS);
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

    public StateManager getStateManager() {
        return stateManager;
    }

    public JSONProtocolHandler getProtocolHandler() {
        return protocolHandler;
    }

    public RealtimeJanitor getGate() {
        return gate;
    }

}
