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

package com.openexchange.chronos.provider.common;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import static com.openexchange.chronos.common.CalendarUtils.getAlarmIDs;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.AlarmField;
import com.openexchange.chronos.AlarmTrigger;
import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.EventFlag;
import com.openexchange.chronos.ExtendedProperty;
import com.openexchange.chronos.common.AlarmUtils;
import com.openexchange.chronos.common.UpdateResultImpl;
import com.openexchange.chronos.common.UserConfigWrapper;
import com.openexchange.chronos.common.mapping.AlarmMapper;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.service.CollectionUpdate;
import com.openexchange.chronos.service.ItemUpdate;
import com.openexchange.chronos.service.UpdateResult;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.chronos.storage.operation.OSGiCalendarStorageOperation;
import com.openexchange.conversion.ConversionService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.osgi.Tools;
import com.openexchange.server.ServiceLookup;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * {@link AlarmHelper}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since 7.10.0
 */
public class AlarmHelper {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AlarmHelper.class);

    private final ServiceLookup services;
    private final Context context;
    final CalendarAccount account;

    /**
     * Initializes a new {@link AlarmHelper}.
     *
     * @param services The service lookup reference to use
     * @param context The context
     * @param account The calendar account
     */
    public AlarmHelper(ServiceLookup services, Context context, CalendarAccount account) {
        super();
        this.services = services;
        this.context = context;
        this.account = account;
    }

    /**
     * Loads and applies all alarm data of the account user associated with the supplied event.
     * <p/>
     * In case alarms are set, also the event's {@link EventField#TIMESTAMP} property is adjusted dynamically so that the maximum of the
     * timestamps is returned implicitly. Similarly, the event's {@link EventField#FLAGS} property is adjusted, too.
     * 
     * @param event The event to load and apply the alarm data for
     * @return The event, enhanced with the loaded alarm data
     */
    public Event applyAlarms(Event event) throws OXException {
        return applyAlarms(event, loadAlarms(Collections.singletonList(event)).get(event.getId()));
    }

    /**
     * Loads and applies all alarm data of the account user associated with the supplied events.
     * <p/>
     * In case alarms are set, also the event's {@link EventField#TIMESTAMP} property is adjusted dynamically so that the maximum of the
     * timestamps is returned implicitly. Similarly, the event's {@link EventField#FLAGS} property is adjusted, too.
     *
     * @param events The events to load and apply the alarm data for
     * @return The events, enhanced with the loaded alarm data
     */
    public List<Event> applyAlarms(List<Event> events) throws OXException {
        if (null == events || 0 == events.size()) {
            return events;
        }
        Map<String, List<Alarm>> alarmsById = loadAlarms(events);
        if (alarmsById.isEmpty()) {
            return events;
        }
        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            List<Alarm> alarms = alarmsById.get(event.getId());
            if (null != alarms && 0 < alarms.size()) {
                events.set(i, applyAlarms(event, alarms));
            }
        }
        return events;
    }

    /**
     * Loads all alarm data of the account user associated with the supplied events.
     *
     * @param events The events to load the alarm data for
     * @return The alarm data, mapped to the each event identifier
     */
    public Map<String, List<Alarm>> loadAlarms(final List<Event> events) throws OXException {
        return new OSGiCalendarStorageOperation<Map<String, List<Alarm>>>(services, context.getContextId(), account.getAccountId()) {

            @Override
            protected Map<String, List<Alarm>> call(CalendarStorage storage) throws OXException {
                return storage.getAlarmStorage().loadAlarms(events, account.getUserId());
            }
        }.executeQuery();
    }

    /**
     * Deletes stored alarm- and trigger data associated with a specific event of the calendar account.
     *
     * @param eventId The identifier of the event to delete the alarms for
     */
    public void deleteAlarms(final String eventId) throws OXException {
        new OSGiCalendarStorageOperation<Void>(services, context.getContextId(), account.getAccountId()) {

            @Override
            protected Void call(CalendarStorage storage) throws OXException {
                storage.getAlarmStorage().deleteAlarms(eventId, account.getUserId());
                storage.getAlarmTriggerStorage().deleteTriggers(eventId);
                return null;
            }
        }.executeUpdate();
    }

    /**
     * Deletes stored alarm- and trigger data associated with any event of the calendar account.
     */
    public void deleteAllAlarms() throws OXException {
        new OSGiCalendarStorageOperation<Void>(services, context.getContextId(), account.getAccountId()) {

            @Override
            protected Void call(CalendarStorage storage) throws OXException {
                storage.getAlarmStorage().deleteAlarms(account.getUserId());
                storage.getAlarmTriggerStorage().deleteTriggers(account.getUserId());
                return null;
            }
        }.executeUpdate();
    }

    /**
     * Gets a value indicating whether default alarms are configured for the calendar account or not.
     *
     * @return <code>true</code> if default alarms are configured, <code>false</code>, otherwise
     */
    public boolean hasDefaultAlarms() {
        List<Alarm> defaultAlarms = getDefaultAlarms();
        return null != defaultAlarms && 0 < defaultAlarms.size();
    }

    /**
     * Gets the default alarms configured in the calendar account.
     *
     * @return The default alarms, or <code>null</code> if none are defined
     */
    public List<Alarm> getDefaultAlarms() {
        try {
            UserConfigWrapper configWrapper = new UserConfigWrapper(Tools.requireService(ConversionService.class, services), account.getUserConfiguration());
            return configWrapper.getDefaultAlarmDate();
        } catch (Exception e) {
            LOG.warn("Error getting default alarm from user configuration \"{}\": {}", account.getUserConfiguration(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * Inserts the configured default alarms and sets up corresponding triggers for the supplied event.
     *
     * @param event The event to insert the default alarms for
     */
    public void insertDefaultAlarms(Event event) throws OXException {
        insertDefaultAlarms(Collections.singletonList(event));
    }

    /**
     * Inserts the configured default alarms and sets up corresponding triggers list of events.
     *
     * @param events The events to insert the default alarms for
     */
    public void insertDefaultAlarms(final List<Event> events) throws OXException {
        final List<Alarm> defaultAlarms = getDefaultAlarms();
        if (null == defaultAlarms || 0 == defaultAlarms.size()) {
            return;
        }
        new OSGiCalendarStorageOperation<Void>(services, context.getContextId(), account.getAccountId()) {

            @Override
            protected Void call(CalendarStorage storage) throws OXException {
                insertDefaultAlarms(storage, defaultAlarms, events);
                return null;
            }
        }.executeUpdate();
    }

    public List<AlarmTrigger> getAlarmTriggers(Date until, Set<String> actions) throws OXException {
        List<AlarmTrigger> triggers = new OSGiCalendarStorageOperation<List<AlarmTrigger>>(services, context.getContextId(), account.getAccountId()) {

            @Override
            protected List<AlarmTrigger> call(CalendarStorage storage) throws OXException {
                return storage.getAlarmTriggerStorage().loadTriggers(account.getUserId(), until);
            }
        }.executeQuery();
        return AlarmUtils.filter(triggers, actions.toArray(new String[actions.size()]));
    }

    /**
     * Updates the user's alarms for a specific event.
     *
     * @param event The event to update the alarms for
     * @param updatedAlarms The updated alarms
     * @return The update result
     */
    public UpdateResult updateAlarms(final Event event, final List<Alarm> updatedAlarms) throws OXException {
        return new OSGiCalendarStorageOperation<UpdateResult>(services, context.getContextId(), account.getAccountId()) {

            @Override
            protected UpdateResult call(CalendarStorage storage) throws OXException {
                /*
                 * update alarm data in storage
                 */
                List<Alarm> originalAlarms = storage.getAlarmStorage().loadAlarms(event, account.getUserId());
                if (false == updateAlarms(storage, event, originalAlarms, updatedAlarms)) {
                    return null;
                }
                /*
                 * (re)-schedule any alarm triggers & return appropriate update result
                 */
                List<Alarm> newAlarms = storage.getAlarmStorage().loadAlarms(event, account.getUserId());
                Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId = Collections.singletonMap(event.getId(), Collections.singletonMap(I(account.getUserId()), newAlarms));
                if (null != originalAlarms && 0 < originalAlarms.size()) {
                    storage.getAlarmTriggerStorage().deleteTriggers(Collections.singletonList(event.getId()), account.getUserId());
                }
                storage.getAlarmTriggerStorage().insertTriggers(alarmsByUserByEventId, Collections.singletonList(event));
                return new UpdateResultImpl(applyAlarms(event, originalAlarms), applyAlarms(event, newAlarms));
            }
        }.executeUpdate();
    }

    /**
     * Returns the latest timestamp of alarms for this user.
     * Can be limited to an event.
     *
     * @param eventIds The optional event identifiers, can be null
     * @param userId The user identifier
     * @return The latest timestamp
     * @throws OXException
     */
    public Map<String, Long> getLatestTimestamps(final List<String> eventIds, final int userId) throws OXException {
        return new OSGiCalendarStorageOperation<Map<String, Long>>(services, context.getContextId(), account.getAccountId()) {

            @Override
            protected Map<String, Long> call(CalendarStorage storage) throws OXException {
                AlarmStorage alarmStorage = storage.getAlarmStorage();
                return eventIds == null || eventIds.isEmpty() ? Collections.emptyMap() : alarmStorage.getLatestTimestamp(eventIds, userId);
            }
        }.executeQuery();
    }

    /**
     * Returns the latest timestamp of alarms for this user.
     *
     * @param userId The user identifier
     * @return The latest timestamp
     * @throws OXException
     */
    public long getLatestTimestamp(final int userId) throws OXException {
        return new OSGiCalendarStorageOperation<Long>(services, context.getContextId(), account.getAccountId()) {

            @Override
            protected Long call(CalendarStorage storage) throws OXException {
                AlarmStorage alarmStorage = storage.getAlarmStorage();
                return alarmStorage.getLatestTimestamp(userId);
            }
        }.executeQuery();
    }

    boolean updateAlarms(CalendarStorage storage, Event event, List<Alarm> originalAlarms, List<Alarm> updatedAlarms) throws OXException {
        CollectionUpdate<Alarm, AlarmField> alarmUpdates = AlarmUtils.getAlarmUpdates(originalAlarms, updatedAlarms);
        if (alarmUpdates.isEmpty()) {
            return false;
        }
        /*
         * delete removed alarms
         */
        List<Alarm> removedItems = alarmUpdates.getRemovedItems();
        if (0 < removedItems.size()) {
            storage.getAlarmStorage().deleteAlarms(event.getId(), account.getUserId(), getAlarmIDs(removedItems));
        }
        /*
         * save updated alarms
         */
        List<? extends ItemUpdate<Alarm, AlarmField>> updatedItems = alarmUpdates.getUpdatedItems();
        if (0 < updatedItems.size()) {
            List<Alarm> alarms = new ArrayList<Alarm>(updatedItems.size());
            for (ItemUpdate<Alarm, AlarmField> itemUpdate : updatedItems) {
                Alarm alarm = AlarmMapper.getInstance().copy(itemUpdate.getOriginal(), null, (AlarmField[]) null);
                AlarmMapper.getInstance().copy(itemUpdate.getUpdate(), alarm, AlarmField.values());
                alarm.setId(itemUpdate.getOriginal().getId());
                alarm.setUid(itemUpdate.getOriginal().getUid());
                alarm.setTimestamp(System.currentTimeMillis());
                alarms.add(alarm);
                //                alarms.add(Check.alarmIsValid(alarm));//TODO
            }
            storage.getAlarmStorage().updateAlarms(event, account.getUserId(), alarms);
        }
        /*
         * insert new alarms
         */
        List<Alarm> addedItems = alarmUpdates.getAddedItems();
        if (0 < addedItems.size()) {
            List<Alarm> newAlarms = new ArrayList<Alarm>(addedItems.size());
            for (Alarm alarm : addedItems) {
                Alarm newAlarm = AlarmMapper.getInstance().copy(alarm, null, (AlarmField[]) null);
                newAlarm.setId(storage.getAlarmStorage().nextId());
                newAlarm.setTimestamp(System.currentTimeMillis());
                if (false == newAlarm.containsUid() || Strings.isEmpty(newAlarm.getUid())) {
                    newAlarm.setUid(UUID.randomUUID().toString());
                }
                newAlarms.add(newAlarm);
            }
            storage.getAlarmStorage().insertAlarms(event, account.getUserId(), newAlarms);
        }
        return true;
    }

    int insertDefaultAlarms(CalendarStorage storage, List<Alarm> defaultAlarms, List<Event> birthdaySeriesList) throws OXException {
        int count = 0;
        Map<String, Map<Integer, List<Alarm>>> alarmsByUserByEventId = new HashMap<String, Map<Integer, List<Alarm>>>(birthdaySeriesList.size());
        for (Event birthdaySeries : birthdaySeriesList) {
            List<Alarm> newAlarms = prepareNewAlarms(storage, defaultAlarms);
            alarmsByUserByEventId.put(birthdaySeries.getId(), Collections.singletonMap(I(account.getUserId()), newAlarms));
            count += newAlarms.size();
        }
        storage.getAlarmStorage().insertAlarms(alarmsByUserByEventId);
        storage.getAlarmTriggerStorage().insertTriggers(alarmsByUserByEventId, birthdaySeriesList);
        return count;
    }

    private static List<Alarm> prepareNewAlarms(CalendarStorage storage, List<Alarm> defaultAlarms) throws OXException {
        List<Alarm> newAlarms = new ArrayList<Alarm>(defaultAlarms.size());
        for (Alarm alarm : defaultAlarms) {
            Alarm newAlarm = AlarmMapper.getInstance().copy(alarm, null, (AlarmField[]) null);
            newAlarm.setId(storage.getAlarmStorage().nextId());
            newAlarm.setUid(UUID.randomUUID().toString());
            newAlarm.setTimestamp(System.currentTimeMillis());
            AlarmUtils.addExtendedProperty(newAlarm, new ExtendedProperty("X-APPLE-LOCAL-DEFAULT-ALARM", "TRUE"), true);
            newAlarms.add(newAlarm);
        }
        return newAlarms;
    }

    /**
     * Applies a specific list of alarms for an event.
     * <p/>
     * In case alarms are set, also the event's {@link EventField#TIMESTAMP} property is adjusted dynamically so that the maximum of the
     * timestamps is returned implicitly. Similarly, the event's {@link EventField#FLAGS} property is adjusted, too.
     *
     * @param event The event to apply the alarms for
     * @param alarms The alarms to apply
     * @return A delegating event with the alarms applied
     */
    static Event applyAlarms(Event event, final List<Alarm> alarms) {
        if (null == event || null == alarms && null == event.getAlarms()) {
            return event;
        }
        return new DelegatingEvent(event) {

            @Override
            public List<Alarm> getAlarms() {
                return alarms;
            }

            @Override
            public boolean containsAlarms() {
                return true;
            }

            @Override
            public long getTimestamp() {
                long timestamp = super.getTimestamp();
                if (null != alarms && 0 < alarms.size()) {
                    for (Alarm alarm : alarms) {
                        timestamp = Math.max(timestamp, alarm.getTimestamp());
                    }
                }
                return timestamp;
            }

            @Override
            public EnumSet<EventFlag> getFlags() {
                EnumSet<EventFlag> flags = super.getFlags();
                if (null != alarms && 0 < alarms.size()) {
                    flags = EnumSet.copyOf(flags);
                    flags.add(EventFlag.ALARMS);
                }
                return flags;
            }

        };
    }

}
