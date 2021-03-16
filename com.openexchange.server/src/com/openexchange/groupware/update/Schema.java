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

package com.openexchange.groupware.update;

import java.io.Serializable;

/**
 * Interface to the data container for the update information of a database
 * schema.
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Schema extends Serializable {

    /**
     * Checks if the schema is currently updated by blocking update tasks.
     *
     * @return <code>true</code> if the schema will currently be updated.
     */
    boolean isLocked();

    /**
     * Gets the name of the server that is currently updating the schema.
     *
     * @return The name of the server that is currently updating the schema or <code>null</code>.
     */
    String getServer();

    /**
     * Gets the name of the database schema.
     *
     * @return schema name
     */
    String getSchema();

    /**
     * Gets the identifier of the schema-associated database pool.
     *
     * @return The pool identifier
     */
    int getPoolId();
}
