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
import static com.openexchange.java.Autoboxing.L;
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
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link CopyCalendarDataTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CopyCalendarDataTask extends MigrationTask {

    private long copiedEvents;
    private long copiedAttendees;
    private long copiedAlarms;

    /**
     * Initializes a new {@link CopyCalendarDataTask}.
     *
     * @param config The migration config
     * @param contextId The identifier of the context being migrated
     * @param sourceStorage The source calendar storage
     * @param destinationStorage The destination calendar storage
     */
    public CopyCalendarDataTask(MigrationConfig config, int contextId, CalendarStorage sourceStorage, CalendarStorage destinationStorage) {
        super(config, contextId, sourceStorage, destinationStorage);
    }

    public long getCopiedEvents() {
        return copiedEvents;
    }

    public long getCopiedAttendees() {
        return copiedAttendees;
    }

    public long getCopiedAlarms() {
        return copiedAlarms;
    }

    @Override
    protected void perform() throws OXException {
        /*
         * probe total number of events to migrate
         */
        long eventCount = sourceStorage.getEventStorage().countEvents();
        setProgress(copiedEvents, eventCount);
        if (0 >= eventCount) {
            LOG.info("No events found in source storage for context {}, nothing to do.", I(contextId));
            return;
        }
        /*
         * copy calendar data in batches & update progress
         */
        int lastObjectId = 0;
        do {
            lastObjectId = copyCalendarData(lastObjectId, config.getBatchSize());
            setProgress(copiedEvents, eventCount);
        } while (0 < lastObjectId);
        LOG.info("Successfully copied {} events, {} attendees and {} alarms in context {}.", L(copiedEvents), L(copiedAttendees), L(copiedAlarms), I(contextId));
    }

    private int copyCalendarData(int lastObjectId, int length) throws OXException {
        /*
         * read from source storage: events, corresponding attendees and alarms
         */
        LOG.trace("Loading next chunk of {} events, with object id > {}...", I(length), I(lastObjectId));
        SingleSearchTerm searchTerm = getSearchTerm(EventField.ID, SingleOperation.GREATER_THAN, I(lastObjectId));
        SearchOptions searchOptions = new SearchOptions().addOrder(SortOrder.getSortOrder(EventField.ID, Order.ASC)).setLimits(0, length);
        List<Event> events = sourceStorage.getEventStorage().searchEvents(searchTerm, searchOptions, null);
        if (null == events || 0 == events.size()) {
            LOG.trace("No further events with object id > {} found.", I(lastObjectId));
            return -1;
        }
        LOG.trace("Successfully loaded {} events, searching corresponding attendees and alarms...", I(events.size()));
        Map<String, List<Attendee>> attendees = sourceStorage.getAttendeeStorage().loadAttendees(getObjectIDs(events));
        Map<String, Map<Integer, List<Alarm>>> alarms = sourceStorage.getAlarmStorage().loadAlarms(events);
        int attendeeCount = countMultiMap(attendees);
        int alarmCount = countMultiMultiMap(alarms);
        LOG.trace("Successfully loaded {} attendees and {} alarms.", I(attendeeCount), I(alarmCount));
        /*
         * write to destination storage & track result
         */
        LOG.trace("Inserting {} events, {} attendees and {} alarms into destination storage...", I(events.size()), I(attendeeCount), I(alarmCount));
        destinationStorage.getEventStorage().insertEvents(events);
        copiedEvents += events.size();
        destinationStorage.getAttendeeStorage().insertAttendees(attendees);
        copiedAttendees += attendeeCount;
        destinationStorage.getAlarmStorage().insertAlarms(prepareAlarms(destinationStorage.getAlarmStorage(), alarms));
        copiedAlarms += alarmCount;
        int nextLastObjectId = Integer.parseInt(events.get(events.size() - 1).getId());
        LOG.trace("Successfully copied {} events, {} attendees and {} alarms; next last object id evaluated to {}.",
            I(events.size()), I(attendeeCount), I(alarmCount), I(nextLastObjectId));
        return nextLastObjectId;
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

}
