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
 *    trademarks of the OX Software GmbH. group of companies.
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
