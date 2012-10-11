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
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import org.apache.commons.logging.Log;

/**
 * {@link JDBC3PreparedStatementWrapper}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public abstract class JDBC3PreparedStatementWrapper extends JDBC3StatementWrapper implements PreparedStatement {

    private final PreparedStatement delegate;

    private static final Log LOG = com.openexchange.log.Log.loggerFor(JDBC3PreparedStatementWrapper.class);

    /**
     * Initializes a new {@link JDBC3PreparedStatementWrapper}.
     *
     * @param delegate The delegate statement
     * @param con The connection returner instance
     */
    public JDBC3PreparedStatementWrapper(final PreparedStatement delegate, final JDBC3ConnectionReturner con) {
        super(delegate, con);
        this.delegate = delegate;
    }

    @Override
    public void addBatch() throws SQLException {
        delegate.addBatch();
    }

    @Override
    public void clearParameters() throws SQLException {
        delegate.clearParameters();
    }

    @Override
    public boolean execute() throws SQLException {
        if(LOG.isDebugEnabled()) {
            LOG.debug(Thread.currentThread()+" executes: "+delegate.toString());
        }
        return delegate.execute();
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        if(LOG.isDebugEnabled()) {
            LOG.debug(Thread.currentThread()+" executes: "+delegate.toString());
        }
        return new JDBC4ResultSetWrapper(delegate.executeQuery(), this);
    }

    @Override
    public int executeUpdate() throws SQLException {
        if(LOG.isDebugEnabled()) {
            LOG.debug(Thread.currentThread()+" executes: "+delegate.toString());
        }
        return delegate.executeUpdate();
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return delegate.getMetaData();
    }

    @Override
    public ParameterMetaData getParameterMetaData() throws SQLException {
        return delegate.getParameterMetaData();
    }

    @Override
    public void setArray(final int i, final Array x) throws SQLException {
        delegate.setArray(i, x);
    }

    @Override
    public void setAsciiStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        delegate.setAsciiStream(parameterIndex, x, length);
    }

    @Override
    public void setBigDecimal(final int parameterIndex, final BigDecimal x) throws SQLException {
        delegate.setBigDecimal(parameterIndex, x);
    }

    @Override
    public void setBinaryStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        delegate.setBinaryStream(parameterIndex, x, length);
    }

    @Override
    public void setBlob(final int i, final Blob x) throws SQLException {
        delegate.setBlob(i, x);
    }

    @Override
    public void setBoolean(final int parameterIndex, final boolean x) throws SQLException {
        delegate.setBoolean(parameterIndex, x);
    }

    @Override
    public void setByte(final int parameterIndex, final byte x) throws SQLException {
        delegate.setByte(parameterIndex, x);
    }

    @Override
    public void setBytes(final int parameterIndex, final byte[] x) throws SQLException {
        delegate.setBytes(parameterIndex, x);
    }

    @Override
    public void setCharacterStream(final int parameterIndex, final Reader reader, final int length) throws SQLException {
        delegate.setCharacterStream(parameterIndex, reader, length);
    }

    @Override
    public void setClob(final int i, final Clob x) throws SQLException {
        delegate.setClob(i, x);
    }

    @Override
    public void setDate(final int parameterIndex, final Date x) throws SQLException {
        delegate.setDate(parameterIndex, x);
    }

    @Override
    public void setDate(final int parameterIndex, final Date x, final Calendar cal) throws SQLException {
        delegate.setDate(parameterIndex, x, cal);
    }

    @Override
    public void setDouble(final int parameterIndex, final double x) throws SQLException {
        delegate.setDouble(parameterIndex, x);
    }

    @Override
    public void setFloat(final int parameterIndex, final float x) throws SQLException {
        delegate.setFloat(parameterIndex, x);
    }

    @Override
    public void setInt(final int parameterIndex, final int x) throws SQLException {
        delegate.setInt(parameterIndex, x);
    }

    @Override
    public void setLong(final int parameterIndex, final long x) throws SQLException {
        delegate.setLong(parameterIndex, x);
    }

    @Override
    public void setNull(final int parameterIndex, final int sqlType) throws SQLException {
        delegate.setNull(parameterIndex, sqlType);
    }

    @Override
    public void setNull(final int paramIndex, final int sqlType, final String typeName) throws SQLException {
        delegate.setNull(paramIndex, sqlType, typeName);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x) throws SQLException {
        delegate.setObject(parameterIndex, x);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType);
    }

    @Override
    public void setObject(final int parameterIndex, final Object x, final int targetSqlType, final int scale) throws SQLException {
        delegate.setObject(parameterIndex, x, targetSqlType, scale);
    }

    @Override
    public void setRef(final int i, final Ref x) throws SQLException {
        delegate.setRef(i, x);
    }

    @Override
    public void setShort(final int parameterIndex, final short x) throws SQLException {
        delegate.setShort(parameterIndex, x);
    }

    @Override
    public void setString(final int parameterIndex, final String x) throws SQLException {
        delegate.setString(parameterIndex, x);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x) throws SQLException {
        delegate.setTime(parameterIndex, x);
    }

    @Override
    public void setTime(final int parameterIndex, final Time x, final Calendar cal) throws SQLException {
        delegate.setTime(parameterIndex, x, cal);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x) throws SQLException {
        delegate.setTimestamp(parameterIndex, x);
    }

    @Override
    public void setTimestamp(final int parameterIndex, final Timestamp x, final Calendar cal) throws SQLException {
        delegate.setTimestamp(parameterIndex, x, cal);
    }

    @Override
    public void setURL(final int parameterIndex, final URL x) throws SQLException {
        delegate.setURL(parameterIndex, x);
    }

    @Override
    @Deprecated
    public void setUnicodeStream(final int parameterIndex, final InputStream x, final int length) throws SQLException {
        delegate.setUnicodeStream(parameterIndex, x, length);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
