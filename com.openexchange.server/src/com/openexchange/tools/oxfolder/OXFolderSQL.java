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
 *    trademarks of the OX Software GmbH group of companies.
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

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.closeResources;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.json.JSONException;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.cache.impl.FolderCacheManager;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderEventConstants;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.oxfolder.memory.ConditionTreeMapManagement;
import com.openexchange.tools.sql.DBUtils;
import gnu.trove.TIntCollection;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.list.linked.TIntLinkedList;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * Contains useful SQL-related helper methods for folder operations
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderSQL {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(OXFolderSQL.class);

    private static final int UPDATE_CHUNK_SIZE = 100;

    /**
     * Initializes a new OXFolderSQL
     */
    private OXFolderSQL() {
        super();
    }

    private static final String SQL_LOCK = "SELECT fuid FROM oxfolder_tree WHERE cid=? AND fuid=? FOR UPDATE";
    private static final String SQL_LOCK_BACKUP = "SELECT fuid FROM del_oxfolder_tree WHERE cid=? AND fuid=? FOR UPDATE";

    /**
     * Performs a lock on the folder entry in associated table.
     *
     * @param folderId The folder identifier
     * @param contextId The context identifier
     * @param con The connection to use
     * @throws SQLException If lock attempt fails
     */
    public static void lock(final int folderId, final int contextId, final Connection con) throws SQLException {
        lock(folderId, contextId, false, con);
    }

    /**
     * Performs a lock on the folder entry in associated table.
     *
     * @param folderId The folder identifier
     * @param contextId The context identifier
     * @param backupTable <code>true</code> to also put a lock on backup tables; otherwise <code>false</code>
     * @param con The connection to use
     * @throws SQLException If lock attempt fails
     */
    public static void lock(final int folderId, final int contextId, final boolean backupTable, final Connection con) throws SQLException {
        if (null == con) {
            return;
        }
        PreparedStatement stmt = null;
        try {
            if (con.getAutoCommit()) {
                throw new SQLException("Connection is not in transaction state.");
            }
            stmt = con.prepareStatement(SQL_LOCK);
            stmt.setInt(1, contextId);
            stmt.setInt(2, folderId);
            stmt.executeQuery();

            if (backupTable) {
                closeSQLStuff(stmt);
                stmt = con.prepareStatement(SQL_LOCK_BACKUP);
                stmt.setInt(1, contextId);
                stmt.setInt(2, folderId);
                stmt.executeQuery();
            }
        } finally {
            closeSQLStuff(stmt);
        }
    }

    /*
     * A possible optimization for this query can be:
     * SELECT ot1.fuid FROM oxfolder_tree AS ot1 LEFT JOIN oxfolder_tree ot2 ON ot1.cid=ot2.cid AND ot1.parent=ot2.fuid WHERE ot1.cid=? AND ot1.parent<>0 AND ot2.fuid IS NULL;
     */
    private static final String SQL_SELECT_WITH_NON_EXISTING_PARENT = "SELECT ot1.fuid FROM oxfolder_tree AS ot1 where ot1.cid = ? AND ot1.parent <> "+FolderObject.SYSTEM_ROOT_FOLDER_ID+" AND NOT EXISTS (SELECT ot2.fuid FROM oxfolder_tree AS ot2 where ot2.cid = ? AND ot1.parent = ot2.fuid)";

    /**
     * Gets the non-existing parents in specified context.
     *
     * @param ctx The context
     * @return The non-existing parents in specified context
     * @throws OXException If operation fails
     */
    public static int[] getNonExistingParents(final Context ctx) throws OXException {
        final Connection con = DBPool.pickup(ctx);
        try {
            return getNonExistingParents(ctx, con);
        } finally {
            DBPool.closeReaderSilent(ctx, con);
        }
    }

    /**
     * Gets the non-existing parents in specified context.
     *
     * @param ctx The context
     * @param con The connection to user; <b>must not be <code>null</code></b>
     * @return The non-existing parents in specified context
     * @throws OXException If operation fails
     */
    public static int[] getNonExistingParents(final Context ctx, final Connection con) throws OXException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_SELECT_WITH_NON_EXISTING_PARENT);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            rs = executeQuery(stmt);
            final TIntHashSet set = new TIntHashSet(16);
            while (rs.next()) {
                set.add(rs.getInt(1));
            }
            return set.toArray();
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            closeSQLStuff(rs, stmt);
        }
    }

    private static final String SQL_SELECT_ADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

    /**
     * Determines the ID of the user who is defined as admin for given context or <code>-1</code> if none found
     *
     * @param ctx The context
     * @param readConArg A readable connection or <code>null</code> to fetch a new one from connection pool
     * @return The ID of context admin or <code>-1</code> if none found
     * @throws OXException If parameter <code>readConArg</code> is <code>null</code> and no readable connection could be fetched from
     *             or put back into connection pool
     * @throws SQLException If a SQL error occurs
     */
    public static int getContextAdminID(final Context ctx, final Connection readConArg) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SELECT_ADMIN);
            stmt.setInt(1, ctx.getContextId());
            rs = executeQuery(stmt);
            if (!rs.next()) {
                return -1;
            }
            return rs.getInt(1);
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_DEFAULTFLD = "SELECT ot.fuid FROM oxfolder_tree AS ot WHERE ot.cid = ? AND ot.created_from = ? AND ot.module = ? AND ot.default_flag = 1";

    /**
     * Gets the specified user's default folder of given module
     *
     * @param userId The user ID
     * @param module The module
     * @param readCon A connection with read capability
     * @param ctx The context
     * @return The folder ID of user's default folder of given module, or <code>-1</code> if not found
     * @throws OXException If a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    public static int getUserDefaultFolder(final int userId, final int module, final Connection readCon, final Context ctx) throws OXException, SQLException {
        Connection rc = readCon;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (rc == null) {
                rc = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = rc.prepareStatement(SQL_DEFAULTFLD);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            stmt.setInt(3, module);
            rs = executeQuery(stmt);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeResources(rs, stmt, closeReadCon ? rc : null, true, ctx);
        }
    }

    private static final String SQL_DEFAULTFLDTYPE = "SELECT ot.fuid FROM oxfolder_tree AS ot WHERE ot.cid = ? AND ot.created_from = ? AND ot.module = ? AND ot.type = ? AND ot.default_flag = 1";

    /**
     * Gets the specified user's default folder of given module and type
     *
     * @param userId The user ID
     * @param module The module
     * @param type The type
     * @param readCon A connection with read capability
     * @param ctx The context
     * @return The folder ID of user's default folder of given module, or <code>-1</code> if not found
     * @throws OXException If a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    public static int getUserDefaultFolder(final int userId, final int module, final int type, final Connection readCon, final Context ctx) throws OXException, SQLException {
        Connection rc = readCon;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (rc == null) {
                rc = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = rc.prepareStatement(SQL_DEFAULTFLDTYPE);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, userId);
            stmt.setInt(3, module);
            stmt.setInt(4, type);
            rs = executeQuery(stmt);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeResources(rs, stmt, closeReadCon ? rc : null, true, ctx);
        }
    }

    private static final String SQL_SELECT_ALL_SHARED_FLDS = "SELECT ot.fuid FROM oxfolder_tree AS ot WHERE ot.cid = ? AND ot.type = ? AND ot.created_from = ? AND " + "(SELECT COUNT(op.permission_id) FROM oxfolder_permissions AS op WHERE op.cid = ot.cid AND op.fuid = ot.fuid) > 1 GROUP BY ot.fuid";

    /**
     * Gets all private folders of specified owner which are shared to other users.
     *
     * @param owner The owner's ID
     * @param readConArg A readable connection or <code>null</code> to fetch a new one from connection pool
     * @param ctx The context
     * @return All private folders of specified owner which are shared to other users.
     * @throws OXException If parameter <code>readConArg</code> is <code>null</code> and no readable connection could be fetched from
     *             or put back into connection pool
     * @throws SQLException If a SQL error occurs
     */
    public static TIntCollection getSharedFoldersOf(final int owner, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SELECT_ALL_SHARED_FLDS);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, FolderObject.PRIVATE);
            stmt.setInt(3, owner);
            rs = executeQuery(stmt);
            if (!rs.next()) {
                return new TIntArrayList(0);
            }
            final TIntList sia = new TIntArrayList(16);
            do {
                sia.add(rs.getInt(1));
            } while (rs.next());
            return sia;
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_UPDATE_LAST_MOD = "UPDATE oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    /**
     * Updates the last modified time stamp of the folder whose ID matches given parameter <code>folderId</code>.
     *
     * @param folderId The folder ID
     * @param lastModified The new last-modified time stamp to set
     * @param modifiedBy The user who shall be inserted as modified-by
     * @param writeConArg A writable connection or <code>null</code> to fetch a new one from pool
     * @param ctx The context
     * @throws OXException If parameter <code>writeConArg</code> is <code>null</code> and a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    public static void updateLastModified(final int folderId, final long lastModified, final int modifiedBy, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            startedTransaction = writeCon.getAutoCommit();
            if (startedTransaction) {
                writeCon.setAutoCommit(false);
                rollback = true;
            }

            // Acquire lock
            lock(folderId, ctx.getContextId(), writeCon);

            // Do the update
            stmt = writeCon.prepareStatement(SQL_UPDATE_LAST_MOD);
            stmt.setLong(1, lastModified);
            stmt.setInt(2, modifiedBy);
            stmt.setInt(3, ctx.getContextId());
            stmt.setInt(4, folderId);
            executeUpdate(stmt);

            if (startedTransaction) {
                writeCon.commit();
                rollback = false;
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (startedTransaction && rollback) {
                if (null != writeCon) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
            }
            closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_UPDATE_LAST_MOD2 = "UPDATE oxfolder_tree SET changing_date = ? WHERE cid = ? AND fuid = ?";

    /**
     * Updates the last modified timestamp of the folder whose ID matches given parameter <code>folderId</code>.
     *
     * @param folderId The folder ID
     * @param lastModified The new last-modified timestamp to set
     * @param writeConArg A writable connection or <code>null</code> to fetch a new one from pool
     * @param ctx The context
     * @throws OXException If parameter <code>writeConArg</code> is <code>null</code> and a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    private static void updateLastModified(final int folderId, final long lastModified, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            startedTransaction = writeCon.getAutoCommit();
            if (startedTransaction) {
                writeCon.setAutoCommit(false);
                rollback = true;
            }

            // Acquire lock
            lock(folderId, ctx.getContextId(), writeCon);

            // Do the update
            stmt = writeCon.prepareStatement(SQL_UPDATE_LAST_MOD2);
            stmt.setLong(1, lastModified);
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, folderId);
            executeUpdate(stmt);

            if (startedTransaction) {
                writeCon.commit();
                rollback = false;
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (startedTransaction && rollback) {
                if (null != writeCon) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
            }
            closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
        }
    }

    /**
     * Updates the "type" of one or more folders identified by their identifiers. Usually used when moving a folder (and its subfolders)
     * from or to the trash folder.
     *
     * @param writeConnection A writable connection or <code>null</code> to fetch a new one from pool
     * @param context The context
     * @param type The type to set
     * @param folderIDs The IDs of the folders to update
     * @return The number of updated entries in the database
     */
    public static int updateFolderType(Connection writeConnection, Context context, int type, List<Integer> folderIDs) throws OXException, SQLException {
        return updateFolderType(writeConnection, context, type, 0, folderIDs);
    }

    /**
     * Updates the "type" of one or more folders identified by their identifiers. Usually used when moving a folder (and its subfolders)
     * from or to the trash folder.
     *
     * @param writeConnection A writable connection or <code>null</code> to fetch a new one from pool
     * @param context The context
     * @param type The type to set
     * @param optNewOwner The optional new owner or less than/equal to <code>0</code> (zero)
     * @param folderIDs The IDs of the folders to update
     * @return The number of updated entries in the database
     */
    public static int updateFolderType(Connection writeConnection, Context context, int type, int optNewOwner, List<Integer> folderIDs) throws OXException, SQLException {
        if (null == folderIDs || 0 == folderIDs.size()) {
            return 0;
        }
        int updated = 0;
        boolean closeWriteConnection = false;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            /*
             * fetch connection if needed
             */
            if (null == writeConnection) {
                writeConnection = DBPool.pickupWriteable(context);
                closeWriteConnection = true;
            }
            startedTransaction = writeConnection.getAutoCommit();
            if (startedTransaction) {
                writeConnection.setAutoCommit(false);
                rollback = true;
            }
            /*
             * perform update chunkwise
             */
            for (int i = 0; i < folderIDs.size(); i += UPDATE_CHUNK_SIZE) {
                int length = Math.min(folderIDs.size(), i + UPDATE_CHUNK_SIZE) - i;
                List<Integer> ids = folderIDs.subList(i, i + length);
                StringBuilder stringBuilder = new StringBuilder("UPDATE oxfolder_tree SET type=?");
                if (optNewOwner > 0) {
                    stringBuilder.append(", created_from=?");
                }
                stringBuilder.append(" WHERE cid=? AND fuid");

                if (1 == ids.size()) {
                    stringBuilder.append("=?;");
                } else {
                    stringBuilder.append(" IN (?");
                    for (int j = 1; j < ids.size(); j++) {
                        stringBuilder.append(",?");
                    }
                    stringBuilder.append(");");
                }
                PreparedStatement stmt = null;
                try {
                    stmt = writeConnection.prepareStatement(stringBuilder.toString());
                    int pos = 0;
                    stmt.setInt(++pos, type);
                    if (optNewOwner > 0) {
                        stmt.setInt(++pos, optNewOwner);
                    }
                    stmt.setInt(++pos, context.getContextId());
                    int off = ++pos;
                    for (int j = 0; j < ids.size(); j++) {
                        stmt.setInt(j + off, ids.get(j).intValue());
                    }
                    updated += executeUpdate(stmt);
                } finally {
                    closeSQLStuff(stmt);
                }
            }
            /*
             * commit if appropriate
             */
            if (startedTransaction) {
                writeConnection.commit();
                rollback = false;
                writeConnection.setAutoCommit(true);
            }
        } finally {
            /*
             * cleanup
             */
            if (startedTransaction && rollback) {
                if (null != writeConnection) {
                    writeConnection.rollback();
                    writeConnection.setAutoCommit(true);
                }
            }
            if (closeWriteConnection) {
                DBPool.closeWriterSilent(context, writeConnection);
            }
        }
        return updated;
    }

    /**
     * Updates the "owner" of one or more folders identified by their identifiers. Usually used when moving a folder.
     *
     * @param writeConnection A writable connection or <code>null</code> to fetch a new one from pool
     * @param context The context
     * @param newOwner The owner to set
     * @param folderIDs The IDs of the folders to update
     * @return The number of updated entries in the database
     */
    public static int updateFolderOwner(Connection writeConnection, Context context, int newOwner, List<Integer> folderIDs) throws OXException, SQLException {
        if (null == folderIDs || 0 == folderIDs.size()) {
            return 0;
        }
        int updated = 0;
        boolean closeWriteConnection = false;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            /*
             * fetch connection if needed
             */
            if (null == writeConnection) {
                writeConnection = DBPool.pickupWriteable(context);
                closeWriteConnection = true;
            }
            startedTransaction = writeConnection.getAutoCommit();
            if (startedTransaction) {
                writeConnection.setAutoCommit(false);
                rollback = true;
            }
            /*
             * perform update chunkwise
             */
            for (int i = 0; i < folderIDs.size(); i += UPDATE_CHUNK_SIZE) {
                int length = Math.min(folderIDs.size(), i + UPDATE_CHUNK_SIZE) - i;
                List<Integer> ids = folderIDs.subList(i, i + length);
                StringBuilder stringBuilder = new StringBuilder("UPDATE oxfolder_tree SET created_from=? WHERE cid=? AND fuid");

                if (1 == ids.size()) {
                    stringBuilder.append("=?;");
                } else {
                    stringBuilder.append(" IN (?");
                    for (int j = 1; j < ids.size(); j++) {
                        stringBuilder.append(",?");
                    }
                    stringBuilder.append(");");
                }
                PreparedStatement stmt = null;
                try {
                    stmt = writeConnection.prepareStatement(stringBuilder.toString());
                    int pos = 0;
                    stmt.setInt(++pos, newOwner);
                    stmt.setInt(++pos, context.getContextId());
                    int off = ++pos;
                    for (int j = 0; j < ids.size(); j++) {
                        stmt.setInt(j + off, ids.get(j).intValue());
                    }
                    updated += executeUpdate(stmt);
                } finally {
                    closeSQLStuff(stmt);
                }
            }
            /*
             * commit if appropriate
             */
            if (startedTransaction) {
                writeConnection.commit();
                rollback = false;
                writeConnection.setAutoCommit(true);
            }
        } finally {
            /*
             * cleanup
             */
            if (startedTransaction && rollback) {
                if (null != writeConnection) {
                    writeConnection.rollback();
                    writeConnection.setAutoCommit(true);
                }
            }
            if (closeWriteConnection) {
                DBPool.closeWriterSilent(context, writeConnection);
            }
        }
        return updated;
    }

    private static final String SQL_UPDATE_NAME = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    /**
     * Updates the name of the folder whose ID matches given parameter <code>folderId</code>.
     *
     * @param folderId The folder ID
     * @param newName The new name to set
     * @param lastModified The last modified timestamp
     * @param modifiedBy The user who shall be inserted as modified-by
     * @param writeConArg A writeable connection or <code>null</code> to fetch a new one from pool
     * @param ctx The context
     * @throws OXException If parameter <code>writeConArg</code> is <code>null</code> and a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    static void updateName(final int folderId, final String newName, final long lastModified, final int modifiedBy, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            startedTransaction = writeCon.getAutoCommit();
            if (startedTransaction) {
                writeCon.setAutoCommit(false);
                rollback = true;
            }

            // Acquire lock
            lock(folderId, ctx.getContextId(), writeCon);

            // Do the update
            stmt = writeCon.prepareStatement(SQL_UPDATE_NAME);
            stmt.setString(1, newName);
            stmt.setLong(2, lastModified);
            stmt.setInt(3, modifiedBy);
            stmt.setInt(4, ctx.getContextId());
            stmt.setInt(5, folderId);
            executeUpdate(stmt);

            if (startedTransaction) {
                writeCon.commit();
                rollback = false;
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (startedTransaction && rollback) {
                if (null != writeCon) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
            }
            closeResources(null, stmt, closeWriteCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_LOOKUPFOLDER = "SELECT fuid,fname FROM oxfolder_tree WHERE cid=? AND parent=? AND fname=? AND module=?";

    /**
     * Returns an {@link TIntList} of folders whose name (ignoring case) and module matches the given parameters in the given parent
     * folder.
     *
     * @param parent The parent folder whose subfolders shall be looked up
     * @param folderName The folder name to look for
     * @param module The folder module
     * @param readConArg A readable connection (may be <code>null</code>)
     * @param ctx The context
     * @return A list of folders with the same name and module.
     * @throws OXException
     * @throws SQLException
     */
    public static TIntList lookUpFolders(final int parent, final String folderName, final int module, final Connection readConArg, final Context ctx) throws OXException, SQLException {
    	Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final TIntList folderList = new TIntLinkedList();
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_LOOKUPFOLDER);
            stmt.setInt(1, ctx.getContextId()); // cid
            stmt.setInt(2, parent); // parent
            stmt.setString(3, folderName); // fname
            stmt.setInt(4, module); // module
            rs = executeQuery(stmt);
            while (rs.next()) {
                final int fuid = rs.getInt(1);
                final String fname = rs.getString(2);
                if (Strings.equalsNormalizedIgnoreCase(folderName, fname)) {
                    folderList.add(fuid);
                }
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }

        return folderList;
    }

    /**
     * Checks for a duplicate folder in parental folder. A folder is treated as a duplicate if name and module are equal.
     *
     * @return folder id or <tt>-1</tt> if none found
     */
    public static int lookUpFolder(final int parent, final String folderName, final int module, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        return lookUpFolderOnUpdate(-1, parent, folderName, module, readConArg, ctx);
    }

    /**
     * Checks for a duplicate folder in parental folder. A folder is treated as a duplicate if name and module are equal, ignoring case.
     *
     * @param folderId The ID of the folder whose is equal to given folder name (used on update). Set this parameter to <code>-1</code> to
     *            ignore.
     * @param parent The parent folder whose subfolders shall be looked up
     * @param folderName The folder name to look for
     * @param module The folder module
     * @param readConArg A readable connection (may be <code>null</code>)
     * @param ctx The context
     * @return The folder id or <tt>-1</tt> if none found
     * @throws OXException
     * @throws SQLException
     */
    public static int lookUpFolderOnUpdate(final int folderId, final int parent, final String folderName, final int module, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            StringBuilder stmtBuilder = new StringBuilder("SELECT fuid,fname FROM oxfolder_tree WHERE cid=? AND parent=? AND fname=?");
            if (module > 0) {
                final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                if (null != service && service.getBoolProperty("com.openexchange.oxfolder.considerModuleOnDuplicateCheck", false)) {
                    stmtBuilder.append(" AND module=").append(module);
                }
            }
            if (folderId > 0) {
                stmtBuilder.append(" AND fuid!=").append(folderId);
            }
            stmt = readCon.prepareStatement(stmtBuilder.toString());
            stmtBuilder = null;
            stmt.setInt(1, ctx.getContextId()); // cid
            stmt.setInt(2, parent); // parent
            stmt.setString(3, folderName); // fname
            rs = executeQuery(stmt);
            while (rs.next()) {
                final int fuid = rs.getInt(1);
                final String fname = rs.getString(2);
                if (Strings.equalsNormalizedIgnoreCase(folderName, fname)) {
                    return fuid;
                }
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
        return -1;
    }

    /**
     * Checks if underlying storage contains a folder whose ID matches given ID
     *
     * @return <tt>true</tt> if folder exists, otherwise <tt>false</tt>
     */
    public static boolean exists(final int folderId, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement("SELECT 1 FROM oxfolder_tree WHERE cid = ? AND fuid = ?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = executeQuery(stmt);
            return rs.next();
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    /**
     * Checks if underlying storage contains a folder whose ID matches given ID
     *
     * @return <tt>true</tt> if folder exists, otherwise <tt>false</tt>
     */
    public static boolean exists(final int folderId, final Connection readConArg, final Context ctx, final String table) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(new StringBuilder(40).append("SELECT 1 FROM ").append(table).append(
                " WHERE cid = ? AND fuid = ?").toString());
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = executeQuery(stmt);
            return rs.next();
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_UPDATE_PERMS = "UPDATE oxfolder_permissions SET" + " fp = ?, orp = ?, owp = ?, odp = ?" + " WHERE cid = ? AND fuid = ? AND permission_id = ?";

    /**
     * Updates a single folder permission and updates folder's last-modified time stamp
     *
     * @param folderId The folder ID
     * @param permissionId The entity ID; either user or group ID
     * @param folderPermission The folder permission to set
     * @param objectReadPermission The object read permission to set
     * @param objectWritePermission The object write permission to set
     * @param objectDeletePermission The object delete permission to set
     * @param writeCon A connection with write capability; may be <code>null</code> to fetch from pool
     * @param ctx The context
     * @return <code>true</code> if corresponding entry was successfully updated; otherwise <code>false</code>
     * @throws OXException If a pooling error occurred
     * @throws SQLException If a SQL error occurred
     */
    public static boolean updateSinglePermission(final int folderId, final int permissionId, final int folderPermission, final int objectReadPermission, final int objectWritePermission, final int objectDeletePermission, final Connection writeCon, final Context ctx) throws OXException, SQLException {
        Connection wc = writeCon;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            startedTransaction = wc.getAutoCommit();
            if (startedTransaction) {
                wc.setAutoCommit(false);
                rollback = true;
            }

            // Acquire lock
            lock(folderId, ctx.getContextId(), wc);

            // Do the update
            stmt = wc.prepareStatement(SQL_UPDATE_PERMS);
            int pos = 1;
            stmt.setInt(pos++, folderPermission);
            stmt.setInt(pos++, objectReadPermission);
            stmt.setInt(pos++, objectWritePermission);
            stmt.setInt(pos++, objectDeletePermission);
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, permissionId);
            final boolean failed = executeUpdate(stmt) != 1;

            if (failed) {
                if (startedTransaction) {
                    wc.commit();
                    rollback = false;
                    wc.setAutoCommit(true);
                }
                return false;
            }
            closeSQLStuff(null, stmt);
            stmt = null;

            // Update last-modified to propagate changes to clients
            updateLastModified(folderId, System.currentTimeMillis(), wc, ctx);

            if (startedTransaction) {
                wc.commit();
                rollback = false;
                wc.setAutoCommit(true);
            }

            return true;
        } finally {
            if (startedTransaction && rollback) {
                if (null != wc) {
                    wc.rollback();
                    wc.setAutoCommit(true);
                }
            }
            closeResources(null, stmt, closeWriteCon ? wc : null, false, ctx);
        }
    }

    private static final String SQL_ADD_PERMS = "INSERT INTO oxfolder_permissions" + " (cid, fuid, permission_id, group_flag, fp, orp, owp, odp, admin_flag, system)" + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Inserts a single folder permission.
     *
     * @param folderId The folder ID
     * @param permissionId The entity ID; either user or group ID
     * @param isGroup <code>true</code> if permission ID denotes a group; otherwise <code>false</code>
     * @param folderPermission The folder permission to set
     * @param objectReadPermission The object read permission to set
     * @param objectWritePermission The object write permission to set
     * @param objectDeletePermission The object delete permission to set
     * @param isAdmin <code>true</code> if permission ID is a folder administrator; otherwise <code>false</code>
     * @param system The system bit mask
     * @param writeCon A connection with write capability; may be <code>null</code> to fetch from pool
     * @param ctx The context
     * @return <code>true</code> if corresponding entry was successfully inserted; otherwise <code>false</code>
     * @throws OXException If a pooling error occurred
     * @throws SQLException If a SQL error occurred
     */
    public static boolean addSinglePermission(final int folderId, final int permissionId, final boolean isGroup, final int folderPermission, final int objectReadPermission, final int objectWritePermission, final int objectDeletePermission, final boolean isAdmin, final int system, final Connection writeCon, final Context ctx) throws OXException, SQLException {
        Connection wc = writeCon;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            startedTransaction = wc.getAutoCommit();
            if (startedTransaction) {
                wc.setAutoCommit(false);
                rollback = true;
            }

            // Acquire lock
            lock(folderId, ctx.getContextId(), wc);

            stmt = wc.prepareStatement("SELECT 1 FROM oxfolder_permissions WHERE cid=? AND permission_id=? AND fuid=? AND system=?");
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, permissionId);
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, system);
            rs = stmt.executeQuery();
            final boolean alreadyExists = rs.next();
            closeSQLStuff(rs, stmt);
            rs = null;
            stmt = null;

            // Do the update if absent
            final boolean success;
            if (alreadyExists) {
                success = true;
            } else {
                stmt = wc.prepareStatement(SQL_ADD_PERMS);
                pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, folderId);
                stmt.setInt(pos++, permissionId);
                stmt.setInt(pos++, isGroup ? 1 : 0);
                stmt.setInt(pos++, folderPermission);
                stmt.setInt(pos++, objectReadPermission);
                stmt.setInt(pos++, objectWritePermission);
                stmt.setInt(pos++, objectDeletePermission);
                stmt.setInt(pos++, isAdmin ? 1 : 0);
                stmt.setInt(pos++, system);
                success = executeUpdate(stmt) == 1;
            }

            if (startedTransaction) {
                wc.commit();
                rollback = false;
                wc.setAutoCommit(true);
            }

            return success;
        } finally {
            if (startedTransaction && rollback) {
                if (null != wc) {
                    wc.rollback();
                    wc.setAutoCommit(true);
                }
            }
            closeResources(rs, stmt, closeWriteCon ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REM_SINGLE_SYS_PERM = "DELETE FROM oxfolder_permissions " + "WHERE cid = ? AND fuid = ? AND permission_id = ? AND system = 1";

    /**
     * Deletes a single system permission
     *
     * @param folderId The folder ID
     * @param permissionId The entity ID; either user or group ID
     * @param writeCon A connection with write capability; may be <code>null</code> to fetch from pool
     * @param ctx The context
     * @return <code>true</code> if corresponding entry was successfully deleted; otherwise <code>false</code>
     * @throws OXException If a pooling error occurred
     * @throws SQLException If a SQL error occurred
     */
    public static boolean deleteSingleSystemPermission(final int folderId, final int permissionId, final Connection writeCon, final Context ctx) throws OXException, SQLException {
        Connection wc = writeCon;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = wc.prepareStatement(SQL_REM_SINGLE_SYS_PERM);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            stmt.setInt(pos++, permissionId);
            return (executeUpdate(stmt) == 1);
        } finally {
            closeResources(null, stmt, closeWriteCon ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REM_ALL_SYS_PERM = "DELETE FROM oxfolder_permissions " + "WHERE cid = ? AND fuid = ? AND system = 1";

    /**
     * Deletes all system permission from specified folder
     *
     * @param folderId The folder ID
     * @param writeCon A writable connection
     * @param ctx The context
     * @throws OXException If a pooling error occurred
     * @throws SQLException If a SQL error occurred
     */
    public static void deleteAllSystemPermission(final int folderId, final Connection writeCon, final Context ctx) throws OXException, SQLException {
        Connection wc = writeCon;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            stmt = wc.prepareStatement(SQL_REM_ALL_SYS_PERM);
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWriteCon ? wc : null, false, ctx);
        }
    }

    private static final String SQL_GETSUBFLDIDS = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ?";

    /**
     * Creates a <tt>TIntList</tt> instance containing all subfolder IDs of given folder
     *
     * @return a <tt>TIntList</tt> instance containing all subfolder IDs of given folder
     */
    public static TIntList getSubfolderIDs(final int folderId, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        final TIntList retval = new TIntArrayList();
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_GETSUBFLDIDS);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = executeQuery(stmt);
            while (rs.next()) {
                retval.add(rs.getInt(1));
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
        return retval;
    }

    /**
     * Gets the IDs of all folders whose parent folder ID equals the supplied one, i.e. the IDs of all subfolders.
     *
     * @param folderId The ID of the parent folder to get the subfolder IDs for
     * @param readConnection A connection with read capability, or <code>null</code> to fetch from pool dynamically
     * @param context The context
     * @param recursive <code>true</code> to lookup subfolder IDs recursively, <code>false</code>, otherwise
     * @return The subfolder IDs, or an empty list if none were found
     */
    public static List<Integer> getSubfolderIDs(int folderId, Connection readConnection, Context context, boolean recursive) throws OXException, SQLException {
        List<Integer> subfolderIDs = new ArrayList<Integer>();
        boolean closeReadConnection = false;
        try {
            /*
             * acquire local read connection if not supplied
             */
            if (null == readConnection) {
                readConnection = DBPool.pickup(context);
                closeReadConnection = true;
            }
            List<Integer> parentFolderIDs = new ArrayList<Integer>();
            parentFolderIDs.add(folderId);
            do {
                /*
                 * build statement for current parent folder IDs
                 */
                StringBuilder stringBuilder = new StringBuilder("SELECT fuid FROM oxfolder_tree WHERE cid=? AND parent");
                if (1 == parentFolderIDs.size()) {
                    stringBuilder.append("=?;");
                } else {
                    stringBuilder.append(" IN (?");
                    for (int i = 1; i < parentFolderIDs.size(); i++) {
                        stringBuilder.append(",?");
                    }
                    stringBuilder.append(");");
                }
                /*
                 * execute
                 */
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = readConnection.prepareStatement(stringBuilder.toString());
                    stmt.setInt(1, context.getContextId());
                    for (int i = 0; i < parentFolderIDs.size(); i++) {
                        stmt.setInt(i + 2, parentFolderIDs.get(i).intValue());
                    }
                    parentFolderIDs.clear();
                    rs = executeQuery(stmt);
                    while (rs.next()) {
                        Integer folderID = Integer.valueOf(rs.getInt(1));
                        subfolderIDs.add(folderID);
                        parentFolderIDs.add(folderID);
                    }
                } finally {
                    closeSQLStuff(rs, stmt);
                }
            } while (recursive && false == parentFolderIDs.isEmpty());
        } finally {
            closeResources(null, null, closeReadConnection ? readConnection : null, true, context);
        }
        return subfolderIDs;
    }

    /**
     * Gets the identifiers of all permission entities belonging to one of the supplied folder identifiers.
     *
     * @param folderIDs The folder identifiers to get the permission entities for
     * @param readConnection A connection with read capability, or <code>null</code> to fetch from pool dynamically
     * @param context The context
     * @param includeGroups <code>true</code> to also include group permissions, <code>false</code>, otherwise
     * @return The entity IDs, or an empty list if none were found
     */
    public static List<Integer> getPermissionEntities(List<Integer> folderIDs, Connection readConnection, Context context, boolean includeGroups) throws OXException, SQLException {
        /*
         * build statement
         */
        StringBuilder stringBuilder = new StringBuilder("SELECT DISTINCT permission_id FROM oxfolder_permissions WHERE cid=? AND fuid");
        if (1 == folderIDs.size()) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < folderIDs.size(); i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(')');
        }
        if (false == includeGroups) {
            stringBuilder.append(" AND group_flag=0");
        }
        List<Integer> entityIDs = new ArrayList<Integer>();
        boolean closeReadConnection = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * acquire local read connection if not supplied
             */
            if (null == readConnection) {
                readConnection = DBPool.pickup(context);
                closeReadConnection = true;
            }
            /*
             * execute query
             */
            stmt = readConnection.prepareStatement(stringBuilder.toString());
            stmt.setInt(1, context.getContextId());
            for (int i = 0; i < folderIDs.size(); i++) {
                stmt.setInt(i + 2, folderIDs.get(i).intValue());
            }
            rs = executeQuery(stmt);
            while (rs.next()) {
                entityIDs.add(Integer.valueOf(rs.getInt(1)));
            }
        } finally {
            closeResources(rs, stmt, closeReadConnection ? readConnection : null, true, context);
        }
        return entityIDs;
    }

    /**
     * Gets the identifiers of all parent folders in the tree down to the root folder.
     *
     * @param folderId The ID of the folder to get the path for
     * @param readConnection A connection with read capability, or <code>null</code> to fetch from pool dynamically
     * @param context The context
     * @return The IDs of all parent folders on the path in (hierarchical) descending order; the supplied folder ID itself is not included
     */
    public static List<Integer> getPathToRoot(int folderId, Connection readConnection, Context context) throws OXException, SQLException {
        List<Integer> subfolderIDs = new ArrayList<Integer>();
        boolean closeReadConnection = false;
        try {
            /*
             * acquire local read connection if not supplied
             */
            if (null == readConnection) {
                readConnection = DBPool.pickup(context);
                closeReadConnection = true;
            }
            /*
             * get parent folders recursively
             */
            int currentID = folderId;
            while (FolderObject.SYSTEM_ROOT_FOLDER_ID != currentID) {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    stmt = readConnection.prepareStatement("SELECT parent FROM oxfolder_tree WHERE cid=? AND fuid=?;");
                    stmt.setInt(1, context.getContextId());
                    stmt.setInt(2, currentID);
                    rs = executeQuery(stmt);
                    currentID = rs.next() ? Integer.valueOf(rs.getInt(1)) : 0;
                } finally {
                    closeSQLStuff(rs, stmt);
                }
                subfolderIDs.add(Integer.valueOf(currentID));
            }
        } finally {
            closeResources(null, null, closeReadConnection ? readConnection : null, true, context);
        }
        return subfolderIDs;
    }

    /**
     * Gets the parent identifier for specified folder.
     *
     * @param folder The folder identifier
     * @param ctx The associated context
     * @return The parent identifier or <code>-1</code>
     * @throws OXException If an Open-Xchange error occurs
     * @throws SQLException If an SQL error occurs
     */
    public static int getParentId(int folder, Context ctx) throws OXException, SQLException {
        Connection connection = DBPool.pickup(ctx);
        try {
            return getParentId(folder, ctx, connection);
        } finally {
            DBPool.closeReaderSilent(ctx, connection);
        }
    }

    /**
     * Gets the parent identifier for specified folder.
     *
     * @param folder The folder identifier
     * @param ctx The associated context
     * @param connection The connection to use
     * @return The parent identifier or <code>-1</code>
     * @throws OXException If an Open-Xchange error occurs
     * @throws SQLException If an SQL error occurs
     */
    public static int getParentId(int folder, Context ctx, Connection connection) throws OXException, SQLException {
        if (null == connection) {
            return getParentId(folder, ctx);
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = connection.prepareStatement("SELECT parent FROM oxfolder_tree WHERE cid=? AND fuid=?");
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folder);
            rs = stmt.executeQuery();
            return rs.next() ? rs.getInt(1) : -1;
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

    /**
     * Gets a folder's path down to the root folder, ready to be used in events.
     *
     * @param folder The folder to get the path for
     * @param connection A connection to use
     * @return The folder path
     * @throws OXException
     * @throws SQLException
     */
    public static String[] getFolderPath(int folder, Connection connection, Context ctx) throws OXException {
        List<String> folderPath = new ArrayList<String>();
        folderPath.add(String.valueOf(folder));
        int startID = folder;
        if (FolderObject.SYSTEM_ROOT_FOLDER_ID != startID) {
            try {
                List<Integer> pathToRoot = getPathToRoot(startID, connection, ctx);
                for (Integer id : pathToRoot) {
                    folderPath.add(String.valueOf(id));
                }
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        return folderPath.toArray(new String[folderPath.size()]);
    }

    private static final String SQL_UDTSUBFLDFLG = "UPDATE oxfolder_tree SET subfolder_flag = ?, changing_date = ? WHERE cid = ? AND fuid = ?";

    /**
     * Updates the field 'subfolder_flag' of matching folder in underlying storage
     */
    static void updateSubfolderFlag(final int folderId, final boolean hasSubfolders, final long lastModified, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeCon = true;
            }
            startedTransaction = writeCon.getAutoCommit();
            if (startedTransaction) {
                writeCon.setAutoCommit(false);
                rollback = true;
            }

            // Acquire lock
            lock(folderId, ctx.getContextId(), writeCon);

            // Do the update
            stmt = writeCon.prepareStatement(SQL_UDTSUBFLDFLG);
            stmt.setInt(1, hasSubfolders ? 1 : 0);
            stmt.setLong(2, lastModified);
            stmt.setInt(3, ctx.getContextId());
            stmt.setInt(4, folderId);
            executeUpdate(stmt);

            if (startedTransaction) {
                writeCon.commit();
                rollback = false;
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (startedTransaction && rollback) {
                if (null != writeCon) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
            }
            closeResources(null, stmt, closeCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_NUMSUB = "SELECT COUNT(ot.fuid) FROM oxfolder_tree AS ot JOIN oxfolder_permissions AS op" + " ON ot.fuid = op.fuid AND ot.cid = ? AND op.cid = ?" + " WHERE op.permission_id IN #IDS# AND op.admin_flag > 0 AND ot.parent = ?";

    /**
     * @return the number of subfolders of given folder which can be moved according to user's permissions
     */
    public static int getNumOfMoveableSubfolders(final int folderId, final int userId, final int[] groups, final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_NUMSUB.replaceFirst("#IDS#", StringCollection.getSqlInString(userId, groups)));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            stmt.setInt(3, folderId);
            rs = executeQuery(stmt);
            if (rs.next()) {
                return rs.getInt(1);
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
        return 0;
    }

    private static final String SQL_INSERT_NEW_FOLDER = "INSERT INTO oxfolder_tree (fuid, cid, parent, fname, module, type, creating_date,"
        + " created_from, changing_date, changed_from, permission_flag, subfolder_flag, default_flag) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

    private static final String SQL_INSERT_NEW_PERMISSIONS = "INSERT INTO oxfolder_permissions " + "(cid, fuid, permission_id, fp, orp, owp, odp, admin_flag, group_flag) " + "VALUES (?,?,?,?,?,?,?,?,?)";

    private static final String SQL_UPDATE_PARENT_SUBFOLDER_FLAG = "UPDATE oxfolder_tree " + "SET subfolder_flag = 1, changing_date = ? WHERE cid = ? AND fuid = ?";

    static void insertFolderSQL(final int newFolderID, final int userId, final FolderObject folder, final long creatingTime, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        insertFolderSQL(newFolderID, userId, folder, creatingTime, false, ctx, writeConArg);
    }

    static void insertDefaultFolderSQL(final int newFolderID, final int userId, final FolderObject folder, final long creatingTime, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        insertFolderSQL(newFolderID, userId, folder, creatingTime, true, ctx, writeConArg);
    }

    private static void insertFolderSQL(final int newFolderID, final int userId, final FolderObject folder, final long creatingTime, final boolean acceptDefaultFlag, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        int permissionFlag = determinePermissionFlag(folder);
        boolean closeWriteCon = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            final boolean startedTransaction = writeCon.getAutoCommit();
            if (startedTransaction) {
                writeCon.setAutoCommit(false);
            }
            try {
                InputStream metaStream = null;
                PreparedStatement stmt = null;
                try {
                    // Acquire lock
                    lock(folder.getParentFolderID(), ctx.getContextId(), writeCon);

                    // Do the insert
                    stmt = writeCon.prepareStatement("INSERT INTO oxfolder_tree " +
                        "(fuid,cid,parent,fname,module,type,creating_date,created_from,changing_date,changed_from,permission_flag,subfolder_flag," +
                        "default_flag,meta) SELECT ?,?,?,?,?,?,?,?,?,?,?,?,?,? FROM DUAL " +
                        "WHERE NOT EXISTS (SELECT 1 FROM oxfolder_tree WHERE cid=? AND parent=? AND fname=? AND parent>?);");
                    stmt.setInt(1, newFolderID);
                    stmt.setInt(2, ctx.getContextId());
                    stmt.setInt(3, folder.getParentFolderID());
                    stmt.setString(4, folder.getFolderName());
                    stmt.setInt(5, folder.getModule());
                    stmt.setInt(6, folder.getType());
                    stmt.setLong(7, creatingTime);
                    stmt.setInt(8, folder.containsCreatedBy() ? folder.getCreatedBy() : userId);
                    stmt.setLong(9, creatingTime);
                    stmt.setInt(10, userId);
                    stmt.setInt(11, permissionFlag);
                    stmt.setInt(12, 0); // new folder does not contain
                    // subfolders
                    if (acceptDefaultFlag) {
                        stmt.setInt(13, folder.isDefaultFolder() ? 1 : 0); // default_flag
                    } else {
                        stmt.setInt(13, 0); // default_flag
                    }
                    {
                        metaStream = OXFolderUtility.serializeMeta(folder.getMeta());
                        if (null == metaStream) {
                            stmt.setNull(14, java.sql.Types.BLOB);
                        } else {
                            stmt.setBinaryStream(14, metaStream); // meta
                        }
                    }
                    stmt.setInt(15, ctx.getContextId());
                    stmt.setInt(16, folder.getParentFolderID());
                    stmt.setString(17, folder.getFolderName());
                    stmt.setInt(18, FolderObject.MIN_FOLDER_ID);
                    if (0 == executeUpdate(stmt)) {
                        // due to already existing subfolder with the same name
                        throw new SQLException("Entry not inserted");
                    }
                    stmt.close();
                    stmt = null;
                    /*
                     * Mark parent folder to have subfolders
                     */
                    stmt = writeCon.prepareStatement(SQL_UPDATE_PARENT_SUBFOLDER_FLAG);
                    stmt.setLong(1, creatingTime);
                    stmt.setInt(2, ctx.getContextId());
                    stmt.setInt(3, folder.getParentFolderID());
                    executeUpdate(stmt);
                    stmt.close();
                    stmt = null;
                    /*
                     * Insert permissions
                     */
                    stmt = writeCon.prepareStatement(SQL_INSERT_NEW_PERMISSIONS);
                    final OCLPermission[] permissions = folder.getNonSystemPermissionsAsArray();
                    for (final OCLPermission ocl : permissions) {
                        stmt.setInt(1, ctx.getContextId());
                        stmt.setInt(2, newFolderID);
                        stmt.setInt(3, ocl.getEntity());
                        stmt.setInt(4, ocl.getFolderPermission());
                        stmt.setInt(5, ocl.getReadPermission());
                        stmt.setInt(6, ocl.getWritePermission());
                        stmt.setInt(7, ocl.getDeletePermission());
                        stmt.setInt(8, ocl.isFolderAdmin() ? 1 : 0);
                        stmt.setInt(9, ocl.isGroupPermission() ? 1 : 0);
                        stmt.addBatch();
                    }
                    executeBatch(stmt);
                    stmt.close();
                    stmt = null;
                    final Date creatingDate = new Date(creatingTime);
                    folder.setObjectID(newFolderID);
                    folder.setCreationDate(creatingDate);
                    folder.setCreatedBy(userId);
                    folder.setLastModified(creatingDate);
                    folder.setModifiedBy(userId);
                    folder.setSubfolderFlag(false);
                    if (!acceptDefaultFlag) {
                        folder.setDefaultFolder(false);
                    }
                } finally {
                    if (stmt != null) {
                        stmt.close();
                        stmt = null;
                    }
                    Streams.close(metaStream);
                }
            } catch (final SQLException e) {
                if (startedTransaction) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw e;
            } catch (final JSONException e) {
                if (startedTransaction) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw OXFolderExceptionCode.JSON_ERROR.create(e, e.getMessage());
            }
            if (startedTransaction) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (closeWriteCon && writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    /**
     * Transforms an existing folder into a default folder of a certain type by setting the <code>default_flag</code> to <code>1</code>.
     *
     * @param connection A (writable) connection to the database, or <code>null</code> to fetch one on demand
     * @param context The context
     * @param folderID The identifier of the folder to mark as default folder
     * @param type The type to apply for the folder
     * @param folderName The name to apply for the folder
     * @param lastModified The last modification timestamp to apply for the folder
     */
    static void markAsDefaultFolder(Connection connection, Context context, int folderID, int type, String folderName, long lastModified) throws SQLException, OXException {
        Connection writeCon = connection;
        boolean closeWriteCon = false;
        PreparedStatement stmt = null;
        boolean rollback = false;
        boolean startedTransaction = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(context);
                closeWriteCon = true;
            }
            startedTransaction = writeCon.getAutoCommit();
            if (startedTransaction) {
                writeCon.setAutoCommit(false);
                rollback = true;
            }
            lock(folderID, context.getContextId(), writeCon);
            stmt = writeCon.prepareStatement("UPDATE oxfolder_tree SET type=?,default_flag=1,fname=?,changing_date=? WHERE cid=? AND fuid=?;");
            stmt.setInt(1, type);
            stmt.setString(2, folderName);
            stmt.setLong(3, lastModified);
            stmt.setInt(4, context.getContextId());
            stmt.setInt(5, folderID);
            executeUpdate(stmt);
            if (startedTransaction) {
                writeCon.commit();
                rollback = false;
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (startedTransaction && rollback) {
                if (null != writeCon) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
            }
            closeResources(null, stmt, closeWriteCon ? writeCon : null, false, context);
        }
    }

    private static final String SQL_DELETE_EXISTING_PERMISSIONS = "DELETE FROM oxfolder_permissions WHERE cid = ? AND fuid = ? AND system = 0";

    static void updateFolderSQL(final int userId, final FolderObject folder, final long lastModified, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        /*
         * Update Folder
         */
        int permissionFlag = determinePermissionFlag(folder);
        boolean closeWriteCon = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            final boolean startedTransaction = writeCon.getAutoCommit();
            if (startedTransaction) {
                writeCon.setAutoCommit(false);
            }
            PreparedStatement stmt = null;
            InputStream metaStream = null;
            try {
                // Acquire lock
                lock(folder.getObjectID(), ctx.getContextId(), writeCon);

                // Do the update
                int pos = 1;
                final boolean containsMeta = folder.containsMeta();
                final boolean containsCreatedBy = folder.containsCreatedBy();
                if (folder.containsFolderName()) {
                    stmt = writeCon.prepareStatement("UPDATE oxfolder_tree SET fname=?" + (containsMeta ? ",meta=?" : "") +
                        ",changing_date=?,changed_from=?,permission_flag=?,module=?" + (containsCreatedBy ? ",created_from=?" : "") +
                        " WHERE cid=? AND fuid=? AND NOT EXISTS (SELECT 1 FROM (" +
                        "SELECT fname,fuid FROM oxfolder_tree WHERE cid=? AND parent=? AND parent>?) AS ft WHERE ft.fname=? AND ft.fuid<>?);");
                    stmt.setString(pos++, folder.getFolderName());
                    if (containsMeta) {
                        metaStream = OXFolderUtility.serializeMeta(folder.getMeta());
                        if (null == metaStream) {
                            stmt.setNull(pos++, java.sql.Types.BLOB);
                        } else {
                            stmt.setBinaryStream(pos++, metaStream); // meta
                        }
                    }
                    stmt.setLong(pos++, lastModified);
                    stmt.setInt(pos++, userId);
                    stmt.setInt(pos++, permissionFlag);
                    stmt.setInt(pos++, folder.getModule());
                    if (containsCreatedBy) {
                        stmt.setInt(pos++, folder.getCreatedBy());
                    }
                    stmt.setInt(pos++, ctx.getContextId());
                    stmt.setInt(pos++, folder.getObjectID());
                    stmt.setInt(pos++, ctx.getContextId());
                    stmt.setInt(pos++, folder.getParentFolderID());
                    stmt.setInt(pos++, FolderObject.MIN_FOLDER_ID);
                    stmt.setString(pos++, folder.getFolderName());
                    stmt.setInt(pos++, folder.getObjectID());
                    if (0 == executeUpdate(stmt)) {
                        // due to already existing subfolder with the same name
                        throw new SQLException("Entry not updated");
                    }
                    stmt.close();
                    stmt = null;
                } else {
                    stmt = writeCon.prepareStatement("UPDATE oxfolder_tree SET " + (containsMeta ? "meta = ?, " : "") +
                        "changing_date = ?, changed_from = ?, " + "permission_flag = ?, module = ? " +
                        (containsCreatedBy ? ", created_from = ? " : "") + "WHERE cid = ? AND fuid = ?");
                    if (containsMeta) {
                        metaStream = OXFolderUtility.serializeMeta(folder.getMeta());
                        if (null == metaStream) {
                            stmt.setNull(pos++, java.sql.Types.BLOB);
                        } else {
                            stmt.setBinaryStream(pos++, metaStream); // meta
                        }
                    }
                    stmt.setLong(pos++, lastModified);
                    stmt.setInt(pos++, userId);
                    stmt.setInt(pos++, permissionFlag);
                    stmt.setInt(pos++, folder.getModule());
                    if (containsCreatedBy) {
                        stmt.setInt(pos++, folder.getCreatedBy());
                    }
                    stmt.setInt(pos++, ctx.getContextId());
                    stmt.setInt(pos++, folder.getObjectID());
                    executeUpdate(stmt);
                    stmt.close();
                    stmt = null;
                }
                /*
                 * Delete old non-system-permissions
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_EXISTING_PERMISSIONS);
                pos = 1;
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, folder.getObjectID());
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
                /*
                 * Insert new non-system-permissions
                 */
                stmt = writeCon.prepareStatement(SQL_INSERT_NEW_PERMISSIONS);
                final OCLPermission[] permissions = folder.getNonSystemPermissionsAsArray();
                for (final OCLPermission oclPerm : permissions) {
                    pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
                    stmt.setInt(pos++, folder.getObjectID());
                    stmt.setInt(pos++, oclPerm.getEntity());
                    stmt.setInt(pos++, oclPerm.getFolderPermission());
                    stmt.setInt(pos++, oclPerm.getReadPermission());
                    stmt.setInt(pos++, oclPerm.getWritePermission());
                    stmt.setInt(pos++, oclPerm.getDeletePermission());
                    stmt.setInt(pos++, oclPerm.isFolderAdmin() ? 1 : 0);
                    stmt.setInt(pos++, oclPerm.isGroupPermission() ? 1 : 0);
                    stmt.addBatch();
                }
                executeBatch(stmt);
                stmt.close();
                stmt = null;
            } catch (final SQLException e) {
                if (startedTransaction) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw e;
            } catch (final JSONException e) {
                if (startedTransaction) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw OXFolderExceptionCode.JSON_ERROR.create(e, e.getMessage());
            } finally {
                if (stmt != null) {
                    stmt.close();
                    stmt = null;
                }
                Streams.close(metaStream);
            }
            if (startedTransaction) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (closeWriteCon && writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String SQL_MOVE_UPDATE = "UPDATE oxfolder_tree SET parent=?,changing_date=?,changed_from=?,fname=? " +
        "WHERE cid=? AND fuid=? AND NOT EXISTS (SELECT 1 FROM (" +
        "SELECT fname,fuid FROM oxfolder_tree WHERE cid=? AND parent=? AND parent>?) AS ft WHERE ft.fname=? AND ft.fuid<>?);"
    ;

    private static final String SQL_MOVE_SELECT = "SELECT fuid FROM oxfolder_tree WHERE cid = ? AND parent = ?";

    private static final String SQL_MOVE_UPDATE2 = "UPDATE oxfolder_tree SET subfolder_flag = ?, changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    static void moveFolderSQL(final int userId, final FolderObject src, final FolderObject dest, final long lastModified, final Context ctx, final Connection readConArg, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        Connection readCon = readConArg;
        PreparedStatement pst = null;
        ResultSet subFolderRS = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            if (readCon == null) {
                /*
                 * Use write-connection as read-connection
                 */
                readCon = writeCon;
            }
            final boolean isAuto = writeCon.getAutoCommit();
            if (isAuto) {
                writeCon.setAutoCommit(false);
            }
            try {
                int srcParentId = getParentId(src.getObjectID(), ctx, writeCon);
                int destParentId = getParentId(dest.getObjectID(), ctx, writeCon);

                // Acquire lock
                lock(srcParentId > 0 ? srcParentId : src.getObjectID(), ctx.getContextId(), writeCon);
                lock(destParentId > 0 ? destParentId : dest.getObjectID(), ctx.getContextId(), writeCon);

                // Do the move
                pst = writeCon.prepareStatement(SQL_MOVE_UPDATE);
                pst.setInt(1, dest.getObjectID());
                pst.setLong(2, lastModified);
                pst.setInt(3, src.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
                pst.setString(4, src.getFolderName());
                pst.setInt(5, ctx.getContextId());
                pst.setInt(6, src.getObjectID());
                pst.setInt(7, ctx.getContextId());
                pst.setInt(8, dest.getObjectID());
                pst.setInt(9, dest.getObjectID());
                pst.setString(10, src.getFolderName());
                pst.setInt(11, src.getObjectID());
                if (0 == executeUpdate(pst)) {
                    // due to already existing subfolder with the same name
                    throw new SQLException("Entry not updated");
                }
                pst.close();
                pst = null;
                /*
                 * Set target folder's/source parent folder's subfolder flag
                 */
                pst = readCon.prepareStatement(SQL_MOVE_SELECT);
                pst.setInt(1, ctx.getContextId());
                pst.setInt(2, src.getParentFolderID());
                subFolderRS = executeQuery(pst);
                final boolean srcParentHasSubfolders = subFolderRS.next();
                subFolderRS.close();
                subFolderRS = null;
                pst.close();
                pst = null;
                pst = writeCon.prepareStatement(SQL_MOVE_UPDATE2);
                pst.setInt(1, 1);
                pst.setLong(2, lastModified);
                pst.setInt(3, dest.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
                pst.setInt(4, ctx.getContextId());
                pst.setInt(5, dest.getObjectID());
                pst.addBatch();
                pst.setInt(1, srcParentHasSubfolders ? 1 : 0);
                pst.setLong(2, lastModified);
                pst.setInt(3, src.getType() == FolderObject.SYSTEM_TYPE ? ctx.getMailadmin() : userId);
                pst.setInt(4, ctx.getContextId());
                pst.setInt(5, src.getParentFolderID());
                pst.addBatch();
                executeBatch(pst);
                pst.close();
                pst = null;
            } catch (final SQLException se) {
                if (isAuto) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw se;
            }
            if (isAuto) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        } finally {
            closeSQLStuff(subFolderRS, pst);
            if (closeWriteCon && writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String SQL_RENAME_UPDATE = "UPDATE oxfolder_tree SET fname = ?, changing_date = ?, changed_from = ? where cid = ? AND fuid = ?";

    static void renameFolderSQL(final int userId, final FolderObject folderObj, final long lastModified, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                closeWriteCon = true;
            }
            final boolean isAuto = writeCon.getAutoCommit();
            if (isAuto) {
                writeCon.setAutoCommit(false);
            }
            PreparedStatement pst = null;
            try {
                // Acquire lock
                lock(folderObj.getObjectID(), ctx.getContextId(), writeCon);

                // Do the rename
                pst = writeCon.prepareStatement(SQL_RENAME_UPDATE);
                pst.setString(1, folderObj.getFolderName());
                pst.setLong(2, lastModified);
                pst.setInt(3, userId);
                pst.setInt(4, ctx.getContextId());
                pst.setInt(5, folderObj.getObjectID());
                executeUpdate(pst);
                pst.close();
                pst = null;
            } catch (final SQLException sqle) {
                if (isAuto) {
                    writeCon.rollback();
                    writeCon.setAutoCommit(true);
                }
                throw sqle;
            } finally {
                if (pst != null) {
                    pst.close();
                    pst = null;
                }
            }
            if (isAuto) {
                writeCon.commit();
                writeCon.setAutoCommit(true);
            }
        } finally {
            if (closeWriteCon && writeCon != null) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String STR_OXFOLDERTREE = "oxfolder_tree";

    private static final String STR_OXFOLDERPERMS = "oxfolder_permissions";

    private static final String STR_DELOXFOLDERTREE = "del_oxfolder_tree";

    private static final String STR_DELOXFOLDERPERMS = "del_oxfolder_permissions";

    static void delWorkingOXFolder(final int folderId, final int userId, final long lastModified, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        delOXFolder(folderId, userId, lastModified, true, true, ctx, writeConArg);
    }

    private static final String SQL_DELETE_INSERT_OT = "INSERT INTO del_oxfolder_tree (cid, fuid, parent, module, type, creating_date, created_from, changing_date, changed_from, permission_flag, subfolder_flag, default_flag) SELECT oxfolder_tree.cid, oxfolder_tree.fuid, oxfolder_tree.parent, oxfolder_tree.module, oxfolder_tree.type, oxfolder_tree.creating_date, oxfolder_tree.created_from, oxfolder_tree.changing_date, oxfolder_tree.changed_from, oxfolder_tree.permission_flag, oxfolder_tree.subfolder_flag, oxfolder_tree.default_flag FROM oxfolder_tree WHERE oxfolder_tree.cid = ? AND oxfolder_tree.fuid = ?";

    private static final String SQL_DELETE_INSERT_OP = "INSERT INTO del_oxfolder_permissions SELECT * FROM oxfolder_permissions WHERE cid = ? AND fuid = ?";

    private static final String SQL_DELETE_DELETE_SF = "DELETE FROM oxfolder_specialfolders WHERE cid = ? AND fuid = ?";

    private static final String SQL_DELETE_DELETE = "DELETE FROM #TABLE# WHERE cid = ? AND fuid = ?";

    private static final String SQL_DELETE_UPDATE = "UPDATE del_oxfolder_tree SET changing_date = ?, changed_from = ? WHERE cid = ? AND fuid = ?";

    /**
     * Deletes a folder entry - and its corresponding permission entries as well - from underlying storage. <code>deleteWorking</code>
     * determines whether working or backup tables are affected by delete operation. <code>createBackup</code> specifies if backup entries
     * are going to be created and is only allowed if <code>deleteWorking</code> is set to <code>true</code>.
     */
    static void delOXFolder(final int folderId, final int userId, final long lastModified, final boolean deleteWorking, final boolean createBackup, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        if (writeCon == null) {
            writeCon = DBPool.pickupWriteable(ctx);
            closeWriteCon = true;
        }
        final boolean isAuto = writeCon.getAutoCommit();
        if (isAuto) {
            writeCon.setAutoCommit(false);
        }
        final String folderTable = deleteWorking ? STR_OXFOLDERTREE : STR_DELOXFOLDERTREE;
        final String permTable = deleteWorking ? STR_OXFOLDERPERMS : STR_DELOXFOLDERPERMS;
        final boolean backup = (createBackup && deleteWorking);
        PreparedStatement stmt = null;
        try {
            int parent = getParentId(folderId, ctx, writeCon);

            // Acquire lock
            lock(parent > 0 ? parent : folderId, ctx.getContextId(), backup, writeCon);

            // Do delete
            if (backup) {
                /*
                 * Clean backup tables
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERPERMS));
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
                stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERTREE));
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
                /*
                 * Copy backup entries into del_oxfolder_tree and del_oxfolder_permissions
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OT);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
                stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OP);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
            }
            if (deleteWorking) {
                /*
                 * Delete from oxfolder_specialfolders
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_DELETE_SF);
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
            }
            /*
             * Delete from permission table
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", permTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Delete from folder table
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", folderTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            if (backup) {
                /*
                 * Update last-modified timestamp of entries in backup tables
                 */
                stmt = writeCon.prepareStatement(SQL_DELETE_UPDATE);
                stmt.setLong(1, lastModified);
                stmt.setInt(2, userId);
                stmt.setInt(3, ctx.getContextId());
                stmt.setInt(4, folderId);
                executeUpdate(stmt);
                stmt.close();
                stmt = null;
            }
            /*
             * Commit
             */
            if (isAuto) {
                writeCon.commit();
            }
        } catch (final SQLException e) {
            if (isAuto) {
                DBUtils.rollback(writeCon);
            }
            throw e;
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (isAuto) {
                DBUtils.autocommit(writeCon);
            }
            if (closeWriteCon) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    static void backupOXFolder(final int folderId, final int userId, final long lastModified, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        if (writeCon == null) {
            writeCon = DBPool.pickupWriteable(ctx);
            closeWriteCon = true;
        }
        final boolean isAuto = writeCon.getAutoCommit();
        if (isAuto) {
            writeCon.setAutoCommit(false);
        }
        PreparedStatement stmt = null;
        try {
            // Acquire lock
            lock(folderId, ctx.getContextId(), true, writeCon);

            // Clean backup tables
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERPERMS));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERTREE));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Copy backup entries into del_oxfolder_tree and del_oxfolder_permissions
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OT);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            stmt = writeCon.prepareStatement(SQL_DELETE_INSERT_OP);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Update last-modified timestamp of entries in backup tables
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_UPDATE);
            stmt.setLong(1, lastModified);
            stmt.setInt(2, userId);
            stmt.setInt(3, ctx.getContextId());
            stmt.setInt(4, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Commit
             */
            if (isAuto) {
                writeCon.commit();
            }
        } catch (final SQLException e) {
            if (isAuto) {
                DBUtils.rollback(writeCon);
            }
            throw e;
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (isAuto) {
                DBUtils.autocommit(writeCon);
            }
            if (closeWriteCon) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    private static final String SQL_RESTORE_OT = "INSERT INTO oxfolder_tree SELECT * FROM del_oxfolder_tree WHERE cid = ? AND fuid = ?";

    private static final String SQL_RESTORE_OP = "INSERT INTO oxfolder_permissions SELECT * FROM del_oxfolder_permissions WHERE cid = ? AND fuid = ?";

    public static void restore(final int folderId, final Context ctx, final Connection writeConArg) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean closeWriteCon = false;
        if (writeCon == null) {
            writeCon = DBPool.pickupWriteable(ctx);
            closeWriteCon = true;
        }
        final boolean isAuto = writeCon.getAutoCommit();
        if (isAuto) {
            writeCon.setAutoCommit(false);
        }
        PreparedStatement stmt = null;
        try {
            // Acquire lock
            lock(folderId, ctx.getContextId(), true, writeCon);

            // Copy backup entries into oxfolder_tree and oxfolder_permissions
            stmt = writeCon.prepareStatement(SQL_RESTORE_OT);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            stmt = writeCon.prepareStatement(SQL_RESTORE_OP);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Clean backup tables
             */
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERPERMS));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            stmt = writeCon.prepareStatement(SQL_DELETE_DELETE.replaceFirst("#TABLE#", STR_DELOXFOLDERTREE));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            executeUpdate(stmt);
            stmt.close();
            stmt = null;
            /*
             * Commit
             */
            if (isAuto) {
                writeCon.commit();
            }
        } catch (final SQLException e) {
            if (isAuto) {
                DBUtils.rollback(writeCon);
            }
            throw e;
        } finally {
            DBUtils.closeSQLStuff(stmt);
            if (isAuto) {
                DBUtils.autocommit(writeCon);
            }
            if (closeWriteCon) {
                DBPool.closeWriterSilent(ctx, writeCon);
            }
        }
    }

    /**
     * This method is used to generate identifier when creating a context.
     * @param ctx context to create.
     * @param con writable connection to the context database in transaction mode - autocommit is false.
     * @return a unique identifier for a folder.
     * @throws SQLException if generating this unique folder identifier fails.
     */
    public static int getNextSerialForAdmin(final Context ctx, final Connection con) throws SQLException {
        return IDGenerator.getId(ctx, Types.FOLDER, con);
    }

    static void hardDeleteOXFolder(final int folderId, final Context ctx, final Connection writeConArg) throws SQLException, OXException {
        Connection writeCon = writeConArg;
        boolean closeWrite = false;
        if (writeCon == null) {
            try {
                writeCon = DBPool.pickupWriteable(ctx);
            } catch (final OXException e) {
                throw e;
            }
            closeWrite = true;
        }
        final boolean isAuto = writeCon.getAutoCommit();
        if (isAuto) {
            writeCon.setAutoCommit(false);
        }
        Statement stmt = null;
        try {
            final String andClause = " AND fuid = ";
            stmt = writeCon.createStatement();
            stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_specialfolders WHERE cid = ").append(ctx.getContextId()).append(andClause).append(
                folderId).toString());

            stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_permissions WHERE cid = ").append(ctx.getContextId()).append(andClause).append(
                folderId).toString());

            stmt.addBatch(new StringBuilder("DELETE FROM oxfolder_tree WHERE cid = ").append(ctx.getContextId()).append(andClause).append(
                folderId).toString());

            stmt.executeBatch();

            if (isAuto) {
                writeCon.commit();
            }
        } catch (final SQLException e) {
            if (isAuto) {
                writeCon.rollback();
            }
            throw e;
        } finally {
            if (isAuto) {
                autocommit(writeCon);
            }
            closeResources(null, stmt, closeWrite ? writeCon : null, false, ctx);
        }
    }

    /*-
     * -------------- Helper methods for OXFolderDeleteListener (User removal) --------------
     */

    private static final String TMPL_FOLDER_TABLE = "#FOLDER#";

    private static final String TMPL_PERM_TABLE = "#PERM#";

    private static final String TMPL_IDS = "#IDS#";

    private static final String SQL_DROP_SYS_PERMS = "DELETE FROM " + TMPL_PERM_TABLE + " WHERE cid = ? AND permission_id = ? AND system > 0";

    /**
     * Drops all system-permissions belonging to specified entity in given context
     *
     * @param entity The entity
     * @param permTable The permission table
     * @param writeConArg The writable connection
     * @param ctx The context
     * @throws OXException If a pooling error occurs
     * @throws SQLException If a SQL error occurs
     */
    static void cleanseSystemPermissions(final int entity, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection writeCon = writeConArg;
        boolean createReadCon = false;
        PreparedStatement stmt = null;
        try {
            if (writeCon == null) {
                writeCon = DBPool.pickupWriteable(ctx);
                createReadCon = true;
            }
            stmt = writeCon.prepareStatement(SQL_DROP_SYS_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, createReadCon ? writeCon : null, false, ctx);
        }
    }

    private static final String SQL_GET_CONTEXT_MAILADMIN = "SELECT user FROM user_setting_admin WHERE cid = ?";

    static int getContextMailAdmin(final Connection readConArg, final Context ctx) throws OXException, SQLException {
        Connection readCon = readConArg;
        boolean createReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                createReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_GET_CONTEXT_MAILADMIN);
            stmt.setInt(1, ctx.getContextId());
            rs = executeQuery(stmt);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        } finally {
            closeResources(rs, stmt, createReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_SEL_PERMS = "SELECT ot.fuid, ot.type, ot.module, ot.default_flag FROM " + TMPL_PERM_TABLE + " AS op JOIN " + TMPL_FOLDER_TABLE + " AS ot ON op.fuid = ot.fuid AND op.cid = ? AND ot.cid = ? WHERE op.permission_id IN " + TMPL_IDS + " GROUP BY ot.fuid";

    /**
     * Deletes all permissions assigned to context's mail admin from given permission table.
     */
    static void handleMailAdminPermissions(final int mailAdmin, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        handleEntityPermissions(mailAdmin, null, null, -1L, folderTable, permTable, readConArg, writeConArg, ctx);
    }

    /**
     * Handles entity' permissions located in given permission table. If permission is associated with a private folder, it is going to be
     * deleted. Otherwise the permission is reassigned to mailadmin.
     */
    static void handleEntityPermissions(final int entity, final int mailAdmin, Integer destUser, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        handleEntityPermissions(entity, Integer.valueOf(mailAdmin), destUser, lastModified, folderTable, permTable, readConArg, writeConArg, ctx);
    }

    private static void handleEntityPermissions(final int entity, final Integer mailAdmin, Integer destUserID, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        if (destUserID == null) {
            destUserID = ctx.getMailadmin();
        }
        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final boolean isMailAdmin = (mailAdmin == null);
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            final String permissionsIDs;
            if (isMailAdmin) {
                permissionsIDs = new StringBuilder().append('(').append(entity).append(',').append(OCLPermission.ALL_GROUPS_AND_USERS).append(
                    ')').toString();
            } else {
                permissionsIDs = new StringBuilder().append('(').append(entity).append(')').toString();
            }
            stmt = readCon.prepareStatement(SQL_SEL_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable).replaceFirst(
                TMPL_FOLDER_TABLE,
                folderTable).replaceFirst(TMPL_IDS, permissionsIDs));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, ctx.getContextId());
            rs = executeQuery(stmt);

            TIntSet deletePerms = new TIntHashSet();
            TIntSet reassignPerms = new TIntHashSet();
            while (rs.next()) {
                int fuid = rs.getInt(1);
                int type = rs.getInt(2);
                int module = rs.getInt(3);
                if (destUserID <= 0 || isMailAdmin || markForDeletion(type, module)) {
                    deletePerms.add(fuid);
                } else {
                    reassignPerms.add(fuid);
                }
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            /*
             * Delete
             */
            deletePermissions(deletePerms, entity, permTable, writeConArg, ctx);
            if (!isMailAdmin && destUserID > 0) {
                /*
                 * Reassign
                 */
                reassignPermissions(
                    reassignPerms,
                    entity,
                    destUserID,
                    lastModified,
                    folderTable,
                    permTable,
                    readCon,
                    writeConArg,
                    ctx);
            }
            /*
             * Remove from cache
             */
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            if (FolderCacheManager.isInitialized()) {
                /*
                 * Invalidate cache
                 */
                try {
                    TIntIterator iter = deletePerms.iterator();
                    for (int i = deletePerms.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iter.next(), ctx);
                    }
                    iter = reassignPerms.iterator();
                    for (int i = reassignPerms.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iter.next(), ctx);
                    }
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
            /*
             * Post events
             */
            final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
            if (null != eventAdmin) {
                TIntIterator iter = deletePerms.iterator();
                for (int i = deletePerms.size(); i-- > 0;) {
                    broadcastEvent(iter.next(), false, entity, ctx, eventAdmin, readCon);
                }
                iter = reassignPerms.iterator();
                for (int i = reassignPerms.size(); i-- > 0;) {
                    broadcastEvent(iter.next(), false, entity, ctx, eventAdmin, readCon);
                }
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static void broadcastEvent(final int fuid, final boolean deleted, final int entity, final Context ctx, final EventAdmin eventAdmin, Connection readCon) throws OXException {
        final Dictionary<String, Object> properties = new Hashtable<String, Object>(6);
        properties.put(FolderEventConstants.PROPERTY_CONTEXT, Integer.valueOf(ctx.getContextId()));
        properties.put(FolderEventConstants.PROPERTY_USER, Integer.valueOf(entity));
        properties.put(FolderEventConstants.PROPERTY_FOLDER, Integer.toString(fuid));
        properties.put(FolderEventConstants.PROPERTY_CONTENT_RELATED, Boolean.valueOf(!deleted));
        if (deleted) {
            //get path to root and send it this is only needed if folder is changed
            String[] pathToRootString = getFolderPath(fuid, readCon, ctx);
            properties.put(FolderEventConstants.PROPERTY_FOLDER_PATH, pathToRootString);
        }

        /*
         * Create event with push topic
         */
        final Event event = new Event(FolderEventConstants.TOPIC, properties);
        /*
         * Finally deliver it
         */
        eventAdmin.sendEvent(event);
        LOG.debug("Notified content-related-wise changed folder \"{} in context {}", fuid, ctx);
    }

    private static final String SQL_DELETE_PERMS = "DELETE FROM " + TMPL_PERM_TABLE + " WHERE cid = ? AND fuid = ? AND permission_id = ?";

    private static void deletePermissions(final TIntSet deletePerms, final int entity, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        final int size = deletePerms.size();
        if (size == 0) {
            return;
        }
        final TIntIterator iter = deletePerms.iterator();
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            stmt = wc.prepareStatement(SQL_DELETE_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable));
            for (int i = size; i-- > 0;) {
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, iter.next());
                stmt.setInt(3, entity);
                stmt.addBatch();
            }
            executeBatch(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REASSIGN_PERMS = "UPDATE " + TMPL_PERM_TABLE + " SET permission_id = ?, group_flag = 0 WHERE cid = ? AND fuid = ? AND permission_id = ?";

    private static final String SQL_REASSIGN_UPDATE_TIMESTAMP = "UPDATE " + TMPL_FOLDER_TABLE + " SET changed_from = ?, changing_date = ? WHERE cid = ? AND fuid = ?";

    private static void reassignPermissions(final TIntSet reassignPerms, final int entity, final int destUser, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        final int size = reassignPerms.size();
        if (size == 0) {
            return;
        }
        Connection wc = writeConArg;
        boolean closeWrite = false;
        Connection rc = readConArg;
        boolean closeRead = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            if (rc == null) {
                rc = DBPool.pickup(ctx);
                closeRead = true;
            }
            // stmt =
            // wc.prepareStatement(SQL_REASSIGN_PERMS.replaceFirst(TMPL_PERM_TABLE
            // ,
            // permTable));
            TIntIterator iter = reassignPerms.iterator();
            Next: for (int i = size; i-- > 0;) {
                final int fuid = iter.next();
                /*
                 * Check if user already holds permission on current folder
                 */
                stmt = rc.prepareStatement("SELECT 1 FROM " + permTable + " WHERE cid = ? AND permission_id = ? AND fuid = ?");
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, destUser);
                stmt.setInt(3, fuid);
                rs = executeQuery(stmt);
                final boolean hasPerm = rs.next();
                rs.close();
                rs = null;
                stmt.close();
                stmt = null;
                if (hasPerm) {
                    /*
                     * User destUser already holds permission on this folder
                     */
                    try {
                        /*
                         * Set to merged permission
                         */
                        final OCLPermission mergedPerm = getMergedPermission(entity, destUser, fuid, permTable, readConArg, ctx);
                        deleteSingleEntityPermission(entity, fuid, permTable, wc, ctx);
                        updateSingleEntityPermission(mergedPerm, destUser, fuid, permTable, wc, ctx);
                    } catch (final Exception e) {
                        LOG.error("", e);
                        continue Next;
                    }
                } else {
                    stmt = wc.prepareStatement(SQL_REASSIGN_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable));
                    stmt.setInt(1, destUser);
                    stmt.setInt(2, ctx.getContextId());
                    stmt.setInt(3, fuid);
                    stmt.setInt(4, entity);
                    try {
                        executeUpdate(stmt);
                    } catch (final SQLException e) {
                        LOG.error("", e);
                        continue Next;
                    } finally {
                        stmt.close();
                    }
                }
            }
            stmt = wc.prepareStatement(SQL_REASSIGN_UPDATE_TIMESTAMP.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            iter = reassignPerms.iterator();
            for (int i = size; i-- > 0;) {
                stmt.setInt(1, destUser);
                stmt.setLong(2, lastModified);
                stmt.setInt(3, ctx.getContextId());
                stmt.setInt(4, iter.next());
                stmt.addBatch();
            }
            executeBatch(stmt);
        } finally {
            closeResources(rs, stmt, closeWrite ? wc : null, false, ctx);
            if (closeRead && rc != null) {
                DBPool.closeReaderSilent(ctx, rc);
            }
        }
    }

    private static final String SQL_REASSIGN_DEL_PERM = "DELETE FROM " + TMPL_PERM_TABLE + " WHERE cid = ? AND permission_id = ? AND fuid = ?";

    private static void deleteSingleEntityPermission(final int entity, final int fuid, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean close = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                close = true;
            }
            stmt = wc.prepareStatement(SQL_REASSIGN_DEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            stmt.setInt(3, fuid);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, close ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REASSIGN_UPDATE_PERM = "UPDATE " + TMPL_PERM_TABLE + " SET fp = ?, orp = ?, owp = ?, odp = ?, admin_flag = ?, group_flag = ? WHERE cid = ? AND permission_id = ? AND fuid = ?";

    private static void updateSingleEntityPermission(final OCLPermission mergedPerm, final int destUser, final int fuid, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean close = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                close = true;
            }
            stmt = wc.prepareStatement(SQL_REASSIGN_UPDATE_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
            stmt.setInt(1, mergedPerm.getFolderPermission());
            stmt.setInt(2, mergedPerm.getReadPermission());
            stmt.setInt(3, mergedPerm.getWritePermission());
            stmt.setInt(4, mergedPerm.getDeletePermission());
            stmt.setInt(5, mergedPerm.isFolderAdmin() ? 1 : 0);
            stmt.setInt(6, mergedPerm.isGroupPermission() ? 1 : 0);
            stmt.setInt(7, ctx.getContextId());
            stmt.setInt(8, destUser);
            stmt.setInt(9, fuid);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, close ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REASSIGN_SEL_PERM = "SELECT fp, orp, owp, odp, admin_flag FROM " + TMPL_PERM_TABLE + " WHERE cid = ? AND permission_id = ? AND fuid = ?";

    private static OCLPermission getMergedPermission(final int entity, final int destUser, final int fuid, final String permTable, final Connection readConArg, final Context ctx) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeRead = false;
        PreparedStatement innerStmt = null;
        ResultSet innerRs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeRead = true;
            }
            innerStmt = readCon.prepareStatement(SQL_REASSIGN_SEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
            innerStmt.setInt(1, ctx.getContextId());
            innerStmt.setInt(2, destUser);
            innerStmt.setInt(3, fuid);
            innerRs = executeQuery(innerStmt);
            if (!innerRs.next()) {
                /*
                 * Merged permission is entity's permission since no permission is defined for user destUser
                 */
                innerRs.close();
                innerStmt.close();
                innerStmt = readCon.prepareStatement(SQL_REASSIGN_SEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
                innerStmt.setInt(1, ctx.getContextId());
                innerStmt.setInt(2, entity);
                innerStmt.setInt(3, fuid);
                innerRs = executeQuery(innerStmt);
                if (!innerRs.next()) {
                    /*
                     * Empty permission
                     */
                    return new OCLPermission(destUser, fuid);
                }
                final OCLPermission destUserPerm = new OCLPermission(destUser, fuid);
                destUserPerm.setAllPermission(innerRs.getInt(1), innerRs.getInt(2), innerRs.getInt(3), innerRs.getInt(4));
                destUserPerm.setFolderAdmin(innerRs.getInt(5) > 0);
                destUserPerm.setGroupPermission(false);
                return destUserPerm;
            }
            final OCLPermission destUserPerm = new OCLPermission(destUser, fuid);
            destUserPerm.setAllPermission(innerRs.getInt(1), innerRs.getInt(2), innerRs.getInt(3), innerRs.getInt(4));
            destUserPerm.setFolderAdmin(innerRs.getInt(5) > 0);
            destUserPerm.setGroupPermission(false);
            innerRs.close();
            innerStmt.close();
            innerStmt = readCon.prepareStatement(SQL_REASSIGN_SEL_PERM.replaceFirst(TMPL_PERM_TABLE, permTable));
            innerStmt.setInt(1, ctx.getContextId());
            innerStmt.setInt(2, entity);
            innerStmt.setInt(3, fuid);
            innerRs = executeQuery(innerStmt);
            if (!innerRs.next()) {
                return destUserPerm;
            }
            final OCLPermission entityPerm = new OCLPermission(entity, fuid);
            entityPerm.setAllPermission(innerRs.getInt(1), innerRs.getInt(2), innerRs.getInt(3), innerRs.getInt(4));
            entityPerm.setFolderAdmin(innerRs.getInt(5) > 0);
            /*
             * Merge
             */
            final OCLPermission mergedPerm = new OCLPermission(destUser, fuid);
            mergedPerm.setFolderPermission(Math.max(destUserPerm.getFolderPermission(), entityPerm.getFolderPermission()));
            mergedPerm.setReadObjectPermission(Math.max(destUserPerm.getReadPermission(), entityPerm.getReadPermission()));
            mergedPerm.setWriteObjectPermission(Math.max(destUserPerm.getWritePermission(), entityPerm.getWritePermission()));
            mergedPerm.setDeleteObjectPermission(Math.max(destUserPerm.getDeletePermission(), entityPerm.getDeletePermission()));
            mergedPerm.setFolderAdmin(destUserPerm.isFolderAdmin() || entityPerm.isFolderAdmin());
            mergedPerm.setGroupPermission(false);
            return mergedPerm;
        } finally {
            closeResources(innerRs, innerStmt, closeRead ? readCon : null, true, ctx);
        }
    }

    // ------------------- DELETE FOLDERS --------------------------

    static void handleMailAdminFolders(final int mailAdmin, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        handleEntityFolders(mailAdmin, null, null, -1L, folderTable, permTable, readConArg, writeConArg, ctx);
    }

    static void handleEntityFolders(final int entity, final int mailAdmin, Integer destUser, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        handleEntityFolders(entity, Integer.valueOf(mailAdmin), destUser, lastModified, folderTable, permTable, readConArg, writeConArg, ctx);
    }

    private static final String SQL_SEL_FOLDERS = "SELECT ot.fuid, ot.type, ot.module, ot.default_flag FROM #FOLDER# AS ot WHERE ot.cid = ? AND ot.created_from = ?";

    private static final String SQL_SEL_FOLDERS2 = "SELECT ot.fuid FROM #FOLDER# AS ot WHERE ot.cid = ? AND ot.changed_from = ?";

    private static void handleEntityFolders(final int entity, final Integer mailAdmin, Integer destUser, final long lastModified, final String folderTable, final String permTable, final Connection readConArg, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        if (destUser == null) {
            destUser = ctx.getMailadmin();
        }

        Connection readCon = readConArg;
        boolean closeReadCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        final boolean isMailAdmin = (mailAdmin == null);
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeReadCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SEL_FOLDERS.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            rs = executeQuery(stmt);

            TIntSet deleteFolders = new TIntHashSet();
            TIntSet reassignFolders = new TIntHashSet();
            while (rs.next()) {
                int fuid = rs.getInt(1);
                int type = rs.getInt(2);
                int module = rs.getInt(3);
                if (destUser <= 0 || isMailAdmin || markForDeletion(type, module)) {
                    deleteFolders.add(fuid);
                } else {
                    reassignFolders.add(fuid);
                }
            }
            rs.close();
            rs = null;
            stmt.close();
            stmt = null;
            /*
             * Delete
             */
            deleteFolders(deleteFolders, folderTable, permTable, writeConArg, ctx);
            if (!isMailAdmin && destUser > 0) {
                /*
                 * Reassign
                 */
                reassignFolders(reassignFolders, entity, destUser.intValue(), lastModified, folderTable, writeConArg, ctx);
            }
            /*
             * Remove from cache
             */
            ConditionTreeMapManagement.dropFor(ctx.getContextId());
            if (FolderCacheManager.isInitialized()) {
                /*
                 * Invalidate cache
                 */
                try {
                    TIntIterator iterator = deleteFolders.iterator();
                    for (int i = deleteFolders.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iterator.next(), ctx);
                    }
                    iterator = reassignFolders.iterator();
                    for (int i = reassignFolders.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iterator.next(), ctx);
                    }
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
            /*
             * Check column "changed_from"
             */
            stmt = readCon.prepareStatement(SQL_SEL_FOLDERS2.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, entity);
            rs = executeQuery(stmt);
            deleteFolders = new TIntHashSet();
            reassignFolders = new TIntHashSet();
            while (rs.next()) {
                final int fuid = rs.getInt(1);
                if (isMailAdmin) {
                    deleteFolders.add(fuid);
                } else {
                    reassignFolders.add(fuid);
                }
            }
            /*
             * Delete
             */
            deleteFolders(deleteFolders, folderTable, permTable, writeConArg, ctx);
            if (!isMailAdmin) {
                /*
                 * Reassign
                 */
                int tmpDestination = destUser > 0 ? destUser : mailAdmin.intValue();
                reassignFolders(reassignFolders, entity, tmpDestination, lastModified, folderTable, writeConArg, ctx);
            }
            /*
             * Remove from cache
             */
            if (FolderCacheManager.isInitialized()) {
                /*
                 * Invalidate cache
                 */
                try {
                    TIntIterator iterator = deleteFolders.iterator();
                    for (int i = deleteFolders.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iterator.next(), ctx);
                    }
                    iterator = reassignFolders.iterator();
                    for (int i = reassignFolders.size(); i-- > 0;) {
                        FolderCacheManager.getInstance().removeFolderObject(iterator.next(), ctx);
                    }
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
            /*
             * Post events
             */
            final EventAdmin eventAdmin = ServerServiceRegistry.getInstance().getService(EventAdmin.class);
            if (null != eventAdmin) {
                TIntIterator iterator = deleteFolders.iterator();
                for (int i = deleteFolders.size(); i-- > 0;) {
                    broadcastEvent(iterator.next(), true, entity, ctx, eventAdmin, readCon);
                }
                iterator = reassignFolders.iterator();
                for (int i = reassignFolders.size(); i-- > 0;) {
                    broadcastEvent(iterator.next(), false, entity, ctx, eventAdmin, readCon);
                }
            }
        } finally {
            closeResources(rs, stmt, closeReadCon ? readCon : null, true, ctx);
        }
    }

    private static final String SQL_DELETE_FOLDER = "DELETE FROM #FOLDER# WHERE cid = ? AND fuid = ?";

    private static void deleteFolders(final TIntSet deleteFolders, final String folderTable, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        final int size = deleteFolders.size();
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            /*
             * Delete folder's permissions if any exist
             */
            TIntIterator iter = deleteFolders.iterator();
            for (int i = size; i-- > 0;) {
                final int fuid = iter.next();
                checkFolderPermissions(fuid, permTable, writeConArg, ctx);
            }
            /*
             * Delete references to table 'oxfolder_specialfolders'
             */
            iter = deleteFolders.iterator();
            for (int i = size; i-- > 0;) {
                final int fuid = iter.next();
                deleteSpecialfoldersRefs(fuid, writeConArg, ctx);
            }
            /*
             * Delete folders
             */
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            stmt = wc.prepareStatement(SQL_DELETE_FOLDER.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            iter = deleteFolders.iterator();
            for (int i = size; i-- > 0;) {
                final int fuid = iter.next();
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, fuid);
                stmt.addBatch();
            }
            executeBatch(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_DELETE_SPECIAL_REFS = "DELETE FROM oxfolder_specialfolders WHERE cid = ? AND fuid = ?";

    private static void deleteSpecialfoldersRefs(final int fuid, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            stmt = wc.prepareStatement(SQL_DELETE_SPECIAL_REFS);
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, fuid);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_DELETE_FOLDER_PERMS = "DELETE FROM #PERM# WHERE cid = ? AND fuid = ?";

    private static void checkFolderPermissions(final int fuid, final String permTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            stmt = wc.prepareStatement(SQL_DELETE_FOLDER_PERMS.replaceFirst(TMPL_PERM_TABLE, permTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, fuid);
            executeUpdate(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_REASSIGN_FOLDERS = "UPDATE #FOLDER# SET created_from = ?, changed_from = ?, changing_date = ?, default_flag = 0 WHERE cid = ? AND fuid = ?";

    private static final String SQL_REASSIGN_FOLDERS_WITH_NAME = "UPDATE #FOLDER# SET created_from = ?, changed_from = ?, changing_date = ?, default_flag = 0, fname = ? WHERE cid = ? AND fuid = ?";

    private static void reassignFolders(final TIntSet reassignFolders, final int entity, final int destUser, final long lastModified, final String folderTable, final Connection writeConArg, final Context ctx) throws OXException, SQLException {
        Connection wc = writeConArg;
        boolean closeWrite = false;
        PreparedStatement stmt = null;
        try {
            if (wc == null) {
                wc = DBPool.pickupWriteable(ctx);
                closeWrite = true;
            }
            int size = reassignFolders.size();
            {
                // Special handling for default infostore folder
                FuidAndName defaultInfostoreFolder = getDefaultInfostoreFolder(entity, folderTable, wc, ctx);
                if (null != defaultInfostoreFolder) {
                    TIntIterator iter = reassignFolders.iterator();
                    boolean go = true;
                    for (int i = size; go && i-- > 0;) {
                        int fuid = iter.next();
                        if (fuid == defaultInfostoreFolder.fuid) {
                            iter.remove();
                            size--;

                            stmt = wc.prepareStatement(SQL_REASSIGN_FOLDERS_WITH_NAME.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
                            stmt.setInt(1, destUser);
                            stmt.setInt(2, destUser);
                            stmt.setLong(3, lastModified);
                            stmt.setString(4, new StringBuilder(defaultInfostoreFolder.name).append(fuid).toString());
                            stmt.setInt(5, ctx.getContextId());
                            stmt.setInt(6, fuid);
                            executeUpdate(stmt);
                            stmt.close();
                            stmt = null;

                            // Leave loop
                            go = false;
                        }
                    }
                }
            }

            // Iterate others
            TIntIterator iter = reassignFolders.iterator();
            stmt = wc.prepareStatement(SQL_REASSIGN_FOLDERS.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            for (int i = size; i-- > 0;) {
                stmt.setInt(1, destUser);
                stmt.setInt(2, destUser);
                stmt.setLong(3, lastModified);
                stmt.setInt(4, ctx.getContextId());
                stmt.setInt(5, iter.next());
                stmt.addBatch();
            }
            executeBatch(stmt);
        } finally {
            closeResources(null, stmt, closeWrite ? wc : null, false, ctx);
        }
    }

    private static final String SQL_DEFAULT_INFOSTORE = "SELECT fuid, fname FROM #FOLDER# WHERE cid=? and module=? AND default_flag=1 AND created_from=? AND type=?";

    private static FuidAndName getDefaultInfostoreFolder(int entity, String folderTable, Connection con, Context ctx) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = con.prepareStatement(SQL_DEFAULT_INFOSTORE.replaceFirst(TMPL_FOLDER_TABLE, folderTable));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, FolderObject.INFOSTORE);
            stmt.setInt(3, entity);
            stmt.setInt(4, FolderObject.PUBLIC);

            rs = executeQuery(stmt);
            if (false == rs.next()) {
                return null;
            }

            return new FuidAndName(rs.getInt(1), rs.getString(2));
        } finally {
            closeResources(rs, stmt, null, true, ctx);
        }
    }

    /**
     * @return <code>true</code> if folder type is set to private, <code>false</code> otherwise
     */
    private static boolean markForDeletion(final int type, int module) {
        return isPrivate(type) || isTrashInfoStoreFolder(type, module);
    }

    private static boolean isTrashInfoStoreFolder(final int type, int module) {
        return (type == FolderObject.TRASH) && (module == FolderObject.INFOSTORE);
    }

    private static boolean isPrivate(final int type) {
        return type == FolderObject.PRIVATE;
    }

    private static int executeUpdate(final PreparedStatement stmt) throws SQLException {
        try {
            return stmt.executeUpdate();
        } catch (final SQLException e) {
            if ("MySQLSyntaxErrorException".equals(e.getClass().getSimpleName())) {
                final String sql = stmt.toString();
                LOG.error("\nFollowing SQL query contains syntax errors:\n{}", sql.substring(sql.indexOf(": ") + 2));
            }
            throw e;
        }
    }

    private static int[] executeBatch(final PreparedStatement stmt) throws SQLException {
        try {
            return stmt.executeBatch();
        } catch (final SQLException e) {
            if ("MySQLSyntaxErrorException".equals(e.getClass().getSimpleName())) {
                final String sql = stmt.toString();
                LOG.error("\nFollowing SQL query contains syntax errors:\n{}", sql.substring(sql.indexOf(": ") + 2));
            }
            throw e;
        }
    }

    private static ResultSet executeQuery(final PreparedStatement stmt) throws SQLException {
        try {
            return stmt.executeQuery();
        } catch (final SQLException e) {
            if ("MySQLSyntaxErrorException".equals(e.getClass().getSimpleName())) {
                final String sql = stmt.toString();
                LOG.error("\nFollowing SQL query contains syntax errors:\n{}", sql.substring(sql.indexOf(": ") + 2));
            }
            throw e;
        }
    }

    /**
     * Determines the permission flag based on the supplied folder's type and permissions.
     *
     * @param folder The folder to get the permission flag for
     * @return The permission flag, i.e. one of {@link FolderObject#CUSTOM_PERMISSION}, {@link FolderObject#PRIVATE_PERMISSION},
     *         {@link FolderObject#PUBLIC_PERMISSION},
     */
    private static int determinePermissionFlag(final FolderObject folder) {
        int permissionFlag = FolderObject.CUSTOM_PERMISSION;
        if (folder.getType() == FolderObject.PRIVATE) {
            if (folder.getPermissions().size() == 1) {
                permissionFlag = FolderObject.PRIVATE_PERMISSION;
            }
        } else if (folder.getType() == FolderObject.PUBLIC) {
            int permissionsSize = folder.getPermissions().size();
            Iterator<OCLPermission> iter = folder.getPermissions().iterator();
            for (int i = permissionsSize; i-- > 0;) {
                OCLPermission oclPerm = iter.next();
                if (oclPerm.getEntity() == OCLPermission.ALL_GROUPS_AND_USERS && oclPerm.getFolderPermission() > OCLPermission.NO_PERMISSIONS) {
                    permissionFlag = FolderObject.PUBLIC_PERMISSION;
                    break;
                }
            }
        }
        return permissionFlag;
    }

    // --------------------------------------------------------------------------------------------------------------------------- //

    private static class FuidAndName {
        final int fuid;
        final String name;

        FuidAndName(int fuid, String name) {
            super();
            this.fuid = fuid;
            this.name = name;
        }
    }

}
