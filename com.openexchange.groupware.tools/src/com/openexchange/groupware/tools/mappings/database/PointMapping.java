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
