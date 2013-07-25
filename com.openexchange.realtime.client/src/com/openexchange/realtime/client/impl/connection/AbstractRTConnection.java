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

package com.openexchange.realtime.client.impl.connection;

import java.io.StringReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.Validate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.realtime.client.RTConnection;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.RTSession;
import com.openexchange.realtime.client.impl.config.ConfigurationProvider;

/**
 * {@link AbstractRTConnection}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractRTConnection implements RTConnection, RTProtocolCallback {

    private final static Logger LOG = LoggerFactory.getLogger(AbstractRTConnection.class);

    protected RTConnectionProperties connectionProperties;

    protected AtomicReference<Thread> delivererRef;

    protected ConcurrentMap<String, RTMessageHandler> messageHandlers;

    protected ResendBuffer resendBuffer;

    protected RTProtocol protocol;

    protected AtomicReference<RTSession> sessionRef;

    protected AtomicBoolean isActive = new AtomicBoolean(false);


    protected AbstractRTConnection() {
        super();
    }

    protected void init(RTConnectionProperties connectionProperties, RTMessageHandler messageHandler) throws RTException {
        Validate.notNull(connectionProperties, "ConnectionProperties are needed to create a new Connection");
        this.connectionProperties = connectionProperties;
        delivererRef = new AtomicReference<Thread>();
        messageHandlers = new ConcurrentHashMap<String, RTMessageHandler>();
        resendBuffer = new ResendBuffer(this);
        protocol = new RTProtocol(this);
        sessionRef = new AtomicReference<RTSession>();
        login(messageHandler);
        reconnect();
        isActive.set(true);
    }

    /*
     * RTConnection overrides
     */
    @Override
    public void registerHandler(String selector, RTMessageHandler messageHandler) throws RTException {
        registerHandler0(selector, messageHandler);
    }

    @Override
    public void unregisterHandler(String selector) {
        messageHandlers.remove(selector);
    }

    @Override
    public RTSession getOXSession() {
        RTSession session = sessionRef.get();
        if (session == null) {
            throw new IllegalStateException("The connection has not been successfully established.");
        }

        return session;
    }

    @Override
    public void send(JSONValue message) throws RTException {
        if (!isActive.get()) {
            LOG.warn("Connection is already closed. Message getting lost: " + message.toString());
            return;
        }

        if(message.isArray()) {
            Map<Long, JSONObject> addSequence = protocol.addSequence(message.toArray());
            for (Entry<Long, JSONObject> entry : addSequence.entrySet()) {
                resendBuffer.put(entry.getKey(), entry.getValue());
            }
        } else if (message.isObject()){
            Map<Long, JSONObject> addSequence = protocol.addSequence(message.toObject());
            Entry<Long, JSONObject> entry = addSequence.entrySet().iterator().next();
            resendBuffer.put(entry.getKey(), entry.getValue());
        } else {
            throw new RTException("jsonValue must be either JSONArray or JSONObject");
        }
        protocol.resetPingTimeout();
    }

    @Override
    public void post(JSONValue message) throws RTException {
        if (!isActive.get()) {
            LOG.warn("Connection is already closed. Message getting lost: " + message.toString());
            return;
        }

        doSend(message);
        protocol.resetPingTimeout();
    }

    @Override
    public void close() throws RTException {
        isActive.set(false);
        protocol.release();
        Thread deliverer = delivererRef.get();
        if (deliverer != null) {
            deliverer.interrupt();
            delivererRef.compareAndSet(deliverer, null);
        }

        resendBuffer.stop();
        logout();
        doClose();
    }

    /*
     * RTProtocolCallback overrides
     */
    @Override
    public void sendACK(JSONObject ack) throws RTException {
        if (!isActive.get()) {
            LOG.warn("Connection is already closed. Ack will not be sent: " + ack.toString());
            return;
        }

        doSendACK(ack);
    }

    @Override
    public void sendPing(JSONObject ping) throws RTException {
        if (!isActive.get()) {
            LOG.warn("Connection is already closed. Ping will not be sent: " + ping.toString());
            return;
        }

        doSendPing(ping);
    }

    @Override
    public void onTimeout() {
        try {
            LOG.info("PONG timeout. Reconnecting...");
            reconnect();
        } catch (RTException e) {
            throw new IllegalStateException("Could not reconnect after timeout.", e);
        }
    }

    @Override
    public void onSessionInvalid() {
        try {
            LOG.info("Received invalid session error. Reconnecting...");
            login(null);
            reconnect();
        } catch (RTException e) {
            throw new IllegalStateException("Could not reconnect after invalid session error.", e);
        }
    }

    @Override
    public void onAck(long seq) {
        resendBuffer.remove(seq);
    }

    /*
     * Abstract methods
     */
    protected abstract void reconnect() throws RTException;

    protected abstract void doSend(JSONValue message) throws RTException;

    protected abstract void doSendACK(JSONObject ack) throws RTException;

    protected abstract void doSendPing(JSONObject ping) throws RTException;

    protected abstract void doClose();

    /*
     * Convenience methods
     */
    /**
     * A convenience method for creating a valid OX session and registering a message handler
     * for the default selector.
     *
     * @param messageHandler A handler for messages that address the selector 'default'.
     * @return The newly created session.
     * @throws RTException
     */
    protected void login(RTMessageHandler messageHandler) throws RTException {
        if (messageHandler != null) {
            registerHandler0(ConfigurationProvider.getInstance().getDefaultSelector(), messageHandler);
        }

        sessionRef.set(Login.doLogin(
            connectionProperties.getSecure(),
            connectionProperties.getHost(),
            connectionProperties.getPort(),
            connectionProperties.getUser(),
            connectionProperties.getPassword()));
    }

    /**
     * Destroys the active user session and performs a logout on the server.
     */
    protected void logout() {
        if (sessionRef != null) {
            try {
                Login.doLogout(sessionRef.get(), connectionProperties.getSecure(), connectionProperties.getHost(), connectionProperties.getPort());
            } catch (RTException e) {
                LOG.warn("Error during logout request.", e);
            }

            sessionRef = null;
        }
    }

    /**
     * A convenience method for handling incoming messages. The message
     * will pass the {@link RTProtocol} and will be delivered immediately
     * if it doesn't contain a sequence number and the given {@link RTMessageHandler}
     * is not <code>null</code>.
     *
     * @param message The received message.
     * @param Whether the PONG timeout should be reset or not
     */
    protected void onReceive(String message, boolean resetPongTimeout) throws RTException {
        try {
            if (resetPongTimeout) {
                protocol.resetPongTimeout();
            }

            JSONValue json = JSONObject.parse(new StringReader(message));
            //Array or Object
            if(json.isArray()) {
                JSONArray stanzas = (JSONArray) json;
                Iterator<Object> stanzaIterator = stanzas.iterator();
                while (stanzaIterator.hasNext()) {
                    Object next = stanzaIterator.next();
                    if(!(next instanceof JSONObject)) {
                        throw new RTException("Array must only contain JSONObjects");
                    }
                    JSONObject stanza = (JSONObject) next;
                    RTMessageHandler handlerForSelector = getHandlerForSelector(stanza);
                    if (protocol.handleIncoming(stanza) && handlerForSelector != null) {
                        handlerForSelector.onMessage(stanza);
                    }
                }
            } else if(json.isObject()){
                JSONObject stanza = (JSONObject)json;
                RTMessageHandler handlerForSelector = getHandlerForSelector(stanza);
                if (protocol.handleIncoming(stanza) && handlerForSelector != null) {
                    handlerForSelector.onMessage(stanza);
                }
            } else {
                throw new RTException("Messages must consist of a single JSONObject or an JSONArray of JSONObjects");
            }

        } catch (JSONException e) {
            throw new RTException("The given string was not a valid JSON message: " + message, e);
        }
    }

    /**
     * A convenience method for handling incoming messages. The message
     * will pass the {@link RTProtocol} and will be delivered immediately
     * if it doesn't contain a sequence number and the given {@link RTMessageHandler}
     * is not <code>null</code>.
     *
     * @param message The received message.
     * @param Whether the PONG timeout should be reset or not
     */
    protected void onReceive(JSONObject message, boolean resetPongTimeout) throws RTException {
        if (resetPongTimeout) {
            protocol.resetPongTimeout();
        }

        RTMessageHandler handlerForSelector = getHandlerForSelector(message);
        if (protocol.handleIncoming(message) && handlerForSelector != null) {
            handlerForSelector.onMessage(message);
        }
    }

    protected void registerHandler0(String selector, RTMessageHandler messageHandler) throws RTException {
        if (messageHandlers.putIfAbsent(selector, messageHandler) != null) {
            throw new RTException("There is already a mapping selector <-> messageHandler for the selector: " + selector);
        }

        if (delivererRef.get() == null) {
            MessageDeliverer deliverer = new MessageDeliverer(messageHandlers);
            Thread delivererThread = new Thread(deliverer);
            if (delivererRef.compareAndSet(null, delivererThread)) {
                delivererThread.start();
                protocol.setSequenceGate(deliverer.getSequenceGate());
            }
        }
    }

    /**
     * Get the proper handler for the selector found in the message.
     * @param message The message to be delivered to a @{link RTMessageHandler}
     * @return the @{link RTMessageHandler} associated with the selector found in the message or null
     */
    protected RTMessageHandler getHandlerForSelector(JSONObject message) {
        RTMessageHandler associatedHandler = null;
        String selector = message.optString("selector");
        if(selector != null) {
            associatedHandler = messageHandlers.get(selector);
        }
        return associatedHandler;
    }

}
