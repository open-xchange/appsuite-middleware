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

package com.openexchange.gdpr.dataexport.impl;

import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccess;
import com.openexchange.filestore.DatabaseAccessProvider;
import com.openexchange.filestore.utils.DefaultDatabaseAccess;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DataExportDatabaseAccessProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class DataExportDatabaseAccessProvider implements DatabaseAccessProvider {

    private final ServiceLookup services;

    /**
     * Initializes a new {@link DataExportDatabaseAccessProvider}.
     *
     * @param services The service look-up
     */
    public DataExportDatabaseAccessProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public DatabaseAccess getAccessFor(int fileStorageId, String prefix) throws OXException {
        int contextId = DataExportUtility.extractContextIdFrom(prefix);
        if (contextId <= 0) {
            return null;
        }

        return new DefaultDatabaseAccess(0, contextId, services.getServiceSafe(DatabaseService.class));
    }

}
