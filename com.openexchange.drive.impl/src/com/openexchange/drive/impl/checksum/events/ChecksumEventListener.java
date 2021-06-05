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
import static com.openexchange.file.storage.FileStorageEventConstants.UPDATE_FOLDER_TOPIC;
import static com.openexchange.file.storage.FileStorageEventConstants.UPDATE_TOPIC;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.drive.checksum.rdb.RdbChecksumStore;
import com.openexchange.drive.impl.DriveUtils;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.session.Session;

/**
 * {@link ChecksumEventListener}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class ChecksumEventListener implements EventHandler {

    /**
     * Gets the event topics handled by the checksum event listener.
     *
     * @return An array of handled event topics.
     */
    public static String[] getHandledTopics() {
        return new String[] { DELETE_TOPIC, UPDATE_TOPIC, CREATE_TOPIC, DELETE_FOLDER_TOPIC, UPDATE_FOLDER_TOPIC };
    }

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ChecksumEventListener.class);

    /**
     * Initializes a new {@link ChecksumEventListener}.
     */
    public ChecksumEventListener() {
        super();
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
            RdbChecksumStore checksumStore = new RdbChecksumStore(session.getContextId());
            String topic = event.getTopic();
            if (DELETE_TOPIC.equals(topic) || UPDATE_TOPIC.equals(topic) || CREATE_TOPIC.equals(topic)) {
                /*
                 * extract event properties
                 */
                String serviceID = FileStorageEventHelper.extractService(event);
                String accountID = FileStorageEventHelper.extractAccountId(event);
                String folderID = FileStorageEventHelper.extractFolderId(event);
                /*
                 * invalidate checksum of parent directory
                 */
                checksumStore.removeDirectoryChecksum(new FolderID(serviceID, accountID, folderID));
                /*
                 * invalidate checksum of file in case of deletion or update
                 */
                if (DELETE_TOPIC.equals(topic) || UPDATE_TOPIC.equals(topic)) {
                    String objectID = FileStorageEventHelper.extractObjectId(event);
                    checksumStore.removeFileChecksums(new FileID(serviceID, accountID, folderID, objectID));
                }
            } else if (DELETE_FOLDER_TOPIC.equals(topic) || UPDATE_FOLDER_TOPIC.equals(topic)) {
                /*
                 * extract event properties
                 */
                String serviceID = FileStorageEventHelper.extractService(event);
                String accountID = FileStorageEventHelper.extractAccountId(event);
                String folderID = FileStorageEventHelper.extractFolderId(event);
                /*
                 * invalidate checksums of directory and contained files
                 */
                FolderID id = new FolderID(serviceID, accountID, folderID);
                checksumStore.removeDirectoryChecksum(id);
                checksumStore.removeFileChecksumsInFolder(id);
            }
        } catch (OXException e) {
            LOG.warn("unexpected error during event handling", e);
        }
    }

}
