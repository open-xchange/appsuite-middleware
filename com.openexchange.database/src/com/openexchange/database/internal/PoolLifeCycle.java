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

package com.openexchange.database.internal;

import com.openexchange.exception.OXException;

/**
 * Interface for creating database connection pools.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public interface PoolLifeCycle {

    /**
     * Creates a connection pool for the given pool identifier.
     *
     * @param poolId The pool identifier.
     * @return The connection pool or <code>null</code> if the current {@link PoolLifeCycle} is not responsive for the given pool
     *         identifier.
     * @throws OXException If creating the connection pool has some serious problems.
     */
    ConnectionPool create(int poolId) throws OXException;

    /**
     * Destroys a {@link ConnectionPool}
     * 
     * @param poolId The identifier of the pool
     * @return <code>true</code> if the pool was within this life cycle and was destroyed
     *         <code>false</code> otherwise
     */
    boolean destroy(int poolId);
}
