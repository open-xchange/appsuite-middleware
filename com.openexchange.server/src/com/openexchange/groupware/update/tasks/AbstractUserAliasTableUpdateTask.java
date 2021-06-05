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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.util.UUIDs;

/**
 * {@link AbstractUserAliasTableUpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
abstract class AbstractUserAliasTableUpdateTask extends UpdateTaskAdapter {

    /**
     * Initialises a new {@link AbstractUserAliasTableUpdateTask}.
     */
    public AbstractUserAliasTableUpdateTask() {
        super();
    }

    /**
     * Returns all the aliases that are stored in the 'user_attribute' table
     *
     * @param conn The Connection
     * @return A {@link Set} with all the aliases
     * @throws SQLException If an SQL error is occurred
     */
    protected Set<Alias> getAllAliasesInUserAttributes(Connection conn) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Set<Alias> aliases = new LinkedHashSet<Alias>();
        try {
            stmt = conn.prepareStatement("SELECT cid, id, value, uuid FROM user_attribute WHERE name='alias'");
            rs = stmt.executeQuery();
            if (!rs.next()) {
                return Collections.emptySet();
            }

            int index;
            do {
                index = 0;
                int cid = rs.getInt(++index);
                int uid = rs.getInt(++index);
                String alias = rs.getString(++index);
                byte[] bytes = rs.getBytes(++index);
                UUID uuid = UUIDs.toUUID(bytes);
                aliases.add(new Alias(cid, uid, alias, uuid));
            } while (rs.next());
        } finally {
            closeSQLStuff(stmt);
        }
        return aliases;
    }

    /**
     * Private {@link Alias} DAO class
     */
    class Alias {

        private final int cid;
        private final int userId;
        private final String alias;
        private final int hash;
        private final UUID uuid;

        /**
         * Initializes a new {@link Alias}.
         *
         * @param cid The context identifier
         * @param userId The user identifier
         * @param alias The alias
         * @param uuid The UUID
         */
        Alias(final int cid, final int userId, final String alias, final UUID uuid) {
            this.cid = cid;
            this.userId = userId;
            this.alias = alias;
            this.uuid = uuid;

            final int prime = 31;
            int result = 1;
            result = prime * result + ((alias == null) ? 0 : alias.hashCode());
            result = prime * result + cid;
            result = prime * result + userId;
            result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
            hash = result;
        }

        /**
         * Gets the cid
         *
         * @return The cid
         */
        public int getCid() {
            return cid;
        }

        /**
         * Gets the userId
         *
         * @return The userId
         */
        public int getUserId() {
            return userId;
        }

        /**
         * Gets the alias
         *
         * @return The alias
         */
        public String getAlias() {
            return alias;
        }

        /**
         * Gets the hash
         *
         * @return The hash
         */
        public int getHash() {
            return hash;
        }

        /**
         * Gets the uuid
         *
         * @return The uuid
         */
        public UUID getUuid() {
            return uuid;
        }

        @Override
        public int hashCode() {
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Alias other = (Alias) obj;
            if (!getOuterType().equals(other.getOuterType())) {
                return false;
            }
            if (alias == null) {
                if (other.alias != null) {
                    return false;
                }
            } else if (!alias.equals(other.alias)) {
                return false;
            }
            if (cid != other.cid) {
                return false;
            }
            if (userId != other.userId) {
                return false;
            }
            if (uuid == null) {
                if (other.uuid != null) {
                    return false;
                }
            } else if (!uuid.equals(other.uuid)) {
                return false;
            }
            return true;
        }

        private AbstractUserAliasTableUpdateTask getOuterType() {
            return AbstractUserAliasTableUpdateTask.this;
        }
    }

}
