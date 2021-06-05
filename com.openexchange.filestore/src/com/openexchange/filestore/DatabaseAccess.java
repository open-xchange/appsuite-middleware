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

package com.openexchange.filestore;

import java.sql.Connection;
import com.openexchange.exception.OXException;

/**
 * {@link DatabaseAccess} - A database access for file storages.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public interface DatabaseAccess {

    /**
     * Checks existence for given tables and creates each of them, which does not yet exist.
     *
     * @param tables The tables to create
     * @throws OXException If tables cannot be created
     */
    void createIfAbsent(DatabaseTable... tables) throws OXException;

    // ------------------------------------------------------------------------------

    /**
     * Acquires a read-only connection.
     *
     * @return A read-only connection
     * @throws OXException If a read-only connection cannot be established
     */
    Connection acquireReadOnly() throws OXException;

    /**
     * Releases a previously acquired read-only connection.
     *
     * @param con The read-only connection to release
     */
    void releaseReadOnly(Connection con);

    // ------------------------------------------------------------------------------

    /**
     * Acquires a read-write connection.
     *
     * @return A read-write connection
     * @throws OXException If a read-write connection cannot be established
     */
    Connection acquireWritable() throws OXException;

    /**
     * Releases a previously acquired read-write connection.
     *
     * @param con The read-write connection to release
     * @param forReading <code>true</code> if read-write connection was only used for reading and no modification was performed; otherwise <code>false</code>
     */
    void releaseWritable(Connection con, boolean forReading);

}
