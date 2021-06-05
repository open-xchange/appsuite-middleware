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

package com.openexchange.folderstorage.messaging;

import com.openexchange.folderstorage.BasicPermission;
import com.openexchange.folderstorage.FolderPermissionType;
import com.openexchange.messaging.MessagingPermission;

/**
 * {@link MessagingPermissionImpl} - A messaging folder permission.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingPermissionImpl extends BasicPermission {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -7029036350372452977L;

    /**
     * Initializes an empty {@link MessagingPermissionImpl}.
     */
    public MessagingPermissionImpl() {
        super();
    }

    /**
     * Initializes a new {@link MessagingPermissionImpl} from given messaging permission.
     *
     * @param messagingPermission The messaging permission
     */
    public MessagingPermissionImpl(final MessagingPermission messagingPermission) {
        super();
        admin = messagingPermission.isAdmin();
        deletePermission = messagingPermission.getDeletePermission();
        entity = messagingPermission.getEntity();
        folderPermission = messagingPermission.getFolderPermission();
        group = messagingPermission.isGroup();
        readPermission = messagingPermission.getReadPermission();
        system = messagingPermission.getSystem();
        type = FolderPermissionType.getType(messagingPermission.getType().getTypeNumber());
        legator = null; // ignore
        writePermission = messagingPermission.getWritePermission();
    }
}
