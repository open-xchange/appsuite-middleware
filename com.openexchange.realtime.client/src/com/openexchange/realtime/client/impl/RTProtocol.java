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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.realtime.client.impl;

import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.realtime.client.RTConnection;
import com.openexchange.realtime.client.RTException;


/**
 * {@link RTProtocol}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class RTProtocol {
    
    private static final Logger LOG = LoggerFactory.getLogger(RTProtocol.class);

    private static final long PING_TIME = 60000L;

    private final RTProtocolCallback callback;

    private final SequenceGate gate;

    private final PingPongTimer pingPongTimer;

    private final Thread pingPongTimerThread;
    
    private final SequenceGenerator sequenceGenerator;

    /**
     * Initializes a new {@link RTProtocol}.
     * The protocol takes care about necessary PINGs to signal the server that the client
     * is still available. Additionally it produces ACKs for incoming messages.
     *
     * @param callback A callback to let the connection react to events and to allow the protocol to send
     * messages like ACKs and PINGs.
     * @param gate The sequence gate to ensure the order of incoming messages.
     */
    public RTProtocol(RTProtocolCallback callback, SequenceGate gate) {
        super();
        this.callback = callback;
        this.gate = gate;
        pingPongTimer = new PingPongTimer(callback, PING_TIME, true);
        pingPongTimerThread = new Thread(pingPongTimer);
        pingPongTimerThread.start();
        sequenceGenerator = new SequenceGenerator();
    }

    /**
     * Analyzes an incoming JSON message and handles PONGs or sends necessary ACKs to the server.
     * If the message contains a valid sequence number and contains more than protocol overhead
     * it will be enqueued in the given {@link SequenceGate}.
     *
     * @param jsonValue The JSON message.
     * @return <code>true</code> if the message can be delivered directly. That may be the case for 
     * messages without sequence number. If the message was enqueued in the sequence gate <code>false</code>
     * will be returned.
     */
    public boolean handleIncoming(JSONValue jsonValue) throws RTException {
        long seq = -1;
        JSONObject element;
        if (jsonValue.isArray()) {
            JSONArray arr = jsonValue.toArray();
            try {
                element = arr.getJSONObject(0);
            } catch (JSONException e) {
                throw new RTException("The received JSONArray did not contain a valid JSONObject", e);
            }
        } else {
            element = jsonValue.toObject();
        }

        try {
            if (element.hasAndNotNull("seq")) {
                seq = element.getLong("seq");
            }
        } catch (JSONException e) {
            throw new RTException("The contained sequence number '" + element.opt("seq") + "' is not a valid long.", e);
        }
        //TODO: Can jsonValue contain several Stanzas with sequencenumbers?
        try {
            if (parseElement(jsonValue.toObject())) {
                if (seq < 0) {
                    return true;
                } else if (gate != null) {
                    if (gate.enqueue(jsonValue, seq)) {
                        /*
                         * {"type":"ack","seq":["0"]}
                         */
                        JSONArray seqNums = new JSONArray();
                        seqNums.put(seq);
                        JSONObject ack = new JSONObject();
                        ack.put("type", "ack");
                        ack.put("seq", seqNums);
                        callback.sendACK(ack);
                    }
                }
            }

            return false;
        } catch (JSONException e) {
            throw new RTException("The received message could not be parsed.", e);
        }
    }

    /**
     * Let the {@link RTConnection} reset the PING timeout. That means if the connection just
     * sent a message to the server the next PING can be delayed to avoid unnecessary traffic.
     */
    public void resetPingTimeout() {
        resetPingTimer();
    }
    
    /**
     * To reliably send messages to the server we have to make use of numeric sequences in every message that gets sent from the client to
     * the server. The Server uses these sequences to ensure the sequence of incoming messages. The client waits for acknowledges for every
     * sequence number to ensure successful delivery of messages. This method takes a JSONArray of messages and adds a sequence number to
     * every single one of them.
     * 
     * @param messages The array containing the messages that need to be enhanced with sequence numbers 
     * @return An array containing the messages that are enhanced with sequence numbers
     * @throws RTException if the messages array doesn't consist of JSONObjects
     */
    public JSONArray addSequence(JSONArray messages) throws RTException {
        JSONArray sequencedMessages = new JSONArray();
        Iterator<Object> iterator = messages.iterator();
        while(iterator.hasNext()) {
            Object message = iterator.next();
            if(!(message instanceof JSONObject)) {
                throw new RTException("Only JSONObjects are allowed in the messages array");
            }
            JSONObject sequencedMessage = addSequence((JSONObject)message);
            sequencedMessages.put(sequencedMessage);
        }
        return sequencedMessages;
    }
    
    /**
     * To reliably send messages to the server we have to make use of numeric sequences in every message that gets sent from the client to
     * the server. The Server uses these sequences to ensure the sequence of incoming messages. The client waits for acknowledges for every
     * sequence number to ensure successful delivery of messages. This method takes a message as JSONObject and adds a sequence number to
     * it.
     * 
     * @param message The messages that needs to be enhanced with a sequence number 
     * @return The messages that is enhanced with a sequence number
     */
    public JSONObject addSequence(JSONObject message) {
        long sequence;
        JSONObject sequencedMessage=null;
        try {
            sequence = sequenceGenerator.nextSequence();
            sequencedMessage = message.put("seq", sequence);
            return sequencedMessage;
        } catch (RTException rte) {
            // TODO: Implement reset of sequence numbers on the server side if a user should really manage to exhaust 2^63-1 sequences.
            LOG.error("Error while adding sequence to message object", rte);
        } catch (JSONException je) {
            //Can't happen
        }
        return sequencedMessage;
    }

    /**
     * Releases all used resources.
     */
    public void release() {
        pingPongTimerThread.interrupt();
    }

    /**
     * Parse an element for for a pong message from the server.
     * @param element the element to parse
     * @return false if the message contains only a pong as payload, true otherwise
     * @throws JSONException
     */
    private boolean parseElement(JSONObject element) throws JSONException {
        if (isMessage(element) && element.has("payloads")) {
            /*
             * This might be a pong. We have to look for it in the payload tree.
             */
            JSONArray payloads = element.getJSONArray("payloads");
            for (int j = 0; j < payloads.length(); j++) {
                JSONObject payload = payloads.getJSONObject(j);
                if (isPong(payload)) {
                    pingPongTimer.onPong();
                    if (payloads.length() == 1) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private void resetPingTimer() {
        pingPongTimer.resetTimer();
    }


    private boolean isMessage(JSONObject element) throws JSONException {
        return element.hasAndNotNull("element") && element.get("element").equals("message");
    }

    private boolean isPong(JSONObject payload) throws JSONException {
        return
            payload.hasAndNotNull("namespace")
            && payload.get("namespace").equals("atmosphere")
            && payload.hasAndNotNull("element") 
            && payload.get("element").equals("pong");
    }

}
