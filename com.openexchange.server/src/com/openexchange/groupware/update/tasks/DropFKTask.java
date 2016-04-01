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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.sql.DBUtils;
import com.openexchange.tools.update.Tools;

/**
 * {@link DropFKTask} - Drops rather needless foreign keys from pop3 tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropFKTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link DropFKTask}.
     */
    public DropFKTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        final int cid = params.getContextId();
        final Connection con = Database.getNoTimeout(cid, true);
        try {
            Databases.startTransaction(con);

            /*
             * Drop foreign keys
             */
            {
                final List<String> tables =
                    Arrays.asList(
                        "pop3_storage_deleted",
                        "pop3_storage_ids",
                        "user_mail_account_properties",
                        "user_mail_account",
                        "user_transport_account_properties",
                        "user_transport_account",
                        "virtualBackupPermission",
                        "virtualBackupSubscription",
                        "virtualBackupTree",
                        "virtualPermission",
                        "virtualSubscription",
                        "virtualTree");
                for (final String table : tables) {
                    dropForeignKeysFrom(table, con);
                }
            }
            /*
             * Check indexes
             */
            {
                final Map<String, List<List<String>>> indexes = new LinkedHashMap<String, List<List<String>>>();

                List<List<String>> indexList = new ArrayList<List<String>>(4);
                indexList.add(Arrays.asList("cid", "tree", "user", "parentId"));
                indexList.add(Arrays.asList("cid", "tree", "user", "shadow"));
                indexList.add(Arrays.asList("cid", "user"));
                indexList.add(Arrays.asList("cid", "modifiedBy"));
                indexes.put("virtualTree", indexList);

                indexList = new ArrayList<List<String>>(1);
                indexList.add(Arrays.asList("cid", "tree", "user", "folderId"));
                indexes.put("virtualPermission", indexList);

                indexList = new ArrayList<List<String>>(4);
                indexList.add(Arrays.asList("cid", "tree", "user", "parentId"));
                indexList.add(Arrays.asList("cid", "tree", "user", "shadow"));
                indexList.add(Arrays.asList("cid", "user"));
                indexList.add(Arrays.asList("cid", "modifiedBy"));
                indexes.put("virtualTree", indexList);

                indexList = new ArrayList<List<String>>(1);
                indexList.add(Arrays.asList("cid", "tree", "user", "folderId"));
                indexes.put("virtualPermission", indexList);

                indexList = new ArrayList<List<String>>(2);
                indexList.add(Arrays.asList("cid", "user"));
                indexList.add(Arrays.asList("cid", "user", "id"));
                indexes.put("pop3_storage_ids", indexList);

                indexList = new ArrayList<List<String>>(2);
                indexList.add(Arrays.asList("cid", "user"));
                indexList.add(Arrays.asList("cid", "user", "id"));
                indexes.put("pop3_storage_deleted", indexList);

                for (final Entry<String, List<List<String>>> entry : indexes.entrySet()) {
                    final String table = entry.getKey();
                    for (final List<String> cols : entry.getValue()) {
                        checkIndex(table, cols.toArray(new String[cols.size()]), null, con);
                    }
                }
            }

            con.commit();
        } catch (final SQLException e) {
            rollback(con);
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            rollback(con);
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            autocommit(con);
            Database.backNoTimeout(cid, true, con);
        }
    }

    private void dropForeignKeysFrom(final String table, final Connection con) throws SQLException {
        final List<String> keyNames = Tools.allForeignKey(con, table);
        Statement stmt = null;
        for (final String keyName : keyNames) {
            try {
                stmt = con.createStatement();
                stmt.execute("ALTER TABLE " + table + " DROP FOREIGN KEY " + keyName);
            } finally {
                DBUtils.closeSQLStuff(null, stmt);
            }
        }
    }

    private void checkIndex(final String table, final String[] columns, final String optName, final Connection con) throws SQLException {
        if (null == Tools.existsIndex(con, table, columns)) {
            Tools.createIndex(con, table, optName, columns, false);
        }
    }
}
