/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.pns.appsuite;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.Client;
import com.openexchange.exception.OXException;
import com.openexchange.pns.JsonMessage;
import com.openexchange.pns.KnownTopic;
import com.openexchange.pns.KnownTransport;
import com.openexchange.pns.Message;
import com.openexchange.pns.PushExceptionCodes;
import com.openexchange.pns.PushMessageGenerator;
import com.openexchange.pns.PushNotification;


/**
 * {@link AppSuiteWebSocketMessageGenerator} - The message generator for App Suite UI.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class AppSuiteWebSocketMessageGenerator implements PushMessageGenerator {

    private static final String TRANSPORT_ID_WEB_SOCKET = KnownTransport.WEB_SOCKET.getTransportId();

    private static final String DEFAULT_NAMESPACE = "/";

    private static interface ArgsGenerator {

        /**
         * Provides the JSON-compatible arguments to append to a Socket.IO message.
         *
         * @param notification The notification from which to create the arguments
         * @return The arguments
         * @throws OXException If generating the arguments fails
         */
        Collection<Object> generateArgsFrom(PushNotification notification) throws OXException;

    }

    // ------------------------------------------------------------------------------------------------------------------------

    private final Map<String, ArgsGenerator> generators;

    /**
     * Initializes a new {@link AppSuiteWebSocketMessageGenerator}.
     */
    public AppSuiteWebSocketMessageGenerator() {
        super();

        Map<String, ArgsGenerator> generators = new HashMap<>(16);
        generators.put(KnownTopic.MAIL_NEW.getName(), new ArgsGenerator() {

            @Override
            public Collection<Object> generateArgsFrom(PushNotification notification) throws OXException {
                Map<String, Object> messageData = notification.getMessageData();
                // Socket.io compatible text message?
                return Collections.<Object> singleton(new JSONObject(messageData));
            }
        });

        this.generators = ImmutableMap.copyOf(generators);
    }

    @Override
    public String getClient() {
        return Client.APPSUITE_UI.getClientId();
    }

    @Override
    public Message<JSONValue> generateMessageFor(String transportId, PushNotification notification) throws OXException {
        if (!TRANSPORT_ID_WEB_SOCKET.equals(transportId)) {
            throw PushExceptionCodes.UNSUPPORTED_TRANSPORT.create(null == transportId ? "null" : transportId);
        }

        // From here on it's known that we are supposed to compile a Socket.IO JSON message since
        // com.openexchange.pns.appsuite.AppSuiteWebSocketToClientResolver only accepts "/socket.io/appsuite/*"

        String topic = notification.getTopic();
        Collection<Object> args;
        {
            ArgsGenerator argsGenerator = generators.get(topic);
            if (null == argsGenerator) {
                // Default to wrap message data as a single JSON object
                args = Collections.<Object> singleton(new JSONObject(notification.getMessageData()));
            } else {
                args = argsGenerator.generateArgsFrom(notification);
            }
        }

        try {
            return new JsonMessage(new JSONObject(3).put("name", topic).put("args", new JSONArray(args)).put("namespace", DEFAULT_NAMESPACE));
        } catch (JSONException e) {
            throw PushExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
