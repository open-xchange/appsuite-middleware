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

package com.openexchange.database;

import java.sql.Connection;
import com.openexchange.exception.OXException;

/**
 * If your bundle needs to create database tables for working properly this service must be implemented. Its method are called if a new
 * schema for contexts is created. The order of executing {@link CreateTableService} instances is calculated by the string arrays given from
 * the methods {@link #requiredTables()} and {@link #tablesToCreate()}. The {@link #perform(Connection)} method should then create the
 * tables needed for your bundle.
 *
 * The table must be created in its newest version. <code>com.openexchange.groupware.update.UpdateTask</code>s are not executed after all
 * tables have been created and the schema
 * is marked in that way that all {@link UpdateTask}s have already been executed.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface CreateTableService {

    /**
     * This method should return all names of those table that have to exist before the {@link #perform(Connection)} method is called.
     * For instance if a table has foreign keys constraints to other tables.
     *
     * @return An array with table names that have to exist before the {@link #perform(Connection)} method is called.
     */
    String[] requiredTables();

    /**
     * This method must return all names of those tables that shall be created during call of the {@link #perform(Connection)} method.
     *
     * @return An array with table names that are created during call of the {@link #perform(Connection)} method.
     */
    String[] tablesToCreate();

    /**
     * The implementation of this method should create the required tables on the given database connection. The connection is already
     * configured to use the correct schema. The given connection is already in a transaction. Do not modify the transaction state of the
     * connection.
     *
     * @param con writable connection in a transaction state.
     * @throws OXException should be thrown if creating the table fails.
     */
    void perform(Connection con) throws OXException;

}
