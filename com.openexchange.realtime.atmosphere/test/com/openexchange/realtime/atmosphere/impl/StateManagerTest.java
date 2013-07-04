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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.atmosphere.protocol.StanzaTransmitter;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.IDEventHandler;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * {@link StateManagerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class StateManagerTest {
    
    
    StateManager stateManager;
    StanzaTransmitter transmitter;
    
    @Before
    public void setup() {
        stateManager = new StateManager();
        transmitter = mock(StanzaTransmitter.class);
    }
    
    @Test
    public void canCreateTheStateOnDemand() {
        
        stateManager.rememberTransmitter(new ID("test@1"), transmitter);
        StateEntry entry = stateManager.retrieveState(new ID("test@1"));
        
        assertNotNull(entry);
        assertTrue(entry.created);
        assertEquals(transmitter, entry.transmitter);
    }
    
    @Test
    public void canRetrievePreviouslyCreatedState() {
        stateManager.rememberTransmitter(new ID("test@1"), transmitter);
        StateEntry entry1 = stateManager.retrieveState(new ID("test@1"));
        StateEntry entry2 = stateManager.retrieveState(new ID("test@1"));
        
        assertEquals(entry1.state, entry2.state);
        assertEquals(entry1.transmitter, entry2.transmitter);
        assertFalse(entry2.created);
    }
    
    @Test
    public void timesOutStaleState() {
        StateEntry entry1 = stateManager.retrieveState(new ID("test1@1"));
        StateEntry entry2 = stateManager.retrieveState(new ID("test1@2"));
        StateEntry entry3 = stateManager.retrieveState(new ID("test1@3"));
        StateEntry entry4 = stateManager.retrieveState(new ID("test1@4"));
        StateEntry entry5 = stateManager.retrieveState(new ID("test1@5"));
        
        ((RTClientStateImpl) entry1.state).setLastSeen(0);
        ((RTClientStateImpl) entry2.state).setLastSeen(0);
        ((RTClientStateImpl) entry3.state).setLastSeen(60000);
        ((RTClientStateImpl) entry4.state).setLastSeen(60000);
        ((RTClientStateImpl) entry5.state).setLastSeen(60000);
        
        List<Integer> disposed = new ArrayList<Integer>();
        
        entry1.state.getId().on(ID.Events.DISPOSE, new DisposeHandler(disposed, 1));
        entry2.state.getId().on(ID.Events.DISPOSE, new DisposeHandler(disposed, 2));
        entry3.state.getId().on(ID.Events.DISPOSE, new DisposeHandler(disposed, 3));
        entry4.state.getId().on(ID.Events.DISPOSE, new DisposeHandler(disposed, 4));
        entry5.state.getId().on(ID.Events.DISPOSE, new DisposeHandler(disposed, 5));
        
        stateManager.timeOutStaleStates(120001);
        
        assertEquals(2, disposed.size());
        assertTrue(disposed.contains(1));
        assertTrue(disposed.contains(2));
        
    }
    
    public class DisposeHandler implements IDEventHandler {

        private List<Integer> disposed;
        private int number;

        public DisposeHandler(List<Integer> disposed, int number) {
            super();
            this.disposed = disposed;
            this.number = number;
            
        }

        @Override
        public void handle(String event, ID id, Object source, Map<String, Object> properties) {
            disposed.add(number);
        }

    }
    
    @Test
    public void isConnectedReturnsStateBasedOnEntries() {
        stateManager.retrieveState(new ID("connected@1"));
        assertFalse(stateManager.isConnected(new ID("disconnected@1")));
        assertTrue(stateManager.isConnected(new ID("connected@1")));
    }

}
