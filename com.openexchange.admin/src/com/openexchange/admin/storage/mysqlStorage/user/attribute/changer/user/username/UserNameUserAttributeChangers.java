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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.username;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.properties.AdminProperties;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.InvalidDataException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractAttributeChangers;
import com.openexchange.admin.tools.AdminCache;

/**
 * {@link UserNameUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class UserNameUserAttributeChangers extends AbstractAttributeChangers {

    private final AdminCache adminCache;
    private static final Logger LOG = LoggerFactory.getLogger(UserNameUserAttributeChangers.class);

    /**
     * Initialises a new {@link UserNameUserAttributeChangers}.
     */
    public UserNameUserAttributeChangers(AdminCache adminCache) {
        super();
        this.adminCache = adminCache;
    }

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        // Updates the username in 'login2user' table only if the if 'USERNAME_CHANGEABLE' is set to 'true'
        if (!adminCache.getProperties().getUserProp(AdminProperties.User.USERNAME_CHANGEABLE, false)) {
            return EMPTY_SET;
        }
        if (userData.getName() == null) {
            return EMPTY_SET;
        }
        if (userData.getName().trim().length() <= 0) {
            return EMPTY_SET;
        }
        if (adminCache.getProperties().getUserProp(AdminProperties.User.CHECK_NOT_ALLOWED_CHARS, true)) {
            try {
                OXToolStorageInterface.getInstance().validateUserName(userData.getName());
            } catch (InvalidDataException e) {
                LOG.error("Error", e);
                throw new StorageException(e);
            }
        }

        if (adminCache.getProperties().getUserProp(AdminProperties.User.AUTO_LOWERCASE, false)) {
            userData.setName(userData.getName().toLowerCase());
        }
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE login2user SET uid=? WHERE cid=? AND id=?")) {
            stmt.setString(1, userData.getName().trim());
            stmt.setInt(2, contextId);
            stmt.setInt(3, userId);
            return stmt.executeUpdate() == 1 ? Collections.singleton("username") : EMPTY_SET;
        } catch (SQLException e) {
            LOG.error("SQL Error", e);
            throw new StorageException(e);
        }
    }
}
