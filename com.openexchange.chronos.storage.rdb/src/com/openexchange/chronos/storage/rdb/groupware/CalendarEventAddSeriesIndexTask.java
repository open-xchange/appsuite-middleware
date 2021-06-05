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

package com.openexchange.chronos.storage.rdb.groupware;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.rollback;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Tools;

/**
 * {@link CalendarEventAddSeriesIndexTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarEventAddSeriesIndexTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link CalendarEventAddSeriesIndexTask}.
     */
    public CalendarEventAddSeriesIndexTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { ChronosCreateTableTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            String[] indexColumns = new String[] { "cid", "account", "series" };

            // Check if there is no such index
            List<String> tables = new ArrayList<>(Arrays.asList("calendar_event", "calendar_event_tombstone"));
            for (Iterator<String> it = tables.iterator(); it.hasNext(); ) {
                if (null != Tools.existsIndex(connection, it.next(), indexColumns)) {
                    // Such an index already exists
                    it.remove();
                }
            }

            // Check if there is any index, which needs to be added
            int size = tables.size();
            if (size > 0) {
                connection.setAutoCommit(false);
                rollback = 1;

                ProgressState progressState = params.getProgressState();
                progressState.setTotal(size);
                for (String tableName : tables) {
                    Tools.createIndex(connection, tableName, "series", indexColumns, false);
                    progressState.incrementState();
                }

                connection.commit();
                rollback = 2;
            }
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
    }

}
