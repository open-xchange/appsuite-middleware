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

package com.openexchange.chat;

import java.util.List;
import com.openexchange.exception.OXException;

/**
 * {@link ChatAccess} - Provides the methods to access a chat for a certain user.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface ChatAccess {

    /**
     * Gets the capabilities of this access.
     * 
     * @return The capabilities
     */
    ChatCaps getCapabilities();

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
    ChatMember getUser();

    /**
     * Gets all descriptions for opened chats for this access.
     * 
     * @return All descriptions for opened chats
     * @throws OXException If an error occurs
     */
    List<ChatDesc> getChats() throws OXException;

    /**
     * Opens described chat with specified member.
     * 
     * @param chatDescription The chat description
     * @param member The member with which to open the chat
     * @return The opened chat
     * @throws OXException If chat cannot be opened
     * @see ChatDesc#DEFAULT_CHAT
     */
    Chat openChat(ChatDesc chatDescription, MessageListener listener, ChatMember member) throws OXException;

    /**
     * Opens described chat with specified members.
     * 
     * @param chatDescription The chat description
     * @param members The members with which to open the chat
     * @return The opened chat
     * @throws OXException If chat cannot be opened
     * @see ChatDesc#DEFAULT_CHAT
     */
    Chat openChat(ChatDesc chatDescription, MessageListener listener, ChatMember... members) throws OXException;

}
