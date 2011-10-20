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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.chat.Chat;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.Presence;


/**
 * {@link Writer} - Provides write methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Writer {

    /**
     * Initializes a new {@link Writer}.
     */
    private Writer() {
        super();
    }

    /**
     * Writes specified chat to its JSON representation.
     * 
     * @param chat The chat
     * @param optPresence The optional presence
     * @return The JSON object
     * @throws JSONException If a JSON error occurs
     */
    public static JSONObject writeChat(final Chat chat, final List<ChatUser> members, final List<Presence> presences) throws JSONException {
        final JSONObject jsonChat = new JSONObject();
        jsonChat.put("id", chat.getChatId());
        jsonChat.put("subject", chat.getSubject());
        final JSONArray jsonMembers = new JSONArray();
        final int size = members.size();
        for (int i = 0; i < size; i++) {
            jsonMembers.put(writeChatUser(members.get(i), presences.get(i)));
        }
        jsonChat.put("members", jsonMembers);
        return jsonChat;
    }

    /**
     * Writes specified chat user to its JSON representation.
     * 
     * @param chatUser The chat user
     * @param optPresence The optional presence
     * @return The JSON object
     * @throws JSONException If a JSON error occurs
     */
    public static JSONObject writeChatUser(final ChatUser chatUser, final Presence optPresence) throws JSONException {
        final JSONObject jsonChatUser = new JSONObject();
        jsonChatUser.put("id", chatUser.getId());
        jsonChatUser.put("name", chatUser.getName());
        if (null != optPresence) {
            final JSONObject jsonPresence = writePresence(optPresence);
            jsonChatUser.put("presence", jsonPresence);
        }
        return jsonChatUser;
    }

    /**
     * Writes specified presence to its JSON representation.
     * 
     * @param presence The presence
     * @return The JSON object
     * @throws JSONException If a JSON error occurs
     */
    public static JSONObject writePresence(final Presence presence) throws JSONException {
        final JSONObject jsonPresence = new JSONObject();
        jsonPresence.put("type", presence.getType().name());
        jsonPresence.put("mode", presence.getMode().name());
        jsonPresence.put("status", presence.getStatus());
        final Date timeStamp = presence.getTimeStamp();
        if (null != timeStamp) {
            jsonPresence.put("timeStamp", timeStamp.getTime());
        }
        return jsonPresence;
    }

}
