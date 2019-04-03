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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.java.Autoboxing.l;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.AlarmTriggerField;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.storage.AdministrativeAlarmTriggerStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.Pair;

/**
 * {@link AdministrativeRdbAlarmTriggerStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class AdministrativeRdbAlarmTriggerStorage implements AdministrativeAlarmTriggerStorage {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AdministrativeRdbAlarmTriggerStorage.class);

    private static final AlarmTriggerDBMapper MAPPER = AlarmTriggerDBMapper.getInstance();

    @Override
    public Map<Pair<Integer, Integer>, List<AlarmTrigger>> getAndLockTriggers(Connection con, Date until, Date overdueTime, boolean lock, AlarmAction... actions) throws OXException {
        if (overdueTime == null) {
            Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            instance.add(Calendar.MINUTE, -5);
            overdueTime = instance.getTime();
        }
        if (until == null) {
            return Collections.emptyMap();
        }
        return getAndLockTriggers(con, until.getTime(), overdueTime.getTime(), lock, actions);
    }

    private Map<Pair<Integer, Integer>, List<AlarmTrigger>> getAndLockTriggers(Connection con, long until, long overdueTime, boolean lock, AlarmAction... actions) throws OXException {
        if(actions == null || actions.length == 0) {
            return Collections.emptyMap();
        }
        try {
            AlarmTriggerField[] mappedFields = new AlarmTriggerField[] { AlarmTriggerField.ALARM_ID, AlarmTriggerField.TIME, AlarmTriggerField.EVENT_ID, AlarmTriggerField.USER_ID, AlarmTriggerField.RECURRENCE_ID};
            StringBuilder stringBuilder = new StringBuilder().append("SELECT cid,account,").append(MAPPER.getColumns(mappedFields)).append(" FROM ").append("calendar_alarm_trigger WHERE");
            addAlarmActions(stringBuilder, actions);
            stringBuilder.append(" AND triggerDate<?");
            stringBuilder.append(" AND ( processed=0 OR (triggerDate<? AND processed<?))");
            if (lock) {
                stringBuilder.append(" FOR UPDATE;");
            } else {
                stringBuilder.append(";");
            }

            Map<Pair<Integer, Integer>, List<AlarmTrigger>> result = new HashMap<>();
            try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {

                int index = 1;
                for(AlarmAction action: actions) {
                    stmt.setString(index++, action.getValue());
                }
                stmt.setLong(index++, until);
                stmt.setLong(index++, overdueTime);
                stmt.setLong(index++, overdueTime);

                try (ResultSet resultSet = logExecuteQuery(stmt)) {
                    while (resultSet.next()) {
                        Pair<Integer, Integer> pair = new Pair<Integer, Integer>(I(resultSet.getInt(1)), I(resultSet.getInt(2)));
                        List<AlarmTrigger> list = result.get(pair);
                        if (list == null) {
                            list = new ArrayList<>();
                            result.put(pair, list);
                        }
                        list.add(readTrigger(resultSet, mappedFields));
                    }
                }
            }
            return result;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    };

    private void addAlarmActions(StringBuilder stringBuilder, AlarmAction...actions) {
        stringBuilder.append(" (");
        boolean first = true;
        for(int x=0; x<actions.length; x++) {
            if(first) {
                stringBuilder.append("action=?");
                    first = false;
            } else {
                stringBuilder.append(" OR action=?");
            }
        }
        stringBuilder.append(") ");
    }

    @Override
    public void setProcessingStatus(Connection con, Map<Pair<Integer, Integer>, List<AlarmTrigger>> triggers, Long time) throws OXException {
        if (null != time && l(time) < 0) {
            time = null;
        }
        StringBuilder stringBuilder = new StringBuilder().append("UPDATE calendar_alarm_trigger SET processed=?");
        stringBuilder.append(" WHERE cid=? AND account=? AND alarm=?");
        try {
            try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {
                for (Entry<Pair<Integer, Integer>, List<AlarmTrigger>> entry : triggers.entrySet()) {
                    int cid = i(entry.getKey().getFirst());
                    int account = i(entry.getKey().getSecond());
                    for (AlarmTrigger trigger : entry.getValue()) {
                        int param = 1;
                        stmt.setLong(param++, time == null ? 0 : l(time));
                        stmt.setInt(param++, cid);
                        stmt.setInt(param++, account);
                        stmt.setInt(param++, i(trigger.getAlarm()));
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            }
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Retrieves an {@link AlarmTrigger} by reading the given {@link AlarmTriggerField}s from the result set.
     *
     * @param resultSet The {@link ResultSet}
     * @param fields The fields to read
     * @return The {@link AlarmTrigger}
     * @throws SQLException
     * @throws OXException
     */
    private AlarmTrigger readTrigger(ResultSet resultSet, AlarmTriggerField... fields) throws SQLException, OXException {
        return MAPPER.fromResultSet(resultSet, fields);
    }

    @Override
    public Map<Pair<Integer, Integer>, List<AlarmTrigger>> getMessageAlarmTriggers(Connection con, int cid, int account, String eventId, boolean lock, AlarmAction... actions) throws OXException {
        if (actions == null || actions.length == 0) {
            return Collections.emptyMap();
        }
        try {
            AlarmTriggerField[] mappedFields = new AlarmTriggerField[] { AlarmTriggerField.ALARM_ID, AlarmTriggerField.TIME, AlarmTriggerField.EVENT_ID, AlarmTriggerField.USER_ID, AlarmTriggerField.RECURRENCE_ID };
            StringBuilder stringBuilder = new StringBuilder().append("SELECT cid,account,").append(MAPPER.getColumns(mappedFields)).append(" FROM ").append("calendar_alarm_trigger WHERE");
            stringBuilder.append(" cid=? AND account=? AND");
            addAlarmActions(stringBuilder, actions);
            stringBuilder.append("AND eventId=? AND processed=0");
            if(lock) {
                stringBuilder.append(" FOR UPDATE;");
            } else {
                stringBuilder.append(";");
            }
            Map<Pair<Integer, Integer>, List<AlarmTrigger>> result = new HashMap<>();
            try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {
                int parameterIndex = 1;
                stmt.setInt(parameterIndex++, cid);
                stmt.setInt(parameterIndex++, account);
                for(AlarmAction action: actions) {
                    stmt.setString(parameterIndex++, action.getValue());
                }
                stmt.setString(parameterIndex++, eventId);
                try (ResultSet resultSet = logExecuteQuery(stmt)) {
                    while (resultSet.next()) {
                        Pair<Integer, Integer> pair = new Pair<Integer, Integer>(I(resultSet.getInt(1)), I(resultSet.getInt(2)));
                        List<AlarmTrigger> list = result.get(pair);
                        if(list == null) {
                            list = new ArrayList<>();
                            result.put(pair, list);
                        }
                        list.add(readTrigger(resultSet, mappedFields));
                    }
                }
                return result;
            }
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    /**
     * Logs & executes a prepared statement's SQL query.
     *
     * @param stmt The statement to execute the SQL query from
     * @return The result set
     */
    protected static ResultSet logExecuteQuery(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeQuery();
        }
        String statementString = String.valueOf(stmt);
        long start = System.currentTimeMillis();
        ResultSet resultSet = stmt.executeQuery();
        LOG.debug("executeQuery: {} - {} ms elapsed.", statementString, L(System.currentTimeMillis() - start));
        return resultSet;
    }

    /**
     * Logs & executes a prepared statement's SQL update.
     *
     * @param stmt The statement to execute the SQL update from
     * @return The number of affected rows
     */
    protected static int logExecuteUpdate(PreparedStatement stmt) throws SQLException {
        if (false == LOG.isDebugEnabled()) {
            return stmt.executeUpdate();
        }
        String statementString = String.valueOf(stmt);
        long start = System.currentTimeMillis();
        int rowCount = stmt.executeUpdate();
        LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", statementString, I(rowCount), L(System.currentTimeMillis() - start));
        return rowCount;
    }
}
