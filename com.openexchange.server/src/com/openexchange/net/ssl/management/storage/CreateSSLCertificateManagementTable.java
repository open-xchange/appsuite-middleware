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

package com.openexchange.net.ssl.management.storage;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreateSSLCertificateManagementTable}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CreateSSLCertificateManagementTable extends AbstractCreateTableImpl {

    private static final String TABLE_NAME = "user_certificate";

    private static final String CREATE_STATEMENT = "CREATE TABLE `user_certificate` (" +
        "`cid` INT4 UNSIGNED NOT NULL," +
        "`userid` INT UNSIGNED NOT NULL," +
        "`host` VARCHAR(255) NOT NULL," +
        "`host_hash` VARCHAR(64) NOT NULL," +
        "`fingerprint` VARCHAR(64) NOT NULL," +
        "`trusted` BOOLEAN NOT NULL," +
        "PRIMARY KEY (`cid`,`userid`,`host_hash`,`fingerprint`)" +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";

    /**
     * Initialises a new {@link CreateSSLCertificateManagementTable}.
     */
    public CreateSSLCertificateManagementTable() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return NO_TABLES;
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { TABLE_NAME };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] { CREATE_STATEMENT };
    }
}
