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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import com.openexchange.realtime.cleanup.GlobalRealtimeCleanup;
import com.openexchange.realtime.cleanup.RealtimeJanitor;
import com.openexchange.realtime.group.GroupManagerService;
import com.openexchange.realtime.json.osgi.JSONServiceRegistry;
import com.openexchange.realtime.json.protocol.RTClientState;
import com.openexchange.realtime.json.protocol.StanzaTransmitter;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.util.Duration;

/**
 * The {@link StateManager} manages the state of connected clients.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class StateManager implements RealtimeJanitor {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StateManager.class);

    protected final ConcurrentHashMap<ID, RTClientState> states = new ConcurrentHashMap<ID, RTClientState>();

    private final ConcurrentHashMap<ID, StanzaTransmitter> transmitters = new ConcurrentHashMap<ID, StanzaTransmitter>();

    /**
     * Retrieves stored {@link RTClientState} or creates a new entry for the given id.
     * @param id The id
     * @return The stored or new {@link RTClientState} for the given id
     */
    public StateEntry retrieveState(ID id) {
        RTClientState state = states.get(id);
        boolean created = false;

        if (state == null) {
            state = new RTClientStateImpl(id);
            RTClientState meantime = states.putIfAbsent(id, state);
            created = meantime == null;
            state = (created) ? state : meantime;
        }
        StanzaTransmitter transmitter = transmitters.get(id);

        return new StateEntry(state, transmitter, created);
    }

    /**
     * Associate a {@linkStanza Transmitter} with an {@link ID}
     *
     * @param id The ID
     * @param transmitter The transmitter that can be used to send messages to the associated ID
     */
    public void rememberTransmitter(ID id, StanzaTransmitter transmitter) {
        transmitters.put(id, transmitter);
    }

    /**
     * Remove a {@linkStanza Transmitter} <-> {@link ID} association
     *
     * @param id The ID
     * @param transmitter The transmitter
     */
    public void forgetTransmitter(ID id, StanzaTransmitter transmitter) {
        transmitters.remove(id, transmitter);
    }

    /**
     * Times out states that haven't been touched in more than thirty minutes. Additionally this triggers a refresh of IDs that aren't
     * timed out, yet.
     *
     * @param timestamp - The timestamp to compare the lastSeen value to
     */
    public void timeOutStaleStates(long timestamp) {
        GlobalRealtimeCleanup globalRealtimeCleanup = JSONServiceRegistry.getInstance().getService(GlobalRealtimeCleanup.class);
        GroupManagerService groupManager = JSONServiceRegistry.getInstance().getService(GroupManagerService.class);
        for (RTClientState state : new ArrayList<RTClientState>(states.values())) {
            ID client = state.getId();
            Duration inactivity = state.getInactivityDuration();
            LOG.debug("Client {} is inactive since {} seconds", client, inactivity.getValueInS());
            if(groupManager != null) {
                groupManager.setInactivity(client, inactivity);
            } else {
                LOG.error("Unable to inform GroupManager about inactivity duration. GroupManagerService is missing!");
            }
            if (state.isTimedOut(timestamp)) {
                /*
                 * The client timed out: if he'd be still active and was just rerouted to another backend the cleanup would have already
                 * happened during enrol on the other node. As we reached this code there was no cleanup yet and we still have to do
                 * it cluster-wide.
                 */
                LOG.debug("State for id {} is timed out. Last seen: {}", state.getId(), state.getLastSeen());
                if(globalRealtimeCleanup != null) {
                    globalRealtimeCleanup.cleanForId(state.getId());
                } else {
                    LOG.error("Unable to cleanup for id {}. GLobalRealtimeCleanupService is missing!", client);
                }
            } else {
                try {
                    state.getId().trigger(ID.Events.REFRESH, this);
                } catch (Exception e) {
                    LOG.error("Triggering refresh of ID: {} failed.", state.getId(), e);
                }
            }
        }
    }

    /**
     * Checks if we already have a state associated with this client
     *
     * @param id the {@link ID} representing the client
     * @return true if we already have a state associated with this client
     */
    public boolean isConnected(ID id) {
        return states.containsKey(id);
    }

    @Override
    public void cleanupForId(ID id) {
        LOG.debug("Cleanup for ID: {}", id);
        states.remove(id);
        transmitters.remove(id);
    }

}
