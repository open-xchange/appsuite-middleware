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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.custom;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.config.Reloadables;
import com.openexchange.database.Databases;
import com.openexchange.java.util.UUIDs;

/**
 * {@link CustomUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class CustomUserAttributeChangers extends AbstractAttributeChangers {

    private static final String INSERT_STATEMENT = "INSERT INTO user_attribute (cid, id, name, value, uuid) VALUES (?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE value=?";
    private static final String DELETE_STATEMENT = "DELETE FROM user_attribute WHERE cid=? AND id=? AND name=?";

    /**
     * Initialises a new {@link CustomUserAttributeChangers}.
     */
    public CustomUserAttributeChangers() {
        super();
    }

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        if (!userData.isUserAttributesset()) {
            return Collections.emptySet();
        }
        PreparedStatement stmtInsertAttribute = null;
        PreparedStatement stmtDeleteAttribute = null;
        Set<String> changedAttributes = new HashSet<>();
        Set<String> changedConfigAttributes = new HashSet<>();
        try {
            for (Map.Entry<String, Map<String, String>> ns : userData.getUserAttributes().entrySet()) {
                String namespace = ns.getKey();
                for (Map.Entry<String, String> pair : ns.getValue().entrySet()) {
                    String name = namespace + "/" + pair.getKey();
                    String value = pair.getValue();
                    if (value == null) {
                        if (null == stmtDeleteAttribute) {
                            stmtDeleteAttribute = prepareStatement(DELETE_STATEMENT, contextId, userId, connection);
                        }
                        stmtDeleteAttribute.setString(3, name);
                        stmtDeleteAttribute.addBatch();
                    } else {
                        if (null == stmtInsertAttribute) {
                            stmtInsertAttribute = prepareStatement(INSERT_STATEMENT, contextId, userId, connection);
                        }
                        stmtInsertAttribute.setString(3, name);
                        stmtInsertAttribute.setString(4, value);
                        stmtInsertAttribute.setBytes(5, UUIDs.toByteArray(UUID.randomUUID()));
                        stmtInsertAttribute.setString(6, value);
                        stmtInsertAttribute.executeUpdate();
                        stmtInsertAttribute.addBatch();
                    }
                    changedAttributes.add(name);
                    if ("config".equals(namespace)) {
                        changedConfigAttributes.add(name);
                    }
                }
            }

            if (null != stmtDeleteAttribute) {
                stmtDeleteAttribute.executeBatch();
            }
            if (null != stmtInsertAttribute) {
                stmtInsertAttribute.executeBatch();
            }
        } catch (SQLException e) {
            throw new StorageException(e);
        } finally {
            Databases.closeSQLStuff(stmtInsertAttribute);
            Databases.closeSQLStuff(stmtDeleteAttribute);
        }
        if (false == changedConfigAttributes.isEmpty()) {
            pendingInvocations.add(() -> Reloadables.propagatePropertyChange(changedConfigAttributes));
        }
        return changedAttributes;
    }

    /**
     * Prepare the specified statement with the context and user identifiers
     *
     * @param sqlStatement The SQL statement to prepare
     * @param contextId The context identifier
     * @param userId The user identifier
     * @param connection The {@link Connection}
     * @return The {@link PreparedStatement}
     * @throws SQLException if an SQL error is occurred
     */
    private PreparedStatement prepareStatement(String sqlStatement, int contextId, int userId, Connection connection) throws SQLException {
        boolean error = true;
        PreparedStatement preparedStatement = null;
        try {
            preparedStatement = connection.prepareStatement(sqlStatement);
            preparedStatement.setInt(1, contextId);
            preparedStatement.setInt(2, userId);
            error = false;
            return preparedStatement;
        } finally {
            if (error) {
                Databases.closeSQLStuff(preparedStatement);
            }
        }
    }
}
