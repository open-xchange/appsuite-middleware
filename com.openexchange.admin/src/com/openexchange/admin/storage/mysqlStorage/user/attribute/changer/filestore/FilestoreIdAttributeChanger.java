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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.admin.rmi.dataobjects.Filestore;
import com.openexchange.admin.rmi.dataobjects.User;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.storage.interfaces.OXUtilStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.user.AbstractUserAttributeChanger;

/**
 * {@link FilestoreIdAttributeChanger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public class FilestoreIdAttributeChanger extends AbstractUserAttributeChanger {

    /**
     * Initialises a new {@link FilestoreIdAttributeChanger}.
     */
    public FilestoreIdAttributeChanger() {
        super();
    }

    @Override
    public boolean changeAttribute(int userId, int contextId, User userData, Connection connection) throws SQLException {
        Integer filestoreId = userData.getFilestoreId();
        if (filestoreId == null) {
            return false;
        }

        Filestore filestore;
        try {
            OXUtilStorageInterface oxutil = OXUtilStorageInterface.getInstance();
            filestore = oxutil.getFilestore(filestoreId.intValue(), false);
        } catch (StorageException e) {
            // TODO: Throw as SQL?
            return false;
        }

        if (filestore.getId() != null && -1 != filestore.getId().intValue()) {
            try (PreparedStatement prep = connection.prepareStatement("UPDATE user SET filestore_id = ? WHERE cid = ? AND id = ? AND filestore_id <> ?")) {
                prep.setInt(1, filestore.getId().intValue());
                prep.setInt(2, contextId);
                prep.setInt(3, userId);
                prep.setInt(4, filestore.getId().intValue());
                return prep.executeUpdate() > 0;
            }
        }
        return false;
    }
}
