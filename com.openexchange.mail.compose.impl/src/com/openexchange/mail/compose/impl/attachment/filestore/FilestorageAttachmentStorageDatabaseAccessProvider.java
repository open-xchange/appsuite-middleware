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

package com.openexchange.mail.compose.impl.attachment.filestore;

import static com.openexchange.mail.compose.impl.attachment.filestore.DedicatedFileStorageAttachmentStorage.extractContextIdFrom;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccess;
import com.openexchange.filestore.DatabaseAccessProvider;
import com.openexchange.filestore.utils.DefaultDatabaseAccess;
import com.openexchange.server.ServiceLookup;

/**
 * {@link FilestorageAttachmentStorageDatabaseAccessProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class FilestorageAttachmentStorageDatabaseAccessProvider implements DatabaseAccessProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link FilestorageAttachmentStorageDatabaseAccessProvider}.
     *
     * @param services The service look-up
     */
    public FilestorageAttachmentStorageDatabaseAccessProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public DatabaseAccess getAccessFor(int fileStorageId, String prefix) throws OXException {
        int contextId = extractContextIdFrom(prefix);
        if (contextId <= 0) {
            return null;
        }

        return new DefaultDatabaseAccess(0, contextId, services.getServiceSafe(DatabaseService.class));
    }

}
