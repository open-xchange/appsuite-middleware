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
import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.Lists;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Reference;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link CalendarEventRemoveStaleFolderReferencesTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.3
 */
public class CalendarEventRemoveStaleFolderReferencesTask extends UpdateTaskAdapter {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarEventRemoveStaleFolderReferencesTask.class);

    @Override
    public String[] getDependencies() {
        return new String[] { ChronosCreateTableTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Reference<Boolean> suggestConsistencyTool = new Reference<Boolean>(Boolean.FALSE);
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;
            /*
             * get event identifiers of events per context in public and personal folders that reference a no longer existing folder
             */
            Map<Integer, List<Integer>> eventsIdsToDeleteEventData = getIdsWithStaleFolderReferencesForEventFolder(connection);
            Map<Integer, List<Integer>> eventsIdsToDeleteAttendeeData = getIdsWithStaleFolderReferencesForAttendeeFolder(connection);
            /*
             * for those in personal folders, check which of them appear in at least one other folder that actually exists
             */
            if (false == eventsIdsToDeleteAttendeeData.isEmpty()) {
                /*
                 * check if those events appear in at least one other folder that actually exists
                 */
                Map<Integer, List<Integer>> idsWithValidFolderReferences = getIdsWithValidFolderReferencesForAttendeeFolder(connection, eventsIdsToDeleteAttendeeData);
                for (Entry<Integer, List<Integer>> entry : eventsIdsToDeleteAttendeeData.entrySet()) {
                    List<Integer> validIds = idsWithValidFolderReferences.get(entry.getKey());
                    for (Iterator<Integer> iterator = entry.getValue().iterator(); iterator.hasNext();) {
                        Integer id = iterator.next();
                        if (null == validIds || false == validIds.contains(id)) {
                            /*
                             * no other valid folder reference found, so all event data can be removed
                             */
                            iterator.remove();
                            Collections.put(eventsIdsToDeleteEventData, entry.getKey(), id);
                        }
                    }
                }
            }
            if (false == eventsIdsToDeleteEventData.isEmpty()) {
                /*
                 * delete event data for those without a valid folder reference at all
                 */
                deleteEventData(connection, eventsIdsToDeleteEventData, suggestConsistencyTool);
            }
            if (false == eventsIdsToDeleteAttendeeData.isEmpty()) {
                /*
                 * delete attendee data referencing no longer existing folders
                 */
                deleteAttendeeData(connection, eventsIdsToDeleteAttendeeData);
            }
            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (1 == rollback) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
        /*
         * if attachment metadata was affected, suggest to run the consistency tool later on
         */
        if (Boolean.TRUE.equals(suggestConsistencyTool.getValue())) {
            LOG.info("Attachment metadata was affected, so consider running the consistency tool.");
        }
    }

    private static Map<Integer, List<Integer>> getIdsWithStaleFolderReferencesForEventFolder(Connection connection) throws SQLException {
        Map<Integer, List<Integer>> eventsPerContext = new HashMap<Integer, List<Integer>>();
        String sql = "SELECT e.cid,e.id FROM calendar_event AS e WHERE e.account=0 AND e.folder IS NOT NULL " + 
            "AND NOT EXISTS (SELECT 1 FROM oxfolder_tree as f WHERE e.cid=f.cid AND e.folder=f.fuid);";
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                Collections.put(eventsPerContext, I(resultSet.getInt(1)), I(resultSet.getInt(2)));
            }
        }
        return eventsPerContext;
    }
    
    private static Map<Integer, List<Integer>> getIdsWithStaleFolderReferencesForAttendeeFolder(Connection connection) throws SQLException {
        Map<Integer, List<Integer>> eventsPerContext = new HashMap<Integer, List<Integer>>();
        String sql = "SELECT DISTINCT a.cid,a.event FROM calendar_attendee AS a WHERE a.account=0 AND a.folder IS NOT NULL " + 
            "AND NOT EXISTS (SELECT 1 FROM oxfolder_tree as f WHERE a.cid=f.cid AND a.folder=f.fuid);";
        try (PreparedStatement stmt = connection.prepareStatement(sql); ResultSet resultSet = stmt.executeQuery()) {
            while (resultSet.next()) {
                Collections.put(eventsPerContext, I(resultSet.getInt(1)), I(resultSet.getInt(2)));
            }
        }
        return eventsPerContext;
    }
    
    private static Map<Integer, List<Integer>> getIdsWithValidFolderReferencesForAttendeeFolder(Connection connection, Map<Integer, List<Integer>> idsPerContextToCheck) throws SQLException {
        Map<Integer, List<Integer>> validIdsPerContext = new HashMap<Integer, List<Integer>>();
        for (Entry<Integer, List<Integer>> entry : idsPerContextToCheck.entrySet()) {
            for (List<Integer> chunk : Lists.partition(entry.getValue(), 500)) {
                String sql = new StringBuilder()
                    .append("SELECT DISTINCT a.event FROM calendar_attendee AS a ")
                    .append("WHERE a.cid=? AND a.account=0 AND a.event IN (").append(getParameters(chunk.size())).append(") ")
                    .append("AND a.folder IS NOT NULL AND EXISTS (SELECT 1 FROM oxfolder_tree as f WHERE a.cid=f.cid AND a.folder=f.fuid);")
                .toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, i(entry.getKey()));
                    for (Integer id : chunk) {
                        stmt.setString(parameterIndex++, String.valueOf(id));
                    }
                    try (ResultSet resultSet = stmt.executeQuery()) {
                        while (resultSet.next()) {
                            Collections.put(validIdsPerContext, entry.getKey(), I(resultSet.getInt(1)));
                        }
                    }
                }
            }
        }
        return validIdsPerContext;
    }
    
    private static int deleteEventData(Connection connection, Map<Integer, List<Integer>> eventsPerContext, Reference<Boolean> suggestConsistencyTool) throws SQLException {
        int updated = 0;
        for (Entry<Integer, List<Integer>> entry : eventsPerContext.entrySet()) {
            int cid = i(entry.getKey());
            int affectedRows = 0;
            for (List<Integer> chunk : Lists.partition(entry.getValue(), 500)) {
                /*
                 * delete attachment metadata
                 */
                String sql = new StringBuilder().append("DELETE FROM prg_attachment WHERE cid=? AND module=1 AND attached IN (").append(getParameters(chunk.size())).append(");").toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, cid);
                    for (Integer id : chunk) {
                        stmt.setInt(parameterIndex++, i(id));
                    }
                    int affectedAttachmentRows = stmt.executeUpdate();
                    if (0 < affectedAttachmentRows) {
                        affectedRows += affectedAttachmentRows;
                        suggestConsistencyTool.setValue(Boolean.TRUE);
                    }
                }
                /*
                 * delete alarm triggers
                 */
                sql = new StringBuilder().append("DELETE FROM calendar_alarm_trigger WHERE cid=? AND account=0 AND eventId IN (").append(getParameters(chunk.size())).append(");").toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, cid);
                    for (Integer id : chunk) {
                        stmt.setString(parameterIndex++, String.valueOf(id));
                    }
                    affectedRows += stmt.executeUpdate();
                }
                /*
                 * delete alarms
                 */
                sql = new StringBuilder().append("DELETE FROM calendar_alarm WHERE cid=? AND account=0 AND event IN (").append(getParameters(chunk.size())).append(");").toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, cid);
                    for (Integer id : chunk) {
                        stmt.setString(parameterIndex++, String.valueOf(id));
                    }
                    affectedRows += stmt.executeUpdate();
                }
                /*
                 * delete attendees
                 */
                sql = new StringBuilder().append("DELETE FROM calendar_attendee WHERE cid=? AND account=0 AND event IN (").append(getParameters(chunk.size())).append(");").toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, cid);
                    for (Integer id : chunk) {
                        stmt.setString(parameterIndex++, String.valueOf(id));
                    }
                    affectedRows += stmt.executeUpdate();
                }
                /*
                 * delete events
                 */
                sql = new StringBuilder().append("DELETE FROM calendar_event WHERE cid=? AND account=0 AND id IN (").append(getParameters(chunk.size())).append(");").toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, cid);
                    for (Integer id : chunk) {
                        stmt.setInt(parameterIndex++, i(id));
                    }
                    affectedRows += stmt.executeUpdate();
                }
            }
            LOG.info("Purged data for {} events with stale folder references in context {}, {} rows affected.", I(entry.getValue().size()), entry.getKey(), I(affectedRows));
            updated += affectedRows;
        }
        return updated;
    }
    
    private static int deleteAttendeeData(Connection connection, Map<Integer, List<Integer>> eventsPerContext) throws SQLException {
        int updated = 0;
        for (Entry<Integer, List<Integer>> entry : eventsPerContext.entrySet()) {
            int cid = i(entry.getKey());
            int affectedRows = 0;
            for (List<Integer> chunk : Lists.partition(entry.getValue(), 500)) {
                /*
                 * delete attendees with stale folder references
                 */
                String sql = new StringBuilder()
                    .append("DELETE a FROM calendar_attendee AS a ")
                    .append("WHERE a.cid=? AND a.account=0 AND a.event IN (").append(getParameters(chunk.size())).append(") ")
                    .append("AND a.folder IS NOT NULL AND NOT EXISTS (SELECT 1 FROM oxfolder_tree as f WHERE a.cid=f.cid AND a.folder=f.fuid);")
                .toString();
                try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                    int parameterIndex = 1;
                    stmt.setInt(parameterIndex++, cid);
                    for (Integer id : chunk) {
                        stmt.setString(parameterIndex++, String.valueOf(id));
                    }
                    affectedRows += stmt.executeUpdate();
                }
            }
            LOG.info("Purged attendee data for {} events with stale folder references in context {}, {} rows affected.", I(entry.getValue().size()), entry.getKey(), I(affectedRows));
            updated += affectedRows;
        }
        return updated;
    }

}
