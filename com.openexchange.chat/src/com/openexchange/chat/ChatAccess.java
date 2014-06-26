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

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link ChatAccess} - Provides access to chat functionality in the name of associated user.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ChatAccess {

    /**
     * Closes the connection by setting presence to unavailable then closing the connection.
     */
    void disconnect();

    /**
     * Logs in to the server, then sets presence to available.
     *
     * @throws OXException If an error occurs.
     */
    void login() throws OXException;

    /**
     * Gets the associated user.
     *
     * @return The user
     */
    ChatUser getUser();

    /**
     * Sends given presence packet for associated {@link #getUser() user}.
     *
     * @param presence The presence packet
     * @throws OXException If sending presence packet fails
     */
    void sendPresence(Presence presence) throws OXException;

    /**
     * Gets the user's roster.
     *
     * @return The roster
     * @throws OXException If roster cannot be returned
     */
    Roster getRoster() throws OXException;

    /**
     * Gets all identifiers for opened chats for this access.
     *
     * @return All identifiers for opened chats
     * @throws OXException If an error occurs
     */
    List<String> getChats() throws OXException;

    /**
     * Gets existing chat with specified member.
     *
     * @param chatId The chat identifier
     * @return The existing chat
     * @throws OXException If chat cannot be returned
     */
    Chat getChat(String chatId) throws OXException;

    /**
     * Opens described chat with specified member.
     *
     * @param chatId The chat identifier or <code>null</code> to generate a unique one
     * @param member The member with which to open the chat
     * @return The opened chat
     * @throws OXException If chat cannot be opened
     */
    Chat openChat(String chatId, MessageListener listener, ChatUser member) throws OXException;

    /**
     * Opens described chat with specified members.
     *
     * @param chatId The chat identifier or <code>null</code> to generate a unique one
     * @param members The members with which to open the chat
     * @return The opened chat
     * @throws OXException If chat cannot be opened
     */
    Chat openChat(String chatId, MessageListener listener, ChatUser... members) throws OXException;

    /**
     * Updates the denoted chat by {@link ChatDescription}'s arguments.
     *
     * @param chatDescription The chat description
     * @throws OXException If update operation fails
     */
    void updateChat(ChatDescription chatDescription) throws OXException;

}
