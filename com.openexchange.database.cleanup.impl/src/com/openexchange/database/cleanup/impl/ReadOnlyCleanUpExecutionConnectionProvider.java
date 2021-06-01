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

package com.openexchange.database.cleanup.impl;

import java.sql.Connection;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;


/**
 * {@link ReadOnlyCleanUpExecutionConnectionProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public class ReadOnlyCleanUpExecutionConnectionProvider implements CleanUpExecutionConnectionProvider, AutoCloseable {

    private final ServiceLookup services;
    private final int representativeContextId;
    private Connection connection;
    private DatabaseService databaseService;

    /**
     * Initializes a new {@link ReadOnlyCleanUpExecutionConnectionProvider}.
     *
     * @param representativeContextId The identifier of a representative context in that schema
     * @param services The service look-up
     */
    public ReadOnlyCleanUpExecutionConnectionProvider(int representativeContextId, ServiceLookup services) {
        super();
        this.representativeContextId = representativeContextId;
        this.services = services;
    }

    @Override
    public synchronized Connection getConnection() throws OXException {
        Connection connection = this.connection;
        if (connection == null) {
            // Acquire database service
            DatabaseService databaseService = services.getServiceSafe(DatabaseService.class);
            this.databaseService = databaseService;

            // Fetch connection
            connection = databaseService.getReadOnly(representativeContextId);
            this.connection = connection;
        }
        return connection;
    }

    @Override
    public synchronized void close() {
        Connection connection = this.connection;
        if (connection != null) {
            this.connection = null;
            databaseService.backReadOnly(representativeContextId, connection);
        }
    }

}
