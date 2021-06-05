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

package com.openexchange.file.storage.composition.internal;

import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderAware;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositingIDBasedFileAccess} - The default ID-based file access implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CompositingIDBasedFileAccess extends AbstractCompositingIDBasedFileAccess implements FolderAware {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CompositingIDBasedFileAccess}.
     *
     * @param session The session providing user information
     * @param services The service look-up
     */
    public CompositingIDBasedFileAccess(Session session, ServiceLookup services) {
        super(session);
        this.services = services;
    }

    @Override
    protected FileStorageServiceRegistry getFileStorageServiceRegistry() {
        return services.getService(FileStorageServiceRegistry.class);
    }

    @Override
    protected EventAdmin getEventAdmin() {
        return services.getService(EventAdmin.class);
    }

    @Override
    public FileStoragePermission optOwnPermission(final String id) throws OXException {
        final FileID fileID = new FileID(id);
        final String folderId = fileID.getFolderId();
        if (null == folderId) {
            return null;
        }
        return getFolderAccess(fileID.getService(), fileID.getAccountId()).getFolder(folderId).getOwnPermission();
    }

    @Override
    public FileStorageFolder optFolder(String id) throws OXException {
        final FileID fileID = new FileID(id);
        final String folderId = fileID.getFolderId();
        if (null == folderId) {
            return null;
        }
        return getFolderAccess(fileID.getService(), fileID.getAccountId()).getFolder(folderId);
    }

}