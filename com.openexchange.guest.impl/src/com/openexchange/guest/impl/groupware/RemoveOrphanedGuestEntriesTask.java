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

package com.openexchange.guest.impl.groupware;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.guest.GuestExceptionCodes;
import com.openexchange.server.ServiceLookup;

/**
 * {@link RemoveOrphanedGuestEntriesTask} - Removes guest entries in the globalDB that have no corresponding data in the user table.
 * <p>
 * <i>Note</i>:
 * Task is written in case that removing orphaned entries when found in {@link com.openexchange.guest.impl.internal.DefaultGuestService#createUserCopy(String, String, int)}
 * will be insufficient at any time in the future. Currently the UpdateTask <b>is not and must not be executed</b>.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class RemoveOrphanedGuestEntriesTask implements UpdateTaskV2 {

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoveOrphanedGuestEntriesTask.class);
    private final ServiceLookup serviceLookup;

    /**
     * Initializes a new {@link RemoveOrphanedGuestEntriesTask}.
     * 
     * @param serviceLookup The service lookup
     */
    public RemoveOrphanedGuestEntriesTask(ServiceLookup serviceLookup) {
        super();
        this.serviceLookup = serviceLookup;
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        DatabaseService databaseService = serviceLookup.getServiceSafe(DatabaseService.class);
        for (int contextId : params.getContextsInSameSchema()) {
            Connection connection = null;
            boolean manipulated = false;
            try {
                connection = databaseService.getWritableForGlobal(contextId);
                manipulated = removeOrphanedEntries(contextId, connection, params.getConnection());
            } catch (SQLException e) {
                throw GuestExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                if (manipulated) {
                    databaseService.backWritableForGlobal(contextId, connection);
                } else {
                    databaseService.backWritableForGlobalAfterReading(contextId, connection);
                }
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] {};
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes();
    }

    /**
     * Remove orphaned entries in the <code>guest</code> and <code>guest2context</code> table
     *
     * @param contextId The context identifier
     * @param globalConnection The connection to use
     * @return <code>true</code> if data has been manipulated, <code>false</code> otherwise
     * @throws OXException In case of error
     * @throws SQLException In case of SQL error
     */
    private boolean removeOrphanedEntries(int contextId, Connection globalConnection, Connection userConnection) throws OXException, SQLException {
        /*
         * Get all user IDs a guest refers to
         */
        List<Long> guestIds = new ArrayList<>();
        // @formatter:off
        Databases.executeAndConsumeQuery(
            globalConnection,
            (rs) -> guestIds.add(L(rs.getLong(1))),
            "SELECT guest_id, uid FROM guest2context where cid = ?",
            (stmt) -> stmt.setInt(1, contextId));
        // @formatter:on
        if (guestIds.isEmpty()) {
            return false;
        }

        /*
         * Select all possible mail addresses to search for
         */
        HashMap<Integer, String> guestToMailAddress = new HashMap<>();
        // @formatter:off
        Databases.executeAndConsumeQuery(
            globalConnection,
            (rs) -> guestToMailAddress.put(I(rs.getInt(1)), rs.getString(2)),
            Databases.getIN("SELECT id, mail_address FROM guest WHERE id IN(", guestIds.size()),
            (stmt) -> setGuestIds(1, stmt, guestIds));
        // @formatter:on

        /*
         * Search for mail addresses, if found remove from guest from list
         */
        // @formatter:off
        String sql = new StringBuilder("SELECT id FROM user WHERE cid=? AND LOWER(mail) LIKE LOWER(?) COLLATE ")
            .append(Databases.getCharacterSet(userConnection).contains("utf8mb4") ? "utf8mb4_bin" : "utf8_bin")
            .append(" AND guestCreatedBy>0")
            .toString();
        for (Entry<Integer, String> entry : guestToMailAddress.entrySet()) {
            Databases.executeAndConsumeQuery(
                userConnection,
                (rs)-> guestIds.remove(L(Integer.toUnsignedLong(i(entry.getKey())))), // Found an user, so remove from list
                sql, 
                (stmt) -> stmt.setInt(1, contextId),
                (stmt) -> stmt.setString(2, entry.getValue()));
            // @formatter:on
        }
        int manipulated = 0;
        /*
         * Remove orphaned items contained in the list
         */
        if (false == guestIds.isEmpty()) {
            // @formatter:off
            manipulated = Databases.executeUpdate(
                globalConnection,
                Databases.getIN("DELETE FROM guest2context WHERE cid = ? AND guest_id IN (", guestIds.size()),
                (stmt) -> stmt.setInt(1, contextId),
                (stmt) -> setGuestIds(2, stmt, guestIds));
            // @formatter:on

            if (0 == manipulated) {
                LOGGER.warn("No data was removed for table \"guest2context\".");
            }

            // @formatter:off
            manipulated = 0;
            manipulated = Databases.executeUpdate(
                globalConnection,
                Databases.getIN("DELETE FROM guest WHERE id IN (", guestIds.size()),
                (stmt) -> setGuestIds(1, stmt, guestIds));
            // @formatter:on

            if (0 == manipulated) {
                LOGGER.warn("No data was removed for table \"guest\".");
            }
        }
        return manipulated > 0;
    }

    /**
     * Sets the guest IDs to the SQL statement
     *
     * @param beginIndex The index to set the first ID at
     * @param stmt The statement
     * @param guestToUserIds The IDs to set
     * @throws SQLException In case of SQL error
     */
    private void setGuestIds(int beginIndex, PreparedStatement stmt, Collection<Long> ids) throws SQLException {
        int index = beginIndex;
        for (long l : ids) {
            stmt.setLong(index++, l);
        }
    }

}
