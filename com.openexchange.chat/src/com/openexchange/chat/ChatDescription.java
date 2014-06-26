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

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ChatDescription} - Provides changeable attributes of a {@link Chat chat}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ChatDescription {

    private String chatId;

    private final List<String> newMembers;

    private final List<String> delMembers;

    private String subject;

    /**
     * Initializes a new {@link ChatDescription}.
     *
     * @param chatId The chat identifier; <code>null</code> for a new chat
     */
    public ChatDescription(final String chatId) {
        super();
        this.chatId = chatId;
        newMembers = new ArrayList<String>(8);
        delMembers = new ArrayList<String>(8);
    }

    /**
     * Initializes a new {@link ChatDescription}.
     */
    public ChatDescription() {
        this(null);
    }

    /**
     * Sets the chat identifier
     *
     * @param chatId The chat identifier to set
     */
    public void setChatId(final String chatId) {
        this.chatId = chatId;
    }

    /**
     * Checks if this chat description provides any changed attribute.
     *
     * @return <code>true</code> if this chat description provides any changed attribute; otherwise <code>false</code>
     */
    public boolean hasAnyAttribute() {
        if (null != subject) {
            return true;
        }
        if (!newMembers.isEmpty()) {
            return true;
        }
        if (!delMembers.isEmpty()) {
            return true;
        }
        return false;
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
     * Adds the identifier of the user who shall be added to denoted chat.
     *
     * @param user The user identifier
     */
    public void addNewMember(final String user) {
        newMembers.add(user);
    }

    /**
     * Gets the identifiers of the users who shall be added to denoted chat.
     *
     * @return The user identifiers
     */
    public List<String> getNewMembers() {
        return newMembers;
    }

    /**
     * Adds the identifier of the user who shall be deleted from denoted chat.
     *
     * @param user The user identifier
     */
    public void addDeleteMember(final String user) {
        delMembers.add(user);
    }

    /**
     * Gets the identifiers of the users who shall be deleted from denoted chat.
     *
     * @return The user identifiers
     */
    public List<String> getDeletedMembers() {
        return delMembers;
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

}
