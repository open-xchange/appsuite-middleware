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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.realtime.json.mock.JSONServiceRegistryMock;
import com.openexchange.realtime.json.osgi.JSONServiceRegistry;
import com.openexchange.realtime.json.protocol.StanzaTransmitter;
import com.openexchange.realtime.packet.ID;

/**
 * {@link StateManagerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class StateManagerTest extends StateManager {
    
    
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
        ID one = new ID("test1@1");
        ID two = new ID("test1@2");
        ID three = new ID("test1@3");
        ID four = new ID("test1@4");
        ID five = new ID("test1@5");
        StateEntry entry1 = retrieveState(one);
        StateEntry entry2 = retrieveState(two);
        StateEntry entry3 = retrieveState(three);
        StateEntry entry4 = retrieveState(four);
        StateEntry entry5 = retrieveState(five);
        
        ((RTClientStateImpl) entry1.state).setLastSeen(0);
        ((RTClientStateImpl) entry2.state).setLastSeen(0);
        ((RTClientStateImpl) entry3.state).setLastSeen(60000);
        ((RTClientStateImpl) entry4.state).setLastSeen(60000);
        ((RTClientStateImpl) entry5.state).setLastSeen(60000);
        
        //set serviceLookup Mock so we can call timeOutStaleStates that would call GlobalCleanup
        JSONServiceRegistry.SERVICES.set(new JSONServiceRegistryMock(states));
        timeOutStaleStates(1800001);
        
        Set<ID> keySet = states.keySet();
        assertEquals(3, keySet.size());
        
        assertTrue(keySet.contains(three));
        assertTrue(keySet.contains(four));
        assertTrue(keySet.contains(five));
        
    }

    @Test
    public void isConnectedReturnsStateBasedOnEntries() {
        stateManager.retrieveState(new ID("connected@1"));
        assertFalse(stateManager.isConnected(new ID("disconnected@1")));
        assertTrue(stateManager.isConnected(new ID("connected@1")));
    }

}
