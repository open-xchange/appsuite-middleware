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

import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Message;
import static org.junit.Assert.*;

/**
 * {@link RTClientStateTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RTClientStateTest {
    
    private RTClientStateImpl state;
    
    @Before
    public void setup() {
        state = new RTClientStateImpl(new ID("dummyId@1"));
    }
    
    /**
     * Tests for buffer handling
     */
    
    @Test
    public void aStanzaWithoutSequenceShouldBeAddedToTheNonsequencedList() {
        Message message = new Message();
        state.enqueue(message);
        
        assertEquals(1, state.getNonsequenceStanzas().size());
        assertEquals(state.getNonsequenceStanzas().get(0), message);
        assertEquals(0, state.getResendBuffer().size());
    }
    
    @Test
    public void aStanzaWithSequenceShouldBeAddedToTheResendBufferWithTheSequenceRecastToTheCurrentSequenceNumber() {
        Message message = new Message();
        message.setSequenceNumber(23);
        state.enqueue(message);
        
        assertEquals(1, state.getResendBuffer().size());
        EnqueuedStanza enqueuedStanza = state.getResendBuffer().get(0l);
        assertEquals(enqueuedStanza.stanza, message);
        assertEquals(enqueuedStanza.sequenceNumber, 0l);
        assertEquals(enqueuedStanza.stanza.getSequenceNumber(), 0l);
        
        assertEquals(0, state.getNonsequenceStanzas().size());

        message = new Message();
        message.setSequenceNumber(24);
        state.enqueue(message);
        
        enqueuedStanza = state.getResendBuffer().get(1l);

        assertEquals(enqueuedStanza.sequenceNumber, 1l);
        assertEquals(enqueuedStanza.stanza.getSequenceNumber(), 1l);
        
    }
    
    @Test
    public void theStanzasToSendConsistOfOutstandingNonsequencedStanzasAndTheResendBuffer() {
        Message unsequenced = new Message();
        state.enqueue(unsequenced);

        Message sequenced = new Message();
        sequenced.setSequenceNumber(23);
        state.enqueue(sequenced);
        
        assertTrue(state.getStanzasToSend().contains(unsequenced));
        assertTrue(state.getStanzasToSend().contains(sequenced));
    }
    
    @Test
    public void aPurgeRunShouldCleanOutAllNonsequencedStanzas() {
        Message unsequenced = new Message();
        state.enqueue(unsequenced);

        state.purge();
        
        assertTrue(state.getNonsequenceStanzas().isEmpty());
    }
    
    @Test
    public void aPurgeRunShouldCleanOutSequencedStanzasAfterOneHundredAttempts() {
        Message sequenced = new Message();
        sequenced.setSequenceNumber(0);
        state.enqueue(sequenced);
        
        state.purge();
        
        assertEquals(state.getResendBuffer().get(0l).stanza, sequenced);
        
        for (int i = 0; i < 100; i++) {
            state.purge();
        }
        
        assertTrue(state.getResendBuffer().isEmpty());
    }
    
    @Test
    public void receivingAnAcknowledgementShouldRemoveTheStanzaFromTheResendBuffer() {
        Message sequenced = new Message();
        sequenced.setSequenceNumber(0);
        state.enqueue(sequenced);
        
        state.acknowledgementReceived(0);
        
        assertTrue(state.getResendBuffer().isEmpty());
    }
    
    /**
     * Tests for timeouts
     */    
    @Test
    public void touchShouldSetLastSeenToTheCurrentTime() {
        long now = System.currentTimeMillis();
        state.touch();
        assertTrue(now - state.getLastSeen() < 10);
    }
    
    @Test
    public void theStateShouldExpireAfterTwoMinutes() {
        state.touch();
        long now = state.getLastSeen();
        
        assertFalse(state.isTimedOut(now + 59000));
        assertTrue(state.isTimedOut(now + 120001));
        
    }

    
}
