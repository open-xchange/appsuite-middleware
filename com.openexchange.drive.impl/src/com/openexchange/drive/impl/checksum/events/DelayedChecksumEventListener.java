/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.drive.impl.checksum.events;

import static com.openexchange.file.storage.FileStorageEventConstants.CREATE_TOPIC;
import static com.openexchange.file.storage.FileStorageEventConstants.DELETE_FOLDER_TOPIC;
import static com.openexchange.file.storage.FileStorageEventConstants.DELETE_TOPIC;
import static com.openexchange.file.storage.FileStorageEventConstants.FILE_NAME;
import static com.openexchange.file.storage.FileStorageEventConstants.UPDATE_FOLDER_TOPIC;
import static com.openexchange.file.storage.FileStorageEventConstants.UPDATE_TOPIC;
import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.drive.checksum.rdb.RdbChecksumStore;
import com.openexchange.drive.impl.DriveConstants;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.drive.impl.internal.DriveServiceLookup;
import com.openexchange.drive.impl.management.DriveConfig;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.java.Strings;
import com.openexchange.server.Initialization;
import com.openexchange.session.Session;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * {@link DelayedChecksumEventListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DelayedChecksumEventListener implements EventHandler, Initialization {

    /**
     * Gets the event topics handled by the checksum event listener.
     *
     * @return An array of handled event topics.
     */
    public static String[] getHandledTopics() {
        return new String[] { DELETE_TOPIC, UPDATE_TOPIC, CREATE_TOPIC, DELETE_FOLDER_TOPIC, UPDATE_FOLDER_TOPIC };
    }

    /**
     * Gets the checksum event listener instance.
     *
     * @return The instance
     */
    public static DelayedChecksumEventListener getInstance() {
        return instance;
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(DelayedChecksumEventListener.class);
    private static final int TRIGGER_INTERVAL = 2000;
    private static final DelayedChecksumEventListener instance = new DelayedChecksumEventListener();

    private final DelayedChecksumInvalidationQueue invalidationQueue;
    private final AtomicBoolean started;
    private ScheduledTimerTask timerTask;

    /**
     * Initializes a new {@link DelayedChecksumEventListener}.
     */
    private DelayedChecksumEventListener() {
        super();
        this.started = new AtomicBoolean();
        invalidationQueue = new DelayedChecksumInvalidationQueue();
    }

    @Override
    public void start() throws OXException {
        if (false == started.compareAndSet(false, true)) {
            LOG.warn("Already started - aborting.");
            return;
        }
        /*
         * schedule checksum invalidations regularly
         */
        TimerService timerService = DriveServiceLookup.getService(TimerService.class);
        this.timerTask = timerService.scheduleWithFixedDelay(() -> {
            try {
                triggerChecksumInvalidation();
            } catch (Exception e) {
                LOG.warn("Error triggering checksum invalidations", e);
            }
        }, TRIGGER_INTERVAL, TRIGGER_INTERVAL);
    }

    @Override
    public void stop() throws OXException {
        if (false == started.compareAndSet(true, false)) {
            LOG.warn("Not started - aborting.");
            return;
        }
        if (null != timerTask) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    @Override
    public void handleEvent(final Event event) {
        try {
            Session session = FileStorageEventHelper.extractSession(event);
            if (null == session || DriveUtils.isDriveSession(session)) {
                // skip
                return;
            }
            LOG.debug("{}", new Object() { @Override public String toString() { return FileStorageEventHelper.createDebugMessage("event", event);}});
            int contextID = session.getContextId();
            String topic = event.getTopic();
            if (DELETE_TOPIC.equals(topic) || UPDATE_TOPIC.equals(topic) || CREATE_TOPIC.equals(topic)) {
                /*
                 * extract event properties
                 */
                String folderID = FileStorageEventHelper.extractFolderId(event);
                String objectID = FileStorageEventHelper.extractObjectId(event);
                String fileName = (String)event.getProperty(FILE_NAME);
                if (Strings.isNotEmpty(fileName) && (FilenameValidationUtils.isInvalidFileName(fileName) ||
                    DriveUtils.isIgnoredFileName(fileName, new DriveConfig(contextID, session.getUserId())))) {
                    LOG.trace("Skipping event processing for ignored file: {}", fileName);
                    return;
                }
                /*
                 * enqueue invalidation
                 */
                invalidationQueue.offer(new DelayedChecksumInvalidation(contextID, topic, new FolderID(folderID), new FileID(objectID)));
            } else if (DELETE_FOLDER_TOPIC.equals(topic) || UPDATE_FOLDER_TOPIC.equals(topic)) {
                /*
                 * extract event properties
                 */
                String folderID = FileStorageEventHelper.extractFolderId(event);
                /*
                 * enqueue invalidation
                 */
                invalidationQueue.offer(new DelayedChecksumInvalidation(contextID, topic, new FolderID(folderID), null));
            }
        } catch (OXException e) {
            LOG.warn("unexpected error during event handling", e);
        }
    }

    void triggerChecksumInvalidation() {
        DelayedChecksumInvalidation invalidation = invalidationQueue.poll();
        if (null != invalidation) {
            List<DelayedChecksumInvalidation> invalidations = new ArrayList<DelayedChecksumInvalidation>();
            do {
                invalidations.add(invalidation);
                invalidation = invalidationQueue.poll();
            } while (null != invalidation);
            invalidateChecksums(invalidations);
        }
    }

    private void invalidateChecksums(List<DelayedChecksumInvalidation> invalidations) {
        if (null != invalidations && 0 < invalidations.size()) {
            Map<Integer, Set<FolderID>> directoryChecksumsToInvalidate = new HashMap<Integer, Set<FolderID>>();
            Map<Integer, Set<FileID>> fileChecksumsToInvalidate = new HashMap<Integer, Set<FileID>>();
            Map<Integer, Set<FolderID>> fileChecksumsInFolderToInvalidate = new HashMap<Integer, Set<FolderID>>();
            for (DelayedChecksumInvalidation invalidation : invalidations) {
                String topic = invalidation.getTopic();
                Integer contextID = Integer.valueOf(invalidation.getContextID());
                if (DELETE_TOPIC.equals(topic) || UPDATE_TOPIC.equals(topic) || CREATE_TOPIC.equals(topic)) {
                    /*
                     * invalidate checksum of parent directory
                     */
                    if (null != invalidation.getFolderID()) {
                        Set<FolderID> folderIDs = directoryChecksumsToInvalidate.get(contextID);
                        if (null == folderIDs) {
                            folderIDs = new HashSet<FolderID>();
                            directoryChecksumsToInvalidate.put(contextID, folderIDs);
                        }
                        folderIDs.add(invalidation.getFolderID());
                    }
                    /*
                     * invalidate any .drive-meta checksums for parent directory, too
                     */
                    if (null != invalidation.getFolderID()) {
                        Set<FileID> fileIDs = fileChecksumsToInvalidate.get(contextID);
                        if (null == fileIDs) {
                            fileIDs = new HashSet<FileID>();
                            fileChecksumsToInvalidate.put(contextID, fileIDs);
                        }
                        fileIDs.add(new FileID(invalidation.getFolderID().toUniqueID() + '/' + DriveConstants.METADATA_FILENAME));
                    }
                    /*
                     * invalidate checksum of file in case of deletion or update
                     */
                    if (DELETE_TOPIC.equals(topic) || UPDATE_TOPIC.equals(topic) && null != invalidation.getFileID()) {
                        Set<FileID> fileIDs = fileChecksumsToInvalidate.get(contextID);
                        if (null == fileIDs) {
                            fileIDs = new HashSet<FileID>();
                            fileChecksumsToInvalidate.put(contextID, fileIDs);
                        }
                        fileIDs.add(invalidation.getFileID());
                    }
                } else if (DELETE_FOLDER_TOPIC.equals(topic) || UPDATE_FOLDER_TOPIC.equals(topic)) {
                    /*
                     * invalidate checksums of directory and contained files
                     */
                    Set<FolderID> folderIDs = directoryChecksumsToInvalidate.get(contextID);
                    if (null == folderIDs) {
                        folderIDs = new HashSet<FolderID>();
                        directoryChecksumsToInvalidate.put(contextID, folderIDs);
                    }
                    folderIDs.add(invalidation.getFolderID());
                    folderIDs = fileChecksumsInFolderToInvalidate.get(contextID);
                    if (null == folderIDs) {
                        folderIDs = new HashSet<FolderID>();
                        fileChecksumsInFolderToInvalidate.put(contextID, folderIDs);
                    }
                    folderIDs.add(invalidation.getFolderID());
                }
            }
            /*
             * trigger invalidations
             */
            removeRedundantFiles(fileChecksumsToInvalidate, fileChecksumsInFolderToInvalidate);
            invalidateDirectoryChecksums(directoryChecksumsToInvalidate);
            invalidateFileChecksumsInFolder(fileChecksumsInFolderToInvalidate);
            invalidateFileChecksums(fileChecksumsToInvalidate);
        }
    }

    private static void invalidateDirectoryChecksums(Map<Integer, Set<FolderID>> directoryChecksumsToInvalidate) {
        if (0 < directoryChecksumsToInvalidate.size()) {
            try {
                for (Map.Entry<Integer, Set<FolderID>> entry : directoryChecksumsToInvalidate.entrySet()) {
                    RdbChecksumStore checksumStore = new RdbChecksumStore(entry.getKey().intValue());
                    LOG.debug("Invalidating directory checksums for {} folders in context {}...",
                        I(entry.getValue().size()), entry.getKey());
                    checksumStore.removeAllDirectoryChecksums(new ArrayList<FolderID>(entry.getValue()));
                }
            } catch (OXException e) {
                LOG.warn("Error invalidating directory checksums", e);
            }
        }
    }

    private static void invalidateFileChecksums(Map<Integer, Set<FileID>> fileChecksumsToInvalidate) {
        if (0 < fileChecksumsToInvalidate.size()) {
            try {
                for (Map.Entry<Integer, Set<FileID>> entry : fileChecksumsToInvalidate.entrySet()) {
                    RdbChecksumStore checksumStore = new RdbChecksumStore(entry.getKey().intValue());
                    LOG.debug("Invalidating file checksums for {} files in context {}...",
                        I(entry.getValue().size()), entry.getKey());
                    checksumStore.removeFileChecksums(entry.getValue().toArray(new FileID[entry.getValue().size()]));
                }
            } catch (OXException e) {
                LOG.warn("Error invalidating file checksums", e);
            }
        }
    }

    private static void invalidateFileChecksumsInFolder(Map<Integer, Set<FolderID>> fileChecksumsInFolderToInvalidate) {
        if (0 < fileChecksumsInFolderToInvalidate.size()) {
            try {
                for (Map.Entry<Integer, Set<FolderID>> entry : fileChecksumsInFolderToInvalidate.entrySet()) {
                    RdbChecksumStore checksumStore = new RdbChecksumStore(entry.getKey().intValue());
                    LOG.debug("Invalidating file checksums for {} folders in context {}...",
                        I(entry.getValue().size()), entry.getKey());
                    checksumStore.removeFileChecksumsInFolders(new ArrayList<FolderID>(entry.getValue()));
                }
            } catch (OXException e) {
                LOG.warn("Error invalidating file checksums in folder", e);
            }
        }
    }

    private static void removeRedundantFiles(Map<Integer, Set<FileID>> fileChecksumsToInvalidate, Map<Integer, Set<FolderID>> fileChecksumsInFolderToInvalidate) {
        for (Entry<Integer, Set<FolderID>> entry : fileChecksumsInFolderToInvalidate.entrySet()) {
            Integer contextID = entry.getKey();
            Set<FileID> fileIDs = fileChecksumsToInvalidate.get(contextID);
            if (null != fileIDs && 0 < fileIDs.size()) {
                Iterator<FileID> iterator = fileIDs.iterator();
                while (iterator.hasNext()) {
                    FileID fileID = iterator.next();
                    for (FolderID folderID : entry.getValue()) {
                        if (matches(folderID, fileID)) {
                            iterator.remove();
                            break;
                        }
                    }
                }
                if (fileIDs.isEmpty()) {
                    fileChecksumsToInvalidate.remove(contextID);
                }
            }
        }
    }

    private static boolean matches(FolderID folderID, FileID fileID) {
        if (null == folderID || null == folderID.getService() || null == folderID.getAccountId() || null == folderID.getFolderId()) {
            return false;
        }
        if (null == fileID || null == fileID.getService() || null == fileID.getAccountId() || null == fileID.getFolderId()) {
            return false;
        }
        return folderID.getService().equals(fileID.getService()) &&
            folderID.getAccountId().equals(fileID.getAccountId()) &&
            folderID.getFolderId().equals(fileID.getFolderId());
    }

}
