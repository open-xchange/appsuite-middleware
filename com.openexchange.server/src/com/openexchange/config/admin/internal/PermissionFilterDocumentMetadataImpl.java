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

import java.util.List;
import java.util.stream.Collectors;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;

/**
 *
 * {@link PermissionFilterDocumentMetadataImpl} overrides {@link DocumentMetadataImpl} to filter out administrators permission
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class PermissionFilterDocumentMetadataImpl extends DocumentMetadataImpl implements DocumentMetadata {

    private static final long serialVersionUID = -1093207200136520302L;

    private final int adminUserId;

    /**
     * Initializes a new {@link PermissionFilterDocumentMetadataImpl} from specified folder.
     *
     * @param adminUserId The user id of the context admin
     * @param userizedFolder The requested origin {@link UserizedFolder}
     */
    public PermissionFilterDocumentMetadataImpl(int adminUserId, DocumentMetadata documentMetadata) {
        super(documentMetadata);
        this.adminUserId = adminUserId;
    }

    /**
     * {@inheritDoc}
     *
     * The returned {@link ObjectPermission}s will not contain one for the administrator even she actually has got {@link ObjectPermission}s for the related document. So this implementation should be used to view permissions only.
     */
    @Override
    public List<ObjectPermission> getObjectPermissions() {
        List<ObjectPermission> lObjectPermissions = super.getObjectPermissions();
        if (lObjectPermissions == null || lObjectPermissions.isEmpty()) {
            return lObjectPermissions;
        }
        return lObjectPermissions.stream().filter(x -> x.getEntity() != this.adminUserId).collect(Collectors.toList());
    }
}
