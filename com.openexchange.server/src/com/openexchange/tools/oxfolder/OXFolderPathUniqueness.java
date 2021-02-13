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
import java.util.Locale;
import java.util.function.Consumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.AfterCommitDatabaseConnectionListener;
import com.openexchange.database.DatabaseConnectionListenerAnnotatable;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.update.CreateTableUpdateTask;
import com.openexchange.java.util.Pair;

/**
 * {@link OXFolderPathUniqueness} - Utility class to ensure a unique folder path.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @author <a href="mailto:thorben.bette@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class OXFolderPathUniqueness {

    /** The logger constant */
    static final Logger LOGGER = LoggerFactory.getLogger(OXFolderPathUniqueness.class);

    /**
     * Initializes a new {@link OXFolderPathUniqueness}.
     */
    private OXFolderPathUniqueness() {
        super();
    }

    /**
     * Checks whether given folder path is unique or not.
     * <p>
     * Unique folder names aren't enforced under system folders ("Private -&gt; Calendar", etc.).
     *
     * @param folderName The name of the folder
     * @param parentFolderId The parent folder identifier under which a unique folder shall be checked
     * @param contextId The context identifier
     * @param connection The connection to use
     * @return <code>true</code> if the folder name in unique or uniqueness is not enforced (system folder), <code>false</code> otherwise
     * @throws SQLException In case of SQL error
     * @throws OXException Should not be thrown
     */
    public static boolean isUniqueFolderPath(String folderName, int parentFolderId, int contextId, Connection connection) throws SQLException, OXException {
        if (FolderObject.MIN_FOLDER_ID > parentFolderId) {
            /*
             * System folder, folder name uniqueness is not enforced for these
             */
            return true;
        }
        return null == executeQuery(connection, // @formatter:off
            rs -> Boolean.TRUE, // Will only be called when result is found, so no need no check further
            "SELECT 1 FROM oxfolder_tree WHERE cid=? AND parent=? AND LOWER(fname)=LOWER(?) COLLATE " + (Databases.getCharacterSet(connection).contains("utf8mb4") ? "utf8mb4_bin" : "utf8_bin"),
            s -> s.setInt(1, contextId),
            s -> s.setInt(2, parentFolderId),
            s -> s.setString(3, folderName)); // @formatter:on
    }

    /**
     * Gets the name of a specific folder.
     *
     * @param folderId The folder identifier to get the name for
     * @param contextId The context identifier
     * @param connection The connection to use
     * @return The name of the folder or <code>null</code> if no folder with given ID is found
     * @throws SQLException In case of SQL error
     * @throws OXException Should not be thrown
     */
    public static String getFolderName(int folderId, int contextId, Connection connection) throws SQLException, OXException {
        return executeQuery(connection, // @formatter:off
            rs -> rs.getString(1),
            "SELECT fname FROM oxfolder_tree WHERE cid=? AND fuid=?",
            s -> s.setInt(1, contextId),
            s -> s.setInt(2, folderId)); // @formatter:on
    }

    /**
     * Reserves the folder name under the given parent folder.
     *
     * @param folderName The name of the folder
     * @param parentFolderId The identifier of the parent folder
     * @param context The context
     * @param connection The connection to use
     * @throws SQLException In case the lock can't be inserted
     */
    public static void reserveFolderPath(String folderName, int parentFolderId, Context context, Connection connection) throws SQLException {
        if (FolderObject.MIN_FOLDER_ID > parentFolderId) {
            return;
        }
        executeUpdate( // @formatter:off
            connection,
            "INSERT INTO oxfolder_reservedpaths (cid, parent, fnamehash, expires) VALUES (?,?,?,?)",
            s -> s.setInt(1, context.getContextId()),
            s -> s.setInt(2, parentFolderId),
            s -> s.setLong(3, hashForLowerCaseOf(folderName)),
            s -> s.setLong(4, System.currentTimeMillis()));// @formatter:on
    }

    /**
     * Deletes the associated reserved folder name of the given folder.
     * <p>
     * <b>Note</b>: Deletes the entry after the given connection is committed.
     *
     * @param folderId The identifier of the folder
     * @param contextId The context
     * @param connection The connection to use
     * @throws SQLException In case the lock can't be deleted
     * @throws OXException Should not be thrown
     */
    public static void clearReservedFolderPathFor(int folderId, int contextId, Connection connection) throws SQLException, OXException {
        Pair<Integer, String> parentAndName = executeQuery(connection, // @formatter:off
            rs -> new Pair<Integer, String>(I(rs.getInt(1)), rs.getString(2)),
            "SELECT parent, fname FROM oxfolder_tree WHERE cid=? AND fuid=?",
            s -> s.setInt(1, contextId),
            s -> s.setInt(2, folderId)); // @formatter:on
        if (null != parentAndName) {
            clearReservedFolderPath(parentAndName.getSecond(), i(parentAndName.getFirst()), contextId, connection);
        }
    }

    /**
     * Deletes a reserved folder name under a specific parent folder.
     * <p>
     * <b>Note</b>: Deletes the entry after the given connection is committed.
     *
     * @param folderName The unique name of the folder
     * @param parentFolderId The identifier of the parent folder
     * @param contextId The context identifier
     * @param connection The connection to use
     * @throws SQLException In case deletion cannot be performed/scheduled
     */
    public static void clearReservedFolderPath(String folderName, int parentFolderId, int contextId, Connection connection) throws SQLException {
        if (FolderObject.MIN_FOLDER_ID > parentFolderId) {
            // Aren't inserted..
            return;
        }

        // Check if we should register a listener
        if (!Databases.isInTransaction(connection)) {
            // Given connection is not in transaction mode. Thus delete immediately.
            doClearReservedPath(folderName, parentFolderId, contextId, connection);
            return;
        }

        // Check if we can register a listener
        DatabaseConnectionListenerAnnotatable listenerAnnotatable = null;
        if (DatabaseConnectionListenerAnnotatable.class.isInstance(connection)) {
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
        if (listenerAnnotatable == null) {
            // Delete immediately
            doClearReservedPath(folderName, parentFolderId, contextId, connection);
        } else {
            // Register listener to delete entry after the connection is committed to avoid visibility issues
            Consumer<Connection> callback = c -> doClearReservedPath(folderName, parentFolderId, contextId, c);
            listenerAnnotatable.addListener(new AfterCommitDatabaseConnectionListener(callback));
        }
    }

    private static void doClearReservedPath(String folderName, int parentFolderId, int contextId, Connection connection) {
        try {
            executeUpdate( // @formatter:off
                connection,
                "DELETE FROM oxfolder_reservedpaths WHERE cid=? AND parent=? AND fnamehash=?",
                s -> s.setInt(1, contextId),
                s -> s.setInt(2, parentFolderId),
                s -> s.setLong(3, hashForLowerCaseOf(folderName))); // @formatter:on
        } catch (SQLException e) {
            LOGGER.warn("Unable to remove entry from \"oxfolder_reservedpaths\" table. Reason: ", e.getMessage(), e);
        }
    }

    /**
     * Creates a <code>long</code> hash value for given string.
     *
     * @param string The string to generate hash for
     * @return The hash value
     */
    private static long hashForLowerCaseOf(String string) {
        if (string == null) {
            throw new IllegalArgumentException("Given string is null");
        }

        string = string.toLowerCase(Locale.US); // Use default locale to yield same results
        long h = 1125899906842597L; // prime
        int len = string.length();

        for (int i = 0; i < len; i++) {
            h = 31 * h + string.charAt(i);
        }
        return h;
    }

    // ------------------------------------------------------- Helper classes --------------------------------------------------------------

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
         * Initializes a new {@link OXFolderPathUniqueness.CreateFolderReservedPathTable}.
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

}
