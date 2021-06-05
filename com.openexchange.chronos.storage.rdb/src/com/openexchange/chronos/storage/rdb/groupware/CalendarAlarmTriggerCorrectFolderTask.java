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
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link CalendarAlarmTriggerCorrectFolderTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.2
 */
public class CalendarAlarmTriggerCorrectFolderTask extends UpdateTaskAdapter {

    @Override
    public String[] getDependencies() {
        return new String[] { ChronosCreateTableTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;
            /*
             * gather parent folder references that need to be corrected within calendar_alarm_trigger
             */
            Map<Integer, List<Entry<Integer, String>>> folderReferencesPerContext = new HashMap<Integer, List<Entry<Integer, String>>>();  
            String sql = "SELECT t.cid,t.alarm,a.folder FROM calendar_alarm_trigger as t LEFT JOIN calendar_attendee AS a " + 
                "ON t.cid=a.cid AND t.account=a.account AND t.user=a.entity AND t.eventId=a.event WHERE t.account=0 AND a.folder>0 AND t.folder<>a.folder;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                try (ResultSet resultSet = stmt.executeQuery()) {
                    while (resultSet.next()) {
                        Integer cid = I(resultSet.getInt(1));                        
                        Entry<Integer, String> alarmAndFolder = new SimpleEntry<Integer, String>(I(resultSet.getInt(2)), resultSet.getString(3));
                        Collections.put(folderReferencesPerContext, cid, alarmAndFolder);
                    }
                }
            }
            if (folderReferencesPerContext.isEmpty()) {
                org.slf4j.LoggerFactory.getLogger(CalendarAlarmTriggerCorrectFolderTask.class).info(
                    "No wrong folder references in calendar_alarm_trigger found on schema {}.", connection.getCatalog());
                return;
            }
            /*
             * correct all folder references in calendar_alarm_trigger
             */
            sql = "UPDATE calendar_alarm_trigger SET folder=? WHERE cid=? AND account=0 AND alarm=?;";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (Entry<Integer, List<Entry<Integer, String>>> entry : folderReferencesPerContext.entrySet()) {
                    stmt.setInt(2, i(entry.getKey()));
                    for (Entry<Integer, String> alarmAndFolder : entry.getValue()) {
                        stmt.setString(1, alarmAndFolder.getValue());
                        stmt.setInt(3, i(alarmAndFolder.getKey()));
                        stmt.addBatch();
                    }                    
                }
                int[] updated = stmt.executeBatch();
                org.slf4j.LoggerFactory.getLogger(CalendarAlarmTriggerCorrectFolderTask.class).info(
                    "Corrected {} wrong folder references in calendar_alarm_trigger on schema {}.", I(updated.length), connection.getCatalog());
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

}
