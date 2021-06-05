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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.java.Strings;

/**
 * {@link VarCharListMapping}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class VarCharListMapping<O> extends DefaultDbMapping<List<String>, O> {

    /**
     * Initializes a new {@link VarCharListMapping}.
     *
     * @param columnLabel The label of the column holding the value
     * @param readableName The readable name for the mapped field
     */
    protected VarCharListMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName, Types.VARCHAR);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (false == isSet(object)) {
            statement.setNull(parameterIndex, getSqlType());
            return 1;
        }
        List<String> value = get(object);
        if (null == value || value.isEmpty()) {
            statement.setNull(parameterIndex, getSqlType());
        } else {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(value.get(0));
            for (int i = 1; i < value.size(); i++) {
                //TODO: escape
                stringBuilder.append(',').append(value.get(i));
            }
            statement.setString(parameterIndex, stringBuilder.toString());
        }
        return 1;
    }

    @Override
    public List<String> get(ResultSet resultSet, String columnLabel) throws SQLException {
        String value = resultSet.getString(columnLabel);
        if (null == value || value.isEmpty()) {
            return null;
        }
        //TODO unescape
        String[] splitted = Strings.splitByComma(resultSet.getString(columnLabel));
        return null != splitted ? Arrays.asList(splitted) : null;
    }

}
