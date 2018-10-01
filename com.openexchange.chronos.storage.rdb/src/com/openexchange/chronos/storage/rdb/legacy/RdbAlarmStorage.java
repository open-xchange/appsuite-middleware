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

package com.openexchange.chronos.storage.rdb.legacy;

import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.RdbStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.impl.IDGenerator;
import com.openexchange.java.util.TimeZones;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAlarmStorage extends RdbStorage implements AlarmStorage {

    /** The module identifier used in the <code>reminder</code> table */
    private static final int REMINDER_MODULE = Types.APPOINTMENT;

    private final EntityResolver entityResolver;

    /**
     * Initializes a new {@link RdbAlarmStorage}.
     *
     * @param context The context
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbAlarmStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy) {
        super(context, dbProvider, txPolicy);
        this.entityResolver = entityResolver;
    }

    @Override
    public int nextId() throws OXException {
        return 0; // no unique identifiers required
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
            Map<Integer, ReminderData> remindersByUserID = selectReminders(connection, context.getContextId(), Collections.singleton(event.getId())).get(event.getId());
            if (null == remindersByUserID) {
                return Collections.emptyMap();
            }
            Map<Integer, List<Alarm>> alarmsByUserID = new HashMap<Integer, List<Alarm>>(remindersByUserID.size());
            for (Map.Entry<Integer, ReminderData> entry : remindersByUserID.entrySet()) {
                List<Alarm> alarms = optAlarms(event, i(entry.getKey()), entry.getValue());
                if (null != alarms) {
                    alarmsByUserID.put(entry.getKey(), alarms);
                }
            }
            return alarmsByUserID;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Map<String, List<Alarm>> loadAlarms(List<Event> events, int userID) throws OXException {
        Map<String, Event> eventsByID = CalendarUtils.getEventsByID(events);
        Map<String, List<Alarm>> alarmsByEventID = new HashMap<String, List<Alarm>>(eventsByID.size());
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            Map<String, ReminderData> remindersByID = selectReminders(connection, context.getContextId(), eventsByID.keySet(), userID);
            for (Map.Entry<String, ReminderData> entry : remindersByID.entrySet()) {
                List<Alarm> alarms = optAlarms(eventsByID.get(entry.getKey()), userID, entry.getValue());
                if (null != alarms) {
                    alarmsByEventID.put(entry.getKey(), alarms);
                }
            }
            return alarmsByEventID;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public Map<String, Map<Integer, List<Alarm>>> loadAlarms(List<Event> events) throws OXException {
        Map<String, Event> eventsByID = CalendarUtils.getEventsByID(events);
        Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventID = new HashMap<String, Map<Integer, List<Alarm>>>(eventsByID.size());
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            Map<String, Map<Integer, ReminderData>> remindersByUserByID = selectReminders(connection, context.getContextId(), eventsByID.keySet());
            for (Entry<String, Map<Integer, ReminderData>> entry : remindersByUserByID.entrySet()) {
                String eventID = entry.getKey();
                for (Entry<Integer, ReminderData> reminderEntry : entry.getValue().entrySet()) {
                    Integer userID = reminderEntry.getKey();
                    List<Alarm> alarms = optAlarms(eventsByID.get(eventID), i(userID), reminderEntry.getValue());
                    if (null != alarms) {
                        Map<Integer, List<Alarm>> alarmsByUser = alarmsByUserByEventID.get(eventID);
                        if (null == alarmsByUser) {
                            alarmsByUser = new HashMap<Integer, List<Alarm>>();
                            alarmsByUserByEventID.put(eventID, alarmsByUser);
                        }
                        alarmsByUser.put(userID, alarms);
                    }
                }
            }
            return alarmsByUserByEventID;
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public void insertAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        updateAlarms(event, userID, alarms);
    }

    @Override
    public void insertAlarms(Event event, Map<Integer, List<Alarm>> alarmsByUserId) throws OXException {
        for (Entry<Integer, List<Alarm>> entry : alarmsByUserId.entrySet()) {
            insertAlarms(event, i(entry.getKey()), entry.getValue());
        }
    }

    @Override
    public void insertAlarms(Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId) throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            ReminderData originalReminder = selectReminder(connection, context.getContextId(), asInt(event.getId()), userID);
            ReminderData updatedReminder = getNextReminder(event, userID, alarms, originalReminder);
            if (null == originalReminder) {
                if (null != updatedReminder) {
                    updated += insertReminder(connection, context.getContextId(), event, userID, updatedReminder);
                }
            } else {
                if (null == updatedReminder) {
                    updated += deleteReminderMinutes(connection, context.getContextId(), asInt(event.getId()), new int[] { userID });
                    updated += deleteReminderTriggers(connection, context.getContextId(), asInt(event.getId()), new int[] { userID });
                } else {
                    updated += updateReminderMinutes(connection, context.getContextId(), event, userID, updatedReminder.reminderMinutes);
                    updated += updateReminderTrigger(connection, context.getContextId(), event, userID, updatedReminder.nextTriggerTime);
                }
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
    public void deleteAlarms(String eventID, int[] userIDs) throws OXException {
        if (null == userIDs || 0 == userIDs.length) {
            return;
        }
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += deleteReminderMinutes(connection, context.getContextId(), asInt(eventID), userIDs);
            updated += deleteReminderTriggers(connection, context.getContextId(), asInt(eventID), userIDs);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAlarms(String eventID) throws OXException {
        deleteAlarms(Collections.singletonList(eventID));
    }

    @Override
    public void deleteAlarms(List<String> eventIds) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += deleteReminderMinutes(connection, context.getContextId(), eventIds);
            updated += deleteReminderTriggers(connection, context.getContextId(), eventIds);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public void deleteAlarms(String eventId, int userId, int[] alarmIds) throws OXException {
        deleteAlarms(eventId, userId);
    }

    @Override
    public void deleteAlarms(int userId) throws OXException {
        int updated = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            updated += deleteReminderMinutes(connection, context.getContextId(), userId);
            updated += deleteReminderTriggers(connection, context.getContextId(), userId);
            txPolicy.commit(connection);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    @Override
    public boolean deleteAllAlarms() throws OXException {
        throw new UnsupportedOperationException();
    }

    /**
     * Optionally gets the alarms representing the stored reminder data. Invalid / malformed alarm data is ignored implicitly.
     *
     * @param event The event the alarms are associated with
     * @param userID The identifier of the user
     * @param reminderData The stored reminder data
     * @return The alarms, or <code>null</code> if there are none or if no alarms couldn't be derived
     */
    private List<Alarm> optAlarms(Event event, int userID, ReminderData reminderData) {
        try {
            return getAlarms(event, userID, reminderData);
        } catch (OXException e) {
            addInvalidDataWaring(event.getId(), EventField.ALARMS, ProblemSeverity.MINOR, "Ignoring invalid legacy " + reminderData + " for user " + userID, e);
            return null;
        }
    }

    /**
     * Gets the alarms representing the stored reminder data. Invalid / malformed alarm data is ignored implicitly.
     *
     * @param event The event the alarms are associated with
     * @param userID The identifier of the user
     * @param reminderData The stored reminder data
     * @return The alarms, or <code>null</code> if there are none
     */
    private List<Alarm> getAlarms(Event event, int userID, ReminderData reminderData) throws OXException {
        if (null == reminderData) {
            return null;
        }
        /*
         * construct primary alarm from reminder minutes
         */
        Alarm primaryAlarm = new Alarm(new Trigger("-PT" + reminderData.reminderMinutes + 'M'), AlarmAction.DISPLAY);
        primaryAlarm.setDescription("Reminder");
        primaryAlarm.setUid(new UUID(context.getContextId(), reminderData.id).toString().toUpperCase());
        primaryAlarm.setId(reminderData.id);
        /*
         * assume alarm is not yet acknowledged if next trigger still matches the primary alarm's regular trigger time
         */
        if (0 == reminderData.nextTriggerTime) {
            return Collections.singletonList(primaryAlarm);
        }
        Date acknowledgedGuardian = getAcknowledgedGuardian(reminderData);
        Date nextRegularTriggerTime = optNextTriggerTime(event, primaryAlarm, entityResolver.getTimeZone(userID), acknowledgedGuardian);
        if (null == nextRegularTriggerTime) {
            return Collections.singletonList(primaryAlarm);
        } else {
            /*
             * use primary alarm with acknowledged guardian to prevent premature triggers
             */
            primaryAlarm.setAcknowledged(acknowledgedGuardian);
            return Collections.singletonList(primaryAlarm);
        }
    }

    /**
     * Evaluates the reminder data to insert for a specific event based on the supplied alarm list.
     *
     * @param event The event the alarms are associated with
     * @param userID The identifier of the user
     * @param alarms The alarms to derive the reminder data from
     * @param originalReminder The previously stored reminder data, or <code>null</code> when inserting a new reminder
     * @return The next reminder, or <code>null</code> if there is none
     */
    private ReminderData getNextReminder(Event event, int userID, List<Alarm> alarms, ReminderData originalReminder) throws OXException {
        /*
         * consider ACTION=DISPLAY alarms, only
         */
        if (null == alarms || alarms.isEmpty()) {
            return null;
        }
        List<Alarm> displayAlarms = new ArrayList<Alarm>(alarms.size());
        for (Alarm alarm : alarms) {
            if (AlarmAction.DISPLAY.equals(alarm.getAction())) {
                displayAlarms.add(alarm);
            } else {
                addUnsupportedDataError(event.getId(), EventField.ALARMS, ProblemSeverity.MAJOR, "Can only store DISPLAY alarms");
            }
        }
        if (displayAlarms.isEmpty()) {
            return null;
        }
        /*
         * distinguish between 'snooze' & regular alarms (via RELTYPE=SNOOZE),
         * only considering regular alarms with a non-positive duration relative to event's the start date (reminder minutes >= 0)
         */
        TimeZone timeZone = entityResolver.getTimeZone(userID);
        List<Alarm> regularAlarms = new ArrayList<Alarm>();
        for (Alarm alarm : displayAlarms) {
            if (AlarmUtils.isSnoozed(alarm, displayAlarms)) {
                addUnsupportedDataError(event.getId(), EventField.ALARMS, ProblemSeverity.MAJOR, "Can't store snoozed alarms");
            } else if (0 <= getReminderMinutes(alarm.getTrigger(), event)) {
                regularAlarms.add(alarm);
            } else {
                addUnsupportedDataError(event.getId(), EventField.ALARMS, ProblemSeverity.NORMAL, "Can only store triggers prior start of event");
            }
        }
        /*
         * regular alarm, only
         */
        if (1 < regularAlarms.size()) {
            addUnsupportedDataError(event.getId(), EventField.ALARMS, ProblemSeverity.MAJOR, "Cannot store more than one alarm");
        }
        Alarm regularAlarm = chooseNextAlarm(event, originalReminder, regularAlarms, timeZone);
        if (null != regularAlarm) {
            Date nextTriggerTime = optNextTriggerTime(event, regularAlarm, timeZone);
            if (null != nextTriggerTime) {
                int reminderMinutes = getReminderMinutes(regularAlarm.getTrigger(), event);
                return new ReminderData(null != originalReminder ? originalReminder.id : 0, reminderMinutes, nextTriggerTime.getTime());
            }
        }
        return null;
    }

    private int getReminderMinutes(Trigger trigger, Event event) throws OXException {
        String duration = AlarmUtils.getTriggerDuration(trigger, event, Services.getService(RecurrenceService.class));
        return null == duration ? 0 : -1 * (int) TimeUnit.MILLISECONDS.toMinutes(AlarmUtils.getTriggerDuration(duration));
    }

    private static ReminderData selectReminder(Connection connection, int contextID, int eventID, int userID) throws SQLException {
        String sql = new StringBuilder()
            .append("SELECT m.reminder,r.object_id,r.alarm,r.last_modified FROM prg_dates_members AS m ")
            .append("LEFT JOIN reminder AS r ON m.cid=r.cid AND m.member_uid=r.userid AND m.object_id=r.target_id ")
            .append("WHERE m.cid=? AND m.member_uid=? AND m.object_id=?;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            stmt.setInt(parameterIndex++, eventID);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                return resultSet.next() ? readReminder(resultSet) : null;
            }
        }
    }

    private static Map<String, ReminderData> selectReminders(Connection connection, int contextID, Collection<String> eventIDs, int userID) throws SQLException {
        Map<String, ReminderData> remindersByID = new HashMap<String, ReminderData>(eventIDs.size());
        String sql = new StringBuilder()
            .append("SELECT m.object_id,m.reminder,r.object_id,r.alarm,r.last_modified FROM prg_dates_members AS m ")
            .append("LEFT JOIN reminder AS r ON m.cid=r.cid AND m.member_uid=r.userid AND m.object_id=r.target_id ")
            .append("WHERE m.cid=? AND m.member_uid=? AND m.object_id IN (").append(getParameters(eventIDs.size())).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            for (String eventID : eventIDs) {
                stmt.setInt(parameterIndex++, asInt(eventID));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    ReminderData reminder = readReminder(resultSet);
                    if (null != reminder) {
                        String eventID = asString(resultSet.getInt("m.object_id"));
                        remindersByID.put(eventID, reminder);
                    }
                }
            }
        }
        return remindersByID;
    }

    private static Map<String, Map<Integer, ReminderData>> selectReminders(Connection connection, int contextID, Collection<String> eventIDs) throws SQLException {
        /*
         * load reminder minutes from 'prg_dates_members'
         */
        Map<String, Map<Integer, ReminderData>> remindersByUserByID = new HashMap<String, Map<Integer, ReminderData>>();
        String sql = new StringBuilder()
            .append("SELECT object_id,member_uid,reminder FROM prg_dates_members ")
            .append("WHERE cid=? AND object_id IN (").append(getParameters(eventIDs.size())).append(") AND reminder IS NOT NULL;")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (String eventID : eventIDs) {
                stmt.setInt(parameterIndex++, asInt(eventID));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    String eventID = asString(resultSet.getInt("object_id"));
                    Integer userID = I(resultSet.getInt("member_uid"));
                    Integer reminderMinutes = I(resultSet.getInt("reminder"));
                    ReminderData reminder = new ReminderData(0, i(reminderMinutes), 0L);
                    Map<Integer, ReminderData> remindersByUser = remindersByUserByID.get(eventID);
                    if (null == remindersByUser) {
                        remindersByUser = new HashMap<Integer, ReminderData>();
                        remindersByUserByID.put(eventID, remindersByUser);
                    }
                    remindersByUser.put(userID, reminder);
                }
            }
        }
        if (remindersByUserByID.isEmpty()) {
            return Collections.emptyMap();
        }
        /*
         * load associated triggers from 'reminder' table
         */
        eventIDs = remindersByUserByID.keySet();
        sql = new StringBuilder()
            .append("SELECT object_id,target_id,userid,alarm,last_modified FROM reminder ")
            .append("WHERE cid=? AND target_id IN (").append(getParameters(eventIDs.size())).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            for (String eventID : eventIDs) {
                stmt.setInt(parameterIndex++, asInt(eventID));
            }
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    String eventID = asString(resultSet.getInt("target_id"));
                    Map<Integer, ReminderData> remindersByUser = remindersByUserByID.get(eventID);
                    if (null == remindersByUser) {
                        continue;
                    }
                    Integer userID = I(resultSet.getInt("userid"));
                    ReminderData reminder = remindersByUser.get(userID);
                    if (null != reminder) {
                        reminder.id = resultSet.getInt("object_id");
                        Timestamp nextTriggerTime = resultSet.getTimestamp("alarm");
                        if (null != nextTriggerTime) {
                            reminder.nextTriggerTime = nextTriggerTime.getTime();
                        }
                    }
                }
            }
        }
        return remindersByUserByID;
    }

    private static int insertReminder(Connection connection, int contextID, Event event, int userID, ReminderData reminder) throws SQLException {
        int updated = 0;
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO reminder (cid,object_id,last_modified,target_id,module,userid,alarm,recurrence,folder) VALUES (?,?,?,?,?,?,?,?,?);")) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, IDGenerator.getId(contextID, Types.REMINDER, connection));
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setInt(4, asInt(event.getId()));
            stmt.setInt(5, Types.APPOINTMENT);
            stmt.setInt(6, userID);
            stmt.setTimestamp(7, new Timestamp(reminder.nextTriggerTime));
            stmt.setInt(8, isSeriesMaster(event) ? 1 : 0);
            stmt.setInt(9, asInt(event.getFolderId()));
            updated += logExecuteUpdate(stmt);
        }
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id=? AND member_uid=?;")) {
            logMinutes(event.getId(), reminder.reminderMinutes);
            stmt.setInt(1, reminder.reminderMinutes);
            stmt.setInt(2, contextID);
            stmt.setInt(3, asInt(event.getId()));
            stmt.setInt(4, userID);
            updated += logExecuteUpdate(stmt);
        }
        return updated;
    }

    private static int updateReminderTrigger(Connection connection, int contextID, Event event, int userID, long triggerTime) throws SQLException {
        String sql = "INSERT INTO reminder (cid,object_id,last_modified,target_id,module,userid,alarm,recurrence,folder) VALUES (?,?,?,?,?,?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE last_modified=?,alarm=?,recurrence=?,folder=?;"
        ;
        logMinutes(event.getId(), TimeUnit.MILLISECONDS.toMinutes(event.getStartDate().getTimestamp() - triggerTime));
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, contextID);
            stmt.setInt(2, IDGenerator.getId(contextID, Types.REMINDER, connection));
            stmt.setLong(3, System.currentTimeMillis());
            stmt.setInt(4, asInt(event.getId()));
            stmt.setInt(5, REMINDER_MODULE);
            stmt.setInt(6, userID);
            stmt.setTimestamp(7, new Timestamp(triggerTime));
            stmt.setInt(8, isSeriesMaster(event) ? 1 : 0);
            stmt.setInt(9, asInt(event.getFolderId()));
            stmt.setLong(10, System.currentTimeMillis());
            stmt.setTimestamp(11, new Timestamp(triggerTime));
            stmt.setInt(12, isSeriesMaster(event) ? 1 : 0);
            stmt.setInt(13, asInt(event.getFolderId()));
            return logExecuteUpdate(stmt);
        }
    }

    private static int updateReminderMinutes(Connection connection, int contextID, Event event, int userID, int reminderMinutes) throws SQLException {
        logMinutes(event.getId(), reminderMinutes);
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id=? AND member_uid=?;")) {
            stmt.setInt(1, reminderMinutes);
            stmt.setInt(2, contextID);
            stmt.setInt(3, asInt(event.getId()));
            stmt.setInt(4, userID);
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteReminderTriggers(Connection connection, int contextID, int userID) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM reminder WHERE cid=? AND module=? AND userid=?")) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, REMINDER_MODULE);
            stmt.setInt(parameterIndex++, userID);
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteReminderTriggers(Connection connection, int contextID, List<String> eventIds) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("DELETE FROM reminder WHERE cid=? AND module=? AND target_id")
            .append(getPlaceholders(eventIds.size())).append(';')
        ;
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, REMINDER_MODULE);
            for (String id : eventIds) {
                stmt.setInt(parameterIndex++, asInt(id));
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteReminderTriggers(Connection connection, int contextID, int eventID, int[] userIDs) throws SQLException {
        String sql = new StringBuilder()
            .append("DELETE FROM reminder WHERE cid=? AND module=? AND target_id=? AND userid IN (")
            .append(getParameters(userIDs.length)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, REMINDER_MODULE);
            stmt.setInt(parameterIndex++, eventID);
            for (Integer userID : userIDs) {
                stmt.setInt(parameterIndex++, i(userID));
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteReminderMinutes(Connection connection, int contextID, int userID) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND member_uid=?")) {
            int parameterIndex = 1;
            stmt.setNull(parameterIndex++, java.sql.Types.INTEGER);
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteReminderMinutes(Connection connection, int contextID, List<String> eventIds) throws SQLException {
        StringBuilder stringBuilder = new StringBuilder()
            .append("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id")
            .append(getPlaceholders(eventIds.size())).append(';');
        ;
        try (PreparedStatement stmt = connection.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setNull(parameterIndex++, java.sql.Types.INTEGER);
            stmt.setInt(parameterIndex++, contextID);
            for (String id : eventIds) {
                stmt.setInt(parameterIndex++, asInt(id));
            }
            return logExecuteUpdate(stmt);
        }
    }

    private static int deleteReminderMinutes(Connection connection, int contextID, int eventID, int[] userIDs) throws SQLException {
        String sql = new StringBuilder()
            .append("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id=? AND member_uid IN (")
            .append(getParameters(userIDs.length)).append(");")
        .toString();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setNull(parameterIndex++, java.sql.Types.INTEGER);
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, eventID);
            for (Integer userID : userIDs) {
                stmt.setInt(parameterIndex++, i(userID));
            }
            return logExecuteUpdate(stmt);
        }
    }

    /**
     * Reads reminder data from the supplied result set.
     *
     * @param resultSet The result set to read from
     * @return The reminder data, or <code>null</code> if not set
     */
    private static ReminderData readReminder(ResultSet resultSet) throws SQLException {
        int reminderMinutes = resultSet.getInt("m.reminder");
        if (resultSet.wasNull()) {
            return null;
        }
        int reminderID = resultSet.getInt("r.object_id");
        Timestamp nextTriggerTime = resultSet.getTimestamp("r.alarm");
        return new ReminderData(reminderID, reminderMinutes, null == nextTriggerTime ? 0L : nextTriggerTime.getTime());
    }

    /**
     * Determines the next date-time for a specific alarm trigger associated with an event.
     * <p/>
     * For non-recurring events, this is always the static time of the alarm's trigger.
     * <p/>
     * For event series, the trigger is calculated for the <i>next</i> occurrence after a certain start date, which may be either passed
     * in <code>startDate</code>, or is either the last acknowledged date of the alarm or the current server time.
     *
     * @param event The event the alarm is associated with
     * @param alarm The alarm associated with the event
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @return The next trigger time, or <code>null</code> if there is none
     */
    private Date optNextTriggerTime(Event event, Alarm alarm, TimeZone timeZone) {
        return optNextTriggerTime(event, alarm, timeZone, null);
    }

    /**
     * Determines the next date-time for a specific alarm trigger associated with an event.
     * <p/>
     * For non-recurring events, this is always the static time of the alarm's trigger.
     * <p/>
     * For event series, the trigger is calculated for the <i>next</i> occurrence after a certain start date, which may be supplied
     * directly via the <code>startDate</code> argument, or is either the last acknowledged date of the alarm or the current server time.
     *
     * @param event The event the alarm is associated with
     * @param alarm The alarm associated with the event
     * @param timeZone The timezone to consider if the event has <i>floating</i> dates
     * @param startDate The start date marking the lower (inclusive) limit for the actual event occurrence to begin, or <code>null</code>
     *            to select automatically
     * @return The next trigger time, or <code>null</code> if there is none
     */
    private Date optNextTriggerTime(Event event, Alarm alarm, TimeZone timeZone, Date startDate) {
        if (false == isSeriesMaster(event)) {
            return AlarmUtils.getTriggerTime(alarm.getTrigger(), event, timeZone);
        }
        try {
            return AlarmUtils.getNextTriggerTime(event, alarm, startDate, timeZone, Services.getService(RecurrenceService.class));
        } catch (OXException e) {
            LOG.warn("Error determining next trigger time for alarm", e);
        }
        return null;
    }

    /**
     * Chooses the alarm with the 'nearest' trigger time, that is not yet acknowledged, from a list of multiple alarms.
     *
     * @param event The event the alarms are associated with
     * @param originalReminder The originally stored reminder data in case of updates, or <code>null</code> if not set
     * @param alarms The alarms to choose from
     * @param timeZone The timezone to consider when evaluating the next trigger time of <i>floating</i> events
     * @return The next alarm, or <code>null</code> if there is none
     */
    private Alarm chooseNextAlarm(Event event, ReminderData originalReminder, List<Alarm> alarms, TimeZone timeZone) {
        if (null == alarms || 0 == alarms.size()) {
            return null;
        }
        Alarm nearestAlarm = null;
        Date nearestTriggerTime = null;
        for (Alarm alarm : alarms) {
            Date nextTriggerTime = optNextTriggerTime(event, alarm, timeZone);
            if (null != nextTriggerTime) {
                if (null != alarm.getAcknowledged() && false == alarm.getAcknowledged().before(nextTriggerTime)) {
                    /*
                     * skip acknowledged alarms, but ignore an auto-inserted acknowledged guardian if unchanged during an update
                     */
                    Date originalAcknowledgedGuardian = getAcknowledgedGuardian(originalReminder);
                    if (null == originalAcknowledgedGuardian || false == originalAcknowledgedGuardian.equals(alarm.getAcknowledged())) {
                        continue;
                    }
                }
                if (null == nearestTriggerTime || nearestTriggerTime.before(nextTriggerTime)) {
                    nearestAlarm = alarm;
                    nearestTriggerTime = nextTriggerTime;
                }
            }
        }
        return nearestAlarm;
    }

    /**
     * Gets the date that is used as <i>acknowledged guardian</i> to prevent premature alarm triggers at clients.
     *
     * @param triggerTime The trigger time of an alarm
     * @return The date of the corresponding acknowledged guardian
     */
    private static Date getAcknowledgedGuardian(ReminderData reminderData) {
        if (null != reminderData && 0 < reminderData.nextTriggerTime) {
            Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, reminderData.nextTriggerTime);
            calendar.add(Calendar.MINUTE, -1);
            return calendar.getTime();
        }
        return null;
    }

    private static void logMinutes(String eventId, long minutes) {
        if (minutes > 1000) {
            LOG.debug("A trigger with unlikely duration is inserted. Event {}, minutes {}", eventId, Long.valueOf(minutes), new Throwable("Trigger"));
        }
    }

    private static final class ReminderData {

        int reminderMinutes;
        long nextTriggerTime;
        int id;

        /**
         * Initializes a new {@link ReminderData}.
         *
         * @param id The identifier of the stored reminder
         * @param reminderMinutes The reminder minutes, relative to the targeted event's start date
         * @param nextTriggerTime The next trigger time
         */
        ReminderData(int id, int reminderMinutes, long nextTriggerTime) {
            super();
            this.id = id;
            this.reminderMinutes = reminderMinutes;
            this.nextTriggerTime = nextTriggerTime;
        }

        @Override
        public String toString() {
            return "ReminderData [reminderMinutes=" + reminderMinutes + ", nextTriggerTime=" + new Date(nextTriggerTime) + "]";
        }

    }

    @Override
    public Alarm loadAlarm(int alarmId) throws OXException {
        throw new UnsupportedOperationException();
    }
}
