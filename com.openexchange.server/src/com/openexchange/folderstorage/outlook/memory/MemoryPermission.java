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

package com.openexchange.folderstorage.outlook.memory;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.FolderPermissionType;

/**
 * {@link MemoryPermission} - A mail folder permission.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MemoryPermission extends BasicPermission {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -4886238734505728866L;

    /**
     * Initializes a {@link MemoryPermission} from specified {@link ResultSet}'s currently select row:<br>
     * <code>SELECT entity, fp, orp, owp, odp, adminFlag, groupFlag, system, type, sharedParentFolder FROM virtualPermission ...</code>
     *
     * @throws SQLException If reading from result set fails
     */
    public MemoryPermission(final ResultSet rs) throws SQLException {
        super();
        entity = rs.getInt(1);
        folderPermission = rs.getInt(2);
        readPermission = rs.getInt(3);
        writePermission = rs.getInt(4);
        deletePermission = rs.getInt(5);
        admin = rs.getInt(6) > 0;
        group = rs.getInt(7) > 0;
        system = rs.getInt(8);
        type = FolderPermissionType.getType(rs.getInt(9));
        legator = rs.getString(10);
    }

}
