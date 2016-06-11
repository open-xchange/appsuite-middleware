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

import static com.openexchange.file.storage.composition.internal.FileStorageTools.*;
import static com.openexchange.file.storage.composition.internal.idmangling.IDManglingFolder.*;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.DefaultFileStoragePermission;
import com.openexchange.file.storage.DefaultTypeAwareFileStorageFolder;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.File.Field;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageFolderType;
import com.openexchange.file.storage.FileStoragePermission;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.FolderStatsAware;
import com.openexchange.file.storage.PermissionAware;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.RootFolderPermissionsAware;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.java.Collators;
import com.openexchange.java.Strings;
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
            throw FileStorageExceptionCodes.NO_PERMISSION_SUPPORT.create(FileStorageTools.getAccountName(this, parentFolderID), parentFolderID, session.getContextId());
        }
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(parentFolderID.getFolderId());
        String newID = folderAccess.createFolder(withRelativeID(toCreate));
        FolderID newFolderID = new FolderID(parentFolderID.getService(), parentFolderID.getAccountId(), newID);
        fire(new Event(FileStorageEventConstants.CREATE_FOLDER_TOPIC, getEventProperties(session, newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String updateFolder(String identifier, FileStorageFolder toUpdate) throws OXException {
        FolderID folderID = new FolderID(identifier);
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        if (containsForeignPermissions(session.getUserId(), toUpdate) && false == PermissionAware.class.isInstance(folderAccess)) {
            throw FileStorageExceptionCodes.NO_PERMISSION_SUPPORT.create(FileStorageTools.getAccountName(this, folderID), folderID, session.getContextId());
        }
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(folderID.getFolderId());
        String newID = folderAccess.updateFolder(folderID.getFolderId(), withRelativeID(toUpdate));
        FolderID newFolderID = new FolderID(folderID.getService(), folderID.getAccountId(), newID);
        fire(new Event(FileStorageEventConstants.UPDATE_FOLDER_TOPIC, getEventProperties(session, newFolderID, path)));
        return newFolderID.toUniqueID();
    }

    @Override
    public String moveFolder(String folderId, String newParentId) throws OXException {
        return moveFolder(folderId, newParentId, null);
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
            FileStorageFolder[] sourcePath = folderAccess.getPath2DefaultFolder(sourceFolderID.getFolderId());
            String newID = folderAccess.moveFolder(sourceFolderID.getFolderId(), targetParentFolderID.getFolderId(), newName);
            FolderID newFolderID = new FolderID(sourceFolderID.getService(), sourceFolderID.getAccountId(), newID);
            FileStorageFolder[] newPath = folderAccess.getPath2DefaultFolder(newID);
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
            getFolderAccess(sourceFolderID).deleteFolder(sourceFolderID.getFolderId());
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
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(folderID.getFolderId());
        String newID = folderAccess.renameFolder(folderID.getFolderId(), newName);
        FolderID newFolderID =new FolderID(folderID.getService(), folderID.getAccountId(), newID);
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
        if (FileStorageFolder.ROOT_FULLNAME.equals(folderID.getFolderId())) {
            throw FileStorageExceptionCodes.DELETE_DENIED.create(folderID.getService(), folderId);
        }
        FileStorageFolderAccess folderAccess = getFolderAccess(folderID);
        FileStorageFolder[] path = folderAccess.getPath2DefaultFolder(folderID.getFolderId());
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
        List<FileStorageFolder> rootFolders = new ArrayList<FileStorageFolder>();
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
        List<FileStorageFolder> sharedFolders = new ArrayList<FileStorageFolder>();
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
        return Long.valueOf(totalSize);
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
        return Long.valueOf(numFiles);
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
            permission.setAdmin(false);
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
