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
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.compose.Attachment;
import com.openexchange.mail.compose.DefaultAttachment;

/**
 * {@link VarCharJsonAttachmentsArrayMapping}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public abstract class VarCharJsonAttachmentsArrayMapping<O> extends AbstractVarCharJsonObjectMapping<List<Attachment>, O> {

    private static final Logger LOG = LoggerFactory.getLogger(VarCharJsonAttachmentsArrayMapping.class);

    public VarCharJsonAttachmentsArrayMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (!isSet(object)) {
            statement.setNull(parameterIndex, getSqlType());
            return 1;
        }

        List<Attachment> value = get(object);
        if (value == null || value.isEmpty()) {
            statement.setNull(parameterIndex, getSqlType());
        } else {
            JSONArray json = new JSONArray(value.size());
            for (Attachment attachment : value) {
                json.put(UUIDs.getUnformattedString(attachment.getId()));
            }
            statement.setString(parameterIndex, json.toString());
        }
        return 1;
    }

    @Override
    public List<Attachment> get(ResultSet resultSet, String columnLabel) throws SQLException {
        String value = resultSet.getString(columnLabel);
        if (value == null || value.isEmpty()) {
            return null;
        }

        List<Attachment> retval = Collections.emptyList();
        try {
            JSONArray jsonAttachments = new JSONArray(value);
            int length = jsonAttachments.length();
            retval = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                UUID attachmentId = UUIDs.fromUnformattedString(jsonAttachments.getString(i));
                retval.add(DefaultAttachment.createWithId(attachmentId, null));
            }
        } catch (JSONException | ClassCastException e) {
            LOG.error("Unable to parse {} to a List of Attachments", value, e);
        }

        return retval;
    }

}
