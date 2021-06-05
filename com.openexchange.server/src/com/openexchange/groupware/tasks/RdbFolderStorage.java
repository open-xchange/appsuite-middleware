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

package com.openexchange.groupware.tasks;

import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.tools.sql.DBUtils.getIN;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.tools.Collections;

/**
 * Implements the interface for storing task folders using a relational
 * database.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbFolderStorage extends FolderStorage {

    /**
     * Default constructor.
     */
    public RdbFolderStorage() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void insertFolder(final Context ctx, final Connection con,
        final int taskId, final Set<Folder> folders, final StorageType type)
        throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(SQL.INSERT_FOLDER.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            for (final Folder folder : folders) {
                pos = 3;
                stmt.setInt(pos++, folder.getIdentifier());
                stmt.setInt(pos++, folder.getUser());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Folder> selectFolder(final Context ctx, final Connection con,
        final int taskId, final StorageType type) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final Set<Folder> retval = new HashSet<Folder>();
        try {
            stmt = con.prepareStatement(SQL.SELECT_FOLDER.get(type));
            stmt.setInt(1, ctx.getContextId());
            stmt.setInt(2, taskId);
            result = stmt.executeQuery();
            while (result.next()) {
                retval.add(new Folder(result.getInt(1), result.getInt(2)));
            }
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Folder selectFolderByUser(final Context ctx, final Connection con,
        final int taskId, final int userId, final StorageType type)
        throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        Folder retval = null;
        try {
            stmt = con.prepareStatement(SQL.FOLDER_BY_USER.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setInt(pos++, userId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = new Folder(result.getInt(1), userId);
            }
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    Folder selectFolderById(final Context ctx, final Connection con,
        final int taskId, final int folderId, final StorageType type)
        throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        Folder retval = null;
        try {
            stmt = con.prepareStatement(SQL.FOLDER_BY_ID.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, taskId);
            stmt.setInt(pos++, folderId);
            result = stmt.executeQuery();
            if (result.next()) {
                retval = new Folder(folderId, result.getInt(1));
            }
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    void deleteFolder(final Context ctx, final Connection con, final int taskId,
        final int[] folderIds, final StorageType type, final boolean sanityCheck)
        throws OXException {
        PreparedStatement stmt = null;
        int deleted = 0;
        try {
            stmt = con.prepareStatement(getIN(SQL.DELETE_FOLDER.get(type),
                folderIds.length));
            int counter = 1;
            stmt.setInt(counter++, ctx.getContextId());
            stmt.setInt(counter++, taskId);
            for (final int folderId : folderIds) {
                stmt.setInt(counter++, folderId);
            }
            deleted = stmt.executeUpdate();
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(null, stmt);
        }
        if (sanityCheck && folderIds.length != deleted) {
            throw TaskExceptionCode.FOLDER_DELETE_WRONG.create(Integer
                .valueOf(folderIds.length), Integer.valueOf(deleted));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int[] getTasksInFolder(final Context ctx, final Connection con,
        final int folderId, final StorageType type) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> tasks = new ArrayList<Integer>();
        try {
             stmt = con.prepareStatement(SQL.TASK_IN_FOLDER.get(type));
             int counter = 1;
             stmt.setInt(counter++, ctx.getContextId());
             stmt.setInt(counter++, folderId);
             result = stmt.executeQuery();
             while (result.next()) {
                 tasks.add(Integer.valueOf(result.getInt(1)));
             }
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        return Collections.toArray(tasks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[][] searchFolderByUser(final Context ctx, final Connection con,
        final int userId, final StorageType type) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<int[]> tmp = new ArrayList<int[]>();
        try {
            stmt = con.prepareStatement(SQL.SEARCH_FOLDER_BY_USER.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            result = stmt.executeQuery();
            while (result.next()) {
                final int[] folderAndTask = new int[2];
                folderAndTask[0] = result.getInt(1);
                folderAndTask[1] = result.getInt(2);
                tmp.add(folderAndTask);
            }
        } catch (SQLException e) {
            throw TaskExceptionCode.SQL_ERROR.create(e);
        } finally {
            closeSQLStuff(result, stmt);
        }
        final int[][] retval = new int[tmp.size()][];
        for (int i = 0; i < retval.length; i++) {
            retval[i] = tmp.get(i);
        }
        return retval;
    }
}
