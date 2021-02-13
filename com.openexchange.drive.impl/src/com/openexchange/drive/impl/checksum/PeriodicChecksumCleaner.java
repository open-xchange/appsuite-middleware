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
 *    trademarks of the OX Software GmbH. group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH.
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

package com.openexchange.drive.impl.checksum;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.java.Autoboxing.L;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.context.PoolAndSchema;
import com.openexchange.database.Databases;
import com.openexchange.database.cleanup.CleanUpExecution;
import com.openexchange.database.cleanup.CleanUpExecutionConnectionProvider;
import com.openexchange.database.cleanup.CleanUpJob;
import com.openexchange.database.cleanup.DefaultCleanUpJob;
import com.openexchange.drive.checksum.rdb.RdbChecksumStore;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;

/**
 * {@link PeriodicChecksumCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class PeriodicChecksumCleaner implements CleanUpExecution {

    private static final Logger LOG = LoggerFactory.getLogger(PeriodicChecksumCleaner.class);

    private final long checksumExpiry;
    private final Duration delay;
    private final Duration initialDelay;

    /**
     * Initializes a new {@link PeriodicChecksumCleaner}.
     *
     * @param checksumExpiry The time span (in milliseconds) after which an unused directory checksum may be deleted permanently
     * @param delay The delay between subsequent executions of the cleaner
     * @param initialDelay The initial delay when to start the cleaner
     */
    public PeriodicChecksumCleaner(long checksumExpiry, Duration delay, Duration initialDelay) {
        super();
        this.checksumExpiry = checksumExpiry;
        this.delay = delay;
        this.initialDelay = initialDelay;
    }

    @Override
    public boolean isApplicableFor(String schema, int representativeContextId, int databasePoolId, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        try {
            return Databases.tableExists(connectionProvider.getConnection(), "directorychecksums") && Databases.tableExists(connectionProvider.getConnection(), "filechecksums");
        } catch (SQLException e) {
            LOG.warn("Unable to look-up \"directorychecksums\" or \"filechecksums\" table", e);
        }
        return false;
    }

    @Override
    public void executeFor(String schema, int representativeContextId, int databasePoolId, CleanUpExecutionConnectionProvider connectionProvider) throws OXException {
        long start = System.currentTimeMillis();
        LOG.info("Periodic checksum cleanup task starting, going to check all contexts...");
        EligibleContextsResult eligibleContextsResult = getEligibleContextIDs(schema, databasePoolId);
        if (eligibleContextsResult.numOfUnUpdatedContexts > 0) {
            LOG.info("Skipping {} contexts due to not up-to-date database schemas.", I(eligibleContextsResult.numOfUnUpdatedContexts));
        }
        List<Integer> contextIDs = eligibleContextsResult.upToDateContextIDs;
        for (Integer ctxID : contextIDs) {
            int contextID = ctxID.intValue();
            cleanupContext(contextID, System.currentTimeMillis() - checksumExpiry, connectionProvider.getConnection());
            break;
        }
        LOG.info("Periodic checksum cleanup task finished after {}ms.", L(System.currentTimeMillis() - start));
    }

    public CleanUpJob getCleanUpJob() {
        return DefaultCleanUpJob.builder(). //@formatter:off
            withId(PeriodicChecksumCleaner.class).
            withDelay(delay).
            withInitialDelay(initialDelay).
            withRunsExclusive(true).
            withExecution(this).
            build();
        //@formatter:on
    }

    /**
     * Synchronously cleans obsolete checksums for a context.
     *
     * @param contextID The context ID
     * @param unusedSince The maximum "used" timestamp of a checksum to be considered as "unused"
     * @param connection The database connection
     */
    private void cleanupContext(int contextID, long unusedSince, Connection connection) throws OXException {
        List<DirectoryChecksum> unusedChecksums = RdbChecksumStore.getUnusedDirectoryChecksums(connection, contextID, unusedSince);
        if (0 == unusedChecksums.size()) {
            LOG.debug("No unused directory checksums detected in context {}.", I(contextID));
            return;
        }
        /*
         * collect affected folder identifiers
         */
        Set<FolderID> folderIDs = new HashSet<>();
        for (DirectoryChecksum unusedChecksum : unusedChecksums) {
            folderIDs.add(unusedChecksum.getFolderID());
        }
        /*
         * remove checksums
         */
        int removed = RdbChecksumStore.removeDirectoryChecksums(connection, contextID, unusedChecksums);
        LOG.debug("Removed {} unused directory checksums in context {}.", I(removed), I(contextID));
        /*
         * determine folder ids no longer referenced at all by checking against the still used directory checksums
         */
        Set<FolderID> obsoleteFolderIDs = new HashSet<FolderID>(folderIDs);
        List<DirectoryChecksum> usedChecksums = RdbChecksumStore.getDirectoryChecksums(connection, contextID, new ArrayList<FolderID>(folderIDs));
        for (DirectoryChecksum usedChecksum : usedChecksums) {
            obsoleteFolderIDs.remove(usedChecksum.getFolderID());
        }
        /*
         * remove file checksums for obsolete folder ids, too
         */
        if (0 < obsoleteFolderIDs.size()) {
            removed = RdbChecksumStore.removeFileChecksumsInFolders(connection, contextID, new ArrayList<FolderID>(obsoleteFolderIDs));
            LOG.debug("Removed {} file checksums for {} obsolete directories in context {}.", I(removed), I(obsoleteFolderIDs.size()), I(contextID));
        }
    }

    /**
     * Gets the identifiers of all contexts eligible for a checksum cleaner run, i.e. contexts from non-up-to-date schemas are filtered
     * out beforehand.
     *
     * @param schema The schema name
     * @param poolId The database pool id
     *
     * @return The context identifiers
     */
    private static EligibleContextsResult getEligibleContextIDs(String schema, int poolId) throws OXException {
        Set<Integer> upToDateContextIDs = new HashSet<>();
        int numOfUnUpdatedContexts = 0;

        Map<PoolAndSchema, List<Integer>> schemaAssociations = DriveServiceLookup.getService(ContextService.class).getSchemaAssociations();
        if (schemaAssociations == null || schemaAssociations.isEmpty()) {
            LOG.debug("No schema assosiation for schema {} and database pool id {} found.", schema, I(poolId));
            return new EligibleContextsResult(upToDateContextIDs, numOfUnUpdatedContexts);
        }
        List<Integer> contextsInSchema = schemaAssociations.get(new PoolAndSchema(poolId, schema));
        Updater updater = Updater.getInstance();
        if (contextsInSchema != null) {
            UpdateStatus status = updater.getStatus(contextsInSchema.get(0).intValue());
            if (status.needsBackgroundUpdates() || status.needsBlockingUpdates() || status.backgroundUpdatesRunning() || status.blockingUpdatesRunning()) {
                numOfUnUpdatedContexts += contextsInSchema.size();
            } else {
                upToDateContextIDs.addAll(contextsInSchema);
            }
        }
        return new EligibleContextsResult(upToDateContextIDs, numOfUnUpdatedContexts);
    }

    private static class EligibleContextsResult {
        final List<Integer> upToDateContextIDs;
        final int numOfUnUpdatedContexts;

        EligibleContextsResult(Set<Integer> upToDateContextIDs, int numOfUnUpdatedContexts) {
            super();
            this.upToDateContextIDs = new ArrayList<>(upToDateContextIDs);
            this.numOfUnUpdatedContexts = numOfUnUpdatedContexts;
        }
    }

}
