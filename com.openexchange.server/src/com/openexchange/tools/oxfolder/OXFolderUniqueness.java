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

package com.openexchange.tools.oxfolder;

import static com.openexchange.database.Databases.executeQuery;
import static com.openexchange.database.Databases.executeUpdate;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.AfterCommitDatabaseConnectionListener;
import com.openexchange.database.DatabaseConnectionListenerAnnotatable;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.CreateTableUpdateTask;
import com.openexchange.java.util.Pair;
import com.openexchange.server.ServiceLookup;

/**
 * {@link OXFolderUniqueness} - Utility class to ensure a unique folder name
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @author <a href="mailto:thorben.bette@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class OXFolderUniqueness {

    static final Logger LOGGER = LoggerFactory.getLogger(OXFolderUniqueness.class);

    /**
     * 
     * Initializes a new {@link OXFolderUniqueness}.
     */
    private OXFolderUniqueness() {}

    /**
     * Gets a value indicating whether a folder name is unique or not
     * <p>
     * Unique folder names aren't enforced under system folders ("Calendar", etc.)
     *
     * @param connection The connection to use
     * @param contextId The context ID
     * @param parentFolderId The parent folder ID under which a unique folder shall be checked
     * @param folderName The name of the folder
     * @return <code>true</code> if the folder name in unique or uniqueness is not enforced (system folder), <code>false</code> otherwise
     * @throws SQLException In case of SQL error
     * @throws OXException Should not be thrown ..
     */
    public static boolean isUniqueFolderName(Connection connection, int contextId, int parentFolderId, String folderName) throws SQLException, OXException {
        if (FolderObject.MIN_FOLDER_ID > parentFolderId) {
            /*
             * System folder, folder name uniqueness is not enforced for these
             */
            return true;
        }
        return null == executeQuery(connection, // @formatter:off
            rs -> Boolean.TRUE, // Will only be called when result is found, so no need no check further
            "SELECT 1 FROM oxfolder_tree WHERE cid=? AND parent=? AND fname=?",
            s -> s.setInt(1, contextId),
            s -> s.setInt(2, parentFolderId),
            s -> s.setString(3, folderName)); // @formatter:on
    }

    /**
     * Get the name of a specific folder
     * 
     * @param connection The connection to use
     * @param contextId The context ID
     * @param folderId The folder ID to get the name for
     * @return The name of the folder or <code>null</code> if no folder with given ID is found
     * @throws SQLException In case of SQL error
     * @throws OXException Should not be thrown ..
     */
    public static String getFolderName(Connection connection, int contextId, int folderId) throws SQLException, OXException {
        return executeQuery(connection, // @formatter:off
            rs -> rs.getString(1),
            "SELECT fname FROM oxfolder_tree WHERE cid=? AND fuid=?",
            s -> s.setInt(1, contextId),
            s -> s.setInt(2, folderId)); // @formatter:on
    }

    /**
     * Reserves the folder name under the given parent folder
     * <p>
     * Note: Deletes the entry after the given connection is commited
     *
     * @param connection The connection to use
     * @param context The context
     * @param parentFolderId The identifier of the parent folder
     * @param folderName The unique name of the folder
     * @throws SQLException In case the lock can't be inserted
     */
    public static void reserveFolderName(Connection connection, Context context, int parentFolderId, String folderName) throws SQLException {
        if (FolderObject.MIN_FOLDER_ID > parentFolderId) {
            return;
        }
        executeUpdate( // @formatter:off
            connection, 
            "INSERT INTO oxfolder_reservedpaths (cid, parent, fnamehash, expires) VALUES (?,?,?, ?)",
            s -> s.setInt(1, context.getContextId()),
            s -> s.setInt(2, parentFolderId),
            s -> s.setLong(3, hash(folderName)),
            s -> s.setLong(4, System.currentTimeMillis()));// @formatter:on
    }

    /**
     * Deletes the associated reserved folder name of the given folder
     * <p>
     * Note: Deletes the entry after the given connection is commited
     *
     * @param connection The connection to use
     * @param contextId The context
     * @param folderId The identifier of the folder
     * @throws SQLException In case the lock can't be deleted
     * @throws OXException NOOP
     */
    public static void clearFolderName(Connection connection, int contextId, int folderId) throws SQLException, OXException {
        Pair<Integer, String> data = executeQuery(connection, // @formatter:off
            rs -> new Pair<Integer, String>(I(rs.getInt(1)), rs.getString(2)),
            "SELECT parent, fname FROM oxfolder_tree WHERE cid=? AND fuid=?",
            s -> s.setInt(1, contextId),
            s -> s.setInt(2, folderId)); // @formatter:on
        if (null != data) {
            clearFolderName(connection, contextId, i(data.getFirst()), data.getSecond());
        }
    }

    /**
     * Deletes a reserved folder name under a specific parent folder
     *
     * @param connection The connection to use
     * @param contextId The context identifier
     * @param parentFolderId The identifier of the parent folder
     * @param folderName The unique name of the folder
     * @throws SQLException In case the lock can't be deleted
     */
    public static void clearFolderName(Connection connection, int contextId, int parentFolderId, String folderName) throws SQLException {
        if (FolderObject.MIN_FOLDER_ID > parentFolderId) {
            // Aren't inserted.. 
            return;
        }
        /*
         * Check if we can register a listener
         */
        DatabaseConnectionListenerAnnotatable listenerAnnotatable = null;
        if (DatabaseConnectionListenerAnnotatable.class.isInstance(connection) && Databases.isInTransaction(connection)) {
            listenerAnnotatable = (DatabaseConnectionListenerAnnotatable) connection;
        } else if (connection.isWrapperFor(DatabaseConnectionListenerAnnotatable.class)) {
            try {
                if (connection.isWrapperFor(DatabaseConnectionListenerAnnotatable.class)) {
                    listenerAnnotatable = connection.unwrap(DatabaseConnectionListenerAnnotatable.class);
                }
            } catch (SQLException e) {
                LOGGER.warn("", e);
            }
        }

        if (null != listenerAnnotatable) {
            /*
             * Register listener to delete entry after the connection is commit to avoid race conditions
             */
            Consumer<Connection> callback = c -> clear(c, parentFolderId, contextId, folderName);
            listenerAnnotatable.addListener(new AfterCommitDatabaseConnectionListener(callback));
        } else {
            /*
             * Delete immediately, we don't know better
             */
            clear(connection, contextId, parentFolderId, folderName);
        }
    }

    private static void clear(Connection connection, int contextId, int parentFolderId, String folderName) {
        try {
            executeUpdate( // @formatter:off
                connection, 
                "DELETE FROM oxfolder_reservedpaths WHERE cid=? AND parent=? AND fnamehash=?",
                s -> s.setInt(1, contextId),
                s -> s.setInt(2, parentFolderId),
                s -> s.setLong(3, hash(folderName))); // @formatter:on
        } catch (SQLException e) {
            LOGGER.warn("Unable to remove entry from \"oxfolder_reservedpaths\" table. Reason: ", e.getMessage(), e);
        }
    }

    /**
     * Create a long hash value for given string.
     *
     * @param string The string to generate hash for
     * @return The hash value
     */
    public static long hash(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Given string is null");
        }
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }

    /**
     * 
     * {@link CreateFolderReservedPathTable} -The update task to add the table <code>oxfolder_reservedpaths</code>
     * <p>
     * The table is used to avoid multiple folder names and works like a lock on the table without the need of the <code>FOR UPDATE</code> phrase
     * in SQL statements.
     * <p>
     * The table is necessary because depending on the DB properties regarding GAP locking, multiple transaction can insert the same folder name
     * into the <code>oxfolder_tree</code> table (folder name must not always be unique). Therefore before inserting or updating the folder name
     * in the <code>oxfolder_tree</code> table the name will get reserved in this table.
     * <p>
     * See also <a href="https://dev.mysql.com/doc/refman/5.7/en/innodb-parameters.html#sysvar_innodb_locks_unsafe_for_binlog">innodb_locks_unsafe_for_binlog</a>
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.5
     */
    public static class CreateFolderReservedPathTable extends AbstractCreateTableImpl {

        /**
         * Initializes a new {@link OXFolderUniqueness.CreateFolderReservedPathTable}.
         */
        public CreateFolderReservedPathTable() {
            super();
        }

        @Override
        public String[] tablesToCreate() {
            return new String[] { "oxfolder_reservedpaths" };
        }

        @Override
        public String[] requiredTables() {
            return NO_TABLES;
        }

        @Override
        protected String[] getCreateStatements() {
            return new String[] { "CREATE TABLE oxfolder_reservedpaths (" // @formatter:off
                + "cid INT4 UNSIGNED NOT NULL, " // Context identifier
                + "parent INT4 UNSIGNED NOT NULL, " // PARENT folder identifier
                + "fnamehash bigint(64) NOT NULL, " // Hash of (child) folder name
                + "expires bigint(64) NOT NULL, " // Clean up time
                + "PRIMARY KEY (cid, parent, fnamehash)"
                + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci"}; // @formatter:on
        }
    }

    /**
     * 
     * {@link CreateFolderReservedPathUpdateTask}
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.5
     */
    public static class CreateFolderReservedPathUpdateTask extends CreateTableUpdateTask {

        /**
         * Initializes a new {@link CreateFolderReservedPathUpdateTask}.
         */
        public CreateFolderReservedPathUpdateTask() {
            super(new CreateFolderReservedPathTable(), null);
        }
    }

    /**
     * 
     * {@link CleanUpReservedPathsTask}
     *
     * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
     * @since v7.10.5
     */
    public static class CleanUpReservedPathsTask implements Runnable {

        final ServiceLookup services;

        /**
         * Initializes a new {@link OXFolderUniqueness.CleanUpReservedPathsTask}.
         * 
         * @param services The service lookup
         */
        public CleanUpReservedPathsTask(ServiceLookup services) {
            super();
            this.services = services;

        }

        @Override
        public void run() {
            try {
                ContextService contextService = services.getServiceSafe(ContextService.class);
                DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
                for (Integer representiveContextId : contextService.getDistinctContextsPerSchema()) {
                    clearExpired(databaseService, i(representiveContextId));
                }
            } catch (SQLException | OXException e) {
                LOGGER.warn("Unable to clean up data!", e);
            }
        }

        private static void clearExpired(DatabaseService databaseService, int representiveContextId) throws SQLException, OXException {
            executeUpdate( // @formatter:off
                representiveContextId,
                databaseService, 
                "DELETE FROM oxfolder_reservedpaths WHERE expires <= ?",
                s -> s.setLong(1, System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)));  // @formatter:on
        }
    }

}
