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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.AlarmTriggerField;
import com.openexchange.chronos.AlarmTriggerWrapper;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.storage.AdministrativeAlarmTriggerStorage;
import com.openexchange.exception.OXException;

/**
 * {@link AdministrativeRdbAlarmTriggerStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.1
 */
public class AdministrativeRdbAlarmTriggerStorage implements AdministrativeAlarmTriggerStorage {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AdministrativeRdbAlarmTriggerStorage.class);

    private static final AlarmTriggerDBMapper MAPPER = AlarmTriggerDBMapper.getInstance();

    /**
     * Lists alarm triggers for the given user until the given time in ascending order and locks it for update.
     *
     * @param user The user id
     * @param until The upper limit
     * @return A list of {@link AlarmTrigger}
     * @throws OXException
     */
    @Override
    public List<AlarmTriggerWrapper> getAndLockTriggers(Connection con, Date until) throws OXException {
            return getAndLockTriggers(until == null ? null : until.getTime(), con);
    }

    private List<AlarmTriggerWrapper> getAndLockTriggers(Long until, Connection con) throws OXException {
        Calendar instance = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        instance.add(Calendar.MINUTE, -5);
        try {
            AlarmTriggerField[] mappedFields = MAPPER.getMappedFields();
            StringBuilder stringBuilder = new StringBuilder().append("SELECT cid,account,").append(MAPPER.getColumns(new AlarmTriggerField[] {AlarmTriggerField.ALARM_ID, AlarmTriggerField.TIME})).append(" FROM ").append("calendar_alarm_trigger");

            if (until != null) {
                stringBuilder.append(" AND triggerDate<?");
            }
            stringBuilder.append(" AND ( processed=0 OR processed<"+instance.getTimeInMillis()+" )");
            stringBuilder.append(" AND action="+ AlarmAction.EMAIL.getValue());

            stringBuilder.append(" ORDER BY triggerDate");
            stringBuilder.append(" FOR UPDATE");
            List<AlarmTriggerWrapper> alarmTriggers = new ArrayList<AlarmTriggerWrapper>();
            try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {

                if (until != null) {
                    stmt.setLong(1, until);
                }

                try (ResultSet resultSet = logExecuteQuery(stmt)) {
                    while (resultSet.next()) {
                        alarmTriggers.add(readTrigger(resultSet, mappedFields));
                    }
                }
            }
            return alarmTriggers;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    };

    @Override
    public void setProcessingStatus(Connection con, List<AlarmTriggerWrapper> triggers, Long time) throws OXException {
        if(time<0) {
            time = null;
        }
        StringBuilder stringBuilder = new StringBuilder().append("UPDATE calendar_alarm_trigger SET processed=?");
        stringBuilder.append(" WHERE cid=? AND account=? AND alarm=?");
        try {
            PreparedStatement stmt = con.prepareStatement(stringBuilder.toString());
            for(AlarmTriggerWrapper trigger: triggers) {
                int param = 1;
                stmt.setLong(param++, time==null ? 0 : time);
                stmt.setInt(param++, trigger.getCtx());
                stmt.setInt(param++, trigger.getAccount());
                stmt.setInt(param++, trigger.getAlarmTrigger().getAlarm());
                stmt.addBatch();
            }
            stmt.executeBatch();
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
    private AlarmTriggerWrapper readTrigger(ResultSet resultSet, AlarmTriggerField... fields) throws SQLException, OXException {
        return new AlarmTriggerWrapper(MAPPER.fromResultSet(resultSet, fields), resultSet.getInt(1), resultSet.getInt(2));
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
        } else {
            String statementString = String.valueOf(stmt);
            long start = System.currentTimeMillis();
            ResultSet resultSet = stmt.executeQuery();
            LOG.debug("executeQuery: {} - {} ms elapsed.", statementString, L(System.currentTimeMillis() - start));
            return resultSet;
        }
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
        } else {
            String statementString = String.valueOf(stmt);
            long start = System.currentTimeMillis();
            int rowCount = stmt.executeUpdate();
            LOG.debug("executeUpdate: {} - {} rows affected, {} ms elapsed.", statementString, I(rowCount), L(System.currentTimeMillis() - start));
            return rowCount;
        }
    }
}
