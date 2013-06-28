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
import java.util.concurrent.ConcurrentHashMap;
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

    protected final RTConnectionProperties connectionProperties;

    protected final AtomicReference<Thread> delivererRef;

    protected final ConcurrentHashMap<String, RTMessageHandler> messageHandlers;

    protected final RTProtocol protocol;

    protected RTSession session;

    //did we successfully login to the backend to receive a serversession?
    protected boolean loggedIn = false;

    //did we establish a "duplex" connection to the backend?
    protected  boolean isConnected = false;


    protected AbstractRTConnection(RTConnectionProperties connectionProperties) {
        super();
        Validate.notNull(connectionProperties, "ConnectionProperties are needed to create a new Connection");
        this.connectionProperties = connectionProperties;
        delivererRef = new AtomicReference<Thread>();
        messageHandlers = new ConcurrentHashMap<String, RTMessageHandler>();
        protocol = new RTProtocol(this);
    }

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
        if (session == null || !loggedIn) {
            throw new IllegalStateException();
        }

        return session;
    }

    @Override
    public void close() throws RTException {
        protocol.release();
        Thread deliverer = delivererRef.get();
        if (deliverer != null) {
            deliverer.interrupt();
            delivererRef.compareAndSet(deliverer, null);
        }

        logout();
    }

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

        if (!loggedIn) {
            session = Login.doLogin(
                connectionProperties.getSecure(),
                connectionProperties.getHost(),
                connectionProperties.getPort(),
                connectionProperties.getUser(),
                connectionProperties.getPassword());
            loggedIn = true;
        } else {
            LOG.info("User is already logged in. Returning existing user state.");
        }
    }

    /**
     * Destroys the active user session and performs a logout on the server.
     */
    protected void logout() {
        if (loggedIn && session != null) {
            try {
                Login.doLogout(session, connectionProperties.getSecure(), connectionProperties.getHost(), connectionProperties.getPort());
            } catch (RTException e) {
                LOG.warn("Error during logout request.", e);
            }

            loggedIn = false;
            session = null;
        }
    }

    /**
     * A convenience method for handling incoming messages. The message
     * will pass the {@link RTProtocol} and will be delivered immediately
     * if it doesn't contain a sequence number and the given {@link RTMessageHandler}
     * is not <code>null</code>.
     *
     * @param message The received message.
     */
    protected void onReceive(String message) throws RTException {
        try {
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
            throw new RTException("The given string was not a valid JSON message.", e);
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
