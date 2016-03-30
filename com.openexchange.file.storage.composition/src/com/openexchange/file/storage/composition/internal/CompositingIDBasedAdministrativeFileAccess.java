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
