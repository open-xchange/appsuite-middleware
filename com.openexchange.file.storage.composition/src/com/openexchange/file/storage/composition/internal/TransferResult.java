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

package com.openexchange.file.storage.composition.internal;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.event.Event;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageEventHelper;
import com.openexchange.file.storage.FileStorageEventHelper.EventProperty;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.session.Session;

/**
 * {@link TransferResult}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class TransferResult {

    private final FolderID sourceFolderID;
    private final FileStorageFolder[] sourcePath;
    private final FolderID targetFolderID;
    private final FileStorageFolder[] targetPath;
    private final Map<File, IDTuple> transferredFiles;
    private final List<TransferResult> nestedResults;
    private final List<OXException> warnings;

    /**
     * Initializes a new {@link TransferResult}.
     *
     * @param sourceFolderID The fully qualified identifier of the source folder
     * @param sourcePath The path to the source folder
     * @param targetFolderID The fully qualified identifier of the target folder
     * @param targetPath The path to the target folder
     */
    public TransferResult(FolderID sourceFolderID, FileStorageFolder[] sourcePath, FolderID targetFolderID, FileStorageFolder[] targetPath) {
        super();
        this.sourceFolderID = sourceFolderID;
        this.sourcePath = sourcePath;
        this.targetFolderID = targetFolderID;
        this.targetPath = targetPath;
        this.transferredFiles = new HashMap<File, IDTuple>();
        this.nestedResults = new ArrayList<TransferResult>();
        this.warnings = new ArrayList<OXException>();
    }

    public void addTransferredFile(File sourceFile, IDTuple targetID) {
        transferredFiles.put(sourceFile, targetID);
    }

    public void addTransferredFiles(Map<? extends File, ? extends IDTuple> transferredFiles) {
        this.transferredFiles.putAll(transferredFiles);
    }

    public void addNestedResult(TransferResult nestedResult) {
        nestedResults.add(nestedResult);
    }

    public void addWarnings(List<OXException> warnings) {
        this.warnings.addAll(warnings);
    }

    public List<Event> buildCreateEvents(Session session) {
        List<Event> createEvents = new ArrayList<Event>();
        createEvents.addAll(buildFileCreateEvents(session));
        createEvents.addAll(buildFolderCreateEvents(session));
        return createEvents;
    }

    public List<Event> buildDeleteEvents(Session session) {
        List<Event> deleteEvents = new ArrayList<Event>();
        deleteEvents.addAll(buildFileDeleteEvents(session));
        deleteEvents.addAll(buildFolderDeleteEvents(session));
        return deleteEvents;
    }

    public List<Event> buildFileCreateEvents(Session session) {
        List<Event> createEvents = new ArrayList<Event>();
        for (Map.Entry<File, IDTuple> entry : transferredFiles.entrySet()) {
            IDTuple id = entry.getValue();
            String fileID = new FileID(targetFolderID.getService(), targetFolderID.getAccountId(), id.getFolder(), id.getId()).toUniqueID();
            String fileName = entry.getKey().getFileName();
            FolderID[] path = FileStorageTools.getPath(targetPath, targetFolderID.getService(), targetFolderID.getAccountId());
            createEvents.add(FileStorageEventHelper.buildCreateEvent(session, targetFolderID.getService(), targetFolderID.getAccountId(),
                targetFolderID.toUniqueID(), fileID, fileName, new EventProperty(FileStorageEventConstants.FOLDER_PATH, path)));
        }
        for (TransferResult nestedResult : nestedResults) {
            createEvents.addAll(nestedResult.buildFileCreateEvents(session));
        }
        return createEvents;
    }

    public List<Event> buildFileDeleteEvents(Session session) {
        List<Event> deleteEvents = new ArrayList<Event>();
        for (Map.Entry<File, IDTuple> entry : transferredFiles.entrySet()) {
            File file = entry.getKey();
            String fileID = new FileID(sourceFolderID.getService(), sourceFolderID.getAccountId(), sourceFolderID.getFolderId(), file.getId()).toUniqueID();
            String fileName = file.getFileName();
            FolderID[] path = FileStorageTools.getPath(targetPath, targetFolderID.getService(), targetFolderID.getAccountId());
            deleteEvents.add(FileStorageEventHelper.buildDeleteEvent(session, sourceFolderID.getService(), sourceFolderID.getAccountId(),
                sourceFolderID.toUniqueID(), fileID, fileName, null, new EventProperty(FileStorageEventConstants.FOLDER_PATH, path)));
        }
        for (TransferResult nestedResult : nestedResults) {
            deleteEvents.addAll(nestedResult.buildFileDeleteEvents(session));
        }
        return deleteEvents;
    }

    public List<Event> buildFolderCreateEvents(Session session) {
        List<Event> createEvents = new ArrayList<Event>();
        Dictionary<String, Object> properties = FileStorageTools.getEventProperties(session, targetFolderID, targetPath);
        createEvents.add(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, properties));
        for (TransferResult nestedResult : nestedResults) {
            createEvents.addAll(nestedResult.buildFolderCreateEvents(session));
        }
        return createEvents;
    }

    public List<Event> buildFolderDeleteEvents(Session session) {
        List<Event> deleteEvents = new ArrayList<Event>();
        Dictionary<String, Object> properties = FileStorageTools.getEventProperties(session, sourceFolderID, sourcePath);
        deleteEvents.add(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, properties));
        for (TransferResult nestedResult : nestedResults) {
            deleteEvents.addAll(nestedResult.buildFolderDeleteEvents(session));
        }
        return deleteEvents;
    }

    /**
     * Gets the copiedFiles
     *
     * @return The copiedFiles
     */
    public Map<File, IDTuple> getTransferredFiles() {
        return transferredFiles;
    }

    /**
     * Gets the nested copy results from subfolders.
     *
     * @return The nested copy results
     */
    public List<TransferResult> getNestedResults() {
        return nestedResults;
    }

    /**
     * Gets the warnings.
     *
     * @param includeNested <code>true</code> to include the warnings of nested results, <code>false</code>, otherwise
     * @return The warnings
     */
    public List<OXException> getWarnings(boolean includeNested) {
        if (includeNested && null != nestedResults && 0 < nestedResults.size()) {
            List<OXException> warnings = new ArrayList<OXException>();
            warnings.addAll(this.warnings);
            for (TransferResult nestedResult : nestedResults) {
                warnings.addAll(nestedResult.getWarnings(includeNested));
            }
            return warnings;
        }
        return this.warnings;
    }

    /**
     * Gets the identifier of the target folder.
     *
     * @return The target folder ID
     */
    public FolderID getTargetFolderID() {
        return targetFolderID;
    }

}
