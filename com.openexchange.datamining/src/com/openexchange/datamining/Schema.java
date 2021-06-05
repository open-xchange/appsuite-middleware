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

package com.openexchange.datamining;

/**
 * {@link Schema} - Represents a database schema.
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class Schema {

    private final String schemaname;
    private final String db_pool_id;
    private final String url;
    private final String login;
    private final String password;

    /**
     * Initializes a new {@link Schema}.
     *
     * @param schemaname The schema name
     * @param read_db_pool_id The identifier of the read-only database pool
     * @param url The base URL of the database pool
     * @param login The login for database access
     * @param password The password for database access
     */
    public Schema(String schemaname, String read_db_pool_id, String url, String login, String password) {
        this.schemaname = schemaname;
        this.db_pool_id = read_db_pool_id;
        this.url = url;
        this.login = login;
        this.password = password;
    }

    /**
     * Gets the schema name.
     *
     * @return The schema name
     */
    public String getSchemaname() {
        return schemaname;
    }

    /**
     * Gets the identifier of the read-only database pool.
     *
     * @return The identifier of the read-only database pool
     */
    public String getDb_pool_id() {
        return db_pool_id;
    }

    /**
     * Gets the base URL of the database pool.
     *
     * @return The base URL of the database pool
     */
    public String getUrl() {
        return url;
    }

    /**
     * Gets the login for database access.
     *
     * @return The login for database access
     */
    public String getLogin() {
        return login;
    }

    /**
     * Gets the password for database access.
     *
     * @return The password for database access
     */
    public String getPassword() {
        return password;
    }

}
