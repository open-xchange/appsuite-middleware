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
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.chat.Chat;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.Message;
import com.openexchange.chat.MessageListener;
import com.openexchange.chat.Packet;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link DBChat}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChat implements Chat {

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DBChat.class));

    private static final ConcurrentMap<Key, DBChat> CHAT_MAP = new ConcurrentHashMap<Key, DBChat>();

    /**
     * Gets the chat for specified arguments
     * 
     * @param chatId The chat identifier
     * @param contextId The context identifier
     * @return The chat or <code>null</code> if absent
     */
    public static DBChat optDBChat(final int chatId, final int contextId) {
        return CHAT_MAP.get(new Key(chatId, contextId));
    }

    /**
     * Gets the chat for specified arguments
     * 
     * @param chatId The chat identifier
     * @param contextId The context identifier
     * @return The chat
     */
    public static DBChat getDBChat(final int chatId, final int contextId) {
        final Key key = new Key(chatId, contextId);
        DBChat dbChat = CHAT_MAP.get(key);
        if (null == dbChat) {
            final DBChat newChat = new DBChat(chatId, contextId);
            dbChat = CHAT_MAP.putIfAbsent(key, newChat);
            if (null == dbChat) {
                dbChat = newChat;
            }
        }
        return dbChat;
    }
    
    /**
     * Removes specified chat.
     * 
     * @param chatId The chat identifier
     * @param contextId The context identifier
     * @throws OXException If removal fails
     */
    public static void removeDBChat(final int chatId, final int contextId) throws OXException {
        CHAT_MAP.remove(new Key(chatId, contextId));
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            final TIntList list = new TIntArrayList(1);
            list.add(chatId);
            dropChats(list, contextId, con);
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

    /**
     * Drops all data associated with specified chats.
     * 
     * @param chatIds The chat identifiers
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws OXException If operation fails
     */
    public static void dropChats(final TIntList chatIds, final int contextId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        int pos;
        /*
         * Drop chat message entries for each chat identifier
         */
        try {
            stmt = con.prepareStatement("DELETE FROM chatMessage WHERE cid = ? AND chatId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            for (final TIntIterator iterator = chatIds.iterator(); iterator.hasNext();) {
                stmt.setInt(pos, iterator.next());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
        /*
         * Drop chat member entries for each chat identifier
         */
        try {
            stmt = con.prepareStatement("DELETE FROM chatMember WHERE cid = ? AND chatId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            for (final TIntIterator iterator = chatIds.iterator(); iterator.hasNext();) {
                stmt.setInt(pos, iterator.next());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
        /*
         * Drop chat entry for each chat identifier
         */
        try {
            stmt = con.prepareStatement("DELETE FROM chat WHERE cid = ? AND chatId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            for (final TIntIterator iterator = chatIds.iterator(); iterator.hasNext();) {
                stmt.setInt(pos, iterator.next());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }
    

    private final int chatId;

    private final List<MessageListener> messageListeners;

    private final int contextId;

    /**
     * Initializes a new {@link DBChat}.
     */
    public DBChat(final int chatId, final int contextId) {
        super();
        this.chatId = chatId;
        this.contextId = contextId;
        messageListeners = new CopyOnWriteArrayList<MessageListener>();
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
        try {
            final DatabaseService databaseService = getDatabaseService();
            final Connection con = databaseService.getReadOnly(contextId);
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT subject FROM chat WHERE cid = ? AND chatId = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, chatId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return null;
                }
                return rs.getString(1);
            } catch (final SQLException e) {
                LOG.error("A SQL error occurred.", e);
                return null;
            } finally {
                databaseService.backReadOnly(contextId, con);
            }
        } catch (final OXException e) {
            LOG.error("An OX error occurred.", e);
            return null;
        }
    }

    @Override
    public List<String> getMembers() throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT user FROM chatMember WHERE cid = ? AND chatId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, chatId);
            rs = stmt.executeQuery();
            final List<String> ret = new LinkedList<String>();
            while (rs.next()) {
                ret.add(String.valueOf(rs.getInt(1)));
            }
            return ret;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public void join(final String user) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            join(user, con);
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

    private void join(final String user, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int pos;
        try {
            stmt = con.prepareStatement("SELECT user FROM chatMember WHERE cid = ? AND chatId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                throw ChatExceptionCodes.CHAT_MEMBER_ALREADY_EXISTS.create(user, Integer.valueOf(chatId));
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        rs = null;
        try {
            stmt = con.prepareStatement("INSERT INTO chatMember (cid, user, chatId, opMode) VALUES (?, ?, ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, Integer.parseInt(user));
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos, 0);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public void part(final String user) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            part(user, con);
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

    private void part(final String user, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        int pos;
        try {
            stmt = con.prepareStatement("DELETE FROM chatMember WHERE cid = ? AND user = ? AND chatId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, Integer.parseInt(user));
            stmt.setInt(pos, chatId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void post(final Packet packet) throws OXException {
        if (packet instanceof Message) {
            final DatabaseService databaseService = getDatabaseService();
            final Connection con = databaseService.getWritable(contextId);
            try {
                con.setAutoCommit(false);
                post((Message) packet, con);
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
    }

    private void post(final Message message, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        int pos;
        try {
            stmt = con.prepareStatement("INSERT INTO chatMessage (cid, user, chatId, messageId, message, createdAt) VALUES (?, ?, ?, " + DBChatUtility.getUnhexReplaceString() + ", ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, Integer.parseInt(message.getFrom().getId()));
            stmt.setInt(pos++, chatId);
            stmt.setString(pos++, UUID.randomUUID().toString());
            stmt.setString(pos++, message.getText());
            stmt.setLong(pos, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
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
    public void setSubject(final String subject) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            setSubject(subject, con);
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

    private void setSubject(final String subject, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        int pos;
        try {
            stmt = con.prepareStatement("UPDATE chat SET subject = ? WHERE cid = ? AND chatId = ?");
            pos = 1;
            stmt.setString(pos++, subject == null ? "" : subject);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos, chatId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private static DatabaseService getDatabaseService() throws OXException {
        final DatabaseService databaseService = DBChatServiceLookup.getService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        return databaseService;
    }

    private static final class Key {

        private final int contextId;

        private final int chatId;

        private final int hash;

        public Key(final int chatId, final int contextId) {
            super();
            this.chatId = chatId;
            this.contextId = contextId;
            final int prime = 31;
            int result = 1;
            result = prime * result + chatId;
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
            if (chatId != other.chatId) {
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
            sb.append(", chatId = ");
            sb.append(chatId);
            sb.append(" )");
            return sb.toString();
        }

    } // End of class Key

}
