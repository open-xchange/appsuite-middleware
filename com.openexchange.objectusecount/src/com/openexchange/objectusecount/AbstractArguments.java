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

package com.openexchange.objectusecount;

import java.sql.Connection;

/**
 * {@link AbstractArguments} - Specifies arguments to use when modifying use count(s).
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public abstract class AbstractArguments {

    /** the optional connection reference */
    protected final Connection con;

    /** Signals whether an error is supposed to be thrown or not */
    protected final boolean throwException;

    /**
     * Initializes a new {@link AbstractArguments}.
     *
     * @param con The connection to use or <code>null</code>
     * @param throwException Whether an error is supposed to be thrown or not
     */
    protected AbstractArguments(Connection con, boolean throwException) {
        super();
        this.con = con;
        this.throwException = throwException;
    }

    /**
     * Checks if an exception is supposed to be thrown or not
     *
     * @return <code>true</code> to throw an exception; otherwise <code>false</code>
     */
    public boolean isThrowException() {
        return throwException;
    }

    /**
     * Gets the connection
     *
     * @return The connection or <code>null</code>
     */
    public Connection getCon() {
        return con;
    }

}
