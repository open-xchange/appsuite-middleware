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

package com.openexchange.filestore;

/**
 * {@link DatabaseTable} - A representation for a database table that is supposed to be created if absent.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DatabaseTable {

    private final String tableName;
    private final String createTableStatement;

    /**
     * Initializes a new {@link DatabaseTable}.
     *
     * @param tableName The name of the table
     * @param createTableStatement The <code>CREATE TABLE</code> statement that is supposed to be executed if such a table does not yet exist
     */
    public DatabaseTable(String tableName, String createTableStatement) {
        super();
        this.tableName = tableName;
        this.createTableStatement = createTableStatement;
    }

    /**
     * Gets the name of the table
     *
     * @return The name of the table
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * Gets the <code>CREATE TABLE</code> statement that is supposed to be executed if such a table does not yet exist
     *
     * @return The <code>CREATE TABLE</code> statement that is supposed to be executed if such a table does not yet exist
     */
    public String getCreateTableStatement() {
        return createTableStatement;
    }

}
