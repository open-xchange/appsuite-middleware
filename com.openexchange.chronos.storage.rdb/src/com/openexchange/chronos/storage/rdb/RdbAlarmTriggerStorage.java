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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.EventID;
import com.openexchange.chronos.storage.AlarmTrigger;
import com.openexchange.chronos.storage.AlarmTriggerField;
import com.openexchange.chronos.storage.AlarmTriggerStorage;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link RdbAlarmTriggerStorage}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class RdbAlarmTriggerStorage extends RdbStorage implements AlarmTriggerStorage {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbAlarmTriggerStorage.class);
    private static final AlarmTriggerDBMapper MAPPER = AlarmTriggerDBMapper.getInstance();
    private final int accountId;

    /**
     * Initializes a new {@link RdbAlarmTriggerStorage}.
     *
     * @param context
     * @param accountId
     * @param dbProvider
     * @param txPolicy
     */
    protected RdbAlarmTriggerStorage(Context context, int accountId, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
        this.accountId = accountId;
    }

    @Override
    public void insertAlarmTrigger(AlarmTrigger trigger) throws OXException {
        Connection writeCon = dbProvider.getWriteConnection(context);
        int updated = 0;
        try {
            txPolicy.setAutoCommit(writeCon, false);
            updated = insertAlarmTrigger(trigger, writeCon);
            txPolicy.commit(writeCon);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(writeCon, updated);
        }
    };

    private int insertAlarmTrigger(AlarmTrigger trigger, Connection writeCon) throws OXException {

        try {
            AlarmTriggerField[] mappedFields = MAPPER.getMappedFields();
            String sql = new StringBuilder().append("INSERT INTO calendar_alarm_trigger (cid, account, ").append(MAPPER.getColumns(mappedFields)).append(") VALUES (?,?,").append(MAPPER.getParameters(mappedFields)).append(");").toString();
            try (PreparedStatement stmt = writeCon.prepareStatement(sql)) {
                int parameterIndex = 1;
                stmt.setInt(parameterIndex++, context.getContextId());
                stmt.setInt(parameterIndex++, accountId);
                parameterIndex = MAPPER.setParameters(stmt, parameterIndex, trigger, mappedFields);
                return logExecuteUpdate(stmt);
            }
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public void updateAlarmTrigger(AlarmTrigger trigger) throws OXException {
        Connection writeCon = dbProvider.getWriteConnection(context);
        int updated = 0;
        try {
            txPolicy.setAutoCommit(writeCon, false);
            updateAlarmTrigger(trigger, writeCon);
            txPolicy.commit(writeCon);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(writeCon, updated);
        }
    };

    private int updateAlarmTrigger(AlarmTrigger trigger, Connection writeCon) throws OXException {

        try {
            AlarmTriggerField[] assignedfields = MAPPER.getAssignedFields(trigger);
            String sql = new StringBuilder().append("UPDATE calendar_alarm_trigger SET ").append(MAPPER.getAssignments(assignedfields)).append(" WHERE cid=? AND account=? AND alarm=?;").toString();
            try (PreparedStatement stmt = writeCon.prepareStatement(sql)) {
                int parameterIndex = 1;
                parameterIndex = MAPPER.setParameters(stmt, parameterIndex, trigger, assignedfields);
                stmt.setInt(parameterIndex++, context.getContextId());
                stmt.setInt(parameterIndex++, accountId);
                stmt.setInt(parameterIndex++, trigger.getAlarm());
                return logExecuteUpdate(stmt);
            }
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public List<AlarmTrigger> getAlarmTriggers(int contextId, int account, long until, AlarmTriggerField fields[]) throws OXException {
        Connection con = dbProvider.getReadConnection(context);
        try {
            return getAlarmTriggers(contextId, account, until, fields, con);
        } finally {
            dbProvider.releaseReadConnection(context, con);
        }
    }

    private List<AlarmTrigger> getAlarmTriggers(int contextId, int account, long until, AlarmTriggerField fields[], Connection con) throws OXException {
        try {
            AlarmTriggerField[] mappedFields = MAPPER.getMappedFields(fields);
            StringBuilder stringBuilder = new StringBuilder().append("SELECT ").append(MAPPER.getColumns(mappedFields)).append(" FROM ").append("calendar_alarm_trigger").append(" WHERE cid=? AND account=? AND triggerDate<? ORDER BY triggerDate");

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
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    };

    private AlarmTrigger readTrigger(ResultSet resultSet, AlarmTriggerField[] fields) throws SQLException, OXException {
        return MAPPER.fromResultSet(resultSet, fields);
    }

    @Override
    public void deleteAlarmTriggers(List<EventID> alarmIds) throws OXException {
        Connection writeCon = dbProvider.getWriteConnection(context);
        int deleted = 0;
        try {
            txPolicy.setAutoCommit(writeCon, false);
            deleted = deleteAlarmTriggers(context.getContextId(), accountId, alarmIds, writeCon);
            txPolicy.commit(writeCon);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(writeCon, deleted);
        }

    }

    private static final String SQL_DELETE = "DELETE FROM calendar_alarm_trigger WHERE cid=? AND account=? AND eventId IN (";
    private static final String SQL_DELETE_RECURRENCE = "DELETE FROM calendar_alarm_trigger WHERE cid=? AND account=? AND eventId=? AND recurrence=?;";

    private int deleteAlarmTriggers(int contextId, int accountId, List<EventID> eventIds, Connection writeCon) throws OXException {

        // divide into basic deletes and recurrence delete operations
        List<EventID> recurrence = new ArrayList<>();
        List<EventID> basic = new ArrayList<>();
        for (EventID eventID : eventIds) {
            if (eventID.getRecurrenceID() != null) {
                recurrence.add(eventID);
            } else {
                basic.add(eventID);
            }
        }

        int deleted = 0;
        // Delete basic
        if (!basic.isEmpty()) {
            PreparedStatement prepareStatement = null;

            try {
                String sql = Databases.getIN(SQL_DELETE, basic.size());
                prepareStatement = writeCon.prepareStatement(sql);
                int x = 0;
                prepareStatement.setInt(++x, contextId);
                prepareStatement.setInt(++x, accountId);
                for (EventID eventId : basic) {
                    prepareStatement.setInt(++x, Integer.valueOf(eventId.getObjectID()));
                }
                deleted += logExecuteUpdate(prepareStatement);
            } catch (SQLException e) {
                throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(prepareStatement);
            }
        }

        // Delete recurrences
        if (!recurrence.isEmpty()) {
            PreparedStatement stmt = null;
            try {
                stmt = writeCon.prepareStatement(SQL_DELETE_RECURRENCE);
                int x = 0;
                for (EventID eventId : recurrence) {
                    stmt.setInt(++x, contextId);
                    stmt.setInt(++x, accountId);
                    stmt.setInt(++x, Integer.valueOf(eventId.getObjectID()));
                    stmt.setString(++x, String.valueOf(eventId.getRecurrenceID().getValue().getTimestamp()));
                    stmt.addBatch();
                }
                int[] executeBatch = stmt.executeBatch();
                for (int del : executeBatch) {
                    deleted += del;
                }
            } catch (SQLException e) {
                throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(stmt);
            }
        }
        return deleted;
    }

}
