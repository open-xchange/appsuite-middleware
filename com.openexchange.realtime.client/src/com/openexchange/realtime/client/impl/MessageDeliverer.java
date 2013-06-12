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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;


/**
 * A {@link MessageDeliverer} listens on a sequence gate for incoming messages
 * and uses the given message handler to deliver them.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class MessageDeliverer implements Runnable {

    private final static Logger LOG = LoggerFactory.getLogger(MessageDeliverer.class);
    
    private final ConcurrentHashMap<String, RTMessageHandler> messageHandlers;

    private final SequenceGate gate;

    /**
     * Initializes a new {@link MessageDeliverer}.
     * @param messageHandler
     * @param gate
     */
    public MessageDeliverer(ConcurrentHashMap<String, RTMessageHandler> messageHandlers, SequenceGate gate) {
        super();
        this.messageHandlers = messageHandlers;
        this.gate = gate;
    }

    /* (non-Javadoc)
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run() {
        while (true) {
            if (Thread.interrupted()) {
                return;
            }

            try {
                List<JSONValue> messages = gate.take();
                for (JSONValue message : messages) {
                    if(!message.isObject()) {
                        LOG.error("The JSONValue did not contain a valid JSONObject:" + message.toString());
                    } else {
                        RTMessageHandler handlerForSelector = getHandlerForSelector(message.toObject());
                        if(handlerForSelector == null) {
                            LOG.warn("Couldn't find handler for message, discarding: "+ message.toString());
                        } else {
                            handlerForSelector.onMessage(message);
                        }
                    }
                }
            } catch (InterruptedException e) {
                return;
            } catch (Throwable t) {
                LOG.error("Exception during MessageDelivery run", t);
            }
        }
    }

    /**
     * Get the proper handler for the selector found in the message.
     * @param message The message to be delivered to a @{link RTMessageHandler}
     * @return the @{link RTMessageHandler} associated with the selector found in the message or null
     */
    private RTMessageHandler getHandlerForSelector(JSONObject message) {
        RTMessageHandler associatedHandler = null;
        String selector = message.optString("selector");
        if(selector != null) {
            associatedHandler = messageHandlers.get(selector);
        }
        return associatedHandler;
    }


}
