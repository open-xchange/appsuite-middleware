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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.compat.Appointment2Event;
import com.openexchange.chronos.compat.Event2Appointment;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.exception.EventExceptionCode;
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

    /**
     * Initializes a new {@link RdbAlarmStorage}.
     *
     * @param context The context
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param The transaction policy
     */
    public RdbAlarmStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, entityResolver, dbProvider, txPolicy);
    }

    @Override
    public Map<Integer, List<Alarm>> loadAlarms(int objectID) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAlarms(connection, context.getContextId(), objectID);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public List<Alarm> loadAlarms(int objectID, int userID) throws OXException {
        return loadAlarms(new int[] { objectID }, userID).get(I(objectID));
    }

    @Override
    public Map<Integer, List<Alarm>> loadAlarms(int[] objectIDs, int userID) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectAlarms(connection, context.getContextId(), objectIDs, userID);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void insertAlarms(int objectID, int userID, List<Alarm> alarms) throws OXException {
        updateAlarms(objectID, userID, alarms);
    }

    @Override
    public void updateAlarms(int objectID, int userID, List<Alarm> alarms) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            Integer reminder = Event2Appointment.getReminder(alarms);
            updated += updateReminder(connection, context.getContextId(), objectID, userID, reminder);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAlarms(int objectID, int userID) throws OXException {
        updateAlarms(objectID, userID, null);
    }

    @Override
    public void deleteAlarms(int objectID, int[] userIDs) throws OXException {
        for (int userID : userIDs) {
            deleteAlarms(objectID, userID);
        }
    }

    @Override
    public void deleteAlarms(int objectID) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += updateReminders(connection, context.getContextId(), objectID, null);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw EventExceptionCode.MYSQL.create(e);
        } finally {
            release(connection, updated);
        }
    }

    private static Map<Integer, List<Alarm>> selectAlarms(Connection connection, int contextID, int[] objectIDs, int userID) throws SQLException {
        Map<Integer, List<Alarm>> alarmsById = new HashMap<Integer, List<Alarm>>();
        String sql = new StringBuilder()
            .append("SELECT object_id,reminder FROM prg_dates_members ")
            .append("WHERE cid=? AND member_uid=? AND object_id IN (").append(getParameters(objectIDs.length)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            for (int objectID : objectIDs) {
                stmt.setInt(parameterIndex++, objectID);
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                if (resultSet.next()) {
                    int reminder = resultSet.getInt("reminder");
                    if (false == resultSet.wasNull()) {
                        alarmsById.put(I(resultSet.getInt("object_id")), Collections.singletonList(Appointment2Event.getAlarm(reminder)));
                    }
                }
            }
        }
        return alarmsById;
    }

    private static Map<Integer, List<Alarm>> selectAlarms(Connection connection, int contextID, int objectID) throws SQLException {
        Map<Integer, List<Alarm>> alarmsById = new HashMap<Integer, List<Alarm>>();
        String sql = new StringBuilder()
            .append("SELECT member_uid,reminder FROM prg_dates_members ")
            .append("WHERE cid=? AND object_id=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, objectID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    int reminder = resultSet.getInt("reminder");
                    if (false == resultSet.wasNull()) {
                        alarmsById.put(I(resultSet.getInt("member_uid")), Collections.singletonList(Appointment2Event.getAlarm(reminder)));
                    }
                }
            }
        }
        return alarmsById;
    }

    private static int updateReminder(Connection connection, int contextID, int objectID, int userID, Integer reminder) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id=? AND member_uid=?;")) {
            if (null == reminder) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, reminder.intValue());
            }
            stmt.setInt(2, contextID);
            stmt.setInt(3, objectID);
            stmt.setInt(4, userID);
            return logExecuteUpdate(stmt);
        }
    }

    private static int updateReminders(Connection connection, int contextID, int objectID, Integer reminder) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id=?;")) {
            if (null == reminder) {
                stmt.setNull(1, java.sql.Types.INTEGER);
            } else {
                stmt.setInt(1, reminder.intValue());
            }
            stmt.setInt(2, contextID);
            stmt.setInt(3, objectID);
            return logExecuteUpdate(stmt);
        }
    }

}
