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

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.commons.lang.Validate;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.openexchange.realtime.client.Constants;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.RTUserState;
import com.openexchange.realtime.client.RTUserStateChangeListener;
import com.openexchange.realtime.client.user.RTUser;

/**
 * {@link MixedModeRTConnection} This Connection class is needed to communicate with the realtime interfaces of our backend after the
 * refactoring in 05.13.
 * 
 * It's a mixed mode connection because we use two different types of communication here.
 * 
 * <ol>
 * <li> Synchronous calls to the api/rt interface
 *   <ol>
 *     <li> Query actions
 *       <ol>
 *         <li> Join a room via PUT
 *           <pre>
 *           {
 *              "payloads": [
 *               {
 *                 "namespace": "group",
 *                 "data": "join",
 *                 "element": "command"
 *               }
 *             ],
 *             "element": "message",
 *             "selector": "chineseRoomSelector",
 *             "to": "synthetic.china://room1"
 *           }
 *           </pre>
 *         <li> Leave a room via PUT
 *         <pre>
 *         {
 *           "payloads": [
 *             {
 *               "namespace": "group",
 *               "data": "leave",
 *               "element": "command"
 *             }
 *           ],
 *           "element": "message",
 *           "to": "synthetic.china://room1"
 *         }
 *         </pre>
 *       </ol>
 *     <li> Send actions
 *       <ol>
 *         <li> Send an acknowledgement
 *           <pre>
 *           {
 *             "seq": [
 *               "0"
 *             ],
 *             "type": "ack"
 *           }
 *           </pre>
 *         <li> Send a ping into a room to verify that you didn't leve the room without a proper leave message
 *           <pre>
 *           [
 *             {
 *               "payloads": [
 *                 {
 *                   "data": 1,
 *                   "namespace": "group",
 *                   "element": "ping"
 *                 }
 *               ],
 *               "to": "synthetic.china://room1",
 *               "element": "message"
 *             }
 *           ]
 *           </pre>
 *         <li> Generally send messages to the server, e.g. say something into a room
 *         <pre>
 *         {
 *           "payloads": [
 *             {
 *               "data": "say",
 *               "element": "action"
 *             },
 *             {
 *               "namespace": "china",
 *               "data": "Hello World",
 *               "element": "message"
 *             }
 *           ],
 *           "seq": 0,
 *           "element": "message",
 *           "to": "synthetic.china://room1"
 *         }
 *         </pre>
 *       </ol>
 *   </ol>
 * <li> Asynchronous call(back)s to the atmosphere/rt interface
 *   <ol>
 *     <li> Send pings via POST to keep the long polling connection alive
 *       <pre>
 *       {
 *         "commit": true,
 *         "type": "ping"
 *       }
 *       </pre>
 *     <li> Receive message from the server e.g. from Chatrooms that you are a member of via a long running GET
 *   <ol>
 * </ol>
 * 
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class MixedModeRTConnection extends AbstractRTConnection {

    private static final Logger LOG = LoggerFactory.getLogger(MixedModeRTConnection.class);
    
    //Data received by these clients has to be handled by super.onReceive(). Post reliable may not return before 
    private AsyncHttpClient asyncHttpClient;
    private AtmosphereClient atmosphereClient;
    
    SequenceGenerator sequenceGenerator = new SequenceGenerator();
    
    public MixedModeRTConnection(RTConnectionProperties connectionProperties) {
        super(connectionProperties);
        asyncHttpClient = new AsyncHttpClient();
        atmosphereClient = new AtmosphereClient();
    }
    
    @Override
    public RTUserState connect( String selector, RTMessageHandler messageHandler) throws RTException {
        RTUserState rtUserState = super.connect(selector, messageHandler);
        //connect to atmosphere
        
        return rtUserState;
    }

    /* atmosphere ping pong
     * {"type": "ping", "commit": true }
     * [{"selector":"default","element":"message","payloads":[{"element":"pong","data":"1","namespace":"atmosphere"}],"from":"ox://marc.arens@premium/68ef1855-242c-cbc4-b7aa-0b2c9738b6bb"}]
     * 
     * group ping
     * [{"element":"message","to":"synthetic.china://room1","payloads":[{"element":"ping","namespace":"group","data":1}]}]
     * {"data":{"acknowledgements":[]}}
     */
    @Override
    public void post(JSONValue value) throws RTException {
        doPost(value);
    }

    @Override
    public void postReliable(JSONValue jsonValue) throws RTException {
        JSONValue sequencedPayload = null;
        if(jsonValue.isArray()) {
            sequencedPayload = protocol.addSequence(jsonValue.toArray());
        } else if (jsonValue.isObject()){
            sequencedPayload = protocol.addSequence(jsonValue.toObject());
        } else {
            throw new RTException("jsonValue must be either JSONArray or JSONObject");
        }
        doPost(sequencedPayload);
        protocol.resetPingTimeout();
    }
    
    private void doPost(JSONValue message) throws RTException {
        if(isQueryAction(message)) {
            fireQueryRequest(message);
        } else if(isSendAction(message)) {
            fireSendRequest(message);
        } else {
            throw new RTException("Couldn't determine the type of message to send");
        }
    }
    
    @Override
    public void sendACK(JSONObject ack) throws RTException {
        fireSendRequest(ack);
    }

    @Override
    public void sendPing(JSONObject ping) throws RTException {
        fireAtmosphereRequest(ping);
    }
    

    private boolean isQueryAction(JSONValue json) {
        // query actions consist of a single json object
        if(json.isObject()) {
            JSONObject object = json.toObject();
            //Queries consist of only one payload element
            JSONArray payloads = object.optJSONArray("payloads");
            if(payloads != null) {
                if (payloads.length() != 1) {
                    return false;
                }
                JSONObject command = (JSONObject) payloads.opt(0);
                String commandData = command.optString("data");
                if("join".equalsIgnoreCase(commandData) || "leave".equalsIgnoreCase(commandData)) {
                    return true;
                }
            }
        }
        return false;
    }
    

    private boolean isSendAction(JSONValue json) {
        if (json.isObject()) {
            // ack or single message
            JSONObject object = json.toObject();
            String type = object.optString("type");
            String element = object.optString("element");
            if ("ack".equalsIgnoreCase(type)) {
                return true;
            } else if ("message".equalsIgnoreCase(element)) {
                return true;
            }
            return false;
        } else {
            if (json.isArray()) {
                JSONArray jsonArray = json.toArray();
                if (jsonArray.length() == 1) {
                    // group ping?
                    Object stanzaObject = jsonArray.opt(0);
                    if (!(stanzaObject instanceof JSONObject)) {
                        return false;
                    }
                    JSONObject stanza = (JSONObject) stanzaObject;
                    if (!"message".equalsIgnoreCase(stanza.optString("element"))) {
                        return false;
                    }
                    Object payloadsObject = stanza.opt("payloads");
                    if (!(payloadsObject instanceof JSONArray)) {
                        return false;
                    }
                    JSONArray payloadsArray = (JSONArray) payloadsObject;
                    if (payloadsArray.length() != 1) {
                        return false;
                    }
                    Object payloadObject = payloadsArray.opt(0);
                    if (!(payloadObject instanceof JSONObject)) {
                        return false;
                    }
                    JSONObject payload = (JSONObject) payloadObject;
                    String element = payload.optString("element");
                    String namespace = payload.optString("namespace");
                    if ("ping".equalsIgnoreCase(element) && "group".equalsIgnoreCase(namespace)) {
                        return true;
                    }
                    return false;
                } else if (jsonArray.length() > 1) {
                    // message?
                    Object stanzaObject = jsonArray.opt(0);
                    if (!(stanzaObject instanceof JSONObject)) {
                        return false;
                    }
                    JSONObject stanza = (JSONObject) stanzaObject;
                    if ("message".equalsIgnoreCase(stanza.optString("element"))) {
                        return true;
                    }
                    return false;
                }
                return false;
            } else {
                return false;
            }
        }
    }
    
    /*
     * atmosphere ping: {"type": "ping", "commit": true }
     */
    private boolean isAtmosphereRequest(JSONValue json) {
        if (json.isObject()) {
            // atmosphere ping?
            JSONObject object = json.toObject();
            String type = object.optString("type");
            if ("ping".equalsIgnoreCase(type)) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private void fireQueryRequest(JSONValue jsonValue) throws RTException {
        RequestBuilder queryRequestBuilder = RTRequestBuilderHelper.newQueryRequest(connectionProperties, userState);
        queryRequestBuilder.setBody(jsonValue.toString());
        Request request = queryRequestBuilder.build();
        fireSynchronousRequest(request);
    }

    private void fireSendRequest(JSONValue jsonValue) throws RTException {
        RequestBuilder builder = RTRequestBuilderHelper.newSendRequest(connectionProperties, userState);
        builder.setBody(jsonValue.toString());
        Request request = builder.build();
        fireSynchronousRequest(request);
    }

    private void fireSynchronousRequest(Request request) throws RTException {
        try {
            ListenableFuture<Response> requestFuture = asyncHttpClient.executeRequest(request);
            Response response = requestFuture.get(Constants.REQUEST_TIMEOUT, TimeUnit.SECONDS);
            if(response.getStatusCode()!= 200) {
                throw new RTException("Expected a HTTP status code but got: " + response.getStatusCode());
            }
            JSONObject jsonResponse = new JSONObject(response.getResponseBody());
            if(jsonResponse.optString("error") != "") {
                throw new RTException("Request caused server side error: " + jsonResponse.toString());
            }
            protocol.handleIncoming(jsonResponse);
        } catch (Exception e) {
            LOG.error("Exception while executing send request.", e);
            throw new RTException("Exception while executing send request.", e);
        }
    }

    private void fireAtmosphereRequest(JSONValue jsonValue) {
        
    }

    @Override
    public void removeHandler(String selector) {
        messageHandlers.remove("selector");
    }

}
