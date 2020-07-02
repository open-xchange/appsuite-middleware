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
            "SELECT guest_id, uid FROM guest2context where cid = ?",
            (rs) -> guestIds.add(L(rs.getLong(1))),
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
            Databases.getIN("SELECT id, mail_address FROM guest WHERE id IN(", guestIds.size()),
            (rs) -> guestToMailAddress.put(I(rs.getInt(1)), rs.getString(2)),
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
                sql, 
                (rs)-> guestIds.remove(L(Integer.toUnsignedLong(i(entry.getKey())))), // Found an user, so remove from list
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
