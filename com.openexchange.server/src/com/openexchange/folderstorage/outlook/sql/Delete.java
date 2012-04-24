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

package com.openexchange.folderstorage.outlook.sql;

import static com.openexchange.folderstorage.outlook.sql.Utility.debugSQL;
import static com.openexchange.folderstorage.outlook.sql.Utility.getDatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Delete} - SQL for deleting a virtual folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Delete {

    /**
     * Initializes a new {@link Delete}.
     */
    private Delete() {
        super();
    }

    private static final String SQL_DELETE_SUBS =
        "DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_GLOBAL_DELETE_SUBS =
        "DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND folderId = ?";

    private static final String SQL_DELETE_INSERT_SUBS =
        "INSERT INTO virtualBackupSubscription SELECT * FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_GLOBAL_DELETE_INSERT_SUBS =
        "INSERT INTO virtualBackupSubscription SELECT * FROM virtualSubscription WHERE cid = ? AND tree = ? AND folderId = ?";

    private static final String SQL_DELETE_PERMS = "DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_GLOBAL_DELETE_PERMS = "DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND folderId = ?";

    private static final String SQL_DELETE_INSERT_PERMS =
        "INSERT INTO virtualBackupPermission SELECT * FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_GLOBAL_DELETE_INSERT_PERMS =
        "INSERT INTO virtualBackupPermission SELECT * FROM virtualPermission WHERE cid = ? AND tree = ? AND folderId = ?";

    private static final String SQL_DELETE = "DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_GLOBAL_DELETE = "DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND folderId = ?";

    private static final String SQL_DELETE_INSERT =
        "INSERT INTO virtualBackupTree SELECT * FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_GLOBAL_DELETE_INSERT =
        "INSERT INTO virtualBackupTree SELECT * FROM virtualTree WHERE cid = ? AND tree = ? AND folderId = ?";

    /**
     * Deletes specified folder.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folderId The folder identifier
     * @param global <code>true</code> for global folder; otherwise <code>false</code>
     * @param backup <code>true</code> to backup folder data prior to deletion; otherwise <code>false</code>
     * @throws OXException If delete fails
     */
    public static boolean deleteFolder(final int cid, final int tree, final int user, final String folderId, final boolean global, final boolean backup) throws OXException {
        final DatabaseService databaseService = getDatabaseService();
        // Get a connection
        final Connection con = databaseService.getWritable(cid);
        try {
            con.setAutoCommit(false); // BEGIN
            final boolean ret = deleteFolder(cid, tree, user, folderId, global, backup, con);
            con.commit(); // COMMIT
            return ret;
        } catch (final SQLException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } catch (final OXException e) {
            DBUtils.rollback(con); // ROLLBACK
            throw e;
        } catch (final Exception e) {
            DBUtils.rollback(con); // ROLLBACK
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

    /**
     * Deletes specified folder with specified connection.
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folderId The folder identifier
     * @param global <code>true</code> for global folder; otherwise <code>false</code>
     * @param backup <code>true</code> to backup folder data prior to deletion; otherwise <code>false</code>
     * @param con The connection to use
     * @return <code>true</code> if a folder denoted by given identifier was deleted; otherwise <code>false</code>
     * @throws OXException If delete fails
     */
    public static boolean deleteFolder(final int cid, final int tree, final int user, final String folderId, final boolean global, final boolean backup, final Connection con) throws OXException {
        if (null == con) {
            return deleteFolder(cid, tree, user, folderId, global, backup);
        }
        PreparedStatement stmt = null;
        if (backup) {
            /*
             * Backup folder data
             */
            try {
                stmt = con.prepareStatement(global ? SQL_GLOBAL_DELETE_INSERT : SQL_DELETE_INSERT);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                /*
                 * Backup failed
                 */
                debugSQL(stmt);
                final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(Delete.class));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Backup failed.", e);
                }
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * Backup permission data
             */
            try {
                stmt = con.prepareStatement(global ? SQL_GLOBAL_DELETE_INSERT_PERMS : SQL_DELETE_INSERT_PERMS);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                debugSQL(stmt);
                final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(Delete.class));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Backup failed.", e);
                }
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * Backup subscribe data
             */
            try {
                stmt = con.prepareStatement(global ? SQL_GLOBAL_DELETE_INSERT_SUBS : SQL_DELETE_INSERT_SUBS);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                debugSQL(stmt);
                final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(Delete.class));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Backup failed.", e);
                }
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        }
        /*
         * Delete subscribe data
         */
        try {
            stmt = con.prepareStatement(global ? SQL_GLOBAL_DELETE_SUBS : SQL_DELETE_SUBS);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            if (!global) {
                stmt.setInt(pos++, user);
            }
            stmt.setString(pos, folderId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            debugSQL(stmt);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        /*
         * Delete permission data
         */
        try {
            stmt = con.prepareStatement(global ? SQL_GLOBAL_DELETE_PERMS : SQL_DELETE_PERMS);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            if (!global) {
                stmt.setInt(pos++, user);
            }
            stmt.setString(pos, folderId);
            stmt.executeUpdate();
        } catch (final SQLException e) {
            debugSQL(stmt);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        /*
         * Delete folder data
         */
        try {
            stmt = con.prepareStatement(global ? SQL_GLOBAL_DELETE : SQL_DELETE);
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            if (!global) {
                stmt.setInt(pos++, user);
            }
            stmt.setString(pos, folderId);
            return stmt.executeUpdate() > 0;
        } catch (final SQLException e) {
            debugSQL(stmt);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
    }

}
