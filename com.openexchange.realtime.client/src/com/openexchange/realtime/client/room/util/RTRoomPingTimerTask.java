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

package com.openexchange.realtime.client.room.util;

import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.realtime.client.RTConnection;

/**
 * Sends a ping into a room to give a heart beat the realtime server and with that to not be removed out of the room.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public class RTRoomPingTimerTask extends TimerTask {

    /**
     * Lock for the ping
     */
    final Object lock = new Object();

    /**
     * {@link RTConnection} to send the ping
     */
    private RTConnection rtConnection;

    /**
     * String with the to address to send the ping.
     */
    private String to;

    /**
     * Initializes a new {@link RTRoomPingTimerTask}.
     * 
     * @param rtConnection
     * @param to
     */
    public RTRoomPingTimerTask(RTConnection rtConnection, String to) {
        super();
        this.rtConnection = rtConnection;
        this.to = to;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        synchronized (this.lock) {
            try {
                final JSONValue ping = this.createPingObject();
                this.rtConnection.post(ping);
            } catch (final Exception e) {
                // TODO LOG.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Creates the object to send a ping.
     * 
     * @return {@JSONObject} which includes the ping data.
     * @throws JSONException - thrown when an error while JSON object creation occurs.
     */
    private JSONValue createPingObject() throws JSONException {
        JSONObject pingObject = new JSONObject();

        pingObject.put("element", "message");
        pingObject.put("to", to);

        JSONObject payload = new JSONObject();
        payload.put("element", "ping");
        payload.put("namespace", "group");

        final JSONArray payloads = new JSONArray();
        payloads.put(payload);

        pingObject.put("payloads", payloads);

        return pingObject;
    }
}
