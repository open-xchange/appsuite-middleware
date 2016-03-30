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

import static com.openexchange.tools.sql.DBUtils.closeResources;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Pattern;
import org.json.JSONException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Streams;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link OXFolderLoader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderLoader {

    private static final String TABLE_OT = "oxfolder_tree";

    private static final String TABLE_OP = "oxfolder_permissions";

    private static final Pattern PAT_RPL_TABLE = Pattern.compile("#TABLE#");

    /**
     * Initializes a new {@link OXFolderLoader}.
     */
    private OXFolderLoader() {
        super();
    }

    public static FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx) throws OXException {
        return loadFolderObjectFromDB(folderId, ctx, null, true, false);
    }

    public static FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx, final Connection readCon) throws OXException {
        return loadFolderObjectFromDB(folderId, ctx, readCon, true, false);
    }

    /**
     * Loads specified folder from database.
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param loadPermissions <code>true</code> to load folder's permissions, otherwise <code>false</code>
     * @param loadSubfolderList <code>true</code> to load subfolders, otherwise <code>false</code>
     * @return The loaded folder object from database
     * @throws OXException If folder cannot be loaded
     */
    public static FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx, final Connection readConArg, final boolean loadPermissions, final boolean loadSubfolderList) throws OXException {
        return loadFolderObjectFromDB(folderId, ctx, readConArg, loadPermissions, loadSubfolderList, TABLE_OT, TABLE_OP);
    }

    private static final String SQL_LOAD_F =
        "SELECT parent, fname, module, type, creating_date, created_from, changing_date, changed_from, permission_flag, subfolder_flag, default_flag, meta FROM #TABLE# WHERE cid = ? AND fuid = ?";

    /**
     * Loads specified folder from database.
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param loadPermissions <code>true</code> to load folder's permissions, otherwise <code>false</code>
     * @param loadSubfolderList <code>true</code> to load subfolders, otherwise <code>false</code>
     * @param table The folder's working or backup table name
     * @param permTable The folder permissions' working or backup table name
     * @return The loaded folder object from database
     * @throws OXException If folder cannot be loaded
     */
    public static FolderObject loadFolderObjectFromDB(final int folderId, final Context ctx, final Connection readConArg, final boolean loadPermissions, final boolean loadSubfolderList, final String table, final String permTable) throws OXException {
        try {
            Connection readCon = readConArg;
            boolean closeCon = false;
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                if (readCon == null) {
                    readCon = DBPool.pickup(ctx);
                    closeCon = true;
                }
                stmt = readCon.prepareStatement(PAT_RPL_TABLE.matcher(SQL_LOAD_F).replaceFirst(table));
                stmt.setInt(1, ctx.getContextId());
                stmt.setInt(2, folderId);
                rs = stmt.executeQuery();
                if (!rs.next()) {
                    throw OXFolderExceptionCode.NOT_EXISTS.create(Integer.valueOf(folderId), Integer.valueOf(ctx.getContextId()));
                }
                final FolderObject folderObj = new FolderObject(rs.getString(2), folderId, rs.getInt(3), rs.getInt(4), rs.getInt(6));
                folderObj.setParentFolderID(rs.getInt(1));
                folderObj.setCreatedBy(parseStringValue(rs.getString(6), ctx));
                folderObj.setCreationDate(new Date(rs.getLong(5)));
                folderObj.setSubfolderFlag(rs.getInt(10) > 0 ? true : false);
                folderObj.setLastModified(new Date(rs.getLong(7)));
                folderObj.setModifiedBy(parseStringValue(rs.getString(8), ctx));
                folderObj.setPermissionFlag(rs.getInt(9));
                final int defaultFolder = rs.getInt(11);
                if (rs.wasNull()) {
                    folderObj.setDefaultFolder(false);
                } else {
                    folderObj.setDefaultFolder(defaultFolder > 0);
                }
                {
                    final InputStream jsonBlobStream = rs.getBinaryStream(12);
                    if (!rs.wasNull() && null != jsonBlobStream) {
                        try {
                            folderObj.setMeta(OXFolderUtility.deserializeMeta(jsonBlobStream));
                        } finally {
                            Streams.close(jsonBlobStream);
                        }
                    }
                }
                if (loadSubfolderList) {
                    final ArrayList<Integer> subfolderList = getSubfolderIds(folderId, ctx, readCon, table);
                    folderObj.setSubfolderIds(subfolderList);
                }

                if (loadPermissions) {
                    folderObj.setPermissionsAsArray(getFolderPermissions(folderId, ctx, readCon, permTable));
                }
                return folderObj;
            } finally {
                closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
            }
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(e, Integer.toString(folderId), Integer.toString(ctx.getContextId()));
        } catch (final JSONException e) {
            throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(e, Integer.toString(folderId), Integer.toString(ctx.getContextId()));
        }
        //catch (final OXException e) {
        //    throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(e, String.valueOf(folderId), String.valueOf(ctx.getContextId()));
        //}
    }

    /**
     * Loads folder permissions from database. Creates a new connection if <code>null</code> is given.
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @return The folder's permissions
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static OCLPermission[] getFolderPermissions(final int folderId, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        return getFolderPermissions(folderId, ctx, readConArg, TABLE_OP);
    }

    private static final String SQL_LOAD_P =
        "SELECT permission_id, fp, orp, owp, odp, admin_flag, group_flag, system FROM #TABLE# WHERE cid = ? AND fuid = ?";

    /**
     * Loads folder permissions from database. Creates a new connection if <code>null</code> is given.
     *
     * @param folderId The folder ID
     * @param ctx The context
     * @param readCon A connection with read capability; may be <code>null</code> to fetch from pool
     * @param table Either folder permissions working or backup table name
     * @return The folder's permissions
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static OCLPermission[] getFolderPermissions(final int folderId, final Context ctx, final Connection readConArg, final String table) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(PAT_RPL_TABLE.matcher(SQL_LOAD_P).replaceFirst(table));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = stmt.executeQuery();
            final ArrayList<OCLPermission> permList = new ArrayList<OCLPermission>();
            while (rs.next()) {
                final OCLPermission p = new OCLPermission();
                p.setEntity(rs.getInt(1)); // Entity
                p.setAllPermission(rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5)); // fp, orp, owp, and odp
                p.setFolderAdmin(rs.getInt(6) > 0 ? true : false); // admin_flag
                p.setGroupPermission(rs.getInt(7) > 0 ? true : false); // group_flag
                p.setSystem(rs.getInt(8)); // system
                permList.add(p);
            }
            stmt.close();
            rs = null;
            stmt = null;
            return permList.toArray(new OCLPermission[permList.size()]);
        } finally {
            closeSQLStuff(rs, stmt);
            if (closeCon) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
    }

    /**
     * Gets the subfolder IDs and names of specified folder.
     *
     * @param folderId The ID of the folder whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static List<IdAndName> getSubfolderIdAndNames(final int folderId, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        return getSubfolderIdAndNames(folderId, ctx, readConArg, TABLE_OT);
    }

    private static final String SQL_SEL2 = "SELECT fuid, fname FROM #TABLE# WHERE cid = ? AND parent = ? ORDER BY default_flag DESC, fname";

    /**
     * Gets the subfolder IDs and names of specified folder.
     *
     * @param folderId The ID of the folder whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param table The folder's working or backup table name
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static List<IdAndName> getSubfolderIdAndNames(final int folderId, final Context ctx, final Connection readConArg, final String table) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SEL2.replaceFirst("#TABLE#", table));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = stmt.executeQuery();
            final List<IdAndName> retval = new ArrayList<IdAndName>();
            while (rs.next()) {
                retval.add(new IdAndName(rs.getInt(1), rs.getString(2)));
            }
            return retval;
        } finally {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
        }
    }

    /**
     * Gets the subfolder IDs of specified folder.
     *
     * @param folderId The ID of the folder whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static ArrayList<Integer> getSubfolderIds(final int folderId, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        return getSubfolderIds(folderId, ctx, readConArg, TABLE_OT);
    }

    private static final String SQL_SEL = "SELECT fuid FROM #TABLE# WHERE cid = ? AND parent = ? ORDER BY default_flag DESC, fname";

    /**
     * Gets the subfolder IDs of specified folder.
     *
     * @param folderId The ID of the folder whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param table The folder's working or backup table name
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static ArrayList<Integer> getSubfolderIds(final int folderId, final Context ctx, final Connection readConArg, final String table) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SEL.replaceFirst("#TABLE#", table));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = stmt.executeQuery();
            final ArrayList<Integer> retval = new ArrayList<Integer>();
            while (rs.next()) {
                retval.add(Integer.valueOf(rs.getInt(1)));
            }
            return retval;
        } finally {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
        }
    }

    /**
     * Gets the subfolder IDs of specified folder.
     *
     * @param folderId The ID of the folder whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static TIntList getSubfolderInts(final int folderId, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        return getSubfolderInts(folderId, ctx, readConArg, TABLE_OT);
    }

    /**
     * Gets the subfolder IDs of specified folder.
     *
     * @param folderId The ID of the folder whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param table The folder's working or backup table name
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static TIntList getSubfolderInts(final int folderId, final Context ctx, final Connection readConArg, final String table) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            stmt = readCon.prepareStatement(SQL_SEL.replaceFirst("#TABLE#", table));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, folderId);
            rs = stmt.executeQuery();
            final TIntList retval = new TIntArrayList();
            while (rs.next()) {
                retval.add(rs.getInt(1));
            }
            return retval;
        } finally {
            closeResources(rs, stmt, closeCon ? readCon : null, true, ctx);
        }
    }

    private static final int parseStringValue(final String str, final Context ctx) {
        if (null == str) {
            return -1;
        }
        try {
            return Integer.parseInt(str);
        } catch (final NumberFormatException e) {
            if (str.equalsIgnoreCase("system")) {
                return ctx.getMailadmin();
            }
        }
        return -1;
    }

    public static final class IdAndName {

        private final int fuid;

        private final String fname;

        private final int hash;

        IdAndName(final int fuid, final String fname) {
            super();
            this.fuid = fuid;
            this.fname = fname;
            final int prime = 31;
            int result = 1;
            result = prime * result + ((fname == null) ? 0 : fname.hashCode());
            result = prime * result + fuid;
            this.hash = result;
        }

        /**
         * Gets the folder ID
         *
         * @return The folder ID
         */
        public int getFolderId() {
            return fuid;
        }

        /**
         * Gets the folder name
         *
         * @return The folder name
         */
        public String getName() {
            return fname;
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
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof IdAndName)) {
                return false;
            }
            final IdAndName other = (IdAndName) obj;
            if (fname == null) {
                if (other.fname != null) {
                    return false;
                }
            } else if (!fname.equals(other.fname)) {
                return false;
            }
            if (fuid != other.fuid) {
                return false;
            }
            return true;
        }

    }

}
