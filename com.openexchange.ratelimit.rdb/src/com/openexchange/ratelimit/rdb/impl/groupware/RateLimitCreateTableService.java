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

package com.openexchange.ratelimit.rdb.impl.groupware;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link RateLimitCreateTableService} - The service to create needed "ratelimit" table.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class RateLimitCreateTableService extends AbstractCreateTableImpl {

    private static String TABLE_NAME = "ratelimit";

    @Override
    public String[] requiredTables() {
        return new String[0];
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { TABLE_NAME };
    }

    @Override
    protected String[] getCreateStatements() {
        return new String[] {   "CREATE TABLE " + TABLE_NAME + " (" +
                                    "cid INT4 UNSIGNED NOT NULL," +
                                    "userId INT4 UNSIGNED NOT NULL," +
                                    "id VARCHAR(128) COLLATE utf8mb4_bin NOT NULL," +
                                    "timestamp BIGINT(20) NOT NULL," +
                                    "permits BIGINT(20) NOT NULL," +
                                    "PRIMARY KEY (cid,userId,id,timestamp)" +
                                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"};
    }

}
