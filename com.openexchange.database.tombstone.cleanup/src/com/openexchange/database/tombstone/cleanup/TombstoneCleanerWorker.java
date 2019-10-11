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
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.tombstone.cleanup.osgi.Services;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.server.ServiceLookup;

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

    private final ServiceLookup serviceLookup;

    public TombstoneCleanerWorker(ServiceLookup lServiceLookup, long lTimespan) {
        this.serviceLookup = lServiceLookup;
        this.timespan = lTimespan;
        this.active = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        Thread.currentThread().setName(TombstoneCleanerWorker.class.getSimpleName());

        try {
            Thread currentThread = Thread.currentThread();
            if (currentThread.isInterrupted() || false == active.get()) {
                LOG.info("Periodic cleanup task for tombstone data interrupted or stopped.");
                return;
            }

            ContextService contextService = serviceLookup.getServiceSafe(ContextService.class);
            List<Integer> distinctContextsPerSchema = contextService.getDistinctContextsPerSchema();
            if (distinctContextsPerSchema.isEmpty() || active.get() == false || currentThread.isInterrupted()) {
                LOG.info("Schema map has been empty or thread has been interrupted. Skip cleaning up.");
                return;
            }

            DatabaseService databaseService = Services.getService(DatabaseService.class);
            // filter schemata that are not up to date
            for (Iterator<Integer> iterator = distinctContextsPerSchema.iterator(); active.get() && iterator.hasNext();) {
                Integer contextId = iterator.next();
                if (contextId == null) {
                    iterator.remove();
                    continue;
                }
                try {
                    UpdateStatus status = Updater.getInstance().getStatus(contextId.intValue());
                    if (!status.isExecutedSuccessfully(com.openexchange.database.tombstone.cleanup.update.InitialTombstoneCleanupUpdateTask.class.getName()) || status.blockingUpdatesRunning() || status.needsBlockingUpdates()) {
                        //skip update for the schema
                        iterator.remove();
                    }
                } catch (OXException e) {
                    try {
                        LOG.warn("Unable to retrieve update status for schema {}. Skip this schema for cleanup.", databaseService.getSchemaName(contextId.intValue()), e);
                    } catch (OXException e1) {
                        LOG.error("Unable to retrieve schema name for context with id {}: {}.", contextId, e1.getMessage(), e1);
                    }
                    iterator.remove();
                }
            }
            if (distinctContextsPerSchema.isEmpty()) {
                LOG.info("No schema available that already ran the update task. Skip cleaning up.");
                return;
            }
            long before = System.currentTimeMillis();
            long timestamp = before - this.timespan;

            LOG.info("Starting daily cleanup of tombstone tables. All entries before {} will be removed.", new Date(timestamp));

            for (Iterator<Integer> contextIdIter = distinctContextsPerSchema.iterator(); active.get() && currentThread.isInterrupted() == false && contextIdIter.hasNext();) {
                long beforeSchema = System.currentTimeMillis();
                Integer contextId = contextIdIter.next();
                if (contextId == null) {
                    contextIdIter.remove();
                    continue;
                }
                SchemaTombstoneCleaner schemaCleaner = new SchemaTombstoneCleaner(databaseService, contextId);
                Map<String, Integer> cleanedTables = schemaCleaner.cleanup(timestamp);
                schemaCleaner.logResults(databaseService.getSchemaName(contextId.intValue()), cleanedTables);
                long afterSchema = System.currentTimeMillis();
                try {
                    Integer purgedRecords = cleanedTables.values().stream().filter(x -> x.intValue() > 0).collect(Collectors.summingInt(x -> x.intValue()));
                    if (purgedRecords != null && purgedRecords.intValue() > 0) {
                        LOG.info("Successfully purged {} tombstone records on schema {} ({} seconds elapsed).", purgedRecords, databaseService.getSchemaName(contextId.intValue()), L(TimeUnit.MILLISECONDS.toSeconds(afterSchema - beforeSchema)));
                    }
                } catch (OXException e1) {
                    LOG.error("Unable to retrieve schema name for context with id {}: {}.", contextId, e1.getMessage(), e1);
                }
            }
            long after = System.currentTimeMillis();
            LOG.info("Finished daily cleanup of tombstone tables in all schemas that were up-to-date. Processing took {}ms.", L(after - before));
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
