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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.tools.sql.DBUtils.autocommit;
import static com.openexchange.tools.sql.DBUtils.rollback;
import static com.openexchange.tools.update.Tools.createIndex;
import static com.openexchange.tools.update.Tools.existsIndex;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import org.slf4j.LoggerFactory;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link DListAddIndexForLookup} - Creates indexes on tables "prg_dlist" and "del_dlist" to improve look-up.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DListAddIndexForLookup extends UpdateTaskAdapter {

    public DListAddIndexForLookup() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int contextId = params.getContextId();
        final Connection con = Database.getNoTimeout(contextId, true);
        try {
            con.setAutoCommit(false);
            final String[] tables = { "prg_dlist", "del_dlist" };
            createDListIndex(con, tables, "userIndex", "intfield02", "intfield03");
            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(con);
            throw UpdateExceptionCodes.UPDATE_FAILED.create(e, params.getSchema().getSchema(), e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(contextId, true, con);
        }
    }

    private void createDListIndex(final Connection con, final String[] tables, final String name, final String... columns) throws SQLException {
        final org.slf4j.Logger log = LoggerFactory.getLogger(DListAddIndexForLookup.class);
        final String[] cols = new String[columns.length + 1];
        cols[0] = "cid";
        System.arraycopy(columns, 0, cols, 1, columns.length);
        final StringBuilder sb = new StringBuilder(64);
        for (final String table : tables) {
            final String indexName = existsIndex(con, table, cols);
            if (null == indexName) {
                if (log.isInfoEnabled()) {
                    sb.setLength(0);
                    sb.append("Creating new index named \"");
                    sb.append(name);
                    sb.append("\" with columns ");
                    sb.append(Arrays.toString(cols));
                    sb.append(" on table ");
                    sb.append(table);
                    sb.append('.');
                    log.info(sb.toString());
                }
                createIndex(con, table, name, cols, false);
            } else {
                if (log.isInfoEnabled()) {
                    sb.setLength(0);
                    sb.append("New index named \"");
                    sb.append(indexName);
                    sb.append("\" with columns ");
                    sb.append(Arrays.toString(cols));
                    sb.append(" already exists on table ");
                    sb.append(table);
                    sb.append('.');
                    log.info(sb.toString());
                }
            }
        }
    }

}
