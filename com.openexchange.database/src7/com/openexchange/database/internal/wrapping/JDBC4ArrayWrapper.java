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

package com.openexchange.database.internal.wrapping;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * The method {@link #getResultSet()} must be overwritten to return the {@link JDBC4ResultSetWrapper}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class JDBC4ArrayWrapper implements Array {

    private final Array delegate;

    private final JDBC4ResultSetWrapper result;

    public JDBC4ArrayWrapper(final Array delegate, final JDBC4ResultSetWrapper result) {
        super();
        this.delegate = delegate;
        this.result = result;
    }

    @Override
    public Object getArray() throws SQLException {
        return delegate.getArray();
    }

    @Override
    public Object getArray(final Map<String, Class<?>> map) throws SQLException {
        return delegate.getArray(map);
    }

    @Override
    public Object getArray(final long index, final int count) throws SQLException {
        return delegate.getArray(index, count);
    }

    @Override
    public Object getArray(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
        return delegate.getArray(index, count, map);
    }

    @Override
    public int getBaseType() throws SQLException {
        return delegate.getBaseType();
    }

    @Override
    public String getBaseTypeName() throws SQLException {
        return delegate.getBaseTypeName();
    }

    @Override
    public ResultSet getResultSet() throws SQLException {
        return new JDBC41ResultSetWrapper(delegate.getResultSet(), result.getStatement());
    }

    @Override
    public ResultSet getResultSet(final Map<String, Class<?>> map) throws SQLException {
        return new JDBC41ResultSetWrapper(delegate.getResultSet(map), result.getStatement());
    }

    @Override
    public ResultSet getResultSet(final long index, final int count) throws SQLException {
        return new JDBC41ResultSetWrapper(delegate.getResultSet(index, count), result.getStatement());
    }

    @Override
    public ResultSet getResultSet(final long index, final int count, final Map<String, Class<?>> map) throws SQLException {
        return new JDBC41ResultSetWrapper(delegate.getResultSet(index, count, map), result.getStatement());
    }

    @Override
    public void free() throws SQLException {
        delegate.free();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
