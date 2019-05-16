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
import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.google.common.collect.Lists;
import com.openexchange.chronos.Event;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;
import com.openexchange.tools.arrays.Collections;

/**
 * {@link CalendarEventCorrectFilenamesTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.1
 */
public class CalendarEventCorrectFilenamesTask extends UpdateTaskAdapter {

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.chronos.storage.rdb.migration.ChronosStorageMigrationTask"
        };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;
            /*
             * get all events with 'filename' per context & correct entries as needed
             */
            for (Map.Entry<Integer, List<Event>> entry : getEventsWithFilename(connection).entrySet()) {
                correctFilenames(connection, i(entry.getKey()), entry.getValue());
            }
            connection.commit();
            rollback = 2;
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw UpdateExceptionCodes.OTHER_PROBLEM.create(e, e.getMessage());
        } finally {
            if (rollback > 0) {
                if (rollback==1) {
                    rollback(connection);
                }
                autocommit(connection);
            }
        }
    }

    private static int correctFilenames(Connection connection, int contextId, List<Event> eventsWithFilename) throws SQLException {
        /*
         * check for redundant filenames & group the remaining events
         */
        Set<String> idsWithRemovableFilenames = new HashSet<String>();
        Map<String, List<Event>> eventsByFilename = new HashMap<String, List<Event>>();
        for (Event event : eventsWithFilename) {
            if (Objects.equals(event.getUid(), event.getFilename())) {
                org.slf4j.LoggerFactory.getLogger(CalendarEventCorrectFilenamesTask.class).debug(
                    "Redundant filename {} in {} in context {}, remembering for cleanup.", event.getFilename(), event, I(contextId));
                idsWithRemovableFilenames.add(event.getId());
                continue;
            }
            if (Strings.isNotEmpty(event.getFilename())) {
                Collections.put(eventsByFilename, event.getFilename(), event);
            }
        }
        /*
         * check for ambiguous filenames (more than one uid per filename)
         */
        for (Map.Entry<String, List<Event>> entry : eventsByFilename.entrySet()) {
            Map<String, List<Event>> eventsByUID = getEventsByUID(entry.getValue());
            if (1 >= eventsByUID.size()) {
                org.slf4j.LoggerFactory.getLogger(CalendarEventCorrectFilenamesTask.class).debug(
                    "Same UID for all events with filename {} in context {}, no action required.", entry.getKey(), I(contextId));
                continue;
            }
            /*
             * preserve filename in "oldest" event group (based on series id), remember identifiers of others for cleanup
             */
            List<List<Event>> eventGroups = sortBySeriesId(eventsByUID.values());
            for (int i = 1; i < eventGroups.size(); i++) {
                for (Event event : eventGroups.get(i)) {
                    org.slf4j.LoggerFactory.getLogger(CalendarEventCorrectFilenamesTask.class).debug(
                        "Ambigious filename {} for {} in context {}, remembering for cleanup.", event.getFilename(), event, I(contextId));
                    idsWithRemovableFilenames.add(event.getId());
                }
            }
        }
        /*
         * remove any redundant or ambiguous filename values
         */
        int updated = 0;
        if (0 < idsWithRemovableFilenames.size()) {
            updated = removeFilenames(connection, contextId, idsWithRemovableFilenames);
            org.slf4j.LoggerFactory.getLogger(CalendarEventCorrectFilenamesTask.class).info(
                "Updated {} events with redundant or ambiguous filenames in context {}.", I(updated), I(contextId));
            idsWithRemovableFilenames.clear();
        }
        /*
         * get remaining events by filename & check for conflicts with uids of other events
         */
        eventsByFilename = getEventsWithFilename(connection, contextId);
        Map<String, List<Event>> eventsWithUid = getEventsWithUid(connection, contextId, eventsByFilename.keySet());
        for (String uid : eventsWithUid.keySet()) {
            for (Event event : eventsByFilename.get(uid)) {
                org.slf4j.LoggerFactory.getLogger(CalendarEventCorrectFilenamesTask.class).debug(
                    "Conflicting filename {} for {} in context {}, remembering for cleanup.", uid, event, I(contextId));
                idsWithRemovableFilenames.add(event.getId());
            }
        }
        if (0 < idsWithRemovableFilenames.size()) {
            int updated2 = removeFilenames(connection, contextId, idsWithRemovableFilenames);
            org.slf4j.LoggerFactory.getLogger(CalendarEventCorrectFilenamesTask.class).info(
                "Updated {} events with conflicting filenames in context {}.", I(updated2), I(contextId));
            updated += updated2;
        }
        return updated;
    }

    private static List<List<Event>> sortBySeriesId(Collection<List<Event>> eventCollections) {
        List<List<Event>> sortedCollections = new ArrayList<List<Event>>(eventCollections);
        java.util.Collections.sort(sortedCollections, (events1, events2) -> {
            String seriesId1 = null != events1 && 0 < events1.size() ? events1.get(0).getSeriesId() : null;
            String seriesId2 = null != events2 && 0 < events2.size() ? events2.get(0).getSeriesId() : null;
            if (null == seriesId1) {
                return null == seriesId2 ? 0 : -1;
            }
            if (null == seriesId2) {
                return 1;
            }
            try {
                return Integer.compare(Integer.parseInt(seriesId1), Integer.parseInt(seriesId2));
            } catch (NumberFormatException e) {
                return seriesId1.compareTo(seriesId2);
            }
        });
        return sortedCollections;
    }

    private static Map<String, List<Event>> getEventsByUID(List<Event> events) {
        Map<String, List<Event>> eventsByUID = new LinkedHashMap<String, List<Event>>();
        for (Event event : events) {
            if (Strings.isNotEmpty(event.getUid())) {
                Collections.put(eventsByUID, event.getUid(), event);
            }
        }
        return eventsByUID;
    }

    private static Map<String, List<Event>> getEventsWithUid(Connection connection, int cid, Collection<String> uids) throws SQLException {
        if (null == uids || uids.isEmpty()) {
            return java.util.Collections.emptyMap();
        }
        String sql = new StringBuilder()
            .append("SELECT id,series,uid,filename FROM calendar_event WHERE cid=? AND uid IN (")
            .append(getParameters(uids.size())).append(");")
        .toString();
        Map<String, List<Event>> eventsPerUid = new HashMap<String, List<Event>>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            for (String uid : uids) {
                stmt.setString(parameterIndex++, uid);
            }
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    Event event = new Event();
                    event.setId(resultSet.getString("id"));
                    event.setSeriesId(resultSet.getString("series"));
                    event.setUid(resultSet.getString("uid"));
                    event.setFilename(resultSet.getString("filename"));
                    Collections.put(eventsPerUid, event.getUid(), event);
                }
            }
        }
        return eventsPerUid;
    }

    private static Map<String, List<Event>> getEventsWithFilename(Connection connection, int cid) throws SQLException {
        Map<String, List<Event>> eventsPerFilename = new HashMap<String, List<Event>>();
        String sql = "SELECT id,series,uid,filename FROM calendar_event WHERE cid=? AND account=0 AND filename IS NOT NULL;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, cid);
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    Event event = new Event();
                    event.setId(resultSet.getString("id"));
                    event.setSeriesId(resultSet.getString("series"));
                    event.setUid(resultSet.getString("uid"));
                    event.setFilename(resultSet.getString("filename"));
                    Collections.put(eventsPerFilename, event.getFilename(), event);
                }
            }
        }
        return eventsPerFilename;
    }

    private static Map<Integer, List<Event>> getEventsWithFilename(Connection connection) throws SQLException {
        Map<Integer, List<Event>> eventsPerContext = new HashMap<Integer, List<Event>>();
        String sql = "SELECT cid,id,series,uid,filename FROM calendar_event WHERE account=0 AND filename IS NOT NULL;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    Event event = new Event();
                    event.setId(resultSet.getString("id"));
                    event.setSeriesId(resultSet.getString("series"));
                    event.setUid(resultSet.getString("uid"));
                    event.setFilename(resultSet.getString("filename"));
                    Collections.put(eventsPerContext, I(resultSet.getInt("cid")), event);
                }
            }
        }
        return eventsPerContext;
    }

    private static int removeFilenames(Connection connection, int cid, Collection<String> ids) throws SQLException {
        if (null == ids || ids.isEmpty()) {
            return 0;
        }
        int updated = 0;
        for (List<String> chunk : Lists.partition(new ArrayList<String>(ids), 500)) {
            String sql = new StringBuilder().append("UPDATE calendar_event SET filename=NULL WHERE cid=? AND account=0 AND id IN (").append(getParameters(chunk.size())).append(");").toString();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                int parameterIndex = 1;
                stmt.setInt(parameterIndex++, cid);
                for (String id : chunk) {
                    stmt.setString(parameterIndex++, id);
                }
                updated += stmt.executeUpdate();
            }
        }
        return updated;
    }

}
