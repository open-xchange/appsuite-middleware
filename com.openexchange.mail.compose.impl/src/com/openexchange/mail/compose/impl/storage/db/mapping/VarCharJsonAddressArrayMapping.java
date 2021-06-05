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
