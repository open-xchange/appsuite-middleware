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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.chat.json.conversation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chat.ChatDescription;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.Message;
import com.openexchange.chat.MessageDescription;
import com.openexchange.chat.util.MessageImpl;

/**
 * {@link JSONConversationParser}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONConversationParser {

    /**
     * Initializes a new {@link JSONConversationParser}.
     */
    private JSONConversationParser() {
        super();
    }

    /**
     * Parses JSON to a message instance.
     *
     * @param jsonMessage The JSON message
     * @param from The chat user posting the message
     * @return The parsed message
     * @throws JSONException If parsing fails
     */
    public static Message parseMessage(final JSONObject jsonMessage, final ChatUser from) throws JSONException {
        final MessageImpl message = new MessageImpl();
        if (null != jsonMessage) {
            if (jsonMessage.hasAndNotNull("id")) {
                message.setPacketId(jsonMessage.getString("id"));
            }
            message.setFrom(from);
            if (jsonMessage.hasAndNotNull("subject")) {
                message.setSubject(jsonMessage.getString("subject"));
            }
            if (jsonMessage.hasAndNotNull("text")) {
                message.setText(jsonMessage.getString("text"));
            }
        }
        return message;
    }

    /**
     * Parses JSON to a message description.
     *
     * @param jsonMessage The JSON data
     * @return The parsed message description
     * @throws JSONException If a JSON error occurs
     */
    public static MessageDescription parseMessageDescription(final JSONObject jsonMessage) throws JSONException {
        final MessageDescription messageDesc;
        if (null == jsonMessage) {
            messageDesc = new MessageDescription();
        } else {
            if (jsonMessage.hasAndNotNull("id")) {
                messageDesc = new MessageDescription(jsonMessage.getString("id"));
            } else {
                messageDesc = new MessageDescription();
            }
            if (jsonMessage.hasAndNotNull("subject")) {
                messageDesc.setSubject(jsonMessage.getString("subject"));
            }
            if (jsonMessage.hasAndNotNull("text")) {
                messageDesc.setText(jsonMessage.getString("text"));
            }
        }
        return messageDesc;
    }

    /**
     * Parses JSON to a chat instance.
     *
     * @param jsonChat The JSON chat representation
     * @return The parsed chat instance
     * @throws JSONException If a JSON error occurs
     */
    public static ChatDescription parseJSONChatDescriptionForUpdate(final JSONObject jsonChat) throws JSONException {
        final ChatDescription chatDesc = new ChatDescription();
        if (null != jsonChat) {
            if (jsonChat.hasAndNotNull("id")) {
                chatDesc.setChatId(jsonChat.getString("id"));
            }
            if (jsonChat.hasAndNotNull("subject")) {
                chatDesc.setSubject(jsonChat.getString("subject"));
            }
            if (jsonChat.hasAndNotNull("newMembers")) {
                final JSONArray jsonNewMembers = jsonChat.getJSONArray("newMembers");
                final int length = jsonNewMembers.length();
                for (int i = 0; i < length; i++) {
                    chatDesc.addNewMember(jsonNewMembers.getString(i));
                }
            }
            if (jsonChat.hasAndNotNull("deleteMembers")) {
                final JSONArray jsonDeleteMembers = jsonChat.getJSONArray("deleteMembers");
                final int length = jsonDeleteMembers.length();
                for (int i = 0; i < length; i++) {
                    chatDesc.addDeleteMember(jsonDeleteMembers.getString(i));
                }
            }
        }
        return chatDesc;
    }

    /**
     * Parses JSON to a chat instance.
     *
     * @param jsonChat The JSON chat representation
     * @return The parsed chat instance
     * @throws JSONException If a JSON error occurs
     */
    public static ChatDescription parseJSONChatDescriptionForCreate(final JSONObject jsonChat) throws JSONException {
        final ChatDescription chatDesc = new ChatDescription();
        if (null != jsonChat) {
            if (jsonChat.hasAndNotNull("subject")) {
                chatDesc.setSubject(jsonChat.getString("subject"));
            }
            if (jsonChat.hasAndNotNull("newMembers")) {
                final JSONArray jsonNewMembers = jsonChat.getJSONArray("newMembers");
                final int length = jsonNewMembers.length();
                for (int i = 0; i < length; i++) {
                    chatDesc.addNewMember(jsonNewMembers.getString(i));
                }
            }
        }
        return chatDesc;
    }

}
