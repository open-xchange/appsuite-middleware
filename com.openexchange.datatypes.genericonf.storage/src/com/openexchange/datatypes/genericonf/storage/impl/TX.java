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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;


/**
 * {@link TX}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class TX {
    private Connection connection;
    private final List<PreparedStatement> statements = new LinkedList<PreparedStatement>();

    public abstract Object perform() throws SQLException;

    public void close() {
        for (PreparedStatement stmt : statements) {
            try {
                stmt.close();
            } catch (SQLException x) {
                // IGNORE
            }
        }
        statements.clear();
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return connection;
    }

    public void register(PreparedStatement stmt) {
        statements.add(stmt);
    }

    public PreparedStatement prepare(String sql) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(sql);
        register(stmt);
        return stmt;
    }
}
