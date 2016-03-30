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

import static com.openexchange.realtime.packet.StanzaMatcher.isStanza;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.json.protocol.RTClientState;
import com.openexchange.realtime.json.protocol.RTProtocol;
import com.openexchange.realtime.json.protocol.StanzaTransmitter;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.StanzaSequenceGate;

/**
 * {@link ProtocolHandlerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class ProtocolHandlerTest {
    
    private JSONProtocolHandler handler;
    private RTProtocol protocol;
    private ID id = new ID("test@1");
    private StanzaSequenceGate gate = new StanzaSequenceGate("Test") {
        
        @Override
        public void handleInternal(Stanza stanza, ID recipient) throws OXException {
        }
    };
    private JSONObject message0, message1, message2;
    private List<JSONObject> stanzas;
    
    
    @Before
    public void setup() throws Exception {
        protocol = mock(RTProtocol.class);
        handler = new JSONProtocolHandler(protocol, gate);
        stanzas = new ArrayList<JSONObject>();
        message0 = new JSONObject("{element: 'message', payloads: [{namespace: 'test', element: 'number', data: 1}], to: 'test@1', from: 'test@1', seq: '0'}");
        message1 = new JSONObject("{element: 'message', payloads: [{namespace: 'test', element: 'number', data: 2}], to: 'test@1', from: 'test@1', seq: '1'}");
        message2 = new JSONObject("{element: 'message', payloads: [{namespace: 'test', element: 'number', data: 3}], to: 'test@1', from: 'test@1', seq: '2'}");
        stanzas.add(message0);
        stanzas.add(message1);
        stanzas.add(message2);
    }
    
    @Test
    public void passesATypePingJSONObjectToThePingMethod() throws OXException, JSONException {
        String message = "{type: 'ping', commit: true}";
        handle(message);
        
        verify(protocol).ping(id, true, null, null);
    }
    
    @Test
    public void passesATypePingJSONObjectToThePingMethodWithoutCommit() throws OXException, JSONException {
        String message = "{type: 'ping'}";
        handle(message);
        
        verify(protocol).ping(id, false, null, null);
    }
    
    @Test
    public void passesAnAck() throws OXException, JSONException {
        String message = "{type: 'ack', seq: 12}";
        handle(message);
        
        verify(protocol).acknowledgementReceived(12l, null);
        
    }
    
    @Test
    public void passesANextSequence() throws OXException, JSONException {
        String message = "{type: 'nextSequence', seq: 0}";
        handle(message);
        verify(protocol).nextSequence(id, 0, gate, null);
    }
    
    
    @Test
    public void acksCanBeCompressedIntoAnArrayOfValues() throws OXException, JSONException {
        String message = "{type: 'ack', seq: [12, 13, 14, 15, 16]}";
        handle(message);
        
        verify(protocol).acknowledgementReceived(12l, null);
        verify(protocol).acknowledgementReceived(13l, null);
        verify(protocol).acknowledgementReceived(14l, null);
        verify(protocol).acknowledgementReceived(15l, null);
        verify(protocol).acknowledgementReceived(16l, null);
        
    }
    
    @Test
    public void acksCanBeCompressedIntoAnArrayOfValuesAndRanges() throws OXException, JSONException {
        String message = "{type: 'ack', seq: [12, [16, 20], 22]}";
        handle(message);
        
        verify(protocol).acknowledgementReceived(12l, null);
        verify(protocol).acknowledgementReceived(16l, null);
        verify(protocol).acknowledgementReceived(17l, null);
        verify(protocol).acknowledgementReceived(18l, null);
        verify(protocol).acknowledgementReceived(19l, null);
        verify(protocol).acknowledgementReceived(20l, null);
        verify(protocol).acknowledgementReceived(22l, null);
        
    }
    
    @Test
    public void passesARegularMessage() throws OXException, JSONException {
        String message = "{element: 'message', payloads: [{namespace: 'test', element: 'number', data: 23}], to: 'test@1', from: 'test@1'}";
        handle(message);
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 23)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
    }
    
    @Test
    public void passNextSequenceAndMessages() throws Exception {
        JSONObject nextSequence = new JSONObject("{type: 'nextSequence', seq: 0}");
        stanzas.add(0, nextSequence);
        handle(stanzas);
        verify(protocol).nextSequence(id, 0, gate, null);
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 1)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 2)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 3)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
    }

    @Test
    public void passPingAndMessages() throws Exception {
        JSONObject ping = new JSONObject("{type: 'ping', commit: false}");
        stanzas.add(0, ping);
        handle(stanzas);
        verify(protocol).ping(id, false, null, null);
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 1)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 2)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 3)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
    }

    @Test
    public void passAckAndMessages() throws Exception {
        JSONObject ack = new JSONObject("{type: 'ack', seq: 12}");
        stanzas.add(0, ack);
        handle(stanzas);
        verify(protocol).acknowledgementReceived(12L, null);
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 1)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 2)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
        verify(protocol).receivedMessage(argThat(isStanza(id, id, "test", "number", 3)), eq(gate), isNull(RTClientState.class), eq(false), isNull(StanzaTransmitter.class));
    }

    private void handle(String message) throws OXException, JSONException {
        handler.handleIncomingMessages(id, null, new StateEntry(null, null, false), Arrays.asList(new JSONObject(message)), null);
    }

    private void handle(List<JSONObject> stanzas) throws OXException, JSONException {
        handler.handleIncomingMessages(id, null, new StateEntry(null, null, false), stanzas, null);
    }
}
