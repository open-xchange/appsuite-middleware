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

import java.sql.DataTruncation;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.openexchange.groupware.search.TaskSearchObject;
import com.openexchange.groupware.tasks.Mapping.Mapper;
import com.openexchange.groupware.tasks.TaskException.Code;
import com.openexchange.groupware.tasks.TaskException.Detail;

/**
 * This class contains methods for building the sql query for searches.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class SQL {

    /**
     * SQL statement selecting all fields for a task.
     */
    private static final String ALL_FIELDS;

    /**
     * Prevent instanciation.
     */
    private SQL() {
        super();
    }

    /**
     * @return all fields colon seperated for using in SELECT and INSERT
     * statements.
     */
    static String getAllFields() {
        return ALL_FIELDS;
    }

    /**
     * @param columns attributes of a task that should be selected.
     * @param folder <code>true</code> if the folder must be selected in
     * searches.
     * @return all fields that are specified in the columns colon seperated for
     * using in SELECT and INSERT statements.
     * @throws TaskException if a mapping for a column isn't implemented.
     */
    static String getFields(final int[] columns, final boolean folder)
        throws TaskException {
        final StringBuilder sql = new StringBuilder();
        for (int i : columns) {
            final Mapper mapper = Mapping.getMapping(i);
            if (null == mapper) {
                switch (i) {
                case Task.PARTICIPANTS:
                case Task.ALARM:
                    break;
                case Task.FOLDER_ID:
                    if (folder) {
                        sql.append("folder,");
                    }
                    break;
                default:
                    throw new TaskException(Code.UNKNOWN_ATTRIBUTE, i);
                }
            } else {
                sql.append(mapper.getDBColumnName());
                sql.append(',');
            }
        }
        sql.setLength(sql.length() - 1);
        return sql.toString();
    }

    /**
     * @param all folder identifier where all tasks can be seen.
     * @param own folder identifier where own tasks can be seen.
     * @param shared folder identifier of shared task folders that can be seen.
     * @return the sql where condition for limiting the tasks to all readable
     * objects in folders.
     */
    static String allFoldersWhere(final List<Integer> all,
        final List<Integer> own, final List<Integer> shared) {
        final StringBuilder sql = new StringBuilder();
        if (all.size() + own.size() + shared.size() > 0) {
            sql.append('(');
            if (all.size() > 0) {
                sql.append("(folder in (");
                for (int i = 0; i < all.size(); i++) {
                    sql.append("?,");
                }
                sql.setLength(sql.length() - 1);
                sql.append(")) OR ");
            }
            if (own.size() > 0) {
                sql.append("(folder in (");
                for (int i = 0; i < own.size(); i++) {
                    sql.append("?,");
                }
                sql.setLength(sql.length() - 1);
                sql.append(") AND created_from=?) OR ");
            }
            if (shared.size() > 0) {
                sql.append("(folder in (");
                for (int i = 0; i < shared.size(); i++) {
                    sql.append("?,");
                }
                sql.setLength(sql.length() - 1);
                sql.append(") AND private=false)");
            } else {
                sql.setLength(sql.length() - "OR ".length());
            }
            sql.append(')');
        } else {
            sql.append("false");
        }
        return sql.toString();
    }

    /**
     * @param search task search object.
     * @return SQL condition checking the end of tasks to be in range.
     * @throws TaskException if the range is not defined properly in the task
     * search object.
     */
    static String getRangeWhere(final TaskSearchObject search)
        throws TaskException {
        final StringBuilder sql = new StringBuilder();
        final Date[] range = search.getRange();
        if (null != range) {
            if (range.length == 2) {
                sql.append("(end >= ? AND end <= ?)");
            } else {
                throw new TaskException(Code.WRONG_DATE_RANGE, range.length);
            }
        }
        return sql.toString();
    }

    static String getPatternWhere(final TaskSearchObject search) {
        final StringBuilder sql = new StringBuilder();
        // This compare is correct. NO_PATTERN is null and cannot be compared
        // with Object.equals()
        if (TaskSearchObject.NO_PATTERN != search.getPattern()) {
            sql.append('(');
            sql.append(Mapping.getMapping(Task.TITLE).getDBColumnName());
            sql.append(" LIKE ? OR ");
            sql.append(Mapping.getMapping(Task.NOTE).getDBColumnName());
            sql.append(" LIKE ? OR ");
            sql.append(Mapping.getMapping(Task.CATEGORIES).getDBColumnName());
            sql.append(" LIKE ?)");
        }
        return sql.toString();
    }

    /**
     * @param orderBy attribute identifier that should be used for sorting.
     * @param orderDir string defining the order direction. <code>"ASC"</code>
     * or <code>"DESC"</code>.
     * @return SQL order by expression.
     */
    static String getOrder(final int orderBy, final String orderDir) {
        final StringBuilder sql = new StringBuilder();
        if (0 != orderBy) {
            sql.append(" ORDER BY ");
            sql.append(Mapping.getMapping(orderBy).getDBColumnName());
            sql.append(' ');
            sql.append(orderDir);
        }
        return sql.toString();
    }

    /**
     * Creates the row limit for a SQL select command.
     * @param from start of limit.
     * @param to end of limit.
     * @return string to append to a SQL select command.
     */
    static String getLimit(final int from, final int to) {
        final StringBuilder sql = new StringBuilder();
        if (-1 != to) {
            sql.append(" LIMIT ");
            sql.append(from);
            sql.append(',');
            sql.append(to - from);
        }
        return sql.toString();
    }

    /**
     * Maps the index of the truncated value to the according identifier of the
     * truncated attribute.
     * @param fields string array with all truncated field names.
     * @return the identifier of the truncated attribute.
     */
    public static int[] findTruncated(final String[] fields) {
        final List<Integer> truncated = new ArrayList<Integer>();
        for (String field : fields) {
            for (Mapper mapper : Mapping.MAPPERS) {
                if (mapper.getDBColumnName().equals(field)) {
                    truncated.add(mapper.getId());
                    break;
                }
            }
        }
        final int[] retval = new int[truncated.size()];
        for (int i = 0; i < truncated.size(); i++) {
            retval[i] = truncated.get(i);
        }
        return retval;
    }

    static {
        final StringBuilder selectAll = new StringBuilder();
        for (Mapper mapper : Mapping.MAPPERS) {
            selectAll.append(mapper.getDBColumnName());
            selectAll.append(',');
        }
        selectAll.setLength(selectAll.length() - 1);
        ALL_FIELDS = selectAll.toString();
    }
}
