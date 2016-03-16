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

package com.openexchange.groupware.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
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
        } catch (final SQLException e) {
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
        } catch (final SQLException e) {
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
        } catch (final SQLException e) {
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
        } catch (final SQLException e) {
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
        } catch (final SQLException e) {
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
        } catch (final SQLException e) {
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
        } catch (final SQLException e) {
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
