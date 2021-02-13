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

package com.openexchange.database.tombstone.cleanup;

import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.openexchange.database.cleanup.CleanUpJob;
import com.openexchange.database.cleanup.CompositeCleanUpExecution;
import com.openexchange.database.cleanup.DefaultCleanUpJob;
import com.openexchange.database.tombstone.cleanup.cleaners.AbstractTombstoneTableCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.AttachmentTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.CalendarTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ContactTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.FolderTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.GroupTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.InfostoreTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ObjectPermissionTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ResourceTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.TaskTombstoneCleaner;
import com.openexchange.java.Strings;

/**
 * {@link SchemaTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class SchemaTombstoneCleaner {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchemaTombstoneCleaner.class);

    private final static Set<AbstractTombstoneTableCleaner> tombstoneCleaner = Stream.of(new AttachmentTombstoneCleaner(), new CalendarTombstoneCleaner(), new ContactTombstoneCleaner(), new FolderTombstoneCleaner(), new GroupTombstoneCleaner(), new InfostoreTombstoneCleaner(), new ObjectPermissionTombstoneCleaner(), new ResourceTombstoneCleaner(), new TaskTombstoneCleaner()).collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));

    public static CleanUpJob getCleanUpJob(long timespan) {
        return DefaultCleanUpJob.builder(). //@formatter:off
            withId(CompositeCleanUpExecution.class).
            withDelay(Duration.ofDays(1)).
            withInitialDelay(Duration.ofMinutes(60)).
            withRunsExclusive(true).
            withExecution(new CompositeCleanUpExecution(getTombstoneCleaner(timespan))).
            build();
        //@formatter:on
    }

    /**
     * Cleans up based on the given connection and timestamp. All entries older than the timestamp will be removed.
     *
     * @param writeConnection {@link Connection} to a schema.
     * @param timestamp long defining what will be removed.
     * @return {@link Map} containing the result of the cleanup in a 'table name' - 'number of removed rows' mapping
     * @see SchemaTombstoneCleaner#SchemaCleaner()
     */
    public Map<String, Integer> cleanup(Connection writeConnection, long timestamp) throws SQLException {
        Map<String, Integer> tableCleanupResults = new HashMap<>();

        for (AbstractTombstoneTableCleaner cleaner : getTombstoneCleaner()) {
            long before = System.currentTimeMillis();
            LOG.debug("Starting TombstoneCleaner '{}'", cleaner.toString());
            Map<String, Integer> cleanedTables = cleaner.cleanup(writeConnection, timestamp);
            tableCleanupResults.putAll(cleanedTables);
            long after = System.currentTimeMillis();
            LOG.debug("Successfully finished TombstoneCleaner '{}' on schema '{}'. Processing took {}ms.", cleaner.toString(), writeConnection.getCatalog(), L(after - before));
        }
        return tableCleanupResults;
    }

    protected Set<AbstractTombstoneTableCleaner> getTombstoneCleaner() {
        return tombstoneCleaner;
    }

    /**
     * Logs the result of the cleanup run
     *
     * @param lSchemaName The name of the schema that has been cleaned up.
     * @param cleanedTables The result of the cleanup that will be prepared to be logged.
     */
    public void logResults(String lSchemaName, Map<String, Integer> cleanedTables) {
        if (LOG.isDebugEnabled() == false) {
            return;
        }

        if (cleanedTables == null || cleanedTables.isEmpty()) {
            return;
        }
        Map<String, Integer> tablesHavingRowsRemoved = cleanedTables.entrySet().stream().filter(x -> Strings.isNotEmpty(x.getKey()) && x.getValue() != null && x.getValue().intValue() > 0).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        if (tablesHavingRowsRemoved == null || tablesHavingRowsRemoved.isEmpty()) {
            LOG.info("No rows have been identified to be removed for schema {}", lSchemaName);
            return;
        }

        final StringBuilder logBuilder = new StringBuilder(1024);
        List<Object> args = new ArrayList<>(16);
        logBuilder.append("The following clean ups have been made on schema '{}'{}");
        String separator = Strings.getLineSeparator();
        args.add(lSchemaName);
        args.add(separator);

        for (Entry<String, Integer> tableResults : tablesHavingRowsRemoved.entrySet()) {
            if (tableResults != null && tableResults.getValue() != null) {
                logBuilder.append("\tRemoved {} rows for table {}.{}");
                args.add(tableResults.getValue());
                args.add(tableResults.getKey());
                args.add(separator);
            }
        }
        LOG.debug(logBuilder.toString(), args.toArray(new Object[args.size()]));
    }

    private static Set<AbstractTombstoneTableCleaner> getTombstoneCleaner(long timespan) {
        for (AbstractTombstoneTableCleaner cleaner : tombstoneCleaner) {
            cleaner.setTimespan(timespan);
        }
        return tombstoneCleaner;
    }
}
