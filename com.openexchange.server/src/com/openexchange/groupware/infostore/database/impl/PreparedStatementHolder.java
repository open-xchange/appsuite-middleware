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

package com.openexchange.groupware.infostore.database.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


/**
 * {@link PreparedStatementHolder}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class PreparedStatementHolder {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PreparedStatementHolder.class);

    private final Connection writeConnection;
    private final Map<String, PreparedStatement> statements = new HashMap<String, PreparedStatement>();

    public PreparedStatementHolder(Connection writeConnection) {
        this.writeConnection = writeConnection;
    }

    public void execute(String sql, Object...replacements) throws SQLException {
        PreparedStatement statement = get(sql);
        for(int i = 0, size = replacements.length; i < size; i++) {
            statement.setObject(i+1, replacements[i]);
        }
        statement.executeUpdate();
    }

    private PreparedStatement get(String sql) throws SQLException {
        if (statements.containsKey(sql)) {
            return statements.get(sql);
        } else {
            PreparedStatement prepped = writeConnection.prepareStatement(sql);
            statements.put(sql, prepped);
            return prepped;
        }
    }

    public void close() {
        for(PreparedStatement stmt : statements.values()) {
            try {
                stmt.close();
            } catch (SQLException x) {
                LOG.error("", x);
            }
        }
    }

    public Connection getConnection() {
        return writeConnection;
    }


}
