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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.update.Tools.createIndex;
import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.SQLException;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link FolderAddIndex4SharedFolderSearch} - Creates indexes on tables "oxfolder_tree" and "del_oxfolder_tree" to improve shared folder
 * search.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class FolderAddIndex4SharedFolderSearch extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link FolderAddIndex4SharedFolderSearch}.
     */
    public FolderAddIndex4SharedFolderSearch() {
        super();
    }

    @Override
    public int addedWithVersion() {
        return 100;
    }

    @Override
    public int getPriority() {
        return UpdateTaskPriority.HIGH.priority;
    }

    private static final String[] DEPENDENCIES = { DuplicateContactCollectFolderRemoverTask.class.getName() };

    @Override
    public String[] getDependencies() {
        return DEPENDENCIES;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        int contextId = params.getContextId();
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false);

            final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(FolderAddIndex4SharedFolderSearch.class));

            String[] tables = { "oxfolder_tree", "del_oxfolder_tree" };
            createIndexes(con, tables, new String[] { "cid", "parent" }, "parentIndex", log);
            createIndexes(con, tables, new String[] { "cid", "type" }, "typeIndex", log);
            createIndexes(con, tables, new String[] { "cid", "module" }, "moduleIndex", log);

            tables = new String[] { "oxfolder_permissions", "del_oxfolder_permissions" };
            createIndexes(con, tables, new String[] { "cid", "permission_id", "fuid" }, "principal", log);

            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private void createIndexes(final Connection con, final String[] tables, final String[] fieldNames, final String name, final Log log) {
        final StringBuilder sb = new StringBuilder(64);
        for (final String table : tables) {
            try {
                final String indexName = existsIndex(con, table, fieldNames);
                if (null == indexName) {
                    if (log.isInfoEnabled()) {
                        sb.setLength(0);
                        sb.append("Creating new index named \"");
                        sb.append(name);
                        sb.append("\" with columns (");
                        sb.append(fieldNames[0]);
                        for (int i = 1; i < fieldNames.length; i++) {
                            sb.append(',').append(fieldNames[i]);
                        }
                        sb.append(") on table ");
                        sb.append(table);
                        sb.append('.');
                        log.info(sb.toString());
                    }
                    createIndex(con, table, name, fieldNames, false);
                } else {
                    if (log.isInfoEnabled()) {
                        sb.setLength(0);
                        sb.append("New index named \"");
                        sb.append(indexName);
                        sb.append("\" with columns (");
                        sb.append(fieldNames[0]);
                        for (int i = 1; i < fieldNames.length; i++) {
                            sb.append(',').append(fieldNames[i]);
                        }
                        sb.append(") already exists on table ");
                        sb.append(table);
                        sb.append('.');
                        log.info(sb.toString());
                    }
                }
            } catch (final SQLException e) {
                log.error(
                    new StringBuilder("Problem adding index ").append(name).append(" on table ").append(table).append('.').toString(),
                    e);
            }
        }
    }
}
