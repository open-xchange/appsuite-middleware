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

import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.messaging.osgi.Services;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.messaging.registry.MessagingServiceRegistry;

/**
 * {@link MessagingFolderType} - The folder type for messaging.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MessagingFolderType implements FolderType {

    private static final MessagingFolderType instance = new MessagingFolderType();

    /**
     * Gets the {@link MessagingFolderType} instance.
     *
     * @return The {@link MessagingFolderType} instance
     */
    public static MessagingFolderType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link MessagingFolderType}.
     */
    private MessagingFolderType() {
        super();
    }

    @Override
    public boolean servesTreeId(final String treeId) {
        return FolderStorage.REAL_TREE_ID.equals(treeId);
    }

    /**
     * The private folder identifier.
     */
    private static final String PRIVATE_FOLDER_ID = String.valueOf(FolderObject.SYSTEM_PRIVATE_FOLDER_ID);

    @Override
    public boolean servesFolderId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        /*
         * <service-id>://<account-id>/<fullname>
         */
        final MessagingFolderIdentifier pfi;
        try {
            pfi = new MessagingFolderIdentifier(folderId);
        } catch (OXException e) {
            // org.slf4j.LoggerFactory.getLogger(MessagingFolderType.class).warn("", e);
            return false;
        }
        /*
         * Check if service exists
         */
        final MessagingServiceRegistry registry = Services.getService(MessagingServiceRegistry.class);
        if (null == registry) {
            return false;
        }
        if (!registry.containsMessagingService(pfi.getServiceId(), -1, -1)) { // FIXME: Please pass user and context
            return false;
        }
        return true;
    }

    @Override
    public boolean servesParentId(final String folderId) {
        if (null == folderId) {
            return false;
        }
        if (PRIVATE_FOLDER_ID.equals(folderId)) {
            return true;
        }
        return servesFolderId(folderId);
    }

}
