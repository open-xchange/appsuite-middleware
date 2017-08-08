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

package com.openexchange.chronos.alarm.storage.impl;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.chronos.alarm.AlarmTrigger;
import com.openexchange.chronos.alarm.AlarmTriggerField;
import com.openexchange.chronos.alarm.storage.AlarmTriggerStorage;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;

/**
 * {@link AlarmTriggerStorageImpl}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class AlarmTriggerStorageImpl implements AlarmTriggerStorage {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlarmTriggerStorageImpl.class);
    private final ServiceLookup services;
    private static final AlarmTriggerDBMapper MAPPER = AlarmTriggerDBMapper.getInstance();

    /**
     * Initializes a new {@link AlarmTriggerStorageImpl}.
     */
    public AlarmTriggerStorageImpl(ServiceLookup lookup) {
        super();
        this.services = lookup;

    }

    @Override
    public void insertAlarm(AlarmTrigger trigger) throws OXException{
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection writeCon = dbService.getWritable(trigger.getContextId());
        try {
            insertAlarm(trigger, writeCon);
        } finally {
            dbService.backWritable(trigger.getContextId(), writeCon);
        }
    };

    @Override
    public void insertAlarm(AlarmTrigger trigger, Connection writeCon) throws OXException{

        try {
            AlarmTriggerField[] mappedFields = MAPPER.getMappedFields();
            String sql = new StringBuilder()
                .append("INSERT INTO calendar_alarm_trigger (")
                .append(MAPPER.getColumns(mappedFields))
                .append(") VALUES (").append(MAPPER.getParameters(mappedFields)).append(");")
            .toString();
            try (PreparedStatement stmt = writeCon.prepareStatement(sql)) {
                int parameterIndex = 1;
                parameterIndex = MAPPER.setParameters(stmt, parameterIndex, trigger, mappedFields);
                logExecuteUpdate(stmt);
            }
        } catch (SQLException e) {
            //TODO wrap in ox exception
        }
    }

    @Override
    public void updateAlarm(AlarmTrigger trigger) throws OXException{
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection writeCon = dbService.getWritable(trigger.getContextId());
        try {
            insertAlarm(trigger, writeCon);
        } finally {
            dbService.backWritable(trigger.getContextId(), writeCon);
        }
    };

    @Override
    public void updateAlarm(AlarmTrigger trigger, Connection writeCon) throws OXException{

        try {
            AlarmTriggerField[] assignedfields = MAPPER.getAssignedFields(trigger);
            String sql = new StringBuilder()
                .append("UPDATE calendar_alarm_trigger SET ").append(MAPPER.getAssignments(assignedfields))
                .append(" WHERE cid=? AND account=? AND alarm=?;")
            .toString();
            try (PreparedStatement stmt = writeCon.prepareStatement(sql)) {
                int parameterIndex = 1;
                parameterIndex = MAPPER.setParameters(stmt, parameterIndex, trigger, assignedfields);
                stmt.setInt(parameterIndex++, trigger.getContextId());
                stmt.setInt(parameterIndex++, trigger.getAccount());
                stmt.setInt(parameterIndex++, trigger.getAlarm());
                logExecuteUpdate(stmt);
            }
        } catch (SQLException e) {
            // TODO wrap in ox exception
        }
    }

    @Override
    public List<AlarmTrigger> getAlarms(int contextId, int account, long until, AlarmTriggerField fields[]) throws OXException{
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection con = dbService.getReadOnly(contextId);
        try {
            return getAlarms(contextId, account, until, fields, con);
        } finally {
            dbService.backReadOnly(contextId, con);
        }
    }

    @Override
    public List<AlarmTrigger> getAlarms(int contextId, int account, long until, AlarmTriggerField fields[], Connection con) throws OXException{
        try {
            StringBuilder stringBuilder = new StringBuilder()
                .append("SELECT ").append(MAPPER.getColumns(fields))
                .append(" FROM ").append("calendar_alarm_trigger")
                .append(" WHERE cid=? AND account=? AND triggerDate<? ORDER BY triggerDate");

            List<AlarmTrigger> alrmTriggers = new ArrayList<AlarmTrigger>();
            try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {
                int parameterIndex = 1;
                stmt.setInt(parameterIndex++, contextId);
                stmt.setInt(parameterIndex++, account);
                stmt.setLong(parameterIndex++, until);

                try (ResultSet resultSet = logExecuteQuery(stmt)) {
                    while (resultSet.next()) {
                        alrmTriggers.add(readTrigger(resultSet, fields));
                    }
                }
            }
            return alrmTriggers;
        } catch (SQLException e) {
            // TODO wrap as ox exception
            throw new OXException();
        }
    };

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

    private AlarmTrigger readTrigger(ResultSet resultSet, AlarmTriggerField[] fields) throws SQLException, OXException {
        return MAPPER.fromResultSet(resultSet, fields);
    }

    @Override
    public void deleteAlarms(int contextId, int accountId, List<Integer> alarmIds) throws OXException {
        DatabaseService dbService = services.getService(DatabaseService.class);
        Connection writeCon = dbService.getWritable(contextId);
        try {
            deleteAlarms(contextId, accountId, alarmIds, writeCon);
        } finally {
            dbService.backReadOnly(contextId, writeCon);
        }

    }

    private static final String SQL_DELETE = "DELETE FROM calendar_alarm_trigger WHERE cid=? AND account=? AND alarm IN (";

    @Override
    public void deleteAlarms(int contextId, int accountId, List<Integer> alarmIds, Connection writeCon) throws OXException {
        PreparedStatement prepareStatement=null;
        try {
            String sql = Databases.getIN(SQL_DELETE, alarmIds.size());
            prepareStatement = writeCon.prepareStatement(sql);
            int x=0;
            prepareStatement.setInt(++x, contextId);
            prepareStatement.setInt(++x, accountId);
            for(Integer alarmId: alarmIds){
                prepareStatement.setInt(++x, alarmId);
            }
            prepareStatement.executeUpdate();
        } catch (SQLException e) {
            // TODO wrap as ox exception
            e.printStackTrace();
        } finally {
            Databases.closeSQLStuff(prepareStatement);
        }

    }

}
