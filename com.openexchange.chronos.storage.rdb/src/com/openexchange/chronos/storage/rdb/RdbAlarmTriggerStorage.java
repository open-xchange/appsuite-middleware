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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.AlarmTriggerField;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventExceptionWrapper;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.common.DefaultRecurrenceData;
import com.openexchange.chronos.common.DefaultRecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.exception.ProblemSeverity;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.RangeOption;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.AlarmTriggerStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.database.Databases;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;

/**
 * {@link RdbAlarmTriggerStorage} is an implementation of the {@link AlarmTriggerStorage}
 * which uses a sql database to store alarm triggers.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.10.0
 */
public class RdbAlarmTriggerStorage extends RdbStorage implements AlarmTriggerStorage {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RdbAlarmTriggerStorage.class);
    private static final AlarmTriggerDBMapper MAPPER = AlarmTriggerDBMapper.getInstance();
    private final int accountId;
    private final AlarmStorage alarmStorage;
    private final RecurrenceService recurrenceService;
    private final EntityResolver resolver;

    private static final String SQL_DELETE = "DELETE FROM calendar_alarm_trigger WHERE cid=? AND account=? AND eventId=?;";

    /**
     * Initializes a new {@link RdbAlarmTriggerStorage}.
     *
     * @param context The context id
     * @param accountId The account id
     * @param dbProvider A db provider
     * @param txPolicy The transaction policy
     * @throws OXException
     */
    protected RdbAlarmTriggerStorage(Context context, int accountId, DBProvider dbProvider, DBTransactionPolicy txPolicy, AlarmStorage alarmStorage, EntityResolver resolver) throws OXException {
        super(context, dbProvider, txPolicy);
        this.accountId = accountId;
        this.alarmStorage = alarmStorage;
        this.recurrenceService = Services.getService(RecurrenceService.class, true);
        this.resolver = resolver;
    }

    /**
     * Inserts the alarm trigger
     *
     * @param trigger The {@link AlarmTrigger}
     * @throws OXException
     */
    private void insertAlarmTrigger(AlarmTrigger trigger) throws OXException {
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

    /**
     * Updates the alarm trigger
     *
     * @param trigger The updated {@link AlarmTrigger}
     * @throws OXException
     */
    @SuppressWarnings("unused")
    private void updateAlarmTrigger(AlarmTrigger trigger) throws OXException {
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

    /**
     * Lists alarm triggers for the given user from now until the given time in ascending order
     *
     * @param user The user id
     * @param options The range options
     * @return A list of {@link AlarmTrigger}
     * @throws OXException
     */
    private List<AlarmTrigger> getAlarmTriggers(int user, RangeOption options) throws OXException {
        Connection con = dbProvider.getReadConnection(context);
        try {
            return getAlarmTriggers(user, options.getFrom() != null ? options.getFrom().getTime() : System.currentTimeMillis(), options.getUntil().getTime(), con);
        } finally {
            dbProvider.releaseReadConnection(context, con);
        }
    }

    private List<AlarmTrigger> getAlarmTriggers(int user, Long from, Long until, Connection con) throws OXException {
        try {
            AlarmTriggerField[] mappedFields = MAPPER.getMappedFields();
            StringBuilder stringBuilder = new StringBuilder().append("SELECT account,cid,").append(MAPPER.getColumns(mappedFields)).append(" FROM ").append("calendar_alarm_trigger").append(" WHERE cid=? AND user=? AND triggerDate>? AND triggerDate<? ORDER BY triggerDate");

            List<AlarmTrigger> alrmTriggers = new ArrayList<AlarmTrigger>();
            try (PreparedStatement stmt = con.prepareStatement(stringBuilder.toString())) {
                int parameterIndex = 1;
                stmt.setInt(parameterIndex++, context.getContextId());
                stmt.setInt(parameterIndex++, user);
                stmt.setLong(parameterIndex++, from);
                stmt.setLong(parameterIndex++, until);

                try (ResultSet resultSet = logExecuteQuery(stmt)) {
                    while (resultSet.next()) {
                        alrmTriggers.add(readTrigger(resultSet, mappedFields));
                    }
                }
            }
            return alrmTriggers;
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    };

    private AlarmTrigger readTrigger(ResultSet resultSet, AlarmTriggerField... fields) throws SQLException, OXException {
        AlarmTrigger result = MAPPER.fromResultSet(resultSet, fields);
        result.setAccount(accountId);
        result.setContextId(context.getContextId());
        return result;
    }

    /**
     * Deletes all alarm triggers for the given event
     *
     * @param eventId The event id to delete
     * @throws OXException
     */
    private void deleteAlarmTriggers(String eventId) throws OXException {
        Connection writeCon = dbProvider.getWriteConnection(context);
        int deleted = 0;
        try {
            txPolicy.setAutoCommit(writeCon, false);
            deleted = deleteAlarmTriggers(context.getContextId(), accountId, eventId, writeCon);
            txPolicy.commit(writeCon);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(writeCon, deleted);
        }

    }

    private int deleteAlarmTriggers(int contextId, int accountId, String eventId, Connection writeCon) throws OXException {
        if (!Strings.isEmpty(eventId)) {
            PreparedStatement prepareStatement = null;

            try {
                prepareStatement = writeCon.prepareStatement(SQL_DELETE);
                int x = 0;
                prepareStatement.setInt(++x, contextId);
                prepareStatement.setInt(++x, accountId);
                prepareStatement.setString(++x, eventId);
                return logExecuteUpdate(prepareStatement);
            } catch (SQLException e) {
                throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
            } finally {
                Databases.closeSQLStuff(prepareStatement);
            }
        }

        return 0;
    }

    @Override
    public void insertTriggers(Event event, Set<RecurrenceId> exceptions) throws OXException {
        EventExceptionWrapper wrapper = new EventExceptionWrapper(event, exceptions);
        Map<Integer, List<Alarm>> alarmsPerAttendee = alarmStorage.loadAlarms(event);
        insertTriggers(alarmsPerAttendee, wrapper);
    }

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    /**
     * Creates new alarm triggers
     *
     * @param alarmsPerAttendee A map of alarms per attendee
     * @param eventWrapper The newly created event
     * @throws OXException
     */
    private void insertTriggers(Map<Integer, List<Alarm>> alarmsPerAttendee, EventExceptionWrapper eventWrapper) throws OXException {
        Event event = eventWrapper.getEvent();
        for (Integer userId : alarmsPerAttendee.keySet()) {

            List<Alarm> alarms = alarmsPerAttendee.get(userId);
            if (alarms == null || alarms.isEmpty()) {
                continue;
            }
            for (Alarm alarm : alarms) {
                AlarmTrigger trigger = new AlarmTrigger();
                trigger.setUserId(userId);
                trigger.setAction(alarm.getAction().getValue());
                trigger.setProcessed(false);
                trigger.setAlarm(alarm.getId());
                trigger.setEventId(event.getId());

                if (CalendarUtils.isFloating(event)) {
                    trigger.setTimezone(resolver.getTimeZone(userId));
                }
                TimeZone tz = UTC;
                if (trigger.containsTimezone()) {
                    tz = trigger.getTimezone();
                }
                if (event.containsRecurrenceRule() && event.getRecurrenceRule() != null && event.getRecurrenceId() == null && event.getId().equals(event.getSeriesId())) {

                    long[] exceptions = null;
                    if (eventWrapper.getExceptions() != null) {
                        exceptions = new long[eventWrapper.getExceptions().size()];
                        int x = 0;
                        for (RecurrenceId recurrenceId : eventWrapper.getExceptions()) {
                            exceptions[x++] = recurrenceId.getValue().getTimestamp();
                        }
                    }
                    RecurrenceData data = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), exceptions);
                    RecurrenceIterator<RecurrenceId> iterateRecurrenceIds = recurrenceService.iterateRecurrenceIds(data, new Date(), null);
                    if(!iterateRecurrenceIds.hasNext()){
                        continue;
                    }

                    RecurrenceId recurrenceId = new DefaultRecurrenceId(iterateRecurrenceIds.next().getValue());
                    trigger.setRecurrence(recurrenceId);
                    addRelatedDate(alarm, event, trigger);

                    trigger.setTime(AlarmUtils.getNextTriggerTime(event, alarm, new Date(), tz, recurrenceService, eventWrapper.getExceptions()).getTime());
                } else {
                    if (event.getRecurrenceId() != null) {
                        trigger.setRecurrence(event.getRecurrenceId());
                    }
                    addRelatedDate(alarm, event, trigger);
                    trigger.setTime(AlarmUtils.getTriggerTime(alarm.getTrigger(), event, tz).getTime());
                }

                // Set proper folder id
                try {
                    trigger.setFolder(getFolderId(event, userId));
                } catch (OXException e) {
                    addInvalidDataWaring(event.getId(), EventField.ALARMS, ProblemSeverity.MINOR, "Unable to determine parent folder for user \"" + userId + "\", skipping insertion of alarm triggers", e);
                    continue;
                }

                insertAlarmTrigger(trigger);
            }

        }

    }

    private static String getFolderId(Event event, int userId) throws OXException {
        String folderId = CalendarUtils.getFolderView(event, userId);
        if (null == folderId) {
            throw CalendarExceptionCodes.UNEXPECTED_ERROR.create("No folder view for user " + userId);
        }
        return folderId;
    }

    private void addRelatedDate(Alarm alarm, Event event, AlarmTrigger trigger){
        if(alarm.getTrigger().getDateTime()==null){
            trigger.setRelatedTime(AlarmUtils.getRelatedDate(alarm.getTrigger().getRelated(), event).getTimestamp());
        }
    }


    @Override
    public void removeTriggers(String eventId) throws OXException {
        deleteAlarmTriggers(eventId);
    }

    @Override
    public List<AlarmTrigger> loadTriggers(int userId, RangeOption option) throws OXException {
        return getAlarmTriggers(userId, option);
    }

    @Override
    public Integer recalculateFloatingAlarmTriggers(int userId) throws OXException {
        Connection writeCon = dbProvider.getWriteConnection(context);
        int updated = 0;
        try {
            txPolicy.setAutoCommit(writeCon, false);
            updated = recalculateFloatingAlarmTriggers(userId, writeCon);
            txPolicy.commit(writeCon);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(writeCon, updated);
        }
        return updated;

    }

    private static final String FLOATING_TRIGGER_SQL = "SELECT alarm, triggerDate, floatingTimezone, relatedTime FROM calendar_alarm_trigger WHERE cid=? AND account=? AND user=? AND floatingTimezone IS NOT NULL;";
    private static final String UPDATE_TRIGGER_TIME_SQL = "UPDATE calendar_alarm_trigger SET triggerDate=?, floatingTimezone=? WHERE cid=? AND account=? AND alarm=?;";

    private int recalculateFloatingAlarmTriggers(int userId, Connection writeCon) throws OXException, SQLException {
        ResultSet resultSet = null;
        PreparedStatement stmt = null;
        List<AlarmTrigger> triggers = new ArrayList<>();
        try {
            stmt = writeCon.prepareStatement(FLOATING_TRIGGER_SQL);
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, accountId);
            stmt.setInt(parameterIndex++, userId);
            resultSet = logExecuteQuery(stmt);
            while (resultSet.next()) {
                triggers.add(readTrigger(resultSet, AlarmTriggerField.ALARM_ID, AlarmTriggerField.TIME, AlarmTriggerField.FLOATING_TIMEZONE, AlarmTriggerField.RELATED_TIME));
            }

            if (triggers.isEmpty()) {
                return 0;
            }

        } finally {
            Databases.closeSQLStuff(resultSet, stmt);
        }

        try {
            stmt = writeCon.prepareCall(UPDATE_TRIGGER_TIME_SQL);

            for (AlarmTrigger trigger : triggers) {
                int index = 1;
                TimeZone oldTimeZone = trigger.getTimezone();
                TimeZone newTimeZone = resolver.getTimeZone(userId);
                int offsetOld = oldTimeZone.getOffset(trigger.getRelatedTime());
                int offsetNew = newTimeZone.getOffset(trigger.getRelatedTime());
                int dif = offsetOld - offsetNew;
                long newTriggerTime = trigger.getTime() + dif;
                stmt.setLong(index++, newTriggerTime);
                stmt.setString(index++, newTimeZone.getID());
                stmt.setInt(index++, context.getContextId());
                stmt.setInt(index++, accountId);
                stmt.setInt(index++, trigger.getAlarm());
                stmt.addBatch();
            }

            int[] executeBatch = stmt.executeBatch();
            int result = 0;
            for(int x: executeBatch){
                result+=x;
            }
            return result;
        } finally {
            Databases.closeSQLStuff(stmt);
        }
    }

    @Override
    public void insertTriggers(Map<String, Map<Integer, List<Alarm>>> alarmMap, List<Event> events, Map<String, Set<RecurrenceId>> exceptionsMap) throws OXException {

        Connection writeCon = dbProvider.getWriteConnection(context);
        int updated = 0;
        try {
            txPolicy.setAutoCommit(writeCon, false);
            updated = insertTrigger(alarmMap, events, exceptionsMap, writeCon);
            txPolicy.commit(writeCon);
        } catch (SQLException e) {
            throw asOXException(e);
        } finally {
            release(writeCon, updated);
        }


    }

    private int insertTrigger(Map<String, Map<Integer, List<Alarm>>> alarmMap, List<Event> events, Map<String, Set<RecurrenceId>> exceptionsMap, Connection writeCon) throws OXException, SQLException {
        PreparedStatement stmt = getBatchStmt(writeCon);
        for (Event event : events) {

            Map<Integer, List<Alarm>> alarmsPerAttendee = alarmMap.get(event.getId());
            if (alarmsPerAttendee == null) {
                continue;
            }
            Set<RecurrenceId> exceptionSet = null;
            RecurrenceId recurrenceId = null;
            if (event.containsRecurrenceRule() && event.getRecurrenceRule() != null && event.getRecurrenceId() == null && event.getId().equals(event.getSeriesId())) {

                long[] exceptions = null;
                exceptionSet = exceptionsMap.get(event.getId());
                if (exceptionSet != null) {
                    exceptions = new long[exceptionSet.size()];
                    int x = 0;
                    for (RecurrenceId recId : exceptionSet) {
                        exceptions[x++] = recId.getValue().getTimestamp();
                    }
                }
                RecurrenceData data = new DefaultRecurrenceData(event.getRecurrenceRule(), event.getStartDate(), exceptions);
                RecurrenceIterator<RecurrenceId> iterateRecurrenceIds = recurrenceService.iterateRecurrenceIds(data, new Date(), null);
                if(!iterateRecurrenceIds.hasNext()){
                    continue;
                }
                recurrenceId = new DefaultRecurrenceId(iterateRecurrenceIds.next().getValue());

            }

            for (Integer userId : alarmsPerAttendee.keySet()) {

                List<Alarm> alarms = alarmsPerAttendee.get(userId);
                if (alarms == null || alarms.isEmpty()) {
                    continue;
                }
                for (Alarm alarm : alarms) {
                    AlarmTrigger trigger = new AlarmTrigger();
                    trigger.setUserId(userId);
                    trigger.setAction(alarm.getAction().getValue());
                    trigger.setProcessed(false);
                    trigger.setAlarm(alarm.getId());
                    trigger.setEventId(event.getId());

                    if (CalendarUtils.isFloating(event)) {
                        trigger.setTimezone(resolver.getTimeZone(userId));
                    }
                    TimeZone tz = UTC;
                    if (trigger.containsTimezone()) {
                        tz = trigger.getTimezone();
                    }

                    if (event.containsRecurrenceRule() && event.getRecurrenceRule() != null && event.getRecurrenceId() == null && event.getId().equals(event.getSeriesId())) {
                        long triggerTime = AlarmUtils.getNextTriggerTime(event, alarm, new Date(), tz, recurrenceService, exceptionSet).getTime();
                        if(triggerTime <= System.currentTimeMillis()){
                            continue;
                        }
                        addRelatedDate(alarm, event, trigger);
                        trigger.setRecurrence(recurrenceId);
                        trigger.setTime(triggerTime);
                    } else {
                        long triggerTime = AlarmUtils.getTriggerTime(alarm.getTrigger(), event, tz).getTime();
                        if(triggerTime <= System.currentTimeMillis()){
                            continue;
                        }
                        trigger.setTime(triggerTime);
                        if (event.getRecurrenceId() != null) {
                            trigger.setRecurrence(event.getRecurrenceId());
                        }
                        addRelatedDate(alarm, event, trigger);
                    }

                    // Set proper folder id
                    try {
                        trigger.setFolder(getFolderId(event, userId));
                    } catch (OXException e) {
                        addInvalidDataWaring(event.getId(), EventField.ALARMS, ProblemSeverity.MINOR, "Unable to determine parent folder for user \"" + userId + "\", skipping insertion of alarm triggers", e);
                        continue;
                    }

                    addBatch(trigger, stmt);
                }
            }
        }
        int[] executeBatch = stmt.executeBatch();
        int result = 0;
        for(int i: executeBatch){
            result+=i;
        }
        return result;

    }

    private PreparedStatement getBatchStmt(Connection writeCon) throws OXException {
        try {
            AlarmTriggerField[] mappedFields = MAPPER.getMappedFields();
            String sql = new StringBuilder().append("INSERT INTO calendar_alarm_trigger (cid, account, ").append(MAPPER.getColumns(mappedFields)).append(") VALUES (?,?,").append(MAPPER.getParameters(mappedFields)).append(");").toString();
            return writeCon.prepareStatement(sql);
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }

    private void addBatch(AlarmTrigger trigger, PreparedStatement stmt) throws OXException {
        try {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, context.getContextId());
            stmt.setInt(parameterIndex++, accountId);
            parameterIndex = MAPPER.setParameters(stmt, parameterIndex, trigger, MAPPER.getMappedFields());
            stmt.addBatch();
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
    }


}
