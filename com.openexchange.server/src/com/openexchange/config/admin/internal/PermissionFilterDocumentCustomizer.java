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
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.ObjectPermission;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.tools.iterator.Customizer;

/**
 * {@link PermissionFilterDocumentCustomizer}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class PermissionFilterDocumentCustomizer implements Customizer<DocumentMetadata> {

    private final int adminUserId;

    /**
     * Initializes a new {@link PermissionFilterDocumentCustomizer}.
     *
     * @param adminUserId The user id of the context admin
     */
    public PermissionFilterDocumentCustomizer(int adminUserId) {
        super();
        this.adminUserId = adminUserId;
    }

    @Override
    public DocumentMetadata customize(DocumentMetadata thing) throws OXException {
        if (contains(thing.getObjectPermissions(), adminUserId)) {
            return new PermissionFilterDocumentMetadataImpl(adminUserId, thing);
        }
        return thing;
    }

    private static boolean contains(List<ObjectPermission> permissions, int entity) {
        if (null != permissions) {
            for (ObjectPermission permission : permissions) {
                if (permission.getEntity() == entity) {
                    return true;
                }
            }
        }
        return false;
    }

}
