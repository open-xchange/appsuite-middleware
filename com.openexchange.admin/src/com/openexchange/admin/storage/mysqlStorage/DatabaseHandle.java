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

package com.openexchange.admin.storage.mysqlStorage;

import com.openexchange.admin.rmi.dataobjects.Database;

/**
 * Internally used object for getnextdbhandlebyweight method instead of
 */
public class DatabaseHandle extends Database {

    private static final long serialVersionUID = -4816706296673058930L;

    private int count;
    private int schemaCount;

    public DatabaseHandle() {
        super();
        this.count = -1;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSchemaCount() {
        return schemaCount;
    }

    public void setSchemaCount(int schemaCount) {
        this.schemaCount = schemaCount;
    }

}
