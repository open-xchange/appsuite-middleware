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

package com.openexchange.groupware.update.tasks.objectusagecount;

import com.openexchange.database.AbstractCreateTableImpl;

/**
 * {@link CreateObjectUseCountTableService}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.1
 */
public class CreateObjectUseCountTableService extends AbstractCreateTableImpl {

    /**
     * Initializes a new {@link CreateObjectUseCountTableService}.
     */
    public CreateObjectUseCountTableService() {
        super();
    }

    @Override
    public String[] requiredTables() {
        return new String[] { "user" };
    }

    @Override
    public String[] tablesToCreate() {
        return new String[] { "object_use_count" };
    }

    @Override
    protected String[] getCreateStatements() {
        String stmt = "CREATE TABLE object_use_count (" +
            "cid int(10) unsigned NOT NULL, " +
            "user int(10) unsigned NOT NULL, " +
            "folder int(10) unsigned NOT NULL, " +
            "object int(10) unsigned NOT NULL, " +
            "value int(10) unsigned NOT NULL, " +
            "PRIMARY KEY (cid, user, folder, object)" +
            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
        return new String[] { stmt };
    }

}
