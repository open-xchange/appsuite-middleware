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
