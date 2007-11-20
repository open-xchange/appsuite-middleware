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

package com.openexchange.groupware.tasks;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.server.impl.DBPool;
import com.openexchange.server.impl.DBPoolingException;
import com.openexchange.tools.Collections;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.sql.DBUtils;

/**
 * Implementation of search for tasks interface using a relational database
 * currently MySQL.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class RdbTaskSearch extends TaskSearch {

    /**
     * Default constructor.
     */
    RdbTaskSearch() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    int[] findDelegatedTasks(final Context ctx, final Connection con,
        final int userId, final StorageType type) throws TaskException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> tasks = new ArrayList<Integer>();
        try {
            stmt = con.prepareStatement(SQL.SEARCH_DELEGATED.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, userId);
            result = stmt.executeQuery();
            while (result.next()) {
                tasks.add(result.getInt(1));
            }
        } catch (SQLException e) {
            throw new TaskException(TaskException.Code.SQL_ERROR, e,
                e.getMessage());
        } finally {
            closeSQLStuff(result, stmt);
        }
        return Collections.toArray(tasks);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SearchIterator listModifiedTasks(final Context ctx,
        final int folderId, final StorageType type, final int[] columns,
        final Date since, final boolean onlyOwn, final int userId,
        final boolean noPrivate) throws TaskException {
        final StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");
        sql.append(SQL.getFields(columns, false));
        sql.append(" FROM ");
        final String taskTable = SQL.TASK_TABLES.get(type);
        sql.append(taskTable);
        sql.append(" JOIN ");
        final String folderTable = SQL.FOLDER_TABLES.get(type);
        sql.append(folderTable);
        sql.append(" USING (cid,id) WHERE ");
        sql.append(taskTable);
        sql.append(".cid=? AND ");
        sql.append(folderTable);
        sql.append(".folder=? AND ");
        sql.append(taskTable);
        sql.append(".last_modified>=?");
        if (onlyOwn) {
            sql.append(" AND ");
            sql.append(SQL.getOnlyOwn(taskTable));
        }
        if (noPrivate) {
            sql.append(" AND ");
            sql.append(SQL.getNoPrivate(taskTable));
        }
        if (StorageType.DELETED == type) {
            sql.append(" UNION ALL SELECT ");
            sql.append(SQL.getFields(columns, false));
            sql.append(" FROM ");
            final String activeTaskTable = SQL.TASK_TABLES.get(StorageType
                .ACTIVE);
            sql.append(activeTaskTable);
            sql.append(" JOIN ");
            final String removedPartsTable = SQL.PARTS_TABLES.get(StorageType
                .REMOVED);
            sql.append(removedPartsTable);
            sql.append(" ON ");
            sql.append(activeTaskTable);
            sql.append(".cid=");
            sql.append(removedPartsTable);
            sql.append(".cid AND ");
            sql.append(activeTaskTable);
            sql.append(".id=");
            sql.append(removedPartsTable);
            sql.append(".task ");
            sql.append("WHERE ");
            sql.append(activeTaskTable);
            sql.append(".cid=? AND ");
            sql.append(removedPartsTable);
            sql.append(".folder=? AND ");
            sql.append(activeTaskTable);
            sql.append(".last_modified>=?");
            if (onlyOwn) {
                sql.append(" AND ");
                sql.append(SQL.getOnlyOwn(activeTaskTable));
            }
            if (noPrivate) {
                sql.append(" AND ");
                sql.append(SQL.getNoPrivate(activeTaskTable));
            }
        }
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            con = DBPool.pickup(ctx);
            stmt = con.prepareStatement(sql.toString());
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, folderId);
            stmt.setLong(pos++, since.getTime());
            if (onlyOwn) {
                stmt.setInt(pos++, userId);
            }
            if (StorageType.DELETED == type) {
                stmt.setInt(pos++, ctx.getContextId());
                stmt.setInt(pos++, folderId);
                stmt.setLong(pos++, since.getTime());
                if (onlyOwn) {
                    stmt.setInt(pos++, userId);
                }
            }
            return new TaskIterator(ctx, userId, stmt.executeQuery(),
                folderId, columns, type);
        } catch (SQLException e) {
            DBUtils.closeSQLStuff(null, stmt);
            DBPool.closeWriterSilent(ctx, con);
            throw new TaskException(Code.SQL_ERROR, e, e.getMessage());
        } catch (DBPoolingException e) {
            throw new TaskException(Code.NO_CONNECTION, e);
        }
    }
}
