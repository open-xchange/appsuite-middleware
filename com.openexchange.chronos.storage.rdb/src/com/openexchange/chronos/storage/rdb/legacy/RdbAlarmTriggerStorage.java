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

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.java.Autoboxing.l;
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
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmAction;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.RecurrenceId;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.RecurrenceData;
import com.openexchange.chronos.service.RecurrenceIterator;
import com.openexchange.chronos.service.RecurrenceService;
import com.openexchange.chronos.storage.AlarmTriggerStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.rdb.RdbStorage;
import com.openexchange.chronos.storage.rdb.osgi.Services;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.contexts.Context;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAlarmTriggerStorage extends RdbStorage implements AlarmTriggerStorage {

    /** The module identifier used in the <code>reminder</code> table */
    private static final int REMINDER_MODULE = Types.APPOINTMENT;

    private final RdbEventStorage eventStorage;

    /**
     * Initializes a new {@link RdbAlarmTriggerStorage}.
     *
     * @param context The context
     * @param entityResolver The entity resolver to use
     * @param dbProvider The database provider to use
     * @param txPolicy The transaction policy
     */
    public RdbAlarmTriggerStorage(Context context, EntityResolver entityResolver, DBProvider dbProvider, DBTransactionPolicy txPolicy, RdbEventStorage eventStorage) {
        super(context, dbProvider, txPolicy);
        this.eventStorage = eventStorage;
    }

    @Override
    public void insertTriggers(Event event, Map<Integer, List<Alarm>> alarmsPerUserId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void insertTriggers(Map<String, Map<Integer, List<Alarm>>> alarms, List<Event> events) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteTriggers(String eventId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteTriggers(List<String> eventIds) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteTriggers(List<String> eventIds, int userId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public void deleteTriggers(int userId) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public boolean deleteAllTriggers() throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

    @Override
    public List<AlarmTrigger> loadTriggers(int userId, Date from, Date until) throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return selectTriggers(connection, context.getContextId(), userId, from, until);
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

    private List<AlarmTrigger> selectTriggers(Connection connection, int contextID, int userID, Date from, Date until) throws SQLException, OXException {
        List<AlarmTrigger> triggers = new ArrayList<AlarmTrigger>();
        String sql = new StringBuilder()
            .append("SELECT r.object_id,r.target_id,r.alarm,r.folder,r.recurrence,p.reminder ")
            .append("FROM reminder AS r JOIN prg_dates_members AS p ON r.target_id=p.object_id AND r.userid=p.member_uid AND r.cid=p.cid ")
            .append("WHERE r.cid=? AND r.userid=?")
            .append(null != until ? " AND r.alarm<?" : "")
            .append(null != from ? " AND (r.recurrence=0 OR r.alarm>=?)" : "")
            .append("AND r.module=?")
        .toString();
        RecurrenceService recurrenceService = Services.getService(RecurrenceService.class);
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int parameterIndex = 1;
            stmt.setInt(parameterIndex++, contextID);
            stmt.setInt(parameterIndex++, userID);
            if (null != until) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(until.getTime()));
            }
            if (null != from) {
                stmt.setTimestamp(parameterIndex++, new Timestamp(from.getTime()));
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

                    if (resultSet.getInt("recurrence")>0) {
                        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        calendar.setTime(new Date(l(trigger.getTime())));
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

    @Override
    public void deleteTriggersById(List<Integer> alarmIds) throws OXException {
        throw CalendarExceptionCodes.STORAGE_NOT_AVAILABLE.create("'Legacy' storage is operating in read-only mode.");
    }

}
