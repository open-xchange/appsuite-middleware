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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * The method {@link #getStatement()} must be overwritten to return a {@link JDBC41StatementWrapper}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class JDBC4ResultSetWrapper implements ResultSet {

    private final ResultSet delegate;

    private final JDBC4StatementWrapper stmt;

    /**
     * Initializes a new {@link JDBC4ResultSetWrapper}.
     *
     * @param delegate The delegate result set
     * @param stmt The statement wrapper
     */
    public JDBC4ResultSetWrapper(final ResultSet delegate, final JDBC4StatementWrapper stmt) {
        super();
        this.delegate = delegate;
        this.stmt = stmt;
    }

    @Override
    public boolean absolute(final int row) throws SQLException {
        return delegate.absolute(row);
    }

    @Override
    public void afterLast() throws SQLException {
        delegate.afterLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        delegate.beforeFirst();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        delegate.cancelRowUpdates();
    }

    @Override
    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    @Override
    public void close() throws SQLException {
        delegate.close();
    }

    @Override
    public void deleteRow() throws SQLException {
        delegate.deleteRow();
    }

    @Override
    public int findColumn(final String columnName) throws SQLException {
        return delegate.findColumn(columnName);
    }

    @Override
    public boolean first() throws SQLException {
        return delegate.first();
    }

    @Override
    public Array getArray(final int i) throws SQLException {
        return new JDBC4ArrayWrapper(delegate.getArray(i), this);
    }

    @Override
    public Array getArray(final String colName) throws SQLException {
        return new JDBC4ArrayWrapper(delegate.getArray(colName), this);
    }

    @Override
    public InputStream getAsciiStream(final int columnIndex) throws SQLException {
        return delegate.getAsciiStream(columnIndex);
    }

    @Override
    public InputStream getAsciiStream(final String columnName) throws SQLException {
        return delegate.getAsciiStream(columnName);
    }

    @Override
    public BigDecimal getBigDecimal(final int columnIndex) throws SQLException {
        return delegate.getBigDecimal(columnIndex);
    }

    @Override
    public BigDecimal getBigDecimal(final String columnName) throws SQLException {
        return delegate.getBigDecimal(columnName);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(final int columnIndex, final int scale) throws SQLException {
        return delegate.getBigDecimal(columnIndex, scale);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(final String columnName, final int scale) throws SQLException {
        return delegate.getBigDecimal(columnName, scale);
    }

    @Override
    public InputStream getBinaryStream(final int columnIndex) throws SQLException {
        return delegate.getBinaryStream(columnIndex);
    }

    @Override
    public InputStream getBinaryStream(final String columnName) throws SQLException {
        return delegate.getBinaryStream(columnName);
    }

    @Override
    public Blob getBlob(final int i) throws SQLException {
        return delegate.getBlob(i);
    }

    @Override
    public Blob getBlob(final String colName) throws SQLException {
        return delegate.getBlob(colName);
    }

    @Override
    public boolean getBoolean(final int columnIndex) throws SQLException {
        return delegate.getBoolean(columnIndex);
    }

    @Override
    public boolean getBoolean(final String columnName) throws SQLException {
        return delegate.getBoolean(columnName);
    }

    @Override
    public byte getByte(final int columnIndex) throws SQLException {
        return delegate.getByte(columnIndex);
    }

    @Override
    public byte getByte(final String columnName) throws SQLException {
        return delegate.getByte(columnName);
    }

    @Override
    public byte[] getBytes(final int columnIndex) throws SQLException {
        return delegate.getBytes(columnIndex);
    }

    @Override
    public byte[] getBytes(final String columnName) throws SQLException {
        return delegate.getBytes(columnName);
    }

    @Override
    public Reader getCharacterStream(final int columnIndex) throws SQLException {
        return delegate.getCharacterStream(columnIndex);
    }

    @Override
    public Reader getCharacterStream(final String columnName) throws SQLException {
        return delegate.getCharacterStream(columnName);
    }

    @Override
    public Clob getClob(final int i) throws SQLException {
        return delegate.getClob(i);
    }

    @Override
    public Clob getClob(final String colName) throws SQLException {
        return delegate.getClob(colName);
    }

    @Override
    public int getConcurrency() throws SQLException {
        return delegate.getConcurrency();
    }

    @Override
    public String getCursorName() throws SQLException {
        return delegate.getCursorName();
    }

    @Override
    public Date getDate(final int columnIndex) throws SQLException {
        return delegate.getDate(columnIndex);
    }

    @Override
    public Date getDate(final String columnName) throws SQLException {
        return delegate.getDate(columnName);
    }

    @Override
    public Date getDate(final int columnIndex, final Calendar cal) throws SQLException {
        return delegate.getDate(columnIndex, cal);
    }

    @Override
    public Date getDate(final String columnName, final Calendar cal) throws SQLException {
        return delegate.getDate(columnName, cal);
    }

    @Override
    public double getDouble(final int columnIndex) throws SQLException {
        return delegate.getDouble(columnIndex);
    }

    @Override
    public double getDouble(final String columnName) throws SQLException {
        return delegate.getDouble(columnName);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    @Override
    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    @Override
    public float getFloat(final int columnIndex) throws SQLException {
        return delegate.getFloat(columnIndex);
    }

    @Override
    public float getFloat(final String columnName) throws SQLException {
        return delegate.getFloat(columnName);
    }

    @Override
    public int getInt(final int columnIndex) throws SQLException {
        return delegate.getInt(columnIndex);
    }

    @Override
    public int getInt(final String columnName) throws SQLException {
        return delegate.getInt(columnName);
    }

    @Override
    public long getLong(final int columnIndex) throws SQLException {
        return delegate.getLong(columnIndex);
    }

    @Override
    public long getLong(final String columnName) throws SQLException {
        return delegate.getLong(columnName);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public Object getObject(final int columnIndex) throws SQLException {
        return delegate.getObject(columnIndex);
    }

    @Override
    public Object getObject(final String columnName) throws SQLException {
        return delegate.getObject(columnName);
    }

    @Override
    public Object getObject(final int i, final Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(i, map);
    }

    @Override
    public Object getObject(final String colName, final Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(colName, map);
    }

    @Override
    public Ref getRef(final int i) throws SQLException {
        return delegate.getRef(i);
    }

    @Override
    public Ref getRef(final String colName) throws SQLException {
        return delegate.getRef(colName);
    }

    @Override
    public int getRow() throws SQLException {
        return delegate.getRow();
    }

    @Override
    public short getShort(final int columnIndex) throws SQLException {
        return delegate.getShort(columnIndex);
    }

    @Override
    public short getShort(final String columnName) throws SQLException {
        return delegate.getShort(columnName);
    }

    @Override
    public JDBC4StatementWrapper getStatement() {
        return stmt;
    }

    @Override
    public String getString(final int columnIndex) throws SQLException {
        return delegate.getString(columnIndex);
    }

    @Override
    public String getString(final String columnName) throws SQLException {
        return delegate.getString(columnName);
    }

    @Override
    public Time getTime(final int columnIndex) throws SQLException {
        return delegate.getTime(columnIndex);
    }

    @Override
    public Time getTime(final String columnName) throws SQLException {
        return delegate.getTime(columnName);
    }

    @Override
    public Time getTime(final int columnIndex, final Calendar cal) throws SQLException {
        return delegate.getTime(columnIndex, cal);
    }

    @Override
    public Time getTime(final String columnName, final Calendar cal) throws SQLException {
        return delegate.getTime(columnName, cal);
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex) throws SQLException {
        return delegate.getTimestamp(columnIndex);
    }

    @Override
    public Timestamp getTimestamp(final String columnName) throws SQLException {
        return delegate.getTimestamp(columnName);
    }

    @Override
    public Timestamp getTimestamp(final int columnIndex, final Calendar cal) throws SQLException {
        return delegate.getTimestamp(columnIndex, cal);
    }

    @Override
    public Timestamp getTimestamp(final String columnName, final Calendar cal) throws SQLException {
        return delegate.getTimestamp(columnName, cal);
    }

    @Override
    public int getType() throws SQLException {
        return delegate.getType();
    }

    @Override
    public URL getURL(final int columnIndex) throws SQLException {
        return delegate.getURL(columnIndex);
    }

    @Override
    public URL getURL(final String columnName) throws SQLException {
        return delegate.getURL(columnName);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(final int columnIndex) throws SQLException {
        return delegate.getUnicodeStream(columnIndex);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(final String columnName) throws SQLException {
        return delegate.getUnicodeStream(columnName);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    @Override
    public void insertRow() throws SQLException {
        delegate.insertRow();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return delegate.isAfterLast();
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return delegate.isBeforeFirst();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return delegate.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return delegate.isLast();
    }

    @Override
    public boolean last() throws SQLException {
        return delegate.last();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        delegate.moveToCurrentRow();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        delegate.moveToInsertRow();
    }

    @Override
    public boolean next() throws SQLException {
        return delegate.next();
    }

    @Override
    public boolean previous() throws SQLException {
        return delegate.previous();
    }

    @Override
    public void refreshRow() throws SQLException {
        delegate.refreshRow();
    }

    @Override
    public boolean relative(final int rows) throws SQLException {
        return delegate.relative(rows);
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return delegate.rowDeleted();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return delegate.rowInserted();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return delegate.rowUpdated();
    }

    @Override
    public void setFetchDirection(final int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    @Override
    public void setFetchSize(final int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    @Override
    public void updateArray(final int columnIndex, final Array x) throws SQLException {
        delegate.updateArray(columnIndex, x);
    }

    @Override
    public void updateArray(final String columnName, final Array x) throws SQLException {
        delegate.updateArray(columnName, x);
    }

    @Override
    public void updateAsciiStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        delegate.updateAsciiStream(columnIndex, x, length);
    }

    @Override
    public void updateAsciiStream(final String columnName, final InputStream x, final int length) throws SQLException {
        delegate.updateAsciiStream(columnName, x, length);
    }

    @Override
    public void updateBigDecimal(final int columnIndex, final BigDecimal x) throws SQLException {
        delegate.updateBigDecimal(columnIndex, x);
    }

    @Override
    public void updateBigDecimal(final String columnName, final BigDecimal x) throws SQLException {
        delegate.updateBigDecimal(columnName, x);
    }

    @Override
    public void updateBinaryStream(final int columnIndex, final InputStream x, final int length) throws SQLException {
        delegate.updateBinaryStream(columnIndex, x, length);
    }

    @Override
    public void updateBinaryStream(final String columnName, final InputStream x, final int length) throws SQLException {
        delegate.updateBinaryStream(columnName, x, length);
    }

    @Override
    public void updateBlob(final int columnIndex, final Blob x) throws SQLException {
        delegate.updateBlob(columnIndex, x);
    }

    @Override
    public void updateBlob(final String columnName, final Blob x) throws SQLException {
        delegate.updateBlob(columnName, x);
    }

    @Override
    public void updateBoolean(final int columnIndex, final boolean x) throws SQLException {
        delegate.updateBoolean(columnIndex, x);
    }

    @Override
    public void updateBoolean(final String columnName, final boolean x) throws SQLException {
        delegate.updateBoolean(columnName, x);
    }

    @Override
    public void updateByte(final int columnIndex, final byte x) throws SQLException {
        delegate.updateByte(columnIndex, x);
    }

    @Override
    public void updateByte(final String columnName, final byte x) throws SQLException {
        delegate.updateByte(columnName, x);
    }

    @Override
    public void updateBytes(final int columnIndex, final byte[] x) throws SQLException {
        delegate.updateBytes(columnIndex, x);
    }

    @Override
    public void updateBytes(final String columnName, final byte[] x) throws SQLException {
        delegate.updateBytes(columnName, x);
    }

    @Override
    public void updateCharacterStream(final int columnIndex, final Reader x, final int length) throws SQLException {
        delegate.updateCharacterStream(columnIndex, x, length);
    }

    @Override
    public void updateCharacterStream(final String columnName, final Reader reader, final int length) throws SQLException {
        delegate.updateCharacterStream(columnName, reader, length);
    }

    @Override
    public void updateClob(final int columnIndex, final Clob x) throws SQLException {
        delegate.updateClob(columnIndex, x);
    }

    @Override
    public void updateClob(final String columnName, final Clob x) throws SQLException {
        delegate.updateClob(columnName, x);
    }

    @Override
    public void updateDate(final int columnIndex, final Date x) throws SQLException {
        delegate.updateDate(columnIndex, x);
    }

    @Override
    public void updateDate(final String columnName, final Date x) throws SQLException {
        delegate.updateDate(columnName, x);
    }

    @Override
    public void updateDouble(final int columnIndex, final double x) throws SQLException {
        delegate.updateDouble(columnIndex, x);
    }

    @Override
    public void updateDouble(final String columnName, final double x) throws SQLException {
        delegate.updateDouble(columnName, x);
    }

    @Override
    public void updateFloat(final int columnIndex, final float x) throws SQLException {
        delegate.updateFloat(columnIndex, x);
    }

    @Override
    public void updateFloat(final String columnName, final float x) throws SQLException {
        delegate.updateFloat(columnName, x);
    }

    @Override
    public void updateInt(final int columnIndex, final int x) throws SQLException {
        delegate.updateInt(columnIndex, x);
    }

    @Override
    public void updateInt(final String columnName, final int x) throws SQLException {
        delegate.updateInt(columnName, x);
    }

    @Override
    public void updateLong(final int columnIndex, final long x) throws SQLException {
        delegate.updateLong(columnIndex, x);
    }

    @Override
    public void updateLong(final String columnName, final long x) throws SQLException {
        delegate.updateLong(columnName, x);
    }

    @Override
    public void updateNull(final int columnIndex) throws SQLException {
        delegate.updateNull(columnIndex);
    }

    @Override
    public void updateNull(final String columnName) throws SQLException {
        delegate.updateNull(columnName);
    }

    @Override
    public void updateObject(final int columnIndex, final Object x) throws SQLException {
        delegate.updateObject(columnIndex, x);
    }

    @Override
    public void updateObject(final String columnName, final Object x) throws SQLException {
        delegate.updateObject(columnName, x);
    }

    @Override
    public void updateObject(final int columnIndex, final Object x, final int scale) throws SQLException {
        delegate.updateObject(columnIndex, x, scale);
    }

    @Override
    public void updateObject(final String columnName, final Object x, final int scale) throws SQLException {
        delegate.updateObject(columnName, x, scale);
    }

    @Override
    public void updateRef(final int columnIndex, final Ref x) throws SQLException {
        delegate.updateRef(columnIndex, x);
    }

    @Override
    public void updateRef(final String columnName, final Ref x) throws SQLException {
        delegate.updateRef(columnName, x);
    }

    @Override
    public void updateRow() throws SQLException {
        delegate.updateRow();
    }

    @Override
    public void updateShort(final int columnIndex, final short x) throws SQLException {
        delegate.updateShort(columnIndex, x);
    }

    @Override
    public void updateShort(final String columnName, final short x) throws SQLException {
        delegate.updateShort(columnName, x);
    }

    @Override
    public void updateString(final int columnIndex, final String x) throws SQLException {
        delegate.updateString(columnIndex, x);
    }

    @Override
    public void updateString(final String columnName, final String x) throws SQLException {
        delegate.updateString(columnName, x);
    }

    @Override
    public void updateTime(final int columnIndex, final Time x) throws SQLException {
        delegate.updateTime(columnIndex, x);
    }

    @Override
    public void updateTime(final String columnName, final Time x) throws SQLException {
        delegate.updateTime(columnName, x);
    }

    @Override
    public void updateTimestamp(final int columnIndex, final Timestamp x) throws SQLException {
        delegate.updateTimestamp(columnIndex, x);
    }

    @Override
    public void updateTimestamp(final String columnName, final Timestamp x) throws SQLException {
        delegate.updateTimestamp(columnName, x);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return delegate.wasNull();
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

    @Override
    public String toString() {
        return delegate.toString();
    }
}
