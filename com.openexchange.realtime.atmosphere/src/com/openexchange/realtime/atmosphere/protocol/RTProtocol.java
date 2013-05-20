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

package com.openexchange.realtime.atmosphere.protocol;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.dispatch.MessageDispatcher;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.PayloadTree;
import com.openexchange.realtime.payload.PayloadTreeNode;
import com.openexchange.realtime.util.StanzaSequenceGate;


/**
 * The {@link RTProtocol} contains the logic to handle all protocol events of the RT protocol.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTProtocol {
    
    private static final AtomicReference<RTProtocol> PROTOCOL = new AtomicReference<RTProtocol>();

    public static RTProtocol getInstance() {
        if (null == PROTOCOL.get()) {
            PROTOCOL.compareAndSet(null, new RTProtocol());
        }
        return PROTOCOL.get();
    }

    /**
     * Called when a GET request is received from the client. 
     * @param state - the client state
     * @param transmitter - the client transmitter 
     * @throws OXException 
     */
    public void getReceived(RTClientState state, StanzaTransmitter transmitter) throws OXException {
        emptyBuffer(state, transmitter);
    }
    
    /**
     * Called when a Ping is received from the client. If the ping asks for a commit, a Pong message is generated, enqueued and the buffer is emptied, otherwise only the states
     * timestamp is touched.
     * @throws OXException 
     */
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

    /**
     * A 'received' message was received from the client
     */
    public void acknowledgementReceived(long seq, RTClientState state) {
        state.acknowledgementReceived(seq);
    }

    /**
     * Enqueus a stanza and empties the buffer
     * @throws OXException 
     */
    public void send(Stanza stanza, RTClientState state, StanzaTransmitter transmitter) throws OXException {
        try {
            state.lock();
            state.enqueue(stanza);
            emptyBuffer(state, transmitter);
        } finally {
            state.unlock();
        }
    }
    
    /**
     * A message was received from the client
     * @param newState 
     * @throws OXException 
     */
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

    /**
     * Empties the buffer, if there are messages to be sent
     * @throws OXException 
     */
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


    public void handleOXException(OXException e) {
        // TODO Auto-generated method stub
        
    }

    public void handleException(Exception e) {
        // TODO Auto-generated method stub
        
    }




    
}
