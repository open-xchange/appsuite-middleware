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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.xing;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.xing.exception.XingException;

/**
 * {@link Conversation} - Represents a XING conversation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class Conversation {

    private final String id;
    private final String subject;
    private final int messageCount;
    private final int unreadMessageCount;
    private final Date updatedAt;
    private final boolean readOnly;
    private final List<User> participants;
    private final List<Message> latestMessages;

    /**
     * Initializes a new {@link Conversation}.
     *
     * @throws XingException If parsing fails
     */
    public Conversation(final JSONObject conversationInformation) throws XingException {
        super();
        try {
            this.id = conversationInformation.optString("id", null);
            this.subject = conversationInformation.optString("subject", null);
            this.messageCount = conversationInformation.optInt("message_count", 0);
            this.unreadMessageCount = conversationInformation.optInt("unread_message_count", 0);
            if (conversationInformation.hasAndNotNull("updated_at")) {
                final DateFormat dateFormat = Message.getDateFormat();
                synchronized (dateFormat) {
                    Date d;
                    try {
                        d = dateFormat.parse(conversationInformation.getString("updated_at"));
                    } catch (final ParseException e) {
                        d = null;
                    }
                    this.updatedAt = d;
                }
            } else {
                updatedAt = null;
            }
            this.readOnly = conversationInformation.optBoolean("read_only", false);
            if (conversationInformation.hasAndNotNull("participants")) {
                final JSONArray participantsInformation = conversationInformation.optJSONArray("participants");
                final int length = participantsInformation.length();
                participants = new ArrayList<User>(length);
                for (int i = 0; i < length; i++) {
                    participants.add(new User(participantsInformation.optJSONObject(i)));
                }
            } else {
                participants = Collections.emptyList();
            }
            if (conversationInformation.hasAndNotNull("latest_messages")) {
                final JSONArray msgsInformation = conversationInformation.optJSONArray("latest_messages");
                final int length = msgsInformation.length();
                latestMessages = new ArrayList<Message>(length);
                for (int i = 0; i < length; i++) {
                    latestMessages.add(new Message(msgsInformation.optJSONObject(i)));
                }
            } else {
                latestMessages = Collections.emptyList();
            }
        } catch (final JSONException e) {
            throw new XingException(e);
        }
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
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
     * Gets the message count
     *
     * @return The message count
     */
    public int getMessageCount() {
        return messageCount;
    }

    /**
     * Gets the unread message count
     *
     * @return The unread message count
     */
    public int getUnreadMessageCount() {
        return unreadMessageCount;
    }

    /**
     * Gets the updated-at time stamp
     *
     * @return The updated-at time stamp
     */
    public Date getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Gets the read-only flag
     *
     * @return The read-only flag
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * Gets the participants
     *
     * @return The participants
     */
    public List<User> getParticipants() {
        return participants;
    }

    /**
     * Gets the latest messages
     *
     * @return The latest messages
     */
    public List<Message> getLatestMessages() {
        return latestMessages;
    }

}
