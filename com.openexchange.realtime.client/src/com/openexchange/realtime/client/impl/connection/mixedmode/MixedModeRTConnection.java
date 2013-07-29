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

package com.openexchange.realtime.client.impl.connection.mixedmode;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.ReplayDecoder;
import org.atmosphere.wasync.impl.AtmosphereClient;
import org.atmosphere.wasync.impl.DefaultOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.base.Strings;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;
import com.ning.http.client.Response;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.impl.connection.AbstractRTConnection;
import com.openexchange.realtime.client.impl.connection.Constants;
import com.openexchange.realtime.client.impl.connection.RequestBuilderHelper;

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
 *         <li> Send a ping into a room to verify that you didn't leave the room without a proper leave message
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

    private org.atmosphere.wasync.Request request;

    private org.atmosphere.wasync.Socket socket;

    //Data received by these clients has to be handled by super.onReceive(). Post reliable may not return before
    private AsyncHttpClient asyncHttpClient;

    // Data received by this client should delegate the data to RTProtocol.handleIncoming(JSONObject)
    private AtmosphereClient atmosphereClient;

    public MixedModeRTConnection(AsyncHttpClient asyncHttpClient, RTConnectionProperties connectionProperties, RTMessageHandler messageHandler) throws RTException {
        super();
        this.asyncHttpClient = asyncHttpClient;
        atmosphereClient = new AtmosphereClient();
        init(connectionProperties, messageHandler);
    }

    @Override
    protected void reconnect() throws RTException {
        if (socket != null) {
            socket.close();
        }

        request = RequestBuilderHelper.newAtmosphereRequestBuilder(atmosphereClient, connectionProperties, getOXSession())
            .encoder(new Encoder<JSONObject, String>() {
                @Override
                public String encode(final JSONObject jsonObject) {
                    return jsonObject.toString();
                }
            })
            /*
             * Message might span several packages/chunks
             */
            .decoder(new ReplayDecoder() {
                private StringBuilder messageBuilder = new StringBuilder(1024);
                private final List<String> EMPTY = Collections.emptyList();
                private boolean wasLastMesageComplete = true;

                @Override
                public List<String> decode(Event event, String received) {
                    if(event.equals(Event.MESSAGE)) {
                        if(received.endsWith("}") || received.endsWith("}]")) { //either complete message or we are completing a chunked one
                            if(wasLastMesageComplete) {
                                List<String> messages = Collections.singletonList(received);
                                return messages;
                            } else { //we are completing a chunked message
                                messageBuilder.append(received);
                                List<String> messages = Collections.singletonList(messageBuilder.toString());
                                messageBuilder.delete(0, messageBuilder.length());
                                wasLastMesageComplete = true;
                                if (LOG.isTraceEnabled()) {
                                    LOG.trace("Completed message in atmosphere decoder: " + received);
                                }
                                return messages;
                            }
                        } else { // incoming message wasn't complete
                            messageBuilder.append(received);
                            wasLastMesageComplete = false;
                            if (LOG.isTraceEnabled()) {
                                LOG.trace("Received incompleted message in atmosphere decoder: " + received);
                            }
                            return EMPTY;
                        }
                    }
                    return EMPTY;
                }
            })
            .build();
        List<Decoder<?,?>> decoders = request.decoders();
        decoders.remove(0);
        decoders.remove(0);

        socket = atmosphereClient.create(
            (DefaultOptions) atmosphereClient.newOptionsBuilder().runtime(asyncHttpClient, true).reconnect(true).build()
        );
        try {
            socket.on(Event.MESSAGE.name(), new Function<String>() {

                @Override
                public void on(String received) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Received message in atmosphere channel: " + received);
                    }

                    try {
                        onReceive(received, true);
                    } catch (RTException rtException) {
                        LOG.error("Error in handling the incoming object.", rtException);
                    }
                }
            });
            socket.open(request);
        } catch (IOException e) {
            throw new RTException("Could not open return channel.", e);
        }
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
    protected void doSend(JSONObject message) throws RTException {
        if(isQueryAction(message)) {
            fireQueryRequest(message);
        } else if(isSendAction(message)) {
            fireSendRequest(message);
        } else {
            throw new RTException("Couldn't determine the type of message to send");
        }
    }

    @Override
    public void doSendACK(JSONObject ack) throws RTException {
        fireSendRequest(ack);
    }

    @Override
    public void doSendPing(JSONObject ping) throws RTException {
        fireAtmosphereRequest(ping);
    }

    @Override
    protected void doClose() {
        if (socket != null) {
            socket.close();
        }
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
        }
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
        }
        return false;
    }

    private void fireQueryRequest(JSONValue jsonValue) throws RTException {
        RequestBuilder queryRequestBuilder = RequestBuilderHelper.newQueryRequest(connectionProperties, getOXSession());
        queryRequestBuilder.setBody(jsonValue.toString());
        Request request = queryRequestBuilder.build();
        fireSynchronousRequest(request);
    }

    private void fireSendRequest(JSONValue jsonValue) throws RTException {
        RequestBuilder builder = RequestBuilderHelper.newSendRequest(connectionProperties, getOXSession());
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
            //the ox http api wraps the response stanzas into a data : {} object, try to unwrap
            String responseBody = response.getResponseBody();
            if(!Strings.isNullOrEmpty(responseBody)) {
                try {
                    JSONObject body = new JSONObject(responseBody);
                    Object data = body.opt("data");
                    if(data != null) {
                        JSONObject jsonData = (JSONObject)data;
                        onReceive(jsonData , false);
                    } else {
                        onReceive(responseBody , false);
                    }
                } catch (JSONException e) {
                    LOG.error("Response wasn't valid JSON: {}", responseBody);
                    throw new RTException("Response wasn't valid JSON");
                }
            }
        } catch (Exception e) {
            LOG.error("Exception while executing send request.", e);
            throw new RTException("Exception while executing send request.", e);
        }
    }

    /**
     * Send asynchronous messages to the server.
     *
     * @param jsonValue - JSONValue with the JSON which should be sent to the server
     */
    private void fireAtmosphereRequest(JSONValue jsonValue) {
        try {
            socket.fire(jsonValue);
        } catch (IOException ioException) {
            LOG.info("Unable to fire ping request. Try again with the next iteration");
        }
    }
}
