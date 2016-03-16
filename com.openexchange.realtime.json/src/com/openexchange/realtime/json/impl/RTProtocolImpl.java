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

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.json.osgi.JSONServiceRegistry;
import com.openexchange.realtime.json.protocol.NextSequence;
import com.openexchange.realtime.json.protocol.RTClientState;
import com.openexchange.realtime.json.protocol.RTProtocol;
import com.openexchange.realtime.json.protocol.StanzaTransmitter;
import com.openexchange.realtime.packet.GenericError;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.StanzaSequenceGate;


/**
 * The {@link RTProtocolImpl} contains the logic to handle all protocol events of the RT protocol like pings and acknowledgements.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTProtocolImpl implements RTProtocol {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RTProtocol.class);

    private static final AtomicReference<RTProtocolImpl> PROTOCOL = new AtomicReference<RTProtocolImpl>();

    public static RTProtocol getInstance() {
        if (null == PROTOCOL.get()) {
            PROTOCOL.compareAndSet(null, new RTProtocolImpl());
        }
        return PROTOCOL.get();
    }

    @Override
    public void getReceived(RTClientState state, StanzaTransmitter transmitter) {
        LOG.debug("Get received from {}", state.getId());
        emptyBuffer(state, transmitter);
    }

    @Override
    public void ping(ID from, boolean commit, RTClientState state, StanzaTransmitter transmitter) {
        state.lock();
        try {
            state.touch();
            if (commit) {
                sendPong(from, state, transmitter);
            }
        } finally {
            state.unlock();
        }
    }

    @Override
    public void acknowledgementReceived(long seq, RTClientState state) {
        state.acknowledgementReceived(seq);
    }

    @Override
    public void send(Stanza stanza, RTClientState state, StanzaTransmitter transmitter) {
        state.lock();
        try {
            state.enqueue(stanza);
            emptyBuffer(state, transmitter);
        } finally {
            state.unlock();
        }
    }

    @Override
    public void receivedMessage(Stanza stanza, StanzaSequenceGate gate, RTClientState state, boolean newState, StanzaTransmitter transmitter) throws RealtimeException {
        state.lock();
        try {
            state.touch();
            stanza.trace("Received message in RTProtocol with asynchronous acknowledgements");
            boolean enqueued = false;
            if (newState) {
                stanza.trace("We have no state about this client " + stanza.getFrom()+ " sending nextSequence message");
                enqueueNextSequence(stanza.getFrom(), state, transmitter);
                enqueued = true;
            }

            if (gate.handle(stanza, stanza.getTo())) {
                stanza.trace("Sending receipt for client message " + stanza.getSequenceNumber());
                enqueueAcknowledgement(stanza.getFrom(), stanza.getSequenceNumber(), state, transmitter);
                enqueued = true;
            }

            if (enqueued) {
                emptyBuffer(state, transmitter);
            }
        } finally {
            state.unlock();
        }
    }

    @Override
    public void nextSequence(ID constructedId, int newSequence, StanzaSequenceGate gate, RTClientState clientState) throws RealtimeException {
        gate.resetThreshold(constructedId, newSequence);
        clientState.reset();
    }

    @Override
    public void receivedMessage(Stanza stanza, StanzaSequenceGate gate, RTClientState state, boolean newState, StanzaTransmitter transmitter, List<Long> acknowledgements) throws RealtimeException {
        state.lock();
        try {
            state.touch();
            stanza.trace("Received message in RTProtocol with synchronous acknowledgements");
            if (newState) {
                stanza.trace("We have no state about this client " + stanza.getFrom() + " sending nextSequence message");
                enqueueNextSequence(stanza.getFrom(), state, transmitter);
            }
        } finally {
            state.unlock();
        }
        //Remember the original sequence as it might get changed for local or remote delivery
        long sequenceNumber = stanza.getSequenceNumber();
        if (gate.handle(stanza, stanza.getTo())) {
            stanza.trace("Adding receipt for client message " + sequenceNumber + " to acknowledgement list");
            acknowledgements.add(sequenceNumber);
        }
    }

    @Override
    public void emptyBuffer(RTClientState state, StanzaTransmitter transmitter) {
        if (transmitter == null) {
            LOG.debug("Transmitter was null.");
            return;
        }
        state.lock();
        try {
            state.touch();

            List<Stanza> stanzasToSend = state.getStanzasToSend();
            if (stanzasToSend.isEmpty()) {
                LOG.debug("No stanzas to send for {}. Suspending transmitter.", state.getId());
                transmitter.suspend();
                return;
            }
            try {
                LOG.debug("Trying to send {} stanzas to {}.", stanzasToSend.size(), state.getId());
                transmitter.send(stanzasToSend);
            } catch (OXException e) {
                LOG.debug("Error while trying to send Stanza(s) to client: {}", state.getId(), e);
            }
        } finally {
            //Increment TTL count even after failure as offending stanza might cause sending to fail. Incrementing will get rid of it.
            state.purge();
            state.unlock();
        }
    }

    // Protected methods

    protected void sendPong(ID to, RTClientState state, StanzaTransmitter transmitter) {
        Stanza s = new Message();
        s.setFrom(to);
        s.setTo(to);
        s.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
            "1",
            "json",
            "atmosphere",
            "pong").build()));
        state.enqueue(s);
        emptyBuffer(state, transmitter);
    }

    protected void enqueueNextSequence(ID to, RTClientState state, StanzaTransmitter transmitter) {
        NextSequence nextSequence = new NextSequence(to, to, 0);
        state.enqueue(nextSequence);
    }

    protected void enqueueAcknowledgement(ID to, long sequenceNumber, RTClientState state, StanzaTransmitter transmitter) {
        Stanza s = new Message();
        s.setFrom(to);
        s.setTo(to);
        s.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
            sequenceNumber,
            "json",
            "atmosphere",
            "received").build()));
        state.enqueue(s);
    }

    @Override
    public void handleRealtimeException(ID recipient, RealtimeException exception, Stanza stanza) {
        if(recipient != null) {
            if(stanza == null) {
                stanza = new GenericError(exception);
                stanza.setTo(recipient);
            } else {
                stanza.setError(exception);
                stanza.setTo(stanza.getFrom());
            }
            try {
                MessageDispatcher messageDispatcher = JSONServiceRegistry.getInstance().getService(MessageDispatcher.class);
                LOG.debug("Sending error message to client: {}", stanza);
                messageDispatcher.send(stanza);
            } catch (Exception e) {
                LOG.error("Error while handling RealtimeException: {}", stanza, e);
            }

        }
        LOG.error("", exception);
    }

}
