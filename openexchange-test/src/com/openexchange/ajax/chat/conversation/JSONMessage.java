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

import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.chat.conversation.actions.AbstractChatConversationRequest;

/**
 * {@link JSONMessage} - Represents a chat message rendered with JSON.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JSONMessage {

    /**
     * Parses given JSON to a {@link JSONMessage}.
     *
     * @param jsonMessage The JSON message data
     * @return The {@link JSONMessage}
     * @throws JSONException If a JSON error occurs
     */
    public static JSONMessage valueOf(final JSONObject jsonMessage, final TimeZone timeZone) throws JSONException {
        final JSONMessage ret = new JSONMessage();
        ret.setMessageId(jsonMessage.getString("id"));
        if (jsonMessage.hasAndNotNull("from")) {
            ret.setFrom(JSONChatUser.valueOf(jsonMessage.getJSONObject("from"), timeZone));
        }
        if (jsonMessage.hasAndNotNull("subject")) {
            ret.setSubject(jsonMessage.getString("subject"));
        }
        if (jsonMessage.hasAndNotNull("text")) {
            ret.setText(jsonMessage.getString("text"));
        }
        if (jsonMessage.hasAndNotNull("timeStamp")) {
            ret.setTimeStamp(AbstractChatConversationRequest.getDateField(jsonMessage, "timeStamp", timeZone));
        }
        return ret;
    }

    private String messageId;

    private JSONChatUser from;

    private String subject;

    private String text;

    private Date timeStamp;

    /**
     * Initializes a new {@link JSONMessage}.
     */
    public JSONMessage() {
        super();
    }

    /**
     * Gets the messageId
     *
     * @return The messageId
     */
    public String getMessageId() {
        return messageId;
    }

    /**
     * Sets the messageId
     *
     * @param messageId The messageId to set
     */
    public void setMessageId(final String messageId) {
        this.messageId = messageId;
    }

    /**
     * Gets the from
     *
     * @return The from
     */
    public JSONChatUser getFrom() {
        return from;
    }

    /**
     * Sets the from
     *
     * @param from The from to set
     */
    public void setFrom(final JSONChatUser from) {
        this.from = from;
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
     * Gets the text
     *
     * @return The text
     */
    public String getText() {
        return text;
    }

    /**
     * Sets the text
     *
     * @param text The text to set
     */
    public void setText(final String text) {
        this.text = text;
    }

    /**
     * Gets the timeStamp
     *
     * @return The timeStamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * Sets the timeStamp
     *
     * @param timeStamp The timeStamp to set
     */
    public void setTimeStamp(final Date timeStamp) {
        this.timeStamp = timeStamp;
    }

}
