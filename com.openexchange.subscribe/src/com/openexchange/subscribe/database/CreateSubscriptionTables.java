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

package com.openexchange.subscribe.database;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * Creates the tables needed for the subscription part of PubSub
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias Prinz</a>
 */
public class CreateSubscriptionTables extends AbstractCreateTableImpl {

    @Override
    public String[] getCreateStatements() {
        return new String[] {
            "CREATE TABLE subscriptions (" +
            "cid INT4 UNSIGNED NOT NULL," +
            "id INT4 UNSIGNED NOT NULL," +
            "user_id INT4 UNSIGNED NOT NULL," +
            "configuration_id INT4 UNSIGNED NOT NULL," +
            "source_id VARCHAR(255) NOT NULL," +
            "folder_id VARCHAR(255) NOT NULL," +
            "last_update INT8 UNSIGNED NOT NULL," +
            "enabled BOOLEAN DEFAULT true NOT NULL," +
            "created INT8 NOT NULL DEFAULT 0," +
            "lastModified INT8 NOT NULL DEFAULT 0," +
            "PRIMARY KEY (cid,id)," +
            "INDEX `folderIndex` (`cid`, `folder_id`(191))," +
            "FOREIGN KEY(cid,user_id) REFERENCES user(cid,id)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;",

            "CREATE TABLE sequence_subscriptions (" +
            "cid INT4 UNSIGNED NOT NULL," +
            "id INT4 UNSIGNED NOT NULL," +
            "PRIMARY KEY (cid)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;"
        };
    }

    @Override
    public String[] requiredTables() {
        return new String[]{"user"};
    }

    @Override
    public String[] tablesToCreate() {
        return new String[]{"subscriptions","sequence_subscriptions"};
    }

}
