/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
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
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.TimeZones;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAlarmStorage extends RdbStorage implements AlarmStorage {

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
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void insertAlarms(Event event, Map<Integer, List<Alarm>> alarmsByUserId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void insertAlarms(Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void updateAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteAlarms(String eventID, int userID) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteAlarms(String eventID, int[] userIDs) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteAlarms(String eventID) throws OXException {
        deleteAlarms(Collections.singletonList(eventID));
    }

    @Override
    public void deleteAlarms(List<String> eventIds) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteAlarms(String eventId, int userId, int[] alarmIds) throws OXException {
        deleteAlarms(eventId, userId);
    }

    @Override
    public void deleteAlarms(int userId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
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
            addInvalidDataWarning(event.getId(), EventField.ALARMS, ProblemSeverity.MINOR, "Ignoring invalid legacy " + reminderData + " for user " + userID, e);
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
            RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);
            if (recurrenceService == null) {
                throw new IllegalStateException("No such service: " + RecurrenceService.class.getName());
            }
            return AlarmUtils.getNextTriggerTime(event, alarm, startDate, timeZone, recurrenceService);
        } catch (OXException e) {
            LOG.warn("Error determining next trigger time for alarm", e);
        }
        return null;
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

    @Override
    public long getLatestTimestamp(int userId) {
        return 0;
    }

    @Override
    public long getLatestTimestamp(String eventId, int userId) {
        return 0;
    }

    @Override
    public Map<String, Long> getLatestTimestamp(List<String> eventIds, int userId) throws OXException {
        return Collections.emptyMap();
    }
}
