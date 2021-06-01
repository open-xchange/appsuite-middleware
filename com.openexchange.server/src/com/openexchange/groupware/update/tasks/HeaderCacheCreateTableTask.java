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

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.AbstractCreateTableImpl;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.tools.update.Tools;

/**
 * {@link HeaderCacheCreateTableTask} - Inserts necessary tables to support MAL Poll bundle features.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class HeaderCacheCreateTableTask extends AbstractCreateTableImpl implements UpdateTaskV2 {

    private static String getCreateMailUUIDTable() {
        return "CREATE TABLE mailUUID (" +
        " cid INT4 unsigned NOT NULL," +
        " user INT4 unsigned NOT NULL," +
        " account INT4 unsigned NOT NULL," +
        " fullname VARCHAR(128) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
        " id VARCHAR(70) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
        " uuid BINARY(16) NOT NULL," +
        " PRIMARY KEY (cid, user, account, fullname, id)," +
        " INDEX (cid, user, uuid)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    private static final String getCreateHeaderBlobTable() {
        return "CREATE TABLE headersAsBlob (" +
        " cid INT4 unsigned NOT NULL," +
        " user INT4 unsigned NOT NULL," +
        " uuid BINARY(16) NOT NULL," +
        " flags INT4 unsigned NOT NULL default '0'," +
        " receivedDate bigint(64) default NULL," +
        " rfc822Size bigint(64) UNSIGNED NOT NULL," +
        " userFlags VARCHAR(1024) collate utf8_unicode_ci default NULL," +
        " headers BLOB," +
        " PRIMARY KEY (cid, user, uuid)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";
    }

    @Override
    public String[] getCreateStatements() {
        return new String[] { getCreateMailUUIDTable(), getCreateHeaderBlobTable() };
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "mailUUID", "headersAsBlob" };
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            boolean mailUUIDExists = Tools.tableExists(con, "mailUUID");
            boolean headersAsBlobExists = Tools.tableExists(con, "headersAsBlob");
            if (headersAsBlobExists && mailUUIDExists) {
                return;
            }

            con.setAutoCommit(false);
            rollback = 1;

            if (!mailUUIDExists) {
                createTable(getCreateMailUUIDTable(), con);
            }
            if (!headersAsBlobExists) {
                createTable(getCreateHeaderBlobTable(), con);
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    private void createTable(String sqlCreate, Connection con) throws OXException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement(sqlCreate);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            closeSQLStuff(stmt);
        }
    }
}
