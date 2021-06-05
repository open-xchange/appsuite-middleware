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

package com.openexchange.snippet.db;


/**
 * {@link Tables} - Provides table specifications.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Tables {

    /**
     * Initializes a new {@link Tables}.
     */
    private Tables() {
        super();
    }

    /**
     * Gets the name of the snippet table.
     *
     * @return The table name
     */
    public static String getSnippetName() {
        return "snippet";
    }

    /**
     * Gets the SQL's <code>CREATE</code> statement for the snippet table
     *
     * @return The SQL's <code>CREATE</code> statement
     */
    public static String getSnippetTable() {
        return "CREATE TABLE "+getSnippetName()+" (" +
               " cid INT4 unsigned NOT NULL," +
               " user INT4 unsigned NOT NULL," +
               " id VARCHAR(64) CHARACTER SET latin1 NOT NULL," +
               " accountId INT4 unsigned DEFAULT NULL," +
               " displayName VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
               " module VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
               " type VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
               " shared TINYINT unsigned DEFAULT NULL," +
               " refType TINYINT unsigned NOT NULL," +
               " refId VARCHAR(255) CHARACTER SET latin1 NOT NULL," +
               " lastModified BIGINT(64) NOT NULL," +
               " size INT4 unsigned DEFAULT NULL," +
               " PRIMARY KEY (cid, user, id)," +
               " INDEX `indexShared` (cid, shared)," +
               " INDEX `indexRefType` (cid, user, id, refType)" +
               ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
    }

}
