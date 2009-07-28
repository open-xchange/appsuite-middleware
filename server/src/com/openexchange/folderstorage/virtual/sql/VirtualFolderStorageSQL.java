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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.virtual.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.virtual.VirtualFolder;
import com.openexchange.folderstorage.virtual.VirtualPermission;
import com.openexchange.folderstorage.virtual.VirtualServiceRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link VirtualFolderStorageSQL} - SQL related utility methods.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class VirtualFolderStorageSQL {

    /**
     * Initializes a new {@link VirtualFolderStorageSQL}.
     */
    public VirtualFolderStorageSQL() {
        super();
    }

    /**
     * Updates specified folder.
     * 
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folder The folder
     * @throws FolderException If update fails
     */
    public static void updateFolder(final int cid, final int tree, final int user, final Folder folder) throws FolderException {
        deleteFolder(cid, tree, user, folder.getID());
        insertFolder(cid, tree, user, folder);
    }

    private static final String SQL_DELETE_SUBS = "DELETE FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_DELETE_PERMS = "DELETE FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_DELETE = "DELETE FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    /**
     * Deletes specified folder.
     * 
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folderId The folder identifier
     * @throws FolderException If delete fails
     */
    public static void deleteFolder(final int cid, final int tree, final int user, final String folderId) throws FolderException {
        final DatabaseService databaseService;
        try {
            databaseService = VirtualServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new FolderException(e);
        }
        // Get a connection
        final Connection con;
        try {
            con = databaseService.getWritable(cid);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        }
        try {
            // Delete subscribe data
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(SQL_DELETE_SUBS);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            // Delete permission data
            try {
                stmt = con.prepareStatement(SQL_DELETE_PERMS);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            // Delete folder data
            try {
                stmt = con.prepareStatement(SQL_DELETE);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        } finally {
            databaseService.backWritable(cid, con);
        }
    }

    private static final String SQL_INSERT = "INSERT INTO virtualTree (cid, tree, user, folderId, parentId, name) VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_PERM = "INSERT INTO virtualPermission (cid, tree, user, folderId, entity, groupFlag, fp, orp, owp, odp, adminFlag, system) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_SUBS = "INSERT INTO virtualSubscription (cid, tree, user, folderId, subscribed) VALUES (?, ?, ?, ?, ?)";

    /**
     * Inserts specified folder.
     * 
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param folder The folder
     * @throws FolderException If insertion fails
     */
    public static void insertFolder(final int cid, final int tree, final int user, final Folder folder) throws FolderException {
        final DatabaseService databaseService;
        try {
            databaseService = VirtualServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new FolderException(e);
        }
        // Get a connection
        final Connection con;
        try {
            con = databaseService.getWritable(cid);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        }
        final String folderId = folder.getID();
        try {
            // Insert folder data
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement(SQL_INSERT);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                stmt.setString(pos, folder.getParentID());
                stmt.setString(pos, folder.getName());
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            // Insert permission data
            try {
                stmt = con.prepareStatement(SQL_INSERT_PERM);
                final Permission[] permissions = folder.getPermissions();
                for (int i = 0; i < permissions.length; i++) {
                    final Permission p = permissions[i];
                    int pos = 1;
                    stmt.setInt(pos++, cid);
                    stmt.setInt(pos++, tree);
                    stmt.setInt(pos++, user);
                    stmt.setString(pos, folderId);
                    stmt.setInt(pos++, p.getEntity());
                    stmt.setInt(pos++, p.isGroup() ? 1 : 0);
                    stmt.setInt(pos++, p.getFolderPermission());
                    stmt.setInt(pos++, p.getReadPermission());
                    stmt.setInt(pos++, p.getWritePermission());
                    stmt.setInt(pos++, p.getDeletePermission());
                    stmt.setInt(pos++, p.isAdmin() ? 1 : 0);
                    stmt.setInt(pos++, p.getSystem());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
            // Insert subscription data
            try {
                stmt = con.prepareStatement(SQL_INSERT_SUBS);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                stmt.setInt(pos, folder.isSubscribed() ? 1 : 0);
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(stmt);
            }
        } finally {
            databaseService.backWritable(cid, con);
        }
    }

    private static final String SQL_SELECT = "SELECT parentId, name FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT_SUBF = "SELECT folderId FROM virtualTree WHERE cid = ? AND tree = ? AND user = ? AND parentId = ?";

    private static final String SQL_SELECT_PERMS = "SELECT entity, groupFlag, fp, orp, owp, odp, adminFlag, system FROM virtualPermission WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    private static final String SQL_SELECT_SUBSCRIPTION = "SELECT subscribed FROM virtualSubscription WHERE cid = ? AND tree = ? AND user = ? AND folderId = ?";

    /**
     * Fills specified folder.
     * 
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param virtualFolder The folder to fill
     * @throws FolderException If filling the folder fails
     */
    public static void fillFolder(final int cid, final int tree, final int user, final VirtualFolder virtualFolder) throws FolderException {
        final DatabaseService databaseService;
        try {
            databaseService = VirtualServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new FolderException(e);
        }
        // Get a connection
        final Connection con;
        try {
            con = databaseService.getReadOnly(cid);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        }
        final String folderId = virtualFolder.getID();
        try {
            // Select folder data
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement(SQL_SELECT);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                pos = 1;
                virtualFolder.setParentID(rs.getString(pos++));
                virtualFolder.setName(rs.getString(pos));
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
            // Select subfolders
            try {
                stmt = con.prepareStatement(SQL_SELECT_SUBF);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                pos = 1;
                final List<String> subfolderIds = new ArrayList<String>();
                while (rs.next()) {
                    subfolderIds.add(rs.getString(pos));
                }
                virtualFolder.setSubfolderIDs(subfolderIds.toArray(new String[subfolderIds.size()]));
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
            // Select permissions
            try {
                stmt = con.prepareStatement(SQL_SELECT_PERMS);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                final List<Permission> permissions = new ArrayList<Permission>();
                while (rs.next()) {
                    final Permission p = new VirtualPermission();
                    pos = 1;
                    p.setEntity(rs.getInt(pos++));
                    p.setGroup(rs.getInt(pos++) > 0);
                    p.setFolderPermission(rs.getInt(pos++));
                    p.setReadPermission(rs.getInt(pos++));
                    p.setWritePermission(rs.getInt(pos++));
                    p.setDeletePermission(rs.getInt(pos++));
                    p.setAdmin(rs.getInt(pos++) > 0);
                    p.setSystem(rs.getInt(pos++));
                    permissions.add(p);
                }
                if (!permissions.isEmpty()) {
                    virtualFolder.setPermissions(permissions.toArray(new Permission[permissions.size()]));
                }
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
            // Select subscription
            try {
                stmt = con.prepareStatement(SQL_SELECT_SUBSCRIPTION);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, folderId);
                rs = stmt.executeQuery();
                pos = 1;
                boolean subscribed = true;
                if (rs.next()) {
                    subscribed = rs.getInt(pos) > 0;
                }
                virtualFolder.setSubscribed(subscribed);
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        } finally {
            databaseService.backReadOnly(cid, con);
        }
    }

    /**
     * Gets the identifier-name-pairs of the subfolders located below specified parent.
     * 
     * @param cid The context identifier
     * @param tree The tree identifier
     * @param user The user identifier
     * @param parentId The parent identifier
     * @return The identifier-name-pairs of the subfolders located below specified parent
     * @throws FolderException If subfolders cannot be detected
     */
    public static String[][] getSubfolderIds(final int cid, final int tree, final int user, final String parentId) throws FolderException {
        final DatabaseService databaseService;
        try {
            databaseService = VirtualServiceRegistry.getServiceRegistry().getService(DatabaseService.class, true);
        } catch (final ServiceException e) {
            throw new FolderException(e);
        }
        // Get a connection
        final Connection con;
        try {
            con = databaseService.getReadOnly(cid);
        } catch (final DBPoolingException e) {
            throw new FolderException(e);
        }
        try {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = con.prepareStatement(SQL_SELECT_SUBF);
                int pos = 1;
                stmt.setInt(pos++, cid);
                stmt.setInt(pos++, tree);
                stmt.setInt(pos++, user);
                stmt.setString(pos, parentId);
                rs = stmt.executeQuery();
                pos = 1;
                final List<String> subfolderIds = new ArrayList<String>();
                while (rs.next()) {
                    subfolderIds.add(rs.getString(pos));
                }
                DBUtils.closeSQLStuff(rs, stmt);
                final String[][] ret = new String[subfolderIds.size()][];
                // Select names
                for (int i = 0; i < ret.length; i++) {
                    final String subfolderId = subfolderIds.get(i);
                    stmt = con.prepareStatement(SQL_SELECT);
                    pos = 1;
                    stmt.setInt(pos++, cid);
                    stmt.setInt(pos++, tree);
                    stmt.setInt(pos++, user);
                    stmt.setString(pos, subfolderId);
                    rs = stmt.executeQuery();
                    pos = 2;
                    if (rs.next()) {
                        ret[i] = new String[] { subfolderId, rs.getString(pos) };
                    } else {
                        ret[i] = new String[] { subfolderId, null };
                    }
                    DBUtils.closeSQLStuff(rs, stmt);
                }
                return ret;
            } catch (final SQLException e) {
                throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
            } finally {
                DBUtils.closeSQLStuff(rs, stmt);
            }
        } finally {
            databaseService.backReadOnly(cid, con);
        }
    }

}
