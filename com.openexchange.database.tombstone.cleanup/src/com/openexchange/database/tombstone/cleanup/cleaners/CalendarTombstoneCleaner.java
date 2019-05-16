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
 *    trademarks of the OX Software GmbH group of companies.
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

package com.openexchange.database.tombstone.cleanup.cleaners;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;

/**
 * {@link CalendarTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class CalendarTombstoneCleaner extends AbstractTombstoneTableCleaner {

    @Override
    public void checkTables(Connection connection) throws OXException, SQLException {
        boolean tablesExist = Databases.tablesExist(connection, "calendar_attendee_tombstone", "calendar_event_tombstone");
        if (!tablesExist) {
            throw TombstoneCleanupExceptionCode.TABLE_NOT_EXISTS_ERROR.create("calendar_attendee_tombstone, calendar_event_tombstone");
        }
        boolean columnsExist = Databases.columnsExist(connection, "calendar_event_tombstone", "cid", "account", "id", "timestamp");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("calendar_event_tombstone", "cid, account, id, timestamp");
        }
        columnsExist = Databases.columnsExist(connection, "calendar_attendee_tombstone", "cid", "account", "event");
        if (!columnsExist) {
            throw TombstoneCleanupExceptionCode.COLUMN_NOT_EXISTS_ERROR.create("calendar_attendee_tombstone", "cid, account, event");
        }
    }

    @Override
    public Map<String, Integer> cleanupSafe(Connection connection, long timestamp) throws SQLException {
        Map<String, Integer> deletedRowsPerTable = new HashMap<>();

        // Removes entries from both tables where the relation matches
        String deleteEntriesWithConstraints = "DELETE FROM calendar_event_tombstone, calendar_attendee_tombstone USING calendar_event_tombstone INNER JOIN calendar_attendee_tombstone ON calendar_event_tombstone.cid = calendar_attendee_tombstone.cid AND calendar_event_tombstone.account = calendar_attendee_tombstone.account AND calendar_event_tombstone.id = calendar_attendee_tombstone.event WHERE calendar_event_tombstone.timestamp < ?;";
        int rowsDeleted = delete(connection, timestamp, deleteEntriesWithConstraints);
        deletedRowsPerTable.put("calendar_attendee_tombstone", Autoboxing.I(rowsDeleted));

        String delete = "DELETE FROM calendar_event_tombstone WHERE timestamp < ?";
        rowsDeleted += delete(connection, timestamp, delete);
        deletedRowsPerTable.put("calendar_event_tombstone", Autoboxing.I(rowsDeleted));

        return deletedRowsPerTable;
    }
}
