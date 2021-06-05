/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.tools.oxfolder;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

/**
 * {@link OXFolderDependentDeleter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OXFolderDependentDeleter {

    private static final int DELETE_CHUNK_SIZE = 50;

    /**
     * Deletes any existing dependent entities (e.g. subscriptions, shares) for the supplied folder ID.
     *
     * @param con A "write" connection to the database
     * @param session The affected session
     * @param folder The deleted folder
     * @param handDown <code>true</code> to also remove the subscriptions of any nested subfolder, <code>false</code>,
     *            otherwise
     * @return The number of removed subscriptions
     * @throws OXException
     */
    public static void folderDeleted(Connection con, Session session, FolderObject folder, boolean handDown) throws OXException {
        foldersDeleted(con, session, Collections.singletonList(folder), handDown);
    }

    /**
     * Deletes any existing dependent entities (e.g. subscriptions, shares) for the supplied folder ID.
     *
     * @param con A "write" connection to the database
     * @param session The affected session
     * @param folders The deleted folders
     * @param handDown <code>true</code> to also remove the subscriptions of any nested subfolder, <code>false</code>,
     *            otherwise
     * @return The number of removed subscriptions
     * @throws OXException
     */
    public static void foldersDeleted(Connection con, Session session, Collection<FolderObject> folders, boolean handDown) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Context context = serverSession.getContext();
        /*
         * gather all folder identifiers by module & collect potentially affected user permission entities
         */
        TIntSet affectedEntities = new TIntHashSet();
        TIntObjectMap<List<Integer>> byModule = new TIntObjectHashMap<>();
        if (handDown) {
            List<Integer> folderIDs = new LinkedList<>();
            for (FolderObject folder : folders) {
                List<Integer> subfolderIDs;
                try {
                    subfolderIDs = OXFolderSQL.getSubfolderIDs(folder.getObjectID(), con, context, true);
                } catch (SQLException e) {
                    throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                }

                List<Integer> list = byModule.get(folder.getModule());
                if (null == list) {
                    list = new LinkedList<>();
                    byModule.put(folder.getModule(), list);
                }
                list.add(Integer.valueOf(folder.getObjectID()));
                list.addAll(subfolderIDs);

                folderIDs.add(Integer.valueOf(folder.getObjectID()));
                folderIDs.addAll(subfolderIDs);
            }
            if (false == folderIDs.isEmpty()) {
                try {
                    affectedEntities.addAll(OXFolderSQL.getPermissionEntities(folderIDs, con, context, false));
                } catch (SQLException e) {
                    throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
                }
            }
        } else {
            for (FolderObject folder : folders) {
                List<Integer> list = byModule.get(folder.getModule());
                if (null == list) {
                    list = new LinkedList<>();
                    byModule.put(folder.getModule(), list);
                }
                list.add(Integer.valueOf(folder.getObjectID()));

                for (OCLPermission permission : folder.getPermissions()) {
                    if (false == permission.isGroupPermission()) {
                        affectedEntities.add(permission.getEntity());
                    }
                }
            }
        }
        /*
         * collect potentially affected object permission entities, too
         */
        TIntObjectIterator<List<Integer>> iterator = byModule.iterator();
        for (int i = byModule.size(); i-- > 0;) {
            iterator.advance();
            int module = iterator.key();
            List<Integer> fuids = iterator.value();
            affectedEntities.addAll(getObjectPermissionEntities(con, context, module, fuids, false));
        }
        /*
         * delete subscriptions, and any adjacent object permissions
         */
        iterator = byModule.iterator();
        for (int i = byModule.size(); i-- > 0;) {
            iterator.advance();
            int module = iterator.key();
            List<Integer> fuids = iterator.value();
            try {
                deleteDependentEntries(con, context.getContextId(), module, fuids);
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        /*
         * schedule cleanup for affected guest users as needed
         */
        int[] guestIDs = filterGuests(con, context, affectedEntities.toArray());
        if (guestIDs.length > 0) {
            ServerServiceRegistry.getInstance().getService(ShareService.class, true).scheduleGuestCleanup(context.getContextId(), guestIDs);
        }
    }

    /**
     * Deletes all subscriptions and object permissions referencing one of the supplied folder identifiers.
     *
     * @param con The (writable) database connection to use
     * @param cid The context identifier
     * @param module The folder's module identifier
     * @param folderIDs The folder identifiers to delete the dependent entries for
     */
    private static void deleteDependentEntries(Connection con, int cid, int module, List<Integer> folderIDs) throws SQLException {
        for (int i = 0; i < folderIDs.size(); i += DELETE_CHUNK_SIZE) {
            /*
             * prepare chunk
             */
            int length = Math.min(folderIDs.size(), i + DELETE_CHUNK_SIZE) - i;
            List<Integer> chunk = folderIDs.subList(i, i + length);
            /*
             * delete chunk
             */
            deleteSubscriptions(con, cid, chunk);
            deleteObjectPermissions(con, cid, module, chunk);
            deleteFolderProperties(con, cid, chunk);
        }
    }

    private static int[] filterGuests(Connection con, Context context, int[] entityIDs) throws OXException {
        if (0 == entityIDs.length) {
            return entityIDs;
        }
        TIntSet guestIDs = new TIntHashSet(entityIDs.length);
        /*
         * build statement
         */
        StringBuilder stringBuilder = new StringBuilder("SELECT id FROM user where cid=? AND id");
        if (1 == entityIDs.length) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < entityIDs.length; i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(')');
        }
        stringBuilder.append(" AND guestCreatedBy>0;");
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * execute query
             */
            stmt = con.prepareStatement(stringBuilder.toString());
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            for (int i = 0; i < entityIDs.length; i++) {
                stmt.setInt(pos++, entityIDs[i]);
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                guestIDs.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
        return guestIDs.toArray();
    }

    private static List<Integer> getObjectPermissionEntities(Connection con, Context context, int module, List<Integer> folderIDs, boolean includeGroups) throws OXException {
        Set<Integer> entityIDs = new LinkedHashSet<>();
        /*
         * prepare statement
         */
        StringBuilder stringBuilder = new StringBuilder("SELECT permission_id FROM object_permission WHERE cid=? AND module=? AND folder_id");
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
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            /*
             * read out permission entities
             */
            stmt = con.prepareStatement(stringBuilder.toString());
            int pos = 1;
            stmt.setInt(pos++, context.getContextId());
            stmt.setInt(pos++, module);
            for (Integer folderId : folderIDs) {
                stmt.setInt(pos++, i(folderId));
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                entityIDs.add(I(rs.getInt(1)));
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rs, stmt);
        }
        return new ArrayList<>(entityIDs);
    }

    private static int deleteSubscriptions(Connection connection, int cid, List<Integer> folderIDs) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder(96);
        stringBuilder.append("SELECT id,configuration_id FROM subscriptions WHERE cid=? AND folder_id");
        appendPlaceholdersForWhere(stringBuilder, folderIDs.size());

        TIntIntMap ids;
        {
            PreparedStatement stmt = null;
            ResultSet rs = null;
            try {
                stmt = connection.prepareStatement(stringBuilder.toString());
                int pos = 1;
                stmt.setInt(pos++, cid);
                for (Integer folderID : folderIDs) {
                    stmt.setInt(pos++, folderID.intValue());
                }
                rs = stmt.executeQuery();
                if (false == rs.next()) {
                    return 0;
                }

                ids = new TIntIntHashMap();
                do {
                    ids.put(rs.getInt(1), rs.getInt(2));
                } while (rs.next());
            } finally {
                Databases.closeSQLStuff(rs, stmt);
            }
        }
        int updated = deleteFrom("genconf_attributes_strings", connection, cid, ids.values());
        return updated + deleteFrom("subscriptions", connection, cid, ids.keys());
    }

    /**
     * Helper for deleting subscriptions. Deletes from the specified table.
     *
     * @param table The table's name
     * @param queryBuilder The query builder
     * @param connection the connection
     * @param cid The context identifier
     * @param ids The ids
     * @return The amount of deleted rows
     * @throws SQLException if an SQL error is occurred
     */
    private static int deleteFrom(String table, Connection connection, int cid, int[] ids) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder(256);
        queryBuilder.append("DELETE FROM ").append(table).append(" WHERE cid=? AND id");
        appendPlaceholdersForWhere(queryBuilder, ids.length);
        try (PreparedStatement stmt = connection.prepareStatement(queryBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            for (int j = ids.length; j-- > 0;) {
                stmt.setInt(parameterIndex++, ids[j]);
            }
            return stmt.executeUpdate();
        }
    }

    private static int deleteObjectPermissions(Connection connection, int cid, int module, List<Integer> folderIDs) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder(128);
        stringBuilder.append("DELETE FROM object_permission WHERE cid=? AND module=? AND folder_id");
        appendPlaceholdersForWhere(stringBuilder, folderIDs.size()).append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, module);
            for (Integer entity : folderIDs) {
                stmt.setInt(parameterIndex++, entity.intValue());
            }
            return stmt.executeUpdate();
        }
    }

    /**
     * Deletes all properties on the specific folder for all users
     *
     * @param connection The {@link Connection} to use
     * @param contextId The ID of the context the folder belongs to
     * @param folderIDs The folder to be deleted
     * @throws SQLException In case folder can't be deleted
     * @return See {@link PreparedStatement#executeUpdate()}
     */
    private static int deleteFolderProperties(Connection connection, int contextId, List<Integer> folderIDs) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM oxfolder_user_property WHERE cid=? AND fuid");
        appendPlaceholdersForWhere(stringBuilder, folderIDs.size()).append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextId);
            for (Integer entity : folderIDs) {
                stmt.setInt(parameterIndex++, entity.intValue());
            }
            return stmt.executeUpdate();
        }
    }

    private static StringBuilder appendPlaceholdersForWhere(StringBuilder stringBuilder, int count) {
        if (1 > count) {
            throw new IllegalArgumentException("count");
        }
        if (1 == count) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < count; i++) {
                stringBuilder.append(",?");
            }
            stringBuilder.append(')');
        }
        return stringBuilder;
    }

}
