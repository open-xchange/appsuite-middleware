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
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;

/**
 * {@link CompositingIDBasedFolderAccess} - The default ID-based folder access implementation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public class CompositingIDBasedFolderAccess extends AbstractCompositingIDBasedFolderAccess {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link CompositingIDBasedFolderAccess}.
     *
     * @param session The session providing user information
     * @param services The service look-up
     */
    public CompositingIDBasedFolderAccess(Session session, ServiceLookup services) {
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

}