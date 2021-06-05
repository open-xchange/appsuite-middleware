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
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailsetting.UserSettingMailAttributeChangers;

/**
 * {@link AbstractUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractUserAttributeChangers extends AbstractAttributeChangers {

    private static final Logger LOG = LoggerFactory.getLogger(UserSettingMailAttributeChangers.class);

    private final Map<Attribute, UserAttributeChanger> changers;
    private final String table;

    /**
     * Initialises a new {@link AbstractUserAttributeChangers}.
     */
    public AbstractUserAttributeChangers(String table, EnumSet<? extends Attribute> attributes) {
        super(attributes);
        this.table = table;
        changers = initialiseChangers();
    }

    /**
     * Initialises the changers
     *
     * @return a map with the changers
     */
    protected abstract Map<Attribute, UserAttributeChanger> initialiseChangers();

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        Set<String> changedAttributes = new HashSet<>();
        for (Attribute attribute : getAttributes()) {
            if (change(attribute, userData, userId, contextId, connection)) {
                changedAttributes.add(attribute.getName());
            }
        }
        return Collections.unmodifiableSet(changedAttributes);
    }

    /**
     * Changes the specified {@link Attribute}
     *
     * @param attribute The {@link Attribute} to change
     * @param userData The {@link User} data
     * @param userId the user identifier
     * @param contextId The context identifier
     * @param connection The {@link Connection} to use
     * @return <code>true</code> if the attribute was changed successfully; <code>false</code> otherwise
     * @throws StorageException if an SQL error or any other error is occurred
     */
    private boolean change(Attribute attribute, User userData, int userId, int contextId, Connection connection) throws StorageException {
        UserAttributeChanger changer = changers.get(attribute);
        if (changer == null) {
            LOG.debug("No user attribute changer found for user attribute '{}' in table '{}'. The attribute will not be changed.", attribute.getSQLFieldName(), table);
            return false;
        }
        try {
            return changer.changeAttribute(userId, contextId, userData, connection);
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }
}
