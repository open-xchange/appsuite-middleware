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

package com.openexchange.realtime.client.impl.room;

import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;


/**
 * Helper class that generates the {@link JSONValue} instances to participate in room actions.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class RTRoomJSONHelper {

    /**
     * Creates the {@link JSONValue} to join a room
     * 
     * @param name - String with the name of the room (selector)
     * @param to - String with the address to send the message to
     * @return {@link JSONValue} to join a room
     * @throws JSONException
     */
    public static JSONValue createJoinMessage(String name, String to) throws JSONException {
        JSONObject objectToSend = new JSONObject();
        objectToSend.put("element", "message");
        objectToSend.put("selector", name);
        objectToSend.put("to", to);

        JSONObject payload = new JSONObject();
        payload.put("element", "command");
        payload.put("namespace", "group");
        payload.put("data", "join");

        final JSONArray payloads = new JSONArray();
        payloads.put(payload);

        objectToSend.put("payloads", payloads);

        return objectToSend;
    }

    /**
     * Creates the {@link JSONValue} to say a message into room
     * 
     * @param to - String with the address to send the message to
     * @param payloads - {@link JSONArray} with the message (all required payloads) to say.
     * @return {@link JSONValue} to say a message into a room
     * @throws JSONException
     */
    public static JSONValue createSayMessage(String to, JSONArray payloads) throws JSONException {
        JSONObject objectToSend = new JSONObject();
        objectToSend.put("element", "message");
        objectToSend.put("to", to);
        objectToSend.put("payloads", payloads);
        objectToSend.put("tracer", UUID.randomUUID().toString());

        return objectToSend;
    }

    /**
     * Creates the {@link JSONValue} to leave a room
     * 
     * @param to - String with the address to send the message to
     * @return {@link JSONValue} to leave into a room
     * @throws JSONException
     */
    public static JSONValue createLeaveMessage(String to) throws JSONException {
        JSONObject objectToSend = new JSONObject();
        objectToSend.put("element", "message");
        objectToSend.put("to", to);

        JSONObject payload = new JSONObject();
        payload.put("element", "command");
        payload.put("namespace", "group");
        payload.put("data", "leave");

        final JSONArray payloads = new JSONArray();
        payloads.put(payload);

        objectToSend.put("payloads", payloads);

        return objectToSend;
    }
}
