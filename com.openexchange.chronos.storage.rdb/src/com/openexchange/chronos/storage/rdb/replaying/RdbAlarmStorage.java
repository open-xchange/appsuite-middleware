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

package com.openexchange.chronos.storage.rdb.replaying;

import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;

/**
 * {@link CalendarStorage}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class RdbAlarmStorage implements AlarmStorage {

    private final AlarmStorage delegate;
    private final AlarmStorage legacyDelegate;

    /**
     * Initializes a new {@link RdbAlarmStorage}.
     *
     * @param delegate The delegate storage
     * @param legacyDelegate The legacy delegate storage
     */
    public RdbAlarmStorage(AlarmStorage delegate, AlarmStorage legacyDelegate) {
        super();
        this.delegate = delegate;
        this.legacyDelegate = legacyDelegate;
    }

    @Override
    public Map<Integer, List<Alarm>> loadAlarms(Event event) throws OXException {
        return delegate.loadAlarms(event);
    }

    @Override
    public List<Alarm> loadAlarms(Event event, int userID) throws OXException {
        return delegate.loadAlarms(event, userID);
    }

    @Override
    public Map<String, List<Alarm>> loadAlarms(List<Event> events, int userID) throws OXException {
        return delegate.loadAlarms(events, userID);
    }

    @Override
    public Map<String, Map<Integer, List<Alarm>>> loadAlarms(List<Event> events) throws OXException {
        return delegate.loadAlarms(events);
    }

    @Override
    public int nextId() throws OXException {
        return delegate.nextId();
    }

    @Override
    public void insertAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        delegate.insertAlarms(event, userID, alarms);
        legacyDelegate.insertAlarms(event, userID, alarms);
    }

    @Override
    public void insertAlarms(Event event, Map<Integer, List<Alarm>> alarmsByUserId) throws OXException {
        delegate.insertAlarms(event, alarmsByUserId);
        legacyDelegate.insertAlarms(event, alarmsByUserId);
    }

    @Override
    public void insertAlarms(Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId) throws OXException {
        delegate.insertAlarms(alarmsByUserByEventId);
        legacyDelegate.insertAlarms(alarmsByUserByEventId);
    }

    @Override
    public void updateAlarms(Event event, int userID, List<Alarm> alarms) throws OXException {
        delegate.updateAlarms(event, userID, alarms);
        legacyDelegate.updateAlarms(event, userID, alarms);
    }

    @Override
    public void deleteAlarms(String eventId) throws OXException {
        delegate.deleteAlarms(eventId);
        legacyDelegate.deleteAlarms(eventId);
    }

    @Override
    public void deleteAlarms(List<String> eventIds) throws OXException {
        delegate.deleteAlarms(eventIds);
        legacyDelegate.deleteAlarms(eventIds);
    }

    @Override
    public void deleteAlarms(String eventId, int userId) throws OXException {
        delegate.deleteAlarms(eventId, userId);
        legacyDelegate.deleteAlarms(eventId, userId);
    }

    @Override
    public void deleteAlarms(String eventId, int[] userIds) throws OXException {
        delegate.deleteAlarms(eventId, userIds);
        legacyDelegate.deleteAlarms(eventId, userIds);
    }

    @Override
    public void deleteAlarms(String eventId, int userId, int[] alarmIds) throws OXException {
        delegate.deleteAlarms(eventId, userId, alarmIds);
        legacyDelegate.deleteAlarms(eventId, userId, alarmIds);
    }

    @Override
    public void deleteAlarms(int userId) throws OXException {
        delegate.deleteAlarms(userId);
        legacyDelegate.deleteAlarms(userId);
    }

    @Override
    public boolean deleteAllAlarms() throws OXException {
        return delegate.deleteAllAlarms();
    }

    @Override
    public Alarm loadAlarm(int alarmId) throws OXException {
        return delegate.loadAlarm(alarmId);
    }

    @Override
    public long getLatestTimestamp(int userId) throws OXException {
        return delegate.getLatestTimestamp(userId);
    }

    @Override
    public long getLatestTimestamp(String eventId, int userId) throws OXException {
        return delegate.getLatestTimestamp(eventId, userId);
    }

    @Override
    public Map<String, Long> getLatestTimestamp(List<String> eventIds, int userId) throws OXException {
        return delegate.getLatestTimestamp(eventIds, userId);
    }

}
