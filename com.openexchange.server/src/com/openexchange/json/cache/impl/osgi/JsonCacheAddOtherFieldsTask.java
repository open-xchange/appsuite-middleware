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

package com.openexchange.json.cache.impl.osgi;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link JsonCacheAddOtherFieldsTask} - Adds several fields to JSON cache table.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class JsonCacheAddOtherFieldsTask extends UpdateTaskAdapter {

    /**
     * Initializes a new {@link JsonCacheAddOtherFieldsTask}.
     */
    public JsonCacheAddOtherFieldsTask() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { JsonCacheAddInProgressFieldTask.class.getName() };
    }

    @Override
    public void perform(final PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            Tools.checkAndAddColumns(con, "jsonCache", new Column[] { new Column("inProgressSince", "bigint(64) DEFAULT NULL") });
            Tools.checkAndAddColumns(con, "jsonCache", new Column[] { new Column("lastUpdate", "bigint(64) DEFAULT NULL") });
            Tools.checkAndAddColumns(con, "jsonCache", new Column[] { new Column("took", "bigint(64) DEFAULT 0") });
            Tools.checkAndAddColumns(con, "jsonCache", new Column[] { new Column("size", "bigint(64) DEFAULT 0") });

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }
}
