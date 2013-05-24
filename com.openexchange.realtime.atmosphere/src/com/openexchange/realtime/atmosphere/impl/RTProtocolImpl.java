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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.protocol.RTClientState;
import com.openexchange.realtime.atmosphere.protocol.RTProtocol;
import com.openexchange.realtime.atmosphere.protocol.StanzaTransmitter;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.StanzaSequenceGate;


/**
 * The {@link RTProtocolImpl} contains the logic to handle all protocol events of the RT protocol.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTProtocolImpl implements RTProtocol {
    
    private static final AtomicReference<RTProtocolImpl> PROTOCOL = new AtomicReference<RTProtocolImpl>();

    public static RTProtocol getInstance() {
        if (null == PROTOCOL.get()) {
            PROTOCOL.compareAndSet(null, new RTProtocolImpl());
        }
        return PROTOCOL.get();
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#getReceived(com.openexchange.realtime.atmosphere.protocol.RTClientState, com.openexchange.realtime.atmosphere.protocol.StanzaTransmitter)
     */
    @Override
    public void getReceived(RTClientState state, StanzaTransmitter transmitter) throws OXException {
        emptyBuffer(state, transmitter);
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#ping(com.openexchange.realtime.packet.ID, boolean, com.openexchange.realtime.atmosphere.protocol.RTClientState, com.openexchange.realtime.atmosphere.protocol.StanzaTransmitter)
     */
    @Override
    public void ping(ID from, boolean commit, RTClientState state, StanzaTransmitter transmitter) throws OXException {
        try {
            state.lock();
            state.touch();
            if (commit) {
                sendPong(from, state, transmitter);
            }
        } finally {
            state.unlock();
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#acknowledgementReceived(long, com.openexchange.realtime.atmosphere.protocol.RTClientState)
     */
    @Override
    public void acknowledgementReceived(long seq, RTClientState state) {
        state.acknowledgementReceived(seq);
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#send(com.openexchange.realtime.packet.Stanza, com.openexchange.realtime.atmosphere.protocol.RTClientState, com.openexchange.realtime.atmosphere.protocol.StanzaTransmitter)
     */
    @Override
    public void send(Stanza stanza, RTClientState state, StanzaTransmitter transmitter) throws OXException {
        try {
            state.lock();
            state.enqueue(stanza);
            emptyBuffer(state, transmitter);
        } finally {
            state.unlock();
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#receivedMessage(com.openexchange.realtime.packet.Stanza, com.openexchange.realtime.util.StanzaSequenceGate, com.openexchange.realtime.atmosphere.protocol.RTClientState, boolean, com.openexchange.realtime.atmosphere.protocol.StanzaTransmitter)
     */
    @Override
    public void receivedMessage(Stanza stanza, StanzaSequenceGate gate, RTClientState state, boolean newState, StanzaTransmitter transmitter) throws OXException {
        try {
            state.lock();
            state.touch();
            if (newState) {
                sendNextSequence(stanza.getFrom(), state, transmitter);
            }

            if (gate.handle(stanza, stanza.getTo())) {
                sendAcknowledgement(stanza.getFrom(), stanza.getSequenceNumber(), state, transmitter);
            }
        } finally {
            state.unlock();
        }
    }
    
    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#receivedMessage(com.openexchange.realtime.packet.Stanza, com.openexchange.realtime.util.StanzaSequenceGate, com.openexchange.realtime.atmosphere.protocol.RTClientState, boolean, com.openexchange.realtime.atmosphere.protocol.StanzaTransmitter)
     */
    @Override
    public void receivedMessage(Stanza stanza, StanzaSequenceGate gate, RTClientState state, boolean newState, StanzaTransmitter transmitter, List<Long> acknowledgements) throws OXException {
        try {
            state.lock();
            state.touch();
            if (newState) {
                sendNextSequence(stanza.getFrom(), state, transmitter);
            }

            if (gate.handle(stanza, stanza.getTo())) {
                sendAcknowledgement(stanza.getFrom(), stanza.getSequenceNumber(), state, transmitter);
            }
        } finally {
            state.unlock();
        }
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#emptyBuffer(com.openexchange.realtime.atmosphere.protocol.RTClientState, com.openexchange.realtime.atmosphere.protocol.StanzaTransmitter)
     */
    @Override
    public void emptyBuffer(RTClientState state, StanzaTransmitter transmitter) throws OXException {
        try {
            state.lock();
            state.touch();

            List<Stanza> stanzasToSend = state.getStanzasToSend();
            if (stanzasToSend.isEmpty()) {
                transmitter.suspend();
                return;
            }
            if (transmitter.send(stanzasToSend)) {
                state.purge();
            }
        } finally {
            state.unlock();
        }
    }
    
    // Protected methods
    
    protected void sendPong(ID to, RTClientState state, StanzaTransmitter transmitter) throws OXException {
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
    
    protected void sendNextSequence(ID to, RTClientState state, StanzaTransmitter transmitter) throws OXException {
        Stanza s = new Message();
        s.setFrom(to);
        s.setTo(to);
        s.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
            0,
            "json",
            "atmosphere",
            "nextSequence").build()));
        state.enqueue(s);
        emptyBuffer(state, transmitter);
    }

    protected void sendAcknowledgement(ID to, long sequenceNumber, RTClientState state, StanzaTransmitter transmitter) throws OXException {
        Stanza s = new Message();
        s.setFrom(to);
        s.setTo(to);
        s.addPayload(new PayloadTree(PayloadTreeNode.builder().withPayload(
            sequenceNumber,
            "json",
            "atmosphere",
            "received").build()));
        state.enqueue(s);
        emptyBuffer(state, transmitter);
    }


    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#handleOXException(com.openexchange.exception.OXException)
     */
    @Override
    public void handleOXException(OXException e) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.atmosphere.protocol.RTProtocol#handleException(java.lang.Exception)
     */
    @Override
    public void handleException(Exception e) {
        // TODO Auto-generated method stub
        
    }




    
}
