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

package com.openexchange.filestore.utils;

import java.sql.Connection;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.DatabaseAccess;
import com.openexchange.filestore.DatabaseTable;

/**
 * {@link DefaultDatabaseAccess} - The default connection access backed by {@link DatabaseService}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class DefaultDatabaseAccess implements DatabaseAccess {

    private final int contextId;
    private final int userId;
    private final DatabaseService databaseService;

    /**
     * Initializes a new {@link DefaultDatabaseAccess}.
     *
     * @param userId The user identifier
     * @param contextId The context identifier
     * @param databaseService The database service
     */
    public DefaultDatabaseAccess(int userId, int contextId, DatabaseService databaseService) {
        super();
        this.contextId = contextId;
        this.userId = userId;
        this.databaseService = databaseService;
    }

    /**
     * Gets the assigned user identifier
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the assigned context identifier
     *
     * @return The context identifier
     */
    public int getContextId() {
        return contextId;
    }

    @Override
    public void createIfAbsent(DatabaseTable... tables) throws OXException {
        // Nothing to do as common update task mechanism is supposed to ensure tables' existence
    }

    @Override
    public Connection acquireReadOnly() throws OXException {
        return databaseService.getReadOnly(contextId);
    }

    @Override
    public void releaseReadOnly(Connection con) {
        if (null != con) {
            databaseService.backReadOnly(contextId, con);
        }
    }

    @Override
    public Connection acquireWritable() throws OXException {
        return databaseService.getWritable(contextId);
    }

    @Override
    public void releaseWritable(Connection con, boolean forReading) {
        if (null != con) {
            if (forReading) {
                databaseService.backWritableAfterReading(contextId, con);
            } else {
                databaseService.backWritable(contextId, con);
            }
        }
    }

}
