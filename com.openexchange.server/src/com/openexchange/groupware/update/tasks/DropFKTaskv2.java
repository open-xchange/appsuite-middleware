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
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.sql.DBUtils;
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
        final int cid = params.getContextId();
        final DatabaseService dbService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);

        boolean modified = false;
        Connection con = null;
        boolean rollback = false;
        try {
            con = dbService.getForUpdateTask(cid);
            con.setAutoCommit(false);
            rollback = true;
            final List<String> tables = Arrays.asList(
                "pop3_storage_deleted",
                "pop3_storage_ids",
                "user_mail_account_properties",
                "user_mail_account",
                "user_transport_account_properties",
                "user_transport_account"
            );
            for (final String table : tables) {
                modified |= handleTable(table, con);
            }
            con.commit();
            rollback = false;
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback) {
                rollback(con);
            }
            autocommit(con);
            if (modified) {
                dbService.backForUpdateTask(cid, con);
            } else {
                dbService.backForUpdateTaskAfterReading(cid, con);
            }
        }

        // Check for >>CONSTRAINT `pop3_storage_deleted_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)<<
        con = dbService.getForUpdateTask(cid);
        modified = false;
        try {
            modified = dropForeignKeySafe("pop3_storage_deleted_ibfk_1", "pop3_storage_deleted", con);
        } finally {
            if (modified) {
                dbService.backForUpdateTask(cid, con);
            } else {
                dbService.backForUpdateTaskAfterReading(cid, con);
            }
        }

        // Check for >>CONSTRAINT `pop3_storage_deleted_ibfk_2` FOREIGN KEY (`cid`, `user`, `id`) REFERENCES `user_mail_account` (`cid`, `user`, `id`)<<
        con = dbService.getForUpdateTask(cid);
        modified = false;
        try {
            modified = dropForeignKeySafe("pop3_storage_deleted_ibfk_2", "pop3_storage_deleted", con);
        } finally {
            if (modified) {
                dbService.backForUpdateTask(cid, con);
            } else {
                dbService.backForUpdateTaskAfterReading(cid, con);
            }
        }

        // Check for >>CONSTRAINT `pop3_storage_ids_ibfk_1` FOREIGN KEY (`cid`, `user`) REFERENCES `user` (`cid`, `id`)<<
        con = dbService.getForUpdateTask(cid);
        modified = false;
        try {
            modified = dropForeignKeySafe("pop3_storage_ids_ibfk_1", "pop3_storage_ids", con);
        } finally {
            if (modified) {
                dbService.backForUpdateTask(cid, con);
            } else {
                dbService.backForUpdateTaskAfterReading(cid, con);
            }
        }

        // Check for >>CONSTRAINT `pop3_storage_ids_ibfk_2` FOREIGN KEY (`cid`, `user`, `id`) REFERENCES `user_mail_account` (`cid`, `user`, `id`)<<
        con = dbService.getForUpdateTask(cid);
        modified = false;
        try {
            modified = dropForeignKeySafe("pop3_storage_ids_ibfk_2", "pop3_storage_ids", con);
        } finally {
            if (modified) {
                dbService.backForUpdateTask(cid, con);
            } else {
                dbService.backForUpdateTaskAfterReading(cid, con);
            }
        }

        // Check "uid" column in prg_dates
        con = dbService.getForUpdateTask(cid);
        modified = false;
        try {
            modified = enlargeVarcharColumn("uid", 1024, "prg_dates", con);
        } finally {
            if (modified) {
                dbService.backForUpdateTask(cid, con);
            } else {
                dbService.backForUpdateTaskAfterReading(cid, con);
            }
        }

        // Check "uid" column in del_dates
        con = dbService.getForUpdateTask(cid);
        modified = false;
        try {
            modified = enlargeVarcharColumn("uid", 1024, "del_dates", con);
        } finally {
            if (modified) {
                dbService.backForUpdateTask(cid, con);
            } else {
                dbService.backForUpdateTaskAfterReading(cid, con);
            }
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
        } catch (final SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (final RuntimeException e) {
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
                DBUtils.closeSQLStuff(null, stmt);
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
                DBUtils.closeSQLStuff(null, stmt);
            }
        } catch (final Exception e) {
            // Ignore
        }
        return modified;
    }
}
