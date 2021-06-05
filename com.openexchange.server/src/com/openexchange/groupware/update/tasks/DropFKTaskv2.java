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
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link DropFKTaskv2} - Performs several adjustments to DB schema to get aligned to clean v7.4.1 installation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.4.2
 */
public final class DropFKTaskv2 extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link DropFKTaskv2}.
     */
    public DropFKTaskv2() {
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
            con.setAutoCommit(false);
            rollback = 1;

            final List<String> tables = Arrays.asList(
                "pop3_storage_deleted",
                "pop3_storage_ids",
                "user_mail_account_properties",
                "user_mail_account",
                "user_transport_account_properties",
                "user_transport_account"
            );
            for (final String table : tables) {
                handleTable(table, con);
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
                    rollback(con);
                }
                autocommit(con);
            }
        }

        // Check for >>CONSTRAINT `pop3_storage_deleted_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)<<
        dropForeignKeySafe("pop3_storage_deleted_ibfk_1", "pop3_storage_deleted", con);

        // Check for >>CONSTRAINT `pop3_storage_deleted_ibfk_2` FOREIGN KEY (`cid`, `user`, `id`) REFERENCES `user_mail_account` (`cid`, `user`, `id`)<<
        dropForeignKeySafe("pop3_storage_deleted_ibfk_2", "pop3_storage_deleted", con);

        // Check for >>CONSTRAINT `pop3_storage_ids_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)<<
        dropForeignKeySafe("pop3_storage_ids_ibfk_1", "pop3_storage_ids", con);

        // Check for >>CONSTRAINT `pop3_storage_ids_ibfk_2` FOREIGN KEY (`cid`, `user`, `id`) REFERENCES `user_mail_account` (`cid`, `user`, `id`)<<
        dropForeignKeySafe("pop3_storage_ids_ibfk_2", "pop3_storage_ids", con);

        try {
            if (Databases.tablesExist(con, "prg_dates", "del_dates")) {
                // Check "uid" column in prg_dates
                enlargeVarcharColumn("uid", 1024, "prg_dates", con);

                // Check "uid" column in del_dates
                enlargeVarcharColumn("uid", 1024, "del_dates", con);
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        }
    }

    private boolean enlargeVarcharColumn(final String colName, final int newSize, final String tableName, final Connection con) throws OXException {
        ResultSet rsColumns = null;
        boolean doAlterTable = false;
        try {
            DatabaseMetaData meta = con.getMetaData();
            rsColumns = meta.getColumns(null, null, tableName, null);
            while (rsColumns.next()) {
                final String columnName = rsColumns.getString("COLUMN_NAME");
                if (colName.equals(columnName)) {
                    final int size = rsColumns.getInt("COLUMN_SIZE");
                    if (size < newSize) {
                        doAlterTable = true;
                    }
                    break;
                }
            }
            Databases.closeSQLStuff(rsColumns);
            rsColumns = null;

            if (doAlterTable) {
                com.openexchange.tools.update.Tools.modifyColumns(con, tableName, new Column(colName, "VARCHAR("+newSize+")"));
                return true;
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(rsColumns);
        }
        return false;
    }

    private boolean handleTable(final String table, final Connection con) throws SQLException {
        final List<String> keyNames = Tools.allForeignKey(con, table);
        PreparedStatement stmt = null;
        boolean modified = false;
        for (final String keyName : keyNames) {
            try {
                stmt = con.prepareStatement("ALTER TABLE " + table + " DROP FOREIGN KEY " + keyName);
                modified |= (stmt.executeUpdate() > 0);
            } finally {
                Databases.closeSQLStuff(null, stmt);
            }
        }
        return modified;
    }

    private boolean dropForeignKeySafe(final String foreignKeyName, final String table, final Connection con) {
        boolean modified = false;
        try {
            PreparedStatement stmt = null;
            try {
                stmt = con.prepareStatement("ALTER TABLE " + table + " DROP FOREIGN KEY " + foreignKeyName);
                modified = stmt.executeUpdate() > 0;
            } finally {
                Databases.closeSQLStuff(null, stmt);
            }
        } catch (Exception e) {
            // Ignore
        }
        return modified;
    }
}
