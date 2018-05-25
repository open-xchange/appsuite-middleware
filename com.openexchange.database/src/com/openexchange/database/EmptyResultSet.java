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

package com.openexchange.database;

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
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * {@link EmptyResultSet}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class EmptyResultSet implements ResultSet {

    private static final EmptyResultSet INSTANCE = new EmptyResultSet();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static EmptyResultSet getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link EmptyResultSet}.
     */
    private EmptyResultSet() {
        super();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean next() throws SQLException {
        return false;
    }

    @Override
    public void close() throws SQLException {
        // Nothing to do
    }

    @Override
    public boolean wasNull() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public String getString(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean getBoolean(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public byte getByte(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public short getShort(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public int getInt(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public long getLong(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public float getFloat(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public double getDouble(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public byte[] getBytes(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Date getDate(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Time getTime(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Timestamp getTimestamp(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public InputStream getAsciiStream(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public InputStream getUnicodeStream(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public InputStream getBinaryStream(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public String getString(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean getBoolean(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public byte getByte(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public short getShort(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public int getInt(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public long getLong(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public float getFloat(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public double getDouble(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public byte[] getBytes(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Date getDate(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Time getTime(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Timestamp getTimestamp(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public InputStream getAsciiStream(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public InputStream getUnicodeStream(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public InputStream getBinaryStream(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return null;
    }

    @Override
    public void clearWarnings() throws SQLException {
        // Don't care
    }

    @Override
    public String getCursorName() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Object getObject(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Object getObject(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public int findColumn(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Reader getCharacterStream(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Reader getCharacterStream(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean isFirst() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean isLast() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void beforeFirst() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void afterLast() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean first() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean last() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public int getRow() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean absolute(int row) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean relative(int rows) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean previous() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void setFetchDirection(int direction) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public int getFetchDirection() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void setFetchSize(int rows) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public int getFetchSize() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public int getType() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public int getConcurrency() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean rowInserted() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateNull(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateBoolean(int columnIndex, boolean x) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateByte(int columnIndex, byte x) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateShort(int columnIndex, short x) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateInt(int columnIndex, int x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateLong(int columnIndex, long x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateFloat(int columnIndex, float x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateDouble(int columnIndex, double x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateString(int columnIndex, String x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBytes(int columnIndex, byte[] x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateDate(int columnIndex, Date x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateTime(int columnIndex, Time x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateObject(int columnIndex, Object x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNull(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBoolean(String columnLabel, boolean x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateByte(String columnLabel, byte x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateShort(String columnLabel, short x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateInt(String columnLabel, int x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateLong(String columnLabel, long x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateFloat(String columnLabel, float x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateDouble(String columnLabel, double x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateString(String columnLabel, String x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBytes(String columnLabel, byte[] x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateDate(String columnLabel, Date x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateTime(String columnLabel, Time x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateObject(String columnLabel, Object x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void insertRow() throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateRow() throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void deleteRow() throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void refreshRow() throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void moveToInsertRow() throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public Statement getStatement() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Ref getRef(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Blob getBlob(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Clob getClob(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Array getArray(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Ref getRef(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Blob getBlob(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Clob getClob(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Array getArray(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Date getDate(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Date getDate(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Time getTime(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Time getTime(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public URL getURL(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public URL getURL(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateRef(int columnIndex, Ref x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateRef(String columnLabel, Ref x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBlob(int columnIndex, Blob x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBlob(String columnLabel, Blob x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateClob(int columnIndex, Clob x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateClob(String columnLabel, Clob x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateArray(int columnIndex, Array x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateArray(String columnLabel, Array x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public RowId getRowId(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public RowId getRowId(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateRowId(int columnIndex, RowId x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateRowId(String columnLabel, RowId x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public int getHoldability() throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public boolean isClosed() throws SQLException {
        return true;
    }

    @Override
    public void updateNString(int columnIndex, String nString) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNString(String columnLabel, String nString) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public NClob getNClob(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public NClob getNClob(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public SQLXML getSQLXML(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public SQLXML getSQLXML(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public String getNString(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public String getNString(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Reader getNCharacterStream(int columnIndex) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public Reader getNCharacterStream(String columnLabel) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNClob(int columnIndex, Reader reader) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public void updateNClob(String columnLabel, Reader reader) throws SQLException {
        throw new SQLException("Result set is empty");

    }

    @Override
    public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
        throw new SQLException("Result set is empty");
    }

    @Override
    public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
        throw new SQLException("Result set is empty");
    }

}
