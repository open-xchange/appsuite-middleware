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

package com.openexchange.chronos.storage.rdb;

import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Attachment;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.ExtendedProperties;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.ExtendedPropertyParameter;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAlarmStorage extends RdbStorage implements AlarmStorage {

    private static final int INSERT_CHUNK_SIZE = 200;
    private static final int DELETE_CHUNK_SIZE = 200;
    private static final AlarmMapper MAPPER = AlarmMapper.getInstance();

    private final int accountId;
    private final EntityProcessor entityProcessor;

    /**
     * Initializes a new {@link RdbAlarmStorage}.
     *
     * @param context The context
     * @param accountId The account identifier
     * @param entityResolver The entity resolver to use, or <code>null</code> if not available
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbAlarmStorage(Context context, int accountId, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
        this.accountId = accountId;
        this.entityProcessor = new EntityProcessor(context.getContextId(), entityResolver);
    }

    @Override
    public int nextId() throws OXException {
        int value;
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            value = nextId(connection, accountId, "calendar_alarm_sequence");
            updated = 1;
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
        return value;
    }

    @Override
    public List<Alarm> loadAlarms(Event event, int userID) throws OXException {
        return loadAlarms(Collections.singletonList(event), userID).get(event.getId());
    }

    @Override
    public Map<Integer, List<Alarm>> loadAlarms(Event event) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAlarms(connection, context.getContextId(), accountId, event.getId(), AlarmField.values());
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Map<String, List<Alarm>> loadAlarms(List<Event> events, int userId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAlarms(connection, context.getContextId(), accountId, userId, getObjectIDs(events), AlarmField.values());
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Map<String, Map<Integer, List<Alarm>>> loadAlarms(List<Event> events) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAlarms(connection, context.getContextId(), accountId, getObjectIDs(events), AlarmField.values());
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void insertAlarms(Event event, int userId, List<Alarm> alarms) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            for (Alarm alarm : alarms) {
                updated += insertAlarm(connection, context.getContextId(), accountId, event.getId(), userId, alarm);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void insertAlarms(Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            Map<String, Map<Integer, List<Alarm>>> chunk = new HashMap<String, Map<Integer, List<Alarm>>>();
            int chunkSize = 0;
            for (Entry<String, Map<Integer, List<Alarm>>> entry : alarmsByUserByEventId.entrySet()) {
                /*
                 * add to current chunk
                 */
                chunk.put(entry.getKey(), entry.getValue());
                for (List<Alarm> alarms : entry.getValue().values()) {
                    chunkSize += alarms.size();
                }
                if (chunkSize >= INSERT_CHUNK_SIZE) {
                    /*
                     * insert & reset current chunk
                     */
                    updated += insertAlarms(connection, context.getContextId(), accountId, chunk);
                    chunk.clear();
                    chunkSize = 0;
                }
            }
            /*
             * finally insert remaining chunk
             */
            if (0 < chunkSize) {
                updated += insertAlarms(connection, context.getContextId(), accountId, chunk);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void insertAlarms(Event event, Map<Integer, List<Alarm>> alarmsByUserId) throws OXException {
        Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId = Collections.singletonMap(event.getId(), alarmsByUserId);
        insertAlarms(alarmsByUserByEventId);
    }

    @Override
    public void updateAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            for (Alarm alarm : alarms) {
                updated += updateAlarm(connection, context.getContextId(), accountId, alarm.getId(), alarm, event.getId());
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAlarms(String eventID, int userID) throws OXException {
        deleteAlarms(eventID, new int[] { userID });
    }

    @Override
    public void deleteAlarms(String eventId, int[] userIds) throws OXException {
        if (null == userIds || 0 == userIds.length) {
            return;
        }
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAlarms(connection, context.getContextId(), accountId, eventId, userIds);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAlarms(String eventId) throws OXException {
        deleteAlarms(Collections.singletonList(eventId));
    }

    @Override
    public void deleteAlarms(List<String> eventIds) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            for (List<String> chunk : Lists.partition(eventIds, DELETE_CHUNK_SIZE)) {
                updated += deleteAlarms(connection, context.getContextId(), accountId, chunk);
            }
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAlarms(String eventId, int userId, int[] alarmIds) throws OXException {
        if (null == alarmIds || 0 == alarmIds.length) {
            return;
        }
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAlarms(connection, context.getContextId(), accountId, alarmIds);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAlarms(int userId) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAlarms(connection, context.getContextId(), accountId, userId);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public boolean deleteAllAlarms() throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAlarms(connection, context.getContextId(), accountId);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
        return 0 < updated;
    }

    @Override
    public long getLatestTimestamp(int userId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            try (PreparedStatement stmt = connection.prepareStatement("SELECT MAX(timestamp) FROM calendar_alarm WHERE cid = ? AND user = ? AND account = ?")) {
                int parameterIndex = 1;
                stmt.setInt(parameterIndex++, context.getContextId());
                stmt.setInt(parameterIndex++, userId);
                stmt.setInt(parameterIndex++, accountId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public long getLatestTimestamp(String eventId, int userId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            try (PreparedStatement stmt = connection.prepareStatement("SELECT MAX(timestamp) FROM calendar_alarm WHERE cid = ? AND event = ? AND user = ? AND account = ?")) {
                int parameterIndex = 1;
                stmt.setInt(parameterIndex++, context.getContextId());
                stmt.setString(parameterIndex++, eventId);
                stmt.setInt(parameterIndex++, userId);
                stmt.setInt(parameterIndex++, accountId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
            return 0;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Map<String, Long> getLatestTimestamp(List<String> eventIds, int userId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            StringBuilder sql = new StringBuilder("SELECT event, MAX(timestamp) FROM calendar_alarm WHERE cid = ? AND user = ? AND account = ? AND event");
            sql.append(Databases.getPlaceholders(eventIds.size())).append(" GROUP BY event;");
            try (PreparedStatement stmt = connection.prepareStatement(sql.toString())) {
                int parameterIndex = 1;
                stmt.setInt(parameterIndex++, context.getContextId());
                stmt.setInt(parameterIndex++, userId);
                stmt.setInt(parameterIndex++, accountId);
                for (String eventId : eventIds) {
                    stmt.setString(parameterIndex++, eventId);
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    Map<String, Long> retval = new HashMap<>();
                    while (rs.next()) {
                        retval.put(rs.getString("event"), rs.getLong("MAX(timestamp)"));
                    }
                    return retval;
                }
            }
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    private int insertAlarm(Connection connection, int cid, int account, String eventId, int userId, Alarm alarm) throws OXException {
        AlarmField[] mappedFields = MAPPER.getMappedFields();
        String sql = new StringBuilder()
            .append("INSERT INTO calendar_alarm (cid,account,event,user,")
            .append(MAPPER.getColumns(mappedFields)).append(") ")
            .append("VALUES (?,?,?,?,").append(MAPPER.getParameters(mappedFields)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            stmt.setString(parameterIndex++, eventId);
            stmt.setInt(parameterIndex++, userId);
            parameterIndex = MAPPER.setParameters(stmt, parameterIndex, adjustPriorSave(eventId, alarm), mappedFields);
            return logExecuteUpdate(stmt);
        } catch (SQLException e) {
            throw asOXException(e, MAPPER, alarm, connection, "calendar_alarm");
        }
    }

    private int insertAlarms(Connection connection, int cid, int account, Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId) throws SQLException, OXException {
        if (null == alarmsByUserByEventId || 0 == alarmsByUserByEventId.size()) {
            return 0;
        }
        AlarmField[] mappedFields = MAPPER.getMappedFields();
        StringBuilder stringBuilder = new StringBuilder()
            .append("INSERT INTO calendar_alarm (cid,account,event,user,")
            .append(MAPPER.getColumns(mappedFields)).append(") VALUES ");
        for (Map<Integer, List<Alarm>> alarmsByUser : alarmsByUserByEventId.values()) {
            for (List<Alarm> alarms : alarmsByUser.values()) {
                for (int i = 0; i < alarms.size(); i++) {
                    stringBuilder.append("(?,?,?,?,").append(MAPPER.getParameters(mappedFields)).append("),");
                }
            }
        }
        stringBuilder.setLength(stringBuilder.length() - 1);
        stringBuilder.append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            for (Entry<String, Map<Integer, List<Alarm>>> entry : alarmsByUserByEventId.entrySet()) {
                String eventId = entry.getKey();
                for (Entry<Integer, List<Alarm>> alarmsByUser : entry.getValue().entrySet()) {
                    int userId = i(alarmsByUser.getKey());
                    for (Alarm alarm : alarmsByUser.getValue()) {
                        stmt.setInt(parameterIndex++, cid);
                        stmt.setInt(parameterIndex++, account);
                        stmt.setString(parameterIndex++, eventId);
                        stmt.setInt(parameterIndex++, userId);
                        parameterIndex = MAPPER.setParameters(stmt, parameterIndex, adjustPriorSave(entry.getKey(), alarm), mappedFields);
                    }
                }
            }
            return logExecuteUpdate(stmt);
        }
    }

    private int updateAlarm(Connection connection, int cid, int account, int id, Alarm alarm, String eventId) throws SQLException, OXException {
        AlarmField[] assignedfields = MAPPER.getAssignedFields(alarm);
        String sql = new StringBuilder()
            .append("UPDATE calendar_alarm SET ").append(MAPPER.getAssignments(assignedfields))
            .append(" WHERE cid=? AND account=? AND id=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            parameterIndex = MAPPER.setParameters(stmt, parameterIndex, adjustPriorSave(eventId, alarm), assignedfields);
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            stmt.setInt(parameterIndex++, id);
            return logExecuteUpdate(stmt);
        }
    }

    private Map<Integer, List<Alarm>> selectAlarms(Connection connection, int cid, int account, String eventId, AlarmField[] fields) throws SQLException, OXException {
        AlarmField[] mappedFields = MAPPER.getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT user,").append(MAPPER.getColumns(mappedFields))
            .append(" FROM calendar_alarm WHERE cid=? AND account=? AND event=?;")
        ;
        Map<Integer, List<Alarm>> alarmsByUserId = new HashMap<Integer, List<Alarm>>();
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            stmt.setString(parameterIndex++, eventId);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    int userId = resultSet.getInt("user");
                    com.openexchange.tools.arrays.Collections.put(alarmsByUserId, I(userId), readAlarm(eventId, resultSet, mappedFields));
                }
            }
        }
        return alarmsByUserId;
    }

    private Map<String, List<Alarm>> selectAlarms(Connection connection, int cid, int account, int user, String[] eventIds, AlarmField[] fields) throws SQLException, OXException {
        AlarmField[] mappedFields = MAPPER.getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT event,").append(MAPPER.getColumns(mappedFields))
            .append(" FROM calendar_alarm WHERE cid=? AND account=? AND user=?")
        ;
        if (1 == eventIds.length) {
            stringBuilder.append(" AND event=?");
        } else if (eventIds.length > 1) {
            stringBuilder.append(" AND event IN (").append(getParameters(eventIds.length)).append(')');
        }
        stringBuilder.append(';');
        Map<String, List<Alarm>> alarmsByEventId = new HashMap<String, List<Alarm>>(eventIds.length);
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            stmt.setInt(parameterIndex++, user);
            for (String eventId : eventIds) {
                stmt.setString(parameterIndex++, eventId);
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    String eventId = resultSet.getString(1);
                    com.openexchange.tools.arrays.Collections.put(alarmsByEventId, eventId, readAlarm(eventId, resultSet, mappedFields));
                }
            }
        }
        return alarmsByEventId;
    }

    private Map<String, Map<Integer, List<Alarm>>> selectAlarms(Connection connection, int cid, int account, String[] eventIds, AlarmField[] fields) throws SQLException, OXException {
        AlarmField[] mappedFields = MAPPER.getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT event,user,").append(MAPPER.getColumns(mappedFields))
            .append(" FROM calendar_alarm WHERE cid=? AND account=?")
        ;
        if (1 == eventIds.length) {
            stringBuilder.append(" AND event=?");
        } else if (eventIds.length > 1) {
            stringBuilder.append(" AND event IN (").append(getParameters(eventIds.length)).append(')');
        }
        stringBuilder.append(';');
        Map<String, Map<Integer, List<Alarm>>> alarmsByUserById = new HashMap<String, Map<Integer, List<Alarm>>>(eventIds.length);
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            for (String eventId : eventIds) {
                stmt.setString(parameterIndex++, eventId);
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    String eventID = resultSet.getString(1);
                    Map<Integer, List<Alarm>> alarmsByUser = alarmsByUserById.get(eventID);
                    if (null == alarmsByUser) {
                        alarmsByUser = new HashMap<Integer, List<Alarm>>();
                        alarmsByUserById.put(eventID, alarmsByUser);
                    }
                    com.openexchange.tools.arrays.Collections.put(alarmsByUser, I(resultSet.getInt(2)), readAlarm(eventID, resultSet, mappedFields));
                }
            }
        }
        return alarmsByUserById;
    }

    private static int deleteAlarms(Connection connection, int cid, int account, List<String> eventIds) throws SQLException {
        if (null == eventIds || 0 == eventIds.size()) {
            return 0;
        }
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM calendar_alarm WHERE cid=? AND account=? AND event")
            .append(Databases.getPlaceholders(eventIds.size())).append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            for (String id : eventIds) {
                stmt.setString(parameterIndex++, id);
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteAlarms(Connection connection, int cid, int account, int user) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM calendar_alarm WHERE cid=? AND account=? AND user=?;")) {
            stmt.setInt(1, cid);
            stmt.setInt(2, account);
            stmt.setInt(3, user);
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteAlarms(Connection connection, int cid, int account) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM calendar_alarm WHERE cid=? AND account=?;")) {
            stmt.setInt(1, cid);
            stmt.setInt(2, account);
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteAlarms(Connection connection, int cid, int account, String eventId, int[] userIds) throws SQLException {
        String sql = new StringBuilder()
            .append("DELETE FROM calendar_alarm WHERE cid=? AND account=? AND event=? AND user IN (")
            .append(getParameters(userIds.length)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            stmt.setString(parameterIndex++, eventId);
            for (int userId : userIds) {
                stmt.setInt(parameterIndex++, userId);
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteAlarms(Connection connection, int cid, int account, int[] alarmIds) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder().append("DELETE FROM calendar_alarm WHERE cid=? AND account=?");
        if (1 == alarmIds.length) {
            stringBuilder.append(" AND id=?");
        } else {
            stringBuilder.append(" AND id IN (").append(getParameters(alarmIds.length)).append(')');
        }
        stringBuilder.append(';');
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            for (int alarmId : alarmIds) {
                stmt.setInt(parameterIndex++, alarmId);
            }
            return logExecuteUpdate(stmt);
        }
    }

    private Alarm readAlarm(String eventId, ResultSet resultSet, AlarmField[] fields) throws SQLException, OXException {
        return adjustAfterLoad(eventId, MAPPER.fromResultSet(resultSet, MAPPER.getMappedFields(fields)));
    }

    /**
     * Adjusts certain properties of an alarm after loading it from the database.
     *
     * @param eventId The identifier of the associated event
     * @param alarm The alarm to adjust
     * @return The (possibly adjusted) alarm reference
     */
    private Alarm adjustAfterLoad(String eventId, Alarm alarm) {
        ExtendedProperties extendedProperties = alarm.getExtendedProperties();
        if (null == extendedProperties) {
            return alarm;
        }
        /*
         * move specific properties from container into alarm object
         */
        ExtendedProperty summaryProperty = extendedProperties.get("SUMMARY");
        if (null != summaryProperty) {
            alarm.setSummary((String) summaryProperty.getValue());
            extendedProperties.remove(summaryProperty);
        }
        ExtendedProperty descriptionProperty = extendedProperties.get("DESCRIPTION");
        if (null != descriptionProperty) {
            alarm.setDescription((String) descriptionProperty.getValue());
            extendedProperties.remove(descriptionProperty);
        }
        List<ExtendedProperty> attendeeProperties = extendedProperties.getAll("ATTENDEE");
        if (null != attendeeProperties && 0 < attendeeProperties.size()) {
            alarm.setAttendees(decodeAttendees(eventId, attendeeProperties));
            extendedProperties.removeAll(attendeeProperties);
        }
        List<ExtendedProperty> attachmentProperties = extendedProperties.getAll("ATTACH");
        if (null != attachmentProperties && 0 < attachmentProperties.size()) {
            alarm.setAttachments(decodeAttachments(eventId, attachmentProperties));
            extendedProperties.removeAll(attachmentProperties);
        }

        if (extendedProperties.size() == 0) {
            alarm.removeExtendedProperties();
        }
        return alarm;
    }

    /**
     * Adjusts certain properties of an alarm prior inserting it into the database.
     *
     * @param eventId The identifier of the associated event
     * @param alarm The alarm to adjust
     * @return The (possibly adjusted) alarm reference
     */
    private Alarm adjustPriorSave(String eventId, Alarm alarm) {
        /*
         * get or initialize new extended properties container
         */
        ExtendedProperties extendedProperties = alarm.getExtendedProperties();
        if (null == extendedProperties) {
            extendedProperties = new ExtendedProperties();
        }
        alarm.setExtendedProperties(extendedProperties);
        /*
         * move specific alarm properties into container
         */
        if (alarm.containsSummary()) {
            encdodeProperty(extendedProperties, "SUMMARY", alarm.getSummary());
        }
        if (alarm.containsDescription()) {
            encdodeProperty(extendedProperties, "DESCRIPTION", alarm.getDescription());
        }
        if (alarm.containsAttachments()) {
            encdodeAttachments(eventId, extendedProperties, alarm.getAttachments());
        }
        if (alarm.containsAttendees()) {
            encdodeAttendees(eventId, extendedProperties, alarm.getAttendees());
        }
        return alarm;
    }

    /**
     * Decodes a list of extended properties into a valid list of attendees.
     *
     * @param eventId The identifier of the associated event
     * @param attendeeProperties The extended attendee properties to decode
     * @return The decoded attendees, or an empty list if there are none
     */
    private List<Attendee> decodeAttendees(String eventId, List<ExtendedProperty> attendeeProperties) {
        List<Attendee> attendees = new ArrayList<Attendee>(attendeeProperties.size());
        for (ExtendedProperty attendeeProperty : attendeeProperties) {
            Attendee attendee = new Attendee();
            attendee.setUri((String) attendeeProperty.getValue());
            ExtendedPropertyParameter cnParameter = attendeeProperty.getParameter("CN");
            if (null != cnParameter) {
                attendee.setCn(cnParameter.getValue());
            }
            try {
                attendee = entityProcessor.adjustAfterLoad(attendee);
            } catch (OXException e) {
                addInvalidDataWarning(eventId, EventField.ALARMS, ProblemSeverity.NORMAL, "Error processing " + attendee, e);
            }
            attendees.add(attendee);
        }
        return attendees;
    }

    /**
     * Decodes a list of extended properties into a valid list of attachments.
     *
     * @param eventId The identifier of the associated event
     * @param attachmentProperties The extended attachment properties to decode
     * @return The decoded attendees, or an empty list if there are none
     */
    private List<Attachment> decodeAttachments(String eventId, List<ExtendedProperty> attachmentProperties) {
        List<Attachment> attachments = new ArrayList<Attachment>(attachmentProperties.size());
        for (ExtendedProperty attachmentProperty : attachmentProperties) {
            Attachment attachment = new Attachment();
            ExtendedPropertyParameter fmtTypeParameter = attachmentProperty.getParameter("FMTTYPE");
            if (null != fmtTypeParameter) {
                attachment.setFormatType(fmtTypeParameter.getValue());
            }
            ExtendedPropertyParameter filenameParameter = attachmentProperty.getParameter("FILENAME");
            if (null != filenameParameter) {
                attachment.setFilename(filenameParameter.getValue());
            }
            ExtendedPropertyParameter sizeParameter = attachmentProperty.getParameter("SIZE");
            if (null != sizeParameter) {
                try {
                    attachment.setSize(Long.parseLong(sizeParameter.getValue()));
                } catch (NumberFormatException e) {
                    addInvalidDataWarning(eventId, EventField.ALARMS, ProblemSeverity.TRIVIAL, "Error parsing attachment size parameter", e);
                }
            }
            ExtendedPropertyParameter valueParameter = attachmentProperty.getParameter("VALUE");
            if (null != valueParameter && "BINARY".equals(valueParameter.getValue())) {
                ThresholdFileHolder fileHolder = new ThresholdFileHolder();
                try {
                    fileHolder.write(BaseEncoding.base64().decode((String) attachmentProperty.getValue()));
                    attachment.setData(fileHolder);
                } catch (IllegalArgumentException | OXException e) {
                    addInvalidDataWarning(eventId, EventField.ALARMS, ProblemSeverity.NORMAL, "Error processing binary alarm data", e);
                    Streams.close(fileHolder);
                }
            } else {
                attachment.setUri((String) attachmentProperty.getValue());
            }
            attachments.add(attachment);
        }
        return attachments;
    }

    /**
     * Encodes a list of attachments into the supplied extended properties container. Any previously stored <code>ATTACHMENT</code>
     * property is overwritten implicitly.
     *
     * @param eventId The identifier of the associated event
     * @param extendedProperties The extended properties to use
     * @param attachments The attachments to encode, or <code>null</code> to just remove any possibly existing properties
     * @return <code>true</code> if the extended properties container was modified, <code>false</code>, otherwise
     */
    private boolean encdodeAttachments(String eventId, ExtendedProperties extendedProperties, List<Attachment> attachments) {
        boolean modified = extendedProperties.removeAll("ATTACH");
        if (null != attachments) {
            for (Attachment attachment : attachments) {
                List<ExtendedPropertyParameter> parameters = new ArrayList<ExtendedPropertyParameter>();
                String value;
                if (Strings.isNotEmpty(attachment.getUri())) {
                    value = attachment.getUri();
                } else if (null != attachment.getData()) {
                    IFileHolder data = attachment.getData();
                    try (InputStream inputStream = data.getStream()) {
                        value = BaseEncoding.base64().encode(Streams.stream2bytes(inputStream));
                        parameters.add(new ExtendedPropertyParameter("ENCODING", "BASE64"));
                        parameters.add(new ExtendedPropertyParameter("VALUE", "BINARY"));
                    } catch (IOException | OXException e) {
                        addInvalidDataWarning(eventId, EventField.ALARMS, ProblemSeverity.NORMAL, "Error processing binary alarm data", e);
                        value = null;
                    }
                } else {
                    value = null;
                }
                if (null != attachment.getFormatType()) {
                    parameters.add(new ExtendedPropertyParameter("FMTTYPE", attachment.getFormatType()));
                }
                if (null != attachment.getFilename()) {
                    parameters.add(new ExtendedPropertyParameter("FILENAME", attachment.getFilename()));
                }
                if (0 < attachment.getSize()) {
                    parameters.add(new ExtendedPropertyParameter("SIZE", String.valueOf(attachment.getSize())));
                }
                modified |= extendedProperties.add(new ExtendedProperty("ATTACH", value, parameters));
            }
        }
        return modified;
    }

    /**
     * Encodes a list of attendees into the supplied extended properties container. Any previously stored <code>ATTENDEE</code>
     * property is overwritten implicitly.
     *
     * @param eventId The identifier of the associated event
     * @param extendedProperties The extended properties to use
     * @param attendees The attendees to encode, or <code>null</code> to just remove any possibly existing properties
     * @return <code>true</code> if the extended properties container was modified, <code>false</code>, otherwise
     */
    private boolean encdodeAttendees(String eventId, ExtendedProperties extendedProperties, List<Attendee> attendees) {
        boolean modified = extendedProperties.removeAll("ATTENDEE");
        if (null != attendees) {
            for (Attendee attendee : attendees) {
                if (attendee.containsCn() && null != attendee.getCn()) {
                    List<ExtendedPropertyParameter> parameters = Collections.singletonList(new ExtendedPropertyParameter("CN", attendee.getCn()));
                    modified |= extendedProperties.add(new ExtendedProperty("ATTENDEE", attendee.getUri(), parameters));
                } else {
                    modified |= extendedProperties.add(new ExtendedProperty("ATTENDEE", attendee.getUri()));
                }
            }
        }
        return modified;
    }

    /**
     * Encodes a single property of an alarm into the supplied extended properties container. Any previously stored property with the
     * same name is overwritten implicitly.
     *
     * @param extendedProperties The extended properties to use
     * @param name The name of the property to encode
     * @param value The value of the property to encode, or <code>null</code> to just remove any possibly existing properties
     * @return <code>true</code> if the extended properties container was modified, <code>false</code>, otherwise
     */
    private static boolean encdodeProperty(ExtendedProperties extendedProperties, String name, String value) {
        boolean modified = extendedProperties.removeAll(name);
        if (null != value) {
            modified |= extendedProperties.add(new ExtendedProperty(name, value));
        }
        return modified;
    }


    @Override
    public Alarm loadAlarm(int alarmId) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAlarm(connection, context.getContextId(), accountId, alarmId, AlarmField.values());
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    /**
     * Selects a single {@link Alarm} defined by the given cid / accountId / alarmId combination.
     *
     * @param con The connection to use
     * @param cid The context id
     * @param accountId The account id
     * @param alarmId The alarm id
     * @param fields The fields to request
     * @return The {@link Alarm}
     * @throws OXException
     * @throws SQLException
     */
    private Alarm selectAlarm(Connection con, int cid, int accountId, int alarmId, AlarmField[] fields) throws OXException, SQLException {
        AlarmField[] mappedFields = MAPPER.getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT event,").append(MAPPER.getColumns(mappedFields))
            .append(" FROM calendar_alarm WHERE cid=? AND account=? AND id=?;");
        try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, accountId);
            stmt.setInt(parameterIndex++, alarmId);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                if(resultSet.next()) {
                    String eventId = resultSet.getString(1);
                    return readAlarm(eventId, resultSet, fields);
                }
                return null;
            }
        }
    }

}
