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

package com.openexchange.groupware.infostore.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.tx.DBService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.tools.iterator.FolderObjectIterator;
import com.openexchange.groupware.userconfiguration.CapabilityUserConfigurationStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationCodes;
import com.openexchange.server.impl.OCLPermission;
import com.openexchange.tools.oxfolder.OXFolderIteratorSQL;
import com.openexchange.user.User;

public class DelUserFolderDiscoverer extends DBService {

    public DelUserFolderDiscoverer() {
        super();
    }

    public DelUserFolderDiscoverer(final DBProvider provider) {
        super(provider);
    }

    public List<FolderObject> discoverFolders(final int userId, final Context ctx, boolean includeShared) throws OXException {
        final List<FolderObject> discovered = new ArrayList<FolderObject>();
        final User user = UserStorage.getInstance().getUser(userId, ctx);
        int[] accessibleModules;
        try {
            accessibleModules = CapabilityUserConfigurationStorage.loadUserConfiguration(userId, ctx).getAccessibleModules();
        } catch (OXException e) {
            if (!UserConfigurationCodes.NOT_FOUND.equals(e)) {
                throw e;
            }
            accessibleModules = null;
        }

        final Queue<FolderObject> queue = ((FolderObjectIterator) OXFolderIteratorSQL.getAllVisibleFoldersIteratorOfModule(
            userId,
            user.getGroups(),
            accessibleModules,
            FolderObject.INFOSTORE,
            ctx)).asQueue();
        folder: for (final FolderObject fo : queue) {
            if (isVirtual(fo)) {
                continue folder;
            }
            for (final OCLPermission perm : fo.getPermissionsAsArray()) {
                if (!includeShared && someoneElseMayReadInfoitems(perm, userId)) {
                    continue folder;
                }
            }
            discovered.add(fo);
        }

        return discovered;
    }

    private boolean isVirtual(final FolderObject fo) {
        final int id = fo.getObjectID();
        return id == FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID || id == FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID;
    }

    private boolean someoneElseMayReadInfoitems(final OCLPermission perm, final int userId) {
       return (perm.isGroupPermission() || perm.getEntity() != userId) && (perm.canReadAllObjects() || perm.canReadOwnObjects());
    }
}
