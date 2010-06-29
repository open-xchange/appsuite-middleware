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

import java.sql.Connection;
import java.sql.SQLException;
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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import com.openexchange.database.DBPoolingException;
import com.openexchange.database.DatabaseService;
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
import com.openexchange.folderstorage.database.DatabaseFolderType;
import com.openexchange.folderstorage.database.DatabaseParameterConstants;
import com.openexchange.folderstorage.internal.StorageParametersImpl;
import com.openexchange.folderstorage.internal.Tools;
import com.openexchange.folderstorage.mail.MailFolderType;
import com.openexchange.folderstorage.mail.contentType.DraftsContentType;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.mail.contentType.SentContentType;
import com.openexchange.folderstorage.mail.contentType.SpamContentType;
import com.openexchange.folderstorage.mail.contentType.TrashContentType;
import com.openexchange.folderstorage.messaging.MessagingFolderIdentifier;
import com.openexchange.folderstorage.outlook.sql.Delete;
import com.openexchange.folderstorage.outlook.sql.Insert;
import com.openexchange.folderstorage.outlook.sql.Select;
import com.openexchange.folderstorage.outlook.sql.Update;
import com.openexchange.folderstorage.outlook.sql.Utility;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.MailException;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.messaging.MailMessagingService;
import com.openexchange.mail.mime.MIMEMailException;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountException;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.UnifiedINBOXManagement;
import com.openexchange.messaging.MessagingAccount;
import com.openexchange.messaging.MessagingException;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingService;
import com.openexchange.messaging.registry.MessagingServiceRegistry;
import com.openexchange.server.ServiceException;
import com.openexchange.session.Session;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.sql.DBUtils;

