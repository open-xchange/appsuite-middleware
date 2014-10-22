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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.share.storage.mapping;

import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONInputStream;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.tools.mappings.database.BigIntMapping;
import com.openexchange.groupware.tools.mappings.database.BinaryMapping;
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.share.storage.internal.RdbShareTarget;

/**
 * {@link ShareTargetMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareTargetMapper extends DefaultDbMapper<RdbShareTarget, ShareTargetField> {

    /**
     * Initializes a new {@link ShareTargetMapper}.
     */
    public ShareTargetMapper() {
        super();
    }

    @Override
    public RdbShareTarget newInstance() {
        return new RdbShareTarget();
    }

    @Override
    public ShareTargetField[] newArray(int size) {
        return new ShareTargetField[size];
    }

    @Override
    protected EnumMap<ShareTargetField, ? extends DbMapping<? extends Object, RdbShareTarget>> createMappings() {

        EnumMap<ShareTargetField, DbMapping<? extends Object, RdbShareTarget>> mappings = new
            EnumMap<ShareTargetField, DbMapping<? extends Object, RdbShareTarget>>(ShareTargetField.class);

        mappings.put(ShareTargetField.UUID, new BinaryMapping<RdbShareTarget>("uuid", "UUID") {

            @Override
            public void set(RdbShareTarget target, byte[] value) {
                target.setUuid(value);
            }

            @Override
            public boolean isSet(RdbShareTarget target) {
                return null != target.getUuid();
            }

            @Override
            public byte[] get(RdbShareTarget target) {
                return target.getUuid();
            }

            @Override
            public void remove(RdbShareTarget target) {
                target.setUuid(null);
            }
        });
        mappings.put(ShareTargetField.TOKEN, new BinaryMapping<RdbShareTarget>("token", "Token") {

            @Override
            public void set(RdbShareTarget target, byte[] value) {
                target.setToken(UUIDs.getUnformattedString(UUIDs.toUUID(value)));
            }

            @Override
            public boolean isSet(RdbShareTarget target) {
                return null != target.getToken();
            }

            @Override
            public byte[] get(RdbShareTarget target) {
                return UUIDs.toByteArray(UUIDs.fromUnformattedString(target.getToken()));
            }

            @Override
            public void remove(RdbShareTarget target) {
                target.setToken(null);
            }
        });
        mappings.put(ShareTargetField.CONTEXT_ID, new IntegerMapping<RdbShareTarget>("cid", "Context ID") {

            @Override
            public void set(RdbShareTarget target, Integer value) {
                target.setContextID(value.intValue());
            }

            @Override
            public boolean isSet(RdbShareTarget target) {
                return 0 < target.getContextID();
            }

            @Override
            public Integer get(RdbShareTarget target) {
                return Integer.valueOf(target.getContextID());
            }

            @Override
            public void remove(RdbShareTarget target) {
                target.setContextID(0);
            }
        });
        mappings.put(ShareTargetField.MODULE, new IntegerMapping<RdbShareTarget>("module", "Module ID") {

            @Override
            public void set(RdbShareTarget target, Integer value) {
                target.setModule(value.intValue());
            }

            @Override
            public boolean isSet(RdbShareTarget target) {
                return 0 < target.getModule();
            }

            @Override
            public Integer get(RdbShareTarget target) {
                return Integer.valueOf(target.getModule());
            }

            @Override
            public void remove(RdbShareTarget target) {
                target.setModule(0);
            }
        });
        mappings.put(ShareTargetField.FOLDER, new VarCharMapping<RdbShareTarget>("folder", "Folder ID") {

            @Override
            public void set(RdbShareTarget target, String value) {
                target.setFolder(value);
            }

            @Override
            public boolean isSet(RdbShareTarget target) {
                return null != target.getFolder();
            }

            @Override
            public String get(RdbShareTarget target) {
                return target.getFolder();
            }

            @Override
            public void remove(RdbShareTarget target) {
                target.setFolder(null);
            }
        });
        mappings.put(ShareTargetField.ITEM, new VarCharMapping<RdbShareTarget>("item", "Item") {

            @Override
            public void set(RdbShareTarget target, String value) {
                target.setItem(value);
            }

            @Override
            public boolean isSet(RdbShareTarget target) {
                return null != target.getItem();
            }

            @Override
            public String get(RdbShareTarget target) {
                return target.getItem();
            }

            @Override
            public void remove(RdbShareTarget target) {
                target.setItem(null);
            }
        });
        mappings.put(ShareTargetField.EXPIRY_DATE, new BigIntMapping<RdbShareTarget>("expiryDate", "Expiry date") {

            @Override
            public void set(RdbShareTarget target, Long value) {
                target.setExpiryDate(new Date(value));
            }

            @Override
            public boolean isSet(RdbShareTarget target) {
                return null != target.getExpiryDate();
            }

            @Override
            public Long get(RdbShareTarget target) {
                return target.getExpiryDate().getTime();
            }

            @Override
            public void remove(RdbShareTarget target) {
                target.setExpiryDate(null);
            }
        });
        mappings.put(ShareTargetField.META, new DefaultDbMapping<Map<String, Object>, RdbShareTarget>("meta", "Meta", java.sql.Types.BLOB) {

            @Override
            public Map<String, Object> get(ResultSet resultSet, String columnLabel) throws SQLException {
                InputStream inputStream = null;
                try {
                    inputStream = resultSet.getBinaryStream(columnLabel);
                    if (false == resultSet.wasNull() && null != inputStream) {
                        return new JSONObject(new AsciiReader(inputStream)).asMap();
                    }
                } catch (JSONException e) {
                    throw new SQLException(e);
                } finally {
                    Streams.close(inputStream);
                }
                return null;
            }

            @Override
            public void set(PreparedStatement statement, int parameterIndex, RdbShareTarget target) throws SQLException {
                if (isSet(target)) {
                    Object coerced;
                    try {
                        coerced = JSONCoercion.coerceToJSON(target.getMeta());
                    } catch (JSONException e) {
                        throw new SQLException(e);
                    }
                    if (null == coerced || JSONObject.NULL.equals(coerced)) {
                        statement.setNull(parameterIndex, getSqlType());
                    } else {
                        statement.setBinaryStream(parameterIndex, new JSONInputStream((JSONValue) coerced, "US-ASCII"));
                    }
                } else {
                    statement.setNull(parameterIndex, getSqlType());
                }
            }

            @Override
            public boolean isSet(RdbShareTarget target) {
                Map<String, Object> meta = target.getMeta();
                return null != target.getMeta() && 0 < meta.size();
            }

            @Override
            public void set(RdbShareTarget target, Map<String, Object> value) throws OXException {
                target.setMeta(value);
            }

            @Override
            public Map<String, Object> get(RdbShareTarget target) {
                return target.getMeta();
            }

            @Override
            public void remove(RdbShareTarget target) {
                target.setMeta(null);
            }
        });

        return mappings;
    }

}

