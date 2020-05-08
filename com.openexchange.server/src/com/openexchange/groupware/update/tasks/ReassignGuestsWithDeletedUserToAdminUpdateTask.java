/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH. group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
