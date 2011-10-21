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

package com.openexchange.ajax.chat.conversation;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * {@link JSONChat}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONChat {

    /**
     * Parses given JSON to a {@link JSONChat}.
     * 
     * @param jsonChat The JSON chat data
     * @return The parsed {@link JSONChat}
     * @throws JSONException If a JSON error occurs
     */
    public static JSONChat valueOf(final JSONObject jsonChat) throws JSONException {
        final JSONChat ret = new JSONChat();
        ret.setChatId(jsonChat.getString("id"));
        ret.setSubject(jsonChat.getString("subject"));
        final JSONArray jsonMembers = jsonChat.getJSONArray("members");
        final int length = jsonMembers.length();
        for (int i = 0; i < length; i++) {
            final JSONObject jsonChatUser = jsonMembers.getJSONObject(i);
            ret.add(JSONChatUser.valueOf(jsonChatUser));
        }
        return ret;
    }

    private final List<JSONChatUser> members;

    private String chatId;

    private String subject;

    /**
     * Initializes a new {@link JSONChat}.
     */
    public JSONChat() {
        super();
        members = new LinkedList<JSONChatUser>();
    }

    /**
     * Gets the chat identifier
     * 
     * @return The chat identifier
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * Sets the chatId
     * 
     * @param chatId The chatId to set
     */
    public void setChatId(final String chatId) {
        this.chatId = chatId;
    }

    /**
     * Gets the subject
     * 
     * @return The subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject
     * 
     * @param subject The subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    /**
     * Gets the chat members
     * 
     * @return The members
     */
    public List<JSONChatUser> getMembers() {
        return members;
    }

    /**
     * Adds given member
     * 
     * @param user The member
     */
    public void add(final JSONChatUser user) {
        members.add(user);
    }

    /**
     * Removes given member
     * 
     * @param user The member
     */
    public void remove(final JSONChatUser user) {
        members.remove(user);
    }

}
