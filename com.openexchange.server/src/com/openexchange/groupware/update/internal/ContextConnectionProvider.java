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

package com.openexchange.groupware.update.internal;

import java.sql.Connection;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;


/**
 * {@link ContextConnectionProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public class ContextConnectionProvider extends AbstractConnectionProvider {

    private static class ContextConnectionAccess implements ConnectionAccess {

        private final DatabaseService databaseService;
        private final int contextId;

        ContextConnectionAccess(int contextId) {
            super();
            this.contextId = contextId;
            databaseService = ServerServiceRegistry.getInstance().getService(DatabaseService.class);
        }

        @Override
        public Connection getConnection() throws OXException {
            if (null == databaseService) {
                throw ServiceExceptionCode.absentService(DatabaseService.class);
            }

            return databaseService.getForUpdateTask(contextId);
        }

        @Override
        public void closeConnection(Connection connection) {
            if (null != databaseService && null != connection) {
                databaseService.backForUpdateTask(contextId, connection);
            }
        }

        @Override
        public int[] getContextsInSameSchema() throws OXException {
            if (null == databaseService) {
                throw ServiceExceptionCode.absentService(DatabaseService.class);
            }

            return databaseService.getContextsInSameSchema(contextId);
        }

    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * Initializes a new {@link ContextConnectionProvider}.
     */
    public ContextConnectionProvider(int contextId) {
        super(new ContextConnectionAccess(contextId));
    }

}
