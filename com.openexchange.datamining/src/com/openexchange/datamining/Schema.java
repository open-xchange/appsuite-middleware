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
