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

package com.openexchange.datatypes.genericonf.storage.impl;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import com.openexchange.datatypes.genericonf.IterationBreak;
import com.openexchange.java.util.UUIDs;

/**
 * {@link InsertIterator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InsertIterator implements MapIterator<String, Object> {

    private static final String INSERT_STRING = "INSERT INTO genconf_attributes_strings (id, cid, name, value, uuid) VALUES (?,?,?,?,?)";
    private static final String INSERT_BOOL = "INSERT INTO genconf_attributes_bools (id, cid, name, value, uuid) VALUES (?,?,?,?,?)";


    private SQLException exception;

    private final Map<Class<?>, PreparedStatement> statementMap = new HashMap<>();

    public void prepareStatements(TX tx) throws SQLException {

        PreparedStatement insertString = tx.prepare(INSERT_STRING);
        PreparedStatement insertBool = tx.prepare(INSERT_BOOL);

        statementMap.put(String.class, insertString);
        statementMap.put(Boolean.class, insertBool);
    }

    public void setIds(int cid, int id) throws SQLException {
        for(PreparedStatement stmt : statementMap.values()) {
            stmt.setInt(1, id);
            stmt.setInt(2, cid);
        }
    }

    @Override
    public void handle(String key, Object value) throws IterationBreak {
        if (exception != null) {
            return;
        }
        if (value == null) {
            return;
        }
        PreparedStatement stmt = statementMap.get(value.getClass());
        if (null == stmt) {
            exception = new SQLException("Unsupported object type: " + value.getClass().getName());
            throw new IterationBreak();
        }
        UUID uuid = UUID.randomUUID();
        byte[] uuidBinary = UUIDs.toByteArray(uuid);
        try {
            stmt.setString(3, key);
            stmt.setObject(4, value);
            stmt.setBytes(5, uuidBinary);
            stmt.executeUpdate();
        } catch (SQLException e) {
            exception = e;
            throw new IterationBreak();
        }

    }

    public void throwException() throws SQLException {
        if (exception != null) {
            throw exception;
        }
    }

    public void close() {
        Collection<PreparedStatement> statements = statementMap.values();
        for (PreparedStatement preparedStatement : statements) {
            try {
                preparedStatement.close();
            } catch (SQLException x) {
                // Ignore
            }
        }
    }

}
