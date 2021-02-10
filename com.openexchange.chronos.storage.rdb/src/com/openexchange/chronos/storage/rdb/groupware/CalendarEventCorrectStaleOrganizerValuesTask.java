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
import static com.openexchange.database.Databases.getPlaceholders;
import static com.openexchange.database.Databases.rollback;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;
import com.openexchange.chronos.CalendarUserType;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.Organizer;
import com.openexchange.chronos.ResourceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.storage.rdb.EntityProcessor;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.java.Strings;

/**
 * {@link CalendarEventCorrectStaleOrganizerValuesTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class CalendarEventCorrectStaleOrganizerValuesTask extends UpdateTaskAdapter {

    @Override
    public String[] getDependencies() {
        return new String[] { CalendarEventUnfoldOrganizerValuesTask.class.getName() };
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        int rollback = 0;
        try {
            connection.setAutoCommit(false);
            rollback = 1;
            /*
             * lookup all context-internal references in organizer column distinctively
             */
            Map<Integer, Map<Integer, String>> organizerReferencesPerContext = getOrganizerReferencesPerContext(connection);
            /*
             * check and adjust those references that point to a no longer existing users in the context
             */
            int numUpdated = 0;
            int numStaleOrganizers = 0;
            for (Entry<Integer, Map<Integer, String>> entry : organizerReferencesPerContext.entrySet()) {
                int contextId = i(entry.getKey());
                List<String> staleOrganizerReferences = getStaleOrganizerReferences(connection, contextId, entry.getValue());
                if (0 < staleOrganizerReferences.size()) {
                    numStaleOrganizers += staleOrganizerReferences.size();
                    numUpdated += adjustStaleOrganizerReferences(connection, contextId, staleOrganizerReferences);
                }
            }
            if (0 == numStaleOrganizers) {
                org.slf4j.LoggerFactory.getLogger(CalendarEventCorrectStaleOrganizerValuesTask.class).info(
                    "No stale organizer references in calendar_event found on schema {}.", connection.getCatalog());
            } else {            
                org.slf4j.LoggerFactory.getLogger(CalendarEventCorrectStaleOrganizerValuesTask.class).info(
                    "Successfully updated {} organizer references to {} no longer existing users in calendar_event on schema {}.", I(numUpdated), I(numStaleOrganizers), connection.getCatalog());
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

    private static List<String> getStaleOrganizerReferences(Connection connection, int contextId, Map<Integer, String> organizerReferences) throws SQLException {
        Set<Integer> existingUserIds = new HashSet<Integer>();
        String sql = new StringBuilder("SELECT id FROM user WHERE cid=? AND id").append(getPlaceholders(organizerReferences.size())).append(';').toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextId);
            for (Integer userId : organizerReferences.keySet()) {
                stmt.setInt(parameterIndex++, i(userId));
            }
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    existingUserIds.add(I(resultSet.getInt(1)));
                }
            }
        }
        List<String> staleOrganizerReferences = new ArrayList<String>();
        for (Entry<Integer, String> userIdAndValue : organizerReferences.entrySet()) {
            if (false == existingUserIds.contains(userIdAndValue.getKey())) {
                staleOrganizerReferences.add(userIdAndValue.getValue());
            }
        }
        return staleOrganizerReferences;
    }

    private static int adjustStaleOrganizerReferences(Connection connection, int contextId, List<String> staleOrganizerReferences) throws SQLException, OXException {
        ResourceId adminId = new ResourceId(contextId, getAdminId(connection, contextId), CalendarUserType.INDIVIDUAL);
        String sql = "UPDATE calendar_event SET organizer=? WHERE cid=? AND account=0 AND organizer=?;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(2, contextId);
            for (String value : staleOrganizerReferences) {
                stmt.setString(3, value);
                Organizer organizer = decodeOrganizer(value);
                organizer.setEntity(adminId.getEntity());
                organizer.setUri(adminId.getURI());
                stmt.setString(1, encodeOrganizer(contextId, organizer));
                stmt.addBatch();
            }
            return IntStream.of(stmt.executeBatch()).sum();
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
        throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("Can't lookup admin for context " + contextId);
    }

    private static Map<Integer, Map<Integer, String>> getOrganizerReferencesPerContext(Connection connection) throws SQLException, OXException {
        Map<Integer, Map<Integer, String>> organizerReferencesPerContext = new HashMap<Integer, Map<Integer, String>>();
        String sql = "SELECT DISTINCT organizer FROM calendar_event WHERE ACCOUNT=0;";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = stmt.executeQuery()) {
                while (resultSet.next()) {
                    String value = resultSet.getString(1);
                    if (Strings.isNotEmpty(value)) {
                        Organizer organizer = decodeOrganizer(value);
                        if (null != organizer && 0 < organizer.getEntity() && null != organizer.getUri()) {
                            ResourceId resourceId = ResourceId.parse(organizer.getUri().trim());
                            if (null != resourceId && CalendarUserType.INDIVIDUAL.equals(resourceId.getCalendarUserType())) {
                                Map<Integer, String> map = organizerReferencesPerContext.get(I(resourceId.getContextID()));
                                if (null == map) {
                                    map = new HashMap<Integer, String>();
                                    organizerReferencesPerContext.put(I(resourceId.getContextID()), map);
                                }
                                map.put(I(resourceId.getEntity()), value);
                            }
                        }
                    }
                }
            }
        }
        return organizerReferencesPerContext;
    }

    private static String encodeOrganizer(int contextId, Organizer organizer) throws OXException {
        Event event = new Event();
        event.setOrganizer(organizer);
        return new EntityProcessor(contextId, null).adjustPriorSave(event).getOrganizer().getUri();
    }

    private static Organizer decodeOrganizer(String value) throws OXException {
        Organizer organizer = new Organizer();
        organizer.setUri(value);
        Event event = new Event();
        event.setOrganizer(organizer);
        return new EntityProcessor(0, null).adjustAfterLoad(event).getOrganizer();
    }

}
