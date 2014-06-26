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

package com.openexchange.chat;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link Chat} - Represents a chat (room) or a user's private chat.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Chat extends ChatGroup {

    /**
     * Gets the chat identifier.
     *
     * @return The chat identifier.
     */
    String getChatId();

    /**
     * Gets the chat's subject.
     *
     * @return The subject
     */
    String getSubject();

    /**
     * Joins specified chat member to this chat.
     *
     * @param user The identifier of the member to join
     * @throws OXException If joining the chat member fails
     */
    void join(String user) throws OXException;

    /**
     * Parts specified chat member from this chat.
     *
     * @param user The identifier of the member to part
     * @throws OXException If member cannot be removed
     */
    void part(String user) throws OXException;

    /**
     * Deletes a message by specified identifier.
     *
     * @param messageId The message identifier
     */
    void deleteMessage(String messageId) throws OXException;

    /**
     * Posts specified packet to this chat.
     *
     * @param packet The packet to post
     * @throws OXException If posting the packet fails
     */
    void post(Packet packet) throws OXException;

    /**
     * Get count of unread messages since last poll
     *
     * @param chatUser The chat user for whom the messages are requested
     * @return Count of unread messages
     * @throws OXException If counting unread messages fails
     */
    int getUnreadCount(ChatUser chatUser) throws OXException;

    /**
     * Polls all messages from this chat that were posted after specified date.
     *
     * @param since The date or <code>null</code> to poll whole chat's time line
     * @param user The chat user for whom the messages are requested
     * @return All messages
     * @throws OXException If messages cannot be polled
     */
    List<Message> pollMessages(Date since, ChatUser user) throws OXException;

    /**
     * Updates an existing message according {@link MessageDescription}'s arguments.
     *
     * @param messageDesc The message description
     * @throws OXException If update operation fails
     */
    void updateMessage(MessageDescription messageDesc) throws OXException;

    /**
     * Gets a message by specified identifier.
     *
     * @param messageId The message identifier
     * @return The associated message
     */
    Message getMessage(String messageId, int userId) throws OXException;

    /**
     * Gets messages by specified identifiers.
     *
     * @param messageIds The message identifiers
     * @return The associated messages
     */
    List<Message> getMessages(Collection<String> messageIds, int userId) throws OXException;

    /**
     * Adds given message listener that will be notified of any new messages in the chat.
     * <p>
     * If <code>false</code> is returned, the client is supposed to poll messages with {@link #pollMessages(Date)} while remembering last
     * packet's time stamp.
     * <p>
     * See also {@link ChatCaps#supportsNotifcation()}.
     *
     * @param listener A message listener.
     * @return <code>true</code> if listener could be registered; otherwise <code>false</code> if this chat does not support notifications
     * @see ChatCaps#supportsNotifcation()
     */
    boolean addMessageListener(MessageListener listener);

    /**
     * Removes specified message listener
     *
     * @param listener The message listener to remove
     */
    void removeMessageListener(MessageListener listener);

    /**
     * Returns an unmodifiable collection of all of the listeners registered with this chat.
     *
     * @return An unmodifiable collection of all of the listeners registered with this chat.
     */
    Collection<MessageListener> getListeners();

}
