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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

import static com.openexchange.chat.db.DBChatUtility.toUUID;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.ConcurrentTIntObjectHashMap;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.procedure.TIntObjectProcedure;
import java.sql.Connection;
import java.sql.DataTruncation;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.logging.Log;
import com.openexchange.chat.Chat;
import com.openexchange.chat.ChatExceptionCodes;
import com.openexchange.chat.ChatStrings;
import com.openexchange.chat.ChatUser;
import com.openexchange.chat.Message;
import com.openexchange.chat.MessageDescription;
import com.openexchange.chat.MessageListener;
import com.openexchange.chat.Packet;
import com.openexchange.chat.util.ChatUserImpl;
import com.openexchange.chat.util.MessageImpl;
import com.openexchange.context.ContextService;
import com.openexchange.crypto.CryptoService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.log.LogFactory;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.user.UserService;

/**
 * {@link DBChat}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DBChat implements Chat {

    protected static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(DBChat.class));

    private static abstract class SafeRunnable implements Runnable {

        protected SafeRunnable() {
            super();
        }

        @Override
        public void run() {
            try {
                execute();
            } catch (final Exception e) {
                LOG.error("Task execution failed.", e);
            }
        }

        /**
         * Executes this {@link SafeRunnable}'s task.
         *
         * @throws Exception If an error occurs
         */
        protected abstract void execute() throws Exception;

    }

    private static abstract class SafeTIntObjectProcedure<V> implements TIntObjectProcedure<V> {

        protected SafeTIntObjectProcedure() {
            super();
        }

        @Override
        public boolean execute(final int key, final V value) {
            try {
                process(key, value);
            } catch (final Exception e) {
                LOG.error("Procedure iteration failed.", e);
            }
            // Always return true to continue iteration
            return true;
        }

        /**
         * Handles specified key-value-pair.
         *
         * @param key The key
         * @param value The value
         * @throws Exception If an error occurs
         */
        protected abstract void process(int key, V value) throws Exception;
    }

    protected static final ConcurrentTIntObjectHashMap<ConcurrentTIntObjectHashMap<DBChat>> CHAT_MAP = new ConcurrentTIntObjectHashMap<ConcurrentTIntObjectHashMap<DBChat>>();

    protected static final List<MessageListener> GLOBAL_LISTENERS = new CopyOnWriteArrayList<MessageListener>();

    private static final class ChatProcedure extends SafeTIntObjectProcedure<DBChat> {

        private final Connection con;

        protected ChatProcedure(final Connection con) {
            this.con = con;
        }

        @Override
        public void process(final int chatId, final DBChat dbChat) throws OXException {
            if (GLOBAL_LISTENERS.isEmpty() && dbChat.messageListeners.isEmpty()) {
                return;
            }
            final List<Message> messages = dbChat.getNewMessages(con);
            if (!messages.isEmpty()) {
                for (final Message message : messages) {
                    for (final MessageListener ml : GLOBAL_LISTENERS) {
                        ml.handleMessage(dbChat, message);
                    }
                    for (final MessageListener ml : dbChat.messageListeners) {
                        ml.handleMessage(dbChat, message);
                    }
                }
            }
        }
    }

    private static final AtomicReference<CryptoService> CRYPTO_SERVICE_REF = new AtomicReference<CryptoService>();

    private static CryptoService getCryptoService() throws OXException {
        final CryptoService cs = CRYPTO_SERVICE_REF.get();
        if (null == cs) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(CryptoService.class.getName());
        }
        return cs;
    }

    /**
     * Sets the crypto service reference.
     *
     * @param cryptoService The crypto service
     */
    public static void setCryptoService(final CryptoService cryptoService) {
        CRYPTO_SERVICE_REF.set(cryptoService);
    }

    /**
     * Adds specified message listener.
     *
     * @param messageListener
     */
    public static void addMessageListenerStatic(final MessageListener messageListener) {
        if (null == messageListener) {
            return;
        }
        GLOBAL_LISTENERS.add(messageListener);
    }

    /**
     * Removes specified message listener.
     *
     * @param messageListener
     */
    public static void removeMessageListenerStatic(final MessageListener messageListener) {
        if (null == messageListener) {
            return;
        }
        GLOBAL_LISTENERS.remove(messageListener);
    }

    private static final AtomicReference<ScheduledTimerTask> TIMER_TASK = new AtomicReference<ScheduledTimerTask>();

    /**
     * Starts-up chat resources.
     */
    public static void startUp() {
        final TimerService timerService = DBChatServiceLookup.getService(TimerService.class);
        final Runnable task = new SafeRunnable() {

            private final TIntObjectProcedure<ConcurrentTIntObjectHashMap<DBChat>> procedure = new SafeTIntObjectProcedure<ConcurrentTIntObjectHashMap<DBChat>>() {

                @Override
                public void process(final int contextId, final ConcurrentTIntObjectHashMap<DBChat> map) {
                    if (map.isEmpty()) {
                        return;
                    }
                    final Runnable subtask = new SafeRunnable() {

                        @Override
                        public void execute() throws OXException {
                            final DatabaseService databaseService = getDatabaseService();
                            final Connection con = databaseService.getReadOnly(contextId);
                            try {
                                map.forEachEntry(new ChatProcedure(con));
                            } finally {
                                databaseService.backReadOnly(contextId, con);
                            }
                        }
                    };
                    ThreadPools.getThreadPool().submit(ThreadPools.trackableTask(subtask));
                }
            };

            @Override
            public void execute() {
                if (!CHAT_MAP.isEmpty()) {
                    CHAT_MAP.forEachEntry(procedure);
                }
            }
        };
        TIMER_TASK.set(timerService.scheduleWithFixedDelay(task, 5000, 5000));
    }

    /**
     * Shuts-down chat resources.
     */
    public static void shutDown() {
        final ScheduledTimerTask timerTask = TIMER_TASK.get();
        if (null != timerTask) {
            timerTask.cancel(false);
            TIMER_TASK.set(null);
        }
        final ConcurrentTIntObjectHashMap<ConcurrentTIntObjectHashMap<DBChat>> clone = new ConcurrentTIntObjectHashMap<ConcurrentTIntObjectHashMap<DBChat>>();
        CHAT_MAP.forEachEntry(new TIntObjectProcedure<ConcurrentTIntObjectHashMap<DBChat>>() {

            @Override
            public boolean execute(final int contextId, final ConcurrentTIntObjectHashMap<DBChat> map) {
                clone.put(contextId, map);
                return true;
            }
        });
        CHAT_MAP.clear();
        clone.forEachEntry(new TIntObjectProcedure<ConcurrentTIntObjectHashMap<DBChat>>() {

            @Override
            public boolean execute(final int contextId, final ConcurrentTIntObjectHashMap<DBChat> map) {
                final TIntList chatIds = new TIntArrayList();
                chatIds.add(map.keys());
                try {
                    removeDBChats(chatIds, contextId);
                } catch (final OXException e) {
                    // Ignore
                } catch (final Exception e) {
                    // Ignore
                }
                return true;
            }
        });
    }

    /**
     * Gets the chat for specified arguments
     *
     * @param chatId The chat identifier
     * @param contextId The context identifier
     * @return The chat or <code>null</code> if absent
     */
    public static DBChat optDBChat(final int chatId, final int contextId) {
        final ConcurrentTIntObjectHashMap<DBChat> map = CHAT_MAP.get(contextId);
        return null == map ? null : map.get(chatId);
    }

    /**
     * Gets the chat for specified arguments
     *
     * @param chatId The chat identifier
     * @param contextId The context identifier
     * @return The chat
     */
    public static DBChat getDBChat(final int chatId, final int contextId) {
        ConcurrentTIntObjectHashMap<DBChat> map = CHAT_MAP.get(contextId);
        if (null == map) {
            final ConcurrentTIntObjectHashMap<DBChat> newMap = new ConcurrentTIntObjectHashMap<DBChat>();
            map = CHAT_MAP.putIfAbsent(contextId, newMap);
            if (null == map) {
                map = newMap;
            }
        }
        DBChat dbChat = map.get(chatId);
        if (null == dbChat) {
            final DBChat newChat = new DBChat(chatId, contextId);
            dbChat = map.putIfAbsent(chatId, newChat);
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
        final ConcurrentTIntObjectHashMap<DBChat> map = CHAT_MAP.get(contextId);
        if (null != map) {
            map.remove(chatId);
        }
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            final TIntList list = new TIntArrayList(1);
            list.add(chatId);
            removeDBChats(list, contextId, con);
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
     * Removes specified chat.
     *
     * @param chatIds The chat identifiers
     * @param contextId The context identifier
     * @throws OXException If removal fails
     */
    public static void removeDBChats(final TIntList chatIds, final int contextId) throws OXException {
        /*
         * Delete from map
         */
        final ConcurrentTIntObjectHashMap<DBChat> map = CHAT_MAP.get(contextId);
        if (null != map) {
            for (final TIntIterator iterator = chatIds.iterator(); iterator.hasNext();) {
                map.remove(iterator.next());
            }
        }
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            removeDBChats(chatIds, contextId, con);
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
    public static void removeDBChats(final TIntList chatIds, final int contextId, final Connection con) throws OXException {
        /*
         * Delete from map
         */
        final ConcurrentTIntObjectHashMap<DBChat> map = CHAT_MAP.get(contextId);
        if (null != map) {
            for (final TIntIterator iterator = chatIds.iterator(); iterator.hasNext();) {
                map.remove(iterator.next());
            }
        }
        /*
         * Clean tables
         */
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
        /*
         * Drop chat chunks
         */
        try {
            stmt = con.prepareStatement("DELETE FROM chatChunk WHERE cid = ? AND chatId = ?");
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

    /*-
     * -------------------------------------- MEMBER STUFF --------------------------------------
     */

    private final int chatId;

    private final String sChatId;

    protected final List<MessageListener> messageListeners;

    private final int contextId;

    private volatile Context context;

    private volatile long lastChecked;

    private final ConcurrentTIntObjectHashMap<String> userNameCache;

    private final boolean secureMessaging;

    private volatile Date createdAt;

    /**
     * Initializes a new {@link DBChat}.
     */
    private DBChat(final int chatId, final int contextId) {
        super();
        userNameCache = new ConcurrentTIntObjectHashMap<String>(128);
        this.chatId = chatId;
        sChatId = Integer.toString(chatId);
        this.contextId = contextId;
        messageListeners = new CopyOnWriteArrayList<MessageListener>();
        secureMessaging = false;
    }

    /**
     * Gets lazy initialized context.
     *
     * @return The context
     * @throws OXException If initializing context fails
     */
    private Context getContext() throws OXException {
        Context tmp = context;
        if (null == tmp) {
            synchronized (this) {
                tmp = context;
                if (null == context) {
                    tmp = DBChatServiceLookup.getService(ContextService.class).getContext(contextId);
                    context = tmp;
                }
            }
        }
        return tmp;
    }

    /**
     * Prepares given text queried from database.
     *
     * @param text The queried text
     * @return The prepared text ready for being returned to caller
     * @throws OXException If preparing text fails
     */
    private String prepareSelect(final String text) throws OXException {
        return secureMessaging ? getCryptoService().decrypt(text, sChatId) : text;
    }

    /**
     * Prepares given text intended for being written to database.
     *
     * @param text The text to insert/update
     * @return The prepared text ready for being written to database
     * @throws OXException If preparing text fails
     */
    private String prepareInsert(final String text) throws OXException {
        return secureMessaging ? getCryptoService().encrypt(text, sChatId) : text;
    }

    /**
     * Checks for newly arrived messages in this chat.
     *
     * @param con The connection to use
     * @return Newly arrived messages (sorted by time stamp) or an empty list
     * @throws OXException If check fails
     */
    protected List<Message> getNewMessages(final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int chunkId = 1;
        try {
            stmt = con.prepareStatement("SELECT MAX(chunkId) FROM chatChunk WHERE chatId = ?");
            stmt.setInt(1, chatId);
            rs = stmt.executeQuery();
            if (rs.last()) {
                chunkId = rs.getInt(1);
            }
        } catch (final SQLException e) {
            ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            stmt = con.prepareStatement("SELECT user, messageId, message, createdAt FROM chatMessage WHERE cid = ? AND chatId = ? AND chunkId = ? AND createdAt > ? ORDER BY createdAt");
            int pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos++, chunkId);
            stmt.setLong(pos, lastChecked);
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final Context context = getContext();
            final List<Message> list = new ArrayList<Message>();
            long lc = lastChecked;
            do {
                final MessageImpl message = new MessageImpl();
                pos = 1;
                final int userId = rs.getInt(pos++);
                message.setFrom(new ChatUserImpl(Integer.toString(userId), getUserName(userId, context)));
                message.setPacketId(toUUID(rs.getBytes(pos++)).toString());
                message.setText(prepareSelect(rs.getString(pos++)));
                final long createdAt = rs.getLong(pos);
                message.setTimeStamp(new Date(createdAt));
                if (createdAt > lc) {
                    lc = createdAt;
                }
                list.add(message);
            } while (rs.next());
            if (lc > lastChecked) {
                lastChecked = lc;
            }
            return list;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private String getUserName(final int userId, final Context context) throws OXException {
        String displayName = userNameCache.get(userId);
        if (null == displayName) {
            final String dn = DBChatServiceLookup.getService(UserService.class).getUser(userId, context).getDisplayName();
            displayName = userNameCache.putIfAbsent(userId, dn);
            if (null == displayName) {
                displayName = dn;
            }
        }
        return displayName;
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
        return sChatId;
    }

    @Override
    public Date getTimeStamp() {
        // Lazy initialization
        Date tmp = createdAt;
        if (null == tmp) {
            synchronized (this) {
                tmp = createdAt;
                if (null == createdAt) {
                    tmp = getCreatedAt();
                    createdAt = tmp;
                }
            }
        }
        return tmp;
    }

    private Date getCreatedAt() {
        try {
            final DatabaseService databaseService = getDatabaseService();
            final Connection con = databaseService.getReadOnly(contextId);
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement("SELECT createdAt FROM chat WHERE cid = ? AND chatId = ?");
                stmt.setInt(1, contextId);
                stmt.setInt(2, chatId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    return null;
                }
                return new Date(rs.getLong(1));
            } catch (final SQLException e) {
                LOG.error("A SQL error occurred.", e);
                return null;
            } finally {
                closeSQLStuff(rs, stmt);
                databaseService.backReadOnly(contextId, con);
            }
        } catch (final OXException e) {
            LOG.error("An OX error occurred.", e);
            return null;
        } catch (final RuntimeException e) {
            LOG.error("A runtime error occurred.", e);
            return null;
        }
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
                closeSQLStuff(rs, stmt);
                databaseService.backReadOnly(contextId, con);
            }
        } catch (final OXException e) {
            LOG.error("An OX error occurred.", e);
            return null;
        } catch (final RuntimeException e) {
            LOG.error("A runtime error occurred.", e);
            return null;
        }
    }

    @Override
    public List<String> getMembers() throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getReadOnly(contextId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        /*
         * Get current chunkId
         */
        int currentChunkId = 1;
        try {
            stmt = con.prepareStatement("SELECT MAX(chunkId) FROM chatChunk WHERE cid = ? AND chatId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, chatId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                currentChunkId = rs.getInt(1);
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            stmt = con.prepareStatement("SELECT user FROM chatMember WHERE cid = ? AND chatId = ? AND chunkId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, chatId);
            stmt.setInt(3, currentChunkId);
            rs = stmt.executeQuery();
            final List<String> ret = new LinkedList<String>();
            while (rs.next()) {
                ret.add(Integer.toString(rs.getInt(1)));
            }
            return ret;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
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
        int chunkId = 1;
        try {
            stmt = con.prepareStatement("SELECT MAX(chunkId) FROM chatChunk WHERE cid = ? AND chatId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, chatId);
            rs = stmt.executeQuery();
            if (rs.last()) {
                chunkId = rs.getInt(1);
            }
        } catch (final SQLException e) {
            ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            stmt = con.prepareStatement("SELECT user FROM chatMember WHERE cid = ? AND chatId = ? AND chunkId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos, chunkId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                final int currentUser = rs.getInt(1);
                if (currentUser == DBChatUtility.parseUnsignedInt(user)) {
                    throw ChatExceptionCodes.CHAT_MEMBER_ALREADY_EXISTS.create(user, Integer.valueOf(chatId));
                }
            }
        } catch (final NumberFormatException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        rs = null;

        final long currentTime = System.currentTimeMillis();
        final Message userJoined = createJoinMessage(user);
        post(userJoined, con);

        try {
            stmt = con.prepareStatement("INSERT INTO chatChunk (cid, chatId, chunkId, createdAt) VALUES (?, ?, ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos++, ++chunkId);
            stmt.setLong(pos, currentTime);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            stmt = con.prepareStatement("INSERT INTO chatMember (cid, user, chatId, chunkId, opModem, lastPoll) VALUES (?, ?, ?, ?, ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, DBChatUtility.parseUnsignedInt(user));
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos++, chunkId);
            stmt.setInt(pos++, 0);
            stmt.setLong(pos, currentTime);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }

        try {
            stmt = con.prepareStatement("SELECT user, opMode, lastPoll FROM chatMember WHERE cid = ? AND chatId = ? AND chunkId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos, chunkId - 1);
            rs = stmt.executeQuery();
            if (rs.next()) { // At least one result
                PreparedStatement stmt2 = null;
                try {
                    stmt2 = con.prepareStatement("INSERT INTO chatMember (cid, chatId, chunkId, opMode, user, lastPoll) VALUES (?, ?, ?, ?, ?, ?)");
                    pos = 1;
                    stmt2.setInt(pos++, contextId);
                    stmt2.setInt(pos++, chatId);
                    stmt2.setInt(pos, chunkId);
                    do {
                        pos = 4;
                        stmt2.setInt(pos++, rs.getInt(2)); // opMode
                        stmt2.setInt(pos++, rs.getInt(1)); // user
                        stmt2.setLong(pos, rs.getLong(3)); // lastPoll
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
        ResultSet rs = null;
        int pos;
        int chunkId = 1;
        boolean dropChat = false;
        try {
            stmt = con.prepareStatement("SELECT MAX(chunkId) FROM chatChunk WHERE cid = ? AND chatId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, chatId);
            rs = stmt.executeQuery();
            if (rs.last()) {
                chunkId = rs.getInt(1);
            }
        } catch (final SQLException e) {
            ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            stmt = con.prepareStatement("DELETE FROM chatMember WHERE cid = ? AND user = ? AND chatId = ? AND chunkId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, DBChatUtility.parseUnsignedInt(user));
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos, chunkId);
            stmt.executeUpdate();
            closeSQLStuff(stmt);
            rs = null;
            stmt = null;
            /*
             * Drop chat, too
             */
            final TIntList list = new TIntArrayList(1);
            list.add(chatId);
            DBChat.removeDBChats(list, contextId, con);
            stmt = con.prepareStatement("SELECT COUNT(cm.user) FROM chatMember AS cm WHERE cm.cid = ? AND cm.chatId = ? AND chunkId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos, chunkId);
            rs = stmt.executeQuery();
            rs.next();
            final int count = rs.getInt(1);
            if (count == 0) {
                dropChat = true;
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;
        }

        if (dropChat) {
            return;
        }

        final Message userLeft = createLeftMessage(user);
        post(userLeft, con);

        try {
            stmt = con.prepareStatement("INSERT INTO chatChunk (cid, chatId, chunkId, createdAt) VALUES (?, ?, ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos++, ++chunkId);
            stmt.setLong(pos, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }

        try {
            stmt = con.prepareStatement("SELECT user, opMode, lastPoll FROM chatMember WHERE cid = ? AND chatId = ? AND chunkId = ?");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos, chunkId - 1);
            rs = stmt.executeQuery();
            if (rs.next()) { // At least one result
                PreparedStatement stmt2 = null;
                try {
                    stmt2 = con.prepareStatement("INSERT INTO chatMember (cid, chatId, chunkId, opMode, user, lastPoll) VALUES (?, ?, ?, ?, ?, ?)");
                    pos = 1;
                    stmt2.setInt(pos++, contextId);
                    stmt2.setInt(pos++, chatId);
                    stmt2.setInt(pos, chunkId);
                    do {
                        pos = 4;
                        stmt2.setInt(pos++, rs.getInt(2)); // opMode
                        stmt2.setInt(pos++, rs.getInt(1)); // user
                        stmt2.setLong(pos, rs.getLong(3)); // lastPoll
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
        int chunkId = 1;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT MAX(chunkId) FROM chatChunk WHERE cid = ? AND chatId = ?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, chatId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                chunkId = rs.getInt(1);
            }
        } catch (final SQLException e) {
            ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
        try {
            stmt = con.prepareStatement("INSERT INTO chatMessage (cid, user, chatId, chunkId, messageId, message, createdAt) VALUES (?, ?, ?, ?, " + DBChatUtility.getUnhexReplaceString() + ", ?, ?)");
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, DBChatUtility.parseUnsignedInt(message.getFrom().getId()));
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos++, chunkId);
            stmt.setString(pos++, UUID.randomUUID().toString());
            stmt.setString(pos++, prepareInsert(message.getText()));
            stmt.setLong(pos, System.currentTimeMillis());
            stmt.executeUpdate();
        } catch (final DataTruncation e) {
            throw ChatExceptionCodes.MESSAGE_TOO_LONG.create(e);
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public void updateMessage(final MessageDescription messageDesc) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            updateMessage(messageDesc, con);
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

    private void updateMessage(final MessageDescription messageDesc, final Connection con) throws OXException {
        if (null == messageDesc || !messageDesc.hasAnyAttribute()) {
            return;
        }
        PreparedStatement stmt = null;
        int pos;
        try {
            final StringBuilder sql = new StringBuilder(192);
            sql.append("UPDATE chatMessage SET");
            final List<Object> values = new LinkedList<Object>();
            {
                final String subject = messageDesc.getSubject();
                if (null != subject) {
                    sql.append(" subject = ?,");
                    values.add(subject);
                }
            }
            {
                final String msg = messageDesc.getText();
                if (null != msg) {
                    sql.append(" message = ?,");
                    values.add(prepareInsert(msg));
                }
            }
            sql.append(" createdAt = ? WHERE cid = ? AND chatId = ? AND messageId = ").append(DBChatUtility.getUnhexReplaceString());
            stmt = con.prepareStatement(sql.toString());
            pos = 1;
            for (final Object value : values) {
                stmt.setObject(pos++, value);
            }
            stmt.setLong(pos++, System.currentTimeMillis());
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setString(pos, messageDesc.getMessageId());
            stmt.executeUpdate();
        } catch (final DataTruncation e) {
            throw ChatExceptionCodes.MESSAGE_TOO_LONG.create(e);
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /**
     * Deletes a message by specified identifier.
     *
     * @param messageId The message identifier
     */
    @Override
    public void deleteMessage(final String messageId) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            deleteMessage(messageId, con);
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

    private void deleteMessage(final String messageId, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        int pos;
        try {
            stmt = con.prepareStatement("DELETE FROM chatMessage WHERE cid = ? AND chatId = ? AND messageId = " + DBChatUtility.getUnhexReplaceString());
            pos = 1;
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setString(pos, messageId.toString());
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public List<Message> getMessages(final Collection<String> messageIds, final int userId) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getReadOnly(contextId);
        try {
            return getMessages(messageIds, userId, getContext(), con);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public Message getMessage(final String messageId, final int userId) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getReadOnly(contextId);
        try {
            return getMessages(Collections.singletonList(messageId), userId, getContext(), con).get(0);
        } finally {
            databaseService.backReadOnly(contextId, con);
        }
    }

    private List<Message> getMessages(final Collection<String> messageIds, final int userId, final Context context, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            int pos;
            final List<Message> messages = new ArrayList<Message>(messageIds.size());
            for (final String messageId : messageIds) {
                pos = 1;
                {
                    final String sql = "SELECT message, createdAt FROM chatMessage WHERE cid = ? AND chatId = ? AND chunkId IN (SELECT chunkId FROM chatMember WHERE cid = ? AND chatId = ? AND user = ?) AND messageId = " + DBChatUtility.getUnhexReplaceString();
                    stmt = con.prepareStatement(sql);
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, chatId);
                    stmt.setInt(pos++, contextId);
                    stmt.setInt(pos++, chatId);
                    stmt.setInt(pos++, userId);
                    stmt.setString(pos, messageId);
                }
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw ChatExceptionCodes.MESSAGE_NOT_FOUND.create(messageId, Integer.valueOf(chatId));
                }
                final MessageImpl message = new MessageImpl();
                pos = 1;
                message.setFrom(new ChatUserImpl(Integer.toString(userId), getUserName(userId, context)));
                message.setPacketId(messageId);
                message.setText(prepareSelect(rs.getString(pos++)));
                message.setTimeStamp(new Date(rs.getLong(pos)));
                messages.add(message);
            }
            return messages;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public int getUnreadCount(final ChatUser chatUser) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getReadOnly(contextId);
        try {
            con.setAutoCommit(false);
            final int count = getUnreadCount(con, chatUser);
            con.commit();
            return count;
        } catch (final SQLException e) {
            DBUtils.rollback(con);
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            DBUtils.rollback(con);
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            databaseService.backReadOnly(context, con);
        }
    }

    private int getUnreadCount(final Connection con, final ChatUser chatUser) throws OXException {
        final int chatUserId = DBChatUtility.parseUnsignedInt(chatUser.getId());
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;
        try {
            int pos = 1;
            stmt = con.prepareStatement("SELECT user, messageId, message, createdAt FROM chatMessage WHERE cid = ? AND chatId = ? AND chunkId IN (SELECT cm.chunkId FROM chatMember AS cm WHERE cm.cid = ? AND cm.user = ? AND cm.chatId = ?) AND createdAt > (SELECT MAX(lastPoll) FROM chatMember AS cm WHERE cm.cid = ? AND cm.user = ? AND cm.chatId = ?) ORDER BY createdAt");
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatUserId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatUserId);
            stmt.setInt(pos, chatId);
            rs = stmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
            return count;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    @Override
    public List<Message> pollMessages(final Date since, final ChatUser chatUser) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        final Connection con = databaseService.getWritable(contextId);
        try {
            con.setAutoCommit(false);
            final List<Message> messages = pollMessages(con, since, chatUser);
            con.commit();
            return messages;
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


    private List<Message> pollMessages(final Connection con, final Date since, final ChatUser chatUser) throws OXException {
        final int chatUserId = DBChatUtility.parseUnsignedInt(chatUser.getId());
        int chunkId = 1;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        /*
         * Poll messages
         */
        final List<Message> list = new ArrayList<Message>();
        try {
            int pos = 1;
            if (null == since) {
                stmt = con.prepareStatement("SELECT user, messageId, message, createdAt FROM chatMessage WHERE cid = ? AND chatId = ? AND chunkId IN (SELECT cm.chunkId FROM chatMember AS cm WHERE cm.cid = ? AND cm.user = ? AND cm.chatId = ?) AND createdAt > (SELECT MAX(lastPoll) FROM chatMember AS cm WHERE cm.cid = ? AND cm.user = ? AND cm.chatId = ?) ORDER BY createdAt");
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, chatId);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, chatUserId);
                stmt.setInt(pos++, chatId);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, chatUserId);
                stmt.setInt(pos, chatId);
            } else {
                stmt = con.prepareStatement("SELECT user, messageId, message, createdAt FROM chatMessage WHERE cid = ? AND chatId = ? AND chunkId IN (SELECT cm.chunkId FROM chatMember AS cm WHERE cm.cid = ? AND cm.user = ? AND cm.chatId = ?) AND createdAt > ? ORDER BY createdAt");
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, chatId);
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, chatUserId);
                stmt.setInt(pos++, chatId);
                stmt.setLong(pos, since.getTime());
            }
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptyList();
            }
            final Context context = getContext();
            do {
                final MessageImpl message = new MessageImpl();
                pos = 1;
                final int userId = rs.getInt(pos++);
                message.setFrom(new ChatUserImpl(Integer.toString(userId), getUserName(userId, context)));
                message.setPacketId(toUUID(rs.getBytes(pos++)).toString());
                message.setText(prepareSelect(rs.getString(pos++)));
                message.setTimeStamp(new Date(rs.getLong(pos)));
                list.add(message);
            } while (rs.next());
            // return list;
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }

        /*
         * Get actual chunkId
         */
        try {
            int pos = 1;
            stmt = con.prepareStatement("SELECT MAX(chunkId) FROM chatMember WHERE cid = ? AND user = ? AND chatId = ?");
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatUserId);
            stmt.setInt(pos, chatId);
            rs = stmt.executeQuery();
            if (rs.last()) {
                chunkId = rs.getInt(1);
            }
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }

        /*
         * Update lastPoll
         */
        try {
            int pos = 1;
            stmt = con.prepareStatement("UPDATE chatMember SET lastPoll = ? WHERE cid = ? AND chatId = ? AND chunkId = ? AND user = ?");
            stmt.setLong(pos++, System.currentTimeMillis());
            stmt.setInt(pos++, contextId);
            stmt.setInt(pos++, chatId);
            stmt.setInt(pos++, chunkId);
            stmt.setInt(pos, chatUserId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            throw ChatExceptionCodes.ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
        return list;
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
        final Set<MessageListener> set = new HashSet<MessageListener>(GLOBAL_LISTENERS);
        if (!messageListeners.isEmpty()) {
            set.addAll(messageListeners);
        }
        return set;
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

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("DBChat {chatId=").append(chatId).append(", ");
        builder.append("contextId=").append(contextId).append(", lastChecked=").append(lastChecked).append(", ");
        if (messageListeners != null) {
            builder.append("messageListeners=").append(messageListeners).append(", ");
        }
        builder.append('}');
        return builder.toString();
    }

    /**
     * Gets the database service instance.
     *
     * @return The database service
     * @throws OXException If service cannot be returned
     */
    protected static DatabaseService getDatabaseService() throws OXException {
        final DatabaseService databaseService = DBChatServiceLookup.getService(DatabaseService.class);
        if (null == databaseService) {
            throw ServiceExceptionCode.SERVICE_UNAVAILABLE.create(DatabaseService.class.getName());
        }
        return databaseService;
    }

    private Message createJoinMessage(final String userJoined) {
        final MessageImpl m = new MessageImpl();
        final ChatUser user = new ChatUserImpl(Integer.toString(0));
        m.setFrom(user);
        m.setText(String.format(ChatStrings.CHAT_JOIN, userJoined));
        return m;
    }

    private Message createLeftMessage(final String userLeft) {
        final MessageImpl m = new MessageImpl();
        final ChatUser user = new ChatUserImpl(Integer.toString(0));
        m.setFrom(user);
        m.setText(String.format(ChatStrings.CHAT_LEFT, userLeft));
        return m;
    }

}
