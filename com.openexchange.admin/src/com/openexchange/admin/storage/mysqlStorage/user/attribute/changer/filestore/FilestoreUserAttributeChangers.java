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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.filestore;

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractUserAttributeChangers;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.UserAttributeChanger;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.AbstractUserAttributeChanger;
import com.openexchange.filestore.FileStorages;

/**
 * {@link FilestoreUserAttributeChangers}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class FilestoreUserAttributeChangers extends AbstractUserAttributeChangers {

    private static final String TABLE = "user";
    private final UserAttributeChanger filestoreIdAttributeChanger;

    /**
     * Initialises a new {@link FilestoreUserAttributeChangers}.
     */
    public FilestoreUserAttributeChangers() {
        super(TABLE, EnumSet.allOf(FilestoreAttribute.class));
        filestoreIdAttributeChanger = new FilestoreIdAttributeChanger();
    }

    @Override
    protected Map<Attribute, UserAttributeChanger> initialiseChangers() {
        Map<Attribute, UserAttributeChanger> changers = new HashMap<>();
        changers.put(FilestoreAttribute.OWNER, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                Integer filestoreOwner = userData.getFilestoreOwner();
                int ownerId = (filestoreOwner != null && -1 != filestoreOwner.intValue()) ? filestoreOwner.intValue() : 0;
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(FilestoreAttribute.OWNER, I(ownerId)), connection);
            }
        });

        changers.put(FilestoreAttribute.OWNER, new AbstractUserAttributeChanger() {

            @Override
            public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
                String filestore_name = userData.getFilestore_name();
                String filestoreName = (filestore_name != null) ? filestore_name : FileStorages.getNameForUser(userData.getId().intValue(), contextId);
                return setAttributes(userId, contextId, TABLE, Collections.singletonMap(FilestoreAttribute.NAME, filestoreName), connection);
            }
        });
        return Collections.unmodifiableMap(changers);
    }

    @Override
    public Set<String> change(User userData, int userId, int contextId, Connection connection, Collection<Runnable> pendingInvocations) throws StorageException {
        try {
            if (filestoreIdAttributeChanger.changeAttribute(userId, contextId, userData, connection)) {
                return super.change(userData, userId, contextId, connection, pendingInvocations);
            }
            return EMPTY_SET;
        } catch (SQLException e) {
            throw new StorageException(e);
        }
    }
}
