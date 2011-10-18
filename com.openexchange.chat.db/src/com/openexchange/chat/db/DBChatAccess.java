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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.chat.Chat;
import com.openexchange.chat.ChatAccess;
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
import com.openexchange.id.IDGeneratorService;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;

/**
 * {@link DBChatAccess}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChatAccess implements ChatAccess {

    private static final ConcurrentMap<Key, DBChatAccess> ACCESS_MAP = new ConcurrentHashMap<Key, DBChatAccess>();

    private static final String PACKAGE_NAME = "com.openexchange.chat.db";

    /**
     * Removes associated chat access.
     * 
     * @param session The session
     * @throws OXException If clean-up fails
     */
    public static void removeDbChatAccess(final Session session) throws OXException {
        removeDbChatAccess(session.getUserId(), session.getContextId());
    }

    /**
     * Removes associated chat access.
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If clean-up fails
     */
    public static void removeDbChatAccess(final int userId, final int contextId) throws OXException {
        final DBChatAccess access = ACCESS_MAP.remove(new Key(userId, contextId));
        if (null != access) {
            access.cleanUp();
        }
    }

    /**
     * Gets the database chat access for specified arguments.
     * 
     * @param session The session
     * @return The access
     * @throws OXException If initialization fails
     */
    public static DBChatAccess getDbChatAccess(final Session session) throws OXException {
        return getDbChatAccess(session.getUserId(), session.getContextId());
    }

    /**
     * Gets the database chat access for specified arguments.
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The access
     * @throws OXException If initialization fails
     */
    public static DBChatAccess getDbChatAccess(final int userId, final int contextId) throws OXException {
        final Key key = new Key(userId, contextId);
        DBChatAccess dbChatAccess = ACCESS_MAP.get(key);
        if (null == dbChatAccess) {
            final DBChatAccess newAccess = new DBChatAccess(userId, contextId);
            dbChatAccess = ACCESS_MAP.putIfAbsent(key, newAccess);
            if (null == dbChatAccess) {
                dbChatAccess = newAccess;
            }
        }
        return dbChatAccess;
    }

    /*-
     * ------------------------------------
     */

    private final int userId;

    private final ChatUser user;

    private final Context context;

    private final int contextId;

    /**
     * Initializes a new {@link DBChatAccess}.
     * 
     * @throws OXException If init fails
     */
    private DBChatAccess(final int userId, final int contextId) throws OXException {
        super();
        this.userId = userId;
        context = DBChatServiceLookup.getService(ContextService.class).getContext(contextId);
        final ChatUserImpl user = new ChatUserImpl();
        user.setId(String.valueOf(userId));
        user.setName(getUserName(userId));
        this.user = user;
        this.contextId = contextId;
    }

    private String getUserName(final int userId) throws OXException {
        return DBChatServiceLookup.getService(UserService.class).getUser(userId, context).getDisplayName();
    }

    /**
     * Clean up any remaining single-chats/occurrences.
     * 
     * @throws OXException If an error occurs
     */
    private void cleanUp() throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(context);
        try {
            con.setAutoCommit(false);
            cleanUp(con);
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(con);
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            autocommit(con);
            databaseService.backWritable(context, con);
        }
    }

    /**
     * Clean up any remaining single-chats and user occurrences.
     * 
     * @throws OXException If an error occurs
     */
    private void cleanUp(final Connection con) throws OXException {
        final TIntList singleChatIds = new TIntLinkedList();
        PreparedStatement stmt = null;
        int pos;
        /*
         * Determine single chats. Those chats with number of members less than 3 that associated user participates with
         */
        {
            ResultSet rs = null;
            try {
                stmt =
                    con.prepareStatement("SELECT cm1.chatId FROM chatMember AS cm1 WHERE cid = ? AND (SELECT COUNT(cm2.user) FROM chatMember AS cm2 WHERE cm2.cid = ? AND cm2.chatId = cm1.chatId) < ? AND cm1.user = ? GROUP BY cm1.chatId");
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, 3); // Less than 3
                stmt.setInt(pos, userId);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    singleChatIds.add(rs.getInt(1));
                }
            } catch (final SQLException e) {
                throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
        }
        /*
         * Remove user from chat members
         */
        try {
            stmt = con.prepareStatement("DELETE FROM chatMember WHERE cid = ? AND user = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, userId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
        /*
         * Drop determined chats
         */
        if (!singleChatIds.isEmpty()) {
            DBChat.dropChats(singleChatIds, contextId, con);
        }
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
                final Mode mode = presence.getMode();
                stmt.setInt(pos++, (null == mode ? Mode.AVAILABLE : mode).ordinal());
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
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
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
            stmt = con.prepareStatement("SELECT chatId FROM chat WHERE cid = ?");
            stmt.setInt(1, contextId);
            rs = stmt.executeQuery();
            final List<String> ids = new LinkedList<String>();
            while (rs.next()) {
                ids.add(String.valueOf(rs.getInt(1)));
            }
            return ids;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            databaseService.backReadOnly(context, con);
        }
    }

    @Override
    public Chat openChat(final String chatId, final MessageListener listener, final ChatUser member) throws OXException {
        return openChat(chatId, listener, member);
    }

    @Override
    public Chat openChat(final String chatId, final MessageListener listener, final ChatUser... members) throws OXException {
        String chid = chatId;
        if (null == chid) {
            chid = String.valueOf(getService(IDGeneratorService.class).getId(PACKAGE_NAME, contextId));
        }
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            final Chat chat = openChat0(chatId, listener, members, con);
            con.commit();
            return chat;
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            DBUtils.rollback(con);
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            databaseService.backWritable(contextId, con);
        }
    }

    private Chat openChat0(final String chatId, final MessageListener listener, final ChatUser[] members, final Connection con) throws OXException {
        int chid;
        if (null == chatId) {
            chid = getService(IDGeneratorService.class).getId(PACKAGE_NAME, contextId);
        } else {
            chid = Integer.parseInt(chatId);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int pos;
        try {
            stmt = con.prepareStatement("SELECT chatId FROM chat WHERE cid = ? AND chatId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chid);
            rs = stmt.executeQuery();
            if (rs.next()) {
                throw ChatExceptionCodes.CHAT_ALREADY_EXISTS.create(Integer.valueOf(chid));
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        rs = null;
        try {
            stmt = con.prepareStatement("INSERT INTO chat (cid, user, chatId, subject) VALUES (?, ?, ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, chid);
            stmt.setString(pos, "");
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            stmt = con.prepareStatement("INSERT INTO chatMember (cid, user, chatId, opMode) VALUES (?, ?, ?, ?)");
            stmt.setInt(1, contextId);
            stmt.setInt(3, chid);
            stmt.setInt(4, 0);
            /*
             * This user
             */
            stmt.setInt(2, userId);
            stmt.addBatch();
            /*
             * Others
             */
            for (final ChatUser chatUser : members) {
                stmt.setInt(2, Integer.parseInt(chatUser.getId()));
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        /*
         * Get associated chat
         */
        final DBChat chat = DBChat.getDBChat(chid, contextId);
        chat.addMessageListener(listener);
        return chat;
    }

    private static DatabaseService getDatabaseService() throws OXException {
        final DatabaseService databaseService = DBChatServiceLookup.getService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        return databaseService;
    }

    private static <S> S getService(final Class<? extends S> clazz) throws OXException {
        final S service = DBChatServiceLookup.getService(clazz);
        if (null == service) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(clazz.getName());
        }
        return service;
    }

    private static final class Key {

        private final int contextId;

        private final int userId;

        private final int hash;

        public Key(final int userId, final int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + userId;
            result = prime * result + contextId;
            this.hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Key)) {
                return false;
            }
            final Key other = (Key) obj;
            if (userId != other.userId) {
                return false;
            }
            if (contextId != other.contextId) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Key ( ");
            sb.append("contextId = ");
            sb.append(contextId);
            sb.append(", userId = ");
            sb.append(userId);
            sb.append(" )");
            return sb.toString();
        }

    } // End of class Key

}
