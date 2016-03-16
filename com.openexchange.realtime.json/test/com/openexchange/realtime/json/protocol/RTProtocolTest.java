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

package com.openexchange.realtime.json.protocol;

import static com.openexchange.realtime.packet.StanzaMatcher.isStanza;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.json.JSONException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.json.impl.RTProtocolImpl;
import com.openexchange.realtime.json.protocol.RTClientState;
import com.openexchange.realtime.json.protocol.RTProtocol;
import com.openexchange.realtime.json.protocol.StanzaTransmitter;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDManager;
import com.openexchange.realtime.packet.Message;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.StanzaSequenceGate;
/**
 * {@link RTProtocolTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTProtocolTest {
    
    boolean bufferEmptied = false;
    Stanza sequenceGateStanza = null;
    long nextSequence = -1;
    
    RTProtocol protocol = new RTProtocolImpl() {
        @Override
        public void emptyBuffer(RTClientState state, StanzaTransmitter transmitter) {
            bufferEmptied = true;
        };
    };
    
    RTClientState state;
    StanzaTransmitter transmitter;
    StanzaSequenceGate gate = null;
    
    @BeforeClass
    public static void setUp() {
        ID.ID_MANAGER_REF.set(new IDManager());
    }

    @AfterClass
    public static void tearDown() {
        ID.ID_MANAGER_REF.set(null);
    }
    
    @Before
    public void setup() {
        state = mock(RTClientState.class);
        transmitter = mock(StanzaTransmitter.class);
        bufferEmptied = false;
        sequenceGateStanza = null;
        
        gate = new StanzaSequenceGate("Test") {
            
            @Override
            public void handleInternal(Stanza stanza, ID recipient) throws OXException {
                sequenceGateStanza = stanza;
            }
            
            @Override
            public void resetThreshold(ID constructedId, long newSequence) {
                nextSequence = newSequence;
            }
        };
    }

    /**
     * Emptying the buffer
     * @throws OXException 
     */
    @Test
    public void emptyingTheBufferSendsBufferedStanzasAndUpdatesTheClientState() throws OXException {
        List<Stanza> bufferedStanzas = Arrays.asList((Stanza) new Message(), new Message(), new Message());
        
        when(state.getStanzasToSend()).thenReturn(bufferedStanzas);
        when(transmitter.send(bufferedStanzas)).thenReturn(true);
        
        // Needs an untouched RTProtocol. Method is mocked for the remaining tests
        new RTProtocolImpl().emptyBuffer(state, transmitter);
        
        verify(transmitter).send(bufferedStanzas);
        verify(state).purge();
        verify(state).touch();
    }

    @Test
    public void emptyingAnAlreadyEmptyBufferSuspendsTheTransmitter() throws OXException {
        List<Stanza> emptyBuffer = Collections.emptyList();
        
        when(state.getStanzasToSend()).thenReturn(emptyBuffer);
        
        // Needs an untouched RTProtocol. Method is mocked for the remaining tests
        new RTProtocolImpl().getReceived(state, transmitter);
        
        verify(transmitter).suspend();
        verify(state).touch();
    }
    
    /**
     * Incoming GET
     * @throws OXException 
     */
    @Test
    public void anIncomingGETemptiesTheBuffer() throws OXException {
        protocol.getReceived(state, transmitter);
        assertTrue(bufferEmptied);
    }
    

    /**
     * PING handling
     * @throws OXException 
     */
    @Test
    public void onAnIncomingPingTheStateIsTouched() throws OXException {
        protocol.ping(null, false, state, transmitter);
        
        verify(state).touch();
        assertFalse(bufferEmptied);        
    }
    
    @Test
    public void onAnIncomingPingThatAsksForACommitAPongIsSentBack() throws JSONException, OXException {
        ID from = new ID("test@1");
        
        protocol.ping(from, true, state, transmitter);
        
        verify(state).enqueue(argThat(isStanza(from, from, "atmosphere", "pong", "1")));
        verify(state).touch();
        assertTrue(bufferEmptied);
    }
    
    /**
     * Incoming messages
     * @throws OXException 
     */
    
    @Test
    public void anIncomingMessageIsPassedToASequenceGate() throws OXException {
        Message message = new Message();
        ID from = new ID("test@1");
        ID to = new ID("test2@1");
        
        message.setFrom(from);
        message.setTo(to);
        
        protocol.receivedMessage(message, gate, state, true, transmitter);
        
        assertEquals(message, sequenceGateStanza);
    }

    @Test
    public void ifTheConnectionHasNoStateANextSequenceMessageIsSentBackOnAnIncomingMessage() throws OXException {
        Message message = new Message();
        ID from = new ID("test@1");
        message.setFrom(from);
        
        protocol.receivedMessage(message, gate, state, true, transmitter);
        
        verify(state).enqueue(argThat(isStanza(from, from, "atmosphere", "nextSequence", 0l)));
        assertTrue(bufferEmptied);
    }
    

    @Test
    public void anIncomingSequencedMessageIsAnsweredWithAnAcknowledgement() throws OXException {
        Message message = new Message();
        ID from = new ID("test@1");
        message.setFrom(from);
        message.setSequenceNumber(2);
        
        protocol.receivedMessage(message, gate, state, false, transmitter);
        
        verify(state).enqueue(argThat(isStanza(from, from, "atmosphere", "received", 2l)));
        assertTrue(bufferEmptied);
    }
    
    @Test
    public void alternativelyAcknowledgementsAreCollectedInAList() throws OXException {
        Message message = new Message();
        ID from = new ID("test@1");
        message.setFrom(from);
        message.setSequenceNumber(2);
        
        ArrayList<Long> acknowledgements = new ArrayList<Long>();
        
        
        protocol.receivedMessage(message, gate, state, false, transmitter, acknowledgements);
        
        verify(state, never()).enqueue(argThat(isStanza(from, from, "atmosphere", "received", 2l)));
        assertFalse(bufferEmptied);
        
        assertEquals(1, acknowledgements.size());
        assertEquals((Long)2l, acknowledgements.get(0));
    }
    
    @Test
    public void anIncomingMessageThatIsDiscardedByTheSequenceGateResultsInAnException() throws OXException {
        int i = 1;
        Stanza s = new Message();
        s.setSequenceNumber(i);
        
        ID from = new ID("test@1");
        s.setFrom(from);
        
        while(i < 21) {
            i++;
            s = new Message();
            s.setSequenceNumber(i);
            s.setFrom(from);
        }
        
        boolean caught = false;
        try {
            protocol.receivedMessage(s, gate, state, false, transmitter);
        } catch (RealtimeException x) {
            caught = x.getCode() == 1006;
            if (!caught) {
                throw x;
            }
        }
        assertTrue(caught);
        
    }
    
    @Test
    public void anIncomingMessageTouchesTheClientState() throws OXException {
        Message message = new Message();
        ID from = new ID("test@1");
        message.setFrom(from);
        
        protocol.receivedMessage(message, gate, state, false, transmitter);
        
        verify(state).touch();
    }
    
    /**
     * Incoming acknowledgements
     */
    @Test
    public void anIncomingAcknowledgementIsForwardedToTheState() {
        protocol.acknowledgementReceived(23, state);
        verify(state).acknowledgementReceived(23);
    }
    
    
    /**
     * Outgoing messages
     * @throws OXException 
     */
    @Test
    public void anOutgoingStanzaIsEnqueudAndTheBufferIsDrained() throws OXException {
        Message message = new Message();
        protocol.send(message, state, transmitter);
        
        verify(state).enqueue(message);
        assertTrue(bufferEmptied);
    }
    
    /**
     * Resetting sequence numbers
     * @throws RealtimeException 
     */
    @Test
    public void clientsCanResetTheirSequenceNumbering() throws RealtimeException {
        protocol.nextSequence(new ID("test://user1@1") , 12, gate, state);
        assertEquals(12, nextSequence);
    }
    
}
