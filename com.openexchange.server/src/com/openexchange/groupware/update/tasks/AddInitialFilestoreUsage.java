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

package com.openexchange.groupware.update.tasks;

import static com.openexchange.database.Databases.autocommit;
import static com.openexchange.database.Databases.closeSQLStuff;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;

/**
 * Creates an initial empty filestore usage entry for every context that currently did not uploaded anything. The new quota counting
 * filestore implementation requires an existing entry for every context and it does not create it if it is missing. The has better
 * performance.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class AddInitialFilestoreUsage extends UpdateTaskAdapter {

    public AddInitialFilestoreUsage() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        ProgressState state = params.getProgressState();
        final Connection con = params.getConnection();
        int[] contextIDs = params.getContextsInSameSchema();

        int rollback = 0;
        try {
            con.setAutoCommit(false);
            rollback = 1;

            state.setTotal(contextIDs.length);
            for (int i = 0; i < contextIDs.length; i++) {
                if (isFilestoreUsageMissing(con, contextIDs[i])) {
                    addInitialFilestoreUsage(con, contextIDs[i]);
                }
                state.setState(i);
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    rollback(con);
                }
                autocommit(con);
            }
        }
    }

    private boolean isFilestoreUsageMissing(Connection con, int contextID) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=0");
            stmt.setInt(1, contextID);
            result = stmt.executeQuery();
            return !result.next();
        } finally {
            closeSQLStuff(result, stmt);
        }
    }

    private void addInitialFilestoreUsage(Connection con, int contextID) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO filestore_usage (cid,user,used) VALUES (?,0,0)");
            stmt.setInt(1, contextID);
            stmt.executeUpdate();
        } finally {
            closeSQLStuff(stmt);
        }
    }
}
