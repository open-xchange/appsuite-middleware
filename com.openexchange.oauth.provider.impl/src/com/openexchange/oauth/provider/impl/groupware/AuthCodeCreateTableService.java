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
 * {@link AuthCodeCreateTableService}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AuthCodeCreateTableService extends AbstractCreateTableImpl {

    private static final String TABLE_AUTH_CODE = "authCode";

    private static final String CREATE_AUTH_CODE = "CREATE TABLE `"+TABLE_AUTH_CODE+"` (" +
        " `code` varchar(64) NOT NULL," +
        " `cid` INT4 unsigned NOT NULL," +
        " `user` INT4 unsigned NOT NULL," +
        " `clientId` varchar(255) NOT NULL," +
        " `redirectURI` varchar(767) NOT NULL," +
        " `scope` varchar(512) NOT NULL," +
        " `nanos` BIGINT(64) NOT NULL," +
        " PRIMARY KEY (`code`)," +
        " KEY `userIndex` (`cid`,`user`)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    /**
     * Gets the table names.
     *
     * @return The table names.
     */
    public static String[] getTablesToCreate() {
        return new String[] { TABLE_AUTH_CODE };
    }

    /**
     * Gets the CREATE-TABLE statements.
     *
     * @return The CREATE statements
     */
    public static String[] getCreateStmts() {
        return new String[] { CREATE_AUTH_CODE };
    }

    /**
     * Initializes a new {@link AuthCodeCreateTableService}.
     */
    public AuthCodeCreateTableService() {
        super();
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
