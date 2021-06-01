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
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.mail.compose.Security;

/**
 * {@link VarCharJsonSecurityMapping}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public abstract class VarCharJsonSecurityMapping<O> extends AbstractVarCharJsonObjectMapping<Security, O> {

    private static final Logger LOG = LoggerFactory.getLogger(VarCharJsonSecurityMapping.class);

    public VarCharJsonSecurityMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (!isSet(object)) {
            statement.setNull(parameterIndex, getSqlType());
            return 1;
        }

        Security value = get(object);
        if (value == null) {
            statement.setNull(parameterIndex, getSqlType());
        } else {

            JSONObject jsonSharedAttachmentsInfo = new JSONObject(9);
            try {
                jsonSharedAttachmentsInfo.put("encrypt", value.isEncrypt());
                jsonSharedAttachmentsInfo.put("pgpInline", value.isPgpInline());
                jsonSharedAttachmentsInfo.put("sign", value.isSign());
                jsonSharedAttachmentsInfo.put("language", getNullable(value.getLanguage()));
                jsonSharedAttachmentsInfo.put("message", getNullable(value.getMessage()));
                jsonSharedAttachmentsInfo.put("pin", getNullable(value.getPin()));
                jsonSharedAttachmentsInfo.put("msgRef", getNullable(value.getMsgRef()));
            } catch (JSONException e) {
                LOG.error("Unable to generate JSONObject.", e);
            }
            statement.setString(parameterIndex, jsonSharedAttachmentsInfo.toString());
        }
        return 1;
    }

    @Override
    public Security get(ResultSet resultSet, String columnLabel) throws SQLException {
        String value = resultSet.getString(columnLabel);
        if (value == null || value.isEmpty()) {
            return null;
        }

        Security retval = null;
        try {
            JSONObject jsonSecurity = new JSONObject(value);

            // @formatter:off
            retval = Security.builder()
                .withEncrypt(jsonSecurity.optBoolean("encrypt"))
                .withPgpInline(jsonSecurity.optBoolean("pgpInline"))
                .withSign(jsonSecurity.optBoolean("sign"))
                .withLanguage(jsonSecurity.optString("language", null))
                .withMessage(jsonSecurity.optString("message", null))
                .withPin(jsonSecurity.optString("pin", null))
                .withMsgRef(jsonSecurity.optString("msgRef", null))
                .build();
            // @formatter:on
        } catch (JSONException | ClassCastException | NumberFormatException e) {
            LOG.error("Unable to parse {} to a security settings", value, e);
        }
        return retval;
    }

}
