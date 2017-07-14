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

package com.openexchange.groupware.tools.mappings.database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import com.openexchange.java.Streams;

/**
 * {@link PointMapping}
 *
 * @param <O> the type of the object
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class PointMapping<O> extends DefaultDbMapping<double[], O> {

    /** The spatial reference system identifier */
    private static final byte[] SRID = { 0, 0, 0, 0 };

    /** The little endian byte order mark */
    private static final byte[] BOM = { 1 };

    /** The WKB type "point" */
    private static final byte[] WKB_TYPE = { 1, 0, 0, 0 };

    /**
     * Initializes a new {@link PointMapping}.
     *
     * @param columnLabel The column label
     * @param readableName The readable name
     */
    public PointMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName, Types.BINARY);
	}

    @Override
    public double[] get(ResultSet resultSet, String columnLabel) throws SQLException {
        byte[] value = resultSet.getBytes(columnLabel);
        if (null == value) {
            return null;
        }
        return new double[] { decode(Arrays.copyOfRange(value, 9, 17)), decode(Arrays.copyOfRange(value, 17, 25)) };
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (isSet(object)) {
            double[] value = get(object);
            if (null != value) {
                try (ByteArrayOutputStream outputStream = Streams.newByteArrayOutputStream(32)) {
                    outputStream.write(SRID);
                    outputStream.write(BOM);
                    outputStream.write(WKB_TYPE);
                    outputStream.write(encode(value[0]));
                    outputStream.write(encode(value[1]));
                    statement.setBytes(parameterIndex, outputStream.toByteArray());
                } catch (IOException e) {
                    throw new SQLException(e);
                }
            } else {
                statement.setNull(parameterIndex, getSqlType());
            }
        } else {
            statement.setNull(parameterIndex, getSqlType());
        }
        return 1;
    }

    private static byte[] encode(double value) {
        long longBits = Double.doubleToLongBits(value);
        return ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).putLong(longBits).array();
    }

    private static double decode(byte[] value) {
        long longBits = ByteBuffer.wrap(value).order(ByteOrder.LITTLE_ENDIAN).getLong();
        return Double.longBitsToDouble(longBits);
    }

}
