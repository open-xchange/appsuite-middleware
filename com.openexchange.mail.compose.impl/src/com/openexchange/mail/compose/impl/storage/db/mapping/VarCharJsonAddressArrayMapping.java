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
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.java.Strings;
import com.openexchange.mail.compose.Address;

/**
 * {@link VarCharJsonAddressArrayMapping} Maps a varchar column containing a JSON Array of {@link Address}es (also a JSON Array) to a List of {@link Address}es.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public abstract class VarCharJsonAddressArrayMapping<O> extends AbstractVarCharJsonObjectMapping<List<Address>, O> {

    private static final Logger LOG = LoggerFactory.getLogger(VarCharJsonAddressArrayMapping.class);

    public VarCharJsonAddressArrayMapping(String columnLabel, String readableName) {
        super(columnLabel, readableName);
    }

    @Override
    public int set(PreparedStatement statement, int parameterIndex, O object) throws SQLException {
        if (!isSet(object)) {
            statement.setNull(parameterIndex, getSqlType());
            return 1;
        }

        List<Address> value = get(object);
        if (value == null || value.isEmpty()) {
            statement.setNull(parameterIndex, getSqlType());
        } else {
            JSONArray json = new JSONArray();
            for (Address address : value) {
                JSONArray jsonAddress = new JSONArray();
                jsonAddress.put(Strings.isEmpty(address.getPersonal()) ? JSONObject.NULL : address.getPersonal());
                jsonAddress.put(Strings.isEmpty(address.getAddress()) ? JSONObject.NULL : address.getAddress());
                json.put(jsonAddress);
            }
            statement.setString(parameterIndex, json.toString());
        }
        return 1;
    }

    @Override
    public List<Address> get(ResultSet resultSet, String columnLabel) throws SQLException {
        String value = resultSet.getString(columnLabel);
        if (value == null || value.isEmpty()) {
            return null;
        }

        List<Address> retval = new ArrayList<>();
        try {
            JSONArray jsonAddresses = new JSONArray(value);
            for (int i = 0; i < jsonAddresses.length(); i++) {
                JSONArray jsonAddress = (JSONArray) jsonAddresses.get(i);
                retval.add(new Address((String) (jsonAddress.isNull(0) ? null : jsonAddress.get(0)), (String) (jsonAddress.isNull(1) ? null : jsonAddress.get(1))));
            }
        } catch (JSONException | ClassCastException e) {
            LOG.error("Unable to parse {} to a list of addresses", value, e);
        }
        return retval;
    }

}
