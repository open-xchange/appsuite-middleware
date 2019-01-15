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

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.Validate;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.database.tombstone.cleanup.cleaners.AttachmentTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.CalendarTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ContactTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.FolderTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.GroupTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.InfostoreTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ObjectPermissionTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.ResourceTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.TaskTombstoneCleaner;
import com.openexchange.database.tombstone.cleanup.cleaners.TombstoneTableCleaner;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;

/**
 * {@link SchemaTombstoneCleaner}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class SchemaTombstoneCleaner {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SchemaTombstoneCleaner.class);

    private final Set<TombstoneTableCleaner> tombstoneCleaner = Stream.of(new AttachmentTombstoneCleaner(), new CalendarTombstoneCleaner(), new ContactTombstoneCleaner(), new FolderTombstoneCleaner(), new GroupTombstoneCleaner(), new InfostoreTombstoneCleaner(), new ObjectPermissionTombstoneCleaner(), new ResourceTombstoneCleaner(), new TaskTombstoneCleaner()).collect(Collectors.collectingAndThen(Collectors.toSet(), Collections::unmodifiableSet));
    private final String schemaName;
    private final Integer writePoolId;
    private final DatabaseService databaseService;

    /**
     * Constructor that should be used if you already have a connection for the target schema
     *
     * @param databaseService {@link DatabaseService} used to retrieve {@link Connection}s
     * @param schemaName {@link String} containing the name of the schema to process
     * @param writePoolId {@link Integer} containing the write pool identifier
     */
    public SchemaTombstoneCleaner(final DatabaseService databaseService, String schemaName, Integer writePoolId) {
        this.databaseService = databaseService;
        this.schemaName = schemaName;
        this.writePoolId = writePoolId;
    }

    /**
     * Default constructor that should be used if you do not yet have a {@link Connection} to the target schema
     */
    public SchemaTombstoneCleaner() {
        this(null, null, null);
    }

    /**
     * Cleans up the provided schema based on the timestamp. All entries older than the timestamp will be removed.
     *
     * @param timestamp long defining what will be removed.
     * @return {@link Map} containing the result of the cleanup in a 'table name' - 'number of removed rows' mapping
     * @see SchemaTombstoneCleaner#SchemaCleaner(DatabaseService, String, Integer)
     */
    public Map<String, Integer> cleanup(long timestamp) {
        validateParams();

        Map<String, Integer> tableCleanupResults = new HashMap<>();
        Connection writeConnection = null;
        try {
            writeConnection = databaseService.getNoTimeout(this.writePoolId.intValue(), schemaName);
            writeConnection.setAutoCommit(false);
            Map<String, Integer> cleanup = cleanup(writeConnection, timestamp);
            writeConnection.commit();
            tableCleanupResults.putAll(cleanup);
        } catch (final SQLException | OXException e) {
            Databases.rollback(writeConnection);
            LOG.error("Cannot clean up data in schema '{}': {}", schemaName, e.getMessage(), e);
        } finally {
            Databases.autocommit(writeConnection);
            databaseService.backNoTimeoout(writePoolId, writeConnection);
        }
        return tableCleanupResults;
    }

    private void validateParams() {
        Validate.notNull(this.databaseService, "DatabaseService might not be null. Use param constructor if you do not yet have a connection.");
        Validate.notNull(this.schemaName, "SchemaName might not be null. Use param constructor if you do not yet have a connection.");
        Validate.notNull(this.writePoolId, "WritePoolId might not be null. Use param constructor if you do not yet have a connection.");
    }

    /**
     * Cleans up based on the given connection and timestamp. All entries older than the timestamp will be removed.
     *
     * @return {@link Map} containing the result of the cleanup in a 'table name' - 'number of removed rows' mapping
     * @see SchemaTombstoneCleaner#SchemaCleaner()
     */
    public Map<String, Integer> cleanup(Connection writeConnection, long timestamp) throws SQLException {
        Map<String, Integer> tableCleanupResults = new HashMap<>();

        for (TombstoneTableCleaner cleaner : getTombstoneCleaner()) {
            long before = System.currentTimeMillis();
            LOG.debug("Starting TombstoneCleaner '{}'", cleaner.toString());
            Map<String, Integer> cleanedTables = cleaner.cleanup(writeConnection, timestamp);
            tableCleanupResults.putAll(cleanedTables);
            long after = System.currentTimeMillis();
            LOG.debug("Successfully finished TombstoneCleaner '{}' on schema '{}'. Processing took {}ms.", cleaner.toString(), writeConnection.getCatalog(), after - before);
        }
        return tableCleanupResults;
    }

    protected Set<TombstoneTableCleaner> getTombstoneCleaner() {
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
}
