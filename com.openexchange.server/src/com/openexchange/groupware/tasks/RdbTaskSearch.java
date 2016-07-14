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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.search.Order;
import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.TaskIterator2.StatementSetter;
import com.openexchange.tools.Collections;
import com.openexchange.tools.StringCollection;
import com.openexchange.tools.iterator.CombinedSearchIterator;
import com.openexchange.tools.iterator.SearchIterator;

/**
 * Implementation of search for tasks interface using a relational database
 * currently MySQL.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a> (find method)
 */
public class RdbTaskSearch extends TaskSearch {

    /**
     * Default constructor.
     */
    RdbTaskSearch() {
        super();
    }

    @Override
    int[] findUserTasks(final Context ctx, final Connection con, final int userId, final StorageType type) throws OXException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        final List<Integer> tasks = new ArrayList<Integer>();
        try {
            stmt = con.prepareStatement(SQL.SEARCH_USER_TASKS.get(type));
            int pos = 1;
            stmt.setInt(pos++, ctx.getContextId());
            stmt.setInt(pos++, userId);
            stmt.setInt(pos++, userId);
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
    SearchIterator<Task> listModifiedTasks(final Context ctx,
        final int folderId, final StorageType type, final int[] columns,
        final Date since, final boolean onlyOwn, final int userId,
        final boolean noPrivate) throws OXException {
        final StringBuilder sql1 = new StringBuilder();
        sql1.append("SELECT ");
        sql1.append(SQL.getFields(columns, false));
        sql1.append(" FROM ");
        final String taskTable = SQL.TASK_TABLES.get(type);
        sql1.append(taskTable);
        sql1.append(" JOIN ");
        final String folderTable = SQL.FOLDER_TABLES.get(type);
        sql1.append(folderTable);
        sql1.append(" USING (cid,id) WHERE ");
        sql1.append(taskTable);
        sql1.append(".cid=? AND ");
        sql1.append(folderTable);
        sql1.append(".folder=? AND ");
        sql1.append(taskTable);
        sql1.append(".last_modified>?");
        if (onlyOwn) {
            sql1.append(" AND ");
            sql1.append(SQL.getOnlyOwn(taskTable));
        }
        if (noPrivate) {
            sql1.append(" AND ");
            sql1.append(SQL.getNoPrivate(taskTable));
        }
        final TaskIterator iter1 = new TaskIterator2(ctx, userId, sql1.toString(),
            new StatementSetter() {
                @Override
                public void perform(final PreparedStatement stmt)
                    throws SQLException {
                    int pos = 1;
                    stmt.setInt(pos++, ctx.getContextId());
                    stmt.setInt(pos++, folderId);
                    stmt.setLong(pos++, since.getTime());
                    if (onlyOwn) {
                        stmt.setInt(pos++, userId);
                    }
                }
            }, folderId, columns, type);
        if (StorageType.DELETED == type) {
            final StringBuilder sql2 = new StringBuilder();
            sql2.append("SELECT ");
            final String activeTaskTable = SQL.TASK_TABLES.get(StorageType.ACTIVE);
            sql2.append(SQL.getFields(columns, false, activeTaskTable));
            sql2.append(" FROM ");
            sql2.append(activeTaskTable);
            sql2.append(" JOIN ");
            final String removedPartsTable = SQL.PARTS_TABLES.get(StorageType.REMOVED);
            sql2.append(removedPartsTable);
            sql2.append(" ON ");
            sql2.append(activeTaskTable);
            sql2.append(".cid=");
            sql2.append(removedPartsTable);
            sql2.append(".cid AND ");
            sql2.append(activeTaskTable);
            sql2.append(".id=");
            sql2.append(removedPartsTable);
            sql2.append(".task ");
            sql2.append("WHERE ");
            sql2.append(activeTaskTable);
            sql2.append(".cid=? AND ");
            sql2.append(removedPartsTable);
            sql2.append(".folder=? AND ");
            sql2.append(activeTaskTable);
            sql2.append(".last_modified>?");
            if (onlyOwn) {
                sql2.append(" AND ");
                sql2.append(SQL.getOnlyOwn(activeTaskTable));
            }
            if (noPrivate) {
                sql2.append(" AND ");
                sql2.append(SQL.getNoPrivate(activeTaskTable));
            }
            final TaskIterator iter2 = new TaskIterator2(ctx, userId,
                sql2.toString(),
                new StatementSetter() {
                    @Override
                    public void perform(final PreparedStatement stmt)
                        throws SQLException {
                        int pos = 1;
                        stmt.setInt(pos++, ctx.getContextId());
                        stmt.setInt(pos++, folderId);
                        stmt.setLong(pos++, since.getTime());
                        if (onlyOwn) {
                            stmt.setInt(pos++, userId);
                        }
                    }
                }, folderId, columns, StorageType.REMOVED);
            return new CombinedSearchIterator<Task>(iter1, iter2);
        }
        return iter1;
    }

    /* (non-Javadoc)
     * @see com.openexchange.groupware.tasks.TaskSearch#find()
     */
    @Override
    public SearchIterator<Task> find(final Context context, final int userID, TaskSearchObject searchObject, int[] columns, int orderBy, Order order, final List<Integer> all, final List<Integer> own, final List<Integer> shared) throws OXException {
        final List<Object> searchParameters = new ArrayList<Object>();
        StringBuilder builder = new StringBuilder();
        String fields = SQL.getFields(columns, true, "t");
        builder.append("SELECT DISTINCT ").append(fields);
        builder.append(" FROM task AS t ");
        builder.append(" LEFT JOIN task_folder AS tf ON (tf.id = t.id AND tf.cid = t.cid)");
        builder.append(" LEFT JOIN prg_attachment AS a ON (t.cid = a.cid AND t.id = a.attached)");

        if (searchObject.hasInternalParticipants()) {
            builder.append(" LEFT JOIN task_participant AS tp ON (t.cid = tp.cid AND t.id = tp.task)");
        }

        if (searchObject.hasExternalParticipants()) {
            builder.append(" LEFT JOIN task_eparticipant AS etp ON (t.cid = etp.cid AND t.id = etp.task)");
        }

        builder.append(" WHERE ");
        builder.append(" t.cid = ? AND ");
        builder.append(SQL.allFoldersWhere(all, own, shared));

        //set titles
        Set<String> titleFilters = searchObject.getTitles();
        if (titleFilters != null && titleFilters.size() > 0) {
            for(String t : titleFilters) {
                builder.append(" AND ");
                String preparedPattern = StringCollection.prepareForSearch(t, true, true);
                builder.append(containsWildcards(preparedPattern) ? " t.title LIKE ? " : " t.title = ? ");
                searchParameters.add(preparedPattern);
            }
        }

        //set descriptions
        Set<String> descriptionFilters = searchObject.getNotes();
        if (descriptionFilters != null && descriptionFilters.size() > 0) {
            for(String t : descriptionFilters) {
                builder.append(" AND ");
                String preparedPattern = StringCollection.prepareForSearch(t, true, true);
                builder.append(containsWildcards(preparedPattern) ? " t.description LIKE ? " : " t.description = ? ");
                searchParameters.add(preparedPattern);
            }
        }

        //set status
        Set<Integer> statusFilters = searchObject.getStateFilters();
        if (statusFilters != null && statusFilters.size() > 0) {
            builder.append(" AND (");
            int i = 0;
            for(Integer s : statusFilters) {
                if (i++ > 0) {
                    builder.append(" OR ");
                }
                builder.append(" t.state = ? ");
                searchParameters.add(s);
            }
            builder.append(" ) ");
        }

        //set attachment
        Set<String> attachmentFilters = searchObject.getAttachmentNames();
        if (attachmentFilters != null && attachmentFilters.size() > 0) {
            for(String t : attachmentFilters) {
                builder.append(" AND ");
                String preparedPattern = StringCollection.prepareForSearch(t, true, true);
                builder.append(containsWildcards(preparedPattern) ? " a.filename LIKE ? " : " a.filename = ? ");
                searchParameters.add(preparedPattern);
            }
        }

        //set queries
        Set<String> queries = searchObject.getQueries();
        if (queries != null && queries.size() > 0) {
            for(String q : queries) {
                String preparedPattern = StringCollection.prepareForSearch(q, true, true);
                builder.append(containsWildcards(preparedPattern) ?
                                                    " AND (t.description LIKE ? OR t.title LIKE ? OR a.filename LIKE ? ) " :
                                                    " AND (t.description = ? OR t.title = ? OR a.filename = ?) ");
                searchParameters.add(preparedPattern);
                searchParameters.add(preparedPattern);
                searchParameters.add(preparedPattern);
            }
        }

        //set participants
        if (searchObject.hasParticipants()) {
            builder.append(" AND ( ");
            int i = 0;
            for(Integer id : searchObject.getUserIDs()) {
                if (i++ > 0) {
                    builder.append(" AND ");
                }
                builder.append(" t.id IN ( SELECT tp.task FROM task_participant AS tp WHERE t.id = tp.task AND t.cid = tp.cid AND tp.user = ? )");
                searchParameters.add(id);
            }
            //if (searchObject.hasInternalParticipants())
                //builder.append(" ) ");
            i = 0;
            for(String mail : searchObject.getExternalParticipants()) {
                if (searchObject.hasInternalParticipants() || i++ >= 1) {
                    builder.append(" AND ");
                }
                String preparedPattern = StringCollection.prepareForSearch(mail, false, false);
                builder.append(" etp.mail = ? ");
                searchParameters.add(preparedPattern);
            }
            builder.append(" ) ");
        }

        //set the recurrence type (mutually exclusive)
        if (searchObject.isSingleOccurenceFilter()) {
            builder.append(" AND t.recurrence_type = 0 ");
        } else if (searchObject.isSeriesFilter()) {
            builder.append(" AND t.recurrence_type > 0 ");
        }

        builder.append(SQL.getOrder(orderBy, order)).append(SQL.getLimit(searchObject.getStart(), searchObject.getSize()));

        //set parameters
        StatementSetter ss = new StatementSetter() {
            @Override
            public void perform(PreparedStatement stmt) throws SQLException {
                int pos = 1;
                stmt.setInt(pos++, context.getContextId());
                for (final int i : all) {
                    stmt.setInt(pos++, i);
                }
                for (final int i : own) {
                    stmt.setInt(pos++, i);
                }
                if (own.size() > 0) {
                    stmt.setInt(pos++, userID);
                }
                for (final int i : shared) {
                    stmt.setInt(pos++, i);
                }
                for (Object o : searchParameters) {
                    stmt.setObject(pos++, o);
                }
            }
        };

        TaskIterator it = new TaskIterator2(context, userID, builder.toString(), ss, -1, columns, StorageType.ACTIVE);

        return it;
    }

    private static final Pattern WILDCARD_PATTERN = Pattern.compile("((^|[^\\\\])%)|((^|[^\\\\])_)");

    /**
     * Verify whether the given pattern contains wild-cards.
     *
     * @param pattern The pattern to check
     * @return <code>true</code> if pattern contains wild-card; otherwise <code>false</code>
     */
    private static boolean containsWildcards(String pattern) {
        return null != pattern && WILDCARD_PATTERN.matcher(pattern).find();
    }
}
