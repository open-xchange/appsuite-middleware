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
import com.openexchange.groupware.tools.mappings.database.DbMapping;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapper;
import com.openexchange.groupware.tools.mappings.database.DefaultDbMapping;
import com.openexchange.groupware.tools.mappings.database.IntegerMapping;
import com.openexchange.groupware.tools.mappings.database.VarCharMapping;
import com.openexchange.java.AsciiReader;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link ShareMapper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ShareMapper extends DefaultDbMapper<RdbShare, ShareField> {

    /**
     * Initializes a new {@link ShareMapper}.
     */
    public ShareMapper() {
        super();
    }

    @Override
    public RdbShare newInstance() {
        return new RdbShare();
    }

    @Override
    public ShareField[] newArray(int size) {
        return new ShareField[size];
    }

    @Override
    protected EnumMap<ShareField, ? extends DbMapping<? extends Object, RdbShare>> createMappings() {

        EnumMap<ShareField, DbMapping<? extends Object, RdbShare>> mappings = new
            EnumMap<ShareField, DbMapping<? extends Object, RdbShare>>(ShareField.class);

//        "cid int(10) unsigned NOT NULL," +
//        "guest int(10) unsigned NOT NULL," +
//        "module tinyint(3) unsigned NOT NULL," +
//        "folder varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
//        "item varchar(255) COLLATE utf8_unicode_ci NOT NULL," +
//        "owner int(10) unsigned NOT NULL," +
//        "expires bigint(64) DEFAULT NULL," +
//        "created bigint(64) NOT NULL," +
//        "created_by int(10) unsigned NOT NULL," +
//        "modified bigint(64) NOT NULL," +
//        "modified_by int(10) unsigned NOT NULL," +
//        "meta BLOB DEFAULT NULL," +

        mappings.put(ShareField.CONTEXT_ID, new IntegerMapping<RdbShare>("cid", "Context ID") {

            @Override
            public void set(RdbShare share, Integer value) {
                share.setCid(value.intValue());
            }

            @Override
            public boolean isSet(RdbShare share) {
                return 0 < share.getCid();
            }

            @Override
            public Integer get(RdbShare share) {
                return Integer.valueOf(share.getCid());
            }

            @Override
            public void remove(RdbShare share) {
                share.setCid(0);
            }
        });
        mappings.put(ShareField.GUEST, new IntegerMapping<RdbShare>("guest", "Guest ID") {

            @Override
            public void set(RdbShare share, Integer value) {
                share.setGuest(value.intValue());
            }

            @Override
            public boolean isSet(RdbShare share) {
                return 0 < share.getGuest();
            }

            @Override
            public Integer get(RdbShare share) {
                return Integer.valueOf(share.getGuest());
            }

            @Override
            public void remove(RdbShare share) {
                share.setGuest(0);
            }
        });
        mappings.put(ShareField.MODULE, new IntegerMapping<RdbShare>("module", "Module ID") {

            @Override
            public void set(RdbShare share, Integer value) {
                share.setModule(value.intValue());
            }

            @Override
            public boolean isSet(RdbShare share) {
                return 0 < share.getModule();
            }

            @Override
            public Integer get(RdbShare share) {
                return Integer.valueOf(share.getModule());
            }

            @Override
            public void remove(RdbShare share) {
                share.setModule(0);
            }
        });
        mappings.put(ShareField.FOLDER, new EmptyVarCharMapping<RdbShare>("folder", "Folder ID") {

            @Override
            public void set(RdbShare share, String value) {
                share.setFolder(value);
            }

            @Override
            public boolean isSet(RdbShare share) {
                return null != share.getFolder();
            }

            @Override
            public String get(RdbShare share) {
                return share.getFolder();
            }

            @Override
            public void remove(RdbShare share) {
                share.setFolder(null);
            }
        });
        mappings.put(ShareField.ITEM, new EmptyVarCharMapping<RdbShare>("item", "Item") {

            @Override
            public void set(RdbShare share, String value) {
                share.setItem(value);
            }

            @Override
            public boolean isSet(RdbShare share) {
                return null != share.getItem();
            }

            @Override
            public String get(RdbShare share) {
                return share.getItem();
            }

            @Override
            public void remove(RdbShare share) {
                share.setItem(null);
            }
        });
        mappings.put(ShareField.OWNER, new IntegerMapping<RdbShare>("owner", "Owned by") {

            @Override
            public void set(RdbShare share, Integer value) {
                share.setOwner(value.intValue());
            }

            @Override
            public boolean isSet(RdbShare share) {
                return 0 < share.getOwner();
            }

            @Override
            public Integer get(RdbShare share) {
                return Integer.valueOf(share.getOwner());
            }

            @Override
            public void remove(RdbShare share) {
                share.setOwner(0);
            }
        });
        mappings.put(ShareField.EXPIRES, new BigIntMapping<RdbShare>("expires", "Expiry date") {

            @Override
            public void set(RdbShare share, Long value) {
                share.setExpires(new Date(value));
            }

            @Override
            public boolean isSet(RdbShare share) {
                return null != share.getExpires();
            }

            @Override
            public Long get(RdbShare share) {
                return share.getExpires().getTime();
            }

            @Override
            public void remove(RdbShare share) {
                share.setExpires(null);
            }
        });
        mappings.put(ShareField.CREATED, new BigIntMapping<RdbShare>("created", "Creation date") {

            @Override
            public void set(RdbShare share, Long value) {
                share.setCreated(new Date(value));
            }

            @Override
            public boolean isSet(RdbShare share) {
                return null != share.getCreated();
            }

            @Override
            public Long get(RdbShare share) {
                return share.getCreated().getTime();
            }

            @Override
            public void remove(RdbShare share) {
                share.setCreated(null);
            }
        });
        mappings.put(ShareField.CREATED_BY, new IntegerMapping<RdbShare>("created_by", "Created by") {

            @Override
            public void set(RdbShare share, Integer value) {
                share.setCreatedBy(value.intValue());
            }

            @Override
            public boolean isSet(RdbShare share) {
                return 0 < share.getCreatedBy();
            }

            @Override
            public Integer get(RdbShare share) {
                return Integer.valueOf(share.getCreatedBy());
            }

            @Override
            public void remove(RdbShare share) {
                share.setCreatedBy(0);
            }
        });
        mappings.put(ShareField.MODIFIED, new BigIntMapping<RdbShare>("modified", "Last modification date") {

            @Override
            public void set(RdbShare share, Long value) {
                share.setModified(new Date(value));
            }

            @Override
            public boolean isSet(RdbShare share) {
                return null != share.getModified();
            }

            @Override
            public Long get(RdbShare share) {
                return share.getModified().getTime();
            }

            @Override
            public void remove(RdbShare share) {
                share.setModified(null);
            }
        });
        mappings.put(ShareField.MODIFIED_BY, new IntegerMapping<RdbShare>("modified_by", "Modified by") {

            @Override
            public void set(RdbShare share, Integer value) {
                share.setModifiedBy(value.intValue());
            }

            @Override
            public boolean isSet(RdbShare share) {
                return 0 < share.getModifiedBy();
            }

            @Override
            public Integer get(RdbShare share) {
                return Integer.valueOf(share.getModifiedBy());
            }

            @Override
            public void remove(RdbShare share) {
                share.setModifiedBy(0);
            }
        });
        mappings.put(ShareField.META, new DefaultDbMapping<Map<String, Object>, RdbShare>("meta", "Meta", java.sql.Types.BLOB) {

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
            public void set(PreparedStatement statement, int parameterIndex, RdbShare share) throws SQLException {
                if (isSet(share)) {
                    Object coerced;
                    try {
                        coerced = JSONCoercion.coerceToJSON(share.getMeta());
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
            public boolean isSet(RdbShare share) {
                return null != share.getMeta() && 0 < share.getMeta().size();
            }

            @Override
            public void set(RdbShare share, Map<String, Object> value) throws OXException {
                share.setMeta(value);
            }

            @Override
            public Map<String, Object> get(RdbShare share) {
                return share.getMeta();
            }

            @Override
            public void remove(RdbShare share) {
                share.setMeta(null);
            }
        });

        return mappings;
    }

    private abstract class EmptyVarCharMapping<O> extends VarCharMapping<O> {

        public EmptyVarCharMapping(String columnName, String readableName) {
            super(columnName, readableName);
        }

        @Override
        public String get(ResultSet resultSet, String columnLabel) throws SQLException {
            String value = resultSet.getString(columnLabel);
            return Strings.isEmpty(value) ? null : value;
        }

        @Override
        public void set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
            if (isSet(object)) {
                String value = get(object);
                statement.setObject(parameterIndex, Strings.isEmpty(value) ? "" : value, getSqlType());
            } else {
                statement.setObject(parameterIndex, "", getSqlType());
            }
        }
    }

}

