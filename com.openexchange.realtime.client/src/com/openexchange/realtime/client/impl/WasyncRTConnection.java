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

package com.openexchange.realtime.client.impl;

import org.apache.commons.lang.Validate;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ning.http.client.AsyncHttpClient;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.RTUserState;
import com.openexchange.realtime.client.RTUserStateChangeListener;

/**
 * {@link WasyncRTConnection}
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class WasyncRTConnection extends AbstractRTConnection {

    private static final Logger LOG = LoggerFactory.getLogger(WasyncRTConnection.class);
    
    //Data received by these clients has to be handled by super.onReceive(). Post reliable may not return before 
    private AsyncHttpClient asyncHttpClient;
    private AtmosphereClient atmosphereClient;
    
    SequenceGenerator sequenceGenerator = new SequenceGenerator();
    
    public WasyncRTConnection(RTConnectionProperties connectionProperties) {
        super(connectionProperties);
        asyncHttpClient = new AsyncHttpClient();
        atmosphereClient = new AtmosphereClient();
    }
    
    @Override
    public RTUserState connect(RTMessageHandler messageHandler, RTUserStateChangeListener changeListener) throws RTException {
        RTUserState rtUserState = super.connect(messageHandler, changeListener);
        
        return rtUserState;
    }


    /*
     * 1. Decide which client to use based on the message
     *   /api/rt
     *     send
     *       acks
     *       messages
     *       ping groupdispatcher
     *     query
     *       join room: {"element":"message","selector":"rt-group-0","payloads":[{"element":"command","namespace":"group","data":"join"}],"to":"synthetic.office://operations/33341.27381","seq":0}
     *       leave room: {"element":"message","payloads":[{"element":"command","namespace":"group","data":"leave"}],"to":"synthetic.office://operations/33341.27381","seq":2}
     * 2. Send message 
     */
    @Override
    public void post(JSONValue message) throws RTException {
        resetPingTimer();
    }

    @Override
    public void postReliable(JSONValue message) throws RTException {
        resetPingTimer();
        
        /*
         * - Get next sequence number from SequenceGenerator in protocol
         * - Post
         * - Return when put returns 
         */
        
    }
    
    /**
     * We are sending a message so there is no need for a keepalive ping 
     */
    private void resetPingTimer() {
        //protocol.resetPingTimer()
    }
    
    /*
     *query
     *  join room: {"element":"message","selector":"rt-group-0","payloads":[{"element":"command","namespace":"group","data":"join"}],"to":"synthetic.office://operations/33341.27381","seq":0}
     *  leave room: {"element":"message","payloads":[{"element":"command","namespace":"group","data":"leave"}],"to":"synthetic.office://operations/33341.27381","seq":2}
     */
    private boolean isQueryAction(JSONValue json) {
        boolean isQuery = false;
        // query actions consist of a single json object
        if(json.isObject()) {
            JSONObject object = json.toObject();
            JSONArray payloads = object.optJSONArray("payloads");
            if(payloads != null) {
                Validate.isTrue(payloads.length() == 1, "Queries must only consist of one payload element");
            }
            JSONObject command = (JSONObject) payloads.opt(0);
            String commandData = command.optString("data");
            if("join".equalsIgnoreCase(commandData) || "leave".equalsIgnoreCase(commandData)) {
                isQuery=true;
            }
        }
        return isQuery;
    }
    
    /*
     * /api/rt
     *  send
     *    acks
     *    messages
     *    ping groupdispatcher
     */
    private boolean isSendAction(JSONValue json) {
        boolean isSend = false;
        return isSend;
    }
    
    private boolean isAtmosphereRequest() {
        return false;
    }
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.client.impl.RTProtocolCallback#sendACK(org.json.JSONObject)
     */
    @Override
    public void sendACK(JSONObject ack) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see com.openexchange.realtime.client.impl.RTProtocolCallback#sendPing(org.json.JSONObject)
     */
    @Override
    public void sendPing(JSONObject ping) {
        // TODO Auto-generated method stub
        
    }

}
