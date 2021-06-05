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

package com.openexchange.filestore.impl.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TransactionalUpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link MakeQuotaMaxConsistentInUserTable} - Ensures a NOT NULL value for "quota_max" column in "user" and "del_user" tables.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MakeQuotaMaxConsistentInUserTable extends TransactionalUpdateTaskAdapter {

    /**
     * Initializes a new {@link MakeQuotaMaxConsistentInUserTable}.
     */
    public MakeQuotaMaxConsistentInUserTable() {
        super();
    }

    @Override
    protected void performChanges(PerformParameters params, Connection con) throws OXException, SQLException {
        Logger log = org.slf4j.LoggerFactory.getLogger(MakeQuotaMaxConsistentInUserTable.class);
        log.info("Performing update task {}", MakeQuotaMaxConsistentInUserTable.class.getSimpleName());

        // Converts all NULL values to -1
        turnNulltoNumber("user", con);
        turnNulltoNumber("del_user", con);

        // Changes "quota_max BIGINT(20) DEFAULT NULL" to "quota_max BIGINT(20) NOT NULL DEFAULT -1" (if not yet performed)
        Column column = new Column("quota_max", "BIGINT(20) NOT NULL DEFAULT -1");
        if (Tools.isNullable(con, "user", "quota_max")) {
            Tools.modifyColumns(con, "user", column);
        }
        if (Tools.isNullable(con, "del_user", "quota_max")) {
            Tools.modifyColumns(con, "del_user", column);
        }

        log.info("{} successfully performed.", MakeQuotaMaxConsistentInUserTable.class.getSimpleName());
    }

    private void turnNulltoNumber(String table, Connection con) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("UPDATE " + table + " SET quota_max=-1 WHERE quota_max IS NULL");
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddFilestoreColumnsToUserTable.class.getName() };
    }
}
