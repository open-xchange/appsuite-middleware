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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.alias;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.database.Databases;
import com.openexchange.java.Strings;
import com.openexchange.java.util.UUIDs;

/**
 * {@link AliasUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class AliasUserAttributeChangers extends AbstractAttributeChangers {

    /**
     * Initialises a new {@link AliasUserAttributeChangers}.
     */
    public AliasUserAttributeChangers() {
        super();
    }

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        Set<String> aliases = userData.getAliases();
        try {
            if (null == aliases) {
                if (userData.isAliasesset()) {
                    deleteAliases(connection, contextId, userId);
                    return Collections.singleton("removed all user alias addresses");
                }
                return EMPTY_SET;
            }

            Set<String> aliasesToSet = new LinkedHashSet<>(aliases.size());
            for (String alias : aliases) {
                if (Strings.isNotEmpty(alias)) {
                    alias = alias.trim();
                    aliasesToSet.add(alias);
                }
            }
            deleteAliases(connection, contextId, userId);
            setAliases(connection, contextId, userId, aliasesToSet);
            return Collections.singleton("aliases: " + aliasesToSet.toString());
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }

    private static int[] setAliases(Connection connection, int contextId, int userId, Set<String> aliases) throws SQLException {
        if (null == aliases || aliases.isEmpty()) {
            return new int[0];
        }
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("INSERT INTO user_alias (cid,user,alias,uuid) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE alias=?;");
            for (String alias : aliases) {
                stmt.setInt(1, contextId);
                stmt.setInt(2, userId);
                stmt.setString(3, alias);
                stmt.setBytes(4, UUIDs.toByteArray(UUID.randomUUID()));
                stmt.setString(5, alias);
                stmt.addBatch();
            }
            return stmt.executeBatch();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    private static int deleteAliases(Connection connection, int contextId, int userId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = connection.prepareStatement("DELETE FROM user_alias WHERE cid=? AND user=?;");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            return stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

}
