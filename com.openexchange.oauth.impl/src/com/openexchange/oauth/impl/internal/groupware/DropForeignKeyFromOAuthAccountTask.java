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

package com.openexchange.oauth.impl.internal.groupware;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.tools.update.Tools;

/**
 * {@link DropForeignKeyFromOAuthAccountTask} - Drops rather needless foreign key from <code>"oauthAccounts"</code> table.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public final class DropForeignKeyFromOAuthAccountTask extends AbstractOAuthUpdateTask {

    /**
     * Initializes a new {@link DropForeignKeyFromOAuthAccountTask}.
     */
    public DropForeignKeyFromOAuthAccountTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    void innerPerform(Connection connection, PerformParameters performParameters) throws OXException, SQLException {
        // Drop foreign keys from...
        for (String table : Arrays.asList("oauthAccounts")) {
            dropForeignKeysFrom(table, connection);
        }
    }

    /**
     * Drops the foreign keys from the specified table
     * 
     * @param table The table from which to drop the foreign keys
     * @param con The {@link Connection} to use
     * @throws SQLException If an SQL error is occurred
     */
    private void dropForeignKeysFrom(String table, Connection con) throws SQLException {
        List<String> keyNames = Tools.allForeignKey(con, table);
        for (String keyName : keyNames) {
            try (Statement stmt = con.createStatement()) {
                stmt.execute("ALTER TABLE " + table + " DROP FOREIGN KEY " + keyName);
            }
        }
    }

}
