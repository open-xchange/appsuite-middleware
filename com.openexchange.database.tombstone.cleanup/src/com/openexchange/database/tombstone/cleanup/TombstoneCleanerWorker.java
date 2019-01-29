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
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.tombstone.cleanup.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.java.Strings;

/**
 * {@link TombstoneCleanerWorker}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since v7.10.2
 */
public class TombstoneCleanerWorker implements Runnable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(TombstoneCleanerWorker.class);

    private final long timespan;
    private final AtomicBoolean active;

    public TombstoneCleanerWorker(long timespan) {
        this.timespan = timespan;
        this.active = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        try {
            DatabaseService databaseService = Services.getService(DatabaseService.class);

            Thread currentThread = Thread.currentThread();
            if (currentThread.isInterrupted() || false == active.get()) {
                LOG.info("Periodic cleanup task for tombstone data interrupted or stopped.");
                return;
            }

            Connection connection = null;
            Map<String, Integer> map = new HashMap<String, Integer>();
            try {
                connection = databaseService.getReadOnly();
                map.putAll(databaseService.getAllSchemata(connection));
            } catch (OXException e) {
                LOG.error("Unexpected error while retrieving schema information.", e);
            } finally {
                databaseService.backReadOnly(connection);
            }
            Map<String, Integer> schemata = map;
            if (schemata.isEmpty() || active.get() == false || currentThread.isInterrupted()) {
                LOG.info("Schema map has been empty or thread has been interrupted. Skip cleaning up.");
                return;
            }

            // filter schemata that are not up to date
            for (Iterator<Map.Entry<String, Integer>> iterator = schemata.entrySet().iterator(); active.get() && iterator.hasNext();) {
                Entry<String, Integer> schema = iterator.next();
                Integer writePoolId = schema.getValue();
                String schemaName = schema.getKey();
                try {
                    UpdateStatus status = Updater.getInstance().getStatus(schemaName, writePoolId);
                    if (!status.isExecutedSuccessfully(com.openexchange.database.tombstone.cleanup.update.InitialTombstoneCleanupUpdateTask.class.getName()) || status.blockingUpdatesRunning() || status.needsBlockingUpdates()) {
                        //skip update for the schema
                        iterator.remove();
                    }
                } catch (OXException e) {
                    LOG.warn("Unable to retrieve update status for schema {}. Skip this schema for cleanup.", schemaName, e);
                }
            }
            if (schemata.isEmpty()) {
                LOG.info("No schema available that already ran the update task. Skip cleaning up.");
                return;
            }
            long before = System.currentTimeMillis();
            long timestamp = before - this.timespan;

            LOG.info("Starting daily cleanup of tombstone tables. All entries before {} will be removed.", new Date(timestamp));

            for (Iterator<Entry<String, Integer>> schemaEntryIter = schemata.entrySet().iterator(); active.get() && currentThread.isInterrupted() == false && schemaEntryIter.hasNext();) {
                long beforeSchema = System.currentTimeMillis();
                Entry<String, Integer> schema = schemaEntryIter.next();
                SchemaTombstoneCleaner schemaCleaner = new SchemaTombstoneCleaner(databaseService, schema.getKey(), schema.getValue());
                Map<String, Integer> cleanedTables = schemaCleaner.cleanup(timestamp);
                schemaCleaner.logResults(schema.getKey(), cleanedTables);
                long afterSchema = System.currentTimeMillis();
                LOG.info("Successfully purged {} tombstone records on schema {} ({} seconds elapsed).", cleanedTables.values().stream().filter(x -> x.intValue() > 0).collect(Collectors.summingInt(x -> x)), schema.getKey(), TimeUnit.MILLISECONDS.toSeconds(afterSchema - beforeSchema));
            }
            long after = System.currentTimeMillis();
            LOG.info("Finished daily cleanup of tombstone tables in schemas {}. Processing took {}ms.", Strings.concat(",", schemata.keySet()), after - before);
        } catch (Exception e) {
            LOG.error("Error during periodic tombstone cleanup task: {}", e.getMessage(), e);
        }
    }

    /**
     * Stops all background processing by signaling termination flag.
     */
    public void stop() {
        active.set(false);
    }
}
