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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAlarmStorage extends RdbStorage implements AlarmStorage {

    private final int accountId;

    /**
     * Initializes a new {@link RdbAlarmStorage}.
     *
     * @param context The context
     * @param accountId The account identifier
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbAlarmStorage(Context context, int accountId, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
        this.accountId = accountId;
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
            return selectAlarms(connection, context.getContextId(), accountId, userId, CalendarUtils.getObjectIDs(events), AlarmField.values());
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
    public void updateAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            //TODO
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
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated = deleteAlarms(connection, context.getContextId(), accountId, eventId);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    private static int insertAlarm(Connection connection, int cid, int account, String eventId, int userId, Alarm alarm) throws SQLException, OXException {
        AlarmField[] mappedFields = AlarmMapper.getInstance().getMappedFields();
        String sql = new StringBuilder()
            .append("INSERT INTO calendar_alarm (cid,account,event,user,")
            .append(AlarmMapper.getInstance().getColumns(mappedFields)).append(") ")
            .append("VALUES (?,?,?,?,").append(AlarmMapper.getInstance().getParameters(mappedFields)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            stmt.setInt(parameterIndex++, asInt(eventId));
            stmt.setInt(parameterIndex++, userId);
            parameterIndex = AlarmMapper.getInstance().setParameters(stmt, parameterIndex, alarm, mappedFields);
            return logExecuteUpdate(stmt);
        }
    }

    private static Map<Integer, List<Alarm>> selectAlarms(Connection connection, int cid, int account, String eventId, AlarmField[] fields) throws SQLException, OXException {
        AlarmField[] mappedFields = AlarmMapper.getInstance().getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT user,").append(AlarmMapper.getInstance().getColumns(mappedFields))
            .append(" FROM calendar_alarm WHERE cid=? AND account=? AND event=?;")
        ;
        Map<Integer, List<Alarm>> alarmsByUserId = new HashMap<Integer, List<Alarm>>();
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, cid);
            stmt.setInt(parameterIndex++, account);
            stmt.setInt(parameterIndex++, asInt(eventId));
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                int userId = resultSet.getInt("user");
                com.openexchange.tools.arrays.Collections.put(alarmsByUserId, I(userId), readAlarm(resultSet, mappedFields));
            }
        }
        return alarmsByUserId;
    }

    private static Map<String, List<Alarm>> selectAlarms(Connection connection, int cid, int account, int user, String[] eventIds, AlarmField[] fields) throws SQLException, OXException {
        AlarmField[] mappedFields = AlarmMapper.getInstance().getMappedFields(fields);
        StringBuilder stringBuilder = new StringBuilder()
            .append("SELECT event,").append(AlarmMapper.getInstance().getColumns(mappedFields))
            .append(" FROM calendar_alarm WHERE cid=? AND account=? AND user=?")
        ;
        if (1 == eventIds.length) {
            stringBuilder.append(" AND event=?");
        } else {
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
                stmt.setInt(parameterIndex++, Integer.parseInt(eventId));
            }
            ResultSet resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                com.openexchange.tools.arrays.Collections.put(alarmsByEventId, resultSet.getString(1), readAlarm(resultSet, mappedFields));
            }
        }
        return alarmsByEventId;
    }

    private static int deleteAlarms(Connection connection, int cid, int account, String eventId) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM calendar_alarm WHERE cid=? AND account=? AND event=?;")) {
            stmt.setInt(1, cid);
            stmt.setInt(2, account);
            stmt.setInt(3, asInt(eventId));
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
            stmt.setInt(parameterIndex++, asInt(eventId));
            for (Integer userId : userIds) {
                stmt.setInt(parameterIndex++, i(userId));
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static Alarm readAlarm(ResultSet resultSet, AlarmField[] fields) throws SQLException, OXException {
        return AlarmMapper.getInstance().fromResultSet(resultSet, fields);
    }


}
