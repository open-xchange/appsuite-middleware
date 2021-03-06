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

import static com.openexchange.file.storage.composition.internal.FileStorageTools.containsForeignPermissions;
import static com.openexchange.file.storage.composition.internal.FileStorageTools.getEventProperties;
import static com.openexchange.file.storage.composition.internal.FileStorageTools.getPath;
import static com.openexchange.file.storage.composition.internal.idmangling.IDManglingFolder.withRelativeID;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.CapabilityAware;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.DefaultTypeAwareFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageAccountManager;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageCapability;
import com.openexchange.file.storage.FileStorageCapabilityTools;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFileAccess;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.FileStorageRestoringFolderAccess;
import com.openexchange.file.storage.FileStorageResult;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.FolderStatsAware;
import com.openexchange.file.storage.PathKnowingFileStorageFolderAccess;
import com.openexchange.file.storage.PermissionAware;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.RootFolderPermissionsAware;
import com.openexchange.file.storage.SearchableFolderNameFolderAccess;
import com.openexchange.file.storage.UserCreatedFileStorageFolderAccess;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.file.storage.composition.internal.idmangling.IDManglingFolder;
import com.openexchange.file.storage.generic.DefaultFileStorageAccount;
import com.openexchange.java.Collators;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.session.Session;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;

