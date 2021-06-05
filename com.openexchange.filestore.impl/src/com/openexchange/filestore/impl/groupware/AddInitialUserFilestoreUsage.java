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

package com.openexchange.filestore.impl.groupware;

import static com.openexchange.groupware.update.UpdateConcurrency.BACKGROUND;
import static com.openexchange.groupware.update.WorkingLevel.SCHEMA;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.ProgressState;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.IntReference;

/**
 * Creates an initial empty "filestore_usage" entry for users.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AddInitialUserFilestoreUsage extends UpdateTaskAdapter {

    public AddInitialUserFilestoreUsage() {
        super();
    }

    @Override
    public String[] getDependencies() {
        return new String[] { AddUserColumnToFilestoreUsageTable.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(BACKGROUND, SCHEMA);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        ProgressState state = params.getProgressState();
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            IntReference count = new IntReference();
            Map<Integer, List<Integer>> users = loadUsersInSchema(params.getContextsInSameSchema(), count, con);

            con.setAutoCommit(false);
            rollback = 1;

            state.setTotal(count.getValue());
            int i = 0;
            for (Map.Entry<Integer, List<Integer>> entry : users.entrySet()) {
                int currentContextId = entry.getKey().intValue();
                for (Integer userId : entry.getValue()) {
                    if (isFilestoreUsageMissing(con, currentContextId, userId.intValue())) {
                        addInitialFilestoreUsage(con, currentContextId, userId.intValue());
                    }
                    state.setState(i++);
                }
            }

            con.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback == 1) {
                    Databases.rollback(con);
                }
                Databases.autocommit(con);
            }
        }
    }

    private Map<Integer, List<Integer>> loadUsersInSchema(int[] contextIds, IntReference count, Connection con) throws SQLException {
        int c = 0;
        Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>(contextIds.length);
        for (int cid : contextIds) {
            PreparedStatement stmt = null;
            ResultSet result = null;
            try {
                stmt = con.prepareStatement("SELECT id FROM user WHERE cid=? AND filestore_id > 0");
                stmt.setInt(1, cid);
                result = stmt.executeQuery();

                List<Integer> users = new LinkedList<Integer>();
                while (result.next()) {
                    users.add(Integer.valueOf(result.getInt(1)));
                    c++;
                }

                map.put(Integer.valueOf(cid), users);
            } finally {
                Databases.closeSQLStuff(result, stmt);
            }
        }

        count.setValue(c);
        return map;
    }

    private boolean isFilestoreUsageMissing(Connection con, int contextId, int userId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            stmt = con.prepareStatement("SELECT used FROM filestore_usage WHERE cid=? AND user=?");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            result = stmt.executeQuery();
            return !result.next();
        } finally {
            Databases.closeSQLStuff(result, stmt);
        }
    }

    private void addInitialFilestoreUsage(Connection con, int contextId, int userId) throws SQLException {
        PreparedStatement stmt = null;
        try {
            stmt = con.prepareStatement("INSERT INTO filestore_usage (cid,user,used) VALUES (?,?,0)");
            stmt.setInt(1, contextId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }
}
