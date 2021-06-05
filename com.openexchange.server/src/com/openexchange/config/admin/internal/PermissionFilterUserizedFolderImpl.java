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

package com.openexchange.config.admin.internal;

import java.util.Arrays;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.UserizedFolderImpl;

/**
 * {@link PermissionFilterUserizedFolderImpl} overrides {@link UserizedFolderImpl} to filter out administrators permission
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class PermissionFilterUserizedFolderImpl extends UserizedFolderImpl implements UserizedFolder {

    private static final long serialVersionUID = 3941871306938103932L;
    private final int adminUserId;

    /**
     * Initializes a new {@link PermissionFilterUserizedFolderImpl} from specified folder.
     * 
     * @param adminUserId The user id of the context admin
     * @param userizedFolder The requested origin {@link UserizedFolder}
     *
     * @throws IllegalArgumentException If folder is <code>null</code>
     */
    public PermissionFilterUserizedFolderImpl(int adminUserId, UserizedFolder userizedFolder) {
        super(userizedFolder);
        this.adminUserId = adminUserId;
    }

    /**
     * {@inheritDoc}
     * 
     * The returned {@link Permission}s will not contain one for the administrator even she actually has got {@link Permission}s for the given {@link UserizedFolder}. So this implementation should be used to view permissions only.
     */
    @Override
    public Permission[] getPermissions() {
        Permission[] permissions = super.getPermissions();
        if (permissions == null || permissions.length == 0) {
            return permissions;
        }
        return Arrays.stream(permissions).filter(x -> x.getEntity() != adminUserId).toArray(Permission[]::new);
    }
}
