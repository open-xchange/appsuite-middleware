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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * The method {@link #getStatement()} must be overwritten to return a {@link StatementWrapper}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class ResultSetWrapper implements ResultSet {

    private final ResultSet delegate;

    private final StatementWrapper stmt;

    public ResultSetWrapper(ResultSet delegate, StatementWrapper stmt) {
        super();
        this.delegate = delegate;
        this.stmt = stmt;
    }

    public boolean absolute(int row) throws SQLException {
        return delegate.absolute(row);
    }

    public void afterLast() throws SQLException {
        delegate.afterLast();
    }

    public void beforeFirst() throws SQLException {
        delegate.beforeFirst();
    }

    public void cancelRowUpdates() throws SQLException {
        delegate.cancelRowUpdates();
    }

    public void clearWarnings() throws SQLException {
        delegate.clearWarnings();
    }

    public void close() throws SQLException {
        delegate.close();
    }

    public void deleteRow() throws SQLException {
        delegate.deleteRow();
    }

    public int findColumn(String columnName) throws SQLException {
        return delegate.findColumn(columnName);
    }

    public boolean first() throws SQLException {
        return delegate.first();
    }

    public Array getArray(int i) throws SQLException {
        return new ArrayWrapper(delegate.getArray(i), this);
    }

    public Array getArray(String colName) throws SQLException {
        return new ArrayWrapper(delegate.getArray(colName), this);
    }

    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        return delegate.getAsciiStream(columnIndex);
    }

    public InputStream getAsciiStream(String columnName) throws SQLException {
        return delegate.getAsciiStream(columnName);
    }

    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        return delegate.getBigDecimal(columnIndex);
    }

    public BigDecimal getBigDecimal(String columnName) throws SQLException {
        return delegate.getBigDecimal(columnName);
    }

    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        return delegate.getBigDecimal(columnIndex, scale);
    }

    public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
        return delegate.getBigDecimal(columnName, scale);
    }

    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        return delegate.getBinaryStream(columnIndex);
    }

    public InputStream getBinaryStream(String columnName) throws SQLException {
        return delegate.getBinaryStream(columnName);
    }

    public Blob getBlob(int i) throws SQLException {
        return delegate.getBlob(i);
    }

    public Blob getBlob(String colName) throws SQLException {
        return delegate.getBlob(colName);
    }

    public boolean getBoolean(int columnIndex) throws SQLException {
        return delegate.getBoolean(columnIndex);
    }

    public boolean getBoolean(String columnName) throws SQLException {
        return delegate.getBoolean(columnName);
    }

    public byte getByte(int columnIndex) throws SQLException {
        return delegate.getByte(columnIndex);
    }

    public byte getByte(String columnName) throws SQLException {
        return delegate.getByte(columnName);
    }

    public byte[] getBytes(int columnIndex) throws SQLException {
        return delegate.getBytes(columnIndex);
    }

    public byte[] getBytes(String columnName) throws SQLException {
        return delegate.getBytes(columnName);
    }

    public Reader getCharacterStream(int columnIndex) throws SQLException {
        return delegate.getCharacterStream(columnIndex);
    }

    public Reader getCharacterStream(String columnName) throws SQLException {
        return delegate.getCharacterStream(columnName);
    }

    public Clob getClob(int i) throws SQLException {
        return delegate.getClob(i);
    }

    public Clob getClob(String colName) throws SQLException {
        return delegate.getClob(colName);
    }

    public int getConcurrency() throws SQLException {
        return delegate.getConcurrency();
    }

    public String getCursorName() throws SQLException {
        return delegate.getCursorName();
    }

    public Date getDate(int columnIndex) throws SQLException {
        return delegate.getDate(columnIndex);
    }

    public Date getDate(String columnName) throws SQLException {
        return delegate.getDate(columnName);
    }

    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        return delegate.getDate(columnIndex, cal);
    }

    public Date getDate(String columnName, Calendar cal) throws SQLException {
        return delegate.getDate(columnName, cal);
    }

    public double getDouble(int columnIndex) throws SQLException {
        return delegate.getDouble(columnIndex);
    }

    public double getDouble(String columnName) throws SQLException {
        return delegate.getDouble(columnName);
    }

    public int getFetchDirection() throws SQLException {
        return delegate.getFetchDirection();
    }

    public int getFetchSize() throws SQLException {
        return delegate.getFetchSize();
    }

    public float getFloat(int columnIndex) throws SQLException {
        return delegate.getFloat(columnIndex);
    }

    public float getFloat(String columnName) throws SQLException {
        return delegate.getFloat(columnName);
    }

    public int getInt(int columnIndex) throws SQLException {
        return delegate.getInt(columnIndex);
    }

    public int getInt(String columnName) throws SQLException {
        return delegate.getInt(columnName);
    }

    public long getLong(int columnIndex) throws SQLException {
        return delegate.getLong(columnIndex);
    }

    public long getLong(String columnName) throws SQLException {
        return delegate.getLong(columnName);
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    public Object getObject(int columnIndex) throws SQLException {
        return delegate.getObject(columnIndex);
    }

    public Object getObject(String columnName) throws SQLException {
        return delegate.getObject(columnName);
    }

    public Object getObject(int i, Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(i, map);
    }

    public Object getObject(String colName, Map<String, Class<?>> map) throws SQLException {
        return delegate.getObject(colName, map);
    }

    public Ref getRef(int i) throws SQLException {
        return delegate.getRef(i);
    }

    public Ref getRef(String colName) throws SQLException {
        return delegate.getRef(colName);
    }

    public int getRow() throws SQLException {
        return delegate.getRow();
    }

    public short getShort(int columnIndex) throws SQLException {
        return delegate.getShort(columnIndex);
    }

    public short getShort(String columnName) throws SQLException {
        return delegate.getShort(columnName);
    }

    public StatementWrapper getStatement() throws SQLException {
        return stmt;
    }

    public String getString(int columnIndex) throws SQLException {
        return delegate.getString(columnIndex);
    }

    public String getString(String columnName) throws SQLException {
        return delegate.getString(columnName);
    }

    public Time getTime(int columnIndex) throws SQLException {
        return delegate.getTime(columnIndex);
    }

    public Time getTime(String columnName) throws SQLException {
        return delegate.getTime(columnName);
    }

    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        return delegate.getTime(columnIndex, cal);
    }

    public Time getTime(String columnName, Calendar cal) throws SQLException {
        return delegate.getTime(columnName, cal);
    }

    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        return delegate.getTimestamp(columnIndex);
    }

    public Timestamp getTimestamp(String columnName) throws SQLException {
        return delegate.getTimestamp(columnName);
    }

    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        return delegate.getTimestamp(columnIndex, cal);
    }

    public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
        return delegate.getTimestamp(columnName, cal);
    }

    public int getType() throws SQLException {
        return delegate.getType();
    }

    public URL getURL(int columnIndex) throws SQLException {
        return delegate.getURL(columnIndex);
    }

    public URL getURL(String columnName) throws SQLException {
        return delegate.getURL(columnName);
    }

    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        return delegate.getUnicodeStream(columnIndex);
    }

    public InputStream getUnicodeStream(String columnName) throws SQLException {
        return delegate.getUnicodeStream(columnName);
    }

    public SQLWarning getWarnings() throws SQLException {
        return delegate.getWarnings();
    }

    public void insertRow() throws SQLException {
        delegate.insertRow();
    }

    public boolean isAfterLast() throws SQLException {
        return delegate.isAfterLast();
    }

    public boolean isBeforeFirst() throws SQLException {
        return delegate.isBeforeFirst();
    }

    public boolean isFirst() throws SQLException {
        return delegate.isFirst();
    }

    public boolean isLast() throws SQLException {
        return delegate.isLast();
    }

    public boolean last() throws SQLException {
        return delegate.last();
    }

    public void moveToCurrentRow() throws SQLException {
        delegate.moveToCurrentRow();
    }

    public void moveToInsertRow() throws SQLException {
        delegate.moveToInsertRow();
    }

    public boolean next() throws SQLException {
        return delegate.next();
    }

    public boolean previous() throws SQLException {
        return delegate.previous();
    }

    public void refreshRow() throws SQLException {
        delegate.refreshRow();
    }

    public boolean relative(int rows) throws SQLException {
        return delegate.relative(rows);
    }

    public boolean rowDeleted() throws SQLException {
        return delegate.rowDeleted();
    }

    public boolean rowInserted() throws SQLException {
        return delegate.rowInserted();
    }

    public boolean rowUpdated() throws SQLException {
        return delegate.rowUpdated();
    }

    public void setFetchDirection(int direction) throws SQLException {
        delegate.setFetchDirection(direction);
    }

    public void setFetchSize(int rows) throws SQLException {
        delegate.setFetchSize(rows);
    }

    public void updateArray(int columnIndex, Array x) throws SQLException {
        delegate.updateArray(columnIndex, x);
    }

    public void updateArray(String columnName, Array x) throws SQLException {
        delegate.updateArray(columnName, x);
    }

    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        delegate.updateAsciiStream(columnIndex, x, length);
    }

    public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
        delegate.updateAsciiStream(columnName, x, length);
    }

    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        delegate.updateBigDecimal(columnIndex, x);
    }

    public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
        delegate.updateBigDecimal(columnName, x);
    }

    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        delegate.updateBinaryStream(columnIndex, x, length);
    }

    public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
        delegate.updateBinaryStream(columnName, x, length);
    }

    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        delegate.updateBlob(columnIndex, x);
    }

    public void updateBlob(String columnName, Blob x) throws SQLException {
        delegate.updateBlob(columnName, x);
    }

    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        delegate.updateBoolean(columnIndex, x);
    }

    public void updateBoolean(String columnName, boolean x) throws SQLException {
        delegate.updateBoolean(columnName, x);
    }

    public void updateByte(int columnIndex, byte x) throws SQLException {
        delegate.updateByte(columnIndex, x);
    }

    public void updateByte(String columnName, byte x) throws SQLException {
        delegate.updateByte(columnName, x);
    }

    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        delegate.updateBytes(columnIndex, x);
    }

    public void updateBytes(String columnName, byte[] x) throws SQLException {
        delegate.updateBytes(columnName, x);
    }

    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        delegate.updateCharacterStream(columnIndex, x, length);
    }

    public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
        delegate.updateCharacterStream(columnName, reader, length);
    }

    public void updateClob(int columnIndex, Clob x) throws SQLException {
        delegate.updateClob(columnIndex, x);
    }

    public void updateClob(String columnName, Clob x) throws SQLException {
        delegate.updateClob(columnName, x);
    }

    public void updateDate(int columnIndex, Date x) throws SQLException {
        delegate.updateDate(columnIndex, x);
    }

    public void updateDate(String columnName, Date x) throws SQLException {
        delegate.updateDate(columnName, x);
    }

    public void updateDouble(int columnIndex, double x) throws SQLException {
        delegate.updateDouble(columnIndex, x);
    }

    public void updateDouble(String columnName, double x) throws SQLException {
        delegate.updateDouble(columnName, x);
    }

    public void updateFloat(int columnIndex, float x) throws SQLException {
        delegate.updateFloat(columnIndex, x);
    }

    public void updateFloat(String columnName, float x) throws SQLException {
        delegate.updateFloat(columnName, x);
    }

    public void updateInt(int columnIndex, int x) throws SQLException {
        delegate.updateInt(columnIndex, x);
    }

    public void updateInt(String columnName, int x) throws SQLException {
        delegate.updateInt(columnName, x);
    }

    public void updateLong(int columnIndex, long x) throws SQLException {
        delegate.updateLong(columnIndex, x);
    }

    public void updateLong(String columnName, long x) throws SQLException {
        delegate.updateLong(columnName, x);
    }

    public void updateNull(int columnIndex) throws SQLException {
        delegate.updateNull(columnIndex);
    }

    public void updateNull(String columnName) throws SQLException {
        delegate.updateNull(columnName);
    }

    public void updateObject(int columnIndex, Object x) throws SQLException {
        delegate.updateObject(columnIndex, x);
    }

    public void updateObject(String columnName, Object x) throws SQLException {
        delegate.updateObject(columnName, x);
    }

    public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
        delegate.updateObject(columnIndex, x, scale);
    }

    public void updateObject(String columnName, Object x, int scale) throws SQLException {
        delegate.updateObject(columnName, x, scale);
    }

    public void updateRef(int columnIndex, Ref x) throws SQLException {
        delegate.updateRef(columnIndex, x);
    }

    public void updateRef(String columnName, Ref x) throws SQLException {
        delegate.updateRef(columnName, x);
    }

    public void updateRow() throws SQLException {
        delegate.updateRow();
    }

    public void updateShort(int columnIndex, short x) throws SQLException {
        delegate.updateShort(columnIndex, x);
    }

    public void updateShort(String columnName, short x) throws SQLException {
        delegate.updateShort(columnName, x);
    }

    public void updateString(int columnIndex, String x) throws SQLException {
        delegate.updateString(columnIndex, x);
    }

    public void updateString(String columnName, String x) throws SQLException {
        delegate.updateString(columnName, x);
    }

    public void updateTime(int columnIndex, Time x) throws SQLException {
        delegate.updateTime(columnIndex, x);
    }

    public void updateTime(String columnName, Time x) throws SQLException {
        delegate.updateTime(columnName, x);
    }

    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        delegate.updateTimestamp(columnIndex, x);
    }

    public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
        delegate.updateTimestamp(columnName, x);
    }

    public boolean wasNull() throws SQLException {
        return delegate.wasNull();
    }
}
