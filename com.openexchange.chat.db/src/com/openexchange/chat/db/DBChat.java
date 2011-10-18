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

package com.openexchange.chat.db;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import com.openexchange.chat.Chat;
import com.openexchange.chat.MessageListener;
import com.openexchange.chat.Packet;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;

/**
 * {@link DBChat}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChat implements Chat {

    private int chatId;

    private final List<String> members;

    private final List<MessageListener> messageListeners;

    private String subject;

    /**
     * Initializes a new {@link DBChat}.
     */
    public DBChat() {
        super();
        messageListeners = new CopyOnWriteArrayList<MessageListener>();
        members = new CopyOnWriteArrayList<String>();
    }

    /**
     * Sets the chatId
     * 
     * @param chatId The chatId to set
     */
    public void setChatId(final int chatId) {
        this.chatId = chatId;
    }

    /**
     * Gets the numeric chat identifier.
     * 
     * @return The numeric chat identifier
     */
    public int getChatIdInt() {
        return chatId;
    }

    @Override
    public String getChatId() {
        return String.valueOf(chatId);
    }

    @Override
    public String getSubject() {
        return subject;
    }

    @Override
    public List<String> getMembers() throws OXException {
        return Collections.unmodifiableList(members);
    }

    @Override
    public void join(final String user) throws OXException {
        
        
        members.add(user);
    }

    @Override
    public void part(final String user) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public void post(final Packet packet) throws OXException {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean addMessageListener(final MessageListener listener) {
        if (listener == null) {
            return false;
        }
        return messageListeners.add(listener);
    }

    @Override
    public void removeMessageListener(final MessageListener listener) {
        if (listener == null) {
            return;
        }
        messageListeners.remove(listener);
    }

    @Override
    public Collection<MessageListener> getListeners() {
        return Collections.unmodifiableList(messageListeners);
    }

    /**
     * Sets the subject
     * 
     * @param subject The subject to set
     */
    public void setSubject(final String subject) {
        this.subject = subject;
    }

    private DatabaseService getDatabaseService() throws OXException {
        final DatabaseService databaseService = DBChatServiceLookup.getService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        return databaseService;
    }

}
