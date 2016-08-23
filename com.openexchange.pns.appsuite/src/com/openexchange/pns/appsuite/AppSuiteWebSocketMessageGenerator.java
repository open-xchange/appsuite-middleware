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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.pns.appsuite;

import java.util.Collection;
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
                return messageData.values();
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

        // From here on it known that we are supposed to compile a Socket.IO JSON message since
        // com.openexchange.pns.appsuite.AppSuiteWebSocketToClientResolver only accepts "/socket.io/*"

        String topic = notification.getTopic();
        ArgsGenerator argsGenerator = generators.get(topic);
        if (null == argsGenerator) {
            throw PushExceptionCodes.MESSAGE_GENERATION_FAILED.create("Unhandled topic " + topic);
        }

        try {
            return new JsonMessage(new JSONObject(3).put("name", topic).put("args", new JSONArray(argsGenerator.generateArgsFrom(notification))).put("namespace", DEFAULT_NAMESPACE));
        } catch (JSONException e) {
            throw PushExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
