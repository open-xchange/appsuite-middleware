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

package com.openexchange.regional.impl.db;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreateRegionalSettingsTableService}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.3
 */
public class CreateRegionalSettingsTableService extends AbstractCreateTableImpl {

    public static final String TABLE_NAME = "regional_settings";
    //@formatter:off
    public static final String TABLE_STMT = "CREATE TABLE regional_settings (" +
                                            "cid INT4 UNSIGNED NOT NULL," +
                                            "userId INT4 UNSIGNED NOT NULL," +
                                            "timeFormat VARCHAR(64) COLLATE utf8mb4_bin," +
                                            "timeFormatLong VARCHAR(64) COLLATE utf8mb4_bin," +
                                            "dateFormat VARCHAR(64) COLLATE utf8mb4_bin," +
                                            "dateFormatShort VARCHAR(64) COLLATE utf8mb4_bin," +
                                            "dateFormatMedium VARCHAR(64) COLLATE utf8mb4_bin," +
                                            "dateFormatLong VARCHAR(64) COLLATE utf8mb4_bin," +
                                            "dateFormatFull VARCHAR(64) COLLATE utf8mb4_bin," +
                                            "numberFormat VARCHAR(64) COLLATE utf8mb4_bin," +
                                            "firstDayOfWeek TINYINT," +
                                            "firstDayOfYear TINYINT," +
                                            "PRIMARY KEY (cid,userId)" +
                                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;";
    //@formatter:on

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
        return new String[] { TABLE_STMT };
    }
}
