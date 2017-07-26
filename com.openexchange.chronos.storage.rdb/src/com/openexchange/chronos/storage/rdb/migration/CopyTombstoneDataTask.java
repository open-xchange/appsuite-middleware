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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.SortOrder.Order;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.exception.OXException;
import com.openexchange.java.util.TimeZones;
import com.openexchange.search.SearchTerm;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link CopyTombstoneDataTask}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CopyTombstoneDataTask extends MigrationTask {

    private long copiedEventTombstones;
    private long copiedAttendeeTombstones;

    /**
     * Initializes a new {@link CopyTombstoneDataTask}.
     *
     * @param progress The migration progress callback, or <code>null</code> if not used
     * @param config The migration config to use
     * @param contextId The identifier of the context being migrated
     * @param sourceStorage The source calendar storage
     * @param destinationStorage The destination calendar storage
     */
    public CopyTombstoneDataTask(MigrationProgress progress, MigrationConfig config, int contextId, CalendarStorage sourceStorage, CalendarStorage destinationStorage) {
        super(progress, config, contextId, sourceStorage, destinationStorage);
    }

    public long getCopiedEventTombstones() {
        return copiedEventTombstones;
    }

    public long getCopiedAttendeeTombstones() {
        return copiedAttendeeTombstones;
    }

    @Override
    protected void perform() throws OXException {
        /*
         * determine maximum age of copied event tombstones
         */
        Date minLastModified = getMinTombstoneLastModified();
        if (null == minLastModified) {
            LOG.info("Event tombstone migration disabled by configuration for context {}, nothing to do.", I(contextId));
            return;
        }
        /*
         * construct search term & probe total number of event tombstones to migrate
         */
        SearchTerm<?> searchTerm = getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, minLastModified);
        long tombstoneCount = sourceStorage.getEventStorage().countEventTombstones(searchTerm);
        setProgress(copiedEventTombstones, tombstoneCount);
        if (0 >= tombstoneCount) {
            LOG.info("No event tombstones with timestamp > {} found in source storage for context {}, nothing to do.", L(minLastModified.getTime()), I(contextId));
            return;
        }
        /*
         * copy tombstone data in batches & update progress
         */
        long lastTimestamp = minLastModified.getTime();
        do {
            lastTimestamp = copyTombstoneData(lastTimestamp, config.getBatchSize());
            setProgress(copiedEventTombstones, tombstoneCount);
        } while (0 < lastTimestamp);
        LOG.info("Successfully copied {} event tombstones and {} attendee tombstones in context {}.", L(copiedEventTombstones), L(copiedAttendeeTombstones), I(contextId));
    }

    private long copyTombstoneData(long lastTimestamp, int length) throws OXException {
        /*
         * read from source storage: event tombstones, corresponding attendees
         */
        LOG.trace("Loading next chunk of {} event tombstones, with timestamp > {}...", I(length), L(lastTimestamp));
        SingleSearchTerm searchTerm = getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, lastTimestamp);
        SearchOptions searchOptions = new SearchOptions().addOrder(SortOrder.getSortOrder(EventField.TIMESTAMP, Order.ASC)).setLimits(0, length);
        List<Event> events = sourceStorage.getEventStorage().searchEventTombstones(searchTerm, searchOptions, EventField.values());
        if (null == events || 0 == events.size()) {
            LOG.trace("No further event tombstones with timestamp > {} found.", L(lastTimestamp));
            return -1L;
        }
        LOG.trace("Successfully loaded {} event tombstones, searching corresponding attendee tombstones...", I(events.size()));
        Map<String, List<Attendee>> attendees = sourceStorage.getAttendeeStorage().loadAttendeeTombstones(getObjectIDs(events));
        int attendeeCount = countMultiMap(attendees);
        LOG.trace("Successfully loaded {} attendee tombstones.", I(attendeeCount));
        /*
         * write to destination storage & track result
         */
        LOG.trace("Inserting {} event tombstones and {} attendee tombstones into destination storage...", I(events.size()), I(attendeeCount));
        destinationStorage.getEventStorage().insertEventTombstones(events);
        destinationStorage.getAttendeeStorage().insertAttendeeTombstones(attendees);
        copiedEventTombstones += events.size();
        copiedAttendeeTombstones += attendeeCount;
        long nextTimestamp = events.get(events.size() - 1).getTimestamp();
        LOG.trace("Successfully copied {} event tombstones and {} attendee tombstones; next timestamp evaluated to {}.", I(events.size()), I(attendeeCount), L(nextTimestamp));
        return nextTimestamp;
    }

    private Date getMinTombstoneLastModified() {
        int months = config.getMaxTombstoneAgeInMonths();
        if (0 >= months) {
            return null;
        }
        Calendar calendar = CalendarUtils.initCalendar(TimeZones.UTC, (Date) null);
        calendar.add(Calendar.MONTH, -1 * months);
        return calendar.getTime();
    }

}
