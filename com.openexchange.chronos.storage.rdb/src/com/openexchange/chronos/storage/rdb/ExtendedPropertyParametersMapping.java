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

package com.openexchange.chronos.storage.rdb;

import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.java.Streams;

/**
 * {@link ExtendedPropertyParametersMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ExtendedPropertyParametersMapping<O> extends DefaultDbMapping<List<ExtendedPropertyParameter>, O> {

    /**
     * Initializes a new {@link ExtendedPropertyParametersMapping}.
     *
     * @param columnLabel The column label
     * @param readableName The readable name for the mapped field
     */
    protected ExtendedPropertyParametersMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName, Types.BLOB);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        try {
            byte[] data = ExtendedPropertiesCodec.encodeParameters(get(object));
            if (null == data) {
                statement.setNull(parameterIndex, getSqlType());
            } else {
                statement.setBinaryStream(parameterIndex, Streams.newByteArrayInputStream(data), data.length);
            }
            return 1;
        } catch (IOException e) {
            throw new SQLException(e);
        }
    }

    @Override
    public List<ExtendedPropertyParameter> get(ResultSet resultSet, String columnLabel) throws SQLException {
        InputStream inputStream = null;
        try {
            inputStream = resultSet.getBinaryStream(columnLabel);
            return ExtendedPropertiesCodec.decodeParameters(inputStream);
        } catch (IOException e) {
            throw new SQLException(e);
        } finally {
            Streams.close(inputStream);
        }
    }

}
