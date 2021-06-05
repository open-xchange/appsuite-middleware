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

package com.openexchange.mail.compose.impl.storage.db.mapping;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link VarCharJsonCustomHeadersMapping}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public abstract class VarCharJsonCustomHeadersMapping<O> extends AbstractVarCharJsonObjectMapping<Map<String, String>, O> {

    private static final Logger LOG = LoggerFactory.getLogger(VarCharJsonCustomHeadersMapping.class);

    public VarCharJsonCustomHeadersMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (!isSet(object)) {
            statement.setNull(parameterIndex, getSqlType());
            return 1;
        }

        Map<String, String> value = get(object);
        if (value == null) {
            statement.setNull(parameterIndex, getSqlType());
        } else {
            JSONObject jsonMeta = new JSONObject(value.size());
            try {
                for (Map.Entry<String, String> customHeader : value.entrySet()) {
                    jsonMeta.put(customHeader.getKey(), customHeader.getValue());
                }
            } catch (JSONException e) {
                LOG.error("Unable to generate JSONObject.", e);
            }
            statement.setString(parameterIndex, jsonMeta.toString());
        }
        return 1;
    }

    @Override
    public Map<String, String> get(ResultSet resultSet, String columnLabel) throws SQLException {
        String value = resultSet.getString(columnLabel);
        if (value == null || value.isEmpty()) {
            return null;
        }

        Map<String, String> retval = null;
        try {
            JSONObject jsonCustomHeaders = new JSONObject(value);
            retval = new LinkedHashMap<>(jsonCustomHeaders.length());
            for (Map.Entry<String, Object> jsonCustomHeader : jsonCustomHeaders.entrySet()) {
                retval.put(jsonCustomHeader.getKey(), jsonCustomHeader.getValue().toString());
            }
        } catch (JSONException | ClassCastException | NumberFormatException e) {
            LOG.error("Unable to parse {} to custom headers information", value, e);
        }
        return retval;
    }

}
