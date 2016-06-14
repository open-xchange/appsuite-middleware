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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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

package com.openexchange.admin.storage.utils;

import static com.openexchange.database.DBPoolingExceptionCodes.NOT_RESOLVED_SERVER;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import com.openexchange.admin.rmi.exceptions.PoolException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.tools.AdminCache;
import com.openexchange.admin.tools.AdminCacheExtended;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;

/**
 * {@link Filestore2UserUtil}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class Filestore2UserUtil {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Filestore2UserUtil.class);

    private static final ThreadPools.ExpectedExceptionFactory<StorageException> EXCEPTION_FACTORY = new ThreadPools.ExpectedExceptionFactory<StorageException>() {

        @Override
        public StorageException newUnexpectedError(final Throwable t) {
            return new StorageException(t);
        }

        @Override
        public Class<StorageException> getType() {
            return StorageException.class;
        }
    };

    private static final int REASON = 57234;
    private static final String TEXT_TERMINATED = "TERMINATED";
    private static final String TEXT_PROCESSING = "PROCESSING";

    /**
     * Initializes a new {@link Filestore2UserUtil}.
     */
    private Filestore2UserUtil() {
        super();
    }

    /**
     * Checks if table <code>"filestore2user"</code> is not yet initialized.
     *
     * @param cache The admin cache to use
     * @return <code>true</code> if not terminated; otherwise <code>false</code>
     * @throws StorageException If check fails
     */
    public static boolean isNotTerminated(AdminCacheExtended cache) throws StorageException {
        Connection con = null;
        try {
            con = cache.getConnectionForConfigDB();
            return isNotTerminated(con);
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } catch (RuntimeException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Pooling error", e);
                }
            }
        }
    }

    /**
     * Gets the users for specified file store.
     *
     * @param filestoreId The file store identifier
     * @param cache The admin cache to use
     * @return The users
     * @throws StorageException If users cannot be returned
     */
    public static Set<UserAndContext> getUsersFor(int filestoreId, AdminCacheExtended cache) throws StorageException {
        Connection con = null;
        try {
            con = cache.getConnectionForConfigDB();

            // Check if processing
            if (isNotTerminated(con)) {
                throw new StorageException("Table \"filestore2user\" not yet initialized");
            }

            return getUsersFor(filestoreId, con);
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } catch (RuntimeException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Pooling error", e);
                }
            }
        }
    }

    private static Set<UserAndContext> getUsersFor(int filestoreId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user FROM filestore2user WHERE filestore_id=? ORDER BY cid, user");
            stmt.setInt(1, filestoreId);
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return Collections.emptySet();
            }

            Set<UserAndContext> users = new LinkedHashSet<UserAndContext>();
            do {
                users.add(new UserAndContext(rs.getInt(2), rs.getInt(1)));
            } while (rs.next());
            return users;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Gets the user count for specified file store.
     *
     * @param filestoreId The file store identifier
     * @param cache The admin cache to use
     * @return The user count
     * @throws StorageException If user count cannot be returned
     */
    public static int getUserCountFor(int filestoreId, AdminCache cache) throws StorageException {
        Connection con = null;
        try {
            con = cache.getConnectionForConfigDB();

            // Check if processing
            if (isNotTerminated(con)) {
                throw new StorageException("Table \"filestore2user\" not yet initialized");
            }

            return getUserCountFor(filestoreId, con);
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } catch (RuntimeException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Pooling error", e);
                }
            }
        }
    }

    private static int getUserCountFor(int filestoreId, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT COUNT(user) FROM filestore2user WHERE filestore_id=?");
            stmt.setInt(1, filestoreId);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : 0;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Gets the sorted (lowest first) file store user counts.
     *
     * @param cache The admin cache to use
     * @return The sorted file store user counts
     * @throws StorageException If file store user counts cannot be returned
     */
    public static FilestoreCountCollection getUserCounts(AdminCacheExtended cache) throws StorageException {
        Connection con = null;
        try {
            con = cache.getConnectionForConfigDB();

            // Check if processing
            if (isNotTerminated(con)) {
                throw new StorageException("Table \"filestore2user\" not yet initialized");
            }

            return getUserCounts(con);
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } catch (RuntimeException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Pooling error", e);
                }
            }
        }
    }

    private static FilestoreCountCollection getUserCounts(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT cid, user, filestore_id FROM filestore2user");
            rs = stmt.executeQuery();
            if (false == rs.next()) {
                return new FilestoreCountCollection(Collections.<Integer, FilestoreCount> emptyMap());
            }

            Map<Integer, FilestoreCount> counts = new HashMap<Integer, FilestoreCount>();
            do {
                Integer fsId = Integer.valueOf(rs.getInt(3));
                FilestoreCount filestoreCount = counts.get(fsId);
                if (null == filestoreCount) {
                    filestoreCount = new FilestoreCount(fsId.intValue(), 1);
                    counts.put(fsId, filestoreCount);
                } else {
                    filestoreCount.incrementCount();
                }
            } while (rs.next());
            return new FilestoreCountCollection(counts);
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Adds specified entry to <code>"filestore2user"</code> table.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param filestoreId The file store identifier
     * @param cache The admin cache to use
     * @throws StorageException If operation fails
     */
    public static void addFilestore2UserEntry(int contextId, int userId, int filestoreId, AdminCacheExtended cache) throws StorageException {
        Connection con = null;
        boolean rollback = false;
        try {
            con = cache.getConnectionForConfigDB();
            if (false == tableExists(con, "filestore2user")) {
                LOG.warn("Table \"filestore2user\" does not exist.");
                return;
            }

            // Check if processing
            if (isNotTerminated(con)) {
                throw new StorageException("Table \"filestore2user\" not yet initialized");
            }

            // Start database transaction
            Databases.startTransaction(con);
            rollback = true;

            // Insert entry
            insertEntry(new FilestoreEntry(contextId, userId, filestoreId), con);

            // Commit
            con.commit();
            rollback = false;
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } catch (RuntimeException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Pooling error", e);
                }
            }
        }
    }

    /**
     * Removes specified entry to <code>"filestore2user"</code> table.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param cache The admin cache to use
     * @throws StorageException If operation fails
     */
    public static void removeFilestore2UserEntry(int contextId, int userId, AdminCacheExtended cache) throws StorageException {
        Connection con = null;
        boolean rollback = false;
        try {
            con = cache.getConnectionForConfigDB();
            if (false == tableExists(con, "filestore2user")) {
                LOG.warn("Table \"filestore2user\" does not exist.");
                return;
            }

            // Check if processing
            if (isNotTerminated(con)) {
                throw new StorageException("Table \"filestore2user\" not yet initialized");
            }

            // Start database transaction
            Databases.startTransaction(con);
            rollback = true;

            // Insert entry
            deleteEntry(new FilestoreEntry(contextId, userId, 0), con);

            // Commit
            con.commit();
            rollback = false;
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } catch (RuntimeException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Pooling error", e);
                }
            }
        }
    }

    /**
     * Replaces specified entry in <code>"filestore2user"</code> table.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param filestoreId The new file store identifier
     * @param cache The admin cache to use
     * @throws StorageException If operation fails
     */
    public static void replaceFilestore2UserEntry(int contextId, int userId, int filestoreId, AdminCacheExtended cache) throws StorageException {
        Connection con = null;
        boolean rollback = false;
        try {
            con = cache.getConnectionForConfigDB();
            if (false == tableExists(con, "filestore2user")) {
                LOG.warn("Table \"filestore2user\" does not exist.");
                return;
            }

            // Check if processing
            if (isNotTerminated(con)) {
                throw new StorageException("Table \"filestore2user\" not yet initialized");
            }

            // Start database transaction
            Databases.startTransaction(con);
            rollback = true;

            // Insert entry
            replaceEntry(new FilestoreEntry(contextId, userId, filestoreId), con);

            // Commit
            con.commit();
            rollback = false;
        } catch (PoolException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } catch (RuntimeException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                if (rollback) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
                try {
                    cache.pushConnectionForConfigDB(con);
                } catch (PoolException e) {
                    LOG.error("Pooling error", e);
                }
            }
        }
    }

    /**
     * Initializes the <code>"filestore2user"</code> table.
     *
     * @param databaseService The database service to use
     * @throws StorageException If initialization fails
     */
    public static void initFilestore2User(DatabaseService databaseService) throws StorageException {
        Connection con = null;
        boolean inTransaction = false;
        boolean unmarkOnError = false;
        try {
            con = databaseService.getForUpdateTask();
            if (false == tableExists(con, "filestore2user")) {
                throw new StorageException("Table \"filestore2user\" does not exist.");
            }

            // Try to insert marker
            boolean marked = mark(con);
            if (marked) {
                unmarkOnError = true;

                // Determine server id
                int serverID;
                try {
                    serverID = databaseService.getServerId();
                } catch (OXException e) {
                    if (NOT_RESOLVED_SERVER.equals(e)) {
                        // Assume initial start and thus mark as processed as there are no entries to insert
                        processed(con);
                        unmarkOnError = false;
                        return;
                    }

                    // Re-throw...
                    throw e;
                }

                // Start database transaction
                Databases.startTransaction(con);
                inTransaction = true;

                // Determine all pools/schemas
                Set<PoolAndSchema> pools = PoolAndSchema.determinePoolsAndSchemas(serverID, con);

                // Determine all users having an individual file store set
                Set<FilestoreEntry> allEntries = determineAllEntries(pools, databaseService);

                // Insert entries
                insertEntries(allEntries, con);

                // Mark as processed
                processed(con);

                // Commit
                con.commit();
                Databases.autocommit(con);
                inTransaction = false;
                unmarkOnError = false;
            } else {
                // Apparently we did nothing...
                databaseService.backForUpdateTaskAfterReading(con);
                con = null;
            }
        } catch (OXException e) {
            throw new StorageException(e);
        } catch (SQLException e) {
            throw new StorageException(e);
        } catch (RuntimeException e) {
            throw new StorageException(e);
        } finally {
            if (null != con) {
                // Check whether to roll-back transactional changes
                if (inTransaction) {
                    Databases.rollback(con);

                    // Switch back to auto-commit
                    Databases.autocommit(con);
                }

                // Check whether to unmark
                unmarkOnError(unmarkOnError, con);

                // Finally, push back used connection
                databaseService.backForUpdateTask(con);
            }
        }
    }

    private static Set<FilestoreEntry> determineAllEntries(Set<PoolAndSchema> pools, final DatabaseService databaseService) throws StorageException {
        // Setup completion service
        CompletionService<Set<FilestoreEntry>> completionService = new ThreadPoolCompletionService<Set<FilestoreEntry>>(AdminServiceRegistry.getInstance().getService(ThreadPoolService.class));
        int taskCount = 0;

        // Determine entries for each pool/schema
        for (final PoolAndSchema poolAndSchema : pools) {
            completionService.submit(new Callable<Set<FilestoreEntry>>() {

                @Override
                public Set<FilestoreEntry> call() throws StorageException {
                    Connection con = null;
                    PreparedStatement stmt = null;
                    ResultSet result = null;
                    try {
                        con = databaseService.getNoTimeout(poolAndSchema.getPoolId(), poolAndSchema.getSchema());

                        if (!columnExists(con, "user","filestore_id")) {
                            // This schema cannot hold users having an individual file storage assigned
                            return Collections.emptySet();
                        }

                        stmt = con.prepareStatement("SELECT cid, id, filestore_id FROM user WHERE filestore_id>0 AND (filestore_owner=0 OR filestore_owner=id)");
                        result = stmt.executeQuery();

                        if (false == result.next()) {
                            return Collections.emptySet();
                        }

                        Set<FilestoreEntry> entries = new LinkedHashSet<FilestoreEntry>();
                        do {
                            entries.add(new FilestoreEntry(result.getInt(1), result.getInt(2), result.getInt(3)));
                        } while (result.next());
                        return entries;
                    } catch (OXException e) {
                        LOG.error("Pool Error", e);
                        throw new StorageException(e);
                    } catch (SQLException e) {
                        LOG.error("SQL Error", e);
                        throw new StorageException(e);
                    } finally {
                        closeSQLStuff(result, stmt);
                        if (null != con) {
                            databaseService.backNoTimeoout(poolAndSchema.getPoolId(), con);
                        }
                    }
                }
            });
            taskCount++;
        }

        // Await completion
        Set<FilestoreEntry> allEntries;
        {
            List<Set<FilestoreEntry>> entries = ThreadPools.<Set<FilestoreEntry>, StorageException> takeCompletionService(completionService, taskCount, EXCEPTION_FACTORY);
            allEntries = new LinkedHashSet<FilestoreEntry>(entries.size());
            for (Set<FilestoreEntry> set : entries) {
                allEntries.addAll(set);
            }
        }
        return allEntries;
    }

    private static void insertEntries(Set<FilestoreEntry> allEntries, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT IGNORE INTO filestore2user (cid, user, filestore_id) VALUES (?, ?, ?)");
            for (FilestoreEntry filestoreEntry : allEntries) {
                stmt.setInt(1, filestoreEntry.cid);
                stmt.setInt(2, filestoreEntry.user);
                stmt.setInt(3, filestoreEntry.filestoreId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static boolean insertEntry(FilestoreEntry filestoreEntry, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO filestore2user (cid, user, filestore_id) VALUES (?, ?, ?)");
            stmt.setInt(1, filestoreEntry.cid);
            stmt.setInt(2, filestoreEntry.user);
            stmt.setInt(3, filestoreEntry.filestoreId);
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                    return false;
                }
                throw e;
            }
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static boolean replaceEntry(FilestoreEntry filestoreEntry, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("REPLACE INTO filestore2user (cid, user, filestore_id) VALUES (?, ?, ?)");
            stmt.setInt(1, filestoreEntry.cid);
            stmt.setInt(2, filestoreEntry.user);
            stmt.setInt(3, filestoreEntry.filestoreId);
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                    return false;
                }
                throw e;
            }
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static void deleteEntry(FilestoreEntry filestoreEntry, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("DELETE FROM filestore2user (cid, user) VALUES (?, ?)");
            stmt.setInt(1, filestoreEntry.cid);
            stmt.setInt(2, filestoreEntry.user);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static boolean mark(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO reason_text (id, text) VALUES (?, ?)");
            stmt.setInt(1, REASON);
            stmt.setString(2, TEXT_PROCESSING);
            try {
                stmt.executeUpdate();
                return true;
            } catch (SQLException e) {
                if (Databases.isPrimaryKeyConflictInMySQL(e)) {
                    // Another machine is currently processing
                    return false;
                }
                throw e;
            }
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static void processed(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE reason_text SET text=? WHERE id=?");
            stmt.setString(1, TEXT_TERMINATED);
            stmt.setInt(2, REASON);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static void unmarkOnError(boolean error, Connection con) {
        if (error) {
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement("DELETE FROM reason_text WHERE id=?");
                stmt.setInt(1, REASON);
                stmt.executeUpdate();
            } catch (Exception e) {
                LOG.error("Failed to unmark filestore2user table!", e);
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
    }

    private static boolean isNotTerminated(Connection con) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement("SELECT text FROM reason_text WHERE id=?");
            stmt.setInt(1, REASON);
            rs = stmt.executeQuery();
            return (false == rs.next()) || (false == TEXT_TERMINATED.equals(rs.getString(1)));
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    private static boolean tableExists(final Connection con, final String table) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getTables(null, null, table, new String[] { "TABLE" });
            retval = (rs.next() && rs.getString("TABLE_NAME").equalsIgnoreCase(table));
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

    static boolean columnExists(final Connection con, final String table, final String column) throws SQLException {
        final DatabaseMetaData metaData = con.getMetaData();
        ResultSet rs = null;
        boolean retval = false;
        try {
            rs = metaData.getColumns(null, null, table, column);
            while (rs.next()) {
                retval = rs.getString(4).equalsIgnoreCase(column);
            }
        } finally {
            closeSQLStuff(rs);
        }
        return retval;
    }

    // ----------------------------------------------------------------------------------------------------------------------------------

    private static final class FilestoreEntry {

        final int cid;
        final int user;
        final int filestoreId;
        private final int hash;

        FilestoreEntry(int cid, int user, int filestoreId) {
            super();
            this.cid = cid;
            this.user = user;
            this.filestoreId = filestoreId;

            int prime = 31;
            int result = prime * 1 + cid;
            result = prime * result + filestoreId;
            result = prime * result + user;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof FilestoreEntry)) {
                return false;
            }
            FilestoreEntry other = (FilestoreEntry) obj;
            if (cid != other.cid) {
                return false;
            }
            if (filestoreId != other.filestoreId) {
                return false;
            }
            if (user != other.user) {
                return false;
            }
            return true;
        }
    }

    /**
     * A collection of {@link FilestoreCount} instances.
     */
    public static final class FilestoreCountCollection implements Iterable<FilestoreCount> {

        private final Map<Integer, FilestoreCount> counts;

        /**
         * Initializes a new {@link FilestoreCountCollection}.
         */
        FilestoreCountCollection(Map<Integer, FilestoreCount> counts) {
            super();
            this.counts = counts;
        }

        /**
         * Gets the size of this collection
         *
         * @return The size
         */
        public int size() {
            return counts.size();
        }

        /**
         * gets the user count for specified file store identifier
         *
         * @param filestoreId The file store identifier to look-up with
         * @return The user count or <code>null</code>
         */
        public FilestoreCount get(int filestoreId) {
            return counts.get(Integer.valueOf(filestoreId));
        }

        @Override
        public Iterator<FilestoreCount> iterator() {
            List<FilestoreCount> l = new ArrayList<>(counts.values());
            Collections.sort(l);
            return l.iterator();
        }
    }

    /**
     * A simple object signaling the number of users using a referenced file store.
     */
    public static final class FilestoreCount implements Comparable<FilestoreCount> {

        private final int filestoreId;
        private int count;

        /**
         * Initializes a new {@link Filestore2UserUtil.FilestoreCount}.
         */
        FilestoreCount(int filestoreId, int count) {
            super();
            this.filestoreId = filestoreId;
            this.count = count;
        }

        void incrementCount() {
            count++;
        }

        /**
         * Gets the number of users using referenced file store
         *
         * @return The number of users using referenced file store
         */
        public int getCount() {
            return count;
        }

        /**
         * Gets the file store identifier
         *
         * @return The file store identifier
         */
        public int getFilestoreId() {
            return filestoreId;
        }

        @Override
        public int compareTo(FilestoreCount o) {
            int x = this.count;
            int y = o.count;
            if (x == y) {
                x = this.filestoreId;
                y = o.filestoreId;
                return (x < y) ? -1 : ((x == y) ? 0 : 1);
            }

            return (x < y) ? -1 : 1;
        }
    }

    /**
     * A user and context identifier pair
     */
    public static final class UserAndContext {

        /** The user identifier */
        public final int userId;

        /** The context identifier */
        public final int contextId;

        private final int hash;

        UserAndContext(int userId, int contextId) {
            super();
            this.userId = userId;
            this.contextId = contextId;

            int prime = 31;
            int result = prime * 1 + contextId;
            result = prime * result + userId;
            hash = result;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof UserAndContext)) {
                return false;
            }
            UserAndContext other = (UserAndContext) obj;
            if (contextId != other.contextId) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            return true;
        }
    }

}
