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

package com.openexchange.guest.impl.internal;

import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 *
 * Used to handle connections to the global database transactionally.
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class GlobalDBConnectionHelper extends AbstractConnectionHelper {

    private final int contextId;
    private final String groupId;

    /**
     * Initializes a new {@link GlobalDBConnectionHelper}.
     * 
     * @param services The service lookup
     * @param needsWritable <code>true</code> if connections used by this helper needs to be writable, <code>false</code> for read only
     * @param contextId The context identifier
     */
    public GlobalDBConnectionHelper(ServiceLookup services, boolean needsWritable, int contextId) {
        super(services, needsWritable);
        this.contextId = contextId;
        this.groupId = null;
    }

    /**
     * Initializes a new {@link GlobalDBConnectionHelper}.
     * 
     * @param services The service lookup
     * @param needsWritable <code>true</code> if connections used by this helper needs to be writable, <code>false</code> for read only
     * @param groupId The group identifier
     */
    public GlobalDBConnectionHelper(ServiceLookup services, boolean needsWritable, String groupId) {
        super(services, needsWritable);
        this.contextId = -1;
        this.groupId = groupId;
    }

    /**
     * Backs the underlying connection in case the connection is owned by this instance, rolling back automatically if not yet committed.
     */
    @Override
    public void finish() {
        if (false == committed) {
            Databases.rollback(connection);
        }
        Databases.autocommit(connection);
        if (writableConnection) {
            if (contextId != -1) {
                services.getService(DatabaseService.class).backWritableForGlobal(contextId, connection);
            } else {
                services.getService(DatabaseService.class).backWritableForGlobal(groupId, connection);
            }
        } else {
            if (contextId != -1) {
                services.getService(DatabaseService.class).backReadOnlyForGlobal(contextId, connection);
            } else {
                services.getService(DatabaseService.class).backReadOnlyForGlobal(groupId, connection);
            }
        }
    }

    @Override
    public void acquireConnection() throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        if (contextId != -1) {
            this.connection = this.writableConnection ? dbService.getWritableForGlobal(contextId) : dbService.getReadOnlyForGlobal(contextId);
        } else {
            this.connection = this.writableConnection ? dbService.getWritableForGlobal(groupId) : dbService.getReadOnlyForGlobal(groupId);
        }
    }
}
