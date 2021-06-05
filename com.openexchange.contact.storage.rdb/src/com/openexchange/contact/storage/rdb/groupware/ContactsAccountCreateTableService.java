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

package com.openexchange.contact.storage.rdb.groupware;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link ContactsAccountCreateTableService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class ContactsAccountCreateTableService extends AbstractCreateTableImpl {

    /**
     * Gets the <code>CREATE TABLE</code> statement for a specific <i>contacts</i> table.
     *
     * @return The <code>CREATE TABLE</code> statement, or <code>null</code> if table name unknown
     */
    static String getCreateTableStmt(String tableName) {
        return getTablesByName().get(tableName);
    }

    /**
     * Gets the <code>CREATE TABLE</code> statements for the <i>contacts</i> tables, mapped by their table names.
     *
     * @return The <code>CREATE TABLE</code> statements mapped by their table name
     */
    static Map<String, String> getTablesByName() {
        Map<String, String> tablesByName = new HashMap<String, String>(11);
        //@formatter:off
        tablesByName.put("contacts_account",
            "CREATE TABLE contacts_account (" +
                "cid INT4 UNSIGNED NOT NULL," +
                "id INT4 UNSIGNED NOT NULL," +
                "user INT4 UNSIGNED NOT NULL," +
                "provider VARCHAR(64) COLLATE utf8mb4_bin NOT NULL," +
                "modified BIGINT(20) NOT NULL," +
                "internalConfig BLOB," +
                "userConfig BLOB," +
                "PRIMARY KEY (cid,id,user)," +
                "KEY user (cid,user,provider)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
        );
        //@formatter:on
        return tablesByName;
    }

    /**
     * Initializes a new {@link ContactsAccountCreateTableService}.
     */
    public ContactsAccountCreateTableService() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return new String[0];
    }

    @Override
    public String[] tablesToCreate() {
        Set<String> tableNames = getTablesByName().keySet();
        return tableNames.toArray(new String[tableNames.size()]);
    }

    @Override
    protected String[] getCreateStatements() {
        Collection<String> createStatements = getTablesByName().values();
        return createStatements.toArray(new String[createStatements.size()]);
    }
}
