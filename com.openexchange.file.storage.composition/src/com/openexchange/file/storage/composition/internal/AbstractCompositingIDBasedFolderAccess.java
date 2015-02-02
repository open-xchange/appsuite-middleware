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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

import static com.openexchange.file.storage.composition.internal.IDManglingFolder.withRelativeID;
import static com.openexchange.file.storage.composition.internal.IDManglingFolder.withUniqueID;
import static com.openexchange.file.storage.composition.internal.FileStorageTools.getEventProperties;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.List;
import java.util.Locale;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.AccountAware;
import com.openexchange.file.storage.FileStorageAccount;
import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageFolder;
import com.openexchange.file.storage.FileStorageFolderAccess;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.Quota;
import com.openexchange.file.storage.Quota.Type;
import com.openexchange.file.storage.composition.FolderID;
import com.openexchange.file.storage.composition.IDBasedFolderAccess;
import com.openexchange.java.Collators;
import com.openexchange.session.Session;

/**
 * {@link AbstractCompositingIDBasedFolderAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractCompositingIDBasedFolderAccess extends AbstractCompositingIDBasedAccess implements IDBasedFolderAccess {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCompositingIDBasedFolderAccess.class);

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
        } catch (final OXException e) {
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
        FileStorageFolder folder = getFolderAccess(folderID).getFolder(folderID.getFolderId());
        return withUniqueID(folder, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public FileStorageFolder[] getSubfolders(String parentIdentifier, boolean all) throws OXException {
        FolderID folderID = new FolderID(parentIdentifier);
        FileStorageFolder[] folders = getFolderAccess(folderID).getSubfolders(folderID.getFolderId(), all);
        return withUniqueID(folders, folderID.getService(), folderID.getAccountId());
    }

    @Override
    public String createFolder(FileStorageFolder toCreate) throws OXException {
        FolderID parentFolderID = new FolderID(toCreate.getParentId());
        FileStorageFolderAccess folderAccess = getFolderAccess(parentFolderID);
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
         * take over any warnings
         */
        List<OXException> warnings = transferResult.getWarnings(true);
        if (null != warnings && 0 < warnings.size()) {
            for (OXException warning : warnings) {
                this.addWarning(warning);
            }
        }
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
    public FileStorageFolder[] getRootFolders(final Locale locale) throws OXException {
        // Sort according to account name
        List<AccessWrapper> accessWrappers = getAllAccountAccesses();
        Collections.sort(accessWrappers, new AccessWrapperComparator(locale == null ? Locale.US : locale));

        // Get root folders
        List<FileStorageFolder> folders = new ArrayList<FileStorageFolder>(accessWrappers.size());
        for (AccessWrapper accessWrapper : accessWrappers) {
            FileStorageAccountAccess accountAccess = accessWrapper.accountAccess;
            FileStorageFolderAccess folderAccess = accountAccess.getFolderAccess();
            try {
                FileStorageFolder rootFolder = folderAccess.getRootFolder();
                if (null != rootFolder) {
                    folders.add(IDManglingFolder.withUniqueID(rootFolder, accountAccess.getService().getId(), accountAccess.getAccountId()));
                }
            } catch (OXException e) {
                // Check for com.openexchange.folderstorage.FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE -- 'FLD-0003'
                if (false == e.equalsCode(3, "FLD")) {
                    LOG.warn("Could not load root folder for account {}", accessWrapper.displayName, e);
                }
            }
        }

        return folders.toArray(new FileStorageFolder[folders.size()]);
    }

    protected List<AccessWrapper> getAllAccountAccesses() throws OXException {
        List<FileStorageService> allFileStorageServices = getFileStorageServiceRegistry().getAllServices();
        List<AccessWrapper> accountAccesses = new ArrayList<AccessWrapper>(allFileStorageServices.size());
        for (FileStorageService fsService : allFileStorageServices) {
            List<FileStorageAccount> accounts = null;
            if (fsService instanceof AccountAware) {
                accounts = ((AccountAware)fsService).getAccounts(session);
            }
            if (null == accounts) {
                accounts = fsService.getAccountManager().getAccounts(session);
            }
            for (FileStorageAccount fileStorageAccount : accounts) {
                FileStorageAccountAccess accountAccess = getAccountAccess(fsService.getId(), fileStorageAccount.getId());
                accountAccesses.add(new AccessWrapper(accountAccess, fileStorageAccount.getDisplayName()));
            }
        }
        return accountAccesses;
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

    private static final class AccessWrapper {

        final FileStorageAccountAccess accountAccess;
        final String displayName;

        AccessWrapper(FileStorageAccountAccess accountAccess, String displayName) {
            super();
            this.accountAccess = accountAccess;
            this.displayName = displayName;
        }

    }

    private static final class AccessWrapperComparator implements Comparator<AccessWrapper> {

        private final Collator collator;

        AccessWrapperComparator(final Locale locale) {
            super();
            collator = Collators.getSecondaryInstance(locale);
        }

        @Override
        public int compare(final AccessWrapper o1, final AccessWrapper o2) {
            return collator.compare(o1.displayName, o2.displayName);
        }

    } // End of FileStorageAccountComparator

}
