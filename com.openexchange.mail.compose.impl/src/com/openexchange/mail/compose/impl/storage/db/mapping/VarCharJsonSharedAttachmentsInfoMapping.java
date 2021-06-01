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
import java.util.Date;
import java.util.Locale;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.mail.compose.SharedAttachmentsInfo;

/**
 * {@link VarCharJsonSharedAttachmentsInfoMapping}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public abstract class VarCharJsonSharedAttachmentsInfoMapping<O> extends AbstractVarCharJsonObjectMapping<SharedAttachmentsInfo, O> {

    private static final Logger LOG = LoggerFactory.getLogger(VarCharJsonSharedAttachmentsInfoMapping.class);

    public VarCharJsonSharedAttachmentsInfoMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (!isSet(object)) {
            statement.setNull(parameterIndex, getSqlType());
            return 1;
        }

        SharedAttachmentsInfo value = get(object);
        if (value == null) {
            statement.setNull(parameterIndex, getSqlType());
        } else {

            JSONObject jsonSharedAttachmentsInfo = new JSONObject(6);
            try {
                jsonSharedAttachmentsInfo.put("language", getNullable(value.getLanguage()));
                jsonSharedAttachmentsInfo.put("enabled", value.isEnabled());
                jsonSharedAttachmentsInfo.put("autoDelete", value.isAutoDelete());
                Date expiryDate = value.getExpiryDate();
                jsonSharedAttachmentsInfo.put("expiryDate", null == expiryDate ? JSONObject.NULL : Long.valueOf(expiryDate.getTime()));
                jsonSharedAttachmentsInfo.put("password", getNullable(value.getPassword()));
            } catch (JSONException e) {
                LOG.error("Unable to generate JSONObject.", e);
            }
            statement.setString(parameterIndex, jsonSharedAttachmentsInfo.toString());
        }
        return 1;
    }

    @Override
    public SharedAttachmentsInfo get(ResultSet resultSet, String columnLabel) throws SQLException {
        String value = resultSet.getString(columnLabel);
        if (value == null || value.isEmpty()) {
            return null;
        }

        SharedAttachmentsInfo retval = null;
        try {
            JSONObject jsonSharedAttachmentsInfo = new JSONObject(value);

            // @formatter:off
            retval = SharedAttachmentsInfo.builder()
                .withExpiryDate(jsonSharedAttachmentsInfo.hasAndNotNull("expiryDate") ? new Date(jsonSharedAttachmentsInfo.getLong("expiryDate")) : null)
                .withLanguage(jsonSharedAttachmentsInfo.hasAndNotNull("language") ? new Locale(jsonSharedAttachmentsInfo.getString("language")): null)
                .withEnabled(jsonSharedAttachmentsInfo.optBoolean("enabled"))
                .withAutoDelete(jsonSharedAttachmentsInfo.optBoolean("autoDelete"))
                .withPassword(jsonSharedAttachmentsInfo.optString("password", null))
                .build();
            // @formatter:on
        } catch (JSONException | ClassCastException | NumberFormatException e) {
            LOG.error("Unable to parse {} to shared attachments information", value, e);
        }
        return retval;
    }

}
