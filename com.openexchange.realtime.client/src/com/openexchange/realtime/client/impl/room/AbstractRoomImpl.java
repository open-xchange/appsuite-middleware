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

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import org.apache.commons.lang.Validate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONValue;
import com.openexchange.realtime.client.ID;
import com.openexchange.realtime.client.RTConnection;
import com.openexchange.realtime.client.RTConnectionProperties;
import com.openexchange.realtime.client.RTException;
import com.openexchange.realtime.client.RTMessageHandler;
import com.openexchange.realtime.client.room.RTRoom;

/**
 * Basic implementation of {@link RTRoom} that should be used as base for group chats.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.4
 */
public abstract class AbstractRoomImpl implements RTRoom {

    /**
     * Instance used for communication (beside 'create')
     */
    private RTConnection rtConnection = null;

    /**
     * String to address the messages
     */
    private String toAddress = null;

    /**
     * The name of the room what is also known as 'selector'
     */
    private String roomName = null;

    /**
     * Timer to send a ping
     */
    private Timer pingTimer = null;

    /**
     * Task for the timer to send the ping
     */
    private TimerTask pingTimerTask = null;

    /**
     * Message handler for receiving messages
     */
    private RTMessageHandler rtMessageHandler = null;


    /**
     * Initializes a new {@link AbstractRoomImpl}.
     * 
     * @param rtUser - {@link RTUser} that will interact with the room.
     * @param rtConnectionProperties - {@link RTConnectionProperties} related to the user.
     */
    public AbstractRoomImpl(RTConnection connection) {
        Validate.notNull(connection);
        this.rtConnection = connection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void join(ID room, RTMessageHandler messageHandler) throws RTException {
        Validate.notNull(room, "room cannot be null.");
        Validate.notNull(messageHandler, "messageHandler must be set.");

        this.roomName = UUID.randomUUID().toString();
        this.toAddress = room.toString();
        this.rtMessageHandler = messageHandler;
        rtConnection.registerHandler(this.roomName, this.rtMessageHandler);
        setupTimer();

        try {
            JSONValue join = this.createJoinObject();
            this.send(join);
        } catch (Exception exception) {
            throw new RTException(exception);
        }
    }

    /**
     * Sets up the ping timer to run and give a heart beat to the server
     */
    protected void setupTimer() {
        Validate.notNull(this.rtConnection);
        Validate.notNull(this.toAddress);

        this.pingTimer = new Timer("Ping");
        this.pingTimerTask = new RTRoomPingTimerTask(rtConnection, this.toAddress);
        this.pingTimer.scheduleAtFixedRate(this.pingTimerTask, 30000, 30000);
    }

    /**
     * Creates the join object that will be send to the given room and address.
     * 
     * @return {@link JSONValue} with the data to join a room.
     * @throws JSONException
     */
    private JSONValue createJoinObject() throws JSONException {
        return RTRoomJSONHelper.createJoinMessage(this.roomName, this.toAddress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void say(String message) throws RTException {
        Validate.notNull(rtConnection, "Not logged in!");
        Validate.notNull(message, "Message cannot be null!");

        try {
            JSONValue say = this.createSayObject(toPayloads(message));
            this.send(say);
        } catch (Exception exception) {
            throw new RTException(exception);
        }
    }

    protected abstract JSONArray toPayloads(String message) throws JSONException, RTException;

    /**
     * Creates the say object that will be send into a room.
     * 
     * @param message - {@link JSONArray} with the payloads to send to the room
     * @return {@link JSONValue} with the complete message to send to the room
     * @throws JSONException
     */
    private JSONValue createSayObject(JSONArray payloads) throws JSONException {
        return RTRoomJSONHelper.createSayMessage(this.toAddress, payloads);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void leave() throws RTException {
        Validate.notNull(rtConnection, "Not logged in!");

        try {
            JSONValue leave = this.createLeaveObject();
            this.send(leave);

            this.rtConnection.unregisterHandler(this.roomName);
        } catch (Exception exception) {
            throw new RTException(exception);
        }
    }

    /**
     * Creates the leave object that will be send to leave the room.
     * 
     * @param message - {@link JSONValue} with the message to say into a room
     * @return {@link JSONValue} with the data to say something into a room.
     * @throws JSONException
     */
    private JSONValue createLeaveObject() throws JSONException {
        return RTRoomJSONHelper.createLeaveMessage(this.toAddress);
    }

    /**
     * Sends the object and resets the ping timer
     * 
     * @param objectToSend - {@link JSONValue} with the data to send to the server and deal with join/say/leave
     */
    protected void send(JSONValue objectToSend) throws RTException {
        try {
            this.rtConnection.postReliable(objectToSend);
        } catch (Exception exception) {
            throw new RTException(exception);
        }
        this.resetTimer();
    }

    /**
     * Resets the ping timer
     */
    private void resetTimer() {
        this.pingTimer.cancel();
        this.pingTimerTask.cancel();
        this.pingTimer = null;
        this.pingTimerTask = null;
        setupTimer();
    }

    /**
     * Gets the rtConnection
     *
     * @return The rtConnection
     */
    protected RTConnection getRtConnection() {
        return rtConnection;
    }

    /**
     * Sets the rtConnection
     * 
     * @param rtConnection The rtConnection to set
     */
    protected void setRtConnection(RTConnection rtConnection) {
        this.rtConnection = rtConnection;
    }

    /**
     * Gets the rtMessageHandler
     * 
     * @return The rtMessageHandler
     */
    protected RTMessageHandler getRtMessageHandler() {
        return rtMessageHandler;
    }

    /**
     * Gets the toAddress
     * 
     * @return The toAddress
     */
    protected String getToAddress() {
        return toAddress;
    }

    /**
     * Gets the pingTimer
     * 
     * @return The pingTimer
     */
    protected Timer getPingTimer() {
        return pingTimer;
    }
}
