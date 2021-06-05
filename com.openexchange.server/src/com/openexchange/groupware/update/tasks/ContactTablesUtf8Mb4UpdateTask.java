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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.SimpleConvertUtf8ToUtf8mb4UpdateTask;
import com.openexchange.tools.update.Column;

/**
 * {@link ContactTablesUtf8Mb4UpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class ContactTablesUtf8Mb4UpdateTask extends SimpleConvertUtf8ToUtf8mb4UpdateTask {

    private static final Logger LOG = LoggerFactory.getLogger(ContactTablesUtf8Mb4UpdateTask.class);

    /**
     * Initialises a new {@link ContactTablesUtf8Mb4UpdateTask}.
     */
    public ContactTablesUtf8Mb4UpdateTask() {
        //@formatter:off
        super(Arrays.asList("prg_dlist", "del_dlist", "prg_contacts_image", "del_contacts_image"),
            ContactAddVCardIdTask.class.getName());
        //@formatter:on
    }

    @Override
    protected void before(PerformParameters params, Connection connection) throws SQLException {
        recreateKey(connection, "prg_contacts", new String[] { "cid", "field01" }, new int[] { -1, 191 });
        recreateKey(connection, "prg_contacts", new String[] { "cid", "field65" }, new int[] { -1, 191 });
        recreateKey(connection, "prg_contacts", new String[] { "cid", "field66" }, new int[] { -1, 191 });
        recreateKey(connection, "prg_contacts", new String[] { "cid", "field67" }, new int[] { -1, 191 });

        recreateKey(connection, "del_contacts", new String[] { "cid", "field01" }, new int[] { -1, 191 });
        recreateKey(connection, "del_contacts", new String[] { "cid", "field65" }, new int[] { -1, 191 });
        recreateKey(connection, "del_contacts", new String[] { "cid", "field66" }, new int[] { -1, 191 });
        recreateKey(connection, "del_contacts", new String[] { "cid", "field67" }, new int[] { -1, 191 });

        Column column = new Column("field17", "TEXT COLLATE utf8mb4_unicode_ci NULL");
        String schema = params.getSchema().getSchema();

        resetZeroedTimestampColumn(connection, "timestampfield01");
        resetZeroedTimestampColumn(connection, "timestampfield02");

        LOG.info("");

        changeTable(connection, schema, "prg_contacts", Collections.emptyMap(), Collections.singletonList(column), Collections.emptyList());
        changeTable(connection, schema, "del_contacts", Collections.emptyMap(), Collections.singletonList(column), Collections.emptyList());
    }

    /**
     * Resets the value of the column with the specified name to <code>NULL</code> if the timestamp is '0000-00-00'
     *
     * @param connection The {@link Connection}
     * @param columnName The column name
     * @throws SQLException if an SQL error is occurred
     */
    private void resetZeroedTimestampColumn(Connection connection, String columnName) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = connection.prepareStatement("UPDATE IGNORE prg_contacts SET " + columnName + "=NULL WHERE " + columnName + "='0000-00-00'");
            int rows = ps.executeUpdate();
            LOG.info("Reset {} rows for column '{}' that contained invalid timestamps", Integer.valueOf(rows), columnName);
        } finally {
            Databases.closeSQLStuff(ps);
        }
    }
}