/**
 * {@link AbstractCompositingIDBasedFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractCompositingIDBasedFolderAccess extends AbstractCompositingIDBasedAccess implements IDBasedFolderAccess {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCompositingIDBasedFolderAccess.class);
    private static final String INFOSTORE_FOLDER_ID = "9"; // FolderObject.SYSTEM_INFOSTORE_FOLDER_ID

    /**
     * Initializes a new {@link AbstractCompositingIDBasedFolderAccess}.
     *
     * @param session The associated session
     */
    protected AbstractCompositingIDBasedFolderAccess(Session session) {
        super(session);
    }

    @Override
    public boolean hasCapability(FileStorageCapability capability, String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        return hasCapability(capability, folderID);
    }

    @Override
    public boolean hasCapability(FileStorageCapability capability, FolderID folderId) throws OXException {
        FileStorageAccountAccess accountAccess = optAccountAccess(folderId.getService(), folderId.getAccountId());
        if (null == accountAccess) {
            FileStorageService fileStorage = getFileStorageServiceRegistry().getFileStorageService(folderId.getService());
            accountAccess = fileStorage.getAccountAccess(folderId.getAccountId(), session);
        }

        if (accountAccess instanceof CapabilityAware) {
            CapabilityAware capabilityAware = (CapabilityAware) accountAccess;
            Boolean supported = capabilityAware.supports(capability);
            return (null != supported && supported.booleanValue());
        }

        FileStorageFileAccess fileAccess = getFileAccess(folderId);
        return FileStorageCapabilityTools.supports(fileAccess, capability);
    }

    @Override
    public boolean exists(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        try {
            return getFolderAccess(folderID).exists(folderID.getFolderId());
        } catch (OXException e) {
            if (FileStorageExceptionCodes.UNKNOWN_FILE_STORAGE_SERVICE.equals(e)) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public FileStorageFolder getFolder(String folderId) throws OXException {
        return getFolder(new FolderID(folderId));
    }

    @Override
    public FileStorageFolder getFolder(FolderID folderID) throws OXException {
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderID.getFolderId())) {
            return getRootFolder(folderID.getService(), folderID.getAccountId());
        }
        FileStorageFolder folder = getFolderAccess(folderID).getFolder(folderID.getFolderId());
        return withUniqueID(folder, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        FolderID folderID = new FolderID(parentIdentifier);
        FileStorageFolder[] folders = getFolderAccess(folderID).getSubfolders(folderID.getFolderId(), all);
        if (null != folders && 0 < folders.length && INFOSTORE_FOLDER_ID.equals(parentIdentifier)) {
            /*
             * file storage root folders below folder 9 already contain unique identifiers as fetched from
             * com.openexchange.folderstorage.filestorage.FileStorageFolderStorage.getFolder
             */
            return folders;
        }
        return withUniqueID(folders, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {

        if (Strings.isNotEmpty(toCreate.getName())) {
            FilenameValidationUtils.checkCharacters(toCreate.getName());
            FilenameValidationUtils.checkName(toCreate.getName());
        }

        FolderID parentFolderID = new FolderID(toCreate.getParentId());
        FileStorageFolderAccess folderAccess = getFolderAccess(parentFolderID);
        if (containsForeignPermissions(session.getUserId(), toCreate) && false == PermissionAware.class.isInstance(folderAccess)) {
            throw FileStorageExceptionCodes.NO_PERMISSION_SUPPORT.create(FileStorageTools.getAccountName(this, parentFolderID), parentFolderID, Integer.valueOf(session.getContextId()));
        }
        FolderID[] path = getPathIds(parentFolderID.getFolderId(), parentFolderID.getAccountId(), parentFolderID.getService(), folderAccess);
        String newID = folderAccess.createFolder(withRelativeID(toCreate));
        FolderID newFolderID = new FolderID(parentFolderID.getService(), parentFolderID.getAccountId(), newID);
        fire(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, getEventProperties(session, newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        return updateFolder(identifier, toUpdate, false);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate, boolean cascadePermissions) throws OXException {
        return updateFolder(identifier, toUpdate, cascadePermissions, true);
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate, boolean cascadePermissions, boolean ignoreWarnings) throws OXException {
        FolderID folderID = new FolderID(identifier);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        if (containsForeignPermissions(session.getUserId(), toUpdate) && false == PermissionAware.class.isInstance(folderAccess)) {
            throw FileStorageExceptionCodes.NO_PERMISSION_SUPPORT.create(FileStorageTools.getAccountName(this, folderID), folderID, Integer.valueOf(session.getContextId()));
        }
        FolderID[] path = getPathIds(folderID.getFolderId(), folderID.getAccountId(), folderID.getService(), folderAccess);
        FileStorageResult<String> result = null;
        if (cascadePermissions) {
            if (false == PermissionAware.class.isInstance(folderAccess)) {
                throw FileStorageExceptionCodes.NO_PERMISSION_SUPPORT.create(FileStorageTools.getAccountName(this, folderID), folderID, Integer.valueOf(session.getContextId()));
            }
            result = ((PermissionAware) folderAccess).updateFolder(ignoreWarnings, folderID.getFolderId(), withRelativeID(toUpdate), cascadePermissions);
        } else {
            result = folderAccess.updateFolder(folderID.getFolderId(), ignoreWarnings, withRelativeID(toUpdate));
        }

        Collection<OXException> warnings = result.getWarnings();
        if(0 < warnings.size()) {
            addWarnings(warnings);
            if(ignoreWarnings == false) {
                return null;
            }
        }
        if(result.getResponse() == null) {
            return null;
        }

        FolderID newFolderID = new FolderID(folderID.getService(), folderID.getAccountId(), result.getResponse());
        fire(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, getEventProperties(session, newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, boolean ignoreWarnings) throws OXException {
        return moveFolder(folderId, newParentId, null, ignoreWarnings);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName) throws OXException {
        return moveFolder(folderId, newParentId, newName, false);
    }

    @Override
    public String moveFolder(String folderId, String newParentId, String newName, boolean ignoreWarnings) throws OXException {
        FolderID sourceFolderID = new FolderID(folderId);
        FolderID targetParentFolderID = new FolderID(newParentId);
        if (isSameAccount(sourceFolderID, targetParentFolderID)) {
            /*
             * move within same storage
             */
            FileStorageFolderAccess folderAccess = getFolderAccess(sourceFolderID);
            FolderID[] sourcePath = getPathIds(sourceFolderID.getFolderId(), sourceFolderID.getAccountId(), sourceFolderID.getService(), folderAccess);
            String newID;

            if (folderAccess instanceof PermissionAware) {
                FileStorageResult<String> response = ((PermissionAware) folderAccess).moveFolder(ignoreWarnings, sourceFolderID.getFolderId(), targetParentFolderID.getFolderId(), newName);
                newID = response.getResponse();
                Collection<OXException> warnings = response.getWarnings();
                if (0 < warnings.size()) {
                    addWarnings(warnings);
                    if (ignoreWarnings == false) {
                        return null;
                    }
                }
            } else {
                newID = folderAccess.moveFolder(sourceFolderID.getFolderId(), targetParentFolderID.getFolderId(), newName);
            }

            FolderID newFolderID = new FolderID(sourceFolderID.getService(), sourceFolderID.getAccountId(), newID);
            FolderID[] newPath = getPathIds(newID, sourceFolderID.getAccountId(), sourceFolderID.getService(), folderAccess);
            fire(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, getEventProperties(session, sourceFolderID, sourcePath)));
            fire(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, getEventProperties(session, newFolderID, newPath)));

            // TODO: events for nested files & folders ?

            return newFolderID.toUniqueID();
        }
        /*
         * transfer folder(-tree) to target storage recursively
         */
        boolean dryRun = false == ignoreWarnings;
        StorageTransfer storageTransfer = new StorageTransfer(this, sourceFolderID, targetParentFolderID, newName);
        TransferResult transferResult = storageTransfer.run(dryRun);
        if (dryRun && 0 == transferResult.getWarnings(true).size()) {
            dryRun = false;
            transferResult = storageTransfer.run(dryRun);
        }
        if (false == dryRun) {
            /*
             * delete folder in source storage (including all descendants)
             */
            getFolderAccess(sourceFolderID).deleteFolder(sourceFolderID.getFolderId(), true);
            /*
             * fire appropriate events
             */
            EventAdmin eventAdmin = getEventAdmin();
            if (null != eventAdmin) {
                for (Event createEvent : transferResult.buildCreateEvents(session)) {
                    eventAdmin.postEvent(createEvent);
                }
                for (Event deleteEvent : transferResult.buildDeleteEvents(session)) {
                    eventAdmin.postEvent(deleteEvent);
                }
            }
        }
        /*
         * take over any warnings & return resulting folder identifier
         */
        addWarnings(transferResult.getWarnings(true));
        return dryRun ? null : transferResult.getTargetFolderID().toUniqueID();
    }

    @Override
    public String renameFolder(String folderId, String newName) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        FolderID[] path = getPathIds(folderID.getFolderId(), folderID.getAccountId(), folderID.getService(), folderAccess);
        if (folderAccess instanceof UserCreatedFileStorageFolderAccess) {
            String rootId = null;
            try {
                rootId = folderAccess.getRootFolder().getId();
            } catch (OXException e) {
                if (false == FileStorageExceptionCodes.NO_SUCH_FOLDER.equals(e)) {
                    throw e;
                }
            }
            if (null != rootId && rootId.equals(folderID.getFolderId())) {
                // rename of root folder -> rename account
                FileStorageAccountManagerLookupService service = Services.getService(FileStorageAccountManagerLookupService.class);
                if (service == null) {
                    throw ServiceExceptionCode.absentService(FileStorageAccountManagerLookupService.class);
                }
                FileStorageAccountManager accountManager = service.getAccountManager(folderID.getAccountId(), this.session);
                if (accountManager == null) {
                    throw FileStorageExceptionCodes.NO_ACCOUNT_MANAGER_FOR_SERVICE.create(folderID.getService());
                }
                final DefaultFileStorageAccount updatedAccount = new DefaultFileStorageAccount();
                updatedAccount.setId(folderID.getAccountId());
                updatedAccount.setDisplayName(newName);
                accountManager.updateAccount(updatedAccount, session);
                FolderID newFolderID = new FolderID(folderID.getService(), folderID.getAccountId(), rootId);
                fire(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, getEventProperties(session, newFolderID, path)));
                return newFolderID.toUniqueID();
            }
        }

        String newID = folderAccess.renameFolder(folderID.getFolderId(), newName);
        FolderID newFolderID = new FolderID(folderID.getService(), folderID.getAccountId(), newID);
        fire(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, getEventProperties(session, newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String deleteFolder(String folderId) throws OXException {
        return deleteFolder(folderId, false);
    }

    @Override
    public String deleteFolder(String folderId, boolean hardDelete) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);

        if (FileStorageFolder.ROOT_FULLNAME.equals(folderID.getFolderId())) {
            // Deletion of root folder
            if (folderAccess instanceof UserCreatedFileStorageFolderAccess) {
                // Deletion of root folder -> delete account
                FileStorageAccountManagerLookupService service = Services.getService(FileStorageAccountManagerLookupService.class);
                if (service == null) {
                    throw ServiceExceptionCode.absentService(FileStorageAccountManagerLookupService.class);
                }
                FileStorageAccountManager accountManager = service.getAccountManager(folderID.getAccountId(), this.session);
                if (accountManager == null) {
                    throw FileStorageExceptionCodes.NO_ACCOUNT_MANAGER_FOR_SERVICE.create(folderID.getService());
                }

                FileStorageAccount accountToDelete = accountManager.getAccount(folderID.getAccountId(), session);
                accountManager.deleteAccount(accountToDelete, session);

                FolderID[] path = getPathIds(folderID.getFolderId(), folderID.getAccountId(), folderID.getService(), folderAccess);
                Dictionary<String, Object> eventProperties = getEventProperties(session, folderID, path);
                fire(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, eventProperties));
                return folderID.toUniqueID();
            }

            throw FileStorageExceptionCodes.DELETE_DENIED.create(folderID.getService(), folderId);
        }

        FolderID[] path = getPathIds(folderID.getFolderId(), folderID.getAccountId(), folderID.getService(), folderAccess);
        folderAccess.deleteFolder(folderID.getFolderId(), hardDelete);

        Dictionary<String, Object> eventProperties = getEventProperties(session, folderID, path);
        eventProperties.put(FileStorageEventConstants.HARD_DELETE, Boolean.valueOf(hardDelete));
        fire(new Event(FileStorageEventConstants.DELETE_FOLDER_TOPIC, eventProperties));
        return folderID.toUniqueID();
    }

    @Override
    public void clearFolder(String folderId) throws OXException {
        clearFolder(folderId, true);
    }

    @Override
    public void clearFolder(String folderId, boolean hardDelete) throws OXException {
        FolderID folderID = new FolderID(folderId);
        getFolderAccess(folderID).clearFolder(folderID.getFolderId(), hardDelete);
    }

    @Override
    public FileStorageFolder[] getPath2DefaultFolder(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolder[] folders = getFolderAccess(folderID).getPath2DefaultFolder(folderID.getFolderId());
        return withUniqueID(folders, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public Quota getStorageQuota(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        return getFolderAccess(folderID).getStorageQuota(folderID.getFolderId());
    }

    @Override
    public Quota getFileQuota(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        return getFolderAccess(folderID).getFileQuota(folderID.getFolderId());
    }

    @Override
    public Quota[] getQuotas(String folder, Type[] types) throws OXException {
        FolderID folderID = new FolderID(folder);
        return getFolderAccess(folderID).getQuotas(folderID.getFolderId(), types);
    }

    @Override
    public FileStorageFolder getPersonalFolder(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolder folder = getFolderAccess(folderID).getPersonalFolder();
        if (null == folder) {
            throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
        }
        return withUniqueID(folder, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public FileStorageFolder getTrashFolder(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolder folder = getFolderAccess(folderID).getTrashFolder();
        if (null == folder) {
            throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
        }
        return withUniqueID(folder, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public FileStorageFolder[] getPublicFolders(String folderId) throws OXException {
        FolderID folderID = new FolderID(folderId);
        FileStorageFolder[] folders = getFolderAccess(folderID).getPublicFolders();
        if (null == folders) {
            throw FileStorageExceptionCodes.NO_SUCH_FOLDER.create();
        }
        return withUniqueID(folders, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public FileStorageFolder[] getRootFolders(Locale locale) throws OXException {
        /*
         * get root folder for all accounts from all filestorage services
         */
        List<FileStorageFolder> rootFolders = new ArrayList<>();
        for (FileStorageService service : getFileStorageServiceRegistry().getAllServices()) {
            List<FileStorageAccount> accounts = null;
            if (AccountAware.class.isInstance(service)) {
                accounts = ((AccountAware) service).getAccounts(session);
            }
            if (null == accounts) {
                accounts = service.getAccountManager().getAccounts(session);
            }
            for (FileStorageAccount account : accounts) {
                List<FileStoragePermission> rootFolderPermissions = null;
                if (service instanceof RootFolderPermissionsAware) {
                    RootFolderPermissionsAware rootFolderPermissionsAware = (RootFolderPermissionsAware) service;
                    rootFolderPermissions = rootFolderPermissionsAware.getRootFolderPermissions(account.getId(), session);
                }
                rootFolders.add(getRootFolder(session.getUserId(), service.getId(), account.getId(), account.getDisplayName(), rootFolderPermissions));
            }
        }
        /*
         * sort them by display/account name & return
         */
        if (1 < rootFolders.size()) {
            Collections.sort(rootFolders, new FolderComparator(locale));
        }
        return rootFolders.toArray(new FileStorageFolder[rootFolders.size()]);
    }

    @Override
    public FileStorageFolder[] getUserSharedFolders() throws OXException {
        /*
         * get shared folders of all accounts from all filestorage services
         */
        List<FileStorageFolder> sharedFolders = new ArrayList<>();
        for (FileStorageService service : getFileStorageServiceRegistry().getAllServices()) {
            List<FileStorageAccount> accounts = null;
            if (AccountAware.class.isInstance(service)) {
                accounts = ((AccountAware) service).getAccounts(session);
            }
            if (null == accounts) {
                accounts = service.getAccountManager().getAccounts(session);
            }
            for (FileStorageAccount account : accounts) {
                FileStorageFolderAccess folderAccess = getFolderAccess(service.getId(), account.getId());
                FileStorageFolder[] folders = folderAccess.getUserSharedFolders();
                if (null != folders && 0 < folders.length) {
                    for (FileStorageFolder folder : folders) {
                        sharedFolders.add(withUniqueID(folder, service.getId(), account.getId()));
                    }
                }
            }
        }
        /*
         * convert to array & return
         */
        return sharedFolders.toArray(new FileStorageFolder[sharedFolders.size()]);
    }

    @Override
    public long getTotalSize(String folder) throws OXException {
        /*
         * get directly if supported
         */
        FolderID folderID = new FolderID(folder);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        if (FolderStatsAware.class.isInstance(folderAccess)) {
            return ((FolderStatsAware) folderAccess).getTotalSize(folderID.getFolderId());
        }
        /*
         * count manually as fallback
         */
        long totalSize = 0;
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = getFileAccess(folderID).getDocuments(folderID.getFolderId(), Arrays.asList(Field.FILE_SIZE)).results();
            while (searchIterator.hasNext()) {
                File file = searchIterator.next();
                if (null != file) {
                    totalSize += file.getFileSize();
                }
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return totalSize;
    }

    @Override
    public long getNumFiles(String folder) throws OXException {
        /*
         * get directly if supported
         */
        FolderID folderID = new FolderID(folder);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        if (FolderStatsAware.class.isInstance(folderAccess)) {
            return ((FolderStatsAware) folderAccess).getNumFiles(folderID.getFolderId());
        }
        /*
         * count manually as fallback
         */
        long numFiles = 0;
        SearchIterator<File> searchIterator = null;
        try {
            searchIterator = getFileAccess(folderID).getDocuments(folderID.getFolderId(), Arrays.asList(Field.ID)).results();
            while (searchIterator.hasNext()) {
                numFiles++;
            }
        } finally {
            SearchIterators.close(searchIterator);
        }
        return numFiles;
    }

    @Override
    public Map<String, FileStorageFolder[]> restoreFolderFromTrash(List<String> folderIds, String defaultDestFolderId) throws OXException {
        FileStorageFolderAccess folderAccess = getFolderAccess(new FolderID(defaultDestFolderId));

        if (null == folderIds || folderIds.isEmpty()) {
            return Collections.emptyMap();
        }

        if (folderAccess instanceof FileStorageRestoringFolderAccess) {
            Map<String,FolderID[]> sourcePathFolders = getFolderPaths(folderIds, folderAccess);
            Map<String, FileStorageFolder[]> restoreFolderFromTrash = ((FileStorageRestoringFolderAccess) folderAccess).restoreFolderFromTrash(folderIds, defaultDestFolderId);
            Map<String,FolderID[]> targetPathFolders = getFolderPaths(folderIds, folderAccess);

            fireEvents(sourcePathFolders, FileStorageEventConstants.DELETE_FOLDER_TOPIC);
            fireEvents(targetPathFolders, FileStorageEventConstants.CREATE_FOLDER_TOPIC);

            return restoreFolderFromTrash;
        }
        return Collections.emptyMap();
    }

    private FolderID[] getPathIds(String folderId, String accountId, String serviceId, FileStorageFolderAccess folderAccess) throws OXException {
        if (folderAccess instanceof PathKnowingFileStorageFolderAccess) {
            PathKnowingFileStorageFolderAccess pathKnowing = (PathKnowingFileStorageFolderAccess) folderAccess;
            String[] pathIds = pathKnowing.getPathIds2DefaultFolder(folderId);
            FolderID[] path = new FolderID[pathIds.length];
            for (int i = 0; i < pathIds.length; i++) {
                path[i] = new FolderID(serviceId, accountId, pathIds[i]);
            }
            return path;
        }

        FileStorageFolder[] pathFolders = folderAccess.getPath2DefaultFolder(folderId);
        return getPath(pathFolders, serviceId, accountId);
    }

    @Override
    public FileStorageFolder[] searchFolderByName(String query, String folderId, long date, boolean includeSubfolders, boolean all, int start, int end) throws OXException {
        FileStorageFolderAccess folderAccess = getFolderAccess(new FolderID(folderId));
        if (SearchableFolderNameFolderAccess.class.isInstance(folderAccess)) {
            return ((SearchableFolderNameFolderAccess) folderAccess).searchFolderByName(query, folderId, date, includeSubfolders, all, start, end);
        }
        return new FileStorageFolder[0];
    }

    private Map<String,FolderID[]> getFolderPaths(List<String> folderIds, FileStorageFolderAccess folderAccess) throws OXException {
        Map<String,FolderID[]> folderPaths = new HashMap<>();
        for (String folder : folderIds) {
            FolderID folderID = new FolderID(folder);
            folderPaths.put(folder, getPathIds(folderID.getFolderId(), folderID.getAccountId(), folderID.getService(), folderAccess));
        }
        return folderPaths;
    }

    private void fireEvents(Map<String,FolderID[]> pathFolders, String fileStorageEventConstant) {
        for (Map.Entry<String, FolderID[]> entry : pathFolders.entrySet()) {
            FolderID sourceFolderID = new FolderID(entry.getKey());
            fire(new Event(fileStorageEventConstant, getEventProperties(session, sourceFolderID, entry.getValue())));
        }
    }

    private void fire(final Event event) {
        EventAdmin eventAdmin = getEventAdmin();
        if (null != eventAdmin) {
            LOG.debug("Publishing: {}", new Object() { @Override public String toString() { return dump(event);} });
            eventAdmin.postEvent(event);
        } else {
            LOG.warn("Unable to access event admin, unable to publish event {}", dump(event));
        }
    }

    /**
     * Creates a file storage root folder for a specific file storage account.
     *
     * @param serviceID The account's service identifier
     * @param accountID The account identifier
     * @return The root folder, already with an unique identifier and the parent set to {@link #INFOSTORE_FOLDER_ID}
     */
    private FileStorageFolder getRootFolder(String serviceID, String accountID) throws OXException {
        FileStorageService service = getFileStorageServiceRegistry().getFileStorageService(serviceID);

        List<FileStoragePermission> rootFolderPermissions = null;
        if (service instanceof RootFolderPermissionsAware) {
            RootFolderPermissionsAware rootFolderPermissionsAware = (RootFolderPermissionsAware) service;
            rootFolderPermissions = rootFolderPermissionsAware.getRootFolderPermissions(accountID, session);
        }

        FileStorageAccount account = service.getAccountManager().getAccount(accountID, session);
        return getRootFolder(session.getUserId(), serviceID, accountID, account.getDisplayName(), rootFolderPermissions);
    }

    /**
     * Create a new {@link FileStorageFolder} instance delegating all regular calls to the supplied folder, but returning the unique ID
     * representations of the folder's own object and the parent folder ID properties based on the underlying service- and account IDs.
     *
     * @param delegate The folder delegate
     * @param serviceId The service ID
     * @param accountId The account ID
     * @return A folder with unique IDs
     */
    protected FileStorageFolder withUniqueID(FileStorageFolder delegate, String serviceId, String accountId) {
        String id = null != delegate.getId() ? getUniqueFolderId(delegate.getId(), serviceId, accountId) : null;
        String parentId = null != delegate.getParentId() ? getUniqueFolderId(delegate.getParentId(), serviceId, accountId) : null;
        return new IDManglingFolder(delegate, id, parentId);
    }

    /**
     * Creates {@link FileStorageFolder} instances delegating all regular calls to the supplied folders, but returning the unique ID
     * representations of the folder's own object and the parent folder ID properties based on the underlying service- and account IDs.
     *
     * @param delegates The folder delegates
     * @param serviceId The service ID
     * @param accountId The account ID
     * @return An array of folders with unique IDs
     */
    protected FileStorageFolder[] withUniqueID(FileStorageFolder[] delegates, String serviceId, String accountId) {
        if (null == delegates) {
            return null;
        }
        FileStorageFolder[] idManglingFolders = new IDManglingFolder[delegates.length];
        for (int i = 0; i < idManglingFolders.length; i++) {
            idManglingFolders[i] = withUniqueID(delegates[i], serviceId, accountId);
        }
        return idManglingFolders;
    }

    /**
     * Creates a file storage root folder for a specific file storage account.
     *
     * @param userID The user identifier to construct the root folder for
     * @param serviceID The account's service identifier
     * @param accountID The account identifier
     * @param displayName The folder name to use, usually the account's display name
     * @param rootFolderPermissions The optional root folder permissions
     * @return The root folder, already with an unique identifier and the parent set to {@link #INFOSTORE_FOLDER_ID}
     */
    private static FileStorageFolder getRootFolder(int userID, String serviceID, String accountID, String displayName, List<FileStoragePermission> rootFolderPermissions) {
        DefaultTypeAwareFileStorageFolder rootFolder = new DefaultTypeAwareFileStorageFolder();
        rootFolder.setParentId(INFOSTORE_FOLDER_ID);
        rootFolder.setId(new FolderID(serviceID, accountID, FileStorageFolder.ROOT_FULLNAME).toUniqueID());
        rootFolder.setName(displayName);
        rootFolder.setType(FileStorageFolderType.NONE);
        rootFolder.setSubscribed(true);
        rootFolder.setSubfolders(true);
        rootFolder.setSubscribedSubfolders(true);
        rootFolder.setRootFolder(true);
        rootFolder.setHoldsFiles(true);
        rootFolder.setHoldsFolders(true);
        rootFolder.setExists(true);

        if (null == rootFolderPermissions || rootFolderPermissions.isEmpty()) {
            DefaultFileStoragePermission permission = DefaultFileStoragePermission.newInstance();
            permission.setAdmin(true);
            permission.setFolderPermission(FileStoragePermission.CREATE_SUB_FOLDERS);
            permission.setEntity(userID);
            rootFolder.setPermissions(Collections.<FileStoragePermission>singletonList(permission));
            rootFolder.setOwnPermission(permission);
        } else {
            rootFolder.setPermissions(rootFolderPermissions);

            FileStoragePermission ownPermission = rootFolderPermissions.get(0);
            if (ownPermission.getEntity() != userID) {
                ownPermission = null;
                for (Iterator<FileStoragePermission> it = rootFolderPermissions.iterator(); null == ownPermission && it.hasNext(); ) {
                    FileStoragePermission permission = it.next();
                    if (permission.getEntity() == userID) {
                        ownPermission = permission;
                    }
                }
            }

            if (null != ownPermission) {
                rootFolder.setOwnPermission(ownPermission);
            }
        }

        rootFolder.setCreatedBy(userID);
        rootFolder.setModifiedBy(userID);
        return rootFolder;
    }

    static String dump(Event event) {
        if (null != event) {
            return new StringBuilder().append(event.getTopic())
                .append(": folderId=").append(event.getProperty(FileStorageEventConstants.FOLDER_ID))
                .append(": folderPath=").append(event.getProperty(FileStorageEventConstants.FOLDER_PATH))
                .append(", service=").append(event.getProperty(FileStorageEventConstants.SERVICE))
                .append(", accountId=").append(event.getProperty(FileStorageEventConstants.ACCOUNT_ID))
                .append(", session=").append(event.getProperty(FileStorageEventConstants.SESSION))
                .toString();
        }
        return null;
    }

    /**
     * Gets a value indicating whether the folders identified by the given identifiers are located in the same folder storage or not.
     *
     * @param folderID1 The first folder ID to check
     * @param folderID2 The first folder ID to check
     * @return <code>true</code> if both folders are located within the same folder storage, <code>false</code>, otherwise
     */
    private static boolean isSameAccount(FolderID folderID1, FolderID folderID2) {
        return folderID1.getService().equals(folderID2.getService()) && folderID1.getAccountId().equals(folderID2.getAccountId());
    }

    private static final class FolderComparator implements Comparator<FileStorageFolder> {

        private final Collator collator;

        /**
         * Initializes a new {@link FolderComparator}.
         *
         * @param locale The locale to use, or <code>null</code> to fall back to the default locale
         */
        public FolderComparator(Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(null == locale ? Locale.US : locale);
        }

        @Override
        public int compare(FileStorageFolder folder1, FileStorageFolder folder2) {
            return collator.compare(folder1.getName(), folder2.getName());
        }

    }

}
