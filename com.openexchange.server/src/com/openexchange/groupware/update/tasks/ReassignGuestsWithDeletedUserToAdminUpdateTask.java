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

import static com.openexchange.java.Autoboxing.I;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.groupware.update.WorkingLevel;

/**
 * {@link ReassignGuestsWithDeletedUserToAdminUpdateTask}
 *
 * Reassign values of the column 'guestCreatedBy' in table 'user' to the context admin id, if the user referenced in the 'guestCreatedBy' value has been deleted.
 *
 * @author <a href="mailto:anna.ottersbach@open-xchange.com">Anna Ottersbach</a>
 * @since v7.10.4
 */
public class ReassignGuestsWithDeletedUserToAdminUpdateTask implements UpdateTaskV2 {

    private static final Logger LOG = LoggerFactory.getLogger(ReassignGuestsWithDeletedUserToAdminUpdateTask.class);

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection con = params.getConnection();
        int rollback = 0;
        try {
            if (!Databases.tablesExist(con, "user")) {
                // No "user" table. Leave.
                return;
            }

            // Check for orphaned guest users
            List<ContextAndGuestCreatedBy> reslts = determineOrphanedGuestUsers(con);
            if (reslts.isEmpty()) {
                // No orphaned guest users present. Leave.
                return;
            }

            // There are orphaned guest users that need to be reassigned. Start transaction...
            con.setAutoCommit(false);
            rollback = 1;

            updateOrphanedGuestUsers(reslts, con);

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

    private List<ContextAndGuestCreatedBy> determineOrphanedGuestUsers(Connection con) throws SQLException {
        PreparedStatement selectStatement = null;
        ResultSet resultSet = null;
        try {
            //@formatter:off
            selectStatement = con.prepareStatement("SELECT DISTINCT cid, guestCreatedBy FROM user as a WHERE guestCreatedBy > 0 "
                                                        + "AND NOT EXISTS (SELECT id FROM user WHERE cid = a.cid AND id = a.guestCreatedBy)");
            //@formatter:on
            resultSet = selectStatement.executeQuery();
            if (resultSet.next() == false) {
                // No result available
                return Collections.emptyList();
            }

            // Collect results
            List<ContextAndGuestCreatedBy> results = new ArrayList<>();
            do {
                results.add(new ContextAndGuestCreatedBy(resultSet.getInt(1), resultSet.getInt(2)));
            } while (resultSet.next());
            return results;
        } finally {
            Databases.closeSQLStuff(resultSet, selectStatement);
        }
    }

    private void updateOrphanedGuestUsers(List<ContextAndGuestCreatedBy> results, Connection con) throws SQLException {
        PreparedStatement updateStatement = null;
        try {
            updateStatement = con.prepareStatement("UPDATE user AS u JOIN user_setting_admin AS a ON u.cid=a.cid SET u.guestCreatedBy = a.user WHERE u.cid = ? AND guestCreatedBy = ?");
            for (ContextAndGuestCreatedBy contextAndGuestCreatedBy : results) {
                updateStatement.setInt(1, contextAndGuestCreatedBy.contextId);
                updateStatement.setInt(2, contextAndGuestCreatedBy.guestCreatedBy);
                updateStatement.addBatch();
            }
            updateStatement.executeBatch();
            LOG.info("Reassigned {} orphaned guest users to the respective context administrator.", I(results.size()));
        } finally {
            Databases.closeSQLStuff(updateStatement);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { UserAddGuestCreatedByTask.class.getName(), AddGuestCreatedByIndexForUserTable.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING, WorkingLevel.SCHEMA);
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    // Helper class
    private static class ContextAndGuestCreatedBy {

        final int contextId;
        final int guestCreatedBy;

        ContextAndGuestCreatedBy(int contextId, int guestCreatedBy) {
            super();
            this.contextId = contextId;
            this.guestCreatedBy = guestCreatedBy;
        }
    }

}
