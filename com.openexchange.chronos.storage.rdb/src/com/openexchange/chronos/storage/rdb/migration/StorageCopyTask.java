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

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.java.Autoboxing.I;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.SortOrder.Order;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.threadpool.AbstractTask;

/**
 * {@link StorageCopyTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class StorageCopyTask extends AbstractTask<Void> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StorageCopyTask.class);

    private final CalendarStorage sourceStorage;
    private final CalendarStorage destinationStorage;
    private final int batchSize;

    private int copiedEventTombstones;
    private int copiedEvents;
    private int copiedAttendees;
    private int copiedAttendeeTombstones;
    private int copiedAlarms;

    /**
     * Initializes a new {@link StorageCopyTask}.
     *
     * @param sourceStorage The source calendar storage
     * @param destinationStorage The destination calendar storage
     * @param batchSize The batch size to use
     */
    public StorageCopyTask(CalendarStorage sourceStorage, CalendarStorage destinationStorage, int batchSize) {
        super();
        this.sourceStorage = sourceStorage;
        this.destinationStorage = destinationStorage;
        this.batchSize = batchSize;
    }

    @Override
    public Void call() throws Exception {
        /*
         * copy calendar data
         */
        copyCalendarData(batchSize);
        /*
         * copy tombstone data
         */
        copyTombstoneData(batchSize);
        return null;
    }

    public int getCopiedEventTombstones() {
        return copiedEventTombstones;
    }

    public int getCopiedEvents() {
        return copiedEvents;
    }

    public int getCopiedAttendees() {
        return copiedAttendees;
    }

    public int getCopiedAttendeeTombstones() {
        return copiedAttendeeTombstones;
    }

    public int getCopiedAlarms() {
        return copiedAlarms;
    }

    private void copyTombstoneData(int batchSize) throws OXException {
        int lastObjectId = 0;
        do {
            lastObjectId = copyTombstoneData(lastObjectId, batchSize);
        } while (0 < lastObjectId);
    }

    private void copyCalendarData(int batchSize) throws OXException {
        int lastObjectId = 0;
        do {
            lastObjectId = copyCalendarData(lastObjectId, batchSize);
        } while (0 < lastObjectId);
    }

    private int copyCalendarData(int lastObjectId, int length) throws OXException {
        LOG.info("copyCalendarData@" + lastObjectId);
        /*
         * read from source storage: events, corresponding attendees, corresponding alarms
         */
        List<Event> events = sourceStorage.getEventStorage().searchEvents(
            getSearchTerm(EventField.ID, SingleOperation.GREATER_THAN, I(lastObjectId)), getSearchOptions(0, length), EventField.values());
        if (null == events || 0 == events.size()) {
            return -1;
        }
        Map<String, List<Attendee>> attendees = sourceStorage.getAttendeeStorage().loadAttendees(getObjectIDs(events));
        Map<String, Map<Integer, List<Alarm>>> alarms = sourceStorage.getAlarmStorage().loadAlarms(events);
        /*
         * prepare data for destination storage
         */
        alarms = prepareAlarms(destinationStorage.getAlarmStorage(), alarms);
        /*
         * write to destination storage & track result
         */
        destinationStorage.getEventStorage().insertEvents(events);
        copiedEvents += events.size();
        destinationStorage.getAttendeeStorage().insertAttendees(attendees);
        copiedAttendees += countMultiMap(attendees);
        destinationStorage.getAlarmStorage().insertAlarms(alarms);
        copiedAlarms += countMultiMultiMap(alarms);
        return Integer.parseInt(events.get(events.size() - 1).getId());
    }

    private boolean copyCalendarData1(int offset, int length) throws OXException {
        LOG.info("copyCalendarData@" + offset);
        /*
         * read from source storage: events, corresponding attendees, corresponding alarms
         */
        List<Event> events = sourceStorage.getEventStorage().searchEvents(null, getSearchOptions(offset, length), EventField.values());
        if (null == events || 0 == events.size()) {
            return false;
        }
        Map<String, List<Attendee>> attendees = sourceStorage.getAttendeeStorage().loadAttendees(getObjectIDs(events));
        Map<String, Map<Integer, List<Alarm>>> alarms = sourceStorage.getAlarmStorage().loadAlarms(events);
        /*
         * prepare data for destination storage
         */
        alarms = prepareAlarms(destinationStorage.getAlarmStorage(), alarms);
        /*
         * write to destination storage & track result
         */
        destinationStorage.getEventStorage().insertEvents(events);
        copiedEvents += events.size();
        destinationStorage.getAttendeeStorage().insertAttendees(attendees);
        copiedAttendees += countMultiMap(attendees);
        destinationStorage.getAlarmStorage().insertAlarms(alarms);
        copiedAlarms += countMultiMultiMap(alarms);
        return true;
    }

    private int copyTombstoneData(int lastObjectId, int length) throws OXException {
        LOG.info("copyTombstoneData@" + lastObjectId);
        /*
         * read from source storage: event tombstones, corresponding attendees
         */
        List<Event> events = sourceStorage.getEventStorage().searchEventTombstones(
            getSearchTerm(EventField.ID, SingleOperation.GREATER_THAN, I(lastObjectId)), getSearchOptions(0, length), EventField.values());
        if (null == events || 0 == events.size()) {
            return -1;
        }
        Map<String, List<Attendee>> attendees = sourceStorage.getAttendeeStorage().loadAttendeeTombstones(getObjectIDs(events));
        /*
         * write to destination storage & track result
         */
        destinationStorage.getEventStorage().insertEventTombstones(events);
        copiedEventTombstones += events.size();
        destinationStorage.getAttendeeStorage().insertAttendeeTombstones(attendees);
        copiedAttendeeTombstones += countMultiMap(attendees);
        return Integer.parseInt(events.get(events.size() - 1).getId());
    }

    private boolean copyTombstoneData1(int offset, int length) throws OXException {
        LOG.info("copyTombstoneData@" + offset);
        /*
         * read from source storage: event tombstones, corresponding attendees
         */
        List<Event> events = sourceStorage.getEventStorage().searchEventTombstones(null, getSearchOptions(offset, length), EventField.values());
        if (null == events || 0 == events.size()) {
            return false;
        }
        Map<String, List<Attendee>> attendees = sourceStorage.getAttendeeStorage().loadAttendeeTombstones(getObjectIDs(events));
        /*
         * write to destination storage & track result
         */
        destinationStorage.getEventStorage().insertEventTombstones(events);
        copiedEventTombstones += events.size();
        destinationStorage.getAttendeeStorage().insertAttendeeTombstones(attendees);
        copiedAttendeeTombstones += countMultiMap(attendees);
        return true;
    }

    private static SearchOptions getSearchOptions(int offset, int length) {
        return new SearchOptions().addOrder(SortOrder.getSortOrder(EventField.ID, Order.ASC)).setLimits(offset, offset + length);
    }

    private static Map<String, Map<Integer, List<Alarm>>> prepareAlarms(AlarmStorage destinationStorage, Map<String, Map<Integer, List<Alarm>>> alarms) throws OXException {
        for (Entry<String, Map<Integer, List<Alarm>>> entry : alarms.entrySet()) {
            for (Entry<Integer, List<Alarm>> alarmsPerUser : entry.getValue().entrySet()) {
                for (Alarm alarm : alarmsPerUser.getValue()) {
                    if (false == alarm.containsId() || 0 == alarm.getId()) {
                        alarm.setId(destinationStorage.nextId());
                    }
                    if (false == alarm.containsUid() || null == alarm.getUid()) {
                        alarm.setUid(UUID.randomUUID().toString());
                    }
                }
            }
        }
        return alarms;
    }

    private static <K, V> int countMultiMap(Map<K, List<V>> multiMap) {
        int count = 0;
        if (null != multiMap) {
            for (Entry<K, List<V>> entry : multiMap.entrySet()) {
                if (null != entry.getValue()) {
                    count += entry.getValue().size();
                }
            }
        }
        return count;
    }

    private static <K1, K2, V> int countMultiMultiMap(Map<K1, Map<K2, List<V>>> multiMultiMap) {
        int count = 0;
        if (null != multiMultiMap) {
            for (Entry<K1, Map<K2, List<V>>> entry : multiMultiMap.entrySet()) {
                count += countMultiMap(entry.getValue());
            }
        }
        return count;
    }

}

