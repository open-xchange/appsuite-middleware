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

package com.openexchange.folderstorage.internal.performers;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.composition.FilenameValidationUtils;
import com.openexchange.folderstorage.ContentType;
import com.openexchange.folderstorage.Folder;
import com.openexchange.folderstorage.FolderExceptionErrorMessage;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.folderstorage.FolderStorage;
import com.openexchange.folderstorage.FolderStorageDiscoverer;
import com.openexchange.folderstorage.Permission;
import com.openexchange.folderstorage.SortableId;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.folderstorage.database.contentType.InfostoreContentType;
import com.openexchange.folderstorage.internal.CalculatePermission;
import com.openexchange.folderstorage.internal.ConfiguredDefaultPermissions;
import com.openexchange.folderstorage.mail.contentType.MailContentType;
import com.openexchange.folderstorage.outlook.DuplicateCleaner;
import com.openexchange.folderstorage.outlook.OutlookFolderStorage;
import com.openexchange.folderstorage.tx.TransactionManager;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Strings;
import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link CreatePerformer} - Serves the <code>CREATE</code> request.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CreatePerformer extends AbstractUserizedFolderPerformer {

    private static final String CONTENT_TYPE_MAIL = MailContentType.getInstance().toString();

    /**
     * Initializes a new {@link CreatePerformer}.
     *
     * @param session The session
     * @throws OXException If passed session is invalid
     */
    public CreatePerformer(final ServerSession session, final FolderServiceDecorator decorator) throws OXException {
        super(session, decorator);
    }

    /**
     * Initializes a new {@link CreatePerformer}.
     *
     * @param user The user
     * @param context The context
     */
    public CreatePerformer(final User user, final Context context, final FolderServiceDecorator decorator) {
        super(user, context, decorator);
    }

    /**
     * Initializes a new {@link CreatePerformer}.
     *
     * @param session The session
     * @param folderStorageDiscoverer The folder storage discoverer
     * @throws OXException If passed session is invalid
     */
    public CreatePerformer(final ServerSession session, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) throws OXException {
        super(session, decorator, folderStorageDiscoverer);
    }

    /**
     * Initializes a new {@link CreatePerformer}.
     *
     * @param user The user
     * @param context The context
     * @param folderStorageDiscoverer The folder storage discoverer
     */
    public CreatePerformer(final User user, final Context context, final FolderServiceDecorator decorator, final FolderStorageDiscoverer folderStorageDiscoverer) {
        super(user, context, decorator, folderStorageDiscoverer);
    }

    /**
     * Gets the configured default permissions for a new folder that is supposed to be created below specified parent.
     *
     * @param parentId The identifier of the parent folder
     * @param userId The user identifier
     * @param contextId The context identifier
     * @return The configured default permissions or <code>null</code>
     * @throws OXException If look-up for configured default permissions fails
     */
    private Permission[] getConfiguredDefaultPermissionsFor(String parentId, int userId, int contextId) throws OXException {
        return ConfiguredDefaultPermissions.getInstance().getConfiguredDefaultPermissionsFor(parentId, userId, contextId);
    }

    /**
     * Performs the <code>CREATE</code> request.
     *
     * @param toCreate The object describing the folder to create
     * @throws OXException If creation fails
     */
    public String doCreate(final Folder toCreate) throws OXException {

        if (InfostoreContentType.getInstance().equals(toCreate.getContentType()) && Strings.isNotEmpty(toCreate.getName())) {
            FilenameValidationUtils.checkCharacters(toCreate.getName());
            FilenameValidationUtils.checkName(toCreate.getName());
        }

        final String parentId = toCreate.getParentID();
        if (null == parentId) {
            throw FolderExceptionErrorMessage.MISSING_PARENT_ID.create(new Object[0]);
        }
        final String treeId = toCreate.getTreeID();
        if (null == treeId) {
            throw FolderExceptionErrorMessage.MISSING_TREE_ID.create(new Object[0]);
        }
        if (!KNOWN_TREES.contains(treeId)) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create("Create not supported by tree " + treeId);
        }
        final FolderStorage parentStorage = folderStorageDiscoverer.getFolderStorage(treeId, parentId);
        if (null == parentStorage) {
            throw FolderExceptionErrorMessage.NO_STORAGE_FOR_ID.create(treeId, parentId);
        }

        TransactionManager transactionManager = TransactionManager.initTransaction(storageParameters);
        boolean rollbackTransaction = true;
        /*
         * Throws an exception if someone tries to add an element. If this happens, you found a bug.
         * As long as a TransactionManager is present, every storage has to add itself to the
         * TransactionManager in FolderStorage.startTransaction() and must return false.
         */
        final List<FolderStorage> openedStorages = Collections.emptyList();
        checkOpenedStorage(parentStorage, openedStorages);
        try {
            final Folder parent = parentStorage.getFolder(treeId, parentId, storageParameters);
            /*
             * Check folder permission for parent folder
             */
            final Permission parentPermission = CalculatePermission.calculate(parent, this, ALL_ALLOWED);
            if (!parentPermission.isVisible()) {
                throw FolderExceptionErrorMessage.FOLDER_NOT_VISIBLE.create(getFolderInfo4Error(parent), getUserInfo4Error(), getContextInfo4Error());
            }
            final String cts;
            {
                final ContentType contentType = toCreate.getContentType();
                if (null == contentType) {
                    throw AjaxExceptionCodes.MISSING_FIELD.create("module");
                }
                cts = contentType.toString();
                if (isPublicPimFolder(parent) && CONTENT_TYPE_MAIL.equals(cts)) {
                    throw FolderExceptionErrorMessage.NO_PUBLIC_MAIL_FOLDER.create();
                }
            }
            /*
             * Check for duplicates for OLOX-covered folders
             */
            UserizedFolder existingFolder = checkForEqualName(treeId, parentId, toCreate, toCreate.getContentType(), true);
            if (null != existingFolder) {
                if (null != session && "USM-JSON".equals(session.getClient())) {
                    return existingFolder.getID(); // taken over from fix for bug #21286 ...
                }
                throw FolderExceptionErrorMessage.EQUAL_NAME.create(toCreate.getName(), parent.getLocalizedName(getLocale()), treeId);
            }
            String reservedName = checkForReservedName(treeId, parentId, toCreate, toCreate.getContentType(), false);
            if (null != reservedName) {
                throw FolderExceptionErrorMessage.RESERVED_NAME.create(toCreate.getName());
            }

            boolean addedDecorator = false;
            FolderServiceDecorator decorator = storageParameters.getDecorator();
            if (decorator == null) {
                decorator = new FolderServiceDecorator();
                storageParameters.setDecorator(decorator);
                addedDecorator = true;
            }
            boolean isRecursion = decorator.containsProperty(RECURSION_MARKER);
            if (!isRecursion) {
                decorator.put(RECURSION_MARKER, true);
            }
            /*
             * check for any present guest permissions
             */
            Permission[] permissions = toCreate.getPermissions();
            if (null == permissions) {
                /*
                 * client did not pass permissions, check whether to apply default ones
                 */
                permissions = getConfiguredDefaultPermissionsFor(parentId, storageParameters.getUserId(), storageParameters.getContextId());
            }
            ComparedFolderPermissions comparedPermissions = new ComparedFolderPermissions(session, permissions, new Permission[0]);
            final String newId;
            try {
                /*
                 * Create folder dependent on folder is virtual or not
                 */
                if (FolderStorage.REAL_TREE_ID.equals(toCreate.getTreeID())) {
                    newId = doCreateReal(toCreate, parentId, treeId, parentStorage, transactionManager, comparedPermissions);
                } else {
                    newId = doCreateVirtual(toCreate, parentId, treeId, parentStorage, openedStorages, transactionManager, comparedPermissions);
                }
            } finally {
                if (!isRecursion) {
                    decorator.remove(RECURSION_MARKER);
                }

                if (addedDecorator) {
                    storageParameters.setDecorator(null);
                }
            }

            transactionManager.commit();
            rollbackTransaction = false;

            /*
             * Sanity check
             */
            if (!FolderStorage.REAL_TREE_ID.equals(toCreate.getTreeID())) {
                final String duplicateId = DuplicateCleaner.cleanDuplicates(treeId, storageParameters, newId);
                if (null != duplicateId) {
                    throw FolderExceptionErrorMessage.EQUAL_NAME.create(toCreate.getName(), parent.getLocalizedName(getLocale()), treeId);
                    // return duplicateId;
                }
            }

            final Set<OXException> warnings = storageParameters.getWarnings();
            if (null != warnings) {
                for (final OXException warning : warnings) {
                    addWarning(warning);
                }
            }

            return newId;
        } catch (final OXException e) {
            throw e;
        } catch (final Exception e) {
            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            if (rollbackTransaction) {
                transactionManager.rollback();
            }
        }
    }

    private String doCreateReal(final Folder toCreate, final String parentId, final String treeId, final FolderStorage parentStorage, final TransactionManager transactionManager, final ComparedFolderPermissions comparedPermissions) throws OXException {
        final ContentType[] contentTypes = parentStorage.getSupportedContentTypes();
        boolean supported = false;
        final ContentType folderContentType = toCreate.getContentType();
        if (0 < contentTypes.length) {
            final String cts = folderContentType.toString();
            for (final ContentType contentType : contentTypes) {
                if (contentType.toString().equals(cts)) {
                    supported = true;
                    break;
                }
            }
        } else {
            /*
             * A zero length array means this folder storage supports all content types for a certain tree identifier.
             */
            supported = true;
        }
        if (!supported) {
            /*
             * Real tree is not capable to create a folder of an unsupported content type
             */
            throw FolderExceptionErrorMessage.INVALID_CONTENT_TYPE.create(parentId, folderContentType.toString(), treeId, Integer.valueOf(user.getId()), Integer.valueOf(context.getContextId()));
        }

        /*
         * Check permissions of anonymous guest users
         */
        checkGuestPermissions(toCreate, comparedPermissions);

        if (comparedPermissions.hasNewGuests()) {
            /*
             * create "plain" folder without guests first...
             */
            List<Permission> plainPermissions = comparedPermissions.getAddedPermissions();
            Folder plainFolder = (Folder) toCreate.clone();
            plainFolder.setPermissions(plainPermissions.toArray(new Permission[plainPermissions.size()]));
            parentStorage.createFolder(plainFolder, storageParameters);
            String folderID = plainFolder.getID();
            /*
             * setup shares and guest users, enrich previously skipped guest permissions with real entities
             */
            processAddedGuestPermissions(folderID, plainFolder.getContentType(), comparedPermissions, transactionManager.getConnection());
            /*
             * update with re-added guest permissions
             */
            toCreate.setID(folderID);
            toCreate.setLastModified(plainFolder.getLastModified());
            parentStorage.updateFolder(toCreate, storageParameters);
        } else {
            parentStorage.createFolder(toCreate, storageParameters);
        }
        return toCreate.getID();
    }

    private String doCreateVirtual(final Folder toCreate, final String parentId, final String treeId, final FolderStorage virtualStorage, final List<FolderStorage> openedStorages, final TransactionManager transactionManager, ComparedFolderPermissions comparedPermissions) throws OXException {
        final ContentType folderContentType = toCreate.getContentType();
        final FolderStorage realStorage = folderStorageDiscoverer.getFolderStorage(FolderStorage.REAL_TREE_ID, parentId);
        if (realStorage.equals(virtualStorage)) {
            doCreateReal(toCreate, parentId, FolderStorage.REAL_TREE_ID, realStorage, transactionManager, comparedPermissions);
        } else {
            /*
             * Check if real storage supports folder's content types
             */
            if (supportsContentType(folderContentType, realStorage)) {
                checkOpenedStorage(realStorage, openedStorages);
                /*
                 * 1. Create in real storage
                 */
                doCreateReal(toCreate, parentId, FolderStorage.REAL_TREE_ID, realStorage, transactionManager, comparedPermissions);
                /*
                 * 2. Create in virtual storage
                 */
                // TODO: Pass this one? final Folder created = realStorage.getFolder(treeId, toCreate.getID(), storageParameters);
                virtualStorage.createFolder(toCreate, storageParameters);
            } else {
                /*
                 * Find the real storage which is capable to create the folder
                 */
                final FolderStorage capStorage =
                    folderStorageDiscoverer.getFolderStorageByContentType(FolderStorage.REAL_TREE_ID, folderContentType);
                if (null == capStorage) {
                    throw FolderExceptionErrorMessage.NO_STORAGE_FOR_CT.create(FolderStorage.REAL_TREE_ID, folderContentType.toString());
                }
                checkOpenedStorage(capStorage, openedStorages);
                /*
                 * Special handling for mail folders on root level
                 */
                if (FolderStorage.PRIVATE_ID.equals(parentId) && CONTENT_TYPE_MAIL.equals(folderContentType.toString())) {
                    final String rootId = MailFolderUtility.prepareFullname(MailAccount.DEFAULT_ID, MailFolder.DEFAULT_FOLDER_ID);
                    /*
                     * Check if create is allowed
                     */
                    final Permission rootPermission;
                    {
                        final Folder rootFolder = capStorage.getFolder(treeId, rootId, storageParameters);
                        final List<ContentType> contentTypes = Collections.<ContentType> emptyList();
                        rootPermission = CalculatePermission.calculate(rootFolder, this, contentTypes);
                    }
                    if (rootPermission.getFolderPermission() >= Permission.CREATE_SUB_FOLDERS) {
                        /*
                         * Creation of subfolders is allowed
                         */
                        final Folder clone4Real = (Folder) toCreate.clone();
                        clone4Real.setParentID(rootId);
                        capStorage.createFolder(clone4Real, storageParameters);
                        toCreate.setID(clone4Real.getID());
                        /*
                         * Update parent's last-modified time stamp
                         */
                        final boolean started = realStorage.startTransaction(storageParameters, true);
                        try {
                            realStorage.updateLastModified(System.currentTimeMillis(), FolderStorage.REAL_TREE_ID, parentId, storageParameters);
                            if (started) {
                                realStorage.commitTransaction(storageParameters);
                            }
                        } catch (final OXException e) {
                            if (started) {
                                realStorage.rollback(storageParameters);
                            }
                            throw e;
                        } catch (final Exception e) {
                            if (started) {
                                realStorage.rollback(storageParameters);
                            }
                            throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                        }
                        OutlookFolderStorage.clearTCM();
                        return toCreate.getID();
                    }
                }
                /*
                 * 1. Create at default location in capable real storage
                 */
                {
                    final String realParentId =
                        virtualStorage.getDefaultFolderID(
                            user,
                            treeId,
                            null == folderContentType ? capStorage.getDefaultContentType() : folderContentType,
                                virtualStorage.getTypeByParent(user, treeId, parentId, storageParameters),
                                storageParameters);
                    if (null == realParentId) {
                        /*
                         * No default folder found
                         */
                        throw FolderExceptionErrorMessage.NO_DEFAULT_FOLDER.create(
                            capStorage.getDefaultContentType(),
                            FolderStorage.REAL_TREE_ID);
                    }

                    // TODO: Check permission for obtained default folder ID?
                    final Folder clone4Real = (Folder) toCreate.clone();
                    clone4Real.setParentID(realParentId);
                    {
                        /*
                         * Ensure no duplicate
                         */
                        final SortableId[] subfolders =
                            capStorage.getSubfolders(FolderStorage.REAL_TREE_ID, realParentId, storageParameters);
                        final String prefix = clone4Real.getName();
                        int appendixCount = 2;
                        while (true) {
                            boolean found = false;
                            final String n = clone4Real.getName();
                            for (int i = 0; !found && i < subfolders.length; i++) {
                                if (n.equals(capStorage.getFolder(FolderStorage.REAL_TREE_ID, subfolders[i].getId(), storageParameters).getName())) {
                                    found = true;
                                    clone4Real.setName(new StringBuilder(prefix).append('_').append(appendixCount++).toString());
                                }
                            }
                            if (!found) {
                                break;
                            }
                        }
                    }
                    doCreateReal(clone4Real, realParentId, FolderStorage.REAL_TREE_ID, capStorage, transactionManager, comparedPermissions);
                    toCreate.setID(clone4Real.getID());
                }
                /*
                 * 2. Create in virtual storage
                 */
                virtualStorage.createFolder(toCreate, storageParameters);
                /*
                 * 3. Update parent's last-modified time stamp
                 */
                final boolean started = realStorage.startTransaction(storageParameters, true);
                try {
                    realStorage.updateLastModified(System.currentTimeMillis(), FolderStorage.REAL_TREE_ID, parentId, storageParameters);
                    if (started) {
                        realStorage.commitTransaction(storageParameters);
                    }
                } catch (final OXException e) {
                    if (started) {
                        realStorage.rollback(storageParameters);
                    }
                    throw e;
                } catch (final Exception e) {
                    if (started) {
                        realStorage.rollback(storageParameters);
                    }
                    throw FolderExceptionErrorMessage.UNEXPECTED_ERROR.create(e, e.getMessage());
                }
            }
        }
        return toCreate.getID();
        // TODO: Check for storage capabilities! Does storage support permissions? Etc.
    }

    private void checkOpenedStorage(final FolderStorage storage, final List<FolderStorage> openedStorages) throws OXException {
        for (final FolderStorage openedStorage : openedStorages) {
            if (openedStorage.equals(storage)) {
                return;
            }
        }
        if (storage.startTransaction(storageParameters, true)) {
            openedStorages.add(storage);
        }
    }

    private static boolean supportsContentType(final ContentType folderContentType, final FolderStorage folderStorage) {
        final ContentType[] supportedContentTypes = folderStorage.getSupportedContentTypes();
        if (null == supportedContentTypes) {
            return false;
        }
        if (0 == supportedContentTypes.length) {
            return true;
        }
        final String cts = folderContentType.toString();
        for (final ContentType supportedContentType : supportedContentTypes) {
            if (supportedContentType.toString().equals(cts)) {
                return true;
            }
        }
        return false;
    }

}
