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

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * {@link AbstractMultiAttributeChanger} - Base stub class for providing the logic of
 * changing multiple attributes for a user in a context.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractMultiAttributeChanger extends AbstractAttributeChanger implements UserAttributeChanger {

    /**
     * Initialises a new {@link AbstractMultiAttributeChanger}.
     */
    public AbstractMultiAttributeChanger() {
        super();
    }

    @Override
    protected int fillSetStatement(PreparedStatement stmt, Map<Attribute, Setter> setters, Map<Attribute, Object> attributes, int userId, int contextId) throws SQLException {
        int parameterIndex = 1;
        for (Entry<Attribute, Object> entry : attributes.entrySet()) {
            Setter setter = setters.get(entry.getKey());
            if (setter == null) {
                continue;
            }
            setter.set(stmt, entry.getValue(), parameterIndex++);
        }
        return appendContextUser(contextId, userId, stmt, parameterIndex);
    }

    @Override
    protected int fillUnsetStatement(PreparedStatement stmt, Map<Attribute, Unsetter> unsetters, Set<Attribute> attributes, int userId, int contextId) throws SQLException {
        int parameterIndex = 1;
        for (Attribute attribute : attributes) {
            Unsetter unsetter = unsetters.get(attribute);
            if (unsetter == null) {
                continue;
            }
            unsetter.unset(stmt, parameterIndex++);
        }
        return appendContextUser(contextId, userId, stmt, parameterIndex);
    }
}
