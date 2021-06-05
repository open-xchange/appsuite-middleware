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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.admin.rmi.dataobjects.User;

/**
 * {@link UserAttributeChanger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public interface UserAttributeChanger {

    /**
     * Changes an attribute (the implementation will define which) from the specified {@link User} data using
     * the specified {@link Connection}
     * 
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param userData The {@link User} data
     * @param connection The {@link Connection}
     * @return <code>true</code> if the attribute was successfully changed; <code>false</code> otherwise
     * @throws SQLException if an SQL error is occurred
     */
    boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException;
}
