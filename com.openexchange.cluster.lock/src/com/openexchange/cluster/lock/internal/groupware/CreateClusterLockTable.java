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

package com.openexchange.cluster.lock.internal.groupware;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreateClusterLockTable}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CreateClusterLockTable extends AbstractCreateTableImpl {

    static final String TABLE_NAME = "clusterLock";

    static final String CREATE_TABLE_STATEMENT = "CREATE TABLE clusterLock (" +
        "cid INT4 UNSIGNED NOT NULL, " +
        "user INT4 UNSIGNED NOT NULL, " +
        "name VARCHAR(255) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL, " +
        "timestamp INT8 UNSIGNED NOT NULL, " +
        "PRIMARY KEY (cid, user, name) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    /**
     * Initialises a new {@link CreateClusterLockTable}.
     */
    public CreateClusterLockTable() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return new String[] {};
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "clusterLock" };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { CREATE_TABLE_STATEMENT };
    }
}
