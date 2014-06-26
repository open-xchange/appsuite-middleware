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

package com.openexchange.chat.db;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.rollback;
import gnu.trove.list.TIntList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
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
import com.openexchange.chat.ChatDescription;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.MessageListener;
import com.openexchange.chat.Presence;
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

    private static final int MIN_CHAT_ID = 1000;

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

    private static final boolean DO_CLEAN_UP = false;

    /**
     * Removes associated chat access.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @throws OXException If clean-up fails
     */
    public static void removeDbChatAccess(final int userId, final int contextId) throws OXException {
        final DBChatAccess access = ACCESS_MAP.remove(new Key(userId, contextId));
        if (DO_CLEAN_UP && null != access) {
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
        user.setId(Integer.toString(userId));
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
                stmt = con.prepareStatement("SELECT cm1.chatId FROM chatMember AS cm1 WHERE cid = ? AND (SELECT COUNT(cm2.user) FROM chatMember AS cm2 WHERE cm2.cid = ? AND cm2.chatId = cm1.chatId) < ? AND cm1.user = ? GROUP BY cm1.chatId");
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
            DBChat.removeDBChats(singleChatIds, contextId, con);
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
        DBRoster.getRosterFor(context).updatePresence(getUser(), presence);
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
            stmt = con.prepareStatement("SELECT DISTINCT chatChunk.chatId FROM chatChunk LEFT JOIN chatMember ON chatChunk.chunkId = chatMember.chunkId WHERE chatChunk.cid = ? AND chatMember.user = ? AND chatChunk.chatId IN (SELECT chat.chatId FROM chat WHERE chat.cid = ? AND chat.user = ?)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.setInt(3, contextId);
            stmt.setInt(4, userId);
            rs = stmt.executeQuery();
            final List<String> ids = new LinkedList<String>();
            // TODO: ids.add("default"); // The default chat where all users are participating
            while (rs.next()) {
                ids.add(Integer.toString(rs.getInt(1)));
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
    public Chat getChat(final String chatId) throws OXException {
        final DBChat dbChat = DBChat.getDBChat(DBChatUtility.parseUnsignedInt(chatId), contextId);
        if (null == dbChat) {
            throw ChatExceptionCodes.CHAT_NOT_FOUND.create(chatId);
        }
        return dbChat;
    }

    @Override
    public void updateChat(final ChatDescription chatDescription) throws OXException {
        if (null == chatDescription || !chatDescription.hasAnyAttribute()) {
            return;
        }
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            updateChat(chatDescription, con);
            con.commit();
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

    private void updateChat(final ChatDescription chatDescription, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int pos;
        final int chatId = DBChatUtility.parseUnsignedInt(chatDescription.getChatId());
        /*
         * Update subject
         */
        try {
            final String subject = chatDescription.getSubject();
            if (null != subject) {
                stmt = con.prepareStatement("UPDATE chat SET subject = ? WHERE cid = ? AND chatId = ?");
                pos = 1;
                stmt.setString(pos++, subject);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos, chatId);
                stmt.executeUpdate();
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
        final List<String> newMembers = chatDescription.getNewMembers();
        final List<String> deletedMembers = chatDescription.getDeletedMembers();
        if ((newMembers != null && !newMembers.isEmpty()) || (deletedMembers != null && !deletedMembers.isEmpty())) {
            /*
             * Get current chunkId & obtain exclusive lock
             */
            int currentChunkId = 1;
            pos = 1;
            try {
                stmt = con.prepareStatement("SELECT MAX(chunkId) FROM chatChunk WHERE cid = ? AND chatId = ? FOR UPDATE");
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos, chatId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    currentChunkId = rs.getInt(1);
                }
            } catch (final SQLException e) {
                throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
            /*
             * A set to track already existing users
             */
            final TIntSet users = new TIntHashSet(16);
            try {
                stmt = con.prepareStatement("SELECT user FROM chatMember WHERE cid = ? AND chatId = ? AND chunkId = ?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, chatId);
                stmt.setInt(pos, currentChunkId);
                rs = stmt.executeQuery();
                while (rs.next()) {
                    users.add(rs.getInt(1));
                }
            } catch (final SQLException e) {
                throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
            /*
             * Check if anything to do
             */
            boolean actionNeeded = false;
            if (newMembers != null && !newMembers.isEmpty()) {
                for (final String user : newMembers) {
                    if (!users.contains(DBChatUtility.parseUnsignedInt(user))) {
                        actionNeeded = true;
                        break;
                    }
                }
            }
            if (!actionNeeded && (deletedMembers != null && !deletedMembers.isEmpty())) {
                for (final String user : deletedMembers) {
                    if (users.contains(DBChatUtility.parseUnsignedInt(user))) {
                        actionNeeded = true;
                        break;
                    }
                }
            }
            /*
             * Abort if no further action needed
             */
            if (!actionNeeded) {
                return;
            }
            /*-
             * Action needed:
             *
             * Create new chunk
             */
            final int newChunkId = currentChunkId + 1;
            try {
                stmt = con.prepareStatement("INSERT INTO chatChunk (cid, chatId, chunkId, createdAt) VALUES (?, ?, ?, ?)");
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, chatId);
                stmt.setInt(pos++, newChunkId);
                stmt.setLong(pos, System.currentTimeMillis());
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(stmt);
            }
            /*
             * Insert existing members into new chunk
             */
            try {
                stmt = con.prepareStatement("SELECT user, opMode, lastPoll FROM chatMember WHERE cid = ? AND chatId = ? AND chunkId = ?");
                pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, chatId);
                stmt.setInt(pos, currentChunkId);
                rs = stmt.executeQuery();
                if (rs.next()) { // At least one result
                    PreparedStatement stmt2 = null;
                    try {
                        stmt2 = con.prepareStatement("INSERT INTO chatMember (cid, chatId, chunkId, opMode, user, lastPoll) VALUES (?, ?, ?, ?, ?, ?)");
                        pos = 1;
                        stmt2.setInt(pos++, contextId);
                        stmt2.setInt(pos++, chatId);
                        stmt2.setInt(pos, newChunkId);
                        do {
                            pos = 4;
                            stmt2.setInt(pos++, rs.getInt(2)); // opMode
                            stmt2.setInt(pos++, rs.getInt(1)); // user
                            stmt2.setLong(pos, rs.getLong(3)); //lastPoll
                            stmt2.addBatch();
                        } while (rs.next());
                        stmt2.executeBatch();
                    } finally {
                        closeSQLStuff(stmt2);
                    }
                }
            } catch (final SQLException e) {
                throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(rs, stmt);
            }
            /*
             * Insert new members
             */
            try {
                if (null != newMembers && !newMembers.isEmpty()) {
                    final long now = System.currentTimeMillis();
                    stmt = con.prepareStatement("INSERT INTO chatMember (cid, chatId, chunkId, opMode, lastPoll, user) VALUES (?, ?, ?, ?, ?, ?)");
                    pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, chatId);
                    stmt.setInt(pos++, newChunkId);
                    stmt.setInt(pos++, 0);
                    stmt.setLong(pos++, now);
                    for (final String user : newMembers) {
                        final int userId = DBChatUtility.parseUnsignedInt(user);
                        if (!users.contains(userId)) {
                            stmt.setInt(pos, userId);
                            stmt.addBatch();
                            users.add(userId);
                        }
                    }
                    stmt.executeBatch();
                }
            } catch (final SQLException e) {
                throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(stmt);
            }
            /*
             * Delete members
             */
            try {
                if (null != deletedMembers && !deletedMembers.isEmpty()) {
                    stmt = con.prepareStatement("DELETE FROM chatMember WHERE cid = ? AND chatId = ? AND chunkId = ? AND user = ?");
                    pos = 1;
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, chatId);
                    stmt.setInt(pos++, newChunkId);
                    for (final String user : deletedMembers) {
                        stmt.setInt(pos, DBChatUtility.parseUnsignedInt(user));
                        stmt.addBatch();
                    }
                    stmt.executeBatch();
                }
            } catch (final SQLException e) {
                throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(stmt);
            }
        }
    }

    @Override
    public Chat openChat(final String chatId, final MessageListener listener, final ChatUser member) throws OXException {
        return openChat(chatId, listener, new ChatUser[] { member });
    }

    @Override
    public Chat openChat(final String chatId, final MessageListener listener, final ChatUser... members) throws OXException {
        String chid = chatId;
        if (null == chid) {
            chid = Integer.toString(getService(IDGeneratorService.class).getId(PACKAGE_NAME, contextId, MIN_CHAT_ID));
        }
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            final Chat chat = openChat0(chid, listener, members, con);
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
            chid = DBChatUtility.parseUnsignedInt(chatId);
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
        final long now = System.currentTimeMillis();
        try {
            stmt = con.prepareStatement("INSERT INTO chat (cid, user, chatId, subject, createdAt) VALUES (?, ?, ?, ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, chid);
            stmt.setString(pos++, "");
            stmt.setLong(pos, now);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            stmt = con.prepareStatement("INSERT INTO chatChunk (cid, chatId, chunkId, createdAt) VALUES (?, ?, ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chid);
            stmt.setInt(pos++, 1);
            stmt.setLong(pos, now);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            pos = 1;
            stmt = con.prepareStatement("INSERT INTO chatMember (cid, chatId, chunkId, opMode, lastPoll, user) VALUES (?, ?, ?, ?, ?, ?)");
            stmt.setInt(pos++, contextId);

            stmt.setInt(pos++, chid);
            stmt.setInt(pos++, 1);
            stmt.setInt(pos++, 0);
            stmt.setLong(pos++, now);

            /*
             * This user
             */
            stmt.setInt(pos, userId);
            stmt.addBatch();
            /*
             * Others
             */
            for (final ChatUser chatUser : members) {
                final int chatUserId = DBChatUtility.parseUnsignedInt(chatUser.getId());
                if (chatUserId != userId) {
                    stmt.setInt(pos, chatUserId);
                    stmt.addBatch();
                }
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
