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
import java.sql.Statement;
import java.util.Set;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.java.util.UUIDs;
import com.openexchange.tools.update.Tools;

/**
 * {@link MigrateAliasUpdateTask}
 *
 * @author <a href="mailto:lars.hoogestraat@open-xchange.com">Lars Hoogestraat</a>
 * @since v7.8.0
 */
public class MigrateAliasUpdateTask extends AbstractUserAliasTableUpdateTask {

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection conn = params.getConnection();
        int rollback = 0;
        try {
            conn.setAutoCommit(false);
            rollback = 1;

            if (false == Tools.tableExists(conn, "user_alias")) {
                createTable(conn);
            }

            Set<Alias> aliases = getAllAliasesInUserAttributes(conn);
            if (aliases != null && false == aliases.isEmpty()) {
                insertAliases(conn, aliases);
            }
            conn.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(conn);
                }
                Databases.autocommit(conn);
            }
        }
    }

    private void createTable(Connection conn) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            stmt.execute("CREATE TABLE `user_alias` ( " // --> Also specified in com.openexchange.admin.mysql.CreateLdap2SqlTables.createAliasTable
            + "`cid` INT4 UNSIGNED NOT NULL, "
            + "`user` INT4 UNSIGNED NOT NULL, "
            + "`alias` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL, "
            + "`uuid` BINARY(16) DEFAULT NULL,"
            + "PRIMARY KEY (`cid`, `user`, `alias`) "
            + ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;");
        } finally {
            closeSQLStuff(stmt);
        }
    }

    private int insertAliases(Connection conn, Set<Alias> aliases) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = conn.prepareStatement("REPLACE INTO user_alias (cid, user, alias, uuid) VALUES(?, ?, ?, ?)");
            int index;
            for (Alias alias : aliases) {
                index = 0;
                stmt.setInt(++index, alias.getCid());
                stmt.setInt(++index, alias.getUserId());
                stmt.setString(++index, alias.getAlias());
                stmt.setBytes(++index, UUIDs.toByteArray(alias.getUuid()));
                stmt.addBatch();
            }
            int[] updateCounts = stmt.executeBatch();

            int updated = 0;
            for (int updateCount : updateCounts) {
                updated += updateCount;
            }
            return updated;
        } finally {
            closeSQLStuff(stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(com.openexchange.groupware.update.UpdateConcurrency.BLOCKING);
    }
}
