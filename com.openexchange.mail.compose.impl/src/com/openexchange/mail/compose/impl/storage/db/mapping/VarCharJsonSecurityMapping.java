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
