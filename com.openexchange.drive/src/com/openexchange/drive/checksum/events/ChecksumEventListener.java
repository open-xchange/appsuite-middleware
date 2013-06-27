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
 *     Copyright (C) 2004-2013 Open-Xchange, Inc.
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

package com.openexchange.drive.checksum.events;

import static com.openexchange.file.storage.FileStorageEventConstants.DELETE_FOLDER_TOPIC;
import static com.openexchange.file.storage.FileStorageEventConstants.DELETE_TOPIC;
import static com.openexchange.file.storage.FileStorageEventConstants.UPDATE_FOLDER_TOPIC;
import static com.openexchange.file.storage.FileStorageEventConstants.UPDATE_TOPIC;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.logging.Log;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import com.openexchange.drive.checksum.rdb.RdbChecksumStore;
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

    public static String[] getHandledTopics() {
        return new String[] { DELETE_TOPIC, UPDATE_TOPIC, DELETE_FOLDER_TOPIC, UPDATE_FOLDER_TOPIC };
    }

    private static final List<String> DRIVE_CLIENTS = Arrays.asList(new String[] {
        "OpenXchange.HTTPClient.OXDrive",
        "OpenXchange.HTTPClient.TestDrive",
        "ox-client.android.normal.hdpi",
        "OpenXchange.iosClient.OXDrive",
    });

    private static final Log LOG = com.openexchange.log.Log.loggerFor(ChecksumEventListener.class);

    /**
     * Initializes a new {@link ChecksumEventListener}.
     */
    public ChecksumEventListener() {
        super();
    }

    @Override
    public void handleEvent(Event event) {
        try {
            if (LOG.isDebugEnabled()) {
                LOG.debug(FileStorageEventHelper.createDebugMessage("event", event));
            }
            String topic = event.getTopic();
            Session session = FileStorageEventHelper.extractSession(event);
            if (null == session || isDriveSession(session)) {
                // skip
            } else if (DELETE_TOPIC.equals(topic) || UPDATE_TOPIC.equals(topic)) {
                FileID fileID = new FileID(
                    FileStorageEventHelper.extractService(event),
                    FileStorageEventHelper.extractAccountId(event),
                    FileStorageEventHelper.extractFolderId(event),
                    FileStorageEventHelper.extractObjectId(event)
                );
                invalidateFile(session, fileID);
            } else if (DELETE_FOLDER_TOPIC.equals(topic) || UPDATE_FOLDER_TOPIC.equals(topic)) {
                FolderID folderID = new FolderID(
                    FileStorageEventHelper.extractService(event),
                    FileStorageEventHelper.extractAccountId(event),
                    FileStorageEventHelper.extractFolderId(event)
                );
                invalidateFolder(session, folderID);
            }
        } catch (OXException e) {
            LOG.warn("unexpected error during event handling", e);
        }
    }

    private static boolean isDriveSession(Session session) {
        return null != session && DRIVE_CLIENTS.contains(session.getClient());
    }

    private static void invalidateFolder(Session session, FolderID folderID) throws OXException {
        if (null == folderID) {
            LOG.warn("No folder ID specified, unable to invalidate checksums.");
        } else if (null == session) {
            LOG.warn("Unable to invalidate checksums for folder '" + folderID + "' due to missing session.");
        } else {
            RdbChecksumStore checksumStore = new RdbChecksumStore(session.getContextId());
            checksumStore.removeDirectoryChecksum(folderID);
            checksumStore.removeFileChecksumsInFolder(folderID);
        }
    }

    private static void invalidateFile(Session session, FileID fileID) throws OXException {
        if (null == fileID) {
            LOG.warn("No file ID specified, unable to invalidate checksums.");
        } else if (null == session) {
            LOG.warn("Unable to invalidate checksums for file '" + fileID + "' due to missing session.");
        } else {
            RdbChecksumStore checksumStore = new RdbChecksumStore(session.getContextId());
            checksumStore.removeFileChecksums(fileID);
        }
    }

}
