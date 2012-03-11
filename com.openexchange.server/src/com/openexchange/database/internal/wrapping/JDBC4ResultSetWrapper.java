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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;

/**
 * {@link JDBC4ResultSetWrapper}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class JDBC4ResultSetWrapper extends JDBC3ResultSetWrapper {

    private final ResultSet delegate;

    public JDBC4ResultSetWrapper(ResultSet delegate, JDBC3StatementWrapper stmt) {
        super(delegate, stmt);
        this.delegate = delegate;
    }

    @Override
    public int getHoldability() throws SQLException {
        return delegate.getHoldability();
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        return delegate.getNCharacterStream(columnIndex);
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        return delegate.getNCharacterStream(columnLabel);
    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        return delegate.getNClob(columnIndex);
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        return delegate.getNClob(columnLabel);
    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        return delegate.getNString(columnIndex);
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        return delegate.getNString(columnLabel);
    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        return delegate.getRowId(columnIndex);
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        return delegate.getRowId(columnLabel);
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        return delegate.getSQLXML(columnIndex);
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        return delegate.getSQLXML(columnLabel);
    }

    @Override
    public boolean isClosed() throws SQLException {
        return delegate.isClosed();
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        delegate.updateAsciiStream(columnIndex, x);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        delegate.updateAsciiStream(columnLabel, x);
    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        delegate.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        delegate.updateAsciiStream(columnLabel, x, length);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        delegate.updateBinaryStream(columnIndex, x);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        delegate.updateBinaryStream(columnLabel, x);
    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        delegate.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        delegate.updateBinaryStream(columnLabel, x, length);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        delegate.updateBlob(columnIndex, inputStream);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        delegate.updateBlob(columnLabel, inputStream);
    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        delegate.updateBlob(columnIndex, inputStream, length);
    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        delegate.updateBlob(columnLabel, inputStream, length);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        delegate.updateCharacterStream(columnIndex, x);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        delegate.updateCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        delegate.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        delegate.updateClob(columnIndex, reader);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        delegate.updateClob(columnLabel, reader);
    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        delegate.updateClob(columnIndex, reader, length);
    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateClob(columnLabel, reader, length);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        delegate.updateNCharacterStream(columnIndex, x);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        delegate.updateNCharacterStream(columnLabel, reader);
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        delegate.updateNCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateNCharacterStream(columnLabel, reader, length);
    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        delegate.updateNClob(columnIndex, nClob);
    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        delegate.updateNClob(columnLabel, nClob);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        delegate.updateNClob(columnIndex, reader);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        delegate.updateNClob(columnLabel, reader);
    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        delegate.updateNClob(columnIndex, reader, length);
    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        delegate.updateNClob(columnLabel, reader, length);
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        delegate.updateNString(columnIndex, nString);
    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        delegate.updateNString(columnLabel, nString);
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        delegate.updateRowId(columnIndex, x);
    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        delegate.updateRowId(columnLabel, x);
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        delegate.updateSQLXML(columnIndex, xmlObject);
    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        delegate.updateSQLXML(columnLabel, xmlObject);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return iface.isAssignableFrom(delegate.getClass());
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isAssignableFrom(delegate.getClass())) {
            return iface.cast(delegate);
        }
        throw new SQLException("Not a wrapper for: " + iface.getName());
    }
}
