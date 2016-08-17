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

import static com.openexchange.java.Autoboxing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.modules.Module;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link OXFolderDependentDeleter}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class OXFolderDependentDeleter {

    private static final int DELETE_CHUNK_SIZE = 50;

    /**
     * Deletes any existing dependent entities (e.g. subscriptions, publications, shares) for the supplied folder ID.
     *
     * @param con A "write" connection to the database
     * @param session The affected session
     * @param folder The deleted folder
     * @param handDown <code>true</code> to also remove the subscriptions and publications of any nested subfolder, <code>false</code>,
     *                 otherwise
     * @return The number of removed subscriptions and publications
     * @throws OXException
     */
    public static void folderDeleted(Connection con, Session session, FolderObject folder, boolean handDown) throws OXException {
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        Context context = serverSession.getContext();
        /*
         * gather all folder identifiers
         */
        List<Integer> folderIDs;
        if (handDown) {
            List<Integer> subfolderIDs;
            try {
                subfolderIDs = OXFolderSQL.getSubfolderIDs(folder.getObjectID(), con, context, true);
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
            folderIDs = new ArrayList<Integer>(subfolderIDs.size() + 1);
            folderIDs.add(Integer.valueOf(folder.getObjectID()));
            folderIDs.addAll(subfolderIDs);
        } else {
            folderIDs = Collections.singletonList(Integer.valueOf(folder.getObjectID()));
        }
        /*
         * determine potentially affected guest user entities
         */
        Set<Integer> affectedEntities = new HashSet<Integer>();
        affectedEntities.addAll(getPermissionEntities(folder, false));
        if (null != folderIDs && 0 < folderIDs.size()) {
            try {
                affectedEntities.addAll(OXFolderSQL.getPermissionEntities(folderIDs, con, context, false));
            } catch (SQLException e) {
                throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
            }
        }
        affectedEntities.addAll(getObjectPermissionEntities(con, context, folder, folderIDs, false));
        /*
         * delete publications, subscriptions, and any adjacent object permissions
         */
        try {
            deleteDependentEntries(con, context.getContextId(), folder.getModule(), folderIDs);
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        }
        /*
         * schedule cleanup for affected guest users as needed
         */
        List<Integer> guestIDs = filterGuests(con, context, new ArrayList<Integer>(affectedEntities));
        if (false == guestIDs.isEmpty()) {
            ServerServiceRegistry.getInstance().getService(ShareService.class, true).scheduleGuestCleanup(context.getContextId(), I2i(guestIDs));
        }
    }

    /**
     * Deletes all publications, subscriptions and object permissions referencing one of the supplied folder identifiers.
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
            deletePublications(con, cid, Module.getModuleString(module, -1), chunk);
            deleteSubscriptions(con, cid, chunk);
            deleteObjectPermissions(con, cid, module, chunk);
        }
    }

    private static List<Integer> filterGuests(Connection con, Context context, List<Integer> entityIDs) throws OXException {
        if (0 == entityIDs.size()) {
            return entityIDs;
        }
        List<Integer> guestIDs = new ArrayList<Integer>();
        /*
         * build statement
         */
        StringBuilder stringBuilder = new StringBuilder("SELECT DISTINCT id FROM user where cid=? AND id");
        if (1 == entityIDs.size()) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 1; i < entityIDs.size(); i++) {
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
            stmt.setInt(1, context.getContextId());
            for (int i = 0; i < entityIDs.size(); i++) {
                stmt.setInt(i + 2, i(entityIDs.get(i)));
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                guestIDs.add(I(rs.getInt(1)));
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        return guestIDs;
    }

    private static List<Integer> getObjectPermissionEntities(Connection con, Context context, FolderObject folder, List<Integer> subfolderIDs, boolean includeGroups) throws OXException {
        List<Integer> entityIDs = new ArrayList<Integer>();
        /*
         * prepare statement
         */
        StringBuilder stringBuilder = new StringBuilder("SELECT DISTINCT permission_id FROM object_permission WHERE cid=? AND module=? AND folder_id");
        if (null == subfolderIDs || 0 == subfolderIDs.size()) {
            stringBuilder.append("=?");
        } else {
            stringBuilder.append(" IN (?");
            for (int i = 0; i < subfolderIDs.size(); i++) {
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
            stmt.setInt(1, context.getContextId());
            int folderID = folder.getObjectID();
            stmt.setInt(2, folder.getModule());
            stmt.setInt(3, folderID);
            if (null != subfolderIDs && 0 < subfolderIDs.size()) {
                for (int i = 0; i < subfolderIDs.size(); i++) {
                    stmt.setInt(i + 4, i(subfolderIDs.get(i)));
                }
            }
            rs = stmt.executeQuery();
            while (rs.next()) {
                entityIDs.add(I(rs.getInt(1)));
            }
        } catch (SQLException e) {
            throw OXFolderExceptionCode.SQL_ERROR.create(e, e.getMessage());
        } finally {
            DBUtils.closeSQLStuff(rs, stmt);
        }
        return entityIDs;
    }

    private static int deletePublications(Connection connection, int cid, String module, List<Integer> entities) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM publications WHERE cid=? AND module=? AND entity");
        appendPlaceholdersForWhere(stringBuilder, entities.size()).append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setString(parameterIndex++, module);
            for (Integer entity : entities) {
                stmt.setInt(parameterIndex++, entity.intValue());
            }
            return stmt.executeUpdate();
        }
    }

    private static int deleteSubscriptions(Connection connection, int cid, List<Integer> folderIDs) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("DELETE FROM subscriptions WHERE cid=? AND folder_id");
        appendPlaceholdersForWhere(stringBuilder, folderIDs.size()).append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            for (Integer entity : folderIDs) {
                stmt.setInt(parameterIndex++, entity.intValue());
            }
            return stmt.executeUpdate();
        }
    }

    private static int deleteObjectPermissions(Connection connection, int cid, int module, List<Integer> folderIDs) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder();
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
     * Gets the identifiers of all permission entities found in a specific folder.
     *
     * @param folder The folder to get the permission entities for
     * @param includeGroups <code>true</code> to also include group permissions, <code>false</code>, otherwise
     * @return The entity IDs, or an empty list if none were found
     */
    private static Set<Integer> getPermissionEntities(FolderObject folder, boolean includeGroups) {
        List<OCLPermission> permissions = folder.getPermissions();
        if (null == permissions) {
            return Collections.emptySet();
        }
        Set<Integer> entityIDs = new HashSet<Integer>(permissions.size());
        for (OCLPermission permission : permissions) {
            if (includeGroups || false == permission.isGroupPermission()) {
                entityIDs.add(I(permission.getEntity()));
            }
        }
        return entityIDs;
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
