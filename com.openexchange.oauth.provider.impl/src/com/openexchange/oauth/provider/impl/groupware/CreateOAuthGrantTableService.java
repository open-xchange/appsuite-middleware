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

package com.openexchange.oauth.provider.impl.groupware;

import com.openexchange.database.AbstractCreateTableImpl;


/**
 * {@link CreateOAuthGrantTableService}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public final class CreateOAuthGrantTableService extends AbstractCreateTableImpl {

    private static final String CREATE_GRANT_TABLE = "CREATE TABLE `oauth_grant` (" +
        " `cid` INT4 unsigned NOT NULL," +
        " `user` INT4 unsigned NOT NULL," +
        " `refresh_token` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
        " `access_token` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
        " `client` VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL," +
        " `expiration_date` BIGINT(64) NOT NULL," +
        " `scopes` VARCHAR(767) NOT NULL," +
        " `creation_date` BIGINT(64) NOT NULL," +
        " `last_modified` BIGINT(64) NOT NULL," +
        " PRIMARY KEY (refresh_token)," +
        " UNIQUE KEY `access_token` (access_token)," +
        " KEY `client` (client)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    /**
     * Initializes a new {@link CreateOAuthGrantTableService}.
     */
    public CreateOAuthGrantTableService() {
        super();
    }

    /**
     * Gets the table names.
     *
     * @return The table names.
     */
    public static String[] getTablesToCreate() {
        return new String[] { "oauth_grant" };
    }

    /**
     * Gets the CREATE-TABLE statements.
     *
     * @return The CREATE statements
     */
    public static String[] getCreateStmts() {
        return new String[] { CREATE_GRANT_TABLE };
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return getTablesToCreate();
    }

    @Override
    protected String[] getCreateStatements() {
        return getCreateStmts();
    }

}
