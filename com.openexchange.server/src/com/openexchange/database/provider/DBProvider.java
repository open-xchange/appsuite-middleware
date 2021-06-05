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

package com.openexchange.database.provider;

import java.sql.Connection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.osgi.annotation.SingletonService;

/**
 * {@link DBProvider} - Provides connections for database access.
 *
 * @author <a href="mailto:info@open-xchange.com">Open-Xchange Engineering</a>
 */
@SingletonService
public interface DBProvider {

    /**
     * The dummy provider.
     */
    static final DBProvider DUMMY = new DBProvider() {
        @Override
        public Connection getReadConnection(final Context ctx) {
            throw new UnsupportedOperationException();
        }
        @Override
        public Connection getWriteConnection(final Context ctx) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void releaseReadConnection(final Context ctx, final Connection con) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void releaseWriteConnection(final Context ctx, final Connection con) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void releaseWriteConnectionAfterReading(Context ctx, Connection con) {
            throw new UnsupportedOperationException();
        }
    };

    /**
     * Gets a read-only connection for <code>SELECT</code> statements.
     *
     * @param ctx The associated context
     * @return A read-only connection
     * @throws OXException If connection cannot be returned
     */
    Connection getReadConnection(Context ctx) throws OXException;

    /**
     * Releases specified read-only connection.
     *
     * @param ctx The associated context
     * @param con The connection to release
     */
    void releaseReadConnection(Context ctx, Connection con);

    /**
     * Gets a read-write connection for <code>INSERT</code>, <code>UPDATE</code>, and/or <code>DELETE</code> statements.
     *
     * @param ctx The associated context
     * @return A read-write connection
     * @throws OXException If connection cannot be returned
     */
    Connection getWriteConnection(Context ctx) throws OXException;

    /**
     * Releases specified read-write connection.
     *
     * @param ctx The associated context
     * @param con The connection to release
     */
    void releaseWriteConnection(Context ctx, Connection con);

    /**
     * Releases specified read-write connection that was <b>only</b> used for read accesses.
     *
     * @param ctx The associated context
     * @param con The connection to release
     */
    void releaseWriteConnectionAfterReading(Context ctx, Connection con);

}
