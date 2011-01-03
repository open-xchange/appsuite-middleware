/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the Open-Xchange, Inc. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.database.internal.wrapping;

import java.io.InputStream;
import java.io.Reader;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

/**
 * {@link JDBC4PreparedStatementWrapper}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class JDBC4PreparedStatementWrapper extends JDBC3PreparedStatementWrapper {

    private final PreparedStatement delegate;

    public JDBC4PreparedStatementWrapper(PreparedStatement delegate, JDBC3ConnectionReturner con) {
        super(delegate, con);
        this.delegate = delegate;
    }

    public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x);
    }

    public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
    }

    public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x);
    }

    public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
    }

    public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream);
    }

    public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
        delegate.setBlob(parameterIndex, inputStream, length);
    }

    public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader);
    }

    public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
    }

    public void setClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setClob(parameterIndex, reader);
    }

    public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setClob(parameterIndex, reader, length);
    }

    public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value);
    }

    public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
        delegate.setNCharacterStream(parameterIndex, value, length);
    }

    public void setNClob(int parameterIndex, NClob value) throws SQLException {
        delegate.setNClob(parameterIndex, value);
    }

    public void setNClob(int parameterIndex, Reader reader) throws SQLException {
        delegate.setNClob(parameterIndex, reader);
    }

    public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
        delegate.setNClob(parameterIndex, reader, length);
    }

    public void setNString(int parameterIndex, String value) throws SQLException {
        delegate.setNString(parameterIndex, value);
    }

    public void setRowId(int parameterIndex, RowId x) throws SQLException {
        delegate.setRowId(parameterIndex, x);
    }

    public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
        delegate.setSQLXML(parameterIndex, xmlObject);
    }

    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    public boolean isPoolable() throws SQLException {
        return delegate.isPoolable();
    }

    public void setPoolable(boolean poolable) throws SQLException {
        delegate.setPoolable(poolable);
    }

    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(delegate.getClass());
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(delegate.getClass())) {
            return iface.cast(delegate);
        }
        throw new SQLException("Not a wrapper for: " + iface.getName());
    }
}
