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

package com.openexchange.folderstorage.outlook.sql;

import static com.openexchange.folderstorage.outlook.sql.Utility.debugSQL;
import static com.openexchange.folderstorage.outlook.sql.Utility.getDatabaseService;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import org.slf4j.Logger;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.java.util.Tools;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link Delete} - SQL for deleting a virtual folder.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Delete {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(Delete.class);

    /**
     * Initializes a new {@link Delete}.
     */
    private Delete() {
        super();
    }

    /**
     * Hard-deletes given folder
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The optional user identifier
     * @param folderId The folder identifier
     * @param global Whether folder is global or not
     * @param recursive Whether to delete recursively
     * @throws OXException If deletion fails
     */
    public static void hardDeleteFolder(int cid, int tree, int user, String folderId, boolean global, boolean recursive) throws OXException {
        DatabaseService databaseService = getDatabaseService();
        Connection con = databaseService.getWritable(cid);
        boolean rollback = false;
        try {
            con.setAutoCommit(false);
            rollback = true;
            hardDeleteFolder(cid, tree, user, folderId, global, recursive, con);
            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            if (rollback) {
                DBUtils.rollback(con); // ROLLBACK
            }
            DBUtils.autocommit(con);
            databaseService.backWritable(cid, con);
        }
    }

    /**
     * Hard-deletes given folder
     *
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The optional user identifier
     * @param folderId The folder identifier
     * @param global Whether folder is global or not
     * @param recursive Whether to delete recursively
     * @param con The connection to use
     * @throws OXException If deletion fails
     */
    public static void hardDeleteFolder(int cid, int tree, int user, String folderId, boolean global, boolean recursive, Connection con) throws OXException {
        if (null == con) {
            hardDeleteFolder(cid, tree, user, folderId, global, recursive);
            return;
        }
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (recursive) {
                stmt = con.prepareStatement(global ? "SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND parentId = ?" : "SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    List<String> children = new LinkedList<String>();
                    do {
                        children.add(rs.getString(1));
                    } while (rs.next());
                    Databases.closeSQLStuff(rs, stmt);

                    for (String childId : children) {
                        boolean nextGlobal = global && (Tools.getUnsignedInteger(childId) > 0);
                        hardDeleteFolder(cid, tree, user, childId, nextGlobal, true, con);
                    }
                } else {
                    Databases.closeSQLStuff(rs, stmt);
                }
            }

            // Delete subscribe data
            {
                stmt = con.prepareStatement(global ? "DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND folderId = ?" : "DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
            }

            // Delete permission data
            {
                stmt = con.prepareStatement(global ? "DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND folderId = ?" : "DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
            }

            // Delete folder data
            {
                stmt = con.prepareStatement(global ? "DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND folderId = ?" : "DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
                Databases.closeSQLStuff(stmt);
            }
        } catch (SQLException e) {
            debugSQL(stmt);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
    }

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

        {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement(global ? "SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND parentId = ?" : "SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                if (rs.next()) {
                    List<String> children = new LinkedList<String>();
                    do {
                        children.add(rs.getString(1));
                    } while (rs.next());
                    Databases.closeSQLStuff(rs, stmt);
                    rs = null;
                    stmt = null;

                    for (String childId : children) {
                        boolean nextGlobal = Tools.getUnsignedInteger(childId) > 0;
                        deleteFolder(cid, tree, user, childId, nextGlobal, backup, con);
                    }
                }
            } catch (SQLException e) {
                debugSQL(stmt);
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        }

        PreparedStatement stmt = null;
        if (backup) {
            /*
             * Backup folder data
             */
            try {
                stmt = con.prepareStatement(global ? "INSERT INTO virtualBackupTree (cid, tree, user, folderId, parentId, lastModified, modifiedBy, shadow, sortNum) SELECT virtualTree.cid, virtualTree.tree, virtualTree.user, virtualTree.folderId, virtualTree.parentId, virtualTree.lastModified, virtualTree.modifiedBy, virtualTree.shadow, virtualTree.sortNum FROM virtualTree WHERE virtualTree.cid = ? AND virtualTree.tree = ? AND virtualTree.folderId = ?" : "INSERT INTO virtualBackupTree (cid, tree, user, folderId, parentId, lastModified, modifiedBy, shadow, sortNum) SELECT virtualTree.cid, virtualTree.tree, virtualTree.user, virtualTree.folderId, virtualTree.parentId, virtualTree.lastModified, virtualTree.modifiedBy, virtualTree.shadow, virtualTree.sortNum FROM virtualTree WHERE virtualTree.cid = ? AND virtualTree.tree = ? AND virtualTree.user = ? AND virtualTree.folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                /*
                 * Backup failed
                 */
                debugSQL(stmt);
                final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Delete.class);
                log.debug("Backup failed.", e);
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * Backup permission data
             */
            try {
                stmt = con.prepareStatement(global ? "INSERT INTO virtualBackupPermission SELECT * FROM virtualPermission WHERE cid = ? AND tree = ? AND folderId = ?" : "INSERT INTO virtualBackupPermission SELECT * FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                debugSQL(stmt);
                final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Delete.class);
                log.debug("Backup failed.", e);
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            /*
             * Backup subscribe data
             */
            try {
                stmt = con.prepareStatement(global ? "INSERT INTO virtualBackupSubscription SELECT * FROM virtualSubscription WHERE cid = ? AND tree = ? AND folderId = ?" : "INSERT INTO virtualBackupSubscription SELECT * FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                if (!global) {
                    stmt.setInt(pos++, user);
                }
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                debugSQL(stmt);
                final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Delete.class);
                log.debug("Backup failed.", e);
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        }
        /*
         * Delete subscribe data
         */
        try {
            stmt = con.prepareStatement(global ? "DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND folderId = ?" : "DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            if (!global) {
                stmt.setInt(pos++, user);
            }
            stmt.setString(pos, folderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            debugSQL(stmt);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        /*
         * Delete permission data
         */
        try {
            stmt = con.prepareStatement(global ? "DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND folderId = ?" : "DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            if (!global) {
                stmt.setInt(pos++, user);
            }
            stmt.setString(pos, folderId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            debugSQL(stmt);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }
        /*
         * Delete folder data
         */
        final boolean success;
        try {
            stmt = con.prepareStatement(global ? "DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND folderId = ?" : "DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?");
            int pos = 1;
            stmt.setInt(pos++, cid);
            stmt.setInt(pos++, tree);
            if (!global) {
                stmt.setInt(pos++, user);
            }
            stmt.setString(pos, folderId);
            success = stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            debugSQL(stmt);
            throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(stmt);
        }

        if (success) {
            LOGGER.debug("{} {}folder {} from virtual tree {} (user={}, context={})", (backup ? "Backup'ed" : "Deleted"), (global ? "global " : ""), folderId, tree, user, cid, new Throwable("Debug throwable"));
        }

        return success;
    }

}
