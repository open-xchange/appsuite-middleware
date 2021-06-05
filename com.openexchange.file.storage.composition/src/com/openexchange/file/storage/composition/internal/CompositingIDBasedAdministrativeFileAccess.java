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

import static com.openexchange.osgi.Tools.requireService;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AdministrativeFileStorageFileAccess;
import com.openexchange.file.storage.AdministrativeFileStorageService;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess.IDTuple;
import com.openexchange.file.storage.FileStorageFileAccess.SortDirection;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.Range;
import com.openexchange.file.storage.composition.FileID;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedAdministrativeFileAccess;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.server.ServiceLookup;


/**
 * This implementation of {@link IDBasedAdministrativeFileAccess} determines the actual
 * {@link AdministrativeFileStorageFileAccess} implementation for given full-qualified
 * {@link FileID}s and {@link FolderID}s and delegates the actual calls.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class CompositingIDBasedAdministrativeFileAccess implements IDBasedAdministrativeFileAccess {

    private final int contextId;

    private final ServiceLookup services;

    public CompositingIDBasedAdministrativeFileAccess(int contextId, ServiceLookup services) {
        super();
        this.contextId = contextId;
        this.services = services;
    }

    @Override
    public boolean supports(String id) throws OXException {
        FileID fileID = toFileID(id);
        return getFileAccess(fileID.getService(), fileID.getAccountId()) != null;
    }

    @Override
    public File getFileMetadata(String id, String version) throws OXException {
        FileID fileID = toFileID(id);
        AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(fileID.getService(), fileID.getAccountId());
        return fileAccess.getFileMetadata(fileID.getFolderId(), fileID.getFileId(), version);
    }

    @Override
    public void saveFileMetadata(File document, long sequenceNumber, List<Field> modifiedColumns) throws OXException {
        FolderID folderID = new FolderID(document.getFolderId());
        AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(folderID.getService(), folderID.getAccountId());
        fileAccess.saveFileMetadata(document, sequenceNumber, modifiedColumns);
    }

    @Override
    public void touch(final String id) throws OXException {
        FileID fileID = toFileID(id);
        AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(fileID.getService(), fileID.getAccountId());
        fileAccess.touch(fileID.getFolderId(), fileID.getFileId());
    }

    @Override
    public void removeDocument(String id) throws OXException {
        FileID fileID = toFileID(id);
        AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(fileID.getService(), fileID.getAccountId());
        fileAccess.removeDocument(fileID.getFolderId(), fileID.getFileId());
    }

    @Override
    public void removeDocuments(List<String> ids) throws OXException {
        Map<String, Map<String, List<IDTuple>>> idsByService = new HashMap<String, Map<String, List<IDTuple>>>();
        for (String id : ids) {
            FileID fileID = toFileID(id);
            String service = fileID.getService();
            String account = fileID.getAccountId();
            Map<String, List<IDTuple>> idsByAccount = idsByService.get(service);
            if (idsByAccount == null) {
                idsByAccount = new HashMap<String, List<IDTuple>>();
                idsByService.put(service, idsByAccount);
            }

            List<IDTuple> tuples = idsByAccount.get(account);
            if (tuples == null) {
                tuples = new LinkedList<IDTuple>();
                idsByAccount.put(account, tuples);
            }

            tuples.add(new IDTuple(fileID.getFolderId(), fileID.getFileId()));
        }

        Map<AdministrativeFileStorageFileAccess, List<IDTuple>> idsByAccess = new HashMap<AdministrativeFileStorageFileAccess, List<IDTuple>>();
        for (Entry<String, Map<String, List<IDTuple>>> idsByServiceEntry : idsByService.entrySet()) {
            String service = idsByServiceEntry.getKey();
            Map<String, List<IDTuple>> idsByAccount = idsByServiceEntry.getValue();
            for (Entry<String, List<IDTuple>> accountEntry : idsByAccount.entrySet()) {
                String account = accountEntry.getKey();
                AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(service, account);
                idsByAccess.put(fileAccess, accountEntry.getValue());
            }
        }

        for (Entry<AdministrativeFileStorageFileAccess, List<IDTuple>> entry : idsByAccess.entrySet()) {
            AdministrativeFileStorageFileAccess fileAccess = entry.getKey();
            fileAccess.removeDocuments(entry.getValue());
        }
    }

    @Override
    public boolean exists(String id, String version) throws OXException {
        FileID fileID = toFileID(id);
        AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(fileID.getService(), fileID.getAccountId());
        return fileAccess.exists(fileID.getFolderId(), fileID.getFileId(), version);
    }

    @Override
    public boolean canRead(String id, int userId) throws OXException {
        FileID fileID = toFileID(id);
        AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(fileID.getService(), fileID.getAccountId());
        return fileAccess.canRead(fileID.getFolderId(), fileID.getFileId(), userId);
    }

    @Override
    public boolean canWrite(String id, int userId) throws OXException {
        FileID fileID = toFileID(id);
        AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(fileID.getService(), fileID.getAccountId());
        return fileAccess.canWrite(fileID.getFolderId(), fileID.getFileId(), userId);
    }

    @Override
    public boolean canDelete(String id, int userId) throws OXException {
        FileID fileID = toFileID(id);
        AdministrativeFileStorageFileAccess fileAccess = requireFileAccess(fileID.getService(), fileID.getAccountId());
        return fileAccess.canDelete(fileID.getFolderId(), fileID.getFileId(), userId);
    }

    @Override
    public TimedResult<File> getDocuments(String folderId, int userId, List<Field> fields, Field sort, SortDirection order, Range range) throws OXException {
        FolderID folderID = new FolderID(folderId);
        return requireFileAccess(folderID.getService(), folderID.getAccountId()).getDocuments(folderID.getFolderId(), userId, fields, sort, order, range);
    }

    private static FileID toFileID(String id) {
        return new FileID(id);
    }

    private AdministrativeFileStorageFileAccess requireFileAccess(final String serviceId, final String accountId) throws OXException {
        AdministrativeFileStorageFileAccess fileAccess = getFileAccess(serviceId, accountId);
        if (fileAccess == null) {
            throw FileStorageExceptionCodes.ADMIN_FILE_ACCESS_NOT_AVAILABLE.create(serviceId);
        }

        return fileAccess;
    }

    private AdministrativeFileStorageFileAccess getFileAccess(final String serviceId, final String accountId) throws OXException {
        FileStorageServiceRegistry storageRegistry = requireService(FileStorageServiceRegistry.class, services);
        FileStorageService fileStorageService = storageRegistry.getFileStorageService(serviceId);
        if (fileStorageService instanceof AdministrativeFileStorageService) {
            return ((AdministrativeFileStorageService) fileStorageService).getAdministrativeFileAccess(accountId, contextId);
        }

        return null;
    }

}
