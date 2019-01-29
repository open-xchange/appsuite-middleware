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

import static com.openexchange.chronos.common.AlarmUtils.filter;
import static com.openexchange.chronos.common.CalendarUtils.getFolderView;
import static com.openexchange.chronos.common.CalendarUtils.isSeriesMaster;
import static com.openexchange.groupware.tools.mappings.database.DefaultDbMapper.getParameters;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.i;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.Trigger;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.AlarmTriggerStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.RdbStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.database.Databases;
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
public class RdbAlarmTriggerStorage extends RdbStorage implements AlarmTriggerStorage {

    /** The module identifier used in the <code>reminder</code> table */
    private static final int REMINDER_MODULE = Types.APPOINTMENT;

    private final EntityResolver entityResolver;

    private final RecurrenceService recurrenceService;

    private final RdbEventStorage eventStorage;

    /**
     * Initializes a new {@link RdbAlarmTriggerStorage}.
     *
     * @param context The context
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     * @throws OXException
     */
    public RdbAlarmTriggerStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy, RdbEventStorage eventStorage) throws OXException {
        super(context, dbProvider, txPolicy);
        this.entityResolver = entityResolver;
        this.recurrenceService = Services.getService(RecurrenceService.class, true);
        this.eventStorage = eventStorage;
    }

    @Override
    public void insertTriggers(Event event, Map<Integer, List<Alarm>> alarmsPerUserId) throws OXException {
        if (event.containsRecurrenceRule() && event.getRecurrenceRule() != null && event.getRecurrenceId() == null && event.getId().equals(event.getSeriesId())) {

            RecurrenceData data = new DefaultRecurrenceData(event);
            RecurrenceIterator<RecurrenceId> iterateRecurrenceIds = recurrenceService.iterateRecurrenceIds(data, new Date(), null);
            if (!iterateRecurrenceIds.hasNext()) {
                // Nothing to do for this event
                return;
            }
        }
        int updated = 0;
        Connection connection = dbProvider.getWriteConnection(context);
        try {
            for (Entry<Integer, List<Alarm>> entry : alarmsPerUserId.entrySet()) {
                int userId = i(entry.getKey());
                String folderView = getFolderView(event, userId);
                if (false == folderView.equals(event.getFolderId())) {
                    Event userizedEvent = new DelegatingEvent(event) {

                        @Override
                        public String getFolderId() {
                            return folderView;
                        }

                        @Override
                        public boolean containsFolderId() {
                            return true;
                        }
                    };
                    updated += insertReminder(connection, userizedEvent, userId, entry.getValue());
                } else {
                    updated += insertReminder(connection, event, userId, entry.getValue());
                }
            }
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(connection, updated);
        }
    }

    private int insertReminder(Connection connection, Event event, int userID, List<Alarm> alarms) throws OXException, SQLException {
        ReminderData reminder = getNextReminder(event, userID, alarms, null);
        if (null == reminder) {
            return 0;
        }
        int updated = 0;
        ReminderData originalReminder = selectReminder(connection, context.getContextId(), asInt(event.getId()), userID);
        if (null == originalReminder || 0 == originalReminder.id) {
            updated += insertReminder(connection, context.getContextId(), event, userID, reminder);
        } else {
            ReminderData updatedReminder = getNextReminder(event, userID, alarms, originalReminder);
            if (null == updatedReminder) {
                updated += deleteReminderMinutes(connection, context.getContextId(), asInt(event.getId()), new int[] { userID });
                updated += deleteReminderTriggers(connection, context.getContextId(), asInt(event.getId()), new int[] { userID });
            } else {
                updated += updateReminderMinutes(connection, context.getContextId(), event, userID, updatedReminder.reminderMinutes);
                updated += updateReminderTrigger(connection, context.getContextId(), event, userID, updatedReminder.nextTriggerTime);
            }
        }
        return updated;
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
        List<Alarm> displayAlarms = filter(alarms, AlarmAction.DISPLAY);
        if (null == displayAlarms || 0 == displayAlarms.size()) {
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
            }
        }
        /*
         * regular alarm, only
         */
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

    private static int insertReminder(Connection connection, int contextID, Event event, int userID, ReminderData reminder) throws SQLException {
        int updated = 0;
        try (PreparedStatement stmt = connection.prepareStatement("INSERT INTO reminder (cid,object_id,last_modified,target_id,module,userid,alarm,recurrence,folder) VALUES (?,?,?,?,?,?,?,?,?);")) {
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
            stmt.setInt(1, reminder.reminderMinutes);
            stmt.setInt(2, contextID);
            stmt.setInt(3, asInt(event.getId()));
            stmt.setInt(4, userID);
            updated += logExecuteUpdate(stmt);
        }
        return updated;
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

    @Override
    public void insertTriggers(Map<String, Map<Integer, List<Alarm>>> alarms, List<Event> events) throws OXException {
        for (Event eve : events) {
            insertTriggers(eve, alarms.get(eve.getId()));
        }
    }

    @Override
    public void deleteTriggers(String eventId) throws OXException {
        deleteTriggers(Collections.singletonList(eventId));
    }

    @Override
    public void deleteTriggers(List<String> eventIds) throws OXException {
        Connection con = dbProvider.getWriteConnection(context);
        int updated = 0;
        try {
            updated = deleteReminderTriggers(con, context.getContextId(), eventIds);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(con, updated);
        }
    }

    @Override
    public void deleteTriggers(List<String> eventIds, int userId) throws OXException {
        Connection con = dbProvider.getWriteConnection(context);
        int updated = 0;
        try {
            for (String eventId : eventIds) {
                updated += deleteReminderTriggers(con, context.getContextId(), Integer.valueOf(eventId), new int[] { userId });
            }
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(con, updated);
        }
    }

    @Override
    public void deleteTriggers(int userId) throws OXException {
        Connection con = dbProvider.getWriteConnection(context);
        int updated = 0;
        try {
            updated = deleteReminderTriggers(con, context.getContextId(), userId);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(con, updated);
        }
    }

    @Override
    public boolean deleteAllTriggers() throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<AlarmTrigger> loadTriggers(int userId, Date until) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectTriggers(connection, context.getContextId(), userId, until);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    @Override
    public AlarmTrigger loadTrigger(int id) throws OXException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Integer recalculateFloatingAlarmTriggers(int userId) throws OXException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> hasTriggers(int userId, String[] eventIds) throws OXException {
        // not implemented in legacy storage
        return Collections.emptySet();
    }

    private List<AlarmTrigger> selectTriggers(Connection connection, int contextID, int userID, Date until) throws SQLException, OXException {
        List<AlarmTrigger> triggers = new ArrayList<AlarmTrigger>();
        String sql = new StringBuilder()
            .append("SELECT r.object_id,r.target_id,r.alarm,r.folder,r.recurrence,p.reminder ")
            .append("FROM reminder AS r JOIN prg_dates_members AS p ON r.target_id=p.object_id AND r.userid=p.member_uid AND r.cid=p.cid ")
            .append("WHERE r.cid=? AND r.userid=?")
            .append(null != until ? " AND r.alarm<?" : "").append("AND r.module=?")
        .toString();
        RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            if (null != until) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(until.getTime()));
            }
            stmt.setInt(parameterIndex++, REMINDER_MODULE);
            try (ResultSet resultSet = logExecuteQuery(stmt)) {
                while (resultSet.next()) {
                    AlarmTrigger trigger = new AlarmTrigger();
                    trigger.setAction(AlarmAction.DISPLAY.getValue());
                    trigger.setUserId(I(userID));
                    trigger.setPushed(Boolean.FALSE);
                    trigger.setAlarm(I(resultSet.getInt("object_id")));
                    int targetId = resultSet.getInt("target_id");
                    trigger.setEventId(String.valueOf(targetId));
                    trigger.setFolder(resultSet.getString("folder"));
                    trigger.setTime(L(resultSet.getTimestamp("alarm").getTime()));

                    if(resultSet.getInt("recurrence")>0) {
                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calendar.setTime(new Date(trigger.getTime()));
                        calendar.add(Calendar.MINUTE, -1);

                        RecurrenceData data = eventStorage.selectRecurrenceData(connection, targetId, false);
                        if (null != data) {
                            int reminder = resultSet.getInt("reminder");
                            RecurrenceIterator<RecurrenceId> iterator = recurrenceService.iterateRecurrenceIds(data, new Date(calendar.getTime().getTime()+TimeUnit.MINUTES.toMillis(reminder)), null);

                            if (iterator.hasNext()) {
                                trigger.setRecurrenceId(iterator.next());
                            }
                        }
                    }

                    triggers.add(trigger);
                }
            }
        }
        return triggers;
    }

    private static ReminderData selectReminder(Connection connection, int contextID, int eventID, int userID) throws SQLException {
        String sql = new StringBuilder().append("SELECT m.reminder,r.object_id,r.alarm,r.last_modified FROM prg_dates_members AS m ").append("LEFT JOIN reminder AS r ON m.cid=r.cid AND m.member_uid=r.userid AND m.object_id=r.target_id ").append("WHERE m.cid=? AND m.member_uid=? AND m.object_id=?;").toString();
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

    private static int updateReminderMinutes(Connection connection, int contextID, Event event, int userID, int reminderMinutes) throws SQLException {
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
        StringBuilder stringBuilder = new StringBuilder().append("DELETE FROM reminder WHERE cid=? AND module=? AND target_id").append(Databases.getPlaceholders(eventIds.size())).append(';');
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
        String sql = new StringBuilder().append("DELETE FROM reminder WHERE cid=? AND module=? AND target_id=? AND userid IN (").append(getParameters(userIDs.length)).append(");").toString();
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

    private static int deleteReminderMinutes(Connection connection, int contextID, int eventID, int[] userIDs) throws SQLException {
        String sql = new StringBuilder().append("UPDATE prg_dates_members SET reminder=? WHERE cid=? AND object_id=? AND member_uid IN (").append(getParameters(userIDs.length)).append(");").toString();
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

    private static int updateReminderTrigger(Connection connection, int contextID, Event event, int userID, long triggerTime) throws SQLException {
        String sql = "INSERT INTO reminder (cid,object_id,last_modified,target_id,module,userid,alarm,recurrence,folder) VALUES (?,?,?,?,?,?,?,?,?) " + "ON DUPLICATE KEY UPDATE last_modified=?,alarm=?,recurrence=?,folder=?;";
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

    /**
     * Safely releases a write connection obeying the configured transaction policy, rolling back automatically if not committed before.
     *
     * @param connection The write connection to release
     * @param updated The number of actually updated rows to
     */
    @Override
    protected void release(Connection connection, int updated) throws OXException {
        if (null != connection) {
            try {
                if (false == connection.getAutoCommit()) {
                    txPolicy.rollback(connection);
                }
                txPolicy.setAutoCommit(connection, true);
            } catch (SQLException e) {
                throw asOXException(e);
            } finally {
                if (0 < updated) {
                    dbProvider.releaseWriteConnection(context, connection);
                } else {
                    dbProvider.releaseWriteConnectionAfterReading(context, connection);
                }
            }
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
    public void deleteTriggersById(List<Integer> alarmIds) throws OXException {
        Connection con = dbProvider.getWriteConnection(context);
        int updated = 0;
        try {
            updated = deleteReminderTriggersById(con, context.getContextId(), alarmIds);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(con, updated);
        }
    }

    /**
     * Deletes the given reminders
     *
     * @param con The connection
     * @param contextId The context id
     * @param alarmIds The reminder ids
     * @return the number of changed items
     */
    private int deleteReminderTriggersById(Connection con, int contextId, List<Integer> alarmIds) throws SQLException {
        if (null == alarmIds || 0 == alarmIds.size()) {
            return 0;
        }
        StringBuilder stringBuilder = new StringBuilder().append("DELETE FROM reminder WHERE cid=? AND object_id").append(Databases.getPlaceholders(alarmIds.size())).append(';');
        try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextId);
            for (int id : alarmIds) {
                stmt.setInt(parameterIndex++, id);
            }
            return logExecuteUpdate(stmt);
        }
    }

}
