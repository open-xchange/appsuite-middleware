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
import org.json.JSONArray;
import org.json.JSONObject;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.json.impl.stanza.builder.StanzaBuilderSelector;
import com.openexchange.realtime.json.protocol.RTProtocol;
import com.openexchange.realtime.json.stanza.StanzaBuilder;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.util.StanzaSequenceGate;
import com.openexchange.tools.session.ServerSession;


/**
 * The {@link JSONProtocolHandler} maps incoming JSON encoded protocol messages to the appropriate methods on the protocol class
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class JSONProtocolHandler {
    
    private final RTProtocol protocol;
    private final StanzaSequenceGate gate;

    public JSONProtocolHandler(RTProtocol protocol, StanzaSequenceGate gate) {
        this.protocol = protocol;
        this.gate = gate;
    }
    
    
    public StanzaSequenceGate getGate() {
        return gate;
    }
    
    /**
     * Handles a list of messages, and calls the appropriate protocol messages
     * 
     * @param constructedId The ID for the client
     * @param serverSession His session
     * @param entry The stateEntry
     * @param stanzas the Stanzas
     * @param acknowledgements List for acknowledgements to use while handling incoming Stanzas
     * @throws RealtimeException 
     */
    public void handleIncomingMessages(ID constructedId, ServerSession serverSession, StateEntry entry, List<JSONObject> stanzas, List<Long> acknowledgements) throws RealtimeException {
        for (JSONObject json : stanzas) {
            if (json.has("type")) {
                String type = json.optString("type");
                
                if (type.equals("nextSequence")) {
                    protocol.nextSequence(constructedId, json.optInt("seq"), gate, entry.state);
                    continue;
                }
                
                if (type.equals("ping")) {
                    // PING received
                    protocol.ping(constructedId, json.optBoolean("commit"), entry.state, entry.transmitter);
                    continue;
                }

                if (type.equals("ack")) {
                    // ACK received
                    /*
                     * TODO: optimize this (e.g. client sends only the highest sequence number of a fully received sequence).
                     */
                    Object seqExpression = json.opt("seq");
                    if (seqExpression instanceof JSONArray) {
                        handleAcknowledgementArray(entry, (JSONArray) seqExpression);
                    } else if (seqExpression instanceof Number) {
                        protocol.acknowledgementReceived(Long.parseLong(seqExpression.toString()), entry.state);
                    }
                    continue;
                }
            }
            // Handle regular message
            StanzaBuilder<? extends Stanza> stanzaBuilder = StanzaBuilderSelector.getBuilder(constructedId, serverSession, json);
            Stanza stanza = stanzaBuilder.build();

            if (stanza.traceEnabled()) {
                stanza.trace("received in backend");
            }

            if (acknowledgements == null) {
                protocol.receivedMessage(stanza, gate, entry.state, entry.created, entry.transmitter);
            } else {
                protocol.receivedMessage(stanza, gate, entry.state, entry.created, entry.transmitter, acknowledgements);
            }

        }
    }

    private void handleAcknowledgementArray(StateEntry entry, JSONArray seqs) {
        for(int i = 0, length = seqs.length(); i < length; i++) {
            Object subExpression = seqs.opt(i);
            if (subExpression instanceof JSONArray) {
                handleAcknowledgementRange(entry, (JSONArray) subExpression);
            } else {
                protocol.acknowledgementReceived(Long.parseLong(subExpression.toString()), entry.state);
            }
        }
    }

    private void handleAcknowledgementRange(StateEntry entry, JSONArray range) {        
        for(long j = range.optLong(0), end = range.optLong(1); j <= end; j++) {
            protocol.acknowledgementReceived(j, entry.state);
        }
    }
}
