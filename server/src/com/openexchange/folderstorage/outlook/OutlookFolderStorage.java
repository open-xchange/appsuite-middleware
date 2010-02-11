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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
import com.openexchange.folderstorage.internal.Tools;
import com.openexchange.folderstorage.mail.contentType.DraftsContentType;
import com.openexchange.folderstorage.mail.contentType.SentContentType;
import com.openexchange.folderstorage.mail.contentType.SpamContentType;
import com.openexchange.folderstorage.mail.contentType.TrashContentType;
import com.openexchange.folderstorage.outlook.sql.Delete;
import com.openexchange.folderstorage.outlook.sql.Insert;
import com.openexchange.folderstorage.outlook.sql.Select;
import com.openexchange.folderstorage.outlook.sql.Update;
import com.openexchange.groupware.ldap.User;
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

    private static final String PREPARED_FULLNAME_INBOX = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, "INBOX");

    /**
     * The reserved tree identifier for MS Outlook folder tree: <code>"1"</code>.
     */
    public static final String OUTLOOK_TREE_ID = "1";

    /**
     * The name of Outlook root folder.
     */
    private static final String OUTLOOK_ROOT_NAME = "Hidden-Root";

    /**
     * The name of Outlook private folder.
     */
    private static final String OUTLOOK_PRIVATE_NAME = "IPM-Root";

    /**
     * The real tree identifier.
     */
    private final String realTreeId;

    /**
     * This storage's folder type.
     */
    private final FolderType folderType;

    /**
     * The folder storage registry.
     */
    private final OutlookFolderStorageRegistry folderStorageRegistry;

    /**
     * Initializes a new {@link OutlookFolderStorage}.
     */
    public OutlookFolderStorage() {
        super();
        realTreeId = FolderStorage.REAL_TREE_ID;
        folderType = new OutlookFolderType();
        folderStorageRegistry = OutlookFolderStorageRegistry.getInstance();
    }

    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Delegate clear invocation to real storage
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
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
        final FolderStorage dedicatedFolderStorage = folderStorageRegistry.getDedicatedFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (!dedicatedFolderStorage.containsFolder(FolderStorage.REAL_TREE_ID, folderId, storageType, storageParameters)) {
            return false;
        }
        
        // Exclude unsupported folders like infostore folders
        // final Folder folder = dedicatedFolderStorage.getFolder(FolderStorage.REAL_TREE_ID, folderId, storageType, storageParameters);
        // if (InfostoreContentType.getInstance().equals(folder.getContentType())) {
        //     return false;
        // }
        return true;
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        /*
         * Create only if folder could not be stored in real storage
         */
        final String folderId = folder.getID();
        final String realParentId;
        {
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
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
        final int contextId = storageParameters.getContextId();
        final int userId = storageParameters.getUserId();
        final int tree = Tools.getUnsignedInteger(folder.getTreeID());
        Insert.insertFolder(contextId, tree, userId, folder);
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Delete from tables if present
         */
        Delete.deleteFolder(
            storageParameters.getContextId(),
            Tools.getUnsignedInteger(treeId),
            storageParameters.getUserId(),
            folderId,
            true);
    }

    public ContentType getDefaultContentType() {
        return null;
    }

    public String getDefaultFolderID(final User user, final String treeId, final ContentType contentType, final StorageParameters storageParameters) throws FolderException {
        // Get default folder
        final FolderStorage byContentType = folderStorageRegistry.getFolderStorageByContentType(realTreeId, contentType);
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
            {
                // Get real folder storage
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get folder
                    rootFolder = folderStorage.getFolder(FolderStorage.REAL_TREE_ID, folderId, storageParameters);
                    folderStorage.commitTransaction(storageParameters);
                } catch (final FolderException e) {
                    folderStorage.rollback(storageParameters);
                    throw e;
                } catch (final Exception e) {
                    folderStorage.rollback(storageParameters);
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final OutlookFolder outlookRootFolder = new OutlookFolder(rootFolder);
            outlookRootFolder.setName(OUTLOOK_ROOT_NAME);
            outlookRootFolder.setTreeID(treeId);
            /*
             * Set subfolder IDs to null to force getSubfolders() invocation
             */
            outlookRootFolder.setSubfolderIDs(null);
            return outlookRootFolder;
        }
        /*
         * Check for private folder
         */
        if (FolderStorage.PRIVATE_ID.equals(folderId)) {
            final Folder privateFolder;
            {
                // Get real folder storage
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get folder
                    privateFolder = folderStorage.getFolder(realTreeId, folderId, storageParameters);
                    folderStorage.commitTransaction(storageParameters);
                } catch (final FolderException e) {
                    folderStorage.rollback(storageParameters);
                    throw e;
                } catch (final Exception e) {
                    folderStorage.rollback(storageParameters);
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
            final OutlookFolder outlookPrivateFolder = new OutlookFolder(privateFolder);
            outlookPrivateFolder.setName(OUTLOOK_PRIVATE_NAME);
            outlookPrivateFolder.setTreeID(treeId);
            outlookPrivateFolder.setSubfolderIDs(null);
            return outlookPrivateFolder;
        }
        /*
         * Other folder than root or private
         */
        final User user = storageParameters.getUser();
        final int tree = Tools.getUnsignedInteger(treeId);
        final int contextId = storageParameters.getContextId();

        final OutlookFolder outlookFolder;
        {
            final Folder realFolder;
            {
                /*
                 * Get real folder storage
                 */
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    /*
                     * Get folder
                     */
                    realFolder = folderStorage.getFolder(realTreeId, folderId, storageParameters);
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
            /*
             * Set subfolders if empty
             */
            final String[] realSubfolderIDs = realFolder.getSubfolderIDs();
            if (null != realSubfolderIDs && realSubfolderIDs.length == 0) {
                /*
                 * Folder indicates to hold no subfolders; verify against virtual tree
                 */
                if (!Select.containsParent(contextId, tree, user.getId(), folderId, StorageType.WORKING)) {
                    outlookFolder.setSubfolderIDs(realSubfolderIDs); // Zero-length array => No subfolders
                } else {
                    outlookFolder.setSubfolderIDs(null);
                }
            } else {
                outlookFolder.setSubfolderIDs(null);
            }
        }
        /*
         * Load folder data from database
         */
        Select.fillFolder(contextId, tree, user.getId(), user.getLocale(), outlookFolder, storageType);
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
        /*
         * Root folder
         */
        if (FolderStorage.ROOT_ID.equals(parentId)) {
            return getRootFolderSubfolders(parentId, storageParameters);
        }
        final User user = storageParameters.getUser();
        final Locale locale = user.getLocale();
        final int contextId = storageParameters.getContextId();
        final int tree = Tools.getUnsignedInteger(treeId);
        /*
         * INBOX folder
         */
        if (PREPARED_FULLNAME_INBOX.equals(parentId)) {
            return getINBOXSubfolders(treeId, parentId, storageParameters, user, locale, contextId, tree);
        }
        /*
         * Check for private folder
         */
        if (FolderStorage.PRIVATE_ID.equals(parentId)) {
            return getPrivateFolderSubfolders(parentId, tree, storageParameters, user, locale, contextId);
        }
        /*
         * Other folder than root or private
         */
        final List<String[]> l;
        {
            {
                // Get real folder storage
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
                }
                folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get real subfolders
                    final Folder parentFolder = folderStorage.getFolder(realTreeId, parentId, storageParameters);
                    final String[] realSubfolderIds = getSubfolderIDs(parentFolder, folderStorage, storageParameters);
                    l = new ArrayList<String[]>(realSubfolderIds.length);
                    if (parentFolder.isDefault()) {
                        /*
                         * Strip subfolders occurring at another location in folder tree
                         */
                        final boolean[] contained =
                            Select.containsFolders(contextId, tree, storageParameters.getUserId(), realSubfolderIds, StorageType.WORKING);
                        for (int k = 0; k < realSubfolderIds.length; k++) {
                            final String realSubfolderId = realSubfolderIds[k];
                            if (!contained[k]) {
                                l.add(new String[] {
                                    realSubfolderId, folderStorage.getFolder(realTreeId, realSubfolderId, storageParameters).getName() });
                            }
                        }
                    } else {
                        for (final String realSubfolderId : realSubfolderIds) {
                            l.add(new String[] {
                                realSubfolderId, folderStorage.getFolder(realTreeId, realSubfolderId, storageParameters).getName() });
                        }
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
        }
        // Load folder data from database
        final String[] ids = Select.getSubfolderIds(contextId, tree, user.getId(), locale, parentId, l, StorageType.WORKING);
        final SortableId[] ret = new SortableId[ids.length];
        for (int i = 0; i < ids.length; i++) {
            ret[i] = new OutlookId(ids[i], i);
        }
        return ret;
    }

    private SortableId[] getINBOXSubfolders(final String treeId, final String parentId, final StorageParameters storageParameters, final User user, final Locale locale, final int contextId, final int tree) throws FolderException {
        /*
         * Get real folder storage for primary mail folder
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
        }
        folderStorage.startTransaction(storageParameters, false);
        try {
            final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(new FolderNameComparator(locale));
            /*
             * Add default folders: Trash, Sent, Drafts, ...
             */
            final Set<String> defIds;
            {
                defIds = new HashSet<String>(6);
                defIds.add(folderStorage.getDefaultFolderID(user, treeId, TrashContentType.getInstance(), storageParameters));
                defIds.add(folderStorage.getDefaultFolderID(user, treeId, DraftsContentType.getInstance(), storageParameters));
                defIds.add(folderStorage.getDefaultFolderID(user, treeId, SentContentType.getInstance(), storageParameters));
                defIds.add(folderStorage.getDefaultFolderID(user, treeId, SpamContentType.getInstance(), storageParameters));
            }
            final SortableId[] inboxSubfolders = folderStorage.getSubfolders(realTreeId, PREPARED_FULLNAME_INBOX, storageParameters);
            final boolean[] contained = Select.containsFolders(contextId, tree, storageParameters.getUserId(), inboxSubfolders, StorageType.WORKING);
            for (int i = 0; i < inboxSubfolders.length; i++) {
                if (!contained[i]) {
                    final String id = inboxSubfolders[i].getId();
                    if (!defIds.contains(id)) {
                        final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                        List<String> list = treeMap.get(localizedName);
                        if (null == list) {
                            list = new ArrayList<String>(2);
                            treeMap.put(localizedName, list);
                        }
                        list.add(id);
                    }
                }
            }
            folderStorage.commitTransaction(storageParameters);
            /*
             * Compose list
             */
            final List<SortableId> sortedIDs;
            {
                final Collection<List<String>> values = treeMap.values();
                sortedIDs = new ArrayList<SortableId>(values.size());
                int i = 0;
                for (final List<String> list : values) {
                    for (final String id : list) {
                        sortedIDs.add(new OutlookId(id, i++));
                    }
                }
            }
            return sortedIDs.toArray(new SortableId[sortedIDs.size()]);
        } catch (final FolderException e) {
            folderStorage.rollback(storageParameters);
            throw e;
        } catch (final Exception e) {
            folderStorage.rollback(storageParameters);
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private SortableId[] getRootFolderSubfolders(final String parentId, final StorageParameters storageParameters) throws FolderException {
        final SortableId[] ids;
        {
            /*
             * Get real folder storage
             */
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
            }
            folderStorage.startTransaction(storageParameters, false);
            try {
                /*
                 * Get subfolders
                 */
                final SortableId[] subfolders = folderStorage.getSubfolders(FolderStorage.REAL_TREE_ID, parentId, storageParameters);
                /*
                 * Get only private folder
                 */
                ids = new SortableId[1];
                boolean b = true;
                for (int i = 0; b && i < subfolders.length; i++) {
                    final SortableId si = subfolders[i];
                    if (FolderStorage.PRIVATE_ID.equals(si.getId())) {
                        ids[0] = si;
                        b = true;
                    }
                }
                if (!b) {
                    // Missing private folder
                    throw FolderExceptionErrorMessage.NOT_FOUND.create(FolderStorage.PRIVATE_ID, OUTLOOK_TREE_ID);
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
        final List<String> subfolderIDs = toIDList(ids);
        final SortableId[] ret = new SortableId[subfolderIDs.size()];
        int i = 0;
        for (final String id : subfolderIDs) {
            ret[i] = new OutlookId(id, i);
            i++;
        }
        return ret;
    }

    private SortableId[] getPrivateFolderSubfolders(final String parentId, final int tree, final StorageParameters storageParameters, final User user, final Locale locale, final int contextId) throws FolderException {
        final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(new FolderNameComparator(locale));
        {
            /*
             * Get real folder storage
             */
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
            }
            folderStorage.startTransaction(storageParameters, false);
            try {
                /*
                 * Get folder
                 */
                final SortableId[] ids = folderStorage.getSubfolders(realTreeId, parentId, storageParameters);
                for (final SortableId sortableId : ids) {
                    final String id = sortableId.getId();
                    final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                    List<String> list = treeMap.get(localizedName);
                    if (null == list) {
                        list = new ArrayList<String>(2);
                        treeMap.put(localizedName, list);
                    }
                    list.add(id);
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
        {
            /*
             * Get real folder storage for primary mail folder
             */
            final String fullname = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.DEFAULT_FOLDER_ID);
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, fullname);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, fullname);
            }
            folderStorage.startTransaction(storageParameters, false);
            try {
                /*
                 * Get IDs
                 */
                final SortableId[] mailIDs = folderStorage.getSubfolders(realTreeId, fullname, storageParameters);
                for (final SortableId sortableId : mailIDs) {
                    final String id = sortableId.getId();
                    final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                    List<String> list = treeMap.get(localizedName);
                    if (null == list) {
                        list = new ArrayList<String>(2);
                        treeMap.put(localizedName, list);
                    }
                    list.add(id);
                }
                /*
                 * Add default folders: Trash, Sent, Drafts, ...
                 */
                final Set<String> defIds;
                {
                    defIds = new HashSet<String>(6);
                    final String treeId = String.valueOf(tree);
                    defIds.add(folderStorage.getDefaultFolderID(user, treeId, TrashContentType.getInstance(), storageParameters));
                    defIds.add(folderStorage.getDefaultFolderID(user, treeId, DraftsContentType.getInstance(), storageParameters));
                    defIds.add(folderStorage.getDefaultFolderID(user, treeId, SentContentType.getInstance(), storageParameters));
                    defIds.add(folderStorage.getDefaultFolderID(user, treeId, SpamContentType.getInstance(), storageParameters));
                }
                final SortableId[] inboxSubfolders = folderStorage.getSubfolders(realTreeId, PREPARED_FULLNAME_INBOX, storageParameters);
                final boolean[] contained = Select.containsFolders(contextId, tree, storageParameters.getUserId(), inboxSubfolders, StorageType.WORKING);
                for (int i = 0; i < inboxSubfolders.length; i++) {
                    if (!contained[i]) {
                        final String id = inboxSubfolders[i].getId();
                        if (defIds.contains(id)) {
                            final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                            List<String> list = treeMap.get(localizedName);
                            if (null == list) {
                                list = new ArrayList<String>(2);
                                treeMap.put(localizedName, list);
                            }
                            list.add(id);
                        }
                    }
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
        {
            /*
             * Get the ones from virtual table
             */
            final List<String[]> l = Select.getSubfolderIds(contextId, tree, user.getId(), parentId, StorageType.WORKING);
            for (final String[] idAndName : l) {
                final String localizedName = idAndName[1];
                List<String> list = treeMap.get(localizedName);
                if (null == list) {
                    list = new ArrayList<String>(2);
                    treeMap.put(localizedName, list);
                }
                list.add(idAndName[0]);
            }
        }
        {
            // Get other top-level folders: shared + public
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
            }
            folderStorage.startTransaction(storageParameters, false);
            try {
                // Get subfolders
                final SortableId[] subfolders =
                    folderStorage.getSubfolders(FolderStorage.REAL_TREE_ID, FolderStorage.ROOT_ID, storageParameters);
                for (int i = 0; i < subfolders.length; i++) {
                    final String id = subfolders[i].getId();
                    if (!FolderStorage.PRIVATE_ID.equals(id)) { // Exclude private folder
                        final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                        List<String> list = treeMap.get(localizedName);
                        if (null == list) {
                            list = new ArrayList<String>(2);
                            treeMap.put(localizedName, list);
                        }
                        list.add(id);
                    }

                    // if (FolderStorage.PUBLIC_ID.equals(id)) {
                    // final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                    // List<String> list = treeMap.get(localizedName);
                    // if (null == list) {
                    // list = new ArrayList<String>(2);
                    // treeMap.put(localizedName, list);
                    // }
                    // list.add(id);
                    // } else if (FolderStorage.SHARED_ID.equals(id)) {
                    // final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                    // List<String> list = treeMap.get(localizedName);
                    // if (null == list) {
                    // list = new ArrayList<String>(2);
                    // treeMap.put(localizedName, list);
                    // }
                    // list.add(id);
                    // }
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
        final List<String> sortedIDs;
        {
            final Collection<List<String>> values = treeMap.values();
            sortedIDs = new ArrayList<String>(values.size());
            for (final List<String> list : values) {
                for (final String id : list) {
                    sortedIDs.add(id);
                }
            }
        }
        /*
         * External mail accounts
         */
        final List<String> subfolderIDs;
        final MailAccountStorageService mass = OutlookServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class);
        if (null != mass) {
            final List<MailAccount> accounts;
            try {
                final MailAccount[] mailAccounts = mass.getUserMailAccounts(user.getId(), contextId);
                accounts = new ArrayList<MailAccount>(mailAccounts.length);
                accounts.addAll(Arrays.asList(mailAccounts));
                Collections.sort(accounts, new MailAccountComparator(locale));
            } catch (final MailAccountException e) {
                throw new FolderException(e);
            }
            if (accounts.isEmpty()) {
                subfolderIDs = sortedIDs;
            } else {
                subfolderIDs = new ArrayList<String>(sortedIDs.size() + accounts.size());
                subfolderIDs.addAll(sortedIDs);
                for (final MailAccount mailAccount : accounts) {
                    if (!mailAccount.isDefaultAccount()) {
                        subfolderIDs.add(MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID));
                    }
                }
                // TODO: No Unified INBOX if not enabled
            }
        } else {
            subfolderIDs = sortedIDs;
        }
        final SortableId[] ret = new SortableId[subfolderIDs.size()];
        {
            int i = 0;
            for (final String id : subfolderIDs) {
                ret[i] = new OutlookId(id, i);
                i++;
            }
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
        final int contextId = storageParameters.getContextId();
        final int tree = Tools.getUnsignedInteger(folder.getTreeID());
        final int userId = storageParameters.getUserId();
        if (Select.containsFolder(contextId, tree, userId, folder.getID(), StorageType.WORKING)) {
            Update.updateFolder(contextId, tree, userId, folder);
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

    private static final class FolderNameComparator implements Comparator<String> {

        private final Collator collator;

        public FolderNameComparator(final Locale locale) {
            super();
            collator = Collator.getInstance(locale);
            collator.setStrength(Collator.SECONDARY);
        }

        public int compare(final String o1, final String o2) {
            return collator.compare(o1, o2);
        }

    } // End of FolderNameComparator

    private static List<String> toIDList(final SortableId[] ids) {
        final List<String> l = new ArrayList<String>(ids.length);
        for (final SortableId id : ids) {
            l.add(id.getId());
        }
        return l;
    }

    private static String[] getSubfolderIDs(final Folder realFolder, final FolderStorage folderStorage, final StorageParameters storageParameters) throws FolderException {
        final String[] ids = realFolder.getSubfolderIDs();
        if (null != ids) {
            return ids;
        }
        final List<String> idList = toIDList(folderStorage.getSubfolders(realFolder.getTreeID(), realFolder.getID(), storageParameters));
        return idList.toArray(new String[idList.size()]);
    }

    private String getLocalizedName(final String id, final int tree, final Locale locale, final FolderStorage folderStorage, final StorageParameters storageParameters) throws FolderException {
        final String name =
            Select.getFolderName(storageParameters.getContextId(), tree, storageParameters.getUserId(), id, StorageType.WORKING);
        if (null != name) {
            /*
             * If name is held in virtual tree, it has no locale-sensitive string
             */
            return name;
        }
        return folderStorage.getFolder(realTreeId, id, storageParameters).getLocalizedName(locale);
    }

}
