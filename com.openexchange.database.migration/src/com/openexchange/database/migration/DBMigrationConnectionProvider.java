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

package com.openexchange.database.migration;

import java.sql.Connection;
import com.openexchange.exception.OXException;

/**
 * {@link DBMigrationConnectionProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public interface DBMigrationConnectionProvider {

    /**
     * Gets a database connection to perform the migration on.
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * <b>Note</b>:<br>
     * Do not forget to ensure obtained connection is orderly pushed back to this instance using either {@link #back(Connection)} or {@link #backAfterReading(Connection)} method.
     * </div>
     * <p>
     * Example:
     * <pre>
     *  Connection con = provider.get();
     *  try {
     *     ...
     *  } finally {
     *     provider.back(con); // or provider.backAfterReading(con) if connection is known to be used for read-only purpose
     *  }
     * </pre>
     *
     * @return The connection
     */
    Connection get() throws OXException;

    /**
     * Releases the previously obtained database connection that was used for <b>read-write</b> operations.
     *
     * @param connection The connection to release
     * @see #get()
     */
    void back(Connection connection);

    /**
     * Releases the previously obtained database connection that was used for <b>read-only</b> operations.
     *
     * @param connection The connection to release
     * @see #get()
     */
    void backAfterReading(Connection connection);

}
