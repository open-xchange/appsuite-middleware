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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.folderstorage.outlook;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TreeMap;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderException;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderType;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.StorageParameters;
import com.openexchange.folderstorage.StoragePriority;
import com.openexchange.folderstorage.StorageType;
import com.openexchange.folderstorage.outlook.sql.Delete;
import com.openexchange.folderstorage.outlook.sql.Insert;
import com.openexchange.folderstorage.outlook.sql.Select;
import com.openexchange.folderstorage.outlook.sql.Update;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;

/**
 * {@link OutlookFolderStorage} - The MS Outlook folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OutlookFolderStorage implements FolderStorage {

    /**
     * The reserved tree identifier for MS Outlook folder tree: <code>"1"</code>.
     */
    public static final String OUTLOOK_TREE_ID = "1";

    private final String realTreeId;

    private final FolderType folderType;

    /**
     * Initializes a new {@link OutlookFolderStorage}.
     */
    public OutlookFolderStorage() {
        super();
        realTreeId = FolderStorage.REAL_TREE_ID;
        folderType = new OutlookFolderType();
    }

    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Delegate clear invocation to real storage
         */
        final FolderStorage folderStorage = OutlookFolderStorageRegistry.getInstance().getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        folderStorage.startTransaction(storageParameters, true);
        try {
            folderStorage.clearFolder(realTreeId, folderId, storageParameters);
            folderStorage.commitTransaction(storageParameters);
        } catch (final FolderException e) {
            folderStorage.rollback(storageParameters);
            throw e;
        } catch (final Exception e) {
            folderStorage.rollback(storageParameters);
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public void commitTransaction(final StorageParameters params) throws FolderException {
        // Nothing to do
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        return Select.containsFolder(
            storageParameters.getContext().getContextId(),
            Integer.parseInt(treeId),
            storageParameters.getUser().getId(),
            folderId,
            storageType);
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        /*
         * Create only if folder could not be stored in real storage
         */
        final String folderId = folder.getID();
        final String realParentId;
        {
            final FolderStorage folderStorage = OutlookFolderStorageRegistry.getInstance().getFolderStorage(realTreeId, folderId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
            }
            folderStorage.startTransaction(storageParameters, true);
            try {
                realParentId = folderStorage.getFolder(realTreeId, folderId, StorageType.WORKING, storageParameters).getParentID();
                folderStorage.commitTransaction(storageParameters);
            } catch (final FolderException e) {
                folderStorage.rollback(storageParameters);
                throw e;
            } catch (final Exception e) {
                folderStorage.rollback(storageParameters);
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        final String parentId = folder.getParentID();
        if (realParentId.equals(parentId)) {
            /*
             * Folder already properly created at right location in real storage
             */
            return;
        }
        Insert.insertFolder(
            storageParameters.getContext().getContextId(),
            Integer.parseInt(folder.getTreeID()),
            storageParameters.getUser().getId(),
            folder);
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Delete from tables if present
         */
        Delete.deleteFolder(
            storageParameters.getContext().getContextId(),
            Integer.parseInt(treeId),
            storageParameters.getUser().getId(),
            folderId,
            true);
    }

    public ContentType getDefaultContentType() {
        return null;
    }

    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        // Get default folder
        final FolderStorage byContentType = OutlookFolderStorageRegistry.getInstance().getFolderStorageByContentType(realTreeId, contentType);
        if (null == byContentType) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(treeId, contentType);
        }
        byContentType.startTransaction(storageParameters, false);
        try {
            final String defaultFolderID = byContentType.getDefaultFolderID(user, treeId, contentType, storageParameters);
            byContentType.commitTransaction(storageParameters);
            return defaultFolderID;
        } catch (final FolderException e) {
            byContentType.rollback(storageParameters);
            throw e;
        } catch (final Exception e) {
            byContentType.rollback(storageParameters);
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) throws FolderException {
        return new String[0];
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        /*
         * Check for root folder
         */
        if (FolderStorage.ROOT_ID.equals(folderId)) {
            final Folder rootFolder;
            final SortableId[] ids;
            {
                // Get real folder storage
                final FolderStorage folderStorage = OutlookFolderStorageRegistry.getInstance().getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get folder
                    rootFolder = folderStorage.getFolder(FolderStorage.REAL_TREE_ID, folderId, storageParameters);
                    ids = folderStorage.getSubfolders(FolderStorage.REAL_TREE_ID, folderId, storageParameters);
                    folderStorage.commitTransaction(storageParameters);
                } catch (final FolderException e) {
                    folderStorage.rollback(storageParameters);
                    throw e;
                } catch (final Exception e) {
                    folderStorage.rollback(storageParameters);
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final List<String> subfolderIDs;
            final MailAccountStorageService mass = OutlookServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class);
            if (null != mass) {
                final List<MailAccount> accounts;
                try {
                    final MailAccount[] mailAccounts =
                        mass.getUserMailAccounts(storageParameters.getUser().getId(), storageParameters.getContext().getContextId());
                    accounts = new ArrayList<MailAccount>(mailAccounts.length);
                    accounts.addAll(Arrays.asList(mailAccounts));
                    Collections.sort(accounts, new MailAccountComparator(storageParameters.getUser().getLocale()));
                } catch (final MailAccountException e) {
                    throw new FolderException(e);
                }
                if (accounts.isEmpty()) {
                    subfolderIDs = toIDList(ids);
                } else {
                    subfolderIDs = new ArrayList<String>(ids.length + accounts.size());
                    subfolderIDs.addAll(toIDList(ids));
                    for (final MailAccount mailAccount : accounts) {
                        if (!mailAccount.isDefaultAccount()) {
                            subfolderIDs.add(MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID));
                        }
                    }
                    // TODO: No Unified INBOX if not enabled
                }
            } else {
                subfolderIDs = toIDList(ids);
            }
            final OutlookFolder outlookRootFolder = new OutlookFolder(rootFolder);
            outlookRootFolder.setTreeID(treeId);
            outlookRootFolder.setSubfolderIDs(subfolderIDs.toArray(new String[subfolderIDs.size()]));
            return outlookRootFolder;
        }
        /*
         * Check for private folder
         */
        if (FolderStorage.PRIVATE_ID.equals(folderId)) {
            final Folder privateFolder;
            final SortableId[] ids;
            {
                // Get real folder storage
                final FolderStorage folderStorage = OutlookFolderStorageRegistry.getInstance().getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get folder
                    privateFolder = folderStorage.getFolder(realTreeId, folderId, storageParameters);
                    ids = folderStorage.getSubfolders(realTreeId, folderId, storageParameters);
                    folderStorage.commitTransaction(storageParameters);
                } catch (final FolderException e) {
                    folderStorage.rollback(storageParameters);
                    throw e;
                } catch (final Exception e) {
                    folderStorage.rollback(storageParameters);
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final SortableId[] mailIDs;
            {
                // Get real folder storage for primary mail folder
                final String fullname = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.DEFAULT_FOLDER_ID);
                final FolderStorage folderStorage = OutlookFolderStorageRegistry.getInstance().getFolderStorage(realTreeId, fullname);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, fullname);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get IDs
                    mailIDs = folderStorage.getSubfolders(realTreeId, fullname, storageParameters);
                    folderStorage.commitTransaction(storageParameters);
                } catch (final FolderException e) {
                    folderStorage.rollback(storageParameters);
                    throw e;
                } catch (final Exception e) {
                    folderStorage.rollback(storageParameters);
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final List<String> subfolderIDs = new ArrayList<String>(mailIDs.length + ids.length);
            subfolderIDs.addAll(toIDList(ids));
            subfolderIDs.addAll(toIDList(mailIDs));
            Collections.sort(subfolderIDs, new Select.FolderNameComparator(storageParameters.getUser().getLocale()));
            final OutlookFolder outlookPrivateFolder = new OutlookFolder(privateFolder);
            outlookPrivateFolder.setTreeID(treeId);
            outlookPrivateFolder.setSubfolderIDs(subfolderIDs.toArray(new String[subfolderIDs.size()]));
            return outlookPrivateFolder;
        }
        /*
         * Other folder than root or private
         */
        final OutlookFolder outlookFolder;
        final List<String[]> l;
        {
            final Folder realFolder;
            {
                // Get real folder storage
                final FolderStorage folderStorage = OutlookFolderStorageRegistry.getInstance().getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get folder
                    realFolder = folderStorage.getFolder(realTreeId, folderId, storageParameters);
                    // Get real subfolders
                    final String[] realSubfolderIds = getSubfolderIDs(realFolder, folderStorage, storageParameters);
                    l = new ArrayList<String[]>(realSubfolderIds.length);
                    for (final String realSubfolderId : realSubfolderIds) {
                        l.add(new String[] {
                            realSubfolderId, folderStorage.getFolder(realTreeId, realSubfolderId, storageParameters).getName() });
                    }
                    folderStorage.commitTransaction(storageParameters);
                } catch (final FolderException e) {
                    folderStorage.rollback(storageParameters);
                    throw e;
                } catch (final Exception e) {
                    folderStorage.rollback(storageParameters);
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            outlookFolder = new OutlookFolder(realFolder);
            outlookFolder.setTreeID(treeId);
        }
        // Load folder data from database
        final User user = storageParameters.getUser();
        final Locale locale = user.getLocale();
        if (Select.fillFolder(
            storageParameters.getContext().getContextId(),
            Integer.parseInt(treeId),
            user.getId(),
            locale,
            outlookFolder,
            l,
            storageType)) {
            return outlookFolder;
        }
        /*
         * Nothing to fill, not contained in database table. Just apply real subfolder identifiers and return.
         */
        final TreeMap<String, String> treeMap = new TreeMap<String, String>(new Select.FolderNameComparator(locale));
        final StringHelper stringHelper = new StringHelper(locale);
        for (final String[] realSubfolderId : l) {
            treeMap.put(stringHelper.getString(realSubfolderId[1]), realSubfolderId[0]);
        }
        outlookFolder.setSubfolderIDs(treeMap.values().toArray(new String[treeMap.size()]));
        return outlookFolder;
    }

    public FolderType getFolderType() {
        return folderType;
    }

    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws FolderException {
        return new String[0];
    }

    public StoragePriority getStoragePriority() {
        return StoragePriority.NORMAL;
    }

    public SortableId[] getSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters) throws FolderException {
        final Folder outlookFolder = getFolder(treeId, parentId, storageParameters);
        final String[] ids = outlookFolder.getSubfolderIDs();
        final SortableId[] ret = new SortableId[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ret[i] = new OutlookId(ids[i], i);
        }
        return ret;
    }

    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

    public void rollback(final StorageParameters params) {
        // Nothing to do
    }

    public StorageParameters startTransaction(final StorageParameters parameters, final boolean modify) throws FolderException {
        return parameters;
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        /*
         * Update only if folder is contained
         */
        if (containsFolder(folder.getTreeID(), folder.getID(), storageParameters)) {
            Update.updateFolder(
                storageParameters.getContext().getContextId(),
                Integer.parseInt(folder.getTreeID()),
                storageParameters.getUser().getId(),
                folder);
        }
    }

    private static final class MailAccountComparator implements Comparator<MailAccount> {

        private final Collator collator;

        public MailAccountComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final MailAccount o1, final MailAccount o2) {
            if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o1.getMailProtocol())) {
                if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                    return 0;
                }
                return -1;
            } else if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(o2.getMailProtocol())) {
                return 1;
            }
            if (o1.isDefaultAccount()) {
                if (o2.isDefaultAccount()) {
                    return 0;
                }
                return -1;
            } else if (o2.isDefaultAccount()) {
                return 1;
            }
            return collator.compare(o1.getName(), o2.getName());
        }

    } // End of MailAccountComparator

    private static List<String> toIDList(final SortableId[] ids) {
        final List<String> l = new ArrayList<String>(ids.length);
        for (final SortableId id : ids) {
            l.add(id.getId());
        }
        return l;
    }

    private static String[] getSubfolderIDs(final Folder realFolder, final FolderStorage folderStorage, final StorageParameters storageParameters) throws FolderException {
        final String[] ids = realFolder.getSubfolderIDs();
        if (null == ids) {
            final List<String> idList =
                toIDList(folderStorage.getSubfolders(realFolder.getTreeID(), realFolder.getID(), storageParameters));
            return idList.toArray(new String[idList.size()]);
        }
        return ids;
    }

}