/**
 * {@link OutlookFolderStorage} - The MS Outlook folder storage.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OutlookFolderStorage implements FolderStorage {

    /**
     * The logger.
     */
    static final org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory.getLog(OutlookFolderStorage.class);

    /**
     * The prepared fullname.
     */
    static final String PREPARED_FULLNAME_INBOX = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, "INBOX");

    private static final ThreadPools.ExpectedExceptionFactory<FolderException> FACTORY =
        new ThreadPools.ExpectedExceptionFactory<FolderException>() {

            public Class<FolderException> getType() {
                return FolderException.class;
            }

            public FolderException newUnexpectedError(final Throwable t) {
                return FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(t, t.getMessage());
            }
        };

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
    final String realTreeId;

    /**
     * This storage's folder type.
     */
    private final FolderType folderType;

    /**
     * The folder storage registry.
     */
    final OutlookFolderStorageRegistry folderStorageRegistry;

    /**
     * Initializes a new {@link OutlookFolderStorage}.
     */
    public OutlookFolderStorage() {
        super();
        realTreeId = FolderStorage.REAL_TREE_ID;
        folderType = new OutlookFolderType();
        folderStorageRegistry = OutlookFolderStorageRegistry.getInstance();
    }

    public void checkConsistency(final String treeId, final StorageParameters storageParameters) throws FolderException {
        final List<String> folderIds = Select.getFolders(storageParameters.getContextId(), Tools.getUnsignedInteger(treeId), storageParameters.getUserId());
        for (final String folderId : folderIds) {
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
            final boolean started = folderStorage.startTransaction(storageParameters, true);
            try {
                if (!folderStorage.containsFolder(realTreeId, folderId, storageParameters)) {
                    folderStorage.restore(realTreeId, folderId, storageParameters);
                }
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final FolderException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                LOG.warn("Checking consistency failed for tree " + treeId, e);
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                LOG.warn("Checking consistency failed for tree " + treeId, e);
            }
        } 
    }

    public void restore(String treeId, String folderId, StorageParameters storageParameters) throws FolderException {
        // Nothing to restore, not a real storage
    }

    public void clearFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Delegate clear invocation to real storage
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, true);
        try {
            folderStorage.clearFolder(realTreeId, folderId, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
        } catch (final FolderException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public void commitTransaction(final StorageParameters params) {
        // Nothing to do
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return containsFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public boolean containsFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        /*
         * No primary mail account root folder in this tree
         */
        if (MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.DEFAULT_FOLDER_ID).equals(folderId)) {
            return false;
        }
        /*
         * Check
         */
        final FolderStorage dedicatedFolderStorage = folderStorageRegistry.getDedicatedFolderStorage(FolderStorage.REAL_TREE_ID, folderId);
        if (!dedicatedFolderStorage.containsFolder(FolderStorage.REAL_TREE_ID, folderId, storageType, storageParameters)) {
            return false;
        }

        // Exclude unsupported folders like infostore folders
        // final Folder folder = dedicatedFolderStorage.getFolder(FolderStorage.REAL_TREE_ID, folderId, storageType, storageParameters);
        // if (InfostoreContentType.getInstance().equals(folder.getContentType())) {
        // return false;
        // }
        return true;
    }

    public void createFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        /*
         * Create only if folder could not be stored in real storage
         */
        final String folderId = folder.getID();
        final Folder realFolder;
        {
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
            }
            final boolean started = folderStorage.startTransaction(storageParameters, true);
            try {
                realFolder = folderStorage.getFolder(realTreeId, folderId, StorageType.WORKING, storageParameters);
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final FolderException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        final String parentId = folder.getParentID();
        if (realFolder.getParentID().equals(parentId)) {
            /*
             * Folder already properly created at right location in real storage
             */
            return;
        }
        final int userId = storageParameters.getUserId();
        if (null == realFolder.getLastModified()) {
            /*
             * Real folder has no last-modified time stamp, but virtual needs to have.
             */
            folder.setModifiedBy(userId);
            folder.setLastModified(new Date());
        }
        final int contextId = storageParameters.getContextId();
        final int tree = Tools.getUnsignedInteger(folder.getTreeID());
        final Connection wcon = checkWriteConnection(storageParameters);
        if (null == wcon) {
            Insert.insertFolder(contextId, tree, userId, folder);
        } else {
            Insert.insertFolder(contextId, tree, userId, folder, wcon);
        }
    }

    public void deleteFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Delete from tables if present
         */
        final Connection wcon = checkWriteConnection(storageParameters);
        if (null == wcon) {
            Delete.deleteFolder(
                storageParameters.getContextId(),
                Tools.getUnsignedInteger(treeId),
                storageParameters.getUserId(),
                folderId,
                true);
        } else {
            Delete.deleteFolder(
                storageParameters.getContextId(),
                Tools.getUnsignedInteger(treeId),
                storageParameters.getUserId(),
                folderId,
                true,
                wcon);
        }
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
        final boolean started = byContentType.startTransaction(storageParameters, false);
        try {
            final String defaultFolderID = byContentType.getDefaultFolderID(user, treeId, contentType, storageParameters);
            if (started) {
                byContentType.commitTransaction(storageParameters);
            }
            return defaultFolderID;
        } catch (final FolderException e) {
            if (started) {
                byContentType.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                byContentType.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public String[] getDeletedFolderIDs(final String treeId, final Date timeStamp, final StorageParameters storageParameters) {
        return new String[0];
    }

    public boolean containsForeignObjects(final User user, final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Get real folder storage
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            final boolean containsForeignObjects = folderStorage.containsForeignObjects(user, treeId, folderId, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return containsForeignObjects;
        } catch (final FolderException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public boolean isEmpty(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        /*
         * Get real folder storage
         */
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, false);
        try {
            final boolean isEmpty = folderStorage.isEmpty(realTreeId, folderId, storageParameters);
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
            return isEmpty;
        } catch (final FolderException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public void updateLastModified(final long lastModified, final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, folderId);
        if (null == folderStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, folderId);
        }
        final boolean started = folderStorage.startTransaction(storageParameters, true);
        try {
            final Folder realFolder = folderStorage.getFolder(realTreeId, folderId, StorageType.WORKING, storageParameters);
            if (null == realFolder.getLastModified()) {
                /*
                 * Real folder has no last-modified time stamp, but virtual needs to have.
                 */
                final int contextId = storageParameters.getContextId();
                final int tree = Tools.getUnsignedInteger(treeId);
                final int userId = storageParameters.getUserId();
                final boolean containsFolder;
                {
                    final Connection con = checkReadConnection(storageParameters);
                    if (null == con) {
                        containsFolder = Select.containsFolder(contextId, tree, userId, folderId, StorageType.WORKING);
                    } else {
                        containsFolder = Select.containsFolder(contextId, tree, userId, folderId, StorageType.WORKING, con);
                    }
                }
                if (containsFolder) {
                    Update.updateLastModified(contextId, tree, userId, folderId, lastModified);
                }
            } else {
                folderStorage.updateLastModified(lastModified, FolderStorage.REAL_TREE_ID, folderId, storageParameters);
            }
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
        } catch (final FolderException e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageParameters storageParameters) throws FolderException {
        return getFolder(treeId, folderId, StorageType.WORKING, storageParameters);
    }

    public Folder getFolder(final String treeId, final String folderId, final StorageType storageType, final StorageParameters storageParameters) throws FolderException {
        /*
         * Primary account's root folder does not exist in tis folder tree
         */
        if (MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.DEFAULT_FOLDER_ID).equals(folderId)) {
            throw FolderExceptionErrorMessage.NOT_FOUND.create(folderId, treeId);
        }
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
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get folder
                    rootFolder = folderStorage.getFolder(FolderStorage.REAL_TREE_ID, folderId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                } catch (final FolderException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
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
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    /*
                     * Get folder
                     */
                    realFolder = folderStorage.getFolder(realTreeId, folderId, storageParameters);
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                } catch (final FolderException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    /*
                     * Check consistency
                     */
                    final Connection wcon = checkWriteConnection(storageParameters);
                    if (null == wcon) {
                        if (Select.containsFolder(contextId, tree, user.getId(), folderId, StorageType.WORKING)) {
                            /*
                             * In virtual tree table, but shouldn't
                             */
                            Delete.deleteFolder(contextId, tree, user.getId(), folderId, false);
                            throw FolderExceptionErrorMessage.TEMPORARY_ERROR.create(e, new Object[0]);
                        }
                    } else {
                        if (Select.containsFolder(contextId, tree, user.getId(), folderId, StorageType.WORKING, wcon)) {
                            /*
                             * In virtual tree table, but shouldn't
                             */
                            Delete.deleteFolder(contextId, tree, user.getId(), folderId, false, wcon);
                            throw FolderExceptionErrorMessage.TEMPORARY_ERROR.create(e, new Object[0]);
                        }
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
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
                final boolean contains;
                {
                    final Connection con = checkReadConnection(storageParameters);
                    if (null == con) {
                        contains = Select.containsParent(contextId, tree, user.getId(), folderId, StorageType.WORKING);
                    } else {
                        contains = Select.containsParent(contextId, tree, user.getId(), folderId, StorageType.WORKING, con);
                    }
                }
                if (contains) {
                    outlookFolder.setSubfolderIDs(null);
                } else {
                    outlookFolder.setSubfolderIDs(realSubfolderIDs); // Zero-length array => No subfolders
                }
            } else {
                outlookFolder.setSubfolderIDs(null);
            }
        }
        /*
         * Load folder data from database
         */
        final boolean presentInTable;
        {
            final Connection con = checkReadConnection(storageParameters);
            if (null == con) {
                presentInTable = Select.fillFolder(contextId, tree, user.getId(), user.getLocale(), outlookFolder, storageType);
            } else {
                presentInTable = Select.fillFolder(contextId, tree, user.getId(), user.getLocale(), outlookFolder, storageType, con);
            }
        }
        // 
        if (!presentInTable) {
            doModifications(outlookFolder);
        }
        return outlookFolder;
    }

    public FolderType getFolderType() {
        return folderType;
    }

    public String[] getModifiedFolderIDs(final String treeId, final Date timeStamp, final ContentType[] includeContentTypes, final StorageParameters storageParameters) throws FolderException {
        if (null == includeContentTypes || includeContentTypes.length == 0) {
            return new String[0];
        }
        boolean containsMail = false;
        for (final ContentType contentType : includeContentTypes) {
            if (MailContentType.getInstance().equals(contentType)) {
                containsMail = true;
                break;
            }
        }
        if (containsMail) {
            final User user = storageParameters.getUser();
            final Locale locale = user.getLocale();
            final int contextId = storageParameters.getContextId();
            final int tree = Tools.getUnsignedInteger(treeId);
            final FolderNameComparator comparator = new FolderNameComparator(locale);
            final List<TreeMap<String, List<String>>> maps = new ArrayList<TreeMap<String,List<String>>>(2);
            /*
             * From primary mail folder
             */
            maps.add(new MailFolderCallable(comparator, locale, user, contextId, tree, storageParameters).call());
            /*
             * Callable for the ones from virtual table
             */
            maps.add(new Callable<TreeMap<String, List<String>>>() {

                public TreeMap<String, List<String>> call() throws FolderException {
                    /*
                     * Get the ones from virtual table
                     */
                    final List<String[]> l;
                    {
                        final Connection con = checkReadConnection(storageParameters);
                        if (null == con) {
                            l = Select.getSubfolderIds(contextId, tree, user.getId(), FolderStorage.PRIVATE_ID, StorageType.WORKING);
                        } else {
                            l = Select.getSubfolderIds(contextId, tree, user.getId(), FolderStorage.PRIVATE_ID, StorageType.WORKING, con);
                        }
                    }
                    /*
                     * Filter only mail folders
                     */
                    final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                    for (final String[] idAndName : l) {
                        final String id = idAndName[0];
                        if (MailFolderType.getInstance().servesFolderId(id)) {
                            put2TreeMap(idAndName[1], id, treeMap);
                        }
                    }
                    return treeMap;
                }
            }.call());
            /*
             * Merge
             */
            final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
            for (final TreeMap<String, List<String>> tm : maps) {
                for (final Entry<String, List<String>> entry : tm.entrySet()) {
                    final String key = entry.getKey();
                    final List<String> list = treeMap.get(key);
                    if (null == list) {
                        treeMap.put(key, entry.getValue());
                    } else {
                        list.addAll(entry.getValue());
                    }
                }
            }
            /*
             * Return
             */
            final Collection<List<String>> values = treeMap.values();
            final List<String> ret = new ArrayList<String>(values.size());
            for (final List<String> list : values) {
                for (final String id : list) {
                    ret.add(id);
                }
            }
            return ret.toArray(new String[ret.size()]);
        }
        /*
         * Empty array
         */
        return new String[0];
    }

    protected static boolean supportsMail(final ContentType[] types) {
        for (ContentType contentType : types) {
            if (MailContentType.getInstance().equals(contentType)) {
                return true;
            }
        }
        return false;
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
         * Other folder than root, INBOX or private
         */
        final List<String[]> l;
        {
            // Get real folder storage
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
            }
            final boolean started = folderStorage.startTransaction(storageParameters, false);
            try {
                // Get real subfolders
                final Folder parentFolder = folderStorage.getFolder(realTreeId, parentId, storageParameters);
                final String[] realSubfolderIds = getSubfolderIDs(parentFolder, folderStorage, storageParameters);
                l = new ArrayList<String[]>(realSubfolderIds.length);
                if (parentFolder.isDefault()) {
                    /*
                     * Strip subfolders occurring at another location in folder tree
                     */
                    final boolean[] contained;
                    {
                        final Connection con = checkReadConnection(storageParameters);
                        if (null == con) {
                            contained =
                                Select.containsFolders(
                                    contextId,
                                    tree,
                                    storageParameters.getUserId(),
                                    realSubfolderIds,
                                    StorageType.WORKING);
                        } else {
                            contained =
                                Select.containsFolders(
                                    contextId,
                                    tree,
                                    storageParameters.getUserId(),
                                    realSubfolderIds,
                                    StorageType.WORKING,
                                    con);
                        }
                    }
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
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final FolderException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        // Load folder data from database
        final String[] ids;
        {
            final Connection con = checkReadConnection(storageParameters);
            if (null == con) {
                ids = Select.getSubfolderIds(contextId, tree, user.getId(), locale, parentId, l, StorageType.WORKING);
            } else {
                ids = Select.getSubfolderIds(contextId, tree, user.getId(), locale, parentId, l, StorageType.WORKING, con);
            }
        }
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
        final boolean started = folderStorage.startTransaction(storageParameters, false);
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
            /*
             * Filter those mail folders which denote a virtual one
             */
            final boolean[] contained;
            {
                final Connection con = checkReadConnection(storageParameters);
                if (null == con) {
                    contained =
                        Select.containsFolders(contextId, tree, storageParameters.getUserId(), inboxSubfolders, StorageType.WORKING);
                } else {
                    contained =
                        Select.containsFolders(contextId, tree, storageParameters.getUserId(), inboxSubfolders, StorageType.WORKING, con);
                }
            }
            for (int i = 0; i < inboxSubfolders.length; i++) {
                if (!contained[i]) {
                    final String id = inboxSubfolders[i].getId();
                    if (!defIds.contains(id)) {
                        final String localizedName = getLocalizedName(id, tree, locale, folderStorage, storageParameters);
                        put2TreeMap(localizedName, id, treeMap);
                    }
                }
            }
            /*
             * Get virtual subfolders
             */
            final List<String[]> ids;
            {
                final Connection con = checkReadConnection(storageParameters);
                if (null == con) {
                    ids = Select.getSubfolderIds(contextId, tree, user.getId(), parentId, StorageType.WORKING);
                } else {
                    ids = Select.getSubfolderIds(contextId, tree, user.getId(), parentId, StorageType.WORKING, con);
                }
            }
            /*
             * Merge them into tree map
             */
            for (final String[] idAndName : ids) {
                /*
                 * Names loaded from DB have no locale-sensitive string
                 */
                put2TreeMap(idAndName[1], idAndName[0], treeMap);
            }
            if (started) {
                folderStorage.commitTransaction(storageParameters);
            }
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
            if (started) {
                folderStorage.rollback(storageParameters);
            }
            throw e;
        } catch (final Exception e) {
            if (started) {
                folderStorage.rollback(storageParameters);
            }
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
            final boolean started = folderStorage.startTransaction(storageParameters, false);
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
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
            } catch (final FolderException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
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

    private SortableId[] getPrivateFolderSubfolders(final String parentId, final int tree, final StorageParameters parameters, final User user, final Locale locale, final int contextId) throws FolderException {
        final CompletionService<TreeMap<String, List<String>>> completionService;
        try {
            completionService =
                new ThreadPoolCompletionService<TreeMap<String, List<String>>>(OutlookServiceRegistry.getServiceRegistry().getService(
                    ThreadPoolService.class,
                    true));
        } catch (final ServiceException e) {
            throw new FolderException(e);
        }
        int taskCount = 0;
        final FolderNameComparator comparator = new FolderNameComparator(locale);
        /*
         * Callable for real folder storage
         */
        completionService.submit(new Callable<TreeMap<String, List<String>>>() {

            public TreeMap<String, List<String>> call() throws FolderException {
                /*
                 * Get real folder storage
                 */
                final FolderStorage folderStorage = folderStorageRegistry.getDedicatedFolderStorage(realTreeId, parentId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
                }
                final StorageParameters storageParameters = newStorageParameters(parameters);
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    /*
                     * Get folder
                     */
                    final SortableId[] ids = folderStorage.getSubfolders(realTreeId, parentId, storageParameters);
                    final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                    for (final SortableId sortableId : ids) {
                        final String id = sortableId.getId();
                        put2TreeMap(getLocalizedName(id, tree, locale, folderStorage, storageParameters), id, treeMap);
                    }
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                    return treeMap;
                } catch (final FolderException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        });
        taskCount++;
        /*
         * Callable for primary mail folder
         */
        completionService.submit(new MailFolderCallable(comparator, locale, user, contextId, tree, parameters));
        taskCount++;
        /*
         * Callable for the ones from virtual table
         */
        completionService.submit(new Callable<TreeMap<String, List<String>>>() {

            public TreeMap<String, List<String>> call() throws FolderException {
                /*
                 * Get the ones from virtual table
                 */
                final List<String[]> l;
                {
                    final Connection con = checkReadConnection(parameters);
                    if (null == con) {
                        l = Select.getSubfolderIds(contextId, tree, user.getId(), parentId, StorageType.WORKING);
                    } else {
                        l = Select.getSubfolderIds(contextId, tree, user.getId(), parentId, StorageType.WORKING, con);
                    }
                }
                final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                for (final String[] idAndName : l) {
                    put2TreeMap(idAndName[1], idAndName[0], treeMap);
                }
                return treeMap;
            }
        });
        taskCount++;
        /*
         * Callable for other top-level folders: shared + public
         */
        completionService.submit(new Callable<TreeMap<String, List<String>>>() {

            public TreeMap<String, List<String>> call() throws FolderException {
                // Get other top-level folders: shared + public
                final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, parentId);
                if (null == folderStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, parentId);
                }
                final StorageParameters storageParameters = newStorageParameters(parameters);
                final boolean started = folderStorage.startTransaction(storageParameters, false);
                try {
                    // Get subfolders
                    final SortableId[] subfolders =
                        folderStorage.getSubfolders(FolderStorage.REAL_TREE_ID, FolderStorage.ROOT_ID, storageParameters);
                    final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                    for (int i = 0; i < subfolders.length; i++) {
                        final String id = subfolders[i].getId();
                        if (!FolderStorage.PRIVATE_ID.equals(id)) { // Exclude private folder
                            put2TreeMap(getLocalizedName(id, tree, locale, folderStorage, storageParameters), id, treeMap);
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
                    if (started) {
                        folderStorage.commitTransaction(storageParameters);
                    }
                    return treeMap;
                } catch (final FolderException e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        folderStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        });
        taskCount++;
        /*
         * Obtain external mail accounts with running thread
         */
        final List<String> accountSubfolderIDs;
        {
            final MailAccountStorageService mass = OutlookServiceRegistry.getServiceRegistry().getService(MailAccountStorageService.class);
            if (null == mass) {
                accountSubfolderIDs = Collections.emptyList();
            } else {
                final List<MailAccount> accounts;
                try {

                    // final MailAccount[] mailAccounts = mass.getUserMailAccounts(user.getId(), contextId);
                    // accounts = new ArrayList<MailAccount>(mailAccounts.length);
                    // accounts.addAll(Arrays.asList(mailAccounts));

                    accounts = Arrays.asList(mass.getUserMailAccounts(user.getId(), contextId));
                    Collections.sort(accounts, new MailAccountComparator(locale));
                } catch (final MailAccountException e) {
                    throw new FolderException(e);
                }
                if (accounts.isEmpty()) {
                    accountSubfolderIDs = Collections.emptyList();
                } else {
                    accountSubfolderIDs = new ArrayList<String>(accounts.size());
                    for (final MailAccount mailAccount : accounts) {
                        if (!mailAccount.isDefaultAccount()) {
                            if (UnifiedINBOXManagement.PROTOCOL_UNIFIED_INBOX.equals(mailAccount.getMailProtocol())) {
                                /*
                                 * Ensure Unified INBOX is enabled; meaning at least one account is subscribed to Unified INBOX
                                 */
                                final UnifiedINBOXManagement uim =
                                    OutlookServiceRegistry.getServiceRegistry().getService(UnifiedINBOXManagement.class);
                                try {
                                    if (null != uim && uim.isEnabled(user.getId(), contextId)) {
                                        accountSubfolderIDs.add(MailFolderUtility.prepareFullname(
                                            mailAccount.getId(),
                                            MailFolder.DEFAULT_FOLDER_ID));
                                    }
                                } catch (final MailAccountException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            } else {
                                accountSubfolderIDs.add(MailFolderUtility.prepareFullname(mailAccount.getId(), MailFolder.DEFAULT_FOLDER_ID));
                            }
                        }
                    }
                }
            }
        }
        /*
         * Obtain external messaging accounts with running thread
         */
        final List<String> messagingSubfolderIDs;
        {
            /*
             * Messaging accounts; except mail
             */
            final List<MessagingAccount> messagingAccounts = new ArrayList<MessagingAccount>();
            {
                final MessagingServiceRegistry msr = OutlookServiceRegistry.getServiceRegistry().getService(MessagingServiceRegistry.class);
                if (null != msr) {
                    try {
                        final List<MessagingService> allServices = msr.getAllServices();
                        for (final MessagingService messagingService : allServices) {
                            if (!messagingService.getId().equals(MailMessagingService.ID)) {
                                /*
                                 * Only non-mail services
                                 */
                                try {
                                    messagingAccounts.addAll(messagingService.getAccountManager().getAccounts(parameters.getSession()));
                                } catch (final MessagingException e) {
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        }
                    } catch (final MessagingException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                if (!messagingAccounts.isEmpty()) {
                    final int sz = messagingAccounts.size();
                    messagingSubfolderIDs = new ArrayList<String>(sz);
                    for (int i = 0; i < sz; i++) {
                        final MessagingAccount ma = messagingAccounts.get(i);
                        final MessagingFolderIdentifier mfi =
                            new MessagingFolderIdentifier(ma.getMessagingService().getId(), ma.getId(), MessagingFolder.ROOT_FULLNAME);
                        messagingSubfolderIDs.add(mfi.toString());
                    }
                } else {
                    messagingSubfolderIDs = Collections.emptyList();
                }
            }
        }
        /*
         * Wait for completion
         */
        final List<String> sortedIDs;
        {
            final List<TreeMap<String, List<String>>> taken = ThreadPools.takeCompletionService(completionService, taskCount, FACTORY);
            final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
            for (final TreeMap<String, List<String>> tm : taken) {
                for (final Entry<String, List<String>> entry : tm.entrySet()) {
                    final String key = entry.getKey();
                    final List<String> list = treeMap.get(key);
                    if (null == list) {
                        treeMap.put(key, entry.getValue());
                    } else {
                        list.addAll(entry.getValue());
                    }
                }
            }
            /*
             * Get sorted values
             */
            final Collection<List<String>> values = treeMap.values();
            sortedIDs = new ArrayList<String>(values.size() + accountSubfolderIDs.size());
            for (final List<String> list : values) {
                for (final String id : list) {
                    sortedIDs.add(id);
                }
            }
        }
        /*
         * Add external mail accounts
         */
        sortedIDs.addAll(accountSubfolderIDs);
        /*
         * Add external messaging accounts
         */
        sortedIDs.addAll(messagingSubfolderIDs);
        final int size = sortedIDs.size();
        final SortableId[] ret = new SortableId[size];
        for (int i = 0; i < size; i++) {
            ret[i] = new OutlookId(sortedIDs.get(i), i);
        }
        return ret;
    }

    static void put2TreeMap(final String localizedName, final String id, final TreeMap<String, List<String>> treeMap) {
        List<String> list = treeMap.get(localizedName);
        if (null == list) {
            list = new ArrayList<String>(2);
            treeMap.put(localizedName, list);
        }
        list.add(id);
    }

    /**
     * Creates a new storage parameter instance.
     */
    static StorageParameters newStorageParameters(final StorageParameters source) {
        final Session session = source.getSession();
        if (null == session) {
            return new StorageParametersImpl(source.getUser(), source.getContext());
        }
        return new StorageParametersImpl((ServerSession) session);
    }

    public ContentType[] getSupportedContentTypes() {
        return new ContentType[0];
    }

    public void rollback(final StorageParameters params) {
        // Nothing to do
    }

    public boolean startTransaction(final StorageParameters parameters, final boolean modify) {
        return false;
    }

    public void updateFolder(final Folder folder, final StorageParameters storageParameters) throws FolderException {
        /*
         * Update only if folder is contained
         */
        final int contextId = storageParameters.getContextId();
        final int tree = Tools.getUnsignedInteger(folder.getTreeID());
        final int userId = storageParameters.getUserId();
        final String folderId = folder.getID();
        final boolean contains;
        {
            final Connection con = checkReadConnection(storageParameters);
            if (null == con) {
                contains = Select.containsFolder(contextId, tree, userId, folderId, StorageType.WORKING);
            } else {
                contains = Select.containsFolder(contextId, tree, userId, folderId, StorageType.WORKING, con);
            }
        }
        if (contains) {
            /*
             * Get a connection
             */
            final Connection wcon = checkWriteConnection(storageParameters);
            if (wcon == null) {
                final DatabaseService databaseService = Utility.getDatabaseService();
                final Connection con;
                try {
                    con = databaseService.getWritable(contextId);
                    con.setAutoCommit(false); // BEGIN
                } catch (final DBPoolingException e) {
                    throw new FolderException(e);
                } catch (final SQLException e) {
                    throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
                }
                try {
                    final String name = folder.getName();
                    if (name != null) {
                        Update.updateName(contextId, tree, userId, folderId, name, con);
                    }
                    final String parentId = folder.getParentID();
                    if (parentId != null) {
                        Update.updateParent(contextId, tree, userId, folderId, parentId, con);
                    }
                    final String newId = folder.getNewID();
                    if (newId != null) {
                        Update.updateId(contextId, tree, userId, folderId, newId, con);
                    }
                    Update.updateLastModified(contextId, tree, userId, folderId, System.currentTimeMillis(), con);
                    con.commit(); // COMMIT
                } catch (final SQLException e) {
                    DBUtils.rollback(con); // ROLLBACK
                    throw FolderExceptionErrorMessage.SQL_ERROR.create(e, e.getMessage());
                } catch (final FolderException e) {
                    DBUtils.rollback(con); // ROLLBACK
                    throw e;
                } catch (final Exception e) {
                    DBUtils.rollback(con); // ROLLBACK
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                } finally {
                    DBUtils.autocommit(con);
                    databaseService.backWritable(contextId, con);
                }
            } else {
                try {
                    final String name = folder.getName();
                    if (name != null) {
                        Update.updateName(contextId, tree, userId, folderId, name, wcon);
                    }
                    final String parentId = folder.getParentID();
                    if (parentId != null) {
                        Update.updateParent(contextId, tree, userId, folderId, parentId, wcon);
                    }
                    final String newId = folder.getNewID();
                    if (newId != null) {
                        Update.updateId(contextId, tree, userId, folderId, newId, wcon);
                    }
                    Update.updateLastModified(contextId, tree, userId, folderId, System.currentTimeMillis(), wcon);
                } catch (final FolderException e) {
                    throw e;
                } catch (final Exception e) {
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        }
    }

    private final class MailFolderCallable implements Callable<TreeMap<String, List<String>>> {

        private final FolderNameComparator comparator;

        private final Locale locale;

        private final User user;

        private final int contextId;

        private final int tree;

        private final StorageParameters parameters;

        public MailFolderCallable(final FolderNameComparator comparator, final Locale locale, final User user, final int contextId, final int tree, final StorageParameters parameters) {
            this.comparator = comparator;
            this.locale = locale;
            this.user = user;
            this.contextId = contextId;
            this.tree = tree;
            this.parameters = parameters;
        }

        public TreeMap<String, List<String>> call() throws FolderException {
            /*
             * Get real folder storage for primary mail folder
             */
            final String fullname = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.DEFAULT_FOLDER_ID);
            final FolderStorage folderStorage = folderStorageRegistry.getFolderStorage(realTreeId, fullname);
            if (null == folderStorage) {
                throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(realTreeId, fullname);
            }
            final StorageParameters storageParameters = newStorageParameters(parameters);
            final boolean started = folderStorage.startTransaction(storageParameters, false);
            try {
                /*
                 * Get IDs
                 */
                final SortableId[] mailIDs;
                try {
                    mailIDs = folderStorage.getSubfolders(realTreeId, fullname, storageParameters);
                } catch (final FolderException e) {
                    final Throwable cause = e.getCause();
                    if (cause instanceof MailException) {
                        final MailException me = (MailException) cause;
                        final int number = me.getDetailNumber();
                        if (MailException.Code.ACCOUNT_DOES_NOT_EXIST.getNumber() == number) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(e.getMessage(), e);
                            }
                            /*
                             * Return empty map
                             */
                            return new TreeMap<String, List<String>>(comparator);
                        } else if (MIMEMailException.Code.INVALID_CREDENTIALS.getNumber() == number) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(e.getMessage(), e);
                            }
                            /*
                             * Return empty map
                             */
                            return new TreeMap<String, List<String>>(comparator);
                        }
                    }
                    throw e;
                }
                final TreeMap<String, List<String>> treeMap = new TreeMap<String, List<String>>(comparator);
                for (final SortableId sortableId : mailIDs) {
                    final String id = sortableId.getId();
                    put2TreeMap(getLocalizedName(id, tree, locale, folderStorage, storageParameters), id, treeMap);
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
                final boolean[] contained;
                {
                    final Connection con = checkReadConnection(storageParameters);
                    if (null == con) {
                        contained =
                            Select.containsFolders(contextId, tree, storageParameters.getUserId(), inboxSubfolders, StorageType.WORKING);
                    } else {
                        contained =
                            Select.containsFolders(
                                contextId,
                                tree,
                                storageParameters.getUserId(),
                                inboxSubfolders,
                                StorageType.WORKING,
                                con);
                    }
                }
                for (int i = 0; i < inboxSubfolders.length; i++) {
                    if (!contained[i]) {
                        final String id = inboxSubfolders[i].getId();
                        if (defIds.contains(id)) {
                            put2TreeMap(getLocalizedName(id, tree, locale, folderStorage, storageParameters), id, treeMap);
                        }
                    }
                }
                if (started) {
                    folderStorage.commitTransaction(storageParameters);
                }
                return treeMap;
            } catch (final FolderException e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw e;
            } catch (final Exception e) {
                if (started) {
                    folderStorage.rollback(storageParameters);
                }
                throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
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

    String getLocalizedName(final String id, final int tree, final Locale locale, final FolderStorage folderStorage, final StorageParameters storageParameters) throws FolderException {
        final String name;
        {
            final Connection con = checkReadConnection(storageParameters);
            if (null == con) {
                name = Select.getFolderName(storageParameters.getContextId(), tree, storageParameters.getUserId(), id, StorageType.WORKING);
            } else {
                name =
                    Select.getFolderName(
                        storageParameters.getContextId(),
                        tree,
                        storageParameters.getUserId(),
                        id,
                        StorageType.WORKING,
                        con);
            }
        }
        if (null != name) {
            /*
             * If name is held in virtual tree, it has no locale-sensitive string
             */
            return name;
        }
        return folderStorage.getFolder(realTreeId, id, storageParameters).getLocalizedName(locale);
    }

    static Connection checkReadConnection(final StorageParameters storageParameters) {
        return storageParameters.<Connection> getParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_CONNECTION);
    }

    static Connection checkWriteConnection(final StorageParameters storageParameters) {
        final Connection con =
            storageParameters.<Connection> getParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_CONNECTION);
        final Boolean writable =
            storageParameters.<Boolean> getParameter(DatabaseFolderType.getInstance(), DatabaseParameterConstants.PARAM_WRITABLE);
        if (null != con && null != writable && writable.booleanValue()) {
            return con;
        }
        return null;
    }

    static void doModifications(final OutlookFolder folder) {
        if (FolderStorage.PUBLIC_ID.equals(folder.getID())) {
            doPublicRootModifications(folder);
        } else if (FolderStorage.SHARED_ID.equals(folder.getID())) {
            doSharedRootModifications(folder);
        } else if (FolderStorage.GLOBAL_ADDRESS_BOOK_ID.equals(folder.getID())) {
            folder.setParentID(FolderStorage.PUBLIC_ID);
        } else if (String.valueOf(FolderObject.SYSTEM_GLOBAL_FOLDER_ID).equals(folder.getID())) { // Should never be reached since GLOBAL_FOLDER is deactivated by default
            folder.setParentID(FolderStorage.PUBLIC_ID);
        }
    }

    private static void doPublicRootModifications(final OutlookFolder folder) {
        folder.setParentID(FolderStorage.PRIVATE_ID);
    }

    private static void doSharedRootModifications(final OutlookFolder folder) {
        folder.setParentID(FolderStorage.PRIVATE_ID);
    }
}
