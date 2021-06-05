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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.autocommit;
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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
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
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            Databases.startTransaction(con);
            rollback = 1;

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
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    Databases.rollback(con);
                }
                autocommit(con);
            }
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
                Databases.closeSQLStuff(null, stmt);
            }
        }
    }

    private void checkIndex(final String table, final String[] columns, final String optName, final Connection con) throws SQLException {
        if (null == Tools.existsIndex(con, table, columns)) {
            Tools.createIndex(con, table, optName, columns, false);
        }
    }
}
