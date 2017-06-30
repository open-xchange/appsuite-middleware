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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.SortOrder.Order;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;
import com.openexchange.threadpool.AbstractTask;

/**
 * {@link StorageReadTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class StorageReadTask extends AbstractTask<Void> {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StorageReadTask.class);

    private final CalendarStorage sourceStorage;
    private final int batchSize;
    private final Date minTombstoneLastModified;

    private int readEventTombstones;
    private int readEvents;
    private int readAttendees;
    private int readAttendeeTombstones;
    private int readAlarms;

    /**
     * Initializes a new {@link StorageReadTask}.
     *
     * @param sourceStorage The source calendar storage
     * @param batchSize The batch size to use
     * @param minTombstoneLastModified
     */
    public StorageReadTask(CalendarStorage sourceStorage, int batchSize, Date minTombstoneLastModified) {
        super();
        this.sourceStorage = sourceStorage;
        this.batchSize = batchSize;
        this.minTombstoneLastModified = minTombstoneLastModified;
    }

    @Override
    public Void call() throws Exception {
        /*
         * copy calendar data
         */
        readCalendarData(batchSize);
        /*
         * read tombstone data
         */
        copyTombstoneData(batchSize, minTombstoneLastModified);
        return null;
    }

    public int getReadEventTombstones() {
        return readEventTombstones;
    }

    public int getReadEvents() {
        return readEvents;
    }

    public int getReadAttendees() {
        return readAttendees;
    }

    public int getReadAttendeeTombstones() {
        return readAttendeeTombstones;
    }

    public int getReadAlarms() {
        return readAlarms;
    }

    private void copyTombstoneData(int batchSize, Date minLastModified) throws OXException {
        Date lastLastModified = minLastModified;
        do {
            lastLastModified = readTombstoneData(lastLastModified, batchSize);
        } while (null != lastLastModified);
    }

    private void readCalendarData(int batchSize) throws OXException {
        int lastObjectId = 0;
        do {
            lastObjectId = readCalendarData(lastObjectId, batchSize);
        } while (0 < lastObjectId);
    }

    private int readCalendarData(int lastObjectId, int length) throws OXException {
        LOG.info("readCalendarData@" + lastObjectId);
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
         * track result
         */
        readEvents += events.size();
        readAttendees += countMultiMap(attendees);
        readAlarms += countMultiMultiMap(alarms);
        return Integer.parseInt(events.get(events.size() - 1).getId());
    }

    private Date readTombstoneData(Date lastLastModified, int length) throws OXException {
        LOG.info("readTombstoneData@" + lastLastModified.getTime());
        /*
         * read from source storage: event tombstones, corresponding attendees
         */
        SingleSearchTerm searchTerm = getSearchTerm(EventField.LAST_MODIFIED, SingleOperation.GREATER_THAN, lastLastModified);
        SearchOptions searchOptions = new SearchOptions().addOrder(SortOrder.getSortOrder(EventField.LAST_MODIFIED, Order.ASC)).setLimits(0, length);
        List<Event> events = sourceStorage.getEventStorage().searchEventTombstones(searchTerm, searchOptions, EventField.values());
        if (null == events || 0 == events.size()) {
            return null;
        }
        Map<String, List<Attendee>> attendees = sourceStorage.getAttendeeStorage().loadAttendeeTombstones(getObjectIDs(events));
        /*
         * track result
         */
        readEventTombstones += events.size();
        readAttendeeTombstones += countMultiMap(attendees);
        return events.get(events.size() - 1).getLastModified();
    }

    private static SearchOptions getSearchOptions(int offset, int length) {
        return new SearchOptions().addOrder(SortOrder.getSortOrder(EventField.ID, Order.ASC)).setLimits(offset, offset + length);
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

