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
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.HashSetValuedHashMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Pair;

/**
 * {@link RemoveOrphanedCalendarAlarmsTask}
 *
 * Removes calendar alarms and triggers from the database of birthday events from deleted users.
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @since v7.10.2
 */
public class RemoveOrphanedCalendarAlarmsTask extends UpdateTaskAdapter {

    private static final String SELECT_USER_CONTACTS = "SELECT cid, intfield01 FROM prg_contacts WHERE fid = 6;";

    private static final String SELECT_TRIGGERS = "SELECT cid, eventId FROM calendar_alarm_trigger WHERE eventId LIKE '6-%';";

    private static final String REMOVE_TRIGGERS = "DELETE FROM calendar_alarm_trigger WHERE cid = ? AND eventId = ?";

    private static final String SELECT_ALARMS = "SELECT cid, event FROM calendar_alarm WHERE event LIKE '6-%';";

    private static final String REMOVE_ALARMS = "DELETE FROM calendar_alarm WHERE cid = ? AND event = ?";

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.chronos.storage.rdb.groupware.ChronosCreateTableTask" };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;
            MultiValuedMap<Integer, Integer> existingContacts = getAllUserContacts(connection);
            removeTriggers(connection, existingContacts);
            removeAlarms(connection, existingContacts);
            connection.commit();
            rollback = 2;
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

    private MultiValuedMap<Integer, Integer> getAllUserContacts(Connection connection) throws SQLException {
        MultiValuedMap<Integer, Integer> retval = new HashSetValuedHashMap<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(SELECT_USER_CONTACTS)) {
            while (rs.next()) {
                retval.put(I(rs.getInt("cid")), I(rs.getInt("intfield01")));
            }
        }
        return retval;
    }

    private void removeTriggers(Connection connection, MultiValuedMap<Integer, Integer> existingContacts) throws SQLException {
        remove(SELECT_TRIGGERS, REMOVE_TRIGGERS, "eventId", connection, existingContacts);
    }

    private void removeAlarms(Connection connection, MultiValuedMap<Integer, Integer> existingContacts) throws SQLException {
        remove(SELECT_ALARMS, REMOVE_ALARMS, "event", connection, existingContacts);
    }

    private void remove(String selectSQL, String removeSQL, String eventColumn, Connection connection, MultiValuedMap<Integer, Integer> existingContacts) throws SQLException {
        Set<Pair<Integer, String>> toRemove = new HashSet<>();
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(selectSQL)) {
            while (rs.next()) {
                String eventId = rs.getString(eventColumn);
                String[] split = Strings.splitBy(eventId, '-', false);
                if (split.length != 2) {
                    continue;
                }
                Integer contactId = null;
                try {
                    contactId = Integer.valueOf(split[1]);
                } catch (NumberFormatException e) {
                    continue;
                }
                if (contactId == null) {
                    continue;
                }
                Integer cid = I(rs.getInt("cid"));
                if (!existingContacts.get(cid).contains(contactId)) {
                    toRemove.add(new Pair<Integer, String>(cid, eventId));
                }
            }
        }

        if (toRemove.isEmpty()) {
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(removeSQL)) {
            for (Pair<Integer, String> entry : toRemove) {
                stmt.setInt(1, i(entry.getFirst()));
                stmt.setString(2, entry.getSecond());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

}
