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

import java.util.List;
import com.openexchange.chat.Chat;
import com.openexchange.chat.ChatAccess;
import com.openexchange.chat.ChatCaps;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.MessageListener;
import com.openexchange.chat.Presence;
import com.openexchange.chat.Roster;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link DBChatAccess}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChatAccess implements ChatAccess {

    private final Session session;

    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link DBChatAccess}.
     * 
     * @param session 
     */
    public DBChatAccess(final Session session) {
        super();
        this.session = session;
        serviceLookup = DBChatServiceLookup.get();
    }

    @Override
    public ChatCaps getCapabilities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void disconnect() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#login()
     */
    @Override
    public void login() throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#getUser()
     */
    @Override
    public ChatUser getUser() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#sendPresence(com.openexchange.chat.Presence)
     */
    @Override
    public void sendPresence(final Presence presence) throws OXException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#getRoster()
     */
    @Override
    public Roster getRoster() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#getChats()
     */
    @Override
    public List<String> getChats() throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#openChat(java.lang.String, com.openexchange.chat.MessageListener, com.openexchange.chat.ChatUser)
     */
    @Override
    public Chat openChat(final String chatId, final MessageListener listener, final ChatUser member) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#openChat(java.lang.String, com.openexchange.chat.MessageListener, com.openexchange.chat.ChatUser[])
     */
    @Override
    public Chat openChat(final String chatId, final MessageListener listener, final ChatUser... members) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    private DatabaseService getDatabaseService() throws OXException {
        final DatabaseService databaseService = serviceLookup.getService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        return databaseService;
    }

}
