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

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreateOAuthAccountTable}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CreateOAuthAccountTable extends AbstractCreateTableImpl {

    static final String TABLE_NAME = "oauthAccounts";

    public static final String CREATE_TABLE_STATEMENT =
        "CREATE TABLE " + TABLE_NAME + " (" +
        "cid INT4 UNSIGNED NOT NULL," +
        "user INT4 UNSIGNED NOT NULL," +
        "id INT4 UNSIGNED NOT NULL," +
        "displayName VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
        "accessToken TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
        "accessSecret TEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
        "serviceId VARCHAR(128) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
        "scope VARCHAR(767) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL," +
        "identity VARCHAR(767) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL," +
        "expiryDate BIGINT(64) DEFAULT NULL," +
        "PRIMARY KEY (cid, id)," +
        "KEY `identity` (cid,identity(191))" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    public CreateOAuthAccountTable() {
        super();
    }

    @Override
    public String[] getCreateStatements() {
        return createStatements;
    }

    @Override
    public String[] requiredTables() {
        return requiredTables;
    }

    @Override
    public String[] tablesToCreate() {
        return createdTables;
    }

    private static final String[] requiredTables = { "user" };

    private static final String[] createdTables = { TABLE_NAME };

    private static final String[] createStatements = { CREATE_TABLE_STATEMENT };

}
