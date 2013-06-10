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

import java.io.StringReader;
import org.apache.commons.lang.Validate;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.realtime.client.RTConnection;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.RTUserState;
import com.openexchange.realtime.client.RTUserStateChangeListener;

/**
 * {@link AbstractRTConnection}
 * 
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public abstract class AbstractRTConnection implements RTConnection, RTProtocolCallback {

    protected final RTConnectionProperties connectionProperties;

    protected RTProtocol protocol;

    protected SequenceGate gate;

    protected RTMessageHandler messageHandler;

    protected RTUserStateChangeListener changeListener;

    protected RTUserState userState;

    protected boolean loggedIn = false;

    private Thread deliverer;


    protected AbstractRTConnection(RTConnectionProperties connectionProperties) {
        super();
        Validate.notNull(connectionProperties, "ConnectionProperties are needed to create a new Connection");
        this.connectionProperties = connectionProperties;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.realtime.client.RTConnection#connect(com.openexchange.realtime.client.RTMessageHandler)
     */
    @Override
    public RTUserState connect(RTMessageHandler messageHandler) throws RTException {
        return connect(messageHandler, new RTUserStateChangeListener() {
            @Override
            public void setUserState(RTUserState state) {
            }
        });
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.client.RTConnection#connect(com.openexchange.realtime.client.RTMessageHandler, com.openexchange.realtime.client.RTUserStateChangeListener)
     */
    @Override
    public RTUserState connect(RTMessageHandler messageHandler, RTUserStateChangeListener changeListener) throws RTException {
        this.messageHandler = messageHandler;
        this.changeListener = changeListener;
        if (messageHandler != null) {
            gate = new SequenceGate();
            deliverer = new Thread(new MessageDeliverer(messageHandler, gate));
            deliverer.start();
        }

        protocol = new RTProtocol(this, gate);
        userState = Login.doLogin(
            connectionProperties.getSecure(),
            connectionProperties.getHost(),
            connectionProperties.getPort(),
            connectionProperties.getUser(),
            connectionProperties.getPassword());
        loggedIn = true;
        return userState;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.realtime.client.RTConnection#close()
     */
    @Override
    public void close() throws RTException {
        protocol.release();

        if (deliverer != null) {
            deliverer.interrupt();
            deliverer = null;
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
            if (protocol.handleIncoming(json) && messageHandler != null) {
                messageHandler.onMessage(json);
            }
        } catch (JSONException e) {
            throw new RTException("The given string was not a valid JSON message.", e);
        }
    }
}
