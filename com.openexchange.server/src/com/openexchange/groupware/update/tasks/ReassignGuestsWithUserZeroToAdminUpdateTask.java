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
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import static org.slf4j.LoggerFactory.getLogger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.impl.ContextExceptionCodes;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * {@link ReassignGuestsWithUserZeroToAdminUpdateTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v8.0.0
 */
public class ReassignGuestsWithUserZeroToAdminUpdateTask implements UpdateTaskV2 {

    @Override
    public String[] getDependencies() {
        return new String[] { ReassignGuestsWithDeletedUserToAdminUpdateTask.class.getName() };
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING);
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            /*
             * lookup all guest users that have their guestCreatedBy set to 0
             */
            Map<Integer, List<Integer>> guestsCreatedByZeroPerContext = getGuestsCreatedByZeroPerContext(connection);
            if (guestsCreatedByZeroPerContext.isEmpty()) {
                getLogger(ReassignGuestsWithUserZeroToAdminUpdateTask.class).info("No guest users with invalid 'guestCreatedBy' found on schema {}.", connection.getCatalog());
                return;
            }
            rollback = 1;
            /*
             * correct the found guest entries by setting the guestCreatedBy value to the context admin id
             */
            for (Entry<Integer, List<Integer>> entry : guestsCreatedByZeroPerContext.entrySet()) {
                int updated = setGuestCreatedByToContextAdmin(connection, i(entry.getKey()), entry.getValue());
                getLogger(ReassignGuestsWithUserZeroToAdminUpdateTask.class).info("Successfully updated {} guest users with invalid 'guestCreatedBy' references in context {}.", I(updated), entry.getKey());
            }
            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (0 < rollback) {
                if (1 == rollback) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
    }

    private static int setGuestCreatedByToContextAdmin(Connection connection, int contextId, List<Integer> guestIds) throws SQLException, OXException {
        int adminId = getAdminId(connection, contextId);
        String sql = "UPDATE user SET guestCreatedBy=? WHERE cid=? AND id" + Databases.getPlaceholders(guestIds.size()) + ';';
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, adminId);
            stmt.setInt(parameterIndex++, contextId);
            for (Integer guestId : guestIds) {
                stmt.setInt(parameterIndex++, i(guestId));
            }
            return stmt.executeUpdate();
        }
    }

    private static int getAdminId(Connection connection, int contextId) throws SQLException, OXException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT user FROM user_setting_admin WHERE cid=?;")) {
            stmt.setInt(1, contextId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1);
                }
            }
        }
        throw ContextExceptionCodes.NO_MAILADMIN.create();
    }

    private static Map<Integer, List<Integer>> getGuestsCreatedByZeroPerContext(Connection connection) throws SQLException {
        Map<Integer, List<Integer>> guestsCreatedByZeroPerContext = new HashMap<Integer, List<Integer>>();
        String sql = "SELECT u.cid,u.id FROM user AS u LEFT JOIN user_attribute AS ua ON u.cid = ua.cid AND u.id=ua.id " + 
            "WHERE u.guestCreatedBy=0 AND ua.name='com.openexchange.shareBaseToken';";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    com.openexchange.tools.arrays.Collections.put(guestsCreatedByZeroPerContext, I(resultSet.getInt(1)), I(resultSet.getInt(2)));
                }
            }
        }
        return guestsCreatedByZeroPerContext;
    }

}
