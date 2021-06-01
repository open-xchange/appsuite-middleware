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

package com.openexchange.tools.oxfolder;

import static com.openexchange.database.Databases.executeUpdate;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.CleanUpExecution;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.exception.OXException;

/**
 * {@link OXFolderPathCleanUp} - The clean up job for the <code>oxfolder_reservedpaths</code> table.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public final class OXFolderPathCleanUp implements CleanUpExecution {

    /**
     * Initializes a new {@link OXFolderPathCleanUp}.
     */
    public OXFolderPathCleanUp() {
        super();
    }

    @Override
    public boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        try {
            /*
             * Check if UpdateTask has run by checking that the table exists in this schema
             */
            return Databases.tableExists(connectionProvider.getConnection(), "oxfolder_reservedpaths");
        } catch (SQLException e) {
            OXFolderPathUniqueness.LOGGER.warn("Unable to look-up \"oxfolder_reservedpaths\" table", e);
        }
        return false;
    }

    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, Map<String, Object> state, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        /*
         * Delete expired entries
         */
        try {
            executeUpdate(connectionProvider.getConnection(),
                "DELETE FROM oxfolder_reservedpaths WHERE expires <= ?",
                s -> s.setLong(1, System.currentTimeMillis() - TimeUnit.HOURS.toMillis(1)));
        } catch (SQLException e) {
            OXFolderPathUniqueness.LOGGER.error("Unable to clean-up \"oxfolder_reservedpaths\" table", e);
        }

    }
}