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

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.chat.Chat;
import com.openexchange.chat.ChatAccess;
import com.openexchange.chat.ChatCaps;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.MessageListener;
import com.openexchange.chat.Presence;
import com.openexchange.chat.Presence.Mode;
import com.openexchange.chat.Roster;
import com.openexchange.chat.util.ChatUserImpl;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.user.UserService;

/**
 * {@link DBChatAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChatAccess implements ChatAccess {

    private final Session session;

    private final ChatUser user;

    private final ServiceLookup serviceLookup;

    private final Context context;

    /**
     * Initializes a new {@link DBChatAccess}.
     * 
     * @param session The session
     * @throws OXException If init fails
     */
    public DBChatAccess(final Session session) throws OXException {
        super();
        this.session = session;
        serviceLookup = DBChatServiceLookup.get();
        context = serviceLookup.getService(ContextService.class).getContext(session.getContextId());
        final ChatUserImpl user = new ChatUserImpl();
        user.setId(String.valueOf(session.getUserId()));
        user.setName(getUserName(session.getUserId()));
        this.user = user;
    }

    private String getUserName(final int userId) throws OXException {
        return serviceLookup.getService(UserService.class).getUser(userId, context).getDisplayName();
    }

    @Override
    public ChatCaps getCapabilities() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void disconnect() {
        // Nothing to do: Login/logout is mapped to availability of any active session for a user
    }

    @Override
    public void login() throws OXException {
        // Nothing to do: Login/logout is mapped to availability of any active session for a user
    }

    @Override
    public ChatUser getUser() {
        return user;
    }

    @Override
    public void sendPresence(final Presence presence) throws OXException {
        if (!Presence.Type.AVAILABLE.equals(presence.getType())) {
            throw ChatExceptionCodes.INVALID_PRESENCE_PACKET.create();
        }
        final DatabaseService databaseService = getDatabaseService();
        PreparedStatement stmt = null;
        final Connection con = databaseService.getWritable(context);
        try {
            stmt = con.prepareStatement("UPDATE chatPresence SET mode = ?, statusMessage = ?, lastModified = ? WHERE cid = ? AND user = ?");
            int pos = 1;
            {
                final Mode  mode = presence.getMode();
                stmt.setString(pos++, (null == mode ? Mode.AVAILABLE : mode).name());
            }
            {
                final String status = presence.getStatus();
                if (null == status) {
                    stmt.setNull(pos++, java.sql.Types.VARCHAR);
                } else {
                    stmt.setString(pos++, status);
                }
            }
            stmt.setLong(pos++, System.currentTimeMillis());
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, session.getUserId());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
            databaseService.backWritable(context, con);
        }
        /*
         * Notify roster listeners
         */
        DBRoster.getRosterFor(context).notifyRosterListeners(presence);
    }

    @Override
    public Roster getRoster() throws OXException {
        return DBRoster.getRosterFor(context);
    }

    @Override
    public List<String> getChats() throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final Connection con = databaseService.getReadOnly(context);
        try {
            stmt = con.prepareStatement("SELECT chatId FROM multiChat WHERE cid = ? AND user = ?");
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, session.getUserId());
            rs = stmt.executeQuery();
            final List<String> ids = new LinkedList<String>();
            while (rs.next()) {
                ids.add(rs.getString(1));
            }
            return ids;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(context, con);
        }
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#openChat(java.lang.String, com.openexchange.chat.MessageListener,
     * com.openexchange.chat.ChatUser)
     */
    @Override
    public Chat openChat(final String chatId, final MessageListener listener, final ChatUser member) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    /*
     * (non-Javadoc)
     * @see com.openexchange.chat.ChatAccess#openChat(java.lang.String, com.openexchange.chat.MessageListener,
     * com.openexchange.chat.ChatUser[])
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
