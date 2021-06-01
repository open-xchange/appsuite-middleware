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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.Meta;
import com.openexchange.mail.compose.Meta.MetaType;

/**
 * {@link VarCharJsonMetaMapping}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public abstract class VarCharJsonMetaMapping<O> extends AbstractVarCharJsonObjectMapping<Meta, O> {

    private static final Logger LOG = LoggerFactory.getLogger(VarCharJsonMetaMapping.class);

    public VarCharJsonMetaMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (!isSet(object)) {
            statement.setNull(parameterIndex, getSqlType());
            return 1;
        }

        Meta value = get(object);
        if (value == null) {
            statement.setNull(parameterIndex, getSqlType());
        } else {
            JSONObject jsonMeta = new JSONObject(6);
            try {
                Date date = value.getDate();
                jsonMeta.put("date", null == date ? JSONObject.NULL : Long.valueOf(date.getTime()));
                jsonMeta.put("type", value.getType() == null ? JSONObject.NULL : value.getType().getId());
                jsonMeta.put("editFor", getNullable(value.getEditFor()));
                jsonMeta.put("replyFor", getNullable(value.getReplyFor()));
                if (value.getForwardsFor() != null && !value.getForwardsFor().isEmpty()) {
                    JSONArray jsonForwards = new JSONArray();
                    for (MailPath forwardFor : value.getForwardsFor()) {
                        jsonForwards.put(getNullable(forwardFor));
                    }
                    jsonMeta.put("forwardsFor", jsonForwards);
                }
            } catch (JSONException e) {
                LOG.error("Unable to generate JSONObject.", e);
            }
            statement.setString(parameterIndex, jsonMeta.toString());
        }
        return 1;
    }

    @Override
    public Meta get(ResultSet resultSet, String columnLabel) throws SQLException {
        String value = resultSet.getString(columnLabel);
        if (value == null || value.isEmpty()) {
            return null;
        }

        Meta retval = null;
        try {
            JSONObject jsonMeta = new JSONObject(value);

            List<MailPath> forwardsFor = null;
            {
                JSONArray jsonForwards = jsonMeta.optJSONArray("forwardsFor");
                if (null != jsonForwards) {
                    int length = jsonForwards.length();
                    forwardsFor = new ArrayList<>(length);
                    for (int i = 0; i < length; i++) {
                        String mailPathStr = jsonForwards.optString(i, null);
                        if (null != mailPathStr) {
                            forwardsFor.add(new MailPath(mailPathStr));
                        } else {
                            forwardsFor.add(null);
                        }
                    }
                }
            }

            // @formatter:off
            retval = Meta.builder()
                .withDate(!jsonMeta.hasAndNotNull("date")? null : new Date(jsonMeta.getLong("date")))
                .withEditFor(MailPath.getMailPathFor(jsonMeta.optString("editFor", null)))
                .withReplyFor(MailPath.getMailPathFor(jsonMeta.optString("replyFor", null)))
                .withType(MetaType.typeFor(jsonMeta.optString("type", null)))
                .withForwardsFor(forwardsFor)
                .build();
            // @formatter:on
        } catch (JSONException | ClassCastException | NumberFormatException | OXException e) {
            LOG.error("Unable to parse {} to meta information", value, e);
        }
        return retval;
    }

}
