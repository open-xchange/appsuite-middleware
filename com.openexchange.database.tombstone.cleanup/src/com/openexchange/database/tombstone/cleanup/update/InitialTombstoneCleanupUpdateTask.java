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

package com.openexchange.database.tombstone.cleanup.update;

import java.sql.SQLException;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * {@link InitialTombstoneCleanupUpdateTask}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class InitialTombstoneCleanupUpdateTask extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(InitialTombstoneCleanupUpdateTask.class);

    private long timespan;

    public InitialTombstoneCleanupUpdateTask(long timespan) {
        this.timespan = timespan;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        long timestamp = System.currentTimeMillis() - timespan;

        try {
            SchemaTombstoneCleanerForUpdateTask schemaCleaner = new SchemaTombstoneCleanerForUpdateTask();
            Map<String, Integer> cleanedTables = schemaCleaner.cleanup(params.getConnection(), timestamp);
            schemaCleaner.logResults(params.getSchema().getSchema(), cleanedTables);
        } catch (SQLException e) {
            LOG.error("Unable to clean up schema.", e);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }
}
