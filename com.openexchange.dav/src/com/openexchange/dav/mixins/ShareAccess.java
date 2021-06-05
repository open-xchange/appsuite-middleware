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

package com.openexchange.dav.mixins;

import com.openexchange.dav.DAVProtocol;
import com.openexchange.dav.resources.FolderCollection;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.webdav.protocol.helpers.SingleXMLPropertyMixin;

/**
 * {@link ShareAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.2
 */
public class ShareAccess extends SingleXMLPropertyMixin {

    /**
     * Used to indicate that the resource is not shared.  This is the default, which means that if the DAV:share-access is omitted, this value is implied.
     */
    public static final String NOT_SHARED = "not-shared";

    /**
     * Used to indicate that the resource is owned by the current user and is being shared by them.
     */
    public static final String SHARED_OWNER = "shared-owner";

    /**
     * Used to indicate that the resource is shared, and the current instance is the 'shared instance' which has read-write access.
     */
    public static final String READ_WRITE = "read-write";

    /**
     * Used to indicate that the resource is shared, and the current instance is the 'shared instance', and only read access is provided.
     */
    public static final String READ = "read";

    private final FolderCollection<?> collection;

    /**
     * Initializes a new {@link ShareAccess}.
     *
     * @param collection The collection
     */
    public ShareAccess(FolderCollection<?> collection) {
        super(DAVProtocol.DAV_NS.getURI(), "share-access");
        this.collection = collection;
    }

    @Override
    protected String getValue() {
        UserizedFolder folder = collection.getFolder();
        Permission[] permissions = folder.getPermissions();
        if (null != permissions && 1 < permissions.length) {
            Permission ownPermission = folder.getOwnPermission();
            if (ownPermission.isAdmin()) {
                return SHARED_OWNER;
            } else if (Permission.WRITE_OWN_OBJECTS < ownPermission.getWritePermission()) {
                return READ_WRITE;
            } else {
                return READ;
            }
        }
        return NOT_SHARED;
    }

}
