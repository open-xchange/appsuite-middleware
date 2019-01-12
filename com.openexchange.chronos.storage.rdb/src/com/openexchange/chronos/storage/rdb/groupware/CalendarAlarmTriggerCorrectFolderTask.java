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
