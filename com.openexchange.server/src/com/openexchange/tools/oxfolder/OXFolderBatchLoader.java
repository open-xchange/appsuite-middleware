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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TObjectProcedure;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
 * {@link OXFolderBatchLoader}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXFolderBatchLoader {

    private static final int IN_LIMIT = com.openexchange.tools.sql.DBUtils.IN_LIMIT;

    private static final class FolderPermissionProcedure implements TObjectProcedure<FolderObject> {

        private final TIntObjectMap<List<OCLPermission>> folderPermissions;

        public FolderPermissionProcedure(final TIntObjectMap<List<OCLPermission>> folderPermissions) {
            super();
            this.folderPermissions = folderPermissions;
        }

        @Override
        public boolean execute(final FolderObject fo) {
            final int id = fo.getObjectID();
            final List<OCLPermission> permissions = folderPermissions.get(id);
            if (null == permissions) {
                return false;
            }
            fo.setPermissionsNoClone(permissions);
            return true;
        }
    }

    private static final class SubfolderProcedure implements TObjectProcedure<FolderObject> {

        private final TIntObjectMap<ArrayList<Integer>> subfolderIds;

        public SubfolderProcedure(final TIntObjectMap<ArrayList<Integer>> subfolderIds) {
            super();
            this.subfolderIds = subfolderIds;
        }

        @Override
        public boolean execute(final FolderObject fo) {
            final ArrayList<Integer> ids = subfolderIds.get(fo.getObjectID());
            if (ids == null) {
                return false;
            }
            fo.setSubfolderIds(ids);
            return true;
        }

    }

    private static final String TABLE_OT = "oxfolder_tree";

    private static final String TABLE_OP = "oxfolder_permissions";

    private static final Pattern PAT_RPL_TABLE = Pattern.compile("#TABLE#");

    /**
     * Initializes a new {@link OXFolderBatchLoader}.
     */
    private OXFolderBatchLoader() {
        super();
    }

    public static List<FolderObject> loadFolderObjectsFromDB(final int[] folderIds, final Context ctx) throws OXException {
        return loadFolderObjectsFromDB(folderIds, ctx, null, true, false);
    }

    public static List<FolderObject> loadFolderObjectsFromDB(final int[] folderIds, final Context ctx, final Connection readCon) throws OXException {
        return loadFolderObjectsFromDB(folderIds, ctx, readCon, true, false);
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
    public static final List<FolderObject> loadFolderObjectsFromDB(final int[] folderIds, final Context ctx, final Connection readConArg, final boolean loadPermissions, final boolean loadSubfolderList) throws OXException {
        return loadFolderObjectsFromDB(folderIds, ctx, readConArg, loadPermissions, loadSubfolderList, TABLE_OT, TABLE_OP);
    }

    /**
     * Loads specified folder from database.
     *
     * @param folderIds The folder IDs
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param loadPermissions <code>true</code> to load folder's permissions, otherwise <code>false</code>
     * @param loadSubfolderList <code>true</code> to load subfolders, otherwise <code>false</code>
     * @param table The folder's working or backup table name
     * @param permTable The folder permissions' working or backup table name
     * @return The loaded folder object from database
     * @throws OXException If folder cannot be loaded
     */
    public static final List<FolderObject> loadFolderObjectsFromDB(final int[] folderIds, final Context ctx, final Connection readConArg, final boolean loadPermissions, final boolean loadSubfolderList, final String table, final String permTable) throws OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        try {
            if (readCon == null) {
                readCon = DBPool.pickup(ctx);
                closeCon = true;
            }
            final FolderObject[] array = new FolderObject[folderIds.length];
            final TIntObjectMap<FolderObject> map = loadFolderObjectsFromDB0(folderIds, ctx, readCon, loadPermissions, loadSubfolderList, table, permTable);
            for (int i = 0; i < folderIds.length; i++) {
                final int fuid = folderIds[i];
                final FolderObject fo = map.get(fuid);
                array[i] = fo;
            }
            return Arrays.asList(array);
        } finally {
            if (closeCon) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
    }

    private static final TIntObjectMap<FolderObject> loadFolderObjectsFromDB0(final int[] folderIds, final Context ctx, final Connection readCon, final boolean loadPermissions, final boolean loadSubfolderList, final String table, final String permTable) throws OXException {
        try {
            final TIntObjectMap<FolderObject> folders = new TIntObjectHashMap<FolderObject>();
            for (int i = 0; i < folderIds.length; i += IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    // Compose statement
                    final int[] currentIds = com.openexchange.tools.arrays.Arrays.extract(folderIds, i, IN_LIMIT);
                    final String sql = getIN(
                        "SELECT parent,fname,module,type,creating_date,created_from,changing_date,changed_from,permission_flag,subfolder_flag,default_flag,fuid,meta FROM #TABLE# WHERE cid=? AND fuid IN (",
                        currentIds.length);
                    stmt = readCon.prepareStatement(PAT_RPL_TABLE.matcher(sql).replaceFirst(table));
                    int pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
                    for (final int folderId : currentIds) {
                        stmt.setInt(pos++, folderId);
                    }
                    rs = stmt.executeQuery();
                    pos = 0;
                    while (rs.next()) {
                        final int fuid = rs.getInt(12);
                        final FolderObject folderObj = new FolderObject(rs.getString(2), fuid, rs.getInt(3), rs.getInt(4), rs.getInt(6));
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
                            final InputStream jsonBlobStream = rs.getBinaryStream(13);
                            if (!rs.wasNull() && null != jsonBlobStream) {
                                try {
                                    folderObj.setMeta(OXFolderUtility.deserializeMeta(jsonBlobStream));
                                } catch (final JSONException e) {
                                    throw OXFolderExceptionCode.FOLDER_COULD_NOT_BE_LOADED.create(e, Integer.toString(fuid), Integer.toString(ctx.getContextId()));
                                } finally {
                                    Streams.close(jsonBlobStream);
                                }
                            }
                        }
                        folders.put(fuid, folderObj);
                    }
                } finally {
                    closeSQLStuff(rs, stmt);
                }
            }
            if (loadSubfolderList) {
                final SubfolderProcedure procedure = new SubfolderProcedure(getSubfolderIds(folderIds, ctx, readCon, table));
                if (!folders.forEachValue(procedure)) {
                    throw OXFolderExceptionCode.RUNTIME_ERROR.create(I(ctx.getContextId()));
                }
            }
            if (loadPermissions) {
                final TIntObjectMap<List<OCLPermission>> permissions = getFolderPermissions(folderIds, ctx, readCon, permTable);
                final FolderPermissionProcedure procedure = new FolderPermissionProcedure(permissions);
                if (!folders.forEachValue(procedure)) {
                    throw OXFolderExceptionCode.RUNTIME_ERROR.create(I(ctx.getContextId()));
                }
            }
            return folders;
        } catch (final SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Loads folder permissions from database. Creates a new connection if <code>null</code> is given.
     *
     * @param folderIds The folder IDs
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @return The folder's permissions
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static final TIntObjectMap<List<OCLPermission>> getFolderPermissions(final int[] folderIds, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        return getFolderPermissions(folderIds, ctx, readConArg, TABLE_OP);
    }

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
    public static final TIntObjectMap<List<OCLPermission>> getFolderPermissions(final int[] folderIds, final Context ctx, final Connection readConArg, final String table) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        if (readCon == null) {
            readCon = DBPool.pickup(ctx);
            closeCon = true;
        }
        try {
            final TIntObjectMap<List<OCLPermission>> ret = new TIntObjectHashMap<List<OCLPermission>>(folderIds.length);
            for (int i = 0; i < folderIds.length; i+= IN_LIMIT) {
                PreparedStatement stmt = null;
                ResultSet rs = null;
                try {
                    final int[] currentIds = com.openexchange.tools.arrays.Arrays.extract(folderIds, i, IN_LIMIT);
                    final String sql = getIN(
                        "SELECT permission_id,fp,orp,owp,odp,admin_flag,group_flag,system,fuid FROM #TABLE# WHERE cid=? AND fuid IN (",
                        currentIds.length);
                    stmt = readCon.prepareStatement(PAT_RPL_TABLE.matcher(sql).replaceFirst(table));
                    int pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
                    for (final int folderId : currentIds) {
                        stmt.setInt(pos++, folderId);
                        ret.put(folderId, new ArrayList<OCLPermission>(4));
                    }
                    rs = stmt.executeQuery();
                    while (rs.next()) {
                        final int fuid = rs.getInt(9);
                        final List<OCLPermission> list = ret.get(fuid);
                        final OCLPermission p = new OCLPermission();
                        p.setEntity(rs.getInt(1)); // Entity
                        p.setAllPermission(rs.getInt(2), rs.getInt(3), rs.getInt(4), rs.getInt(5)); // fp, orp, owp, and odp
                        p.setFolderAdmin(rs.getInt(6) > 0 ? true : false); // admin_flag
                        p.setGroupPermission(rs.getInt(7) > 0 ? true : false); // group_flag
                        p.setSystem(rs.getInt(8)); // system
                        list.add(p);
                    }
                    stmt.close();
                    rs = null;
                    stmt = null;
                } finally {
                    closeSQLStuff(rs, stmt);
                }
            }
            return ret;
        } finally {
            if (closeCon) {
                DBPool.closeReaderSilent(ctx, readCon);
            }
        }
    }

    /**
     * Gets the subfolder IDs of specified folder.
     *
     * @param folderId The IDs of the folders whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static final TIntObjectMap<ArrayList<Integer>> getSubfolderIds(final int[] folderIds, final Context ctx, final Connection readConArg) throws SQLException, OXException {
        return getSubfolderIds(folderIds, ctx, readConArg, TABLE_OT);
    }

    /**
     * Gets the subfolder IDs of specified folders.
     *
     * @param folderIds The IDs of the folders whose subfolders' IDs shall be returned
     * @param ctx The context
     * @param readConArg A connection with read capability; may be <code>null</code> to fetch from pool
     * @param table The folder's working or backup table name
     * @return The subfolder IDs of specified folder
     * @throws SQLException If a SQL error occurs
     * @throws OXException If a pooling error occurs
     */
    public static final TIntObjectMap<ArrayList<Integer>> getSubfolderIds(final int[] folderIds, final Context ctx, final Connection readConArg, final String table) throws SQLException, OXException {
        Connection readCon = readConArg;
        boolean closeCon = false;
        if (readCon == null) {
            readCon = DBPool.pickup(ctx);
            closeCon = true;
        }
        try {
            final TIntObjectMap<ArrayList<Integer>> ret = new TIntObjectHashMap<ArrayList<Integer>>(folderIds.length);
            for (int i = 0; i < folderIds.length; i += IN_LIMIT) {
		        PreparedStatement stmt = null;
		        ResultSet rs = null;
		        try {
		        	final int[] currentIds = com.openexchange.tools.arrays.Arrays.extract(folderIds, i, IN_LIMIT);
					final String sql = getIN(
							"SELECT fuid,parent FROM #TABLE# WHERE cid=? AND parent IN (",
							currentIds.length)
							+ " ORDER BY default_flag DESC, fname";
	                stmt = readCon.prepareStatement(sql.replaceFirst("#TABLE#", table));
		            int pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
		            for (final int folderId : currentIds) {
		                stmt.setInt(pos++, folderId);
		                ret.put(folderId, new ArrayList<Integer>(0));
		            }
		            rs = stmt.executeQuery();
		            while (rs.next()) {
		                ret.get(rs.getInt(2)).add(I(rs.getInt(1)));
		            }
		        } finally {
		        	closeSQLStuff(rs, stmt);
		        }
            }
            return ret;
        } finally {
        	if (closeCon) {
        		DBPool.closeReaderSilent(ctx, readCon);
        	}
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
}
