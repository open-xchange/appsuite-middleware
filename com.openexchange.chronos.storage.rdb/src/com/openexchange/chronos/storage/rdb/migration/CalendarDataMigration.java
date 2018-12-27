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

package com.openexchange.chronos.storage.rdb.migration;

import static com.openexchange.chronos.common.CalendarUtils.getObjectIDs;
import static com.openexchange.chronos.common.CalendarUtils.getSearchTerm;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import static com.openexchange.osgi.Tools.requireService;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import com.openexchange.chronos.Alarm;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.Event;
import com.openexchange.chronos.EventField;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.service.EntityResolver;
import com.openexchange.chronos.service.SearchOptions;
import com.openexchange.chronos.service.SortOrder;
import com.openexchange.chronos.service.SortOrder.Order;
import com.openexchange.chronos.storage.AlarmStorage;
import com.openexchange.chronos.storage.CalendarStorage;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.database.provider.DBTransactionPolicy;
import com.openexchange.database.provider.SimpleDBProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.util.TimeZones;
import com.openexchange.search.SingleSearchTerm;
import com.openexchange.search.SingleSearchTerm.SingleOperation;

/**
 * {@link CalendarDataMigration}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class CalendarDataMigration {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(CalendarDataMigration.class);

    private final MigrationConfig config;
    private final Context context;
    private final int contextId;
    private final MigrationProgress progress;
    private final DBProvider dbProvider;
    private final DBTransactionPolicy txPolicy;
    private final EntityResolver entityResolver;
    private final SortedMap<String, List<OXException>> warnings;

    private long totalEventsToCopy;
    private long lastLogTime;
    private long copiedEvents;
    private long copiedAttendees;
    private long copiedAlarms;
    private long copiedEventTombstones;
    private long copiedAttendeeTombstones;

    /**
     * Initializes a new {@link CalendarDataMigration}.
     *
     * @param progress The migration progress callback, or <code>null</code> if not used
     * @param config The migration config to use
     * @param context The context being migrated
     * @param connection The database connection
     * @throws OXException
     */
    public CalendarDataMigration(MigrationProgress progress, MigrationConfig config, Context context, Connection connection) throws OXException {
        super();
        this.progress = progress;
        this.config = config;
        this.contextId = context.getContextId();
        this.context = context;
        this.entityResolver = config.optEntityResolver(contextId);
        if (config.isIntermediateCommits()) {
            this.dbProvider = new UpdateTaskDBProvider(requireService(DatabaseService.class, config.getServiceLookup()));
            this.txPolicy = DBTransactionPolicy.NORMAL_TRANSACTIONS;
        } else {
            this.dbProvider = new SimpleDBProvider(connection, connection);
            this.txPolicy = DBTransactionPolicy.NO_TRANSACTIONS;
        }
        this.warnings = new TreeMap<String, List<OXException>>(CalendarUtils.ID_COMPARATOR);
    }

    /**
     * Performs the calendar data migration.
     */
    public void perform() throws OXException {
        long startTime = System.currentTimeMillis();
        LOG.info("Starting calendar migration task in context {}.", I(contextId));
        try {
            /*
             * empty destination storage as preparation
             */
            LOG.trace("Emptying destination storage...");
            emptyDestinationStorage();
            LOG.info("Destination storage emptied successfully.");
            /*
             * probe total number of events to migrate
             */
            long eventCount = countEvents();
            /*
             * determine maximum age of copied event tombstones & probe total number of event tombstones to migrate
             */
            Date minTombstoneLastModified = getMinTombstoneLastModified();
            long tombstoneCount = countEventTombstones(minTombstoneLastModified);
            /*
             * init progress
             */
            totalEventsToCopy = eventCount + tombstoneCount;
            if (0 < totalEventsToCopy) {
                LOG.info("Found a total of {} events and {} event tombstones to copy.", L(eventCount), L(tombstoneCount));
            }
            /*
             * copy calendar data in batches & update progress
             */
            if (0 < eventCount) {
                int lastObjectId = 0;
                do {
                    updateProgress();
                    lastObjectId = copyCalendarData(lastObjectId, config.getBatchSize());
                } while (0 < lastObjectId);
                LOG.info("Successfully copied {} events, {} attendees and {} alarms in context {}.", L(copiedEvents), L(copiedAttendees), L(copiedAlarms), I(contextId));
            } else {
                LOG.info("No events found in source storage for context {}, nothing to do.", I(contextId));
            }
            /*
             * copy tombstone data in batches & update progress
             */
            if (null == minTombstoneLastModified) {
                LOG.info("Event tombstone migration disabled by configuration for context {}, nothing to do.", I(contextId));
            } else if (0 < tombstoneCount) {
                long lastTimestamp = minTombstoneLastModified.getTime();
                do {
                    updateProgress();
                    lastTimestamp = copyTombstoneData(lastTimestamp, config.getBatchSize());
                } while (0 < lastTimestamp);
                LOG.info("Successfully copied {} event tombstones and {} attendee tombstones in context {}.", L(copiedEventTombstones), L(copiedAttendeeTombstones), I(contextId));
            } else {
                LOG.info("No event tombstones with timestamp > {} found in source storage for context {}, nothing to do.", L(minTombstoneLastModified.getTime()), I(contextId));
            }
            /*
             * copy current event sequence for account 0
             */
            int sequence = copyCalendarSequence();
            LOG.info("Successfully initialized event identifier sequence with {} in context {}.", I(sequence), I(contextId));
        } catch (SQLException e) {
            LOG.error("Error running calendar migration task in context {}: {}", I(contextId), e.getMessage(), e);
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            LOG.error("Error running calendar migration task in context {}: {}", I(contextId), e.getMessage(), e);
            throw e;
        } finally {
            long millis = System.currentTimeMillis() - startTime;
            float eventsPerSecond = 0 < millis ? totalEventsToCopy / (millis / 1000f) : 0;
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);
            if (0 < seconds) {
                millis -= TimeUnit.MILLISECONDS.convert(seconds, TimeUnit.SECONDS);
            }
            if (0 < totalEventsToCopy) {
                LOG.info("Finished calendar migration task in context {}, {}.{} seconds elapsed for a total of {} events, at a rate of {} events per second.",
                    I(contextId), L(seconds), L(millis), L(totalEventsToCopy), I((int) eventsPerSecond));
            } else {
                LOG.info("Finished calendar migration task in context {}, {}.{} seconds elapsed.", I(contextId), L(seconds), L(millis));
            }
            if (null == warnings || 0 == warnings.size()) {
                LOG.info("No warnings occurred during execution of calendar migration task in context {}.", I(contextId));
            } else {
                LOG.info("Encountered the following warnings during execution of calendar migration task in context {}:{}{}",
                    I(contextId), System.lineSeparator(), dumpWarnings(warnings));
            }
        }
    }

    private CalendarStorage initDestinationStorage(Connection connection) throws OXException {
        DBProvider dbProvider = new SimpleDBProvider(connection, connection);
        return new com.openexchange.chronos.storage.rdb.RdbCalendarStorage(context, 0, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }

    private CalendarStorage initSourceStorage(Connection connection) throws OXException {
        DBProvider dbProvider = new SimpleDBProvider(connection, connection);
        return new com.openexchange.chronos.storage.rdb.legacy.RdbCalendarStorage(context, entityResolver, dbProvider, DBTransactionPolicy.NO_TRANSACTIONS);
    }

    private void emptyDestinationStorage() throws OXException, SQLException {
        boolean updated = false;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            CalendarStorage destinationStorage = initDestinationStorage(connection);
            updated |= destinationStorage.getEventStorage().deleteAllEvents();
            updated |= destinationStorage.getAttendeeStorage().deleteAllAttendees();
            updated |= destinationStorage.getAlarmStorage().deleteAllAlarms();
            updated |= destinationStorage.getAlarmTriggerStorage().deleteAllTriggers();
            txPolicy.commit(connection);
            warnings.putAll(destinationStorage.getAndFlushWarnings());
        } finally {
            release(connection, updated);
        }
    }

    private long countEvents() throws OXException {
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return initSourceStorage(connection).getEventStorage().countEvents();
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }

    private long countEventTombstones(Date minTombstoneLastModified) throws OXException {
        if (null == minTombstoneLastModified) {
            return 0L;
        }
        Connection connection = null;
        try {
            connection = dbProvider.getReadConnection(context);
            return initSourceStorage(connection).getEventStorage().countEventTombstones(
                getSearchTerm(EventField.TIMESTAMP, SingleOperation.GREATER_THAN, minTombstoneLastModified));
        } finally {
            dbProvider.releaseReadConnection(context, connection);
        }
    }


    private int copyCalendarData(int lastObjectId, int length) throws OXException, SQLException {
        int nextLastObjectId = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            CalendarStorage sourceStorage = initSourceStorage(connection);
            CalendarStorage destinationStorage = initDestinationStorage(connection);
            nextLastObjectId = copyCalendarData(sourceStorage, destinationStorage, lastObjectId, length);
            txPolicy.commit(connection);
            warnings.putAll(sourceStorage.getAndFlushWarnings());
            warnings.putAll(destinationStorage.getAndFlushWarnings());
        } finally {
            release(connection, 0 < nextLastObjectId);
        }
        return nextLastObjectId;
    }

    private long copyTombstoneData(long lastTimestamp, int length) throws OXException, SQLException {
        long nextTimestamp = 0;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            CalendarStorage sourceStorage = initSourceStorage(connection);
            CalendarStorage destinationStorage = initDestinationStorage(connection);
            nextTimestamp = copyTombstoneData(sourceStorage, destinationStorage, lastTimestamp, length);
            txPolicy.commit(connection);
            warnings.putAll(sourceStorage.getAndFlushWarnings());
            warnings.putAll(destinationStorage.getAndFlushWarnings());
        } finally {
            release(connection, 0L < nextTimestamp);
        }
        return nextTimestamp;
    }

    private int copyCalendarSequence() throws OXException, SQLException {
        int sequence = -1;
        Connection connection = null;
        try {
            connection = dbProvider.getWriteConnection(context);
            txPolicy.setAutoCommit(connection, false);
            sequence = copyCalendarSequence(connection);
            txPolicy.commit(connection);
        } finally {
            release(connection, -1 != sequence);
        }
        return sequence;
    }

    /**
     * Safely releases a write connection obeying the configured transaction policy, rolling back automatically if not committed before.
     *
     * @param connection The write connection to release
     * @param updated <code>true</code> if there were changes, <code>false</code>, otherwise
     */
    private void release(Connection connection, boolean updated) throws OXException, SQLException {
        if (null != connection) {
            try {
                if (false == connection.getAutoCommit()) {
                    txPolicy.rollback(connection);
                }
                txPolicy.setAutoCommit(connection, true);
            } finally {
                if (updated) {
                    dbProvider.releaseWriteConnection(context, connection);
                } else {
                    dbProvider.releaseWriteConnectionAfterReading(context, connection);
                }
            }
        }
    }

    private int copyCalendarSequence(Connection connection) throws OXException {
        int sequence;
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM sequence_calendar WHERE cid=?;")) {
            stmt.setInt(1, contextId);
            try (ResultSet resultSet = stmt.executeQuery()) {
                sequence = resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
        try (PreparedStatement stmt = connection.prepareStatement("REPLACE INTO calendar_event_sequence (cid,account,id) VALUES(?,?,?);")) {
            stmt.setInt(1, contextId);
            stmt.setInt(2, 0);
            stmt.setInt(3, sequence);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw CalendarExceptionCodes.DB_ERROR.create(e, e.getMessage());
        }
        return sequence;
    }

    private int copyCalendarData(CalendarStorage sourceStorage, CalendarStorage destinationStorage, int lastObjectId, int length) throws OXException {
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
        destinationStorage.getAlarmTriggerStorage().insertTriggers(alarms, events);
        /*
         * return next object id as offset for next iteration
         */
        int nextLastObjectId = Integer.parseInt(events.get(events.size() - 1).getId());
        LOG.trace("Successfully copied {} events, {} attendees and {} alarms; next last object id evaluated to {}.",
            I(events.size()), I(attendeeCount), I(alarmCount), I(nextLastObjectId));
        return nextLastObjectId;
    }

    private long copyTombstoneData(CalendarStorage sourceStorage, CalendarStorage destinationStorage, long lastTimestamp, int length) throws OXException {
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

    /**
     * Updates the migration progress with the current number of copied events and event tombstones and generates a log message if
     * applicable.
     */
    private void updateProgress() {
        long current = copiedEvents + copiedEventTombstones;
        if (null != progress) {
            progress.setContextProgress(current, totalEventsToCopy);
        }
        long currentTimeMillis = System.currentTimeMillis();
        if (0 < lastLogTime && currentTimeMillis - lastLogTime > TimeUnit.SECONDS.toMillis(10L)) {
            LOG.info("Calendar migration task finished {}% for context {}.", I((int) (current * 100 / totalEventsToCopy)), I(contextId));
        }
        lastLogTime = currentTimeMillis;
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

    private static Map<String, Map<Integer, List<Alarm>>> prepareAlarms(AlarmStorage destinationStorage, Map<String, Map<Integer, List<Alarm>>> alarms) throws OXException {
        for (Entry<String, Map<Integer, List<Alarm>>> entry : alarms.entrySet()) {
            for (Entry<Integer, List<Alarm>> alarmsPerUser : entry.getValue().entrySet()) {
                for (Alarm alarm : alarmsPerUser.getValue()) {
                    alarm.setId(destinationStorage.nextId());
                    alarm.setLastModified(System.currentTimeMillis());
                    if (false == alarm.containsUid() || null == alarm.getUid()) {
                        alarm.setUid(UUID.randomUUID().toString());
                    }
                }
            }
        }
        return alarms;
    }

    private String dumpWarnings(Map<String, List<OXException>> warnings) {
        StringBuilder stringBuilder = new StringBuilder();
        if (null != warnings && 0 < warnings.size()) {
            for (Entry<String, List<OXException>> warningsPerEvent : warnings.entrySet()) {
                for (OXException warning : warningsPerEvent.getValue()) {
                    if (config.isSevere(warning)) {
                        try (StringWriter stringWriter = new StringWriter(); PrintWriter printWriter = new PrintWriter(stringWriter)) {
                            warning.printStackTrace(printWriter);
                            stringBuilder.append("* ").append(stringWriter.toString()).append(System.lineSeparator());
                        } catch (IOException e) {
                            // ignore
                        }
                    } else {
                        stringBuilder.append("* ").append(warning.getMessage()).append(System.lineSeparator());
                    }
                }
            }
        }
        return stringBuilder.toString().trim();
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
