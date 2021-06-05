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
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.java.Streams;

/**
 * {@link ExtendedPropertiesMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ExtendedPropertiesMapping<O> extends DefaultDbMapping<ExtendedProperties, O> {

    /**
     * Initializes a new {@link ExtendedPropertiesMapping}.
     *
     * @param columnLabel The column label
     * @param readableName The readable name for the mapped field
     */
    protected ExtendedPropertiesMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName, Types.BLOB);
    }

    @Override
    public void validate(O object) throws OXException {
        ExtendedProperties extendedProperties = get(object);
        if (null == extendedProperties || extendedProperties.isEmpty()) {
            return;
        }
        for (ExtendedProperty property : extendedProperties) {
            validateString(property.getName());
            Object value = property.getValue();
            if (String.class.isInstance(value)) {
                validateString((String) value);
            }
            List<ExtendedPropertyParameter> parameters = property.getParameters();
            if (null != parameters) {
                for (ExtendedPropertyParameter parameter : parameters) {
                    validateString(parameter.getName());
                    validateString(parameter.getValue());
                }
            }
        }
    }

    @Override
    public boolean replaceAll(O object, String regex, String replacement) throws OXException {
        ExtendedProperties extendedProperties = get(object);
        if (null == extendedProperties || extendedProperties.isEmpty()) {
            return false;
        }
        boolean hasReplacedAny = false;
        for (int i = 0; i < extendedProperties.size(); i++) {
            ExtendedProperty property = extendedProperties.get(i);
            String name = property.getName();
            Object value = property.getValue();
            List<ExtendedPropertyParameter> parameters = property.getParameters();
            boolean hasReplaced = false;
            if (null != name) {
                String replaced = name.replaceAll(regex, replacement);
                if (false == name.equals(replaced)) {
                    name = replaced;
                    hasReplaced = true;
                }
            }
            if (null != value && String.class.isInstance(value)) {
                String replaced = ((String) value).replaceAll(regex, replacement);
                if (false == value.equals(replaced)) {
                    value = replaced;
                    hasReplaced = true;
                }
            }
            if (null != parameters && 0 < parameters.size()) {
                for (int j = 0; j < parameters.size(); j++) {
                    String parameterName = parameters.get(j).getName();
                    if (null != parameterName) {
                        String replaced = parameterName.replaceAll(regex, replacement);
                        if (false == parameterName.equals(replaced)) {
                            parameters.set(j, new ExtendedPropertyParameter(replaced, parameters.get(j).getValue()));
                            hasReplaced = true;
                        }
                    }
                    String parameterValue = parameters.get(j).getValue();
                    if (null != parameterValue) {
                        String replaced = parameterValue.replaceAll(regex, replacement);
                        if (false == parameterValue.equals(replaced)) {
                            parameters.set(j, new ExtendedPropertyParameter(parameters.get(j).getName(), replaced));
                            hasReplaced = true;
                        }
                    }
                }
            }
            if (hasReplaced) {
                extendedProperties.set(i, new ExtendedProperty(name, value, parameters));
                hasReplacedAny = true;
            }
        }
        if (hasReplacedAny) {
            set(object, extendedProperties);
        }
        return hasReplacedAny;
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        try {
            byte[] data = ExtendedPropertiesCodec.encode(get(object));
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
    public ExtendedProperties get(ResultSet resultSet, String columnLabel) throws SQLException {
        InputStream inputStream = null;
        try {
            inputStream = resultSet.getBinaryStream(columnLabel);
            return ExtendedPropertiesCodec.decode(inputStream);
        } catch (IOException e) {
            throw new SQLException(e);
        } finally {
            Streams.close(inputStream);
        }
    }

}
