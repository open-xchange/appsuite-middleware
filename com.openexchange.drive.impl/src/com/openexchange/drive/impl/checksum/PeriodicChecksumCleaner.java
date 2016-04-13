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

import static com.openexchange.java.Autoboxing.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.checksum.rdb.RdbChecksumStore;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.groupware.update.UpdateStatus;
import com.openexchange.groupware.update.Updater;
import com.openexchange.java.Autoboxing;

/**
 * {@link PeriodicChecksumCleaner}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.1
 */
public class PeriodicChecksumCleaner implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(PeriodicChecksumCleaner.class);

    private final AtomicBoolean active;
    private final long checksumExpiry;

    /**
     * Initializes a new {@link PeriodicChecksumCleaner}.
     *
     * @param checksumExpiry The timespan (in milliseconds) after which an unused directory checksum may be deleted permanently
     */
    public PeriodicChecksumCleaner(long checksumExpiry) {
        super();
        this.checksumExpiry = checksumExpiry;
        this.active = new AtomicBoolean(true);
    }

    @Override
    public void run() {
        long start = System.currentTimeMillis();
        try {
            List<Integer> allContextIDs = DriveServiceLookup.getService(ContextService.class).getAllContextIds();
            LOG.info("Periodic checksum cleanup task starting, going to check {} contexts...", I(allContextIDs.size()));
            List<Integer> contextIDs = getEligibleContextIDs(allContextIDs);
            if (allContextIDs.size() != contextIDs.size()) {
                LOG.info("Skipping {} contexts due to not up-to-date database schemas.", I(allContextIDs.size() - contextIDs.size()));
            }
            long logTimeDistance = TimeUnit.SECONDS.toMillis(10);
            long lastLogTime = start;
            for (int i = 0; i < contextIDs.size(); i++) {
                int contextID = contextIDs.get(i).intValue();
                for (int retry = 0; retry < 3; retry++) {
                    if (false == active.get()) {
                        LOG.info("Periodic checksum cleanup task stopping.");
                        return;
                    }
                    long now = System.currentTimeMillis();
                    if (now > lastLogTime + logTimeDistance) {
                        LOG.info("Periodic checksum cleanup task {}% finished ({}/{}).", I(i * 100 / contextIDs.size()), I(i), I(contextIDs.size()));
                        lastLogTime = now;
                    }
                    try {
                        cleanupContext(contextID, now - checksumExpiry);
                        break;
                    } catch (OXException e) {
                        if (Category.CATEGORY_TRY_AGAIN.equals(e.getCategory()) && retry < 3) {
                            long delay = 10000 + retry * 20000;
                            LOG.debug("Error during periodic checksum cleanup task for context {}: {}; trying again in {}ms...",
                                I(contextID), e.getMessage(), L(delay));
                            Thread.sleep(delay);
                        } else {
                            LOG.error("Error during periodic checksum cleanup task for context {}: {}", I(contextID), e.getMessage(), e);
                            break;
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOG.warn("Interrupted during periodic checksum cleanup task: {}", e.getMessage(), e);
        } catch (Exception e) {
            LOG.error("Error during periodic checksum cleanup task: {}", e.getMessage(), e);
        }
        LOG.info("Periodic checksum cleanup task finished after {}ms.", L(System.currentTimeMillis() - start));
    }

    /**
     * Stops all background processing by signaling termination flag.
     */
    public void stop() {
        active.set(false);
    }

    /**
     * Synchronously cleans obsolete checksums for a context.
     *
     * @param contextID The context ID
     * @param unusedSince The maximum "used" timestamp of a checksum to be considered as "unused"
     */
    private void cleanupContext(int contextID, long unusedSince) throws OXException {
        RdbChecksumStore checksumStore = new RdbChecksumStore(contextID);
        List<DirectoryChecksum> unusedChecksums = checksumStore.getUnusedDirectoryChecksums(unusedSince);
        if (0 == unusedChecksums.size()) {
            LOG.debug("No unused directory checksums detected in context {}.", I(contextID));
            return;
        }
        /*
         * collect affected folder identifiers
         */
        Set<FolderID> folderIDs = new HashSet<FolderID>();
        for (DirectoryChecksum unusedChecksum : unusedChecksums) {
            folderIDs.add(unusedChecksum.getFolderID());
        }
        /*
         * remove checksums
         */
        int removed = checksumStore.removeDirectoryChecksums(unusedChecksums);
        LOG.debug("Removed {} unused directory checksums in context {}.", I(removed), I(contextID));
        /*
         * determine folder ids no longer referenced at all by checking against the still used directory checksums
         */
        Set<FolderID> obsoleteFolderIDs = new HashSet<FolderID>(folderIDs);
        List<DirectoryChecksum> usedChecksums = checksumStore.getDirectoryChecksums(new ArrayList<FolderID>(folderIDs));
        for (DirectoryChecksum usedChecksum : usedChecksums) {
            obsoleteFolderIDs.remove(usedChecksum.getFolderID());
        }
        /*
         * remove file checksums for obsolete folder ids, too
         */
        if (0 < obsoleteFolderIDs.size()) {
            removed = checksumStore.removeFileChecksumsInFolders(new ArrayList<FolderID>(obsoleteFolderIDs));
            LOG.debug("Removed {} file checksums for {} obsolete directories in context {}.", I(removed), I(obsoleteFolderIDs.size()), I(contextID));
        }
    }

    /**
     * Gets the identifiers of all contexts eligible for a checksum cleaner run, i.e. contexts from non-up-to-date schemas are filtered
     * out beforehand.
     *
     * @param allContextIDs All possible context identifiers
     * @return The context identifiers
     */
    private static List<Integer> getEligibleContextIDs(List<Integer> allContextIDs) throws OXException {
        Set<Integer> upToDateContextIDs = new HashSet<Integer>();
        Set<Integer> unUpdatedContextIDs = new HashSet<Integer>();
        DatabaseService databaseService = DriveServiceLookup.getService(DatabaseService.class);
        Updater updater = Updater.getInstance();
        for (Integer contextID : allContextIDs) {
            if (upToDateContextIDs.contains(contextID) || unUpdatedContextIDs.contains(contextID)) {
                continue;
            }
            List<Integer> contextsInSchema = Arrays.asList(Autoboxing.i2I(databaseService.getContextsInSameSchema(contextID)));
            UpdateStatus status = updater.getStatus(contextID);
            if (status.needsBackgroundUpdates() || status.needsBlockingUpdates() || status.backgroundUpdatesRunning() || status.blockingUpdatesRunning()) {
                unUpdatedContextIDs.addAll(contextsInSchema);
                unUpdatedContextIDs.add(contextID);
            } else {
                upToDateContextIDs.addAll(contextsInSchema);
                upToDateContextIDs.add(contextID);
            }
        }
        return new ArrayList<Integer>(upToDateContextIDs);
    }

}
